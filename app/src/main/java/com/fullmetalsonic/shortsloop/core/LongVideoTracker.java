package com.fullmetalsonic.shortsloop.core;

/** Stable duration/page plus a fresh forward tick. Only consume after final request validation. */
public final class LongVideoTracker {
    private String key;
    private long started, previousAt;
    private Progress previous;
    private int threshold;
    private boolean consumed;
    private final ProgressMotion motion = new ProgressMotion();
    public boolean observe(String safeKey, Progress progress, int seconds, long now) {
        if (safeKey == null || safeKey.isEmpty() || now < 0 || !LongVideoPolicy.qualifies(true, progress, seconds)) {
            reset(); return false;
        }
        if (consumed && safeKey.equals(key)) return false;
        if (!safeKey.equals(key) || seconds != threshold || previous == null
                || now < previousAt || now - previousAt > 1500
                || Math.abs(progress.duration - previous.duration) > 0.5
                || progress.position < previous.position || !motion.accept(progress.position, now)) {
            key = safeKey; threshold = seconds; started = previousAt = now; previous = progress; consumed = false;
            motion.reset(progress.position, now); return false;
        }
        boolean due = now > previousAt && now - started >= 700 && progress.position - previous.position >= 0.1;
        previousAt = now; previous = progress;
        return due;
    }
    public void consume() { if (key != null) consumed = true; }
    public boolean active() { return key != null && !consumed; }
    public void reset() { key = null; previous = null; started = previousAt = 0; consumed = false; }
}
