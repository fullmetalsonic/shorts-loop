package com.fullmetalsonic.shortsloop;

import android.content.Context;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.fullmetalsonic.shortsloop.i18n.AppLocale;
import com.fullmetalsonic.shortsloop.ui.SettingsScreen;
import java.util.HashSet;
import java.util.Set;

/** Native retained-tree checks; does not grant permissions or start social apps. */
final class HostSettingsUiChecks {
    static int run(Context base) {
        int checks = 0;
        for (String language : new String[]{"ko", "en"}) {
            int[] saves = {0, 0, 0, 0};
            SettingsScreen screen = screen(AppLocale.forLanguage(base, language), saves);
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
            require(uniqueIds(screen.root, new HashSet<>()), "No colliding stable IDs across retained host trees"); checks++;
            yt.setText("abc"); ig.setText("-1");
            SparseArray<Parcelable> saved = new SparseArray<>(); screen.root.saveHierarchyState(saved);
            SettingsScreen restored = screen(AppLocale.forLanguage(base, language.equals("ko") ? "en" : "ko"), new int[4]);
            restored.root.restoreHierarchyState(saved);
            require("abc".contentEquals(((EditText)restored.root.findViewById(R.id.count_input)).getText()), "YouTube draft restored"); checks++;
            require("-1".contentEquals(((EditText)restored.root.findViewById(R.id.mw_instagram_count_input)).getText()), "Instagram draft restored separately"); checks++;
            require(!restored.youtubeSettings.count.commit() && !restored.instagramSettings.count.commit(), "Invalid host drafts rejected"); checks++;
        }
        return checks;
    }
    private static SettingsScreen screen(Context c, int[] saves) {
        return new SettingsScreen(c, 7, n -> saves[0] = n, 10, n -> {}, 0, n -> {}, 60,
                n -> saves[2] = n, 3, n -> saves[1] = n, 30, n -> saves[3] = n);
    }
    private static boolean uniqueIds(View view, Set<Integer> seen) {
        if (view.getId() != View.NO_ID && !seen.add(view.getId())) return false;
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++)
            if (!uniqueIds(((ViewGroup)view).getChildAt(i), seen)) return false;
        return true;
    }
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
