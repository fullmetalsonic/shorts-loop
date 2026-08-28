package com.fullmetalsonic.shortsloop.core;

/** Pre-swipe delay only. Caller supplies fresh safe ad identity and resets for every unsafe state. */
public final class AdDelayTracker {
    public record Result(boolean active, long remainingMillis, boolean due) {}
    private static final long MAX_GAP_MS = 1500;
    private String key;
    private int tenths;
    private long startedAt = -1, previousAt = -1, remaining;
    private boolean consumed;

    public Result observe(String pageKey, int requestedTenths, long nowMs) {
        if (pageKey == null || pageKey.trim().isEmpty() || nowMs < 0) { reset(); return result(false); }
        if (consumed && pageKey.equals(key)) return result(false);
        int setting = AdDelayPolicy.sanitize(requestedTenths);
        if (!pageKey.equals(key) || setting != tenths || nowMs < previousAt || nowMs - previousAt > MAX_GAP_MS) {
            key = pageKey; tenths = setting; startedAt = nowMs; consumed = false;
        }
        previousAt = nowMs;
        remaining = Math.max(0, tenths * 100L - (nowMs - startedAt));
        if (remaining == 0) { consumed = true; return result(true); }
        return result(false);
    }
    public boolean active() { return key != null && !consumed; }
    public long remainingMillis() { return remaining; }
    public void reset() { key = null; tenths = 0; startedAt = previousAt = -1; remaining = 0; consumed = false; }
    private Result result(boolean due) { return new Result(active(), remaining, due); }
}
