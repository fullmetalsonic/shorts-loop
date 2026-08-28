package com.fullmetalsonic.shortsloop.updates;

import com.fullmetalsonic.shortsloop.core.UpdatePolicy;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Bounded, unauthenticated GitHub release lookup and private-file download. */
public final class GitHubUpdateClient {
    public static final String RELEASES_URL = "https://api.github.com/repos/fullmetalsonic/shorts-loop/releases?per_page=20";
    private static final int MAX_RELEASES_BYTES = 2 * 1024 * 1024;
    private static final int MAX_MANIFESTS = 6;
    private static final long TOTAL_NANOS = TimeUnit.SECONDS.toNanos(120);
    private static final ReentrantLock DOWNLOAD_LOCK = new ReentrantLock();
    private final UpdateTransport transport;

    public interface Progress { void onProgress(long received, long total); }
    public GitHubUpdateClient() { this(new UpdateTransport.DefaultHttps()); }
    public GitHubUpdateClient(UpdateTransport transport) {
        if (transport == null) throw new IllegalArgumentException("Update transport is required");
        this.transport = transport;
    }

    /** Published prereleases are included; null means no compatible newer candidate in this bounded feed. */
    public UpdateManifest check(int sdk, long installedCode, BooleanSupplier cancelled) throws IOException {
        long deadline = System.nanoTime() + TOTAL_NANOS;
        try {
            Object decoded = UpdateManifest.decode(readText(RELEASES_URL, MAX_RELEASES_BYTES, -1, cancelled, deadline));
            if (!(decoded instanceof JSONArray)) throw UpdateManifest.invalid();
            JSONArray releases = (JSONArray) decoded;
            UpdateManifest newest = null;
            int checked = 0;
            for (int index = 0; index < releases.length() && checked < MAX_MANIFESTS; index++) {
                checkCancelled(cancelled, deadline);
                Object entry = releases.get(index);
                if (!(entry instanceof JSONObject)) throw UpdateManifest.invalid();
                JSONObject release = (JSONObject) entry;
                Object draft = release.get("draft");
                if (!(draft instanceof Boolean)) throw UpdateManifest.invalid();
                if ((Boolean) draft) continue;
                String tag = UpdateManifest.string(release, "tag_name");
                // Other release types are not product candidates; do not guess a version from a title.
                if (!tag.startsWith("v") || !UpdatePolicy.validVersionName(tag.substring(1))) continue;
                String page = UpdateManifest.string(release, "html_url");
                if (!UpdateManifest.releasePage(tag).equals(page)) throw UpdateManifest.invalid();
                Object assetsValue = release.get("assets");
                if (!(assetsValue instanceof JSONArray)) throw UpdateManifest.invalid();
                Map<String, UpdateManifest.Asset> assets = assets((JSONArray) assetsValue);
                UpdateManifest.Asset manifest = assets.get(UpdateManifest.MANIFEST_NAME);
                if (manifest == null) continue; // Old published versions predate the update manifest.
                if (!UpdateManifest.validAsset(manifest, tag) || manifest.size <= 0
                        || manifest.size > UpdatePolicy.MAX_MANIFEST_BYTES) throw UpdateManifest.invalid();
                checked++;
                UpdateManifest item = UpdateManifest.parse(readText(manifest.url, UpdatePolicy.MAX_MANIFEST_BYTES,
                        manifest.size, cancelled, deadline), tag, page, assets);
                if (UpdatePolicy.isNewer(installedCode, item.versionCode) && UpdatePolicy.compatible(sdk, item.minSdk)) {
                    if (newest != null && newest.versionCode == item.versionCode
                            && (!newest.sha256.equals(item.sha256) || !newest.apkUrl.equals(item.apkUrl))) throw UpdateManifest.invalid();
                    if (newest == null || item.versionCode > newest.versionCode) newest = item;
                }
            }
            checkCancelled(cancelled, deadline);
            return newest;
        } catch (JSONException exception) {
            throw UpdateManifest.invalid();
        }
    }

