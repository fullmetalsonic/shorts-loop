package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class LongVideoPolicyTest {
    @Test public void defaultAndBounds() {
        assertEquals(60, LongVideoPolicy.DEFAULT_SECONDS);
        assertEquals(60, LongVideoPolicy.sanitizeSeconds(0)); assertEquals(60, LongVideoPolicy.sanitizeSeconds(3601));
        assertEquals(1, LongVideoPolicy.clamp(-1)); assertEquals(3600, LongVideoPolicy.clamp(5000));
    }
    @Test public void parsesIntegerAndFullWidthDrafts() {
        for (String value : new String[]{"60", " 60 ", "６０"}) assertEquals(Integer.valueOf(60), LongVideoPolicy.parseSeconds(value));
        assertEquals(Integer.valueOf(1), LongVideoPolicy.parseSeconds("1"));
        assertEquals(Integer.valueOf(3600), LongVideoPolicy.parseSeconds("3600"));
    }
    @Test public void rejectsEntireInvalidDraft() {
        for (String value : new String[]{null, "", "0", "-1", "+60", "60.0", "6a0", "3601", "9999999999", "1:00"})
            assertNull(value, LongVideoPolicy.parseSeconds(value));
    }
    @Test public void thresholdIsInclusiveNotWatchTime() {
        assertFalse(LongVideoPolicy.qualifies(true, new Progress(50, 59.9), 60));
        assertTrue(LongVideoPolicy.qualifies(true, new Progress(0, 60), 60));
        assertTrue(LongVideoPolicy.qualifies(true, new Progress(0, 60.1), 60));
    }
    @Test public void optionOffAndUnknownDurationCannotQualify() {
        assertFalse(LongVideoPolicy.qualifies(false, new Progress(0, 120), 60));
        for (Progress value : new Progress[]{null, new Progress(0, Double.NaN), new Progress(0, -1), new Progress(0, 3601)})
            assertFalse(LongVideoPolicy.qualifies(true, value, 60));
    }
    @Test public void maximumThresholdAndInvalidStoredThreshold() {
        assertTrue(LongVideoPolicy.qualifies(true, new Progress(0, 3600), 3600));
        assertFalse(LongVideoPolicy.qualifies(true, new Progress(0, 59), -10));
    }
    @Test public void zeroCountExplainsIndependentOptions() {
        for (int mask = 0; mask < 4; mask++) {
            boolean ads = (mask & 1) != 0, live = (mask & 2) != 0;
            assertEquals(LiveSkipPolicy.zeroCountStatus(ads, live), LongVideoPolicy.zeroCountStatus(ads, live, false));
            assertTrue(LongVideoPolicy.zeroCountStatus(ads, live, true).contains("긴 영상"));
        }
    }
}
