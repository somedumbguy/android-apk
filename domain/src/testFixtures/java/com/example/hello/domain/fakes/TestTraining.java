package com.example.hello.domain.fakes;

import com.example.hello.domain.forecast.Forecaster;
import com.example.hello.domain.plates.PlateMath;
import com.example.hello.domain.progression.ProgressionEngine;
import com.example.hello.domain.training.TrainingService;
import java.time.Clock;

/** Builds a TrainingService wired to in-memory fakes and a fixed clock. */
public final class TestTraining {
    public final InMemoryWorkoutRepository workouts = new InMemoryWorkoutRepository();
    public final InMemorySettingsRepository settings = new InMemorySettingsRepository();
    public final InMemoryActiveWorkoutStore activeWorkout = new InMemoryActiveWorkoutStore();
    public final Clock clock;
    public final TrainingService service;

    public TestTraining() {
        this(TestClocks.fixed());
    }

    public TestTraining(Clock clock) {
        this.clock = clock;
        this.service = new TrainingService(
                workouts,
                settings,
                activeWorkout,
                ProgressionEngine.standard(),
                new Forecaster(clock),
                PlateMath.standard(),
                clock);
    }
}
