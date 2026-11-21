package com.example.hello.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Starting history so the app has something to show before you've logged anything.
 * Once you log a workout, your real numbers replace these for that exercise.
 */
public final class SampleData {
    private SampleData() {}

    public static final int STREAK_DAYS = 12;
    public static final int BEST_STREAK = 21;
    public static final int WORKOUTS_THIS_WEEK = 3;
    public static final int WEEKLY_GOAL = 4;

    /** Volume (lb) for the 6 days before today, oldest first. Today comes from real logs. */
    public static final float[] PREVIOUS_6_DAYS_VOLUME = {5400, 0, 6800, 0, 6220, 0};
    public static final int VOLUME_CHANGE_PCT = 12;

    public static List<SetRecord> seedSession(String exercise) {
        switch (exercise) {
            case "Bench Press": return same(3, 185, 5);
            case "Overhead Press": return same(3, 110, 5);
            case "Squat": return same(3, 275, 5);
            case "Deadlift": return same(1, 365, 5);
            case "Incline Dumbbell Press": return reps(50, 8, 12, 11, 10);
            case "Triceps Pushdown": return reps(60, 10, 15, 15, 15);
            default: return new ArrayList<>();
        }
    }

    public static double seedPr(String exercise) {
        switch (exercise) {
            case "Bench Press": return 185;
            case "Overhead Press": return 115;
            case "Squat": return 315;
            case "Deadlift": return 405;
            case "Incline Dumbbell Press": return 55;
            case "Triceps Pushdown": return 70;
            default: return 0;
        }
    }

    public static int seedPrDaysAgo(String exercise) {
        switch (exercise) {
            case "Deadlift": return 2;
            case "Squat": return 9;
            case "Bench Press": return 14;
            default: return 24;
        }
    }

    private static List<SetRecord> same(int sets, double w, int reps) {
        List<SetRecord> out = new ArrayList<>();
        for (int i = 0; i < sets; i++) out.add(new SetRecord(w, reps, reps));
        return out;
    }

    private static List<SetRecord> reps(double w, int target, int... done) {
        List<SetRecord> out = new ArrayList<>();
        for (int r : done) out.add(new SetRecord(w, r, target));
        return out;
    }
}
