package com.fullmetalsonic.shortsloop.detection;

import com.fullmetalsonic.shortsloop.core.NormalizedProgress;
import org.junit.Test;
import static org.junit.Assert.*;

public class TikTokSnapshotMetadataTest {
    @Test public void allCopyOperationsKeepNormalizedDataSeparateFromSeconds() {
        NormalizedProgress progress = new NormalizedProgress(.5);
        YouTubeSnapshot original = YouTubeSnapshot.normalizedVideo("page", null, progress)
                .withNormalizedIdentity("pager", "media", 4);
        YouTubeSnapshot result = original.withIdentity("package|page").inWindow(8, null)
                .withContentIdentity("extra").withPhotoPageKey("photo-extra");
        assertSame(progress, result.normalizedProgress); assertNull(result.progress);
        assertEquals("pager", result.normalizedPagerKey); assertEquals("media", result.normalizedMediaKey);
        assertEquals(4, result.normalizedPageIndex); assertEquals(8, result.windowId);
        assertFalse(result.usable()); assertFalse(result.ad); assertFalse(result.live); assertFalse(result.visualCandidate);
    }
    @Test public void existingSnapshotKindsNeverAcquireNormalizedCapability() {
        for (YouTubeSnapshot item : new YouTubeSnapshot[]{YouTubeSnapshot.unavailable("waiting"),
                YouTubeSnapshot.advertisement(null), YouTubeSnapshot.livePreview("live", null),
                YouTubeSnapshot.withoutClock("clockless", null, false), new YouTubeSnapshot(null, "ordinary", null, "")}) {
            assertNull(item.normalizedProgress); assertFalse(item.normalizedUsable());
            assertEquals("", item.normalizedPagerKey); assertEquals("", item.normalizedMediaKey); assertEquals(-1, item.normalizedPageIndex);
        }
    }
}
