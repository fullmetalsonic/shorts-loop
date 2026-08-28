package com.fullmetalsonic.shortsloop;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import com.fullmetalsonic.shortsloop.core.FeatureSupportPolicy;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.ui.MainActivity;
import com.fullmetalsonic.shortsloop.ui.CompatibilityPanel;
import com.fullmetalsonic.shortsloop.visual.VisualAssistController;
import com.fullmetalsonic.shortsloop.updates.UpdateClientChecks;
import java.util.Map;

/** Permission-free smoke tests on disposable emulators, not social-app auto-advance E2E. */
public final class CompatibilityInstrumentation extends Instrumentation {
    private int checks;
    private Activity activity;
    @Override public void onCreate(Bundle arguments) { super.onCreate(arguments); start(); }
    @Override public void onStart() {
        Bundle result = new Bundle();
        SharedPreferences prefs = null; Map<String, ?> original = null;
        SharedPreferences updates = null; Map<String, ?> originalUpdates = null;
        try {
            String product = Build.PRODUCT;
            require(product.contains("sdk") || product.contains("emulator"), "Disposable emulator only");
            require(getTargetContext().getDrawable(R.mipmap.ic_launcher) != null, "Launcher icon packaged for runtime OS");
            checks += UpdateClientChecks.run(getTargetContext());
            checks += com.fullmetalsonic.shortsloop.detection.YouTubeLiveChecks.run(getTargetContext());
            checks += com.fullmetalsonic.shortsloop.detection.ContentKeyChecks.run(getTargetContext());
            checks += com.fullmetalsonic.shortsloop.detection.PagePositionChecks.run(getTargetContext());
            updates = getTargetContext().getSharedPreferences("updates", 0); originalUpdates = updates.getAll();
            updates.edit().putBoolean("automatic", false).commit();
            SettingsStore store = new SettingsStore(getTargetContext());
            prefs = store.preferences; original = prefs.getAll();
            runOnMainSync(() -> checks += RecoveryServiceChecks.run(getTargetContext(), store));
            // Simulate restored choices without granting permissions or starting automation.
            store.enabled(false); store.visualAssist(true); store.timedFallback(true); store.skipAds(true);
            store.skipLong(true); store.longVideoSeconds(321);
            store.selectedApp("com.instagram.android", true);
            activity = startActivitySync(new Intent(getTargetContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            waitForIdleSync();
            runOnMainSync(() -> {
                require(activity.findViewById(R.id.compatibility_panel) != null, "Compatibility panel exists");
                Button tile = activity.findViewById(R.id.tile_add);
                require(tile.getText().toString().equals(getTargetContext().getString(Build.VERSION.SDK_INT >= 33
                        ? R.string.compat_tile_add_button : R.string.compat_tile_manual_button)), "Correct tile action");
                Switch visual = activity.findViewById(R.id.visual_assist_toggle);
                boolean installed = installedInstagram();
                require(visual.isEnabled() == (Build.VERSION.SDK_INT >= 34 && installed), "Visual availability");
                require(visual.isChecked() == (Build.VERSION.SDK_INT >= 34 && installed), "Stored visual choice cannot look active when unavailable");
                require(new SettingsStore(getTargetContext()).visualAssist(), "Saved visual preference preserved");
                require(((Switch)activity.findViewById(R.id.timed_fallback_toggle)).isEnabled() == installed, "Timer independent of capture API");
                require(((Switch)activity.findViewById(R.id.skip_ads_toggle)).isEnabled() == installed, "Ads independent of capture API");
                require(new SettingsStore(getTargetContext()).timedFallback() && new SettingsStore(getTargetContext()).skipAds(), "Inactive saved options preserved");
                String reason = ((TextView)activity.findViewById(R.id.visual_support)).getText().toString();
                int reasonId = Build.VERSION.SDK_INT < 34 ? R.string.compat_visual_unsupported
                        : installed ? R.string.compat_visual_supported : R.string.compat_instagram_missing;
                require(reason.startsWith(getTargetContext().getString(reasonId)), "Specific capability reason");
                require(!((Switch)activity.findViewById(R.id.execution_toggle)).isChecked(), "No auto start");
                require(activity.findViewById(R.id.update_status) != null, "Update panel exists");
                require(((Button)activity.findViewById(R.id.permission_accessibility)).getText().toString()
                        .equals(getTargetContext().getString(R.string.accessibility_reconnect_action)), "Reconnect action explains recovery");
                require(!((Switch)activity.findViewById(R.id.update_automatic)).isChecked(), "Automatic network check stays off in fixture tests");
                require(CompatibilityPanel.visualReason(activity, 33, true, true, true).contains("이전 선택"), "Old OS explanation preserves choice");
                require(FeatureSupportPolicy.instagramFeature(true, true), "Basic Instagram capability remains available");
                checks += LiveUiChecks.run(activity, store);
                checks += LongVideoUiChecks.run(activity, store);
                checks += com.fullmetalsonic.shortsloop.overlay.FloatingLayoutChecks.run(activity, store);
            });
            VisualAssistController controller = VisualAssistController.create(null, null);
            require(controller != null && !controller.active(), "Factory loads safely on runtime OS");
            if (Build.VERSION.SDK_INT < 34) {
                controller.observe(null, 1, 1000); controller.reset();
                require(!controller.active() && controller.current() == 0, "Old OS capture is no-op");
            }
            try (XmlResourceParser xml = getTargetContext().getResources().getXml(R.xml.accessibility_service)) {
                while (xml.next() != XmlResourceParser.START_TAG) { }
                boolean capture = xml.getAttributeBooleanValue("http://schemas.android.com/apk/res/android", "canTakeScreenshot", false);
                require(capture == (Build.VERSION.SDK_INT >= 34), "Only modern OS declares capture capability");
                require(xml.getAttributeBooleanValue("http://schemas.android.com/apk/res/android", "canPerformGestures", false), "Gesture capability retained");
            }
            result.putString("result", "PASS"); result.putInt("checks", checks); result.putInt("api", Build.VERSION.SDK_INT);
        } catch (Throwable error) {
            result.putString("result", "FAIL"); result.putString("error", error.toString());
            result.putInt("checks", checks); result.putInt("api", Build.VERSION.SDK_INT);
        } finally {
            if (activity != null) runOnMainSync(() -> activity.finish());
            if (prefs != null && original != null) restore(prefs, original);
            if (updates != null && originalUpdates != null) restore(updates, originalUpdates);
            finish("PASS".equals(result.getString("result")) ? Activity.RESULT_OK : Activity.RESULT_CANCELED, result);
        }
    }
    private void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); checks++; }
    private boolean installedInstagram() {
        try { getTargetContext().getPackageManager().getApplicationInfo("com.instagram.android", 0); return true; }
        catch (android.content.pm.PackageManager.NameNotFoundException ignored) { return false; }
    }
    private void restore(SharedPreferences prefs, Map<String, ?> values) {
        SharedPreferences.Editor editor = prefs.edit().clear();
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            Object v = entry.getValue(); String k = entry.getKey();
            if (v instanceof Boolean) editor.putBoolean(k, (Boolean)v);
            else if (v instanceof Integer) editor.putInt(k, (Integer)v);
            else if (v instanceof Long) editor.putLong(k, (Long)v);
            else if (v instanceof Float) editor.putFloat(k, (Float)v);
            else if (v instanceof String) editor.putString(k, (String)v);
            else if (v instanceof java.util.Set) editor.putStringSet(k, new java.util.HashSet<>((java.util.Set<String>)v));
        }
        editor.commit();
    }
}
