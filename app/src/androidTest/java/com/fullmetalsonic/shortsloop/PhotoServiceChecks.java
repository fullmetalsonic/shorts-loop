package com.fullmetalsonic.shortsloop;

import android.content.Context;
import android.graphics.Rect;
import com.fullmetalsonic.shortsloop.core.PhotoReelPolicy;
import com.fullmetalsonic.shortsloop.core.PhotoReelTracker;
import com.fullmetalsonic.shortsloop.core.PhotoTransition;
import com.fullmetalsonic.shortsloop.core.PlaybackRestart;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.detection.PhotoFrame;
import com.fullmetalsonic.shortsloop.detection.YouTubeSnapshot;
import com.fullmetalsonic.shortsloop.service.RuntimeState;
import com.fullmetalsonic.shortsloop.service.HostPlaybackSession;
import com.fullmetalsonic.shortsloop.visual.VisualAssistController;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Real service methods on an unbound synthetic instance: no gestures or permission changes. */
final class PhotoServiceChecks {
    static int run(Context context, SettingsStore rootStore) {
        SettingsStore store = rootStore.forHost(SettingsStore.INSTAGRAM_PACKAGE);
        RuntimeState.HostState state = RuntimeState.forHost(SettingsStore.INSTAGRAM_PACKAGE);
        int checks=0;
        try {
            HostPlaybackSession service=new HostPlaybackSession(context,SettingsStore.INSTAGRAM_PACKAGE,null);
            rootStore.enabled(true);
            set(service,"store",store);set(service,"activePackage",SettingsStore.INSTAGRAM_PACKAGE);
            set(service,"visual",VisualAssistController.create(null,null));
            store.enabled(true);store.target(0);store.selectedApp(SettingsStore.INSTAGRAM_PACKAGE,true);store.photoEnabled(true);
            store.photoMode(1);store.photoFallback(false);store.photoWholeSeconds(5);store.photoSlideSeconds(5);state.blocked=false;
            require((Boolean)call(service,"photoSkippingEnabled",new Class<?>[0]),"Photo option independent of zero");checks++;
            Rect page=new Rect(0,0,1000,1700);
            YouTubeSnapshot known=YouTubeSnapshot.photograph("post",page,new PhotoFrame(page,new PhotoReelPolicy.Position(1,2))).inWindow(7,page);
            observe(service,known,0);require(state.status.equals("photo.each")&&state.current==0,"Photo display is not a counted play");checks++;
            require(!(Boolean)call(service,"timedCandidate",new Class<?>[]{YouTubeSnapshot.class},known),"Photo cannot enter old timer");checks++;
            YouTubeSnapshot missing=YouTubeSnapshot.photograph("post",page,new PhotoFrame(page,new PhotoReelPolicy.Position(0,0))).inWindow(7,page);
            observe(service,missing,300);require(state.status.equals("photo.index_missing"),"Missing-index OFF waits");checks++;
            store.photoFallback(true);observe(service,missing,600);require(state.status.equals("photo.fallback"),"Missing-index ON uses dedicated fallback");checks++;
            store.photoEnabled(false);observe(service,known,900);require(state.status.equals("photo.disabled"),"Independent switch stops photo timer");checks++;
            store.photoEnabled(true);store.selectedApp(SettingsStore.INSTAGRAM_PACKAGE,false);
            require(!(Boolean)call(service,"photoSkippingEnabled",new Class<?>[0]),"Unselected Instagram cannot run photo rules");checks++;
            store.selectedApp(SettingsStore.INSTAGRAM_PACKAGE,true);
            PhotoTransition transition=(PhotoTransition)get(service,"photoTransition");
            transition.begin(PhotoReelTracker.Action.SLIDE,"window","post",new PhotoReelPolicy.Position(1,2),0);
            set(service,"unresolvedPhotoAttempt",true);call(service,"interruptSession",new Class<?>[0]);
            require(state.blocked&&!transition.pending(),"In-flight interruption hard-stops");checks++;
            require(!((PlaybackRestart)get(service,"restart")).active(),"Photo failure never begins ordinary fresh-start recovery");checks++;
            state.blocked=false;
            service.onSharedPreferenceChanged(store.preferences,"photo_whole_seconds");
            require(state.blocked&&(Boolean)get(service,"unresolvedPhotoAttempt"),"Changing delay cannot rearm failed photo request");checks++;
            require((Integer)get(service,"photoSlideRequests")==0&&(Integer)get(service,"photoReelRequests")==0,"Synthetic checks sent no requests");checks++;
            store.enabled(false);state.blocked=false;return checks;
        } catch(ReflectiveOperationException error){throw new AssertionError(error);}
        finally { rootStore.enabled(false); store.enabled(false); state.blocked=false; state.current=0; state.status="off"; state.timedRemainingSeconds=-1; }
    }
    private static void observe(Object target,YouTubeSnapshot snapshot,long now)throws ReflectiveOperationException {call(target,"observePhoto",new Class<?>[]{YouTubeSnapshot.class,long.class},snapshot,now);}
    private static Object call(Object target,String name,Class<?>[] types,Object... args)throws ReflectiveOperationException {Method m=target.getClass().getDeclaredMethod(name,types);m.setAccessible(true);return m.invoke(target,args);}
    private static Object get(Object target,String name)throws ReflectiveOperationException {Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(target);}
    private static void set(Object target,String name,Object value)throws ReflectiveOperationException {Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);f.set(target,value);}
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
