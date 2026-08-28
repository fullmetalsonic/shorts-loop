package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class LiveTreePolicyTest {
    @Test public void youtubeIncludesLayoutNodesIndependentOfLiveOption() {
        assertTrue(LiveTreePolicy.includeLayoutNodes(true, true, YouTubeLivePolicy.PACKAGE));
    }
    @Test public void executionOffRestoresLegacyTree() {
        assertFalse(LiveTreePolicy.includeLayoutNodes(false, true, YouTubeLivePolicy.PACKAGE));
    }
    @Test public void deselectedYouTubeRestoresLegacyTree() {
        assertFalse(LiveTreePolicy.includeLayoutNodes(true, false, YouTubeLivePolicy.PACKAGE));
    }
    @Test public void instagramAndOtherHostsAlwaysKeepLegacyTree() {
        assertFalse(LiveTreePolicy.includeLayoutNodes(true, true, "com.instagram.android"));
        assertFalse(LiveTreePolicy.includeLayoutNodes(true, true, ""));
        assertFalse(LiveTreePolicy.includeLayoutNodes(true, true, null));
    }
}
