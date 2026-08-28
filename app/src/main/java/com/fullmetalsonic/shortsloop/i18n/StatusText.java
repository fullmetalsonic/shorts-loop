package com.fullmetalsonic.shortsloop.i18n;

import android.content.Context;
import com.fullmetalsonic.shortsloop.R;

/** One display boundary for stable diagnostic codes. Never used to authorize an advance. */
public final class StatusText {
    private StatusText() {}
    public static String text(Context context, String code) {
        if (code != null && code.startsWith("blocked:"))
            return context.getString(R.string.state_blocked, text(context, code.substring(8)));
        return context.getString(resource(code));
    }
    public static int resource(String code) {
        if (code == null) return R.string.state_unknown;
        switch (code) {
            case "tiktok.waiting": return R.string.state_tiktok_waiting;
            case "tiktok.no_progress": return R.string.state_tiktok_no_progress;
            case "tiktok.unsupported": return R.string.state_tiktok_unsupported;
            case "tiktok.paused": return R.string.state_tiktok_paused;
            case "ads.delayed": return R.string.state_ads_delayed;
            case "photo.ready": return R.string.state_photo_ready;
            case "photo.waiting": return R.string.state_photo_waiting;
            case "photo.disabled": return R.string.state_photo_disabled;
            case "photo.whole": return R.string.state_photo_whole;
            case "photo.each": return R.string.state_photo_each;
            case "photo.fallback": return R.string.state_photo_fallback;
            case "photo.index_missing": return R.string.state_photo_index_missing;
            case "photo.confirming": return R.string.state_photo_confirming;
            case "photo.failed": return R.string.state_photo_failed;
            case "photo.rules": return R.string.state_photo_rules;
            case "instagram.waiting": return R.string.state_instagram_waiting;
            case "screen.complex": return R.string.state_screen_complex;
            case "screen.interaction": return R.string.state_screen_interaction;
            case "instagram.mixed": return R.string.state_instagram_mixed;
            case "instagram.single": return R.string.state_instagram_single;
            case "instagram.bar": return R.string.state_instagram_bar;
            case "instagram.identity": return R.string.state_instagram_identity;
            case "playback.refresh": return R.string.state_playback_refresh;
            case "app.selected_wait": return R.string.state_app_selected_wait;
            case "app.unselected": return R.string.state_app_unselected;
            case "app.supported": return R.string.state_app_supported;
            case "live.obstructed": return R.string.state_live_obstructed;
            case "live.refresh": return R.string.state_live_refresh;
            case "live.transition_wait": return R.string.state_live_transition_wait;
            case "live.stabilizing": return R.string.state_live_stabilizing;
            case "youtube.waiting": return R.string.state_youtube_waiting;
            case "shorts.waiting": return R.string.state_shorts_waiting;
            case "playback.waiting": return R.string.state_playback_waiting;
            case "instagram.paused": return R.string.state_instagram_paused;
            case "instagram.no_progress": return R.string.state_instagram_no_progress;
            case "live.waiting": return R.string.state_live_waiting;
            case "ads.waiting": return R.string.state_ads_waiting;
            case "off": return R.string.state_off;
            case "error.query": return R.string.state_error_query;
            case "error.live_settings": return R.string.state_error_live_settings;
            case "error.long_settings": return R.string.state_error_long_settings;
            case "app.select": return R.string.state_app_select;
            case "permission.overlay": return R.string.state_permission_overlay;
            case "playback.start_wait": return R.string.state_playback_start_wait;
            case "error.overlay": return R.string.state_error_overlay;
            case "error.transition_restart": return R.string.state_error_transition_restart;
            case "screen.waiting": return R.string.state_screen_waiting;
            case "ads.confirming": return R.string.state_ads_confirming;
            case "timed.confirming": return R.string.state_timed_confirming;
            case "advance.confirming": return R.string.state_advance_confirming;
            case "live.disabled": return R.string.state_live_disabled;
            case "ads.disabled": return R.string.state_ads_disabled;
            case "timed.checking": return R.string.state_timed_checking;
            case "timed.waiting": return R.string.state_timed_waiting;
            case "playback.next_start": return R.string.state_playback_next_start;
            case "playback.counting": return R.string.state_playback_counting;
            case "error.advance": return R.string.state_error_advance;
            case "live.query_ready": return R.string.state_live_query_ready;
            case "screen.other_window": return R.string.state_screen_other_window;
            case "error.multiple_reels": return R.string.state_error_multiple_reels;
            case "error.ads_action": return R.string.state_error_ads_action;
            case "error.ads_screen": return R.string.state_error_ads_screen;
            case "error.ads_rejected": return R.string.state_error_ads_rejected;
            case "error.floating_obstructs": return R.string.state_error_floating_obstructs;
            case "shorts.confirming": return R.string.state_shorts_confirming;
            case "error.cancelled": return R.string.state_error_cancelled;
            case "error.rejected": return R.string.state_error_rejected;
            case "error.transition": return R.string.state_error_transition;
            case "service.disconnected": return R.string.state_service_disconnected;
            case "visual.waiting": return R.string.state_visual_waiting;
            case "visual.error.unsupported": return R.string.state_visual_error_unsupported;
            case "visual.error.timeout": return R.string.state_visual_error_timeout;
            case "visual.learning": return R.string.state_visual_learning;
            case "estimate.counting": return R.string.state_estimate_counting;
            case "visual.static": return R.string.state_visual_static;
            case "visual.error.repeat_unknown": return R.string.state_visual_error_repeat_unknown;
            case "visual.capture_throttle": return R.string.state_visual_capture_throttle;
            case "visual.error.fallback": return R.string.state_visual_error_fallback;
            case "live.immediate": return R.string.state_live_immediate;
            case "live.delayed": return R.string.state_live_delayed;
            case "live.confirming": return R.string.state_live_confirming;
            case "zero.ads_live": return R.string.state_zero_ads_live;
            case "zero.live": return R.string.state_zero_live;
            case "zero.ads": return R.string.state_zero_ads;
            case "zero.off": return R.string.state_zero_off;
            case "long.checking": return R.string.state_long_checking;
            case "long.confirming": return R.string.state_long_confirming;
            case "restart.waiting": return R.string.state_restart_waiting;
            case "restart.counting": return R.string.state_restart_counting;
            case "visual.error.capture_bounds": return R.string.state_visual_error_capture_bounds;
            case "visual.error.window_changed": return R.string.state_visual_error_window_changed;
            case "visual.error.copy_failed": return R.string.state_visual_error_copy_failed;
            case "visual.error.processing_failed": return R.string.state_visual_error_processing_failed;
            case "visual.error.capture_disconnected": return R.string.state_visual_error_capture_disconnected;
            case "visual.error.secure_window": return R.string.state_visual_error_secure_window;
            case "visual.error.capture_unavailable": return R.string.state_visual_error_capture_unavailable;
            case "visual.error.capture_connection": return R.string.state_visual_error_capture_connection;
            case "timed.missing_action": return R.string.state_timed_missing_action;
            case "timed.invalid_bounds": return R.string.state_timed_invalid_bounds;
            case "timed.request_rejected": return R.string.state_timed_request_rejected;
            case "estimate.missing_action": return R.string.state_estimate_missing_action;
            case "estimate.invalid_bounds": return R.string.state_estimate_invalid_bounds;
            case "estimate.request_rejected": return R.string.state_estimate_request_rejected;
            case "estimate.confirming": return R.string.state_estimate_confirming;
            case "zero.long": return R.string.state_zero_long;
            case "zero.long_live": return R.string.state_zero_long_live;
            case "zero.long_ads": return R.string.state_zero_long_ads;
            case "zero.long_ads_live": return R.string.state_zero_long_ads_live;
            default: return R.string.state_unknown;
        }
    }
}
