package com.fullmetalsonic.shortsloop.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Opt-in visual-cycle estimate, not a media clock or proof of a video's real start.
 * RGB features are 16x24x3 values in [0,255]. Only RAM copies are retained.
 * Two returning moving anchors and a whole-sequence match are required to learn.
 * Learning cycles never count; only subsequently confirmed visual cycles count.
 */
public final class VisualLoopTracker {
    public static final int FEATURE_COUNT = 16 * 24 * 3;
    private static final int HISTORY_LIMIT = 224, TEMPLATE_LIMIT = 88, ANCHOR_POINTS = 5;
    private static final long MIN_PERIOD_MS = 3000, MAX_PERIOD_MS = 60000;
    private static final long MAX_GAP_MS = 2000, STATIC_MS = 2000, LEARNING_LIMIT_MS = 130000;
    private static final long MIN_SAMPLE_MS = 300, PAIR_SLOP_MS = 500, CLUSTER_MS = 1100;
    private static final double PIXEL_MOVEMENT = 3, MOVING_PIXELS = .25;

    public static final class Result {
        public final int current;
        public final boolean advance, learning;
        public final double periodSeconds;
        public final String reason;
        private Result(int current, boolean advance, boolean learning, double periodSeconds, String reason) {
            this.current = current; this.advance = advance; this.learning = learning;
            this.periodSeconds = periodSeconds; this.reason = reason;
        }
    }
    private static final class Frame {
        final float[] features;
        final long at;
        Frame(double[] values, long at) {
            features = new float[FEATURE_COUNT];
            for (int i = 0; i < FEATURE_COUNT; i++) features[i] = (float) values[i];
            this.at = at;
        }
    }
    private static final class Match {
        final long at;
        final double error;
        Match(long at, double error) { this.at = at; this.error = error; }
    }

    private final List<Frame> history = new ArrayList<>();
    private final List<Frame> template = new ArrayList<>();
    private Frame[] anchor;
    private Frame motionReference;
    private long previousAt = -1, learningStarted = -1, lastMotionAt = -1;
    private long anchorEnd, firstReturn, lastReturn, clusterStarted, clusterLastHit;
    private Match pending;
    private int target = -1, completed;
    private double periodMs, anchorSpread;
    private boolean learned, emitted, timedOut, shortPeriod;
    private String reason = "RESET";
    // Scalar-only diagnostics survive later observations and explicit resets.
    // A restart includes initial/changed target, OFF and caller reset as well as safety resets.
    private long restartCount, lastRestartAt = -1, lastRestartIdleMs = -1;
    private String lastRestart = "NONE", lastRestartDetail = "NONE";
    private double lastRestartPeriod, lastRestartDelta = -1, lastRestartMoving = -1;
    private double sampleDelta = -1, sampleMoving = -1, motionDelta = -1, motionMoving = -1;

    public void reset() {
        reset("EXPLICIT_RESET", previousAt);
    }
    private void reset(String code, long now) {
        recordRestart(code, "NONE", now);
        clearTracking();
        previousAt = learningStarted = lastMotionAt = -1;
        motionReference = null; target = -1; emitted = timedOut = shortPeriod = false;
        sampleDelta = sampleMoving = motionDelta = motionMoving = -1;
        reason = "RESET";
    }

