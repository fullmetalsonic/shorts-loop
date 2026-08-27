package com.fullmetalsonic.shortsloop.core;

import java.util.ArrayDeque;

/** Allows whole-second UI update jitter, but not repeated fast jumps. */
public final class ProgressMotion {
    private static final long WINDOW_MS = 3000;
    private static final double MAX_RATE = 2.1;
    private static final double TIME_JITTER_SECONDS = 2.0;
    private static final class Sample {
        final double position; final long at;
        Sample(double position, long at) { this.position = position; this.at = at; }
    }
    private final ArrayDeque<Sample> recent = new ArrayDeque<>();
    public void reset(double position, long at) {
        recent.clear(); recent.addLast(new Sample(position, at));
    }
    public boolean accept(double position, long at) {
        while (!recent.isEmpty() && at - recent.peekFirst().at > WINDOW_MS) recent.removeFirst();
        for (Sample sample : recent) {
            if (position - sample.position > (at - sample.at) / 1000.0 * MAX_RATE + TIME_JITTER_SECONDS)
                return false;
        }
        recent.addLast(new Sample(position, at));
        return true;
    }
}
