package probes;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.UiAutomation;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityRecord;
import java.util.concurrent.atomic.AtomicInteger;

/** Shell-only, bounded numeric event survey. No gestures, text output or product changes. */
public final class InstagramEventProbe {
    private static final String PACKAGE = "com.instagram.android";

    public static void main(String[] args) throws Exception {
        int seconds = args.length == 0 ? 15 : Math.max(2, Math.min(45, Integer.parseInt(args[0])));
        // Use the same platform shell wrapper as AOSP's uiautomator events command.
        Class<?> wrapperType = Class.forName("com.android.uiautomator.core.UiAutomationShellWrapper");
        Object wrapper = wrapperType.getConstructor().newInstance();
        wrapperType.getMethod("connect").invoke(wrapper);
        UiAutomation automation = null;
        AtomicInteger events = new AtomicInteger();
        AtomicInteger numeric = new AtomicInteger();
        try {
            automation = (UiAutomation) wrapperType.getMethod("getUiAutomation").invoke(wrapper);
            AccessibilityServiceInfo info = automation.getServiceInfo();
            info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
            info.packageNames = new String[] { PACKAGE };
            info.notificationTimeout = 0;
            info.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
                    | AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
            automation.setServiceInfo(info);
            long start = SystemClock.elapsedRealtime();
            automation.setOnAccessibilityEventListener(event -> {
                if (!PACKAGE.contentEquals(event.getPackageName() == null ? "" : event.getPackageName())) return;
                int count = events.incrementAndGet();
                if (count > 1500) return;
                if (event.getItemCount() > 0 || event.getCurrentItemIndex() >= 0) numeric.incrementAndGet();
                String id = "none";
                AccessibilityNodeInfo node = event.getSource();
                if (node != null) {
                    try {
                        String value = node.getViewIdResourceName();
                        if (value != null && value.matches("com\\.instagram\\.android:id/[A-Za-z0-9_]+")) id = value;
                    } finally { node.recycle(); }
                }
                System.out.println("IG_EVENT t=" + (SystemClock.elapsedRealtime() - start)
                        + " type=" + event.getEventType() + " change=" + event.getContentChangeTypes()
                        + " id=" + id + " records=" + event.getRecordCount() + fields(event));
                for (int i = 0; i < Math.min(8, event.getRecordCount()); i++) {
                    System.out.println("IG_RECORD index=" + i + fields(event.getRecord(i)));
                }
            });
            System.out.println("IG_EVENT_READY seconds=" + seconds + " allTypes=true");
            for (int i = 0; i < seconds; i++) {
                SystemClock.sleep(1000);
                System.out.println("IG_EVENT_HEARTBEAT second=" + (i + 1) + " count=" + events.get());
            }
            System.out.println("IG_EVENT_SUMMARY total=" + events.get() + " numeric=" + numeric.get()
                    + " truncated=" + (events.get() > 1500));
        } finally {
            if (automation != null) automation.setOnAccessibilityEventListener(null);
            wrapperType.getMethod("disconnect").invoke(wrapper);
        }
    }

    private static String fields(AccessibilityRecord event) {
        return " count=" + event.getItemCount() + " current=" + event.getCurrentItemIndex()
                + " from=" + event.getFromIndex() + " to=" + event.getToIndex()
                + " scrollX=" + event.getScrollX() + " scrollY=" + event.getScrollY()
                + " maxX=" + event.getMaxScrollX() + " maxY=" + event.getMaxScrollY();
    }
}
