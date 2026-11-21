package com.example.hello.ai;

/** A predicted next workout target for one exercise, with a human-readable reason. */
public final class Suggestion {
    public final String exercise;
    public final double weight;
    public final int sets;
    public final int reps;
    /** Change from last session's weight (positive, zero or negative). */
    public final double delta;
    public final String reason;

    public Suggestion(String exercise, double weight, int sets, int reps, double delta, String reason) {
        this.exercise = exercise;
        this.weight = weight;
        this.sets = sets;
        this.reps = reps;
        this.delta = delta;
        this.reason = reason;
    }
}
