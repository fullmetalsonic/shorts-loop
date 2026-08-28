package com.fullmetalsonic.shortsloop.ui;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.fullmetalsonic.shortsloop.R;
import com.fullmetalsonic.shortsloop.core.SettingsPolicy;

/** Typing stays a draft until Done/Apply; arrows commit a single valid step. */
@android.annotation.SuppressLint("ViewConstructor") // Programmatic component with an explicit settings callback.
public final class CountEditor extends LinearLayout {
    public interface Listener { void changed(int value); }
    private final EditText input;
    private final TextView detail;
    private final Button apply, up, down;
    private final Listener listener;
    private boolean binding, dirty;
    private int committed;
    public CountEditor(Context c, int initial, Listener listener) {
        super(c); this.listener = listener; setOrientation(VERTICAL); committed = initial;
        LinearLayout row = new LinearLayout(c); row.setGravity(Gravity.CENTER_VERTICAL);
        input = new EditText(c); input.setId(R.id.count_input); input.setSingleLine(true);
        // Request a numeric keyboard without deleting signs/decimals from pasted text.
        // Validate the complete draft on commit; never truncate it into a different count.
        input.setKeyListener(TextKeyListener.getInstance());
        input.setRawInputType(InputType.TYPE_CLASS_NUMBER); input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setTextSize(40); input.setTextColor(UiTheme.TEXT); input.setGravity(Gravity.CENTER);
        input.setSelectAllOnFocus(true); input.setContentDescription(getContext().getString(R.string.ui_count_description));
        input.setBackground(UiTheme.surface(c, UiTheme.BACKGROUND, 14, true));
        input.setMinimumHeight(UiTheme.dp(c, 104));
        row.addView(input, new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout arrows = UiTheme.column(c);
        up = UiTheme.button(c, getContext().getString(R.string.ui_count_up_symbol)); up.setId(R.id.count_up); up.setContentDescription(getContext().getString(R.string.ui_count_increase));
        down = UiTheme.button(c, getContext().getString(R.string.ui_count_down_symbol)); down.setId(R.id.count_down); down.setContentDescription(getContext().getString(R.string.ui_count_decrease));
        up.setFocusable(false); down.setFocusable(false);
        up.setMinHeight(UiTheme.dp(c, 52)); up.setMinimumHeight(UiTheme.dp(c, 52));
        arrows.addView(up, new LinearLayout.LayoutParams(-1, -2));
        down.setMinHeight(UiTheme.dp(c, 52)); down.setMinimumHeight(UiTheme.dp(c, 52));
        arrows.addView(down, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(UiTheme.dp(c, 64), -2); ap.leftMargin = UiTheme.dp(c, 8);
        row.addView(arrows, ap); addView(row);
        detail = UiTheme.text(c, "", 14, UiTheme.MUTED, false); detail.setPadding(0, UiTheme.dp(c, 10), 0, 0); addView(detail);
        apply = UiTheme.button(c, getContext().getString(R.string.ui_count_apply)); apply.setId(R.id.count_apply); addView(apply, new LinearLayout.LayoutParams(-1, -2));
        apply.setVisibility(GONE); bind(initial);
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding) return;
                dirty = !s.toString().equals(Integer.toString(committed));
                input.setError(null); apply.setVisibility(dirty ? VISIBLE : GONE);
                up.setEnabled(dirty || committed < 99); down.setEnabled(dirty || committed > 0);
                detail.setText(dirty ? R.string.ui_input_draft : R.string.count_helper);
            }
            @Override public void afterTextChanged(Editable e) {}
        });
        input.setOnEditorActionListener((v, action, event) -> {
            if (action != EditorInfo.IME_ACTION_DONE) return false;
            commit(); return true;
        });
        apply.setOnClickListener(v -> commit());
        up.setOnClickListener(v -> step(1)); down.setOnClickListener(v -> step(-1));
    }
    private void step(int delta) {
        Integer value = SettingsPolicy.parseCount(input.getText());
        if (value == null) { invalid(); return; }
        save(Math.max(0, Math.min(99, value + delta)));
    }
    public boolean commit() {
        if (!dirty) return true;
        Integer value = SettingsPolicy.parseCount(input.getText());
        if (value == null) { invalid(); return false; }
        save(value); input.clearFocus();
        android.view.inputmethod.InputMethodManager keyboard = getContext().getSystemService(android.view.inputmethod.InputMethodManager.class);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(input.getWindowToken(), 0);
        return true;
    }
    private void invalid() { input.setError(getContext().getString(R.string.ui_count_error)); input.requestFocus(); }
    private void save(int value) { dirty = false; bind(value); listener.changed(value); }
    public void render(int value) { if (!dirty && committed != value) bind(value); }
    private void bind(int value) {
        committed = value; binding = true; input.setText(String.format(java.util.Locale.ROOT, "%d", value)); binding = false;
        apply.setVisibility(GONE); detail.setText(R.string.count_helper);
        up.setEnabled(value < 99); down.setEnabled(value > 0);
    }
}
