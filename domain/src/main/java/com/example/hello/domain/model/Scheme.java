package com.example.hello.domain.model;

/** How an exercise progresses. Each scheme has a matching ProgressionStrategy. */
public enum Scheme {
    /** Fixed reps (e.g. 3×5). Add weight every successful session. */
    LINEAR("LINEAR PROGRESSION"),
    /** A rep range (e.g. 3×8–12). Add reps until the top of the range, then add weight. */
    DOUBLE("DOUBLE PROGRESSION");

    public final String label;

    Scheme(String label) {
        this.label = label;
    }
}
