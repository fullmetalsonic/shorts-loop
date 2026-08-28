package com.fullmetalsonic.shortsloop.core;

import java.util.List;
import java.util.Locale;

/** Bounded structural checks separated from Android node refresh for synthetic regression tests. */
public final class TikTokStructurePolicy {
    public static final String PACKAGE = "com.ss.android.ugc.trill";
    public static final String PREFIX = PACKAGE + ":id/";
    public static final int MAX_NODES = 700, MAX_DEPTH = 48;
    private TikTokStructurePolicy() { }
    public static final class Node {
        public final String id, type;
        public final int parent, left, top, right, bottom;
        public final boolean visible, selected, editable, blocked, paused, recommended, home;
        public Node(String id, String type, int parent, int left, int top, int right, int bottom,
                boolean visible, boolean selected, boolean editable, boolean blocked, boolean paused,
                boolean recommended, boolean home) {
            this.id = id; this.type = type; this.parent = parent;
            this.left = left; this.top = top; this.right = right; this.bottom = bottom;
            this.visible = visible; this.selected = selected; this.editable = editable;
            this.blocked = blocked; this.paused = paused; this.recommended = recommended; this.home = home;
        }
        boolean valid() { return right > left && bottom > top; }
        boolean contains(Node other) { return valid() && other.valid() && left <= other.left && top <= other.top
                && right >= other.right && bottom >= other.bottom; }
        boolean same(Node other) { return left == other.left && top == other.top && right == other.right && bottom == other.bottom; }
    }
    public static final class Match {
        public final int pager, page, media, seek;
        public final String reason;
        private Match(int pager, int page, int media, int seek, String reason) {
            this.pager = pager; this.page = page; this.media = media; this.seek = seek; this.reason = reason;
        }
        public boolean accepted() { return reason.isEmpty(); }
    }
    public static Match inspect(List<Node> nodes) {
        if (nodes == null || nodes.isEmpty() || nodes.size() > MAX_NODES) return rejected("screen.complex");
        int pager = -1, page = -1, media = -1, seek = -1;
        boolean recommended = false, home = false;
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            if (n.parent >= i || n.parent < -1) return rejected("screen.complex");
            if (!n.visible) continue;
            if (n.editable || n.blocked) return rejected("screen.interaction");
            if (n.paused) return rejected("tiktok.paused");
            recommended |= n.selected && n.recommended;
            home |= n.selected && n.home;
            if (id(n, "viewpager") && n.type.equals("androidx.viewpager.widget.ViewPager")
                    && ancestor(nodes, i, "view_pager_layout_wrapper") && ancestor(nodes, i, "viewpager_container")) {
                if (pager >= 0 || !n.valid()) return rejected("tiktok.unsupported");
                pager = i;
            }
        }
        if (!recommended || !home || pager < 0) return rejected("tiktok.waiting");
        Node pagerNode = nodes.get(pager);
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            if (!n.visible) continue;
            if (n.parent == pager) {
                if (!id(n, "view_rootview") || !n.same(pagerNode) || page >= 0)
                    return rejected("tiktok.unsupported");
                page = i;
            }
        }
        if (page < 0 || pagerNode.right - pagerNode.left < 200 || pagerNode.bottom - pagerNode.top < 300)
            return rejected("tiktok.unsupported");
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            if (!n.visible) continue;
            boolean inPage = descendant(nodes, i, page);
            if (inPage && specialId(n.id)) return rejected("tiktok.unsupported");
            if (inPage && n.type.equals("android.view.TextureView") && ancestor(nodes, i, "player_view")
                    && ancestor(nodes, i, "video_visible_area_container") && ancestor(nodes, i, "video_container_area")) {
                if (media >= 0 || !pagerNode.contains(n)) return rejected("tiktok.unsupported");
                media = i;
            }
            if (n.type.equals("android.widget.SeekBar")) {
                if (seek >= 0 || !id(n, "vb6") || !ancestor(nodes, i, "video_seek_bar")
                        || !pagerNode.contains(n)) return rejected("tiktok.unsupported");
                seek = i;
            }
        }
        if (media < 0) return rejected("tiktok.unsupported");
        if (seek < 0) return rejected("tiktok.no_progress");
        return new Match(pager, page, media, seek, "");
    }
    private static boolean id(Node n, String suffix) { return n.id.equals(PREFIX + suffix); }
    private static boolean ancestor(List<Node> nodes, int index, String suffix) {
        int steps = 0;
        for (int p = nodes.get(index).parent; p >= 0 && ++steps <= MAX_DEPTH; p = nodes.get(p).parent)
            if (id(nodes.get(p), suffix) && nodes.get(p).visible) return true;
        return false;
    }
    private static boolean descendant(List<Node> nodes, int index, int target) {
        int steps = 0;
        for (int p = index; p >= 0 && ++steps <= MAX_DEPTH; p = nodes.get(p).parent) if (p == target) return true;
        return false;
    }
    private static Match rejected(String reason) { return new Match(-1, -1, -1, -1, reason); }
    /** Supplemental exclusions, not claims of universal TikTok ad/photo/LIVE recognition. */
    public static boolean specialId(String id) {
        String suffix = id.startsWith(PREFIX) ? id.substring(PREFIX.length()).toLowerCase(Locale.ROOT) : "";
        return suffix.equals("zb1") || suffix.equals("zax") || suffix.startsWith("ad_")
                || suffix.startsWith("ads_") || suffix.contains("sponsor") || suffix.contains("live_")
                || suffix.contains("photo_") || suffix.contains("image_slide") || suffix.contains("carousel");
    }
    public static boolean interactionType(String type) {
        return type.equals("android.webkit.WebView") || type.equals("android.widget.EditText")
                || type.contains("Dialog") || type.contains("PopupWindow");
    }
    public static boolean isRecommended(String text) { return text.equals("추천") || text.equalsIgnoreCase("For You"); }
    public static boolean isHome(String text) { return text.equals("홈") || text.equalsIgnoreCase("Home"); }
    public static boolean isPlay(String text) { return text.equals("재생") || text.equalsIgnoreCase("Play")
            || text.equals("동영상 재생") || text.equalsIgnoreCase("Play video"); }
    public static boolean specialLabel(String text) { return text.equals("광고") || text.equalsIgnoreCase("Sponsored")
            || text.equalsIgnoreCase("Advertisement") || text.equalsIgnoreCase("LIVE")
            || text.equals("라이브") || text.equals("사진 모드") || text.equalsIgnoreCase("Photo mode"); }
}
