package com.fullmetalsonic.shortsloop.core;

/** Request-scoped page position evidence; never stores a sticky transition flag. */
public final class YouTubePageStepPolicy {
    public static final int UNKNOWN = -1, UNSAFE = -2;
    private YouTubePageStepPolicy() {}
    public static boolean next(int requested, int current) {
        return requested >= 0 && current >= 0 && (long) current == (long) requested + 1;
    }
    public static boolean permits(int requested, int current) {
        if (requested < UNKNOWN || current < UNKNOWN) return false;
        return requested == UNKNOWN || current == UNKNOWN || next(requested, current);
    }
}
