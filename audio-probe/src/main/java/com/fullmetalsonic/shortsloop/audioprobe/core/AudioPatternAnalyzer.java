package com.fullmetalsonic.shortsloop.audioprobe.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * RAM-only 16-kHz mono PCM16 diagnostic. A candidate is an AUDIO period, never a
 * confirmed video ending. An identical chorus may repeat inside a longer video.
 * 100-ms frames, 600-frame/60-second maximum, 3..25-second candidate range.
 * Longer videos, silence tails and missing/distinctiveness-poor audio are not
 * resolved by a timer. Confidence is a similarity score, not a probability.
 */
public final class AudioPatternAnalyzer {
    public record Snapshot(String state, double periodSeconds, double confidence,
                           int featureFrames, int confirmations, int resets, String reason) {}
    /** Counts since clear(); signal resets retain them. No PCM, frequency values or feature vectors. */
    public record Diagnostics(long frames, long valid, long lowRms, long narrow, int recent20Good,
                              long searchAttempts, long recentQualityRejected, long noPeaks,
                              long assessmentRejected, long ambiguous, long shortRepeat,
                              long gapResets, String lastReject) {
        public static Diagnostics empty() {
            return new Diagnostics(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "NONE");
        }
    }
    private static final int FRAME_SAMPLES = 1600, LIMIT_FRAMES = 600, LIMIT_SAMPLES = 960000;
    private static final int MIN_LAG = 30, MAX_LAG = 250, SEARCH_EVERY = 5, CONFIRM_DELAY = 15;
    private static final long GAP_MS = 500;
    private final short[] pending = new short[FRAME_SAMPLES];
    private final float[][] features = new float[LIMIT_FRAMES][SpectralFrame.BANDS];
    private final boolean[] informative = new boolean[LIMIT_FRAMES];
    private final SpectralFrame spectral = new SpectralFrame();
    private int pendingCount, frameCount, receivedSamples, confirmations, resets, quietFrames;
    private int candidateSince = -1, lastReadSamples;
    private long lastElapsed = -1;
    private double pendingPeriod, periodSeconds, confidence;
    private String state = "LISTENING", reason = "COLLECTING";
    private long diagnosticFrames, diagnosticValid, diagnosticLowRms, diagnosticNarrow;
    private long searchAttempts, recentQualityRejected, noPeaks, assessmentRejected, ambiguous, shortRepeat, gapResets;
    private String lastReject = "NONE";

    public AudioPatternAnalyzer() {}

    /** elapsedMs checks continuity only; FFT boundaries and period use sample counts. */
    public void accept(short[] pcm, int count, long elapsedMs) {
        if (pcm == null || count < 0 || count > pcm.length || elapsedMs < 0) {
            reset("INVALID_INPUT"); return;
        }
        if (count == 0) return;
        if (lastElapsed >= 0 && elapsedMs < lastElapsed) reset("BACKWARD_TIME");
        else if (lastElapsed >= 0 && elapsedMs - lastElapsed
                > GAP_MS + Math.max(count, lastReadSamples) * 1000L / 16000) reset("OBSERVATION_GAP");
        lastElapsed = elapsedMs; lastReadSamples = count;
        int usable = Math.min(count, LIMIT_SAMPLES - receivedSamples);
        for (int i = 0; i < usable; i++) {
            pending[pendingCount++] = pcm[i]; receivedSamples++;
            if (pendingCount == FRAME_SAMPLES) {
                processFrame();
                Arrays.fill(pending, (short) 0); pendingCount = 0;
            }
        }
        if (receivedSamples == LIMIT_SAMPLES && !state.equals("CANDIDATE")) {
            state = "UNRESOLVED"; reason = "LIMIT_REACHED";
        }
    }

    public Snapshot snapshot() {
        return new Snapshot(state, periodSeconds, confidence, frameCount, confirmations, resets, reason);
    }

    public Diagnostics diagnostics() {
        return new Diagnostics(diagnosticFrames, diagnosticValid, diagnosticLowRms, diagnosticNarrow,
                recentGood(), searchAttempts, recentQualityRejected, noPeaks, assessmentRejected,
                ambiguous, shortRepeat, gapResets, lastReject);
    }

    public void reset(String reason) {
        clearSignal(); resets++;
        state = "LISTENING"; this.reason = reason == null || reason.isBlank() ? "RESET" : reason;
        if ("OBSERVATION_GAP".equals(reason) || "NO_AUDIO_DATA".equals(reason)) gapResets++;
        lastReject = switch (this.reason) {
            case "OBSERVATION_GAP", "NO_AUDIO_DATA", "BACKWARD_TIME", "INVALID_INPUT" -> this.reason;
            default -> "EXPLICIT_RESET";
        };
    }

    /** Explicitly wipes all PCM, FFT workspaces and retained feature values. */
    public void clear() {
        clearSignal();
        diagnosticFrames = diagnosticValid = diagnosticLowRms = diagnosticNarrow = 0;
        searchAttempts = recentQualityRejected = noPeaks = assessmentRejected = ambiguous = shortRepeat = gapResets = 0;
        lastReject = "NONE";
    }

