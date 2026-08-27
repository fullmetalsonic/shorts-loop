package com.fullmetalsonic.shortsloop.core;

import java.util.Locale;

/**
 * Explicit clockless-page time limit. No media counting, collection or input dispatch.
 * The host supplies a safe identity/window/geometry key, and resets on unsafe states.
 * Qualification's first two seconds are INCLUDED in the selected total duration.
 */
public final class ClocklessTimeoutTracker {
    public record Result(boolean active, boolean qualifying, int remainingSeconds, boolean due) {}
    private static final long QUALIFY_MS = 2000, MAX_GAP_MS = 1500;
    private String pageKey;
    private int seconds = ClocklessTimeoutPolicy.DEFAULT_SECONDS, remaining;
    private long startedAt = -1, previousAt = -1, elapsed;
    private boolean consumed, qualifying;
    private String reason = "IDLE";

    public Result observe(String key, int requestedSeconds, long nowMs) {
        if (key == null || key.trim().isEmpty()) {
            reset(); reason = "UNSAFE_PAGE"; return result(false);
        }
        // A consumed page cannot be re-armed by scheduling gaps or changing a setting.
        // Only a different safe key or an explicit host reset starts another attempt.
        if (consumed && key.equals(pageKey)) { reason = "CONSUMED"; return result(false); }
        if (nowMs < 0) { reset(); reason = "INVALID_TIME"; return result(false); }
        int normalizedSeconds = ClocklessTimeoutPolicy.sanitizeSeconds(requestedSeconds);
        if (!key.equals(pageKey)) {
            start(key, normalizedSeconds, nowMs, "PAGE_CHANGED"); return result(false);
        }
        if (normalizedSeconds != seconds) {
            start(key, normalizedSeconds, nowMs, "SETTING_CHANGED"); return result(false);
        }
        if (nowMs < previousAt) {
            start(key, normalizedSeconds, nowMs, "CLOCK_RESET"); return result(false);
        }
        if (nowMs - previousAt > MAX_GAP_MS) {
            start(key, normalizedSeconds, nowMs, "OBSERVATION_GAP"); return result(false);
        }
        previousAt = nowMs;
        // Subtraction stays safe for nonnegative monotonic timestamps; no overflowing deadline addition.
        elapsed = nowMs - startedAt;
        qualifying = elapsed < QUALIFY_MS;
        long duration = seconds * 1000L;
        remaining = (int) ((Math.max(0, duration - elapsed) + 999) / 1000);
        if (!qualifying && elapsed >= duration) {
            consumed = true; remaining = 0; reason = "DUE";
            return result(true);
        }
        reason = qualifying ? "QUALIFYING" : "COUNTDOWN";
        return result(false);
    }

    public void reset() {
        pageKey = null; seconds = ClocklessTimeoutPolicy.DEFAULT_SECONDS; remaining = 0;
        startedAt = previousAt = -1; elapsed = 0; consumed = qualifying = false; reason = "IDLE";
    }

    public boolean active() { return pageKey != null && !consumed; }
    /** Last-observed remaining whole seconds, rounded upward; this method does not read a clock. */
    public int remainingSeconds() { return remaining; }
    public String diagnostic() {
        return String.format(Locale.ROOT, "clocklessTimeout=%s active=%s qualifying=%s seconds=%d remaining=%d elapsedMs=%d consumed=%s",
                reason, active(), qualifying, seconds, remaining, elapsed, consumed);
    }

    private void start(String key, int setting, long now, String code) {
        pageKey = key; seconds = setting; startedAt = previousAt = now;
        elapsed = 0; remaining = seconds; consumed = false; qualifying = true; reason = code;
    }
    private Result result(boolean due) {
        return new Result(active(), active() && qualifying, remaining, due);
    }
}
