package com.fullmetalsonic.shortsloop;

import android.content.Context;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import com.fullmetalsonic.shortsloop.i18n.AppLocale;
import com.fullmetalsonic.shortsloop.ui.SettingsScreen;

/** Android view-state restore must not mark unchanged values as pending edits. */
final class EditorRestoreChecks {
    static int run(Context base) {
        int checks = 0;
        int[] inputs = {R.id.count_input, R.id.fallback_seconds_input, R.id.live_delay_input, R.id.long_video_seconds_input};
        int[] apply = {R.id.count_apply, R.id.fallback_seconds_apply, R.id.live_delay_apply, R.id.long_video_seconds_apply};
        for (String from : new String[]{"ko", "en"}) {
            int[] saves = {0};
            SettingsScreen old = screen(AppLocale.forLanguage(base, from), saves);
            SparseArray<Parcelable> saved = new SparseArray<>();
            old.root.saveHierarchyState(saved);
            SettingsScreen fresh = screen(AppLocale.forLanguage(base, from.equals("ko") ? "en" : "ko"), saves);
            fresh.root.restoreHierarchyState(saved);
            for (int i = 0; i < inputs.length; i++) {
                require(((EditText)old.root.findViewById(inputs[i])).getText().toString()
                        .equals(((EditText)fresh.root.findViewById(inputs[i])).getText().toString()), "Value restored"); checks++;
                require(fresh.root.findViewById(apply[i]).getVisibility() == View.GONE, "Unchanged restore is not a draft"); checks++;
            }
            for (int id : inputs) ((EditText)old.root.findViewById(id)).setText("9");
            saved.clear(); old.root.saveHierarchyState(saved);
            fresh = screen(AppLocale.forLanguage(base, "en"), saves); fresh.root.restoreHierarchyState(saved);
            for (int i = 0; i < inputs.length; i++) {
                require("9".contentEquals(((EditText)fresh.root.findViewById(inputs[i])).getText()), "Draft retained"); checks++;
                require(fresh.root.findViewById(apply[i]).getVisibility() == View.VISIBLE, "Changed draft still pending"); checks++;
            }
            require(saves[0] == 0, "Restore never saves or changes execution"); checks++;
        }
        return checks;
    }
    private static SettingsScreen screen(Context c, int[] saves) {
        return new SettingsScreen(c, 7, n -> saves[0]++, 17, n -> saves[0]++, 4, n -> saves[0]++, 73, n -> saves[0]++);
    }
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
}
