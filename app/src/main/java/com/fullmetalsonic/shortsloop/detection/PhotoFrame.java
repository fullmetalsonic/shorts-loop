package com.fullmetalsonic.shortsloop.detection;

import android.graphics.Rect;
import com.fullmetalsonic.shortsloop.core.PhotoReelPolicy;

/** Known visible image and optional slide index, never a playback clock. */
public final class PhotoFrame {
    public final Rect image;
    public final PhotoReelPolicy.Position position;
    public PhotoFrame(Rect image, PhotoReelPolicy.Position position) {
        this.image = new Rect(image); this.position = position;
    }
}
