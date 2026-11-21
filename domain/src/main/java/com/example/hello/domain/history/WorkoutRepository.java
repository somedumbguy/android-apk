package com.example.hello.domain.history;

import com.example.hello.domain.model.Exercise;
import com.example.hello.domain.model.PersonalRecord;
import com.example.hello.domain.model.SetRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Where training history is stored. Implemented on the phone now, by a server later. */
public interface WorkoutRepository {

    /** The previous session's work sets, or an empty list if never done. */
    List<SetRecord> lastSession(Exercise exercise);

    /** Sessions in a row that missed the target. */
    int consecutiveFails(Exercise exercise);

    Optional<PersonalRecord> personalRecord(Exercise exercise);

    void saveSession(Exercise exercise, List<SetRecord> sets, int consecutiveFails);

    void savePersonalRecord(PersonalRecord record);

    void addVolume(LocalDate day, double volume);

    ActivitySummary activity(LocalDate today);
}
