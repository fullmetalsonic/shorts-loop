package com.fullmetalsonic.shortsloop.tile;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import com.fullmetalsonic.shortsloop.R;
import com.fullmetalsonic.shortsloop.i18n.AppLocale;
import com.fullmetalsonic.shortsloop.i18n.StatusText;
import com.fullmetalsonic.shortsloop.core.ModePolicy;
import com.fullmetalsonic.shortsloop.core.FeatureSupportPolicy;
import com.fullmetalsonic.shortsloop.core.LiveSkipPolicy;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.service.RuntimeState;
import com.fullmetalsonic.shortsloop.ui.MainActivity;

public final class ShortsTileService extends TileService implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SettingsStore store;
    private boolean listening;
    public static void refresh(Context context) {
        requestListeningState(context, new ComponentName(context, ShortsTileService.class));
    }
    @Override public void onStartListening() {
        super.onStartListening();
        if (store == null) store = new SettingsStore(this);
        if (!listening) { store.preferences.registerOnSharedPreferenceChangeListener(this); listening = true; }
        render();
    }
    @Override public void onStopListening() {
        if (store != null && listening) store.preferences.unregisterOnSharedPreferenceChangeListener(this);
        listening = false; super.onStopListening();
    }
    @Override public void onDestroy() { onStopListening(); super.onDestroy(); }
    @Override public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (!"x".equals(key) && !"y".equals(key)) render();
    }
    @Override public void onClick() {
        super.onClick();
        if (store == null) store = new SettingsStore(this);
        if (isLocked()) { unlockAndRun(this::toggle); return; }
        toggle();
    }
    private void toggle() {
        boolean active = ModePolicy.tileActive(store.enabled(), store.target(), RuntimeState.connected, RuntimeState.blocked);
        if (store.enabled()) { publish(false, text(R.string.off)); store.enabled(false); return; }
        if (!RuntimeState.connected || !store.hasSelectedApps()
                || (store.floatingEnabled() && !Settings.canDrawOverlays(this))) { openSetup(); return; }
        // Publish the visible state immediately; preference callbacks reconcile after activation.
        publish(true, text(R.string.starting));
        if (store.enabled()) store.enabled(false);
        store.start(); render();
    }
    private void render() {
        boolean active = ModePolicy.tileActive(store.enabled(), store.target(), RuntimeState.connected, RuntimeState.blocked);
        boolean ads = store.skipAds() && store.instagramEnabled() && installed(SettingsStore.INSTAGRAM_PACKAGE);
        boolean live = store.skipLive() && store.youtubeEnabled() && installed(SettingsStore.YOUTUBE_PACKAGE);
        boolean longVideo = store.skipLong() && ((store.youtubeEnabled() && installed(SettingsStore.YOUTUBE_PACKAGE))
                || (store.instagramEnabled() && installed(SettingsStore.INSTAGRAM_PACKAGE)));
        String subtitle = active ? store.target() == 0
                ? longVideo ? text(R.string.tile_long) : text(R.string.zero_features_short,
                    text(ads ? R.string.feature_on_short : R.string.feature_off_short),
                    text(live ? R.string.feature_on_short : R.string.feature_off_short))
                : text(R.string.tile_count, store.target())
                : RuntimeState.blocked ? text(R.string.restart_needed)
                : !RuntimeState.connected || (store.floatingEnabled() && !Settings.canDrawOverlays(this)) ? text(R.string.permission_needed)
                : !store.hasSelectedApps() ? text(R.string.tile_select)
                : text(R.string.off);
        publish(active, subtitle, active && store.target() == 0
                ? StatusText.text(AppLocale.wrap(this), com.fullmetalsonic.shortsloop.core.LongVideoPolicy.zeroCountStatus(ads, live, longVideo)) : subtitle);
    }
    private void publish(boolean active, String subtitle) {
        publish(active, subtitle, subtitle);
    }
    private void publish(boolean active, String subtitle, String description) {
        Tile tile = getQsTile(); if (tile == null) return;
        tile.setLabel(FeatureSupportPolicy.tileLabel(Build.VERSION.SDK_INT, text(R.string.tile_label), subtitle));
        tile.setState(active ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        if (Build.VERSION.SDK_INT >= 29) tile.setSubtitle(subtitle);
        tile.setContentDescription(text(R.string.tile_label) + " · " + description); tile.updateTile();
    }
    private String text(int resource, Object... args) { return AppLocale.wrap(this).getString(resource, args); }
    @Override public void onConfigurationChanged(android.content.res.Configuration config) {
        super.onConfigurationChanged(config);
        if (store != null && listening) render();
    }
    private boolean installed(String packageName) {
        try { getPackageManager().getApplicationInfo(packageName, 0); return true; }
        catch (android.content.pm.PackageManager.NameNotFoundException absent) { return false; }
    }
    private void openSetup() {
        Intent intent = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (Build.VERSION.SDK_INT >= 34) startActivityAndCollapse(PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
        else openLegacySetup(intent);
    }
    // The PendingIntent overload does not exist before API 34. Newer OS versions never enter this method.
    @SuppressLint("StartActivityAndCollapseDeprecated")
    @SuppressWarnings("deprecation")
    private void openLegacySetup(Intent intent) {
        if (Build.VERSION.SDK_INT < 34) startActivityAndCollapse(intent);
    }
}
