package probes;

import android.graphics.Rect;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.uiautomator.core.Configurator;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;
import java.util.regex.Pattern;

/** Read-only timing-source survey. Run only with ShortsLoop execution OFF.
 * Never outputs text, descriptions, account names, extras values, or media identities.
 */
public final class InstagramTimingProbe extends UiAutomatorTestCase {
    private static final String PACKAGE = "com.instagram.android";
    private static final Pattern TIME = Pattern.compile(
            "(?:[0-9]{1,3}:)?[0-9]{1,2}:[0-9]{2}(?:\\s*/\\s*(?:[0-9]{1,3}:)?[0-9]{1,2}:[0-9]{2})?"
            + "|[0-9]+(?:\\.[0-9]+)?\\s*(?:초|분|seconds?|minutes?)(?:\\s+[0-9]+\\s*(?:초|seconds?))?",
            Pattern.CASE_INSENSITIVE);

    private static final class Root extends UiObject {
        Root() { super(new UiSelector().packageName(PACKAGE)); }
        AccessibilityNodeInfo snapshot() { return findAccessibilityNodeInfo(0); }
    }

    private int count, ranges, timeFields, stateFields, truncated, stale, otherExtraData;
    private boolean dumpTree;

    public void testTimingSources() {
        Configurator config = Configurator.getInstance();
        long oldIdle = config.getWaitForIdleTimeout();
        long oldSelector = config.getWaitForSelectorTimeout();
        config.setWaitForIdleTimeout(0).setWaitForSelectorTimeout(0);
        AccessibilityNodeInfo root = null;
        try {
            root = new Root().snapshot();
            if (root == null) { System.out.println("IG_TREE root=missing"); return; }
            for (int i = 0; i < 32; i++) {
                AccessibilityNodeInfo parent = root.getParent();
                if (parent == null) break;
                if (!PACKAGE.contentEquals(s(parent.getPackageName()))) { parent.recycle(); break; }
                root.recycle(); root = parent;
            }
            int samples = Math.max(1, Math.min(20,
                    Integer.parseInt(getParams().getString("samples", "4"))));
            long start = SystemClock.elapsedRealtime();
            for (int i = 0; i < samples; i++) {
                count = ranges = timeFields = stateFields = truncated = stale = otherExtraData = 0;
                dumpTree = i == 0;
                boolean fresh = root.refresh();
                System.out.println("IG_TREE sample=" + (i + 1) + " t="
                        + (SystemClock.elapsedRealtime() - start) + " rootFresh=" + fresh);
                if (fresh) visit(root, -1, 0);
                System.out.println("IG_TREE_SUMMARY nodes=" + count + " ranges=" + ranges
                        + " timeFields=" + timeFields + " stateFields=" + stateFields
                        + " stale=" + stale + " otherExtraData=" + otherExtraData + " truncated=" + truncated);
                if (i + 1 < samples) SystemClock.sleep(650);
            }
        } finally {
            if (root != null) root.recycle();
            config.setWaitForIdleTimeout(oldIdle).setWaitForSelectorTimeout(oldSelector);
        }
    }

    private void visit(AccessibilityNodeInfo node, int parent, int depth) {
        if (count >= 600 || depth > 48) { truncated++; return; }
        if (!node.refresh()) { stale++; return; }
        if (!PACKAGE.contentEquals(s(node.getPackageName()))) return;
        int index = count++;
        AccessibilityNodeInfo.RangeInfo range = node.getRangeInfo();
        String text = s(node.getText()), description = s(node.getContentDescription());
        String state = s(node.getStateDescription());
        // Presence only: a caption may contain a timestamp, so a match is not a playback clock.
        boolean timeText = TIME.matcher(text).find(), timeDescription = TIME.matcher(description).find();
        boolean timeState = TIME.matcher(state).find();
        if (range != null) ranges++;
        if (timeText) timeFields++;
        if (timeDescription) timeFields++;
        if (timeState) timeFields++;
        if (!state.isEmpty()) stateFields++;
        int otherExtra = 0;
        for (String key : node.getAvailableExtraData()) {
            if (!AccessibilityNodeInfo.EXTRA_DATA_RENDERING_INFO_KEY.equals(key)
                    && !AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY.equals(key)) otherExtra++;
        }
        otherExtraData += otherExtra;
        Rect bounds = new Rect(); node.getBoundsInScreen(bounds);
        // Only built-in view resource identifiers and class identifiers are retained.
        String id = s(node.getViewIdResourceName());
        if (!id.matches("com\\.instagram\\.android:id/[A-Za-z0-9_]+")) id = "none";
        String type = s(node.getClassName());
        if (!type.matches("(?:android|androidx|com\\.instagram)\\.[A-Za-z0-9_.$]+")) type = "other";
        StringBuilder actions = new StringBuilder();
        for (AccessibilityNodeInfo.AccessibilityAction action : node.getActionList()) {
            if (actions.length() > 0) actions.append(',');
            actions.append(action.getId()); // Never output custom action labels.
        }
        if (dumpTree || range != null || timeText || timeDescription || timeState || !state.isEmpty() || otherExtra > 0)
            System.out.println("IG_NODE n=" + index + " parent=" + parent + " id=" + id + " class=" + type
                + " visible=" + node.isVisibleToUser() + " bounds=" + bounds.toShortString()
                + " textPresent=" + !text.isEmpty() + " descriptionPresent=" + !description.isEmpty()
                + " statePresent=" + !state.isEmpty() + " timeText=" + timeText
                + " timeDescription=" + timeDescription + " timeState=" + timeState
                + " extrasCount=" + node.getExtras().size()
                + " extraDataCount=" + node.getAvailableExtraData().size() + " otherExtraData=" + otherExtra + " actions=" + actions
                + (range == null ? "" : " min=" + range.getMin() + " max=" + range.getMax()
                + " current=" + range.getCurrent() + " rangeType=" + range.getType()));
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child == null) continue;
            try { visit(child, index, depth + 1); }
            finally { child.recycle(); }
            if (truncated > 0) break;
        }
    }

    private static String s(CharSequence value) { return value == null ? "" : value.toString().trim(); }
}
