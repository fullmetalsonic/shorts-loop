package com.fullmetalsonic.shortsloop.core;

import java.text.Normalizer;

/** Opt-in live-preview delay settings; neither a playback clock nor proof of a live page. */
public final class LiveSkipPolicy {
    public static final int DEFAULT_SECONDS = 0;
    public static final int MIN_SECONDS = 0;
    public static final int MAX_SECONDS = 60;
    public static final String STATUS_IMMEDIATE = "라이브 · 바로 넘기기";
    public static final String STATUS_DELAYED = "라이브 · 설정 시간 후 넘김";
    public static final String STATUS_CONFIRMING = "라이브 · 다음 영상 확인 중";

    private LiveSkipPolicy() {}

    public static int clamp(int seconds) { return Math.max(MIN_SECONDS, Math.min(MAX_SECONDS, seconds)); }

    /** Corrupt stored values fall back to the documented default; editor input is validated separately. */
    public static int sanitizeSeconds(int seconds) {
        return seconds >= MIN_SECONDS && seconds <= MAX_SECONDS ? seconds : DEFAULT_SECONDS;
    }

    /** Null means an incomplete or invalid draft, including signs, decimals and values over 60. */
    public static Integer parseSeconds(CharSequence input) {
        if (input == null) return null;
        String text = Normalizer.normalize(input, Normalizer.Form.NFKC).trim();
        if (text.isEmpty()) return null;
        int seconds = 0;
        for (int index = 0; index < text.length(); index++) {
            char digit = text.charAt(index);
            if (digit < '0' || digit > '9') return null;
            seconds = seconds * 10 + digit - '0';
            if (seconds > MAX_SECONDS) return null;
        }
        return seconds;
    }

    /** Repeat target is deliberately absent. The caller still needs a safe, recognized YouTube live preview. */
    public static boolean enabled(boolean execution, boolean skipLive, boolean youtubeSelected) {
        return execution && skipLive && youtubeSelected;
    }

    public static boolean isLiveStatus(String status) {
        return STATUS_IMMEDIATE.equals(status) || STATUS_DELAYED.equals(status) || STATUS_CONFIRMING.equals(status);
    }

    /** Exact status matching prevents a live-related failure message from appearing as progress. */
    public static String floatingLabel(String status, int remainingSeconds) {
        if (STATUS_CONFIRMING.equals(status)) return "다음";
        if (!STATUS_IMMEDIATE.equals(status) && !STATUS_DELAYED.equals(status)) return null;
        return remainingSeconds > 0 ? remainingSeconds + "초" : "라이브";
    }

    public static String zeroCountStatus(boolean adsEnabled, boolean liveEnabled) {
        if (adsEnabled && liveEnabled) return "광고·라이브만 자동 넘김 · 일반·시간제 중지";
        if (liveEnabled) return "라이브만 자동 넘김 · 일반·시간제 중지";
        if (adsEnabled) return "광고만 자동 넘김 · 일반·시간제 중지";
        return "0회 · 반복·시간제 중지, 광고·라이브 꺼짐";
    }
}
