package com.example.hello.data;

/** One logged set: the weight used, reps done and reps planned. */
public final class SetRecord {
    public final double weight;
    public final int reps;
    public final int targetReps;

    public SetRecord(double weight, int reps, int targetReps) {
        this.weight = weight;
        this.reps = reps;
        this.targetReps = targetReps;
    }
}
