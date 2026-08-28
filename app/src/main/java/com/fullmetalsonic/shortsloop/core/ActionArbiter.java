package com.fullmetalsonic.shortsloop.core;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** One OS input at a time, called on the service event loop. No coordinates or content. */
public final class ActionArbiter {
    private static final int MAX_WAITERS = 3;
    private static final long WAIT_MS = 2000, COOLDOWN_MS = 180;
    private final LinkedHashMap<String, Long> waiting = new LinkedHashMap<>();
    // Host names establish a bounded turn order, never an executable saved intention.
    private final List<String> turnOrder = new ArrayList<>();
    private final List<String> survivingWaiters = new ArrayList<>();
    private String owner, lastGranted;
    private long lease, until;
    private boolean expiredOtherBatch;
    private long freshUntil = -1;

    /** A blocked caller joins once; polling never extends its reservation or changes order. */
    public boolean available(String host, long now) {
        if (host == null || host.isEmpty() || now < 0) return false;
        expire(now);
        if (!turnOrder.contains(host) && turnOrder.size() < MAX_WAITERS) turnOrder.add(host);
        if (host.equals(owner)) return false;
        if (owner == null && now >= until && waiting.isEmpty() && !expiredOtherBatch) return true;
        if (!waiting.containsKey(host)) {
            if (waiting.size() >= MAX_WAITERS) return false;
            waiting.put(host, now);
        }
        if (owner != null || now < until) return false;
        if (expiredOtherBatch) {
            // Reservations still alive when expiry began retain FIFO priority.
            for (String candidate : waiting.keySet()) {
                if (survivingWaiters.contains(candidate)) return candidate.equals(host);
            }
            // Only an old owner's fresh attempt with no fresh peer needs a collection window.
            // Its fixed deadline cannot be extended by repeated polls or cancellation.
            if (freshUntil < 0 && waiting.size() == 1 && host.equals(lastGranted)) freshUntil = now + 300;
            if (freshUntil >= 0 && now < freshUntil) return false;
            return nextFreshHost().equals(host);
        }
        return waiting.keySet().iterator().next().equals(host);
    }

    public long acquire(String host, long now) {
        if (!available(host, now)) return -1;
        owner = host;
        lastGranted = host; expiredOtherBatch = false; freshUntil = -1; survivingWaiters.clear();
        waiting.remove(host);
        return ++lease;
    }

    public void release(long token, long now) {
        if (owner == null || token != lease) return;
        owner = null; until = now + COOLDOWN_MS;
    }

    public void cancelWaiting(String host) { waiting.remove(host); survivingWaiters.remove(host); }
    public boolean busy() { return owner != null; }

    private void expire(long now) {
        boolean expiredOther = false;
        Iterator<Map.Entry<String, Long>> entries = waiting.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, Long> entry = entries.next();
            long joined = entry.getValue();
            if (now < joined || now - joined >= WAIT_MS) {
                expiredOther |= !entry.getKey().equals(lastGranted);
                survivingWaiters.remove(entry.getKey());
                entries.remove();
            }
        }
        if (expiredOther && !expiredOtherBatch) {
            expiredOtherBatch = true;
            for (String host : waiting.keySet()) if (!host.equals(lastGranted)) survivingWaiters.add(host);
        }
    }

    private String nextFreshHost() {
        int previous = turnOrder.indexOf(lastGranted);
        for (int offset = 1; offset <= turnOrder.size(); offset++) {
            String host = turnOrder.get((previous + offset) % turnOrder.size());
            if (waiting.containsKey(host)) return host;
        }
        return waiting.keySet().iterator().next();
    }
}
