package com.example.hello.domain.model;

import java.time.LocalDate;

/** A finished workout, as kept in the history log. */
public final class CompletedWorkout {
    public final LocalDate day;
    public final String plan;
    public final double volume;
    public final int sets;

    public CompletedWorkout(LocalDate day, String plan, double volume, int sets) {
        this.day = day;
        this.plan = plan;
        this.volume = volume;
        this.sets = sets;
    }
}
