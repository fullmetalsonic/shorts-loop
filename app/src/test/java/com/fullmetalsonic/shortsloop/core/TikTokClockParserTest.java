package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class TikTokClockParserTest {
    @Test public void exactRealClockCanProvideSecondsWithoutRangeConversion() {
        TikTokClockParser.Result result = TikTokClockParser.parse("00:03 / 01:30", null, null);
        assertFalse(result.contradictory()); assertEquals(3, result.progress().position, 0);
        assertEquals(90, result.progress().duration, 0);
        assertEquals(3600, TikTokClockParser.parse(null, "00:00:03 / 01:00:00", null).progress().duration, 0);
    }
    @Test public void normalizedNumbersAndArbitraryCaptionNeverBecomeSeconds() {
        for (String value : new String[]{"2500 / 10000", "25%", "great 00:03 / 01:30", "00:03", "2 / 5"})
            assertNull(TikTokClockParser.parse(value, null, null).progress());
    }
    @Test public void conflictingOrInvalidExactClocksAreRejected() {
        assertTrue(TikTokClockParser.parse("00:03 / 00:20", "00:04 / 00:20", null).contradictory());
        assertTrue(TikTokClockParser.parse("00:03 / 00:20", "00:03 / 00:30", null).contradictory());
        assertTrue(TikTokClockParser.parse("00:70 / 01:00", null, null).contradictory());
        assertTrue(TikTokClockParser.parse("00:10 / 00:05", null, null).contradictory());
    }
    @Test public void repeatedExactClockSourcesAgreeAndEmptyIsUnknown() {
        assertNotNull(TikTokClockParser.parse("00:01 / 00:05", "00:01 / 00:05", "00:01 / 00:05").progress());
        assertNull(TikTokClockParser.parse(null, null, null).progress());
        assertFalse(TikTokClockParser.parse(null, null, null).contradictory());
    }
}
