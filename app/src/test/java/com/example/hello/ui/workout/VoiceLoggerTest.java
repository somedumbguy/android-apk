package com.example.hello.ui.workout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.fakes.TestTraining;
import com.example.hello.domain.session.SetEntry;
import com.example.hello.domain.session.WorkoutSession;
import com.example.hello.domain.voice.LogParser;
import org.junit.Before;
import org.junit.Test;

public class VoiceLoggerTest {
    private WorkoutSession session; // Workout A: Squat 280, Bench 190, Row 140

    @Before
    public void setUp() {
        TestTraining t = new TestTraining();
        t.workouts
                .withCleanSession(Catalog.SQUAT, 275)
                .withCleanSession(Catalog.BENCH, 185)
                .withCleanSession(Catalog.BARBELL_ROW, 135);
        session = t.service.startSession(Catalog.WORKOUT_A);
    }

    private VoiceLogger.Result say(String s) {
        return VoiceLogger.apply(session, LogParser.parse(s));
    }

    @Test
    public void logsSeveralSetsOfANamedLift() {
        VoiceLogger.Result r = say("bench 3 sets of 5 at 190");
        assertTrue(r.logged);
        assertEquals("Logged Bench Press: 3 × 5 at 190 lb", r.message);
        for (SetEntry s : session.entries.get(1).work) assertEquals(5, s.reps());
        assertFalse(session.entries.get(0).work.get(0).isDone());
        assertEquals(session.entries.get(1).work.get(2), r.lastSet);
    }

    @Test
    public void withNoLiftNamedItUsesTheCurrentOne() {
        VoiceLogger.Result r = say("did 4");
        assertTrue(r.logged);
        assertEquals("Logged Squat set 1: 4 reps at 280 lb", r.message);
        assertEquals(SetEntry.Status.SHORT, session.entries.get(0).work.get(0).status());
    }

    @Test
    public void continuesFromTheNextPendingSet() {
        say("squat 5 reps");
        VoiceLogger.Result r = say("squat 5 reps");
        assertEquals("Logged Squat set 2: 5 reps at 280 lb", r.message);
    }

    @Test
    public void neverLogsMoreSetsThanArePending() {
        assertEquals("Logged Squat: 3 × 5 at 280 lb", say("squat 5 sets of 5").message);
    }

    @Test
    public void aDifferentWeightIsNotLogged() {
        VoiceLogger.Result r = say("squat 3 sets of 5 at 225");
        assertFalse(r.logged);
        assertTrue(r.message.contains("Today's Squat is 280 lb, but I heard 225 lb"));
        assertFalse(session.entries.get(0).work.get(0).isDone());
    }

    @Test
    public void liftsNotInTodaysWorkoutAreRefused() {
        VoiceLogger.Result r = say("deadlift 1x5");
        assertFalse(r.logged);
        assertEquals("Deadlift isn't in today's workout.", r.message);
    }

    @Test
    public void nothingUnderstoodGivesAnExample() {
        VoiceLogger.Result r = say("uh what");
        assertFalse(r.logged);
        assertTrue(r.message.startsWith("Didn't catch that."));
    }

    @Test
    public void finishedLiftsAndWorkoutsAreReported() {
        say("squat 3x5");
        assertEquals("All Squat sets are already logged.", say("squat 5 reps").message);
        say("bench 3x5");
        say("row 3x5");
        assertEquals("Every set is already logged.", say("did 5").message);
    }
}
