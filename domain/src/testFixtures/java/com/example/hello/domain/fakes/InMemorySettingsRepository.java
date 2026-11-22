package com.example.hello.domain.fakes;

import com.example.hello.domain.history.SettingsRepository;
import com.example.hello.domain.progression.Intensity;

/** A SettingsRepository held in memory, for tests. */
public final class InMemorySettingsRepository implements SettingsRepository {
    private Intensity intensity = Intensity.BUILD;
    private int weeklyGoal = 3;
    private String programId = "full_body_ab";

    public InMemorySettingsRepository withWeeklyGoal(int goal) {
        weeklyGoal = goal;
        return this;
    }

    @Override
    public Intensity intensity() {
        return intensity;
    }

    @Override
    public void setIntensity(Intensity intensity) {
        this.intensity = intensity;
    }

    @Override
    public int weeklyGoal() {
        return weeklyGoal;
    }

    @Override
    public String programId() {
        return programId;
    }

    @Override
    public void setProgramId(String id) {
        programId = id;
    }
}
