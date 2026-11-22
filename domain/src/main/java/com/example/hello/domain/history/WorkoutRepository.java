package com.example.hello.domain.history;

import com.example.hello.domain.model.CompletedWorkout;
import com.example.hello.domain.model.Exercise;
import com.example.hello.domain.model.PersonalRecord;
import com.example.hello.domain.model.SetRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/** Where training history is stored. Implemented on the phone now, by a server later. */
public interface WorkoutRepository {

    /** The previous session's work sets, or an empty list if never done. */
    List<SetRecord> lastSession(Exercise exercise);

    /** Sessions in a row that missed the target. */
    int consecutiveFails(Exercise exercise);

    Optional<PersonalRecord> personalRecord(Exercise exercise);

    void saveSession(Exercise exercise, List<SetRecord> sets, int consecutiveFails);

    void savePersonalRecord(PersonalRecord record);

    /** The best estimated one-rep max ever achieved, if any. */
    OptionalDouble bestE1rm(Exercise exercise);

    void saveBestE1rm(Exercise exercise, double e1rm);

    void recordWorkout(CompletedWorkout workout);

    /**
     * Finished workouts, oldest first. Until a real workout is logged this may be sample data
     * (see {@link #hasLoggedWorkouts()}), generated relative to {@code today}.
     */
    List<CompletedWorkout> history(LocalDate today);

    /** False while {@link #history} is sample data. */
    boolean hasLoggedWorkouts();
}
