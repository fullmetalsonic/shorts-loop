package com.fullmetalsonic.shortsloop.detection;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;
import com.fullmetalsonic.shortsloop.core.InstagramPolicy;
import com.fullmetalsonic.shortsloop.core.Progress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Instagram Reels only. Captions/usernames are hashed in memory and never retained. */
public final class InstagramReader {
    public static final String PACKAGE = "com.instagram.android";
    public static final String PAGER_ID = PACKAGE + ":id/clips_viewer_view_pager";
    private static final String PREFIX = PACKAGE + ":id/";
    private static final int MAX_NODES = 600;

    private static final class Entry {
        final AccessibilityNodeInfo node;
        final Entry parent;
        final boolean inPager;
        final boolean inCaption;
        final boolean inAuthor;
        final boolean inActions;
        Entry(AccessibilityNodeInfo node, Entry parent) {
            this.node = node; this.parent = parent;
            String id = node.getViewIdResourceName();
            inPager = PAGER_ID.equals(id) || (parent != null && parent.inPager);
            inCaption = (PREFIX + "clips_caption_component").equals(id) || (parent != null && parent.inCaption);
            inAuthor = (PREFIX + "clips_author_info_component").equals(id)
                    || (PREFIX + "clips_author_username").equals(id) || (parent != null && parent.inAuthor);
            inActions = (PREFIX + "clips_ufi_component").equals(id) || (parent != null && parent.inActions);
        }
    }

    public YouTubeSnapshot read(AccessibilityNodeInfo root) {
        if (root == null || !PACKAGE.contentEquals(string(root.getPackageName())))
            return YouTubeSnapshot.unavailable("인스타그램 릴스 대기");
        List<Entry> nodes = new ArrayList<>();
        try {
            if (!collect(root, nodes, null)) return YouTubeSnapshot.unavailable("화면이 복잡하여 감지 대기");
            Rect page = null, pager = null;
            int pagerCount = 0, mediaCount = 0, videoCount = 0, seekCount = 0, menuCount = 0;
            Entry scrubber = null;
            boolean unknownSeekBar = false, pausedControl = false;
            StringBuilder identity = new StringBuilder();
            // Audit the entire visible tree before accepting any ad label or progress.
            for (Entry entry : nodes) {
                AccessibilityNodeInfo node = entry.node;
                if (!node.isVisibleToUser()) continue;
                String id = string(node.getViewIdResourceName());
                String type = string(node.getClassName());
                if (entry.inPager && !entry.inCaption && !entry.inAuthor
                        && InstagramPolicy.isPlayControl(string(node.getContentDescription()))) pausedControl = true;
                if (InstagramPolicy.blocks(id, type, node.isFocused(), node.isEditable()))
                    return YouTubeSnapshot.unavailable("댓글·메뉴 조작 중 · 대기");
                if (entry.inPager && InstagramPolicy.unsupportedMedia(id))
                    return YouTubeSnapshot.unavailable("사진·혼합 릴스 · 대기");
                if (id.equals(PAGER_ID)) { pagerCount++; pager = bounds(node); }
                if (entry.inPager && id.equals(PREFIX + "clips_video_container")) { videoCount++; page = bounds(node); }
                if (entry.inPager && id.equals(PREFIX + "clips_single_media_component")) mediaCount++;
                if (entry.inPager && id.equals(PREFIX + "clips_ufi_more_button_component")) menuCount++;
                if (type.equals("android.widget.SeekBar")) {
                    seekCount++; scrubber = entry;
                    if (!entry.inPager || !id.equals(PREFIX + "scrubber")) unknownSeekBar = true;
                }
                boolean author = id.equals(PREFIX + "clips_author_username");
                if (entry.inPager && (author || entry.inCaption)) {
                    String text = string(node.getText());
                    if (text.isBlank()) text = string(node.getContentDescription());
                    if (!text.isBlank()) identity.append(author ? "author:" : "caption:").append(text).append('\n');
                }
            }
            if (pagerCount != 1 || !validPage(pager) || mediaCount > 1 || videoCount > 1)
                return YouTubeSnapshot.unavailable("단일 동영상 릴스 화면 대기");
            if (seekCount > 1 || unknownSeekBar) return YouTubeSnapshot.unavailable("릴스 재생 막대 확인 대기");

            boolean singleVideo = mediaCount == 1 && videoCount == 1 && validPage(page) && pager.contains(page);
            boolean endCard = mediaCount == 0 && videoCount == 0 && seekCount == 0;
            if ((singleVideo || endCard) && menuCount == 1 && hasAdIndicator(nodes, pager))
                return YouTubeSnapshot.advertisement(singleVideo ? page : pager);
            if (!singleVideo) return YouTubeSnapshot.unavailable("단일 동영상 릴스 화면 대기");

            String hash = digest(identity.toString());
            if (hash.isEmpty()) return YouTubeSnapshot.unavailable("릴스 구분 정보 대기");
            if (scrubber == null)
                return YouTubeSnapshot.withoutClock(hash, page, pausedControl);
            AccessibilityNodeInfo node = scrubber.node;
            // The same node can be cached through many playback samples.
            if (!node.refresh() || !node.isVisibleToUser()
                    || !(PREFIX + "scrubber").equals(node.getViewIdResourceName())
                    || !"android.widget.SeekBar".contentEquals(string(node.getClassName()))
                    || !PACKAGE.contentEquals(string(node.getPackageName())))
                return YouTubeSnapshot.unavailable("재생 시간 갱신 대기");
            AccessibilityNodeInfo.RangeInfo range = node.getRangeInfo();
            Progress progress = range == null ? null
                    : InstagramPolicy.progress(range.getType(), range.getMin(), range.getMax(), range.getCurrent());
            return progress == null ? YouTubeSnapshot.withoutClock(hash, page, pausedControl)
                    : new YouTubeSnapshot(progress, hash, page, "");
        } finally {
            for (Entry entry : nodes) if (entry.node != root) YouTubeReader.recycle(entry.node);
        }
    }

