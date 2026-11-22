package com.example.hello.domain.training;

import com.example.hello.domain.model.Exercise;
import java.util.Collections;
import java.util.List;

/** The result of finishing a workout. */
public final class WorkoutSummary {

    /** A personal record set during the workout. */
    public static final class NewRecord {
        public enum Kind {
            /** Heaviest weight ever lifted. */
            HEAVIEST,
            /** Best estimated one-rep max, e.g. more reps at the same weight. */
            ESTIMATED_1RM
        }

        public final Exercise exercise;
        public final Kind kind;
        /** The weight, or the estimated max, in lb. */
        public final double weight;

        public NewRecord(Exercise exercise, Kind kind, double weight) {
            this.exercise = exercise;
            this.kind = kind;
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
