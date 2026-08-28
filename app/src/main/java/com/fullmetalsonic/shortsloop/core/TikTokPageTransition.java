package com.fullmetalsonic.shortsloop.core;

import java.util.Objects;

/** Strict vertical-feed confirmation, including positively recognized clockless/photo/ad destinations. */
public final class TikTokPageTransition {
    private static final long TIMEOUT_MS = 4500, REQUEST_SETTLE_MS = 1200, CANDIDATE_SETTLE_MS = 300, MAX_GAP_MS = 1500;
    private static final double MAX_FORWARD_DELTA = .16;
    public static final class Frame {
        public final String scope, pager, page, media;
        public final int index;
        /** -1 is allowed only when the caller has positively identified a safe clockless/special page. */
        public final double fraction;
        public Frame(String scope, String pager, String page, String media, int index, double fraction) {
            this.scope = scope; this.pager = pager; this.page = page; this.media = media;
            this.index = index; this.fraction = fraction;
        }
        boolean valid() {
            return present(scope) && present(pager) && present(page) && present(media) && index >= -1
                    && Double.isFinite(fraction) && (fraction == -1 || fraction >= 0 && fraction <= 1);
        }
    }
    private Frame request, previous;
    private long started, candidateAt, previousAt;
    public boolean pending() { return request != null; }
    public void cancel() { request = previous = null; }
    public void begin(Frame frame, long now) {
        if (frame == null || !frame.valid()) throw new IllegalArgumentException("Known safe TikTok source required");
        request = frame; previous = null; started = now;
    }
    public AdvanceGate.State inspect(Frame frame, long now) {
        if (request == null) return AdvanceGate.State.IDLE;
        if (now < started || now - started >= TIMEOUT_MS) { cancel(); return AdvanceGate.State.FAILED; }
        boolean candidate = frame != null && frame.valid() && Objects.equals(request.scope, frame.scope)
                && Objects.equals(request.pager, frame.pager) && !Objects.equals(request.page, frame.page)
                && !Objects.equals(request.media, frame.media)
                && (request.index < 0 || (long) frame.index == (long) request.index + 1);
        if (!candidate) { previous = null; return AdvanceGate.State.WAITING; }
        if (previous == null || !sameCandidate(previous, frame) || now <= previousAt || now - previousAt > MAX_GAP_MS
                || (frame.fraction >= 0 && (frame.fraction < previous.fraction
                    || frame.fraction - previous.fraction > MAX_FORWARD_DELTA))) {
            previous = frame; candidateAt = previousAt = now;
            return AdvanceGate.State.WAITING;
        }
        boolean observed = frame.fraction == -1 || frame.fraction > previous.fraction;
        previous = frame; previousAt = now;
        if (observed && now - started >= REQUEST_SETTLE_MS && now - candidateAt >= CANDIDATE_SETTLE_MS) {
            cancel(); return AdvanceGate.State.CONFIRMED;
        }
        return AdvanceGate.State.WAITING;
    }
    private static boolean sameCandidate(Frame a, Frame b) {
        return Objects.equals(a.scope, b.scope) && Objects.equals(a.pager, b.pager)
                && Objects.equals(a.page, b.page) && Objects.equals(a.media, b.media)
                && a.index == b.index && (a.fraction == -1) == (b.fraction == -1);
    }
    private static boolean present(String value) { return value != null && !value.isEmpty(); }
}
