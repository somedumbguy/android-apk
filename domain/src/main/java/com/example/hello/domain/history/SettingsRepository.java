package com.example.hello.domain.history;

import com.example.hello.domain.progression.Intensity;

/** User preferences. */
public interface SettingsRepository {
    Intensity intensity();

    void setIntensity(Intensity intensity);
}
