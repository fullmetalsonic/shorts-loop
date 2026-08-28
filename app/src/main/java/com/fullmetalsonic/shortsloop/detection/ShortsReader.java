package com.fullmetalsonic.shortsloop.detection;

import android.view.accessibility.AccessibilityNodeInfo;
import com.fullmetalsonic.shortsloop.data.SettingsStore;

/** Selection is checked before either app's accessibility tree is traversed. */
public final class ShortsReader {
    private final YouTubeReader youtube = new YouTubeReader();
    private final InstagramReader instagram = new InstagramReader();

    /** Release the bounded RAM-only live page identity when the owning service is destroyed. */
    public void close() { youtube.close(); }

    public YouTubeSnapshot read(AccessibilityNodeInfo root, SettingsStore store) {
        if (root == null || root.getPackageName() == null)
            return YouTubeSnapshot.unavailable("app.selected_wait");
        String packageName = root.getPackageName().toString();
        if (!store.isSelected(packageName)) return YouTubeSnapshot.unavailable("app.unselected");
        YouTubeSnapshot snapshot;
        if (YouTubeReader.PACKAGE.equals(packageName)) snapshot = youtube.read(root);
        else if (InstagramReader.PACKAGE.equals(packageName)) snapshot = instagram.read(root);
        else return YouTubeSnapshot.unavailable("app.supported");
        if (!snapshot.usable() && !snapshot.recognized()) return snapshot;
        // Preserve YouTube's unknown-identity sentinel: it must not confirm a swipe.
        String identity = snapshot.identity.isEmpty() ? "" : packageName + "|" + snapshot.identity;
        return snapshot.withIdentity(identity);
    }
}
