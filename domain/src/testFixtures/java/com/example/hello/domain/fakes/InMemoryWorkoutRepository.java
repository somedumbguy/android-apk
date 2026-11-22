package com.example.hello.domain.fakes;

import com.example.hello.domain.history.WorkoutRepository;
import com.example.hello.domain.model.CompletedWorkout;
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
import java.util.OptionalDouble;

/** A WorkoutRepository held in memory, for tests. */
public final class InMemoryWorkoutRepository implements WorkoutRepository {
    private final Map<String, List<SetRecord>> sessions = new HashMap<>();
    private final Map<String, Integer> fails = new HashMap<>();
    private final Map<String, PersonalRecord> records = new HashMap<>();
    private final List<CompletedWorkout> history = new ArrayList<>();
    private final Map<String, Double> e1rms = new HashMap<>();

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

    public InMemoryWorkoutRepository withE1rm(Exercise e, double e1rm) {
        e1rms.put(e.id, e1rm);
        return this;
    }

    /** A logged workout on {@code day}. */
    public InMemoryWorkoutRepository withWorkout(LocalDate day, double volume) {
        history.add(new CompletedWorkout(day, "Test", volume, 3));
        return this;
    }

    // ---- WorkoutRepository ----

    @Override
    public List<SetRecord> lastSession(Exercise e) {
        List<SetRecord> s = sessions.get(e.id);
        return s == null ? new ArrayList<>() : new ArrayList<>(s);
    }

    @Override
    public int consecutiveFails(Exercise e) {
        Integer n = fails.get(e.id);
        return n == null ? 0 : n;
    }

    @Override
    public Optional<PersonalRecord> personalRecord(Exercise e) {
        return Optional.ofNullable(records.get(e.id));
    }

    @Override
    public void saveSession(Exercise e, List<SetRecord> sets, int consecutiveFails) {
        sessions.put(e.id, new ArrayList<>(sets));
        fails.put(e.id, consecutiveFails);
    }

    @Override
    public void savePersonalRecord(PersonalRecord r) {
        records.put(r.exerciseId, r);
    }

    @Override
    public OptionalDouble bestE1rm(Exercise e) {
        Double v = e1rms.get(e.id);
        return v == null ? OptionalDouble.empty() : OptionalDouble.of(v);
    }

    @Override
    public void saveBestE1rm(Exercise e, double e1rm) {
        e1rms.put(e.id, e1rm);
    }

    @Override
    public void recordWorkout(CompletedWorkout workout) {
        history.add(workout);
    }

    @Override
    public List<CompletedWorkout> history(LocalDate today) {
        return new ArrayList<>(history);
    }

    @Override
    public boolean hasLoggedWorkouts() {
        return !history.isEmpty();
    }
}
