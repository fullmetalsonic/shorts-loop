package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class ClocklessTimeoutPolicyTest {
    @Test public void storedSecondsUseTenOnlyForInvalidValues() {
        for (int value = 5; value <= 60; value++) assertEquals(value, ClocklessTimeoutPolicy.sanitizeSeconds(value));
        for (int value : new int[]{Integer.MIN_VALUE, -1, 0, 1, 4, 61, 120, Integer.MAX_VALUE})
            assertEquals(10, ClocklessTimeoutPolicy.sanitizeSeconds(value));
    }
    @Test public void integerInputAcceptsTheInclusiveBounds() {
        assertEquals(Integer.valueOf(5), ClocklessTimeoutPolicy.parseSeconds("5"));
        assertEquals(Integer.valueOf(15), ClocklessTimeoutPolicy.parseSeconds(" 15 "));
        assertEquals(Integer.valueOf(60), ClocklessTimeoutPolicy.parseSeconds("60"));
        assertEquals(Integer.valueOf(15), ClocklessTimeoutPolicy.parseSeconds("0015"));
    }
    @Test public void compatibilityDigitsAndSpacesAreNormalized() {
        assertEquals(Integer.valueOf(15), ClocklessTimeoutPolicy.parseSeconds("　１５　"));
        assertEquals(Integer.valueOf(60), ClocklessTimeoutPolicy.parseSeconds("６０"));
        assertEquals(Integer.valueOf(5), ClocklessTimeoutPolicy.parseSeconds("\u00a05\u00a0"));
    }
    @Test public void unfinishedOutOfRangeAndOverflowingInputIsRejected() {
        for (String value : new String[]{null, "", " ", "0", "4", "61", "120", "2147483648", "99999999999999999999999999999"})
            assertNull(value, ClocklessTimeoutPolicy.parseSeconds(value));
    }
    @Test public void signsDecimalsUnitsAndInternalSpacesAreNotIntegers() {
        for (String value : new String[]{"+15", "-15", "＋１５", "－１５", "15.0", "１５．０", "1 5", "15초", "1e1", "١٥"})
            assertNull(value, ClocklessTimeoutPolicy.parseSeconds(value));
    }
    @Test public void enabledRequiresEveryUserControlledGate() {
        for (int mask = 0; mask < 16; mask++) {
            boolean execution = (mask & 1) != 0, positiveTarget = (mask & 2) != 0;
            boolean instagram = (mask & 4) != 0, optedIn = (mask & 8) != 0;
            assertEquals(mask == 15, ClocklessTimeoutPolicy.enabled(execution, positiveTarget ? 2 : 0, instagram, optedIn));
        }
        assertTrue(ClocklessTimeoutPolicy.enabled(true, 1, true, true));
        assertTrue(ClocklessTimeoutPolicy.enabled(true, 99, true, true));
    }
    @Test public void invalidRepeatCountsNeverEnableTimeout() {
        for (int count : new int[]{-1, 0, 100, Integer.MAX_VALUE})
            assertFalse(ClocklessTimeoutPolicy.enabled(true, count, true, true));
    }
}
