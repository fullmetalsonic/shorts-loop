package com.fullmetalsonic.shortsloop.core;

/** Missing roots during a swipe are not evidence of an app switch. */
public final class SessionPolicy {
    private SessionPolicy() {}
    public static boolean packageChanged(String previous, String observed) {
        return observed != null && !observed.isEmpty() && !observed.equals(previous);
    }
}
