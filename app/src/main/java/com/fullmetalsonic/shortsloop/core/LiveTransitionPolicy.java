package com.fullmetalsonic.shortsloop.core;

/** Fresh, same-pager evidence for the ambiguous live-to-live confirmation path. */
public final class LiveTransitionPolicy {
    private LiveTransitionPolicy() {}
    public static boolean accepts(long requestAt, long eventAt, long now, int requestWindow, int eventWindow,
                                  int fromIndex, int newIndex, boolean samePager) {
        return requestAt >= 0 && eventAt >= requestAt && eventAt <= now && samePager
                && requestWindow >= 0 && eventWindow == requestWindow
                && fromIndex >= 0 && newIndex >= 0 && fromIndex != newIndex;
    }
}
