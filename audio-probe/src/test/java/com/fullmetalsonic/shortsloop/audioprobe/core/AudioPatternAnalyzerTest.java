package com.fullmetalsonic.shortsloop.audioprobe.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

public class AudioPatternAnalyzerTest {
    private static final int RATE = 16000;
    private static final double[][] OSCILLATORS = oscillators();
    private interface Signal { short sample(int index); }
    private record Run(AudioPatternAnalyzer analyzer, boolean candidateSeen, int firstCandidateAt) {}

    private static double[][] oscillators() {
        int[] frequencies = {180, 310, 520, 820, 1270, 1930, 2870, 4170};
        double[][] values = new double[frequencies.length][RATE];
        for (int band = 0; band < frequencies.length; band++)
            for (int i = 0; i < RATE; i++) values[band][i] = Math.sin(2 * Math.PI * frequencies[band] * i / RATE);
        return values;
    }
    private static int hash(int note) {
        int value = note * 0x45d9f3b + 71;
        value = (value ^ (value >>> 16)) * 0x45d9f3b;
        return value ^ (value >>> 16);
    }
    private static double chord(int localSample, int note) {
        int seed = hash(note), first = seed & 7, second = (first + 1 + ((seed >>> 4) & 3)) & 7;
        int third = (second + 1 + ((seed >>> 8) & 3)) & 7;
        if (third == first) third = (third + 1) & 7;
        if (third == second) third = (third + 1) & 7;
        int phase = localSample % RATE;
        return .40 * OSCILLATORS[first][phase] + .34 * OSCILLATORS[second][phase]
                + .26 * OSCILLATORS[third][phase];
    }
    private static short varied(int sample, double periodSeconds, double gain) {
        int period = (int) Math.round(periodSeconds * RATE), local = sample % period;
        int note = local / 5920, within = local % 5920;
        double blend = Math.min(1, within / 1280d);
        double value = chord(local, note) * blend + chord(local, Math.max(0, note - 1)) * (1 - blend);
        return (short) Math.round(value * 14000 * gain);
    }
    private static Run run(Signal signal, double seconds, int[] chunks) {
        AudioPatternAnalyzer analyzer = new AudioPatternAnalyzer();
        int end = (int) (seconds * RATE), cursor = 0, part = 0, first = -1;
        boolean seen = false;
        while (cursor < end) {
            int count = Math.min(chunks[part++ % chunks.length], end - cursor);
            short[] pcm = new short[count];
            for (int i = 0; i < count; i++) pcm[i] = signal.sample(cursor + i);
            analyzer.accept(pcm, count, cursor * 1000L / RATE);
            Arrays.fill(pcm, (short) 0);
            if (analyzer.snapshot().state().equals("CANDIDATE")) {
                seen = true;
                if (first < 0) first = cursor + count;
            }
            cursor += count;
        }
        return new Run(analyzer, seen, first);
    }
    private static Run repeating(double period, double seconds) {
        return run(i -> varied(i, period, 1), seconds, new int[]{1600});
    }
    private static void assertCandidate(Run result, double expected) {
        assertTrue(result.analyzer.snapshot().toString(), result.candidateSeen);
        AudioPatternAnalyzer.Snapshot snapshot = result.analyzer.snapshot();
        assertEquals(snapshot.toString(), "CANDIDATE", snapshot.state());
        assertEquals(expected, snapshot.periodSeconds(), .12);
        assertTrue(snapshot.confidence() > .6); assertTrue(snapshot.confirmations() >= 4);
        assertTrue(result.firstCandidateAt >= (2 * expected + 1.5) * RATE);
    }

