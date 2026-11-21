package com.example.hello.data;

import java.util.Arrays;
import java.util.List;

/**
 * Placeholder training history so the home screen has something to show.
 * Replace with real stored workouts once logging exists.
 */
public final class SampleData {
    private SampleData() {}

    public static final String TODAY_WORKOUT = "Push Day";
    public static final String NEXT_EXERCISE = "Bench Press";

    public static List<SetRecord> lastBenchSession() {
        return Arrays.asList(
                new SetRecord(185, 5, 5),
                new SetRecord(185, 5, 5),
                new SetRecord(185, 5, 5));
    }

    public static final int STREAK_DAYS = 12;
    public static final int BEST_STREAK = 21;
    public static final int WORKOUTS_THIS_WEEK = 3;
    public static final int WEEKLY_GOAL = 4;

    /** Volume (lb) for each of the last 7 days, oldest first; the last entry is today. */
    public static final float[] LAST_7_DAYS_VOLUME = {5400, 0, 6800, 0, 6220, 0, 0};
    public static final int VOLUME_CHANGE_PCT = 12;

    public static final class PersonalRecord {
        public final String lift;
        public final double weight;
        public final String when;
        public final boolean isNew;

        PersonalRecord(String lift, double weight, String when, boolean isNew) {
            this.lift = lift;
            this.weight = weight;
            this.when = when;
            this.isNew = isNew;
        }
    }

    public static List<PersonalRecord> personalRecords() {
        return Arrays.asList(
                new PersonalRecord("Deadlift", 405, "2 days ago", true),
                new PersonalRecord("Squat", 315, "Last week", false),
                new PersonalRecord("Bench Press", 185, "2 weeks ago", false),
                new PersonalRecord("Overhead Press", 135, "3 weeks ago", false));
    }
}
