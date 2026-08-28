package com.fullmetalsonic.shortsloop.core;

import java.util.Objects;

/** No duration guesses and no event-only confirmation for normalized players. */
public final class NormalizedTransition {
    public static final class Frame {
        public final String scope, pager, page, media;
        public final int index;
        public final double fraction;
        public Frame(String scope, String pager, String page, String media, int index, double fraction) {
            this.scope = scope; this.pager = pager; this.page = page; this.media = media;
            this.index = index; this.fraction = fraction;
        }
        boolean valid() { return present(scope) && present(pager) && present(page) && present(media)
                && Double.isFinite(fraction) && fraction >= 0 && fraction <= 1; }
    }
    private Frame request, previous;
    private long started, candidateAt, previousAt;
    public boolean pending() { return request != null; }
    public void cancel() { request = previous = null; }
    public void begin(Frame frame, long now) {
        if (frame == null || !frame.valid()) throw new IllegalArgumentException("Valid normalized page required");
        request = frame; previous = null; started = now;
    }
    public AdvanceGate.State inspect(Frame frame, long now) {
        if (request == null) return AdvanceGate.State.IDLE;
        if (now < started || now - started >= 4500) { cancel(); return AdvanceGate.State.FAILED; }
        boolean candidate = frame != null && frame.valid() && Objects.equals(request.scope, frame.scope)
                && Objects.equals(request.pager, frame.pager) && !Objects.equals(request.page, frame.page)
                && !Objects.equals(request.media, frame.media)
                && (request.index < 0 || frame.index == request.index + 1);
        if (!candidate) { previous = null; return AdvanceGate.State.WAITING; }
        if (previous == null || !samePage(previous, frame) || now <= previousAt || now - previousAt > 1500
                || frame.fraction < previous.fraction || frame.fraction - previous.fraction > .25) {
            previous = frame; candidateAt = previousAt = now; return AdvanceGate.State.WAITING;
        }
        boolean moving = frame.fraction > previous.fraction;
        previous = frame; previousAt = now;
        if (moving && now - started >= 1200 && now - candidateAt >= 300) {
            cancel(); return AdvanceGate.State.CONFIRMED;
        }
        return AdvanceGate.State.WAITING;
    }
    private static boolean samePage(Frame a, Frame b) {
        return Objects.equals(a.page, b.page) && Objects.equals(a.media, b.media) && a.index == b.index;
    }
    private static boolean present(String value) { return value != null && !value.isEmpty(); }
}
