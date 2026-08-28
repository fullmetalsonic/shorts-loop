package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class ActionArbiterQueueTest {
    @Test public void threeHostsFollowFifoDespiteThePreviousOwnerPollingFirst() {
        ActionArbiter arbiter = new ActionArbiter();
        long first = arbiter.acquire("yt", 0);
        assertFalse(arbiter.available("ig", 10));
        assertFalse(arbiter.available("tt", 20));
        arbiter.release(first, 300);
        assertFalse(arbiter.available("yt", 480));
        assertFalse(arbiter.available("tt", 480));
        long second = arbiter.acquire("ig", 480);
        assertTrue(second > first);
        arbiter.release(second, 780);
        assertEquals(-1, arbiter.acquire("yt", 960));
        long third = arbiter.acquire("tt", 960);
        assertTrue(third > second);
        arbiter.release(third, 1260);
        assertTrue(arbiter.acquire("yt", 1440) > third);
    }

    @Test public void repeatedRequestsDoNotDuplicateOrMoveAWaitingHost() {
        ActionArbiter arbiter = new ActionArbiter();
        long owner = arbiter.acquire("yt", 0);
        arbiter.available("ig", 10);
        arbiter.available("tt", 20);
        for (int at = 30; at < 300; at++) {
            assertFalse(arbiter.available("ig", at));
            assertEquals(-1, arbiter.acquire("ig", at));
        }
        arbiter.release(owner, 300);
        long ig = arbiter.acquire("ig", 480);
        assertTrue(ig > owner);
        arbiter.release(ig, 780);
        assertFalse(arbiter.available("ig", 960));
        assertTrue(arbiter.acquire("tt", 960) > ig);
    }

    @Test public void duplicatePollsDoNotExtendTheTwoSecondReservation() {
        ActionArbiter arbiter = new ActionArbiter();
        long owner = arbiter.acquire("yt", 0);
        arbiter.available("ig", 10);
        arbiter.available("tt", 1000);
        arbiter.release(owner, 1700);
        assertTrue(arbiter.available("ig", 1990));
        assertFalse(arbiter.available("tt", 2009));
        assertTrue(arbiter.available("tt", 2010));
        assertFalse(arbiter.busy());
        assertTrue(arbiter.acquire("tt", 2010) > owner);
    }

    @Test public void expiredHostRejoinsBehindStillValidWaiters() {
        ActionArbiter arbiter = new ActionArbiter();
        long owner = arbiter.acquire("yt", 0);
        arbiter.available("ig", 10);
        arbiter.available("tt", 1000);
        arbiter.release(owner, 1700);
        assertFalse(arbiter.available("ig", 2010));
        long tt = arbiter.acquire("tt", 2010);
        assertTrue(tt > owner);
        arbiter.release(tt, 2310);
        assertTrue(arbiter.acquire("ig", 2490) > tt);
    }

    @Test public void cancellingTheHeadPromotesTheNextHostWithoutReleasingTheOwner() {
        ActionArbiter arbiter = new ActionArbiter();
        long owner = arbiter.acquire("yt", 0);
        arbiter.available("ig", 10);
        arbiter.available("tt", 20);
        arbiter.cancelWaiting("ig");
        arbiter.cancelWaiting("yt");
        assertTrue(arbiter.busy());
        assertEquals(-1, arbiter.acquire("tt", 100));
        arbiter.release(owner, 300);
        assertTrue(arbiter.acquire("tt", 480) > owner);
    }

    @Test public void cancelledTailMustJoinAtTheBackAgain() {
        ActionArbiter arbiter = new ActionArbiter();
        long owner = arbiter.acquire("yt", 0);
        arbiter.available("ig", 10);
        arbiter.available("tt", 20);
        arbiter.cancelWaiting("tt");
        arbiter.cancelWaiting("missing");
        arbiter.release(owner, 300);
        assertFalse(arbiter.available("yt", 480));
        assertFalse(arbiter.available("tt", 480));
        long ig = arbiter.acquire("ig", 480);
        arbiter.release(ig, 780);
        assertFalse(arbiter.available("tt", 960));
        assertTrue(arbiter.acquire("yt", 960) > ig);
    }

    @Test public void expiryCannotBypassOwnerOrCooldownAndNeverExecutesInput() {
        ActionArbiter arbiter = new ActionArbiter();
        long owner = arbiter.acquire("yt", 0);
        arbiter.available("ig", 10);
        arbiter.available("tt", 20);
        assertEquals(-1, arbiter.acquire("ig", 4000));
        assertTrue(arbiter.busy());
        arbiter.release(owner, 4100);
        assertFalse(arbiter.available("ig", 4279));
        assertFalse(arbiter.busy());
        assertTrue(arbiter.available("ig", 4280));
        assertFalse(arbiter.busy());
    }

    @Test public void vanishedHeadExpiresEvenWhileAnotherHostKeepsRequesting() {
        ActionArbiter arbiter = new ActionArbiter();
        long owner = arbiter.acquire("yt", 0);
        arbiter.available("ig", 10);
        arbiter.available("tt", 1000);
        arbiter.release(owner, 1700);
        for (int at = 1880; at < 2010; at++) assertFalse(arbiter.available("tt", at));
        assertTrue(arbiter.acquire("tt", 2010) > owner);
    }

    @Test public void staleAndDuplicateCallbacksCannotReleaseOrDelayTheNextOwner() {
        ActionArbiter arbiter = new ActionArbiter();
        long first = arbiter.acquire("yt", 0);
        arbiter.available("ig", 10);
        arbiter.available("tt", 20);
        arbiter.release(first, 300);
        arbiter.release(first, 400);
        long second = arbiter.acquire("ig", 480);
        assertTrue(second > first);
        arbiter.release(first, 600);
        arbiter.release(second + 1, 650);
        assertTrue(arbiter.busy());
        arbiter.release(second, 780);
        arbiter.release(first, 900);
        assertTrue(arbiter.acquire("tt", 960) > second);
    }

    @Test public void queueIsBoundedAndRejectsInvalidHostsWithoutLeakingOwnership() {
        ActionArbiter arbiter = new ActionArbiter();
        assertFalse(arbiter.available(null, 0));
        assertFalse(arbiter.available("", 0));
        assertEquals(-1, arbiter.acquire("yt", -1));
        long owner = arbiter.acquire("yt", 0);
        arbiter.release(owner, 100);
        assertFalse(arbiter.available("yt", 110));
        assertFalse(arbiter.available("ig", 120));
        assertFalse(arbiter.available("tt", 130));
        assertFalse(arbiter.available("fourth", 140));
        arbiter.cancelWaiting(null);
        assertFalse(arbiter.busy());
        assertTrue(arbiter.acquire("yt", 280) > owner);
    }

    @Test public void fixedPollingOrderIsFairAcrossThreeThousandGrants() {
        ActionArbiter arbiter = new ActionArbiter();
        String[] hosts = {"yt", "ig", "tt"};
        int[] grants = new int[hosts.length];
        long[] joined = {-1, -1, -1};
        long[] maximumWait = new long[hosts.length];
        long previousLease = 0;
        for (int round = 0; round < 3000; round++) {
            long now = round * 480L;
            long token = -1;
            int winner = -1;
            for (int host = 0; host < hosts.length; host++) {
                if (joined[host] < 0) joined[host] = now;
                long candidate = arbiter.acquire(hosts[host], now);
                if (candidate > 0) {
                    assertEquals("More than one input owner", -1, winner);
                    winner = host; token = candidate;
                    maximumWait[host] = Math.max(maximumWait[host], now - joined[host]);
                    joined[host] = -1;
                    grants[host]++;
                }
            }
            assertEquals("Fixed poll order must not starve the third host", round % 3, winner);
            assertTrue(token > previousLease);
            assertTrue(arbiter.busy());
            for (String host : hosts) assertEquals(-1, arbiter.acquire(host, now + 100));
            arbiter.release(previousLease, now + 200);
            assertTrue(arbiter.busy());
            arbiter.release(token, now + 300);
            assertFalse(arbiter.busy());
            previousLease = token;
        }
        assertArrayEquals(new int[]{1000, 1000, 1000}, grants);
        for (long wait : maximumWait) assertTrue("No host waits behind more than two inputs", wait <= 960);
    }

    @Test public void aLoneHostHasNoFreshCollectionDelay() {
        ActionArbiter arbiter = new ActionArbiter();
        long first = arbiter.acquire("yt", 0);
        arbiter.release(first, 1800);
        assertTrue(arbiter.acquire("yt", 2100) > first);
    }

    @Test public void expiredOtherDemandGivesOnlyFreshRequestsOneFixedWindow() {
        ActionArbiter arbiter = new ActionArbiter();
        long first = arbiter.acquire("yt", 0);
        arbiter.available("ig", 10);
        arbiter.release(first, 1800);
        assertEquals(-1, arbiter.acquire("yt", 2100));
        assertFalse(arbiter.busy());
        for (int now = 2101; now < 2400; now++) assertFalse(arbiter.available("yt", now));
        long second = arbiter.acquire("yt", 2400);
        assertTrue("An expired peer that never returns cannot acquire or hold the turn", second > first);
        assertEquals(-1, arbiter.acquire("ig", 2700));
        arbiter.release(first, 2750);
        assertTrue(arbiter.busy());
    }

    @Test public void freshPeersAfterTotalExpiryReceiveTurnsWithoutRevivingExpiredIntentions() {
        ActionArbiter arbiter = new ActionArbiter();
        long first = arbiter.acquire("yt", 0);
        arbiter.available("ig", 10);
        arbiter.available("tt", 20);
        arbiter.release(first, 1800);
        assertFalse(arbiter.available("yt", 2100));
        assertFalse(arbiter.available("tt", 2200));
        assertFalse(arbiter.available("yt", 2400));
        long next = arbiter.acquire("tt", 2400);
        assertTrue("Only TT re-requested; the expired IG entry cannot take this turn", next > first);
    }

    @Test public void cancellingFreshPeerDoesNotRestartCollectionOrKeepItsTurn() {
        ActionArbiter arbiter = new ActionArbiter();
        long first = arbiter.acquire("yt", 0);
        arbiter.available("ig", 10);
        arbiter.release(first, 1800);
        arbiter.available("yt", 2100);
        arbiter.available("ig", 2200);
        arbiter.cancelWaiting("ig");
        arbiter.cancelWaiting("yt");
        assertFalse(arbiter.available("yt", 2399));
        assertTrue(arbiter.acquire("yt", 2400) > first);
    }

    @Test public void validWaitersNeedNoExtraCollectionAfterTheirExpiredPredecessor() {
        ActionArbiter arbiter = new ActionArbiter();
        long first = arbiter.acquire("yt", 0);
        arbiter.available("ig", 10);
        arbiter.available("tt", 1000);
        arbiter.release(first, 1800);
        assertFalse(arbiter.available("yt", 2100));
        assertTrue(arbiter.acquire("tt", 2100) > first);
    }

    @Test public void lostCallbacksAndThreeHundredMillisecondPollingStayFairForThreeThousandGrants() {
        ActionArbiter arbiter = new ActionArbiter();
        String[] hosts = {"yt", "ig", "tt"};
        int[] counts = new int[3];
        long owner = -1, releaseAt = -1, previousLease = 0;
        int grants = 0;
        for (long now = 0; grants < 3000 && now < 10_000_000; now += 300) {
            if (owner > 0 && now >= releaseAt) {
                arbiter.release(owner, releaseAt);
                previousLease = owner; owner = -1;
            }
            for (int host = 0; host < hosts.length; host++) {
                long candidate = arbiter.acquire(hosts[host], now);
                if (candidate > 0) {
                    assertEquals("Only the callback timeout released the preceding input", -1, owner);
                    assertEquals("Expiry must not turn fixed polling order into starvation at grant " + grants + ", time " + now,
                            grants % 3, host);
                    assertTrue(candidate > previousLease);
                    owner = candidate; releaseAt = now + 1800;
                    counts[host]++; grants++;
                    arbiter.release(previousLease, now);
                    assertTrue("Late callback cannot release the newly rotated owner", arbiter.busy());
                }
            }
        }
        assertEquals(3000, grants);
        assertArrayEquals(new int[]{1000, 1000, 1000}, counts);
    }
}
