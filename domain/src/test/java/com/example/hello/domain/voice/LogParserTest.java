package com.example.hello.domain.voice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.hello.domain.catalog.Catalog;
import org.junit.Test;

public class LogParserTest {

    private static LogParser.Parsed p(String s) {
        return LogParser.parse(s);
    }

    @Test
    public void setsOfRepsAtWeight() {
        LogParser.Parsed r = p("Squat, 3 sets of 5 at 280");
        assertEquals(Catalog.SQUAT, r.exercise.get());
        assertEquals(3, r.sets.getAsInt());
        assertEquals(5, r.reps.getAsInt());
        assertEquals(280, r.weight.getAsDouble(), 0);
    }

    @Test
    public void spokenNumbers() {
        LogParser.Parsed r = p("bench three sets of five at 190 pounds");
        assertEquals(Catalog.BENCH, r.exercise.get());
        assertEquals(3, r.sets.getAsInt());
        assertEquals(5, r.reps.getAsInt());
        assertEquals(190, r.weight.getAsDouble(), 0);
    }

    @Test
    public void setsByReps() {
        LogParser.Parsed r = p("3 by 5 squat");
        assertEquals(3, r.sets.getAsInt());
        assertEquals(5, r.reps.getAsInt());
        r = p("deadlift 1x5 at 375");
        assertEquals(Catalog.DEADLIFT, r.exercise.get());
        assertEquals(1, r.sets.getAsInt());
        assertEquals(5, r.reps.getAsInt());
        assertEquals(375, r.weight.getAsDouble(), 0);
    }

    @Test
    public void weightForReps() {
        LogParser.Parsed r = p("bench 190 for 5");
        assertEquals(190, r.weight.getAsDouble(), 0);
        assertEquals(5, r.reps.getAsInt());
        assertFalse(r.sets.isPresent());
    }

    @Test
    public void repsOnly() {
        assertEquals(4, p("did 4").reps.getAsInt());
        assertEquals(3, p("only got three reps").reps.getAsInt());
        assertEquals(5, p("5 reps").reps.getAsInt());
    }

    @Test
    public void longestNameWins() {
        assertEquals(Catalog.BENCH, p("bench press 190 for 5").exercise.get());
        assertEquals(Catalog.OVERHEAD_PRESS, p("overhead press 3x5").exercise.get());
        assertEquals(Catalog.OVERHEAD_PRESS, p("OHP 115 for 5").exercise.get());
        assertEquals(
                Catalog.INCLINE_DB,
                p("incline dumbbell press 3 sets of 10").exercise.get());
        assertEquals(Catalog.BARBELL_ROW, p("rows 135 for 5").exercise.get());
    }

    @Test
    public void nothingUsefulIsEmpty() {
        assertTrue(p("hello there").isEmpty());
        assertTrue(p("").isEmpty());
    }

    @Test
    public void wordsInsideOtherWordsDontMatch() {
        assertFalse(p("impressive").exercise.isPresent());
    }
}
