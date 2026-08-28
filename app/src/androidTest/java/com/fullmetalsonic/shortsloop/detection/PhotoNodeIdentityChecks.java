package com.fullmetalsonic.shortsloop.detection;

import android.app.Activity;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.graphics.Rect;
import com.fullmetalsonic.shortsloop.core.PhotoTransition;
import com.fullmetalsonic.shortsloop.core.PhotoReelTracker;
import com.fullmetalsonic.shortsloop.core.PhotoReelPolicy;

/** Native source-node fingerprints and snapshot wrappers; not a simulated social-app E2E. */
public final class PhotoNodeIdentityChecks {
    @SuppressWarnings("deprecation")
    public static int run(Activity activity) {
        InstagramPageIdentity keys=new InstagramPageIdentity();
        View one=new View(activity),two=new View(activity);
        AccessibilityNodeInfo a=AccessibilityNodeInfo.obtain(one),b=AccessibilityNodeInfo.obtain(two),again=AccessibilityNodeInfo.obtain(a);
        try {
            String original=keys.key(a),other=keys.key(b),returned=keys.key(again);
            if(!a.equals(again)||a.hashCode()!=again.hashCode()||!original.equals(returned)) throw new AssertionError("A-B-A source-node rollback changed key");
            if(a.equals(b)||original.equals(other)) throw new AssertionError("Synthetic distinct source nodes expected");
            Rect page=new Rect(0,0,1000,1600);
            YouTubeSnapshot ad=YouTubeSnapshot.advertisement(page).withPhotoPageKey(other).withIdentity("ad").inWindow(7,page);
            if(!ad.ad||!ad.photoPageKey.equals(other)) throw new AssertionError("Known ad lost page-node evidence");
            PhotoTransition gate=new PhotoTransition();
            gate.begin(PhotoReelTracker.Action.REEL,"window","photo",new PhotoReelPolicy.Position(2,2),0);
            gate.inspect("window","ad",null,900,!original.equals(ad.photoPageKey));
            if(gate.inspect("window","ad",null,1200,!original.equals(ad.photoPageKey))!=PhotoTransition.State.CONFIRMED) throw new AssertionError("Known ad cannot confirm photo exit");
            gate.begin(PhotoReelTracker.Action.REEL,"window","photo",new PhotoReelPolicy.Position(2,2),2000);
            gate.inspect("window","changedCaption",null,2900,!original.equals(returned));
            if(gate.inspect("window","changedCaption",null,3200,!original.equals(returned))!=PhotoTransition.State.WAITING) throw new AssertionError("Rollback falsely confirmed");
            return 5;
        } finally {a.recycle();b.recycle();again.recycle();}
    }
}
