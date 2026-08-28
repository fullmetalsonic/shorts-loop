package com.fullmetalsonic.shortsloop.core;

import java.text.Normalizer;

/** Settings for an explicitly selected time limit, never a fabricated playback clock. */
public final class ClocklessTimeoutPolicy {
    public static final int DEFAULT_SECONDS = 3;
    public static final int MIN_SECONDS = 2;
    public static final int MAX_SECONDS = 60;

    private ClocklessTimeoutPolicy() {}

    public static int sanitizeSeconds(int seconds) {
        return seconds >= MIN_SECONDS && seconds <= MAX_SECONDS ? seconds : DEFAULT_SECONDS;
    }

    /** Incomplete/invalid edits do not change a saved setting. Compatibility digits are normalized. */
    public static Integer parseSeconds(CharSequence input) {
        if (input == null) return null;
        String text = Normalizer.normalize(input, Normalizer.Form.NFKC).trim();
        if (text.isEmpty()) return null;
        int seconds = 0;
        for (int i = 0; i < text.length(); i++) {
            char digit = text.charAt(i);
            if (digit < '0' || digit > '9') return null;
            seconds = seconds * 10 + digit - '0';
            if (seconds > MAX_SECONDS) return null;
        }
        return seconds >= MIN_SECONDS ? seconds : null;
    }

    /** The caller must additionally prove a clockless Instagram page and safe visible window. */
    public static boolean enabled(boolean execution, int target, boolean instagramSelected, boolean optedIn) {
        return execution && target > 0 && target <= ModePolicy.MAX_COUNT && instagramSelected && optedIn;
    }
}
