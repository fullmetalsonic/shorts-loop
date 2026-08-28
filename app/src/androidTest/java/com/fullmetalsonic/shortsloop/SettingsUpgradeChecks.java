package com.fullmetalsonic.shortsloop;

import android.app.Instrumentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import java.util.Map;

/** Test APK only: preserve legacy preferences and initialize host settings from code31 to code32. */
final class SettingsUpgradeChecks {
    @SuppressWarnings("deprecation")
    static Bundle run(Instrumentation test, String phase) {
        Bundle result = new Bundle();
        try {
            require(Build.PRODUCT.contains("sdk") || Build.PRODUCT.contains("emulator"), "Disposable emulator only");
            Context target = test.getTargetContext();
            PackageInfo info = target.getPackageManager().getPackageInfo(target.getPackageName(), 64);
            // Instrumentation runs as the target UID, not the separately installed test APK UID.
            // These fixture-only files live on disposable emulators; product code never reads them.
            SharedPreferences baseline = target.getSharedPreferences("upgrade_test_baseline", 0);
            SharedPreferences identity = target.getSharedPreferences("upgrade_test_identity", 0);
            SettingsStore store = new SettingsStore(target);
            if ("seed".equals(phase)) {
                require(info.versionCode == 31 && "0.2.9".equals(info.versionName), "Start with frozen code31");
                require((info.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == 0, "Previous public release is not debuggable");
                store.enabled(false); store.ceiling(7); store.target(3); store.tapMode(0); store.target(3);
                store.floatingEnabled(true); store.position(0.22f, 0.71f);
                store.selectedApp(SettingsStore.YOUTUBE_PACKAGE, true);
                store.selectedApp(SettingsStore.INSTAGRAM_PACKAGE, true);
                store.skipAds(true); store.skipLive(true); store.liveDelaySeconds(4);
                store.skipLong(true); store.longVideoSeconds(73);
                store.timedFallback(true); store.fallbackSeconds(17); store.visualAssist(false);
                store.photoEnabled(true); store.photoMode(1); store.photoWholeSeconds(4);
                store.photoSlideSeconds(6); store.photoFallback(true);
                require(store.preferences.edit().commit(), "Flush target preferences");
                require(target.getSharedPreferences("updates", 0).edit().putBoolean("automatic", false).commit(), "Disable fixture network checks");
                copy(baseline, store.preferences.getAll());
                require(identity.edit().clear().putInt("uid", info.applicationInfo.uid)
                        .putString("signer", info.signatures[0].toCharsString()).putBoolean("seeded", true).commit(), "Save identity");
            } else if ("verify".equals(phase)) {
                require(identity.getBoolean("seeded", false), "Seed phase is required");
                require(info.versionCode == 32 && "0.3.0".equals(info.versionName), "Updated to code32");
                require((info.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == 0, "Release debugging is disabled");
                require(info.applicationInfo.uid == identity.getInt("uid", -1), "Package UID preserved");
                require(info.signatures.length == 1 && info.signatures[0].toCharsString().equals(identity.getString("signer", "")), "Signing identity preserved");
                require(baseline.getAll().equals(store.preferences.getAll()), "Every typed playback preference preserved");
                require(!store.enabled(), "No automatic start after update");
                require(!target.getSharedPreferences("updates", 0).getBoolean("automatic", true), "Update preference preserved");
                require(!store.dualMode(), "Dual mode remains opt-in after upgrade");
                SettingsStore youtube = store.forHost(SettingsStore.YOUTUBE_PACKAGE);
                SettingsStore instagram = store.forHost(SettingsStore.INSTAGRAM_PACKAGE);
                for (SettingsStore host : new SettingsStore[] {youtube, instagram}) {
                    require(host.target() == 3 && host.ceiling() == 7 && host.lastNonZero() == 3,
                            "Each host inherits the saved count, not a default");
                    require(host.tapMode() == 0 && host.skipLong() && host.longVideoSeconds() == 73,
                            "Each host inherits tap and long-video settings");
                    require(host.x() == 0.22f && host.y() == 0.71f && !host.hostPaused(),
                            "Each host inherits placement and begins unpaused");
                }
                for (Map.Entry<String, ?> entry : baseline.getAll().entrySet())
                    require(entry.getValue().equals(store.preferences.getAll().get(entry.getKey())),
                            "Migration preserves every legacy value and type");
                require(instagram.photoEnabled() && instagram.photoMode() == 1
                        && instagram.photoWholeSeconds() == 4 && instagram.photoSlideSeconds() == 6
                        && instagram.photoFallback() && !youtube.photoEnabled(), "Photo settings stay Instagram-only");
                youtube.ceiling(9);
                SettingsStore reopened = new SettingsStore(target);
                require(reopened.forHost(SettingsStore.YOUTUBE_PACKAGE).ceiling() == 9
                        && reopened.forHost(SettingsStore.INSTAGRAM_PACKAGE).ceiling() == 7
                        && reopened.ceiling() == 7, "Reopening never remigrates or changes another host");
            } else throw new AssertionError("Unknown upgrade phase");
            result.putString("result", "PASS"); result.putString("phase", phase);
            result.putInt("preferenceKeys", store.preferences.getAll().size());
            result.putInt("api", Build.VERSION.SDK_INT);
        } catch (Throwable error) {
            result.putString("result", "FAIL"); result.putString("error", error.toString());
        }
        return result;
    }
    private static void copy(SharedPreferences destination, Map<String, ?> values) {
        SharedPreferences.Editor editor = destination.edit().clear();
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            Object value = entry.getValue(); String key = entry.getKey();
            if (value instanceof Boolean) editor.putBoolean(key, (Boolean) value);
            else if (value instanceof Integer) editor.putInt(key, (Integer) value);
            else if (value instanceof Float) editor.putFloat(key, (Float) value);
            else throw new AssertionError("Unexpected fixture preference type");
        }
        require(editor.commit(), "Save typed baseline");
    }
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
