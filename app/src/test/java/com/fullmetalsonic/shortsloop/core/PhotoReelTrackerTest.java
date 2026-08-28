package com.fullmetalsonic.shortsloop.core;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.fullmetalsonic.shortsloop.core.PhotoReelTracker.Action.*;

public class PhotoReelTrackerTest {
    private final PhotoReelTracker t=new PhotoReelTracker();
    private PhotoReelTracker.Result at(int index,int total,int mode,int whole,int slide,boolean fallback,long now) {
        return t.observe("post",new PhotoReelPolicy.Position(index,total),mode,whole,slide,fallback,now);
    }
    @Test public void zeroWholeStillSettles() { assertEquals(NONE,at(1,2,0,0,5,false,0).action());assertEquals(NONE,at(1,2,0,0,5,false,300).action());assertEquals(REEL,at(1,2,0,0,5,false,600).action()); }
    @Test public void eachUsesHorizontalExceptLast() { at(1,2,1,0,0,false,0);assertEquals(SLIDE,at(1,2,1,0,0,false,600).action());t.reset();at(2,2,1,0,0,false,900);assertEquals(REEL,at(2,2,1,0,0,false,1500).action()); }
    @Test public void eachSinglePhotoGoesToNextReel() { at(1,1,1,5,0,false,0);assertEquals(REEL,at(1,1,1,5,0,false,600).action()); }
    @Test public void everyWholeDelayIsRespected() {
        for(int seconds=0;seconds<=10;seconds++) {t.reset();for(long now=0;now<seconds*1000L+450;now+=150)assertEquals(NONE,at(1,3,0,seconds,0,false,now).action()); assertEquals(REEL,at(1,3,0,seconds,0,false,seconds*1000L+450).action());}
    }
    @Test public void everySlideDelayIsRespectedIncludingLast() {
        for(int index=1;index<=2;index++)for(int seconds=0;seconds<=10;seconds++){t.reset();for(long now=0;now<seconds*1000L+450;now+=150)assertEquals(NONE,at(index,2,1,0,seconds,false,now).action());assertEquals(index==2?REEL:SLIDE,at(index,2,1,0,seconds,false,seconds*1000L+450).action());}
    }
    @Test public void consumedDoesNotRepeat() {at(1,2,1,0,0,false,0);assertEquals(SLIDE,at(1,2,1,0,0,false,600).action());for(long n=900;n<9000;n+=300)assertEquals(NONE,at(1,2,1,0,0,false,n).action());}
    @Test public void missingWithoutFallbackWaitsForever() {for(long n=0;n<30000;n+=300)assertEquals(NONE,at(0,0,1,0,0,false,n).action());assertFalse(t.active());}
    @Test public void missingFallbackRequiresLongerQualification() {at(0,0,1,0,10,true,0);assertEquals(NONE,at(0,0,1,0,10,true,600).action());assertEquals(REEL,at(0,0,1,0,10,true,900).action());}
    @Test public void fallbackUsesWholeNotSlideTime() {for(long n=0;n<5900;n+=100)assertEquals(NONE,at(0,0,1,5,0,true,n).action());assertEquals(REEL,at(0,0,1,5,0,true,5900).action());}
    @Test public void indexRecoveryCancelsFallbackTimer() {at(0,0,1,0,5,true,0);at(0,0,1,0,5,true,600);assertEquals(NONE,at(1,2,1,0,5,true,900).action());assertEquals(NONE,at(1,2,1,0,5,true,1200).action());}
    @Test public void transientMissingStartsNewFallbackDelay() {for(long n=0;n<=4500;n+=300)at(1,2,1,0,5,true,n);assertEquals(NONE,at(0,0,1,0,5,true,4800).action());assertEquals(NONE,at(1,2,1,0,5,true,5100).action());}
    @Test public void gapDoesNotCountBackgroundTime() {at(1,2,0,0,5,false,0);assertEquals(NONE,at(1,2,0,0,5,false,5000).action());assertEquals(REEL,at(1,2,0,0,5,false,5600).action());}
    @Test public void clockRollbackResetsEvidence() {at(1,2,0,0,5,false,3000);assertEquals(NONE,at(1,2,0,0,5,false,2000).action());}
    @Test public void newPhotoRestartsPerPhotoDelay() {at(1,3,1,0,1,false,0);at(1,3,1,0,1,false,900);assertEquals(NONE,at(2,3,1,0,1,false,1200).action());}
    @Test public void wholeModeDoesNotUsePhotoCount() {at(0,0,0,0,10,false,0);assertEquals(REEL,at(0,0,0,0,10,false,600).action());}
    @Test public void invalidDataCannotFallback() {assertEquals(NONE,at(3,2,1,0,0,true,0).action());assertEquals(NONE,t.observe("post",null,1,0,0,true,900).action());}
    @Test public void explicitResetDiscardsElapsedTime() {at(1,2,0,0,0,false,0);t.reset();assertEquals(NONE,at(1,2,0,0,0,false,600).action());}
}
