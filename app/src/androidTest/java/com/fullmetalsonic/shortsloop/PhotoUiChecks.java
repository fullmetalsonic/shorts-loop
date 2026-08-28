package com.fullmetalsonic.shortsloop;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Parcelable;
import android.util.SparseArray;
import android.widget.EditText;
import com.fullmetalsonic.shortsloop.core.PhotoReelPolicy;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.detection.PhotoFrame;
import com.fullmetalsonic.shortsloop.detection.YouTubeSnapshot;
import com.fullmetalsonic.shortsloop.i18n.AppLocale;
import com.fullmetalsonic.shortsloop.ui.PhotoReelPanel;

/** Native input/state/metadata checks only; no permissions or social-app interaction. */
final class PhotoUiChecks {
    private static int checks;
    static int run(Activity activity, SettingsStore store) {
        checks=0;
        for(String language:new String[]{"ko","en"}) {
            Context context=AppLocale.forLanguage(activity,language);
            store.photoEnabled(false);store.photoMode(1);store.photoWholeSeconds(0);store.photoSlideSeconds(10);store.photoFallback(true);
            PhotoReelPanel panel=new PhotoReelPanel(context);
            require(((android.widget.TextView)panel.findViewById(R.id.photo_slide_apply)).getText().toString().equals(context.getString(R.string.photo_seconds_apply)),"Photo apply text is not live text");
            final int[] writes={0};
            panel.whole.changed=v->{writes[0]++;store.photoWholeSeconds(v);};
            panel.slide.changed=v->{writes[0]++;store.photoSlideSeconds(v);};
            panel.render(store,true);
            require(panel.modes.getCheckedRadioButtonId()==R.id.photo_mode_each,"Each mode rendered");
            require(panel.fallback.isEnabled()&&panel.fallback.isChecked(),"Fallback independent saved choice");
            EditText whole=panel.findViewById(R.id.photo_whole_input),slide=panel.findViewById(R.id.photo_slide_input);
            require(whole.getText().toString().equals("0")&&slide.getText().toString().equals("10"),"Separate boundaries");
            SparseArray<Parcelable> state=new SparseArray<>();panel.saveHierarchyState(state);panel.restoreHierarchyState(state);
            require(writes[0]==0,"Restore never saves unchanged values");
            for(String invalid:new String[]{"", "-1", "11", "1.5", "+2", "abc"}) {
                slide.setText(invalid); require(!panel.slide.commit(),"Invalid pasted input rejected: "+invalid);
                require(slide.getError()!=null&&store.photoSlideSeconds()==10,"Error preserves saved setting");
            }
            slide.setText("3");state=new SparseArray<>();panel.saveHierarchyState(state);
            PhotoReelPanel restored=new PhotoReelPanel(context);restored.render(store,true);restored.restoreHierarchyState(state);
            require(((EditText)restored.findViewById(R.id.photo_slide_input)).getText().toString().equals("3"),"Draft survives view restoration");
            require(store.photoSlideSeconds()==10,"Draft restore does not save");
            require(panel.slide.commit()&&store.photoSlideSeconds()==3,"Explicit commit saves");
            panel.findViewById(R.id.photo_slide_minus).performClick();require(store.photoSlideSeconds()==2,"Minus saves");
            panel.findViewById(R.id.photo_slide_plus).performClick();require(store.photoSlideSeconds()==3,"Plus saves");
            panel.render(store,false);require(!panel.toggle.isEnabled()&&!panel.fallback.isEnabled()&&!slide.isEnabled(),"Unavailable host disables controls");
            require(store.photoFallback()&&store.photoSlideSeconds()==3,"Unavailable host preserves values");
            store.photoMode(0);panel.render(store,true);require(!panel.fallback.isEnabled()&&panel.fallback.isChecked(),"Whole mode retains inactive fallback");
        }
        Rect page=new Rect(0,0,1000,1700),picture=new Rect(0,200,1000,1400);
        YouTubeSnapshot source=YouTubeSnapshot.photograph("post",page,new PhotoFrame(picture,new PhotoReelPolicy.Position(2,2)));
        YouTubeSnapshot copied=source.withPhotoPageKey("node:1").withIdentity("instagram|post").withContentIdentity("supplement").inWindow(7,page);
        require(copied.photoPageKey.equals("node:1"),"Wrappers preserve independent page-node evidence");
        require(copied.photo!=null&&copied.photo.position.current()==2&&copied.photo.position.total()==2,"Wrappers preserve last-slide metadata");
        require(copied.recognized()&&!copied.usable()&&!copied.visualCandidate&&!copied.live&&!copied.ad,"Photos never enter clock/visual/live paths");
        store.photoEnabled(false);return checks;
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
}
