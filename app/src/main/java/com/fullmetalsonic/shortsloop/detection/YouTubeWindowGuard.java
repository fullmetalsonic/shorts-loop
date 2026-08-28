package com.fullmetalsonic.shortsloop.detection;

import android.view.accessibility.AccessibilityWindowInfo;
import android.graphics.Rect;
import java.util.List;

/** Window geometry guard. Non-focused split panes are allowed, covered/PiP panes are not. */
public final class YouTubeWindowGuard {
    public boolean allows(List<AccessibilityWindowInfo> windows, int videoWindowId) {
        return allowedBounds(windows, videoWindowId) != null;
    }
    public Rect allowedBounds(List<AccessibilityWindowInfo> windows, int videoWindowId) {
        return checkedBounds(windows, videoWindowId, null);
    }
    public boolean allowsInput(List<AccessibilityWindowInfo> windows, int videoWindowId, Rect expectedBounds, Rect corridor) {
        Rect current = checkedBounds(windows, videoWindowId, corridor);
        return corridor != null && expectedBounds != null && expectedBounds.equals(current);
    }
    /** Node actions have no touch path. Own non-focused floats do not intercept a semantic scroll. */
    public boolean allowsSemantic(List<AccessibilityWindowInfo> windows, int videoWindowId, Rect expectedBounds, Rect page) {
        Rect current = allowedBounds(windows, videoWindowId);
        return current != null && expectedBounds != null && current.equals(expectedBounds)
                && page != null && !page.isEmpty() && current.contains(page);
    }
    private Rect checkedBounds(List<AccessibilityWindowInfo> windows, int videoWindowId, Rect corridor) {
        try {
            java.util.List<com.fullmetalsonic.shortsloop.core.WindowPolicy.Window> metadata = new java.util.ArrayList<>();
            Rect target = null;
            for (AccessibilityWindowInfo window : windows) {
                Rect bounds = new Rect(); window.getBoundsInScreen(bounds);
                if (window.getId() == videoWindowId) target = new Rect(bounds);
                CharSequence owner = overlayOwner(window);
                metadata.add(new com.fullmetalsonic.shortsloop.core.WindowPolicy.Window(window.getId(), window.getType(),
                        window.getLayer(), window.isFocused(), window.isInPictureInPictureMode(),
                        com.fullmetalsonic.shortsloop.core.WindowPolicy.ownOverlay(window.getType(),window.isFocused(),owner),
                        new com.fullmetalsonic.shortsloop.core.WindowPolicy.Box(bounds.left,bounds.top,bounds.right,bounds.bottom),
                        com.fullmetalsonic.shortsloop.core.WindowPolicy.systemUi(window.getType(),owner)));
            }
            boolean allowed = corridor == null ? com.fullmetalsonic.shortsloop.core.WindowPolicy.allowed(metadata, videoWindowId)
                    : com.fullmetalsonic.shortsloop.core.WindowPolicy.inputClear(metadata, videoWindowId,
                        new com.fullmetalsonic.shortsloop.core.WindowPolicy.Box(corridor.left,corridor.top,corridor.right,corridor.bottom));
            return allowed ? target : null;
        } finally {
            for (AccessibilityWindowInfo window : windows) recycle(window);
        }
    }
    private CharSequence overlayOwner(AccessibilityWindowInfo window) {
        if (window.getType() != AccessibilityWindowInfo.TYPE_SYSTEM
                && window.getType() != AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY) return null;
        if (window.isFocused()) return null;
        android.view.accessibility.AccessibilityNodeInfo root = window.getRoot();
        // Some OEMs replace the LayoutParams title with a localized application label.
        // Ownership comes from the framework's node package, never presentation text.
        try { return root == null || root.getPackageName() == null ? null : root.getPackageName().toString(); }
        finally { YouTubeReader.recycle(root); }
    }
    @SuppressWarnings("deprecation")
    private static void recycle(AccessibilityWindowInfo window) { window.recycle(); }
}
