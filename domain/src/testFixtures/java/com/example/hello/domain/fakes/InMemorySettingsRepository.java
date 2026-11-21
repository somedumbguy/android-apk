package com.example.hello.domain.fakes;

import com.example.hello.domain.history.SettingsRepository;
import com.example.hello.domain.progression.Intensity;

/** A SettingsRepository held in memory, for tests. */
public final class InMemorySettingsRepository implements SettingsRepository {
    private Intensity intensity = Intensity.BUILD;

    @Override public Intensity intensity() {
        return intensity;
    }

    @Override public void setIntensity(Intensity intensity) {
        this.intensity = intensity;
    }
}
