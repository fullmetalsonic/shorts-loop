package com.fullmetalsonic.shortsloop.core;

import java.text.Normalizer;
import java.util.Locale;

/** Exact integer tenths: no floating-point accumulation in input or scheduling. */
public final class AdDelayPolicy {
    public static final int DEFAULT_TENTHS = 0, MAX_TENTHS = 99;
    private AdDelayPolicy() {}
    public static int sanitize(int tenths) { return tenths >= 0 && tenths <= MAX_TENTHS ? tenths : DEFAULT_TENTHS; }
    public static String format(int tenths) { int value = sanitize(tenths); return String.format(Locale.ROOT, "%d.%d", value / 10, value % 10); }
    public static Integer parseTenths(CharSequence input) {
        if (input == null) return null;
        String value = Normalizer.normalize(input, Normalizer.Form.NFKC).trim();
        if (!value.matches("[0-9](\\.[0-9])?")) return null;
        return (value.charAt(0) - '0') * 10 + (value.length() == 3 ? value.charAt(2) - '0' : 0);
    }
}
