package com.fullmetalsonic.shortsloop.core;

import java.util.List;
import java.util.Locale;

/** Structural live-preview evidence only: deliberately no text, CTA or viewer-count inputs. */
public final class YouTubeLivePolicy {
    public static final int MAX_NODES = 600;
    public static final String PACKAGE = "com.google.android.youtube";
    public static final String RECYCLER = PACKAGE + ":id/reel_recycler";
    public static final String PAGE = PACKAGE + ":id/reel_player_page_container";
    public static final String LIVE = PACKAGE + ":id/immersive_live_preview_player";
    private YouTubeLivePolicy() { }

    public record Bounds(int left, int top, int right, int bottom) {
        long width() { return (long) right - left; }
        long height() { return (long) bottom - top; }
        boolean valid() { return width() > 200 && height() > 300; }
    }
    public record Node(String id, String packageName, String type, boolean visible,
            boolean focused, boolean editable, Bounds bounds, int parent) { }
    public enum State { NOT_LIVE, REJECTED, LIVE }
    public record Result(State state, int recyclerIndex, int pageIndex, int liveIndex) { }
    private static Result rejected() { return new Result(State.REJECTED, -1, -1, -1); }

    public static Result evaluate(List<Node> nodes, boolean complete) {
        if (nodes == null || nodes.isEmpty() || nodes.size() > MAX_NODES) return rejected();
        boolean hasLive = false;
        for (Node node : nodes) {
            if (node == null) return rejected();
            if (node.visible && LIVE.equals(node.id)) hasLive = true;
        }
        if (!hasLive) return new Result(State.NOT_LIVE, -1, -1, -1);
        // Missing children or a truncated tree must never authorize live skipping,
        // but must not replace the legacy normal-video reader when no marker exists.
        if (!complete) return rejected();
        boolean[] visible = new boolean[nodes.size()];
        int recycler = -1, page = -1, live = -1;
        int recyclers = 0, pages = 0, lives = 0, seeks = 0;
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if ((i == 0 && node.parent != -1) || (i > 0 && (node.parent < 0 || node.parent >= i))) return rejected();
            visible[i] = node.visible && (i == 0 || visible[node.parent]);
            // A raw visible blocker is rejected even if an ancestor's visibility is inconsistent.
            if (node.visible && blocks(node.id, node.type, node.focused, node.editable)) return rejected();
            if (!visible[i]) {
                if (node.visible && LIVE.equals(node.id)) return rejected();
                continue;
            }
            if ("android.widget.SeekBar".equals(node.type)) seeks++;
            if (RECYCLER.equals(node.id)) { recycler = i; recyclers++; }
            if (PAGE.equals(node.id)) { page = i; pages++; }
            if (LIVE.equals(node.id)) { live = i; lives++; }
        }
        if (recyclers != 1 || pages != 1 || lives != 1 || seeks != 0) return rejected();
        if (!descendant(nodes, page, recycler) || !descendant(nodes, live, page)) return rejected();
        if (!trustedPath(nodes, live) || !nearlySame(nodes.get(page).bounds, nodes.get(recycler).bounds)
                || !nearlySame(nodes.get(live).bounds, nodes.get(page).bounds)) return rejected();
        return new Result(State.LIVE, recycler, page, live);
    }

    private static boolean descendant(List<Node> nodes, int child, int ancestor) {
        for (int i = nodes.get(child).parent; i >= 0; i = nodes.get(i).parent) if (i == ancestor) return true;
        return false;
    }
    private static boolean trustedPath(List<Node> nodes, int index) {
        for (int i = index; i >= 0; i = nodes.get(i).parent) if (!PACKAGE.equals(nodes.get(i).packageName)) return false;
        return true;
    }
    /** One percent, capped at eight physical pixels, tolerates rounding but not a sliding page. */
    public static boolean nearlySame(Bounds a, Bounds b) {
        if (a == null || b == null || !a.valid() || !b.valid()) return false;
        long x = Math.max(2, Math.min(8, b.width() / 100));
        long y = Math.max(2, Math.min(8, b.height() / 100));
        return Math.abs((long) a.left - b.left) <= x && Math.abs((long) a.right - b.right) <= x
                && Math.abs((long) a.top - b.top) <= y && Math.abs((long) a.bottom - b.bottom) <= y;
    }
    public static boolean blocks(String id, String type, boolean focused, boolean editable) {
        String lower = id == null ? "" : id.toLowerCase(Locale.ROOT);
        return "android.widget.EditText".equals(type) || "android.webkit.WebView".equals(type)
                || "android.app.Dialog".equals(type) || "android.widget.PopupWindow".equals(type)
                || lower.contains("bottom_sheet") || lower.contains("engagement_panel")
                || lower.contains("comments_recycler") || lower.contains("comment_entry")
                || lower.contains("menu_list") || lower.contains("action_sheet")
                || lower.contains("dialog") || lower.contains("popup") || (focused && editable);
    }

    /** Geometry must remain unchanged across two fresh observations, not merely look nearly full once. */
    public static final class Stability {
        private Bounds recycler, page, live;
        private long since = -1, last = -1;
        public boolean observe(boolean sameNode, Bounds r, Bounds p, Bounds l, long now) {
            if (now < 0 || !nearlySame(p, r) || !nearlySame(l, p)) { reset(); return false; }
            // A same-millisecond action recheck preserves settled evidence but adds no time.
            boolean continuous = sameNode && since >= 0 && now >= last && now - last <= 1500
                    && r.equals(recycler) && p.equals(page) && l.equals(live);
            if (!continuous) { since = now; recycler = r; page = p; live = l; }
            last = now;
            return continuous && now - since >= 150;
        }
        public void reset() { recycler = null; page = null; live = null; since = -1; last = -1; }
    }
}
