package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class ModePolicyTest {
    @Test public void cycleIsZeroOneTwoZero() { assertEquals(1, ModePolicy.next(0)); assertEquals(2, ModePolicy.next(1)); assertEquals(0, ModePolicy.next(2)); }
    @Test public void invalidModeUsesSafeDefault() {
        assertEquals(2, ModePolicy.sanitize(-1)); assertEquals(2, ModePolicy.sanitize(100));
        assertEquals(2, ModePolicy.sanitize(Integer.MAX_VALUE)); assertEquals(2, ModePolicy.sanitize(Integer.MIN_VALUE));
    }
    @Test public void allSupportedCountsArePreserved() { for (int count = 0; count <= 99; count++) assertEquals(count, ModePolicy.sanitize(count)); }
    @Test public void resumeNeverChoosesZero() {
        assertEquals(2, ModePolicy.resume(0)); assertEquals(1, ModePolicy.resume(1)); assertEquals(2, ModePolicy.resume(2));
        assertEquals(99, ModePolicy.resume(99)); assertEquals(2, ModePolicy.resume(100));
    }
    @Test public void runningTileIsActiveImmediately() { assertTrue(ModePolicy.tileActive(true, 2, true, false)); }
    @Test public void zeroPausesAdvancementWithoutStoppingControl() { assertTrue(ModePolicy.tileActive(true, 0, true, false)); }
    @Test public void offAndDisconnectedAndFaultAreInactive() {
        assertFalse(ModePolicy.tileActive(false, 2, true, false));
        assertFalse(ModePolicy.tileActive(true, 2, false, false)); assertFalse(ModePolicy.tileActive(true, 2, true, true));
    }
    @Test public void rotaryVisitsEveryCountBelowConfiguredCeiling() {
        for (int ceiling = 1; ceiling <= 99; ceiling++) {
            int target = 0;
            for (int expected = 1; expected <= ceiling; expected++) {
                target = ModePolicy.next(target, ceiling, ModePolicy.ROTARY);
                assertEquals(expected, target);
            }
            assertEquals(0, ModePolicy.next(target, ceiling, ModePolicy.ROTARY));
        }
    }
    @Test public void toggleOnlyChoosesZeroOrCeiling() {
        for (int ceiling = 1; ceiling <= 99; ceiling++) {
            assertEquals(ceiling, ModePolicy.next(0, ceiling, ModePolicy.TOGGLE));
            for (int current = 1; current <= ceiling; current++) assertEquals(0, ModePolicy.next(current, ceiling, ModePolicy.TOGGLE));
        }
    }
    @Test public void zeroCeilingCannotBeEnabledByEitherTapMode() {
        assertEquals(0, ModePolicy.next(0, 0, ModePolicy.ROTARY));
        assertEquals(0, ModePolicy.next(0, 0, ModePolicy.TOGGLE));
        assertEquals(0, ModePolicy.next(99, 0, ModePolicy.ROTARY));
    }
    @Test public void activeTargetIsBoundedWithoutChangingCeiling() {
        assertEquals(5, ModePolicy.clampTarget(99, 5));
        assertEquals(0, ModePolicy.clampTarget(99, 0));
        assertEquals(2, ModePolicy.clampTarget(-1, 5));
        assertEquals(0, ModePolicy.next(99, 5, ModePolicy.ROTARY));
    }
    @Test public void unknownTapModesUseRotary() {
        assertEquals(ModePolicy.ROTARY, ModePolicy.sanitizeTapMode(-1));
        assertEquals(ModePolicy.ROTARY, ModePolicy.sanitizeTapMode(2));
        assertEquals(2, ModePolicy.next(1, 5, 7));
    }
}
