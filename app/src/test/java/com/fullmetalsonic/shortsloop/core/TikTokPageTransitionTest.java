package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;
import static com.fullmetalsonic.shortsloop.core.AdvanceGate.State.*;

public class TikTokPageTransitionTest {
    private static TikTokPageTransition.Frame f(String page, String media, int index, double fraction) {
        return new TikTokPageTransition.Frame("window+geometry", "pager", page, media, index, fraction);
    }
    private static TikTokPageTransition start(int index, double fraction) {
        TikTokPageTransition t = new TikTokPageTransition(); t.begin(f("A", "imageA", index, fraction), 0); return t;
    }
    @Test public void normalizedDestinationNeedsFreshForwardAndBothSettlements() {
        TikTokPageTransition t = start(4, .02);
        assertEquals(WAITING, t.inspect(f("B", "imageB", 5, .01), 600));
        assertEquals(WAITING, t.inspect(f("B", "imageB", 5, .02), 1000));
        assertEquals(CONFIRMED, t.inspect(f("B", "imageB", 5, .03), 1200));
        assertFalse(t.pending()); assertEquals(IDLE, t.inspect(null, 1300));
    }
    @Test public void knownClocklessSpecialCanConfirmWithoutInventedProgress() {
        for (double source : new double[]{-1, .9}) {
            TikTokPageTransition t = start(4, source);
            assertEquals(WAITING, t.inspect(f("B", "imageB", 5, -1), 1000));
            assertEquals(WAITING, t.inspect(f("B", "imageB", 5, -1), 1200));
            assertEquals(CONFIRMED, t.inspect(f("B", "imageB", 5, -1), 1300));
        }
    }
    @Test public void samePageOrReusedMediaNeverConfirms() {
        for (TikTokPageTransition.Frame candidate : new TikTokPageTransition.Frame[]{
                f("A", "imageB", 5, -1), f("B", "imageA", 5, -1), f("A", "imageA", 5, .2)}) {
            TikTokPageTransition t = start(4, .1);
            assertEquals(WAITING, t.inspect(candidate, 1000));
            assertEquals(WAITING, t.inspect(candidate, 1500));
            assertEquals(FAILED, t.inspect(candidate, 4500));
        }
    }
    @Test public void knownRequestIndexRequiresExactlyNextIncludingMissingMetadata() {
        for (int index : new int[]{-2, -1, 3, 4, 6}) {
            TikTokPageTransition t = start(4, -1);
            assertEquals(WAITING, t.inspect(f("B", "imageB", index, -1), 1000));
            assertEquals(WAITING, t.inspect(f("B", "imageB", index, -1), 1500));
        }
    }
    @Test public void unknownRequestIndexStillRequiresIndependentPageAndMediaProof() {
        TikTokPageTransition t = start(-1, -1);
        assertEquals(WAITING, t.inspect(f("B", "imageB", -1, -1), 900));
        assertEquals(CONFIRMED, t.inspect(f("B", "imageB", -1, -1), 1200));
    }
    @Test public void scopeAndPagerChangeCannotConfirm() {
        for (int type = 0; type < 2; type++) {
            TikTokPageTransition t = start(4, -1);
            TikTokPageTransition.Frame bad = new TikTokPageTransition.Frame(type == 0 ? "other" : "window+geometry",
                    type == 1 ? "other" : "pager", "B", "imageB", 5, -1);
            assertEquals(WAITING, t.inspect(bad, 1000)); assertEquals(WAITING, t.inspect(bad, 1500));
        }
    }
    @Test public void rollbackOrMissingFrameDiscardsEarlierCandidate() {
        for (TikTokPageTransition.Frame rollback : new TikTokPageTransition.Frame[]{null, f("A", "imageA", 4, -1)}) {
            TikTokPageTransition t = start(4, -1);
            t.inspect(f("B", "imageB", 5, -1), 500); t.inspect(rollback, 1100);
            assertEquals(WAITING, t.inspect(f("B", "imageB", 5, -1), 1200));
            assertEquals(WAITING, t.inspect(f("B", "imageB", 5, -1), 1400));
            assertEquals(CONFIRMED, t.inspect(f("B", "imageB", 5, -1), 1500));
        }
    }
    @Test public void destinationChangingAgainCannotReuseCandidateTime() {
        TikTokPageTransition t = start(-1, -1);
        t.inspect(f("B", "imageB", -1, -1), 1000);
        assertEquals(WAITING, t.inspect(f("C", "imageC", -1, -1), 1300));
        assertEquals(CONFIRMED, t.inspect(f("C", "imageC", -1, -1), 1600));
    }
    @Test public void stalledNormalizedProgressDoesNotActLikeClockless() {
        TikTokPageTransition t = start(4, .1);
        t.inspect(f("B", "imageB", 5, .2), 800);
        assertEquals(WAITING, t.inspect(f("B", "imageB", 5, .2), 1200));
        assertEquals(WAITING, t.inspect(f("B", "imageB", 5, .2), 1800));
        assertEquals(FAILED, t.inspect(f("B", "imageB", 5, .2), 4500));
    }
    @Test public void backwardAndLargeForwardJumpsResetCandidate() {
        for (double fraction : new double[]{.1, .7}) {
            TikTokPageTransition t = start(4, .01);
            t.inspect(f("B", "imageB", 5, .3), 900);
            assertEquals(WAITING, t.inspect(f("B", "imageB", 5, fraction), 1300));
            assertEquals(WAITING, t.inspect(f("B", "imageB", 5, fraction + .01), 1400));
            assertEquals(CONFIRMED, t.inspect(f("B", "imageB", 5, fraction + .02), 1600));
        }
    }
    @Test public void lostClockMustSettleAnewWithoutReusingForwardEvidence() {
        TikTokPageTransition t = start(4, .01);
        t.inspect(f("B", "imageB", 5, .1), 800);
        assertEquals(WAITING, t.inspect(f("B", "imageB", 5, -1), 1200));
        assertEquals(WAITING, t.inspect(f("B", "imageB", 5, -1), 1400));
        assertEquals(CONFIRMED, t.inspect(f("B", "imageB", 5, -1), 1500));
    }
    @Test public void longGapAndNonIncreasingTimeCannotReuseOldObservation() {
        TikTokPageTransition t = start(4, -1);
        t.inspect(f("B", "imageB", 5, -1), 100);
        assertEquals(WAITING, t.inspect(f("B", "imageB", 5, -1), 1700));
        assertEquals(WAITING, t.inspect(f("B", "imageB", 5, -1), 1700));
        assertEquals(CONFIRMED, t.inspect(f("B", "imageB", 5, -1), 2000));
    }
    @Test public void sourceAndDestinationFractionsAreStrictlyBounded() {
        for (double fraction : new double[]{Double.NaN, Double.POSITIVE_INFINITY, -.5, -2, 1.1}) {
            try { start(4, fraction); fail("invalid source"); } catch (IllegalArgumentException expected) { }
            TikTokPageTransition t = start(4, -1);
            assertEquals(WAITING, t.inspect(f("B", "imageB", 5, fraction), 1500));
        }
    }
    @Test public void cancelAndTimeoutCannotLeavePendingRequest() {
        TikTokPageTransition t = start(4, -1); t.cancel();
        assertFalse(t.pending()); assertEquals(IDLE, t.inspect(f("B", "imageB", 5, -1), 1500));
        t = start(4, -1); assertEquals(FAILED, t.inspect(null, -1)); assertFalse(t.pending());
    }
}
