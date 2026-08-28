package com.fullmetalsonic.shortsloop.detection;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import com.fullmetalsonic.shortsloop.core.YouTubePageStepPolicy;

/** Synthetic CollectionItemInfo extraction only; no live window, refresh or device input. */
public final class PagePositionChecks {
    private PagePositionChecks() {}
    @SuppressWarnings("deprecation")
    public static int run(Context context) {
        int checks = 0;
        Rect expected = new Rect(0, 0, 1000, 1700);
        AccessibilityNodeInfo page = AccessibilityNodeInfo.obtain(new View(context));
        try {
            page.setPackageName(YouTubeReader.PACKAGE);
            page.setViewIdResourceName(YouTubeReader.PACKAGE + ":id/reel_player_page_container");
            page.setVisibleToUser(true); page.setBoundsInScreen(expected);
            // Detached synthetic Views have no live window. Match that field for
            // extraction checks; public read() separately requires a real window.
            int window = page.getWindowId();
            require(YouTubePagePosition.indexOf(null, expected, window) == YouTubePageStepPolicy.UNSAFE, "Null page is unsafe"); checks++;
            require(YouTubePagePosition.indexOf(page, null, window) == YouTubePageStepPolicy.UNSAFE, "Null expected bounds unsafe"); checks++;
            require(YouTubePagePosition.indexOf(page, expected, window) == YouTubePageStepPolicy.UNKNOWN, "Missing item ordinal is unknown, not zero"); checks++;
            page.setCollectionItemInfo(item(0, 1, 0, 1));
            require(YouTubePagePosition.indexOf(page, expected, window) == 0, "Row zero is valid"); checks++;
            page.setCollectionItemInfo(item(1, 1, 0, 1));
            require(YouTubePagePosition.indexOf(page, expected, window) == 1, "Row one is valid"); checks++;
            page.setCollectionInfo(null);
            require(page.getCollectionInfo() == null && YouTubePagePosition.indexOf(page, expected, window) == 1,
                    "CollectionInfo is not required when CollectionItemInfo identifies the row"); checks++;
            page.setCollectionItemInfo(null);
            require(YouTubePagePosition.indexOf(page, expected, window) == YouTubePageStepPolicy.UNKNOWN, "Removed item info returns unknown"); checks++;
            page.setCollectionItemInfo(item(1, 1, 0, 1));
            page.setPackageName("synthetic.other");
            require(YouTubePagePosition.indexOf(page, expected, window) == YouTubePageStepPolicy.UNSAFE, "Wrong package unsafe"); checks++;
            page.setPackageName(null);
            require(YouTubePagePosition.indexOf(page, expected, window) == YouTubePageStepPolicy.UNSAFE, "Missing package unsafe"); checks++;
            page.setPackageName(YouTubeReader.PACKAGE); page.setViewIdResourceName("synthetic:other");
            require(YouTubePagePosition.indexOf(page, expected, window) == YouTubePageStepPolicy.UNSAFE, "Wrong page resource unsafe"); checks++;
            page.setViewIdResourceName(null);
            require(YouTubePagePosition.indexOf(page, expected, window) == YouTubePageStepPolicy.UNSAFE, "Missing page resource unsafe"); checks++;
            page.setViewIdResourceName(YouTubeReader.PACKAGE + ":id/reel_player_page_container");
            require(YouTubePagePosition.indexOf(page, expected, window + 1) == YouTubePageStepPolicy.UNSAFE, "Different window unsafe"); checks++;
            page.setBoundsInScreen(new Rect(0, 0, 1000, 1699));
            require(YouTubePagePosition.indexOf(page, expected, window) == YouTubePageStepPolicy.UNSAFE, "Different bounds unsafe"); checks++;
            page.setBoundsInScreen(new Rect());
            require(YouTubePagePosition.indexOf(page, new Rect(), window) == YouTubePageStepPolicy.UNSAFE, "Empty bounds unsafe even when equal"); checks++;
            page.setBoundsInScreen(expected); page.setVisibleToUser(false);
            require(YouTubePagePosition.indexOf(page, expected, window) == YouTubePageStepPolicy.UNSAFE, "Hidden page unsafe"); checks++;
            page.setVisibleToUser(true);
            for (int[] malformed : new int[][]{{-1, 1, 0, 1}, {0, 1, 1, 1}, {0, 0, 0, 1},
                    {0, 2, 0, 1}, {0, 1, 0, 0}, {0, 1, 0, 2}}) {
                page.setCollectionItemInfo(item(malformed[0], malformed[1], malformed[2], malformed[3]));
                require(YouTubePagePosition.indexOf(page, expected, window) == YouTubePageStepPolicy.UNSAFE,
                        "Negative row, nonzero column and non-unit spans unsafe"); checks++;
            }
            page.setCollectionItemInfo(item(Integer.MAX_VALUE, 1, 0, 1));
            require(YouTubePagePosition.indexOf(page, expected, window) == Integer.MAX_VALUE, "Large row remains exact for overflow-safe policy"); checks++;
            return checks;
        } finally { YouTubeReader.recycle(page); }
    }
    @SuppressWarnings("deprecation")
    private static AccessibilityNodeInfo.CollectionItemInfo item(int row, int rowSpan, int column, int columnSpan) {
        return AccessibilityNodeInfo.CollectionItemInfo.obtain(row, rowSpan, column, columnSpan, false, false);
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
