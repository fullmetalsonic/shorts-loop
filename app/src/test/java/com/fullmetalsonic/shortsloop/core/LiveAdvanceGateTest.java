package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class LiveAdvanceGateTest {
    @Test public void nodeReplacementAloneCannotConfirmLiveSwipe() {
        AdvanceGate g = new AdvanceGate(); g.begin("live-a", -1, 0);
        assertEquals(AdvanceGate.State.WAITING, g.inspectLivePage("live-b", 1300));
        assertEquals(AdvanceGate.State.FAILED, g.inspectLivePage("live-b", 4500));
    }
    @Test public void indexChangeWithoutDifferentPageCannotConfirm() {
        AdvanceGate g = new AdvanceGate(); g.begin("live-a", -1, 0); g.pageChanged();
        assertEquals(AdvanceGate.State.WAITING, g.inspectLivePage("live-a", 1300));
        assertEquals(AdvanceGate.State.FAILED, g.inspectLivePage("live-a", 4500));
    }
    @Test public void requiresStableCandidateAndMinimumGestureTime() {
        AdvanceGate g = new AdvanceGate(); g.begin("a", -1, 0); g.pageChanged();
        assertEquals(AdvanceGate.State.WAITING, g.inspectLivePage("b", 800));
        assertEquals(AdvanceGate.State.WAITING, g.inspectLivePage("b", 1199));
        assertEquals(AdvanceGate.State.CONFIRMED, g.inspectLivePage("b", 1200));
    }
    @Test public void candidateChurnRestartsStabilityCheck() {
        AdvanceGate g = new AdvanceGate(); g.begin("a", -1, 0); g.pageChanged();
        g.inspectLivePage("b", 1200);
        assertEquals(AdvanceGate.State.WAITING, g.inspectLivePage("c", 1500));
        assertEquals(AdvanceGate.State.WAITING, g.inspectLivePage("c", 1799));
        assertEquals(AdvanceGate.State.CONFIRMED, g.inspectLivePage("c", 1800));
    }
    @Test public void unknownPageBreaksCandidateContinuity() {
        AdvanceGate g = new AdvanceGate(); g.begin("a", -1, 0); g.pageChanged();
        g.inspectLivePage("b", 1200); g.inspectLivePage("", 1400);
        assertEquals(AdvanceGate.State.WAITING, g.inspectLivePage("b", 1500));
        assertEquals(AdvanceGate.State.CONFIRMED, g.inspectLivePage("b", 1800));
    }
    @Test public void secondRequestCannotReusePreviousIndexEvidence() {
        AdvanceGate g = new AdvanceGate(); g.begin("a", -1, 0); g.pageChanged();
        g.inspectLivePage("b", 900); g.inspectLivePage("b", 1200);
        g.begin("b", -1, 1500);
        assertEquals(AdvanceGate.State.WAITING, g.inspectLivePage("c", 3000));
        assertEquals(AdvanceGate.State.FAILED, g.inspectLivePage("c", 6000));
    }
    @Test public void liveAndNormalTransitionsRetainStrictRecognizedPageGate() {
        AdvanceGate g = new AdvanceGate(); g.begin("normal-a", 20, 0);
        assertEquals(AdvanceGate.State.CONFIRMED, g.inspectRecognizedPage("live-a", 1200));
        g.begin("live-a", -1, 2000);
        assertEquals(AdvanceGate.State.CONFIRMED, g.inspectRecognizedPage("normal-b", 3200));
    }
    @Test public void staleThenFreshSameIndexUsesRequestBaselineNotLastRejectedEvent() {
        AdvanceGate g = new AdvanceGate(); g.begin("live-a", -1, 1000);
        int requestIndex = 5;
        if (LiveTransitionPolicy.accepts(1000, 900, 1200, 7, 7, requestIndex, 6, true)) g.pageChanged();
        assertEquals(AdvanceGate.State.WAITING, g.inspectLivePage("live-b", 2200));
        if (LiveTransitionPolicy.accepts(1000, 1500, 2300, 7, 7, requestIndex, 6, true)) g.pageChanged();
        assertEquals(AdvanceGate.State.WAITING, g.inspectLivePage("live-b", 2300));
        assertEquals(AdvanceGate.State.CONFIRMED, g.inspectLivePage("live-b", 2600));
    }
}
