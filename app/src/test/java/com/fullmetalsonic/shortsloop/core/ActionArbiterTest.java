package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class ActionArbiterTest {
    @Test public void concurrentRequestsCannotCancelAnActiveGesture() {
        ActionArbiter a = new ActionArbiter();
        long yt = a.acquire("yt", 0); assertTrue(yt > 0);
        assertFalse(a.available("ig", 1)); assertEquals(-1, a.acquire("ig", 2));
        assertEquals(-1, a.acquire("yt", 3));
        a.release(yt, 300); assertFalse(a.available("ig", 479));
        assertTrue(a.available("ig", 480)); assertFalse(a.available("yt", 480));
        assertTrue(a.acquire("ig", 480) > yt);
    }
    @Test public void oldCallbackCannotReleaseANewerLease() {
        ActionArbiter a = new ActionArbiter(); long first = a.acquire("yt", 0);
        a.release(first, 300); long second = a.acquire("ig", 500);
        a.release(first, 800); assertTrue(a.busy()); assertFalse(a.available("yt", 900));
        a.release(second, 1000); assertFalse(a.busy());
    }
    @Test public void vanishedWaitingHostDoesNotStarveRemainingHost() {
        ActionArbiter a = new ActionArbiter(); long first = a.acquire("yt", 0);
        assertFalse(a.available("ig", 20)); a.release(first, 300);
        assertFalse(a.available("yt", 500)); a.cancelWaiting("ig");
        assertTrue(a.available("yt", 500));
    }
    @Test public void waitingReservationExpiresWithoutAutomaticInput() {
        ActionArbiter a = new ActionArbiter(); long first = a.acquire("yt", 0);
        a.available("ig", 20); a.release(first, 300);
        assertFalse(a.available("yt", 1000)); assertTrue(a.available("yt", 2020));
        assertFalse(a.busy());
    }
}
