package com.fullmetalsonic.shortsloop.detection;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import com.fullmetalsonic.shortsloop.core.Progress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Synthetic node metadata and snapshot-copy checks only; never reads a social app. */
public final class ContentKeyChecks {
    private static final Rect PAGE = new Rect(0, 0, 1000, 1000);
    private static final Rect TEXT = new Rect(20, 700, 820, 800);
    private ContentKeyChecks() {}
    @SuppressWarnings("deprecation")
    public static int run(Context context) {
        int checks = 0;
        List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        List<Integer> parents = new ArrayList<>();
        try {
            add(nodes, parents, node(context, "root", "android.widget.FrameLayout", PAGE, ""), -1);
            add(nodes, parents, node(context, YouTubeReader.PACKAGE + ":id/reel_player_page_container",
                    "android.widget.FrameLayout", PAGE, ""), 0);
            AccessibilityNodeInfo title = node(context, null, "android.view.ViewGroup", TEXT, "Synthetic video alpha");
            add(nodes, parents, title, 1);
            String original = key(nodes, parents);
            require(!original.isEmpty() && !original.contains("Synthetic"), "Metadata becomes an opaque RAM key"); checks++;
            title.setClickable(true);
            require(original.equals(key(nodes, parents)), "Clickable ID-less metadata remains usable"); checks++;
            title.setViewIdResourceName(YouTubeReader.PACKAGE + ":id/metadata");
            require(original.equals(key(nodes, parents)), "Optional resource ID does not change metadata key"); checks++;
            title.setText("Synthetic video beta"); title.setContentDescription("Synthetic video beta");
            require(!original.equals(key(nodes, parents)), "Different video metadata changes supplemental key"); checks++;
            title.setText("Synthetic video alpha"); title.setContentDescription("Synthetic video alpha");
            title.setContentDescription("different description");
            require(key(nodes, parents).isEmpty(), "Mismatched text and description are excluded"); checks++;
            title.setContentDescription("Synthetic video alpha"); title.setClassName("android.widget.TextView");
            require(key(nodes, parents).isEmpty(), "Generic TextViews cannot become content metadata"); checks++;
            title.setClassName("android.view.ViewGroup");
            for (String excluded : new String[]{"", "  ", "ab", "12345", "1:23 / 2:34", "1시간 2분 3초", "12.4K"}) {
                title.setText(excluded); title.setContentDescription(excluded);
                require(key(nodes, parents).isEmpty(), "Blank, short, numeric and time labels excluded"); checks++;
            }
            title.setText("Synthetic video alpha"); title.setContentDescription("Synthetic video alpha");
            for (Rect excluded : new Rect[]{new Rect(20, 100, 820, 200), new Rect(600, 700, 950, 800),
                    new Rect(20, 700, 200, 800), new Rect(-1, 700, 820, 800), new Rect(20, 900, 820, 1100)}) {
                title.setBoundsInScreen(excluded);
                require(key(nodes, parents).isEmpty(), "Upper overlays, right controls, narrow and outside labels excluded"); checks++;
            }
            title.setBoundsInScreen(TEXT); parents.set(2, 0);
            require(key(nodes, parents).isEmpty(), "Bottom navigation outside page ancestry excluded"); checks++;
            parents.set(2, 1); title.setVisibleToUser(false);
            require(key(nodes, parents).isEmpty(), "Invisible metadata excluded"); checks++;
            title.setVisibleToUser(true);
            AccessibilityNodeInfo duplicate = node(context, null, "android.view.ViewGroup", TEXT, "Synthetic video alpha");
            add(nodes, parents, duplicate, 1);
            require(original.equals(key(nodes, parents)), "Duplicate wrappers cannot change metadata key"); checks++;
            AccessibilityNodeInfo audio = node(context, null, "android.view.ViewGroup", new Rect(20, 820, 820, 880), "Synthetic audio");
            add(nodes, parents, audio, 1);
            String combined = key(nodes, parents); Collections.swap(nodes, 2, 4);
            require(combined.equals(key(nodes, parents)), "Traversal order does not change key"); checks++;
            Collections.swap(nodes, 2, 4);
            require(YouTubeContentKey.read(nodes, parents, false).isEmpty(), "Incomplete tree has no supplemental identity"); checks++;
            AccessibilityNodeInfo otherPage = node(context, YouTubeReader.PACKAGE + ":id/reel_player_page_container",
                    "android.widget.FrameLayout", PAGE, "");
            add(nodes, parents, otherPage, 0);
            require(key(nodes, parents).isEmpty(), "Multiple visible pages are ambiguous"); checks++;
            otherPage.setVisibleToUser(false);
            require(combined.equals(key(nodes, parents)), "Hidden preloaded page does not replace current metadata"); checks++;
            nodes.get(1).setVisibleToUser(false);
            require(key(nodes, parents).isEmpty(), "No visible current page has no key"); checks++;
            nodes.get(1).setVisibleToUser(true);
            parents.set(2, 2); duplicate.setVisibleToUser(false); audio.setVisibleToUser(false);
            require(key(nodes, parents).isEmpty(), "Invalid ancestry cannot contribute title metadata"); checks++;
            parents.set(2, 1); duplicate.setVisibleToUser(true); audio.setVisibleToUser(true);
            YouTubeSnapshot snapshot = new YouTubeSnapshot(new Progress(1, 62), "ordinary-unchanged", PAGE, "");
            require(snapshot.contentIdentity.isEmpty(), "Existing snapshot constructors default to empty supplemental key"); checks++;
            YouTubeSnapshot wrapped = snapshot.withContentIdentity(combined).withIdentity("prefixed-ordinary")
                    .inWindow(7, PAGE);
            require(combined.equals(wrapped.contentIdentity) && wrapped.identity.equals("prefixed-ordinary")
                    && wrapped.windowId == 7 && wrapped.usable(), "All wrappers preserve separate metadata and normal contract"); checks++;
            require(snapshot.identity.equals("ordinary-unchanged") && snapshot.contentIdentity.isEmpty(), "Original snapshot unchanged"); checks++;
            require(wrapped.withContentIdentity(null).contentIdentity.isEmpty(), "Null supplemental key becomes absent"); checks++;
            for (YouTubeSnapshot special : new YouTubeSnapshot[]{YouTubeSnapshot.livePreview("live", PAGE),
                    YouTubeSnapshot.advertisement(PAGE), YouTubeSnapshot.withoutClock("clockless", PAGE, false),
                    YouTubeSnapshot.unavailable("waiting")}) {
                require(special.contentIdentity.isEmpty(), "Existing special pages have no synthetic content key"); checks++;
            }
            return checks;
        } finally { for (AccessibilityNodeInfo node : nodes) YouTubeReader.recycle(node); }
    }
    private static String key(List<AccessibilityNodeInfo> nodes, List<Integer> parents) {
        return YouTubeContentKey.read(nodes, parents, true);
    }
    private static void add(List<AccessibilityNodeInfo> nodes, List<Integer> parents, AccessibilityNodeInfo node, int parent) {
        nodes.add(node); parents.add(parent);
    }
    @SuppressWarnings("deprecation")
    private static AccessibilityNodeInfo node(Context context, String id, String type, Rect bounds, String value) {
        AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain(new View(context));
        node.setPackageName(YouTubeReader.PACKAGE); node.setViewIdResourceName(id); node.setClassName(type);
        node.setVisibleToUser(true); node.setBoundsInScreen(bounds); node.setText(value); node.setContentDescription(value);
        return node;
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
