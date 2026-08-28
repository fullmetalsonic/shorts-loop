package com.fullmetalsonic.shortsloop.core;

/** Recognize live arrivals even when live skipping is OFF; other hosts keep their legacy tree. */
public final class LiveTreePolicy {
    private LiveTreePolicy() { }
    public static boolean includeLayoutNodes(boolean execution,
            boolean youtubeSelected, String foregroundPackage) {
        return execution && youtubeSelected
                && YouTubeLivePolicy.PACKAGE.equals(foregroundPackage);
    }
}
