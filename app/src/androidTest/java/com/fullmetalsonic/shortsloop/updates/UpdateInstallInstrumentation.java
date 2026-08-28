package com.fullmetalsonic.shortsloop.updates;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import com.fullmetalsonic.shortsloop.core.UpdatePolicy;
import com.fullmetalsonic.shortsloop.ui.MainActivity;
import com.fullmetalsonic.shortsloop.ui.SettingsScreen;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import org.json.JSONArray;
import org.json.JSONObject;

/** Test-APK-only manual install harness. No alternate endpoint exists in the product. */
public final class UpdateInstallInstrumentation extends Instrumentation {
    private static final String FINAL_ASSET = "final-update.apk";
    private static final String FINAL_VERSION = "0.2.5";
    private static final long FINAL_CODE = 16;
    private static final String PUBLIC_APK = "shorts-loop-v0.2.4-debug.apk";
    private static final long PUBLIC_BYTES = 681624;
    private static final String PUBLIC_SHA = "d2846eb1f935f5886dee37cc5d2ea877c7e58db0019ec0286e8afefa7db92944";
    private Bundle arguments;
    private Activity activity;
    private UpdateController injected;
    private SettingsScreen screen;
    private SharedPreferences prefs;
    private Map<String, Object> original;

    @Override public void onCreate(Bundle arguments) {
        super.onCreate(arguments); this.arguments = arguments == null ? new Bundle() : new Bundle(arguments); start();
    }

    @Override public void onStart() {
        Bundle result = new Bundle(); int resultCode = Activity.RESULT_CANCELED;
        try {
            String mode = arguments.getString("mode", "fixture");
            if ("network".equals(mode)) {
                networkCheck(result); resultCode = Activity.RESULT_OK;
            } else if ("preflight".equals(mode)) {
                preflightCheck(result); resultCode = Activity.RESULT_OK;
            } else if ("fixture".equals(mode)) {
                fixtureCheck();
                result.putString("result", "TIMEOUT_NOT_VERIFIED");
                result.putString("message", "Manual install observation timed out. This is not an installation PASS.");
            } else throw new IOException("Only fixture, preflight and network modes are supported");
        } catch (Throwable error) {
            result.putString("result", "FAIL"); result.putString("error", error.getClass().getSimpleName() + ": " + error.getMessage());
        } finally {
            try {
                if (activity != null) onMain(() -> { if (injected != null) injected.close(); activity.finish(); });
                if (prefs != null && original != null) restore(prefs, original);
            } catch (Throwable cleanup) {
                resultCode = Activity.RESULT_CANCELED; result.putString("result", "FAIL");
                result.putString("cleanup", "Could not fully restore test state; inspect the updates preferences.");
            }
            // Successful package replacement normally kills this process before finally/finish.
            // The caller must then verify installed code/signature/settings and restore original prefs.
            finish(resultCode, result);
        }
    }

