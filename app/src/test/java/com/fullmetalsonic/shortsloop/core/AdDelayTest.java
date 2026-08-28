package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public final class AdDelayTest {
    @Test public void allHundredTenthsRoundTripWithoutFloatingPoint() {
        for (int i = 0; i <= 99; i++) assertEquals(Integer.valueOf(i), AdDelayPolicy.parseTenths(AdDelayPolicy.format(i)));
        assertEquals(Integer.valueOf(0), AdDelayPolicy.parseTenths("0"));
        assertEquals(Integer.valueOf(90), AdDelayPolicy.parseTenths("9"));
        assertEquals(Integer.valueOf(13), AdDelayPolicy.parseTenths("　１．３　"));
    }
    @Test public void malformedEditsNeverBecomeASavedDelay() {
        for (String value : new String[]{null,""," ","10","10.0","9.99",".1","1.","-1","+1","1,3","NaN","1e1","9999999999999","1 3"})
            assertNull(value, AdDelayPolicy.parseTenths(value));
        for (int value : new int[]{-1,100,Integer.MIN_VALUE,Integer.MAX_VALUE}) assertEquals(0,AdDelayPolicy.sanitize(value));
    }
    @Test public void everyDelayHasAnExactDeadlineAndSinglePulse() {
        for (int tenths = 0; tenths <= 99; tenths++) {
            AdDelayTracker tracker = new AdDelayTracker();
            long start = 100, end = start + tenths * 100L;
            if (tenths == 0) assertTrue(tracker.observe("ad-a",tenths,start).due());
            else {
                for (long t=start;t<end;t+=100) assertFalse(tracker.observe("ad-a",tenths,t).due());
                assertFalse(tracker.observe("ad-a",tenths,end-1).due());
                assertEquals(1,tracker.remainingMillis());
                assertTrue(tracker.observe("ad-a",tenths,end).due());
            }
            assertFalse(tracker.active());
            assertFalse(tracker.observe("ad-a",0,end+10000).due());
        }
    }
    @Test public void changedPageAndSettingStartTheirOwnDeadlines() {
        AdDelayTracker tracker = new AdDelayTracker();
        tracker.observe("ad-a",10,0); tracker.observe("ad-a",10,500);
        assertEquals(1000,tracker.observe("ad-b",10,600).remainingMillis());
        assertFalse(tracker.observe("ad-b",10,1500).due());
        assertEquals(2000,tracker.observe("ad-b",20,1600).remainingMillis());
        assertFalse(tracker.observe("ad-b",20,2600).due());
        assertTrue(tracker.observe("ad-b",20,3600).due());
    }
    @Test public void gapsUnsafePagesAndClockRollbackDoNotCountUnobservedTime() {
        AdDelayTracker tracker = new AdDelayTracker();
        tracker.observe("ad-a",10,0);
        assertEquals(1000,tracker.observe("ad-a",10,1501).remainingMillis());
        assertEquals(1000,tracker.observe("ad-a",10,1000).remainingMillis());
        for (String unsafe : new String[]{null,""," "}) {
            assertFalse(tracker.observe(unsafe,0,1100).due()); assertFalse(tracker.active());
        }
        assertFalse(tracker.observe("ad-a",0,-1).due());
    }
    @Test public void explicitResetAndDistinctHostsRemainIndependent() {
        AdDelayTracker a=new AdDelayTracker(),b=new AdDelayTracker();
        a.observe("same-key",10,0); b.observe("same-key",20,0);
        assertTrue(a.observe("same-key",10,1000).due()); assertFalse(b.observe("same-key",20,1000).due());
        a.reset(); assertFalse(a.observe("same-key",10,1100).due());
        assertTrue(b.observe("same-key",20,2000).due());
        assertTrue(a.observe("same-key",10,2100).due());
    }
    @Test public void nearLongMaxDoesNotOverflowAndZeroHasNoExtraQualification() {
        AdDelayTracker tracker = new AdDelayTracker();
        assertFalse(tracker.observe("ad-a",99,Long.MAX_VALUE-1).due());
        assertEquals(9899,tracker.observe("ad-a",99,Long.MAX_VALUE).remainingMillis());
        assertTrue(new AdDelayTracker().observe("safe-settled-ad",0,0).due());
    }
}
