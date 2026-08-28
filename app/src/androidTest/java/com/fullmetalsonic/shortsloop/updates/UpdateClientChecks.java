package com.fullmetalsonic.shortsloop.updates;

import android.content.Context;
import android.content.SharedPreferences;
import com.fullmetalsonic.shortsloop.core.UpdatePolicy;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import org.json.JSONArray;
import org.json.JSONObject;

/** Offline integration checks; never constructs the real HTTPS transport or starts an installer. */
public final class UpdateClientChecks {
    private static final BooleanSupplier NEVER_CANCELLED = () -> false;
    private int passed;
    private final File temporary;

    private UpdateClientChecks(File temporary) { this.temporary = temporary; }

    /** Call while no activity/controller is checking updates; returns independent scenario count. */
    public static int run(Context context) throws Exception {
        File cache = context.getCacheDir().getCanonicalFile();
        File temporary = Files.createTempDirectory(cache.toPath(), "update-client-checks-").toFile();
        UpdateClientChecks suite = new UpdateClientChecks(temporary);
        try {
            suite.passed += UpdateMessageChecks.run(context);
            suite.clientChecks();
            suite.downloadChecks();
            suite.stateChecks(context);
            return suite.passed;
        } finally {
            removeOwned(temporary, temporary.getCanonicalFile());
        }
    }

