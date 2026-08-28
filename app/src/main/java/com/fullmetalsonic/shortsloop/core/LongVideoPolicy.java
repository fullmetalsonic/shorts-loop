package com.fullmetalsonic.shortsloop.core;

import java.text.Normalizer;

/** Optional total-duration filter, independent of repeat count. Unknown durations never qualify. */
public final class LongVideoPolicy {
    public static final int DEFAULT_SECONDS = 60, MIN_SECONDS = 1, MAX_SECONDS = 3600;
    public static final String CHECKING = "긴 영상 · 길이 확인 중";
    public static final String CONFIRMING = "긴 영상 · 다음 영상 확인 중";
    private LongVideoPolicy() {}
    public static int clamp(int value) { return Math.max(MIN_SECONDS, Math.min(MAX_SECONDS, value)); }
    public static int sanitizeSeconds(int value) {
        return value >= MIN_SECONDS && value <= MAX_SECONDS ? value : DEFAULT_SECONDS;
    }
    public static Integer parseSeconds(CharSequence input) {
        if (input == null) return null;
        String value = Normalizer.normalize(input.toString().trim(), Normalizer.Form.NFKC);
        if (!value.matches("[0-9]{1,4}")) return null;
        int seconds = Integer.parseInt(value);
        return seconds >= MIN_SECONDS && seconds <= MAX_SECONDS ? seconds : null;
    }
    public static boolean qualifies(boolean enabled, Progress progress, int seconds) {
        return enabled && progress != null && progress.valid() && progress.duration >= sanitizeSeconds(seconds);
    }
    public static String zeroCountStatus(boolean ads, boolean live, boolean longVideo) {
        if (!longVideo) return LiveSkipPolicy.zeroCountStatus(ads, live);
        return "반복 꺼짐 · 긴 영상" + (ads ? "·광고" : "") + (live ? "·라이브" : "") + "만 넘김";
    }
}
