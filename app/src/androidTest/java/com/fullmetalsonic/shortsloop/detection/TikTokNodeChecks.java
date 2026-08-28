package com.fullmetalsonic.shortsloop.detection;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import com.fullmetalsonic.shortsloop.core.NormalizedProgress;
import com.fullmetalsonic.shortsloop.core.TikTokStructurePolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** Synthetic framework-node identity/metadata checks; no TikTok app or permissions. */
public final class TikTokNodeChecks {
    private TikTokNodeChecks() { }
    @SuppressWarnings("deprecation")
    public static int run(Context context) {
        AccessibilityNodeInfo a = AccessibilityNodeInfo.obtain(new View(context));
        AccessibilityNodeInfo copy = AccessibilityNodeInfo.obtain(a);
        AccessibilityNodeInfo b = AccessibilityNodeInfo.obtain(new View(context));
        try {
            String first = TikTokReader.sourceKey("page", a);
            require(first.equals(TikTokReader.sourceKey("page", copy)), "Same node copy keeps identity");
            require(!first.equals(TikTokReader.sourceKey("page", b)), "Different synthetic source differs");
            b.setText("Synthetic title"); require(first.equals(TikTokReader.sourceKey("page", a)), "A-B-A is stateless");
            a.setText("Changed synthetic title"); require(first.equals(TikTokReader.sourceKey("page", a)), "Caption is not identity");
            YouTubeSnapshot snapshot = YouTubeSnapshot.normalizedVideo(first, new Rect(0, 0, 1000, 1600), new NormalizedProgress(.25))
                    .withNormalizedIdentity("pager", "media", -1).withIdentity("package|" + first)
                    .inWindow(7, new Rect(0, 0, 1000, 1700)).withContentIdentity("extra").withPhotoPageKey("");
            require(snapshot.normalizedUsable() && !snapshot.usable() && snapshot.progress == null, "Normalized data never becomes seconds");
            require(snapshot.normalizedPagerKey.equals("pager") && snapshot.normalizedMediaKey.equals("media"), "Independent source keys survive copies");
            require(snapshot.windowId == 7 && snapshot.normalizedPageIndex == -1, "Unknown index stays unknown");
            return 7 + mappingChecks(context);
        } catch (ReflectiveOperationException error) {
            throw new AssertionError("Synthetic TikTok node mapping", error);
        } finally { a.recycle(); copy.recycle(); b.recycle(); }
    }

