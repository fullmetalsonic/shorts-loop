package com.fullmetalsonic.shortsloop.detection;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import java.util.List;

/** Finds a host root by package, never by screen order or the active-window shortcut. */
public final class HostWindowAccess {
    private final AccessibilityService service;
    public HostWindowAccess(AccessibilityService service) { this.service = service; }
    public AccessibilityNodeInfo root(String host) {
        AccessibilityNodeInfo chosen = null;
        List<AccessibilityWindowInfo> windows = service.getWindows();
        try {
            for (AccessibilityWindowInfo window : windows) {
                if (window.getType() != AccessibilityWindowInfo.TYPE_APPLICATION || window.isInPictureInPictureMode()) continue;
                AccessibilityNodeInfo root = window.getRoot();
                if (root == null) continue;
                if (!host.contentEquals(root.getPackageName() == null ? "" : root.getPackageName()) || !root.isVisibleToUser()) {
                    YouTubeReader.recycle(root); continue;
                }
                // Multiple application windows for one host (dialog/pop-up) are ambiguous.
                if (chosen != null) { YouTubeReader.recycle(root); YouTubeReader.recycle(chosen); chosen = null; return null; }
                chosen = root;
            }
            AccessibilityNodeInfo result = chosen; chosen = null; return result;
        } finally {
            YouTubeReader.recycle(chosen);
            for (AccessibilityWindowInfo window : windows) window.recycle();
        }
    }
}
