package com.fullmetalsonic.shortsloop.audioprobe.capture;

import com.fullmetalsonic.shortsloop.audioprobe.core.SignalMeter;
import com.fullmetalsonic.shortsloop.audioprobe.core.AudioPatternAnalyzer;
import com.fullmetalsonic.shortsloop.audioprobe.BuildConfig;

import java.io.PrintWriter;
import java.util.Locale;

/** Process-local status. No persistence and no sample/feature arrays. */
public final class ProbeState {
    private ProbeState() {}
    public static volatile Snapshot current = idle("시작 전");
    public record Snapshot(boolean running, String reason, long elapsedMs, SignalMeter.Reading meter,
                           AudioPatternAnalyzer.Snapshot pattern, long analysisCpuMs, long maxAnalysisWallMs,
                           AudioPatternAnalyzer.Diagnostics diagnostics) {}

    static Snapshot idle(String reason) {
        return new Snapshot(false, reason, 0, new SignalMeter().reading(),
                new AudioPatternAnalyzer.Snapshot("WAITING", 0, 0, 0, 0, 0, "NOT_STARTED"), 0, 0,
                AudioPatternAnalyzer.Diagnostics.empty());
    }

    public static void dump(PrintWriter writer) {
        Snapshot s = current;
        SignalMeter.Reading m = s.meter();
        AudioPatternAnalyzer.Snapshot p = s.pattern();
        writer.printf(Locale.ROOT,
                "audioProbe version=%s running=%s elapsedMs=%d samples=%d blocks=%d signalBlocks=%d latestDbfs=%.2f maxDbfs=%.2f nonZeroPct=%.2f peak=%d reason=%s%n",
                BuildConfig.VERSION_NAME, s.running(), s.elapsedMs(), m.samples(), m.blocks(), m.signalBlocks(),
                m.latestDbfs(), m.maxDbfs(), m.nonZeroPercent(), m.peak(), s.reason());
        writer.printf(Locale.ROOT,
                "audioPattern state=%s periodSeconds=%.3f similarity=%.4f featureFrames=%d confirmations=%d resets=%d analysisCpuMs=%d maxAnalysisWallMs=%d reason=%s%n",
                p.state(), p.periodSeconds(), p.confidence(), p.featureFrames(), p.confirmations(), p.resets(),
                s.analysisCpuMs(), s.maxAnalysisWallMs(), p.reason());
        AudioPatternAnalyzer.Diagnostics d = s.diagnostics();
        writer.printf(Locale.ROOT,
                "audioPatternDiag frames=%d valid=%d lowRms=%d narrow=%d recent20Good=%d searchAttempts=%d recentQualityRejected=%d noPeaks=%d assessmentRejected=%d ambiguous=%d shortRepeat=%d gapResets=%d lastReject=%s%n",
                d.frames(), d.valid(), d.lowRms(), d.narrow(), d.recent20Good(), d.searchAttempts(),
                d.recentQualityRejected(), d.noPeaks(), d.assessmentRejected(), d.ambiguous(),
                d.shortRepeat(), d.gapResets(), d.lastReject());
    }
}
