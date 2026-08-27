package com.fullmetalsonic.shortsloop.core;

/** Input and migration rules, independent from Android storage and widgets. */
public final class SettingsPolicy {
    private SettingsPolicy() {}

    /** Invalid or unfinished input is not a request to save zero. */
    public static Integer parseCount(CharSequence input) {
        if (input == null) return null;
        String text = input.toString().trim();
        if (text.isEmpty()) return null;
        int value = 0;
        for (int i = 0; i < text.length(); i++) {
            char digit = text.charAt(i);
            if (digit < '0' || digit > '9') return null;
            value = value * 10 + digit - '0';
            // Check each digit so a long pasted number cannot overflow the integer.
            if (value > ModePolicy.MAX_COUNT) return null;
        }
        return value;
    }

    public static int legacyCeiling(int legacyTarget, int legacyLastNonZero) {
        int target = ModePolicy.sanitize(legacyTarget);
        return target > 0 ? target : ModePolicy.resume(legacyLastNonZero);
    }
}
