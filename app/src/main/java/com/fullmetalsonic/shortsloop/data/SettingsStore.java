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

    public SettingsStore(Context context) {
        this(context.getSharedPreferences("shorts_loop", Context.MODE_PRIVATE));
    }

    SettingsStore(SharedPreferences preferences) {
        this.preferences = preferences;
        migrate();
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

    public int target() { return ModePolicy.clampTarget(intValue("target", ModePolicy.DEFAULT_COUNT), ceiling()); }
    public int ceiling() { return ModePolicy.sanitize(intValue("ceiling", ModePolicy.DEFAULT_COUNT)); }
    public int tapMode() { return ModePolicy.sanitizeTapMode(intValue("tap_mode", ModePolicy.ROTARY)); }
    public boolean enabled() { return booleanValue("enabled", false); }
    public boolean floatingEnabled() { return booleanValue("floating_enabled", true); }
    public boolean youtubeEnabled() { return booleanValue("youtube_enabled", true); }
    public boolean instagramEnabled() { return booleanValue("instagram_enabled", false); }
    public boolean skipAds() { return booleanValue("skip_ads", false); }
    public boolean skipLive() { return booleanValue("skip_live", false); }
    public int liveDelaySeconds() {
        return LiveSkipPolicy.sanitizeSeconds(intValue("live_delay_seconds", LiveSkipPolicy.DEFAULT_SECONDS));
    }
    public boolean visualAssist() { return booleanValue("visual_assist", false); }
    public boolean timedFallback() { return booleanValue("timed_fallback", false); }
    public int fallbackSeconds() {
        return ClocklessTimeoutPolicy.sanitizeSeconds(intValue("fallback_seconds", ClocklessTimeoutPolicy.DEFAULT_SECONDS));
    }
    public int lastNonZero() { return ModePolicy.resume(intValue("last_nonzero", ModePolicy.DEFAULT_COUNT)); }

    /** Floating changes only the active target, never its configured upper bound. */
    public void target(int target) {
        target = ModePolicy.clampTarget(target, ceiling());
        SharedPreferences.Editor editor = preferences.edit().putInt("target", target);
        if (target > 0) editor.putInt("last_nonzero", target);
        editor.apply();
    }

    public void ceiling(int count) {
        int value = ModePolicy.sanitize(count);
        SharedPreferences.Editor editor = preferences.edit().putInt("ceiling", value).putInt("target", value);
        if (value > 0) editor.putInt("last_nonzero", value);
        editor.apply();
    }

    public void tapMode(int mode) {
        preferences.edit().putInt("tap_mode", ModePolicy.sanitizeTapMode(mode)).putInt("target", ceiling()).apply();
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
    public void enabled(boolean value) { preferences.edit().putBoolean("enabled", value).apply(); }
    public void start() { preferences.edit().putInt("target", ceiling()).putBoolean("enabled", true).apply(); }
    public float x() { return PositionPolicy.fraction(preferences.getFloat("x", 0.9f)); }
    public float y() { return PositionPolicy.fraction(preferences.getFloat("y", 0.35f)); }
    public void position(float x, float y) { preferences.edit().putFloat("x", PositionPolicy.fraction(x)).putFloat("y", PositionPolicy.fraction(y)).apply(); }
}
