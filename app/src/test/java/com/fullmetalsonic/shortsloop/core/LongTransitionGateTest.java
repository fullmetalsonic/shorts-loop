package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class LongTransitionGateTest {
    private final AdvanceGate gate = new AdvanceGate();
    private void begin() { gate.begin("generic", 62, 1000); }
    private AdvanceGate.State sample(double position, double duration, boolean pager, long at) {
        return gate.inspectLongPage("generic", new Progress(position, duration), pager, at);
    }
    @Test public void identicalGenericIdentityNeedsVerifiedPagerAndMovingDifferentDuration() {
        begin(); assertEquals(AdvanceGate.State.WAITING, sample(0, 93, true, 2200));
        assertEquals(AdvanceGate.State.CONFIRMED, sample(0.5, 93, true, 2500));
        assertFalse(gate.pending()); assertEquals(AdvanceGate.State.IDLE, sample(1, 93, true, 2800));
    }
    @Test public void durationChangeWithoutVerifiedPagerCannotConfirm() {
        begin(); assertEquals(AdvanceGate.State.WAITING, sample(0, 93, false, 2200));
        assertEquals(AdvanceGate.State.WAITING, sample(1, 93, false, 3200));
        assertEquals(AdvanceGate.State.FAILED, sample(2, 93, false, 5500));
    }
    @Test public void ordinaryPageChangedFlagCannotReplaceVerifiedEvidence() {
        begin(); gate.pageChanged(); sample(0, 93, false, 2200);
        assertEquals(AdvanceGate.State.WAITING, sample(1, 93, false, 3200));
        assertEquals(AdvanceGate.State.FAILED, sample(2, 93, false, 5500));
    }
    @Test public void sameDurationAndIdentityRemainUnconfirmedEvenWithPagerChange() {
        begin(); sample(0, 62, true, 2200);
        assertEquals(AdvanceGate.State.WAITING, sample(1, 62, true, 3200));
        assertEquals(AdvanceGate.State.FAILED, sample(2, 62, true, 5500));
    }
    @Test public void halfSecondDurationDifferenceIsNotEnough() {
        begin(); sample(0, 62.5, true, 2200);
        assertEquals(AdvanceGate.State.WAITING, sample(1, 62.5, true, 3200));
    }
    @Test public void unknownRequestDurationCannotUseFallback() {
        for (double duration : new double[]{-1, 2, 3601, Double.NaN, Double.POSITIVE_INFINITY}) {
            gate.begin("generic", duration, 1000); sample(0, 93, true, 2200);
            assertEquals(AdvanceGate.State.WAITING, sample(1, 93, true, 3200));
        }
    }
    @Test public void missingOriginalIdentityCannotUseFallback() {
        for (String identity : new String[]{null, ""}) {
            gate.begin(identity, 62, 1000); sample(0, 93, true, 2200);
            assertEquals(AdvanceGate.State.WAITING, sample(1, 93, true, 3200));
        }
    }
    @Test public void unknownDestinationIdentityCannotUseFallback() {
        for (String identity : new String[]{null, ""}) {
            begin(); gate.inspectLongPage(identity, new Progress(0, 93), true, 2200);
            assertEquals(AdvanceGate.State.WAITING,
                    gate.inspectLongPage(identity, new Progress(1, 93), true, 3200));
        }
    }
    @Test public void invalidProgressDropsCandidateEvidence() {
        for (Progress invalid : new Progress[]{null, new Progress(0, 2), new Progress(0, 3601),
                new Progress(Double.NaN, 93), new Progress(-1, 93)}) {
            begin(); sample(0, 93, true, 2200);
            assertEquals(AdvanceGate.State.WAITING, gate.inspectLongPage("generic", invalid, true, 2500));
            assertEquals(AdvanceGate.State.WAITING, sample(1, 93, true, 2800));
            assertEquals(AdvanceGate.State.CONFIRMED, sample(1.5, 93, true, 3100));
        }
    }
    @Test public void freshDurationRequiresThreeHundredMillisecondsOfStability() {
        begin(); sample(0, 93, true, 2200);
        assertEquals(AdvanceGate.State.WAITING, sample(0.1, 93, true, 2300));
        assertEquals(AdvanceGate.State.WAITING, sample(0.2, 93, true, 2499));
        assertEquals(AdvanceGate.State.CONFIRMED, sample(0.4, 93, true, 2500));
    }
    @Test public void animationFloorStillAppliesToFallback() {
        begin(); sample(0, 93, true, 1100);
        assertEquals(AdvanceGate.State.WAITING, sample(0.5, 93, true, 1400));
        assertEquals(AdvanceGate.State.WAITING, sample(1, 93, true, 2199));
        assertEquals(AdvanceGate.State.CONFIRMED, sample(1.2, 93, true, 2200));
    }
    @Test public void exactTimeoutWinsOverOtherwiseCompleteEvidence() {
        begin(); sample(0, 93, true, 5200);
        assertEquals(AdvanceGate.State.FAILED, sample(0.5, 93, true, 5500));
    }
    @Test public void frozenProgressNeverConfirmsDurationFallback() {
        begin(); sample(4, 93, true, 2200);
        assertEquals(AdvanceGate.State.WAITING, sample(4, 93, true, 2500));
        assertEquals(AdvanceGate.State.WAITING, sample(4, 93, true, 3200));
        assertEquals(AdvanceGate.State.FAILED, sample(4, 93, true, 5500));
    }
    @Test public void earlyMovementThenPauseDoesNotConfirm() {
        begin(); sample(0, 93, true, 2200); sample(0.5, 93, true, 2300);
        assertEquals(AdvanceGate.State.WAITING, sample(0.5, 93, true, 2500));
        assertEquals(AdvanceGate.State.CONFIRMED, sample(1, 93, true, 2800));
    }
    @Test public void quantizedProgressWaitsForItsNextRealUpdate() {
        begin(); for (long at = 1300; at <= 3100; at += 300)
            assertEquals(AdvanceGate.State.WAITING, sample(0, 93, true, at));
        assertEquals(AdvanceGate.State.CONFIRMED, sample(2, 93, true, 3400));
    }
    @Test public void implausibleForwardJumpRestartsQualification() {
        begin(); sample(0, 93, true, 2200);
        assertEquals(AdvanceGate.State.WAITING, sample(80, 93, true, 2500));
        assertEquals(AdvanceGate.State.CONFIRMED, sample(80.5, 93, true, 2800));
    }
    @Test public void backwardSeekRequiresNewStableForwardMotion() {
        begin(); sample(20, 93, true, 2200);
        assertEquals(AdvanceGate.State.WAITING, sample(0, 93, true, 2500));
        assertEquals(AdvanceGate.State.CONFIRMED, sample(0.5, 93, true, 2800));
    }
    @Test public void longObservationGapCannotDonateOldStability() {
        begin(); sample(0, 93, true, 2200);
        assertEquals(AdvanceGate.State.WAITING, sample(2, 93, true, 3800));
        assertEquals(AdvanceGate.State.CONFIRMED, sample(2.5, 93, true, 4100));
    }
    @Test public void repeatedOrBackwardTimestampsDiscardOldCandidate() {
        for (long at : new long[]{2200, 2100}) {
            begin(); sample(0, 93, true, 2200);
            assertEquals(AdvanceGate.State.WAITING, sample(1, 93, true, at));
            assertEquals(AdvanceGate.State.CONFIRMED, sample(1.5, 93, true, at + 300));
        }
    }
    @Test public void changingDurationRestartsQualification() {
        begin(); sample(0, 93, true, 2200);
        assertEquals(AdvanceGate.State.WAITING, sample(0.5, 94, true, 2500));
        assertEquals(AdvanceGate.State.CONFIRMED, sample(1, 94, true, 2800));
    }
    @Test public void losingVerifiedPagerEvidenceDropsCandidate() {
        begin(); sample(0, 93, true, 2200);
        assertEquals(AdvanceGate.State.WAITING, sample(0.5, 93, false, 2500));
        assertEquals(AdvanceGate.State.WAITING, sample(1, 93, true, 2800));
        assertEquals(AdvanceGate.State.CONFIRMED, sample(1.5, 93, true, 3100));
    }
    @Test public void existingDifferentStableIdentityPathStillWorksWithoutClockOrPagerEvidence() {
        begin(); assertEquals(AdvanceGate.State.WAITING, gate.inspectLongPage("different", null, false, 2200));
        assertEquals(AdvanceGate.State.CONFIRMED, gate.inspectLongPage("different", null, false, 2500));
    }
    @Test public void cancelAndNewRequestDiscardAllFallbackEvidence() {
        begin(); sample(0, 93, true, 2200); gate.cancel();
        assertEquals(AdvanceGate.State.IDLE, sample(1, 93, true, 2500));
        gate.begin("generic", 62, 2600);
        assertEquals(AdvanceGate.State.WAITING, sample(2, 93, true, 4000));
        assertEquals(AdvanceGate.State.CONFIRMED, sample(2.5, 93, true, 4300));
    }
    @Test public void interruptionNeverBecomesAConfirmedFallback() {
        begin(); sample(0, 93, true, 2200);
        assertEquals(AdvanceGate.State.FAILED, gate.interrupt());
        assertEquals(AdvanceGate.State.IDLE, sample(1, 93, true, 2500));
    }
    private AdvanceGate.State content(String key, double position, double duration, boolean pager, long at) {
        return gate.inspectContentPage(key, new Progress(position, duration), pager, at);
    }
    @Test public void partialMetadataLossAloneCannotConfirmSameVideo() {
        gate.begin("content:title+audio", 62, 1000);
        assertEquals(AdvanceGate.State.WAITING, content("content:title", 1, 62, false, 2200));
        assertEquals(AdvanceGate.State.WAITING, content("content:title", 2, 62, false, 3200));
        assertEquals(AdvanceGate.State.FAILED, content("content:title", 3, 62, false, 5500));
    }
    @Test public void differentMetadataAndDifferentDurationConfirmAfterStableForwardMotion() {
        begin(); assertEquals(AdvanceGate.State.WAITING, content("content:next", 0, 93, false, 2200));
        assertEquals(AdvanceGate.State.CONFIRMED, content("content:next", 0.5, 93, false, 2500));
    }
    @Test public void equalDurationNeedsVerifiedPagerAlongsideDifferentMetadata() {
        begin(); assertEquals(AdvanceGate.State.WAITING, content("content:next", 0, 62, true, 2200));
        assertEquals(AdvanceGate.State.CONFIRMED, content("content:next", 0.5, 62, true, 2500));
    }
    @Test public void unchangedContentKeyNeverUsesEitherIndependentSignalAlone() {
        begin(); assertEquals(AdvanceGate.State.WAITING, content("generic", 0, 93, true, 2200));
        assertEquals(AdvanceGate.State.WAITING, content("generic", 1, 93, true, 3200));
        assertEquals(AdvanceGate.State.FAILED, content("generic", 2, 93, true, 5500));
    }
    @Test public void contentConfirmationStillRequiresFreshMovementAtThreeHundredMilliseconds() {
        begin(); content("content:next", 0, 93, false, 2200);
        assertEquals(AdvanceGate.State.WAITING, content("content:next", 0.5, 93, false, 2300));
        assertEquals(AdvanceGate.State.WAITING, content("content:next", 0.5, 93, false, 2500));
        assertEquals(AdvanceGate.State.CONFIRMED, content("content:next", 1, 93, false, 2800));
    }
    @Test public void changingSupplementalSubsetCannotDonatePriorStability() {
        begin(); content("content:title+audio", 0, 93, false, 2200);
        assertEquals(AdvanceGate.State.WAITING, content("content:title", 0.5, 93, false, 2500));
        assertEquals(AdvanceGate.State.CONFIRMED, content("content:title", 1, 93, false, 2800));
    }
    @Test public void unknownOrInvalidProgressCannotConfirmContentEvenWithPager() {
        for (Progress invalid : new Progress[]{null, new Progress(0, 2), new Progress(Double.NaN, 93), new Progress(0, -1)}) {
            begin(); gate.inspectContentPage("content:next", invalid, true, 2200);
            assertEquals(AdvanceGate.State.WAITING, gate.inspectContentPage("content:next", invalid, true, 2500));
        }
    }
    @Test public void ordinaryPageChangedFlagDoesNotVerifyContentPager() {
        begin(); gate.pageChanged(); content("content:next", 0, 62, false, 2200);
        assertEquals(AdvanceGate.State.WAITING, content("content:next", 0.5, 62, false, 2500));
    }
    @Test public void unknownRequestDurationCannotSubstituteForVerifiedPager() {
        gate.begin("content:old", -1, 1000); content("content:next", 0, 93, false, 2200);
        assertEquals(AdvanceGate.State.WAITING, content("content:next", 0.5, 93, false, 2500));
    }
    @Test public void contentGapRequiresNewStableProgressPair() {
        begin(); content("content:next", 0, 93, false, 2200);
        assertEquals(AdvanceGate.State.WAITING, content("content:next", 2, 93, false, 3800));
        assertEquals(AdvanceGate.State.CONFIRMED, content("content:next", 2.5, 93, false, 4100));
    }
    @Test public void contentAnimationFloorAndTimeoutRemainUnchanged() {
        begin(); content("content:next", 0, 93, false, 1100);
        assertEquals(AdvanceGate.State.WAITING, content("content:next", 0.5, 93, false, 1400));
        assertEquals(AdvanceGate.State.CONFIRMED, content("content:next", 1, 93, false, 2200));
        begin(); content("content:next", 0, 93, false, 5200);
        assertEquals(AdvanceGate.State.FAILED, content("content:next", 0.5, 93, false, 5500));
    }
    @Test public void cancelledContentCandidateCannotConfirmLater() {
        begin(); content("content:next", 0, 93, false, 2200); gate.cancel();
        assertEquals(AdvanceGate.State.IDLE, content("content:next", 1, 93, false, 3200));
    }
}
