package com.fullmetalsonic.shortsloop.data;

import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Test double of the SharedPreferences interface; does not emulate Android disk I/O. */
final class MemoryPreferences implements SharedPreferences {
    private final Map<String, Object> values = new HashMap<>();
    private final Set<OnSharedPreferenceChangeListener> listeners = new HashSet<>();
    final List<Map<String, ?>> writes = new ArrayList<>();

    MemoryPreferences() {}
    MemoryPreferences(Map<String, ?> seed) { values.putAll(seed); }

    @Override public Map<String, ?> getAll() { return new HashMap<>(values); }
    @Override public String getString(String key, String fallback) { return values.containsKey(key) ? (String) values.get(key) : fallback; }
    @Override @SuppressWarnings("unchecked") public Set<String> getStringSet(String key, Set<String> fallback) {
        return values.containsKey(key) ? new HashSet<>((Set<String>) values.get(key)) : fallback;
    }
    @Override public int getInt(String key, int fallback) { return values.containsKey(key) ? (Integer) values.get(key) : fallback; }
    @Override public long getLong(String key, long fallback) { return values.containsKey(key) ? (Long) values.get(key) : fallback; }
    @Override public float getFloat(String key, float fallback) { return values.containsKey(key) ? (Float) values.get(key) : fallback; }
    @Override public boolean getBoolean(String key, boolean fallback) { return values.containsKey(key) ? (Boolean) values.get(key) : fallback; }
    @Override public boolean contains(String key) { return values.containsKey(key); }
    @Override public Editor edit() { return new MemoryEditor(); }
    @Override public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) { listeners.add(listener); }
    @Override public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) { listeners.remove(listener); }

    private final class MemoryEditor implements Editor {
        private final Object removed = new Object();
        private final Map<String, Object> pending = new LinkedHashMap<>();
        private boolean clear;

        @Override public Editor putString(String key, String value) { pending.put(key, value == null ? removed : value); return this; }
        @Override public Editor putStringSet(String key, Set<String> value) { pending.put(key, value == null ? removed : new HashSet<>(value)); return this; }
        @Override public Editor putInt(String key, int value) { pending.put(key, value); return this; }
        @Override public Editor putLong(String key, long value) { pending.put(key, value); return this; }
        @Override public Editor putFloat(String key, float value) { pending.put(key, value); return this; }
        @Override public Editor putBoolean(String key, boolean value) { pending.put(key, value); return this; }
        @Override public Editor remove(String key) { pending.put(key, removed); return this; }
        @Override public Editor clear() { clear = true; return this; }
        @Override public void apply() { commit(); }
        @Override public boolean commit() {
            Map<String, Object> previous = new HashMap<>(values);
            if (clear) values.clear();
            for (Map.Entry<String, Object> entry : pending.entrySet()) {
                if (entry.getValue() == removed) values.remove(entry.getKey());
                else values.put(entry.getKey(), entry.getValue());
            }
            writes.add(new HashMap<>(values));
            Set<String> allKeys = new HashSet<>(previous.keySet());
            allKeys.addAll(values.keySet());
            Set<OnSharedPreferenceChangeListener> observers = new HashSet<>(listeners);
            // Observers see the complete editor update, not a partially replaced N/M pair.
            for (String key : allKeys) {
                if (!Objects.equals(previous.get(key), values.get(key))) {
                    for (OnSharedPreferenceChangeListener observer : observers) observer.onSharedPreferenceChanged(MemoryPreferences.this, key);
                }
            }
            pending.clear();
            clear = false;
            return true;
        }
    }
}
