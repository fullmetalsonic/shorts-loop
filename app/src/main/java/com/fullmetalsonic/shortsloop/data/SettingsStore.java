package com.fullmetalsonic.shortsloop.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.fullmetalsonic.shortsloop.core.ModePolicy;
import com.fullmetalsonic.shortsloop.core.PositionPolicy;
import com.fullmetalsonic.shortsloop.core.SettingsPolicy;
import com.fullmetalsonic.shortsloop.core.ClocklessTimeoutPolicy;
import com.fullmetalsonic.shortsloop.core.LiveSkipPolicy;

public final class SettingsStore {
    public static final String YOUTUBE_PACKAGE = "com.google.android.youtube";
    public static final String INSTAGRAM_PACKAGE = "com.instagram.android";
    private static final int SETTINGS_VERSION = 1;
    private static final Object MIGRATION_LOCK = new Object();
    public final SharedPreferences preferences;
    private final String host;

    public SettingsStore(Context context) {
        this(context.getSharedPreferences("shorts_loop", Context.MODE_PRIVATE));
    }

    SettingsStore(SharedPreferences preferences) {
        this(preferences, null);
    }

    private SettingsStore(SharedPreferences preferences, String host) {
        this.preferences = preferences;
        this.host = host;
        migrate();
    }

    /** Views share the same atomic preference store, not separate files. */
    public SettingsStore forHost(String packageName) {
        if (!supportedHost(packageName)) throw new IllegalArgumentException("Unsupported host");
        migrateHosts();
        return new SettingsStore(preferences, packageName);
    }

    public String hostPackage() { return host; }
    public String scopedKey(String name) { return host == null ? name : prefix(host) + name; }
    public boolean hostPaused() {
        if (host == null) return false;
        try { return preferences.getBoolean(scopedKey("paused"), false); }
        catch (ClassCastException invalidStoredType) { return true; }
    }
    public void hostPaused(boolean value) {
        if (host == null) throw new IllegalStateException("A host view is required");
        preferences.edit().putBoolean(scopedKey("paused"), value).apply();
    }
    private static boolean supportedHost(String value) { return YOUTUBE_PACKAGE.equals(value) || INSTAGRAM_PACKAGE.equals(value); }
    private static String prefix(String value) { return YOUTUBE_PACKAGE.equals(value) ? "host.youtube." : "host.instagram."; }

    /** Playback listeners can ignore another host's edits and global presentation-only keys. */
    public static boolean affectsHost(String key, String packageName) {
        if (!supportedHost(packageName) || key == null) return false;
        if (key.startsWith("host.")) return key.startsWith(prefix(packageName)) && !key.endsWith(".x") && !key.endsWith(".y");
        if ("enabled".equals(key) || "dual_mode".equals(key)) return true;
        if (YOUTUBE_PACKAGE.equals(packageName)) return "youtube_enabled".equals(key) || "skip_live".equals(key) || "live_delay_seconds".equals(key);
        return "instagram_enabled".equals(key) || "skip_ads".equals(key) || "visual_assist".equals(key)
                || "timed_fallback".equals(key) || "fallback_seconds".equals(key) || key.startsWith("photo_");
    }

    private void migrateHosts() {
        synchronized (MIGRATION_LOCK) {
            if (intValue("host_settings_version", 0) >= 1) return;
            SharedPreferences.Editor edit = preferences.edit();
            for (String packageName : new String[] {YOUTUBE_PACKAGE, INSTAGRAM_PACKAGE}) {
                String p = prefix(packageName);
                String[] ints = {"target", "ceiling", "last_nonzero", "tap_mode", "long_video_seconds"};
                int[] values = {ModePolicy.clampTarget(intValue("target", 2), ModePolicy.sanitize(intValue("ceiling", 2))),
                        ModePolicy.sanitize(intValue("ceiling", 2)), ModePolicy.resume(intValue("last_nonzero", 2)),
                        ModePolicy.sanitizeTapMode(intValue("tap_mode", ModePolicy.ROTARY)),
                        com.fullmetalsonic.shortsloop.core.LongVideoPolicy.sanitizeSeconds(intValue("long_video_seconds", 60))};
                for (int i = 0; i < ints.length; i++) if (!preferences.contains(p + ints[i])) edit.putInt(p + ints[i], values[i]);
                if (!preferences.contains(p + "skip_long")) edit.putBoolean(p + "skip_long", booleanValue("skip_long", false));
                if (!preferences.contains(p + "paused")) edit.putBoolean(p + "paused", false);
                if (!preferences.contains(p + "x")) edit.putFloat(p + "x", PositionPolicy.fraction(floatValue("x", 0.9f)));
                if (!preferences.contains(p + "y")) edit.putFloat(p + "y", PositionPolicy.fraction(floatValue("y", 0.35f)));
            }
            edit.putInt("host_settings_version", 1).apply();
        }
    }

