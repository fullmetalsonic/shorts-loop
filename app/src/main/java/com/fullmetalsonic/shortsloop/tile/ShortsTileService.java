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
import com.fullmetalsonic.shortsloop.core.ModePolicy;
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
        if (store.enabled()) { publish(false, getString(R.string.off)); store.enabled(false); return; }
        if (!RuntimeState.connected || !store.hasSelectedApps()
                || (store.floatingEnabled() && !Settings.canDrawOverlays(this))) { openSetup(); return; }
        // Publish the visible state immediately; preference callbacks reconcile after activation.
        publish(true, getString(R.string.starting));
        if (store.enabled()) store.enabled(false);
        store.start(); render();
    }
    private void render() {
        boolean active = ModePolicy.tileActive(store.enabled(), store.target(), RuntimeState.connected, RuntimeState.blocked);
        String subtitle = active ? store.target() == 0
                ? store.skipAds() && store.instagramEnabled() ? "0회 · 광고만 넘김" : "0회 · 넘김 대기"
                : getString(R.string.tile_count, store.target())
                : RuntimeState.blocked ? getString(R.string.restart_needed)
                : !RuntimeState.connected || (store.floatingEnabled() && !Settings.canDrawOverlays(this)) ? getString(R.string.permission_needed)
                : !store.hasSelectedApps() ? "앱 선택 필요"
                : getString(R.string.off);
        publish(active, subtitle);
    }
    private void publish(boolean active, String subtitle) {
        Tile tile = getQsTile(); if (tile == null) return;
        tile.setLabel(getString(R.string.tile_label)); tile.setState(active ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        if (Build.VERSION.SDK_INT >= 29) tile.setSubtitle(subtitle);
        tile.setContentDescription(getString(R.string.tile_label) + " · " + subtitle); tile.updateTile();
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
