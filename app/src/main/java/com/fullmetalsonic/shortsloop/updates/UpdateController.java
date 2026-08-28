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
    private String status = "새 버전을 확인할 수 있습니다.";
    private int progress;
    private long lastManualCheck;

    public UpdateController(Activity activity, UpdatePanel panel, Button banner, Runnable showDetails) {
        this(activity, panel, banner, showDetails, new GitHubUpdateClient());
    }
    /** Transport dependency injection is for tests; production exposes no alternate endpoint or validation bypass. */
    public UpdateController(Activity activity, UpdatePanel panel, Button banner, Runnable showDetails, GitHubUpdateClient client) {
        this.activity = activity; this.panel = panel; this.banner = banner; this.client = client;
        store = new UpdateStateStore(activity); item = store.candidate();
        if (item != null && (!UpdatePolicy.isNewer(BuildConfig.VERSION_CODE, item.versionCode)
                || !UpdatePolicy.compatible(Build.VERSION.SDK_INT, item.minSdk))) { item = null; store.candidate(null); }
        if (item != null) status = "새 버전 " + item.versionName + "을 사용할 수 있습니다.";
        panel.automatic.setChecked(store.automatic());
        panel.automatic.setOnCheckedChangeListener((v, checked) -> store.automatic(checked));
        panel.check.setOnClickListener(v -> check(true));
        panel.action.setOnClickListener(v -> { if (!busy && item != null) { if (ready) install(); else download(); } });
        panel.cancel.setOnClickListener(v -> { cancelled = true; status = "다운로드 취소 중…"; render(); });
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
                    ? "설치 허용됨 · ‘업데이트 설치’를 눌러 계속하세요."
                    : "설치 허용을 선택하지 않았습니다. 기존 앱은 그대로 사용할 수 있습니다.";
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
        store.attempted(now); cancelled = false; busy = true; status = "GitHub에서 새 버전 확인 중…"; render();
        worker.execute(() -> {
            try {
                UpdateManifest found = client.check(Build.VERSION.SDK_INT, BuildConfig.VERSION_CODE, () -> cancelled || closed);
                post(() -> {
                    item = found; store.candidate(found); ready = false; busy = false;
                    status = found == null ? "이 기기에 적용할 새 업데이트가 없습니다."
                            : "새 버전 " + found.versionName + " · 다운로드 후 직접 설치할 수 있습니다.";
                    render();
                    if (found != null && UpdateFileProvider.apk(activity).isFile()) restoreReady();
                });
            } catch (IOException | RuntimeException error) { post(() -> { busy = false; status = "업데이트 확인 실패 · 네트워크를 확인하고 다시 시도하세요."; render(); }); }
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
            post(() -> { busy = false; ready = verified; if (verified) status = "다운로드·검사 완료 · 업데이트 설치를 눌러 주세요."; render(); });
        });
        render();
    }
    public void download() {
        if (closed || busy || item == null) return;
        UpdateManifest candidate = item; cancelled = false; busy = true; downloading = true; ready = false; progress = 0;
        status = "업데이트 다운로드 중…"; render();
        worker.execute(() -> {
            try {
                File file = client.download(candidate, UpdateFileProvider.directory(activity), () -> cancelled || closed, (received, total) -> {
                    int value = total <= 0 ? 0 : (int)Math.min(100, received * 100 / total);
                    post(() -> { progress = value; render(); });
                });
                if (cancelled || closed) throw new IOException("Cancelled");
                ApkVerifier.verify(activity, file, candidate);
                if (cancelled || closed) throw new IOException("Cancelled");
                post(() -> { busy = false; downloading = false; ready = !cancelled;
                    status = cancelled ? "다운로드를 취소했습니다. 설치하지 않습니다." : "다운로드·검사 완료 · 업데이트 설치를 눌러 주세요."; render(); });
            } catch (IOException | RuntimeException error) {
                post(() -> { busy = false; downloading = false; ready = false;
                    status = cancelled ? "다운로드를 취소했습니다. 기존 앱과 설정은 그대로입니다."
                            : "다운로드 또는 안전 검사 실패 · 설치하지 않았습니다. 다시 시도하세요."; render(); });
            }
        });
    }
    private void install() {
        if (closed || busy || !ready || item == null) return;
        // Stop only execution. Repeat count, host selections, timer, ads and floating position stay saved.
        new SettingsStore(activity).enabled(false);
        if (!activity.getPackageManager().canRequestPackageInstalls()) {
            new AlertDialog.Builder(activity).setTitle("업데이트 설치 허용")
                    .setMessage("다음 Android 화면에서 이 앱의 ‘이 출처 허용’을 직접 켜 주세요. 돌아온 뒤 업데이트 설치를 누르면 최종 설치 확인창이 열립니다. 자동 넘김은 안전을 위해 중지합니다.")
                    .setNegativeButton("취소", null).setPositiveButton("설정 열기", (dialog, which) -> {
                        try { waitingPermission = true; activity.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                Uri.parse("package:" + activity.getPackageName()))); }
                        catch (RuntimeException error) { waitingPermission = false; status = "설치 허용 설정을 열 수 없습니다."; render(); }
                    }).show();
            return;
        }
        busy = true; status = "설치 전 파일 다시 확인 중…"; render(); UpdateManifest candidate = item;
        worker.execute(() -> {
            try {
                InstallerArtifact.prepare(activity, candidate);
                post(() -> { busy = false; openInstaller(candidate); });
            } catch (IOException error) { post(() -> { busy = false; ready = false; status = "설치 전 검사 실패 · 다시 다운로드해 주세요."; render(); }); }
        });
    }
    @SuppressWarnings("deprecation")
    private void openInstaller(UpdateManifest candidate) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        if (!resumed) { status = "앱으로 돌아와 업데이트 설치를 눌러 주세요."; render(); return; }
        try {
            Uri uri = UpdateFileProvider.uri(activity, candidate.sha256);
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE).setDataAndType(uri, "application/vnd.android.package-archive")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).putExtra(Intent.EXTRA_RETURN_RESULT, true);
            intent.setClipData(ClipData.newRawUri("ShortsLoop update", uri));
            activity.startActivityForResult(intent, INSTALL_REQUEST);
            status = "Android 설치창에서 직접 확인해 주세요. 아직 설치 완료가 아닙니다."; render();
        } catch (RuntimeException error) { status = "설치창을 열 수 없습니다. 기기의 설치 제한을 확인해 주세요."; render(); }
    }
    public void onInstallResult() {
        if (closed) return;
        status = "설치가 완료되지 않았습니다. 기존 앱을 사용하거나 다시 설치할 수 있습니다."; render();
    }
    private void render() {
        if (closed) return;
        panel.status.setText(status); panel.check.setEnabled(!busy);
        panel.action.setVisibility(item != null ? View.VISIBLE : View.GONE); panel.action.setEnabled(!busy);
        String label = ready ? "업데이트 설치" : "업데이트 다운로드"; panel.action.setText(label);
        panel.cancel.setVisibility(downloading ? View.VISIBLE : View.GONE); panel.cancel.setEnabled(!cancelled);
        panel.progress.setVisibility(downloading ? View.VISIBLE : View.GONE); panel.progress.setProgress(progress);
        banner.setVisibility(item != null ? View.VISIBLE : View.GONE);
        if (item != null) { String text = "새 버전 " + item.versionName + " · 업데이트 보기"; banner.setText(text); }
    }
    private void post(Runnable task) { if (!closed) main.post(() -> { if (!closed) task.run(); }); }
    public void close() { closed = true; cancelled = true; main.removeCallbacksAndMessages(null); worker.shutdownNow(); }
}
