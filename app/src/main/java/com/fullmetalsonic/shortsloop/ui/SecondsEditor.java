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
import com.fullmetalsonic.shortsloop.core.ClocklessTimeoutPolicy;

/** Independent timeout editor. Draft text does not change playback settings. */
@android.annotation.SuppressLint("ViewConstructor")
public final class SecondsEditor extends LinearLayout {
    public interface Listener { void changed(int seconds); }
    private final EditText input;
    private final TextView detail;
    private final Button apply, plus, minus;
    private final Listener listener;
    private boolean binding, dirty;
    private boolean available = true;
    private int committed;

    public SecondsEditor(Context context, int initial, Listener listener) {
        super(context); this.listener = listener; setOrientation(VERTICAL);
        addView(UiTheme.text(context, "재생 정보가 없을 때 대기 시간 (초)", 14, UiTheme.TEXT, true));
        LinearLayout row = new LinearLayout(context); row.setGravity(Gravity.CENTER_VERTICAL);
        minus = UiTheme.button(context, "−"); minus.setId(R.id.fallback_seconds_minus);
        minus.setContentDescription("대기 시간 1초 줄이기"); minus.setFocusable(false);
        row.addView(minus, new LinearLayout.LayoutParams(UiTheme.dp(context, 52), UiTheme.dp(context, 64)));
        input = new EditText(context); input.setId(R.id.fallback_seconds_input); input.setSingleLine(true);
        // Preserve pasted signs/decimals for validation; request only a numeric IME.
        input.setKeyListener(TextKeyListener.getInstance()); input.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE); input.setSelectAllOnFocus(true);
        input.setContentDescription("재생 정보 없는 영상의 대기 시간, 5에서 60초");
        input.setTextSize(30); input.setTextColor(UiTheme.TEXT); input.setGravity(Gravity.CENTER);
        input.setBackground(UiTheme.surface(context, UiTheme.BACKGROUND, 14, true));
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(0, UiTheme.dp(context, 64), 1);
        inputParams.leftMargin = UiTheme.dp(context, 8); inputParams.rightMargin = UiTheme.dp(context, 8);
        row.addView(input, inputParams);
        plus = UiTheme.button(context, "+"); plus.setId(R.id.fallback_seconds_plus);
        plus.setContentDescription("대기 시간 1초 늘리기"); plus.setFocusable(false);
        row.addView(plus, new LinearLayout.LayoutParams(UiTheme.dp(context, 52), UiTheme.dp(context, 64)));
        addView(row);
        detail = UiTheme.text(context, "", 13, UiTheme.MUTED, false);
        detail.setPadding(0, UiTheme.dp(context, 6), 0, 0); addView(detail);
        apply = UiTheme.button(context, "입력한 시간 적용"); apply.setId(R.id.fallback_seconds_apply);
        addView(apply, new LinearLayout.LayoutParams(-1, -2)); bind(initial);
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence text, int start, int before, int count) {
                if (binding) return;
                dirty = true; input.setError(null); apply.setVisibility(VISIBLE); updateAvailability();
                detail.setText("입력 후 키보드의 완료 또는 적용을 눌러 주세요.");
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

    public void setAvailable(boolean value) { available = value; updateAvailability(); }
    public void render(int seconds) { if (!dirty && committed != seconds) bind(seconds); }

    public boolean commit() {
        if (!dirty) return true;
        Integer seconds = ClocklessTimeoutPolicy.parseSeconds(input.getText());
        if (seconds == null) { invalid(); return false; }
        save(seconds); input.clearFocus();
        android.view.inputmethod.InputMethodManager keyboard = getContext().getSystemService(android.view.inputmethod.InputMethodManager.class);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(input.getWindowToken(), 0);
        return true;
    }

    private void step(int delta) {
        Integer seconds = ClocklessTimeoutPolicy.parseSeconds(input.getText());
        if (seconds == null) { invalid(); return; }
        save(Math.max(ClocklessTimeoutPolicy.MIN_SECONDS, Math.min(ClocklessTimeoutPolicy.MAX_SECONDS, seconds + delta)));
    }
    private void invalid() { input.setError("5~60 사이의 정수 초를 입력해 주세요"); input.requestFocus(); }
    private void save(int seconds) { dirty = false; bind(seconds); listener.changed(seconds); }
    private void bind(int seconds) {
        committed = seconds; binding = true;
        input.setText(String.format(java.util.Locale.ROOT, "%d", seconds)); binding = false;
        apply.setVisibility(GONE); detail.setText("기본 10초 · 5~60초 · +/−는 1초씩 적용"); updateAvailability();
    }
    private void updateAvailability() {
        input.setEnabled(available); apply.setEnabled(available);
        minus.setEnabled(available && (dirty || committed > ClocklessTimeoutPolicy.MIN_SECONDS));
        plus.setEnabled(available && (dirty || committed < ClocklessTimeoutPolicy.MAX_SECONDS));
    }
}
