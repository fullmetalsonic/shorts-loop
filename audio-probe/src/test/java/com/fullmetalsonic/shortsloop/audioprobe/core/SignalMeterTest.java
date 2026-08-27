package com.fullmetalsonic.shortsloop.audioprobe.core;

import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.*;

public class SignalMeterTest {
    @Test public void emptyIsNotSilenceEvidence() {
        SignalMeter.Reading r = new SignalMeter().reading();
        assertEquals(0, r.samples()); assertEquals(-120, r.totalDbfs(), 0); assertFalse(r.signalDetected());
    }
    @Test public void digitalSilenceHasNoSignal() {
        SignalMeter m = new SignalMeter();
        for (int i = 0; i < 10; i++) m.accept(new short[1600], 1600);
        assertEquals(16000, m.reading().samples()); assertEquals(0, m.reading().signalBlocks());
        assertEquals(0, m.reading().nonZeroPercent(), 0); assertEquals(0, m.reading().peak());
    }
    @Test public void needsThreeSignalBlocks() {
        SignalMeter m = new SignalMeter(); short[] block = {1000, -1000};
        m.accept(block, 2); m.accept(block, 2); assertFalse(m.reading().signalDetected());
        m.accept(block, 2); assertTrue(m.reading().signalDetected());
    }
    @Test public void fullNegativeScaleDoesNotOverflow() {
        SignalMeter m = new SignalMeter(); m.accept(new short[]{Short.MIN_VALUE, Short.MIN_VALUE}, 2);
        assertEquals(32768, m.reading().peak()); assertEquals(0, m.reading().latestDbfs(), 0);
    }
    @Test public void halfScaleIsMinusSixDb() {
        SignalMeter m = new SignalMeter(); m.accept(new short[]{16384, -16384}, 2);
        assertEquals(-6.0206, m.reading().latestDbfs(), .0001);
    }
    @Test public void ignoresTailAfterPartialRead() {
        SignalMeter m = new SignalMeter(); m.accept(new short[]{0, 0, 32767}, 2);
        assertEquals(0, m.reading().peak()); assertEquals(2, m.reading().samples());
    }
    @Test public void doesNotRetainInputArray() {
        SignalMeter m = new SignalMeter(); short[] pcm = {1000, -1000}; m.accept(pcm, 2);
        Arrays.fill(pcm, (short) 0); assertEquals(1000, m.reading().peak());
        assertEquals(100, m.reading().nonZeroPercent(), 0);
    }
    @Test public void tinyNonZeroIsNotClearSignal() {
        SignalMeter m = new SignalMeter();
        for (int i = 0; i < 4; i++) m.accept(new short[]{1, -1}, 2);
        assertEquals(100, m.reading().nonZeroPercent(), 0); assertFalse(m.reading().signalDetected());
    }
    @Test public void weightedTotalAndZeroPercentage() {
        SignalMeter m = new SignalMeter(); m.accept(new short[]{16384, -16384}, 2); m.accept(new short[]{0, 0}, 2);
        assertEquals(-9.0309, m.reading().totalDbfs(), .0001); assertEquals(50, m.reading().nonZeroPercent(), 0);
    }
    @Test public void emptyReadDoesNotAddBlock() {
        SignalMeter m = new SignalMeter(); m.accept(new short[1], 0); assertEquals(0, m.reading().blocks());
    }
    @Test(expected = IllegalArgumentException.class) public void rejectsInvalidLength() { new SignalMeter().accept(new short[1], 2); }
    @Test(expected = IllegalArgumentException.class) public void rejectsNegativeLength() { new SignalMeter().accept(new short[1], -1); }
    @Test(expected = IllegalArgumentException.class) public void rejectsNull() { new SignalMeter().accept(null, 0); }
}
