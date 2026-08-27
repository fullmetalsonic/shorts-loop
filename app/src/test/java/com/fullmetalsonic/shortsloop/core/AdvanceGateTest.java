package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class AdvanceGateTest {
    @org.junit.Test public void interruptingPendingAdRequiresFailureNotIdle() {
        AdvanceGate gate = new AdvanceGate();
        gate.begin("instagram-ad", -1, 1000);
        assertEquals(AdvanceGate.State.FAILED, gate.interrupt());
        assertFalse(gate.pending());
        assertEquals(AdvanceGate.State.IDLE, gate.inspectRecognizedPage("normal", 2400));
    }
    @org.junit.Test public void interruptingPendingOrdinaryPageAlsoFailsClosed() {
        AdvanceGate gate = new AdvanceGate();
        gate.begin("normal", 20, 1000);
        gate.pageChanged();
        assertEquals(AdvanceGate.State.FAILED, gate.interrupt());
        assertEquals(AdvanceGate.State.IDLE, gate.inspect("normal", 20, 2400));
    }
    @org.junit.Test public void interruptionWithoutRequestIsNotAnError() {
        AdvanceGate gate = new AdvanceGate();
        assertEquals(AdvanceGate.State.IDLE, gate.interrupt());
        gate.begin("normal", 20, 1000);
        gate.cancel();
        assertEquals(AdvanceGate.State.IDLE, gate.interrupt());
    }
    @Test public void idleBeforeRequest() { assertEquals(AdvanceGate.State.IDLE, new AdvanceGate().inspect("A", 35, 1000)); }
    @Test public void doesNotTreatGestureCompletionAsVideoChange() {
        AdvanceGate gate = new AdvanceGate(); gate.begin("A", 35, 1000); assertEquals(AdvanceGate.State.WAITING, gate.inspect("A", 35, 2500));
    }
    @Test public void waitsForAnimationEvenIfVideoChanged() {
        AdvanceGate gate = new AdvanceGate(); gate.begin("A", 35, 1000); assertEquals(AdvanceGate.State.WAITING, gate.inspect("B", 35, 1500));
    }
    @Test public void identityChangeConfirms() {
        AdvanceGate gate = new AdvanceGate(); gate.begin("A", 35, 1000); assertEquals(AdvanceGate.State.CONFIRMED, gate.inspect("B", 35, 2500)); assertFalse(gate.pending());
    }
    @Test public void pageIndexChangeConfirmsEqualLengthVideo() {
        AdvanceGate gate = new AdvanceGate(); gate.begin("", 35, 1000); gate.pageChanged(); assertEquals(AdvanceGate.State.CONFIRMED, gate.inspect("", 35, 2500));
    }
    @Test public void timeoutFailsClosed() {
        AdvanceGate gate = new AdvanceGate(); gate.begin("A", 35, 1000); assertEquals(AdvanceGate.State.FAILED, gate.inspect("A", 35, 5500));
    }
    @Test public void cancelInvalidatesPendingRequest() {
        AdvanceGate gate = new AdvanceGate(); gate.begin("A", 35, 1000); gate.cancel(); assertEquals(AdvanceGate.State.IDLE, gate.inspect("B", 35, 2500));
    }
    @Test public void unknownProgressCannotConfirmTransition() {
        AdvanceGate gate = new AdvanceGate(); gate.begin("A", 35, 1000); assertEquals(AdvanceGate.State.WAITING, gate.inspect("", -1, 2500));
    }
    @Test public void missingSnapshotDoesNotClearPendingProtection() {
        AdvanceGate gate = new AdvanceGate(); gate.begin("A", 35, 1000);
        assertEquals(AdvanceGate.State.WAITING, gate.unavailable(2500)); assertTrue(gate.pending());
        assertEquals(AdvanceGate.State.FAILED, gate.inspect("A", 35, 5500));
    }
    @Test public void missingSnapshotCanRecoverWithConfirmedTransition() {
        AdvanceGate gate = new AdvanceGate(); gate.begin("A", 35, 1000); gate.unavailable(2500);
        assertEquals(AdvanceGate.State.CONFIRMED, gate.inspect("B", 35, 3000));
    }
    @Test public void lateSnapshotCannotBypassTimeout() {
        AdvanceGate gate = new AdvanceGate(); gate.begin("A", 35, 1000);
        assertEquals(AdvanceGate.State.FAILED, gate.inspect("B", 35, 10000));
    }
    @Test public void normalToAdNeedsNoAdDuration() {
        AdvanceGate gate = new AdvanceGate(); gate.begin("video-A", 21, 1000);
        assertEquals(AdvanceGate.State.WAITING, gate.inspectRecognizedPage("instagram-ad", 1500));
        assertEquals(AdvanceGate.State.CONFIRMED, gate.inspectRecognizedPage("instagram-ad", 2500));
    }
    @Test public void adToIdentifiedClocklessVideoCanConfirmButDoesNotInventProgress() {
        AdvanceGate gate = new AdvanceGate(); gate.begin("instagram-ad", -1, 1000);
        assertEquals(AdvanceGate.State.CONFIRMED, gate.inspectRecognizedPage("video-B", 2500));
    }
    @Test public void latePreviousPageEventCannotConfirmSameOrConsecutiveAd() {
        AdvanceGate gate = new AdvanceGate(); gate.begin("instagram-ad", -1, 1000); gate.pageChanged();
        assertEquals(AdvanceGate.State.WAITING, gate.inspectRecognizedPage("instagram-ad", 2500));
        assertTrue(gate.pending());
        assertEquals(AdvanceGate.State.FAILED, gate.inspectRecognizedPage("instagram-ad", 5500));
    }
    @Test public void missingOrUnknownIdentityCannotConfirmClocklessPage() {
        AdvanceGate gate = new AdvanceGate(); gate.begin("instagram-ad", -1, 1000); gate.pageChanged();
        assertEquals(AdvanceGate.State.WAITING, gate.inspectRecognizedPage("", 2500));
        assertEquals(AdvanceGate.State.WAITING, gate.unavailable(3000));
        assertEquals(AdvanceGate.State.FAILED, gate.unavailable(5500));
    }
    @Test public void lateRecognizedPageCannotBypassTimeout() {
        AdvanceGate gate = new AdvanceGate(); gate.begin("instagram-ad", -1, 1000);
        assertEquals(AdvanceGate.State.FAILED, gate.inspectRecognizedPage("video-B", 5500));
    }
}
