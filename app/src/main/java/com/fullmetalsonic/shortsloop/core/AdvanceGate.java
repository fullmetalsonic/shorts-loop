package com.fullmetalsonic.shortsloop.core;

import java.util.Objects;

public final class AdvanceGate {
    public enum State { IDLE, WAITING, CONFIRMED, FAILED }
    private boolean pending;
    private long started;
    private String identity;
    private double duration;
    private boolean pageChanged;
    private String liveCandidate;
    private long liveCandidateAt;
    private String longCandidate;
    private double longCandidateDuration;
    private Progress longPrevious;
    private long longCandidateAt = -1, longPreviousAt = -1;
    private final ProgressMotion longMotion = new ProgressMotion();
    public void begin(String identity, double duration, long now) {
        this.identity = identity; this.duration = duration; started = now; pending = true; pageChanged = false;
        liveCandidate = null; liveCandidateAt = -1;
        resetLongCandidate();
    }
    public void pageChanged() { if (pending) pageChanged = true; }
    public void cancel() {
        pending = false; pageChanged = false; liveCandidate = null; liveCandidateAt = -1;
        resetLongCandidate();
    }
    public boolean pending() { return pending; }
    public String transitionDiagnostic(String newIdentity, long now) {
        return "ageMs=" + (now - started) + " identityPresent=" + (newIdentity != null && !newIdentity.isEmpty())
                + " identityDifferent=" + !Objects.equals(identity, newIdentity)
                + " candidateMs=" + (liveCandidateAt < 0 ? -1 : now - liveCandidateAt)
                + " requestedDuration=" + duration
                + " longCandidateMs=" + (longCandidateAt < 0 ? -1 : now - longCandidateAt)
                + " longCandidateDuration=" + longCandidateDuration;
    }
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
    /** Live-to-live needs an independently observed pager-index change as well as a stable different node key.
     * A rebuilt accessibility subtree or changing CTA alone must never acknowledge another swipe.
     */
    public State inspectLivePage(String newIdentity, long now) {
        if (!pending) return State.IDLE;
        if (now - started >= 4500) { cancel(); return State.FAILED; }
        boolean different = identity != null && !identity.isEmpty() && newIdentity != null && !newIdentity.isEmpty()
                && !Objects.equals(identity, newIdentity);
        if (!pageChanged || !different) { liveCandidate = null; liveCandidateAt = -1; return State.WAITING; }
        if (!newIdentity.equals(liveCandidate)) { liveCandidate = newIdentity; liveCandidateAt = now; }
        if (now - started >= 1200 && now - liveCandidateAt >= 300) { cancel(); return State.CONFIRMED; }
        return State.WAITING;
    }
    /** Duration filtering must not acknowledge the same page merely because its clock changed. */
    public State inspectStableRecognizedPage(String newIdentity, long now) {
        if (!pending) return State.IDLE;
        if (now - started >= 4500) { cancel(); return State.FAILED; }
        boolean different = identity != null && !identity.isEmpty() && newIdentity != null && !newIdentity.isEmpty()
                && !Objects.equals(identity, newIdentity);
        if (!different) { liveCandidate = null; liveCandidateAt = -1; return State.WAITING; }
        if (!newIdentity.equals(liveCandidate)) { liveCandidate = newIdentity; liveCandidateAt = now; }
        if (now - started >= 1200 && now - liveCandidateAt >= 300) { cancel(); return State.CONFIRMED; }
        return State.WAITING;
    }
    /**
     * YouTube can expose identical generic text on different ordinary pages.
     * The fallback therefore needs an independently verified, request-fresh pager
     * change AND a stable different duration with fresh, plausible forward motion.
     * The caller verifies the event's source pager, window, time and index; the
     * ordinary pageChanged flag deliberately cannot supply that evidence here.
     */
    public State inspectLongPage(String newIdentity, Progress progress, boolean verifiedPagerChange, long now) {
        // Retain existing identity confirmation, including clockless destinations,
        // and let its deadline win before considering any fallback evidence.
        State identityState = inspectStableRecognizedPage(newIdentity, now);
        if (identityState != State.WAITING) return identityState;
        if (!verifiedPagerChange || identity == null || identity.isEmpty()
                || newIdentity == null || newIdentity.isEmpty()
                || !Double.isFinite(duration) || duration < 3 || duration > 3600
                || progress == null || !progress.valid() || Math.abs(duration - progress.duration) <= 0.5) {
            resetLongCandidate(); return State.WAITING;
        }
        return inspectProgressCandidate(newIdentity, progress, now);
    }
    /**
     * Metadata can change when one title/audio element temporarily disappears.
     * A different supplemental key therefore also needs independent pager or
     * duration evidence, followed by stable, actually moving playback samples.
     */
    public State inspectContentPage(String newIdentity, Progress progress, boolean verifiedPagerChange, long now) {
        if (!pending) return State.IDLE;
        if (now - started >= 4500) { cancel(); return State.FAILED; }
        boolean different = identity != null && !identity.isEmpty() && newIdentity != null && !newIdentity.isEmpty()
                && !Objects.equals(identity, newIdentity);
        boolean differentDuration = Double.isFinite(duration) && duration >= 3 && duration <= 3600
                && progress != null && progress.valid() && Math.abs(duration - progress.duration) > 0.5;
        if (!different || progress == null || !progress.valid() || !(verifiedPagerChange || differentDuration)) {
            resetLongCandidate(); return State.WAITING;
        }
        return inspectProgressCandidate(newIdentity, progress, now);
    }
    private State inspectProgressCandidate(String newIdentity, Progress progress, long now) {
        if (longPrevious == null || !newIdentity.equals(longCandidate)
                || Math.abs(longCandidateDuration - progress.duration) > 0.5
                || now <= longPreviousAt || now - longPreviousAt > 1500
                || progress.position < longPrevious.position || !longMotion.accept(progress.position, now)) {
            longCandidate = newIdentity; longCandidateDuration = progress.duration;
            longCandidateAt = longPreviousAt = now; longPrevious = progress;
            longMotion.reset(progress.position, now);
            return State.WAITING;
        }
        boolean moving = progress.position - longPrevious.position >= 0.1;
        longPrevious = progress; longPreviousAt = now;
        if (now - started >= 1200 && now - longCandidateAt >= 300 && moving) {
            cancel(); return State.CONFIRMED;
        }
        return State.WAITING;
    }
    private void resetLongCandidate() {
        longCandidate = null; longCandidateDuration = -1; longPrevious = null;
        longCandidateAt = longPreviousAt = -1;
    }
}
