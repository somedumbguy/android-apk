package com.example.hello.domain.session;

import com.example.hello.domain.model.SetRecord;

/** One planned set in a workout, and what was actually done. */
public final class SetEntry {
    public enum Status { PENDING, DONE, SHORT }

    private static final int NOT_DONE = -1;

    public final double weight;
    public final int target;
    public final boolean warmup;
    private int reps = NOT_DONE;

    public SetEntry(double weight, int target, boolean warmup) {
        this.weight = weight;
        this.target = target;
        this.warmup = warmup;
    }

    /** Done as planned. */
    public void complete() {
        reps = target;
    }

    /** Done with a specific number of reps (may be below or above target). */
    public void record(int reps) {
        if (reps < 0) throw new IllegalArgumentException("reps must be >= 0");
        this.reps = reps;
    }

    public void undo() {
        reps = NOT_DONE;
    }

    /** Completes a pending set or undoes a done one. Returns true if the set is now done. */
    public boolean toggle() {
        if (isDone()) {
            undo();
            return false;
        }
        complete();
        return true;
    }

    public boolean isDone() {
        return reps != NOT_DONE;
    }

    /** Reps done, or -1 if not done. */
    public int reps() {
        return reps;
    }

    public Status status() {
        if (!isDone()) return Status.PENDING;
        return reps >= target ? Status.DONE : Status.SHORT;
    }

    SetRecord toRecord() {
        return new SetRecord(weight, reps, target);
    }
}
