package com.fullmetalsonic.shortsloop.detection;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

/** Framework node-copy/identity checks using synthetic Views only; no social app or permission use. */
public final class YouTubeLiveChecks {
    private YouTubeLiveChecks() { }
    @SuppressWarnings("deprecation")
    public static int run(Context context) {
        AccessibilityNodeInfo first = AccessibilityNodeInfo.obtain(new View(context));
        AccessibilityNodeInfo copy = AccessibilityNodeInfo.obtain(first);
        AccessibilityNodeInfo second = AccessibilityNodeInfo.obtain(new View(context));
        YouTubeLiveReader.PageIdentity identity = new YouTubeLiveReader.PageIdentity();
        try {
            String key = identity.key(first);
            first.setText("synthetic title 1"); first.setContentDescription("synthetic viewers 10");
            require(key.equals(identity.key(first)), "Title and viewer mutations keep live identity");
            first.recycle(); first = null;
            require(key.equals(identity.key(copy)), "Reader copy survives caller recycle");
            copy.setText("synthetic title 2"); copy.setContentDescription("synthetic CTA");
            require(key.equals(identity.key(copy)), "Same source retains identity across later metadata");
            require(!key.equals(identity.key(second)), "Distinct source page can have distinct identity");
            String secondKey = identity.key(second);
            require(secondKey.equals(identity.key(second)), "A reused source never becomes new from content alone");
            YouTubeSnapshot live = YouTubeSnapshot.livePreview(secondKey, new Rect(0, 0, 1000, 1700));
            YouTubeSnapshot wrapped = live.withIdentity("com.google.android.youtube|" + secondKey)
                    .inWindow(7, new Rect(0, 0, 1000, 1800));
            require(wrapped.live && wrapped.recognized() && !wrapped.usable() && !wrapped.visualCandidate && !wrapped.ad,
                    "Live metadata stays recognized and outside normal/visual counting");
            require(wrapped.windowId == 7 && wrapped.windowBounds.equals(new Rect(0, 0, 1000, 1800)), "Window copied");
            return 7;
        } finally {
            identity.close();
            YouTubeReader.recycle(first); YouTubeReader.recycle(copy); YouTubeReader.recycle(second);
        }
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
