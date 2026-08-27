package com.fullmetalsonic.shortsloop.core;

/** Ads are independent of playback count; foreground, window and ad proof remain mandatory. */
public final class AdSkipPolicy {
    private AdSkipPolicy() {}
    public static boolean enabled(boolean execution, boolean option, boolean instagramSelected) {
        return execution && option && instagramSelected;
    }
}
