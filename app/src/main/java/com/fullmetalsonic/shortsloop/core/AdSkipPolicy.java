package com.fullmetalsonic.shortsloop.core;

/** User controls only; foreground, window and advertisement proof remain mandatory. */
public final class AdSkipPolicy {
    private AdSkipPolicy() {}
    public static boolean enabled(boolean execution, int target, boolean option, boolean instagramSelected) {
        return execution && target > 0 && option && instagramSelected;
    }
}