    @Test public void tenSecondRichAudioProducesOnlyAudioCandidate() {
        Run result = repeating(10, 36);
        assertCandidate(result, 10);
        assertEquals("AUDIO_PERIOD_ONLY", result.analyzer.snapshot().reason());
    }
    @Test public void fractionalThirteenPointSevenThreeSecondPeriod() {
        assertCandidate(repeating(13.73, 48), 13.73);
    }
    @Test public void twentySecondPeriodFitsSixtySecondSession() {
        assertCandidate(repeating(20, 59), 20);
    }
    @Test public void silenceDoesNotInventRepeat() {
        Run result = run(i -> (short) 0, 60, new int[]{1600});
        assertFalse(result.candidateSeen); assertEquals(0, result.analyzer.snapshot().periodSeconds(), 0);
    }
    @Test public void dcAndSingleToneAreNotPatterns() {
        assertFalse(run(i -> (short) 12000, 30, new int[]{1600}).candidateSeen);
        assertFalse(run(i -> (short) (12000 * OSCILLATORS[3][i % RATE]), 30, new int[]{1600}).candidateSeen);
    }
    @Test public void stationaryChordIsNotDistinctive() {
        assertFalse(run(i -> (short) (14000 * chord(i, 3)), 40, new int[]{1600}).candidateSeen);
    }
    @Test public void subThreeSecondBeatDoesNotBecomeLongerHarmonic() {
        Run result = repeating(1.8, 40);
        assertFalse(result.analyzer.snapshot().toString(), result.candidateSeen);
    }
    @Test public void unrelatedNonRepeatingNoteOrderDoesNotMatch() {
        Run result = run(i -> varied(i, 1000, 1), 60, new int[]{1600});
        assertFalse(result.analyzer.snapshot().toString(), result.candidateSeen);
    }
    @Test public void randomNoiseDoesNotInventPeriod() {
        Random random = new Random(519);
        Run result = run(i -> (short) (random.nextInt(22001) - 11000), 60, new int[]{1600});
        assertFalse(result.analyzer.snapshot().toString(), result.candidateSeen);
    }
    @Test public void gainChangesAndLowNoiseDoNotChangeSpectralPeriod() {
        Random random = new Random(81);
        Run result = run(i -> {
            double gain = i < 10 * RATE ? .8 : i < 20 * RATE ? .35 : .65;
            return (short) (varied(i, 10, gain) + random.nextInt(101) - 50);
        }, 38, new int[]{1600});
        assertCandidate(result, 10);
    }
    @Test public void variableReadChunksHaveIdenticalFeatureTimeAndResult() {
        Run fixed = repeating(10, 36);
        Run chunks = run(i -> varied(i, 10, 1), 36, new int[]{1, 37, 511, 3201, 19, 8192, 800});
        assertEquals(fixed.analyzer.snapshot(), chunks.analyzer.snapshot());
        assertEquals(fixed.candidateSeen, chunks.candidateSeen);
    }
    @Test public void threeSecondFundamentalSurvivesLongerHarmonics() {
        assertCandidate(repeating(3, 59), 3);
    }
    @Test public void sixSecondFundamentalSurvivesLongerHarmonics() {
        assertCandidate(repeating(6, 59), 6);
    }
    @Test public void twentyFiveSecondUpperBoundaryCanBeObserved() {
        assertCandidate(repeating(25, 59), 25);
    }
    @Test public void periodsOverTwentyFiveSecondsRemainUnsupported() {
        Run result = repeating(30, 60);
        assertFalse(result.analyzer.snapshot().toString(), result.candidateSeen);
    }
    @Test public void matchingChorusButChangingOtherSectionsDoesNotMatchWholeLoop() {
        Run result = run(i -> {
            int cycle = i / (10 * RATE), local = i % (10 * RATE);
            return local < 6 * RATE ? varied(local, 10, 1)
                    : varied(local + (cycle + 1) * 7 * RATE, 1000, 1);
        }, 60, new int[]{1600});
        assertFalse(result.analyzer.snapshot().toString(), result.candidateSeen);
    }
    @Test public void repeatWithTwoSecondSilentTailIsNotResolvedAsVideoLength() {
        Run result = run(i -> {
            int local = i % (12 * RATE);
            return local >= 10 * RATE ? 0 : varied(local, 10, 1);
        }, 59, new int[]{1600});
        assertFalse(result.analyzer.snapshot().toString(), result.candidateSeen);
    }
    @Test public void silenceAfterCandidateImmediatelyRemovesStalePeriod() {
        Run result = run(i -> i < 32 * RATE ? varied(i, 10, 1) : 0, 33, new int[]{1600});
        assertTrue(result.candidateSeen);
        assertEquals(0, result.analyzer.snapshot().periodSeconds(), 0);
        assertNotEquals("CANDIDATE", result.analyzer.snapshot().state());
    }
    @Test public void changedAudioDoesNotRetainEarlierCandidate() {
        Run result = run(i -> i < 32 * RATE ? varied(i, 10, 1)
                : varied(i + 17 * RATE, 1000, 1), 43, new int[]{1600});
        assertTrue(result.candidateSeen);
        assertEquals(0, result.analyzer.snapshot().periodSeconds(), 0);
    }
    @Test public void positiveInputAfterMissingAudioGapResets() {
        AudioPatternAnalyzer analyzer = repeating(10, 32).analyzer;
        assertEquals("CANDIDATE", analyzer.snapshot().state());
        short[] pcm = new short[1600];
        for (int i = 0; i < pcm.length; i++) pcm[i] = varied(i, 10, 1);
        analyzer.accept(pcm, 1600, 32600);
        assertEquals("OBSERVATION_GAP", analyzer.snapshot().reason());
        assertEquals(0, analyzer.snapshot().periodSeconds(), 0);
        assertEquals(1, analyzer.snapshot().featureFrames());
        assertEquals(1, analyzer.snapshot().resets());
    }
    @Test public void normalVariableReadDurationIsNotMistakenForGap() {
        Run result = run(i -> varied(i, 10, 1), 36, new int[]{16000, 80, 4000, 1600});
        assertCandidate(result, 10); assertEquals(0, result.analyzer.snapshot().resets());
    }
    @Test public void backwardTimeAndInvalidInputResetWithoutRetainingCandidate() {
        AudioPatternAnalyzer analyzer = repeating(10, 32).analyzer;
        short[] pcm = new short[1600];
        for (int i = 0; i < pcm.length; i++) pcm[i] = varied(i, 10, 1);
        analyzer.accept(pcm, 1600, 100);
        assertEquals("BACKWARD_TIME", analyzer.snapshot().reason());
        assertEquals(0, analyzer.snapshot().periodSeconds(), 0);
        analyzer.accept(null, 1, 200);
        assertEquals("INVALID_INPUT", analyzer.snapshot().reason());
        assertEquals(0, analyzer.snapshot().featureFrames());
        analyzer.accept(new short[1], 2, 200);
        assertEquals("INVALID_INPUT", analyzer.snapshot().reason());
    }
    @Test public void explicitResetDiscardsOldPatternAndStartsNewSampleTimeline() {
        AudioPatternAnalyzer analyzer = repeating(10, 32).analyzer;
        analyzer.reset("USER_CHANGE");
        assertEquals("USER_CHANGE", analyzer.snapshot().reason());
        assertEquals(0, analyzer.snapshot().featureFrames()); assertEquals(0, analyzer.snapshot().periodSeconds(), 0);
        assertEquals(1, analyzer.snapshot().resets());
        analyzer.accept(new short[1600], 1600, 0);
        assertEquals(1, analyzer.snapshot().featureFrames());
    }
    @Test public void sixtySecondCapIsIndependentOfReadChunkAndFeatureCountIsBounded() {
        Run sixty = repeating(10, 60), longer = repeating(10, 75);
        assertEquals(sixty.analyzer.snapshot(), longer.analyzer.snapshot());
        assertEquals(600, longer.analyzer.snapshot().featureFrames());
    }
    @Test public void clearExplicitlyZeroesAllCapturedArraysAndWorkspace() throws Exception {
        AudioPatternAnalyzer analyzer = repeating(10, 32).analyzer;
        analyzer.accept(new short[]{17, 31, -100}, 3, 32000);
        analyzer.clear();
        assertEquals("CLEARED", analyzer.snapshot().state());
        assertEquals(0, analyzer.snapshot().featureFrames()); assertEquals(0, analyzer.snapshot().periodSeconds(), 0);
        assertCapturedArraysZero(analyzer);
    }
    private static void assertCapturedArraysZero(Object owner) throws Exception {
        for (Field field : owner.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);
            Object value = field.get(owner);
            if (value instanceof short[] array) for (short item : array) assertEquals(0, item);
            if (value instanceof double[] array) for (double item : array) assertEquals(0, item, 0);
            if (value instanceof float[][] array) for (float[] row : array) for (float item : row) assertEquals(0, item, 0);
            if (value instanceof boolean[] array) for (boolean item : array) assertFalse(item);
            if (value instanceof SpectralFrame) assertCapturedArraysZero(value);
        }
    }
    @Test public void diagnosticsEmptyAndClearEraseAllDiagnosticCounters() {
        AudioPatternAnalyzer analyzer = new AudioPatternAnalyzer();
        assertEquals(AudioPatternAnalyzer.Diagnostics.empty(), analyzer.diagnostics());
        analyzer = repeating(10, 32).analyzer;
        assertTrue(analyzer.diagnostics().frames() > 0);
        analyzer.reset("NO_AUDIO_DATA");
        assertEquals(1, analyzer.diagnostics().gapResets());
        analyzer.clear();
        assertEquals(AudioPatternAnalyzer.Diagnostics.empty(), analyzer.diagnostics());
    }
    @Test public void diagnosticsClassifyCompletedFramesWithoutExposingSpectra() {
        AudioPatternAnalyzer analyzer = new AudioPatternAnalyzer();
        short[] pcm = new short[RATE];
        for (int i = 0; i < pcm.length; i++) pcm[i] = varied(i, 10, 1);
        analyzer.accept(pcm, pcm.length, 0);
        for (int i = 0; i < pcm.length; i++) pcm[i] = (short) (12000 * OSCILLATORS[3][i]);
        analyzer.accept(pcm, pcm.length, 1000);
        Arrays.fill(pcm, (short) 12000);
        analyzer.accept(pcm, pcm.length, 2000);
        AudioPatternAnalyzer.Diagnostics d = analyzer.diagnostics();
        assertEquals(30, d.frames()); assertEquals(10, d.valid());
        assertEquals(10, d.lowRms()); assertEquals(10, d.narrow());
        assertEquals(d.frames(), d.valid() + d.lowRms() + d.narrow());
        assertEquals(0, d.recent20Good()); assertEquals(0, d.gapResets());
        assertEquals(1, analyzer.snapshot().resets());
    }
    @Test public void diagnosticsSurviveInternalTwoSecondNarrowOrSilentReset() {
        AudioPatternAnalyzer analyzer = run(i -> (short) 0, 6, new int[]{1600}).analyzer;
        AudioPatternAnalyzer.Diagnostics d = analyzer.diagnostics();
        assertEquals(60, d.frames()); assertEquals(60, d.lowRms()); assertEquals(0, d.valid());
        assertEquals(1, analyzer.snapshot().resets()); assertEquals(40, analyzer.snapshot().featureFrames());
        assertEquals("SILENT_OR_NARROW_GAP", d.lastReject()); assertEquals(0, d.gapResets());
    }
    @Test public void explicitSignalResetPreservesDiagnosticLifetime() {
        AudioPatternAnalyzer analyzer = repeating(10, 32).analyzer;
        AudioPatternAnalyzer.Diagnostics before = analyzer.diagnostics();
        analyzer.reset("NO_AUDIO_DATA");
        AudioPatternAnalyzer.Diagnostics after = analyzer.diagnostics();
        assertEquals(before.frames(), after.frames()); assertEquals(before.valid(), after.valid());
        assertEquals(before.searchAttempts(), after.searchAttempts());
        assertEquals(0, after.recent20Good()); assertEquals(1, after.gapResets());
        assertEquals("NO_AUDIO_DATA", after.lastReject());
        assertEquals(0, analyzer.snapshot().featureFrames());
    }
    @Test public void positiveGapDiagnosticPreservesEarlierFrameTotals() {
        AudioPatternAnalyzer analyzer = repeating(10, 32).analyzer;
        short[] pcm = new short[1600];
        for (int i = 0; i < pcm.length; i++) pcm[i] = varied(i, 10, 1);
        analyzer.accept(pcm, pcm.length, 32600);
        assertEquals(321, analyzer.diagnostics().frames()); assertEquals(1, analyzer.diagnostics().gapResets());
        assertEquals("OBSERVATION_GAP", analyzer.diagnostics().lastReject());
        assertEquals(1, analyzer.snapshot().featureFrames());
    }
    @Test public void speechLikeBriefPausesAreIdentifiableAsRecentQualityRejection() {
        Run result = run(i -> i % RATE >= RATE * .8 ? 0 : varied(i, 10, 1), 12, new int[]{1600});
        AudioPatternAnalyzer.Diagnostics d = result.analyzer.diagnostics();
        assertEquals(120, d.frames()); assertEquals(96, d.valid()); assertEquals(24, d.lowRms());
        assertEquals(16, d.recent20Good()); assertTrue(d.recentQualityRejected() > 0);
        assertEquals(0, d.noPeaks()); assertEquals(0, d.assessmentRejected());
        assertEquals("RECENT_QUALITY", d.lastReject()); assertFalse(result.candidateSeen);
    }
    @Test public void unrelatedAudioProducesSearchOrAssessmentRejectionCounters() {
        Run result = run(i -> varied(i, 1000, 1), 30, new int[]{1600});
        AudioPatternAnalyzer.Diagnostics d = result.analyzer.diagnostics();
        assertTrue(d.searchAttempts() > 0); assertTrue(d.noPeaks() + d.assessmentRejected() > 0);
        assertFalse(result.candidateSeen);
    }
    @Test public void stationaryChordReportsFlatMinimumRatherThanMissingInput() {
        Run result = run(i -> (short) (14000 * chord(i, 3)), 15, new int[]{1600});
        AudioPatternAnalyzer.Diagnostics d = result.analyzer.diagnostics();
        assertEquals(150, d.valid()); assertEquals(0, d.lowRms()); assertEquals(0, d.narrow());
        assertTrue(d.assessmentRejected() > 0); assertEquals("FLAT_MINIMUM", d.lastReject());
        assertFalse(result.candidateSeen);
    }
    @Test public void shortRepeatRejectionIsCountedSeparately() {
        Run result = repeating(1.8, 20);
        assertTrue(result.analyzer.diagnostics().shortRepeat() > 0);
        assertEquals("SHORT_REPEAT", result.analyzer.diagnostics().lastReject());
        assertFalse(result.candidateSeen);
    }
    @Test public void diagnosticCountsAreAlsoIndependentOfReadChunkBoundaries() {
        Run fixed = repeating(10, 36);
        Run chunks = run(i -> varied(i, 10, 1), 36, new int[]{1, 37, 511, 3201, 19, 8192, 800});
        assertEquals(fixed.analyzer.diagnostics(), chunks.analyzer.diagnostics());
        AudioPatternAnalyzer.Diagnostics before = chunks.analyzer.diagnostics();
        for (int i = 0; i < 100; i++) chunks.analyzer.diagnostics();
        assertEquals(before, chunks.analyzer.diagnostics());
    }
}
