package com.fullmetalsonic.shortsloop.core;

import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import static org.junit.Assert.*;

public class VisualLoopTrackerTest {
    private static final int SIZE = VisualLoopTracker.FEATURE_COUNT;
    private static double[] wave(double millis, double period) {
        double phase = millis / period * Math.PI * 2;
        double[] result = new double[SIZE];
        for (int i = 0; i < SIZE; i++)
            result[i] = 127 + 75 * Math.sin(phase + (i % 17) * .37)
                    + 25 * Math.cos(phase * 2 + (i % 13) * .23);
        return result;
    }
    private static double[] still() { double[] values = new double[SIZE]; Arrays.fill(values, 100); return values; }
    private static double[] noise(Random random) {
        double[] values = new double[SIZE];
        for (int i = 0; i < SIZE; i++) values[i] = random.nextInt(256);
        return values;
    }
    private static VisualLoopTracker.Result feed(VisualLoopTracker tracker, long start, long end, int target) {
        VisualLoopTracker.Result result = null;
        for (long t = start; t <= end; t += 450) result = tracker.observe(wave(t, 9000), t, target);
        return result;
    }
    private static int number(String diagnostic, String key) {
        Matcher match = Pattern.compile(key + "=(\\d+)").matcher(diagnostic);
        assertTrue(diagnostic, match.find()); return Integer.parseInt(match.group(1));
    }
    private static double decimal(String diagnostic, String key) {
        Matcher match = Pattern.compile("(?:^| )" + key + "=(-?\\d+(?:\\.\\d+)?)").matcher(diagnostic);
        assertTrue(diagnostic, match.find()); return Double.parseDouble(match.group(1));
    }
    @Test public void requiresLearningThenOneWholeCycleForTargetOne() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        boolean learned = false, advanced = false;
        for (long t = 0; t <= 40000; t += 450) {
            VisualLoopTracker.Result result = tracker.observe(wave(t, 9000), t, 1);
            if (!result.learning) {
                learned = true;
                assertTrue("learning must not count retroactively", t >= 18000);
                assertEquals(1, result.current);
            }
            if (result.advance) {
                assertTrue("must observe an additional whole cycle", t >= 27000);
                assertTrue(learned); assertEquals(9, result.periodSeconds, .65);
                advanced = true; break;
            }
        }
        assertTrue(tracker.diagnostic(), advanced);
    }
    @Test public void targetTwoShowsOneThenTwoAndAdvancesOnlyOnce() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        boolean one = false, two = false;
        int advances = 0;
        for (long t = 0; t <= 70000; t += 450) {
            VisualLoopTracker.Result result = tracker.observe(wave(t, 9000), t, 2);
            one |= result.current == 1; two |= result.current == 2;
            if (result.advance) { advances++; assertTrue(t >= 36000); }
        }
        assertTrue(tracker.diagnostic(), one); assertTrue(tracker.diagnostic(), two);
        assertEquals(tracker.diagnostic(), 1, advances);
    }
    @Test public void jitteredCaptureTimesAndMildPixelNoiseCanMatch() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        Random random = new Random(12);
        long t = 0;
        int[] intervals = {370, 510, 430, 470, 390, 530};
        boolean advanced = false;
        for (int n = 0; t < 55000; n++) {
            double[] frame = wave(t + 3333, 9700);
            for (int i = 0; i < SIZE; i++) frame[i] += random.nextDouble() * 2 - 1;
            VisualLoopTracker.Result result = tracker.observe(frame, t, 1);
            if (result.advance) { advanced = true; assertEquals(9.7, result.periodSeconds, .7); break; }
            t += intervals[n % intervals.length];
        }
        assertTrue(tracker.diagnostic(), advanced);
    }
    @Test public void noRepeatNoiseNeverAdvancesAndTimesOut() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        Random random = new Random(3);
        VisualLoopTracker.Result result = null;
        for (long t = 0; t <= 140000; t += 450) {
            result = tracker.observe(noise(random), t, 1);
            assertFalse(result.advance);
        }
        assertNotNull(result); assertEquals("LEARNING_TIMEOUT", result.reason); assertEquals(0, result.current);
    }
    @Test public void stationaryScreenNeverLearns() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        boolean detectedStatic = false;
        for (long t = 0; t <= 30000; t += 450) {
            VisualLoopTracker.Result result = tracker.observe(still(), t, 1);
            detectedStatic |= result.reason.equals("STATIC");
            assertTrue(result.learning); assertFalse(result.advance); assertEquals(0, result.current);
        }
        assertTrue(detectedStatic);
    }
    @Test public void movingCaptionOnMostlyStaticPictureIsRejected() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        boolean detectedStatic = false;
        for (long t = 0; t <= 35000; t += 450) {
            double[] frame = still(), motion = wave(t, 9000);
            System.arraycopy(motion, 0, frame, 0, SIZE / 6);
            VisualLoopTracker.Result result = tracker.observe(frame, t, 1);
            detectedStatic |= result.reason.equals("STATIC");
            assertFalse(result.advance); assertEquals(0, result.current);
        }
        assertTrue(tracker.diagnostic(), detectedStatic);
    }
    @Test public void twoSecondPauseClearsLearnedCycle() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        VisualLoopTracker.Result before = feed(tracker, 0, 22050, 2);
        assertFalse(tracker.diagnostic(), before.learning);
        VisualLoopTracker.Result result = null;
        for (long t = 22500; t <= 25200; t += 450) {
            result = tracker.observe(wave(22050, 9000), t, 2);
            assertFalse(result.advance);
        }
        assertNotNull(result); assertTrue(result.learning); assertEquals(0, result.current);
    }
    @Test public void shortPartialPauseCannotCountTheOldPeriod() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        boolean reset = false;
        for (long t = 0; t <= 35000; t += 450) {
            double playback = t <= 22050 ? t : t <= 23850 ? 22050 : t - 1800;
            VisualLoopTracker.Result result = tracker.observe(wave(playback, 9000), t, 1);
            if (t > 23850) reset |= result.learning;
            assertFalse("partial pause: " + tracker.diagnostic(), result.advance);
        }
        assertTrue(reset);
    }
    @Test public void periodChangeRequiresLearningAgain() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        boolean reset = false;
        for (long t = 0; t <= 33000; t += 450) {
            double position = t <= 22050 ? t : 22050 + (t - 22050) * 9000d / 12000;
            VisualLoopTracker.Result result = tracker.observe(wave(position, 9000), t, 1);
            if (t > 27000) reset |= result.learning;
            assertFalse(tracker.diagnostic(), result.advance);
        }
        assertTrue(tracker.diagnostic(), reset);
    }
    @Test public void matchingShortAnchorWithRandomRestIsNotAWholeCycle() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        Random random = new Random(66);
        boolean learned = false;
        for (long t = 0; t <= 80000; t += 450) {
            double[] frame = t % 9000 <= 2250 ? wave(t, 9000) : noise(random);
            VisualLoopTracker.Result result = tracker.observe(frame, t, 1);
            learned |= !result.learning;
            assertFalse(tracker.diagnostic(), result.advance);
        }
        assertFalse("whole-cycle verification required", learned);
    }
    @Test public void gapOverTwoSecondsResetsWithoutCounting() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        feed(tracker, 0, 22050, 2);
        VisualLoopTracker.Result result = tracker.observe(wave(24500, 9000), 24500, 2);
        assertEquals("OBSERVATION_GAP", result.reason); assertTrue(result.learning);
        assertEquals(0, result.current); assertEquals(0, result.periodSeconds, 0); assertFalse(result.advance);
    }
    @Test public void changingTargetClearsLearningAndCount() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        feed(tracker, 0, 22050, 2);
        VisualLoopTracker.Result result = tracker.observe(wave(22500, 9000), 22500, 3);
        assertTrue(result.learning); assertEquals(0, result.current); assertEquals(0, result.periodSeconds, 0);
    }
    @Test public void zeroTargetDoesNotCaptureOrAdvance() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        feed(tracker, 0, 22050, 2);
        VisualLoopTracker.Result result = tracker.observe(null, 22500, 0);
        assertEquals("OFF", result.reason); assertEquals(0, result.current);
        assertFalse(result.learning); assertFalse(result.advance); assertEquals(0, number(tracker.diagnostic(), "frames"));
    }
    @Test public void malformedFeaturesReset() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        feed(tracker, 0, 22050, 2);
        double[][] invalid = {null, new double[1], new double[SIZE]};
        invalid[2][5] = Double.NaN;
        for (double[] frame : invalid) {
            VisualLoopTracker.Result result = tracker.observe(frame, 22500, 2);
            assertEquals("INVALID_FEATURES", result.reason); assertEquals(0, result.current); assertFalse(result.advance);
        }
        double[] negative = still(); negative[0] = -1;
        assertEquals("INVALID_FEATURES", tracker.observe(negative, 23000, 2).reason);
        double[] tooLarge = still(); tooLarge[0] = 256;
        assertEquals("INVALID_FEATURES", tracker.observe(tooLarge, 23500, 2).reason);
    }
    @Test public void timestampMustIncreaseAndBeNonNegative() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        tracker.observe(still(), 1000, 1);
        assertEquals("INVALID_TIMESTAMP", tracker.observe(still(), 1000, 1).reason);
        assertEquals("INVALID_TIMESTAMP", tracker.observe(still(), 500, 1).reason);
        assertEquals("INVALID_FEATURES", tracker.observe(still(), -1, 1).reason);
    }
    @Test public void invalidTargetsAreNeverSanitizedToAnActiveCount() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        assertEquals("INVALID_TARGET", tracker.observe(still(), 0, -1).reason);
        assertEquals("INVALID_TARGET", tracker.observe(still(), 0, 100).reason);
    }
    @Test public void featureArraysAreCopiedNotRetainedFromCaller() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        boolean advanced = false;
        for (long t = 0; t <= 40000; t += 450) {
            double[] frame = wave(t, 9000);
            advanced |= tracker.observe(frame, t, 1).advance;
            Arrays.fill(frame, 0);
        }
        assertTrue(tracker.diagnostic(), advanced);
    }
    @Test public void resetRearmsOnlyAfterNewLearning() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        feed(tracker, 0, 40050, 1);
        assertEquals("LATCHED", tracker.observe(null, 40500, 1).reason);
        tracker.reset();
        VisualLoopTracker.Result result = tracker.observe(wave(41000, 9000), 41000, 1);
        assertTrue(result.learning); assertFalse(result.advance); assertEquals(0, result.current);
    }
    @Test public void retainedFrameStorageStaysBounded() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        for (long t = 0; t <= 350000; t += 450) {
            VisualLoopTracker.Result result = tracker.observe(wave(t, 9000), t, 99);
            assertFalse(result.advance);
            String state = tracker.diagnostic();
            int total = number(state, "frames") + number(state, "anchor") + number(state, "template");
            assertTrue(state, total <= 317);
        }
    }
    @Test public void veryShortPeriodicMotionIsNotAcceptedAsAVideo() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        for (long t = 0; t <= 30000; t += 450) assertFalse(tracker.observe(wave(t, 1800), t, 1).advance);
    }
    @Test public void minimumThreeSecondCycleCanBeLearned() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        boolean advanced = false;
        for (long t = 0; t <= 16000; t += 375) advanced |= tracker.observe(wave(t, 3000), t, 1).advance;
        assertTrue(tracker.diagnostic(), advanced);
    }
    @Test public void sixtySecondSequenceFitsMemoryAndLearningDeadline() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        boolean advanced = false;
        for (long t = 0; t <= 190000; t += 400) {
            Random random = new Random((t % 60000) / 400 * 31337 + 7);
            VisualLoopTracker.Result result = tracker.observe(noise(random), t, 1);
            if (result.advance) { advanced = true; assertEquals(60, result.periodSeconds, .001); break; }
        }
        assertTrue(tracker.diagnostic(), advanced);
    }
    @Test public void staticRestartCauseSurvivesSubsequentMovingObservations() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        feed(tracker, 0, 22050, 2);
        for (long t = 22500; t <= 24300; t += 450) tracker.observe(wave(22050, 9000), t, 2);
        String stopped = tracker.diagnostic();
        assertTrue(stopped, stopped.contains("lastRestart=STATIC"));
        assertEquals(9, decimal(stopped, "restartPeriod"), .65);
        assertEquals(24300, number(stopped, "restartAt"));
        assertEquals(2250, number(stopped, "restartIdleMs"));
        assertEquals(0, decimal(stopped, "restartDelta"), 0);
        assertEquals(0, decimal(stopped, "restartMoving"), 0);
        int restarts = number(stopped, "restarts");
        tracker.observe(wave(24750, 9000), 24750, 2);
        tracker.observe(wave(25200, 9000), 25200, 2);
        String resumed = tracker.diagnostic();
        assertTrue(resumed, resumed.contains("visual=LEARNING_ANCHOR"));
        assertTrue(resumed, resumed.contains("lastRestart=STATIC"));
        assertEquals(restarts, number(resumed, "restarts"));
        assertEquals(24300, number(resumed, "restartAt"));
    }
    @Test public void restartCounterIncludesCallerResetAndTargetChangesWithoutDoubleCounting() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        assertEquals(0, number(tracker.diagnostic(), "restarts"));
        tracker.observe(still(), 0, 1);
        assertEquals(1, number(tracker.diagnostic(), "restarts"));
        assertTrue(tracker.diagnostic().contains("lastRestart=TARGET_CHANGED"));
        tracker.observe(still(), 450, 1);
        assertEquals(1, number(tracker.diagnostic(), "restarts"));
        tracker.reset();
        assertEquals(2, number(tracker.diagnostic(), "restarts"));
        assertTrue(tracker.diagnostic().contains("lastRestart=EXPLICIT_RESET"));
        tracker.observe(still(), 900, 2);
        assertEquals(3, number(tracker.diagnostic(), "restarts"));
        tracker.observe(null, 1350, 0);
        assertEquals(4, number(tracker.diagnostic(), "restarts"));
        assertTrue(tracker.diagnostic().contains("lastRestart=OFF"));
    }
    @Test public void scalarMovementDiagnosticsDescribeStoredSampleAndMotionReference() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        double[] first = still();
        tracker.observe(first, 0, 1);
        assertEquals(-1, decimal(tracker.diagnostic(), "sampleDelta"), 0);
        double[] second = still(); Arrays.fill(second, 106);
        tracker.observe(second, 450, 1);
        assertEquals(6, decimal(tracker.diagnostic(), "sampleDelta"), 0);
        assertEquals(1, decimal(tracker.diagnostic(), "sampleMoving"), 0);
        assertEquals(6, decimal(tracker.diagnostic(), "motionDelta"), 0);
        assertEquals(1, decimal(tracker.diagnostic(), "motionMoving"), 0);
        double[] third = second.clone(); third[0] = third[1] = third[2] = 112;
        tracker.observe(third, 900, 1);
        assertEquals(6 / 384d, decimal(tracker.diagnostic(), "sampleDelta"), .0005);
        assertEquals(1 / 384d, decimal(tracker.diagnostic(), "sampleMoving"), .0005);
        // One changed pixel does not update the broad-motion reference.
        tracker.observe(second, 1350, 1);
        assertEquals(6 / 384d, decimal(tracker.diagnostic(), "sampleDelta"), .0005);
        assertEquals(0, decimal(tracker.diagnostic(), "motionDelta"), 0);
        assertEquals(0, decimal(tracker.diagnostic(), "motionMoving"), 0);
        Arrays.fill(first, 0); Arrays.fill(second, 0); Arrays.fill(third, 0);
        assertEquals(0, decimal(tracker.diagnostic(), "motionDelta"), 0);
    }
    @Test public void observationGapRestartRemainsVisibleAfterNewSamples() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        tracker.observe(wave(0, 9000), 0, 1);
        tracker.observe(wave(450, 9000), 450, 1);
        tracker.observe(wave(3000, 9000), 3000, 1);
        tracker.observe(wave(3450, 9000), 3450, 1);
        String state = tracker.diagnostic();
        assertTrue(state, state.contains("lastRestart=OBSERVATION_GAP"));
        assertEquals(2, number(state, "restarts"));
        assertEquals(3000, number(state, "restartAt"));
        assertEquals(-1, decimal(state, "restartDelta"), 0);
    }
    @Test public void cycleMismatchDiagnosticIsSeparateFromIntervalMismatch() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        Random random = new Random(66);
        boolean mismatch = false;
        for (long t = 0; t <= 80000; t += 450) {
            double[] frame = t % 9000 <= 2250 ? wave(t, 9000) : noise(random);
            VisualLoopTracker.Result result = tracker.observe(frame, t, 1);
            assertFalse(result.advance);
            if (tracker.diagnostic().contains("restartDetail=CYCLE_MISMATCH")) {
                mismatch = true;
                assertTrue(tracker.diagnostic().contains("lastRestart=LOW_CONFIDENCE"));
                assertTrue(decimal(tracker.diagnostic(), "restartPeriod") >= 3);
                break;
            }
        }
        assertTrue(tracker.diagnostic(), mismatch);
    }
    @Test public void latchedTimeoutIsRecordedOnlyOnce() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        Random random = new Random(3);
        int restartsAtTimeout = -1;
        for (long t = 0; t <= 140000; t += 450) {
            VisualLoopTracker.Result result = tracker.observe(noise(random), t, 1);
            assertFalse(result.advance);
            if (result.reason.equals("LEARNING_TIMEOUT")) {
                if (restartsAtTimeout < 0) restartsAtTimeout = number(tracker.diagnostic(), "restarts");
                assertEquals(restartsAtTimeout, number(tracker.diagnostic(), "restarts"));
                assertTrue(tracker.diagnostic().contains("lastRestart=LEARNING_TIMEOUT"));
            }
        }
        assertTrue(restartsAtTimeout > 0);
    }
    @Test public void latchedShortPeriodIsRecordedOnlyOnce() {
        VisualLoopTracker tracker = new VisualLoopTracker();
        int restartsAtRejection = -1;
        for (long t = 0; t <= 30000; t += 450) {
            VisualLoopTracker.Result result = tracker.observe(wave(t, 1800), t, 1);
            assertFalse(result.advance);
            if (result.reason.equals("SHORT_PERIOD")) {
                if (restartsAtRejection < 0) restartsAtRejection = number(tracker.diagnostic(), "restarts");
                assertEquals(restartsAtRejection, number(tracker.diagnostic(), "restarts"));
                assertTrue(tracker.diagnostic().contains("lastRestart=SHORT_PERIOD"));
            }
        }
        assertTrue(restartsAtRejection > 0);
    }
}
