package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class LongVideoTrackerTest {
    private final LongVideoTracker tracker = new LongVideoTracker();
    private boolean sample(double position, long at) { return tracker.observe("page", new Progress(position, 120), 60, at); }
    @Test public void movingLongVideoIsDueWithoutWatchingItsEnd() {
        assertFalse(sample(0, 1000)); assertFalse(sample(0.3, 1300)); assertTrue(sample(1, 2000));
    }
    @Test public void singleSampleAndUnsettledMotionCannotSkip() {
        assertFalse(sample(10, 1000)); assertFalse(sample(10.5, 1300));
    }
    @Test public void frozenZeroOrMidpointNeverSkips() {
        for (double position : new double[]{0, 30}) {
            tracker.reset(); for (int i = 0; i < 30; i++) assertFalse(sample(position, 1000 + i * 300));
        }
    }
    @Test public void earlyMotionFollowedByPauseDoesNotSkip() {
        sample(0, 1000); assertFalse(sample(0.5, 1300));
        for (int i = 0; i < 30; i++) assertFalse(sample(0.5, 1600 + i * 300));
    }
    @Test public void quantizedClockWaitsForActualUpdate() {
        for (int at = 1000; at < 3000; at += 300) assertFalse(sample(0, at));
        assertTrue(sample(2, 3100));
    }
    @Test public void consumedPageNeverRetriesAcrossTimeAndDurationChanges() {
        sample(0, 1000); assertTrue(sample(1, 2000)); tracker.consume();
        assertFalse(sample(2, 3000)); assertFalse(sample(0, 90000));
        assertFalse(tracker.observe("page", new Progress(4, 130), 60, 100000));
    }
    @Test public void dueWithoutRequestRemainsRetryableAfterFreshValidation() {
        sample(0, 1000); assertTrue(sample(1, 2000));
        assertTrue(sample(2, 3000)); // No consume: the caller never dispatched.
    }
    @Test public void newPageWithSameDurationQualifiesSeparately() {
        sample(0, 1000); sample(1, 2000); tracker.consume();
        assertFalse(tracker.observe("other", new Progress(0, 120), 60, 3000));
        assertTrue(tracker.observe("other", new Progress(1, 120), 60, 4000));
    }
    @Test public void unavailableAndShortProgressCannotSkip() {
        assertFalse(tracker.observe("page", null, 60, 1000));
        assertFalse(tracker.observe("page", new Progress(0, 59), 60, 2000));
        assertFalse(tracker.observe("page", new Progress(1, 59), 60, 3000));
    }
    @Test public void emptyIdentityCannotSkip() {
        assertFalse(tracker.observe("", new Progress(0, 120), 60, 1000));
        assertFalse(tracker.observe("", new Progress(1, 120), 60, 2000));
    }
    @Test public void durationChangeRequiresFreshSettlement() {
        sample(0, 1000); assertFalse(tracker.observe("page", new Progress(1, 130), 60, 2000));
        assertTrue(tracker.observe("page", new Progress(2, 130), 60, 3000));
    }
    @Test public void gapAndBackwardClockDiscardEvidence() {
        sample(0, 1000); assertFalse(sample(2, 3000)); assertFalse(sample(3, 2500));
    }
    @Test public void backwardSeekRestartsSettlement() {
        sample(20, 1000); assertFalse(sample(0, 2000)); assertFalse(sample(0.5, 2300));
    }
    @Test public void forwardSeekCannotBecomeADueSignal() {
        sample(0, 1000); assertFalse(sample(80, 2000));
    }
    @Test public void changedThresholdStartsNewQualification() {
        sample(0, 1000); assertFalse(tracker.observe("page", new Progress(1, 120), 100, 2000));
    }
    @Test public void resetDropsCandidateAndConsumption() {
        sample(0, 1000); tracker.reset(); assertFalse(sample(1, 2000)); assertTrue(sample(2, 3000));
    }
    @Test public void stableDifferentPageConfirmsLengthSkip() {
        AdvanceGate gate = new AdvanceGate(); gate.begin("long-a", -1, 1000);
        assertEquals(AdvanceGate.State.WAITING, gate.inspectStableRecognizedPage("long-b", 2200));
        assertEquals(AdvanceGate.State.CONFIRMED, gate.inspectStableRecognizedPage("long-b", 2500));
    }
    @Test public void samePageOrPageEventAloneNeverConfirmsLengthSkip() {
        AdvanceGate gate = new AdvanceGate(); gate.begin("long-a", -1, 1000); gate.pageChanged();
        assertEquals(AdvanceGate.State.WAITING, gate.inspectStableRecognizedPage("long-a", 2500));
        assertEquals(AdvanceGate.State.FAILED, gate.inspectStableRecognizedPage("long-a", 5500));
    }
    @Test public void unstableIdentityOrWindowCannotConfirm() {
        AdvanceGate gate = new AdvanceGate(); gate.begin("a", -1, 1000);
        assertEquals(AdvanceGate.State.WAITING, gate.inspectStableRecognizedPage("b", 2200));
        assertEquals(AdvanceGate.State.WAITING, gate.inspectStableRecognizedPage("", 2500));
        assertEquals(AdvanceGate.State.WAITING, gate.inspectStableRecognizedPage("b", 2800));
        assertEquals(AdvanceGate.State.WAITING, gate.inspectStableRecognizedPage("c", 3100));
    }
    @Test public void tooEarlyAndExactTimeoutCannotConfirm() {
        AdvanceGate gate = new AdvanceGate(); gate.begin("a", -1, 1000);
        assertEquals(AdvanceGate.State.WAITING, gate.inspectStableRecognizedPage("b", 1100));
        assertEquals(AdvanceGate.State.WAITING, gate.inspectStableRecognizedPage("b", 1400));
        assertEquals(AdvanceGate.State.FAILED, gate.inspectStableRecognizedPage("b", 5500));
    }
}
