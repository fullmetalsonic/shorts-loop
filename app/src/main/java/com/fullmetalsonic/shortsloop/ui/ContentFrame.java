package com.fullmetalsonic.shortsloop.ui;

import android.content.Context;
import android.widget.FrameLayout;

/** Size the centered settings column from its actual viewport, not the whole display. */
final class ContentFrame extends FrameLayout {
    ContentFrame(Context context) { super(context); }
    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        if (getChildCount() > 0 && MeasureSpec.getMode(widthSpec) != MeasureSpec.UNSPECIFIED) {
            int available = Math.max(0, MeasureSpec.getSize(widthSpec) - UiTheme.dp(getContext(), 32));
            LayoutParams params = (LayoutParams)getChildAt(0).getLayoutParams();
            params.width = Math.min(UiTheme.dp(getContext(), 600), available);
        }
        super.onMeasure(widthSpec, heightSpec);
    }
}
