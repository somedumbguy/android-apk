package com.example.hello.domain.training;

import com.example.hello.domain.forecast.Forecast;
import com.example.hello.domain.forecast.Forecaster;
import com.example.hello.domain.history.ActiveWorkoutStore;
import com.example.hello.domain.history.ActivityCalculator;
import com.example.hello.domain.history.ActivitySummary;
import com.example.hello.domain.history.SettingsRepository;
import com.example.hello.domain.history.WorkoutRepository;
import com.example.hello.domain.model.CompletedWorkout;
import com.example.hello.domain.model.Exercise;
import com.example.hello.domain.model.PersonalRecord;
import com.example.hello.domain.model.SetRecord;
import com.example.hello.domain.model.Strength;
import com.example.hello.domain.model.Suggestion;
import com.example.hello.domain.model.WorkoutPlan;
import com.example.hello.domain.plates.PlateMath;
import com.example.hello.domain.progression.Intensity;
import com.example.hello.domain.progression.ProgressionEngine;
import com.example.hello.domain.session.ExerciseEntry;
import com.example.hello.domain.session.SessionCodec;
import com.example.hello.domain.session.SetEntry;
import com.example.hello.domain.session.WorkoutSession;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * The app's training use cases: what to lift next, PR forecasts, starting and finishing
 * workouts. All dependencies are passed in, so it runs in tests with fakes and a fixed clock.
 */
public final class TrainingService {
    /** Ignore rounding noise when comparing estimated maxes. */
    private static final double E1RM_EPSILON = 0.01;

    private final WorkoutRepository workouts;
    private final SettingsRepository settings;
    private final ActiveWorkoutStore activeWorkout;
    private final ProgressionEngine engine;
    private final Forecaster forecaster;
    private final PlateMath plates;
    private final Clock clock;

    public TrainingService(
            WorkoutRepository workouts,
            SettingsRepository settings,
            ActiveWorkoutStore activeWorkout,
            ProgressionEngine engine,
            Forecaster forecaster,
            PlateMath plates,
            Clock clock) {
        this.workouts = workouts;
        this.settings = settings;
        this.activeWorkout = activeWorkout;
        this.engine = engine;
        this.forecaster = forecaster;
        this.plates = plates;
        this.clock = clock;
    }

    // ---- Settings ----

    public Intensity intensity() {
        return settings.intensity();
    }

    public void setIntensity(Intensity intensity) {
        settings.setIntensity(intensity);
    }

    // ---- Prescriptions and records ----

    public Suggestion suggest(Exercise e) {
        return suggest(e, intensity());
    }

    public Suggestion suggest(Exercise e, Intensity intensity) {
        return engine.next(e, workouts.lastSession(e), intensity, workouts.consecutiveFails(e));
    }

    public Optional<PersonalRecord> personalRecord(Exercise e) {
        return workouts.personalRecord(e);
    }

    public OptionalDouble bestE1rm(Exercise e) {
        return workouts.bestE1rm(e);
    }

    /** True when the prescribed weight beats the current PR. */
    public boolean isPrAttempt(Suggestion s) {
        Optional<PersonalRecord> pr = personalRecord(s.exercise);
        return pr.isPresent() && s.weight > pr.get().weight;
    }

    /** When the lift is projected to reach its next plate milestone above the current PR. */
    public Forecast forecastNextMilestone(Exercise e, Intensity intensity) {
        double pr = personalRecord(e).map(r -> r.weight).orElse(0.0);
        return forecaster.project(e, suggest(e, intensity).weight, Forecaster.nextMilestone(pr), intensity);
    }

    public ActivitySummary activity() {
        LocalDate today = LocalDate.now(clock);
        return ActivityCalculator.summarize(
                workouts.history(today), today, settings.weeklyGoal(), !workouts.hasLoggedWorkouts());
    }

    // ---- Workouts ----

