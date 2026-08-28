package com.fullmetalsonic.shortsloop.overlay;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.os.LocaleList;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.fullmetalsonic.shortsloop.R;
import com.fullmetalsonic.shortsloop.core.LongVideoPolicy;
import com.fullmetalsonic.shortsloop.data.SettingsStore;
import com.fullmetalsonic.shortsloop.i18n.AppLocale;
import com.fullmetalsonic.shortsloop.tile.ShortsTileService;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Synthetic lifecycle callbacks on a disposable emulator, NOT a SystemUI/OS-locale E2E test.
 * Window writes are intercepted. Only this instrumentation process's system Resources locale
 * is changed synchronously, then restored; no OS settings, Locale.setDefault, permission or
 * real overlay/tile binding is changed. API33 LocaleManager is hidden by the fixture context.
 */
public final class FloatingLocaleLifecycleChecks {
    private int checks;

    public static int run(Context context, SettingsStore store) {
        return new FloatingLocaleLifecycleChecks().verify(context, store);
    }

    @SuppressWarnings("deprecation")
    private int verify(Context base, SettingsStore store) {
        require(Build.PRODUCT.contains("sdk") || Build.PRODUCT.contains("emulator"), "Disposable emulator only");
        require(Looper.myLooper() == Looper.getMainLooper(), "Run synchronous locale fixtures on the main thread");
        Resources system = Resources.getSystem();
        Configuration original = new Configuration(system.getConfiguration());
        DisplayMetrics originalMetrics = new DisplayMetrics(); originalMetrics.setTo(system.getDisplayMetrics());
        Map<String, ?> preferences = new HashMap<>(store.preferences.getAll());
        Map<String, ?> updatePreferences = new HashMap<>(base.getSharedPreferences("updates", 0).getAll());
        WindowSink windows = new WindowSink(base.getSystemService(WindowManager.class));
        FixtureContext context = new FixtureContext(base, windows.manager);
        Events events = new Events();
        FloatingController controller = null;
        ShortsTileService tile = null;
        try {
            language(system, original, originalMetrics, "ko");
            require("ko".equals(AppLocale.language(context)), "Fixture uses process-only Korean without LocaleManager override");
            controller = new FloatingController(context, store, events);
            controller.show(); controller.update(0, 2, LongVideoPolicy.CHECKING);
            View first = windows.attached;
            Rect position = controller.bounds();
            require(first != null && windows.adds == 1 && windows.removes == 0, "First overlay is attached only to the fake manager");
            assertFloating(context, windows, "ko", R.string.flo_long, "Korean initial view");
            require(first.findViewById(R.id.floating_count).performClick() && events.cycles == 1,
                    "Initial count callback is wired");

            language(system, original, originalMetrics, "en");
            controller.configurationChanged();
            require(windows.attached != first && windows.adds == 2 && windows.removes == 1,
                    "Same controller removes the old view and creates the English view");
            assertFloating(context, windows, "en", R.string.flo_long, "English rebuilt view");
            require(position.equals(controller.bounds()), "Locale rebuild preserves the stored screen position");
            require(events.ends == 1 && !events.interacting && events.closes == 0 && events.cycles == 1,
                    "Rebuild ends interaction without triggering close or count actions");
            require(windows.attached.findViewById(R.id.floating_count).performClick() && events.cycles == 2,
                    "Count callback survives view recreation");
            require(windows.attached.findViewById(R.id.floating_close).performClick() && events.closes == 1,
                    "Close callback survives view recreation");

            controller.update(1, 2, "timed.waiting", 7);
            require("7s".contentEquals(number(windows).getText()), "Countdown renders in the rebuilt English context");
            View second = windows.attached;
            language(system, original, originalMetrics, "ko");
            controller.configurationChanged();
            require(windows.attached != second && windows.adds == 3 && windows.removes == 2,
                    "Same controller can switch back without stale view reuse");
            require("7초".contentEquals(number(windows).getText()), "Rebuild retains countdown state and translates it to Korean");
            require(number(windows).getContentDescription().toString().contains("7초"), "Countdown accessibility description is refreshed");
            assertClose(context, windows, "ko");
            require(position.equals(controller.bounds()), "Second rebuild still preserves screen position");
            controller.update(1, 2, "playback.counting");
            require("1/2".contentEquals(number(windows).getText()), "Ordinary count survives locale changes");
            require(events.ends == 2 && !events.interacting && events.cycles == 2 && events.closes == 1,
                    "Only requested user callbacks occurred");

            controller.hide();
            int added = windows.adds, removed = windows.removes;
            language(system, original, originalMetrics, "en");
            controller.configurationChanged();
            require(windows.attached == null && windows.adds == added && windows.removes == removed,
                    "Changing language while hidden does not create an overlay");
            controller.show(); controller.update(0, 2, LongVideoPolicy.CHECKING);
            assertFloating(context, windows, "en", R.string.flo_long, "Shown after hidden locale change");
            require(position.equals(controller.bounds()), "Show after hidden language change restores the same position");

            // An unbound real TileService safely stops at getQsTile()==null. Count the localized
            // resource requests BEFORE that boundary, exercising the real listening callback
            // branch without pretending to verify SystemUI publication or rendered tile text.
            tile = new ShortsTileService();
            Method attach = ContextWrapper.class.getDeclaredMethod("attachBaseContext", Context.class);
            attach.setAccessible(true); attach.invoke(tile, context);
            require(tile.getQsTile() == null, "Synthetic tile remains unbound to SystemUI");
            int beforeListening = context.resourceRequests;
            tile.onStartListening();
            int requests = context.resourceRequests;
            require(requests > beforeListening && "en".equals(context.lastLanguage), "Listening starts through the localized tile rendering path");
            language(system, original, originalMetrics, "ko");
            tile.onConfigurationChanged(new Configuration(system.getConfiguration()));
            require(context.resourceRequests > requests && "ko".equals(context.lastLanguage),
                    "Listening tile configuration callback refreshes its localized resources");
            tile.onStopListening(); requests = context.resourceRequests;
            language(system, original, originalMetrics, "en");
            tile.onConfigurationChanged(new Configuration(system.getConfiguration()));
            require(context.resourceRequests == requests, "Stopped tile does not render on configuration callback");
            tile.onStartListening();
            require(context.resourceRequests > requests && "en".equals(context.lastLanguage),
                    "Next listening callback uses the latest language");
            require(tile.getQsTile() == null, "No synthetic check bound or published a real tile");

            require(preferences.equals(store.preferences.getAll()), "All typed playback preferences remain unchanged");
            require(updatePreferences.equals(base.getSharedPreferences("updates", 0).getAll()), "All update preferences remain unchanged");
        } catch (ReflectiveOperationException error) {
            throw new AssertionError("Synthetic tile context attachment failed", error);
        } finally {
            try {
                if (tile != null) tile.onStopListening();
                if (controller != null) controller.hide();
            } finally {
                system.updateConfiguration(original, originalMetrics);
            }
        }
        require(original.equals(system.getConfiguration()), "Process system Resources configuration is restored");
        require(windows.attached == null && windows.adds == windows.removes, "Every fake overlay attachment is released");
        return checks;
    }

