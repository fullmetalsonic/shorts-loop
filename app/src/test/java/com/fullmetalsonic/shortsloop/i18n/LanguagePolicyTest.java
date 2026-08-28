package com.fullmetalsonic.shortsloop.i18n;

import org.junit.Test;
import static org.junit.Assert.*;

public class LanguagePolicyTest {
    @Test public void koreanFirstSelectsKorean() {
        assertEquals("ko", LanguagePolicy.select("ko"));
        assertEquals("ko", LanguagePolicy.select("KO"));
    }
    @Test public void everyOtherFirstLanguageFallsBackToEnglish() {
        for (String language : new String[]{null, "", "en", "ja", "zh", "ar", "fr", "de", "es"})
            assertEquals("en", LanguagePolicy.select(language));
    }
}
