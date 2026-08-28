package com.fullmetalsonic.shortsloop.detection;

import android.view.accessibility.AccessibilityNodeInfo;

/** Stateless source-node fingerprint. Equal nodes have equal hashes, including A→B→A.
 * Hash collisions can only reject a real move, never make the same node look different. */
final class InstagramPageIdentity {
    String key(AccessibilityNodeInfo current) {
        return "instagram-page-node:" + current.getWindowId() + ":" + current.hashCode();
    }
}
