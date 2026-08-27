package com.fullmetalsonic.shortsloop.audioprobe.core;

import java.util.Arrays;

/** Small streaming-workspace FFT; no Android dependency and no retained PCM. */
final class SpectralFrame {
    enum Kind { LOW_RMS, NARROW, VALID }
    static final int BANDS = 24, SAMPLES = 1600;
    private static final int FFT = 512;
    private static final double[] EDGES = {50, 90, 140, 200, 270, 350, 450, 570, 700,
            850, 1000, 1200, 1450, 1750, 2100, 2500, 3000, 3500, 4100, 4700,
            5300, 5900, 6500, 7200, 8001};
    private final double[] real = new double[FFT], imaginary = new double[FFT], power = new double[BANDS];
    private Kind lastKind = Kind.LOW_RMS;

    /** Returns false for inaudible/DC/spectrally narrow frames; output is unit-normalized shape. */
    boolean analyze(short[] pcm, float[] output) {
        lastKind = Kind.LOW_RMS;
        Arrays.fill(output, 0); Arrays.fill(power, 0);
        double mean = 0, square = 0;
        for (short value : pcm) mean += value;
        mean /= SAMPLES;
        for (short value : pcm) { double centered = value - mean; square += centered * centered; }
        if (square / SAMPLES < 64 * 64) return false;
        lastKind = Kind.NARROW;
        for (int offset : new int[]{0, 544, 1088}) {
            Arrays.fill(imaginary, 0);
            for (int i = 0; i < FFT; i++)
                real[i] = (pcm[offset + i] - mean) * (.5 - .5 * Math.cos(2 * Math.PI * i / (FFT - 1)));
            transform();
            for (int bin = 2; bin < FFT / 2; bin++) {
                double frequency = bin * 16000d / FFT;
                int band = 0;
                while (band < BANDS - 1 && frequency >= EDGES[band + 1]) band++;
                power[band] += real[bin] * real[bin] + imaginary[bin] * imaginary[bin];
            }
        }
        double total = 0, maximum = 0;
        for (double value : power) { total += value; maximum = Math.max(maximum, value); }
        if (total <= 0) return false;
        int occupied = 0;
        for (int i = 0; i < BANDS; i++) {
            double fraction = power[i] / total;
            output[i] = (float) Math.sqrt(fraction);
            if (fraction >= .025) occupied++;
        }
        boolean valid = occupied >= 3 && maximum / total < .90;
        if (valid) lastKind = Kind.VALID;
        return valid;
    }

    Kind lastKind() { return lastKind; }

    void clear() {
        Arrays.fill(real, 0); Arrays.fill(imaginary, 0); Arrays.fill(power, 0);
        lastKind = Kind.LOW_RMS;
    }

    private void transform() {
        for (int i = 1, j = 0; i < FFT; i++) {
            int bit = FFT >> 1;
            for (; (j & bit) != 0; bit >>= 1) j ^= bit;
            j ^= bit;
            if (i < j) {
                double swap = real[i]; real[i] = real[j]; real[j] = swap;
                swap = imaginary[i]; imaginary[i] = imaginary[j]; imaginary[j] = swap;
            }
        }
        for (int size = 2; size <= FFT; size <<= 1) {
            double cosine = Math.cos(-2 * Math.PI / size), sine = Math.sin(-2 * Math.PI / size);
            for (int start = 0; start < FFT; start += size) {
                double wr = 1, wi = 0;
                for (int j = 0; j < size / 2; j++) {
                    int even = start + j, odd = even + size / 2;
                    double tr = wr * real[odd] - wi * imaginary[odd];
                    double ti = wr * imaginary[odd] + wi * real[odd];
                    real[odd] = real[even] - tr; imaginary[odd] = imaginary[even] - ti;
                    real[even] += tr; imaginary[even] += ti;
                    double next = wr * cosine - wi * sine;
                    wi = wr * sine + wi * cosine; wr = next;
                }
            }
        }
    }
}
