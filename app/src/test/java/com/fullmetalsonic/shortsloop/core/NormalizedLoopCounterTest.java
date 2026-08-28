package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class NormalizedLoopCounterTest {
    private final NormalizedLoopCounter counter = new NormalizedLoopCounter();
    private long time;
    private LoopCounter.Result sample(double value) { time += 300; return counter.observe(new NormalizedProgress(value), "page-A", time); }
    private LoopCounter.Result cycle() {
        sample(0);
        for (int i = 1; i <= 19; i++) assertFalse(sample(i * .05).advance);
        return sample(0);
    }
    @Test public void oneFullObservedCycleEmitsExactlyOnce() {
        counter.setTarget(1);
        assertTrue(cycle().advance);
        for (int i = 0; i < 80; i++) assertFalse(sample((i % 20) * .05).advance);
    }
    @Test public void twoCyclesRequireTwoActualBoundaries() {
        counter.setTarget(2);
        assertFalse(cycle().advance);
        for (int i = 1; i <= 19; i++) assertFalse(sample(i * .05).advance);
        assertTrue(sample(0).advance);
    }
    @Test public void lateSeedWaitsForNextWholeCycle() {
        counter.setTarget(1);
        assertTrue(sample(.5).waitingForStart);
        for (int i = 11; i <= 19; i++) sample(i * .05);
        assertFalse(sample(0).advance);
        for (int i = 1; i <= 19; i++) sample(i * .05);
        assertTrue(sample(0).advance);
    }
    @Test public void seedToleranceIsUnitlessThreePercentNotSeconds() {
        assertEquals(1, sample(.03).current);
        counter.reset(); assertEquals(0, sample(.03001).current);
    }
    @Test public void zeroDisablesAndClearsCounter() {
        counter.setTarget(0);
        assertFalse(cycle().advance); assertEquals(0, sample(0).current);
    }
    @Test public void largeForwardSeekDiscardsCycle() {
        counter.setTarget(1); sample(0); sample(.05); sample(.1);
        assertEquals(0, sample(.8).current);
        sample(.85); sample(.9); sample(.95); assertFalse(sample(0).advance);
    }
    @Test public void backwardSeekDiscardsCycle() {
        counter.setTarget(1); sample(0); sample(.05); sample(.1); sample(.15); sample(.2);
        assertEquals(0, sample(.1).current);
        for (int i = 3; i <= 19; i++) sample(i * .05);
        assertFalse(sample(0).advance);
    }
    @Test public void smallButImplausiblyAcceleratedSeekIsRejected() {
        sample(.1); sample(.101); sample(.102);
        sample(.2); assertEquals("jump", counter.diagnostic());
    }
    @Test public void missingRangeClearsCoverageAndCompletedCounts() {
        counter.setTarget(2); cycle();
        counter.observe(null, "page-A", time += 300);
        assertFalse(cycle().advance);
    }
    @Test public void observationGapDiscardsCycle() {
        counter.setTarget(1); sample(0); sample(.05); sample(.1);
        time += 1600; assertEquals(0, sample(.15).current);
        for (int i = 4; i <= 19; i++) sample(i * .05);
        assertFalse(sample(0).advance);
    }
    @Test public void timestampRollbackClears() {
        sample(.01); sample(.05);
        assertEquals(0, counter.observe(new NormalizedProgress(.1), "page-A", time - 1).current);
    }
    @Test public void stallDoesNotTurnIntoACompletedCycle() {
        counter.setTarget(1); sample(0); sample(.05); sample(.1);
        for (int i = 0; i < 6; i++) assertFalse(sample(.1).advance);
        assertEquals(0, sample(.15).current);
    }
    @Test public void identityChangeAndRollbackCannotCarryCounts() {
        counter.setTarget(2); cycle();
        assertEquals(0, counter.observe(new NormalizedProgress(.5), "page-B", time += 300).current);
        assertFalse(cycle().advance);
    }
    @Test public void unknownIdentityCannotSeed() {
        assertEquals(0, counter.observe(new NormalizedProgress(0), "", 300).current);
        assertEquals(0, counter.observe(new NormalizedProgress(0), null, 600).current);
    }
    @Test public void invalidFractionsCannotSeed() {
        for (double value : new double[]{Double.NaN, Double.POSITIVE_INFINITY, -.1, 1.1})
            assertEquals(0, sample(value).current);
    }
    @Test public void tooSparseAndFastCycleIsNotClaimedComplete() {
        counter.setTarget(1);
        for (int i = 0; i <= 10; i++) counter.observe(new NormalizedProgress(i * .1), "page-A", i * 100 + 1);
        assertFalse(counter.observe(new NormalizedProgress(0), "page-A", 1101).advance);
    }
    @Test public void rangeConversionNeverInventsDuration() {
        assertEquals(.5754, NormalizedProgress.fromTikTokRange(0, 0, 10000, 5754).fraction, .000001);
        assertNull(NormalizedProgress.fromTikTokRange(0, 0, 100, 10));
        assertNull(NormalizedProgress.fromTikTokRange(1, 0, 10000, 10));
        assertNull(NormalizedProgress.fromTikTokRange(0, 1, 10000, 10));
        assertNull(NormalizedProgress.fromTikTokRange(0, 0, 10000, Double.NaN));
        assertNull(NormalizedProgress.fromTikTokRange(0, 0, 10000, 10001));
    }
    @Test public void finalAdvanceRequiresAnEmittedCycleAndDoesNotMutateIt() {
        counter.setTarget(1); sample(0);
        assertFalse(counter.pendingAdvance());
        assertFalse(counter.permitsAdvance(new NormalizedProgress(0), "page-A", time));
        assertTrue(cycle().advance); assertTrue(counter.pendingAdvance());
        assertTrue(counter.permitsAdvance(new NormalizedProgress(0), "page-A", time));
        assertTrue(counter.permitsAdvance(new NormalizedProgress(.05), "page-A", time + 300));
        assertTrue(counter.permitsAdvance(new NormalizedProgress(.05), "page-A", time + 300));
        assertTrue(counter.pendingAdvance());
    }
    @Test public void deferredAdvanceRejectsSeekGapOtherPageAndMissingValues() {
        counter.setTarget(1); assertTrue(cycle().advance);
        assertFalse(counter.permitsAdvance(new NormalizedProgress(.8), "page-A", time + 300));
        assertFalse(counter.permitsAdvance(new NormalizedProgress(.02), "page-A", time + 1501));
        assertFalse(counter.permitsAdvance(new NormalizedProgress(.02), "page-B", time + 300));
        assertFalse(counter.permitsAdvance(null, "page-A", time + 300));
        assertFalse(counter.permitsAdvance(new NormalizedProgress(.01), "page-A", time));
        assertFalse(counter.permitsAdvance(new NormalizedProgress(0), "page-A", time - 1));
    }
    @Test public void finalAdvanceRejectsRollbackEvenInsideStartWindow() {
        counter.setTarget(1); sample(0);
        for (int i = 1; i <= 19; i++) sample(i * .05);
        assertTrue(sample(.02).advance);
        assertFalse(counter.permitsAdvance(new NormalizedProgress(.01), "page-A", time + 300));
        counter.reset(); assertFalse(counter.pendingAdvance());
        assertFalse(counter.permitsAdvance(new NormalizedProgress(.02), "page-A", time + 300));
    }
}