    private void clientChecks() throws Exception {
        test("published prerelease / highest code, not list order", () -> {
            Release a = release(17), b = release(19), c = release(18);
            b.release.put("prerelease", true);
            UpdateManifest selected = fixture(a, b, c).client.check(35, 16, NEVER_CANCELLED);
            require(selected != null && selected.versionCode == 19 && selected.versionName.equals(b.version), "highest published code");
        });
        test("same and older versions ignored", () -> {
            require(fixture(release(16), release(15)).client.check(35, 16, NEVER_CANCELLED) == null, "same/older ignored");
        });
        test("minimum SDK filters without hiding compatible candidate", () -> {
            Release future = release(19); future.metadata.put("minSdk", 34);
            require(fixture(future, release(17)).client.check(33, 16, NEVER_CANCELLED).versionCode == 17, "compatible fallback");
            require(fixture(future).client.check(33, 16, NEVER_CANCELLED) == null, "unsupported only");
        });
        test("draft / legacy / non-product tag never fetch metadata", () -> {
            Release draft = release(19); draft.release.put("draft", true);
            Release old = release(18); old.release.put("assets", new JSONArray().put(old.apkAsset));
            Release other = release(17); other.release.put("tag_name", "experiment");
            Fixture f = fixture(draft, old, other);
            require(f.client.check(35, 16, NEVER_CANCELLED) == null && f.transport.opened.size() == 1, "non-candidates skipped");
        });
        test("malformed / wrong-root / trailing JSON is not latest", () -> {
            for (String text : new String[]{"{", "{}", "[] true"}) {
                Fixture f = fixture(); f.transport.put(GitHubUpdateClient.RELEASES_URL, bytes(text));
                expectIo(() -> f.client.check(35, 16, NEVER_CANCELLED));
            }
            Release r = release(17); r.rawMetadata = bytes(r.metadata.toString() + " false");
            rejected(r);
        });
        test("JSON type coercion is refused", () -> {
            for (String key : new String[]{"schema", "versionCode", "minSdk", "apkSize"}) {
                Release r = release(17); r.metadata.put(key, "17"); rejected(r);
            }
            Release fractional = release(17); fractional.metadata.put("versionCode", 17.5); rejected(fractional);
            Release bool = release(17); bool.release.put("draft", "false"); rejected(bool);
            Release asset = release(17); asset.apkAsset.put("size", "12"); rejected(asset);
        });
        test("required metadata / schema / numeric bounds", () -> {
            Release missing = release(17); missing.metadata.remove("versionCode"); rejected(missing);
            Release schema = release(17); schema.metadata.put("schema", 2); rejected(schema);
            for (long code : new long[]{0, -1, (long)Integer.MAX_VALUE + 1}) {
                Release r = release(17); r.metadata.put("versionCode", code); rejected(r);
            }
        });
        test("package and exact version/tag identity", () -> {
            Release pkg = release(17); pkg.metadata.put("packageName", "example.other"); rejected(pkg);
            Release tag = release(17); tag.metadata.put("versionName", "0.2.99"); rejected(tag);
            Release version = release(17); version.metadata.put("versionName", "0.2.17-test"); rejected(version);
        });
        test("all asset / page URLs stay in the same release", () -> {
            Release manifest = release(17); manifest.manifestAsset.put("browser_download_url", "https://example.invalid/meta.json"); rejected(manifest);
            Release apk = release(17); apk.apkAsset.put("browser_download_url", apk.apkUrl.replace("https:", "http:")); rejected(apk);
            Release otherTag = release(17); otherTag.apkAsset.put("browser_download_url", release(18).apkUrl); rejected(otherTag);
            Release page = release(17); page.release.put("html_url", "https://github.com/other/repo/releases/tag/v0.2.17"); rejected(page);
        });
        test("manifest byte count and APK metadata size agree", () -> {
            Release shortMeta = release(17); Fixture f = fixture(shortMeta);
            f.transport.put(shortMeta.manifestUrl, bytes("{}")); expectIo(() -> f.client.check(35, 16, NEVER_CANCELLED));
            Release apk = release(17); apk.apkAsset.put("size", apk.payload.length + 1); rejected(apk);
            Release oversized = release(17); oversized.metadata.put("apkSize", (long)UpdatePolicy.MAX_APK_BYTES + 1); rejected(oversized);
        });
        test("malformed SHA-256 rejected", () -> {
            Release r = release(17); r.metadata.put("sha256", "bad-hash"); rejected(r);
        });
        test("only uploaded manifest and APK accepted", () -> {
            Release manifest = release(17); manifest.manifestAsset.put("state", "starter"); rejected(manifest);
            Release apk = release(17); apk.apkAsset.put("state", "starter"); rejected(apk);
        });
        test("missing APK / duplicate asset names rejected", () -> {
            Release missing = release(17); missing.release.put("assets", new JSONArray().put(missing.manifestAsset)); rejected(missing);
            Release duplicate = release(17); duplicate.release.getJSONArray("assets").put(duplicate.apkAsset); rejected(duplicate);
        });
        test("direct parser rejects mismatched asset-map keys", () -> {
            Release r = release(17); byte[] encoded = r.metadataBytes();
            Map<String, UpdateManifest.Asset> assets = new HashMap<>();
            assets.put(UpdateManifest.MANIFEST_NAME, new UpdateManifest.Asset(UpdateManifest.MANIFEST_NAME, r.manifestUrl, encoded.length));
            assets.put(r.apkName, new UpdateManifest.Asset("other.apk", r.base + "other.apk", r.payload.length));
            expectIo(() -> UpdateManifest.parse(new String(encoded, StandardCharsets.UTF_8), r.tag, r.page, assets));
        });
        test("at most six manifests fetched", () -> {
            Release[] releases = new Release[7];
            for (int i = 0; i < releases.length; i++) releases[i] = release(17 + i);
            Fixture f = fixture(releases);
            require(f.client.check(35, 16, NEVER_CANCELLED).versionCode == 22, "only first six candidates considered");
            require(f.transport.opened.size() == 7 && !f.transport.opened.contains(releases[6].manifestUrl), "six metadata requests plus feed");
        });
        test("conflicting artifacts with same version code rejected", () -> {
            Release a = release(17), b = release(18); b.metadata.put("versionCode", 17);
            Fixture f = fixture(a, b); expectIo(() -> f.client.check(35, 16, NEVER_CANCELLED));
        });
        test("release and manifest response caps", () -> {
            Fixture feed = fixture(); feed.transport.put(GitHubUpdateClient.RELEASES_URL, new byte[2 * 1024 * 1024 + 1]);
            expectIo(() -> feed.client.check(35, 16, NEVER_CANCELLED));
            Release r = release(17); r.rawMetadata = new byte[UpdatePolicy.MAX_MANIFEST_BYTES + 1]; rejected(r);
        });
        test("invalid UTF-8 refused", () -> {
            Release r = release(17); r.rawMetadata = new byte[]{(byte)0xc3, (byte)0x28}; rejected(r);
        });
    }

