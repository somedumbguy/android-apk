package com.example.hello.domain.model;

import java.util.List;

/** Strength estimates. */
public final class Strength {
    private Strength() {}

    /**
     * Estimated one-rep max using the Epley formula: weight × (1 + reps / 30). A single is
     * its own max, and a set with no reps estimates nothing. Most reliable up to about 10 reps.
     */
    public static double e1rm(double weight, int reps) {
        if (reps <= 0) return 0;
        if (reps == 1) return weight;
        return weight * (1 + reps / 30.0);
    }

    /** The best estimate across a list of sets. */
    public static double bestE1rm(List<SetRecord> sets) {
        double best = 0;
        for (SetRecord s : sets) best = Math.max(best, e1rm(s.weight, s.reps));
        return best;
    }
}
