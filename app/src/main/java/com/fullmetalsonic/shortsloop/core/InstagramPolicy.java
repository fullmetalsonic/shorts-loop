package com.fullmetalsonic.shortsloop.core;

import java.util.Locale;

/** Known Instagram Reels accessibility formats only; no estimated video timers. */
public final class InstagramPolicy {
    private static final String PREFIX = "com.instagram.android:id/";
    private InstagramPolicy() {}

    /** Explicit play affordance outside captions/authors; absence does not prove playback. */
    public static boolean isPlayControl(String description) {
        if (description == null) return false;
        String value = description.trim().toLowerCase(Locale.ROOT);
        return value.equals("재생") || value.equals("동영상 재생") || value.equals("영상 재생")
                || value.equals("play") || value.equals("play video");
    }

    /** The observed scrubber exposes integer milliseconds, not seconds or percent. */
    public static Progress progress(int rangeType, double min, double max, double current) {
        if (rangeType != 0 || !Double.isFinite(min) || !Double.isFinite(max)
                || !Double.isFinite(current) || min != 0 || max < 3000 || max > 3_600_000
                || current < 0 || current > max || max != Math.rint(max)
                || current != Math.rint(current)) return null;
        Progress result = new Progress(current / 1000d, max / 1000d);
        return result.valid() ? result : null;
    }

    public static boolean blocks(String resourceId, String className, boolean focused, boolean editable) {
        String id = resourceId == null ? "" : resourceId.toLowerCase(Locale.ROOT);
        return "android.widget.EditText".equals(className) || "android.webkit.WebView".equals(className)
                || (focused && editable)
                || id.contains("bottom_sheet") || id.contains("action_sheet")
                || id.contains("dialog") || id.contains("menu_list") || id.contains("menu_container")
                || id.contains("comments_list") || id.contains("comments_recycler")
                || id.contains("comments_container") || id.contains("comments_sheet")
                || id.contains("comment_composer") || id.contains("comment_edit")
                || id.contains("comment_thread") || id.contains("comment_entry");
    }

    /** Do not interpret words in a reel caption or username as an advertisement. */
    public static boolean isAdIndicator(String resourceId, String text, boolean captionOrAuthor) {
        if (captionOrAuthor || resourceId == null || !resourceId.startsWith(PREFIX)) return false;
        String id = resourceId.substring(PREFIX.length()).toLowerCase(Locale.ROOT);
        if (id.contains("sponsored") || id.equals("ad_indicator") || id.equals("ad_label")
                || id.equals("ad_badge") || id.equals("ad_header") || id.equals("ad_cta_button")) return true;
        boolean label = id.equals("row_feed_subtitle") || id.equals("clips_subtitle")
                || id.equals("clips_sponsor_label");
        return label && text != null && (text.trim().equalsIgnoreCase("sponsored") || text.trim().equals("광고"));
    }

    public static boolean unsupportedMedia(String resourceId) {
        if (resourceId == null || !resourceId.startsWith(PREFIX)) return false;
        String id = resourceId.substring(PREFIX.length()).toLowerCase(Locale.ROOT);
        return id.startsWith("clips_carousel") || id.startsWith("clips_image")
                || id.startsWith("clips_photo") || id.startsWith("clips_multiple_media")
                || id.startsWith("clips_multi_media") || id.equals("carousel_media_group");
    }

    /** Observed standalone ad-info badge, never arbitrary text inside reel content. */
    public static boolean isAnonymousAdBadge(String resourceId, String className, String text, String description,
            boolean clickable, boolean parentClickableGroup, boolean sameBounds, boolean compactTopBadge,
            boolean excludedContext) {
        return !excludedContext && (resourceId == null || resourceId.isEmpty())
                && "android.view.ViewGroup".equals(className) && !clickable && parentClickableGroup
                && sameBounds && compactTopBadge && text != null && description != null
                && text.trim().equals(description.trim())
                && (text.trim().equals("광고") || text.trim().equalsIgnoreCase("sponsored"));
    }

    /** Observed video-ad badge below the Reels action buttons, with a padded hit target. */
    public static boolean isActionColumnAdBadge(String resourceId, String className, String text, String description,
            boolean clickable, boolean parentClickableGroup, boolean inActions, boolean compactBottomRight,
            boolean excludedContext) {
        return inActions && isAnonymousAdBadge(resourceId, className, text, description, clickable,
                parentClickableGroup, true, compactBottomRight, excludedContext);
    }
}
