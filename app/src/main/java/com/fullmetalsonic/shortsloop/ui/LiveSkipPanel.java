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
import android.widget.Switch;
import android.widget.TextView;
import com.fullmetalsonic.shortsloop.R;
import com.fullmetalsonic.shortsloop.core.LiveSkipPolicy;

/** YouTube-only controls with an independent delay draft; Instagram's timer is untouched. */
@android.annotation.SuppressLint("ViewConstructor")
public final class LiveSkipPanel extends LinearLayout {
    public interface Listener { void changed(int seconds); }
    public final Switch toggle;
    public final TextView support;
    private final EditText input;
    private final TextView detail;
    private final Button minus, plus, apply;
    private final Listener listener;
    private boolean binding, dirty;
    private boolean available = true;
    private int committed;

    public LiveSkipPanel(Context context, int initial, Listener listener) {
        super(context); this.listener = listener; setOrientation(VERTICAL); setId(R.id.live_skip_panel);
        addView(UiTheme.text(context, context.getString(R.string.live_independent), 14, UiTheme.CYAN, true));
        toggle = new Switch(context); toggle.setId(R.id.skip_live_toggle); toggle.setText(R.string.live_toggle);
        toggle.setTextSize(16); toggle.setTextColor(UiTheme.TEXT); toggle.setMinHeight(UiTheme.dp(context, 52));
        toggle.setThumbTintList(UiTheme.checkedColors());
        toggle.setTrackTintList(android.content.res.ColorStateList.valueOf(UiTheme.BORDER));
        toggle.setSwitchPadding(UiTheme.dp(context, 16)); addView(toggle);
        support = UiTheme.text(context, "", 13, UiTheme.CYAN, false); support.setId(R.id.live_support); addView(support);
        addView(UiTheme.text(context, context.getString(R.string.live_helper), 13, UiTheme.MUTED, false));
        UiTheme.space(context, this, 12);
        addView(UiTheme.text(context, context.getString(R.string.live_delay_title), 14, UiTheme.TEXT, true));
        LinearLayout row = new LinearLayout(context); row.setGravity(Gravity.CENTER_VERTICAL);
        minus = UiTheme.button(context, "−"); minus.setId(R.id.live_delay_minus);
        minus.setContentDescription(context.getString(R.string.live_delay_minus_description)); minus.setFocusable(false);
        row.addView(minus, new LinearLayout.LayoutParams(UiTheme.dp(context, 52), UiTheme.dp(context, 64)));
        input = new EditText(context); input.setId(R.id.live_delay_input); input.setSingleLine(true);
        // Keep pasted signs and decimals intact so validation can reject the whole draft.
        input.setKeyListener(TextKeyListener.getInstance()); input.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE); input.setSelectAllOnFocus(true);
        input.setContentDescription(context.getString(R.string.live_delay_description));
        input.setTextSize(30); input.setTextColor(UiTheme.TEXT); input.setGravity(Gravity.CENTER);
        input.setBackground(UiTheme.surface(context, UiTheme.BACKGROUND, 14, true));
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(0, UiTheme.dp(context, 64), 1);
        ip.leftMargin = UiTheme.dp(context, 8); ip.rightMargin = UiTheme.dp(context, 8); row.addView(input, ip);
        plus = UiTheme.button(context, "+"); plus.setId(R.id.live_delay_plus);
        plus.setContentDescription(context.getString(R.string.live_delay_plus_description)); plus.setFocusable(false);
        row.addView(plus, new LinearLayout.LayoutParams(UiTheme.dp(context, 52), UiTheme.dp(context, 64))); addView(row);
        detail = UiTheme.text(context, "", 13, UiTheme.MUTED, false);
        detail.setPadding(0, UiTheme.dp(context, 6), 0, 0); addView(detail);
        apply = UiTheme.button(context, context.getString(R.string.live_delay_apply)); apply.setId(R.id.live_delay_apply);
        addView(apply, new LinearLayout.LayoutParams(-1, -2)); bind(initial);
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence text, int start, int before, int count) {
                if (binding) return;
                dirty = true; input.setError(null); apply.setVisibility(VISIBLE); updateAvailability();
                detail.setText(R.string.live_delay_draft);
            }
            @Override public void afterTextChanged(Editable text) {}
        });
        input.setOnEditorActionListener((view, action, event) -> {
            boolean enter = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
            if (action != EditorInfo.IME_ACTION_DONE && !enter) return false;
            if (!enter || event.getAction() == KeyEvent.ACTION_DOWN) commit();
            return true;
        });
        apply.setOnClickListener(view -> commit());
        minus.setOnClickListener(view -> step(-1)); plus.setOnClickListener(view -> step(1));
    }

    public void setAvailable(boolean value) { available = value; toggle.setEnabled(value); updateAvailability(); }
    public void render(int seconds) { if (!dirty && committed != seconds) bind(seconds); }
    public boolean commit() {
        if (!dirty) return true;
        if (!available) return false;
        Integer seconds = LiveSkipPolicy.parseSeconds(input.getText());
        if (seconds == null) { invalid(); return false; }
        save(seconds); input.clearFocus();
        android.view.inputmethod.InputMethodManager keyboard = getContext().getSystemService(android.view.inputmethod.InputMethodManager.class);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(input.getWindowToken(), 0);
        return true;
    }
    private void step(int delta) {
        if (!available) return;
        Integer seconds = LiveSkipPolicy.parseSeconds(input.getText());
        if (seconds == null) { invalid(); return; }
        save(LiveSkipPolicy.clamp(seconds + delta));
    }
    private void invalid() { input.setError(getContext().getString(R.string.live_delay_error)); input.requestFocus(); }
    private void save(int seconds) { dirty = false; bind(seconds); listener.changed(seconds); }
    private void bind(int seconds) {
        committed = LiveSkipPolicy.sanitizeSeconds(seconds); binding = true;
        input.setText(String.format(java.util.Locale.ROOT, "%d", committed)); binding = false;
        apply.setVisibility(GONE);
        detail.setText(committed == 0 ? getContext().getString(R.string.live_delay_immediate)
                : getContext().getString(R.string.live_delay_saved, committed)); updateAvailability();
    }
    private void updateAvailability() {
        input.setEnabled(available); apply.setEnabled(available);
        minus.setEnabled(available && (dirty || committed > LiveSkipPolicy.MIN_SECONDS));
        plus.setEnabled(available && (dirty || committed < LiveSkipPolicy.MAX_SECONDS));
    }
}