    public WorkoutSession startSession(WorkoutPlan plan) {
        List<ExerciseEntry> entries = new ArrayList<>();
        for (Exercise e : plan.exercises) {
            Suggestion s = suggest(e);
            List<SetEntry> warmups = new ArrayList<>();
            if (e.isBarbell()) {
                for (PlateMath.Warmup w : plates.warmups(s.weight)) warmups.add(new SetEntry(w.weight, w.reps, true));
            }
            List<SetEntry> work = new ArrayList<>();
            for (int r : s.reps()) work.add(new SetEntry(s.weight, r, false));
            entries.add(new ExerciseEntry(e, s, isPrAttempt(s), warmups, work));
        }
        return new WorkoutSession(plan, entries, clock.instant());
    }

    /**
     * Today's in-progress workout if one was saved and still matches the prescription,
     * otherwise a fresh one. A saved workout from another day is discarded.
     */
    public WorkoutSession resumeOrStart(WorkoutPlan plan) {
        Optional<WorkoutSession> resumed = savedToday(plan);
        if (resumed.isPresent()) return resumed.get();
        activeWorkout.clear();
        return startSession(plan);
    }

    /** True when there's a saved, unfinished workout from today for this plan. */
    public boolean hasWorkoutInProgress(WorkoutPlan plan) {
        return savedToday(plan).isPresent();
    }

    /** Saves the workout's progress; call after every change. Nothing done means nothing to keep. */
    public void saveProgress(WorkoutSession session) {
        if (session.hasAnySetDone()) activeWorkout.save(SessionCodec.encode(session));
        else activeWorkout.clear();
    }

    /** Throws away the in-progress workout. */
    public void discard() {
        activeWorkout.clear();
    }

    /** Restores onto a session of its own, so a save that's rejected can't leak into another one. */
    private Optional<WorkoutSession> savedToday(WorkoutPlan plan) {
        return activeWorkout
                .load()
                .flatMap(saved -> SessionCodec.restore(saved, startSession(plan)))
                .filter(s -> sameDay(s.startedAt, clock.instant()));
    }

    private boolean sameDay(Instant a, Instant b) {
        return a.atZone(clock.getZone())
                .toLocalDate()
                .equals(b.atZone(clock.getZone()).toLocalDate());
    }

    /** Saves completed sets, updates stall counts and PRs, and returns a summary. */
    public WorkoutSummary complete(WorkoutSession session) {
        double volume = 0;
        int sets = 0;
        List<WorkoutSummary.NewRecord> records = new ArrayList<>();

        for (ExerciseEntry entry : session.entries) {
            List<SetRecord> done = entry.completedWorkSets();
            if (done.isEmpty()) continue;
            Exercise e = entry.exercise;

            boolean allHit = done.size() >= entry.work.size();
            double best = 0;
            for (SetRecord s : done) {
                if (!s.hitTarget()) allHit = false;
                if (s.reps > 0) best = Math.max(best, s.weight);
                volume += s.volume();
                sets++;
            }

            double previous = SetRecord.maxWeight(workouts.lastSession(e));
            // A lighter session after a stall is a deload, so the miss count starts over.
            int fails = allHit ? 0 : (SetRecord.maxWeight(done) < previous ? 1 : workouts.consecutiveFails(e) + 1);
            workouts.saveSession(e, done, fails);

            double currentPr = personalRecord(e).map(r -> r.weight).orElse(0.0);
            boolean heaviest = best > currentPr;
            if (heaviest) {
                workouts.savePersonalRecord(new PersonalRecord(e.id, best, clock.instant()));
                records.add(new WorkoutSummary.NewRecord(e, WorkoutSummary.NewRecord.Kind.HEAVIEST, best));
            }
            // A higher estimated max counts too (e.g. more reps at the same weight), but one set
            // shouldn't announce two records: a heaviest-weight PR already covers it.
            double e1rm = Strength.bestE1rm(done);
            if (e1rm > bestE1rm(e).orElse(0) + E1RM_EPSILON) {
                workouts.saveBestE1rm(e, e1rm);
                if (!heaviest) {
                    records.add(new WorkoutSummary.NewRecord(e, WorkoutSummary.NewRecord.Kind.ESTIMATED_1RM, e1rm));
                }
            }
        }

        if (sets > 0) {
            workouts.recordWorkout(new CompletedWorkout(LocalDate.now(clock), session.plan.name, volume, sets));
        }
        activeWorkout.clear();
        return new WorkoutSummary(volume, sets, records);
    }
}
