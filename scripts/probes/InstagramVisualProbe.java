package probes;

import android.app.UiAutomation;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Bounded shell research: screenshots/features in RAM only; no input or product changes. */
public final class InstagramVisualProbe {
    private static final String PACKAGE = "com.instagram.android";
    public static void main(String[] args) throws Exception {
        int seconds = args.length == 0 ? 35 : Math.max(8, Math.min(90, Integer.parseInt(args[0])));
        Class<?> wrapperType = Class.forName("com.android.uiautomator.core.UiAutomationShellWrapper");
        Object wrapper = wrapperType.getConstructor().newInstance();
        wrapperType.getMethod("connect").invoke(wrapper);
        List<Double> times = new ArrayList<>();
        List<double[]> frames = new ArrayList<>();
        try {
            UiAutomation automation = (UiAutomation) wrapperType.getMethod("getUiAutomation").invoke(wrapper);
            AccessibilityServiceInfo info = automation.getServiceInfo();
            info.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
            automation.setServiceInfo(info);
            long start = SystemClock.elapsedRealtime();
            Rect initial = null;
            while (SystemClock.elapsedRealtime() - start < seconds * 1000 && frames.size() < 360) {
                long sampleStart = SystemClock.elapsedRealtime();
                AccessibilityNodeInfo root = automation.getRootInActiveWindow();
                if (root == null) throw new IllegalStateException("No active root; capture stopped");
                Rect video = null;
                try {
                    if (!PACKAGE.contentEquals(root.getPackageName())) throw new IllegalStateException("Not Instagram");
                    List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(PACKAGE + ":id/clips_video_container");
                    try {
                        for (AccessibilityNodeInfo node : nodes) if (node.isVisibleToUser()) {
                            if (video != null) throw new IllegalStateException("Multiple video containers");
                            video = new Rect(); node.getBoundsInScreen(video);
                        }
                    } finally { for (AccessibilityNodeInfo node : nodes) node.recycle(); }
                } finally { root.recycle(); }
                if (video == null || video.width() < 200 || video.height() < 300)
                    throw new IllegalStateException("Unsupported video geometry");
                if (initial == null) initial = new Rect(video);
                if (!initial.equals(video)) throw new IllegalStateException("Video window changed");
                Bitmap bitmap = automation.takeScreenshot();
                if (bitmap == null) throw new IllegalStateException("Screenshot unavailable");
                double[] features = new double[16 * 24 * 3];
                try {
                    if (video.left < 0 || video.top < 0 || video.right > bitmap.getWidth() || video.bottom > bitmap.getHeight())
                        throw new IllegalStateException("Video outside screenshot");
                    int k = 0;
                    for (int y = 0; y < 24; y++) for (int x = 0; x < 16; x++) {
                        int px = video.left + (int) ((.1 + (x + .5) / 16 * .65) * video.width());
                        int py = video.top + (int) ((.18 + (y + .5) / 24 * .57) * video.height());
                        int rgb = bitmap.getPixel(px, py);
                        features[k++] = (rgb >> 16) & 255;
                        features[k++] = (rgb >> 8) & 255;
                        features[k++] = rgb & 255;
                    }
                } finally { bitmap.recycle(); }
                double time = (sampleStart + SystemClock.elapsedRealtime() - 2.0 * start) / 2000;
                double motion = frames.isEmpty() ? 0 : VisualCycleMath.distance(features, frames.get(frames.size() - 1));
                times.add(time); frames.add(features);
                System.out.printf(Locale.ROOT, "VIS_FRAME n=%d t=%.3f cost=%d motion=%.3f%n", frames.size(), time,
                        SystemClock.elapsedRealtime() - sampleStart, motion);
                long wait = 250 - (SystemClock.elapsedRealtime() - sampleStart);
                if (wait > 0) SystemClock.sleep(wait);
            }
            System.out.printf(Locale.ROOT, "VIS_SUMMARY frames=%d motion=%.3f%n", frames.size(), VisualCycleMath.motion(frames));
            for (VisualCycleMath.Candidate candidate : VisualCycleMath.search(times, frames)) {
                System.out.printf(Locale.ROOT, "VIS_CANDIDATE period=%.3f error=%.3f pairs=%d%n",
                        candidate.seconds, candidate.error, candidate.pairs);
            }
            System.out.println("VIS_RESEARCH_ONLY no_gesture=true no_files=true no_verified_loop=true");
        } finally {
            frames.clear(); times.clear();
            wrapperType.getMethod("disconnect").invoke(wrapper);
        }
    }
}
