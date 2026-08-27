package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class VisualSafetyTest {
    @Test public void explicitPlayLabelsOnly() {
        assertTrue(InstagramPolicy.isPlayControl("재생"));
        assertTrue(InstagramPolicy.isPlayControl("Play video"));
        assertFalse(InstagramPolicy.isPlayControl("재생 중"));
        assertFalse(InstagramPolicy.isPlayControl("Replay tutorial caption"));
        assertFalse(InstagramPolicy.isPlayControl(null));
    }
    @Test public void clockAppearingOnSameVisualPageCannotConfirmAdvance() {
        AdvanceGate gate = new AdvanceGate(); gate.begin("same-page", -1, 1000);
        gate.pageChanged(); // Even a delayed accessibility page event is insufficient.
        assertEquals(AdvanceGate.State.WAITING, gate.inspectRecognizedPage("same-page", 2500));
        assertEquals(AdvanceGate.State.FAILED, gate.inspectRecognizedPage("same-page", 5500));
    }
    @Test public void visualAdvanceRequiresDifferentIdentifiedPage() {
        AdvanceGate gate = new AdvanceGate(); gate.begin("first", -1, 1000);
        assertEquals(AdvanceGate.State.WAITING, gate.inspectRecognizedPage("", 2500));
        assertEquals(AdvanceGate.State.CONFIRMED, gate.inspectRecognizedPage("next", 3000));
    }
}
