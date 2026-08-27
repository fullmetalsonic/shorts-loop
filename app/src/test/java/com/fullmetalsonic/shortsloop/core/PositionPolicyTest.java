package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class PositionPolicyTest {
    @Test public void draggingCannotLeaveScreen() { assertEquals(0, PositionPolicy.clamp(-50, 200)); assertEquals(200, PositionPolicy.clamp(400, 200)); }
    @Test public void proportionalPositionSurvivesFoldResize() { assertEquals(400, PositionPolicy.restore(PositionPolicy.save(200, 400), 800)); }
    @Test public void oversizedOverlayStaysAtOrigin() { assertEquals(0, PositionPolicy.restore(0.7f, -20)); assertEquals(0, PositionPolicy.save(12, 0), 0); }
    @Test public void corruptStoredPositionIsClamped() { assertEquals(100, PositionPolicy.restore(9, 100)); assertEquals(0, PositionPolicy.restore(-9, 100)); assertEquals(50, PositionPolicy.restore(Float.NaN, 100)); }
}
