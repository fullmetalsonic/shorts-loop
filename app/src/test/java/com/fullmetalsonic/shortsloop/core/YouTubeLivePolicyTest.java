package com.fullmetalsonic.shortsloop.core;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.fullmetalsonic.shortsloop.core.YouTubeLivePolicy.*;

public class YouTubeLivePolicyTest {
    private static final Bounds FULL = new Bounds(0, 0, 1248, 1744);
    private static Node node(String id, int parent) {
        return new Node(id, PACKAGE, "android.widget.FrameLayout", true, false, false, FULL, parent);
    }
    private static List<Node> live() {
        return new ArrayList<>(List.of(node("root", -1), node(RECYCLER, 0), node(PAGE, 1),
                node(PACKAGE + ":id/reel_player_page_content", 2), node(LIVE, 3)));
    }
    private static Node edit(Node source, String id, String pkg, String type, boolean visible, Bounds bounds, int parent) {
        return new Node(id, pkg, type, visible, source.focused(), source.editable(), bounds, parent);
    }
    private static void rejected(List<Node> tree) { assertEquals(State.REJECTED, evaluate(tree, true).state()); }

    @Test public void exactLiveStructureRecognizedBeforeAnyTextOrCta() {
        Result value = evaluate(live(), true);
        assertEquals(State.LIVE, value.state()); assertEquals(1, value.recyclerIndex());
        assertEquals(2, value.pageIndex()); assertEquals(4, value.liveIndex());
    }
    @Test public void normalVideoWithoutLiveMarkerKeepsNormalReader() {
        List<Node> tree = live(); tree.remove(4);
        tree.add(edit(node("seek", 2), "seek", PACKAGE, "android.widget.SeekBar", true, FULL, 2));
        assertEquals(State.NOT_LIVE, evaluate(tree, true).state());
    }
    @Test public void textOrResourceSuffixIsNotFallbackEvidence() {
        List<Node> tree = live(); tree.set(4, node(LIVE + "_label", 3));
        assertEquals(State.NOT_LIVE, evaluate(tree, true).state());
        tree.set(4, node("LIVE", 3)); assertEquals(State.NOT_LIVE, evaluate(tree, true).state());
    }
    @Test public void hiddenPreloadedMarkerIsNotRecognized() {
        List<Node> tree = live(); Node n = tree.get(4);
        tree.set(4, edit(n, n.id(), n.packageName(), n.type(), false, FULL, 3));
        assertEquals(State.NOT_LIVE, evaluate(tree, true).state());
    }
    @Test public void visibleChildUnderHiddenPageIsRejected() {
        List<Node> tree = live(); Node n = tree.get(2);
        tree.set(2, edit(n, n.id(), n.packageName(), n.type(), false, FULL, 1)); rejected(tree);
    }
    @Test public void visibleTinySeventyNinePixelPreviewIsRejected() {
        List<Node> tree = live(); Bounds partial = new Bounds(0, 1665, 1248, 1744);
        tree.set(2, edit(tree.get(2), PAGE, PACKAGE, "android.widget.FrameLayout", true, partial, 1));
        tree.set(4, edit(tree.get(4), LIVE, PACKAGE, "android.widget.FrameLayout", true, partial, 3)); rejected(tree);
    }
    @Test public void twentyEightPixelTransitionOffsetIsRejected() {
        List<Node> tree = live(); Bounds partial = new Bounds(0, 28, 1248, 1744);
        tree.set(2, edit(tree.get(2), PAGE, PACKAGE, "android.widget.FrameLayout", true, partial, 1)); rejected(tree);
    }
    @Test public void multipleVisiblePagesEvenTinyAreRejected() {
        List<Node> tree = live();
        tree.add(edit(node(PAGE, 1), PAGE, PACKAGE, "android.widget.FrameLayout", true, new Bounds(0, 1740, 1248, 1744), 1));
        rejected(tree);
    }
    @Test public void hiddenOtherPageDoesNotBecomeActiveEvidence() {
        List<Node> tree = live(); tree.add(edit(node(PAGE, 1), PAGE, PACKAGE, "android.widget.FrameLayout", false, FULL, 1));
        assertEquals(State.LIVE, evaluate(tree, true).state());
    }
    @Test public void multipleRecyclersAndMultiplePlayersAreRejected() {
        List<Node> tree = live(); tree.add(node(RECYCLER, 0)); rejected(tree);
        tree = live(); tree.add(node(LIVE, 3)); rejected(tree);
    }
    @Test public void liveOutsidePageOrPageOutsideRecyclerIsRejected() {
        List<Node> tree = live(); tree.set(4, node(LIVE, 0)); rejected(tree);
        tree = live(); tree.set(2, node(PAGE, 0)); rejected(tree);
    }
    @Test public void foreignPackageOnMarkerOrAncestorIsRejected() {
        for (int i : new int[]{0, 1, 2, 3, 4}) {
            List<Node> tree = live(); Node n = tree.get(i);
            tree.set(i, edit(n, n.id(), "other.app", n.type(), true, FULL, n.parent())); rejected(tree);
        }
    }
    @Test public void seekBarAnywhereInVisibleTreeRejectsMixedState() {
        List<Node> tree = live(); tree.add(edit(node("seek", 0), "seek", PACKAGE, "android.widget.SeekBar", true, FULL, 0)); rejected(tree);
    }
    @Test public void blockersAfterLiveMarkerAreStillAudited() {
        for (String suffix : new String[]{"bottom_sheet", "engagement_panel", "comments_recycler", "comment_entry",
                "menu_list", "dialog", "action_sheet", "popup_menu"}) {
            List<Node> tree = live(); tree.add(node(PACKAGE + ":id/" + suffix, 0)); rejected(tree);
        }
    }
    @Test public void editWebViewAndFocusedEditableBlock() {
        for (String type : new String[]{"android.widget.EditText", "android.webkit.WebView", "android.app.Dialog", "android.widget.PopupWindow"}) {
            List<Node> tree = live(); tree.add(edit(node("input", 0), "input", PACKAGE, type, true, FULL, 0)); rejected(tree);
        }
        List<Node> tree = live(); tree.add(new Node("input", PACKAGE, "custom", true, true, true, FULL, 0)); rejected(tree);
    }
    @Test public void normalCommentButtonAndHiddenDialogAreNotBlockers() {
        List<Node> tree = live(); tree.add(node(PACKAGE + ":id/comment_button", 2));
        tree.add(edit(node("dialog", 0), "dialog", PACKAGE, "android.widget.FrameLayout", false, FULL, 0));
        assertEquals(State.LIVE, evaluate(tree, true).state());
    }
    @Test public void incompleteAndOverLimitTreesAreRejected() {
        assertEquals(State.REJECTED, evaluate(live(), false).state());
        List<Node> tree = live(); while (tree.size() < MAX_NODES) tree.add(node("filler", 0));
        assertEquals(State.LIVE, evaluate(tree, true).state());
        tree.add(node("filler", 0)); rejected(tree);
    }
    @Test public void incompleteNormalTreeKeepsLegacyNormalReader() {
        List<Node> tree = live(); tree.remove(4);
        tree.add(edit(node("seek", 2), "seek", PACKAGE, "android.widget.SeekBar", true, FULL, 2));
        assertEquals(State.NOT_LIVE, evaluate(tree, false).state());
    }
    @Test public void cappedNormalTreeKeepsLegacyNormalReader() {
        List<Node> tree = live(); tree.remove(4);
        while (tree.size() < MAX_NODES) tree.add(node("filler", 0));
        assertEquals(State.NOT_LIVE, evaluate(tree, false).state());
    }
    @Test public void hiddenMarkerInIncompleteTreeDoesNotInterceptNormalReader() {
        List<Node> tree = live(); Node n = tree.get(4);
        tree.set(4, edit(n, n.id(), n.packageName(), n.type(), false, FULL, 3));
        assertEquals(State.NOT_LIVE, evaluate(tree, false).state());
    }
    @Test public void incompleteTreeWithVisibleMarkerAndSeekBarStillRejectsLive() {
        List<Node> tree = live();
        tree.add(edit(node("seek", 2), "seek", PACKAGE, "android.widget.SeekBar", true, FULL, 2));
        assertEquals(State.REJECTED, evaluate(tree, false).state());
    }
    @Test public void invalidParentChainNullNodeAndNullBoundsAreRejected() {
        List<Node> tree = live(); tree.set(3, node("content", 4)); rejected(tree);
        tree = live(); tree.set(3, null); rejected(tree);
        tree = live(); tree.set(2, edit(tree.get(2), PAGE, PACKAGE, "frame", true, null, 1)); rejected(tree);
    }
    @Test public void smallRoundingAllowedButNotClippedOrOversizedFrames() {
        assertTrue(nearlySame(new Bounds(2, 2, 1246, 1742), FULL));
        assertFalse(nearlySame(new Bounds(-100, 0, 1248, 1744), FULL));
        assertFalse(nearlySame(new Bounds(0, 0, 1248, 300), FULL));
        assertFalse(nearlySame(new Bounds(Integer.MIN_VALUE, 0, Integer.MAX_VALUE, 1744), FULL));
    }
    @Test public void stabilityRequiresTwoFreshSamplesAndMinimumTime() {
        Stability stable = new Stability();
        assertFalse(stable.observe(false, FULL, FULL, FULL, 1000));
        assertFalse(stable.observe(true, FULL, FULL, FULL, 1149));
        assertTrue(stable.observe(true, FULL, FULL, FULL, 1150));
    }
    @Test public void geometryChangeEvenWithinToleranceRestartsQualification() {
        Stability stable = new Stability(); Bounds moved = new Bounds(0, 2, 1248, 1744);
        stable.observe(false, FULL, FULL, FULL, 1000);
        assertFalse(stable.observe(true, FULL, moved, moved, 1300));
        assertTrue(stable.observe(true, FULL, moved, moved, 1600));
    }
    @Test public void nodeChangeGapClockReverseAndResetRestartQualification() {
        Stability stable = new Stability(); stable.observe(false, FULL, FULL, FULL, 1000);
        assertFalse(stable.observe(false, FULL, FULL, FULL, 1300));
        assertFalse(stable.observe(true, FULL, FULL, FULL, 2901));
        assertFalse(stable.observe(true, FULL, FULL, FULL, 2800));
        assertTrue(stable.observe(true, FULL, FULL, FULL, 3100));
        stable.reset(); assertFalse(stable.observe(true, FULL, FULL, FULL, 3400));
        assertFalse(stable.observe(true, FULL, FULL, FULL, -1));
    }
    @Test public void duplicateTimestampCannotAddQualificationTime() {
        Stability stable = new Stability(); stable.observe(false, FULL, FULL, FULL, 1000);
        assertFalse(stable.observe(true, FULL, FULL, FULL, 1000));
        assertFalse(stable.observe(true, FULL, FULL, FULL, 1149));
        assertFalse(stable.observe(true, FULL, FULL, FULL, 1149));
        assertTrue(stable.observe(true, FULL, FULL, FULL, 1150));
    }
    @Test public void sameMillisecondActionRecheckPreservesSettledEvidence() {
        Stability stable = new Stability(); stable.observe(false, FULL, FULL, FULL, 1000);
        assertTrue(stable.observe(true, FULL, FULL, FULL, 1300));
        assertTrue(stable.observe(true, FULL, FULL, FULL, 1300));
        assertTrue(stable.observe(true, FULL, FULL, FULL, 1300));
        assertFalse(stable.observe(true, FULL, FULL, FULL, 1299));
    }
    @Test public void duplicateTimeWithDifferentNodeOrGeometryDoesNotPreserveEvidence() {
        Stability stable = new Stability(); stable.observe(false, FULL, FULL, FULL, 1000);
        assertTrue(stable.observe(true, FULL, FULL, FULL, 1300));
        assertFalse(stable.observe(false, FULL, FULL, FULL, 1300));
        assertTrue(stable.observe(true, FULL, FULL, FULL, 1600));
        Bounds moved = new Bounds(0, 2, 1248, 1744);
        assertFalse(stable.observe(true, FULL, moved, moved, 1600));
    }
}
