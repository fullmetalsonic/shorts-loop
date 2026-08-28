package com.fullmetalsonic.shortsloop.detection;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;
import com.fullmetalsonic.shortsloop.core.Progress;
import com.fullmetalsonic.shortsloop.core.ProgressParser;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class YouTubeReader {
    public static final String PACKAGE = "com.google.android.youtube";
    private static final String PREFIX = PACKAGE + ":id/";
    private final YouTubeLiveReader liveReader = new YouTubeLiveReader();

    public YouTubeSnapshot read(AccessibilityNodeInfo root) {
        if (root == null || !PACKAGE.contentEquals(root.getPackageName() == null ? "" : root.getPackageName())) {
            liveReader.interrupt();
            return YouTubeSnapshot.unavailable("유튜브 쇼츠 대기");
        }
        List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        List<Integer> parents = new ArrayList<>();
        try {
            boolean complete = collect(root, nodes, parents, -1);
            // Only live skipping needs a complete-tree audit. Normal counting retains
            // its existing bounded, null-child-tolerant collection behavior.
            YouTubeSnapshot live = liveReader.read(nodes, parents, complete);
            if (live != null) return live;
            Rect page = null;
            boolean reel = false, timeBar = false;
            Progress progress = null;
            int validSeekBars = 0;
            StringBuilder identity = new StringBuilder();
            for (AccessibilityNodeInfo node : nodes) {
                if (!node.isVisibleToUser()) continue;
                String id = string(node.getViewIdResourceName());
                String type = string(node.getClassName());
                if (id.equals(PREFIX + "reel_recycler")) reel = true;
                if (id.equals(PREFIX + "reel_time_bar")) timeBar = true;
                if (id.equals(PREFIX + "reel_player_page_container")) {
                    Rect bounds = new Rect(); node.getBoundsInScreen(bounds);
                    if (bounds.width() > 200 && bounds.height() > 300) page = bounds;
                }
                if (isBlockingNode(id, type, node)) return YouTubeSnapshot.unavailable("댓글·메뉴 조작 중 · 대기");
                if (type.equals("android.widget.SeekBar")) {
                    // Playback may advance without a content-change event. The cached
                    // accessibility node can therefore retain an old time indefinitely.
                    if (!node.refresh() || !node.isVisibleToUser())
                        return YouTubeSnapshot.unavailable("재생 시간 갱신 대기");
                    Progress parsed = ProgressParser.parse(node.getContentDescription());
                    if (parsed != null) { progress = parsed; validSeekBars++; }
                }
                // Compare video text in memory only. Never persist or print the text itself.
                if (type.equals("android.widget.TextView") && !node.isClickable()
                        && !id.contains("count") && !id.contains("time") && !id.contains("badge")) {
                    String value = string(node.getText());
                    if (!value.isBlank() && !value.matches("[\\d\\s:./]+")) identity.append(value).append('|');
                }
            }
            if (!reel || !timeBar || page == null) return YouTubeSnapshot.unavailable("쇼츠 화면 대기");
            if (validSeekBars != 1) return YouTubeSnapshot.unavailable("재생 시간 확인 대기");
            return new YouTubeSnapshot(progress, digest(identity.toString()), page, "")
                    .withContentIdentity(YouTubeContentKey.read(nodes, parents, complete));
        } finally {
            for (AccessibilityNodeInfo node : nodes) if (node != root) recycle(node);
        }
    }
    private boolean isBlockingNode(String id, String type, AccessibilityNodeInfo node) {
        String lower = id.toLowerCase(Locale.ROOT);
        return type.equals("android.widget.EditText") || lower.contains("bottom_sheet")
                || lower.contains("engagement_panel") || lower.contains("comments_recycler")
                || lower.contains("comment_entry") || lower.contains("menu_list")
                || lower.contains("dialog") || (node.isFocused() && node.isEditable());
    }
    private boolean collect(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> nodes,
            List<Integer> parents, int parent) {
        if (nodes.size() >= 600) return false;
        int index = nodes.size();
        nodes.add(node);
        parents.add(parent);
        boolean complete = true;
        for (int i = 0; i < node.getChildCount(); i++) {
            if (nodes.size() >= 600) return false;
            AccessibilityNodeInfo child = node.getChild(i);
            if (child == null) { complete = false; continue; }
            if (!collect(child, nodes, parents, index)) complete = false;
        }
        return complete;
    }
    public void close() { liveReader.close(); }
    private static String string(CharSequence value) { return value == null ? "" : value.toString(); }
    private static String digest(String value) {
        if (value.isEmpty()) return "";
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < 12; i++) result.append(String.format(Locale.ROOT, "%02x", hash[i] & 255));
            return result.toString();
        } catch (NoSuchAlgorithmException ignored) { return ""; }
    }
    @SuppressWarnings("deprecation")
    public static void recycle(AccessibilityNodeInfo node) { if (node != null) node.recycle(); }
}
