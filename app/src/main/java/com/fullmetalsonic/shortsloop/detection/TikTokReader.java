package com.fullmetalsonic.shortsloop.detection;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;
import com.fullmetalsonic.shortsloop.core.NormalizedProgress;
import com.fullmetalsonic.shortsloop.core.TikTokStructurePolicy;
import java.util.ArrayList;
import java.util.List;

/** Narrow reader for the observed Trill recommendation-video structure; no captions are retained. */
public final class TikTokReader {
    public static final String PACKAGE = TikTokStructurePolicy.PACKAGE;
    public static final String PAGER_ID = PACKAGE + ":id/viewpager";

    public void close() { /* No source nodes or content are retained between reads. */ }

    public YouTubeSnapshot read(AccessibilityNodeInfo root) {
        if (!owned(root)) return YouTubeSnapshot.unavailable("tiktok.waiting");
        try (Tree tree = collect(root)) {
            if (!tree.complete) return YouTubeSnapshot.unavailable("screen.complex");
            TikTokStructurePolicy.Match match = TikTokStructurePolicy.inspect(tree.metadata);
            if (!match.accepted()) return YouTubeSnapshot.unavailable(match.reason);
            if (!fresh(tree, match)) return YouTubeSnapshot.unavailable("playback.refresh");
            AccessibilityNodeInfo seek = tree.nodes.get(match.seek);
            AccessibilityNodeInfo.RangeInfo range = seek.getRangeInfo();
            NormalizedProgress progress = range == null ? null : NormalizedProgress.fromTikTokRange(
                    range.getType(), range.getMin(), range.getMax(), range.getCurrent());
            if (progress == null) return YouTubeSnapshot.unavailable("tiktok.no_progress");
            AccessibilityNodeInfo page = tree.nodes.get(match.page), media = tree.nodes.get(match.media);
            int index = pageIndex(page);
            if (index < -1) return YouTubeSnapshot.unavailable("tiktok.unsupported");
            return YouTubeSnapshot.normalizedVideo(sourceKey("page", page), bounds(page), progress)
                    .withNormalizedIdentity(sourceKey("pager", tree.nodes.get(match.pager)), sourceKey("media", media), index);
        } catch (IllegalStateException | SecurityException error) {
            return YouTubeSnapshot.unavailable("playback.refresh");
        }
    }

    /** Returns an owned copy of the unique, freshly validated inner video pager, or null. */
    @SuppressWarnings("deprecation")
    public static AccessibilityNodeInfo findPager(AccessibilityNodeInfo root, Rect expectedPage, int windowId) {
        if (!owned(root) || expectedPage == null || windowId < 0 || root.getWindowId() != windowId) return null;
        try (Tree tree = collect(root)) {
            if (!tree.complete) return null;
            TikTokStructurePolicy.Match match = TikTokStructurePolicy.inspect(tree.metadata);
            if (!match.accepted() || !fresh(tree, match)) return null;
            AccessibilityNodeInfo pager = tree.nodes.get(match.pager), page = tree.nodes.get(match.page);
            if (pager.getWindowId() != windowId || !expectedPage.equals(bounds(page))
                    || (pager.getActions() & AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) == 0) return null;
            return AccessibilityNodeInfo.obtain(pager);
        } catch (IllegalStateException | SecurityException error) { return null; }
    }

