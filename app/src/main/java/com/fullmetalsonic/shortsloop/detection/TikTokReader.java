package com.fullmetalsonic.shortsloop.detection;

import android.graphics.Rect;
import android.os.Build;
import android.view.accessibility.AccessibilityNodeInfo;
import com.fullmetalsonic.shortsloop.core.NormalizedProgress;
import com.fullmetalsonic.shortsloop.core.Progress;
import com.fullmetalsonic.shortsloop.core.TikTokClockParser;
import com.fullmetalsonic.shortsloop.core.TikTokPhotoIndexParser;
import com.fullmetalsonic.shortsloop.core.TikTokStructurePolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Bounded observed Trill feed structures; source identity never comes from captions/accounts. */
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
            return snapshot(tree, match);
        } catch (IllegalStateException | SecurityException error) {
            return YouTubeSnapshot.unavailable("playback.refresh");
        }
    }

    /** Returns an owned copy of the unique, freshly validated inner video pager, or null. */
    @SuppressWarnings("deprecation")
    public static AccessibilityNodeInfo findPager(AccessibilityNodeInfo root, Rect expectedPage, int windowId) {
        return findPager(root, expectedPage, windowId, null);
    }
    /** The expected snapshot must be the raw reader result, before any policy-only withAd(false) copy. */
    public static AccessibilityNodeInfo findPager(AccessibilityNodeInfo root, YouTubeSnapshot expected, int windowId) {
        return expected == null || expected.windowId >= 0 && expected.windowId != windowId
                ? null : findPager(root, expected.page, windowId, expected);
    }
    @SuppressWarnings("deprecation")
    private static AccessibilityNodeInfo findPager(AccessibilityNodeInfo root, Rect expectedPage, int windowId,
            YouTubeSnapshot expected) {
        if (!owned(root) || expectedPage == null || windowId < 0 || root.getWindowId() != windowId) return null;
        try (Tree tree = collect(root)) {
            if (!tree.complete) return null;
            TikTokStructurePolicy.Match match = TikTokStructurePolicy.inspect(tree.metadata);
            if (!match.accepted() || !fresh(tree, match)) return null;
            AccessibilityNodeInfo pager = tree.nodes.get(match.pager), page = tree.nodes.get(match.page);
            if (pager.getWindowId() != windowId || !expectedPage.equals(bounds(page))
                    || (pager.getActions() & AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) == 0) return null;
            YouTubeSnapshot fresh = snapshot(tree, match);
            if (!fresh.recognized() || (expected != null && !sameSource(expected, fresh))) return null;
            return AccessibilityNodeInfo.obtain(pager);
        } catch (IllegalStateException | SecurityException error) { return null; }
    }

    private static boolean sameSource(YouTubeSnapshot a, YouTubeSnapshot b) {
        return a.recognized() && b.recognized() && a.ad == b.ad && a.visualCandidate == b.visualCandidate
                && Objects.equals(sourceIdentity(a.identity), sourceIdentity(b.identity)) && Objects.equals(a.page, b.page)
                && Objects.equals(a.normalizedPagerKey, b.normalizedPagerKey)
                && Objects.equals(a.normalizedMediaKey, b.normalizedMediaKey)
                && Objects.equals(a.photoPageKey, b.photoPageKey)
                && Objects.equals(a.contentIdentity, b.contentIdentity)
                && a.normalizedPageIndex == b.normalizedPageIndex
                && (a.normalizedProgress == null) == (b.normalizedProgress == null)
                && (a.progress == null ? b.progress == null : b.progress != null && a.progress.duration == b.progress.duration)
                && (a.photo == null ? b.photo == null : b.photo != null
                    && a.photo.image.equals(b.photo.image) && a.photo.position.equals(b.photo.position));
    }
    /** ShortsReader adds one fixed host namespace; no arbitrary prefix or source-key rewriting is accepted. */
    private static String sourceIdentity(String identity) {
        String prefix = PACKAGE + "|";
        return identity != null && identity.startsWith(prefix) ? identity.substring(prefix.length()) : identity;
    }
    private static YouTubeSnapshot snapshot(Tree tree, TikTokStructurePolicy.Match match) {
        AccessibilityNodeInfo page = tree.nodes.get(match.page), media = tree.nodes.get(match.media);
        int index = pageIndex(page);
        if (index < -1) return YouTubeSnapshot.unavailable("tiktok.unsupported");
        NormalizedProgress normalized = null;
        Progress clock = null;
        if (match.ordinary && match.seek >= 0) {
            AccessibilityNodeInfo seek = tree.nodes.get(match.seek);
            AccessibilityNodeInfo.RangeInfo range = seek.getRangeInfo();
            if (range != null) {
                normalized = NormalizedProgress.fromTikTokRange(range.getType(), range.getMin(), range.getMax(), range.getCurrent());
                if (normalized == null) return YouTubeSnapshot.unavailable("tiktok.no_progress");
            }
            TikTokClockParser.Result parsed = TikTokClockParser.parse(seek.getText(), seek.getContentDescription(),
                    Build.VERSION.SDK_INT >= 30 ? seek.getStateDescription() : null);
            if (parsed.contradictory()) return YouTubeSnapshot.unavailable("tiktok.unsupported");
            clock = parsed.progress();
        }
        PhotoFrame photo = match.photograph ? new PhotoFrame(bounds(media), match.photoPosition) : null;
        String mediaKey = sourceKey("media", media);
        return YouTubeSnapshot.tiktokPage(sourceKey("page", page), bounds(page), clock, normalized,
                match.advertisement, match.ordinary && normalized == null && clock == null, photo)
                .withNormalizedIdentity(sourceKey("pager", tree.nodes.get(match.pager)), mediaKey, index)
                .withPhotoPageKey(mediaKey).withContentIdentity("tiktok-render:" + string(media.getClassName()));
    }

    /** Stateless source identity preserves A→B→A; renderer kind is separate, never substitute movement evidence. */
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
        boolean[] refresh = new boolean[tree.nodes.size()];
        int[] indices = {match.pager, match.page, match.media, match.seek, match.player,
                match.photoContainer, match.photoIndex, match.adLabel, match.photoLabel};
        for (int index : indices) for (int p = index; p >= 0; p = tree.metadata.get(p).parent) refresh[p] = true;
        for (int i = 0; i < tree.metadata.size(); i++) {
            TikTokStructurePolicy.Node n = tree.metadata.get(i);
            if ((n.visible && n.selected && (n.recommended || n.home))
                    || (match.photoIndex >= 0 && n.parent == match.photoIndex)) refresh[i] = true;
        }
        for (int index = 0; index < refresh.length; index++) {
            if (!refresh[index]) continue;
            AccessibilityNodeInfo node = tree.nodes.get(index);
            TikTokStructurePolicy.Node before = tree.metadata.get(index);
            Rect expected = new Rect(before.left, before.top, before.right, before.bottom);
            if (!node.refresh() || !owned(node) || node.getWindowId() != windowId || !node.isVisibleToUser()
                    || !before.id.equals(string(node.getViewIdResourceName()))
                    || !before.type.equals(string(node.getClassName())) || !expected.equals(bounds(node))) return false;
            if (before.parent >= 0) {
                AccessibilityNodeInfo parent = node.getParent();
                try { if (parent == null || !parent.equals(tree.nodes.get(before.parent))) return false; }
                finally { YouTubeReader.recycle(parent); }
            }
            tree.metadata.set(index, metadata(node, before.parent, tree));
        }
        TikTokStructurePolicy.Match again = TikTokStructurePolicy.inspect(tree.metadata);
        return again.accepted() && again.pager == match.pager && again.page == match.page && again.media == match.media
                && again.seek == match.seek && again.advertisement == match.advertisement
                && again.ordinary == match.ordinary && again.photograph == match.photograph
                && again.photoIndex == match.photoIndex && Objects.equals(again.photoPosition, match.photoPosition);
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
        tree.metadata.add(metadata(node, parent, tree));
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
    private static TikTokStructurePolicy.Node metadata(AccessibilityNodeInfo node, int parent, Tree tree) {
        Rect bounds = bounds(node);
        String id = string(node.getViewIdResourceName()), type = string(node.getClassName());
        String text = string(node.getText()).trim(), description = string(node.getContentDescription()).trim();
        boolean control = node.isClickable() || type.equals("android.widget.Button") || type.equals("android.widget.ImageButton");
        // Free text is reduced to exact UI roles and then discarded. Captions never provide positive identity.
        boolean paused = TikTokStructurePolicy.isPlay(description) || (control && TikTokStructurePolicy.isPlay(text));
        boolean compactBadge = bounds.width() > 0 && bounds.height() > 0
                && bounds.width() < tree.rootBounds.width() / 3 && bounds.height() < tree.rootBounds.height() / 8;
        boolean ad = id.equals(PACKAGE + ":id/i2n") && type.equals("android.widget.Button") && compactBadge
                && (TikTokStructurePolicy.isAd(text) || TikTokStructurePolicy.isAd(description));
        boolean photo = id.equals(PACKAGE + ":id/tv_label") && type.equals("android.widget.TextView") && compactBadge
                && (TikTokStructurePolicy.isPhoto(text) || TikTokStructurePolicy.isPhoto(description));
        boolean blocked = TikTokStructurePolicy.interactionType(type)
                || ((control || compactBadge) && (unhandledSpecial(text, ad, photo) || unhandledSpecial(description, ad, photo)));
        return new TikTokStructurePolicy.Node(id, type, parent,
                bounds.left, bounds.top, bounds.right, bounds.bottom, node.isVisibleToUser(), node.isSelected(), node.isEditable(),
                blocked, paused, TikTokStructurePolicy.isRecommended(text) || TikTokStructurePolicy.isRecommended(description),
                TikTokStructurePolicy.isHome(text) || TikTokStructurePolicy.isHome(description), ad, photo,
                TikTokPhotoIndexParser.number(text), text.equals("/"));
    }
    private static boolean unhandledSpecial(String text, boolean ad, boolean photo) {
        return TikTokStructurePolicy.specialLabel(text) && !(ad && TikTokStructurePolicy.isAd(text))
                && !(photo && TikTokStructurePolicy.isPhoto(text));
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