    private void clearSignal() {
        Arrays.fill(pending, (short) 0);
        for (float[] frame : features) Arrays.fill(frame, 0);
        Arrays.fill(informative, false); spectral.clear();
        pendingCount = frameCount = receivedSamples = confirmations = quietFrames = lastReadSamples = 0;
        candidateSince = -1; lastElapsed = -1;
        pendingPeriod = periodSeconds = confidence = 0;
        state = "CLEARED"; reason = "CLEARED";
    }

    private void processFrame() {
        if (frameCount >= LIMIT_FRAMES) return;
        informative[frameCount] = spectral.analyze(pending, features[frameCount]);
        diagnosticFrames++;
        switch (spectral.lastKind()) {
            case VALID -> diagnosticValid++;
            case LOW_RMS -> diagnosticLowRms++;
            case NARROW -> diagnosticNarrow++;
        }
        frameCount++;
        if (!informative[frameCount - 1]) quietFrames++; else quietFrames = 0;
        if (quietFrames >= 20) {
            lastReject = "SILENT_OR_NARROW_GAP";
            dropCandidate(); state = "NO_PATTERN"; reason = "SILENT_OR_NARROW_GAP";
            if (quietFrames == 20) {
                for (float[] frame : features) Arrays.fill(frame, 0);
                Arrays.fill(informative, false); frameCount = 0; resets++;
            }
            return;
        }
        if (!informative[frameCount - 1]) {
            lastReject = spectral.lastKind() == SpectralFrame.Kind.LOW_RMS ? "LOW_RMS_FRAME" : "NARROW_FRAME";
            dropCandidate(); state = "NO_PATTERN"; reason = "SILENT_OR_NARROW_FRAME";
        }
        if (frameCount % SEARCH_EVERY != 0) return;
        Candidate found = search();
        if (found == null) {
            dropCandidate(); state = "LISTENING"; reason = "NO_DISTINCT_REPEAT"; return;
        }
        if (found.lag < MIN_LAG) {
            shortRepeat++; lastReject = "SHORT_REPEAT";
            dropCandidate(); state = "NO_PATTERN"; reason = "SHORT_REPEAT"; return;
        }
        if (candidateSince < 0 || Math.abs(pendingPeriod - found.lag) > 1.5) {
            if (candidateSince >= 0) lastReject = "PERIOD_UNSTABLE";
            pendingPeriod = found.lag; candidateSince = frameCount; confirmations = 1;
            periodSeconds = confidence = 0; state = "LISTENING"; reason = "VERIFYING_REPEAT"; return;
        }
        pendingPeriod = found.lag; confirmations++;
        if (frameCount - candidateSince < CONFIRM_DELAY) return;
        periodSeconds = found.lag / 10; confidence = Math.min(.99, 1 - found.ratio);
        state = "CANDIDATE"; reason = "AUDIO_PERIOD_ONLY";
    }

    private record Candidate(double lag, double error, double ratio) {}
    private record Comparison(double error, double good, double worstSegment, double largest, double quality) {
        boolean matches() { return error <= .055 && good >= .90 && worstSegment <= .085 && largest <= .45 && quality >= .95; }
    }

    private Candidate search() {
        searchAttempts++;
        if (frameCount < 55) { lastReject = "INSUFFICIENT_HISTORY"; return null; }
        if (recentGood() < 19) {
            recentQualityRejected++; lastReject = "RECENT_QUALITY"; return null;
        }
        int maximum = Math.min(MAX_LAG, (frameCount - 6) / 2);
        List<Candidate> peaks = new ArrayList<>();
        for (double lag = 5; lag <= maximum; lag++) {
            Comparison comparison = compare(lag, Math.max(40, lag), true);
            if (comparison != null && comparison.error <= .075 && comparison.quality >= .95)
                addPeak(peaks, new Candidate(lag, comparison.error, 0));
        }
        if (peaks.isEmpty()) { noPeaks++; lastReject = "NO_COARSE_PEAK"; return null; }
        List<Candidate> accepted = new ArrayList<>();
        for (Candidate peak : peaks) {
            double bestLag = peak.lag, error = peak.error;
            for (double lag = Math.max(5, peak.lag - .8); lag <= Math.min(maximum, peak.lag + .8) + .001; lag += .2) {
                Comparison comparison = compare(lag, Math.max(40, lag), true);
                if (comparison != null && comparison.quality >= .95 && comparison.error < error) {
                    error = comparison.error; bestLag = lag;
                }
            }
            Candidate candidate = assess(bestLag);
            if (candidate != null) accepted.add(candidate);
        }
        if (accepted.isEmpty()) return null;
        accepted.sort(Comparator.comparingDouble(Candidate::ratio).thenComparingDouble(Candidate::lag));
        Candidate best = accepted.get(0);
        for (Candidate candidate : accepted) if (candidate.lag < MIN_LAG) return candidate;
        // Integer multiples are the same repeating sequence, not independent
        // competing periods. Prefer its shortest equally well-supported member.
        for (Candidate candidate : accepted) {
            if (candidate.lag < best.lag && harmonic(candidate.lag, best.lag)
                    && candidate.ratio <= best.ratio + .06) best = candidate;
        }
        for (Candidate candidate : accepted) {
            if (Math.abs(candidate.lag - best.lag) > 3 && !harmonic(candidate.lag, best.lag)
                    && candidate.ratio <= best.ratio + .06) {
                ambiguous++; lastReject = "AMBIGUOUS_PERIOD"; return null;
            }
        }
        return best;
    }

