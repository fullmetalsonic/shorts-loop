package com.fullmetalsonic.shortsloop.core;

/** Read-only recovery after an ordinary advance timeout. Never counts an unseen play. */
public final class PlaybackRestart {
    public static final String WAITING = "restart.waiting";
    public static final String COUNTING = "restart.counting";
    public static final class Start {
        public final Progress progress;
        public final long at;
        private Start(Progress progress, long at) { this.progress = progress; this.at = at; }
    }
    private boolean active;
    private String host = "", key = "";
    private int window = -1;
    private Progress first;
    private long firstAt, lastAt;
    private double lastPosition;
    private final ProgressMotion motion = new ProgressMotion();

    public static boolean ordinaryRequest(boolean ad, boolean live, boolean timed, boolean visual, boolean longVideo) {
        return !ad && !live && !timed && !visual && !longVideo;
    }
    public void begin(String host, int window) {
        cancel(); this.host = host; this.window = window; active = true;
    }
    public boolean active() { return active; }
    public boolean accepts(String observedHost, int observedWindow) {
        return active && host != null && !host.isEmpty() && host.equals(observedHost)
                && window >= 0 && window == observedWindow;
    }
    public void cancel() { active = false; host = ""; window = -1; suspend(); }
    /** Lose evidence, not the recovery guard, when a banner/menu interrupts observation. */
    public void suspend() { first = null; key = ""; firstAt = lastAt = 0; }

    public Start observe(Progress progress, String pageKey, long now) {
        if (!active || progress == null || !progress.valid() || pageKey == null || pageKey.isEmpty()) {
            suspend(); return null;
        }
        if (first != null && (!key.equals(pageKey) || Math.abs(first.duration - progress.duration) > 0.5
                || now <= lastAt || now - lastAt > 3000 || now - firstAt > 3000
                || progress.position < lastPosition || !motion.accept(progress.position, now))) suspend();
        if (first == null) {
            // A single zero/stale sample is not enough; require a subsequent plausible forward update.
            if (progress.position <= Math.min(1, progress.duration * 0.1)) {
                first = progress; key = pageKey; firstAt = now;
                motion.reset(progress.position, now);
            }
        } else if (now - firstAt >= 300 && progress.position - first.position >= 0.1) {
            Start start = new Start(first, firstAt);
            cancel(); return start;
        }
        lastAt = now; lastPosition = progress.position;
        return null;
    }
}
