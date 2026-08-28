package com.fullmetalsonic.shortsloop.core;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.fullmetalsonic.shortsloop.core.PhotoTransition.State.*;
import static com.fullmetalsonic.shortsloop.core.PhotoReelTracker.Action.*;

public class PhotoTransitionTest {
    private final PhotoTransition gate=new PhotoTransition();
    private PhotoReelPolicy.Position p(int current,int total){return new PhotoReelPolicy.Position(current,total);}
    private void slide(){gate.begin(SLIDE,"window","post",p(1,3),0);}
    @Test public void exactNextSlideMustStabilize() {slide();assertEquals(WAITING,gate.inspect("window","post",p(2,3),300));assertEquals(CONFIRMED,gate.inspect("window","post",p(2,3),600));}
    @Test public void unchangedIndexTimesOut() {slide();assertEquals(WAITING,gate.inspect("window","post",p(1,3),1200));assertEquals(FAILED,gate.inspect("window","post",p(1,3),4500));assertFalse(gate.pending());}
    @Test public void missingIndexCannotPretendLastOrFallback() {slide();assertEquals(WAITING,gate.inspect("window","post",p(0,0),1500));assertEquals(FAILED,gate.inspect("window","post",p(0,0),4500));}
    @Test public void skippedSlideIsNotConfirmed() {slide();assertEquals(WAITING,gate.inspect("window","post",p(3,3),600));assertEquals(FAILED,gate.inspect("window","post",p(3,3),4500));}
    @Test public void differentTotalIsNotConfirmed() {slide();gate.inspect("window","post",p(2,4),300);assertEquals(WAITING,gate.inspect("window","post",p(2,4),600));}
    @Test public void differentPostCannotConfirmHorizontalMove() {slide();gate.inspect("window","other",p(2,3),300);assertEquals(WAITING,gate.inspect("window","other",p(2,3),600));}
    @Test public void wrongWindowNeverConfirms() {slide();gate.inspect("otherWindow","post",p(2,3),300);assertEquals(WAITING,gate.inspect("otherWindow","post",p(2,3),600));}
    @Test public void destinationVideoCannotConfirmHorizontalMove() {slide();gate.inspect("window","post",null,300);assertEquals(WAITING,gate.inspect("window","post",null,600));}
    @Test public void rollbackClearsCandidate() {slide();gate.inspect("window","post",p(2,3),300);gate.inspect("window","post",p(1,3),600);assertEquals(WAITING,gate.inspect("window","post",p(2,3),900));assertEquals(CONFIRMED,gate.inspect("window","post",p(2,3),1200));}
    @Test public void clockRollbackFailsClosed() {slide();gate.inspect("window","post",p(2,3),600);assertEquals(FAILED,gate.inspect("window","post",p(2,3),500));}
    @Test public void verticalRequiresStableDifferentIdentity() {gate.begin(REEL,"window","post",p(3,3),0);assertEquals(WAITING,gate.inspect("window","other",null,900,true));assertEquals(CONFIRMED,gate.inspect("window","other",null,1200,true));}
    @Test public void photoToPhotoVerticalIsSupported() {gate.begin(REEL,"window","post",p(2,2),0);gate.inspect("window","other",p(1,4),900,true);assertEquals(CONFIRMED,gate.inspect("window","other",p(1,4),1200,true));}
    @Test public void captionChangeWithoutPageNodeMovementNeverConfirms() {gate.begin(REEL,"window","post",p(2,2),0);gate.inspect("window","lateCaption",p(2,2),900);assertEquals(WAITING,gate.inspect("window","lateCaption",p(2,2),1200));assertEquals(FAILED,gate.inspect("window","lateCaption",p(2,2),4500));}
    @Test public void pageNodeRollbackClearsVerticalCandidate() {gate.begin(REEL,"window","post",p(2,2),0);gate.inspect("window","other",null,900,true);gate.inspect("window","other",null,1200,false);assertEquals(WAITING,gate.inspect("window","other",null,1500,true));assertEquals(CONFIRMED,gate.inspect("window","other",null,1800,true));}
    @Test public void indexChangeAloneCannotConfirmVertical() {gate.begin(REEL,"window","post",p(1,2),0);gate.inspect("window","post",p(2,2),900);assertEquals(WAITING,gate.inspect("window","post",p(2,2),1200));}
    @Test public void transientIdentityIsNotLatched() {gate.begin(REEL,"window","post",p(2,2),0);gate.inspect("window","other",null,900);gate.inspect("window","post",p(2,2),1200);assertEquals(WAITING,gate.inspect("window","other",null,1500));}
    @Test public void observationGapClearsCandidate() {slide();gate.inspect("window","post",p(2,3),300);assertEquals(WAITING,gate.inspect("window","post",p(2,3),2000));}
    @Test public void explicitResetCancels() {slide();gate.reset();assertEquals(IDLE,gate.inspect("window","post",p(2,3),1200));}
    @Test(expected=IllegalArgumentException.class) public void lastPhotoCannotMoveHorizontally() {gate.begin(SLIDE,"window","post",p(2,2),0);}
    @Test(expected=IllegalArgumentException.class) public void duplicateBeginIsRejected() {slide();slide();}
}
