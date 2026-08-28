package com.fullmetalsonic.shortsloop.core;

import java.util.Objects;

/** Prepared, not connected to automation until actual Shorts ad evidence is verified.
 * Request-scoped navigation: a new clock or an event alone cannot prove a new Short. */
public final class YouTubeAdTransition {
    public enum Kind { AD, VIDEO, LIVE }
    public static final class Frame {
        public final String scope, pager, page;
        public final int row;
        public final Kind kind;
        public final Progress progress;
        public Frame(String scope, String pager, String page, int row, Kind kind, Progress progress) {
            this.scope = scope; this.pager = pager; this.page = page;
            this.row = row; this.kind = kind; this.progress = progress;
        }
        public boolean valid() {
            return present(scope) && present(pager) && present(page) && row >= 0 && kind != null
                    && (kind != Kind.VIDEO || progress != null && progress.valid());
        }
    }
    private Frame request, previous;
    private long started, candidateAt, previousAt;
    private final ProgressMotion motion = new ProgressMotion();

    public boolean pending() { return request != null; }
    public void cancel() { request = previous = null; }
    public void begin(Frame frame, long now) {
        if (frame == null || !frame.valid() || frame.kind != Kind.AD || now < 0)
            throw new IllegalArgumentException("A safe indexed YouTube ad is required");
        request = frame; previous = null; started = now;
    }
    public AdvanceGate.State inspect(Frame frame, long now) {
        if (request == null) return AdvanceGate.State.IDLE;
        if (now < started || now - started >= 4500) { cancel(); return AdvanceGate.State.FAILED; }
        if (frame == null || !frame.valid() || !Objects.equals(request.scope, frame.scope)
                || !Objects.equals(request.pager, frame.pager) || Objects.equals(request.page, frame.page)
                || !YouTubePageStepPolicy.next(request.row, frame.row)) {
            previous = null; return AdvanceGate.State.WAITING;
        }
        if (previous == null || !same(previous, frame) || now <= previousAt || now - previousAt > 1500
                || (frame.kind == Kind.VIDEO && (Math.abs(frame.progress.duration - previous.progress.duration) > .5
                    || frame.progress.position < previous.progress.position || !motion.accept(frame.progress.position, now)))) {
            previous = frame; candidateAt = previousAt = now;
            if (frame.kind == Kind.VIDEO) motion.reset(frame.progress.position, now);
            return AdvanceGate.State.WAITING;
        }
        boolean forward = frame.kind != Kind.VIDEO || frame.progress.position - previous.progress.position >= .1;
        previous = frame; previousAt = now;
        if (forward && now - started >= 1200 && now - candidateAt >= 300) {
            cancel(); return AdvanceGate.State.CONFIRMED;
        }
        return AdvanceGate.State.WAITING;
    }
    private static boolean same(Frame a, Frame b) {
        return Objects.equals(a.scope, b.scope) && Objects.equals(a.pager, b.pager)
                && Objects.equals(a.page, b.page) && a.row == b.row && a.kind == b.kind;
    }
    private static boolean present(String s) { return s != null && !s.isEmpty(); }
}
