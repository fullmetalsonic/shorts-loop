package com.fullmetalsonic.shortsloop.core;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.fullmetalsonic.shortsloop.core.TikTokStructurePolicyTest.n;
import static com.fullmetalsonic.shortsloop.core.TikTokStructurePolicyTest.ordinary;

/** Public fixtures are synthetic structures, not viewing history or a physical TikTok pass. */
public class TikTokContentPolicyTest {
    private static final String P = TikTokStructurePolicy.PREFIX;
    private static TikTokStructurePolicy.Node role(String id, String type, int parent,
            boolean ad, boolean photo, int number, boolean slash) {
        return new TikTokStructurePolicy.Node(P + id, type, parent, 0, 0, 1000, 1600,
                true, false, false, false, false, false, false, ad, photo, number, slash);
    }
    private static void ad(List<TikTokStructurePolicy.Node> nodes) {
        int widget = nodes.size(); nodes.add(n("widget_container", "android.widget.FrameLayout", 5, true));
        nodes.add(role("i2n", "android.widget.Button", widget, true, false, -1, false));
    }
    private static List<TikTokStructurePolicy.Node> photo() {
        List<TikTokStructurePolicy.Node> nodes = ordinary();
        nodes.set(9, n("inactive", "android.view.View", 8, false));
        nodes.set(12, n("vb6", "android.widget.SeekBar", 11, false));
        nodes.add(n("widget_container", "android.widget.FrameLayout", 5, true)); // 15
        nodes.add(role("tv_label", "android.widget.TextView", 15, false, true, -1, false)); // 16
        nodes.add(n("abx", "other", 8, true)); // 17
        nodes.add(n("qzz", "android.view.ViewGroup", 17, true)); // 18
        nodes.add(n("r0h", "other", 18, true)); // 19
        nodes.add(n("r04", "android.view.ViewGroup", 19, true)); // 20
        nodes.add(n("qzz", "android.widget.FrameLayout", 20, true)); // 21
        nodes.add(n("wel", "android.widget.ImageView", 21, true)); // 22
        nodes.add(n("r06", "android.widget.LinearLayout", 18, true)); // 23
        nodes.add(role("", "android.widget.TextView", 23, false, false, 2, false)); // 24
        nodes.add(role("", "android.widget.TextView", 23, false, false, -1, true)); // 25
        nodes.add(role("", "android.widget.TextView", 23, false, false, 3, false)); // 26
        return nodes;
    }
    @Test public void surfaceAndTextureAreSupportedButBothTogetherAreNot() {
        List<TikTokStructurePolicy.Node> nodes = ordinary();
        nodes.set(9, n("surface", "android.view.SurfaceView", 8, true));
        assertTrue(TikTokStructurePolicy.inspect(nodes).ordinary);
        nodes.add(n("texture", "android.view.TextureView", 8, true));
        assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
    }
    @Test public void sourceBoundAdRolePreservesOrdinaryPlaybackCapability() {
        List<TikTokStructurePolicy.Node> nodes = ordinary(); ad(nodes);
        TikTokStructurePolicy.Match match = TikTokStructurePolicy.inspect(nodes);
        assertTrue(match.accepted()); assertTrue(match.advertisement); assertTrue(match.ordinary);
        assertEquals(12, match.seek);
    }
    @Test public void adLabelOutsideWidgetOrWrongIdDoesNotAuthorizeAdMode() {
        List<TikTokStructurePolicy.Node> nodes = ordinary();
        nodes.add(role("i2n", "android.widget.Button", 5, true, false, -1, false));
        assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
        nodes = ordinary(); nodes.add(n("widget_container", "android.widget.FrameLayout", 5, true));
        nodes.add(role("caption", "android.widget.Button", 15, true, false, -1, false));
        assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
    }
    @Test public void dotAdCannotAcquireOrdinaryPlaybackOrClocklessCapability() {
        List<TikTokStructurePolicy.Node> nodes = ordinary();
        nodes.set(8, n("inactive_player", "android.widget.FrameLayout", 7, false));
        nodes.set(9, n("inactive", "android.view.View", 8, false));
        nodes.add(n("player_view_pager", "other", 5, true)); // 15
        nodes.add(n("", "android.widget.FrameLayout", 15, true)); // 16
        nodes.add(n("", "android.view.SurfaceView", 16, true)); // 17
        assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
        ad(nodes);
        TikTokStructurePolicy.Match match = TikTokStructurePolicy.inspect(nodes);
        assertTrue(match.accepted()); assertTrue(match.advertisement);
        assertFalse(match.ordinary); assertFalse(match.photograph);
    }
    @Test public void knownPhotoCarriesOnlyAnExactSplitIndicator() {
        TikTokStructurePolicy.Match match = TikTokStructurePolicy.inspect(photo());
        assertTrue(match.accepted()); assertTrue(match.photograph); assertFalse(match.ordinary);
        assertEquals(new PhotoReelPolicy.Position(2, 3), match.photoPosition);
        assertEquals(22, match.media);
    }
    @Test public void advertisingAndPhotoRolesCanCoexist() {
        List<TikTokStructurePolicy.Node> nodes = photo(); ad(nodes);
        TikTokStructurePolicy.Match match = TikTokStructurePolicy.inspect(nodes);
        assertTrue(match.accepted()); assertTrue(match.advertisement); assertTrue(match.photograph);
        assertEquals(new PhotoReelPolicy.Position(2, 3), match.photoPosition);
    }
    @Test public void absentIndexIsMissingButNeverInventsOneSlide() {
        List<TikTokStructurePolicy.Node> nodes = photo().subList(0, 23);
        TikTokStructurePolicy.Match match = TikTokStructurePolicy.inspect(nodes);
        assertTrue(match.accepted()); assertTrue(match.photoPosition.missing());
    }
    @Test public void nonAbxSingleImageStructureIsStillPhotoWithUnknownTotal() {
        List<TikTokStructurePolicy.Node> nodes = photo().subList(0, 23);
        nodes.set(18, n("qzz", "android.view.ViewGroup", 8, true));
        TikTokStructurePolicy.Match match = TikTokStructurePolicy.inspect(nodes);
        assertTrue(match.accepted()); assertTrue(match.photoPosition.missing());
    }
    @Test public void photoWithoutDedicatedLabelOrWithVideoIsRejected() {
        List<TikTokStructurePolicy.Node> nodes = photo();
        nodes.set(16, n("caption", "android.widget.TextView", 15, true));
        assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
        nodes = photo(); nodes.set(9, n("texture", "android.view.TextureView", 8, true));
        assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
    }
    @Test public void partialSlideAndTwoVisibleImagesCannotFinishTransition() {
        List<TikTokStructurePolicy.Node> nodes = photo();
        nodes.set(20, new TikTokStructurePolicy.Node(P + "r04", "android.view.ViewGroup", 19,
                12, 0, 1000, 1600, true, false, false, false, false, false, false));
        assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
        nodes = photo(); nodes.add(n("wel", "android.widget.ImageView", 21, true));
        assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
    }
    @Test public void counterNeedsThreeDirectTextChildrenAndSlash() {
        List<TikTokStructurePolicy.Node> nodes = photo();
        nodes.set(25, role("", "android.widget.TextView", 23, false, false, -1, false));
        assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
        nodes = photo(); nodes.add(role("", "android.widget.TextView", 23, false, false, 4, false));
        assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
        nodes = photo(); nodes.set(24, role("", "android.widget.TextView", 18, false, false, 2, false));
        assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
    }
    @Test public void contradictoryNumbersCannotUseMissingIndexFallback() {
        for (int current : new int[]{0, 4, -1, -2}) {
            List<TikTokStructurePolicy.Node> nodes = photo();
            nodes.set(24, role("", "android.widget.TextView", 23, false, false, current, false));
            assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
        }
    }
    @Test public void indexFromAnotherContainerCannotAttachToPhoto() {
        List<TikTokStructurePolicy.Node> nodes = photo();
        nodes.set(23, n("r06", "android.widget.LinearLayout", 15, true));
        assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
    }
    @Test public void hiddenNeighborDoesNotSupplyActivePhotoIndex() {
        List<TikTokStructurePolicy.Node> nodes = photo().subList(0, 23);
        nodes.add(n("r06", "android.widget.LinearLayout", 4, false));
        TikTokStructurePolicy.Match match = TikTokStructurePolicy.inspect(nodes);
        assertTrue(match.accepted()); assertTrue(match.photoPosition.missing());
    }
    @Test public void clocklessUnknownPlayerOrRewardPromoRemainRejected() {
        List<TikTokStructurePolicy.Node> nodes = ordinary();
        nodes.set(12, n("vb6", "android.widget.SeekBar", 11, false));
        nodes.set(8, n("unknown_player", "android.widget.FrameLayout", 7, true));
        assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
        nodes = ordinary(); nodes.add(n("zax", "android.view.View", 5, true));
        assertFalse(TikTokStructurePolicy.inspect(nodes).accepted());
    }
}
