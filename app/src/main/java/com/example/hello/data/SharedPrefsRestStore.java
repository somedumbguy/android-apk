package com.example.hello.data;

import android.content.SharedPreferences;
import com.example.hello.domain.history.RestStore;
import java.util.Optional;

/** Keeps the running rest timer on the phone. */
public final class SharedPrefsRestStore implements RestStore {
    private static final String ENDS = "endsAt";
    private static final String TOTAL = "total";
    private static final String NEXT = "next";
    private final SharedPreferences prefs;

    public SharedPrefsRestStore(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    @Override
    public Optional<Rest> load() {
        if (!prefs.contains(ENDS)) return Optional.empty();
        return Optional.of(new Rest(prefs.getLong(ENDS, 0), prefs.getLong(TOTAL, 0), prefs.getString(NEXT, "")));
    }

    @Override
    public void save(Rest rest) {
        prefs.edit()
                .putLong(ENDS, rest.endsAtMillis)
                .putLong(TOTAL, rest.totalMs)
                .putString(NEXT, rest.next)
                .apply();
    }

    @Override
    public void clear() {
        prefs.edit().clear().apply();
    }
}
