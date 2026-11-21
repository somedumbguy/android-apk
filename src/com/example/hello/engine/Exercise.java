package com.example.hello.engine;

/** An exercise and how it's programmed: sets, rep range, frequency and rest. */
public final class Exercise {
    public enum Equipment { BARBELL, DUMBBELL, MACHINE }

    public final String name;
    public final Equipment equipment;
    /** Multiplier on the intensity's jump size: 2 for deadlifts (StrongLifts adds 10 lb), else 1. */
    public final double incrementScale;
    public final int sets;
    public final int minReps;
    public final int maxReps;
    /** How many times a week this lift is trained, used for PR date forecasts. */
    public final double sessionsPerWeek;
    public final int restSeconds;

    public Exercise(String name, Equipment equipment, double incrementScale, int sets, int minReps,
                    int maxReps, double sessionsPerWeek, int restSeconds) {
        this.name = name;
        this.equipment = equipment;
        this.incrementScale = incrementScale;
        this.sets = sets;
        this.minReps = minReps;
        this.maxReps = maxReps;
        this.sessionsPerWeek = sessionsPerWeek;
        this.restSeconds = restSeconds;
    }

    /** Fixed reps (e.g. 5×5) use linear progression; a rep range uses double progression. */
    public boolean isLinear() {
        return minReps == maxReps;
    }

    public String schemeLabel() {
        return isLinear()
                ? sets + "×" + minReps + " · LINEAR PROGRESSION"
                : sets + "×" + minReps + "–" + maxReps + " · DOUBLE PROGRESSION";
    }

    /** Smallest practical weight change for this equipment. */
    public double step() {
        return equipment == Equipment.BARBELL ? 2.5 : 5;
    }
}
