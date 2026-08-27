package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class ProgressParserTest {
    private void check(String text, double position, double duration) {
        Progress p = ProgressParser.parse(text); assertNotNull(text, p); assertEquals(position, p.position, 0); assertEquals(duration, p.duration, 0);
    }
    @Test public void observedKoreanDescription() { check("0분 35초 중 0분 8초", 8, 35); }
    @Test public void observedEndAndWrap() { check("0분 35초 중 0분 35초", 35, 35); check("0분 35초 중 0분 0초", 0, 35); }
    @Test public void minutesAndHours() { check("1시간 0분 0초 중 2분 3초", 123, 3600); }
    @Test public void shortKoreanUnits() { check("35초 중 8초", 8, 35); }
    @Test public void englishDescription() { check("8 seconds of 35 seconds", 8, 35); }
    @Test public void englishMinutesAndComma() { check("1 minute, 2 seconds of 2 minutes, 5 seconds", 62, 125); }
    @Test public void clockDescriptions() { check("0:08 of 0:35", 8, 35); check("00:08 / 00:35", 8, 35); }
    @Test public void noGuessingOnUnknownText() { assertNull(ProgressParser.parse("50%")); assertNull(ProgressParser.parse("재생")); assertNull(ProgressParser.parse(null)); }
    @Test public void invalidRangesFailClosed() { assertNull(ProgressParser.parse("0:40 / 0:35")); assertNull(ProgressParser.parse("0:00 / 0:00")); assertNull(ProgressParser.parse("0:00 / 0:02")); }
    @Test public void malformedTimeFailsClosed() { assertNull(ProgressParser.parse("0:99 / 2:00")); assertNull(ProgressParser.parse("중")); assertNull(ProgressParser.parse("1:00 / 2:00 / 3:00")); }
    @Test public void hugeNumbersFailClosed() { assertNull(ProgressParser.parse("999999999999999999999999시간 중 0초")); }
    @Test public void invalidNumericProgress() { assertFalse(new Progress(Double.NaN, 35).valid()); assertFalse(new Progress(2, Double.POSITIVE_INFINITY).valid()); }
}
