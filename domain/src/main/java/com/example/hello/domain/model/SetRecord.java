package com.example.hello.domain.model;

import java.util.List;

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

    public boolean hitTarget() {
        return reps >= targetReps;
    }

    public double volume() {
        return weight * reps;
    }

    public static double maxWeight(List<SetRecord> sets) {
        double max = 0;
        for (SetRecord s : sets) max = Math.max(max, s.weight);
        return max;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SetRecord)) return false;
        SetRecord s = (SetRecord) o;
        return s.weight == weight && s.reps == reps && s.targetReps == targetReps;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(weight) * 31 * 31 + reps * 31 + targetReps;
    }

    @Override
    public String toString() {
        return weight + "x" + reps + "/" + targetReps;
    }
}
