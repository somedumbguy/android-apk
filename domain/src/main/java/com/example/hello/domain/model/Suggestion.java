package com.example.hello.domain.model;

/** The prescribed weight and reps for an exercise's next session, with the reason. */
public final class Suggestion {
    public final Exercise exercise;
    public final double weight;
    private final int[] reps;
    /** Change from last session's weight (positive, zero or negative). */
    public final double delta;
    public final String reason;

    public Suggestion(Exercise exercise, double weight, int[] reps, double delta, String reason) {
        this.exercise = exercise;
        this.weight = weight;
        this.reps = reps.clone();
        this.delta = delta;
        this.reason = reason;
    }

    /** Target reps for each work set. */
    public int[] reps() {
        return reps.clone();
    }

    public String repsLabel() {
        boolean same = true;
        for (int r : reps) if (r != reps[0]) same = false;
        if (same) return reps.length + (reps.length == 1 ? " set × " : " sets × ") + reps[0] + " reps";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < reps.length; i++) {
            if (i > 0) sb.append(" / ");
            sb.append(reps[i]);
        }
        return reps.length + " sets · " + sb + " reps";
    }
}
