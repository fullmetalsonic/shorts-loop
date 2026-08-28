package com.fullmetalsonic.shortsloop.i18n;

/** Product UI language follows the first system language, not a secondary fallback. */
public final class LanguagePolicy {
    private LanguagePolicy() {}
    public static String select(String firstLanguage) {
        return "ko".equalsIgnoreCase(firstLanguage) ? "ko" : "en";
    }
}
