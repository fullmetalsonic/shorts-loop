package com.fullmetalsonic.shortsloop.detection;

import android.graphics.Rect;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityNodeInfo;
import com.fullmetalsonic.shortsloop.core.YouTubeLivePolicy;
import java.util.ArrayList;
import java.util.List;

/** RAM-only live structure reader. No text fallback and no actions on the inspected nodes. */
final class YouTubeLiveReader implements AutoCloseable {
    private final YouTubeLivePolicy.Stability stability = new YouTubeLivePolicy.Stability();
    private final PageIdentity identity = new PageIdentity();
    private AccessibilityNodeInfo candidate;

    /** null means no visible live marker: preserve the existing normal-video reader. */
    YouTubeSnapshot read(List<AccessibilityNodeInfo> nodes, List<Integer> parents, boolean complete) {
        List<YouTubeLivePolicy.Node> values = values(nodes, parents);
        YouTubeLivePolicy.Result result = YouTubeLivePolicy.evaluate(values, complete);
        if (result.state() != YouTubeLivePolicy.State.LIVE) {
            interrupt();
            return result.state() == YouTubeLivePolicy.State.NOT_LIVE ? null
                    : YouTubeSnapshot.unavailable("라이브 화면 전환·댓글·메뉴 확인 대기");
        }
        int[] critical = {0, result.recyclerIndex(), result.pageIndex(), result.liveIndex()};
        for (int index : critical) {
            AccessibilityNodeInfo node = nodes.get(index);
            if (!node.refresh() || !node.isVisibleToUser()) {
                interrupt(); return YouTubeSnapshot.unavailable("라이브 화면 갱신 대기");
            }
        }
        List<YouTubeLivePolicy.Node> refreshed = values(nodes, parents);
        YouTubeLivePolicy.Result freshResult = YouTubeLivePolicy.evaluate(refreshed, true);
        if (!result.equals(freshResult) || !sameCriticalGeometry(values, refreshed, critical)
                || !freshAncestors(nodes, parents, result.liveIndex())) {
            interrupt(); return YouTubeSnapshot.unavailable("라이브 화면 전환 확인 대기");
        }
        AccessibilityNodeInfo page = nodes.get(result.pageIndex());
        boolean same = candidate != null && candidate.equals(page);
        if (!same) { YouTubeReader.recycle(candidate); candidate = copy(page); }
        boolean settled = stability.observe(same, refreshed.get(result.recyclerIndex()).bounds(),
                refreshed.get(result.pageIndex()).bounds(), refreshed.get(result.liveIndex()).bounds(), SystemClock.uptimeMillis());
        if (!settled) return YouTubeSnapshot.unavailable("라이브 화면 안정화 대기");
        Rect bounds = new Rect(); page.getBoundsInScreen(bounds);
        return YouTubeSnapshot.livePreview(identity.key(page), bounds);
    }
    private static List<YouTubeLivePolicy.Node> values(List<AccessibilityNodeInfo> nodes, List<Integer> parents) {
        List<YouTubeLivePolicy.Node> result = new ArrayList<>(nodes.size());
        for (int i = 0; i < nodes.size(); i++) {
            AccessibilityNodeInfo n = nodes.get(i);
            Rect b = new Rect(); n.getBoundsInScreen(b);
            result.add(new YouTubeLivePolicy.Node(n.getViewIdResourceName(), string(n.getPackageName()),
                    string(n.getClassName()), n.isVisibleToUser(), n.isFocused(), n.isEditable(),
                    new YouTubeLivePolicy.Bounds(b.left, b.top, b.right, b.bottom), parents.get(i)));
        }
        return result;
    }
    private static boolean sameCriticalGeometry(List<YouTubeLivePolicy.Node> before,
            List<YouTubeLivePolicy.Node> after, int[] indices) {
        for (int index : indices) if (!before.get(index).equals(after.get(index))) return false;
        return true;
    }
    /** Compare the current parent links rather than trusting a cached preorder relationship. */
    private static boolean freshAncestors(List<AccessibilityNodeInfo> nodes, List<Integer> parents, int child) {
        for (int index = child; parents.get(index) >= 0; index = parents.get(index)) {
            AccessibilityNodeInfo parent = nodes.get(index).getParent();
            try {
                if (parent == null || !parent.equals(nodes.get(parents.get(index)))) return false;
            } finally { YouTubeReader.recycle(parent); }
        }
        return true;
    }
    /** Interrupt qualification only. Never make a known page look new after a gap or normal video. */
    void interrupt() { stability.reset(); YouTubeReader.recycle(candidate); candidate = null; }
    @Override public void close() { interrupt(); identity.close(); }
    private static String string(CharSequence value) { return value == null ? "" : value.toString(); }
    @SuppressWarnings("deprecation")
    private static AccessibilityNodeInfo copy(AccessibilityNodeInfo source) { return AccessibilityNodeInfo.obtain(source); }

    /** Equality is Android source-node/window identity, NOT a guaranteed stream/content identifier. */
    static final class PageIdentity implements AutoCloseable {
        private AccessibilityNodeInfo page;
        private long serial;
        String key(AccessibilityNodeInfo current) {
            if (page == null || !page.equals(current)) {
                AccessibilityNodeInfo replacement = copy(current);
                YouTubeReader.recycle(page); page = replacement; serial++;
            }
            return "youtube-live-node:" + serial;
        }
        @Override public void close() { YouTubeReader.recycle(page); page = null; }
    }
}
