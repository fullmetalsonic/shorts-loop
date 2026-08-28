package com.fullmetalsonic.shortsloop.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class InstagramPolicyTest {
    @Test public void onlyEmptyNoninteractiveCameraShellIsExempt() {
        String id="com.instagram.android:id/bottom_sheet_camera_container", type="android.widget.FrameLayout";
        assertTrue(InstagramPolicy.emptyCameraShell(id,type,0,false,false,false,false,"",""));
        assertFalse(InstagramPolicy.emptyCameraShell(id,type,1,false,false,false,false,"",""));
        assertFalse(InstagramPolicy.emptyCameraShell(id,type,0,true,false,false,false,"",""));
        assertFalse(InstagramPolicy.emptyCameraShell(id,type,0,false,true,false,false,"",""));
        assertFalse(InstagramPolicy.emptyCameraShell(id,type,0,false,false,true,false,"",""));
        assertFalse(InstagramPolicy.emptyCameraShell(id,type,0,false,false,false,true,"",""));
        assertFalse(InstagramPolicy.emptyCameraShell(id,type,0,false,false,false,false,"camera",""));
        assertFalse(InstagramPolicy.emptyCameraShell(id,type,0,false,false,false,false,"","open"));
        assertFalse(InstagramPolicy.emptyCameraShell("com.instagram.android:id/comments_sheet",type,0,false,false,false,false,"",""));
        assertFalse(InstagramPolicy.emptyCameraShell(id,"android.widget.EditText",0,false,false,false,false,"",""));
    }
    private static final String PREFIX = "com.instagram.android:id/";
    private void check(double current, double max, double position, double duration) {
        Progress p = InstagramPolicy.progress(0, 0, max, current);
        assertNotNull(p); assertEquals(position, p.position, 0.000001); assertEquals(duration, p.duration, 0.000001);
    }
    @Test public void observedIntegerMilliseconds() { check(13441, 19014, 13.441, 19.014); }
    @Test public void observedEndAndWrap() { check(18708, 19014, 18.708, 19.014); check(95, 19014, .095, 19.014); }
    @Test public void observedSecondReel() { check(22377, 22638, 22.377, 22.638); check(147, 22638, .147, 22.638); }
    @Test public void supportsExactStartAndEnd() { check(0, 19014, 0, 19.014); check(19014, 19014, 19.014, 19.014); }
    @Test public void durationBoundaries() { check(0, 3000, 0, 3); check(3600000, 3600000, 3600, 3600); }
    @Test public void rejectsPercentageOrSeconds() { assertNull(InstagramPolicy.progress(0, 0, 100, 30)); assertNull(InstagramPolicy.progress(0, 0, 19, 7)); }
    @Test public void rejectsUnobservedRangeTypes() { assertNull(InstagramPolicy.progress(1, 0, 19014, 100)); assertNull(InstagramPolicy.progress(2, 0, 19014, 100)); }
    @Test public void rejectsUnknownOffsetAndFractions() { assertNull(InstagramPolicy.progress(0, 1, 19014, 100)); assertNull(InstagramPolicy.progress(0, 0, 19014.5, 100)); assertNull(InstagramPolicy.progress(0, 0, 19014, 100.5)); }
    @Test public void rejectsInvalidNumbers() { assertNull(InstagramPolicy.progress(0, Double.NaN, 19014, 100)); assertNull(InstagramPolicy.progress(0, 0, Double.POSITIVE_INFINITY, 100)); assertNull(InstagramPolicy.progress(0, 0, 19014, Double.NaN)); }
    @Test public void rejectsOutOfBounds() { assertNull(InstagramPolicy.progress(0, 0, 19014, -1)); assertNull(InstagramPolicy.progress(0, 0, 19014, 19015)); assertNull(InstagramPolicy.progress(0, 0, 2999, 0)); assertNull(InstagramPolicy.progress(0, 0, 3600001, 0)); }
    @Test public void commentButtonDoesNotBlockNormalPlayback() { assertFalse(InstagramPolicy.blocks(PREFIX + "clips_comment_button", "android.widget.Button", false, false)); assertFalse(InstagramPolicy.blocks(PREFIX + "comments_count", "android.widget.TextView", false, false)); }
    @Test public void blocksCommentComposerAndDialog() { assertTrue(InstagramPolicy.blocks(PREFIX + "comment_composer", "android.view.View", false, false)); assertTrue(InstagramPolicy.blocks(PREFIX + "action_sheet_container", "android.view.View", false, false)); assertTrue(InstagramPolicy.blocks(PREFIX + "comments_list", "android.view.View", false, false)); assertTrue(InstagramPolicy.blocks(PREFIX + "dialog_root", "android.view.View", false, false)); }
    @Test public void blocksEditableFields() { assertTrue(InstagramPolicy.blocks("", "android.widget.EditText", false, false)); assertTrue(InstagramPolicy.blocks("", "android.view.View", true, true)); }
    @Test public void adWordsInCaptionsDoNotBlock() { assertFalse(InstagramPolicy.isAdIndicator(PREFIX + "clips_caption_component", "광고", true)); assertFalse(InstagramPolicy.isAdIndicator(PREFIX + "clips_author_username", "sponsored", true)); assertFalse(InstagramPolicy.isAdIndicator(PREFIX + "unrelated_text", "광고", false)); }
    @Test public void explicitSponsoredIndicatorsBlock() { assertTrue(InstagramPolicy.isAdIndicator(PREFIX + "clips_sponsored_label", "", false)); assertTrue(InstagramPolicy.isAdIndicator(PREFIX + "ad_badge", "", false)); assertTrue(InstagramPolicy.isAdIndicator(PREFIX + "row_feed_subtitle", "Sponsored", false)); assertTrue(InstagramPolicy.isAdIndicator(PREFIX + "row_feed_subtitle", "광고", false)); }
    @Test public void requiresExactAdLabelAndOwnPackage() { assertFalse(InstagramPolicy.isAdIndicator(PREFIX + "row_feed_subtitle", "광고 아닌 영상", false)); assertFalse(InstagramPolicy.isAdIndicator("other.app:id/ad_badge", "광고", false)); assertFalse(InstagramPolicy.isAdIndicator(null, "광고", false)); }
    @Test public void rejectsPhotoAndMixedMediaContainers() { assertTrue(InstagramPolicy.unsupportedMedia(PREFIX + "clips_carousel_container")); assertTrue(InstagramPolicy.unsupportedMedia(PREFIX + "clips_image_component")); assertTrue(InstagramPolicy.unsupportedMedia(PREFIX + "clips_multiple_media_component")); }
    @Test public void acceptsKnownSingleVideoContainers() { assertFalse(InstagramPolicy.unsupportedMedia(PREFIX + "clips_single_media_component")); assertFalse(InstagramPolicy.unsupportedMedia(PREFIX + "clips_video_container")); assertFalse(InstagramPolicy.unsupportedMedia("other.app:id/clips_image_component")); }
    private boolean badge(String text, String description) {
        return InstagramPolicy.isAnonymousAdBadge("", "android.view.ViewGroup", text, description,
                false, true, true, true, false);
    }
    @Test public void observedAnonymousKoreanBadge() { assertTrue(badge("광고", "광고")); }
    @Test public void exactEnglishBadge() { assertTrue(badge("Sponsored", "Sponsored")); }
    @Test public void adBodyWordsAreNotBadges() { assertFalse(badge("광고 포함", "광고 포함")); assertFalse(badge("이것은 광고입니다", "이것은 광고입니다")); assertFalse(badge("#광고", "#광고")); }
    @Test public void badgeTextAndDescriptionMustBothMatch() { assertFalse(badge("광고", "")); assertFalse(badge("", "광고")); assertFalse(badge("광고", "광고 정보")); assertFalse(badge(null, "광고")); }
    @Test public void captionsAndAuthorsCannotBeAnonymousBadges() {
        assertFalse(InstagramPolicy.isAnonymousAdBadge("", "android.view.ViewGroup", "광고", "광고", false, true, true, true, true));
    }
    @Test public void badgeNeedsNonClickableChildAndClickableParent() {
        assertFalse(InstagramPolicy.isAnonymousAdBadge("", "android.view.ViewGroup", "광고", "광고", true, true, true, true, false));
        assertFalse(InstagramPolicy.isAnonymousAdBadge("", "android.view.ViewGroup", "광고", "광고", false, false, true, true, false));
    }
    @Test public void badgeNeedsSameSmallTopBounds() {
        assertFalse(InstagramPolicy.isAnonymousAdBadge("", "android.view.ViewGroup", "광고", "광고", false, true, false, true, false));
        assertFalse(InstagramPolicy.isAnonymousAdBadge("", "android.view.ViewGroup", "광고", "광고", false, true, true, false, false));
    }
    @Test public void badgeCannotBeGenericTextViewOrNamedContent() {
        assertFalse(InstagramPolicy.isAnonymousAdBadge("", "android.widget.TextView", "광고", "광고", false, true, true, true, false));
        assertFalse(InstagramPolicy.isAnonymousAdBadge(PREFIX + "clips_caption_component", "android.view.ViewGroup", "광고", "광고", false, true, true, true, false));
    }
    @Test public void webViewsAlwaysBlockReelAndAdRecognition() { assertTrue(InstagramPolicy.blocks("", "android.webkit.WebView", false, false)); }
    @Test public void observedBottomRightVideoAdBadge() {
        assertTrue(InstagramPolicy.isActionColumnAdBadge("", "android.view.ViewGroup", "광고", "광고", false, true, true, true, false));
        assertTrue(InstagramPolicy.isActionColumnAdBadge("", "android.view.ViewGroup", "Sponsored", "Sponsored", false, true, true, true, false));
    }
    @Test public void bottomBadgeRequiresActionColumnAndCompactBounds() {
        assertFalse(InstagramPolicy.isActionColumnAdBadge("", "android.view.ViewGroup", "광고", "광고", false, true, false, true, false));
        assertFalse(InstagramPolicy.isActionColumnAdBadge("", "android.view.ViewGroup", "광고", "광고", false, true, true, false, false));
    }
    @Test public void bottomBadgeRejectsCaptionAndWrongText() {
        assertFalse(InstagramPolicy.isActionColumnAdBadge("", "android.view.ViewGroup", "광고", "광고", false, true, true, true, true));
        assertFalse(InstagramPolicy.isActionColumnAdBadge("", "android.view.ViewGroup", "광고 포함", "광고 포함", false, true, true, true, false));
        assertFalse(InstagramPolicy.isActionColumnAdBadge("", "android.view.ViewGroup", "광고", "", false, true, true, true, false));
    }
    @Test public void bottomBadgeRequiresExactNodeStructure() {
        assertFalse(InstagramPolicy.isActionColumnAdBadge("", "android.view.ViewGroup", "광고", "광고", true, true, true, true, false));
        assertFalse(InstagramPolicy.isActionColumnAdBadge("", "android.view.ViewGroup", "광고", "광고", false, false, true, true, false));
        assertFalse(InstagramPolicy.isActionColumnAdBadge(PREFIX + "clips_caption_component", "android.view.ViewGroup", "광고", "광고", false, true, true, true, false));
        assertFalse(InstagramPolicy.isActionColumnAdBadge("", "android.widget.TextView", "광고", "광고", false, true, true, true, false));
    }
}
