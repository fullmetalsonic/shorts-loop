package com.fullmetalsonic.shortsloop.core;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;
public class WindowPolicyTest {
    private static final WindowPolicy.Box LEFT = new WindowPolicy.Box(0,0,400,700), RIGHT = new WindowPolicy.Box(410,0,810,700);
    private WindowPolicy.Window w(int id,int type,int layer,boolean focus,boolean pip,boolean own,WindowPolicy.Box box) {
        return new WindowPolicy.Window(id,type,layer,focus,pip,own,box);
    }
    @Test public void bothOrdersAndFocusesAreIndependent() {
        for (boolean focus:new boolean[]{false,true}) {
            var a=w(1,1,1,focus,false,false,LEFT); var b=w(2,1,2,!focus,false,false,RIGHT);
            assertTrue(WindowPolicy.allowed(List.of(a,b),1)); assertTrue(WindowPolicy.allowed(List.of(b,a),2));
        }
    }
    @Test public void verticalSplitAlsoWorks() {
        var a=w(1,1,1,false,false,false,new WindowPolicy.Box(0,0,400,340));
        var b=w(2,1,2,true,false,false,new WindowPolicy.Box(0,350,400,700));
        assertTrue(WindowPolicy.allowed(List.of(a,b),1)); assertTrue(WindowPolicy.allowed(List.of(a,b),2));
    }
    @Test public void coveredPaneIsBlockedButSiblingContinues() {
        var a=w(1,1,1,false,false,false,LEFT); var b=w(2,1,2,false,false,false,RIGHT);
        var dialog=w(3,1,3,true,false,false,new WindowPolicy.Box(0,200,400,600));
        assertFalse(WindowPolicy.allowed(List.of(a,b,dialog),1)); assertTrue(WindowPolicy.allowed(List.of(a,b,dialog),2));
    }
    @Test public void ownSettingsWindowIsNotMistakenForFloating() {
        var a=w(1,1,1,false,false,false,LEFT); var settings=w(2,1,3,true,false,true,LEFT);
        assertFalse(WindowPolicy.allowed(List.of(a,settings),1));
    }
    @Test public void genuineOwnOverlayAllowedAndOtherBubbleBlocked() {
        var a=w(1,1,1,true,false,false,LEFT); var small=new WindowPolicy.Box(100,100,180,160);
        assertTrue(WindowPolicy.allowed(List.of(a,w(2,3,3,false,false,true,small)),1));
        assertFalse(WindowPolicy.allowed(List.of(a,w(2,3,3,false,false,false,small)),1));
    }
    @Test public void shadeKeyboardAndPipFailClosed() {
        var a=w(1,1,1,false,false,false,LEFT);
        assertFalse(WindowPolicy.allowed(List.of(a,w(2,3,3,true,false,false,RIGHT)),1));
        assertFalse(WindowPolicy.allowed(List.of(a,w(2,2,3,false,false,false,LEFT)),1));
        assertFalse(WindowPolicy.allowed(List.of(w(1,1,1,true,true,false,LEFT)),1));
    }
    @Test public void narrowUnfocusedSystemBarsAllowed() {
        var a=w(1,1,1,true,false,false,LEFT);
        assertTrue(WindowPolicy.allowed(List.of(a,w(2,3,3,false,false,false,new WindowPolicy.Box(0,0,400,20))),1));
        assertFalse(WindowPolicy.allowed(List.of(a,w(2,3,3,false,false,false,new WindowPolicy.Box(0,0,400,300))),1));
    }
    @Test public void missingEmptyAndDuplicateTargetsFail() {
        var a=w(1,1,1,true,false,false,LEFT);
        assertFalse(WindowPolicy.allowed(List.of(a),2)); assertFalse(WindowPolicy.allowed(List.of(a,a),1));
        assertFalse(WindowPolicy.allowed(List.of(w(1,1,1,true,false,false,new WindowPolicy.Box(0,0,0,0))),1));
    }
    @Test public void samsungDividerHandleMayStraddleBothBoundaries() {
        var a=w(1,1,0,false,false,false,new WindowPolicy.Box(0,0,1216,1722));
        var b=w(2,1,1,true,false,false,new WindowPolicy.Box(1232,0,2448,1722));
        var handle=w(3,5,2,false,false,false,new WindowPolicy.Box(1184,732,1263,989));
        assertTrue(WindowPolicy.allowed(List.of(a,b,handle),1)); assertTrue(WindowPolicy.allowed(List.of(a,b,handle),2));
    }
    @Test public void largeOrFocusedDividerAndLookalikeBubbleStillRemainBlocked() {
        var a=w(1,1,0,true,false,false,LEFT);
        var narrow=new WindowPolicy.Box(390,250,420,400);
        assertTrue(WindowPolicy.allowed(List.of(a,w(2,5,2,false,false,false,narrow)),1));
        assertFalse(WindowPolicy.allowed(List.of(a,w(2,5,2,true,false,false,narrow)),1));
        assertFalse(WindowPolicy.allowed(List.of(a,w(2,3,2,false,false,false,narrow)),1));
        assertFalse(WindowPolicy.allowed(List.of(a,w(2,5,2,false,false,false,new WindowPolicy.Box(300,250,450,400))),1));
        assertFalse(WindowPolicy.allowed(List.of(a,w(2,5,2,false,false,false,new WindowPolicy.Box(100,250,110,400))),1));
    }
    @Test public void horizontalDividerUsesTheSameNarrowBoundaryRule() {
        var a=w(1,1,0,true,false,false,new WindowPolicy.Box(0,0,1200,700));
        assertTrue(WindowPolicy.allowed(List.of(a,w(2,5,2,false,false,false,new WindowPolicy.Box(500,680,700,730))),1));
    }
    @Test public void overlayOwnershipUsesFrameworkPackageNotLocalizedTitle() {
        String own="com.fullmetalsonic.shortsloop";
        assertTrue(WindowPolicy.ownOverlay(3,false,own));
        assertTrue(WindowPolicy.ownOverlay(4,false,own));
        assertFalse(WindowPolicy.ownOverlay(1,false,own));
        assertFalse(WindowPolicy.ownOverlay(3,true,own));
        assertFalse(WindowPolicy.ownOverlay(3,false,null));
        assertFalse(WindowPolicy.ownOverlay(3,false,""));
        assertFalse(WindowPolicy.ownOverlay(3,false,own+".lookalike"));
        assertFalse(WindowPolicy.ownOverlay(3,false,"com.android.systemui"));
    }
    @Test public void smallSystemCaptionHandlesWorkInEitherPane() {
        for(int offset:new int[]{0,1232}) {
            var pane=new WindowPolicy.Box(offset,0,offset+1216,1722);
            var a=w(1,1,0,true,false,false,pane);
            var small=new WindowPolicy.Box(offset+483,105,offset+732,171);
            assertTrue(WindowPolicy.allowed(List.of(a,w(2,7,2,false,false,false,small)),1));
            assertFalse(WindowPolicy.allowed(List.of(a,w(2,7,2,true,false,false,small)),1));
            assertFalse(WindowPolicy.allowed(List.of(a,w(2,3,2,false,false,false,small)),1));
        }
    }
    @Test public void expandedOrMisplacedCaptionControlsRemainBlocked() {
        var a=w(1,1,0,true,false,false,new WindowPolicy.Box(0,0,1216,1722));
        for(var box:List.of(new WindowPolicy.Box(100,105,1100,400),new WindowPolicy.Box(483,500,732,566),
                new WindowPolicy.Box(0,105,249,171),new WindowPolicy.Box(483,105,800,171),
                new WindowPolicy.Box(483,105,732,205),new WindowPolicy.Box(558,100,658,170))) {
            assertFalse(WindowPolicy.allowed(List.of(a,w(2,7,2,false,false,false,box)),1));
        }
    }
    @Test public void captionObservationNeverAuthorizesAnOverlappingTouchPath() {
        var a=w(1,1,0,true,false,false,new WindowPolicy.Box(0,0,1000,2000));
        var caption=w(2,7,2,false,false,false,new WindowPolicy.Box(400,100,600,180));
        assertTrue(WindowPolicy.allowed(List.of(a,caption),1));
        assertFalse(WindowPolicy.inputClear(List.of(a,caption),1,new WindowPolicy.Box(388,100,412,300)));
        assertTrue(WindowPolicy.inputClear(List.of(a,caption),1,new WindowPolicy.Box(638,100,662,300)));
        assertTrue(WindowPolicy.inputClear(List.of(a,caption),1,new WindowPolicy.Box(388,500,412,1500)));
        assertFalse(WindowPolicy.inputClear(List.of(a,caption),1,new WindowPolicy.Box(200,90,740,190)));
    }
    @Test public void ownOverlayAndDividerAlsoBlockTouchCorridors() {
        var a=w(1,1,0,true,false,false,LEFT);
        var overlay=w(2,3,2,false,false,true,new WindowPolicy.Box(150,200,220,260));
        assertTrue(WindowPolicy.allowed(List.of(a,overlay),1));
        assertFalse(WindowPolicy.inputClear(List.of(a,overlay),1,new WindowPolicy.Box(140,225,230,245)));
        var divider=w(3,5,2,false,false,false,new WindowPolicy.Box(390,250,420,400));
        assertFalse(WindowPolicy.inputClear(List.of(a,divider),1,new WindowPolicy.Box(385,300,395,350)));
    }
    @Test public void inputRevalidatesWindowCoverageAndCurrentBounds() {
        var a=w(1,1,0,true,false,false,LEFT);
        assertFalse(WindowPolicy.inputClear(List.of(a),1,new WindowPolicy.Box(300,100,410,200)));
        assertFalse(WindowPolicy.inputClear(List.of(a),2,new WindowPolicy.Box(300,100,350,200)));
        assertFalse(WindowPolicy.inputClear(List.of(a),1,new WindowPolicy.Box(300,100,300,200)));
        var shade=w(2,3,3,true,false,false,RIGHT);
        assertFalse(WindowPolicy.inputClear(List.of(a,shade),1,new WindowPolicy.Box(300,100,350,200)));
    }
    @Test public void fixedSizeCaptionStillWorksWhenItsPaneNarrows() {
        var pane=new WindowPolicy.Box(1548,0,2448,1722);
        var a=w(1,1,0,true,false,false,pane);
        var caption=w(2,7,2,false,false,false,new WindowPolicy.Box(1873,105,2122,171));
        assertTrue(WindowPolicy.allowed(List.of(a,caption),1));
        assertTrue(WindowPolicy.inputClear(List.of(a,caption),1,new WindowPolicy.Box(1890,500,1910,1240)));
        assertFalse(WindowPolicy.inputClear(List.of(a,caption),1,new WindowPolicy.Box(1890,120,1910,600)));
    }
    @Test public void captionsInRotatedUpperAndLowerPanesUseTheSameRule() {
        for(int top:new int[]{0,1232}) {
            var a=w(1,1,0,true,false,false,new WindowPolicy.Box(0,top,1848,top+1216));
            var caption=w(2,7,2,false,false,false,new WindowPolicy.Box(800,top+105,1049,top+171));
            assertTrue(WindowPolicy.allowed(List.of(a,caption),1));
            assertTrue(WindowPolicy.inputClear(List.of(a,caption),1,new WindowPolicy.Box(700,top+350,730,top+950)));
            assertFalse(WindowPolicy.inputClear(List.of(a,caption),1,new WindowPolicy.Box(810,top+140,840,top+800)));
        }
    }
    @Test public void fixedStatusBarWorksAboveShortRotatedPaneWithoutAllowingTouch() {
        var a=w(1,1,0,false,false,false,new WindowPolicy.Box(0,0,1848,1153));
        var bar=w(2,3,3,false,false,false,new WindowPolicy.Box(0,0,1848,102));
        assertTrue(WindowPolicy.allowed(List.of(a,bar),1));
        assertTrue(WindowPolicy.inputClear(List.of(a,bar),1,new WindowPolicy.Box(720,300,750,850)));
        assertFalse(WindowPolicy.inputClear(List.of(a,bar),1,new WindowPolicy.Box(720,60,750,850)));
        assertFalse(WindowPolicy.allowed(List.of(a,w(2,3,3,true,false,false,bar.bounds())),1));
        assertFalse(WindowPolicy.allowed(List.of(a,w(2,1,3,false,false,false,bar.bounds())),1));
    }
    @Test public void systemBarExceptionRequiresFullSpanEdgeAndBothThicknessLimits() {
        var a=w(1,1,0,false,false,false,new WindowPolicy.Box(0,0,1848,1153));
        assertTrue(WindowPolicy.allowed(List.of(a,w(2,3,3,false,false,false,new WindowPolicy.Box(0,0,1848,138))),1));
        assertFalse(WindowPolicy.allowed(List.of(a,w(2,3,3,false,false,false,new WindowPolicy.Box(0,0,1848,139))),1));
        for(var box:List.of(new WindowPolicy.Box(0,0,1848,200),
                new WindowPolicy.Box(0,200,1848,302),new WindowPolicy.Box(100,0,1948,102),
                new WindowPolicy.Box(-100,0,1748,102))) {
            assertFalse(WindowPolicy.allowed(List.of(a,w(2,3,3,false,false,false,box)),1));
        }
        var shortPane=w(1,1,0,false,false,false,new WindowPolicy.Box(0,0,1848,400));
        assertFalse(WindowPolicy.allowed(List.of(shortPane,w(2,3,3,false,false,false,new WindowPolicy.Box(0,0,1848,102))),1));
        var tallPane=w(1,1,0,false,false,false,new WindowPolicy.Box(0,0,800,1800));
        assertTrue(WindowPolicy.allowed(List.of(tallPane,w(2,3,3,false,false,false,new WindowPolicy.Box(0,0,800,144))),1));
        assertFalse(WindowPolicy.allowed(List.of(tallPane,w(2,3,3,false,false,false,new WindowPolicy.Box(0,0,800,145))),1));
        assertFalse(WindowPolicy.allowed(List.of(tallPane,w(2,3,3,false,false,false,new WindowPolicy.Box(0,0,800,150))),1));
        var narrowPane=w(1,1,0,false,false,false,new WindowPolicy.Box(0,0,600,1800));
        assertTrue(WindowPolicy.allowed(List.of(narrowPane,w(2,3,3,false,false,false,new WindowPolicy.Box(0,0,72,1800))),1));
        assertFalse(WindowPolicy.allowed(List.of(narrowPane,w(2,3,3,false,false,false,new WindowPolicy.Box(0,0,73,1800))),1));
        assertTrue(WindowPolicy.allowed(List.of(narrowPane,w(2,3,3,false,false,false,new WindowPolicy.Box(0,0,60,1800))),1));
        assertFalse(WindowPolicy.allowed(List.of(narrowPane,w(2,3,3,false,false,false,new WindowPolicy.Box(0,0,90,1800))),1));
        assertFalse(WindowPolicy.allowed(List.of(narrowPane,w(2,3,3,false,false,false,new WindowPolicy.Box(0,100,60,1900))),1));
    }
    @Test public void fullscreenSystemHandleMustStayInsideAnAllowedSystemBar() {
        var a=w(1,1,0,true,false,false,new WindowPolicy.Box(0,0,1848,2448));
        var small=new WindowPolicy.Box(792,18,1055,84);
        var handle=new WindowPolicy.Window(2,3,3,false,false,false,small,true);
        var bar=new WindowPolicy.Window(3,3,2,false,false,false,new WindowPolicy.Box(0,0,1848,102),true);
        assertTrue(WindowPolicy.allowed(List.of(a,handle,bar),1));
        assertTrue(WindowPolicy.inputClear(List.of(a,handle,bar),1,new WindowPolicy.Box(700,500,730,1800)));
        assertFalse(WindowPolicy.inputClear(List.of(a,handle,bar),1,new WindowPolicy.Box(800,50,830,1800)));
        assertFalse(WindowPolicy.allowed(List.of(a,handle),1));
        assertFalse(WindowPolicy.allowed(List.of(a,w(2,3,3,false,false,false,small),bar),1));
        assertFalse(WindowPolicy.allowed(List.of(a,handle,w(3,3,2,false,false,false,bar.bounds())),1));
        assertFalse(WindowPolicy.allowed(List.of(a,handle,new WindowPolicy.Window(3,3,-1,false,false,false,bar.bounds(),true)),1));
        assertFalse(WindowPolicy.allowed(List.of(a,new WindowPolicy.Window(2,3,3,true,false,false,small,true),bar),1));
        assertFalse(WindowPolicy.allowed(List.of(a,handle,new WindowPolicy.Window(3,3,2,true,false,false,bar.bounds(),true)),1));
        assertFalse(WindowPolicy.allowed(List.of(a,handle,new WindowPolicy.Window(3,1,2,false,false,false,bar.bounds(),true)),1));
    }
    @Test public void fullscreenHandleCannotUsePartialContainmentOrExpandedBar() {
        var a=w(1,1,0,true,false,false,new WindowPolicy.Box(0,0,1848,2448));
        var bar=new WindowPolicy.Window(3,3,2,false,false,false,new WindowPolicy.Box(0,0,1848,102),true);
        for(var box:List.of(new WindowPolicy.Box(792,80,1055,146),new WindowPolicy.Box(792,500,1055,566),
                new WindowPolicy.Box(100,18,1700,84),new WindowPolicy.Box(792,18,1055,500))) {
            assertFalse(WindowPolicy.allowed(List.of(a,new WindowPolicy.Window(2,3,3,false,false,false,box,true),bar),1));
        }
        var handle=new WindowPolicy.Window(2,3,3,false,false,false,new WindowPolicy.Box(792,18,1055,84),true);
        assertFalse(WindowPolicy.allowed(List.of(a,handle,new WindowPolicy.Window(3,3,2,false,false,false,new WindowPolicy.Box(0,0,1848,500),true)),1));
    }
    @Test public void systemUiProvenanceIsExactAndOldMetadataDefaultsUntrusted() {
        assertTrue(WindowPolicy.systemUi(3,"com.android.systemui"));
        assertFalse(WindowPolicy.systemUi(1,"com.android.systemui"));
        assertFalse(WindowPolicy.systemUi(4,"com.android.systemui"));
        assertFalse(WindowPolicy.systemUi(3,"com.android.systemui.fake"));
        assertFalse(WindowPolicy.systemUi(3,"com.fullmetalsonic.shortsloop"));
        assertFalse(WindowPolicy.systemUi(3,null));
        assertFalse(w(1,3,2,false,false,false,LEFT).systemUi());
    }
}