    public Result observe(double[] features, long now, int requestedTarget) {
        sampleDelta = sampleMoving = motionDelta = motionMoving = -1;
        if (requestedTarget < 0 || requestedTarget > 99) { reset("INVALID_TARGET", now); reason = "INVALID_TARGET"; return result(false); }
        if (requestedTarget == 0) { reset("OFF", now); target = 0; reason = "OFF"; return result(false); }
        if (target != requestedTarget) {
            reset("TARGET_CHANGED", now); target = requestedTarget; reason = "TARGET_CHANGED";
        }
        // A transient capture failure must never re-arm the same emitted video.
        if (emitted) { reason = "LATCHED"; return result(false); }
        if (!valid(features) || now < 0) { restart(null, -1, "INVALID_FEATURES", true); return result(false); }
        if (previousAt >= 0 && now <= previousAt) {
            restart(features, now, "INVALID_TIMESTAMP", true); return result(false);
        }
        if (previousAt >= 0 && now - previousAt > MAX_GAP_MS) {
            restart(features, now, "OBSERVATION_GAP", true); return result(false);
        }
        previousAt = now;
        if (shortPeriod) { reason = "SHORT_PERIOD"; return result(false); }
        if (timedOut) { reason = "LEARNING_TIMEOUT"; return result(false); }
        if (learningStarted < 0) learningStarted = now;
        if (!learned && now - learningStarted >= LEARNING_LIMIT_MS) {
            recordRestart("LEARNING_TIMEOUT", "NONE", now);
            clearTracking(); motionReference = null; timedOut = true;
            reason = "LEARNING_TIMEOUT"; return result(false);
        }
        Frame frame = new Frame(features, now);
        if (!history.isEmpty()) {
            float[] previous = history.get(history.size() - 1).features;
            sampleDelta = distance(previous, frame.features);
            sampleMoving = movingFraction(previous, frame.features);
        }
        if (motionReference != null) {
            motionDelta = distance(motionReference.features, frame.features);
            motionMoving = movingFraction(motionReference.features, frame.features);
        }
        if (motionReference == null || broadMotion(motionReference.features, frame.features)) {
            motionReference = frame; lastMotionAt = now;
        } else if (now - lastMotionAt >= STATIC_MS) {
            restart(features, now, "STATIC", false); return result(false);
        }
        if (!history.isEmpty() && now - history.get(history.size() - 1).at < MIN_SAMPLE_MS)
            return result(false);
        history.add(frame);
        if (history.size() > HISTORY_LIMIT) history.remove(0);
        if (anchor == null) {
            chooseAnchor();
            reason = anchor == null ? "LEARNING_ANCHOR" : "LEARNING_REPEAT";
            return result(false);
        }

        Match hit = matchAnchor(now);
        if (hit != null && hit.at - lastReturn >= 1000) {
            if (pending == null) { pending = hit; clusterStarted = now; }
            else if (hit.error < pending.error) pending = hit;
            clusterLastHit = now;
        }
        if (pending != null && (now - clusterLastHit >= 500 || now - clusterStarted >= CLUSTER_MS)) {
            Match accepted = pending; pending = null;
            boolean advance = acceptReturn(accepted, frame);
            if (advance || anchor == null) return result(advance);
        }
        if (periodMs > 0 && now > lastReturn + periodMs + periodTolerance(periodMs) + CLUSTER_MS + PAIR_SLOP_MS) {
            restart(features, now, "PERIOD_CHANGED", learned); return result(false);
        }
        if (periodMs == 0 && now > anchorEnd + MAX_PERIOD_MS + CLUSTER_MS) {
            restart(features, now, "NO_REPEAT", false); return result(false);
        }
        return result(false);
    }

    public String diagnostic() {
        return String.format(Locale.ROOT, "visual=%s frames=%d anchor=%d template=%d period=%.3f completed=%d target=%d"
                        + " restarts=%d lastRestart=%s restartDetail=%s restartAt=%d restartPeriod=%.3f restartIdleMs=%d"
                        + " restartDelta=%.3f restartMoving=%.3f sampleDelta=%.3f sampleMoving=%.3f motionDelta=%.3f motionMoving=%.3f",
                reason, history.size(), anchor == null ? 0 : anchor.length, template.size(), periodMs / 1000, completed, target,
                restartCount, lastRestart, lastRestartDetail, lastRestartAt, lastRestartPeriod, lastRestartIdleMs,
                lastRestartDelta, lastRestartMoving, sampleDelta, sampleMoving, motionDelta, motionMoving);
    }

    private void chooseAnchor() {
        if (history.size() < ANCHOR_POINTS) return;
        int start = history.size() - ANCHOR_POINTS;
        long span = history.get(history.size() - 1).at - history.get(start).at;
        if (span < 1400 || span > 2800) return;
        Frame[] candidate = new Frame[ANCHOR_POINTS];
        int moving = 0;
        double spread = 0;
        for (int i = 0; i < ANCHOR_POINTS; i++) {
            candidate[i] = history.get(start + i);
            if (i > 0 && broadMotion(candidate[i - 1].features, candidate[i].features)) moving++;
            for (int j = 0; j < i; j++) spread += distance(candidate[j].features, candidate[i].features);
        }
        spread /= ANCHOR_POINTS * (ANCHOR_POINTS - 1) / 2d;
        if (moving < 3 || spread < 10) return;
        anchor = candidate; anchorSpread = spread;
        anchorEnd = candidate[ANCHOR_POINTS - 1].at;
        lastReturn = anchorEnd;
    }

