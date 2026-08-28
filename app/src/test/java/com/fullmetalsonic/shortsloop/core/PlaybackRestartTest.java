package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class PlaybackRestartTest {
    private final PlaybackRestart restart = new PlaybackRestart();
    private PlaybackRestart.Start sample(double position, long at) {
        return restart.observe(new Progress(position, 10), "page", at);
    }
    private void begin() { restart.begin("youtube", 4); }
    @Test public void onlyOrdinaryRequestsAreRecoverable() {
        for (int mask = 0; mask < 32; mask++) assertEquals(mask == 0,
                PlaybackRestart.ordinaryRequest((mask & 1) != 0, (mask & 2) != 0, (mask & 4) != 0, (mask & 8) != 0, (mask & 16) != 0));
    }
    @Test public void hostAndWindowMustMatch() {
        begin(); assertTrue(restart.accepts("youtube", 4));
        assertFalse(restart.accepts("instagram", 4)); assertFalse(restart.accepts("youtube", 5));
        restart.begin("", -1); assertFalse(restart.accepts("", -1));
    }
    @Test public void inactiveNeverRestarts() { assertNull(sample(0, 1000)); assertNull(sample(1, 2000)); }
    @Test public void zeroAloneNeverRestarts() { begin(); assertNull(sample(0, 1000)); assertTrue(restart.active()); }
    @Test public void frozenZeroNeverRestarts() {
        begin(); for (int i = 1; i < 60; i++) assertNull(sample(0, i * 1000L));
    }
    @Test public void frozenEndNeverRestarts() {
        begin(); for (int i = 1; i < 60; i++) assertNull(sample(10, i * 1000L));
    }
    @Test public void partialPlayWaitsForNewStartAndForwardMotion() {
        begin(); for (int i = 5; i <= 10; i++) assertNull(sample(i, i * 1000));
        assertNull(sample(0, 11000)); PlaybackRestart.Start start = sample(1, 12000);
        assertNotNull(start); assertEquals(0, start.progress.position, 0); assertEquals(11000, start.at);
        assertFalse(restart.active()); assertNull(sample(2, 13000));
    }
    @Test public void minimumObservationTimeIsRequired() {
        begin(); assertNull(sample(0, 1000)); assertNull(sample(0.1, 1100)); assertNotNull(sample(0.3, 1300));
    }
    @Test public void fractionalNoiseIsNotMovement() {
        begin(); assertNull(sample(0, 1000)); assertNull(sample(0.01, 1300)); assertNull(sample(0.02, 1600));
    }
    @Test public void missedTailCanStillStartANewUncountedPlay() {
        begin(); assertNull(sample(5, 1000)); assertNull(sample(0, 2000)); assertNotNull(sample(1, 3000));
    }
    @Test public void forwardJumpCannotRestart() {
        begin(); assertNull(sample(0, 1000)); assertNull(sample(8, 1300)); assertTrue(restart.active());
    }
    @Test public void backwardSeekDiscardsCandidate() {
        begin(); assertNull(sample(1, 1000)); assertNull(sample(0, 2000));
        PlaybackRestart.Start start = sample(1, 3000); assertNotNull(start); assertEquals(2000, start.at);
    }
    @Test public void gapDiscardsCandidate() {
        begin(); assertNull(sample(0, 1000)); assertNull(sample(2, 5001)); assertTrue(restart.active());
    }
    @Test public void zeroHeldTooLongDoesNotDonateItsOldTimestamp() {
        begin(); for (int i = 1; i <= 6; i++) assertNull(sample(0, i * 1000L));
        PlaybackRestart.Start start = sample(1, 7000); assertNotNull(start); assertTrue(start.at >= 5000);
    }
    @Test public void invalidProgressDiscardsEvidence() {
        for (Progress invalid : new Progress[]{null, new Progress(Double.NaN, 10), new Progress(0, 2), new Progress(-1, 10)}) {
            begin(); sample(0, 1000); assertNull(restart.observe(invalid, "page", 1300)); assertNull(sample(2, 2000));
        }
    }
    @Test public void unknownIdentityNeverRestarts() {
        begin(); assertNull(restart.observe(new Progress(0, 10), "", 1000));
        assertNull(restart.observe(new Progress(1, 10), "", 2000));
    }
    @Test public void identityChangeDiscardsOldCandidate() {
        begin(); sample(0, 1000); assertNull(restart.observe(new Progress(2, 10), "other", 2000));
    }
    @Test public void durationChangeDiscardsOldCandidate() {
        begin(); sample(0, 1000); assertNull(restart.observe(new Progress(2, 20), "page", 2000));
    }
    @Test public void repeatedOrBackwardClockCannotSupplyEvidence() {
        begin(); sample(0, 1000); assertNull(sample(2, 1000)); assertNull(sample(3, 900));
    }
    @Test public void suspendKeepsGuardButDropsEvidence() {
        begin(); sample(0, 1000); restart.suspend(); assertTrue(restart.active()); assertNull(sample(2, 2000));
    }
    @Test public void cancelRepresentsOffOrSettingsChange() {
        begin(); sample(0, 1000); restart.cancel(); assertFalse(restart.active()); assertNull(sample(1, 2000));
    }
    @Test public void shortVideoRequiresItsActualBeginning() {
        begin(); assertNull(restart.observe(new Progress(1, 3), "short", 1000));
        assertNull(restart.observe(new Progress(2, 3), "short", 2000));
        assertNull(restart.observe(new Progress(0, 3), "short", 3000));
        assertNotNull(restart.observe(new Progress(1, 3), "short", 4000));
    }
    @Test public void quantizedTwoSecondUpdatesAreSupported() {
        begin(); for (int at = 1000; at < 3000; at += 300) assertNull(sample(0, at));
        assertNotNull(sample(2, 3100));
    }
    @Test public void newPlayAfterTimeoutMustCompleteBeforeAnotherRequest() {
        assertFullRecovery(1, false);
    }
    @Test public void alreadyMovedButUnconfirmedVideoIsRecountedSafely() {
        assertFullRecovery(1, true);
    }
    @Test public void recoveredTwoAndNinetyNinePlaysKeepConfiguredTarget() {
        assertFullRecovery(2, false); assertFullRecovery(99, false);
    }
    @Test public void zeroNeverEmitsAfterFreshStart() { assertFullRecovery(0, false); }
    private void assertFullRecovery(int target, boolean moved) {
        AdvanceGate gate = new AdvanceGate(); gate.begin("previous", 10, 0);
        assertEquals(AdvanceGate.State.FAILED, gate.unavailable(4500));
        begin(); String identity = moved ? "new-page" : "previous";
        assertNull(restart.observe(new Progress(7, 10), identity, 5000));
        assertNull(restart.observe(new Progress(0, 10), identity, 9000));
        PlaybackRestart.Start start = restart.observe(new Progress(1, 10), identity, 10000);
        assertNotNull(start);
        LoopCounter counter = new LoopCounter(); counter.setTarget(target);
        assertFalse(counter.observe(start.progress, identity, start.at).advance);
        assertFalse(counter.observe(new Progress(1, 10), identity, 10000).advance);
        long now = 10000;
        for (int cycle = 0; cycle < Math.max(1, target); cycle++) {
            for (int position = cycle == 0 ? 2 : 1; position <= 10; position++)
                assertFalse(counter.observe(new Progress(position, 10), identity, now += 1000).advance);
            assertEquals(target > 0 && cycle + 1 == target,
                    counter.observe(new Progress(0, 10), identity, now += 1000).advance);
        }
    }
}
