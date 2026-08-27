package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class ClocklessTimeoutTrackerTest {
    private static final String PAGE = "synthetic-page-window-geometry";
    private static ClocklessTimeoutTracker.Result feed(ClocklessTimeoutTracker tracker, String page,
                                                      int seconds, long start, long end) {
        ClocklessTimeoutTracker.Result result = null;
        for (long t = start; t <= end; t += 1000) result = tracker.observe(page, seconds, t);
        return result;
    }
    @Test public void defaultTenSecondsIncludesQualificationAndFiresAtExactDeadline() {
        assertEquals(10, ClocklessTimeoutPolicy.DEFAULT_SECONDS);
        ClocklessTimeoutTracker tracker = new ClocklessTimeoutTracker();
        for (long now = 100; now <= 9100; now += 1000) assertFalse(tracker.observe(PAGE, 10, now).due());
        ClocklessTimeoutTracker.Result before = tracker.observe(PAGE, 10, 10099);
        assertFalse(before.due()); assertEquals(1, before.remainingSeconds());
        ClocklessTimeoutTracker.Result due = tracker.observe(PAGE, 10, 10100);
        assertTrue(due.due()); assertFalse(due.active()); assertFalse(due.qualifying());
        assertEquals(0, due.remainingSeconds()); assertFalse(tracker.active());
    }
    @Test public void fifteenSecondsIncludesQualificationAndFiresAtExactDeadline() {
        ClocklessTimeoutTracker tracker = new ClocklessTimeoutTracker();
        for (long now = 100; now <= 14100; now += 1000) assertFalse(tracker.observe(PAGE, 15, now).due());
        ClocklessTimeoutTracker.Result before = tracker.observe(PAGE, 15, 15099);
        assertFalse(before.due()); assertEquals(1, before.remainingSeconds());
        ClocklessTimeoutTracker.Result due = tracker.observe(PAGE, 15, 15100);
        assertTrue(due.due()); assertFalse(due.active()); assertFalse(due.qualifying());
        assertEquals(0, due.remainingSeconds()); assertFalse(tracker.active());
    }
    @Test public void continuousPageQualifiesAtTwoSecondsNotBefore() {
        ClocklessTimeoutTracker tracker = new ClocklessTimeoutTracker();
        assertTrue(tracker.observe(PAGE, 15, 0).qualifying());
        assertTrue(tracker.observe(PAGE, 15, 1000).qualifying());
        assertTrue(tracker.observe(PAGE, 15, 1999).qualifying());
        ClocklessTimeoutTracker.Result qualified = tracker.observe(PAGE, 15, 2000);
        assertFalse(qualified.qualifying()); assertTrue(qualified.active()); assertFalse(qualified.due());
        assertEquals(13, qualified.remainingSeconds());
    }
    @Test public void remainingSecondsRoundUpWithoutUsingAnIndependentClock() {
        ClocklessTimeoutTracker tracker = new ClocklessTimeoutTracker();
        assertEquals(15, tracker.observe(PAGE, 15, 0).remainingSeconds());
        assertEquals(15, tracker.observe(PAGE, 15, 999).remainingSeconds());
        assertEquals(14, tracker.observe(PAGE, 15, 1000).remainingSeconds());
        assertEquals(14, tracker.remainingSeconds());
    }
    @Test public void exactFifteenHundredMillisecondGapsRemainContinuous() {
        ClocklessTimeoutTracker tracker = new ClocklessTimeoutTracker();
        tracker.observe(PAGE, 15, 0);
        for (long now = 1500; now < 15000; now += 1500) assertFalse(tracker.observe(PAGE, 15, now).due());
        assertTrue(tracker.observe(PAGE, 15, 15000).due());
    }
    @Test public void LongerGapRestartsFromCurrentObservationInsteadOfCountingMissedTime() {
        ClocklessTimeoutTracker tracker = new ClocklessTimeoutTracker();
        feed(tracker, PAGE, 15, 0, 13000);
        ClocklessTimeoutTracker.Result reset = tracker.observe(PAGE, 15, 14501);
        assertTrue(reset.active()); assertTrue(reset.qualifying()); assertEquals(15, reset.remainingSeconds());
        assertFalse(reset.due());
        for (long now = 15501; now < 29501; now += 1000) assertFalse(tracker.observe(PAGE, 15, now).due());
        assertTrue(tracker.observe(PAGE, 15, 29501).due());
    }
    @Test public void backwardTimeStartsFreshQualification() {
        ClocklessTimeoutTracker tracker = new ClocklessTimeoutTracker();
        feed(tracker, PAGE, 15, 10000, 15000);
        ClocklessTimeoutTracker.Result reset = tracker.observe(PAGE, 15, 14000);
        assertTrue(reset.qualifying()); assertEquals(15, reset.remainingSeconds()); assertFalse(reset.due());
        assertTrue(tracker.diagnostic().contains("CLOCK_RESET"));
    }
    @Test public void negativeTimestampCannotStartOrCompleteTimer() {
        ClocklessTimeoutTracker tracker = new ClocklessTimeoutTracker();
        tracker.observe(PAGE, 15, 0);
        ClocklessTimeoutTracker.Result invalid = tracker.observe(PAGE, 15, -1);
        assertFalse(invalid.active()); assertFalse(invalid.due()); assertEquals(0, invalid.remainingSeconds());
        assertTrue(tracker.observe(PAGE, 15, 1000).qualifying());
    }
    @Test public void duplicateTimestampDoesNotAdvanceOrRestartTime() {
        ClocklessTimeoutTracker tracker = new ClocklessTimeoutTracker();
        feed(tracker, PAGE, 15, 0, 3000);
        ClocklessTimeoutTracker.Result repeated = tracker.observe(PAGE, 15, 3000);
        assertFalse(repeated.qualifying()); assertEquals(12, repeated.remainingSeconds()); assertFalse(repeated.due());
    }
    @Test public void unsafeMissingPageResetsEverything() {
        for (String unsafe : new String[]{null, "", " \t\n"}) {
            ClocklessTimeoutTracker tracker = new ClocklessTimeoutTracker();
            feed(tracker, PAGE, 15, 0, 10000);
            ClocklessTimeoutTracker.Result result = tracker.observe(unsafe, 15, 11000);
            assertFalse(result.active()); assertFalse(result.qualifying()); assertFalse(result.due());
            assertEquals(0, result.remainingSeconds());
            assertEquals(15, tracker.observe(PAGE, 15, 12000).remainingSeconds());
        }
    }
    @Test public void pageOrGeometryChangeStartsItsOwnDeadline() {
        ClocklessTimeoutTracker tracker = new ClocklessTimeoutTracker();
        feed(tracker, PAGE, 15, 0, 10000);
        ClocklessTimeoutTracker.Result changed = tracker.observe(PAGE + "-new-geometry", 15, 11000);
        assertTrue(changed.qualifying()); assertEquals(15, changed.remainingSeconds());
        assertFalse(changed.due());
        assertTrue(feed(tracker, PAGE + "-new-geometry", 15, 12000, 26000).due());
    }
    @Test public void changedSettingBeforeDeadlineRestartsFromItsChange() {
        ClocklessTimeoutTracker tracker = new ClocklessTimeoutTracker();
        feed(tracker, PAGE, 15, 0, 10000);
        ClocklessTimeoutTracker.Result changed = tracker.observe(PAGE, 5, 11000);
        assertTrue(changed.qualifying()); assertEquals(5, changed.remainingSeconds()); assertFalse(changed.due());
        assertTrue(feed(tracker, PAGE, 5, 12000, 16000).due());
    }
    @Test public void dueIsOnePulseAndConsumedPageIgnoresGapClockAndSettingChanges() {
        ClocklessTimeoutTracker tracker = new ClocklessTimeoutTracker();
        assertTrue(feed(tracker, PAGE, 15, 0, 15000).due());
        for (long now : new long[]{15000, 16000, 60000, 1000, -1}) {
            ClocklessTimeoutTracker.Result result = tracker.observe(PAGE, 5, now);
            assertFalse(result.due()); assertFalse(result.active()); assertEquals(0, result.remainingSeconds());
        }
        assertTrue(tracker.observe("next-safe-page", 15, 61000).qualifying());
    }
    @Test public void explicitResetAllowsFreshSessionForSamePage() {
        ClocklessTimeoutTracker tracker = new ClocklessTimeoutTracker();
        assertTrue(feed(tracker, PAGE, 5, 0, 5000).due());
        tracker.reset(); assertFalse(tracker.active()); assertEquals(0, tracker.remainingSeconds());
        assertTrue(tracker.observe(PAGE, 5, 6000).qualifying());
        assertTrue(feed(tracker, PAGE, 5, 7000, 11000).due());
    }
    @Test public void minimumAndMaximumDurationsRespectBounds() {
        for (int seconds : new int[]{5, 60}) {
            ClocklessTimeoutTracker tracker = new ClocklessTimeoutTracker();
            for (long now = 0; now < seconds * 1000L; now += 1000)
                assertFalse(tracker.observe(PAGE, seconds, now).due());
            assertTrue(tracker.observe(PAGE, seconds, seconds * 1000L).due());
        }
    }
    @Test public void invalidStoredDurationUsesPolicyDefault() {
        ClocklessTimeoutTracker tracker = new ClocklessTimeoutTracker();
        assertEquals(10, tracker.observe(PAGE, 0, 0).remainingSeconds());
        assertTrue(feed(tracker, PAGE, 0, 1000, 10000).due());
    }
    @Test public void largeTimestampsDoNotOverflowDeadlineArithmetic() {
        ClocklessTimeoutTracker tracker = new ClocklessTimeoutTracker();
        long start = Long.MAX_VALUE - 1000;
        tracker.observe(PAGE, 5, start);
        ClocklessTimeoutTracker.Result result = tracker.observe(PAGE, 5, Long.MAX_VALUE);
        assertFalse(result.due()); assertTrue(result.qualifying()); assertEquals(4, result.remainingSeconds());
    }
    @Test public void diagnosticNeverContainsPageIdentity() {
        ClocklessTimeoutTracker tracker = new ClocklessTimeoutTracker();
        tracker.observe("synthetic-private-key-not-for-logs", 15, 0);
        assertFalse(tracker.diagnostic().contains("synthetic-private-key-not-for-logs"));
        assertTrue(tracker.diagnostic().contains("remaining=15"));
    }
}
