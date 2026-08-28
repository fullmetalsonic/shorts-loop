package com.fullmetalsonic.shortsloop.updates;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import com.fullmetalsonic.shortsloop.BuildConfig;
import com.fullmetalsonic.shortsloop.R;
import com.fullmetalsonic.shortsloop.core.UpdatePolicy;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.ui.UpdatePanel;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Foreground settings-screen work only. No service, device identifier, automatic download or silent install. */
public final class UpdateController {
    public static final int INSTALL_REQUEST = 8457;
    private final Activity activity;
    private final UpdatePanel panel;
    private final Button banner;
    private final GitHubUpdateClient client;
    private final UpdateStateStore store;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile boolean closed, cancelled;
    private boolean busy, downloading, ready, waitingPermission, resumed;
    private UpdateManifest item;
    private String status;
    private int progress;
    private long lastManualCheck;

    public UpdateController(Activity activity, UpdatePanel panel, Button banner, Runnable showDetails) {
        this(activity, panel, banner, showDetails, new GitHubUpdateClient());
    }
    /** Transport dependency injection is for tests; production exposes no alternate endpoint or validation bypass. */
    public UpdateController(Activity activity, UpdatePanel panel, Button banner, Runnable showDetails, GitHubUpdateClient client) {
        this.activity = activity; this.panel = panel; this.banner = banner; this.client = client;
        status = text(R.string.updates_ready_to_check);
        store = new UpdateStateStore(activity); item = store.candidate();
        if (item != null && (!UpdatePolicy.isNewer(BuildConfig.VERSION_CODE, item.versionCode)
                || !UpdatePolicy.compatible(Build.VERSION.SDK_INT, item.minSdk))) { item = null; store.candidate(null); }
        if (item != null) status = text(R.string.updates_version_available, item.versionName);
        panel.automatic.setChecked(store.automatic());
        panel.automatic.setOnCheckedChangeListener((v, checked) -> store.automatic(checked));
        panel.check.setOnClickListener(v -> check(true));
        panel.action.setOnClickListener(v -> { if (!busy && item != null) { if (ready) install(); else download(); } });
        panel.cancel.setOnClickListener(v -> { cancelled = true; status = text(R.string.updates_cancelling); render(); });
        banner.setOnClickListener(v -> showDetails.run());
        render();
        if (item != null && UpdateFileProvider.apk(activity).isFile()) restoreReady();
    }
    public void onResume() {
        if (closed) return;
        resumed = true;
        if (waitingPermission) {
            waitingPermission = false;
            status = activity.getPackageManager().canRequestPackageInstalls()
                    ? text(R.string.updates_install_permission_granted)
                    : text(R.string.updates_install_permission_declined);
            render();
        }
        if (!busy && store.automatic() && UpdatePolicy.shouldCheck(System.currentTimeMillis(), store.lastAttempt())) check(false);
    }
    public void onPause() { resumed = false; }
    public void check(boolean manual) {
        if (closed || busy) return;
        long now = System.currentTimeMillis();
        if (manual && lastManualCheck > 0 && now >= lastManualCheck && now - lastManualCheck < 5000) return;
        if (manual) lastManualCheck = now;
        store.attempted(now); cancelled = false; busy = true; status = text(R.string.updates_checking); render();
        worker.execute(() -> {
            try {
                UpdateManifest found = client.check(Build.VERSION.SDK_INT, BuildConfig.VERSION_CODE, () -> cancelled || closed);
                post(() -> {
                    item = found; store.candidate(found); ready = false; busy = false;
                    status = found == null ? text(R.string.updates_no_new_version)
                            : text(R.string.updates_new_version_download, found.versionName);
                    render();
                    if (found != null && UpdateFileProvider.apk(activity).isFile()) restoreReady();
                });
            } catch (IOException | RuntimeException error) { post(() -> { busy = false;
                status = UpdateMessages.failure(activity, R.string.updates_check_failed, error); render(); }); }
        });
    }
    private void restoreReady() {
        if (busy || item == null || closed) return;
        busy = true; UpdateManifest candidate = item;
        worker.execute(() -> {
            boolean valid;
            try { ApkVerifier.verify(activity, UpdateFileProvider.apk(activity), candidate); valid = true; }
            catch (IOException error) { valid = false; }
            boolean verified = valid;
            post(() -> { busy = false; ready = verified; if (verified) status = text(R.string.updates_download_verified); render(); });
        });
        render();
    }
    public void download() {
        if (closed || busy || item == null) return;
        UpdateManifest candidate = item; cancelled = false; busy = true; downloading = true; ready = false; progress = 0;
        status = text(R.string.updates_downloading); render();
        worker.execute(() -> {
            try {
                File file = client.download(candidate, UpdateFileProvider.directory(activity), () -> cancelled || closed, (received, total) -> {
                    int value = total <= 0 ? 0 : (int)Math.min(100, received * 100 / total);
                    post(() -> { progress = value; render(); });
                });
                if (cancelled || closed) throw new UpdateFailure(UpdateFailure.Code.CANCELLED);
                ApkVerifier.verify(activity, file, candidate);
                if (cancelled || closed) throw new UpdateFailure(UpdateFailure.Code.CANCELLED);
                post(() -> { busy = false; downloading = false; ready = !cancelled;
                    status = text(cancelled ? R.string.updates_cancelled_no_install : R.string.updates_download_verified); render(); });
            } catch (IOException | RuntimeException error) {
                post(() -> { busy = false; downloading = false; ready = false;
                    status = cancelled ? text(R.string.updates_cancelled_preserved)
                            : UpdateMessages.failure(activity, R.string.updates_download_failed, error); render(); });
            }
        });
    }
    private void install() {
        if (closed || busy || !ready || item == null) return;
        // Stop only execution. Repeat count, host selections, timer, ads and floating position stay saved.
        new SettingsStore(activity).enabled(false);
        if (!activity.getPackageManager().canRequestPackageInstalls()) {
            new AlertDialog.Builder(activity).setTitle(text(R.string.updates_install_permission_title))
                    .setMessage(text(R.string.updates_install_permission_body))
                    .setNegativeButton(text(R.string.updates_cancel_button), null).setPositiveButton(text(R.string.updates_open_settings), (dialog, which) -> {
                        try { waitingPermission = true; activity.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                Uri.parse("package:" + activity.getPackageName()))); }
                        catch (RuntimeException error) { waitingPermission = false; status = text(R.string.updates_permission_settings_failed); render(); }
                    }).show();
            return;
        }
        busy = true; status = text(R.string.updates_preinstall_check); render(); UpdateManifest candidate = item;
        worker.execute(() -> {
            try {
                InstallerArtifact.prepare(activity, candidate);
                post(() -> { busy = false; openInstaller(candidate); });
            } catch (IOException error) { post(() -> { busy = false; ready = false;
                status = UpdateMessages.failure(activity, R.string.updates_preinstall_failed, error); render(); }); }
        });
    }
    @SuppressWarnings("deprecation")
    private void openInstaller(UpdateManifest candidate) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        if (!resumed) { status = text(R.string.updates_return_to_install); render(); return; }
        try {
            Uri uri = UpdateFileProvider.uri(activity, candidate.sha256);
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE).setDataAndType(uri, "application/vnd.android.package-archive")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).putExtra(Intent.EXTRA_RETURN_RESULT, true);
            intent.setClipData(ClipData.newRawUri(text(R.string.updates_clip_label), uri));
            activity.startActivityForResult(intent, INSTALL_REQUEST);
            status = text(R.string.updates_confirm_android_installer); render();
        } catch (RuntimeException error) { status = text(R.string.updates_installer_unavailable); render(); }
    }
    public void onInstallResult() {
        if (closed) return;
        status = text(R.string.updates_install_not_completed); render();
    }
    private void render() {
        if (closed) return;
        panel.status.setText(status); panel.check.setEnabled(!busy);
        panel.action.setVisibility(item != null ? View.VISIBLE : View.GONE); panel.action.setEnabled(!busy);
        String label = text(ready ? R.string.updates_install_action : R.string.updates_download_action); panel.action.setText(label);
        panel.cancel.setVisibility(downloading ? View.VISIBLE : View.GONE); panel.cancel.setEnabled(!cancelled);
        panel.progress.setVisibility(downloading ? View.VISIBLE : View.GONE); panel.progress.setProgress(progress);
        banner.setVisibility(item != null ? View.VISIBLE : View.GONE);
        if (item != null) banner.setText(text(R.string.updates_banner, item.versionName));
    }
    private String text(int resource, Object... arguments) { return activity.getString(resource, arguments); }
    private void post(Runnable task) { if (!closed) main.post(() -> { if (!closed) task.run(); }); }
    public void close() { closed = true; cancelled = true; main.removeCallbacksAndMessages(null); worker.shutdownNow(); }
}