    private boolean hasAdIndicator(List<Entry> nodes, Rect pager) {
        for (Entry entry : nodes) {
            AccessibilityNodeInfo node = entry.node;
            if (!entry.inPager || !node.isVisibleToUser()) continue;
            String id = string(node.getViewIdResourceName());
            boolean excluded = entry.inCaption || entry.inAuthor;
            if (InstagramPolicy.isAdIndicator(id, string(node.getText()), excluded)) return true;
            if (entry.parent == null) continue;
            AccessibilityNodeInfo parent = entry.parent.node;
            Rect label = bounds(node), parentBounds = bounds(parent);
            boolean compactTopBadge = !label.isEmpty() && pager.contains(label)
                    && label.width() <= pager.width() * .25 && label.height() <= pager.height() * .1
                    && label.bottom <= pager.top + pager.height() * .5;
            boolean parentClickableGroup = parent.isVisibleToUser() && parent.isClickable()
                    && "android.view.ViewGroup".contentEquals(string(parent.getClassName()));
            if (InstagramPolicy.isAnonymousAdBadge(id, string(node.getClassName()), string(node.getText()),
                    string(node.getContentDescription()), node.isClickable(), parentClickableGroup,
                    label.equals(parentBounds), compactTopBadge, excluded)) return true;
            // Video ads place their badge under the right-hand action column;
            // its clickable hit target is larger than the text, unlike end cards.
            boolean compactBottomRight = !label.isEmpty() && pager.contains(parentBounds)
                    && parentBounds.contains(label) && parentBounds.width() <= pager.width() * .2
                    && parentBounds.height() <= pager.height() * .1
                    && label.left >= pager.left + pager.width() * .7
                    && label.top >= pager.top + pager.height() * .7;
            if (InstagramPolicy.isActionColumnAdBadge(id, string(node.getClassName()), string(node.getText()),
                    string(node.getContentDescription()), node.isClickable(), parentClickableGroup,
                    entry.inActions, compactBottomRight, excluded)) return true;
        }
        return false;
    }

    private static boolean validPage(Rect page) { return page != null && page.width() > 200 && page.height() > 300; }
    private static Rect bounds(AccessibilityNodeInfo node) { Rect result = new Rect(); node.getBoundsInScreen(result); return result; }

    /** An incomplete tree is rejected, never treated as proof that no dialog exists. */
    private boolean collect(AccessibilityNodeInfo node, List<Entry> nodes, Entry parent) {
        if (nodes.size() >= MAX_NODES) return false;
        Entry entry = new Entry(node, parent);
        nodes.add(entry);
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child == null) continue;
            if (nodes.size() >= MAX_NODES) { YouTubeReader.recycle(child); return false; }
            if (!collect(child, nodes, entry)) return false;
        }
        return true;
    }

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
}
