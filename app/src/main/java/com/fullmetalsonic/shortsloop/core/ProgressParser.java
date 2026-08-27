package com.fullmetalsonic.shortsloop.core;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses only known time descriptions; unknown UI formats fail closed. */
public final class ProgressParser {
    private static final Pattern KO = Pattern.compile("^(?:(\\d+)시간\\s*)?(?:(\\d+)분\\s*)?(?:(\\d+)초)?$");
    private static final Pattern EN = Pattern.compile("(?:(\\d+)\\s*hours?\\s*)?(?:(\\d+)\\s*minutes?\\s*)?(?:(\\d+)\\s*seconds?)?");
    private static final Pattern CLOCK = Pattern.compile("^\\d{1,3}:\\d{2}(?::\\d{2})?$");
    private ProgressParser() {}
    public static Progress parse(CharSequence value) {
        if (value == null) return null;
        String text = value.toString().trim().toLowerCase(Locale.ROOT).replace(',', ' ').replaceAll("\\s+", " ");
        String[] parts;
        double position, duration;
        if (text.contains(" 중 ")) {
            parts = text.split(" 중 ", -1);
            if (parts.length != 2) return null;
            duration = time(parts[0]); position = time(parts[1]);
        } else if (text.contains(" of ")) {
            parts = text.split(" of ", -1);
            if (parts.length != 2) return null;
            position = time(parts[0]); duration = time(parts[1]);
        } else if (text.contains("/")) {
            parts = text.split("/", -1);
            if (parts.length != 2) return null;
            position = time(parts[0]); duration = time(parts[1]);
        } else return null;
        Progress progress = new Progress(position, duration);
        return progress.valid() ? progress : null;
    }
    private static double time(String text) {
        text = text.trim();
        try {
            if (CLOCK.matcher(text).matches()) {
                String[] pieces = text.split(":");
                double total = 0;
                for (int i = 0; i < pieces.length; i++) {
                    int part = Integer.parseInt(pieces[i]);
                    if (i > 0 && part >= 60) return -1;
                    total = total * 60 + part;
                }
                return total;
            }
            Matcher match = KO.matcher(text);
            if (!match.matches()) match = EN.matcher(text);
            if (!match.matches() || text.isEmpty()) return -1;
            double seconds = 0;
            boolean found = false;
            for (int i = 1; i <= 3; i++) if (match.group(i) != null) {
                found = true;
                seconds += Double.parseDouble(match.group(i)) * (i == 1 ? 3600 : i == 2 ? 60 : 1);
            }
            return found ? seconds : -1;
        } catch (NumberFormatException ignored) { return -1; }
    }
}