    private void migrate() {
        synchronized (MIGRATION_LOCK) {
            if (intValue("settings_version", 0) >= SETTINGS_VERSION) return;
            int oldTarget = ModePolicy.sanitize(intValue("target", ModePolicy.DEFAULT_COUNT));
            int initialCeiling = SettingsPolicy.legacyCeiling(oldTarget, lastNonZero());
            int ceiling = ModePolicy.sanitize(intValue("ceiling", initialCeiling));
            preferences.edit()
                    .putInt("ceiling", ceiling)
                    .putInt("target", ModePolicy.clampTarget(oldTarget, ceiling))
                    .putInt("tap_mode", ModePolicy.sanitizeTapMode(intValue("tap_mode", ModePolicy.ROTARY)))
                    .putBoolean("floating_enabled", booleanValue("floating_enabled", true))
                    .putBoolean("youtube_enabled", booleanValue("youtube_enabled", true))
                    .putBoolean("instagram_enabled", booleanValue("instagram_enabled", false))
                    .putInt("settings_version", SETTINGS_VERSION)
                    .apply();
        }
    }

    private int intValue(String key, int fallback) {
        try { return preferences.getInt(key, fallback); }
        catch (ClassCastException invalidStoredType) { return fallback; }
    }

    private boolean booleanValue(String key, boolean fallback) {
        try { return preferences.getBoolean(key, fallback); }
        catch (ClassCastException invalidStoredType) { return fallback; }
    }

    private float floatValue(String key, float fallback) {
        try { return preferences.getFloat(key, fallback); }
        catch (ClassCastException invalidStoredType) { return fallback; }
    }

    public int target() { return ModePolicy.clampTarget(intValue(scopedKey("target"), ModePolicy.DEFAULT_COUNT), ceiling()); }
    public int ceiling() { return ModePolicy.sanitize(intValue(scopedKey("ceiling"), ModePolicy.DEFAULT_COUNT)); }
    public int tapMode() { return ModePolicy.sanitizeTapMode(intValue(scopedKey("tap_mode"), ModePolicy.ROTARY)); }
    public boolean enabled() { return booleanValue("enabled", false) && (host == null || isSelected(host) && !hostPaused()); }
    public boolean floatingEnabled() { return booleanValue("floating_enabled", true); }
    public boolean dualMode() { return booleanValue("dual_mode", false); }
    public void dualMode(boolean value) { preferences.edit().putBoolean("dual_mode", value).apply(); }
    public boolean youtubeEnabled() { return booleanValue("youtube_enabled", true); }
    public boolean instagramEnabled() { return booleanValue("instagram_enabled", false); }
    public boolean skipAds() { return (host == null || INSTAGRAM_PACKAGE.equals(host)) && booleanValue("skip_ads", false); }
    public boolean photoEnabled() { return (host == null || INSTAGRAM_PACKAGE.equals(host)) && booleanValue("photo_enabled", false); }
    public int photoMode() { return com.fullmetalsonic.shortsloop.core.PhotoReelPolicy.mode(intValue("photo_mode", 0)); }
    public int photoWholeSeconds() { return com.fullmetalsonic.shortsloop.core.PhotoReelPolicy.seconds(intValue("photo_whole_seconds", com.fullmetalsonic.shortsloop.core.PhotoReelPolicy.DEFAULT_SECONDS)); }
    public int photoSlideSeconds() { return com.fullmetalsonic.shortsloop.core.PhotoReelPolicy.seconds(intValue("photo_slide_seconds", com.fullmetalsonic.shortsloop.core.PhotoReelPolicy.DEFAULT_SECONDS)); }
    public boolean photoFallback() { return booleanValue("photo_fallback", false); }
    public void photoEnabled(boolean value) { preferences.edit().putBoolean("photo_enabled", value).apply(); }
    public void photoMode(int value) { preferences.edit().putInt("photo_mode", com.fullmetalsonic.shortsloop.core.PhotoReelPolicy.mode(value)).apply(); }
    public void photoWholeSeconds(int value) { preferences.edit().putInt("photo_whole_seconds", com.fullmetalsonic.shortsloop.core.PhotoReelPolicy.seconds(value)).apply(); }
    public void photoSlideSeconds(int value) { preferences.edit().putInt("photo_slide_seconds", com.fullmetalsonic.shortsloop.core.PhotoReelPolicy.seconds(value)).apply(); }
    public void photoFallback(boolean value) { preferences.edit().putBoolean("photo_fallback", value).apply(); }
    public boolean skipLong() { return booleanValue(scopedKey("skip_long"), false); }
    public int longVideoSeconds() {
        return com.fullmetalsonic.shortsloop.core.LongVideoPolicy.sanitizeSeconds(intValue(scopedKey("long_video_seconds"), 60));
    }
    public void skipLong(boolean enabled) { preferences.edit().putBoolean(scopedKey("skip_long"), enabled).apply(); }
    public void longVideoSeconds(int value) {
        preferences.edit().putInt(scopedKey("long_video_seconds"), com.fullmetalsonic.shortsloop.core.LongVideoPolicy.sanitizeSeconds(value)).apply();
    }
    public boolean skipLive() { return (host == null || YOUTUBE_PACKAGE.equals(host)) && booleanValue("skip_live", false); }
    public int liveDelaySeconds() {
        return LiveSkipPolicy.sanitizeSeconds(intValue("live_delay_seconds", LiveSkipPolicy.DEFAULT_SECONDS));
    }
    public boolean visualAssist() { return (host == null || INSTAGRAM_PACKAGE.equals(host)) && booleanValue("visual_assist", false); }
    public boolean timedFallback() { return (host == null || INSTAGRAM_PACKAGE.equals(host)) && booleanValue("timed_fallback", false); }
    public int fallbackSeconds() {
        return ClocklessTimeoutPolicy.sanitizeSeconds(intValue("fallback_seconds", ClocklessTimeoutPolicy.DEFAULT_SECONDS));
    }
    public int lastNonZero() { return ModePolicy.resume(intValue(scopedKey("last_nonzero"), ModePolicy.DEFAULT_COUNT)); }

