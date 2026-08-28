package com.fullmetalsonic.shortsloop.detection;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

/** Supplemental RAM-only metadata key; never replaces the ordinary playback identity. */
final class YouTubeContentKey {
    private static final String PAGE = YouTubeReader.PACKAGE + ":id/reel_player_page_container";
    private YouTubeContentKey() {}

    static String read(List<AccessibilityNodeInfo> nodes, List<Integer> parents, boolean complete) {
        if (!complete || nodes == null || parents == null || nodes.size() != parents.size()) return "";
        int pageIndex = -1;
        Rect pageBounds = new Rect();
        for (int index = 0; index < nodes.size(); index++) {
            AccessibilityNodeInfo node = nodes.get(index);
            if (!node.isVisibleToUser() || !PAGE.equals(node.getViewIdResourceName())) continue;
            if (pageIndex >= 0) return "";
            pageIndex = index; node.getBoundsInScreen(pageBounds);
        }
        if (pageIndex < 0 || pageBounds.width() <= 200 || pageBounds.height() <= 300) return "";
        TreeSet<String> values = new TreeSet<>();
        for (int index = pageIndex + 1; index < nodes.size(); index++) {
            AccessibilityNodeInfo node = nodes.get(index);
            if (!node.isVisibleToUser() || !"android.view.ViewGroup".contentEquals(string(node.getClassName()))
                    || !visibleDescendant(index, pageIndex, nodes, parents)) continue;
            String text = string(node.getText()), description = string(node.getContentDescription());
            if (!text.equals(description)) continue;
            String value = text.trim();
            if (value.length() < 3 || value.isBlank() || numericOrTime(value)) continue;
            Rect bounds = new Rect(); node.getBoundsInScreen(bounds);
            if (bounds.isEmpty() || !pageBounds.contains(bounds)
                    || bounds.top < pageBounds.top + pageBounds.height() * 0.6
                    || bounds.left >= pageBounds.left + pageBounds.width() * 0.5
                    || bounds.width() < pageBounds.width() * 0.3) continue;
            values.add(value);
        }
        if (values.isEmpty()) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Sorted, de-duplicated, length-delimited values ignore traversal order
            // and duplicate wrappers without merging different text sequences.
            for (String value : values) {
                byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                digest.update(Integer.toString(bytes.length).getBytes(StandardCharsets.US_ASCII));
                digest.update((byte)':'); digest.update(bytes);
            }
            byte[] hash = digest.digest();
            StringBuilder key = new StringBuilder("youtube-content:");
            for (int index = 0; index < 12; index++) key.append(String.format(Locale.ROOT, "%02x", hash[index] & 255));
            return key.toString();
        } catch (NoSuchAlgorithmException unavailable) { return ""; }
    }

    private static boolean visibleDescendant(int index, int page, List<AccessibilityNodeInfo> nodes, List<Integer> parents) {
        int previous = index;
        for (int visited = 0; visited < nodes.size(); visited++) {
            Integer parent = parents.get(previous);
            if (parent == null || parent < 0 || parent >= previous || !nodes.get(parent).isVisibleToUser()) return false;
            if (parent == page) return true;
            previous = parent;
        }
        return false;
    }
    private static boolean numericOrTime(String value) {
        return value.matches("[\\d\\s:./,\\-+%]+")
                || value.matches("[\\d.,]+\\s*(?:[kKmMbB]|천|만|억)")
                || value.matches("(?i)(?:[\\d.,]+\\s*(?:시간|분|초|h|hr|hrs|m|min|mins|s|sec|secs)\\s*)+");
    }
    private static String string(CharSequence value) { return value == null ? "" : value.toString(); }
}
