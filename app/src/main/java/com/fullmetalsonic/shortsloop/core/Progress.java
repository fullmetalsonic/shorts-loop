package com.fullmetalsonic.shortsloop.core;

public final class Progress {
    public final double position;
    public final double duration;
    public Progress(double position, double duration) {
        this.position = position;
        this.duration = duration;
    }
    public boolean valid() {
        return Double.isFinite(position) && Double.isFinite(duration)
                && duration >= 3 && duration <= 3600 && position >= 0 && position <= duration + 0.5;
    }
}
