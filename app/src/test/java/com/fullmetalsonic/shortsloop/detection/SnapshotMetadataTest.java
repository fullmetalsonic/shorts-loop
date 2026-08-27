package com.fullmetalsonic.shortsloop.detection;

import org.junit.Test;
import static org.junit.Assert.*;

/** JVM metadata checks; Android window/geometry checks remain device tests. */
public class SnapshotMetadataTest {
    @Test public void appIdentityPrefixKeepsClocklessCandidateAndWindow() {
        YouTubeSnapshot source = YouTubeSnapshot.withoutClock("hash", null, false).inWindow(7, null);
        YouTubeSnapshot copy = source.withIdentity("com.instagram.android|hash");
        assertTrue(copy.visualCandidate);
        assertEquals(7, copy.windowId);
        assertEquals("com.instagram.android|hash", copy.identity);
        assertEquals(source.reason, copy.reason);
        assertNull(copy.progress); assertFalse(copy.ad);
    }
    @Test public void pausedCandidateDoesNotBecomeEligibleAfterPrefix() {
        assertFalse(YouTubeSnapshot.withoutClock("hash", null, true)
                .withIdentity("com.instagram.android|hash").visualCandidate);
    }
    @Test public void adAndUnknownIdentitySemanticsArePreserved() {
        YouTubeSnapshot ad = YouTubeSnapshot.advertisement(null).withIdentity("com.instagram.android|instagram-ad");
        assertTrue(ad.ad); assertFalse(ad.visualCandidate);
        YouTubeSnapshot unknown = YouTubeSnapshot.unavailable("waiting").withIdentity("");
        assertEquals("", unknown.identity); assertFalse(unknown.recognized());
    }
}
