package com.fullmetalsonic.shortsloop.core;

import java.util.Objects;

/** No timers advance a video. Only a well-observed end-to-start transition may do so. */
public final class LoopCounter {
    public static final class Result {
        public final int current;
        public final boolean advance;
        public final boolean waitingForStart;
        Result(int current, boolean advance, boolean waiting) {
            this.current = current; this.advance = advance; this.waitingForStart = waiting;
        }
    }
    private int target = 2;
    private int completed;
    private Progress previous;
    private long previousAt;
    private String identity;
    private boolean wholeCycle;
    private double covered;
    private boolean emitted;
    private long cycleStartedAt;
    private final ProgressMotion motion = new ProgressMotion();
    private String diagnostic = "initial";
    public String diagnostic() { return diagnostic; }

    public void setTarget(int target) { this.target = ModePolicy.sanitize(target); reset(); }
    public void reset() {
        completed = 0; previous = null; identity = null; wholeCycle = false;
        covered = 0; emitted = false; previousAt = 0; cycleStartedAt = 0;
    }
    public Result observe(Progress progress, String videoIdentity, long now) {
        if (target == 0 || progress == null || !progress.valid()) { reset(); return result(false); }
        if (emitted) return result(false);
        if (previous == null || !Objects.equals(identity, videoIdentity)
                || Math.abs(previous.duration - progress.duration) > 0.5
                || now <= previousAt || now - previousAt > 3000) {
            diagnostic = previous == null ? "seed" : !Objects.equals(identity, videoIdentity) ? "identity changed"
                    : Math.abs(previous.duration - progress.duration) > 0.5 ? "duration changed" : "observation gap";
            reset(); seed(progress, videoIdentity, now); return result(false);
        }
        double delta = progress.position - previous.position;
        double boundary = Math.min(2, Math.max(1, progress.duration * 0.05));
        boolean wrap = previous.position >= progress.duration - boundary
                && progress.position <= boundary && delta < -1;
        boolean advance = false;
        if (wrap) {
            double tail = progress.duration - previous.position;
            double minimumCoverage = progress.duration - Math.min(1, progress.duration * 0.1);
            double minimumElapsed = Math.max(750, progress.duration / 2.25 * 1000 - 250);
            if (wholeCycle && covered + tail >= minimumCoverage && now - cycleStartedAt >= minimumElapsed) completed++;
            wholeCycle = true;
            covered = progress.position;
            cycleStartedAt = now;
            motion.reset(progress.position, now);
            if (completed >= target) { emitted = true; advance = true; }
        } else if (delta < -0.5 || !motion.accept(progress.position, now)) {
            diagnostic = "jump from=" + previous.position + " to=" + progress.position + " elapsedMs=" + (now - previousAt);
            // Backward seek or implausible forward jump: discard all accumulated counts.
            reset(); seed(progress, videoIdentity, now); return result(false);
        } else {
            covered += Math.max(0, delta);
        }
        previous = progress; previousAt = now; identity = videoIdentity;
        return result(advance);
    }
    private void seed(Progress progress, String videoIdentity, long now) {
        previous = progress; previousAt = now; identity = videoIdentity;
        wholeCycle = progress.position <= Math.min(1, progress.duration * 0.1);
        covered = progress.position;
        cycleStartedAt = now;
        motion.reset(progress.position, now);
    }
    private Result result(boolean advance) {
        int current = target == 0 || !wholeCycle ? 0 : Math.min(target, completed + 1);
        return new Result(current, advance, !wholeCycle && target > 0);
    }
}
