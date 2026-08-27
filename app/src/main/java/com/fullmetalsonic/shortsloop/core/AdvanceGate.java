package com.fullmetalsonic.shortsloop.core;

import java.util.Objects;

public final class AdvanceGate {
    public enum State { IDLE, WAITING, CONFIRMED, FAILED }
    private boolean pending;
    private long started;
    private String identity;
    private double duration;
    private boolean pageChanged;
    public void begin(String identity, double duration, long now) {
        this.identity = identity; this.duration = duration; started = now; pending = true; pageChanged = false;
    }
    public void pageChanged() { if (pending) pageChanged = true; }
    public void cancel() { pending = false; pageChanged = false; }
    public boolean pending() { return pending; }
    /** A non-terminal UI change must fail, not silently forget an in-flight request. */
    public State interrupt() {
        boolean wasPending = pending;
        cancel();
        return wasPending ? State.FAILED : State.IDLE;
    }
    public State inspect(String newIdentity, double newDuration, long now) {
        if (!pending) return State.IDLE;
        if (now - started < 1200) return State.WAITING;
        if (now - started >= 4500) { cancel(); return State.FAILED; }
        boolean different = identity != null && !identity.isEmpty() && newIdentity != null && !newIdentity.isEmpty()
                && !Objects.equals(identity, newIdentity);
        if (newDuration >= 3 && (pageChanged || different || Math.abs(duration - newDuration) > 0.5)) {
            cancel(); return State.CONFIRMED;
        }
        return State.WAITING;
    }
    public State unavailable(long now) { return inspect("", -1, now); }
    /** A structurally identified page can confirm a change without a playback clock.
     * Never use pageChanged alone here: a late previous scroll event is not proof
     * that a second advertisement (all ads share one identity) was skipped.
     */
    public State inspectRecognizedPage(String newIdentity, long now) {
        if (!pending) return State.IDLE;
        if (now - started < 1200) return State.WAITING;
        if (now - started >= 4500) { cancel(); return State.FAILED; }
        if (identity != null && !identity.isEmpty() && newIdentity != null && !newIdentity.isEmpty()
                && !Objects.equals(identity, newIdentity)) {
            cancel(); return State.CONFIRMED;
        }
        return State.WAITING;
    }
}
