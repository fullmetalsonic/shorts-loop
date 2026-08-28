package com.fullmetalsonic.shortsloop.visual;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import com.fullmetalsonic.shortsloop.core.VisualLoopTracker;
import com.fullmetalsonic.shortsloop.detection.YouTubeSnapshot;
import java.util.Objects;

/** API26-safe facade. Old Android never constructs the screenshot implementation. */
public abstract class VisualAssistController {
    public interface Host {
        boolean stillEligible(YouTubeSnapshot expected);
        void result(YouTubeSnapshot expected, VisualLoopTracker.Result result);
    }
    public static VisualAssistController create(AccessibilityService service, Host host) {
        if (Build.VERSION.SDK_INT >= 34) return new Api34VisualAssistController(service, host);
        return new Unavailable();
    }
    public static boolean samePage(YouTubeSnapshot a, YouTubeSnapshot b) {
        return a != null && b != null && a.visualCandidate && b.visualCandidate && !a.ad && !b.ad
                && a.recognized() && b.recognized() && a.windowId >= 0 && a.windowId == b.windowId
                && Objects.equals(a.identity, b.identity) && Objects.equals(a.page, b.page)
                && a.windowBounds != null && a.windowBounds.equals(b.windowBounds);
    }
    public abstract void reset();
    public abstract boolean active();
    public abstract int current();
    public abstract String status();
    public abstract String diagnostic();
    public abstract void observe(YouTubeSnapshot snapshot, int count, long now);
    private static final class Unavailable extends VisualAssistController {
        @Override public void reset() { }
        @Override public boolean active() { return false; }
        @Override public int current() { return 0; }
        @Override public String status() { return "화면 분석 미지원 · 시간제 넘김을 사용해 주세요"; }
        @Override public String diagnostic() { return "visual=false frames=0 errors=0 supported=false"; }
        @Override public void observe(YouTubeSnapshot snapshot, int count, long now) { }
    }
}
