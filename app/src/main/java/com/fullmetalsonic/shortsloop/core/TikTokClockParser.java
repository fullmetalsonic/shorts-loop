package com.fullmetalsonic.shortsloop.core;

import java.util.regex.Pattern;

/** Optional seconds only from a dedicated seek control's exact elapsed/total clock text. */
public final class TikTokClockParser {
    private static final String TIME = "[0-9]{1,2}:[0-9]{2}(?::[0-9]{2})?";
    private static final Pattern PAIR = Pattern.compile("^" + TIME + "\\s*/\\s*" + TIME + "$");
    private TikTokClockParser() { }
    public record Result(Progress progress, boolean contradictory) { }
    public static Result parse(CharSequence text, CharSequence description, CharSequence state) {
        Progress found = null;
        for (CharSequence value : new CharSequence[]{text, description, state}) {
            String clock = value == null ? "" : value.toString().trim();
            if (!PAIR.matcher(clock).matches()) continue;
            Progress parsed = ProgressParser.parse(clock);
            if (parsed == null || (found != null && (found.duration != parsed.duration || found.position != parsed.position)))
                return new Result(null, true);
            found = parsed;
        }
        return new Result(found, false);
    }
}
