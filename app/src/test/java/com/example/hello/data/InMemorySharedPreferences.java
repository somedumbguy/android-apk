package com.example.hello.data;

import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** A SharedPreferences held in memory, so storage code can be tested on the JVM. */
final class InMemorySharedPreferences implements SharedPreferences {
    private final Map<String, Object> values = new HashMap<>();

    @Override public Map<String, ?> getAll() { return new HashMap<>(values); }
    @Override public String getString(String k, String d) { return values.containsKey(k) ? (String) values.get(k) : d; }
    @SuppressWarnings("unchecked")
    @Override public Set<String> getStringSet(String k, Set<String> d) {
        return values.containsKey(k) ? (Set<String>) values.get(k) : d;
    }
    @Override public int getInt(String k, int d) { return values.containsKey(k) ? (Integer) values.get(k) : d; }
    @Override public long getLong(String k, long d) { return values.containsKey(k) ? (Long) values.get(k) : d; }
    @Override public float getFloat(String k, float d) { return values.containsKey(k) ? (Float) values.get(k) : d; }
    @Override public boolean getBoolean(String k, boolean d) { return values.containsKey(k) ? (Boolean) values.get(k) : d; }
    @Override public boolean contains(String k) { return values.containsKey(k); }
    @Override public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener l) {}
    @Override public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener l) {}

    @Override public Editor edit() {
        return new Editor() {
            private final Map<String, Object> pending = new HashMap<>();
            private final Set<String> removed = new HashSet<>();
            private boolean clear;

            @Override public Editor putString(String k, String v) { pending.put(k, v); return this; }
            @Override public Editor putStringSet(String k, Set<String> v) { pending.put(k, v); return this; }
            @Override public Editor putInt(String k, int v) { pending.put(k, v); return this; }
            @Override public Editor putLong(String k, long v) { pending.put(k, v); return this; }
            @Override public Editor putFloat(String k, float v) { pending.put(k, v); return this; }
            @Override public Editor putBoolean(String k, boolean v) { pending.put(k, v); return this; }
            @Override public Editor remove(String k) { removed.add(k); return this; }
            @Override public Editor clear() { clear = true; return this; }
            @Override public boolean commit() { apply(); return true; }

            @Override public void apply() {
                if (clear) values.clear();
                for (String k : removed) values.remove(k);
                values.putAll(pending);
            }
        };
    }
}
