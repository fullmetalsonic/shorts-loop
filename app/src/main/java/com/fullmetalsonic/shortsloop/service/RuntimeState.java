package com.fullmetalsonic.shortsloop.service;

/** In-process status only. No video/account data is persisted. */
public final class RuntimeState {
    public static final class HostState {
        public volatile boolean blocked;
        public volatile int current;
        public volatile int timedRemainingSeconds = -1;
        public volatile String status = "off";
    }
    private static final HostState YOUTUBE = new HostState();
    private static final HostState INSTAGRAM = new HostState();
    private static final HostState TIKTOK = new HostState();
    public static HostState forHost(String packageName) {
        if ("com.google.android.youtube".equals(packageName)) return YOUTUBE;
        if ("com.instagram.android".equals(packageName)) return INSTAGRAM;
        if ("com.ss.android.ugc.trill".equals(packageName)) return TIKTOK;
        throw new IllegalArgumentException("Unsupported host");
    }
    private RuntimeState() {}
    public static volatile boolean connected;
    public static volatile boolean blocked;
    public static volatile int current;
    /** -1 means ordinary repeat/status display; nonnegative is a time fallback countdown. */
    public static volatile int timedRemainingSeconds = -1;
    public static volatile String status = "off";
}
