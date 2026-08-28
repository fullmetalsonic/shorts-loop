package com.fullmetalsonic.shortsloop.detection;

import android.graphics.Rect;
import com.fullmetalsonic.shortsloop.core.Progress;

public final class YouTubeSnapshot {
    public final Progress progress;
    public final String identity;
    public final Rect page;
    public final String reason;
    public final boolean ad;
    public final boolean live;
    public final boolean visualCandidate;
    public final int windowId;
    public final Rect windowBounds;
    public YouTubeSnapshot(Progress progress, String identity, Rect page, String reason) {
        this(progress, identity, page, reason, false);
    }
    public YouTubeSnapshot(Progress progress, String identity, Rect page, String reason, boolean ad) {
        this(progress, identity, page, reason, ad, false, false, -1, null);
    }
    private YouTubeSnapshot(Progress progress, String identity, Rect page, String reason, boolean ad,
            boolean visualCandidate, boolean live, int windowId, Rect windowBounds) {
        this.progress = progress; this.identity = identity; this.page = page; this.reason = reason; this.ad = ad;
        this.visualCandidate = visualCandidate; this.live = live; this.windowId = windowId;
        this.windowBounds = windowBounds == null ? null : new Rect(windowBounds);
    }
    public static YouTubeSnapshot withoutClock(String identity, Rect page, boolean paused) {
        return new YouTubeSnapshot(null, identity, page, paused ? "릴스 일시정지 · 대기"
                : "재생 정보 없음 · 이 릴스는 수동 넘김 필요", false, !paused, false, -1, null);
    }
    public YouTubeSnapshot inWindow(int id, Rect bounds) {
        return new YouTubeSnapshot(progress, identity, page, reason, ad, visualCandidate, live, id, bounds);
    }
    public YouTubeSnapshot withIdentity(String value) {
        return new YouTubeSnapshot(progress, value, page, reason, ad, visualCandidate, live, windowId, windowBounds);
    }
    public boolean recognized() { return page != null && identity != null && !identity.isEmpty(); }
    public boolean usable() { return progress != null && page != null && !ad && !live; }
    public static YouTubeSnapshot livePreview(String identity, Rect page) {
        return new YouTubeSnapshot(null, identity, page, "YouTube 라이브 · 대기", false, false, true, -1, null);
    }
    public static YouTubeSnapshot unavailable(String reason) { return new YouTubeSnapshot(null, "", null, reason); }
    // Deliberately constant: changing ad creatives must not permit repeated swipe attempts.
    public static YouTubeSnapshot advertisement(Rect page) {
        return new YouTubeSnapshot(null, "instagram-ad", page, "광고 릴스 · 대기", true);
    }
}