    /** Floating changes only the active target, never its configured upper bound. */
    public void target(int target) {
        target = ModePolicy.clampTarget(target, ceiling());
        SharedPreferences.Editor editor = preferences.edit().putInt(scopedKey("target"), target);
        if (target > 0) editor.putInt(scopedKey("last_nonzero"), target);
        editor.apply();
    }

    public void ceiling(int count) {
        int value = ModePolicy.sanitize(count);
        SharedPreferences.Editor editor = preferences.edit().putInt(scopedKey("ceiling"), value).putInt(scopedKey("target"), value);
        if (value > 0) editor.putInt(scopedKey("last_nonzero"), value);
        editor.apply();
    }

    public void tapMode(int mode) {
        preferences.edit().putInt(scopedKey("tap_mode"), ModePolicy.sanitizeTapMode(mode)).putInt(scopedKey("target"), ceiling()).apply();
    }

    public void floatingEnabled(boolean value) { preferences.edit().putBoolean("floating_enabled", value).apply(); }
    public void skipAds(boolean value) { preferences.edit().putBoolean("skip_ads", value).apply(); }
    public void skipLive(boolean value) { preferences.edit().putBoolean("skip_live", value).apply(); }
    public void liveDelaySeconds(int value) {
        preferences.edit().putInt("live_delay_seconds", LiveSkipPolicy.clamp(value)).apply();
    }
    public void visualAssist(boolean value) { preferences.edit().putBoolean("visual_assist", value).apply(); }
    public void timedFallback(boolean value) { preferences.edit().putBoolean("timed_fallback", value).apply(); }
    public void fallbackSeconds(int value) {
        preferences.edit().putInt("fallback_seconds", ClocklessTimeoutPolicy.sanitizeSeconds(value)).apply();
    }

    public void selectedApp(String packageName, boolean selected) {
        if (YOUTUBE_PACKAGE.equals(packageName)) preferences.edit().putBoolean("youtube_enabled", selected).apply();
        else if (INSTAGRAM_PACKAGE.equals(packageName)) preferences.edit().putBoolean("instagram_enabled", selected).apply();
    }

    public boolean isSelected(String packageName) {
        if (YOUTUBE_PACKAGE.equals(packageName)) return youtubeEnabled();
        return INSTAGRAM_PACKAGE.equals(packageName) && instagramEnabled();
    }

    public boolean hasSelectedApps() { return youtubeEnabled() || instagramEnabled(); }
    public void enabled(boolean value) {
        if (host != null) { hostPaused(!value); return; }
        preferences.edit().putBoolean("enabled", value).apply();
    }
    public void start() {
        if (host != null) {
            preferences.edit().putInt(scopedKey("target"), ceiling()).putBoolean(scopedKey("paused"), false).apply();
            return;
        }
        SharedPreferences.Editor edit = preferences.edit().putInt("target", ceiling()).putBoolean("enabled", true);
        for (String packageName : new String[] {YOUTUBE_PACKAGE, INSTAGRAM_PACKAGE}) {
            if (!isSelected(packageName) || intValue("host_settings_version", 0) < 1) continue;
            SettingsStore scoped = new SettingsStore(preferences, packageName);
            edit.putInt(scoped.scopedKey("target"), scoped.ceiling()).putBoolean(scoped.scopedKey("paused"), false);
        }
        edit.apply();
    }
    public float x() { return PositionPolicy.fraction(floatValue(scopedKey("x"), 0.9f)); }
    public float y() { return PositionPolicy.fraction(floatValue(scopedKey("y"), 0.35f)); }
    public void position(float x, float y) { preferences.edit().putFloat(scopedKey("x"), PositionPolicy.fraction(x)).putFloat(scopedKey("y"), PositionPolicy.fraction(y)).apply(); }
}
