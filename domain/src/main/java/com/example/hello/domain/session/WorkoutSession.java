package com.example.hello.domain.session;

import com.example.hello.domain.model.WorkoutPlan;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A workout in progress. Holds all set state so the screen only has to draw it. */
public final class WorkoutSession {
    public final WorkoutPlan plan;
    public final List<ExerciseEntry> entries;

    public WorkoutSession(WorkoutPlan plan, List<ExerciseEntry> entries) {
        this.plan = plan;
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
    }

    /** True once any work set has been checked off. */
    public boolean hasProgress() {
        for (ExerciseEntry e : entries) if (!e.completedWorkSets().isEmpty()) return true;
        return false;
    }

    public int totalWorkSets() {
        int n = 0;
        for (ExerciseEntry e : entries) n += e.work.size();
        return n;
    }

    /** The exercise a set belongs to, or null. */
    public ExerciseEntry entryFor(SetEntry set) {
        for (ExerciseEntry e : entries) if (e.contains(set)) return e;
        return null;
    }
}