    private Match matchAnchor(long now) {
        if (now - anchorEnd < 1000 || history.size() < ANCHOR_POINTS) return null;
        double error = 0;
        int previousIndex = -1, distinctive = 0;
        double limit = Math.min(28, Math.max(8, anchorSpread * .5));
        for (int i = 0; i < anchor.length; i++) {
            long expected = now - (anchorEnd - anchor[i].at);
            int index = closest(expected, previousIndex + 1);
            if (index < 0) return null;
            Frame candidate = history.get(index);
            double matchError = distance(anchor[i].features, candidate.features);
            if (matchError > limit * 1.6) return null;
            double otherError = 0;
            for (int j = 0; j < anchor.length; j++) if (j != i)
                otherError += distance(anchor[j].features, candidate.features);
            if (matchError + 2 < otherError / (anchor.length - 1)) distinctive++;
            error += matchError;
            previousIndex = index;
        }
        error /= anchor.length;
        return error <= limit && distinctive >= 4 ? new Match(now, error) : null;
    }

    private boolean acceptReturn(Match occurrence, Frame currentFrame) {
        double interval = occurrence.at - lastReturn;
        if (interval < MIN_PERIOD_MS) {
            recordRestart("SHORT_PERIOD", "NONE", currentFrame.at);
            clearTracking(); motionReference = null; shortPeriod = true; reason = "SHORT_PERIOD";
            return false;
        }
        if (interval > MAX_PERIOD_MS) return false;
        if (periodMs == 0) {
            periodMs = interval; firstReturn = occurrence.at; lastReturn = occurrence.at;
            if (!makeTemplate()) { restartFrame(currentFrame, "LOW_CONFIDENCE", "TEMPLATE_UNAVAILABLE", false); return false; }
            reason = "CONFIRMING_REPEAT"; return false;
        }
        boolean intervalMismatch = Math.abs(interval - periodMs) > periodTolerance(periodMs);
        if (intervalMismatch || !matchesWholeCycle(occurrence.at, interval)) {
            restartFrame(currentFrame, "LOW_CONFIDENCE", intervalMismatch ? "INTERVAL_MISMATCH" : "CYCLE_MISMATCH", learned);
            return false;
        }
        lastReturn = occurrence.at;
        if (!learned) {
            learned = true; completed = 0;
            periodMs = (periodMs + interval) / 2;
            reason = "TRACKING";
            return false;
        }
        completed++;
        if (completed >= target) {
            emitted = true; reason = "ADVANCE";
            history.clear(); template.clear(); anchor = null; motionReference = null;
            return true;
        }
        reason = "CYCLE_COMPLETED";
        return false;
    }

    private boolean makeTemplate() {
        List<Frame> cycle = new ArrayList<>();
        for (Frame frame : history) if (frame.at >= anchorEnd && frame.at <= firstReturn) cycle.add(frame);
        if (cycle.size() < 6 || cycle.get(0).at - anchorEnd > PAIR_SLOP_MS
                || firstReturn - cycle.get(cycle.size() - 1).at > PAIR_SLOP_MS) return false;
        template.clear();
        int count = Math.min(TEMPLATE_LIMIT, cycle.size());
        for (int i = 0; i < count; i++) template.add(cycle.get((int) Math.round(i * (cycle.size() - 1d) / (count - 1))));
        return true;
    }

