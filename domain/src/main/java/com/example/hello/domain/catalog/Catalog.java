package com.example.hello.domain.catalog;

import com.example.hello.domain.model.Equipment;
import com.example.hello.domain.model.Exercise;
import com.example.hello.domain.model.WorkoutPlan;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** The exercises and plans the app knows about. */
public final class Catalog {
    private Catalog() {}

    public static final Exercise BENCH = Exercise.builder("bench_press", "Bench Press").build();
    public static final Exercise OVERHEAD_PRESS = Exercise.builder("overhead_press", "Overhead Press").build();
    public static final Exercise SQUAT = Exercise.builder("squat", "Squat").sessionsPerWeek(3).build();
    public static final Exercise DEADLIFT = Exercise.builder("deadlift", "Deadlift")
            .sets(1).incrementScale(2).build();
    public static final Exercise INCLINE_DB = Exercise.builder("incline_db_press", "Incline Dumbbell Press")
            .equipment(Equipment.DUMBBELL).repRange(8, 12).restSeconds(90).build();
    public static final Exercise PUSHDOWN = Exercise.builder("triceps_pushdown", "Triceps Pushdown")
            .equipment(Equipment.MACHINE).repRange(10, 15).restSeconds(60).build();

    public static final WorkoutPlan PUSH_DAY =
            new WorkoutPlan("Push Day", Arrays.asList(BENCH, OVERHEAD_PRESS, INCLINE_DB, PUSHDOWN));

    /** The big barbell lifts shown in personal records and forecasts. */
    public static final List<Exercise> MAIN_LIFTS = Arrays.asList(BENCH, SQUAT, DEADLIFT, OVERHEAD_PRESS);

    /** The lift featured on the home screen's coach card. */
    public static final Exercise FEATURED = BENCH;

    public static final List<Exercise> ALL =
            Arrays.asList(BENCH, OVERHEAD_PRESS, SQUAT, DEADLIFT, INCLINE_DB, PUSHDOWN);

    /** Looks up an exercise by its stable id. */
    public static Optional<Exercise> byId(String id) {
        return ALL.stream().filter(e -> e.id.equals(id)).findFirst();
    }
}
