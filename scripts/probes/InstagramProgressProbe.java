package probes;

import android.os.SystemClock;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.uiautomator.core.Configurator;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

/** Read-only ADB research probe; never run while ShortsLoop automation is enabled. */
public final class InstagramProgressProbe extends UiAutomatorTestCase {
    private static final class Scrubber extends UiObject {
        Scrubber() {
            super(new UiSelector().packageName("com.instagram.android")
                    .resourceId("com.instagram.android:id/scrubber"));
        }
        AccessibilityNodeInfo snapshot() { return findAccessibilityNodeInfo(0); }
    }

    public void testProgress() {
        Configurator config = Configurator.getInstance();
        long oldIdle = config.getWaitForIdleTimeout();
        long oldSelector = config.getWaitForSelectorTimeout();
        config.setWaitForIdleTimeout(0).setWaitForSelectorTimeout(0);
        long start = SystemClock.elapsedRealtime();
        long seconds = Math.max(1, Math.min(120,
                Long.parseLong(getParams().getString("seconds", "45"))));
        AccessibilityNodeInfo node = null;
        try {
            Scrubber scrubber = new Scrubber();
            while (SystemClock.elapsedRealtime() - start < seconds * 1000) {
                if (node == null) node = scrubber.snapshot();
                long elapsed = SystemClock.elapsedRealtime() - start;
                if (node == null) {
                    System.out.println("IG_PROGRESS t=" + elapsed + " node=missing");
                } else {
                        boolean fresh = node.refresh();
                        AccessibilityNodeInfo.RangeInfo range = node.getRangeInfo();
                        // Only numeric progress/booleans: no title, account, description or text.
                        System.out.println("IG_PROGRESS t=" + elapsed + " fresh=" + fresh
                                + " visible=" + node.isVisibleToUser()
                                + (range == null ? " range=missing"
                                : " min=" + range.getMin() + " max=" + range.getMax()
                                + " current=" + range.getCurrent() + " type=" + range.getType()));
                        if (!fresh) { node.recycle(); node = null; }
                }
                SystemClock.sleep(400);
            }
        } finally {
            if (node != null) node.recycle();
            config.setWaitForIdleTimeout(oldIdle).setWaitForSelectorTimeout(oldSelector);
        }
    }
}
