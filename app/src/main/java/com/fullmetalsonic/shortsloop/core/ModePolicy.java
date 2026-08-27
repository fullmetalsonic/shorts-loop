package com.fullmetalsonic.shortsloop.core;

public final class ModePolicy {
    public static final int DEFAULT_COUNT = 2;
    public static final int MAX_COUNT = 99;
    public static final int ROTARY = 0;
    public static final int TOGGLE = 1;

    private ModePolicy() {}

    public static int sanitize(int count) {
        return count >= 0 && count <= MAX_COUNT ? count : DEFAULT_COUNT;
    }

    public static int sanitizeTapMode(int mode) { return mode == TOGGLE ? TOGGLE : ROTARY; }

    public static int clampTarget(int target, int ceiling) {
        return Math.min(sanitize(target), sanitize(ceiling));
    }

    /** Compatibility for the original 0 -> 1 -> 2 -> 0 control. */
    public static int next(int target) { return next(target, DEFAULT_COUNT, ROTARY); }

    public static int next(int target, int ceiling, int tapMode) {
        int limit = sanitize(ceiling);
        int current = clampTarget(target, limit);
        if (limit == 0) return 0;
        if (sanitizeTapMode(tapMode) == TOGGLE) return current == 0 ? limit : 0;
        return current >= limit ? 0 : current + 1;
    }

    /** Used only to migrate the previous last-nonzero preference. */
    public static int resume(int lastNonZero) {
        return lastNonZero > 0 && lastNonZero <= MAX_COUNT ? lastNonZero : DEFAULT_COUNT;
    }

    public static boolean tileActive(boolean enabled, int target, boolean connected, boolean blocked) {
        // Zero pauses ordinary/count/timer advancement, not separately opted-in ads or the control.
        return enabled && connected && !blocked;
    }
}
