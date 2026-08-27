package com.fullmetalsonic.shortsloop.detection;

import android.graphics.Rect;
import com.fullmetalsonic.shortsloop.core.Progress;

public final class YouTubeSnapshot {
    public final Progress progress;
    public final String identity;
    public final Rect page;
    public final String reason;
    public final boolean ad;
    public YouTubeSnapshot(Progress progress, String identity, Rect page, String reason) {
        this(progress, identity, page, reason, false);
    }
    public YouTubeSnapshot(Progress progress, String identity, Rect page, String reason, boolean ad) {
        this.progress = progress; this.identity = identity; this.page = page; this.reason = reason; this.ad = ad;
    }
    public boolean recognized() { return page != null && identity != null && !identity.isEmpty(); }
    public boolean usable() { return progress != null && page != null && !ad; }
    public static YouTubeSnapshot unavailable(String reason) { return new YouTubeSnapshot(null, "", null, reason); }
    // Deliberately constant: changing ad creatives must not permit repeated swipe attempts.
    public static YouTubeSnapshot advertisement(Rect page) {
        return new YouTubeSnapshot(null, "instagram-ad", page, "광고 릴스 · 대기", true);
    }
}
