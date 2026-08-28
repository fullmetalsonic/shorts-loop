package com.fullmetalsonic.shortsloop;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import com.fullmetalsonic.shortsloop.core.LongVideoPolicy;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.ui.LongVideoPanel;
import java.util.HashMap;
import java.util.Map;

/** Synthetic UI checks only. CompatibilityInstrumentation restores all original preferences. */
public final class LongVideoUiChecks {
    private LongVideoUiChecks() {}
    public static int run(Activity activity, SettingsStore store) {
        int checks = 0;
        boolean ready = (installed(activity, SettingsStore.YOUTUBE_PACKAGE) && store.youtubeEnabled())
                || (installed(activity, SettingsStore.INSTAGRAM_PACKAGE) && store.instagramEnabled());
        Switch actual = activity.findViewById(R.id.skip_long_toggle);
        require(actual != null && actual.isEnabled() == ready, "Either selected installed host enables long-video controls"); checks++;
        require(actual.isChecked() == (ready && store.skipLong()), "Unavailable long-video choice does not look active"); checks++;
        require(store.skipLong() && store.longVideoSeconds() == 321, "Rendering preserves saved long-video choice and threshold"); checks++;
        require(((TextView)activity.findViewById(R.id.long_video_support)).getText().toString().equals(activity.getString(
                ready ? R.string.long_video_host_ready : R.string.long_video_host_unavailable)), "Specific host guidance"); checks++;
        require(!store.enabled(), "Long-video panel does not start execution on render"); checks++;
        View repeatCard = (View)activity.findViewById(R.id.count_input).getParent().getParent().getParent();
        View longCard = (View)activity.findViewById(R.id.long_video_panel).getParent();
        ViewGroup cards = (ViewGroup)longCard.getParent();
        require(repeatCard.getParent() == cards && cards.indexOfChild(longCard) == cards.indexOfChild(repeatCard) + 1,
                "Long-video card immediately follows ordinary repeat card"); checks++;
        store.target(0);
        Map<String, Object> originalOthers = unrelated(store);
        LongVideoPanel panel = new LongVideoPanel(activity, LongVideoPolicy.DEFAULT_SECONDS, store::longVideoSeconds);
        EditText input = panel.findViewById(R.id.long_video_seconds_input);
        Button plus = panel.findViewById(R.id.long_video_seconds_plus), minus = panel.findViewById(R.id.long_video_seconds_minus);
        Button apply = panel.findViewById(R.id.long_video_seconds_apply);
        require(input.getText().toString().equals("60") && plus.isEnabled() && minus.isEnabled(), "Default threshold sixty"); checks++;
        require(store.longVideoSeconds() == 321, "Panel creation does not overwrite saved threshold"); checks++;
        input.setText("100"); plus.performClick();
        require(store.longVideoSeconds() == 101 && input.getText().toString().equals("101"), "Typed threshold plus one"); checks++;
        minus.performClick(); require(store.longVideoSeconds() == 100, "Threshold minus one"); checks++;
        for (String invalid : new String[]{"", "0", "-1", "NaN", "1.5", "3601", "999999999999999"}) {
            input.setText(invalid);
            require(input.getText().toString().equals(invalid), "Invalid raw threshold is preserved"); checks++;
            require(!panel.commit() && store.longVideoSeconds() == 100, "Invalid draft does not save a new threshold"); checks++;
        }
        input.setText("-1"); plus.performClick();
        require(store.longVideoSeconds() == 100 && input.getText().toString().equals("-1"), "Arrow cannot coerce invalid draft"); checks++;
        input.setText("1"); apply.performClick();
        require(store.longVideoSeconds() == 1 && !minus.isEnabled(), "Lower bound applies and prevents lower step"); checks++;
        input.setText("3600"); require(panel.commit() && store.longVideoSeconds() == 3600 && !plus.isEnabled(), "Upper bound applies and prevents higher step"); checks++;
        input.setText("75"); input.onEditorAction(EditorInfo.IME_ACTION_DONE);
        require(store.longVideoSeconds() == 75 && input.getText().toString().equals("75"), "Keyboard Done applies threshold"); checks++;
        input.setText("7"); panel.render(90);
        require(input.getText().toString().equals("7") && store.longVideoSeconds() == 75, "Refresh preserves active threshold draft"); checks++;
        panel.setAvailable(false);
        require(!input.isEnabled() && !panel.toggle.isEnabled() && !panel.commit() && store.longVideoSeconds() == 75,
                "No available host prevents committing without losing saved setting"); checks++;
        panel.setAvailable(true);
        require(input.getText().toString().equals("7") && panel.commit() && store.longVideoSeconds() == 7,
                "Returning to an available host preserves and can apply draft"); checks++;
        require(store.skipLong() && store.target() == 0 && !store.enabled(), "Threshold independent of zero count and execution"); checks++;
        require(originalOthers.equals(unrelated(store)), "Long-video UI preserves every unrelated preference"); checks++;
        LongVideoPanel sanitized = new LongVideoPanel(activity, -1, value -> { throw new AssertionError("Binding must not commit"); });
        require(((EditText)sanitized.findViewById(R.id.long_video_seconds_input)).getText().toString().equals("60"),
                "Invalid initial display uses default without saving"); checks++;
        require(LongVideoPolicy.clamp(0) == 1 && LongVideoPolicy.clamp(3601) == 3600, "Threshold step clamps to1..3600"); checks++;
        return checks;
    }
    private static boolean installed(Activity activity, String pkg) {
        try { activity.getPackageManager().getApplicationInfo(pkg, 0); return true; }
        catch (android.content.pm.PackageManager.NameNotFoundException ignored) { return false; }
    }
    private static Map<String, Object> unrelated(SettingsStore store) {
        Map<String, Object> values = new HashMap<>(store.preferences.getAll());
        values.remove("skip_long"); values.remove("long_video_seconds"); return values;
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
