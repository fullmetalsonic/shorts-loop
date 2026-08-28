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
    private static final java.util.regex.Pattern PAGE_PAIR = java.util.regex.Pattern.compile(
            "(?i)^(?:(?:사진|photo)\\s*)?([0-9]{1,3})\\s*(?:/|of)\\s*([0-9]{1,3})(?:\\s*(?:사진|photos?))?$");
    private static final java.util.regex.Pattern PAGE_KOREAN_PAIR = java.util.regex.Pattern.compile(
            "^([0-9]{1,3})장\\s*중\\s*([0-9]{1,3})장$");
    private static final java.util.regex.Pattern PAGE_TOTAL = java.util.regex.Pattern.compile(
            "(?i)^(?:(?:총|사진)\\s*)?([0-9]{1,3})(?:\\s*장의?\\s*사진|\\s*장|\\s+photos)$");
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
        String state = s(node.getStateDescription());
        String clock = clock(text) ? text : clock(desc) ? desc : "none";
        if (!clock.equals("none")) clocks++;
        String role = role(text);
        if (role.equals("none")) role = role(desc);
        String textRole = role(text), descriptionRole = role(desc);
        PageIndicator textPage = pageIndicator(text), descriptionPage = pageIndicator(desc), statePage = pageIndicator(state);
        AccessibilityNodeInfo.CollectionInfo collection = node.getCollectionInfo();
        AccessibilityNodeInfo.CollectionItemInfo item = node.getCollectionItemInfo();
        Rect bounds = new Rect(); node.getBoundsInScreen(bounds);
        boolean identityNode = id.equals(PACKAGE + ":id/view_rootview") || id.equals(PACKAGE + ":id/player_view")
                || (id.equals(PACKAGE + ":id/viewpager") && type.equals("androidx.viewpager.widget.ViewPager"))
                || type.equals("android.view.SurfaceView") || type.equals("android.view.TextureView");
        if (tree || identityNode || range != null || collection != null || item != null
                || !clock.equals("none") || !role.equals("none") || textPage != null || descriptionPage != null
                || statePage != null || smallNumeric(text) || smallNumeric(state)) {
            StringBuilder actions = new StringBuilder();
            for (AccessibilityNodeInfo.AccessibilityAction action : node.getActionList()) {
                if (actions.length() > 0) actions.append(',');
                actions.append(action.getId());
            }
            System.out.println("TT_NODE n=" + index + " parent=" + parent + " id=" + id + " class=" + type
                    + " visible=" + node.isVisibleToUser() + " selected=" + node.isSelected()
                    + " scrollable=" + node.isScrollable() + " bounds=" + bounds.toShortString()
                    + " textPresent=" + !text.isEmpty() + " descriptionPresent=" + !desc.isEmpty()
                    + " role=" + role + " clock=" + clock + " actions=" + actions
                    + " textRole=" + textRole + " descriptionRole=" + descriptionRole
                    + " windowId=" + node.getWindowId() + " nodeHash=" + node.hashCode()
                    + " clickable=" + node.isClickable() + " editable=" + node.isEditable() + " focused=" + node.isFocused()
                    + " statePresent=" + !state.isEmpty()
                    + pageOutput("text", textPage) + pageOutput("description", descriptionPage) + pageOutput("state", statePage)
                    + (smallNumeric(text) ? " textSmallNumber=" + Integer.parseInt(text) : "")
                    + (smallNumeric(state) ? " stateSmallNumber=" + Integer.parseInt(state) : "")
                    + (range == null ? "" : " min=" + range.getMin() + " max=" + range.getMax()
                    + " current=" + range.getCurrent() + " rangeType=" + range.getType())
                    + (collection == null ? "" : " rows=" + collection.getRowCount() + " columns=" + collection.getColumnCount()
                    + " selectionMode=" + collection.getSelectionMode() + " hierarchical=" + collection.isHierarchical())
                    + (item == null ? "" : " row=" + item.getRowIndex() + " column=" + item.getColumnIndex()
                    + " rowSpan=" + item.getRowSpan() + " columnSpan=" + item.getColumnSpan() + " itemSelected=" + item.isSelected()));
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
    /** Bounded whole-string numbers only; no account, caption or arbitrary state text. */
    private static boolean smallNumeric(String value) { return value.matches("[0-9]{1,3}"); }
    private static String role(String value) {
        if (value.equals("추천") || value.equalsIgnoreCase("For You")) return "recommended";
        if (value.equals("팔로잉") || value.equalsIgnoreCase("Following")) return "following";
        if (value.equals("홈") || value.equalsIgnoreCase("Home")) return "home";
        if (value.equals("일시정지") || value.equalsIgnoreCase("Pause")) return "pause";
        if (value.equals("재생") || value.equalsIgnoreCase("Play")) return "play";
        // Exact label observations only: even a caption can contain one of these words.
        // These enums are NOT verified content classifiers and never authorize product input.
        if (value.equals("광고") || value.equalsIgnoreCase("Sponsored")
                || value.equalsIgnoreCase("Advertisement") || value.equalsIgnoreCase("Ad")) return "ad_label";
        if (value.equals("라이브") || value.equalsIgnoreCase("LIVE")) return "live_label";
        if (value.equals("사진 모드") || value.equals("사진모드") || value.equalsIgnoreCase("Photo mode")) return "photo_mode_label";
        if (value.equals("사진") || value.equalsIgnoreCase("Photos")) return "photos_label";
        if (value.equals("다음 사진") || value.equalsIgnoreCase("Next photo")) return "next_photo_control_label";
        if (value.equals("이전 사진") || value.equalsIgnoreCase("Previous photo")) return "previous_photo_control_label";
        if (value.equals("왼쪽으로 스와이프") || value.equals("왼쪽으로 밀어 더 보기")
                || value.equalsIgnoreCase("Swipe left") || value.equalsIgnoreCase("Swipe left to see more")) return "swipe_left_instruction";
        if (value.equals("오른쪽으로 스와이프") || value.equalsIgnoreCase("Swipe right")) return "swipe_right_instruction";
        if (value.equals("사진을 보려면 스와이프") || value.equalsIgnoreCase("Swipe to view photos")) return "swipe_photos_instruction";
        return "none";
    }
    /** Only whole-string, bounded numeric page patterns; never print the matched source text. */
    private static PageIndicator pageIndicator(String value) {
        if (value.length() > 48) return null;
        java.util.regex.Matcher pair = PAGE_PAIR.matcher(value);
        if (pair.matches()) return indicator("pair_pattern", Integer.parseInt(pair.group(1)), Integer.parseInt(pair.group(2)));
        java.util.regex.Matcher korean = PAGE_KOREAN_PAIR.matcher(value);
        if (korean.matches()) return indicator("korean_pair_pattern", Integer.parseInt(korean.group(2)), Integer.parseInt(korean.group(1)));
        java.util.regex.Matcher count = PAGE_TOTAL.matcher(value);
        if (count.matches()) return indicator("total_pattern", -1, Integer.parseInt(count.group(1)));
        return null;
    }
    private static PageIndicator indicator(String pattern, int current, int total) {
        if (total < 1 || total > 999 || (current != -1 && (current < 1 || current > total))) return null;
        return new PageIndicator(pattern, current, total);
    }
    private static String pageOutput(String source, PageIndicator value) {
        return value == null ? "" : " " + source + "PagePattern=" + value.pattern
                + " " + source + "PageCurrent=" + value.current + " " + source + "PageTotal=" + value.total;
    }
    private static final class PageIndicator {
        final String pattern;
        final int current, total;
        PageIndicator(String pattern, int current, int total) { this.pattern = pattern; this.current = current; this.total = total; }
    }
    private static String s(CharSequence value) { return value == null ? "" : value.toString().trim(); }
}
