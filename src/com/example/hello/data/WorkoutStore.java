package com.example.hello.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.hello.engine.Exercise;
import com.example.hello.engine.Forecast;
import com.example.hello.engine.Intensity;
import com.example.hello.engine.ProgressionEngine;
import com.example.hello.engine.Suggestion;
import com.example.hello.engine.Weights;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/** Saves logged workouts, PRs and settings on the phone. Falls back to SampleData. */
public final class WorkoutStore {
    private static final long DAY_MS = 24L * 60 * 60 * 1000;
    private final SharedPreferences p;

    public WorkoutStore(Context c) {
        p = c.getApplicationContext().getSharedPreferences("workouts", Context.MODE_PRIVATE);
    }

    // ---- Settings ----

    public Intensity intensity() {
        try {
            return Intensity.valueOf(p.getString("intensity", Intensity.BUILD.name()));
        } catch (IllegalArgumentException ex) {
            return Intensity.BUILD;
        }
    }

    public void setIntensity(Intensity i) {
        p.edit().putString("intensity", i.name()).apply();
    }

    // ---- History ----

    public List<SetRecord> lastSession(Exercise e) {
        String s = p.getString("sets:" + e.name, null);
        return s == null ? SampleData.seedSession(e.name) : decode(s);
    }

    public int fails(Exercise e) {
        return p.getInt("fails:" + e.name, 0);
    }

    public double pr(Exercise e) {
        String s = p.getString("pr:" + e.name, null);
        return s == null ? SampleData.seedPr(e.name) : Double.parseDouble(s);
    }

    public long prTime(Exercise e) {
        long fallback = System.currentTimeMillis() - SampleData.seedPrDaysAgo(e.name) * DAY_MS;
        return p.getLong("prTime:" + e.name, fallback);
    }

    public boolean prIsRecent(Exercise e) {
        return System.currentTimeMillis() - prTime(e) < 7 * DAY_MS;
    }

    public String prWhen(Exercise e) {
        long days = (System.currentTimeMillis() - prTime(e)) / DAY_MS;
        if (days <= 0) return "Today";
        if (days == 1) return "Yesterday";
        if (days < 7) return days + " days ago";
        if (days < 14) return "Last week";
        return (days / 7) + " weeks ago";
    }

    public boolean workedOutToday() {
        return p.getInt("day", 0) == today();
    }

    public float todayVolume() {
        return workedOutToday() ? p.getFloat("vol", 0) : 0;
    }

    // ---- Engine shortcuts ----

    public Suggestion suggest(Exercise e) {
        return suggest(e, intensity());
    }

    public Suggestion suggest(Exercise e, Intensity i) {
        return ProgressionEngine.suggestNext(e, lastSession(e), i, fails(e));
    }

    public Forecast forecast(Exercise e, Intensity i) {
        return Forecast.project(e, suggest(e, i).weight, Forecast.nextMilestone(pr(e)), i);
    }

    // ---- Saving a workout ----

    public static final class ExerciseLog {
        public final Exercise exercise;
        public final int plannedSets;
        public final List<SetRecord> sets;

        public ExerciseLog(Exercise exercise, int plannedSets, List<SetRecord> sets) {
            this.exercise = exercise;
            this.plannedSets = plannedSets;
            this.sets = sets;
        }
    }

    public static final class Result {
        public double volume;
        public int sets;
        public final List<String> newPrs = new ArrayList<>();
    }

    public Result save(List<ExerciseLog> logs) {
        Result r = new Result();
        SharedPreferences.Editor ed = p.edit();
        long now = System.currentTimeMillis();

        for (ExerciseLog log : logs) {
            Exercise e = log.exercise;
            double prevW = maxWeight(lastSession(e));
            double newW = maxWeight(log.sets);
            boolean allHit = log.sets.size() >= log.plannedSets;
            double best = 0;
            for (SetRecord s : log.sets) {
                if (s.reps < s.targetReps) allHit = false;
                if (s.reps > 0) best = Math.max(best, s.weight);
                r.volume += s.weight * s.reps;
                r.sets++;
            }
            // A lighter session after a stall is a deload, so the miss count starts over.
            int fails = allHit ? 0 : (newW < prevW ? 1 : fails(e) + 1);
            ed.putString("sets:" + e.name, encode(log.sets));
            ed.putInt("fails:" + e.name, fails);
            if (best > pr(e)) {
                ed.putString("pr:" + e.name, String.valueOf(best));
                ed.putLong("prTime:" + e.name, now);
                r.newPrs.add(e.name + " · " + Weights.format(best) + " lb");
            }
        }

        float vol = todayVolume();
        ed.putInt("day", today());
        ed.putFloat("vol", vol + (float) r.volume);
        ed.apply();
        return r;
    }

    // ---- Helpers ----

    private static double maxWeight(List<SetRecord> sets) {
        double m = 0;
        for (SetRecord s : sets) m = Math.max(m, s.weight);
        return m;
    }

    private static int today() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.YEAR) * 1000 + c.get(Calendar.DAY_OF_YEAR);
    }

    private static String encode(List<SetRecord> sets) {
        StringBuilder sb = new StringBuilder();
        for (SetRecord s : sets) {
            if (sb.length() > 0) sb.append(';');
            sb.append(s.weight).append(':').append(s.reps).append(':').append(s.targetReps);
        }
        return sb.toString();
    }

    private static List<SetRecord> decode(String s) {
        List<SetRecord> out = new ArrayList<>();
        if (s.isEmpty()) return out;
        for (String part : s.split(";")) {
            String[] f = part.split(":");
            out.add(new SetRecord(Double.parseDouble(f[0]), Integer.parseInt(f[1]), Integer.parseInt(f[2])));
        }
        return out;
    }
}
