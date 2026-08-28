package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class LiveSkipTrackerTest {
    @Test public void zeroMeansImmediateAfterBriefSafetySettlementNotOff() {
        LiveSkipTracker t = new LiveSkipTracker();
        assertTrue(t.observe("live-a-window-bounds", 0, 0).settling());
        assertFalse(t.observe("live-a-window-bounds", 0, 249).due());
        assertTrue(t.observe("live-a-window-bounds", 0, 250).due());
    }
    @Test public void exactOneSecondDeadline() {
        LiveSkipTracker t = new LiveSkipTracker();
        t.observe("a", 1, 100);
        assertEquals(1, t.observe("a", 1, 1099).remainingSeconds());
        assertTrue(t.observe("a", 1, 1100).due());
    }
    @Test public void fiveSecondsIncludesSettling() {
        LiveSkipTracker t = new LiveSkipTracker();
        for (int n = 0; n < 5; n++) assertFalse(t.observe("a", 5, n * 1000).due());
        assertEquals(1, t.observe("a", 5, 4999).remainingSeconds());
        assertTrue(t.observe("a", 5, 5000).due());
    }
    @Test public void sixtySecondUpperBound() {
        LiveSkipTracker t = new LiveSkipTracker();
        for (int n = 0; n < 60; n++) assertFalse(t.observe("a", 60, n * 1000).due());
        assertTrue(t.observe("a", 60, 60000).due());
    }
    @Test public void noSecondAttemptForConsumedPageEvenAfterGapOrSettingChange() {
        LiveSkipTracker t = new LiveSkipTracker();
        t.observe("a", 0, 0); assertTrue(t.observe("a", 0, 300).due());
        assertFalse(t.observe("a", 0, 600).due());
        assertFalse(t.observe("a", 5, 20000).due());
        assertFalse(t.active());
    }
    @Test public void nextPageGetsOwnDelay() {
        LiveSkipTracker t = new LiveSkipTracker();
        t.observe("a", 0, 0); t.observe("a", 0, 300);
        assertFalse(t.observe("b", 0, 400).due());
        assertTrue(t.observe("b", 0, 650).due());
    }
    @Test public void geometryOrWindowChangeRestartsDelay() {
        LiveSkipTracker t = new LiveSkipTracker();
        t.observe("a|window1|geometry1", 1, 0);
        assertFalse(t.observe("a|window2|geometry2", 1, 900).due());
        assertFalse(t.observe("a|window2|geometry2", 1, 1899).due());
        assertTrue(t.observe("a|window2|geometry2", 1, 1900).due());
    }
    @Test public void observationGapRestartsInsteadOfImmediateSwipe() {
        LiveSkipTracker t = new LiveSkipTracker();
        t.observe("a", 1, 0);
        assertFalse(t.observe("a", 1, 1201).due());
        assertTrue(t.observe("a", 1, 2201).due());
    }
    @Test public void settingChangeRestartsDelay() {
        LiveSkipTracker t = new LiveSkipTracker();
        t.observe("a", 5, 0); t.observe("a", 5, 1000);
        assertFalse(t.observe("a", 0, 1100).due());
        assertTrue(t.observe("a", 0, 1350).due());
    }
    @Test public void unsafePageResetsCountdown() {
        for (String bad : new String[]{null, "", "  "}) {
            LiveSkipTracker t = new LiveSkipTracker();
            t.observe("a", 1, 0);
            assertFalse(t.observe(bad, 1, 500).active());
            assertFalse(t.observe("a", 1, 1000).due());
        }
    }
    @Test public void clockRegressionAndDuplicateCannotCauseEarlyRequest() {
        LiveSkipTracker t = new LiveSkipTracker();
        t.observe("a", 0, 1000);
        assertFalse(t.observe("a", 0, 900).due());
        assertFalse(t.observe("a", 0, 900).due());
        assertTrue(t.observe("a", 0, 1150).due());
    }
    @Test public void invalidTimeResetsAndVeryLargeTimeDoesNotOverflow() {
        LiveSkipTracker t = new LiveSkipTracker();
        assertFalse(t.observe("a", 0, -1).active());
        t.observe("a", 0, Long.MAX_VALUE - 300);
        assertTrue(t.observe("a", 0, Long.MAX_VALUE).due());
    }
    @Test public void explicitOffOnCanRearm() {
        LiveSkipTracker t = new LiveSkipTracker();
        t.observe("a", 0, 0); t.observe("a", 0, 300); t.reset();
        assertFalse(t.observe("a", 0, 400).due());
        assertTrue(t.observe("a", 0, 700).due());
    }
}
