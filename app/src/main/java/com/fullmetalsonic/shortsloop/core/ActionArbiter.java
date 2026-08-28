package com.fullmetalsonic.shortsloop.core;

/** One OS input at a time. No coordinates or content; callbacks use immutable lease IDs. */
public final class ActionArbiter {
    private String owner, waiting;
    private long lease, until, waitingUntil;
    public boolean available(String host, long now) {
        if (owner != null || now < until) {
            if (!host.equals(owner) && (waiting == null || now >= waitingUntil)) { waiting = host; waitingUntil = now + 2000; }
            return false;
        }
        return waiting == null || waiting.equals(host) || now >= waitingUntil;
    }
    public long acquire(String host, long now) {
        if (!available(host, now)) return -1;
        owner = host;
        if (host.equals(waiting)) waiting = null;
        return ++lease;
    }
    public void release(long token, long now) {
        if (owner == null || token != lease) return;
        owner = null; until = now + 180;
    }
    public void cancelWaiting(String host) { if (host.equals(waiting)) waiting = null; }
    public boolean busy() { return owner != null; }
}