    private void downloadChecks() throws Exception {
        test("verified download / fixed name / monotonic progress", () -> {
            Release r = release(17); Fixture f = fixture(r); UpdateManifest item = f.client.check(35, 16, NEVER_CANCELLED);
            File directory = directory("success"); List<Long> progress = new ArrayList<>();
            File output = f.client.download(item, directory, NEVER_CANCELLED, (received, total) -> {
                require(total == r.payload.length, "progress total");
                if (!progress.isEmpty()) require(received >= progress.get(progress.size() - 1), "monotonic progress");
                progress.add(received);
            });
            require(output.getCanonicalFile().equals(new File(directory, "update.apk").getCanonicalFile()), "fixed private output name");
            require(Arrays.equals(r.payload, Files.readAllBytes(output.toPath())), "exact downloaded bytes");
            require(progress.get(0) == 0 && progress.get(progress.size() - 1) == r.payload.length, "complete progress");
            require(!new File(directory, "update.apk.part").exists(), "no partial after success");
        });
        test("short / long downloads preserve previously verified APK", () -> {
            for (int delta : new int[]{-1, 1}) {
                Release r = release(17); Fixture f = fixture(r); UpdateManifest item = f.client.check(35, 16, NEVER_CANCELLED);
                File directory = directory("length" + delta); byte[] original = bytes("previous verified fixture");
                Files.write(new File(directory, "update.apk").toPath(), original);
                f.transport.put(r.apkUrl, Arrays.copyOf(r.payload, r.payload.length + delta));
                expectIo(() -> f.client.download(item, directory, NEVER_CANCELLED, null));
                require(Arrays.equals(original, Files.readAllBytes(new File(directory, "update.apk").toPath())), "old APK preserved");
                require(!new File(directory, "update.apk.part").exists(), "partial removed");
            }
        });
        test("same-size corrupted APK fails hash verification", () -> {
            Release r = release(17); Fixture f = fixture(r); UpdateManifest item = f.client.check(35, 16, NEVER_CANCELLED);
            byte[] changed = r.payload.clone(); changed[0] ^= 1; f.transport.put(r.apkUrl, changed);
            File directory = directory("corrupt"); expectIo(() -> f.client.download(item, directory, NEVER_CANCELLED, null));
            requireEmpty(directory);
        });
        test("cancel before and during write removes partial", () -> {
            Release r = release(17); Fixture f = fixture(r); UpdateManifest item = f.client.check(35, 16, NEVER_CANCELLED);
            File early = directory("cancel-before"); expectIo(() -> f.client.download(item, early, () -> true, null)); requireEmpty(early);
            File during = directory("cancel-during"); AtomicBoolean cancelled = new AtomicBoolean();
            expectIo(() -> f.client.download(item, during, cancelled::get, (received, total) -> { if (received > 0) cancelled.set(true); }));
            requireEmpty(during); require(f.transport.closed == f.transport.opened.size(), "all opened fixture streams closed");
        });
        test("transport failure cleans partial and releases download lock", () -> {
            Release r = release(17); Fixture f = fixture(r); UpdateManifest item = f.client.check(35, 16, NEVER_CANCELLED);
            f.transport.responses.remove(r.apkUrl); File directory = directory("network-failure");
            expectIo(() -> f.client.download(item, directory, NEVER_CANCELLED, null)); requireEmpty(directory);
            f.transport.put(r.apkUrl, r.payload);
            require(f.client.download(item, directory, NEVER_CANCELLED, null).isFile(), "retry can acquire lock");
        });
        test("manually constructed untrusted download denied before file IO", () -> {
            Release r = release(17); Fixture f = fixture(r); UpdateManifest item = f.client.check(35, 16, NEVER_CANCELLED);
            UpdateManifest bad = new UpdateManifest(item.packageName, item.versionCode, item.versionName, item.minSdk,
                    "../bad.apk", item.apkSize, item.apkUrl, item.sha256, item.releaseUrl);
            File directory = directory("untrusted"); int requests = f.transport.opened.size();
            expectIo(() -> f.client.download(bad, directory, NEVER_CANCELLED, null)); requireEmpty(directory);
            require(f.transport.opened.size() == requests, "invalid input cannot request bytes");
        });
    }

