package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class LoopCounterStartToleranceTest {
    @Test public void observedAdReturnSamplesAndInclusiveLimitStartCounting() {
        for (double position : new double[]{1.015, 1.103, 1.299, 1.3}) {
            LoopCounter counter = new LoopCounter();
            counter.setTarget(1);
            LoopCounter.Result result = counter.observe(new Progress(position, 46.066), "destination", 1000);
            assertEquals(1, result.current);
            assertFalse(result.waitingForStart);
            assertFalse(result.advance);
        }
    }

    @Test public void anythingBeyondOnePointThreeStillWaits() {
        LoopCounter counter = new LoopCounter();
        counter.setTarget(1);
        LoopCounter.Result result = counter.observe(new Progress(Math.nextUp(1.3), 46.066), "destination", 1000);
        assertEquals(0, result.current);
        assertTrue(result.waitingForStart);
        assertFalse(result.advance);
    }

    @Test public void shortClipsKeepTenPercentLimit() {
        for (double duration : new double[]{3, 5, 10, 12, 13}) {
            double limit = Math.min(1.3, duration * 0.1);
            LoopCounter accepted = new LoopCounter();
            assertEquals(1, accepted.observe(new Progress(limit, duration), "short", 1000).current);
            LoopCounter rejected = new LoopCounter();
            assertTrue(rejected.observe(new Progress(Math.nextUp(limit), duration), "short", 1000).waitingForStart);
        }
    }

    @Test public void lateAcceptedSeedStillRequiresARealCycleAndOnlyAdvancesOnce() {
        for (int target : new int[]{1, 2}) {
            LoopCounter counter = new LoopCounter();
            counter.setTarget(target);
            long at = 1000;
            assertEquals(1, counter.observe(new Progress(1.3, 46.066), "destination", at).current);
            for (int cycle = 0; cycle < target; cycle++) {
                double start = cycle == 0 ? 1.3 : 0;
                for (double position = start + 0.3; position < 46.066; position += 0.3) {
                    at += 300;
                    assertFalse(counter.observe(new Progress(position, 46.066), "destination", at).advance);
                }
                at += 300;
                LoopCounter.Result wrap = counter.observe(new Progress(0, 46.066), "destination", at);
                assertEquals(cycle == target - 1, wrap.advance);
            }
            assertFalse(counter.observe(new Progress(0.3, 46.066), "destination", at + 300).advance);
        }
    }

    @Test public void zeroRemainsOffAtNewStartLimit() {
        LoopCounter counter = new LoopCounter();
        counter.setTarget(0);
        LoopCounter.Result result = counter.observe(new Progress(1.3, 46.066), "destination", 1000);
        assertEquals(0, result.current);
        assertFalse(result.advance);
    }

    @Test public void failedTransitionRecoveryKeepsItsStricterStartGuard() {
        PlaybackRestart recovery = new PlaybackRestart();
        recovery.begin("host", 1);
        assertNull(recovery.observe(new Progress(1.103, 46.066), "destination", 1000));
        assertNull(recovery.observe(new Progress(1.403, 46.066), "destination", 1300));
        assertTrue(recovery.active());
    }
}
