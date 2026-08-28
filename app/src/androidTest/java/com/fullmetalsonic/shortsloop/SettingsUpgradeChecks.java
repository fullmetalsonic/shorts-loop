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

/** Test APK only: preserve typed per-host preferences across the public code33 to code34 update. */
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
                require(info.versionCode == 33 && "0.4.0".equals(info.versionName), "Start with frozen code33");
                require((info.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == 0, "Previous public release is not debuggable");
                store.enabled(false); store.ceiling(7); store.target(3); store.tapMode(0); store.target(3);
                store.floatingEnabled(true); store.position(0.22f, 0.71f);
                store.selectedApp(SettingsStore.YOUTUBE_PACKAGE, true);
                store.selectedApp(SettingsStore.INSTAGRAM_PACKAGE, true);
                store.selectedApp(SettingsStore.TIKTOK_PACKAGE, true);
                store.adDelayTenths(13);
                store.skipAds(true); store.skipLive(true); store.liveDelaySeconds(4);
                store.skipLong(true); store.longVideoSeconds(73);
                store.timedFallback(true); store.fallbackSeconds(17); store.visualAssist(false);
                store.photoEnabled(true); store.photoMode(1); store.photoWholeSeconds(4);
                store.photoSlideSeconds(6); store.photoFallback(true);
                SettingsStore yt = store.forHost(SettingsStore.YOUTUBE_PACKAGE);
                SettingsStore ig = store.forHost(SettingsStore.INSTAGRAM_PACKAGE);
                yt.ceiling(7); yt.target(3); yt.skipLong(true); yt.longVideoSeconds(73); yt.position(.22f, .71f);
                ig.ceiling(5); ig.target(2); ig.skipLong(false); ig.longVideoSeconds(83); ig.position(.62f, .31f);
                SettingsStore tt = store.forHost(SettingsStore.TIKTOK_PACKAGE);
                tt.ceiling(4); tt.target(1); tt.position(.32f, .61f);
                store.dualMode(true);
                require(store.preferences.edit().commit(), "Flush target preferences");
                require(target.getSharedPreferences("updates", 0).edit().putBoolean("automatic", false).commit(), "Disable fixture network checks");
                copy(baseline, store.preferences.getAll());
                require(identity.edit().clear().putInt("uid", info.applicationInfo.uid)
                        .putString("signer", info.signatures[0].toCharsString()).putBoolean("seeded", true).commit(), "Save identity");
            } else if ("verify".equals(phase)) {
                require(identity.getBoolean("seeded", false), "Seed phase is required");
                require(info.versionCode == 34 && "0.5.0".equals(info.versionName), "Updated to code34");
                require((info.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == 0, "Release debugging is disabled");
                require(info.applicationInfo.uid == identity.getInt("uid", -1), "Package UID preserved");
                require(info.signatures.length == 1 && info.signatures[0].toCharsString().equals(identity.getString("signer", "")), "Signing identity preserved");
                require(baseline.getAll().equals(store.preferences.getAll()), "Every typed playback preference preserved");
                require(!store.enabled(), "No automatic start after update");
                require(!target.getSharedPreferences("updates", 0).getBoolean("automatic", true), "Update preference preserved");
                require(store.dualMode(), "Existing enabled multi-host choice is preserved");
                SettingsStore youtube = store.forHost(SettingsStore.YOUTUBE_PACKAGE);
                SettingsStore instagram = store.forHost(SettingsStore.INSTAGRAM_PACKAGE);
                require(youtube.target() == 3 && youtube.ceiling() == 7 && youtube.lastNonZero() == 3
                        && youtube.skipLong() && youtube.longVideoSeconds() == 73 && youtube.x() == .22f && youtube.y() == .71f,
                        "YouTube saved independent settings survive");
                require(instagram.target() == 2 && instagram.ceiling() == 5 && instagram.lastNonZero() == 2
                        && !instagram.skipLong() && instagram.longVideoSeconds() == 83 && instagram.x() == .62f && instagram.y() == .31f,
                        "Instagram saved independent settings survive");
                for (Map.Entry<String, ?> entry : baseline.getAll().entrySet())
                    require("host_settings_version".equals(entry.getKey()) || entry.getValue().equals(store.preferences.getAll().get(entry.getKey())),
                            "Migration preserves every legacy value and type");
                require(store.preferences.getInt("host_settings_version", 0) == 2, "Existing schema remains unchanged");
                SettingsStore tiktok = store.forHost(SettingsStore.TIKTOK_PACKAGE);
                require(store.tiktokEnabled() && tiktok.target() == 1 && tiktok.ceiling() == 4
                        && tiktok.x() == .32f && tiktok.y() == .61f
                        && !tiktok.skipAds() && !tiktok.skipLong() && !tiktok.skipLive() && !tiktok.photoEnabled(),
                        "TikTok selection/count/position survive without inherited special actions");
                require(store.adDelayTenths() == 13 && store.fallbackSeconds() == 17
                        && tiktok.adDelayTenths() == 0 && tiktok.fallbackSeconds() == 3 && !tiktok.timedFallback()
                        && tiktok.photoWholeSeconds() == 3 && tiktok.photoSlideSeconds() == 3,
                        "Instagram timing survives while new TikTok rules keep independent defaults");
                require(instagram.photoEnabled() && instagram.photoMode() == 1
                        && instagram.photoWholeSeconds() == 4 && instagram.photoSlideSeconds() == 6
                        && instagram.photoFallback() && !youtube.photoEnabled(), "Legacy photo settings stay with Instagram");
                youtube.ceiling(9);
                SettingsStore reopened = new SettingsStore(target);
                require(reopened.forHost(SettingsStore.YOUTUBE_PACKAGE).ceiling() == 9
                        && reopened.forHost(SettingsStore.INSTAGRAM_PACKAGE).ceiling() == 5
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
