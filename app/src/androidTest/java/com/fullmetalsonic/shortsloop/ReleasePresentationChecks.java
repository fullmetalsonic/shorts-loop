package com.fullmetalsonic.shortsloop;

import android.app.Activity;
import android.widget.TextView;

/** Exact rendered labels catch suffixes as well as stale, hardcoded version numbers. */
final class ReleasePresentationChecks {
    static int run(Activity activity) {
        try {
            String version = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
            require(((TextView) activity.findViewById(R.id.installed_version)).getText().toString()
                    .equals("설치 버전 " + version), "Installed version label must be neutral and current");
            require(((TextView) activity.findViewById(R.id.app_version)).getText().toString()
                    .equals("ShortsLoop " + version), "Footer must be neutral and current");
            require(((TextView) activity.findViewById(R.id.visual_assist_toggle)).getText().toString()
                    .contains("시험"), "Experimental feature disclosure must remain visible");
            return 3;
        } catch (android.content.pm.PackageManager.NameNotFoundException error) {
            throw new AssertionError(error);
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
