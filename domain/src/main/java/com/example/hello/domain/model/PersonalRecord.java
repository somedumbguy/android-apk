package com.example.hello.domain.model;

import java.time.Instant;

/** The heaviest weight lifted for an exercise, and when. */
public final class PersonalRecord {
    public final String exerciseId;
    public final double weight;
    public final Instant achievedAt;

    public PersonalRecord(String exerciseId, double weight, Instant achievedAt) {
        this.exerciseId = exerciseId;
        this.weight = weight;
        this.achievedAt = achievedAt;
    }
}
