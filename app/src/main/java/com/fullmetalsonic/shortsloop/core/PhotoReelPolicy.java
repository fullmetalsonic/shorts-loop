package com.fullmetalsonic.shortsloop.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Photo rules never infer playback length or a last slide from elapsed time. */
public final class PhotoReelPolicy {
    public static final int WHOLE = 0, EACH = 1, DEFAULT_SECONDS = 3, MAX_SECONDS = 10;
    private static final Pattern INDEX = Pattern.compile("([0-9]{1,3})\\s*/\\s*([0-9]{1,3})");
    public record Position(int current, int total) {
        public boolean known() { return current > 0 && current <= total && total <= 999; }
        public boolean missing() { return current == 0 && total == 0; }
    }
    private PhotoReelPolicy() { }
    /** Instagram marks its visible slide index as not important for accessibility. */
    public static boolean includeLayoutNodes(boolean execution, boolean instagramSelected,
            boolean photoEnabled, String foregroundPackage) {
        return execution && instagramSelected && photoEnabled && "com.instagram.android".equals(foregroundPackage);
    }
    public static int mode(int value) { return value == EACH ? EACH : WHOLE; }
    public static int seconds(int value) { return value >= 0 && value <= MAX_SECONDS ? value : DEFAULT_SECONDS; }
    public static Integer parseSeconds(CharSequence value) {
        if (value == null || !value.toString().trim().matches("[0-9]{1,2}")) return null;
        int number = Integer.parseInt(value.toString().trim());
        return number <= MAX_SECONDS ? number : null;
    }
    /** Missing/unreadable is eligible for opt-in fallback; contradictory numeric indices are not. */
    public static Position position(CharSequence value) {
        if (value == null) return new Position(0, 0);
        Matcher match = INDEX.matcher(value.toString().trim());
        if (!match.matches()) return new Position(0, 0);
        Position result = new Position(Integer.parseInt(match.group(1)), Integer.parseInt(match.group(2)));
        return result.known() ? result : null;
    }
}
