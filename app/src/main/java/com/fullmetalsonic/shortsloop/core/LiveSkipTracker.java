package com.fullmetalsonic.shortsloop.core;

/** Live preview delay, independent of repeat count. The host supplies a safe, settled page key. */
public final class LiveSkipTracker {
    public record Result(boolean active, boolean settling, int remainingSeconds, boolean due) {}
    private static final long SETTLE_MS = 250, MAX_GAP_MS = 1200;
    private String key;
    private int seconds, remaining;
    private long started = -1, previous = -1;
    private boolean consumed, settling;

    public Result observe(String safeKey, int delaySeconds, long now) {
        if (safeKey == null || safeKey.isBlank() || now < 0) { reset(); return result(false); }
        if (consumed && safeKey.equals(key)) return result(false);
        int requested = LiveSkipPolicy.sanitizeSeconds(delaySeconds);
        if (!safeKey.equals(key) || requested != seconds || now < previous || now - previous > MAX_GAP_MS) {
            key = safeKey; seconds = requested; remaining = requested;
            started = previous = now; consumed = false; settling = true;
            return result(false);
        }
        previous = now;
        long elapsed = now - started;
        settling = elapsed < SETTLE_MS;
        remaining = (int) ((Math.max(0, seconds * 1000L - elapsed) + 999) / 1000);
        if (!settling && elapsed >= seconds * 1000L) {
            consumed = true; remaining = 0;
            return result(true);
        }
        return result(false);
    }
    public void reset() { key = null; seconds = remaining = 0; started = previous = -1; consumed = settling = false; }
    public boolean active() { return key != null && !consumed; }
    public String diagnostic() {
        return "liveDelayActive=" + active() + " settling=" + settling + " remaining=" + remaining + " consumed=" + consumed;
    }
    private Result result(boolean due) { return new Result(active(), active() && settling, remaining, due); }
}
