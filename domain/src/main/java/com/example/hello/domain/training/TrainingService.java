package com.example.hello.domain.training;

import com.example.hello.domain.forecast.Forecast;
import com.example.hello.domain.forecast.Forecaster;
import com.example.hello.domain.history.ActivitySummary;
import com.example.hello.domain.history.SettingsRepository;
import com.example.hello.domain.history.WorkoutRepository;
import com.example.hello.domain.model.Exercise;
import com.example.hello.domain.model.PersonalRecord;
import com.example.hello.domain.model.SetRecord;
import com.example.hello.domain.model.Suggestion;
import com.example.hello.domain.model.WorkoutPlan;
import com.example.hello.domain.plates.PlateMath;
import com.example.hello.domain.progression.Intensity;
import com.example.hello.domain.progression.ProgressionEngine;
import com.example.hello.domain.session.ExerciseEntry;
import com.example.hello.domain.session.SetEntry;
import com.example.hello.domain.session.WorkoutSession;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The app's training use cases: what to lift next, PR forecasts, starting and finishing
 * workouts. All dependencies are passed in, so it runs in tests with fakes and a fixed clock.
 */
public final class TrainingService {
    private final WorkoutRepository workouts;
    private final SettingsRepository settings;
    private final ProgressionEngine engine;
    private final Forecaster forecaster;
    private final PlateMath plates;
    private final Clock clock;

    public TrainingService(WorkoutRepository workouts, SettingsRepository settings, ProgressionEngine engine,
                           Forecaster forecaster, PlateMath plates, Clock clock) {
        this.workouts = workouts;
        this.settings = settings;
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
        return workouts.activity(LocalDate.now(clock));
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
        return new WorkoutSession(plan, entries);
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
            if (best > currentPr) {
                workouts.savePersonalRecord(new PersonalRecord(e.id, best, clock.instant()));
                records.add(new WorkoutSummary.NewRecord(e, best));
            }
        }

        if (sets > 0) workouts.addVolume(LocalDate.now(clock), volume);
        return new WorkoutSummary(volume, sets, records);
    }
}
