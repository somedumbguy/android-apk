package com.example.hello.data;

import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.model.CompletedWorkout;
import com.example.hello.domain.model.Exercise;
import com.example.hello.domain.model.SetRecord;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sample data so the app has something to show before anything is logged. Each exercise's
 * seed is replaced once it's logged; the demo workout history is replaced by the first real
 * workout, and the home screen labels it as demo data until then.
 */
public final class SeedData {
    final Map<String, List<SetRecord>> sessions = new HashMap<>();
    final Map<String, Double> prWeights = new HashMap<>();
    final Map<String, Integer> prDaysAgo = new HashMap<>();
    private int demoWeeks;

    /** No history at all. */
    public static SeedData empty() {
        return new SeedData();
    }

    /** The demo history used until real workouts are logged. */
    public static SeedData demo() {
        SeedData s = new SeedData();
        s.session(Catalog.BENCH, same(3, 185, 5)).pr(Catalog.BENCH, 185, 14);
        s.session(Catalog.OVERHEAD_PRESS, same(3, 110, 5)).pr(Catalog.OVERHEAD_PRESS, 115, 24);
        s.session(Catalog.SQUAT, same(3, 275, 5)).pr(Catalog.SQUAT, 315, 9);
        s.session(Catalog.DEADLIFT, same(1, 365, 5)).pr(Catalog.DEADLIFT, 405, 2);
        s.session(Catalog.BARBELL_ROW, same(3, 135, 5)).pr(Catalog.BARBELL_ROW, 145, 16);
        s.session(
                        Catalog.INCLINE_DB,
                        Arrays.asList(new SetRecord(50, 12, 8), new SetRecord(50, 11, 8), new SetRecord(50, 10, 8)))
                .pr(Catalog.INCLINE_DB, 55, 24);
        s.session(
                        Catalog.PUSHDOWN,
                        Arrays.asList(new SetRecord(60, 15, 10), new SetRecord(60, 15, 10), new SetRecord(60, 15, 10)))
                .pr(Catalog.PUSHDOWN, 70, 24);
        s.demoWeeks = 8;
        return s;
    }

    /**
     * Monday, Wednesday and Friday workouts for the past few weeks, before today. Volumes
     * vary by date but are deterministic, so the same day always looks the same.
     */
    List<CompletedWorkout> demoHistory(LocalDate today) {
        List<CompletedWorkout> out = new ArrayList<>();
        for (LocalDate d = today.minusWeeks(demoWeeks); d.isBefore(today); d = d.plusDays(1)) {
            DayOfWeek dow = d.getDayOfWeek();
            if (dow != DayOfWeek.MONDAY && dow != DayOfWeek.WEDNESDAY && dow != DayOfWeek.FRIDAY) continue;
            double volume = 5200 + (d.toEpochDay() * 37) % 1700;
            String plan = out.size() % 2 == 0 ? Catalog.WORKOUT_A.name : Catalog.WORKOUT_B.name;
            out.add(new CompletedWorkout(d, plan, volume, 9));
        }
        return out;
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
