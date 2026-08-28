package com.fullmetalsonic.shortsloop;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.i18n.AppLocale;
import com.fullmetalsonic.shortsloop.ui.SettingsScreen;
import java.util.HashSet;
import java.util.Set;

/** Native retained-tree checks; does not grant permissions or start social apps. */
final class HostSettingsUiChecks {
    static int run(Context base) {
        int checks = 0;
        for (String language : new String[]{"ko", "en"}) {
            int[] saves = new int[10];
            SettingsScreen screen = screen(AppLocale.forLanguage(base, language), saves);
            require(screen.currentHost() == null && screen.homeContent.getVisibility() == View.VISIBLE
                    && screen.youtubeSettings.root.getVisibility() == View.GONE, "Home is initial route"); checks++;
            require(screen.youtubeEntry.open.performClick() && SettingsStore.YOUTUBE_PACKAGE.equals(screen.currentHost()), "YouTube entry opens detail"); checks++;
            screen.showHost(false);
            require(screen.youtubeSettings.root.getVisibility() == View.VISIBLE, "YouTube tab visible"); checks++;
            require(screen.instagramSettings.root.getVisibility() == View.GONE, "Instagram tab hidden"); checks++;
            require(((View)screen.live.getParent()).getVisibility() == View.VISIBLE, "YouTube live card visible"); checks++;
            require(((View)screen.photos.getParent()).getVisibility() == View.GONE, "Instagram photo card hidden"); checks++;
            EditText yt = screen.root.findViewById(R.id.count_input);
            EditText ig = screen.root.findViewById(R.id.mw_instagram_count_input);
            require("7".contentEquals(yt.getText()) && "3".contentEquals(ig.getText()), "Different host counts"); checks++;
            yt.setText("9"); screen.showHost(true); ig.setText("5"); screen.showHost(false);
            require("9".contentEquals(yt.getText()) && "5".contentEquals(ig.getText()), "Both drafts survive tab switches"); checks++;
            require(saves[0] == 0 && saves[1] == 0, "Tab switches do not commit"); checks++;
            require(screen.youtubeSettings.count.commit(), "YouTube draft valid"); checks++;
            require(saves[0] == 9 && saves[1] == 0, "YouTube commit cannot write Instagram"); checks++;
            require(screen.instagramSettings.count.commit(), "Instagram draft valid"); checks++;
            require(saves[0] == 9 && saves[1] == 5, "Independent Instagram commit"); checks++;
            EditText ytLong = screen.root.findViewById(R.id.long_video_seconds_input);
            EditText igLong = screen.root.findViewById(R.id.mw_instagram_long_input);
            ytLong.setText("120"); igLong.setText("45");
            screen.youtubeSettings.longVideo.commit();
            require(saves[2] == 120 && saves[3] == 0, "Long threshold isolated"); checks++;
            screen.instagramSettings.longVideo.commit();
            require(saves[3] == 45, "Instagram threshold committed separately"); checks++;
            screen.showHost(true);
            require(((View)screen.live.getParent()).getVisibility() == View.GONE, "Instagram hides live card"); checks++;
            require(((View)screen.photos.getParent()).getVisibility() == View.VISIBLE, "Instagram shows photo card"); checks++;
            require(screen.execution.getVisibility() == View.VISIBLE && screen.floating.getVisibility() == View.VISIBLE, "Global controls remain present"); checks++;
            EditText tt = screen.root.findViewById(R.id.mw_tiktok_count_input);
            tt.setText("8"); screen.showHost(SettingsStore.TIKTOK_PACKAGE);
            require(screen.tiktokSettings.root.getVisibility() == View.VISIBLE && screen.youtubeSettings.root.getVisibility() == View.GONE
                    && screen.instagramSettings.root.getVisibility() == View.GONE, "TikTok has independent visible editor"); checks++;
            require(((View)screen.tiktokSettings.longVideo.getParent()).getVisibility() == View.VISIBLE
                    && ((View)screen.live.getParent()).getVisibility() == View.GONE
                    && ((View)screen.photos.getParent()).getVisibility() == View.GONE
                    && ((View)screen.adDelay.getParent()).getVisibility() == View.GONE
                    && screen.tiktokRules.photoCard.getVisibility() == View.VISIBLE
                    && screen.tiktokRules.adsCard.getVisibility() == View.VISIBLE
                    && screen.tiktokRules.timedCard.getVisibility() == View.VISIBLE,
                    "TikTok exposes its own supported rules, not Instagram or YouTube controls"); checks++;
            require(screen.tiktokSettings.count.commit() && saves[4] == 8 && saves[0] == 9 && saves[1] == 5, "TikTok count writes only TikTok"); checks++;
            require(hasText(screen.tiktokSettings.count, screen.root.getContext().getString(R.string.tiktok_count_helper))
                    && !hasText(screen.tiktokSettings.count, screen.root.getContext().getString(R.string.count_helper)),
                    "TikTok helper distinguishes repeat zero from independent rules"); checks++;
            require(screen.tiktokSettings.quick.getText().toString().equals(screen.root.getContext().getString(R.string.tiktok_tap_quick)),
                    "TikTok floating toggle has localized repeat wording"); checks++;
            EditText ttAd = screen.root.findViewById(R.id.tt_ad_delay_input);
            EditText ttTimer = screen.root.findViewById(R.id.tt_fallback_seconds_input);
            EditText ttLong = screen.root.findViewById(R.id.mw_tiktok_long_input);
            ttAd.setText("2.7"); ttTimer.setText("6"); ttLong.setText("90");
            require(screen.tiktokRules.adDelay.commit() && saves[6] == 27 && saves[5] == 0, "TikTok ad callback is isolated"); checks++;
            require(screen.tiktokRules.seconds.commit() && saves[7] == 6, "TikTok timer callback is isolated"); checks++;
            require(screen.tiktokSettings.longVideo.commit() && saves[8] == 90 && saves[2] == 120 && saves[3] == 45, "TikTok long threshold is isolated"); checks++;
            screen.tiktokRules.photos.modes.check(R.id.tt_photo_mode_each);
            require(screen.tiktokRules.photos.isEachChoice(R.id.tt_photo_mode_each)
                    && !screen.tiktokRules.photos.isEachChoice(R.id.photo_mode_each), "TikTok photo choice uses its own stable ID"); checks++;
            require(screen.tiktokRules.photos.whole.getChildAt(0).getLabelFor() == R.id.tt_photo_whole_input
                    && screen.tiktokRules.photos.slide.getChildAt(0).getLabelFor() == R.id.tt_photo_slide_input,
                    "TikTok photo labels point to TikTok inputs, not Instagram"); checks++;
            ttAd.setText("bad"); screen.back.performClick();
            require(screen.currentHost() == null && screen.homeContent.getVisibility() == View.VISIBLE && "bad".contentEquals(ttAd.getText()) && saves[6] == 27,
                    "Back returns home without applying invalid draft"); checks++;
            screen.tiktokEntry.open.performClick(); screen.showHost(SettingsStore.TIKTOK_PACKAGE);
            require("bad".contentEquals(ttAd.getText()) && screen.detailTitle.getText().toString().equals(screen.root.getContext().getString(R.string.ui_tiktok)),
                    "Re-enter and periodic same-route render retain draft and clear app title"); checks++;
            EditText ad = screen.root.findViewById(R.id.ad_delay_input);
            screen.showHost(true); ad.setText("1.3");
            require(screen.adDelay.commit() && saves[5] == 13, "Ad decimal input stores exact tenths"); checks++;
            screen.root.findViewById(R.id.ad_delay_plus).performClick();
            require(saves[5] == 14 && "1.4".contentEquals(ad.getText()), "Ad plus changes exactly one tenth"); checks++;
            ad.setText("9.99"); require(!screen.adDelay.commit() && saves[5] == 14, "Invalid ad precision leaves setting unchanged"); checks++;
            screen.showHost(SettingsStore.TIKTOK_PACKAGE); screen.showHost(true);
            require("9.99".contentEquals(ad.getText()), "Switching hosts preserves ad draft"); checks++;
            EditText timer = screen.root.findViewById(R.id.fallback_seconds_input);
            timer.setText("2"); require(screen.seconds.commit(), "Timer accepts two seconds"); checks++;
            timer.setText("1"); require(!screen.seconds.commit(), "Timer rejects one second"); checks++;
            require(uniqueIds(screen.root, new HashSet<>()), "No colliding stable IDs across retained host trees"); checks++;
            yt.setText("abc"); ig.setText("-1"); tt.setText("100");
            SparseArray<Parcelable> saved = new SparseArray<>(); screen.root.saveHierarchyState(saved);
            SettingsScreen restored = screen(AppLocale.forLanguage(base, language.equals("ko") ? "en" : "ko"), new int[10]);
            restored.root.restoreHierarchyState(saved);
            require("abc".contentEquals(((EditText)restored.root.findViewById(R.id.count_input)).getText()), "YouTube draft restored"); checks++;
            require("-1".contentEquals(((EditText)restored.root.findViewById(R.id.mw_instagram_count_input)).getText()), "Instagram draft restored separately"); checks++;
            require(!restored.youtubeSettings.count.commit() && !restored.instagramSettings.count.commit(), "Invalid host drafts rejected"); checks++;
            require("100".contentEquals(((EditText)restored.root.findViewById(R.id.mw_tiktok_count_input)).getText())
                    && !restored.tiktokSettings.count.commit(), "TikTok invalid draft restored separately"); checks++;
            require("9.99".contentEquals(((EditText)restored.root.findViewById(R.id.ad_delay_input)).getText())
                    && !restored.adDelay.commit(), "Ad draft restored without saving"); checks++;
            require("bad".contentEquals(((EditText)restored.root.findViewById(R.id.tt_ad_delay_input)).getText())
                    && !restored.tiktokRules.adDelay.commit(), "TikTok ad draft restores independently"); checks++;
            Configuration narrow = new Configuration(base.getResources().getConfiguration()); narrow.screenWidthDp = 320; narrow.fontScale = 2f;
            SettingsScreen narrowScreen = screen(AppLocale.forLanguage(base.createConfigurationContext(narrow), language), new int[10]);
            require(narrowScreen.youtubeEntry.getParent() == narrowScreen.instagramEntry.getParent()
                    && narrowScreen.youtubeEntry.getParent() == narrowScreen.tiktokEntry.getParent()
                    && ((LinearLayout)narrowScreen.youtubeEntry.getParent()).getOrientation() == LinearLayout.VERTICAL
                    && narrowScreen.youtubeEntry.getLayoutParams().width == -1,
                    "320dp large-font app entries stack at full width"); checks++;
            require(uniqueIds(narrowScreen.root, new HashSet<>()), "Narrow layout retains unique IDs"); checks++;
        }
        return checks;
    }
    private static SettingsScreen screen(Context c, int[] saves) {
        SettingsScreen screen = new SettingsScreen(c, 7, n -> saves[0] = n, 10, n -> {}, 0, n -> {}, 60,
                n -> saves[2] = n, 3, n -> saves[1] = n, 30, n -> saves[3] = n,
                2, n -> saves[4] = n, 0, n -> saves[5] = n, 60, n -> saves[8] = n);
        screen.tiktokRules.onAdDelay = n -> saves[6] = n;
        screen.tiktokRules.onSeconds = n -> saves[7] = n;
        return screen;
    }
    private static boolean uniqueIds(View view, Set<Integer> seen) {
        if (view.getId() != View.NO_ID && !seen.add(view.getId())) return false;
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++)
            if (!uniqueIds(((ViewGroup)view).getChildAt(i), seen)) return false;
        return true;
    }
    private static boolean hasText(View view, String expected) {
        if (view instanceof TextView && expected.contentEquals(((TextView)view).getText())) return true;
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++)
            if (hasText(((ViewGroup)view).getChildAt(i), expected)) return true;
        return false;
    }
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
