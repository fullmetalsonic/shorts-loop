package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class AdSkipPolicyTest {
    @Test public void allThreeIndependentAdControlsAreRequired() {
        for (int mask = 0; mask < 8; mask++) {
            assertEquals(mask == 7, AdSkipPolicy.enabled((mask & 1) != 0,
                    (mask & 2) != 0, (mask & 4) != 0));
        }
    }
    @Test public void mainExecutionOffStillStopsTheIndependentAdOption() {
        assertTrue(AdSkipPolicy.enabled(true, true, true));
        assertFalse(AdSkipPolicy.enabled(false, true, true));
    }
}