    private void stateChecks(Context context) throws Exception {
        SharedPreferences prefs = context.getSharedPreferences("updates", Context.MODE_PRIVATE);
        Map<String, Object> original = snapshot(prefs);
        try {
            test("update cache exact round-trip / new store instance", () -> {
                require(prefs.edit().clear().commit(), "clear fixture prefs");
                UpdateStateStore store = new UpdateStateStore(context);
                require(store.automatic() && store.lastAttempt() == 0 && store.candidate() == null, "safe defaults");
                Fixture f = fixture(release(17)); UpdateManifest item = f.client.check(35, 16, NEVER_CANCELLED);
                store.automatic(false); store.attempted(123456789L); store.candidate(item);
                UpdateStateStore restored = new UpdateStateStore(context);
                require(!restored.automatic() && restored.lastAttempt() == 123456789L, "saved check options");
                requireSame(item, restored.candidate());
            });
            test("damaged cache / wrong types / zero code fail closed", () -> {
                UpdateStateStore store = new UpdateStateStore(context);
                UpdateManifest item = fixture(release(17)).client.check(35, 16, NEVER_CANCELLED);
                for (String key : new String[]{"code", "min", "size"}) {
                    store.candidate(item); require(prefs.edit().putString(key, "broken").commit(), "corrupt numeric type");
                    require(store.candidate() == null, "wrong numeric type ignored");
                }
                for (String key : new String[]{"name", "asset", "url", "sha"}) {
                    store.candidate(item); require(prefs.edit().putString(key, "broken").commit(), "corrupt string value");
                    require(store.candidate() == null, "damaged identity ignored");
                }
                store.candidate(item); require(prefs.edit().putLong("code", 0).commit(), "zero cached code");
                require(store.candidate() == null, "zero cache is not a candidate");
                require(prefs.edit().putString("attempt", "broken").putString("automatic", "broken").commit(), "corrupt control types");
                require(store.lastAttempt() == 0 && store.automatic(), "safe control defaults");
            });
            test("clearing candidate preserves check preferences", () -> {
                UpdateStateStore store = new UpdateStateStore(context);
                store.automatic(false); store.attempted(98765); store.candidate(fixture(release(17)).client.check(35, 16, NEVER_CANCELLED));
                store.candidate(null);
                require(store.candidate() == null && !store.automatic() && store.lastAttempt() == 98765, "cache removal isolated");
            });
        } finally {
            restore(prefs, original);
        }
    }

