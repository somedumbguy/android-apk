package com.example.hello.domain.forecast;

import com.example.hello.domain.model.Exercise;
import com.example.hello.domain.progression.Intensity;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Projects when a lift reaches a target, from the next session's weight, the intensity's jump
 * size and its expected miss rate. Deterministic for a given clock.
 */
public final class Forecaster {
    /** Plate milestones: 1 plate per side (135) up to 6 plates (585). */
    static final double[] MILESTONES = {135, 225, 315, 405, 495, 585};

    private final Clock clock;

    public Forecaster(Clock clock) {
        this.clock = clock;
    }

    /** The next plate milestone above a weight; beyond 585, the next multiple of 50. */
    public static double nextMilestone(double weight) {
        for (double m : MILESTONES) if (m > weight) return m;
        return Math.ceil((weight + 1) / 50.0) * 50;
    }

    public Forecast project(Exercise e, double nextSessionWeight, double target, Intensity intensity) {
        double inc = intensity.increment(e);
        int jumps = nextSessionWeight >= target ? 0 : (int) Math.ceil((target - nextSessionWeight) / inc);
        int sessions = 1 + (int) Math.ceil(jumps * (1 + intensity.missRate));
        long days = Math.round(sessions / e.sessionsPerWeek * 7);
        return new Forecast(target, sessions, LocalDate.now(clock).plusDays(days));
    }
}
