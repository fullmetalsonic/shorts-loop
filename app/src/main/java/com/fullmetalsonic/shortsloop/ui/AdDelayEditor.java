package com.fullmetalsonic.shortsloop.ui;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.fullmetalsonic.shortsloop.R;
import com.fullmetalsonic.shortsloop.core.AdDelayPolicy;

/** Tenths-of-a-second editor. Incomplete text remains an unapplied draft. */
@android.annotation.SuppressLint("ViewConstructor")
public final class AdDelayEditor extends LinearLayout {
    public interface Listener { void changed(int tenths); }
    private final EditText input;
    private final TextView detail;
    private final Button apply, minus, plus;
    private final Listener listener;
    private boolean binding, dirty, available = true;
    private int committed;

    public AdDelayEditor(Context context, int initial, Listener listener) {
        super(context); this.listener = listener; setOrientation(VERTICAL);
        addView(UiTheme.text(context, context.getString(R.string.ad_delay_title), 14, UiTheme.TEXT, true));
        LinearLayout row = new LinearLayout(context); row.setGravity(Gravity.CENTER_VERTICAL);
        minus = UiTheme.button(context, context.getString(R.string.ui_minus_symbol)); minus.setId(R.id.ad_delay_minus);
        minus.setContentDescription(context.getString(R.string.ad_delay_decrease)); minus.setFocusable(false);
        minus.setMinHeight(UiTheme.dp(context, 64)); minus.setMinimumHeight(UiTheme.dp(context, 64));
        row.addView(minus, new LinearLayout.LayoutParams(UiTheme.dp(context, 52), -2));
        input = new EditText(context); input.setId(R.id.ad_delay_input); input.setSingleLine(true);
        input.setKeyListener(TextKeyListener.getInstance()); input.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE); input.setSelectAllOnFocus(true);
        input.setContentDescription(context.getString(R.string.ad_delay_description));
        input.setMinimumHeight(UiTheme.dp(context, 64)); input.setTextSize(30); input.setTextColor(UiTheme.TEXT); input.setGravity(Gravity.CENTER);
        input.setBackground(UiTheme.surface(context, UiTheme.BACKGROUND, 14, true));
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, -2, 1);
        textParams.leftMargin = textParams.rightMargin = UiTheme.dp(context, 8); row.addView(input, textParams);
        plus = UiTheme.button(context, context.getString(R.string.ui_plus_symbol)); plus.setId(R.id.ad_delay_plus);
        plus.setContentDescription(context.getString(R.string.ad_delay_increase)); plus.setFocusable(false);
        plus.setMinHeight(UiTheme.dp(context, 64)); plus.setMinimumHeight(UiTheme.dp(context, 64));
        row.addView(plus, new LinearLayout.LayoutParams(UiTheme.dp(context, 52), -2)); addView(row);
        detail = UiTheme.text(context, "", 13, UiTheme.MUTED, false); addView(detail);
        apply = UiTheme.button(context, context.getString(R.string.ad_delay_apply)); apply.setId(R.id.ad_delay_apply); addView(apply, new LinearLayout.LayoutParams(-1, -2));
        bind(initial);
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding) return;
                dirty = !s.toString().equals(AdDelayPolicy.format(committed)); input.setError(null);
                apply.setVisibility(dirty ? VISIBLE : GONE); detail.setText(dirty ? R.string.ui_input_draft : R.string.ad_delay_saved); updateAvailability();
            }
            @Override public void afterTextChanged(Editable value) {}
        });
        input.setOnEditorActionListener((v, action, event) -> {
            boolean enter = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
            if (action != EditorInfo.IME_ACTION_DONE && !enter) return false;
            if (!enter || event.getAction() == KeyEvent.ACTION_DOWN) commit();
            return true;
        });
        apply.setOnClickListener(v -> commit()); minus.setOnClickListener(v -> step(-1)); plus.setOnClickListener(v -> step(1));
    }
    public void render(int tenths) { if (!dirty && committed != tenths) bind(tenths); }
    public void setAvailable(boolean value) { available = value; updateAvailability(); }
    public boolean commit() {
        if (!dirty) return true;
        Integer value = AdDelayPolicy.parseTenths(input.getText());
        if (value == null) { invalid(); return false; }
        save(value); input.clearFocus();
        android.view.inputmethod.InputMethodManager keyboard = getContext().getSystemService(android.view.inputmethod.InputMethodManager.class);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(input.getWindowToken(), 0);
        return true;
    }
    private void step(int delta) {
        Integer value = AdDelayPolicy.parseTenths(input.getText());
        if (value == null) { invalid(); return; }
        save(Math.max(0, Math.min(AdDelayPolicy.MAX_TENTHS, value + delta)));
    }
    private void invalid() { input.setError(getContext().getString(R.string.ad_delay_error)); input.requestFocus(); }
    private void save(int value) { dirty = false; bind(value); listener.changed(value); }
    private void bind(int value) {
        committed = AdDelayPolicy.sanitize(value); binding = true; input.setText(AdDelayPolicy.format(committed)); binding = false;
        apply.setVisibility(GONE); detail.setText(R.string.ad_delay_saved); updateAvailability();
    }
    private void updateAvailability() {
        input.setEnabled(available); apply.setEnabled(available);
        minus.setEnabled(available && (dirty || committed > 0)); plus.setEnabled(available && (dirty || committed < AdDelayPolicy.MAX_TENTHS));
    }
}
