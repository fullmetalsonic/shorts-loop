package com.fullmetalsonic.shortsloop.audioprobe.capture;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.projection.MediaProjection;
import android.os.PowerManager;
import android.os.SystemClock;

import com.fullmetalsonic.shortsloop.audioprobe.core.SignalMeter;
import com.fullmetalsonic.shortsloop.audioprobe.core.AudioPatternAnalyzer;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/** Sole owner of AudioRecord and its PCM buffer; bounded nonblocking reads. */
final class CaptureSession implements Runnable {
    static final int SAMPLE_RATE = 16000;
    static final long LIMIT_MS = 60_000;
    private static final long MAX_ANALYSIS_WALL_MS = 150;
    private final MediaProjection projection;
    private final int uid;
    private final KeyguardManager keyguard;
    private final PowerManager power;
    private final Runnable completed;
    private final AtomicReference<String> stopReason = new AtomicReference<>();

    CaptureSession(Context context, MediaProjection projection, int uid, Runnable completed) {
        this.projection = projection;
        this.uid = uid;
        this.completed = completed;
        keyguard = context.getSystemService(KeyguardManager.class);
        power = context.getSystemService(PowerManager.class);
    }

    void stop(String reason) { stopReason.compareAndSet(null, reason); }

    @Override @SuppressLint("MissingPermission") // Checked by activity AND service before construction.
    public void run() {
        AudioRecord record = null;
        short[] pcm = new short[1600];
        SignalMeter meter = new SignalMeter();
        AudioPatternAnalyzer analyzer = new AudioPatternAnalyzer();
        long analysisCpuMs = 0;
        long maxAnalysisWallMs = 0;
        long lastPositiveReadMs = 0;
        boolean noDataReset = false;
        long started = SystemClock.elapsedRealtime();
        try {
            if (stopReason.get() != null) return;
            AudioPlaybackCaptureConfiguration config = new AudioPlaybackCaptureConfiguration.Builder(projection)
                    .addMatchingUid(uid)
                    .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                    .build();
            int minimum = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            if (minimum <= 0) throw new IllegalStateException("Unsupported PCM format");
            record = new AudioRecord.Builder()
                    .setAudioPlaybackCaptureConfig(config)
                    .setAudioFormat(new AudioFormat.Builder().setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT).build())
                    .setBufferSizeInBytes(Math.max(minimum, pcm.length * 4))
                    .build();
            if (record.getState() != AudioRecord.STATE_INITIALIZED) throw new IllegalStateException("Not initialized");
            record.startRecording();
            while (stopReason.get() == null) {
                long elapsed = SystemClock.elapsedRealtime() - started;
                if (elapsed >= LIMIT_MS) { stop("60초 자동 종료"); break; }
                if (!power.isInteractive() || keyguard.isDeviceLocked()) { stop("화면 잠금으로 종료"); break; }
                int count = record.read(pcm, 0, pcm.length, AudioRecord.READ_NON_BLOCKING);
                if (count < 0) { stop("오디오 읽기 오류 " + count); break; }
                if (count > 0) {
                    lastPositiveReadMs = elapsed;
                    noDataReset = false;
                    meter.accept(pcm, count);
                    long cpuStart = SystemClock.currentThreadTimeMillis();
                    long wallStart = SystemClock.elapsedRealtime();
                    analyzer.accept(pcm, count, elapsed);
                    analysisCpuMs += SystemClock.currentThreadTimeMillis() - cpuStart;
                    maxAnalysisWallMs = Math.max(maxAnalysisWallMs, SystemClock.elapsedRealtime() - wallStart);
                    if (maxAnalysisWallMs > MAX_ANALYSIS_WALL_MS) {
                        analyzer.reset("ANALYSIS_TOO_SLOW");
                        stop("분석 처리 지연으로 종료");
                        break;
                    }
                } else if (!noDataReset && elapsed - lastPositiveReadMs >= 500) {
                    analyzer.reset("NO_AUDIO_DATA");
                    noDataReset = true;
                }
                Arrays.fill(pcm, (short) 0);
                ProbeState.current = new ProbeState.Snapshot(true, "Instagram 음향 반복 분석 중", elapsed,
                        meter.reading(), analyzer.snapshot(), analysisCpuMs, maxAnalysisWallMs, analyzer.diagnostics());
                SystemClock.sleep(20);
            }
        } catch (RuntimeException e) {
            // Exception message can contain framework identifiers; retain only its class.
            stop("캡처 실패 · " + e.getClass().getSimpleName());
        } finally {
            Arrays.fill(pcm, (short) 0);
            if (record != null) {
                try { record.stop(); } catch (RuntimeException ignored) { /* May not have started. */ }
                try { record.release(); } catch (RuntimeException ignored) { /* Still clear all analysis memory. */ }
            }
            AudioPatternAnalyzer.Snapshot finalPattern = analyzer.snapshot();
            AudioPatternAnalyzer.Diagnostics finalDiagnostics = analyzer.diagnostics();
            analyzer.clear();
            if (stopReason.get() == null) stop("시험 종료");
            ProbeState.current = new ProbeState.Snapshot(false, stopReason.get(),
                    SystemClock.elapsedRealtime() - started, meter.reading(), finalPattern, analysisCpuMs, maxAnalysisWallMs, finalDiagnostics);
            completed.run();
        }
    }
}
