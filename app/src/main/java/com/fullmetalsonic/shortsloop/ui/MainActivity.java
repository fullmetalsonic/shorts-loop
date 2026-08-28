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
    private static final String YOUTUBE = SettingsStore.YOUTUBE_PACKAGE, INSTAGRAM = SettingsStore.INSTAGRAM_PACKAGE, TIKTOK = SettingsStore.TIKTOK_PACKAGE;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SettingsStore store;
    private SettingsStore youtubeStore, instagramStore, tiktokStore;
    private String settingsHost;
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
        youtubeStore = store.forHost(YOUTUBE); instagramStore = store.forHost(INSTAGRAM); tiktokStore = store.forHost(TIKTOK);
        if (savedInstanceState != null) settingsHost = savedInstanceState.getString("settings_host");
        if (!SettingsStore.supportedHost(settingsHost)) settingsHost = null;
        screen = new SettingsScreen(this, youtubeStore.ceiling(), value -> { youtubeStore.ceiling(value); render(); },
                store.fallbackSeconds(), value -> { store.fallbackSeconds(value); render(); },
                store.liveDelaySeconds(), value -> { store.liveDelaySeconds(value); render(); },
                youtubeStore.longVideoSeconds(), value -> { youtubeStore.longVideoSeconds(value); render(); },
                instagramStore.ceiling(), value -> { instagramStore.ceiling(value); render(); },
                instagramStore.longVideoSeconds(), value -> { instagramStore.longVideoSeconds(value); render(); },
                tiktokStore.ceiling(), value -> { tiktokStore.ceiling(value); render(); },
                store.adDelayTenths(), value -> { store.adDelayTenths(value); render(); },
                tiktokStore.longVideoSeconds(), value -> { tiktokStore.longVideoSeconds(value); render(); });
        setContentView(screen.root);
        screen.restoreNavigation(savedInstanceState);
        bindHostPanel(screen.youtubeSettings, youtubeStore, false);
        bindHostPanel(screen.instagramSettings, instagramStore, true);
        bindHostPanel(screen.tiktokSettings, tiktokStore, false);
        screen.youtubeEntry.open.setOnClickListener(v -> showHost(YOUTUBE));
        screen.instagramEntry.open.setOnClickListener(v -> showHost(INSTAGRAM));
        screen.tiktokEntry.open.setOnClickListener(v -> showHost(TIKTOK));
        screen.back.setOnClickListener(v -> showHome());
        bindRules(screen.instagramRules, instagramStore);
        bindRules(screen.tiktokRules, tiktokStore);
        updater = new UpdateController(this, screen.updates, screen.updateBanner, () -> { showHome(); screen.showUpdates(); });
        screen.setupJump.setOnClickListener(v -> { showHome(); screen.showSetup(RuntimeState.connected && !hasInstalledSelection()); });
        screen.youtube.setOnCheckedChangeListener((v, checked) -> { if (!rendering) { store.selectedApp(YOUTUBE, checked); render(); } });
        screen.instagram.setOnCheckedChangeListener((v, checked) -> { if (!rendering) { store.selectedApp(INSTAGRAM, checked); render(); } });
        screen.tiktok.setOnCheckedChangeListener((v, checked) -> { if (!rendering) { store.selectedApp(TIKTOK, checked); render(); } });
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
        screen.dualMode.setOnCheckedChangeListener((v, checked) -> { if (!rendering) { store.dualMode(checked); render(); } });
        screen.execution.setOnCheckedChangeListener((v, checked) -> {
            if (rendering) return;
            if (!checked) { store.enabled(false); render(); return; }
            if (!commitHost(screen.youtubeSettings, youtubeStore, false)
                    || !commitHost(screen.instagramSettings, instagramStore, true)
                    || !commitHost(screen.tiktokSettings, tiktokStore, false)) { render(); return; }
            if (store.skipLive() && store.youtubeEnabled() && installed(YOUTUBE) && !screen.live.commit()) { showHost(false); return; }
            if (!store.hasSelectedApps()) { toast(getString(R.string.ui_select_app_error)); render(); return; }
            if (!hasInstalledSelection()) {
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
            boolean youtubeInstalled = installed(YOUTUBE), instagramInstalled = installed(INSTAGRAM), tiktokInstalled = installed(TIKTOK);
            boolean youtubeReady = youtubeInstalled && store.youtubeEnabled();
            if (settingsHost == null) screen.showHome(); else screen.showHost(settingsHost);
            renderHostPanel(screen.youtubeSettings, youtubeStore, youtubeInstalled, false);
            renderHostPanel(screen.instagramSettings, instagramStore, instagramInstalled, true);
            renderHostPanel(screen.tiktokSettings, tiktokStore, tiktokInstalled, false);
            renderRules(screen.instagramRules, instagramStore, instagramInstalled);
            renderRules(screen.tiktokRules, tiktokStore, tiktokInstalled);
            screen.youtubeEntry.render(youtubeInstalled, store.youtubeEnabled(), youtubeStore.target(), youtubeStore.hostPaused(), store.enabled());
            screen.instagramEntry.render(instagramInstalled, store.instagramEnabled(), instagramStore.target(), instagramStore.hostPaused(), store.enabled());
            screen.tiktokEntry.render(tiktokInstalled, store.tiktokEnabled(), tiktokStore.target(), tiktokStore.hostPaused(), store.enabled());
            screen.live.render(store.liveDelaySeconds()); screen.live.setAvailable(youtubeReady);
            screen.live.toggle.setChecked(store.skipLive() && youtubeReady);
            screen.live.support.setText(!youtubeInstalled ? R.string.live_youtube_missing
                    : !store.youtubeEnabled() ? R.string.live_youtube_select : R.string.live_youtube_ready);
            screen.youtube.setChecked(store.youtubeEnabled()); screen.instagram.setChecked(store.instagramEnabled());
            screen.tiktok.setChecked(store.tiktokEnabled()); screen.tiktok.setEnabled(tiktokInstalled);
            screen.youtube.setEnabled(youtubeInstalled); screen.instagram.setEnabled(instagramInstalled);
            screen.visualAssist.setChecked(FeatureSupportPolicy.visualChecked(Build.VERSION.SDK_INT, instagramInstalled, store.instagramEnabled(), store.visualAssist()));
            screen.visualAssist.setEnabled(visualAssistAvailable());
            screen.visualSupport.setText(CompatibilityPanel.visualReason(this, Build.VERSION.SDK_INT, instagramInstalled, store.instagramEnabled(), store.visualAssist()));
            screen.floating.setChecked(store.floatingEnabled()); screen.floatingDetails.setVisibility(store.floatingEnabled() ? View.VISIBLE : View.GONE);
            screen.dualMode.setChecked(store.dualMode());
            boolean active = store.enabled(); screen.execution.setChecked(active);
            int executionStatus = !active ? R.string.ui_execution_off : store.dualMode() ? R.string.mw_all_running : R.string.mw_single_running;
            screen.status.setText(screen.compactFooter ? active ? R.string.ui_on : R.string.off : executionStatus);
            screen.status.setContentDescription(getString(executionStatus));
            screen.status.setTextColor(active ? UiTheme.CYAN : UiTheme.MUTED);
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
    private boolean hostReady(SettingsStore hostStore) {
        return hostStore.isSelected(hostStore.hostPackage()) && installed(hostStore.hostPackage());
    }
    private void bindRules(SharedVideoRulesPanel rules, SettingsStore hostStore) {
        rules.onSeconds = value -> { hostStore.fallbackSeconds(value); render(); };
        rules.onAdDelay = value -> { hostStore.adDelayTenths(value); render(); };
        rules.photos.whole.changed = value -> { hostStore.photoWholeSeconds(value); render(); };
        rules.photos.slide.changed = value -> { hostStore.photoSlideSeconds(value); render(); };
        rules.photos.toggle.setOnCheckedChangeListener((v, checked) -> {
            if (rendering) return;
            if (!checked) { hostStore.photoEnabled(false); render(); return; }
            if (!hostReady(hostStore) || !rules.photos.commit(hostStore)) { render(); return; }
            hostStore.photoEnabled(true); render();
        });
        rules.photos.modes.setOnCheckedChangeListener((v, id) -> {
            if (!rendering) { hostStore.photoMode(rules.photos.isEachChoice(id) ? 1 : 0); render(); }
        });
        rules.photos.fallback.setOnCheckedChangeListener((v, checked) -> {
            if (!rendering) { hostStore.photoFallback(checked); render(); }
        });
        rules.timedFallback.setOnCheckedChangeListener((v, checked) -> {
            if (rendering) return;
            if (!checked) { hostStore.timedFallback(false); render(); return; }
            if (!hostReady(hostStore) || !rules.seconds.commit()) { render(); return; }
            hostStore.timedFallback(true); render();
        });
        rules.skipAds.setOnCheckedChangeListener((v, checked) -> {
            if (rendering) return;
            if (!checked) { hostStore.skipAds(false); render(); return; }
            if (!hostReady(hostStore) || !rules.adDelay.commit()) { render(); return; }
            hostStore.skipAds(true); render();
        });
    }
    private void renderRules(SharedVideoRulesPanel rules, SettingsStore hostStore, boolean appInstalled) {
        boolean selected = hostStore.isSelected(hostStore.hostPackage());
        boolean available = appInstalled && selected;
        int reason = !appInstalled ? R.string.mw_host_missing : !selected ? R.string.mw_host_unselected : R.string.host_rules_ready;
        rules.seconds.render(hostStore.fallbackSeconds()); rules.seconds.setAvailable(available);
        rules.adDelay.render(hostStore.adDelayTenths()); rules.adDelay.setAvailable(available);
        rules.photos.render(hostStore, available); rules.photos.support.setText(reason);
        rules.timedFallback.setChecked(hostStore.timedFallback() && available); rules.timedFallback.setEnabled(available);
        rules.skipAds.setChecked(hostStore.skipAds() && available); rules.skipAds.setEnabled(available);
        rules.timedSupport.setText(reason); rules.adSupport.setText(reason);
    }
    private void bindHostPanel(HostSettingsPanel panel, SettingsStore hostStore, boolean instagramHost) {
        panel.longVideo.toggle.setOnCheckedChangeListener((view, checked) -> {
            if (rendering) return;
            if (!checked) { hostStore.skipLong(false); render(); return; }
            if (!hostStore.isSelected(hostStore.hostPackage()) || !installed(hostStore.hostPackage()) || !panel.longVideo.commit()) { render(); return; }
            hostStore.skipLong(true); render();
        });
        panel.tapModes.setOnCheckedChangeListener((view, id) -> {
            if (rendering) return;
            hostStore.tapMode(id == panel.quick.getId() ? ModePolicy.TOGGLE : ModePolicy.ROTARY);
            render();
        });
        panel.resume.setOnClickListener(view -> {
            if (!store.enabled() || !hostStore.isSelected(hostStore.hostPackage()) || !installed(hostStore.hostPackage())) return;
            if (!commitHost(panel, hostStore, instagramHost)) return;
            if (RuntimeState.forHost(hostStore.hostPackage()).blocked) hostStore.enabled(false);
            hostStore.start(); render();
        });
    }
    private boolean commitHost(HostSettingsPanel panel, SettingsStore hostStore, boolean instagramHost) {
        if (!hostStore.isSelected(hostStore.hostPackage()) || !installed(hostStore.hostPackage())) return true;
        SharedVideoRulesPanel rules = screen.rules(hostStore.hostPackage());
        boolean valid = panel.count.commit() && (!hostStore.skipLong() || panel.longVideo.commit());
        if (valid && rules != null) {
            valid = (hostStore.ceiling() == 0 || !hostStore.timedFallback() || rules.seconds.commit())
                    && (!hostStore.photoEnabled() || rules.photos.commit(hostStore))
                    && (!hostStore.skipAds() || rules.adDelay.commit());
        }
        if (valid) return true;
        showHost(hostStore.hostPackage()); screen.focusError(hostStore.hostPackage()); return false;
    }
    private void hideKeyboard() {
        View focused = getCurrentFocus();
        if (focused == null) return;
        focused.clearFocus();
        android.view.inputmethod.InputMethodManager keyboard = getSystemService(android.view.inputmethod.InputMethodManager.class);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(focused.getWindowToken(), 0);
    }
    private void showHost(boolean instagramHost) { showHost(instagramHost ? INSTAGRAM : YOUTUBE); }
    private void showHost(String host) { hideKeyboard(); settingsHost = host; render(); }
    private void showHome() { hideKeyboard(); settingsHost = null; render(); }
    @Override public void onBackPressed() {
        if (settingsHost != null) showHome(); else super.onBackPressed();
    }
    private void renderHostPanel(HostSettingsPanel panel, SettingsStore hostStore, boolean appInstalled, boolean instagramHost) {
        boolean selected = hostStore.isSelected(hostStore.hostPackage());
        boolean available = selected && appInstalled;
        panel.count.render(hostStore.ceiling());
        panel.longVideo.render(hostStore.longVideoSeconds()); panel.longVideo.setAvailable(available);
        panel.longVideo.toggle.setChecked(hostStore.skipLong() && available);
        panel.longVideo.support.setText(!appInstalled ? R.string.mw_host_missing : !selected ? R.string.mw_host_unselected : R.string.long_video_host_ready);
        panel.applied.setText(hostStore.target() == hostStore.ceiling()
                ? getString(R.string.ui_applied_count, hostStore.target())
                : getString(R.string.ui_applied_different, hostStore.ceiling(), hostStore.target()));
        panel.state.setText(!appInstalled ? R.string.mw_host_missing : !selected ? R.string.mw_host_unselected
                : !store.enabled() ? R.string.mw_host_off : hostStore.hostPaused() ? R.string.mw_host_paused : R.string.mw_host_running);
        RuntimeState.HostState runtime = RuntimeState.forHost(hostStore.hostPackage());
        if (available && hostStore.enabled()) {
            String code = runtime.status;
            String localized = StatusText.text(this, code);
            long remaining = runtime.timedRemainingSeconds;
            String value = runtime.blocked ? localized
                    : remaining >= 0 ? getString(R.string.ui_status_timer, remaining, localized)
                    : hostStore.target() == 0 ? StatusText.text(this, hostStore.photoEnabled() ? "photo.rules"
                            : LongVideoPolicy.zeroCountStatus(hostStore.skipAds(), hostStore.skipLive(), hostStore.skipLong()))
                    : getString(R.string.ui_status_count, runtime.current, hostStore.target(), localized);
            if (!runtime.blocked && remaining < 0 && hostStore.target() == 0
                    && (LiveSkipPolicy.isLiveStatus(code) || LongVideoPolicy.CHECKING.equals(code) || LongVideoPolicy.CONFIRMING.equals(code)))
                value += "\n" + localized;
            panel.state.setText(value);
        }
        panel.state.setContentDescription(panel.state.getText());
        panel.state.setTextColor(runtime.blocked && hostStore.enabled() ? UiTheme.WARNING : UiTheme.MUTED);
        panel.resume.setVisibility(hostStore.hostPaused() || runtime.blocked ? View.VISIBLE : View.GONE);
        panel.resume.setEnabled(store.enabled() && available);
        panel.tapModes.check(hostStore.tapMode() == ModePolicy.TOGGLE ? panel.quick.getId() : panel.rotary.getId());
    }
    private boolean hasInstalledSelection() {
        return (store.youtubeEnabled() && installed(YOUTUBE)) || (store.instagramEnabled() && installed(INSTAGRAM))
                || (store.tiktokEnabled() && installed(TIKTOK));
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
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putString("settings_host", settingsHost);
        screen.saveNavigation(state);
        super.onSaveInstanceState(state);
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
