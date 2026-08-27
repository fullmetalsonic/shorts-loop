package com.fullmetalsonic.shortsloop.core;

/** Screenshot timestamps and these clocks all use Android uptime milliseconds. */
public final class VisualCapturePolicy {
    private VisualCapturePolicy() {}
    public static boolean accepts(long requestEpoch, long currentEpoch, long requestedAt,
            long capturedAt, long receivedAt, long previousAt) {
        return requestEpoch == currentEpoch && requestedAt >= 0 && capturedAt >= requestedAt - 100
                && capturedAt > previousAt && receivedAt - capturedAt <= 1500 && capturedAt <= receivedAt + 100;
    }
}
