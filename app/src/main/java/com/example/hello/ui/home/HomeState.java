package com.example.hello.ui.home;

import java.util.Collections;
import java.util.List;

/** Everything the home screen shows, already formatted. The view just draws it. */
public final class HomeState {

    public static final class RecordCard {
        public final String lift;
        public final String weight;
        public final String when;
        public final boolean fresh;
        public final String next;
        public final String nextDate;

        RecordCard(String lift, String weight, String when, boolean fresh, String next, String nextDate) {
            this.lift = lift;
            this.weight = weight;
            this.when = when;
            this.fresh = fresh;
            this.next = next;
            this.nextDate = nextDate;
        }
    }

    public static final class Target {
        public final String intensity;
        public final String context;
        public final String weight;
        public final String reps;
        public final boolean prAttempt;
        /** "PR ATTEMPT" badge text, or the change from last session. */
        public final String badge;

        public final String reason;
        /** "Start workout" or, with one in progress today, "Resume workout". */
        public final String action;

        Target(
                String intensity,
                String context,
                String weight,
                String reps,
                boolean prAttempt,
                String badge,
                String reason,
                String action) {
            this.intensity = intensity;
            this.context = context;
            this.weight = weight;
            this.reps = reps;
            this.prAttempt = prAttempt;
            this.badge = badge;
            this.reason = reason;
            this.action = action;
        }
    }

    public static final class Stats {
        public final String streak;
        public final String best;
        public final int workoutsDone;
        public final int weeklyGoal;
        public final double[] volumes;
        public final String[] dayLetters;
        public final String totalVolume;
        public final String change;

        Stats(
                String streak,
                String best,
                int workoutsDone,
                int weeklyGoal,
                double[] volumes,
                String[] dayLetters,
                String totalVolume,
                String change) {
            this.streak = streak;
            this.best = best;
            this.workoutsDone = workoutsDone;
            this.weeklyGoal = weeklyGoal;
            this.volumes = volumes;
            this.dayLetters = dayLetters;
            this.totalVolume = totalVolume;
            this.change = change;
        }
    }

    public final String date;
    public final String greeting;
    public final String initial;
    public final List<RecordCard> records;
    public final Target target;
    public final Stats stats;

    HomeState(String date, String greeting, String initial, List<RecordCard> records, Target target, Stats stats) {
        this.date = date;
        this.greeting = greeting;
        this.initial = initial;
        this.records = Collections.unmodifiableList(records);
        this.target = target;
        this.stats = stats;
    }
}
