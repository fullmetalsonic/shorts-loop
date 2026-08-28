package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class TikTokPhotoIndexParserTest {
    @Test public void exactSplitNumbersIncludingLastPhotoAreReadable() {
        assertEquals(new PhotoReelPolicy.Position(1, 5), TikTokPhotoIndexParser.parseSplit("1", "/", "5"));
        assertEquals(new PhotoReelPolicy.Position(5, 5), TikTokPhotoIndexParser.parseSplit(" 5 ", " / ", " 5 "));
        assertEquals(new PhotoReelPolicy.Position(999, 999), TikTokPhotoIndexParser.parseSplit("999", "/", "999"));
    }
    @Test public void zeroReversalOversizeAndWrongSeparatorReject() {
        assertNull(TikTokPhotoIndexParser.parseSplit("0", "/", "5"));
        assertNull(TikTokPhotoIndexParser.parseSplit("6", "/", "5"));
        assertNull(TikTokPhotoIndexParser.parseSplit("1", "/", "1000"));
        assertNull(TikTokPhotoIndexParser.parseSplit("1", "of", "5"));
    }
    @Test public void captionsDecimalPairsAndUnicodeDigitsAreNotIndices() {
        for (String value : new String[]{"photo 1", "1/5", "1.0", "１", "hello", "", "1000"})
            assertNull(TikTokPhotoIndexParser.parseSplit(value, "/", "5"));
        assertNull(TikTokPhotoIndexParser.parseSplit(null, "/", "5"));
    }
}
