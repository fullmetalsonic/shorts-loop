package com.fullmetalsonic.shortsloop.core;

import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import static org.junit.Assert.*;

public class VisualSequenceTrackerTest {
    private static final int SIZE = VisualSequenceTracker.FEATURE_COUNT;
    private static double[] wave(double millis, double period) {
        double phase = millis / period * Math.PI * 2;
        double[] values = new double[SIZE];
        for (int i = 0; i < SIZE; i++) values[i] = 127 + 75 * Math.sin(phase + (i % 17) * .37)
                + 25 * Math.cos(phase * 2 + (i % 13) * .23);
        return values;
    }
    private static double[] picture(double millis, double period) {
        double phase = millis / period * Math.PI * 2;
        double[] values = new double[SIZE];
        for (int y = 0; y < 24; y++) for (int x = 0; x < 16; x++) {
            int i = (y * 16 + x) * 3;
            values[i] = 127 + 70 * Math.sin(x * .43 + phase) + 35 * Math.cos(y * .57 - phase * 2);
            values[i + 1] = 127 + 70 * Math.sin(y * .31 - phase) + 35 * Math.cos(x * .37 + phase * 3);
            values[i + 2] = 127 + 70 * Math.cos(x * .22 + y * .29 + phase) + 35 * Math.sin(phase * 2 + y * .4);
        }
        return values;
    }
    private static double[] still() { double[] values = new double[SIZE]; Arrays.fill(values, 100); return values; }
    private static double[] noise(Random random) {
        double[] values = new double[SIZE];
        for (int i = 0; i < SIZE; i++) values[i] = random.nextInt(256);
        return values;
    }
    private static int number(String text, String key) {
        Matcher match = Pattern.compile("(?:^| )" + key + "=(\\d+)").matcher(text);
        assertTrue(text, match.find()); return Integer.parseInt(match.group(1));
    }
    private static VisualSequenceTracker.Result feed(VisualSequenceTracker tracker, long start, long end, int target) {
        VisualSequenceTracker.Result result = null;
        for (long t = start; t <= end; t += 450) result = tracker.observe(wave(t, 9000), t, target);
        return result;
    }

