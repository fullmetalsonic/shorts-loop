package com.fullmetalsonic.shortsloop.core;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/** Synthetic structure only; not a TikTok app or device end-to-end test. */
public class TikTokStructurePolicyTest {
    private static final String P = TikTokStructurePolicy.PREFIX;
    static TikTokStructurePolicy.Node n(String id, String type, int parent, boolean visible) {
        return new TikTokStructurePolicy.Node(P + id, type, parent, 0, 0, 1000, 1600,
                visible, false, false, false, false, false, false);
    }
    static List<TikTokStructurePolicy.Node> ordinary() {
        List<TikTokStructurePolicy.Node> nodes = new ArrayList<>();
        nodes.add(n("root", "android.widget.FrameLayout", -1, true)); // 0
        nodes.add(n("viewpager_container", "android.widget.LinearLayout", 0, true)); // 1
        nodes.add(n("view_pager_layout_wrapper", "android.widget.FrameLayout", 1, true)); // 2
        nodes.add(n("viewpager", "androidx.viewpager.widget.ViewPager", 2, true)); // 3
        nodes.add(n("view_rootview", "android.widget.FrameLayout", 3, false)); // 4
        nodes.add(n("view_rootview", "android.widget.FrameLayout", 3, true)); // 5
        nodes.add(n("video_container_area", "android.widget.FrameLayout", 5, true)); // 6
        nodes.add(n("video_visible_area_container", "android.widget.FrameLayout", 6, true)); // 7
        nodes.add(n("player_view", "android.widget.FrameLayout", 7, true)); // 8
        nodes.add(n("w4g", "android.view.TextureView", 8, true)); // 9
        nodes.add(n("view_rootview", "android.widget.FrameLayout", 3, false)); // 10
        nodes.add(n("video_seek_bar", "android.widget.LinearLayout", 0, true)); // 11: outside pager, as observed
        nodes.add(n("vb6", "android.widget.SeekBar", 11, true)); // 12
        nodes.add(new TikTokStructurePolicy.Node("", "android.widget.TextView", 0, 0, 0, 100, 100,
                true, true, false, false, false, true, false)); // 13
        nodes.add(new TikTokStructurePolicy.Node("", "android.widget.TextView", 0, 0, 1500, 100, 1600,
                true, true, false, false, false, false, true)); // 14
        return nodes;
    }
    @Test public void acceptsExactlyOneInnerVisibleVideoAndExternalSeek() {
        TikTokStructurePolicy.Match match = TikTokStructurePolicy.inspect(ordinary());
        assertTrue(match.accepted()); assertEquals(3, match.pager); assertEquals(5, match.page);
        assertEquals(9, match.media); assertEquals(12, match.seek);
    }
    @Test public void observedRewardPromoIsRejectedEvenWithPlayerAndProgress() {
        List<TikTokStructurePolicy.Node> nodes = ordinary(); nodes.add(n("zb1", "android.widget.FrameLayout", 5, true));
        nodes.add(n("zax", "android.widget.FrameLayout", 15, true));
        assertEquals("tiktok.unsupported", TikTokStructurePolicy.inspect(nodes).reason);
    }
    @Test public void offscreenPromoDoesNotContaminateCurrentOrdinaryPage() {
        List<TikTokStructurePolicy.Node> nodes = ordinary(); nodes.add(n("zb1", "android.widget.FrameLayout", 4, false));
        assertTrue(TikTokStructurePolicy.inspect(nodes).accepted());
    }
    @Test public void hiddenRangeOnlyQualifiesAnOtherwiseKnownOrdinaryPage() {
        List<TikTokStructurePolicy.Node> nodes = ordinary(); nodes.set(12, n("vb6", "android.widget.SeekBar", 11, false));
        TikTokStructurePolicy.Match match = TikTokStructurePolicy.inspect(nodes);
        assertTrue(match.accepted()); assertTrue(match.ordinary); assertEquals(-1, match.seek);
    }
    @Test public void twoVisiblePagesOrPagersAreRejected() {
        List<TikTokStructurePolicy.Node> nodes = ordinary(); nodes.set(4, n("view_rootview", "android.widget.FrameLayout", 3, true));
        assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
        nodes = ordinary(); nodes.add(n("viewpager", "androidx.viewpager.widget.ViewPager", 2, true));
        assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
    }
    @Test public void outerPagerIdAloneCannotBeUsed() {
        List<TikTokStructurePolicy.Node> nodes = ordinary(); nodes.set(2, n("not_wrapper", "android.widget.FrameLayout", 1, true));
        assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
    }
    @Test public void missingSelectedTabCannotProveRecommendation() {
        List<TikTokStructurePolicy.Node> nodes = ordinary(); nodes.remove(14);
        assertEquals("tiktok.waiting", TikTokStructurePolicy.inspect(nodes).reason);
    }
    @Test public void editableModalAndPauseAlwaysWinOverValidProgress() {
        for (int mode = 0; mode < 3; mode++) {
            List<TikTokStructurePolicy.Node> nodes = ordinary();
            nodes.add(new TikTokStructurePolicy.Node("", "android.view.View", 0, 0, 0, 100, 100,
                    true, false, mode == 0, mode == 1, mode == 2, false, false));
            assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
        }
    }
    @Test public void malformedCycleAndTruncatedTreeCannotBeAccepted() {
        List<TikTokStructurePolicy.Node> nodes = ordinary(); nodes.set(0, n("root", "android.view.View", 0, true));
        assertEquals("screen.complex", TikTokStructurePolicy.inspect(nodes).reason);
        nodes = ordinary(); while (nodes.size() <= 700) nodes.add(n("padding", "android.view.View", 0, false));
        assertEquals("screen.complex", TikTokStructurePolicy.inspect(nodes).reason);
    }
    @Test public void unknownSeekPhotoLiveAndDuplicateTextureReject() {
        for (String special : new String[]{"photo_mode", "live_preview", "ad_container", "image_slide"}) {
            List<TikTokStructurePolicy.Node> nodes = ordinary(); nodes.add(n(special, "android.view.View", 5, true));
            assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
        }
        List<TikTokStructurePolicy.Node> nodes = ordinary(); nodes.set(12, n("unknown", "android.widget.SeekBar", 11, true));
        assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
        nodes = ordinary(); nodes.add(n("second_texture", "android.view.TextureView", 8, true));
        assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
    }
    @Test public void semanticRolesAreExactNotCaptionSubstringHeuristics() {
        assertTrue(TikTokStructurePolicy.specialLabel("Sponsored"));
        assertFalse(TikTokStructurePolicy.specialLabel("My sponsored adventure"));
        assertTrue(TikTokStructurePolicy.isRecommended("For You"));
        assertFalse(TikTokStructurePolicy.isRecommended("A gift for you"));
        assertTrue(TikTokStructurePolicy.isPlay("재생"));
        assertFalse(TikTokStructurePolicy.isPlay("Playing music"));
    }
}
