package com.fullmetalsonic.shortsloop.detection;

import android.graphics.Rect;
import com.fullmetalsonic.shortsloop.core.Progress;
import com.fullmetalsonic.shortsloop.core.NormalizedProgress;

public final class YouTubeSnapshot {
    public final Progress progress;
    /** TikTok unitless progress. Never pass this range to second-based policies. */
    public final NormalizedProgress normalizedProgress;
    public final String normalizedPagerKey, normalizedMediaKey;
    public final int normalizedPageIndex;
    public final String identity;
    /** Optional page metadata key for strict duration-filter confirmation, not ordinary counting. */
    public final String contentIdentity;
    public final Rect page;
    public final String reason;
    public final boolean ad;
    public final boolean live;
    public final boolean visualCandidate;
    public final int windowId;
    public final Rect windowBounds;
    public final PhotoFrame photo;
    /** RAM-only source-node identity, independent of captions; used only by photo transitions. */
    public final String photoPageKey;
    public YouTubeSnapshot(Progress progress, String identity, Rect page, String reason) {
        this(progress, identity, page, reason, false);
    }
    public YouTubeSnapshot(Progress progress, String identity, Rect page, String reason, boolean ad) {
        this(progress, identity, page, reason, ad, false, false, -1, null, "");
    }
    private YouTubeSnapshot(Progress progress, String identity, Rect page, String reason, boolean ad,
            boolean visualCandidate, boolean live, int windowId, Rect windowBounds, String contentIdentity) {
        this(progress, identity, page, reason, ad, visualCandidate, live, windowId, windowBounds, contentIdentity, null);
    }
    private YouTubeSnapshot(Progress progress, String identity, Rect page, String reason, boolean ad,
            boolean visualCandidate, boolean live, int windowId, Rect windowBounds, String contentIdentity, PhotoFrame photo) {
        this(progress, identity, page, reason, ad, visualCandidate, live, windowId, windowBounds, contentIdentity, photo, "");
    }
    private YouTubeSnapshot(Progress progress, String identity, Rect page, String reason, boolean ad,
            boolean visualCandidate, boolean live, int windowId, Rect windowBounds, String contentIdentity, PhotoFrame photo, String photoPageKey) {
        this(progress, identity, page, reason, ad, visualCandidate, live, windowId, windowBounds,
                contentIdentity, photo, photoPageKey, null);
    }
    private YouTubeSnapshot(Progress progress, String identity, Rect page, String reason, boolean ad,
            boolean visualCandidate, boolean live, int windowId, Rect windowBounds, String contentIdentity,
            PhotoFrame photo, String photoPageKey, NormalizedProgress normalizedProgress) {
        this(progress, identity, page, reason, ad, visualCandidate, live, windowId, windowBounds,
                contentIdentity, photo, photoPageKey, normalizedProgress, "", "", -1);
    }
    private YouTubeSnapshot(Progress progress, String identity, Rect page, String reason, boolean ad,
            boolean visualCandidate, boolean live, int windowId, Rect windowBounds, String contentIdentity,
            PhotoFrame photo, String photoPageKey, NormalizedProgress normalizedProgress,
            String normalizedPagerKey, String normalizedMediaKey, int normalizedPageIndex) {
        this.progress = progress; this.identity = identity; this.page = page; this.reason = reason; this.ad = ad;
        this.normalizedProgress = normalizedProgress;
        this.normalizedPagerKey = normalizedPagerKey;
        this.normalizedMediaKey = normalizedMediaKey;
        this.normalizedPageIndex = normalizedPageIndex;
        this.visualCandidate = visualCandidate; this.live = live; this.windowId = windowId;
        this.contentIdentity = contentIdentity == null ? "" : contentIdentity;
        this.windowBounds = windowBounds == null ? null : new Rect(windowBounds);
        this.photo = photo;
        this.photoPageKey = photoPageKey;
    }
    public static YouTubeSnapshot photograph(String identity, Rect page, PhotoFrame photo) {
        return new YouTubeSnapshot(null, identity, page, "photo.ready", false, false, false, -1, null, "", photo);
    }
    public static YouTubeSnapshot withoutClock(String identity, Rect page, boolean paused) {
        return new YouTubeSnapshot(null, identity, page, paused ? "instagram.paused"
                : "instagram.no_progress", false, !paused, false, -1, null, "");
    }
    public YouTubeSnapshot inWindow(int id, Rect bounds) {
        return new YouTubeSnapshot(progress, identity, page, reason, ad, visualCandidate, live, id, bounds, contentIdentity, photo, photoPageKey, normalizedProgress, normalizedPagerKey, normalizedMediaKey, normalizedPageIndex);
    }
    public YouTubeSnapshot withIdentity(String value) {
        return new YouTubeSnapshot(progress, value, page, reason, ad, visualCandidate, live, windowId, windowBounds, contentIdentity, photo, photoPageKey, normalizedProgress, normalizedPagerKey, normalizedMediaKey, normalizedPageIndex);
    }
    public YouTubeSnapshot withContentIdentity(String value) {
        return new YouTubeSnapshot(progress, identity, page, reason, ad, visualCandidate, live, windowId, windowBounds, value, photo, photoPageKey, normalizedProgress, normalizedPagerKey, normalizedMediaKey, normalizedPageIndex);
    }
    public YouTubeSnapshot withPhotoPageKey(String value) {
        return new YouTubeSnapshot(progress, identity, page, reason, ad, visualCandidate, live, windowId, windowBounds, contentIdentity, photo, value == null ? "" : value, normalizedProgress, normalizedPagerKey, normalizedMediaKey, normalizedPageIndex);
    }
    /** Policy-only copy. Preserve the original reader snapshot for fresh source/type verification. */
    public YouTubeSnapshot withAd(boolean value) {
        return new YouTubeSnapshot(progress, identity, page, reason, value, visualCandidate, live,
                windowId, windowBounds, contentIdentity, photo, photoPageKey, normalizedProgress,
                normalizedPagerKey, normalizedMediaKey, normalizedPageIndex);
    }
    public boolean recognized() { return page != null && identity != null && !identity.isEmpty(); }
    public boolean usable() { return progress != null && page != null && !ad && !live; }
    public boolean normalizedUsable() {
        return normalizedProgress != null && normalizedProgress.valid() && recognized() && !ad && !live && photo == null;
    }
    public static YouTubeSnapshot normalizedVideo(String identity, Rect page, NormalizedProgress progress) {
        return new YouTubeSnapshot(null, identity, page, "", false, false, false, -1, null, "", null, "", progress);
    }
    /** Retains overlapping advertising/photo evidence; session settings choose the policy. */
    public static YouTubeSnapshot tiktokPage(String identity, Rect page, Progress clock,
            NormalizedProgress normalized, boolean ad, boolean clocklessEligible, PhotoFrame photo) {
        String reason = ad ? "ads.waiting" : photo != null ? "photo.ready"
                : normalized != null || clock != null ? "" : "tiktok.no_progress";
        return new YouTubeSnapshot(clock, identity, page, reason, ad, clocklessEligible, false,
                -1, null, "", photo, "", normalized);
    }
    public YouTubeSnapshot withNormalizedIdentity(String pagerKey, String mediaKey, int pageIndex) {
        return new YouTubeSnapshot(progress, identity, page, reason, ad, visualCandidate, live, windowId,
                windowBounds, contentIdentity, photo, photoPageKey, normalizedProgress,
                pagerKey == null ? "" : pagerKey, mediaKey == null ? "" : mediaKey, pageIndex);
    }
    public static YouTubeSnapshot livePreview(String identity, Rect page) {
        return new YouTubeSnapshot(null, identity, page, "live.waiting", false, false, true, -1, null, "");
    }
    public static YouTubeSnapshot unavailable(String reason) { return new YouTubeSnapshot(null, "", null, reason); }
    // Deliberately constant: changing ad creatives must not permit repeated swipe attempts.
    public static YouTubeSnapshot advertisement(Rect page) {
        return new YouTubeSnapshot(null, "instagram-ad", page, "ads.waiting", true);
    }
}
