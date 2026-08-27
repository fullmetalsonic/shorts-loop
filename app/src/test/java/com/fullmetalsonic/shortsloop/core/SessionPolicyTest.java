package com.fullmetalsonic.shortsloop.core;
import org.junit.Test;
import static org.junit.Assert.*;
public final class SessionPolicyTest {
    @Test public void missingRootRetainsPendingConfirmation() {
        assertFalse(SessionPolicy.packageChanged("com.instagram.android", null));
        assertFalse(SessionPolicy.packageChanged("com.instagram.android", ""));
        AdvanceGate gate = new AdvanceGate(); gate.begin("same-video", 19, 1000);
        if (SessionPolicy.packageChanged("com.instagram.android", "")) gate.cancel();
        assertTrue(gate.pending());
        assertEquals(AdvanceGate.State.WAITING, gate.unavailable(2000));
        assertEquals(AdvanceGate.State.FAILED, gate.unavailable(6000));
    }
    @Test public void knownDifferentAppResetsSession() {
        assertFalse(SessionPolicy.packageChanged("com.instagram.android", "com.instagram.android"));
        assertTrue(SessionPolicy.packageChanged("com.google.android.youtube", "com.instagram.android"));
        assertTrue(SessionPolicy.packageChanged("com.instagram.android", "com.android.systemui"));
    }
}
