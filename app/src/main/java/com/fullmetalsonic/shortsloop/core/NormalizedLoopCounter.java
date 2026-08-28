package com.fullmetalsonic.shortsloop.core;

import java.util.Objects;

/**
 * Counts observed normalized cycles, never inferring a clip's seconds from its range.
 * Requires a near-start seed, substantial continuous forward coverage and an actual wrap.
 * Gaps, stalls, seeks and identity changes discard counts. Very fast/unobservable cycles
 * intentionally remain uncounted; this is not a clockless timer or duration estimator.
 */
public final class NormalizedLoopCounter {
    private static final long MAX_GAP_MS = 1500;
    private static final long MAX_STALL_MS = 1500;
    private static final double START = .03;
    private static final double EDGE = .06;
    private int target = ModePolicy.DEFAULT_COUNT;
    private int completed, movingSamples;
    private double previous, covered, rate;
    private long previousAt, movedAt, cycleStartedAt;
    private String identity;
    private boolean seeded, wholeCycle, emitted;
    private String diagnostic = "initial";

    public void setTarget(int value) { target = ModePolicy.sanitize(value); reset(); }
    public String diagnostic() { return diagnostic; }
    public boolean pendingAdvance() { return emitted; }
    /** Read-only final/deferred guard: a completed cycle is not a lease to skip a subsequently sought page. */
    public boolean permitsAdvance(NormalizedProgress progress, String pageIdentity, long now) {
        if (!emitted || !seeded || target <= 0 || progress == null || !progress.valid()
                || !Objects.equals(identity, pageIdentity) || now < previousAt || now - previousAt > MAX_GAP_MS) return false;
        double delta = progress.fraction - previous;
        long elapsed = now - previousAt;
        return delta >= 0 && delta <= .16 && (elapsed > 0 || delta == 0)
                && (rate <= 0 || delta <= Math.max(.025, rate * elapsed * 2.5));
    }
    public void reset() {
        completed = movingSamples = 0; previous = covered = rate = 0;
        previousAt = movedAt = cycleStartedAt = 0; identity = null;
        seeded = wholeCycle = emitted = false;
    }
    public LoopCounter.Result observe(NormalizedProgress progress, String pageIdentity, long now) {
        if (target == 0 || progress == null || !progress.valid()
                || pageIdentity == null || pageIdentity.isEmpty()) {
            diagnostic = "unavailable"; reset(); return result(false);
        }
        if (emitted) return result(false);
        double value = progress.fraction;
        if (!seeded || !Objects.equals(identity, pageIdentity)
                || now <= previousAt || now - previousAt > MAX_GAP_MS) {
            diagnostic = !seeded ? "seed" : !Objects.equals(identity, pageIdentity) ? "identity changed" : "observation gap";
            reset(); seed(value, pageIdentity, now); return result(false);
        }
        long elapsed = now - previousAt;
        double delta = value - previous;
        boolean wrap = previous >= 1 - EDGE && value <= EDGE && delta < -.8;
        double movement = wrap ? 1 - previous + value : delta;
        boolean stalled = now - movedAt > MAX_STALL_MS;
        boolean implausible = movement < -.0001 || movement > .16
                || (rate > 0 && movement > Math.max(.025, rate * elapsed * 2.5));
        if (stalled || implausible) {
            diagnostic = stalled ? "stalled" : "jump";
            reset(); seed(value, pageIdentity, now); return result(false);
        }
        boolean advance = false;
        if (wrap) {
            if (wholeCycle && movingSamples >= 8 && covered + (1 - previous) >= .94
                    && now - cycleStartedAt >= 2000) completed++;
            // An observed plausible boundary starts a new cycle, even after a late initial seed.
            wholeCycle = true; covered = value; cycleStartedAt = now; movingSamples = 0;
            if (completed >= target) { emitted = true; advance = true; }
        } else if (delta > 0) {
            covered += delta; movingSamples++;
        }
        if (movement > .00005) {
            movedAt = now;
            double observedRate = movement / elapsed;
            rate = rate == 0 ? observedRate : rate * .85 + observedRate * .15;
        }
        previous = value; previousAt = now;
        diagnostic = advance ? "cycle completed" : "observing";
        return result(advance);
    }
    private void seed(double value, String pageIdentity, long now) {
        seeded = true; previous = value; previousAt = movedAt = cycleStartedAt = now;
        identity = pageIdentity; wholeCycle = value <= START; covered = value;
    }
    private LoopCounter.Result result(boolean advance) {
        return new LoopCounter.Result(target == 0 || !wholeCycle ? 0 : Math.min(target, completed + 1),
                advance, target > 0 && !wholeCycle);
    }
}
