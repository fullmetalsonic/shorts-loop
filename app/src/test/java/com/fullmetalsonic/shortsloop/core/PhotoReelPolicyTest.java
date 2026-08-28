package com.fullmetalsonic.shortsloop.core;
import org.junit.Test;
import static org.junit.Assert.*;

public class PhotoReelPolicyTest {
    @Test public void photoIndexTreeIsOnlyEnabledForOptedInInstagram() {
        assertTrue(PhotoReelPolicy.includeLayoutNodes(true,true,true,"com.instagram.android"));
        assertFalse(PhotoReelPolicy.includeLayoutNodes(false,true,true,"com.instagram.android"));
        assertFalse(PhotoReelPolicy.includeLayoutNodes(true,false,true,"com.instagram.android"));
        assertFalse(PhotoReelPolicy.includeLayoutNodes(true,true,false,"com.instagram.android"));
        assertFalse(PhotoReelPolicy.includeLayoutNodes(true,true,true,"com.google.android.youtube"));
        assertFalse(PhotoReelPolicy.includeLayoutNodes(true,true,true,null));
    }
    @Test public void acceptsEverySupportedSecond() { for (int n=0;n<=10;n++) assertEquals(Integer.valueOf(n), PhotoReelPolicy.parseSeconds(""+n)); }
    @Test public void rejectsUnmodifiedInvalidDrafts() { for (String s:new String[]{"", "-1", "+1", "1.5", "1e1", "11", "100", "1 0", "abc"}) assertNull(s,PhotoReelPolicy.parseSeconds(s)); }
    @Test public void invalidStoredTimesUseThree() { assertEquals(3,PhotoReelPolicy.DEFAULT_SECONDS); for(int n:new int[]{-1,11,Integer.MAX_VALUE,Integer.MIN_VALUE}) assertEquals(3,PhotoReelPolicy.seconds(n)); }
    @Test public void missingIndicesAreNotLast() { for(String s:new String[]{"", "?", "unavailable", "1 of 2"}) { var p=PhotoReelPolicy.position(s);assertTrue(p.missing()); assertFalse(p.known()); } }
    @Test public void nullIsMissing() { assertTrue(PhotoReelPolicy.position(null).missing()); }
    @Test public void numericContradictionsCannotFallback() { for(String s:new String[]{"0/0","0/2","3/2","2/0"}) assertNull(PhotoReelPolicy.position(s)); }
    @Test public void observedIndicesAreExact() { assertEquals(new PhotoReelPolicy.Position(1,2),PhotoReelPolicy.position("1/2")); assertEquals(new PhotoReelPolicy.Position(2,2),PhotoReelPolicy.position(" 2 / 2 ")); }
    @Test public void onePhotoAndLargeCarousel() { assertTrue(PhotoReelPolicy.position("1/1").known());assertTrue(PhotoReelPolicy.position("999/999").known()); }
    @Test public void invalidModeCannotCreateNewBehavior() { assertEquals(0,PhotoReelPolicy.mode(4));assertEquals(1,PhotoReelPolicy.mode(1)); }
}
