package com.fullmetalsonic.shortsloop.service;

/** In-process status only. No video/account data is persisted. */
public final class RuntimeState {
    private RuntimeState() {}
    public static volatile boolean connected;
    public static volatile boolean blocked;
    public static volatile int current;
    /** -1 means ordinary repeat/status display; nonnegative is a time fallback countdown. */
    public static volatile int timedRemainingSeconds = -1;
    public static volatile String status = "꺼짐";
}
