package com.fullmetalsonic.shortsloop.ui;

import android.content.Context;
import android.widget.*;
import com.fullmetalsonic.shortsloop.R;
import com.fullmetalsonic.shortsloop.core.PhotoReelPolicy;
import com.fullmetalsonic.shortsloop.data.SettingsStore;

@android.annotation.SuppressLint("ViewConstructor")
public final class PhotoReelPanel extends LinearLayout {
    public final Switch toggle, fallback;
    public final RadioGroup modes;
    public final PhotoSecondsEditor whole, slide;
    public final TextView support;
    private final RadioButton wholeChoice, eachChoice;
    public PhotoReelPanel(Context c) {
        super(c); setOrientation(VERTICAL); setId(R.id.photo_panel);
        addView(UiTheme.text(c, c.getString(R.string.photo_independent), 14, UiTheme.CYAN, true));
        toggle = toggle(c, R.string.photo_toggle, R.id.photo_toggle); addView(toggle);
        support = UiTheme.text(c, "", 13, UiTheme.CYAN, false); addView(support);
        modes = new RadioGroup(c); modes.setId(R.id.photo_modes); modes.setOrientation(VERTICAL);
        wholeChoice = choice(c, R.string.photo_mode_whole, R.id.photo_mode_whole);
        eachChoice = choice(c, R.string.photo_mode_each, R.id.photo_mode_each);
        modes.addView(wholeChoice); modes.addView(eachChoice); addView(modes);
        whole = new PhotoSecondsEditor(c, R.id.photo_whole_input, R.id.photo_whole_minus, R.id.photo_whole_plus, R.id.photo_whole_apply, R.string.photo_whole_seconds);
        slide = new PhotoSecondsEditor(c, R.id.photo_slide_input, R.id.photo_slide_minus, R.id.photo_slide_plus, R.id.photo_slide_apply, R.string.photo_slide_seconds);
        UiTheme.space(c, this, 10); addView(whole); UiTheme.space(c, this, 10); addView(slide);
        fallback = toggle(c, R.string.photo_fallback, R.id.photo_fallback); addView(fallback);
        addView(UiTheme.text(c, c.getString(R.string.photo_help), 13, UiTheme.MUTED, false));
    }
    public void render(SettingsStore store, boolean available) {
        toggle.setEnabled(available); toggle.setChecked(store.photoEnabled() && available);
        modes.check(store.photoMode() == PhotoReelPolicy.EACH ? R.id.photo_mode_each : R.id.photo_mode_whole);
        wholeChoice.setEnabled(available); eachChoice.setEnabled(available);
        whole.render(store.photoWholeSeconds(), available); slide.render(store.photoSlideSeconds(), available);
        fallback.setChecked(store.photoFallback()); fallback.setEnabled(available && store.photoMode() == PhotoReelPolicy.EACH);
    }
    public boolean commit(SettingsStore store) {
        if (store.photoMode() == PhotoReelPolicy.WHOLE) return whole.commit();
        return slide.commit() && (!store.photoFallback() || whole.commit());
    }
    private static Switch toggle(Context c, int label, int id) {
        Switch view = new Switch(c); view.setId(id); view.setText(label); view.setTextSize(16); view.setSingleLine(false);
        view.setTextColor(UiTheme.TEXT); view.setMinHeight(UiTheme.dp(c, 52)); view.setSwitchPadding(UiTheme.dp(c, 16));
        view.setThumbTintList(UiTheme.checkedColors()); view.setTrackTintList(android.content.res.ColorStateList.valueOf(UiTheme.BORDER));
        return view;
    }
    private static RadioButton choice(Context c, int label, int id) {
        RadioButton view = new RadioButton(c); view.setId(id); view.setText(label); view.setTextSize(15); view.setSingleLine(false);
        view.setTextColor(UiTheme.TEXT); view.setMinHeight(UiTheme.dp(c, 56)); view.setButtonTintList(UiTheme.checkedColors());
        view.setLayoutParams(new RadioGroup.LayoutParams(-1, -2)); return view;
    }
}