    private boolean matchesWholeCycle(long end, double interval) {
        int pairs = 0, good = 0, previousIndex = -1;
        double error = 0, wrongPhaseError = 0;
        for (int i = 0; i < template.size(); i++) {
            Frame reference = template.get(i);
            double phase = (reference.at - anchorEnd) / (double) (firstReturn - anchorEnd);
            long expected = Math.round(end - interval + phase * interval);
            int index = closest(expected, previousIndex + 1);
            if (index < 0) continue;
            previousIndex = index;
            Frame actual = history.get(index);
            double currentError = distance(reference.features, actual.features);
            error += currentError;
            if (currentError <= 35) good++;
            wrongPhaseError += distance(template.get((i + template.size() / 2) % template.size()).features, actual.features);
            pairs++;
        }
        return pairs >= 6 && pairs >= template.size() * .85 && good >= pairs * .8
                && error / pairs <= 28 && wrongPhaseError / pairs >= 10 && error <= wrongPhaseError * .60;
    }

    private int closest(long expected, int from) {
        int best = -1;
        long bestError = PAIR_SLOP_MS + 1;
        for (int i = from; i < history.size(); i++) {
            long error = Math.abs(history.get(i).at - expected);
            if (error < bestError) { bestError = error; best = i; }
            if (history.get(i).at > expected + PAIR_SLOP_MS) break;
        }
        return best;
    }

    private void restart(double[] features, long now, String code, boolean restartDeadline) {
        recordRestart(code, "NONE", now);
        long deadlineStart = learningStarted;
        clearTracking(); motionReference = null; previousAt = now; lastMotionAt = now;
        timedOut = shortPeriod = false;
        learningStarted = restartDeadline ? now : deadlineStart;
        if (features != null) {
            Frame seed = new Frame(features, now); history.add(seed); motionReference = seed;
        }
        reason = code;
    }
    private void restartFrame(Frame frame, String code, String detail, boolean restartDeadline) {
        recordRestart(code, detail, frame.at);
        long deadlineStart = learningStarted;
        clearTracking(); history.add(frame); motionReference = frame; lastMotionAt = frame.at;
        learningStarted = restartDeadline ? frame.at : deadlineStart;
        reason = code;
    }
    private void recordRestart(String code, String detail, long now) {
        restartCount++;
        lastRestart = code; lastRestartDetail = detail; lastRestartAt = now;
        lastRestartPeriod = periodMs / 1000;
        lastRestartIdleMs = lastMotionAt >= 0 && now >= lastMotionAt ? now - lastMotionAt : -1;
        lastRestartDelta = motionDelta; lastRestartMoving = motionMoving;
    }
    private void clearTracking() {
        history.clear(); template.clear(); anchor = null; pending = null;
        anchorEnd = firstReturn = lastReturn = clusterStarted = clusterLastHit = 0;
        periodMs = anchorSpread = 0; completed = 0; learned = false;
    }
    private Result result(boolean advance) {
        int current = target <= 0 || !learned ? 0 : Math.min(target, completed + 1);
        return new Result(current, advance, target > 0 && !learned, periodMs / 1000, reason);
    }
    private static boolean valid(double[] features) {
        if (features == null || features.length != FEATURE_COUNT) return false;
        for (double value : features) if (!Double.isFinite(value) || value < 0 || value > 255) return false;
        return true;
    }
    private static double periodTolerance(double period) { return Math.min(900, Math.max(650, period * .025)); }
    private static double distance(float[] a, float[] b) {
        double sum = 0;
        for (int i = 0; i < FEATURE_COUNT; i++) sum += Math.abs(a[i] - b[i]);
        return sum / FEATURE_COUNT;
    }
    private static boolean broadMotion(float[] a, float[] b) {
        int moving = 0;
        for (int i = 0; i < FEATURE_COUNT; i += 3)
            if ((Math.abs(a[i] - b[i]) + Math.abs(a[i + 1] - b[i + 1]) + Math.abs(a[i + 2] - b[i + 2])) / 3 >= PIXEL_MOVEMENT) moving++;
        return moving >= FEATURE_COUNT / 3 * MOVING_PIXELS && distance(a, b) >= 1;
    }
    private static double movingFraction(float[] a, float[] b) {
        int moving = 0;
        for (int i = 0; i < FEATURE_COUNT; i += 3)
            if ((Math.abs(a[i] - b[i]) + Math.abs(a[i + 1] - b[i + 1]) + Math.abs(a[i + 2] - b[i + 2])) / 3 >= PIXEL_MOVEMENT) moving++;
        return moving / (FEATURE_COUNT / 3d);
    }
}
