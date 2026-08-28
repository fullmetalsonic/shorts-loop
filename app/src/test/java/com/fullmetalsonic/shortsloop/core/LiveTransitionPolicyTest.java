package com.fullmetalsonic.shortsloop.core;
import org.junit.Test;
import static org.junit.Assert.*;
public class LiveTransitionPolicyTest {
    @Test public void freshSamePagerDifferentIndexAccepted() {
        assertTrue(LiveTransitionPolicy.accepts(1000, 1200, 1300, 7, 7, 2, 3, true));
    }
    @Test public void delayedPriorEventAndFutureTimeRejected() {
        assertFalse(LiveTransitionPolicy.accepts(1000, 999, 1300, 7, 7, 2, 3, true));
        assertFalse(LiveTransitionPolicy.accepts(1000, 1400, 1300, 7, 7, 2, 3, true));
    }
    @Test public void unknownOrUnchangedIndexRejected() {
        assertFalse(LiveTransitionPolicy.accepts(1000, 1200, 1300, 7, 7, -1, 3, true));
        assertFalse(LiveTransitionPolicy.accepts(1000, 1200, 1300, 7, 7, 2, -1, true));
        assertFalse(LiveTransitionPolicy.accepts(1000, 1200, 1300, 7, 7, 2, 2, true));
    }
    @Test public void differentWindowOrPagerRejected() {
        assertFalse(LiveTransitionPolicy.accepts(1000, 1200, 1300, 7, 8, 2, 3, true));
        assertFalse(LiveTransitionPolicy.accepts(1000, 1200, 1300, 7, 7, 2, 3, false));
        assertFalse(LiveTransitionPolicy.accepts(1000, 1200, 1300, -1, -1, 2, 3, true));
    }
}
