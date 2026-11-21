package com.example.hello.domain.session;

import com.example.hello.domain.model.Exercise;
import com.example.hello.domain.model.SetRecord;
import com.example.hello.domain.model.Suggestion;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** One exercise within a workout: its prescription, warm-ups and work sets. */
public final class ExerciseEntry {
    public final Exercise exercise;
    public final Suggestion suggestion;
    public final boolean prAttempt;
    public final List<SetEntry> warmups;
    public final List<SetEntry> work;

    public ExerciseEntry(
            Exercise exercise, Suggestion suggestion, boolean prAttempt, List<SetEntry> warmups, List<SetEntry> work) {
        this.exercise = exercise;
        this.suggestion = suggestion;
        this.prAttempt = prAttempt;
        this.warmups = Collections.unmodifiableList(new ArrayList<>(warmups));
        this.work = Collections.unmodifiableList(new ArrayList<>(work));
    }

    public List<SetRecord> completedWorkSets() {
        List<SetRecord> out = new ArrayList<>();
        for (SetEntry s : work) if (s.isDone()) out.add(s.toRecord());
        return out;
    }

    public boolean contains(SetEntry set) {
        return warmups.contains(set) || work.contains(set);
    }
}
