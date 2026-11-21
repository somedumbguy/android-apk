package com.example.hello.domain.history;

/** Consistency stats for the home screen. */
public final class ActivitySummary {
    public final int streakDays;
    public final int bestStreak;
    public final int workoutsThisWeek;
    public final int weeklyGoal;
    private final double[] last7DaysVolume;
    public final int volumeChangePct;

    /** @param last7DaysVolume volume for each of the last 7 days, oldest first; the last is today */
    public ActivitySummary(int streakDays, int bestStreak, int workoutsThisWeek, int weeklyGoal,
                           double[] last7DaysVolume, int volumeChangePct) {
        if (last7DaysVolume.length != 7) throw new IllegalArgumentException("need 7 days");
        this.streakDays = streakDays;
        this.bestStreak = bestStreak;
        this.workoutsThisWeek = workoutsThisWeek;
        this.weeklyGoal = weeklyGoal;
        this.last7DaysVolume = last7DaysVolume.clone();
        this.volumeChangePct = volumeChangePct;
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
