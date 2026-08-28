package com.fullmetalsonic.shortsloop.core;

import java.util.Objects;

/** Separate horizontal-slide and vertical-Reel confirmation; never re-arms a failed request. */
public final class PhotoTransition {
    public enum State { IDLE, WAITING, CONFIRMED, FAILED }
    private PhotoReelTracker.Action action = PhotoReelTracker.Action.NONE;
    private String scope, identity, candidate;
    private PhotoReelPolicy.Position position;
    private long started, candidateAt = -1, previous = -1;
    public void begin(PhotoReelTracker.Action action, String scope, String identity,
            PhotoReelPolicy.Position position, long now) {
        if (pending() || action == PhotoReelTracker.Action.NONE || scope == null || scope.isEmpty()
                || identity == null || identity.isEmpty() || position == null
                || (action == PhotoReelTracker.Action.SLIDE && (!position.known() || position.current() >= position.total())))
            throw new IllegalArgumentException("Invalid photo request");
        this.action = action; this.scope = scope; this.identity = identity; this.position = position;
        started = previous = now; candidate = null; candidateAt = -1;
    }
    public State inspect(String scope, String identity, PhotoReelPolicy.Position position, long now) {
        return inspect(scope, identity, position, now, false);
    }
    public State inspect(String scope, String identity, PhotoReelPolicy.Position position, long now, boolean differentPageNode) {
        if (!pending()) return State.IDLE;
        if (now < previous || now - started >= 4500) { reset(); return State.FAILED; }
        if (now - previous > 1200) { candidate = null; candidateAt = -1; }
        previous = now;
        boolean eligible = this.scope.equals(scope) && identity != null && !identity.isEmpty();
        if (action == PhotoReelTracker.Action.SLIDE) {
            eligible &= this.identity.equals(identity) && position != null && position.known()
                    && position.total() == this.position.total() && position.current() == this.position.current() + 1;
        } else eligible &= differentPageNode && !this.identity.equals(identity);
        if (!eligible) { candidate = null; candidateAt = -1; return State.WAITING; }
        String next = identity + (action == PhotoReelTracker.Action.SLIDE ? "|" + position.current() + "/" + position.total() : "");
        if (!Objects.equals(candidate, next)) { candidate = next; candidateAt = now; }
        if (now - candidateAt >= 300 && now - started >= (action == PhotoReelTracker.Action.SLIDE ? 600 : 1200)) {
            reset(); return State.CONFIRMED;
        }
        return State.WAITING;
    }
    public boolean pending() { return action != PhotoReelTracker.Action.NONE; }
    public PhotoReelTracker.Action action() { return action; }
    public void reset() { action = PhotoReelTracker.Action.NONE; scope = identity = candidate = null; position = null; candidateAt = previous = -1; }
}