    private void assertFloating(Context context, WindowSink windows, String language, int text, String where) {
        Context expected = AppLocale.forLanguage(context, language);
        require(expected.getString(text).contentEquals(number(windows).getText()), where + ": translated label");
        require(language.equals(windows.attached.getContext().getResources().getConfiguration().getLocales().get(0).getLanguage()),
                where + ": newly localized view context");
        require(windows.params.width == dp(context, 72) && windows.params.height == dp(context, 56), where + ": compact dimensions");
        require(windows.params.type == WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY && windows.params.alpha == .8f,
                where + ": overlay type and opacity preserved");
        assertClose(context, windows, language);
    }

    private void assertClose(Context context, WindowSink windows, String language) {
        require(AppLocale.forLanguage(context, language).getString(R.string.close_description)
                        .contentEquals(windows.attached.findViewById(R.id.floating_close).getContentDescription()),
                "Close accessibility description uses " + language);
    }

    @SuppressWarnings("deprecation")
    private static void language(Resources system, Configuration original, DisplayMetrics metrics, String language) {
        Configuration value = new Configuration(original);
        Locale locale = Locale.forLanguageTag(language);
        value.setLocales(new LocaleList(locale)); value.setLayoutDirection(locale);
        system.updateConfiguration(value, metrics);
    }

    private static TextView number(WindowSink windows) { return windows.attached.findViewById(R.id.floating_count); }
    private static int dp(Context context, int value) { return Math.round(value * context.getResources().getDisplayMetrics().density); }
    private void require(boolean value, String message) { if (!value) throw new AssertionError(message); checks++; }

    private static final class Events implements FloatingController.Listener {
        int cycles, closes, ends;
        boolean interacting;
        @Override public void cycle() { cycles++; }
        @Override public void close() { closes++; }
        @Override public void interaction(boolean active) { interacting = active; if (!active) ends++; }
    }

    private static final class FixtureContext extends ContextWrapper {
        private final WindowManager manager;
        int resourceRequests;
        String lastLanguage;
        FixtureContext(Context base, WindowManager manager) { super(base); this.manager = manager; }
        @Override public Object getSystemService(String name) {
            if (Context.WINDOW_SERVICE.equals(name)) return manager;
            // API33+ production normally reads LocaleManager; this fixture deliberately tests
            // its supported process-Resources fallback without changing actual system locales.
            if ("locale".equals(name)) return null;
            return super.getSystemService(name);
        }
        @Override public Context createConfigurationContext(Configuration configuration) {
            resourceRequests++;
            lastLanguage = configuration.getLocales().isEmpty() ? "" : configuration.getLocales().get(0).getLanguage();
            return super.createConfigurationContext(configuration);
        }
    }

    private static final class WindowSink {
        final WindowManager manager;
        View attached;
        WindowManager.LayoutParams params;
        int adds, removes;
        WindowSink(WindowManager readOnlyGeometry) {
            if (readOnlyGeometry == null) throw new AssertionError("Real read-only display geometry is required");
            manager = (WindowManager) Proxy.newProxyInstance(WindowManager.class.getClassLoader(),
                    new Class<?>[]{WindowManager.class}, (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "addView":
                                if (attached != null) throw new AssertionError("Duplicate fake overlay attachment");
                                attached = (View) args[0]; params = (WindowManager.LayoutParams) args[1]; adds++; return null;
                            case "removeView": case "removeViewImmediate":
                                if (attached != args[0]) throw new AssertionError("Removing an unknown fake overlay");
                                attached = null; params = null; removes++; return null;
                            case "updateViewLayout":
                                if (attached != args[0]) throw new AssertionError("Updating an unknown fake overlay");
                                params = (WindowManager.LayoutParams) args[1]; return null;
                            case "getDefaultDisplay": case "getCurrentWindowMetrics": case "getMaximumWindowMetrics":
                                try { return method.invoke(readOnlyGeometry, args); }
                                catch (InvocationTargetException error) { throw error.getCause(); }
                            case "toString": return "DetachedWindowSink";
                            case "hashCode": return System.identityHashCode(proxy);
                            case "equals": return proxy == args[0];
                            default: throw new AssertionError("Unexpected WindowManager operation: " + method.getName());
                        }
                    });
        }
    }
}
