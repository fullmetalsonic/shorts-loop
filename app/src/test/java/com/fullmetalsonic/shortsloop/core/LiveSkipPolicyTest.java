package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class LiveSkipPolicyTest {
    @Test public void defaultIsImmediateAndRangeIsIndependentOfInstagram() {
        assertEquals(0, LiveSkipPolicy.DEFAULT_SECONDS);
        assertEquals(0, LiveSkipPolicy.MIN_SECONDS); assertEquals(60, LiveSkipPolicy.MAX_SECONDS);
        assertEquals(5, ClocklessTimeoutPolicy.MIN_SECONDS); assertEquals(10, ClocklessTimeoutPolicy.DEFAULT_SECONDS);
    }
    @Test public void everySupportedIntegerParsesWithoutClamping() {
        for (int seconds = 0; seconds <= 60; seconds++)
            assertEquals(Integer.valueOf(seconds), LiveSkipPolicy.parseSeconds(Integer.toString(seconds)));
    }
    @Test public void compatibilityDigitsAndOuterWhitespaceAreAccepted() {
        assertEquals(Integer.valueOf(0), LiveSkipPolicy.parseSeconds(" ０ "));
        assertEquals(Integer.valueOf(60), LiveSkipPolicy.parseSeconds("６０"));
        assertEquals(Integer.valueOf(5), LiveSkipPolicy.parseSeconds("005"));
    }
    @Test public void incompleteAndInvalidDraftsAreNotSavedAsZero() {
        for (String invalid : new String[]{"", " ", "-1", "+1", "1.0", "1,0", "1 0", "61", "120", "abc", "1秒"})
            assertNull(invalid, LiveSkipPolicy.parseSeconds(invalid));
        assertNull(LiveSkipPolicy.parseSeconds(null));
    }
    @Test public void overflowingDraftIsRejected() {
        assertNull(LiveSkipPolicy.parseSeconds("9999999999999999999999999999999999999999999999"));
    }
    @Test public void clampingIsBoundedEvenAtIntegerExtremes() {
        assertEquals(0, LiveSkipPolicy.clamp(Integer.MIN_VALUE)); assertEquals(60, LiveSkipPolicy.clamp(Integer.MAX_VALUE));
        assertEquals(0, LiveSkipPolicy.clamp(-1)); assertEquals(60, LiveSkipPolicy.clamp(61));
        assertEquals(5, LiveSkipPolicy.clamp(5));
    }
    @Test public void corruptSavedNumbersRecoverToDefaultZero() {
        for (int invalid : new int[]{Integer.MIN_VALUE, -1, 61, Integer.MAX_VALUE}) assertEquals(0, LiveSkipPolicy.sanitizeSeconds(invalid));
        for (int valid = 0; valid <= 60; valid++) assertEquals(valid, LiveSkipPolicy.sanitizeSeconds(valid));
    }
    @Test public void allThreeIndependentGatesMustBeOn() {
        for (int flags = 0; flags < 8; flags++) {
            assertEquals(flags == 7, LiveSkipPolicy.enabled((flags & 1) != 0, (flags & 2) != 0, (flags & 4) != 0));
        }
    }
    @Test public void onlyExactLiveRuntimeStatesAreActive() {
        assertTrue(LiveSkipPolicy.isLiveStatus(LiveSkipPolicy.STATUS_IMMEDIATE));
        assertTrue(LiveSkipPolicy.isLiveStatus(LiveSkipPolicy.STATUS_DELAYED));
        assertTrue(LiveSkipPolicy.isLiveStatus(LiveSkipPolicy.STATUS_CONFIRMING));
        for (String invalid : new String[]{null, "", "라이브", "라이브 · 다음 영상 확인 중 실패", "라이브 · 넘김 확인 실패", "라이브 일시정지"})
            assertFalse(LiveSkipPolicy.isLiveStatus(invalid));
    }
    @Test public void immediateAndElapsedLiveShowLiveNotZeroSeconds() {
        assertEquals("라이브", LiveSkipPolicy.floatingLabel(LiveSkipPolicy.STATUS_IMMEDIATE, 0));
        assertEquals("라이브", LiveSkipPolicy.floatingLabel(LiveSkipPolicy.STATUS_DELAYED, 0));
        assertEquals("라이브", LiveSkipPolicy.floatingLabel(LiveSkipPolicy.STATUS_IMMEDIATE, -1));
    }
    @Test public void liveDelayShowsRemainingSeconds() {
        assertEquals("5초", LiveSkipPolicy.floatingLabel(LiveSkipPolicy.STATUS_DELAYED, 5));
        assertEquals("60초", LiveSkipPolicy.floatingLabel(LiveSkipPolicy.STATUS_DELAYED, 60));
    }
    @Test public void confirmationTakesPriorityOverStaleRemainingSeconds() {
        assertEquals("다음", LiveSkipPolicy.floatingLabel(LiveSkipPolicy.STATUS_CONFIRMING, -1));
        assertEquals("다음", LiveSkipPolicy.floatingLabel(LiveSkipPolicy.STATUS_CONFIRMING, 5));
    }
    @Test public void errorsNeverBecomeLiveOrNextLabels() {
        assertNull(LiveSkipPolicy.floatingLabel("라이브 · 다음 영상 확인 중 실패", 5));
        assertNull(LiveSkipPolicy.floatingLabel("라이브 · 바로 넘기기 실패", 0));
        assertNull(LiveSkipPolicy.floatingLabel("시간제 · 다음 영상 확인 중", -1));
        assertNull(LiveSkipPolicy.floatingLabel(null, 5));
    }
    @Test public void zeroCountExplainsEveryIndependentOptionCombination() {
        assertEquals("광고·라이브만 자동 넘김 · 일반·시간제 중지", LiveSkipPolicy.zeroCountStatus(true, true));
        assertEquals("라이브만 자동 넘김 · 일반·시간제 중지", LiveSkipPolicy.zeroCountStatus(false, true));
        assertEquals("광고만 자동 넘김 · 일반·시간제 중지", LiveSkipPolicy.zeroCountStatus(true, false));
        assertEquals("0회 · 반복·시간제 중지, 광고·라이브 꺼짐", LiveSkipPolicy.zeroCountStatus(false, false));
    }
}
