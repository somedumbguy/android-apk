package com.example.hello.domain.history;

import java.util.OptionalInt;

/** Consistency stats for the home screen, calculated from the workout history. */
public final class ActivitySummary {
    /** Weeks in a row (ending now) that hit the weekly goal. */
    public final int streakWeeks;

    public final int bestStreakWeeks;
    /** Workout days in the current Monday-to-Sunday week. */
    public final int workoutsThisWeek;

    public final int weeklyGoal;
    private final double[] last7DaysVolume;
    /** Last 7 days' volume vs the 7 before; empty when there's nothing to compare with. */
    public final OptionalInt volumeChangePct;
    /** True while the history is sample data, before any real workout is logged. */
    public final boolean demo;

    /** @param last7DaysVolume volume for each of the last 7 days, oldest first; the last is today */
    public ActivitySummary(
            int streakWeeks,
            int bestStreakWeeks,
            int workoutsThisWeek,
            int weeklyGoal,
            double[] last7DaysVolume,
            OptionalInt volumeChangePct,
            boolean demo) {
        if (last7DaysVolume.length != 7) throw new IllegalArgumentException("need 7 days");
        this.streakWeeks = streakWeeks;
        this.bestStreakWeeks = bestStreakWeeks;
        this.workoutsThisWeek = workoutsThisWeek;
        this.weeklyGoal = weeklyGoal;
        this.last7DaysVolume = last7DaysVolume.clone();
        this.volumeChangePct = volumeChangePct;
        this.demo = demo;
    }

    public double[] last7DaysVolume() {
        return last7DaysVolume.clone();
    }

    public double totalVolume() {
        double t = 0;
        for (double v : last7DaysVolume) t += v;
        return t;
    }
}
