package com.fullmetalsonic.shortsloop.ui;

import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import com.fullmetalsonic.shortsloop.BuildConfig;
import com.fullmetalsonic.shortsloop.R;

/** An unobtrusive banner is bound separately; this card contains the infrequent update actions. */
public final class UpdatePanel extends LinearLayout {
    public final TextView status;
    public final Button check, action, cancel;
    public final ProgressBar progress;
    public final Switch automatic;
    public UpdatePanel(Context context) {
        super(context); setOrientation(VERTICAL);
        addView(UiTheme.text(context, "설치 버전 " + BuildConfig.VERSION_NAME + " · 공개 시험판", 14, UiTheme.MUTED, false));
        status = UiTheme.text(context, "새 버전을 확인할 수 있습니다.", 14, UiTheme.CYAN, false); status.setId(R.id.update_status); addView(status);
        progress = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal); progress.setMax(100); progress.setVisibility(GONE); addView(progress);
        check = UiTheme.button(context, "업데이트 확인"); check.setId(R.id.update_check); addView(check);
        action = UiTheme.button(context, "업데이트 다운로드"); action.setId(R.id.update_action); action.setVisibility(GONE); addView(action);
        cancel = UiTheme.button(context, "다운로드 취소"); cancel.setId(R.id.update_cancel); cancel.setVisibility(GONE); addView(cancel);
        automatic = new Switch(context); automatic.setText(R.string.update_auto_title); automatic.setTextColor(UiTheme.TEXT); automatic.setTextSize(15);
        automatic.setId(R.id.update_automatic); automatic.setMinHeight(UiTheme.dp(context, 52)); addView(automatic);
        addView(UiTheme.text(context, "하루 한 번 GitHub에 확인 · 영상·계정·시청 기록은 전송하지 않습니다. 다운로드와 설치는 직접 선택합니다. 설치 전 전체 실행은 중지됩니다.", 13, UiTheme.MUTED, false));
    }
}