    private void fixtureCheck() throws Exception {
        Context target = getTargetContext();
        require(installedCode(target) == 15, "Manual fixture requires the approved code15 bootstrap");
        byte[] apk = finalAsset();
        String hash = UpdatePolicy.sha256(apk);
        int preflightChecks = runPreflight(apk, hash);
        GitHubUpdateClient client = new GitHubUpdateClient(fixture(apk, hash));
        prefs = target.getSharedPreferences("updates", Context.MODE_PRIVATE); original = snapshot(prefs);
        require(prefs.edit().putBoolean("automatic", false).remove("code").commit(), "Cannot disable automatic network lookup for test");
        activity = startActivitySync(new Intent(target, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        waitForIdleSync();
        onMain(() -> {
            Field updater = MainActivity.class.getDeclaredField("updater"); updater.setAccessible(true);
            UpdateController previous = (UpdateController)updater.get(activity); if (previous != null) previous.close();
            Field layout = MainActivity.class.getDeclaredField("screen"); layout.setAccessible(true);
            screen = (SettingsScreen)layout.get(activity);
            injected = new UpdateController(activity, screen.updates, screen.updateBanner, screen::showUpdates, client);
            updater.set(activity, injected);
            injected.onResume(); injected.check(true); screen.root.post(screen::showUpdates);
        });
        long readyDeadline = SystemClock.elapsedRealtime() + 30_000;
        AtomicBoolean ready = new AtomicBoolean();
        while (!ready.get() && SystemClock.elapsedRealtime() < readyDeadline) {
            onMain(() -> ready.set(screen.updates.action.getVisibility() == View.VISIBLE && screen.updates.action.isEnabled()));
            if (!ready.get()) Thread.sleep(100);
        }
        require(ready.get(), "Fixture check did not expose a usable update action");
        UpdateManifest candidate = new UpdateStateStore(target).candidate();
        require(candidate != null && candidate.versionCode == FINAL_CODE && hash.equals(candidate.sha256), "Fixture candidate identity mismatch");
        Bundle status = new Bundle(); status.putString("result", "FIXTURE_READY");
        status.putString("message", "Production update UI is ready for manual download, permission, and installation. This is NOT an installation PASS.");
        status.putLong("expectedCode", FINAL_CODE); status.putString("expectedVersion", FINAL_VERSION);
        status.putLong("bytes", apk.length); status.putString("sha256", hash);
        status.putInt("checks", preflightChecks);
        status.putString("restoration", "If package replacement kills instrumentation, caller must restore the original updates preferences.");
        sendStatus(1, status);
        long deadline = SystemClock.elapsedRealtime() + 10 * 60_000L;
        while (SystemClock.elapsedRealtime() < deadline) {
            AtomicBoolean alive = new AtomicBoolean();
            onMain(() -> alive.set(!activity.isFinishing() && !activity.isDestroyed()));
            require(alive.get(), "Fixture Activity ended/recreated; installation remains externally unverified");
            Thread.sleep(250);
        }
    }

    private void preflightCheck(Bundle result) throws Exception {
        require(installedCode(getTargetContext()) == 15, "Preflight requires the approved code15 bootstrap");
        byte[] apk = finalAsset(); String hash = UpdatePolicy.sha256(apk);
        int checks = runPreflight(apk, hash);
        result.putString("result", "PASS"); result.putInt("checks", checks);
        result.putString("scope", "Installer artifact preflight only. No installation attempted.");
        result.putLong("bytes", apk.length); result.putString("sha256", hash);
        result.putLong("expectedCode", FINAL_CODE); result.putString("expectedVersion", FINAL_VERSION);
        sendStatus(1, new Bundle(result));
    }

    private int runPreflight(byte[] apk, String hash) throws Exception {
        Context target = getTargetContext();
        UpdateManifest item = new GitHubUpdateClient(fixture(apk, hash)).check(Build.VERSION.SDK_INT, 15, () -> false);
        require(item != null && item.versionCode == FINAL_CODE, "Missing compatible preflight metadata");
        File root = Files.createTempDirectory(target.getCacheDir().toPath(), "update-install-preflight-").toFile().getCanonicalFile();
        File validApk = new File(root, FINAL_ASSET);
        try {
            Files.write(validApk.toPath(), apk);
            return InstallerArtifactChecks.run(target, validApk, item);
        } finally {
            require(!Files.isSymbolicLink(validApk.toPath()) && root.equals(validApk.getCanonicalFile().getParentFile()), "Unsafe preflight cleanup path");
            Files.deleteIfExists(validApk.toPath()); Files.delete(root.toPath());
        }
    }

    private byte[] finalAsset() throws IOException {
        byte[] apk;
        try (InputStream input = getContext().getAssets().open(FINAL_ASSET)) { apk = readBounded(input); }
        require(apk.length > 0, "Missing final update asset");
        return apk;
    }

    private void networkCheck(Bundle result) throws Exception {
        Context target = getTargetContext(); GitHubUpdateClient real = new GitHubUpdateClient();
        UpdateManifest found = real.check(Build.VERSION.SDK_INT, installedCode(target), () -> false);
        File root = Files.createTempDirectory(target.getCacheDir().toPath(), "update-network-check-").toFile().getCanonicalFile();
        try {
            String page = "https://github.com/" + UpdatePolicy.REPOSITORY + "/releases/tag/v0.2.4";
            String url = "https://github.com/" + UpdatePolicy.REPOSITORY + "/releases/download/v0.2.4/" + PUBLIC_APK;
            UpdateManifest publicItem = new UpdateManifest(UpdatePolicy.PACKAGE, 13, "0.2.4", 26,
                    PUBLIC_APK, PUBLIC_BYTES, url, PUBLIC_SHA, page);
            File downloaded = real.download(publicItem, root, () -> false, null);
            require(downloaded.length() == PUBLIC_BYTES && PUBLIC_SHA.equals(UpdatePolicy.sha256(Files.readAllBytes(downloaded.toPath()))),
                    "Known public artifact did not match");
            result.putString("result", "PASS"); result.putString("scope", "Real GitHub query and known public APK download/hash only. No installation attempted.");
            result.putLong("bytes", downloaded.length()); result.putString("sha256", PUBLIC_SHA);
            result.putLong("compatibleNewerCode", found == null ? 0 : found.versionCode);
            sendStatus(1, new Bundle(result));
        } finally { removeNetworkFiles(root); }
    }

    private static UpdateTransport fixture(byte[] apk, String hash) throws Exception {
        String tag = "v" + FINAL_VERSION, name = "shorts-loop-v" + FINAL_VERSION + "-debug.apk";
        String base = "https://github.com/" + UpdatePolicy.REPOSITORY + "/releases/download/" + tag + "/";
        String page = "https://github.com/" + UpdatePolicy.REPOSITORY + "/releases/tag/" + tag;
        JSONObject metadata = new JSONObject().put("schema", 1).put("packageName", UpdatePolicy.PACKAGE)
                .put("versionCode", FINAL_CODE).put("versionName", FINAL_VERSION).put("minSdk", 26)
                .put("apkName", name).put("apkSize", apk.length).put("sha256", hash);
        byte[] json = metadata.toString().getBytes(StandardCharsets.UTF_8);
        JSONObject release = new JSONObject().put("draft", false).put("prerelease", true).put("tag_name", tag).put("html_url", page)
                .put("assets", new JSONArray().put(asset(UpdateManifest.MANIFEST_NAME, base + UpdateManifest.MANIFEST_NAME, json.length))
                        .put(asset(name, base + name, apk.length)));
        Map<String, byte[]> responses = new HashMap<>();
        responses.put(GitHubUpdateClient.RELEASES_URL, new JSONArray().put(release).toString().getBytes(StandardCharsets.UTF_8));
        responses.put(base + UpdateManifest.MANIFEST_NAME, json); responses.put(base + name, apk);
        return new UpdateTransport() {
            @Override public InputStream open(String url, BooleanSupplier cancelled) throws IOException {
                if (cancelled != null && cancelled.getAsBoolean()) throw new IOException("Fixture cancelled");
                byte[] body = responses.get(url); if (body == null) throw new IOException("No matching offline fixture");
                return new ByteArrayInputStream(body);
            }
        };
    }
    private static JSONObject asset(String name, String url, long bytes) throws Exception {
        return new JSONObject().put("name", name).put("browser_download_url", url).put("size", bytes).put("state", "uploaded");
    }
    @SuppressWarnings("deprecation")
    private static long installedCode(Context context) throws Exception {
        PackageInfo info = context.getPackageManager().getPackageInfo(UpdatePolicy.PACKAGE, 0);
        return Build.VERSION.SDK_INT >= 28 ? info.getLongVersionCode() : info.versionCode;
    }
    private static byte[] readBounded(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(); byte[] buffer = new byte[16 * 1024]; int count;
        while ((count = input.read(buffer)) >= 0) {
            if (output.size() + (long)count > UpdatePolicy.MAX_APK_BYTES) throw new IOException("Final fixture exceeds APK limit");
            output.write(buffer, 0, count);
        }
        return output.toByteArray();
    }
    private interface MainAction { void run() throws Exception; }
    private void onMain(MainAction action) throws Exception {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        runOnMainSync(() -> { try { action.run(); } catch (Throwable error) { failure.set(error); } });
        Throwable error = failure.get();
        if (error instanceof Exception) throw (Exception)error;
        if (error != null) throw new AssertionError("Main-thread fixture failed", error);
    }
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private static Map<String, Object> snapshot(SharedPreferences preferences) {
        Map<String, Object> copy = new HashMap<>();
        for (Map.Entry<String, ?> entry : preferences.getAll().entrySet()) {
            Object value = entry.getValue(); copy.put(entry.getKey(), value instanceof Set ? new HashSet<>((Set<?>)value) : value);
        }
        return copy;
    }
    @SuppressWarnings("unchecked")
    private static void restore(SharedPreferences preferences, Map<String, Object> values) {
        SharedPreferences.Editor editor = preferences.edit().clear();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String key = entry.getKey(); Object value = entry.getValue();
            if (value instanceof Boolean) editor.putBoolean(key, (Boolean)value);
            else if (value instanceof Integer) editor.putInt(key, (Integer)value);
            else if (value instanceof Long) editor.putLong(key, (Long)value);
            else if (value instanceof Float) editor.putFloat(key, (Float)value);
            else if (value instanceof String) editor.putString(key, (String)value);
            else if (value instanceof Set) editor.putStringSet(key, new HashSet<>((Set<String>)value));
            else throw new AssertionError("Cannot restore unknown original preference type");
        }
        require(editor.commit() && snapshot(preferences).equals(values), "Original update preferences not restored exactly");
    }
    private static void removeNetworkFiles(File root) throws IOException {
        // Fixed names only inside this invocation's freshly created cache child; no recursive delete.
        for (String name : new String[]{"update.apk.part", "update.apk"}) {
            File file = new File(root, name);
            require(!Files.isSymbolicLink(file.toPath()) && root.equals(file.getCanonicalFile().getParentFile()), "Unsafe test cleanup path");
            Files.deleteIfExists(file.toPath());
        }
        Files.delete(root.toPath());
    }
}
