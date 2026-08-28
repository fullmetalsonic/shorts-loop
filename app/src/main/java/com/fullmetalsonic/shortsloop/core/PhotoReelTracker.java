package com.fullmetalsonic.shortsloop.core;

/** A settled photo page timer. A due action is consumed once until reset/confirmed movement. */
public final class PhotoReelTracker {
    public enum Action { NONE, SLIDE, REEL }
    public record Result(Action action, int remaining, String status) { }
    private String key;
    private long started = -1, previous = -1;
    private boolean consumed;
    public Result observe(String pageKey, PhotoReelPolicy.Position position, int mode,
            int wholeSeconds, int slideSeconds, boolean fallback, long now) {
        if (pageKey == null || pageKey.isEmpty() || position == null || now < 0
                || (!position.known() && !position.missing())) { reset(); return result(-1, "photo.waiting"); }
        boolean each = mode == PhotoReelPolicy.EACH;
        boolean missing = each && !position.known();
        if (missing && !fallback) { reset(); return result(-1, "photo.index_missing"); }
        int delay = PhotoReelPolicy.seconds(each && !missing ? slideSeconds : wholeSeconds);
        String nextKey = pageKey + "|" + mode + "|" + delay + "|" + missing
                + (each ? "|" + position.current() + "/" + position.total() : "");
        if (!nextKey.equals(key) || now < previous || now - previous > 1200) {
            key = nextKey; started = now; consumed = false;
        }
        previous = now;
        // Missing indices are rechecked longer; fallback receives a fresh full delay after qualification.
        long settle = missing ? 900 : 450;
        long elapsed = Math.max(0, now - started - settle);
        int remaining = (int) ((Math.max(0, delay * 1000L - elapsed) + 999) / 1000);
        String status = missing ? "photo.fallback" : each ? "photo.each" : "photo.whole";
        if (!consumed && now - started >= settle && elapsed >= delay * 1000L) {
            consumed = true;
            Action action = each && !missing && position.current() < position.total() ? Action.SLIDE : Action.REEL;
            return new Result(action, 0, status);
        }
        return result(remaining, status);
    }
    private Result result(int remaining, String status) { return new Result(Action.NONE, remaining, status); }
    public boolean active() { return key != null && !consumed; }
    public void reset() { key = null; started = previous = -1; consumed = false; }
}
