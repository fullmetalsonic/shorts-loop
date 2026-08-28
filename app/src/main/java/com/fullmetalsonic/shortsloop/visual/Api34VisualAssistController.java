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
    private String status = "visual.waiting";
    private long captureCost;

    Api34VisualAssistController(AccessibilityService service, Host host) {
        this.service = service; this.host = host;
    }
    public void reset() {
        // Invalidate results, but retain the physical in-flight slot until its callback returns.
        epoch++; context = null; failed = false; lastFrame = -1; nextCaptureAt = 0;
        current = 0; tracker.reset(); status = "visual.waiting";
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
        if (Build.VERSION.SDK_INT < 34) { status = "visual.error.unsupported"; return; }
        if (count <= 0 || failed) return;
        if (inFlight) {
            if (now - requestStarted > 2000) {
                reset(); context = snapshot; failed = true; errors++; status = "visual.error.timeout";
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
                        if (window == null || !window.contains(snapshot.page)) { unavailable("capture_bounds"); return; }
                        hardware = Bitmap.wrapHardwareBuffer(buffer, screenshot.getColorSpace());
                        if (hardware == null || hardware.getWidth() != window.width() || hardware.getHeight() != window.height()) {
                            unavailable("window_changed"); return;
                        }
                        // Hardware bitmaps cannot be read with getPixel or drawn into a software canvas.
                        software = hardware.copy(Bitmap.Config.ARGB_8888, false);
                        if (software == null) { unavailable("copy_failed"); return; }
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
                        status = result.learning ? "visual.learning" : "estimate.counting";
                        if ("STATIC".equals(result.reason)) status = "visual.static";
                        if ("LEARNING_TIMEOUT".equals(result.reason) || "SHORT_PERIOD".equals(result.reason)) {
                            failed = true; // No more screenshots for this page until an explicit reset/new page.
                            status = "visual.error.repeat_unknown";
                        }
                        host.result(snapshot, result);
                    } catch (RuntimeException ignored) {
                        if (token == epoch) unavailable("processing_failed");
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
                        status = "visual.capture_throttle";
                    } else unavailable(errorCode == AccessibilityService.ERROR_TAKE_SCREENSHOT_NO_ACCESSIBILITY_ACCESS
                            ? "capture_disconnected" : errorCode == AccessibilityService.ERROR_TAKE_SCREENSHOT_SECURE_WINDOW
                            ? "secure_window" : "capture_unavailable");
                }
            });
        } catch (RuntimeException ignored) { inFlight = false; errors++; unavailable("capture_connection"); }
    }
    private void unavailable(String message) {
        tracker.reset(); current = 0; failed = true;
        status = "visual.error." + message;
    }
}
