package com.fullmetalsonic.shortsloop.updates;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import com.fullmetalsonic.shortsloop.core.UpdatePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/** Device-side checks only. The caller must close its updater and disable automatic checks first. */
public final class InstallerArtifactChecks {
    public static final int EXPECTED_CHECKS = 36;
    private int checks;

    private InstallerArtifactChecks() { }

    /** Uses bootstrap code 15 and final code 16; never starts an installer or changes permissions/settings. */
    public static int run(Context context, File validFinalApk, UpdateManifest item) throws Exception {
        return new InstallerArtifactChecks().execute(context, validFinalApk, item);
    }

    private int execute(Context context, File validFinalApk, UpdateManifest item) throws Exception {
        require(UpdatePolicy.PACKAGE.equals(context.getPackageName()), "Product context required"); // 1
        require(ApkVerifier.version(context.getPackageManager().getPackageInfo(context.getPackageName(), 0)) == 15,
                "Install bootstrap code 15 before final-APK checks"); // 2
        require(item != null && item.versionCode == 16, "Final candidate code 16 required"); // 3
        ApkVerifier.verify(context, validFinalApk, item); checks++; // 4: reject a bad fixture before mutations.

        try (Snapshot files = new Snapshot(context, validFinalApk, item)) {
            Files.createDirectories(files.source.getParentFile().toPath());
            Files.copy(files.fixture.toPath(), files.source.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(files.target.toPath());
            Files.deleteIfExists(files.pending.toPath());

            File prepared = InstallerArtifact.prepare(context, item);
            require(prepared.getCanonicalFile().equals(files.target.getCanonicalFile()), "Digest-named target"); // 5
            require(matches(prepared, item), "Normal prepare has verified final bytes"); // 6
            require(!files.pending.exists(), "Normal prepare removes staging file"); // 7
            require(InstallerArtifact.prepare(context, item).equals(prepared) && matches(prepared, item),
                    "Existing valid snapshot remains reusable"); // 8

            Files.write(files.target.toPath(), new byte[0]);
            require(matches(InstallerArtifact.prepare(context, item), item), "Broken existing snapshot recovers"); // 9
            Files.write(files.target.toPath(), new byte[0]);
            Files.write(files.pending.toPath(), new byte[]{4, 3, 2, 1});
            require(matches(InstallerArtifact.prepare(context, item), item), "Stale partial snapshot recovers"); // 10
            require(!files.pending.exists(), "Recovered snapshot removes stale staging file"); // 11

            corruptFirstByte(files.source);
            rejectIo(() -> InstallerArtifact.prepare(context, item), "Source hash mismatch rejected"); // 12
            require(matches(files.target, item), "Bad source cannot replace valid snapshot"); // 13
            Files.copy(files.fixture.toPath(), files.source.toPath(), StandardCopyOption.REPLACE_EXISTING);
            UpdateManifest wrongVersion = new UpdateManifest(item.packageName, item.versionCode + 1,
                    item.versionName, item.minSdk, item.apkName, item.apkSize, item.apkUrl, item.sha256, item.releaseUrl);
            rejectIo(() -> InstallerArtifact.prepare(context, wrongVersion), "APK/version metadata mismatch rejected"); // 14
            require(matches(files.target, item), "Wrong metadata cannot replace valid snapshot"); // 15

            ContentResolver resolver = context.getContentResolver();
            Uri uri = UpdateFileProvider.uri(context, item.sha256);
            require("application/vnd.android.package-archive".equals(resolver.getType(uri)), "Provider MIME type"); // 16
            try (Cursor cursor = resolver.query(uri, null, null, null, null)) {
                require(cursor != null && cursor.getCount() == 1 && cursor.moveToFirst(), "Provider query row"); // 17
                require("ShortsLoop-update.apk".equals(cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))),
                        "Provider display name"); // 18
                require(cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE)) == item.apkSize,
                        "Provider exact size"); // 19
            }
            require(item.sha256.equalsIgnoreCase(readProviderHash(resolver, uri)), "Provider reads exact verified snapshot"); // 20
            corruptFirstByte(files.source);
            require(item.sha256.equalsIgnoreCase(readProviderHash(resolver, uri)),
                    "Later source replacement cannot change install URI bytes"); // 21

            for (String mode : new String[]{"w", "rw", "rwt", "wa"}) {
                rejectProvider(() -> {
                    try (ParcelFileDescriptor descriptor = resolver.openFileDescriptor(uri, mode)) {
                        // Opening any writable descriptor would itself fail this check.
                    }
                }, "Provider refuses write mode " + mode); // 22..25
            }
            String base = "content://" + context.getPackageName() + ".updates";
            String leaf = "/" + files.target.getName();
            for (String path : new String[]{"/update.apk", leaf + ".part", "/../" + files.target.getName(),
                    "/%2e%2e/" + files.target.getName(), leaf + "?read=1", leaf + "#fragment", "/install-short.apk", "/"}) {
                rejectProvider(() -> {
                    try (ParcelFileDescriptor descriptor = resolver.openFileDescriptor(Uri.parse(base + path), "r")) {
                        // No other cache file, query, fragment, or traversal may be opened.
                    }
                }, "Provider refuses unsupported path"); // 26..33
            }
            rejectProvider(() -> resolver.insert(uri, new ContentValues()), "Provider refuses insert"); // 34
            rejectProvider(() -> resolver.update(uri, new ContentValues(), null, null), "Provider refuses update"); // 35
            rejectProvider(() -> resolver.delete(uri, null, null), "Provider refuses delete"); // 36
            if (checks != EXPECTED_CHECKS) throw new AssertionError("Installer check count changed: " + checks);
            return checks;
        }
    }

    private interface Action { void run() throws Exception; }
    private void rejectIo(Action action, String message) throws Exception {
        try { action.run(); }
        catch (IOException expected) { checks++; return; }
        throw new AssertionError(message);
    }
    private void rejectProvider(Action action, String message) throws Exception {
        try { action.run(); }
        catch (FileNotFoundException | IllegalArgumentException | UnsupportedOperationException | SecurityException expected) {
            checks++; return;
        }
        throw new AssertionError(message);
    }
    private void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
    private static void corruptFirstByte(File file) throws IOException {
        try (RandomAccessFile output = new RandomAccessFile(file, "rw")) {
            int original = output.read();
            if (original < 0) throw new IOException("Fixture must not be empty");
            output.seek(0); output.write(original ^ 0xff);
        }
    }
    private static boolean matches(File file, UpdateManifest item) throws Exception {
        return file.isFile() && file.length() == item.apkSize && item.sha256.equalsIgnoreCase(hash(file));
    }
    private static String readProviderHash(ContentResolver resolver, Uri uri) throws Exception {
        try (InputStream input = resolver.openInputStream(uri)) {
            if (input == null) throw new AssertionError("Provider returned no stream");
            return hash(input);
        }
    }
    private static String hash(File file) throws Exception {
        try (InputStream input = new FileInputStream(file)) { return hash(input); }
    }
    private static String hash(InputStream input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[32 * 1024]; int count;
        while ((count = input.read(buffer)) != -1) digest.update(buffer, 0, count);
        StringBuilder result = new StringBuilder(64);
        for (byte value : digest.digest()) {
            result.append(Character.forDigit((value >>> 4) & 15, 16));
            result.append(Character.forDigit(value & 15, 16));
        }
        return result.toString();
    }

    /** All originals are copied and hash-checked before the first product-cache mutation. */
    private static final class Snapshot implements AutoCloseable {
        final File root, fixture, source, target, pending;
        final boolean directoryExisted;
        final List<SavedFile> saved = new ArrayList<>();
        Snapshot(Context context, File validApk, UpdateManifest item) throws Exception {
            root = Files.createTempDirectory(context.getCacheDir().toPath(), "installer-artifact-checks-").toFile();
            fixture = new File(root, "fixture.apk");
            Files.copy(validApk.toPath(), fixture.toPath());
            if (!matches(fixture, item)) throw new IOException("Fixture backup verification failed; originals untouched");
            source = UpdateFileProvider.apk(context); target = UpdateFileProvider.installFile(context, item.sha256);
            pending = new File(target.getParentFile(), target.getName() + ".part");
            directoryExisted = source.getParentFile().exists();
            saved.add(new SavedFile(source, new File(root, "original-source.apk")));
            saved.add(new SavedFile(target, new File(root, "original-target.apk")));
            saved.add(new SavedFile(pending, new File(root, "original-pending.part")));
        }
        @Override public void close() throws Exception {
            Exception failure = null;
            for (SavedFile entry : saved) {
                try { entry.restore(); }
                catch (Exception error) { if (failure == null) failure = error; else failure.addSuppressed(error); }
            }
            // Keep all backup files for recovery if any original could not be restored.
            if (failure != null) throw failure;
            if (!directoryExisted) {
                try { Files.deleteIfExists(source.getParentFile().toPath()); }
                catch (java.nio.file.DirectoryNotEmptyException otherWorkPresent) { /* Never delete unrelated files. */ }
            }
            for (SavedFile entry : saved) Files.deleteIfExists(entry.backup.toPath());
            Files.deleteIfExists(fixture.toPath());
            Files.deleteIfExists(root.toPath());
        }
    }
    private static final class SavedFile {
        final File original, backup;
        final boolean existed;
        final String originalHash;
        SavedFile(File original, File backup) throws Exception {
            this.original = original; this.backup = backup; existed = original.exists();
            if (existed) {
                if (!original.isFile()) throw new IOException("Existing cache entry is not a regular file; originals untouched");
                originalHash = hash(original);
                Files.copy(original.toPath(), backup.toPath());
                if (!originalHash.equals(hash(backup))) throw new IOException("Original backup verification failed; originals untouched");
            } else originalHash = null;
        }
        void restore() throws Exception {
            if (existed) {
                if (!backup.isFile() || !originalHash.equals(hash(backup)))
                    throw new IOException("Recovery backup missing or changed; keep test backup directory");
                Files.copy(backup.toPath(), original.toPath(), StandardCopyOption.REPLACE_EXISTING);
                if (!originalHash.equals(hash(original))) throw new IOException("Original cache restoration verification failed");
            } else {
                Files.deleteIfExists(original.toPath());
                if (original.exists()) throw new IOException("Test cache entry could not be removed");
            }
        }
    }
}
