package com.fullmetalsonic.shortsloop.ui;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.fullmetalsonic.shortsloop.R;
import com.fullmetalsonic.shortsloop.core.PhotoReelPolicy;
import java.util.function.IntConsumer;

/** Independent draft, strict paste validation, stable IDs and native saved-view-state support. */
@android.annotation.SuppressLint("ViewConstructor")
public final class PhotoSecondsEditor extends LinearLayout {
    public IntConsumer changed = value -> { };
    private final EditText input;
    private final Button minus, plus, apply;
    private boolean binding, dirty, available = true;
    private int committed = PhotoReelPolicy.DEFAULT_SECONDS;
    public PhotoSecondsEditor(Context c, int inputId, int minusId, int plusId, int applyId, int label) {
        super(c); setOrientation(VERTICAL);
        TextView title = UiTheme.text(c, c.getString(label), 14, UiTheme.TEXT, true);
        title.setLabelFor(inputId); addView(title);
        LinearLayout row = new LinearLayout(c); row.setGravity(Gravity.CENTER_VERTICAL);
        minus = UiTheme.button(c, c.getString(R.string.ui_minus_symbol)); minus.setId(minusId);
        plus = UiTheme.button(c, c.getString(R.string.ui_plus_symbol)); plus.setId(plusId);
        minus.setContentDescription(c.getString(R.string.photo_decrease, c.getString(label)));
        plus.setContentDescription(c.getString(R.string.photo_increase, c.getString(label)));
        input = new EditText(c); input.setId(inputId); input.setSingleLine(true);
        input.setKeyListener(TextKeyListener.getInstance()); input.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE); input.setSelectAllOnFocus(true);
        input.setContentDescription(c.getString(label)); input.setTextSize(28); input.setTextColor(UiTheme.TEXT);
        input.setGravity(Gravity.CENTER); input.setMinimumHeight(UiTheme.dp(c, 64));
        input.setBackground(UiTheme.surface(c, UiTheme.BACKGROUND, 14, true));
        row.addView(minus, new LayoutParams(UiTheme.dp(c, 52), -2));
        LayoutParams ip = new LayoutParams(0, -2, 1); ip.leftMargin = ip.rightMargin = UiTheme.dp(c, 4);
        row.addView(input, ip); row.addView(plus, new LayoutParams(UiTheme.dp(c, 52), -2)); addView(row);
        apply = UiTheme.button(c, c.getString(R.string.photo_seconds_apply)); apply.setId(applyId);
        addView(apply, new LayoutParams(-1, -2)); bind(committed);
        input.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding) return;
                dirty = !s.toString().equals(Integer.toString(committed)); input.setError(null); update();
            }
            public void afterTextChanged(Editable value) { }
        });
        minus.setOnClickListener(v -> step(-1)); plus.setOnClickListener(v -> step(1)); apply.setOnClickListener(v -> commit());
        input.setOnEditorActionListener((v, action, event) -> {
            boolean enter = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
            if (action != EditorInfo.IME_ACTION_DONE && !enter) return false;
            if (!enter || event.getAction() == KeyEvent.ACTION_DOWN) commit();
            return true;
        });
    }
    public void render(int value, boolean available) {
        this.available = available;
        if (!dirty && committed != value) bind(value);
        update();
    }
    public boolean commit() {
        if (!dirty) return true;
        Integer value = PhotoReelPolicy.parseSeconds(input.getText());
        if (!available || value == null) { input.setError(getContext().getString(R.string.photo_seconds_error)); return false; }
        save(value); input.clearFocus();
        android.view.inputmethod.InputMethodManager keyboard = getContext().getSystemService(android.view.inputmethod.InputMethodManager.class);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(input.getWindowToken(), 0);
        return true;
    }
    private void step(int delta) {
        if (!available) return;
        Integer value = PhotoReelPolicy.parseSeconds(input.getText());
        if (value == null) { input.setError(getContext().getString(R.string.photo_seconds_error)); return; }
        save(Math.max(0, Math.min(PhotoReelPolicy.MAX_SECONDS, value + delta)));
    }
    private void save(int value) { dirty = false; bind(value); changed.accept(value); }
    private void bind(int value) {
        committed = PhotoReelPolicy.seconds(value); binding = true; input.setText(String.format(java.util.Locale.ROOT, "%d", committed)); binding = false; update();
    }
    private void update() {
        input.setEnabled(available); apply.setEnabled(available); apply.setVisibility(dirty ? VISIBLE : GONE);
        minus.setEnabled(available && (dirty || committed > 0)); plus.setEnabled(available && (dirty || committed < 10));
    }
}
