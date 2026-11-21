package com.example.hello.domain.model;

public enum Equipment {
    BARBELL(2.5),
    DUMBBELL(5),
    MACHINE(5);

    /** Smallest practical weight change, in lb. */
    public final double step;

    Equipment(double step) {
        this.step = step;
    }
}
