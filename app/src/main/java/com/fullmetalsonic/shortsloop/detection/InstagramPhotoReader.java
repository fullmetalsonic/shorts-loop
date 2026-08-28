package com.fullmetalsonic.shortsloop.detection;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;
import com.fullmetalsonic.shortsloop.core.InstagramPolicy;
import com.fullmetalsonic.shortsloop.core.PhotoReelPolicy;
import java.util.List;
import java.util.ArrayList;

/** Narrow support for an observed image carousel, not every unsupported/mixed media container. */
final class InstagramPhotoReader {
    static final String CAROUSEL = InstagramReader.PACKAGE + ":id/clips_carousel_viewpager";
    static final String IMAGE = InstagramReader.PACKAGE + ":id/clips_carousel_image_media_content";
    static final String INDEX = InstagramReader.PACKAGE + ":id/carousel_index_indicator_text_view";
    private InstagramPhotoReader() { }
    static YouTubeSnapshot read(List<InstagramReader.Entry> nodes, Rect pager, String identity) {
        InstagramReader.Entry carousel = null, image = null, index = null;
        List<InstagramReader.Entry> videoPlaceholders = new ArrayList<>();
        for (InstagramReader.Entry entry : nodes) {
            AccessibilityNodeInfo node = entry.node;
            if (!entry.inPager || !node.isVisibleToUser()) continue;
            String id = node.getViewIdResourceName();
            if (CAROUSEL.equals(id)) {
                if (carousel != null) return unavailable();
                carousel = entry;
            } else if (IMAGE.equals(id)) {
                if (image != null) return unavailable();
                image = entry;
            } else if (INDEX.equals(id) && !entry.inCaption && !entry.inAuthor) {
                if (index != null) return unavailable();
                index = entry;
            } else if ((InstagramReader.PACKAGE + ":id/clips_video_container").equals(id)) videoPlaceholders.add(entry);
            else if (InstagramPolicy.unsupportedMedia(id)) return unavailable();
        }
        if (carousel == null || image == null || identity.isEmpty() || !descendant(image, carousel)) return unavailable();
        // The observed photo page has one empty video placeholder in the same media component.
        // A populated/different-page video is not that placeholder and cannot become a photo timer.
        if (videoPlaceholders.size() > 1) return unavailable();
        for (InstagramReader.Entry entry : videoPlaceholders) {
            AccessibilityNodeInfo video = entry.node;
            if (!fresh(video, InstagramReader.PACKAGE + ":id/clips_video_container")
                    || video.getChildCount() != 0 || video.isClickable() || video.isFocused() || video.isEditable()
                    || !blank(video.getText())
                    || mediaParent(entry) == null || mediaParent(entry) != mediaParent(carousel)) return unavailable();
        }
        if (!fresh(carousel.node, CAROUSEL) || !fresh(image.node, IMAGE)
                || !"android.widget.ImageView".contentEquals(image.node.getClassName())) return unavailable();
        Rect page = bounds(carousel.node), picture = bounds(image.node);
        // Reject peeking/partially swiped slides: a single nearly full-width image must be inside its page.
        if (page.width() < 200 || page.height() < 300 || !pager.contains(page) || !page.contains(picture)
                || picture.width() < page.width() * .9 || picture.height() < page.height() * .2) return unavailable();
        PhotoReelPolicy.Position position = new PhotoReelPolicy.Position(0, 0);
        if (index != null) {
            if (!fresh(index.node, INDEX) || !page.contains(bounds(index.node))
                    || !"android.widget.TextView".contentEquals(index.node.getClassName())) return unavailable();
            position = PhotoReelPolicy.position(index.node.getText());
            if (position == null) return unavailable();
        }
        return YouTubeSnapshot.photograph(identity, page, new PhotoFrame(picture, position));
    }
    private static boolean blank(CharSequence value) { return value == null || value.length() == 0; }
    private static InstagramReader.Entry mediaParent(InstagramReader.Entry entry) {
        for (InstagramReader.Entry parent = entry.parent; parent != null; parent = parent.parent)
            if ((InstagramReader.PACKAGE + ":id/clips_media_component").equals(parent.node.getViewIdResourceName())) return parent;
        return null;
    }
    private static boolean descendant(InstagramReader.Entry child, InstagramReader.Entry parent) {
        for (InstagramReader.Entry entry = child.parent; entry != null; entry = entry.parent) if (entry == parent) return true;
        return false;
    }
    private static boolean fresh(AccessibilityNodeInfo node, String id) {
        return node.refresh() && node.isVisibleToUser() && id.equals(node.getViewIdResourceName())
                && InstagramReader.PACKAGE.contentEquals(node.getPackageName() == null ? "" : node.getPackageName());
    }
    private static Rect bounds(AccessibilityNodeInfo node) { Rect r = new Rect(); node.getBoundsInScreen(r); return r; }
    private static YouTubeSnapshot unavailable() { return YouTubeSnapshot.unavailable("instagram.mixed"); }
}
