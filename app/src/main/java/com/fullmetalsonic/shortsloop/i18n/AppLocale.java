package com.fullmetalsonic.shortsloop.i18n;

import android.app.LocaleManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import java.util.Locale;

/** Display-only contexts. Never changes global locale, persisted settings or host apps. */
public final class AppLocale {
    private AppLocale() {}
    public static String language(Context context) {
        LocaleList locales = Resources.getSystem().getConfiguration().getLocales();
        if (Build.VERSION.SDK_INT >= 33) {
            LocaleManager manager = context.getSystemService(LocaleManager.class);
            if (manager != null) locales = manager.getSystemLocales();
        }
        return LanguagePolicy.select(locales.isEmpty() ? null : locales.get(0).getLanguage());
    }
    public static Context wrap(Context base) { return forLanguage(base, language(base)); }
    @android.annotation.SuppressLint("AppBundleLocaleChanges") // Universal GitHub APK contains both languages; no Play language splits.
    public static Context forLanguage(Context base, String firstLanguage) {
        Locale locale = Locale.forLanguageTag(LanguagePolicy.select(firstLanguage));
        // Only override language; later density, rotation and font-scale changes still propagate.
        Configuration override = new Configuration();
        override.setLocales(new LocaleList(locale));
        override.setLayoutDirection(locale);
        return base.createConfigurationContext(override);
    }
}
