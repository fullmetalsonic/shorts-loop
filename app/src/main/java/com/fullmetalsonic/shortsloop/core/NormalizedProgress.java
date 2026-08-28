package com.fullmetalsonic.shortsloop.core;

/** A unitless playback fraction. It is deliberately not a Progress or a duration in seconds. */
public final class NormalizedProgress {
    public final double fraction;
    public NormalizedProgress(double fraction) { this.fraction = fraction; }
    public boolean valid() { return Double.isFinite(fraction) && fraction >= 0 && fraction <= 1; }

    /** Only the actually observed TikTok integer range is accepted; other scales fail closed. */
    public static NormalizedProgress fromTikTokRange(int type, double min, double max, double current) {
        if (type != 0 || min != 0 || max != 10000 || !Double.isFinite(current)
                || current < min || current > max) return null;
        return new NormalizedProgress(current / 10000.0);
    }
}
