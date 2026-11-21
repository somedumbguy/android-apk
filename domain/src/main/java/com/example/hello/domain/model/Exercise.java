package com.example.hello.domain.model;

import java.util.Objects;

/** An exercise and how it's programmed. Identified by a stable id, not its display name. */
public final class Exercise {
    public final String id;
    public final String name;
    public final Equipment equipment;
    public final Scheme scheme;
    public final int sets;
    public final int minReps;
    public final int maxReps;
    /** Multiplier on the intensity's jump: 2 for deadlifts (StrongLifts adds 10 lb), else 1. */
    public final double incrementScale;
    /** Times per week this lift is trained, used for forecasts. */
    public final double sessionsPerWeek;
    public final int restSeconds;

    private Exercise(Builder b) {
        id = Objects.requireNonNull(b.id);
        name = Objects.requireNonNull(b.name);
        equipment = b.equipment;
        scheme = b.minReps == b.maxReps ? Scheme.LINEAR : Scheme.DOUBLE;
        sets = b.sets;
        minReps = b.minReps;
        maxReps = b.maxReps;
        incrementScale = b.incrementScale;
        sessionsPerWeek = b.sessionsPerWeek;
        restSeconds = b.restSeconds;
    }

    public String schemeLabel() {
        String reps = minReps == maxReps ? String.valueOf(minReps) : minReps + "–" + maxReps;
        return sets + "×" + reps + " · " + scheme.label;
    }

    public boolean isBarbell() {
        return equipment == Equipment.BARBELL;
    }

    @Override public boolean equals(Object o) {
        return o instanceof Exercise && ((Exercise) o).id.equals(id);
    }

    @Override public int hashCode() {
        return id.hashCode();
    }

    @Override public String toString() {
        return name;
    }

    public static Builder builder(String id, String name) {
        return new Builder(id, name);
    }

    public static final class Builder {
        private final String id;
        private final String name;
        private Equipment equipment = Equipment.BARBELL;
        private int sets = 3;
        private int minReps = 5;
        private int maxReps = 5;
        private double incrementScale = 1;
        private double sessionsPerWeek = 1.5;
        private int restSeconds = 180;

        private Builder(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public Builder equipment(Equipment e) { equipment = e; return this; }
        public Builder sets(int n) { sets = n; return this; }
        public Builder reps(int n) { minReps = n; maxReps = n; return this; }
        public Builder repRange(int min, int max) { minReps = min; maxReps = max; return this; }
        public Builder incrementScale(double s) { incrementScale = s; return this; }
        public Builder sessionsPerWeek(double s) { sessionsPerWeek = s; return this; }
        public Builder restSeconds(int s) { restSeconds = s; return this; }
        public Exercise build() { return new Exercise(this); }
    }
}