    private int recentGood() {
        int good = 0;
        for (int i = Math.max(0, frameCount - 20); i < frameCount; i++) if (informative[i]) good++;
        return good;
    }

    private static boolean harmonic(double first, double second) {
        double smaller = Math.min(first, second), larger = Math.max(first, second);
        long multiple = Math.round(larger / smaller);
        return multiple >= 2 && Math.abs(larger - smaller * multiple) <= 1.5;
    }

    private void addPeak(List<Candidate> peaks, Candidate candidate) {
        for (int i = 0; i < peaks.size(); i++) if (Math.abs(peaks.get(i).lag - candidate.lag) < 3) {
            if (candidate.error < peaks.get(i).error) peaks.set(i, candidate);
            return;
        }
        peaks.add(candidate);
        peaks.sort(Comparator.comparingDouble(Candidate::error).thenComparingDouble(Candidate::lag));
        if (peaks.size() > 8) peaks.remove(peaks.size() - 1);
    }

    private Candidate assess(double lag) {
        if (lag < 5 || lag > MAX_LAG) return rejectAssessment("OUT_OF_RANGE");
        double window = Math.max(40, lag);
        Comparison matching = compare(lag, window, false);
        if (matching == null) return rejectAssessment("WHOLE_DATA");
        if (!matching.matches()) {
            String code = matching.quality < .95 ? "WHOLE_QUALITY"
                    : matching.error > .055 ? "WHOLE_MEAN_ERROR"
                    : matching.good < .90 ? "WHOLE_MATCH_FRACTION"
                    : matching.worstSegment > .085 ? "WHOLE_SEGMENT_ERROR" : "WHOLE_OUTLIER";
            return rejectAssessment(code);
        }
        // Both sides must have been observed: do not select a falling search-boundary slope.
        for (double offset : new double[]{-3, 3}) {
            Comparison neighbor = compare(lag + offset, window, false);
            if (neighbor == null) return rejectAssessment("BOUNDARY_DATA");
            if (neighbor.error <= matching.error + .012) return rejectAssessment("FLAT_MINIMUM");
        }
        double wrong = Double.POSITIVE_INFINITY;
        for (double fraction : new double[]{.25, .5, .75}) {
            Comparison different = compare(lag * fraction, window, false);
            if (different == null) return rejectAssessment("PHASE_DATA");
            wrong = Math.min(wrong, different.error);
        }
        if (wrong < .08) return rejectAssessment("LOW_PHASE_VARIATION");
        if (matching.error > wrong * .35) return rejectAssessment("PHASE_NOT_DISTINCT");
        return new Candidate(lag, matching.error, matching.error / wrong);
    }

    private Candidate rejectAssessment(String code) {
        assessmentRejected++; lastReject = code;
        return null;
    }

    private Comparison compare(double lag, double window, boolean fast) {
        int length = (int) Math.ceil(window), start = frameCount - length;
        if (lag <= 0 || start < Math.ceil(lag) || length < 10) return null;
        int count = 0, good = 0, quality = 0, segmentCount = 0;
        double sum = 0, segmentSum = 0, worst = 0, largest = 0;
        int stride = fast ? 4 : 1;
        for (int i = start; i < frameCount; i += stride) {
            double at = i - lag;
            int low = (int) Math.floor(at), high = (int) Math.ceil(at);
            double fraction = at - low;
            double dot = 0, norm = 0;
            for (int band = 0; band < SpectralFrame.BANDS; band++) {
                double reference = features[low][band] * (1 - fraction) + features[high][band] * fraction;
                dot += features[i][band] * reference; norm += reference * reference;
            }
            double error = norm <= 1e-12 ? 1 : Math.max(0, 1 - dot / Math.sqrt(norm));
            sum += error; segmentSum += error; segmentCount++; count++;
            largest = Math.max(largest, error);
            if (error <= .10) good++;
            if (informative[i] && informative[low] && informative[high]) quality++;
            if (segmentCount * stride >= 10 || i + stride >= frameCount) {
                worst = Math.max(worst, segmentSum / segmentCount); segmentSum = 0; segmentCount = 0;
            }
        }
        return new Comparison(sum / count, good / (double) count, worst, largest, quality / (double) count);
    }

    private void dropCandidate() {
        pendingPeriod = periodSeconds = confidence = 0; confirmations = 0; candidateSince = -1;
    }
}
