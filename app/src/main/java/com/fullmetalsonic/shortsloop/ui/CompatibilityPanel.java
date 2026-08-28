package com.fullmetalsonic.shortsloop.ui;

import android.content.Context;
import android.os.Build;
import android.widget.LinearLayout;
import com.fullmetalsonic.shortsloop.R;
import com.fullmetalsonic.shortsloop.core.FeatureSupportPolicy;

/** Short OS explanation; capability is not an assertion of host-app compatibility. */
public final class CompatibilityPanel extends LinearLayout {
    public CompatibilityPanel(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setPadding(0, UiTheme.dp(context, 12), 0, UiTheme.dp(context, 8));
        addView(UiTheme.text(context, context.getString(R.string.compat_heading, Build.VERSION.RELEASE), 16, UiTheme.TEXT, true));
        addView(UiTheme.text(context, context.getString(R.string.compat_basics), 13, UiTheme.MUTED, false));
        addView(UiTheme.text(context, context.getString(FeatureSupportPolicy.tileAddRequest(Build.VERSION.SDK_INT)
                ? R.string.compat_tile_request : R.string.compat_tile_manual), 13, UiTheme.MUTED, false));
        addView(UiTheme.text(context, context.getString(FeatureSupportPolicy.visualCapture(Build.VERSION.SDK_INT)
                ? R.string.compat_visual_supported : R.string.compat_visual_unsupported), 13, UiTheme.MUTED, false));
    }

    public static String instagramReason(Context context, boolean installed, boolean selected) {
        return context.getString(!installed ? R.string.compat_instagram_missing
                : !selected ? R.string.compat_instagram_select : R.string.compat_instagram_ready);
    }
    public static String visualReason(Context context, int sdk, boolean installed, boolean selected, boolean saved) {
        FeatureSupportPolicy.Availability state = FeatureSupportPolicy.visualAvailability(sdk, installed, selected);
        if (state == FeatureSupportPolicy.Availability.ANDROID_TOO_OLD) {
            return context.getString(R.string.compat_visual_unsupported)
                    + (saved ? "\n" + context.getString(R.string.compat_saved_inactive) : "");
        }
        return state == FeatureSupportPolicy.Availability.AVAILABLE ? context.getString(R.string.compat_visual_supported)
                : instagramReason(context, installed, selected);
    }
}
