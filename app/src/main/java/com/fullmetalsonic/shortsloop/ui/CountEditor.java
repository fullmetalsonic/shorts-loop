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
        input.setSelectAllOnFocus(true); input.setContentDescription("기준 반복 횟수, 0에서 99회");
        input.setBackground(UiTheme.surface(c, UiTheme.BACKGROUND, 14, true));
        row.addView(input, new LinearLayout.LayoutParams(0, UiTheme.dp(c, 104), 1));
        LinearLayout arrows = UiTheme.column(c);
        up = UiTheme.button(c, "▲"); up.setId(R.id.count_up); up.setContentDescription("반복 횟수 한 회 늘리기");
        down = UiTheme.button(c, "▼"); down.setId(R.id.count_down); down.setContentDescription("반복 횟수 한 회 줄이기");
        up.setFocusable(false); down.setFocusable(false);
        arrows.addView(up, new LinearLayout.LayoutParams(-1, UiTheme.dp(c, 52)));
        arrows.addView(down, new LinearLayout.LayoutParams(-1, UiTheme.dp(c, 52)));
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(UiTheme.dp(c, 64), -2); ap.leftMargin = UiTheme.dp(c, 8);
        row.addView(arrows, ap); addView(row);
        detail = UiTheme.text(c, "", 14, UiTheme.MUTED, false); detail.setPadding(0, UiTheme.dp(c, 10), 0, 0); addView(detail);
        apply = UiTheme.button(c, "입력한 횟수 적용"); apply.setId(R.id.count_apply); addView(apply, new LinearLayout.LayoutParams(-1, -2));
        apply.setVisibility(GONE); bind(initial);
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding) return;
                dirty = true; input.setError(null); apply.setVisibility(VISIBLE); up.setEnabled(true); down.setEnabled(true);
                detail.setText("입력 후 키보드의 완료 또는 적용을 눌러 주세요.");
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
    private void invalid() { input.setError("0~99 사이의 정수를 입력해 주세요"); input.requestFocus(); }
    private void save(int value) { dirty = false; bind(value); listener.changed(value); }
    public void render(int value) { if (!dirty && committed != value) bind(value); }
    private void bind(int value) {
        committed = value; binding = true; input.setText(String.format(java.util.Locale.ROOT, "%d", value)); binding = false;
        apply.setVisibility(GONE); detail.setText(R.string.count_helper);
        up.setEnabled(value < 99); down.setEnabled(value > 0);
    }
}
