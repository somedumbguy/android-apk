package com.example.hello.domain.model;

import java.util.Collections;
import java.util.List;

/** A named list of exercises, e.g. "Push Day". */
public final class WorkoutPlan {
    public final String name;
    public final List<Exercise> exercises;

    public WorkoutPlan(String name, List<Exercise> exercises) {
        this.name = name;
        this.exercises = Collections.unmodifiableList(exercises);
    }
}
