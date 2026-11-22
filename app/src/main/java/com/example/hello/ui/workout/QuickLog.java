package com.example.hello.ui.workout;

import com.example.hello.domain.model.WorkoutPlan;
import com.example.hello.domain.session.ExerciseEntry;
import com.example.hello.domain.session.SetEntry;
import com.example.hello.domain.session.WorkoutSession;
import com.example.hello.domain.training.TrainingService;
import java.util.Optional;

/**
 * Logging from outside the app, e.g. the rest notification's "Done" button: marks the next
 * pending set as done as planned, then starts the rest before the set after it.
 */
public final class QuickLog {
    private final TrainingService training;
    private final RestController rest;

    public QuickLog(TrainingService training, RestController rest) {
        this.training = training;
        this.rest = rest;
    }

    /** Returns true if a set was logged. */
    public boolean logNextSet(WorkoutPlan plan) {
        WorkoutSession session = training.resumeOrStart(plan);
        Optional<SetEntry> next = session.nextPendingWorkSet();
        if (!next.isPresent()) {
            rest.stop();
            return false;
        }
        SetEntry set = next.get();
        set.complete();
        training.saveProgress(session);
        startRestAfter(session, set);
        return true;
    }

    /** Starts the rest that follows {@code justDone}, or stops it when nothing's left. */
    public void startRestAfter(WorkoutSession session, SetEntry justDone) {
        Optional<SetEntry> following = session.nextPendingWorkSet();
        if (!following.isPresent()) {
            rest.stop();
            return;
        }
        ExerciseEntry entry = session.entryFor(justDone);
        rest.start(entry.exercise.restSeconds, WorkoutFormatter.nextSet(session, following.get()));
    }
}