    /** Stateless identity preserves A→B→A and reused nodes; a hash collision only blocks a real change. */
    static String sourceKey(String role, AccessibilityNodeInfo node) {
        return "tiktok-" + role + ":" + node.getWindowId() + ":" + node.hashCode();
    }
    private static int pageIndex(AccessibilityNodeInfo page) {
        AccessibilityNodeInfo.CollectionItemInfo item = page.getCollectionItemInfo();
        if (item == null) return -1;
        if (item.getRowIndex() < 0 || item.getRowSpan() != 1 || item.getColumnIndex() != 0 || item.getColumnSpan() != 1) return -2;
        return item.getRowIndex();
    }
    private static boolean fresh(Tree tree, TikTokStructurePolicy.Match match) {
        int windowId = tree.root.getWindowId();
        if (windowId < 0 || !tree.root.refresh() || !owned(tree.root)) return false;
        int[] indices = {match.pager, match.page, match.media, match.seek};
        for (int index : indices) {
            AccessibilityNodeInfo node = tree.nodes.get(index);
            TikTokStructurePolicy.Node before = tree.metadata.get(index);
            Rect expected = new Rect(before.left, before.top, before.right, before.bottom);
            if (!node.refresh() || !owned(node) || node.getWindowId() != windowId || !node.isVisibleToUser()
                    || !before.id.equals(string(node.getViewIdResourceName()))
                    || !before.type.equals(string(node.getClassName())) || !expected.equals(bounds(node))) return false;
        }
        AccessibilityNodeInfo parent = tree.nodes.get(match.page).getParent();
        try { return parent != null && parent.equals(tree.nodes.get(match.pager)); }
        finally { YouTubeReader.recycle(parent); }
    }
    private static boolean owned(AccessibilityNodeInfo node) {
        return node != null && PACKAGE.contentEquals(string(node.getPackageName()));
    }
    private static Tree collect(AccessibilityNodeInfo root) {
        Tree tree = new Tree(root);
        try {
            tree.complete = visit(root, -1, 0, tree);
            return tree;
        } catch (RuntimeException error) {
            tree.close(); throw error;
        }
    }
    private static boolean visit(AccessibilityNodeInfo node, int parent, int depth, Tree tree) {
        if (tree.nodes.size() >= TikTokStructurePolicy.MAX_NODES || depth > TikTokStructurePolicy.MAX_DEPTH || !owned(node)) return false;
        int index = tree.nodes.size();
        tree.nodes.add(node);
        Rect bounds = bounds(node);
        String id = string(node.getViewIdResourceName()), type = string(node.getClassName());
        String text = string(node.getText()).trim(), description = string(node.getContentDescription()).trim();
        boolean control = node.isClickable() || type.equals("android.widget.Button") || type.equals("android.widget.ImageButton");
        // Free text is reduced to exact UI roles and then discarded. Captions never provide positive identity.
        boolean paused = TikTokStructurePolicy.isPlay(description) || (control && TikTokStructurePolicy.isPlay(text));
        boolean compactBadge = bounds.width() > 0 && bounds.height() > 0
                && bounds.width() < tree.rootBounds.width() / 3 && bounds.height() < tree.rootBounds.height() / 8;
        boolean blocked = TikTokStructurePolicy.interactionType(type)
                || ((control || compactBadge) && (TikTokStructurePolicy.specialLabel(text) || TikTokStructurePolicy.specialLabel(description)));
        tree.metadata.add(new TikTokStructurePolicy.Node(id, type, parent,
                bounds.left, bounds.top, bounds.right, bounds.bottom, node.isVisibleToUser(), node.isSelected(), node.isEditable(),
                blocked, paused, TikTokStructurePolicy.isRecommended(text) || TikTokStructurePolicy.isRecommended(description),
                TikTokStructurePolicy.isHome(text) || TikTokStructurePolicy.isHome(description)));
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child == null) return false;
            if (tree.nodes.size() >= TikTokStructurePolicy.MAX_NODES || depth >= TikTokStructurePolicy.MAX_DEPTH || !owned(child)) {
                YouTubeReader.recycle(child); return false;
            }
            if (!visit(child, index, depth + 1, tree)) return false;
        }
        return true;
    }
    private static Rect bounds(AccessibilityNodeInfo node) { Rect result = new Rect(); node.getBoundsInScreen(result); return result; }
    private static String string(CharSequence text) { return text == null ? "" : text.toString(); }
    private static final class Tree implements AutoCloseable {
        final AccessibilityNodeInfo root;
        final Rect rootBounds;
        final List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        final List<TikTokStructurePolicy.Node> metadata = new ArrayList<>();
        boolean complete;
        Tree(AccessibilityNodeInfo root) { this.root = root; rootBounds = bounds(root); }
        @Override public void close() { for (AccessibilityNodeInfo node : nodes) if (node != root) YouTubeReader.recycle(node); }
    }
}
