package com.example.hello.data;

import android.content.SharedPreferences;
import com.example.hello.domain.history.SettingsRepository;
import com.example.hello.domain.progression.Intensity;

/** Stores user preferences on the phone. */
public final class SharedPrefsSettingsRepository implements SettingsRepository {
    private static final String INTENSITY = "intensity";
    private final SharedPreferences prefs;

    public SharedPrefsSettingsRepository(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    @Override
    public Intensity intensity() {
        try {
            return Intensity.valueOf(prefs.getString(INTENSITY, Intensity.BUILD.name()));
        } catch (IllegalArgumentException e) {
            return Intensity.BUILD;
        }
    }

    @Override
    public void setIntensity(Intensity intensity) {
        prefs.edit().putString(INTENSITY, intensity.name()).apply();
    }
}
