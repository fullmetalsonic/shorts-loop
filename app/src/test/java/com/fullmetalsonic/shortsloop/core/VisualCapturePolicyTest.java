package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class VisualCapturePolicyTest {
    @Test public void currentFreshFrameAccepted() { assertTrue(VisualCapturePolicy.accepts(2, 2, 1000, 1020, 1050, 500)); }
    @Test public void resetOrNewSessionRejectsOldCallback() { assertFalse(VisualCapturePolicy.accepts(2, 3, 1000, 1020, 1050, 500)); }
    @Test public void duplicateAndReverseFramesRejected() {
        assertFalse(VisualCapturePolicy.accepts(2, 2, 1000, 1020, 1050, 1020));
        assertFalse(VisualCapturePolicy.accepts(2, 2, 1000, 1020, 1050, 1100));
    }
    @Test public void delayedResultRejected() { assertFalse(VisualCapturePolicy.accepts(2, 2, 1000, 1020, 3000, 500)); }
    @Test public void OldOrFutureTimestampRejected() {
        assertFalse(VisualCapturePolicy.accepts(2, 2, 1000, 500, 1050, 100));
        assertFalse(VisualCapturePolicy.accepts(2, 2, 1000, 1200, 1050, 100));
    }
}
