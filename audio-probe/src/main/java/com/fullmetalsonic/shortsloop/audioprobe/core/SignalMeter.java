package com.fullmetalsonic.shortsloop.audioprobe.core;

/** Scalar measurements only. Does not retain samples or identify audio content. */
public final class SignalMeter {
    public static final double SIGNAL_THRESHOLD_DBFS = -70;
    private long samples;
    private long nonZero;
    private long blocks;
    private long signalBlocks;
    private double squares;
    private double latestDbfs = -120;
    private double maxDbfs = -120;
    private int peak;

    public void accept(short[] pcm, int count) {
        if (pcm == null || count < 0 || count > pcm.length) throw new IllegalArgumentException("Invalid PCM range");
        if (count == 0) return;
        double blockSquares = 0;
        for (int i = 0; i < count; i++) {
            int value = pcm[i];
            if (value != 0) nonZero++;
            peak = Math.max(peak, Math.abs(value));
            blockSquares += (double) value * value;
        }
        samples += count;
        blocks++;
        squares += blockSquares;
        latestDbfs = dbfs(blockSquares, count);
        maxDbfs = Math.max(maxDbfs, latestDbfs);
        if (latestDbfs >= SIGNAL_THRESHOLD_DBFS) signalBlocks++;
    }

    private static double dbfs(double energy, long count) {
        return count == 0 || energy == 0 ? -120
                : Math.max(-120, 20 * Math.log10(Math.sqrt(energy / count) / 32768.0));
    }

    public Reading reading() {
        return new Reading(samples, blocks, signalBlocks, latestDbfs, maxDbfs,
                dbfs(squares, samples), samples == 0 ? 0 : nonZero * 100.0 / samples, peak);
    }

    public record Reading(long samples, long blocks, long signalBlocks, double latestDbfs,
                          double maxDbfs, double totalDbfs, double nonZeroPercent, int peak) {
        public boolean signalDetected() { return signalBlocks >= 3; }
    }
}
