package com.fullmetalsonic.shortsloop;

import android.app.Activity;
import android.widget.TextView;

/** Exact rendered labels catch suffixes as well as stale, hardcoded version numbers. */
final class ReleasePresentationChecks {
    static int run(Activity activity) {
        try {
            String version = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
            require(((TextView) activity.findViewById(R.id.installed_version)).getText().toString()
                    .equals(activity.getString(R.string.installed_version, version)), "Installed version label must be neutral and current");
            require(((TextView) activity.findViewById(R.id.app_version)).getText().toString()
                    .equals(activity.getString(R.string.app_version, version)), "Footer must be neutral and current");
            require(((TextView) activity.findViewById(R.id.visual_assist_toggle)).getText().toString()
                    .equals(activity.getString(R.string.ui_visual_toggle)), "Experimental feature disclosure must remain visible");
            return 3;
        } catch (android.content.pm.PackageManager.NameNotFoundException error) {
            throw new AssertionError(error);
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
