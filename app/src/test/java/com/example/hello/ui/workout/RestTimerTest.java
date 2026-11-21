package com.example.hello.ui.workout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RestTimerTest {
    private long now = 1_000_000;
    private final RestTimer timer = new RestTimer(() -> now);

    @Test public void countsDownAndFinishes() {
        timer.start(90);
        assertEquals("1:30", timer.label());
        assertEquals(1f, timer.fractionRemaining(), 0.001);

        now += 45_000;
        assertEquals("0:45", timer.label());
        assertEquals(0.5f, timer.fractionRemaining(), 0.001);
        assertFalse(timer.isFinished());

        now += 45_000;
        assertTrue(timer.isFinished());
        assertEquals("0:00", timer.label());
    }

    @Test public void roundsUpSoItNeverShowsZeroEarly() {
        timer.start(60);
        now += 59_500;
        assertEquals("0:01", timer.label());
    }

    @Test public void addingTimeExtendsTheRest() {
        timer.start(60);
        now += 50_000;
        timer.addSeconds(30);
        assertEquals("0:40", timer.label());
    }

    @Test public void stoppedTimerHasNoTimeLeftAndIgnoresExtensions() {
        timer.start(60);
        timer.stop();
        timer.addSeconds(30);
        assertFalse(timer.isRunning());
        assertFalse(timer.isFinished());
        assertEquals(0, timer.remainingMs());
    }
}
