package com.fullmetalsonic.shortsloop.ui;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.fullmetalsonic.shortsloop.R;

/** A retained editor tree per host: switching tabs never commits or replaces a draft. */
public final class HostSettingsPanel {
    public final LinearLayout root;
    public final CountEditor count;
    public final LongVideoPanel longVideo;
    public final TextView applied, state;
    public final Button resume;
    public final RadioGroup tapModes;
    public final RadioButton rotary, quick;
    public HostSettingsPanel(Context c, int initial, CountEditor.Listener countListener,
            int initialSeconds, LongVideoPanel.Listener longListener, boolean instagram) {
        root = UiTheme.column(c);
        root.setId(instagram ? R.id.mw_instagram_settings : R.id.mw_youtube_settings);
        LinearLayout repeat = UiTheme.card(c, root, c.getString(R.string.ui_repeat_title));
        repeat.addView(UiTheme.text(c, c.getString(instagram ? R.string.mw_instagram_repeat : R.string.mw_youtube_repeat), 14, UiTheme.MUTED, false));
        UiTheme.space(c, repeat, 12);
        count = new CountEditor(c, initial, countListener); repeat.addView(count);
        applied = UiTheme.text(c, "", 14, UiTheme.CYAN, false); repeat.addView(applied);
        state = UiTheme.text(c, "", 14, UiTheme.MUTED, false); state.setId(instagram ? R.id.mw_instagram_state : R.id.mw_youtube_state); repeat.addView(state);
        resume = UiTheme.button(c, c.getString(R.string.mw_resume_host));
        resume.setVisibility(View.GONE);
        resume.setId(instagram ? R.id.mw_instagram_resume : R.id.mw_youtube_resume); repeat.addView(resume);
        LinearLayout longCard = UiTheme.card(c, root, c.getString(R.string.long_video_card_title));
        longVideo = new LongVideoPanel(c, initialSeconds, longListener); longCard.addView(longVideo);
        LinearLayout taps = UiTheme.card(c, root, c.getString(R.string.mw_host_floating_title));
        taps.addView(UiTheme.text(c, c.getString(R.string.mw_host_floating_help), 13, UiTheme.MUTED, false));
        tapModes = new RadioGroup(c); tapModes.setOrientation(RadioGroup.VERTICAL);
        rotary = radio(c, R.string.ui_tap_rotary, instagram ? R.id.mw_instagram_rotary : R.id.tap_rotary);
        quick = radio(c, R.string.ui_tap_quick, instagram ? R.id.mw_instagram_quick : R.id.tap_quick);
        tapModes.addView(rotary); tapModes.addView(quick); taps.addView(tapModes);
        if (instagram) remapIds(root);
    }
    private static RadioButton radio(Context c, int label, int id) {
        RadioButton view = new RadioButton(c); view.setId(id); view.setText(label); view.setTextSize(15);
        view.setTextColor(UiTheme.TEXT); view.setSingleLine(false); view.setButtonTintList(UiTheme.checkedColors());
        view.setMinHeight(UiTheme.dp(c, 64)); view.setPadding(0, UiTheme.dp(c, 6), 0, UiTheme.dp(c, 6));
        view.setLayoutParams(new RadioGroup.LayoutParams(-1, -2)); return view;
    }
    private static void remapIds(View root) {
        int[] oldIds = {R.id.count_input, R.id.count_up, R.id.count_down, R.id.count_apply,
                R.id.long_video_panel, R.id.skip_long_toggle, R.id.long_video_support, R.id.long_video_seconds_input,
                R.id.long_video_seconds_minus, R.id.long_video_seconds_plus, R.id.long_video_seconds_apply};
        int[] newIds = {R.id.mw_instagram_count_input, R.id.mw_instagram_count_up, R.id.mw_instagram_count_down, R.id.mw_instagram_count_apply,
                R.id.mw_instagram_long_panel, R.id.mw_instagram_skip_long, R.id.mw_instagram_long_support, R.id.mw_instagram_long_input,
                R.id.mw_instagram_long_minus, R.id.mw_instagram_long_plus, R.id.mw_instagram_long_apply};
        for (int i = 0; i < oldIds.length; i++) root.findViewById(oldIds[i]).setId(newIds[i]);
    }
}
