package com.example.hello.domain.training;

import com.example.hello.domain.model.Exercise;

import java.util.Collections;
import java.util.List;

/** The result of finishing a workout. */
public final class WorkoutSummary {

    /** A personal record set during the workout. */
    public static final class NewRecord {
        public final Exercise exercise;
        public final double weight;

        public NewRecord(Exercise exercise, double weight) {
            this.exercise = exercise;
            this.weight = weight;
        }
    }

    public final double volume;
    public final int sets;
    public final List<NewRecord> newRecords;

    public WorkoutSummary(double volume, int sets, List<NewRecord> newRecords) {
        this.volume = volume;
        this.sets = sets;
        this.newRecords = Collections.unmodifiableList(newRecords);
    }
}
