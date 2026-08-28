package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class YouTubePageStepPolicyTest {
    private final AdvanceGate gate = new AdvanceGate();
    private void begin() { gate.begin("content:old", 59, 1000); }
    private AdvanceGate.State observe(int requested, int current, double position, double duration, long now) {
        boolean allowed = YouTubePageStepPolicy.permits(requested, current);
        return gate.inspectContentPage(allowed ? "content:new" : "", allowed ? new Progress(position, duration) : null,
                allowed && YouTubePageStepPolicy.next(requested, current), now);
    }
    @Test public void onlyExactNextOrdinalIsVerified() {
        assertTrue(YouTubePageStepPolicy.next(0, 1)); assertTrue(YouTubePageStepPolicy.next(8, 9));
        assertFalse(YouTubePageStepPolicy.next(0, 0)); assertFalse(YouTubePageStepPolicy.next(8, 7));
        assertFalse(YouTubePageStepPolicy.next(0, 2)); assertFalse(YouTubePageStepPolicy.next(8, 100));
    }
    @Test public void unknownDoesNotBecomeVerifiedPositionEvidence() {
        for (int[] pair : new int[][]{{-1, -1}, {-1, 1}, {0, -1}}) {
            assertFalse(YouTubePageStepPolicy.next(pair[0], pair[1]));
            assertTrue(YouTubePageStepPolicy.permits(pair[0], pair[1]));
        }
    }
    @Test public void unsafeAndOtherInvalidValuesNeverPermitFallback() {
        for (int invalid : new int[]{YouTubePageStepPolicy.UNSAFE, -3, Integer.MIN_VALUE})
            for (int other : new int[]{YouTubePageStepPolicy.UNKNOWN, 0, 1, Integer.MAX_VALUE}) {
                assertFalse(YouTubePageStepPolicy.permits(invalid, other));
                assertFalse(YouTubePageStepPolicy.permits(other, invalid));
                assertFalse(YouTubePageStepPolicy.next(invalid, other));
                assertFalse(YouTubePageStepPolicy.next(other, invalid));
            }
    }
    @Test public void knownSameBackwardOrSkippedOrdinalRejectsAllFallbacks() {
        for (int[] pair : new int[][]{{0, 0}, {1, 0}, {0, 2}, {5, 4}, {5, 7}})
            assertFalse(YouTubePageStepPolicy.permits(pair[0], pair[1]));
    }
    @Test public void ordinalArithmeticDoesNotOverflow() {
        assertTrue(YouTubePageStepPolicy.next(Integer.MAX_VALUE - 1, Integer.MAX_VALUE));
        assertTrue(YouTubePageStepPolicy.permits(Integer.MAX_VALUE - 1, Integer.MAX_VALUE));
        assertFalse(YouTubePageStepPolicy.next(Integer.MAX_VALUE, Integer.MIN_VALUE));
        assertFalse(YouTubePageStepPolicy.next(Integer.MAX_VALUE, 0));
        assertFalse(YouTubePageStepPolicy.permits(Integer.MAX_VALUE, 0));
        assertFalse(YouTubePageStepPolicy.permits(Integer.MAX_VALUE, Integer.MIN_VALUE));
    }
    @Test public void equalFiftyNineSecondVideosConfirmWithExactNextRow() {
        begin(); assertEquals(AdvanceGate.State.WAITING, observe(0, 1, 0, 59, 2200));
        assertEquals(AdvanceGate.State.CONFIRMED, observe(0, 1, 0.5, 59, 2500));
    }
    @Test public void sameRowCannotConfirmEvenWhenMetadataAndDurationChange() {
        begin(); observe(0, 0, 0, 93, 2200);
        assertEquals(AdvanceGate.State.WAITING, observe(0, 0, 1, 93, 3200));
        assertEquals(AdvanceGate.State.FAILED, observe(0, 0, 2, 93, 5500));
    }
    @Test public void backwardRowCannotConfirmEvenWhenDurationChanges() {
        begin(); observe(1, 0, 0, 93, 2200);
        assertEquals(AdvanceGate.State.WAITING, observe(1, 0, 1, 93, 3200));
        assertEquals(AdvanceGate.State.FAILED, observe(1, 0, 2, 93, 5500));
    }
    @Test public void twoRowsForwardCannotConfirmEvenWhenDurationChanges() {
        begin(); observe(0, 2, 0, 93, 2200);
        assertEquals(AdvanceGate.State.WAITING, observe(0, 2, 1, 93, 3200));
        assertEquals(AdvanceGate.State.FAILED, observe(0, 2, 2, 93, 5500));
    }
    @Test public void rollbackClearsCandidateInsteadOfKeepingStickyPageChange() {
        begin(); assertEquals(AdvanceGate.State.WAITING, observe(0, 1, 0, 59, 2200));
        assertEquals(AdvanceGate.State.WAITING, observe(0, 0, 0.5, 93, 2500));
        assertEquals(AdvanceGate.State.WAITING, observe(0, 1, 1, 59, 2800));
        assertEquals(AdvanceGate.State.CONFIRMED, observe(0, 1, 1.5, 59, 3100));
    }
    @Test public void unknownOrdinalStillNeedsExistingDifferentDurationEvidence() {
        for (int[] pair : new int[][]{{-1, -1}, {-1, 1}, {0, -1}}) {
            begin(); observe(pair[0], pair[1], 0, 59, 2200);
            assertEquals(AdvanceGate.State.WAITING, observe(pair[0], pair[1], 1, 59, 3200));
            assertEquals(AdvanceGate.State.FAILED, observe(pair[0], pair[1], 2, 59, 5500));
            begin(); observe(pair[0], pair[1], 0, 93, 2200);
            assertEquals(AdvanceGate.State.CONFIRMED, observe(pair[0], pair[1], 0.5, 93, 2500));
        }
    }
    @Test public void unsafeOrdinalCannotUseDifferentDurationFallback() {
        for (int[] pair : new int[][]{{-2, 1}, {0, -2}, {-1, -2}, {-2, -1}}) {
            begin(); observe(pair[0], pair[1], 0, 93, 2200);
            assertEquals(AdvanceGate.State.WAITING, observe(pair[0], pair[1], 1, 93, 3200));
            assertEquals(AdvanceGate.State.FAILED, observe(pair[0], pair[1], 2, 93, 5500));
        }
    }
    @Test public void exactNextRowStillNeedsFreshMovementAndStability() {
        begin(); observe(0, 1, 0, 59, 2200);
        assertEquals(AdvanceGate.State.WAITING, observe(0, 1, 0.5, 59, 2300));
        assertEquals(AdvanceGate.State.WAITING, observe(0, 1, 0.5, 59, 2500));
        assertEquals(AdvanceGate.State.CONFIRMED, observe(0, 1, 1, 59, 2800));
    }
    @Test public void exactNextRowCannotConfirmPastDeadline() {
        begin(); observe(0, 1, 0, 59, 5200);
        assertEquals(AdvanceGate.State.FAILED, observe(0, 1, 0.5, 59, 5500));
    }
}
