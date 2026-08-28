package com.fullmetalsonic.shortsloop.updates;

import android.content.Context;
import android.content.SharedPreferences;
import com.fullmetalsonic.shortsloop.core.UpdatePolicy;

/** Separate from playback settings, so network progress cannot reset an active playback session. */
final class UpdateStateStore {
    private final SharedPreferences prefs;
    UpdateStateStore(Context context) { prefs = context.getSharedPreferences("updates", Context.MODE_PRIVATE); }
    long lastAttempt() { try { return prefs.getLong("attempt", 0); } catch (ClassCastException e) { return 0; } }
    void attempted(long value) { prefs.edit().putLong("attempt", value).apply(); }
    boolean automatic() { try { return prefs.getBoolean("automatic", true); } catch (ClassCastException e) { return true; } }
    void automatic(boolean value) { prefs.edit().putBoolean("automatic", value).apply(); }
    void candidate(UpdateManifest item) {
        SharedPreferences.Editor e = prefs.edit();
        if (item == null) { e.remove("code").apply(); return; }
        e.putLong("code", item.versionCode).putString("name", item.versionName).putInt("min", item.minSdk)
                .putString("asset", item.apkName).putString("url", item.apkUrl).putLong("size", item.apkSize)
                .putString("sha", item.sha256).putString("release", item.releaseUrl).apply();
    }
    UpdateManifest candidate() {
        try {
            long code = prefs.getLong("code", 0); String name = prefs.getString("name", "");
            String asset = prefs.getString("asset", ""), url = prefs.getString("url", ""), sha = prefs.getString("sha", "");
            String expected = "https://github.com/" + UpdatePolicy.REPOSITORY + "/releases/download/v" + name + "/" + asset;
            String release = "https://github.com/" + UpdatePolicy.REPOSITORY + "/releases/tag/v" + name;
            long size = prefs.getLong("size", 0); int min = prefs.getInt("min", 0);
            if (code <= 0 || code > Integer.MAX_VALUE || !UpdatePolicy.validVersionName(name)
                    || !UpdatePolicy.validAssetName(asset) || !asset.endsWith(".apk") || !expected.equals(url)
                    || !UpdatePolicy.trustedReleaseUrl(url) || !UpdatePolicy.validSha256(sha)
                    || min < 26 || size <= 0 || size > UpdatePolicy.MAX_APK_BYTES) return null;
            return new UpdateManifest(UpdatePolicy.PACKAGE, code, name, min, asset, size, url, sha, release);
        } catch (RuntimeException invalid) { return null; }
    }
}
