package com.example.hello.domain.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SetEntryTest {

    @Test
    public void startsPending() {
        SetEntry s = new SetEntry(190, 5, false);
        assertFalse(s.isDone());
        assertEquals(SetEntry.Status.PENDING, s.status());
    }

    @Test
    public void toggleCompletesThenUndoes() {
        SetEntry s = new SetEntry(190, 5, false);
        assertTrue(s.toggle());
        assertEquals(5, s.reps());
        assertEquals(SetEntry.Status.DONE, s.status());
        assertFalse(s.toggle());
        assertEquals(SetEntry.Status.PENDING, s.status());
    }

    @Test
    public void fewerRepsThanTargetIsShort() {
        SetEntry s = new SetEntry(190, 5, false);
        s.record(3);
        assertEquals(SetEntry.Status.SHORT, s.status());
        s.record(6);
        assertEquals(SetEntry.Status.DONE, s.status());
    }

    @Test
    public void zeroRepsCountsAsDone() {
        SetEntry s = new SetEntry(190, 5, false);
        s.record(0);
        assertTrue(s.isDone());
        assertEquals(SetEntry.Status.SHORT, s.status());
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeRepsAreRejected() {
        new SetEntry(190, 5, false).record(-1);
    }

    @Test
    public void nextPendingWorkSetSkipsDoneSetsAndWarmups() {
        com.example.hello.domain.fakes.TestTraining t = new com.example.hello.domain.fakes.TestTraining();
        t.workouts.withCleanSession(com.example.hello.domain.catalog.Catalog.BENCH, 185);
        WorkoutSession s = t.service.startSession(new com.example.hello.domain.model.WorkoutPlan(
                "Test", java.util.Arrays.asList(com.example.hello.domain.catalog.Catalog.BENCH)));
        assertTrue(s.nextPendingWorkSet().get() == s.entries.get(0).work.get(0));
        s.entries.get(0).work.get(0).complete();
        assertTrue(s.nextPendingWorkSet().get() == s.entries.get(0).work.get(1));
        for (SetEntry set : s.entries.get(0).work) set.complete();
        assertFalse(s.nextPendingWorkSet().isPresent());
    }
}
