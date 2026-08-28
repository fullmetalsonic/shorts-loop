package com.fullmetalsonic.shortsloop.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.view.accessibility.AccessibilityNodeInfo;
import com.fullmetalsonic.shortsloop.core.PhotoReelTracker;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.detection.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Re-reads window, full tree, image and index immediately before a single scoped request. */
final class PhotoGestureDispatcher {
    private PhotoGestureDispatcher() { }
    static boolean samePhoto(YouTubeSnapshot a, YouTubeSnapshot b) {
        return a != null && b != null && a.photo != null && b.photo != null && a.recognized() && b.recognized()
                && a.windowId == b.windowId && Objects.equals(a.windowBounds, b.windowBounds)
                && a.ad == b.ad && a.live == b.live
                && Objects.equals(a.identity, b.identity) && Objects.equals(a.page, b.page)
                && Objects.equals(a.photoPageKey, b.photoPageKey)
                && Objects.equals(a.normalizedPagerKey, b.normalizedPagerKey)
                && a.normalizedPageIndex == b.normalizedPageIndex
                && a.photo.image.equals(b.photo.image) && a.photo.position.equals(b.photo.position);
    }
    static boolean dispatch(HostPlaybackSession service, ShortsReader reader, YouTubeWindowGuard guard,
            SettingsStore store, YouTubeSnapshot expected, PhotoReelTracker.Action action, Rect overlay,
            AccessibilityService.GestureResultCallback callback, Handler handler) {
        AccessibilityNodeInfo root = service.getHostRoot();
        List<AccessibilityNodeInfo> pagers = Collections.emptyList();
        AccessibilityNodeInfo tiktokPager = null;
        try {
            String host = store.hostPackage();
            boolean tiktok = TikTokReader.PACKAGE.equals(host);
            if (!store.enabled() || !store.photoEnabled() || !store.isSelected(host) || root == null || !root.refresh()
                    || (!tiktok && !InstagramReader.PACKAGE.equals(host))
                    || !host.contentEquals(root.getPackageName() == null ? "" : root.getPackageName())) return false;
            Rect window = guard.allowedBounds(service.getWindows(), root.getWindowId());
            if (window == null || !window.equals(expected.windowBounds)) return false;
            YouTubeSnapshot fresh = reader.read(root, store).inWindow(root.getWindowId(), window);
            if (!samePhoto(expected, fresh) || (fresh.ad && store.skipAds())) return false;
            if (action == PhotoReelTracker.Action.SLIDE) {
                if (!fresh.photo.position.known() || fresh.photo.position.current() >= fresh.photo.position.total()) return false;
                Rect picture = fresh.photo.image;
                float start = picture.left + picture.width() * .74f, end = picture.left + picture.width() * .2f;
                for (float fraction : new float[]{.46f, .6f, .32f}) {
                    float y = picture.top + picture.height() * fraction;
                    Rect corridor = new Rect((int) end - 12, (int) y - 16, (int) start + 12, (int) y + 16);
                    if (!picture.contains(corridor) || Rect.intersects(overlay, corridor)) continue;
                    if (!guard.allowsInput(service.getWindows(), fresh.windowId, fresh.windowBounds, corridor)) continue;
                    Path path = new Path(); path.moveTo(start, y); path.lineTo(end, y);
                    GestureDescription gesture = new GestureDescription.Builder()
                            .addStroke(new GestureDescription.StrokeDescription(path, 0, 320)).build();
                    return service.dispatchGesture(gesture, callback, handler);
                }
                return false;
            }
            if (action != PhotoReelTracker.Action.REEL) return false;
            if (tiktok) {
                tiktokPager = TikTokReader.findPager(root, fresh, fresh.windowId);
                if (tiktokPager == null) return false;
                YouTubeSnapshot verified = reader.read(root, store).inWindow(root.getWindowId(), window);
                return samePhoto(fresh, verified) && store.enabled() && store.photoEnabled()
                        && !(verified.ad && store.skipAds())
                        && guard.allowsSemantic(service.getWindows(), verified.windowId, verified.windowBounds, verified.page)
                        && service.performScroll(tiktokPager);
            }
            pagers = root.findAccessibilityNodeInfosByViewId(InstagramReader.PAGER_ID);
            AccessibilityNodeInfo chosen = null;
            for (AccessibilityNodeInfo node : pagers) if (node.isVisibleToUser()) {
                if (chosen != null) return false;
                chosen = node;
            }
            if (chosen == null || !chosen.refresh() || !chosen.isVisibleToUser() || !chosen.isScrollable()
                    || chosen.getWindowId() != fresh.windowId || !InstagramReader.PAGER_ID.equals(chosen.getViewIdResourceName())
                    || !InstagramReader.PACKAGE.contentEquals(chosen.getPackageName() == null ? "" : chosen.getPackageName())) return false;
            Rect bounds = new Rect(); chosen.getBoundsInScreen(bounds);
            return bounds.contains(fresh.page) && service.performScroll(chosen);
        } finally {
            YouTubeReader.recycle(tiktokPager);
            for (AccessibilityNodeInfo pager : pagers) YouTubeReader.recycle(pager);
            YouTubeReader.recycle(root);
        }
    }
}