    @Test public void learnsTwoObservedPeriodsThenOneExtraCompletePeriod() {
        VisualSequenceTracker tracker = new VisualSequenceTracker();
        boolean advanced = false;
        for (long t = 0; t <= 45000; t += 450) {
            VisualSequenceTracker.Result result = tracker.observe(wave(t, 9000), t, 1);
            if (!result.learning) { assertTrue(t >= 18000); assertEquals(1, result.current); }
            if (result.advance) {
                assertTrue(t >= 27000); assertEquals(9, result.periodSeconds, .35); advanced = true; break;
            }
        }
        assertTrue(tracker.diagnostic(), advanced);
    }
    @Test public void targetTwoCountsOnlyNewFullSequencesAndLatches() {
        VisualSequenceTracker tracker = new VisualSequenceTracker();
        boolean sawOne = false, sawTwo = false;
        int advances = 0;
        for (long t = 0; t <= 65000; t += 450) {
            VisualSequenceTracker.Result result = tracker.observe(wave(t, 9000), t, 2);
            sawOne |= result.current == 1; sawTwo |= result.current == 2;
            if (result.advance) { advances++; assertTrue(t >= 36000); }
        }
        assertTrue(sawOne); assertTrue(sawTwo); assertEquals(tracker.diagnostic(), 1, advances);
        assertEquals("LATCHED", tracker.observe(null, 100000, 2).reason);
    }
    @Test public void spatialMotionWithJitterAndPixelNoiseCanLearn() {
        VisualSequenceTracker tracker = new VisualSequenceTracker();
        int[] gaps = {370, 510, 430, 470, 390, 530};
        Random random = new Random(12);
        long t = 0; boolean advanced = false;
        for (int n = 0; t <= 65000; n++) {
            double[] frame = picture(t + 3170, 13700);
            for (int i = 0; i < SIZE; i++) frame[i] += random.nextDouble() * 3 - 1.5;
            VisualSequenceTracker.Result result = tracker.observe(frame, t, 1);
            if (result.advance) { advanced = true; assertEquals(13.7, result.periodSeconds, .35); break; }
            t += gaps[n % gaps.length];
        }
        assertTrue(tracker.diagnostic(), advanced);
    }
    @Test public void longSpatialSceneDoesNotDependOnOneMovingAnchor() {
        VisualSequenceTracker tracker = new VisualSequenceTracker();
        boolean advanced = false;
        for (long t = 0; t <= 135000; t += 450) {
            VisualSequenceTracker.Result result = tracker.observe(picture(t + 6000, 39150), t, 1);
            if (result.advance) { advanced = true; assertEquals(39.15, result.periodSeconds, .5); break; }
        }
        assertTrue(tracker.diagnostic(), advanced);
    }
    @Test public void randomFramesNeverAdvanceAndEventuallyStopLearning() {
        VisualSequenceTracker tracker = new VisualSequenceTracker();
        Random random = new Random(7);
        VisualSequenceTracker.Result result = null;
        for (long t = 0; t <= 140000; t += 450) {
            result = tracker.observe(noise(random), t, 1);
            assertFalse(result.advance); assertEquals(0, result.current);
        }
        assertNotNull(result); assertEquals("LEARNING_TIMEOUT", result.reason);
    }
    @Test public void staticFramesNeverLearn() {
        VisualSequenceTracker tracker = new VisualSequenceTracker();
        boolean staticSeen = false;
        for (long t = 0; t <= 35000; t += 450) {
            VisualSequenceTracker.Result result = tracker.observe(still(), t, 1);
            staticSeen |= result.reason.equals("STATIC");
            assertTrue(result.learning); assertFalse(result.advance); assertEquals(0, result.current);
        }
        assertTrue(staticSeen);
    }
    @Test public void movingCaptionOnStaticPictureIsRejected() {
        VisualSequenceTracker tracker = new VisualSequenceTracker();
        for (long t = 0; t <= 45000; t += 450) {
            double[] frame = still(), moving = wave(t, 9000);
            System.arraycopy(moving, 0, frame, 0, SIZE / 6);
            VisualSequenceTracker.Result result = tracker.observe(frame, t, 1);
            assertFalse(result.advance); assertEquals(0, result.current);
        }
        assertTrue(tracker.diagnostic().contains("lastRestart=STATIC"));
    }
    @Test public void oneRecurringShortSceneWithUnrelatedRestCannotLearn() {
        VisualSequenceTracker tracker = new VisualSequenceTracker();
        Random random = new Random(66);
        for (long t = 0; t <= 75000; t += 450) {
            double[] frame = t % 9000 < 2250 ? wave(t, 9000) : noise(random);
            VisualSequenceTracker.Result result = tracker.observe(frame, t, 1);
            assertFalse(tracker.diagnostic(), result.advance); assertTrue(result.learning);
        }
    }
    @Test public void twoSecondPauseResetsEvenAfterLearning() {
        VisualSequenceTracker tracker = new VisualSequenceTracker();
        assertFalse(feed(tracker, 0, 22050, 2).learning);
        VisualSequenceTracker.Result result = null;
        for (long t = 22500; t <= 25200; t += 450) {
            result = tracker.observe(wave(22050, 9000), t, 2); assertFalse(result.advance);
        }
        assertNotNull(result); assertTrue(result.learning); assertEquals(0, result.current);
        assertTrue(tracker.diagnostic().contains("lastRestart=STATIC"));
    }
    @Test public void partialPauseCannotCompletePreviouslyLearnedCycle() {
        VisualSequenceTracker tracker = new VisualSequenceTracker();
        boolean reset = false;
        for (long t = 0; t <= 36000; t += 450) {
            double playback = t <= 22050 ? t : t <= 23850 ? 22050 : t - 1800;
            VisualSequenceTracker.Result result = tracker.observe(wave(playback, 9000), t, 1);
            assertFalse(tracker.diagnostic(), result.advance);
            if (t >= 27000) reset |= result.learning;
        }
        assertTrue(tracker.diagnostic(), reset);
    }
    @Test public void changingPeriodCannotAdvanceUsingOldPeriod() {
        VisualSequenceTracker tracker = new VisualSequenceTracker();
        boolean reset = false;
        for (long t = 0; t <= 36000; t += 450) {
            double playback = t <= 22050 ? t : 22050 + (t - 22050) * 9000d / 12000;
            VisualSequenceTracker.Result result = tracker.observe(wave(playback, 9000), t, 1);
            assertFalse(tracker.diagnostic(), result.advance);
            if (t > 29000) reset |= result.learning;
        }
        assertTrue(tracker.diagnostic(), reset);
    }
    @Test public void gapOverTwoSecondsClearsLearnedPeriod() {
        VisualSequenceTracker tracker = new VisualSequenceTracker();
        feed(tracker, 0, 22050, 2);
        VisualSequenceTracker.Result result = tracker.observe(wave(24500, 9000), 24500, 2);
        assertEquals("OBSERVATION_GAP", result.reason); assertTrue(result.learning);
        assertEquals(0, result.periodSeconds, 0); assertEquals(0, result.current); assertFalse(result.advance);
    }
    @Test public void targetChangeOffAndExplicitResetClearCounts() {
        VisualSequenceTracker tracker = new VisualSequenceTracker();
        feed(tracker, 0, 22050, 2);
        VisualSequenceTracker.Result changed = tracker.observe(wave(22500, 9000), 22500, 3);
        assertTrue(changed.learning); assertEquals(0, changed.current); assertEquals(0, changed.periodSeconds, 0);
        VisualSequenceTracker.Result off = tracker.observe(null, 23000, 0);
        assertEquals("OFF", off.reason); assertFalse(off.learning); assertFalse(off.advance);
        tracker.reset();
        assertEquals(0, number(tracker.diagnostic(), "frames"));
        assertTrue(tracker.observe(wave(23500, 9000), 23500, 1).learning);
    }
    @Test public void invalidFeaturesAndTargetsAreRejected() {
        VisualSequenceTracker tracker = new VisualSequenceTracker();
        double[] nan = still(); nan[0] = Double.NaN;
        double[] infinite = still(); infinite[0] = Double.POSITIVE_INFINITY;
        double[] negative = still(); negative[0] = -1;
        double[] high = still(); high[0] = 256;
        for (double[] values : new double[][]{null, new double[1], nan, infinite, negative, high}) {
            assertEquals("INVALID_FEATURES", tracker.observe(values, 1000, 1).reason);
            assertEquals(0, number(tracker.diagnostic(), "frames"));
        }
        assertEquals("INVALID_TARGET", tracker.observe(still(), 1500, -1).reason);
        assertEquals("INVALID_TARGET", tracker.observe(still(), 1500, 100).reason);
    }
    @Test public void duplicateBackwardAndNegativeTimestampsAreRejected() {
        VisualSequenceTracker tracker = new VisualSequenceTracker();
        tracker.observe(still(), 1000, 1);
        assertEquals("INVALID_TIMESTAMP", tracker.observe(still(), 1000, 1).reason);
        assertEquals("INVALID_TIMESTAMP", tracker.observe(still(), 500, 1).reason);
        assertEquals("INVALID_FEATURES", tracker.observe(still(), -1, 1).reason);
    }
    @Test public void callerMayZeroInputImmediatelyAfterObservation() {
        VisualSequenceTracker tracker = new VisualSequenceTracker();
        boolean advanced = false;
        for (long t = 0; t <= 45000; t += 450) {
            double[] frame = wave(t, 9000);
            advanced |= tracker.observe(frame, t, 1).advance; Arrays.fill(frame, 0);
        }
        assertTrue(tracker.diagnostic(), advanced);
    }
    @Test public void memoryNeverExceedsThreeHundredRetainedHistoryFrames() {
        VisualSequenceTracker tracker = new VisualSequenceTracker();
        for (long t = 0; t <= 220000; t += 450) {
            VisualSequenceTracker.Result result = tracker.observe(wave(t, 9000), t, 99);
            assertFalse(result.advance);
            assertTrue(tracker.diagnostic(), number(tracker.diagnostic(), "frames") <= 300);
        }
    }
    @Test public void shortPeriodCannotMasqueradeAsLongerHarmonic() {
        VisualSequenceTracker tracker = new VisualSequenceTracker();
        boolean rejected = false;
        for (long t = 0; t <= 40000; t += 450) {
            VisualSequenceTracker.Result result = tracker.observe(wave(t, 1800), t, 1);
            assertFalse(result.advance); rejected |= result.reason.equals("SHORT_PERIOD");
        }
        assertTrue(tracker.diagnostic(), rejected);
    }
    @Test public void minimumThreeSecondPeriodIsNotRoundedBelowMinimum() {
        VisualSequenceTracker tracker = new VisualSequenceTracker();
        boolean advanced = false;
        for (long t = 0; t <= 20000; t += 375) {
            VisualSequenceTracker.Result result = tracker.observe(wave(t, 3000), t, 1);
            if (result.advance) { advanced = true; assertEquals(3, result.periodSeconds, .001); }
        }
        assertTrue(tracker.diagnostic(), advanced);
    }
    @Test public void sixtySecondUniqueSequenceCanLearnAndCountWithinMemoryBound() {
        VisualSequenceTracker tracker = new VisualSequenceTracker();
        boolean advanced = false;
        for (long t = 0; t <= 195000; t += 450) {
            // 59.85 seconds is exactly 133 captures, near the 60-second upper boundary.
            Random random = new Random((t % 59850) / 450 * 31337 + 7);
            VisualSequenceTracker.Result result = tracker.observe(noise(random), t, 1);
            if (result.advance) { advanced = true; assertEquals(59.85, result.periodSeconds, .01); break; }
        }
        assertTrue(tracker.diagnostic(), advanced);
    }
}
