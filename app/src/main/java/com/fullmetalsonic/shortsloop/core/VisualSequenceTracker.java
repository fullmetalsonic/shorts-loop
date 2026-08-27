package com.fullmetalsonic.shortsloop.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Experimental whole-sequence visual correlation, not a media playback clock.
 * At least two complete observed periods must agree before learning; those periods
 * never count. Later counts require another observed, matching complete sequence.
 * Repeated actions inside a video remain indistinguishable from real video loops.
 */
public final class VisualSequenceTracker {
    public static final int FEATURE_COUNT = 16 * 24 * 3;
    private static final int HISTORY_LIMIT = 300, FAST_PAIRS = 24, FAST_STRIDE = 19, PEAK_LIMIT = 8;
    private static final long MIN_PERIOD = 3000, MAX_PERIOD = 60000, MIN_WINDOW = 6000;
    private static final long MIN_SAMPLE = 300, MAX_GAP = 2000, STATIC_MS = 2000;
    private static final long PAIR_SLOP = 350, SEARCH_INTERVAL = 900, LEARNING_LIMIT = 130000;
    private static final double MOVING_FRACTION = .25, PIXEL_MOVEMENT = 3;

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
        final long at;
        final float[] pixels = new float[FEATURE_COUNT];
        Frame(double[] values, long at) {
            this.at = at;
            for (int i = 0; i < FEATURE_COUNT; i++) pixels[i] = (float) values[i];
        }
    }
    private static final class Comparison {
        final double error, goodFraction, coverage, alignedLag;
        Comparison(double error, double goodFraction, double coverage, double alignedLag) {
            this.error = error; this.goodFraction = goodFraction; this.coverage = coverage;
            this.alignedLag = alignedLag;
        }
        boolean similar() { return coverage >= .88 && goodFraction >= .82 && error <= 24; }
    }
    private static final class Candidate {
        final double period, error, ratio;
        Candidate(double period, double error, double ratio) {
            this.period = period; this.error = error; this.ratio = ratio;
        }
    }

    private final List<Frame> history = new ArrayList<>();
    private Frame motionReference;
    private int target = -1, completed;
    private long previousAt = -1, lastMotionAt = -1, learningStarted = -1, lastSearch = -1;
    private long candidateSince = -1, lastConfirmedAt = -1, restartCount, restartAt = -1;
    private double period, candidatePeriod, candidateError = -1, candidateRatio = -1;
    private double motionDelta = -1, motionMoving = -1;
    private boolean learned, emitted, stopped, ambiguous;
    private String reason = "RESET", lastRestart = "NONE";

    public void reset() { clear("EXPLICIT_RESET", -1, true); target = -1; }

    public Result observe(double[] features, long now, int requestedTarget) {
        if (requestedTarget < 0 || requestedTarget > 99) {
            clear("INVALID_TARGET", -1, true); target = -1; return result(false);
        }
        if (requestedTarget == 0) {
            clear("OFF", -1, true); target = 0; return result(false);
        }
        if (target != requestedTarget) {
            clear("TARGET_CHANGED", -1, true); target = requestedTarget;
        }
        if (emitted) { reason = "LATCHED"; return result(false); }
        if (!valid(features) || now < 0) {
            clear("INVALID_FEATURES", -1, true); return result(false);
        }
        if (previousAt >= 0 && now <= previousAt) {
            clear("INVALID_TIMESTAMP", now, true); seed(features, now); return result(false);
        }
        if (previousAt >= 0 && now - previousAt > MAX_GAP) {
            clear("OBSERVATION_GAP", now, true); seed(features, now); return result(false);
        }
        previousAt = now;
        if (stopped) return result(false);
        if (learningStarted < 0) learningStarted = now;
        if (!learned && now - learningStarted >= LEARNING_LIMIT) {
            clear("LEARNING_TIMEOUT", now, false); stopped = true; return result(false);
        }

        Frame frame = new Frame(features, now);
        motionDelta = motionReference == null ? -1 : distance(motionReference.pixels, frame.pixels, 1);
        motionMoving = motionReference == null ? -1 : movingFraction(motionReference.pixels, frame.pixels);
        if (motionReference == null || (motionDelta >= 1 && motionMoving >= MOVING_FRACTION)) {
            motionReference = frame; lastMotionAt = now;
        } else if (now - lastMotionAt >= STATIC_MS) {
            boolean hadLearned = learned;
            clear("STATIC", now, hadLearned); seed(features, now); return result(false);
        }
        if (!history.isEmpty() && now - history.get(history.size() - 1).at < MIN_SAMPLE) return result(false);
        history.add(frame);
        if (history.size() > HISTORY_LIMIT) history.remove(0);

        if (learned) return verifyNextCycle(now);
        if (lastSearch >= 0 && now - lastSearch < SEARCH_INTERVAL) return result(false);
        lastSearch = now;
        Candidate candidate = findCandidate(now);
        if (candidate == null) {
            candidatePeriod = 0; candidateSince = -1;
            reason = ambiguous ? "AMBIGUOUS_PERIOD" : "LEARNING_SEQUENCE";
            return result(false);
        }
        candidateError = candidate.error; candidateRatio = candidate.ratio;
        if (candidate.period < MIN_PERIOD) {
            clear("SHORT_PERIOD", now, false); stopped = true; return result(false);
        }
        if (candidateSince < 0 || Math.abs(candidatePeriod - candidate.period) > tolerance(candidate.period)) {
            candidatePeriod = candidate.period; candidateSince = now;
            reason = "CONFIRMING_SEQUENCE"; return result(false);
        }
        candidatePeriod = candidate.period;
        if (now - candidateSince < SEARCH_INTERVAL) return result(false);
        period = candidate.period; learned = true; completed = 0; lastConfirmedAt = now;
        reason = "TRACKING";
        return result(false);
    }

    public String diagnostic() {
        return String.format(Locale.ROOT,
                "visual=%s algorithm=sequence frames=%d period=%.3f candidate=%.3f error=%.3f ratio=%.3f completed=%d target=%d"
                        + " restarts=%d lastRestart=%s restartAt=%d motionDelta=%.3f motionMoving=%.3f",
                reason, history.size(), period / 1000, candidatePeriod / 1000, candidateError, candidateRatio,
                completed, target, restartCount, lastRestart, restartAt, motionDelta, motionMoving);
    }

    private Result verifyNextCycle(long now) {
        if (now - lastConfirmedAt < period) return result(false);
        Candidate verified = assess(period, now);
        if (verified != null && Math.abs(verified.period - period) <= tolerance(period)) {
            candidateError = verified.error; candidateRatio = verified.ratio;
            completed++; lastConfirmedAt = now;
            if (completed >= target) {
                emitted = true; history.clear(); motionReference = null;
                reason = "ADVANCE"; return result(true);
            }
            reason = "CYCLE_COMPLETED"; return result(false);
        }
        reason = "VERIFYING_CYCLE";
        if (now - lastConfirmedAt > period + Math.max(1200, period * .04)) {
            Frame current = history.get(history.size() - 1);
            clear("PERIOD_CHANGED", now, true);
            history.add(current); motionReference = current; lastMotionAt = now;
        }
        return result(false);
    }

    private Candidate findCandidate(long now) {
        ambiguous = false;
        if (history.size() < 16) return null;
        long available = now - history.get(0).at;
        double maximum = Math.min(MAX_PERIOD, available / 2d);
        List<Candidate> peaks = new ArrayList<>();
        for (double lag = 1000; lag <= maximum; lag += 100) {
            if (available < lag + Math.max(MIN_WINDOW, lag)) continue;
            Comparison comparison = compare(lag, Math.max(MIN_WINDOW, lag), now, true);
            if (comparison != null && comparison.similar())
                addPeak(peaks, new Candidate(lag, comparison.error, 0));
        }
        List<Candidate> accepted = new ArrayList<>();
        for (Candidate peak : peaks) {
            double bestLag = peak.period, bestError = peak.error;
            for (double lag = Math.max(1000, peak.period - 150); lag <= Math.min(maximum, peak.period + 150); lag += 25) {
                if (available < lag + Math.max(MIN_WINDOW, lag)) continue;
                Comparison comparison = compare(lag, Math.max(MIN_WINDOW, lag), now, true);
                if (comparison != null && comparison.similar() && comparison.error < bestError) {
                    bestLag = lag; bestError = comparison.error;
                }
            }
            Candidate candidate = assess(bestLag, now);
            if (candidate != null) accepted.add(candidate);
        }
        if (accepted.isEmpty()) return null;
        accepted.sort(Comparator.comparingDouble((Candidate c) -> c.ratio).thenComparingDouble(c -> c.period));
        Candidate best = accepted.get(0);
        // A visible sub-three-second fundamental must not become an accepted longer harmonic.
        for (Candidate candidate : accepted) if (candidate.period < MIN_PERIOD) return candidate;
        for (int i = 1; i < accepted.size(); i++) {
            Candidate other = accepted.get(i);
            if (Math.abs(other.period - best.period) > Math.max(600, best.period * .04)
                    && other.ratio <= best.ratio + .08) {
                ambiguous = true; return null;
            }
        }
        return best;
    }

    private void addPeak(List<Candidate> peaks, Candidate candidate) {
        for (int i = 0; i < peaks.size(); i++) {
            Candidate previous = peaks.get(i);
            if (Math.abs(previous.period - candidate.period) < Math.max(450, candidate.period * .02)) {
                if (candidate.error < previous.error) peaks.set(i, candidate);
                return;
            }
        }
        peaks.add(candidate);
        peaks.sort(Comparator.comparingDouble((Candidate c) -> c.error).thenComparingDouble(c -> c.period));
        if (peaks.size() > PEAK_LIMIT) peaks.remove(peaks.size() - 1);
    }

    private Candidate assess(double lag, long now) {
        double window = Math.max(MIN_WINDOW, lag);
        if (history.isEmpty() || now - history.get(0).at < lag + window) return null;
        Comparison matching = compare(lag, window, now, false);
        if (matching == null || !matching.similar()) return null;
        // Nearest-frame matching has a flat error plateau. Use the actual paired
        // timestamps instead of selecting the first (shortest) grid point on it.
        lag = matching.alignedLag;
        window = Math.max(MIN_WINDOW, lag);
        if (now - history.get(0).at < lag + window) return null;
        matching = compare(lag, window, now, false);
        if (matching == null || !matching.similar()) return null;
        double wrongPhase = Double.POSITIVE_INFINITY;
        for (double fraction : new double[]{.25, .5, .75}) {
            Comparison other = compare(lag * fraction, window, now, false);
            if (other == null || other.coverage < .88) return null;
            wrongPhase = Math.min(wrongPhase, other.error);
        }
        if (wrongPhase < 10 || matching.error > wrongPhase * .50) return null;
        return new Candidate(lag, matching.error, matching.error / wrongPhase);
    }

    private Comparison compare(double lag, double window, long now, boolean fast) {
        int start = lowerBound(now - window), total = history.size() - start;
        if (total < 12 || history.get(start).at - (now - window) > PAIR_SLOP) return null;
        int samples = fast ? Math.min(FAST_PAIRS, total) : total;
        int pairs = 0, good = 0, previousMatch = -1;
        double error = 0, alignedLag = 0;
        for (int n = 0; n < samples; n++) {
            int index = start + (int) Math.round(n * (total - 1d) / (samples - 1));
            Frame recent = history.get(index);
            int match = closest(recent.at - lag);
            if (match <= previousMatch) continue;
            previousMatch = match;
            if (match < 0) continue;
            double difference = distance(recent.pixels, history.get(match).pixels, fast ? FAST_STRIDE : 1);
            error += difference;
            alignedLag += recent.at - history.get(match).at;
            if (difference <= 35) good++;
            pairs++;
        }
        if (pairs < 12) return null;
        return new Comparison(error / pairs, good / (double) pairs, pairs / (double) samples, alignedLag / pairs);
    }

    private int lowerBound(double at) {
        int low = 0, high = history.size();
        while (low < high) {
            int middle = (low + high) >>> 1;
            if (history.get(middle).at < at) low = middle + 1; else high = middle;
        }
        return low;
    }
    private int closest(double at) {
        int right = lowerBound(at), best = -1;
        double error = PAIR_SLOP + .001;
        if (right < history.size() && Math.abs(history.get(right).at - at) < error) {
            best = right; error = Math.abs(history.get(right).at - at);
        }
        if (right > 0 && Math.abs(history.get(right - 1).at - at) < error) best = right - 1;
        return best;
    }
    private void seed(double[] features, long now) {
        Frame frame = new Frame(features, now);
        history.add(frame); motionReference = frame; lastMotionAt = now; previousAt = now;
    }
    private void clear(String code, long now, boolean restartDeadline) {
        restartCount++; lastRestart = code; restartAt = now;
        history.clear(); motionReference = null;
        previousAt = now; lastMotionAt = lastSearch = candidateSince = lastConfirmedAt = -1;
        if (restartDeadline) learningStarted = now;
        period = candidatePeriod = 0; candidateError = candidateRatio = motionDelta = motionMoving = -1;
        completed = 0; learned = emitted = stopped = ambiguous = false; reason = code;
    }
    private Result result(boolean advance) {
        return new Result(target <= 0 || !learned ? 0 : Math.min(target, completed + 1), advance,
                target > 0 && !learned, period / 1000, reason);
    }
    private static double tolerance(double value) { return Math.min(750, Math.max(300, value * .025)); }
    private static boolean valid(double[] features) {
        if (features == null || features.length != FEATURE_COUNT) return false;
        for (double value : features) if (!Double.isFinite(value) || value < 0 || value > 255) return false;
        return true;
    }
    private static double distance(float[] a, float[] b, int stride) {
        double sum = 0; int count = 0;
        for (int i = 0; i < FEATURE_COUNT; i += stride) { sum += Math.abs(a[i] - b[i]); count++; }
        return sum / count;
    }
    private static double movingFraction(float[] a, float[] b) {
        int moving = 0;
        for (int i = 0; i < FEATURE_COUNT; i += 3)
            if ((Math.abs(a[i] - b[i]) + Math.abs(a[i + 1] - b[i + 1]) + Math.abs(a[i + 2] - b[i + 2])) / 3 >= PIXEL_MOVEMENT) moving++;
        return moving / (FEATURE_COUNT / 3d);
    }
}
