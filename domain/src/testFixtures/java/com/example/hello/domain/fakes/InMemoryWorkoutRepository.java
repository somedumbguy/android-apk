package com.example.hello.domain.fakes;

import com.example.hello.domain.history.ActivitySummary;
import com.example.hello.domain.history.WorkoutRepository;
import com.example.hello.domain.model.Exercise;
import com.example.hello.domain.model.PersonalRecord;
import com.example.hello.domain.model.SetRecord;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** A WorkoutRepository held in memory, for tests. */
public final class InMemoryWorkoutRepository implements WorkoutRepository {
    private final Map<String, List<SetRecord>> sessions = new HashMap<>();
    private final Map<String, Integer> fails = new HashMap<>();
    private final Map<String, PersonalRecord> records = new HashMap<>();
    private final Map<LocalDate, Double> volumes = new HashMap<>();
    private int streak;
    private int bestStreak;
    private int weeklyGoal = 4;

    // ---- Test setup helpers ----

    public InMemoryWorkoutRepository withSession(Exercise e, SetRecord... sets) {
        sessions.put(e.id, new ArrayList<>(Arrays.asList(sets)));
        return this;
    }

    /** Sets of the same weight that all hit the exercise's minimum reps. */
    public InMemoryWorkoutRepository withCleanSession(Exercise e, double weight) {
        List<SetRecord> sets = new ArrayList<>();
        for (int i = 0; i < e.sets; i++) sets.add(new SetRecord(weight, e.minReps, e.minReps));
        sessions.put(e.id, sets);
        return this;
    }

    public InMemoryWorkoutRepository withFails(Exercise e, int n) {
        fails.put(e.id, n);
        return this;
    }

    public InMemoryWorkoutRepository withPr(Exercise e, double weight, Instant at) {
        records.put(e.id, new PersonalRecord(e.id, weight, at));
        return this;
    }

    public InMemoryWorkoutRepository withStreak(int streak, int best) {
        this.streak = streak;
        this.bestStreak = best;
        return this;
    }

    public double volumeOn(LocalDate day) {
        Double v = volumes.get(day);
        return v == null ? 0 : v;
    }

    // ---- WorkoutRepository ----

    @Override public List<SetRecord> lastSession(Exercise e) {
        List<SetRecord> s = sessions.get(e.id);
        return s == null ? new ArrayList<>() : new ArrayList<>(s);
    }

    @Override public int consecutiveFails(Exercise e) {
        Integer n = fails.get(e.id);
        return n == null ? 0 : n;
    }

    @Override public Optional<PersonalRecord> personalRecord(Exercise e) {
        return Optional.ofNullable(records.get(e.id));
    }

    @Override public void saveSession(Exercise e, List<SetRecord> sets, int consecutiveFails) {
        sessions.put(e.id, new ArrayList<>(sets));
        fails.put(e.id, consecutiveFails);
    }

    @Override public void savePersonalRecord(PersonalRecord r) {
        records.put(r.exerciseId, r);
    }

    @Override public void addVolume(LocalDate day, double volume) {
        volumes.put(day, volumeOn(day) + volume);
    }

    @Override public ActivitySummary activity(LocalDate today) {
        double[] last7 = new double[7];
        int workouts = 0;
        for (int i = 0; i < 7; i++) {
            last7[i] = volumeOn(today.minusDays(6 - i));
            if (last7[i] > 0) workouts++;
        }
        return new ActivitySummary(streak, bestStreak, workouts, weeklyGoal, last7, 0);
    }
}
