package com.fullmetalsonic.shortsloop.core;

/** Explicit product allowlist. Order is presentation order, never window position. */
public final class HostRegistry {
    public static final String YOUTUBE = "com.google.android.youtube";
    public static final String INSTAGRAM = "com.instagram.android";
    public static final String TIKTOK = "com.ss.android.ugc.trill";
    private HostRegistry() {}
    public static String[] packages() { return new String[] {YOUTUBE, INSTAGRAM, TIKTOK}; }
    public static boolean supports(String host) {
        return YOUTUBE.equals(host) || INSTAGRAM.equals(host) || TIKTOK.equals(host);
    }
}
