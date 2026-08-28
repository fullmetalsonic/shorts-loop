package probes;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.UiAutomation;
import android.graphics.Rect;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/** Shell-only screen survey. No input, APK installation, permissions, or free-form text output.
 * Uses non-suppressing automation only; never falls back to the legacy suppressing wrapper.
 * Framework reflection is for this developer probe, not a product API.
 */
public final class TikTokReadOnlyProbe {
    private static final String PACKAGE = "com.ss.android.ugc.trill";
    private static int nodes, ranges, clocks, truncated;
    private static boolean tree;

    public static void main(String[] args) throws Exception {
        Thread deadline = new Thread(() -> {
            SystemClock.sleep(25000);
            System.out.println("TT_PROBE_TIMEOUT");
            System.exit(2);
        }, "probe-deadline");
        deadline.setDaemon(true);
        deadline.start();
        HandlerThread thread = new HandlerThread("tiktok-read-only");
        thread.start();
        UiAutomation automation = null;
        boolean connected = false;
        try {
            Class<?> connectionInterface = Class.forName("android.app.IUiAutomationConnection");
            Object connection = Class.forName("android.app.UiAutomationConnection").getConstructor().newInstance();
            automation = (UiAutomation) UiAutomation.class.getConstructor(Looper.class, connectionInterface)
                    .newInstance(thread.getLooper(), connection);
            UiAutomation.class.getMethod("connect", int.class).invoke(automation,
                    UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
            connected = true;
            AccessibilityServiceInfo info = automation.getServiceInfo();
            info.packageNames = new String[]{PACKAGE};
            info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
            info.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
                    | AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
            automation.setServiceInfo(info);
            System.out.println("TT_PROBE_READY nonSuppressing=true input=false");
            long started = SystemClock.elapsedRealtime();
            for (int sample = 0; sample < 4; sample++) {
                nodes = ranges = clocks = truncated = 0;
                tree = sample == 0;
                automation.clearCache(); // API33+ research probe, physically inspected on API37.
                AccessibilityNodeInfo root = automation.getRootInActiveWindow();
                try {
                    if (root == null || !PACKAGE.contentEquals(s(root.getPackageName()))) {
                        System.out.println("TT_STOP targetRootMissing=true");
                        break;
                    }
                    System.out.println("TT_SAMPLE n=" + sample + " t=" + (SystemClock.elapsedRealtime() - started));
                    visit(root, -1, 0);
                    System.out.println("TT_SUMMARY nodes=" + nodes + " ranges=" + ranges
                            + " clocks=" + clocks + " truncated=" + truncated);
                } finally { if (root != null) root.recycle(); }
                if (sample < 3) SystemClock.sleep(700);
            }
        } finally {
            if (connected) UiAutomation.class.getMethod("disconnect").invoke(automation);
            thread.quitSafely();
        }
        System.out.println("TT_PROBE_FINISHED");
        System.exit(0);
    }

    private static void visit(AccessibilityNodeInfo node, int parent, int depth) {
        if (nodes >= 700 || depth > 48) { truncated++; return; }
        if (!PACKAGE.contentEquals(s(node.getPackageName()))) return;
        int index = nodes++;
        String id = s(node.getViewIdResourceName());
        if (!id.matches("com\\.ss\\.android\\.ugc\\.trill:id/[A-Za-z0-9_]+")) id = "none";
        String type = s(node.getClassName());
        if (!type.matches("(?:android|androidx|com\\.ss)\\.[A-Za-z0-9_.$]+")) type = "other";
        AccessibilityNodeInfo.RangeInfo range = node.getRangeInfo();
        if (range != null) ranges++;
        String text = s(node.getText()), desc = s(node.getContentDescription());
        String clock = clock(text) ? text : clock(desc) ? desc : "none";
        if (!clock.equals("none")) clocks++;
        String role = role(text);
        if (role.equals("none")) role = role(desc);
        Rect bounds = new Rect(); node.getBoundsInScreen(bounds);
        if (tree || range != null || !clock.equals("none") || !role.equals("none")) {
            StringBuilder actions = new StringBuilder();
            for (AccessibilityNodeInfo.AccessibilityAction action : node.getActionList()) {
                if (actions.length() > 0) actions.append(',');
                actions.append(action.getId());
            }
            AccessibilityNodeInfo.CollectionInfo collection = node.getCollectionInfo();
            AccessibilityNodeInfo.CollectionItemInfo item = node.getCollectionItemInfo();
            System.out.println("TT_NODE n=" + index + " parent=" + parent + " id=" + id + " class=" + type
                    + " visible=" + node.isVisibleToUser() + " selected=" + node.isSelected()
                    + " scrollable=" + node.isScrollable() + " bounds=" + bounds.toShortString()
                    + " textPresent=" + !text.isEmpty() + " descriptionPresent=" + !desc.isEmpty()
                    + " role=" + role + " clock=" + clock + " actions=" + actions
                    + (range == null ? "" : " min=" + range.getMin() + " max=" + range.getMax()
                    + " current=" + range.getCurrent() + " rangeType=" + range.getType())
                    + (collection == null ? "" : " rows=" + collection.getRowCount() + " columns=" + collection.getColumnCount())
                    + (item == null ? "" : " row=" + item.getRowIndex() + " column=" + item.getColumnIndex()));
        }
        for (int i = 0; i < node.getChildCount() && truncated == 0; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child == null) continue;
            try { visit(child, index, depth + 1); } finally { child.recycle(); }
        }
    }

    private static boolean clock(String value) {
        return value.matches("[0-9]{1,2}:[0-9]{2}(?::[0-9]{2})?(?:\\s*/\\s*[0-9]{1,2}:[0-9]{2}(?::[0-9]{2})?)?");
    }
    private static String role(String value) {
        if (value.equals("추천") || value.equalsIgnoreCase("For You")) return "recommended";
        if (value.equals("팔로잉") || value.equalsIgnoreCase("Following")) return "following";
        if (value.equals("홈") || value.equalsIgnoreCase("Home")) return "home";
        if (value.equals("일시정지") || value.equalsIgnoreCase("Pause")) return "pause";
        if (value.equals("재생") || value.equalsIgnoreCase("Play")) return "play";
        return "none";
    }
    private static String s(CharSequence value) { return value == null ? "" : value.toString().trim(); }
}