    /** Directory MUST be the caller's dedicated app-private update directory, never shared storage. */
    public File download(UpdateManifest item, File directory, BooleanSupplier cancelled, Progress progress) throws IOException {
        validateDownload(item);
        if (directory == null) throw new UpdateFailure(UpdateFailure.Code.STORAGE);
        if (!DOWNLOAD_LOCK.tryLock()) throw new UpdateFailure(UpdateFailure.Code.DOWNLOAD_BUSY);
        File part = null;
        boolean committed = false;
        try {
            long deadline = System.nanoTime() + TOTAL_NANOS;
            checkCancelled(cancelled, deadline);
            File root = directory.getCanonicalFile();
            if (root.getParentFile() == null || (!root.isDirectory() && !root.mkdirs()))
                throw new UpdateFailure(UpdateFailure.Code.STORAGE_PREPARE);
            part = child(root, "update.apk.part");
            File complete = child(root, "update.apk");
            Files.deleteIfExists(part.toPath());
            MessageDigest digest = digest();
            long received = 0;
            try (InputStream input = transport.open(item.apkUrl, cancelled);
                    FileOutputStream output = new FileOutputStream(part)) {
                if (progress != null) progress.onProgress(0, item.apkSize);
                byte[] buffer = new byte[16 * 1024];
                while (true) {
                    checkCancelled(cancelled, deadline);
                    int count = input.read(buffer);
                    checkCancelled(cancelled, deadline);
                    if (count < 0) break;
                    if (count == 0) continue;
                    received += count;
                    if (received > item.apkSize || received > UpdatePolicy.MAX_APK_BYTES)
                        throw new UpdateFailure(UpdateFailure.Code.SIZE_MISMATCH);
                    output.write(buffer, 0, count); digest.update(buffer, 0, count);
                    if (progress != null) progress.onProgress(received, item.apkSize);
                }
                if (received != item.apkSize || !hex(digest.digest()).equalsIgnoreCase(item.sha256))
                    throw new UpdateFailure(UpdateFailure.Code.INTEGRITY);
                output.getFD().sync();
            }
            checkCancelled(cancelled, deadline);
            try { Files.move(part.toPath(), complete.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException unavailable) {
                Files.move(part.toPath(), complete.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            committed = true;
            return complete;
        } catch (RuntimeException exception) {
            throw new UpdateFailure(UpdateFailure.Code.DOWNLOAD);
        } finally {
            try { if (!committed && part != null) Files.deleteIfExists(part.toPath()); }
            finally { DOWNLOAD_LOCK.unlock(); }
        }
    }

    private static void validateDownload(UpdateManifest item) throws IOException {
        if (item == null || !UpdatePolicy.PACKAGE.equals(item.packageName)
                || !UpdatePolicy.validVersionName(item.versionName) || item.versionCode <= 0
                || item.versionCode > Integer.MAX_VALUE || item.minSdk < 26
                || !UpdatePolicy.validAssetName(item.apkName) || !item.apkName.endsWith(".apk")
                || item.apkSize <= 0 || item.apkSize > UpdatePolicy.MAX_APK_BYTES || !UpdatePolicy.validSha256(item.sha256)
                || !UpdateManifest.releasePage("v" + item.versionName).equals(item.releaseUrl)
                || !UpdateManifest.validAsset(new UpdateManifest.Asset(item.apkName, item.apkUrl, item.apkSize), "v" + item.versionName))
            throw UpdateManifest.invalid();
    }

    private static File child(File root, String name) throws IOException {
        File result = new File(root, name);
        if (!root.equals(result.getCanonicalFile().getParentFile()))
            throw new UpdateFailure(UpdateFailure.Code.STORAGE_PATH);
        return result;
    }

    private Map<String, UpdateManifest.Asset> assets(JSONArray values) throws JSONException, IOException {
        Map<String, UpdateManifest.Asset> result = new HashMap<>();
        for (int index = 0; index < values.length(); index++) {
            Object value = values.get(index);
            if (!(value instanceof JSONObject)) throw UpdateManifest.invalid();
            JSONObject asset = (JSONObject) value;
            String name = UpdateManifest.string(asset, "name"), url = UpdateManifest.string(asset, "browser_download_url");
            long size = UpdateManifest.integer(asset, "size");
            String state = UpdateManifest.string(asset, "state");
            if (size < 0 || result.put(name, new UpdateManifest.Asset(name, url, size, "uploaded".equals(state))) != null)
                throw UpdateManifest.invalid();
        }
        return result;
    }

    private String readText(String url, long maximum, long expected, BooleanSupplier cancelled, long deadline) throws IOException {
        try (InputStream input = transport.open(url, cancelled);
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            while (true) {
                checkCancelled(cancelled, deadline);
                int count = input.read(buffer);
                checkCancelled(cancelled, deadline);
                if (count < 0) break;
                if (count == 0) continue;
                if (output.size() + (long) count > maximum || (expected >= 0 && output.size() + (long) count > expected))
                    throw new UpdateFailure(UpdateFailure.Code.MANIFEST_SIZE);
                output.write(buffer, 0, count);
            }
            if (expected >= 0 && output.size() != expected) throw UpdateManifest.invalid();
            try {
                return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(output.toByteArray())).toString();
            } catch (CharacterCodingException exception) { throw UpdateManifest.invalid(); }
        }
    }

    private static MessageDigest digest() throws IOException {
        try { return MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException unavailable) { throw new UpdateFailure(UpdateFailure.Code.HASH_UNAVAILABLE); }
    }
    private static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) { result.append(Character.forDigit((value >> 4) & 15, 16)); result.append(Character.forDigit(value & 15, 16)); }
        return result.toString();
    }
    private static void checkCancelled(BooleanSupplier cancelled, long deadline) throws IOException {
        if (Thread.currentThread().isInterrupted() || (cancelled != null && cancelled.getAsBoolean()))
            throw new InterruptedIOException("UPDATE_CANCELLED");
        if (System.nanoTime() - deadline >= 0) throw new UpdateFailure(UpdateFailure.Code.PROCESS_TIMEOUT);
    }
}
