package com.fullmetalsonic.shortsloop.core;

public final class PositionPolicy {
    private PositionPolicy() {}
    public static float fraction(float value) {
        return Float.isFinite(value) ? Math.max(0, Math.min(1, value)) : 0.5f;
    }
    public static int clamp(int pixel, int available) { return Math.max(0, Math.min(Math.max(0, available), pixel)); }
    public static int restore(float fraction, int available) { return Math.round(fraction(fraction) * Math.max(0, available)); }
    public static float save(int pixel, int available) { return available > 0 ? (float) clamp(pixel, available) / available : 0; }
}
