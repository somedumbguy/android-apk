package com.example.hello.ui.workout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.fakes.InMemoryRestStore;
import com.example.hello.domain.fakes.TestClocks;
import com.example.hello.domain.fakes.TestTraining;
import com.example.hello.domain.model.WorkoutPlan;
import com.example.hello.domain.session.SetEntry;
import com.example.hello.domain.session.WorkoutSession;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

public class QuickLogTest {
    private final WorkoutPlan plan = new WorkoutPlan("Test", Arrays.asList(Catalog.BENCH, Catalog.INCLINE_DB));
    private TestTraining t;
    private RestControllerTest.FakeNotifier notifier;
    private RestController rest;
    private QuickLog quickLog;

    @Before
    public void setUp() {
        t = new TestTraining();
        t.workouts.withCleanSession(Catalog.BENCH, 185).withPr(Catalog.BENCH, 185, TestClocks.daysAgo(14));
        notifier = new RestControllerTest.FakeNotifier();
        rest = new RestController(() -> TestClocks.NOW.toEpochMilli(), new InMemoryRestStore(), notifier);
        quickLog = new QuickLog(t.service, rest);
    }

    @Test
    public void doneLogsTheNextPendingSetAndRestsBeforeTheOneAfter() {
        assertTrue(quickLog.logNextSet(plan));

        WorkoutSession s = t.service.resumeOrStart(plan);
        assertTrue(s.entries.get(0).work.get(0).isDone());
        assertFalse(s.entries.get(0).work.get(1).isDone());
        assertTrue(rest.isRunning());
        assertEquals("Bench Press · set 2 of 3 · 190 lb × 5", notifier.shownNext);
    }

    @Test
    public void restLengthComesFromTheSetJustDone() {
        quickLog.logNextSet(plan);
        assertEquals(TestClocks.NOW.toEpochMilli() + Catalog.BENCH.restSeconds * 1000L, notifier.shownUntil);
    }

    @Test
    public void movesOnToTheNextExercise() {
        quickLog.logNextSet(plan);
        quickLog.logNextSet(plan);
        quickLog.logNextSet(plan);
        assertTrue(notifier.shownNext.startsWith("Incline Dumbbell Press · set 1 of 3"));
    }

    @Test
    public void afterTheLastSetTheRestStops() {
        WorkoutSession s = t.service.resumeOrStart(plan);
        for (int e = 0; e < s.entries.size(); e++) {
            for (SetEntry set : s.entries.get(e).work) set.complete();
        }
        // Undo the very last set, then log it from the notification.
        SetEntry last = s.entries.get(1).work.get(2);
        last.undo();
        t.service.saveProgress(s);

        assertTrue(quickLog.logNextSet(plan));
        assertFalse(rest.isRunning());
        assertFalse(quickLog.logNextSet(plan));
    }
}
