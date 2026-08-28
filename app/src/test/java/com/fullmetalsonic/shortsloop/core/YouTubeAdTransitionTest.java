package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;
import static com.fullmetalsonic.shortsloop.core.AdvanceGate.State.*;
import static com.fullmetalsonic.shortsloop.core.YouTubeAdTransition.Kind.*;

public class YouTubeAdTransitionTest {
    private static YouTubeAdTransition.Frame ad(String page, int row) {
        return new YouTubeAdTransition.Frame("window+bounds", "pager", page, row, AD, null);
    }
    private static YouTubeAdTransition.Frame video(String page, int row, double position) {
        return new YouTubeAdTransition.Frame("window+bounds", "pager", page, row, VIDEO, new Progress(position, 12));
    }
    private static YouTubeAdTransition start() {
        YouTubeAdTransition t = new YouTubeAdTransition(); t.begin(ad("A", 4), 0); return t;
    }
    @Test public void adToVideoRequiresFreshForwardAndBothSettlements() {
        YouTubeAdTransition t = start();
        assertEquals(WAITING, t.inspect(video("B", 5, .2), 600));
        assertEquals(WAITING, t.inspect(video("B", 5, .5), 900));
        assertEquals(CONFIRMED, t.inspect(video("B", 5, .8), 1200));
        assertEquals(IDLE, t.inspect(null, 1500));
    }
    @Test public void consecutiveAdNeedsStableIndependentNodeAndExactNextRow() {
        YouTubeAdTransition t = start();
        assertEquals(WAITING, t.inspect(ad("B", 5), 1100));
        assertEquals(WAITING, t.inspect(ad("B", 5), 1200));
        assertEquals(CONFIRMED, t.inspect(ad("B", 5), 1400));
    }
    @Test public void knownLiveCanConfirmButCannotBorrowVideoStability() {
        YouTubeAdTransition t = start(); t.inspect(video("B", 5, .2), 900);
        YouTubeAdTransition.Frame live = new YouTubeAdTransition.Frame("window+bounds", "pager", "B", 5, LIVE, null);
        assertEquals(WAITING, t.inspect(live, 1200));
        assertEquals(CONFIRMED, t.inspect(live, 1500));
    }
    @Test public void samePageRecycledNodeUnknownAndNonNextRowsCannotConfirm() {
        for (int row : new int[]{-2, -1, 3, 4, 6}) {
            YouTubeAdTransition t = start(); t.inspect(ad("B", row), 1000);
            assertEquals(WAITING, t.inspect(ad("B", row), 1400));
            assertEquals(FAILED, t.inspect(ad("B", row), 4500));
        }
        YouTubeAdTransition t = start(); t.inspect(ad("A", 5), 1000);
        assertEquals(WAITING, t.inspect(ad("A", 5), 1400));
    }
    @Test public void rollbackAndMissingEvidenceLoseCandidate() {
        for (YouTubeAdTransition.Frame rollback : new YouTubeAdTransition.Frame[]{ad("A", 4), null}) {
            YouTubeAdTransition t = start(); t.inspect(ad("B", 5), 900); t.inspect(rollback, 1100);
            assertEquals(WAITING, t.inspect(ad("B", 5), 1200));
            assertEquals(CONFIRMED, t.inspect(ad("B", 5), 1500));
        }
    }
    @Test public void changedWindowGeometryOrPagerCannotConfirm() {
        for (int i = 0; i < 2; i++) {
            YouTubeAdTransition t = start();
            YouTubeAdTransition.Frame bad = new YouTubeAdTransition.Frame(i == 0 ? "other" : "window+bounds",
                    i == 1 ? "other" : "pager", "B", 5, AD, null);
            t.inspect(bad, 900); assertEquals(WAITING, t.inspect(bad, 1400));
        }
    }
    @Test public void frozenClockBackwardClockAndSeekNeverConfirm() {
        for (double position : new double[]{.2, .1, 10}) {
            YouTubeAdTransition t = start(); t.inspect(video("B", 5, .2), 900);
            assertEquals(WAITING, t.inspect(video("B", 5, position), 1200));
        }
    }
    @Test public void durationChangeAndObservationGapRestartStability() {
        YouTubeAdTransition t = start(); t.inspect(video("B", 5, .2), 600);
        assertEquals(WAITING, t.inspect(video("B", 5, .6), 2300));
        assertEquals(CONFIRMED, t.inspect(video("B", 5, .9), 2600));
        t = start(); t.inspect(video("B", 5, .2), 900);
        assertEquals(WAITING, t.inspect(new YouTubeAdTransition.Frame("window+bounds", "pager", "B", 5,
                VIDEO, new Progress(.5, 20)), 1200));
    }
    @Test public void deadlineTimeReversalAndCancelAreTerminal() {
        YouTubeAdTransition t = start(); assertEquals(FAILED, t.inspect(ad("B", 5), -1));
        t = start(); t.inspect(ad("B", 5), 4200); assertEquals(FAILED, t.inspect(ad("B", 5), 4500));
        t = start(); t.cancel(); assertFalse(t.pending()); assertEquals(IDLE, t.inspect(ad("B", 5), 1500));
    }
    @Test public void unknownRequestOrVideoSourceIsNotAnAdRequest() {
        for (YouTubeAdTransition.Frame frame : new YouTubeAdTransition.Frame[]{null, ad("A", -1), ad("", 4), video("A", 4, 0)}) {
            try { new YouTubeAdTransition().begin(frame, 0); fail("Must reject unsafe source"); }
            catch (IllegalArgumentException expected) { }
        }
    }
}
