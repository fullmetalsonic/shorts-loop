package com.fullmetalsonic.shortsloop.visual;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.HardwareBuffer;
import android.os.Build;
import android.os.SystemClock;
import com.fullmetalsonic.shortsloop.core.VisualLoopTracker;
import com.fullmetalsonic.shortsloop.core.VisualCapturePolicy;
import com.fullmetalsonic.shortsloop.detection.YouTubeSnapshot;
import java.util.Arrays;
import java.util.Objects;

/** Opt-in, window-scoped RAM analysis. No files, network, audio or input dispatch. */
final class Api34VisualAssistController extends VisualAssistController {
    private final AccessibilityService service;
    private final Host host;
    private final VisualLoopTracker tracker = new VisualLoopTracker();
    private YouTubeSnapshot context;
    private long epoch, requestStarted, lastFrame = -1, nextCaptureAt, requestId, activeRequestId;
    private boolean inFlight, failed;
    private int target, frames, errors, current;
    private String status = "화면 분석 · 학습 대기";
    private long captureCost;

    Api34VisualAssistController(AccessibilityService service, Host host) {
        this.service = service; this.host = host;
    }
    public void reset() {
        // Invalidate results, but retain the physical in-flight slot until its callback returns.
        epoch++; context = null; failed = false; lastFrame = -1; nextCaptureAt = 0;
        current = 0; tracker.reset(); status = "화면 분석 · 학습 대기";
    }
    public boolean active() { return context != null; }
    public int current() { return current; }
    public String status() { return status; }
    public String diagnostic() {
        return "visual=" + active() + " frames=" + frames + " errors=" + errors
                + " inFlight=" + inFlight + " captureMs=" + captureCost + " "
                + tracker.diagnostic().replace("visual=", "tracker=").replace("frames=", "history=");
    }
    public void observe(YouTubeSnapshot snapshot, int count, long now) {
        if (!samePage(context, snapshot) || target != count) {
            reset(); context = snapshot; target = count;
        }
        if (Build.VERSION.SDK_INT < 34) { status = "화면 분석 · Android 14 미만, 수동 넘김 필요"; return; }
        if (count <= 0 || failed) return;
        if (inFlight) {
            if (now - requestStarted > 2000) {
                reset(); context = snapshot; failed = true; errors++; status = "화면 분석 · 캡처 응답 없음, 수동 넘김 필요";
            }
            return;
        }
        if (now < nextCaptureAt || !host.stillEligible(snapshot)) return;
        final long token = epoch;
        final long ticket = ++requestId; activeRequestId = ticket;
        final long requestedAt = now;
        inFlight = true; requestStarted = now; nextCaptureAt = now + 400;
        try {
            service.takeScreenshotOfWindow(snapshot.windowId, service.getMainExecutor(), new AccessibilityService.TakeScreenshotCallback() {
                @Override public void onSuccess(AccessibilityService.ScreenshotResult screenshot) {
                    HardwareBuffer buffer = screenshot.getHardwareBuffer();
                    Bitmap hardware = null, software = null, small = null;
                    double[] features = null;
                    try {
                        if (activeRequestId == ticket) inFlight = false;
                        if (token != epoch) return;
                        long now = SystemClock.uptimeMillis(), timestamp = screenshot.getTimestamp();
                        if (!VisualCapturePolicy.accepts(token, epoch, requestedAt, timestamp, now, lastFrame)
                                || !host.stillEligible(snapshot) || token != epoch) {
                            reset(); return;
                        }
                        Rect window = snapshot.windowBounds;
                        if (window == null || !window.contains(snapshot.page)) { unavailable("캡처 범위 확인 실패"); return; }
                        hardware = Bitmap.wrapHardwareBuffer(buffer, screenshot.getColorSpace());
                        if (hardware == null || hardware.getWidth() != window.width() || hardware.getHeight() != window.height()) {
                            unavailable("창 크기 변경 · 다시 학습 필요"); return;
                        }
                        // Hardware bitmaps cannot be read with getPixel or drawn into a software canvas.
                        software = hardware.copy(Bitmap.Config.ARGB_8888, false);
                        if (software == null) { unavailable("화면 복사 실패"); return; }
                        Rect p = snapshot.page;
                        Rect roi = new Rect(p.left - window.left + (int) (p.width() * .10),
                                p.top - window.top + (int) (p.height() * .18),
                                p.left - window.left + (int) (p.width() * .75),
                                p.top - window.top + (int) (p.height() * .75));
                        small = Bitmap.createBitmap(16, 24, Bitmap.Config.ARGB_8888);
                        new Canvas(small).drawBitmap(software, roi, new Rect(0, 0, 16, 24), new Paint(Paint.FILTER_BITMAP_FLAG));
                        features = new double[16 * 24 * 3];
                        int k = 0;
                        for (int y = 0; y < 24; y++) for (int x = 0; x < 16; x++) {
                            int rgb = small.getPixel(x, y);
                            features[k++] = (rgb >> 16) & 255; features[k++] = (rgb >> 8) & 255; features[k++] = rgb & 255;
                        }
                        // Revalidate after asynchronous capture/copy; a valid old frame never authorizes input.
                        if (!host.stillEligible(snapshot) || token != epoch) { reset(); return; }
                        lastFrame = timestamp; frames++; captureCost = SystemClock.uptimeMillis() - requestedAt;
                        VisualLoopTracker.Result result = tracker.observe(features, timestamp, count);
                        current = result.current;
                        status = result.learning ? "화면 분석 · 학습 중 (추가 재생 가능)" : "화면 추정 · 재생 횟수 확인 중";
                        if ("STATIC".equals(result.reason)) status = "화면 분석 · 정지 화면 대기";
                        if ("LEARNING_TIMEOUT".equals(result.reason) || "SHORT_PERIOD".equals(result.reason)) {
                            failed = true; // No more screenshots for this page until an explicit reset/new page.
                            status = "화면 분석 · 반복 확인 불가, 수동 넘김 필요";
                        }
                        host.result(snapshot, result);
                    } catch (RuntimeException ignored) {
                        if (token == epoch) unavailable("화면 처리 실패");
                    } finally {
                        if (features != null) Arrays.fill(features, 0);
                        if (small != null) small.recycle();
                        if (software != null) software.recycle();
                        if (hardware != null) hardware.recycle();
                        buffer.close();
                    }
                }
                @Override public void onFailure(int errorCode) {
                    if (activeRequestId == ticket) inFlight = false;
                    if (token != epoch) return;
                    errors++;
                    if (errorCode == AccessibilityService.ERROR_TAKE_SCREENSHOT_INTERVAL_TIME_SHORT) {
                        tracker.reset(); current = 0; nextCaptureAt = SystemClock.uptimeMillis() + 2000;
                        status = "화면 분석 · 캡처 간격 조정 중";
                    } else unavailable(errorCode == AccessibilityService.ERROR_TAKE_SCREENSHOT_NO_ACCESSIBILITY_ACCESS
                            ? "접근성 캡처 기능 연결 필요" : errorCode == AccessibilityService.ERROR_TAKE_SCREENSHOT_SECURE_WINDOW
                            ? "보호된 화면 · 수동 넘김 필요" : "캡처 불가 · 수동 넘김 필요");
                }
            });
        } catch (RuntimeException ignored) { inFlight = false; errors++; unavailable("캡처 기능 연결 필요"); }
    }
    private void unavailable(String message) {
        tracker.reset(); current = 0; failed = true;
        status = "화면 분석 · " + message + (message.contains("수동 넘김") ? "" : " · 수동 넘김 필요");
    }
}
