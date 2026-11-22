package com.example.hello.domain.training;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.fakes.TestClocks;
import com.example.hello.domain.fakes.TestTraining;
import com.example.hello.domain.model.WorkoutPlan;
import com.example.hello.domain.session.WorkoutSession;
import java.util.Arrays;
import org.junit.Test;

public class ResumeWorkoutTest {
    private final WorkoutPlan plan = new WorkoutPlan("Test", Arrays.asList(Catalog.BENCH));

    private static TestTraining trainingAt(java.time.Instant now, TestTraining sharing) {
        TestTraining t = new TestTraining(TestClocks.at(now));
        t.workouts.withCleanSession(Catalog.BENCH, 185);
        if (sharing != null) sharing.activeWorkout.load().ifPresent(t.activeWorkout::save);
        return t;
    }

    @Test
    public void progressIsRestoredLaterTheSameDay() {
        TestTraining morning = trainingAt(TestClocks.NOW, null);
        WorkoutSession s = morning.service.resumeOrStart(plan);
        s.entries.get(0).work.get(0).complete();
        morning.service.saveProgress(s);

        TestTraining later = trainingAt(TestClocks.NOW.plusSeconds(3600), morning);
        assertTrue(later.service.hasWorkoutInProgress(plan));
        WorkoutSession resumed = later.service.resumeOrStart(plan);
        assertTrue(resumed.entries.get(0).work.get(0).isDone());
        assertEquals(s.startedAt, resumed.startedAt);
    }

    @Test
    public void yesterdaysWorkoutIsDiscarded() {
        TestTraining yesterday = trainingAt(TestClocks.NOW.minusSeconds(86_400), null);
        WorkoutSession s = yesterday.service.resumeOrStart(plan);
        s.entries.get(0).work.get(0).complete();
        yesterday.service.saveProgress(s);

        TestTraining today = trainingAt(TestClocks.NOW, yesterday);
        assertFalse(today.service.hasWorkoutInProgress(plan));
        assertFalse(today.service.resumeOrStart(plan).hasAnySetDone());
        assertFalse(today.activeWorkout.load().isPresent());
    }

    @Test
    public void warmupOnlyProgressIsKept() {
        TestTraining t = trainingAt(TestClocks.NOW, null);
        WorkoutSession s = t.service.resumeOrStart(plan);
        s.entries.get(0).warmups.get(0).complete();
        t.service.saveProgress(s);
        assertTrue(t.service.resumeOrStart(plan).entries.get(0).warmups.get(0).isDone());
    }

    @Test
    public void undoingEverythingClearsTheSave() {
        TestTraining t = trainingAt(TestClocks.NOW, null);
        WorkoutSession s = t.service.resumeOrStart(plan);
        s.entries.get(0).work.get(0).complete();
        t.service.saveProgress(s);
        s.entries.get(0).work.get(0).undo();
        t.service.saveProgress(s);
        assertFalse(t.activeWorkout.load().isPresent());
    }

    @Test
    public void finishingOrDiscardingClearsTheSave() {
        TestTraining t = trainingAt(TestClocks.NOW, null);
        WorkoutSession s = t.service.resumeOrStart(plan);
        s.entries.get(0).work.get(0).complete();
        t.service.saveProgress(s);
        t.service.complete(s);
        assertFalse(t.service.hasWorkoutInProgress(plan));

        WorkoutSession next = t.service.resumeOrStart(plan);
        next.entries.get(0).work.get(0).complete();
        t.service.saveProgress(next);
        t.service.discard();
        assertFalse(t.service.hasWorkoutInProgress(plan));
    }
}
