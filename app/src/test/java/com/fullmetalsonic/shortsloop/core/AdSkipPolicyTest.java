package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class AdSkipPolicyTest {
    @Test public void allFourControlsAreRequired() {
        for (int mask = 0; mask < 16; mask++) {
            assertEquals(mask == 15, AdSkipPolicy.enabled((mask & 1) != 0, (mask & 2) != 0 ? 2 : 0,
                    (mask & 4) != 0, (mask & 8) != 0));
        }
    }
    @Test public void anyPositiveSupportedCountUsesTheSameAdRule() {
        for (int count = 1; count <= 99; count++) assertTrue(AdSkipPolicy.enabled(true, count, true, true));
        assertFalse(AdSkipPolicy.enabled(true, -1, true, true));
    }
}
