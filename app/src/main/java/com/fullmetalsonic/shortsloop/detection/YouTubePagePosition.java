package com.fullmetalsonic.shortsloop.detection;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;
import com.fullmetalsonic.shortsloop.core.YouTubePageStepPolicy;

/** Reads the native page ordinal, not text or the unavailable scroll-event index. */
public final class YouTubePagePosition {
    private static final String PAGER = YouTubeReader.PACKAGE + ":id/reel_recycler";
    private static final String PAGE = YouTubeReader.PACKAGE + ":id/reel_player_page_container";
    private YouTubePagePosition() {}

    public static int read(AccessibilityNodeInfo pager, Rect expectedPage, int window) {
        if (pager == null || expectedPage == null || window < 0 || !pager.refresh()
                || !pager.isVisibleToUser() || pager.getWindowId() != window
                || !PAGER.equals(pager.getViewIdResourceName())
                || !YouTubeReader.PACKAGE.contentEquals(string(pager.getPackageName()))) return YouTubePageStepPolicy.UNSAFE;
        Rect bounds = new Rect(); pager.getBoundsInScreen(bounds);
        if (!bounds.contains(expectedPage) || pager.getChildCount() < 1 || pager.getChildCount() > 8)
            return YouTubePageStepPolicy.UNSAFE;
        int visiblePages = 0, result = YouTubePageStepPolicy.UNSAFE;
        for (int i = 0; i < pager.getChildCount(); i++) {
            AccessibilityNodeInfo child = pager.getChild(i);
            if (child == null) return YouTubePageStepPolicy.UNSAFE;
            try {
                if (!child.refresh()) return YouTubePageStepPolicy.UNSAFE;
                if (!child.isVisibleToUser()) continue;
                if (!PAGE.equals(child.getViewIdResourceName()) || ++visiblePages != 1)
                    return YouTubePageStepPolicy.UNSAFE;
                AccessibilityNodeInfo parent = child.getParent();
                try {
                    if (parent == null || !pager.equals(parent)) return YouTubePageStepPolicy.UNSAFE;
                } finally { YouTubeReader.recycle(parent); }
                result = indexOf(child, expectedPage, window);
            } finally { YouTubeReader.recycle(child); }
        }
        return visiblePages == 1 ? result : YouTubePageStepPolicy.UNSAFE;
    }

    /** Extraction is separate from live refresh so malformed fields can be tested synthetically. */
    static int indexOf(AccessibilityNodeInfo page, Rect expectedPage, int window) {
        if (page == null || expectedPage == null || !page.isVisibleToUser() || page.getWindowId() != window
                || !PAGE.equals(page.getViewIdResourceName())
                || !YouTubeReader.PACKAGE.contentEquals(string(page.getPackageName()))) return YouTubePageStepPolicy.UNSAFE;
        Rect bounds = new Rect(); page.getBoundsInScreen(bounds);
        if (bounds.isEmpty() || !bounds.equals(expectedPage)) return YouTubePageStepPolicy.UNSAFE;
        AccessibilityNodeInfo.CollectionItemInfo item = page.getCollectionItemInfo();
        if (item == null) return YouTubePageStepPolicy.UNKNOWN;
        if (item.getRowIndex() < 0 || item.getColumnIndex() != 0
                || item.getRowSpan() != 1 || item.getColumnSpan() != 1) return YouTubePageStepPolicy.UNSAFE;
        return item.getRowIndex();
    }
    private static CharSequence string(CharSequence value) { return value == null ? "" : value; }
}
