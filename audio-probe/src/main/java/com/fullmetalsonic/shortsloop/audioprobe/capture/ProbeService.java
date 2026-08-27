package com.fullmetalsonic.shortsloop.audioprobe.capture;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.fullmetalsonic.shortsloop.audioprobe.ui.ProbeActivity;

import java.io.FileDescriptor;
import java.io.PrintWriter;

public final class ProbeService extends Service {
    public static final String START = "audio.probe.START";
    public static final String STOP = "audio.probe.STOP";
    public static final String CONSENT = "consent";
    private final Handler main = new Handler(Looper.getMainLooper());
    private CaptureSession session;
    private MediaProjection projection;
    private final MediaProjection.Callback callback = new MediaProjection.Callback() {
        @Override public void onStop() { if (session != null) session.stop("Android 캡처 승인 종료"); }
    };

    @Override public IBinder onBind(Intent intent) { return null; }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || !START.equals(intent.getAction())) {
            if (session != null) session.stop("사용자 중지");
            else stopSelf();
            return START_NOT_STICKY;
        }
        if (session != null || ProbeState.current.running()) return START_NOT_STICKY;
        try {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                throw new SecurityException("Audio permission required");
            Intent consent = intent.getParcelableExtra(CONSENT);
            intent.removeExtra(CONSENT);
            if (consent == null) throw new SecurityException("New consent required");
            int uid = getPackageManager().getApplicationInfo("com.instagram.android", 0).uid;
            getSystemService(NotificationManager.class).createNotificationChannel(new NotificationChannel(
                    "audio_probe", "내부 오디오 시험", NotificationManager.IMPORTANCE_LOW));
            PendingIntent open = PendingIntent.getActivity(this, 0, new Intent(this, ProbeActivity.class), PendingIntent.FLAG_IMMUTABLE);
            PendingIntent stop = PendingIntent.getService(this, 1,
                    new Intent(this, ProbeService.class).setAction(STOP), PendingIntent.FLAG_IMMUTABLE);
            Notification notification = new Notification.Builder(this, "audio_probe")
                    .setSmallIcon(android.R.drawable.ic_media_play)
                    .setContentTitle("Instagram 내부 오디오 시험")
                    .setContentText("최대 60초 · 저장 없음 · 눌러 결과 확인")
                    .setContentIntent(open).setOngoing(true)
                    .addAction(new Notification.Action.Builder(null, "시험 중지", stop).build()).build();
            startForeground(41, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
            projection = getSystemService(MediaProjectionManager.class).getMediaProjection(Activity.RESULT_OK, consent);
            projection.registerCallback(callback, main);
            ProbeState.Snapshot idle = ProbeState.idle("캡처 준비 중");
            ProbeState.current = new ProbeState.Snapshot(true, idle.reason(), 0, idle.meter(), idle.pattern(), 0, 0, idle.diagnostics());
            session = new CaptureSession(this, projection, uid, () -> main.post(this::finishSession));
            new Thread(session, "instagram-audio-probe").start();
        } catch (RuntimeException | PackageManager.NameNotFoundException e) {
            ProbeState.current = ProbeState.idle("시작 실패 · " + e.getClass().getSimpleName());
            finishSession();
        }
        return START_NOT_STICKY;
    }

    private void finishSession() {
        session = null;
        releaseProjection();
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    private void releaseProjection() {
        if (projection == null) return;
        projection.unregisterCallback(callback);
        projection.stop();
        projection = null;
    }

    @Override public void onDestroy() {
        if (session != null) session.stop("서비스 종료");
        releaseProjection();
        super.onDestroy();
    }

    @Override protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        ProbeState.dump(writer);
    }
}
