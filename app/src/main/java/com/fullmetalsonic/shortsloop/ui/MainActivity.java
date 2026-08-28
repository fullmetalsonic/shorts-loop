package com.fullmetalsonic.shortsloop.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;
import com.fullmetalsonic.shortsloop.R;
import com.fullmetalsonic.shortsloop.core.ModePolicy;
import com.fullmetalsonic.shortsloop.core.FeatureSupportPolicy;
import com.fullmetalsonic.shortsloop.core.LiveSkipPolicy;
import com.fullmetalsonic.shortsloop.core.LongVideoPolicy;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.i18n.AppLocale;
import com.fullmetalsonic.shortsloop.i18n.StatusText;
import com.fullmetalsonic.shortsloop.service.RuntimeState;
import com.fullmetalsonic.shortsloop.tile.ShortsTileService;
import com.fullmetalsonic.shortsloop.updates.UpdateController;

public final class MainActivity extends Activity {
    private static final String YOUTUBE = "com.google.android.youtube", INSTAGRAM = "com.instagram.android";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SettingsStore store;
    private SettingsScreen screen;
    private boolean rendering;
    private String appliedLanguage;
    private boolean localeRestartPending;
    private AlertDialog visualAssistDialog;
    private UpdateController updater;
    private final Runnable refresh = new Runnable() {
        @Override public void run() {
            if (refreshLanguage()) return;
            render(); handler.postDelayed(this, 500);
        }
    };
    @Override protected void attachBaseContext(Context base) {
        appliedLanguage = AppLocale.language(base);
        super.attachBaseContext(AppLocale.wrap(base));
    }
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); store = new SettingsStore(this);
        screen = new SettingsScreen(this, store.ceiling(), value -> { store.ceiling(value); render(); },
                store.fallbackSeconds(), value -> { store.fallbackSeconds(value); render(); },
                store.liveDelaySeconds(), value -> { store.liveDelaySeconds(value); render(); },
                store.longVideoSeconds(), value -> { store.longVideoSeconds(value); render(); });
        setContentView(screen.root);
        screen.photos.whole.changed = value -> { store.photoWholeSeconds(value); render(); };
        screen.photos.slide.changed = value -> { store.photoSlideSeconds(value); render(); };
        screen.photos.toggle.setOnCheckedChangeListener((v, checked) -> {
            if (rendering) return;
            if (!checked) { store.photoEnabled(false); render(); return; }
            if (!installed(INSTAGRAM) || !store.instagramEnabled() || !screen.photos.commit(store)) { render(); return; }
            store.photoEnabled(true); render();
        });
        screen.photos.modes.setOnCheckedChangeListener((v, id) -> {
            if (!rendering) { store.photoMode(id == R.id.photo_mode_each ? 1 : 0); render(); }
        });
        screen.photos.fallback.setOnCheckedChangeListener((v, checked) -> {
            if (!rendering) { store.photoFallback(checked); render(); }
        });
        updater = new UpdateController(this, screen.updates, screen.updateBanner, screen::showUpdates);
        screen.setupJump.setOnClickListener(v -> screen.showSetup(RuntimeState.connected && !hasInstalledSelection()));
        screen.youtube.setOnCheckedChangeListener((v, checked) -> { if (!rendering) { store.selectedApp(YOUTUBE, checked); render(); } });
        screen.instagram.setOnCheckedChangeListener((v, checked) -> { if (!rendering) { store.selectedApp(INSTAGRAM, checked); render(); } });
        screen.timedFallback.setOnCheckedChangeListener((v, checked) -> {
            if (rendering) return;
            if (!checked) { store.timedFallback(false); render(); return; }
            if (!FeatureSupportPolicy.instagramFeature(installed(INSTAGRAM), store.instagramEnabled()) || !screen.seconds.commit()) { render(); return; }
            store.timedFallback(true); render();
        });
        screen.skipAds.setOnCheckedChangeListener((v, checked) -> { if (!rendering) { store.skipAds(checked); render(); } });
        screen.longVideo.toggle.setOnCheckedChangeListener((v, checked) -> {
            if (rendering) return;
            if (!checked) { store.skipLong(false); render(); return; }
            if (!hasInstalledSelection() || !screen.longVideo.commit()) { render(); return; }
            store.skipLong(true); render();
        });
        screen.live.toggle.setOnCheckedChangeListener((v, checked) -> {
            if (rendering) return;
            if (!checked) { store.skipLive(false); render(); return; }
            if (!installed(YOUTUBE) || !store.youtubeEnabled() || !screen.live.commit()) { render(); return; }
            store.skipLive(true); render();
        });
        screen.visualAssist.setOnCheckedChangeListener((v, checked) -> {
            if (rendering) return;
            if (!checked) { store.visualAssist(false); render(); return; }
            // Confirmation, not the initial switch tap, grants this optional analysis.
            render();
            if (visualAssistAvailable()) confirmVisualAssist();
        });
        screen.floating.setOnCheckedChangeListener((v, checked) -> { if (!rendering) { store.floatingEnabled(checked); render(); } });
        screen.tapModes.setOnCheckedChangeListener((v, id) -> { if (!rendering) { store.tapMode(id == R.id.tap_quick ? ModePolicy.TOGGLE : ModePolicy.ROTARY); render(); } });
        screen.execution.setOnCheckedChangeListener((v, checked) -> {
            if (rendering) return;
            if (!checked) { store.enabled(false); render(); return; }
            if (!screen.count.commit()) { render(); return; }
            if (store.ceiling() > 0 && store.timedFallback() && store.instagramEnabled() && !screen.seconds.commit()) { render(); return; }
            if (store.skipLive() && store.youtubeEnabled() && installed(YOUTUBE) && !screen.live.commit()) { render(); return; }
            if (store.skipLong() && hasInstalledSelection() && !screen.longVideo.commit()) { render(); return; }
            if (store.photoEnabled() && store.instagramEnabled() && !screen.photos.commit(store)) { render(); return; }
            if (!store.hasSelectedApps()) { toast(getString(R.string.ui_select_app_error)); render(); return; }
            if (!(store.youtubeEnabled() && installed(YOUTUBE)) && !(store.instagramEnabled() && installed(INSTAGRAM))) {
                toast(getString(R.string.ui_app_missing_error)); render(); return;
            }
            if (!RuntimeState.connected) { toast(getString(R.string.ui_accessibility_required)); render(); return; }
            if (store.floatingEnabled() && !Settings.canDrawOverlays(this)) {
                toast(getString(R.string.ui_overlay_required)); render(); return;
            }
            store.start(); render();
        });
        screen.accessButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(R.string.permission_dialog_title).setMessage(R.string.permission_dialog_body)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.open_settings, (dialog, which) -> open(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))).show());
        screen.overlayButton.setOnClickListener(v -> open(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()))));
        screen.tileButton.setOnClickListener(v -> addTile());
        screen.tileButton.setText(FeatureSupportPolicy.tileAddRequest(Build.VERSION.SDK_INT)
                ? R.string.compat_tile_add_button : R.string.compat_tile_manual_button);
        render();
    }
    private void render() {
        if (screen == null) return;
        rendering = true;
        try {
            boolean youtubeInstalled = installed(YOUTUBE), instagramInstalled = installed(INSTAGRAM);
            boolean instagramReady = FeatureSupportPolicy.instagramFeature(instagramInstalled, store.instagramEnabled());
            boolean youtubeReady = youtubeInstalled && store.youtubeEnabled();
            boolean longReady = youtubeReady || instagramReady;
            screen.count.render(store.ceiling());
            screen.photos.render(store, instagramReady);
            screen.photos.support.setText(CompatibilityPanel.instagramReason(this, instagramInstalled, store.instagramEnabled()));
            screen.seconds.render(store.fallbackSeconds()); screen.seconds.setAvailable(instagramReady);
            screen.live.render(store.liveDelaySeconds()); screen.live.setAvailable(youtubeReady);
            screen.live.toggle.setChecked(store.skipLive() && youtubeReady);
            screen.live.support.setText(!youtubeInstalled ? R.string.live_youtube_missing
                    : !store.youtubeEnabled() ? R.string.live_youtube_select : R.string.live_youtube_ready);
            screen.longVideo.render(store.longVideoSeconds()); screen.longVideo.setAvailable(longReady);
            screen.longVideo.toggle.setChecked(store.skipLong() && longReady);
            screen.longVideo.support.setText(longReady ? R.string.long_video_host_ready : R.string.long_video_host_unavailable);
            screen.applied.setText(store.target() == store.ceiling()
                    ? getString(R.string.ui_applied_count, store.target())
                    : getString(R.string.ui_applied_different, store.ceiling(), store.target()));
            screen.youtube.setChecked(store.youtubeEnabled()); screen.instagram.setChecked(store.instagramEnabled());
            screen.youtube.setEnabled(youtubeInstalled); screen.instagram.setEnabled(instagramInstalled);
            screen.youtube.setText(youtubeInstalled ? getString(R.string.ui_youtube) : getString(R.string.ui_youtube_missing));
            screen.instagram.setText(instagramInstalled ? getString(R.string.ui_instagram) : getString(R.string.ui_instagram_missing));
            screen.timedFallback.setChecked(store.timedFallback() && instagramReady); screen.timedFallback.setEnabled(instagramReady);
            screen.skipAds.setChecked(store.skipAds() && instagramReady); screen.skipAds.setEnabled(instagramReady);
            screen.timedSupport.setText(CompatibilityPanel.instagramReason(this, instagramInstalled, store.instagramEnabled()));
            screen.adSupport.setText(CompatibilityPanel.instagramReason(this, instagramInstalled, store.instagramEnabled()));
            screen.visualAssist.setChecked(FeatureSupportPolicy.visualChecked(Build.VERSION.SDK_INT, instagramInstalled, store.instagramEnabled(), store.visualAssist()));
            screen.visualAssist.setEnabled(visualAssistAvailable());
            screen.visualSupport.setText(CompatibilityPanel.visualReason(this, Build.VERSION.SDK_INT, instagramInstalled, store.instagramEnabled(), store.visualAssist()));
            screen.floating.setChecked(store.floatingEnabled()); screen.floatingDetails.setVisibility(store.floatingEnabled() ? View.VISIBLE : View.GONE);
            screen.tapModes.check(store.tapMode() == ModePolicy.TOGGLE ? R.id.tap_quick : R.id.tap_rotary);
            boolean active = store.enabled(); screen.execution.setChecked(active);
            long timedRemaining = RuntimeState.timedRemainingSeconds;
            boolean adsActive = store.skipAds() && instagramReady, liveActive = store.skipLive() && youtubeReady;
            boolean longActive = store.skipLong() && longReady;
            String statusCode = RuntimeState.status;
            String localizedStatus = StatusText.text(this, statusCode);
            String liveStatus = LiveSkipPolicy.STATUS_DELAYED.equals(statusCode) && timedRemaining > 0
                    ? getString(R.string.live_remaining, timedRemaining) : localizedStatus;
            String zeroStatus = StatusText.text(this, store.photoEnabled() && instagramReady ? "photo.rules"
                    : LongVideoPolicy.zeroCountStatus(adsActive, liveActive, longActive));
            screen.status.setText(!active ? getString(R.string.ui_execution_off) : RuntimeState.blocked ? localizedStatus
                    : statusCode.startsWith("photo.") ? timedRemaining >= 0 ? getString(R.string.photo_status_remaining, localizedStatus, timedRemaining) : localizedStatus
                    : store.target() == 0 ? zeroStatus + (LiveSkipPolicy.isLiveStatus(statusCode)
                            || LongVideoPolicy.CHECKING.equals(statusCode) || LongVideoPolicy.CONFIRMING.equals(statusCode)
                            ? "\n" + liveStatus : "")
                    : LiveSkipPolicy.isLiveStatus(statusCode) ? liveStatus
                    : timedRemaining >= 0 ? getString(R.string.ui_status_timer, timedRemaining, localizedStatus)
                    : getString(R.string.ui_status_count, RuntimeState.current, store.target(), localizedStatus));
            screen.status.setContentDescription(screen.status.getText());
            screen.status.setTextColor(RuntimeState.blocked ? UiTheme.WARNING : active ? UiTheme.CYAN : UiTheme.MUTED);
            boolean access = RuntimeState.connected, overlay = Settings.canDrawOverlays(this);
            screen.permissionStatus.setText(!access ? getString(R.string.accessibility_reconnect_help)
                    : store.floatingEnabled() && !overlay ? getString(R.string.ui_ready_needs_overlay)
                    : store.floatingEnabled() ? getString(R.string.ui_ready_with_overlay) : getString(R.string.ui_ready_without_overlay));
            screen.accessButton.setVisibility(access ? View.GONE : View.VISIBLE);
            screen.accessButton.setText(R.string.accessibility_reconnect_action);
            screen.overlayButton.setVisibility(store.floatingEnabled() && !overlay ? View.VISIBLE : View.GONE);
            screen.setupJump.setVisibility(!access || (store.floatingEnabled() && !overlay) || !hasInstalledSelection() ? View.VISIBLE : View.GONE);
            screen.setupJump.setText(!access ? R.string.accessibility_reconnect_banner : R.string.setup_needed_banner);
        } finally { rendering = false; }
    }
    private boolean installed(String pkg) {
        try { getPackageManager().getApplicationInfo(pkg, 0); return true; }
        catch (android.content.pm.PackageManager.NameNotFoundException e) { return false; }
    }
    private boolean hasInstalledSelection() {
        return (store.youtubeEnabled() && installed(YOUTUBE)) || (store.instagramEnabled() && installed(INSTAGRAM));
    }
    private boolean visualAssistAvailable() {
        return FeatureSupportPolicy.visualAvailability(Build.VERSION.SDK_INT, installed(INSTAGRAM), store.instagramEnabled())
                == FeatureSupportPolicy.Availability.AVAILABLE;
    }
    private void confirmVisualAssist() {
        if (visualAssistDialog != null) return;
        visualAssistDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.ui_visual_dialog_title))
                .setMessage(getString(R.string.ui_visual_dialog_body))
                .setNegativeButton(R.string.cancel, (dialog, which) -> { store.visualAssist(false); render(); })
                .setPositiveButton(getString(R.string.ui_visual_confirm), (dialog, which) -> {
                    store.visualAssist(visualAssistAvailable()); render();
                })
                .setOnCancelListener(dialog -> { store.visualAssist(false); render(); })
                .create();
        visualAssistDialog.setOnDismissListener(dialog -> visualAssistDialog = null);
        visualAssistDialog.show();
    }
    private void addTile() {
        if (Build.VERSION.SDK_INT >= 33) {
            try {
                getSystemService(StatusBarManager.class).requestAddTileService(new ComponentName(this, ShortsTileService.class),
                        getString(R.string.tile_label), Icon.createWithResource(this, R.drawable.ic_loop), getMainExecutor(),
                        result -> toast(getString(result == StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED
                                || result == StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED ? R.string.tile_added : R.string.tile_not_added)));
            } catch (RuntimeException ignored) { showManualTileHelp(); }
        } else showManualTileHelp();
    }
    private void showManualTileHelp() {
        new AlertDialog.Builder(this).setTitle(R.string.compat_tile_manual_button)
                .setMessage(R.string.tile_manual).setPositiveButton(android.R.string.ok, null).show();
    }
    private void open(Intent intent) { try { startActivity(intent); } catch (RuntimeException ignored) { toast(getString(R.string.settings_unavailable)); } }
    private void toast(String text) { Toast.makeText(this, text, Toast.LENGTH_LONG).show(); }
    @Override protected void onRestoreInstanceState(Bundle state) {
        // Android restores CompoundButton via setChecked, which invokes listeners.
        // Preferences, not stale Activity state, own execution and saved settings.
        rendering = true;
        try { super.onRestoreInstanceState(state); }
        finally { rendering = false; }
        render();
    }
    @Override protected void onResume() {
        super.onResume();
        if (refreshLanguage()) return;
        screen.battery.refresh();
        updater.onResume();
        handler.removeCallbacks(refresh); handler.post(refresh);
    }
    @Override protected void onPause() { handler.removeCallbacks(refresh); if (updater != null) updater.onPause(); super.onPause(); }
    private boolean refreshLanguage() {
        if (localeRestartPending) return true;
        if (appliedLanguage.equals(AppLocale.language(this))) return false;
        localeRestartPending = true;
        handler.removeCallbacks(refresh);
        recreate();
        return true;
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UpdateController.INSTALL_REQUEST && updater != null) updater.onInstallResult();
    }
    @Override protected void onDestroy() {
        if (updater != null) updater.close();
        if (visualAssistDialog != null) {
            visualAssistDialog.setOnDismissListener(null);
            visualAssistDialog.dismiss(); visualAssistDialog = null;
        }
        super.onDestroy();
    }
}