    /** Runs real Android metadata and production mapping, not remote refresh or OS input. */
    private static int mappingChecks(Context context) throws ReflectiveOperationException {
        Check c = new Check();
        try (Fixture f = new Fixture(context)) {
            YouTubeSnapshot texture = f.snapshot();
            c.require(texture.normalizedUsable() && !texture.visualCandidate, "Visible normalized zero is not a timer");
            f.nodes.get(9).setClassName("android.view.SurfaceView");
            YouTubeSnapshot surface = f.snapshot();
            c.require(surface.normalizedUsable(), "Surface metadata accepts the same safe video hierarchy");
            c.require(!texture.contentIdentity.equals(surface.contentIdentity), "Renderer kind changes the counter discriminator");
            c.require(texture.normalizedMediaKey.equals(surface.normalizedMediaKey), "Renderer kind never fabricates independent source movement");
            f.nodes.get(12).setVisibleToUser(false);
            YouTubeSnapshot clockless = f.snapshot();
            c.require(clockless.recognized() && clockless.visualCandidate && !clockless.normalizedUsable(), "Hidden zero only grants known-video clockless eligibility");
            f.nodes.get(12).setVisibleToUser(true);
            f.nodes.get(12).setRangeInfo(AccessibilityNodeInfo.RangeInfo.obtain(0, 0, 100, 0));
            c.require(!f.snapshot().recognized(), "Invalid visible range cannot become clockless");
            f.range(0); f.nodes.get(12).setContentDescription("00:00 / 01:30");
            YouTubeSnapshot seconds = f.snapshot();
            c.require(seconds.progress != null && seconds.progress.duration == 90 && seconds.normalizedUsable(), "Actual seek clock coexists with normalized progress");
            f.nodes.get(12).setText("00:01 / 01:30");
            c.require(!f.snapshot().recognized(), "Conflicting dedicated clocks reject the frame");
        }
        try (Fixture f = new Fixture(context)) {
            f.ad(); YouTubeSnapshot raw = f.snapshot(), effective = raw.withAd(false);
            c.require(raw.ad && raw.normalizedProgress != null && !raw.normalizedUsable(), "Ad video preserves playback data but marks advertising");
            c.require(effective.normalizedUsable() && effective.normalizedMediaKey.equals(raw.normalizedMediaKey), "Ad-OFF view preserves verified ordinary data");
            c.require(raw.photoPageKey.equals(raw.normalizedMediaKey) && !raw.photoPageKey.isEmpty(), "Ad timing retains an independent source key");
            c.require(sameSource(raw, raw.inWindow(4, PAGE)), "Source comparison accepts unchanged copies");
            c.require(sameSource(raw, raw.withIdentity(TikTokReader.PACKAGE + "|" + raw.identity)), "Fixed ShortsReader host namespace preserves raw source matching");
            c.require(!sameSource(raw, raw.withIdentity("another.host|" + raw.identity)), "An arbitrary host prefix cannot rewrite identity");
            c.require(!sameSource(raw, raw.withIdentity(TikTokReader.PACKAGE + "|" + TikTokReader.PACKAGE + "|" + raw.identity)), "Duplicate host prefixes are not stripped repeatedly");
            c.require(!sameSource(raw, effective), "Raw-ad verification rejects a policy-cleared copy");
            c.require(!sameSource(raw, raw.withIdentity("different-page")), "Changed page cannot reuse a vertical intention");
            c.require(!sameSource(raw, raw.withNormalizedIdentity("different-pager", raw.normalizedMediaKey, -1)), "Changed pager is rejected");
            c.require(!sameSource(raw, raw.withNormalizedIdentity(raw.normalizedPagerKey, "different-media", -1)), "Changed media is rejected");
            c.require(!sameSource(raw, raw.withNormalizedIdentity(raw.normalizedPagerKey, raw.normalizedMediaKey, 1)), "Changed index is rejected");
            c.require(!sameSource(raw, raw.withPhotoPageKey("different-source")), "Changed delay/photo key is rejected");
            c.require(!sameSource(raw, raw.withContentIdentity("different-renderer")), "Renderer replacement cancels a deferred intention");
            c.require(TikTokReader.findPager(f.nodes.get(0), raw.inWindow(4, PAGE), 5) == null, "Mismatched expected window cannot supply a pager");
        }
        try (Fixture f = new Fixture(context)) {
            int ad = f.ad(); f.nodes.get(ad).setText("My sponsored caption");
            c.require(!f.snapshot().ad, "Caption substring does not create an advertising label");
            f.nodes.get(ad).setText("Sponsored"); f.nodes.get(ad).setViewIdResourceName(P + "caption");
            c.require(!f.snapshot().recognized(), "Exact ad word at an unknown compact control fails closed");
        }
        try (Fixture f = new Fixture(context)) {
            f.photo(true); YouTubeSnapshot photo = f.snapshot();
            c.require(photo.photo != null && photo.photo.position.current() == 2 && photo.photo.position.total() == 3, "Three direct numeric children map a photo index");
            c.require(!photo.visualCandidate && photo.normalizedProgress == null, "Photo cannot acquire an ordinary timer");
            f.ad(); YouTubeSnapshot adPhoto = f.snapshot();
            c.require(adPhoto.ad && adPhoto.photo != null, "Photo and advertising evidence coexist");
            YouTubeSnapshot enabledPhoto = adPhoto.withAd(false).inWindow(7, PAGE).withIdentity("wrapped")
                    .withContentIdentity("render").withNormalizedIdentity("pager", "media", 3).withPhotoPageKey("image");
            c.require(!enabledPhoto.ad && enabledPhoto.photo == adPhoto.photo && enabledPhoto.photo.position.equals(adPhoto.photo.position), "Every copy helper preserves photo payload");
            f.nodes.get(f.index + 2).setText("-");
            c.require(!f.snapshot().recognized(), "Non-slash separator cannot become a missing-index fallback");
        }
        try (Fixture f = new Fixture(context)) {
            f.photo(false); YouTubeSnapshot photo = f.snapshot();
            c.require(photo.photo != null && photo.photo.position.missing(), "No number means unknown total, not one photo");
            f.add("second", "android.view.TextureView", 8, PAGE);
            c.require(!f.snapshot().recognized(), "Visible mixed image/video rejects photo automation");
        }
        try (Fixture f = new Fixture(context)) {
            f.photo(false); f.add("wel", "android.widget.ImageView", f.imageParent, PAGE);
            c.require(!f.snapshot().recognized(), "Two visible images reject an animated slide");
        }
        try (Fixture f = new Fixture(context)) {
            f.nodes.get(8).setVisibleToUser(false); f.nodes.get(9).setVisibleToUser(false);
            int pager = f.add("player_view_pager", "synthetic.Pager", 5, PAGE);
            int frame = f.add("", "android.widget.FrameLayout", pager, PAGE);
            f.add("", "android.view.SurfaceView", frame, PAGE); f.ad();
            YouTubeSnapshot dot = f.snapshot();
            c.require(dot.ad && dot.recognized() && dot.photo == null, "Known ad-only renderer is not invented photo data");
            c.require(dot.normalizedProgress == null && dot.progress == null && !dot.visualCandidate, "Ad-only pager never acquires ordinary playback/timer capability");
        }
        return c.count;
    }
    private static boolean sameSource(YouTubeSnapshot a, YouTubeSnapshot b) throws ReflectiveOperationException {
        Method method = TikTokReader.class.getDeclaredMethod("sameSource", YouTubeSnapshot.class, YouTubeSnapshot.class);
        method.setAccessible(true); return (boolean) method.invoke(null, a, b);
    }
    private static final String P = TikTokStructurePolicy.PREFIX;
    private static final Rect PAGE = new Rect(0, 0, 1000, 1600);
    @SuppressWarnings("deprecation")
    private static final class Fixture implements AutoCloseable {
        final Context context;
        final List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        final List<Integer> parents = new ArrayList<>();
        int index = -1, imageParent = -1;
        Fixture(Context context) {
            this.context = context;
            add("root", "android.widget.FrameLayout", -1, PAGE);
            add("viewpager_container", "android.widget.LinearLayout", 0, PAGE);
            add("view_pager_layout_wrapper", "android.widget.FrameLayout", 1, PAGE);
            add("viewpager", "androidx.viewpager.widget.ViewPager", 2, PAGE);
            add("view_rootview", "android.widget.FrameLayout", 3, PAGE); nodes.get(4).setVisibleToUser(false);
            add("view_rootview", "android.widget.FrameLayout", 3, PAGE);
            add("video_container_area", "android.widget.FrameLayout", 5, PAGE);
            add("video_visible_area_container", "android.widget.FrameLayout", 6, PAGE);
            add("player_view", "android.widget.FrameLayout", 7, PAGE);
            add("", "android.view.TextureView", 8, PAGE);
            add("view_rootview", "android.widget.FrameLayout", 3, PAGE); nodes.get(10).setVisibleToUser(false);
            add("video_seek_bar", "android.widget.LinearLayout", 0, PAGE);
            add("vb6", "android.widget.SeekBar", 11, new Rect(0, 1450, 1000, 1490)); range(0);
            add("", "android.widget.TextView", 0, new Rect(500, 0, 700, 80));
            nodes.get(13).setSelected(true); nodes.get(13).setText("추천");
            add("", "android.widget.TextView", 0, new Rect(0, 1500, 180, 1600));
            nodes.get(14).setSelected(true); nodes.get(14).setText("홈");
        }
        int add(String id, String type, int parent, Rect bounds) {
            AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain(new View(context));
            node.setPackageName(TikTokReader.PACKAGE); node.setViewIdResourceName(id.isEmpty() ? null : P + id);
            node.setClassName(type); node.setBoundsInScreen(bounds); node.setVisibleToUser(true);
            nodes.add(node); parents.add(parent); return nodes.size() - 1;
        }
        void range(float current) { nodes.get(12).setRangeInfo(AccessibilityNodeInfo.RangeInfo.obtain(0, 0, 10000, current)); }
        int ad() {
            int widget = add("widget_container", "android.widget.FrameLayout", 5, PAGE);
            int ad = add("i2n", "android.widget.Button", widget, new Rect(20, 1300, 100, 1330));
            nodes.get(ad).setText("Sponsored"); nodes.get(ad).setClickable(true); return ad;
        }
        void photo(boolean numbered) {
            nodes.get(9).setVisibleToUser(false); nodes.get(12).setVisibleToUser(false);
            int widget = add("widget_container", "android.widget.FrameLayout", 5, PAGE);
            int label = add("tv_label", "android.widget.TextView", widget, new Rect(200, 1200, 270, 1240));
            nodes.get(label).setText("사진");
            int abx = add("abx", "synthetic.Horizontal", 8, PAGE);
            int content = add("qzz", "android.view.ViewGroup", abx, PAGE);
            int carousel = add("r0h", "synthetic.Photos", content, PAGE);
            int slide = add("r04", "android.view.ViewGroup", carousel, PAGE);
            imageParent = add("qzz", "android.widget.FrameLayout", slide, PAGE);
            add("wel", "android.widget.ImageView", imageParent, new Rect(0, 0, 1000, 1400));
            if (!numbered) return;
            index = add("r06", "android.widget.LinearLayout", content, new Rect(800, 200, 960, 240));
            int current = add("", "android.widget.TextView", index, new Rect(810, 205, 840, 235)); nodes.get(current).setText("2");
            int slash = add("", "android.widget.TextView", index, new Rect(850, 205, 870, 235)); nodes.get(slash).setText("/");
            int total = add("", "android.widget.TextView", index, new Rect(880, 205, 920, 235)); nodes.get(total).setText("3");
        }
        @SuppressWarnings("unchecked")
        YouTubeSnapshot snapshot() throws ReflectiveOperationException {
            Class<?> type = Class.forName(TikTokReader.class.getName() + "$Tree");
            Constructor<?> constructor = type.getDeclaredConstructor(AccessibilityNodeInfo.class); constructor.setAccessible(true);
            Object tree = constructor.newInstance(nodes.get(0));
            Field nodeField = type.getDeclaredField("nodes"); nodeField.setAccessible(true);
            ((List<AccessibilityNodeInfo>) nodeField.get(tree)).addAll(nodes);
            Field metaField = type.getDeclaredField("metadata"); metaField.setAccessible(true);
            List<TikTokStructurePolicy.Node> metadata = (List<TikTokStructurePolicy.Node>) metaField.get(tree);
            Method metadataMethod = TikTokReader.class.getDeclaredMethod("metadata", AccessibilityNodeInfo.class, int.class, type);
            metadataMethod.setAccessible(true);
            for (int i = 0; i < nodes.size(); i++) metadata.add((TikTokStructurePolicy.Node)
                    metadataMethod.invoke(null, nodes.get(i), parents.get(i), tree));
            TikTokStructurePolicy.Match match = TikTokStructurePolicy.inspect(metadata);
            if (!match.accepted()) return YouTubeSnapshot.unavailable(match.reason);
            Method snapshotMethod = TikTokReader.class.getDeclaredMethod("snapshot", type, TikTokStructurePolicy.Match.class);
            snapshotMethod.setAccessible(true); return (YouTubeSnapshot) snapshotMethod.invoke(null, tree, match);
        }
        @Override public void close() { for (AccessibilityNodeInfo node : nodes) node.recycle(); }
    }
    private static final class Check {
        int count;
        void require(boolean value, String message) { TikTokNodeChecks.require(value, message); count++; }
    }
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
