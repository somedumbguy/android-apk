package com.example.hello.domain.session;

import com.example.hello.domain.model.WorkoutPlan;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** A workout in progress. Holds all set state so the screen only has to draw it. */
public final class WorkoutSession {
    public final WorkoutPlan plan;
    public final List<ExerciseEntry> entries;
    public final Instant startedAt;

    public WorkoutSession(WorkoutPlan plan, List<ExerciseEntry> entries, Instant startedAt) {
        this.plan = plan;
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
        this.startedAt = startedAt;
    }

    /** True once any work set has been checked off. */
    public boolean hasProgress() {
        for (ExerciseEntry e : entries) if (!e.completedWorkSets().isEmpty()) return true;
        return false;
    }

    /** True once any set, warm-ups included, has been checked off. */
    public boolean hasAnySetDone() {
        for (ExerciseEntry e : entries) {
            for (SetEntry s : e.warmups) if (s.isDone()) return true;
            for (SetEntry s : e.work) if (s.isDone()) return true;
        }
        return false;
    }

    public int totalWorkSets() {
        int n = 0;
        for (ExerciseEntry e : entries) n += e.work.size();
        return n;
    }

    /** The first work set not yet done, in workout order, if any. */
    public Optional<SetEntry> nextPendingWorkSet() {
        for (ExerciseEntry e : entries) {
            for (SetEntry s : e.work) if (!s.isDone()) return Optional.of(s);
        }
        return Optional.empty();
    }

    /** The exercise a set belongs to, or null. */
    public ExerciseEntry entryFor(SetEntry set) {
        for (ExerciseEntry e : entries) if (e.contains(set)) return e;
        return null;
    }
}
