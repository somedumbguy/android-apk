package com.example.hello.data;

import com.example.hello.engine.Exercise;
import com.example.hello.engine.Exercise.Equipment;

import java.util.Arrays;
import java.util.List;

/** The exercises the app knows about, and today's workout. */
public final class Catalog {
    private Catalog() {}

    public static final Exercise BENCH =
            new Exercise("Bench Press", Equipment.BARBELL, 1, 3, 5, 5, 1.5, 180);
    public static final Exercise OVERHEAD_PRESS =
            new Exercise("Overhead Press", Equipment.BARBELL, 1, 3, 5, 5, 1.5, 180);
    public static final Exercise SQUAT =
            new Exercise("Squat", Equipment.BARBELL, 1, 3, 5, 5, 3, 180);
    public static final Exercise DEADLIFT =
            new Exercise("Deadlift", Equipment.BARBELL, 2, 1, 5, 5, 1.5, 180);
    public static final Exercise INCLINE_DB =
            new Exercise("Incline Dumbbell Press", Equipment.DUMBBELL, 1, 3, 8, 12, 1.5, 90);
    public static final Exercise PUSHDOWN =
            new Exercise("Triceps Pushdown", Equipment.MACHINE, 1, 3, 10, 15, 1.5, 60);

    public static final String TODAY_WORKOUT = "Push Day";
    public static final List<Exercise> PUSH_DAY = Arrays.asList(BENCH, OVERHEAD_PRESS, INCLINE_DB, PUSHDOWN);

    /** The big barbell lifts shown in personal records and forecasts. */
    public static final List<Exercise> MAIN_LIFTS = Arrays.asList(BENCH, SQUAT, DEADLIFT, OVERHEAD_PRESS);
}
