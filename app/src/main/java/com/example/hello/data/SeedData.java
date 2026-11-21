package com.example.hello.data;

import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.model.Exercise;
import com.example.hello.domain.model.SetRecord;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Starting history so the app has something to show before anything is logged. Once an
 * exercise is logged, real data replaces its seed. Streak and earlier days are still placeholders.
 */
public final class SeedData {
    final Map<String, List<SetRecord>> sessions = new HashMap<>();
    final Map<String, Double> prWeights = new HashMap<>();
    final Map<String, Integer> prDaysAgo = new HashMap<>();
    int streakDays;
    int bestStreak;
    int workoutsThisWeek;
    int weeklyGoal;
    /** Volume for the 6 days before today, oldest first. */
    double[] previousSixDays = new double[6];

    int volumeChangePct;

    /** No history at all. */
    public static SeedData empty() {
        SeedData s = new SeedData();
        s.weeklyGoal = 4;
        return s;
    }

    /** The demo history used until real workouts are logged. */
    public static SeedData demo() {
        SeedData s = new SeedData();
        s.session(Catalog.BENCH, same(3, 185, 5)).pr(Catalog.BENCH, 185, 14);
        s.session(Catalog.OVERHEAD_PRESS, same(3, 110, 5)).pr(Catalog.OVERHEAD_PRESS, 115, 24);
        s.session(Catalog.SQUAT, same(3, 275, 5)).pr(Catalog.SQUAT, 315, 9);
        s.session(Catalog.DEADLIFT, same(1, 365, 5)).pr(Catalog.DEADLIFT, 405, 2);
        s.session(
                        Catalog.INCLINE_DB,
                        Arrays.asList(new SetRecord(50, 12, 8), new SetRecord(50, 11, 8), new SetRecord(50, 10, 8)))
                .pr(Catalog.INCLINE_DB, 55, 24);
        s.session(
                        Catalog.PUSHDOWN,
                        Arrays.asList(new SetRecord(60, 15, 10), new SetRecord(60, 15, 10), new SetRecord(60, 15, 10)))
                .pr(Catalog.PUSHDOWN, 70, 24);
        s.streakDays = 12;
        s.bestStreak = 21;
        s.workoutsThisWeek = 3;
        s.weeklyGoal = 4;
        s.previousSixDays = new double[] {5400, 0, 6800, 0, 6220, 0};
        s.volumeChangePct = 12;
        return s;
    }

    private SeedData session(Exercise e, List<SetRecord> sets) {
        sessions.put(e.id, sets);
        return this;
    }

    private SeedData pr(Exercise e, double weight, int daysAgo) {
        prWeights.put(e.id, weight);
        prDaysAgo.put(e.id, daysAgo);
        return this;
    }

    private static List<SetRecord> same(int sets, double w, int reps) {
        List<SetRecord> out = new ArrayList<>();
        for (int i = 0; i < sets; i++) out.add(new SetRecord(w, reps, reps));
        return out;
    }
}
