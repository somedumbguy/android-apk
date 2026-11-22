package com.example.hello.domain.model;

import static org.junit.Assert.assertEquals;

import com.example.hello.domain.catalog.Catalog;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;

public class ProgramTest {
    private final Program ab = Catalog.FULL_BODY_AB;

    @Test
    public void startsAtTheFirstWorkout() {
        assertEquals(Catalog.WORKOUT_A, ab.next(Optional.empty()));
    }

    @Test
    public void alternates() {
        assertEquals(Catalog.WORKOUT_B, ab.next(Optional.of("Workout A")));
        assertEquals(Catalog.WORKOUT_A, ab.next(Optional.of("Workout B")));
    }

    @Test
    public void aWorkoutFromAnotherProgramStartsFromTheBeginning() {
        assertEquals(Catalog.WORKOUT_A, ab.next(Optional.of("Push Day")));
    }

    @Test
    public void aSingleWorkoutProgramRepeatsIt() {
        assertEquals(Catalog.PUSH_DAY, Catalog.PUSH.next(Optional.of("Push Day")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void needsAtLeastOneWorkout() {
        new Program("x", "Empty", "", Collections.emptyList());
    }

    @Test
    public void programsAreFoundById() {
        assertEquals(Catalog.PUSH, Catalog.programById("push").get());
        assertEquals(false, Catalog.programById("nope").isPresent());
    }
}
