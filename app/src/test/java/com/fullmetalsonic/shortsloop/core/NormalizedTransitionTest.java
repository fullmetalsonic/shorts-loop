package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class NormalizedTransitionTest {
    private NormalizedTransition.Frame frame(String scope, String pager, String page, String media, int index, double fraction) {
        return new NormalizedTransition.Frame(scope, pager, page, media, index, fraction);
    }
    private NormalizedTransition started() {
        NormalizedTransition tracker = new NormalizedTransition();
        tracker.begin(frame("scope", "pager", "A", "mediaA", 7, .02), 1000); return tracker;
    }
    @Test public void distinctPageAndMediaWithFreshForwardConfirmOnce() {
        NormalizedTransition t = started();
        assertEquals(AdvanceGate.State.WAITING, t.inspect(frame("scope","pager","B","mediaB",8,.02),1800));
        assertEquals(AdvanceGate.State.CONFIRMED,t.inspect(frame("scope","pager","B","mediaB",8,.04),2200));
        assertEquals(AdvanceGate.State.IDLE,t.inspect(frame("scope","pager","C","mediaC",9,.06),2600));
    }
    @Test public void unsafeOrSameEvidenceNeverConfirms() {
        for (NormalizedTransition.Frame f : new NormalizedTransition.Frame[]{null,
                frame("other","pager","B","mediaB",8,.1),frame("scope","other","B","mediaB",8,.1),
                frame("scope","pager","A","mediaB",8,.1),frame("scope","pager","B","mediaA",8,.1),
                frame("scope","pager","B","mediaB",7,.1),frame("scope","pager","B","mediaB",6,.1),
                frame("scope","pager","B","mediaB",9,.1),frame("scope","pager","B","",8,.1),
                frame("scope","pager","B","mediaB",-1,.1),
                frame("scope","pager","B","mediaB",8,Double.NaN)}) {
            NormalizedTransition t=started(); assertEquals(AdvanceGate.State.WAITING,t.inspect(f,2300));
            assertEquals(AdvanceGate.State.FAILED,t.inspect(f,5500)); assertFalse(t.pending());
        }
    }
    @Test public void rollbackClearsEvidenceAndCannotLatchIndex() {
        NormalizedTransition t=started();
        t.inspect(frame("scope","pager","B","mediaB",8,.02),1800);
        t.inspect(frame("scope","pager","A","mediaA",7,.03),2200);
        assertEquals(AdvanceGate.State.WAITING,t.inspect(frame("scope","pager","B","mediaB",8,.04),2300));
        assertEquals(AdvanceGate.State.WAITING,t.inspect(frame("scope","pager","B","mediaB",8,.05),2400));
        assertEquals(AdvanceGate.State.CONFIRMED,t.inspect(frame("scope","pager","B","mediaB",8,.06),2600));
    }
    @Test public void pausedDestinationCannotConfirm() {
        NormalizedTransition t=started();
        for(long now=1300;now<5500;now+=300)
            assertEquals(AdvanceGate.State.WAITING,t.inspect(frame("scope","pager","B","mediaB",8,.02),now));
        assertEquals(AdvanceGate.State.FAILED,t.inspect(null,5500));
    }
    @Test public void missingIndexAloneIsNotAChangeAndDeadlineAlwaysWins() {
        NormalizedTransition t=started();
        assertEquals(AdvanceGate.State.WAITING,t.inspect(frame("scope","pager","A","mediaA",-1,.02),2000));
        t.inspect(frame("scope","pager","B","mediaB",-1,.02),5200);
        assertEquals(AdvanceGate.State.FAILED,t.inspect(frame("scope","pager","B","mediaB",-1,.03),5500));
    }
    @Test public void invalidRequestRejectedAndCancellationIsReadOnly() {
        NormalizedTransition t=new NormalizedTransition();
        try {t.begin(null,1000);fail();}catch(IllegalArgumentException expected){}
        t=started();t.cancel();assertFalse(t.pending());assertEquals(AdvanceGate.State.IDLE,t.inspect(null,6000));
    }
    @Test public void unknownRequestIndexCanUseIndependentKeysButNeverDuration() {
        NormalizedTransition t=new NormalizedTransition();
        t.begin(frame("scope","pager","A","mediaA",-1,.02),1000);
        t.inspect(frame("scope","pager","B","mediaB",-1,.02),1800);
        assertEquals(AdvanceGate.State.CONFIRMED,t.inspect(frame("scope","pager","B","mediaB",-1,.04),2200));
    }
}
