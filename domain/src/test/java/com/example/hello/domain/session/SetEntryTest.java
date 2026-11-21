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
}
