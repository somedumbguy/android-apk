package com.example.hello.data;

import android.content.SharedPreferences;
import com.example.hello.domain.history.ActiveWorkoutStore;
import java.util.Optional;

/**
 * Keeps the in-progress workout on the phone. apply() writes in the background, and Android
 * finishes pending writes when the app is backgrounded, which is when it may be closed.
 */
public final class SharedPrefsActiveWorkoutStore implements ActiveWorkoutStore {
    private static final String KEY = "snapshot";
    private final SharedPreferences prefs;

    public SharedPrefsActiveWorkoutStore(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    @Override
    public Optional<String> load() {
        return Optional.ofNullable(prefs.getString(KEY, null));
    }

    @Override
    public void save(String snapshot) {
        prefs.edit().putString(KEY, snapshot).apply();
    }

    @Override
    public void clear() {
        prefs.edit().remove(KEY).apply();
    }
}
