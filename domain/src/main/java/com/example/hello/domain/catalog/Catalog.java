package com.example.hello.domain.catalog;

import com.example.hello.domain.model.Equipment;
import com.example.hello.domain.model.Exercise;
import com.example.hello.domain.model.Program;
import com.example.hello.domain.model.WorkoutPlan;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** The exercises and plans the app knows about. */
public final class Catalog {
    private Catalog() {}

    public static final Exercise BENCH =
            Exercise.builder("bench_press", "Bench Press").build();
    public static final Exercise OVERHEAD_PRESS =
            Exercise.builder("overhead_press", "Overhead Press").build();
    public static final Exercise SQUAT =
            Exercise.builder("squat", "Squat").sessionsPerWeek(3).build();
    public static final Exercise DEADLIFT =
            Exercise.builder("deadlift", "Deadlift").sets(1).incrementScale(2).build();
    public static final Exercise BARBELL_ROW =
            Exercise.builder("barbell_row", "Barbell Row").build();
    public static final Exercise INCLINE_DB = Exercise.builder("incline_db_press", "Incline Dumbbell Press")
            .equipment(Equipment.DUMBBELL)
            .repRange(8, 12)
            .restSeconds(90)
            .build();
    public static final Exercise PUSHDOWN = Exercise.builder("triceps_pushdown", "Triceps Pushdown")
            .equipment(Equipment.MACHINE)
            .repRange(10, 15)
            .restSeconds(60)
            .build();

    public static final WorkoutPlan PUSH_DAY =
            new WorkoutPlan("Push Day", Arrays.asList(BENCH, OVERHEAD_PRESS, INCLINE_DB, PUSHDOWN));

    /** The big barbell lifts shown in personal records and forecasts. */
    public static final List<Exercise> MAIN_LIFTS = Arrays.asList(BENCH, SQUAT, DEADLIFT, OVERHEAD_PRESS);

    public static final WorkoutPlan WORKOUT_A = new WorkoutPlan("Workout A", Arrays.asList(SQUAT, BENCH, BARBELL_ROW));
    public static final WorkoutPlan WORKOUT_B =
            new WorkoutPlan("Workout B", Arrays.asList(SQUAT, OVERHEAD_PRESS, DEADLIFT));

    /** Classic 5×5-style full-body alternation: squat every session, A then B. */
    public static final Program FULL_BODY_AB =
            new Program("full_body_ab", "Full body A/B", Arrays.asList(WORKOUT_A, WORKOUT_B));

    public static final Program PUSH = new Program("push", "Push day", Arrays.asList(PUSH_DAY));

    public static final Program DEFAULT_PROGRAM = FULL_BODY_AB;
    public static final List<Program> PROGRAMS = Arrays.asList(FULL_BODY_AB, PUSH);

    public static final List<Exercise> ALL =
            Arrays.asList(BENCH, OVERHEAD_PRESS, SQUAT, DEADLIFT, BARBELL_ROW, INCLINE_DB, PUSHDOWN);

    public static Optional<Program> programById(String id) {
        return PROGRAMS.stream().filter(p -> p.id.equals(id)).findFirst();
    }

    /** Looks up an exercise by its stable id. */
    public static Optional<Exercise> byId(String id) {
        return ALL.stream().filter(e -> e.id.equals(id)).findFirst();
    }
}
