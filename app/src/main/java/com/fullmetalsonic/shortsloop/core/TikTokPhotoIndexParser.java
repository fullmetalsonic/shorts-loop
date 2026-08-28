package com.fullmetalsonic.shortsloop.core;

/** Parses only the three direct children of the observed r06 indicator, never arbitrary page text. */
public final class TikTokPhotoIndexParser {
    private TikTokPhotoIndexParser() { }
    public static int number(CharSequence value) {
        String text = value == null ? "" : value.toString().trim();
        if (text.matches("[0-9]{1,3}")) return Integer.parseInt(text);
        return text.matches("[0-9]+") ? -2 : -1;
    }
    public static PhotoReelPolicy.Position parseSplit(CharSequence current, CharSequence separator, CharSequence total) {
        return position(number(current), separator != null && separator.toString().trim().equals("/"), number(total));
    }
    public static PhotoReelPolicy.Position position(int current, boolean slash, int total) {
        if (!slash || current < 1 || total < 1 || total > 999 || current > total) return null;
        return new PhotoReelPolicy.Position(current, total);
    }
}
