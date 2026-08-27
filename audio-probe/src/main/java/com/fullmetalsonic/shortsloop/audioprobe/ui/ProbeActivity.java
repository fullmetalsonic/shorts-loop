package com.fullmetalsonic.shortsloop.audioprobe.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.projection.MediaProjectionManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fullmetalsonic.shortsloop.audioprobe.capture.ProbeService;
import com.fullmetalsonic.shortsloop.audioprobe.capture.ProbeState;
import com.fullmetalsonic.shortsloop.audioprobe.core.SignalMeter;
import com.fullmetalsonic.shortsloop.audioprobe.core.AudioPatternAnalyzer;
import com.fullmetalsonic.shortsloop.audioprobe.BuildConfig;

import java.util.Locale;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public final class ProbeActivity extends Activity {
    private final Handler main = new Handler(Looper.getMainLooper());
    private TextView status;
    private TextView notice;
    private TextView patternStatus;
    private TextView volumeStatus;
    private Button start;
    private Button stop;
    private boolean requesting;
    private final Runnable refresh = new Runnable() {
        @Override public void run() { render(); main.postDelayed(this, 300); }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        requesting = state != null && state.getBoolean("requesting");
        ScrollView scroll = new ScrollView(this);
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(24), dp(20), dp(24), dp(24));
        scroll.addView(column);
        if (android.os.Build.VERSION.SDK_INT >= 30) scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        else scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        column.addView(text("쇼츠 오디오 시험", 26));
        column.addView(text(BuildConfig.VERSION_NAME + " · 음향 반복 후보 시험", 15));
        column.addView(text("인스타그램 내부 재생음만 60초간 분석합니다. 마이크 입력·소리 저장·전송·자동 넘김은 하지 않습니다.", 17));
        column.addView(text("먼저 Instagram 자체 음소거를 해제하세요. 같은 릴스를 유지하도록 기존 자동 넘김도 잠시 꺼 주세요.\n① 시험 시작 → 캡처 승인\n② Instagram에서 같은 릴스 재생\n③ 60초 뒤 이 앱에서 결과 확인", 16));
        volumeStatus = text("미디어 음량 확인 중", 15);
        column.addView(volumeStatus);
        patternStatus = text("반복 후보 없음", 18);
        column.addView(patternStatus);
        start = button("60초 시험 시작", column, view -> requestStart());
        button("Instagram 열기", column, view -> {
            Intent launch = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
            if (launch != null) startActivity(launch);
            else notice.setText("현재 프로필에서 Instagram을 찾을 수 없습니다.");
        });
        stop = button("시험 중지", column, view -> startService(new Intent(this, ProbeService.class).setAction(ProbeService.STOP)));
        notice = text("매번 직접 시작해야 하며, 화면 잠금·승인 취소·60초 경과 시 종료됩니다.", 15);
        column.addView(notice);
        column.addView(text("3~25초 길이의 음향 반복만 시험합니다. 최소 두 주기와 추가 확인 시간이 필요합니다. 후렴이 반복되어도 영상 끝은 아닐 수 있습니다. 25초보다 긴 영상이나 무음에서는 후보가 없을 수 있습니다.", 15));
        column.addView(text("Android의 ‘마이크/오디오 녹음’ 권한은 내부 소리 수신에도 필요합니다. 실제 마이크 입력이나 화면 영상은 수집하지 않습니다. 주파수 특징은 RAM에서만 비교한 뒤 시험 종료 시 지웁니다.", 15));
        status = text("시작 전", 18);
        column.addView(status);
        column.addView(text("‘신호 감지’는 소리를 받았다는 뜻일 뿐, 영상 반복·끝 인식 성공은 아닙니다. 무음 결과만으로 캡처 차단을 단정할 수 없습니다. 결과 수치도 앱 프로세스가 종료되면 사라집니다.", 15));
        setContentView(scroll);
    }

    private void requestStart() {
        if (requesting || ProbeState.current.running()) return;
        requesting = true;
        render();
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 7);
        else requestProjection();
    }

    private void requestProjection() {
        try {
            startActivityForResult(getSystemService(MediaProjectionManager.class).createScreenCaptureIntent(), 8);
        } catch (RuntimeException e) {
            requesting = false;
            notice.setText("캡처 승인창을 열 수 없습니다. 설정 또는 기기 정책을 확인하세요.");
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode != 7) return;
        if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) requestProjection();
        else { requesting = false; notice.setText("오디오 권한이 없어 시작하지 않았습니다. 원하면 다시 시작하거나 앱 설정에서 허용하세요."); }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 8) return;
        requesting = false;
        if (resultCode != RESULT_OK || data == null) { notice.setText("캡처 승인을 취소했습니다. 수집하지 않았습니다."); return; }
        try {
            startForegroundService(new Intent(this, ProbeService.class).setAction(ProbeService.START).putExtra(ProbeService.CONSENT, data));
            notice.setText("시작 요청 완료. ‘Instagram 열기’로 소리 있는 릴스를 재생하세요.");
        } catch (RuntimeException e) { notice.setText("시험 서비스를 시작하지 못했습니다. 다시 시도하세요."); }
    }

    private void render() {
        ProbeState.Snapshot s = ProbeState.current;
        SignalMeter.Reading m = s.meter();
        AudioPatternAnalyzer.Snapshot p = s.pattern();
        AudioManager audio = getSystemService(AudioManager.class);
        int volume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumeStatus.setText(String.format(Locale.KOREA, "휴대폰 미디어 음량 %d / %d%s", volume,
                audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC), volume == 0 ? " · 소리 재생을 확인하세요" : " · Instagram 자체 음소거도 확인하세요"));
        String candidate = p.periodSeconds() > 0 ? String.format(Locale.KOREA, "음향 반복 후보 약 %.2f초", p.periodSeconds())
                : "음향 반복 후보 없음";
        patternStatus.setText(String.format(Locale.KOREA,
                "%s\n%s · 특징 %d개\n패턴 유사도 %.2f · 영상 끝 미확정",
                candidate, s.running() ? "분석 중" : "현재 시험 결과", p.featureFrames(), p.confidence()));
        start.setEnabled(!requesting && !s.running());
        stop.setEnabled(s.running());
        String result = m.samples() == 0 ? "아직 수신 표본 없음"
                : m.signalDetected() ? "내부 오디오 신호 감지 (반복 미판정)" : "뚜렷한 오디오 신호 미검출";
        AudioPatternAnalyzer.Diagnostics d = s.diagnostics();
        status.setText(String.format(Locale.KOREA,
                "%s\n%s\n\n경과 %.1f / 60초\n수신 표본 %,d개\n신호 구간 %,d / %,d\n현재 크기 %.1f dBFS\n최대 구간 %.1f dBFS\n0이 아닌 표본 %.1f%%\n최대 진폭 %d\n분석 CPU %dms / 최장 처리 %dms\n진단 %s · %s",
                s.reason(), result, s.elapsedMs() / 1000.0, m.samples(), m.signalBlocks(), m.blocks(),
                m.latestDbfs(), m.maxDbfs(), m.nonZeroPercent(), m.peak(), s.analysisCpuMs(), s.maxAnalysisWallMs(), p.state(), p.reason()) + String.format(Locale.KOREA,
                "\n\n분석 가능한 구간 %d / %d\n소리 작음 %d · 주파수 폭 부족 %d\n최근 2초 유효 구간 %d / 20\n마지막 탈락 기록 %s\n※ 현재 판정과 별개인 과거 기록입니다.\n신호 수신과 반복 판정은 서로 다른 검사입니다.",
                d.valid(), d.frames(), d.lowRms(), d.narrow(), d.recent20Good(), d.lastReject()));
    }

    @Override protected void onResume() { super.onResume(); main.post(refresh); }
    @Override protected void onPause() { main.removeCallbacks(refresh); super.onPause(); }
    @Override protected void onSaveInstanceState(Bundle out) { out.putBoolean("requesting", requesting); super.onSaveInstanceState(out); }

    @Override public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        ProbeState.dump(writer);
    }

    private TextView text(String value, int sp) {
        TextView view = new TextView(this);
        view.setText(value); view.setTextSize(sp); view.setTextColor(Color.rgb(233, 240, 247));
        view.setPadding(0, dp(10), 0, dp(10));
        return view;
    }
    private Button button(String title, LinearLayout column, View.OnClickListener action) {
        Button view = new Button(this);
        view.setText(title); view.setAllCaps(false); view.setMinHeight(dp(52)); view.setOnClickListener(action);
        column.addView(view, new LinearLayout.LayoutParams(-1, -2));
        return view;
    }
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
}
