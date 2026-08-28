package com.fullmetalsonic.shortsloop.detection;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import com.fullmetalsonic.shortsloop.core.NormalizedProgress;

/** Synthetic framework-node identity/metadata checks; no TikTok app or permissions. */
public final class TikTokNodeChecks {
    private TikTokNodeChecks() { }
    @SuppressWarnings("deprecation")
    public static int run(Context context) {
        AccessibilityNodeInfo a = AccessibilityNodeInfo.obtain(new View(context));
        AccessibilityNodeInfo copy = AccessibilityNodeInfo.obtain(a);
        AccessibilityNodeInfo b = AccessibilityNodeInfo.obtain(new View(context));
        try {
            String first = TikTokReader.sourceKey("page", a);
            require(first.equals(TikTokReader.sourceKey("page", copy)), "Same node copy keeps identity");
            require(!first.equals(TikTokReader.sourceKey("page", b)), "Different synthetic source differs");
            b.setText("Synthetic title"); require(first.equals(TikTokReader.sourceKey("page", a)), "A-B-A is stateless");
            a.setText("Changed synthetic title"); require(first.equals(TikTokReader.sourceKey("page", a)), "Caption is not identity");
            YouTubeSnapshot snapshot = YouTubeSnapshot.normalizedVideo(first, new Rect(0, 0, 1000, 1600), new NormalizedProgress(.25))
                    .withNormalizedIdentity("pager", "media", -1).withIdentity("package|" + first)
                    .inWindow(7, new Rect(0, 0, 1000, 1700)).withContentIdentity("extra").withPhotoPageKey("");
            require(snapshot.normalizedUsable() && !snapshot.usable() && snapshot.progress == null, "Normalized data never becomes seconds");
            require(snapshot.normalizedPagerKey.equals("pager") && snapshot.normalizedMediaKey.equals("media"), "Independent source keys survive copies");
            require(snapshot.windowId == 7 && snapshot.normalizedPageIndex == -1, "Unknown index stays unknown");
            return 7;
        } finally { a.recycle(); copy.recycle(); b.recycle(); }
    }
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
