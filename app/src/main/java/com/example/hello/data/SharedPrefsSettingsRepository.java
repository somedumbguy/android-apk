package com.example.hello.data;

import android.content.SharedPreferences;
import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.history.SettingsRepository;
import com.example.hello.domain.progression.Intensity;

/** Stores user preferences on the phone. */
public final class SharedPrefsSettingsRepository implements SettingsRepository {
    private static final String INTENSITY = "intensity";
    private static final String WEEKLY_GOAL = "weeklyGoal";
    private static final String PROGRAM = "program";
    static final int DEFAULT_WEEKLY_GOAL = 3;
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

    @Override
    public int weeklyGoal() {
        return prefs.getInt(WEEKLY_GOAL, DEFAULT_WEEKLY_GOAL);
    }

    @Override
    public String programId() {
        return prefs.getString(PROGRAM, Catalog.DEFAULT_PROGRAM.id);
    }

    @Override
    public void setProgramId(String id) {
        prefs.edit().putString(PROGRAM, id).apply();
    }

    @Override
    public void setWeeklyGoal(int days) {
        prefs.edit().putInt(WEEKLY_GOAL, days).apply();
    }
}
