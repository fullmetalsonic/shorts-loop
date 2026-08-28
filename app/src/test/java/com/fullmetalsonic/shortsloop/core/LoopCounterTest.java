package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class LoopCounterTest {
    private final LoopCounter counter = new LoopCounter();
    private long now = 1000;
    private LoopCounter.Result sample(double position) { now += 1000; return counter.observe(new Progress(position, 35), "video-A", now); }
    private void through(int from, int to) { for (int i = from; i <= to; i++) assertFalse("Unexpected advance at " + i, sample(i).advance); }
    @Test public void twoFullPlaysProduceOneAdvance() {
        through(0, 35); LoopCounter.Result first = sample(0); assertFalse(first.advance); assertEquals(2, first.current);
        through(1, 35); assertTrue(sample(0).advance); assertFalse(sample(1).advance); assertFalse(sample(0).advance);
    }
    @Test public void singlePlayMode() { counter.setTarget(1); through(0, 35); assertTrue(sample(0).advance); }
    @Test public void emittedCompletionRequiresSameFreshSourceAtDispatch() {
        counter.setTarget(1); through(0, 35); assertTrue(sample(0).advance);
        assertTrue(counter.permitsAdvance(new Progress(0, 35), "video-A", now));
        assertTrue(counter.permitsAdvance(new Progress(1, 35), "video-A", now + 500));
        assertFalse(counter.permitsAdvance(new Progress(0, 35), "video-B", now));
        assertFalse(counter.permitsAdvance(new Progress(0, 40), "video-A", now));
        assertFalse(counter.permitsAdvance(new Progress(0, 35), "video-A", now - 1));
        assertFalse(counter.permitsAdvance(new Progress(0, 35), "video-A", now + 1501));
        assertFalse(counter.permitsAdvance(new Progress(20, 35), "video-A", now + 500));
        counter.reset(); assertFalse(counter.permitsAdvance(new Progress(0, 35), "video-A", now));
    }
    @Test public void threeFullPlaysAreNotClampedToTwo() {
        counter.setTarget(3); through(0, 35);
        assertEquals(2, sample(0).current); through(1, 35);
        LoopCounter.Result second = sample(0); assertFalse(second.advance); assertEquals(3, second.current);
        through(1, 35); assertTrue(sample(0).advance);
    }
    @Test public void maximumCountAdvancesOnlyAfterNinetyNineFullPlays() {
        counter.setTarget(99); through(0, 35);
        for (int completed = 1; completed < 99; completed++) {
            LoopCounter.Result boundary = sample(0);
            assertFalse("Early advance after " + completed + " plays", boundary.advance);
            assertEquals(completed + 1, boundary.current);
            through(1, 35);
        }
        assertTrue(sample(0).advance); assertFalse(sample(1).advance);
    }
    @Test public void zeroNeverAdvances() { counter.setTarget(0); for (int i = 0; i < 100; i++) { LoopCounter.Result r = sample(i % 36); assertFalse(r.advance); assertEquals(0, r.current); } }
    @Test public void partialInitialPlayIsNotCounted() {
        counter.setTarget(1); assertEquals(0, sample(20).current); through(21, 35); assertFalse(sample(0).advance);
        through(1, 35); assertTrue(sample(0).advance);
    }
    @Test public void pauseDoesNotAdvance() {
        through(0, 20); for (int i = 0; i < 150; i++) assertFalse(sample(20).advance);
        through(21, 35); assertFalse(sample(0).advance);
    }
    @Test public void pauseAtEndDoesNotAdvanceUntilWrap() {
        counter.setTarget(1); through(0, 35); for (int i = 0; i < 10; i++) assertFalse(sample(35).advance); assertTrue(sample(0).advance);
    }
    @Test public void forwardSeekCannotFakeCompletion() {
        counter.setTarget(1); through(0, 4); assertFalse(sample(34).advance); assertFalse(sample(0).advance);
    }
    @Test public void backwardsSeekDiscardsCount() {
        through(0, 35); sample(0); through(1, 20); sample(5); through(6, 35); assertFalse(sample(0).advance);
    }
    @Test public void longObservationGapDiscardsCount() {
        counter.setTarget(1); through(0, 30); now += 5000; sample(35); assertFalse(sample(0).advance);
    }
    @Test public void identityChangeResetsEvenWithSameDuration() {
        through(0, 35); sample(0); through(1, 35); now += 1000;
        LoopCounter.Result r = counter.observe(new Progress(0, 35), "video-B", now); assertFalse(r.advance); assertEquals(1, r.current);
    }
    @Test public void changedDurationResets() {
        counter.setTarget(1); through(0, 35); now += 1000; assertFalse(counter.observe(new Progress(0, 40), "video-A", now).advance);
    }
    @Test public void resetCancelsPendingCount() { through(0, 35); sample(0); counter.reset(); through(0, 35); assertFalse(sample(0).advance); }
    @Test public void targetChangeResetsCount() { through(0, 35); sample(0); counter.setTarget(1); assertEquals(0, sample(10).current); }
    @Test public void unavailableSampleResets() {
        counter.setTarget(1); through(0, 35); now += 1000; counter.observe(null, "video-A", now); assertFalse(sample(0).advance);
    }
    @Test public void neverAdvanceBeforeNearEnd() { counter.setTarget(1); through(0, 25); assertFalse(sample(0).advance); }
    @Test public void supportsTwoTimesPlaybackRate() {
        counter.setTarget(1); for (int i = 0; i <= 34; i += 2) assertFalse(sample(i).advance); assertTrue(sample(0).advance);
    }
    @Test public void noCountFromUnobservedInitialEnd() { counter.setTarget(1); sample(35); assertFalse(sample(0).advance); }
    @Test public void missingLastOneSecondStillRecognizesWrap() { counter.setTarget(1); through(0, 34); assertTrue(sample(0).advance); }
    @Test public void frozenProgressCannotCountRepeatedWraps() { counter.setTarget(1); sample(0); sample(35); sample(0); sample(35); assertFalse(sample(0).advance); }
    @Test public void shortVideoSeekCannotBecomeA600MillisecondCompletion() {
        counter.setTarget(1);
        counter.observe(new Progress(0, 3), "short", 1000);
        counter.observe(new Progress(2, 3), "short", 1300);
        assertFalse(counter.observe(new Progress(0, 3), "short", 1600).advance);
    }
    @Test public void genuineThreeSecondVideoCanComplete() {
        counter.setTarget(1);
        counter.observe(new Progress(0, 3), "short", 1000);
        counter.observe(new Progress(1, 3), "short", 2000);
        counter.observe(new Progress(2, 3), "short", 3000);
        assertTrue(counter.observe(new Progress(0, 3), "short", 4000).advance);
    }
    @Test public void partialShortVideoStartsInWaitingState() {
        counter.setTarget(1); assertTrue(counter.observe(new Progress(1, 3), "short", 1000).waitingForStart);
    }
    @Test public void quantizedTwoSecondUpdatesAt300msPollingCompleteTwoPlays() {
        for (int cycle = 0; cycle < 2; cycle++) {
            for (int ms = 0; ms < 52000; ms += 300) {
                LoopCounter.Result result = counter.observe(new Progress(2 * Math.floor(ms / 2000.0), 52), "quantized", 1000L + cycle * 52200L + ms);
                assertFalse(result.advance);
                assertEquals(cycle + 1, result.current);
            }
        }
        assertTrue(counter.observe(new Progress(0, 52), "quantized", 105400).advance);
    }
}
