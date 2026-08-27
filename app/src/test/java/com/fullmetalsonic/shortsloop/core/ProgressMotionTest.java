package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class ProgressMotionTest {
    @Test public void observedTwoSecondUpdateIn309msIsAllowed() {
        ProgressMotion motion = new ProgressMotion(); motion.reset(14, 1000);
        assertTrue(motion.accept(16, 1309));
    }
    @Test public void repeatedFastJumpsCannotExploitPerSampleTolerance() {
        ProgressMotion motion = new ProgressMotion(); motion.reset(0, 1000);
        assertTrue(motion.accept(2, 1300)); assertFalse(motion.accept(4, 1600));
    }
    @Test public void largeSeekStillRejected() {
        ProgressMotion motion = new ProgressMotion(); motion.reset(5, 1000);
        assertFalse(motion.accept(30, 1309));
    }
    @Test public void longPauseDoesNotAccumulateUnlimitedSeekAllowance() {
        ProgressMotion motion = new ProgressMotion(); motion.reset(5, 1000);
        for (int i = 2; i <= 60; i++) assertTrue(motion.accept(5, i * 1000L));
        assertFalse(motion.accept(30, 60300));
    }
    @Test public void twoTimesQuantizedProgressSupported() {
        ProgressMotion motion = new ProgressMotion(); motion.reset(0, 1000);
        for (int i = 1; i < 100; i++) assertTrue(motion.accept(Math.floor(i * .6), 1000 + i * 300L));
    }
    @Test public void resetStartsNewVideoWindow() {
        ProgressMotion motion = new ProgressMotion(); motion.reset(30, 1000);
        motion.reset(0, 1300); assertTrue(motion.accept(1, 2300));
    }
}