    private void test(String name, Check check) throws Exception {
        try { check.run(); passed++; }
        catch (Throwable failure) { throw new AssertionError("UpdateClientChecks: " + name, failure); }
    }
    private interface Check { void run() throws Exception; }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    private static void expectIo(Check check) throws Exception {
        try { check.run(); } catch (IOException expected) { return; }
        throw new AssertionError("Expected IOException, not a successful/latest result");
    }
    private static void rejected(Release release) throws Exception {
        Fixture f = fixture(release); expectIo(() -> f.client.check(35, 16, NEVER_CANCELLED));
    }
    private File directory(String name) throws IOException {
        File result = new File(temporary, name);
        if (!result.mkdir()) throw new IOException("Could not create owned fixture directory");
        return result;
    }
    private static void requireEmpty(File directory) {
        String[] files = directory.list(); require(files != null && files.length == 0, "failed download leaves no files");
    }
    private static byte[] bytes(String text) { return text.getBytes(StandardCharsets.UTF_8); }
    private static Release release(int code) throws Exception { return new Release(code); }
    private static Fixture fixture(Release... releases) throws Exception {
        Fixture result = new Fixture(); JSONArray feed = new JSONArray();
        for (Release release : releases) {
            byte[] metadata = release.metadataBytes(); release.manifestAsset.put("size", metadata.length);
            result.transport.put(release.manifestUrl, metadata); result.transport.put(release.apkUrl, release.payload);
            feed.put(release.release);
        }
        result.transport.put(GitHubUpdateClient.RELEASES_URL, bytes(feed.toString()));
        return result;
    }
    private static final class Fixture {
        final MemoryTransport transport = new MemoryTransport();
        final GitHubUpdateClient client = new GitHubUpdateClient(transport);
    }
    private static final class MemoryTransport implements UpdateTransport {
        final Map<String, byte[]> responses = new HashMap<>();
        final List<String> opened = new ArrayList<>();
        int closed;
        void put(String url, byte[] body) { responses.put(url, body.clone()); }
        @Override public InputStream open(String url, BooleanSupplier cancelled) throws IOException {
            byte[] data = responses.get(url);
            if (data == null) throw new IOException("No offline fixture response");
            opened.add(url);
            return new ByteArrayInputStream(data) {
                private boolean finished;
                @Override public void close() throws IOException { if (!finished) { finished = true; closed++; } super.close(); }
            };
        }
    }
    private static final class Release {
        final String version, tag, page, base, apkName, apkUrl, manifestUrl;
        final JSONObject release, metadata, manifestAsset, apkAsset;
        final byte[] payload = new byte[40 * 1024];
        byte[] rawMetadata;
        Release(int code) throws Exception {
            version = "0.2." + code; tag = "v" + version;
            page = "https://github.com/" + UpdatePolicy.REPOSITORY + "/releases/tag/" + tag;
            base = "https://github.com/" + UpdatePolicy.REPOSITORY + "/releases/download/" + tag + "/";
            apkName = "shorts-loop-" + version + ".apk"; apkUrl = base + apkName;
            manifestUrl = base + UpdateManifest.MANIFEST_NAME;
            for (int i = 0; i < payload.length; i++) payload[i] = (byte)(i * 31 + code);
            metadata = new JSONObject().put("schema", 1).put("packageName", UpdatePolicy.PACKAGE)
                    .put("versionCode", code).put("versionName", version).put("minSdk", 26)
                    .put("apkName", apkName).put("apkSize", payload.length).put("sha256", UpdatePolicy.sha256(payload));
            manifestAsset = asset(UpdateManifest.MANIFEST_NAME, manifestUrl, 0);
            apkAsset = asset(apkName, apkUrl, payload.length);
            release = new JSONObject().put("draft", false).put("prerelease", false).put("tag_name", tag)
                    .put("html_url", page).put("assets", new JSONArray().put(manifestAsset).put(apkAsset));
        }
        byte[] metadataBytes() { return rawMetadata != null ? rawMetadata : bytes(metadata.toString()); }
        private static JSONObject asset(String name, String url, long size) throws Exception {
            return new JSONObject().put("name", name).put("browser_download_url", url).put("size", size).put("state", "uploaded");
        }
    }
    private static void requireSame(UpdateManifest a, UpdateManifest b) {
        require(b != null && a.packageName.equals(b.packageName) && a.versionCode == b.versionCode
                && a.versionName.equals(b.versionName) && a.minSdk == b.minSdk && a.apkName.equals(b.apkName)
                && a.apkSize == b.apkSize && a.apkUrl.equals(b.apkUrl) && a.sha256.equals(b.sha256)
                && a.releaseUrl.equals(b.releaseUrl), "exact cached manifest");
    }
    private static Map<String, Object> snapshot(SharedPreferences prefs) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            Object value = entry.getValue(); result.put(entry.getKey(), value instanceof Set ? new HashSet<>((Set<?>)value) : value);
        }
        return result;
    }
    @SuppressWarnings("unchecked")
    private static void restore(SharedPreferences prefs, Map<String, Object> original) {
        SharedPreferences.Editor editor = prefs.edit().clear();
        for (Map.Entry<String, Object> entry : original.entrySet()) {
            String key = entry.getKey(); Object value = entry.getValue();
            if (value instanceof Boolean) editor.putBoolean(key, (Boolean)value);
            else if (value instanceof Integer) editor.putInt(key, (Integer)value);
            else if (value instanceof Long) editor.putLong(key, (Long)value);
            else if (value instanceof Float) editor.putFloat(key, (Float)value);
            else if (value instanceof String) editor.putString(key, (String)value);
            else if (value instanceof Set) editor.putStringSet(key, new HashSet<>((Set<String>)value));
            else throw new AssertionError("Unsupported original preference type; not silently dropping it");
        }
        require(editor.commit() && snapshot(prefs).equals(original), "original update preferences restored exactly");
    }
    private static void removeOwned(File entry, File root) throws IOException {
        // Never walk outside this invocation's unique cache child, including through symlinks.
        if (Files.isSymbolicLink(entry.toPath())) throw new IOException("Unexpected fixture symlink; cleanup stopped");
        File canonical = entry.getCanonicalFile();
        if (!canonical.equals(root) && !canonical.toPath().startsWith(root.toPath())) throw new IOException("Cleanup escaped owned fixture root");
        if (entry.isDirectory()) {
            File[] children = entry.listFiles(); if (children == null) throw new IOException("Cannot enumerate owned fixture directory");
            for (File child : children) removeOwned(child, root);
        }
        Files.deleteIfExists(entry.toPath());
    }
}
