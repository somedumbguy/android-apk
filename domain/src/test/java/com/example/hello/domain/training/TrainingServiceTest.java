package com.example.hello.domain.training;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.fakes.TestClocks;
import com.example.hello.domain.fakes.TestTraining;
import com.example.hello.domain.model.SetRecord;
import com.example.hello.domain.model.WorkoutPlan;
import com.example.hello.domain.progression.Intensity;
import com.example.hello.domain.session.ExerciseEntry;
import com.example.hello.domain.session.SetEntry;
import com.example.hello.domain.session.WorkoutSession;
import java.time.LocalDate;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

public class TrainingServiceTest {
    private TestTraining t;
    private final WorkoutPlan benchAndDb = new WorkoutPlan("Test", Arrays.asList(Catalog.BENCH, Catalog.INCLINE_DB));

    @Before
    public void setUp() {
        t = new TestTraining();
        t.workouts
                .withCleanSession(Catalog.BENCH, 185)
                .withPr(Catalog.BENCH, 185, TestClocks.daysAgo(14))
                .withSession(
                        Catalog.INCLINE_DB,
                        new SetRecord(50, 12, 8),
                        new SetRecord(50, 11, 8),
                        new SetRecord(50, 10, 8));
    }

    private static void completeAll(ExerciseEntry e) {
        for (SetEntry s : e.work) s.complete();
    }

    @Test
    public void suggestionUsesTheSavedIntensity() {
        assertEquals(190, t.service.suggest(Catalog.BENCH).weight, 0);
        t.service.setIntensity(Intensity.SEND_IT);
        assertEquals(192.5, t.service.suggest(Catalog.BENCH).weight, 0);
    }

    @Test
    public void prAttemptWhenPrescriptionBeatsThePr() {
        assertTrue(t.service.isPrAttempt(t.service.suggest(Catalog.BENCH)));
        t.workouts.withPr(Catalog.BENCH, 200, TestClocks.NOW);
        assertFalse(t.service.isPrAttempt(t.service.suggest(Catalog.BENCH)));
    }

    @Test
    public void noPrRecordedMeansNoPrAttemptBadge() {
        assertFalse(t.service.isPrAttempt(t.service.suggest(Catalog.SQUAT)));
    }

    @Test
    public void forecastTargetsTheMilestoneAboveThePr() {
        assertEquals(225, t.service.forecastNextMilestone(Catalog.BENCH, Intensity.BUILD).target, 0);
    }

    @Test
    public void startSessionAddsWarmupsOnlyForBarbellLifts() {
        WorkoutSession s = t.service.startSession(benchAndDb);
        ExerciseEntry bench = s.entries.get(0);
        ExerciseEntry db = s.entries.get(1);
        assertFalse(bench.warmups.isEmpty());
        assertTrue(db.warmups.isEmpty());
        assertEquals(3, bench.work.size());
        assertEquals(190, bench.work.get(0).weight, 0);
        assertTrue(bench.prAttempt);
        assertEquals(6, s.totalWorkSets());
    }

    @Test
    public void completingSavesSetsAndProgressesNextTime() {
        WorkoutSession s = t.service.startSession(benchAndDb);
        completeAll(s.entries.get(0));
        WorkoutSummary summary = t.service.complete(s);

        assertEquals(3, summary.sets);
        assertEquals(190 * 15, summary.volume, 0);
        assertEquals(195, t.service.suggest(Catalog.BENCH).weight, 0);
    }

    @Test
    public void beatingThePrIsRecorded() {
        WorkoutSession s = t.service.startSession(benchAndDb);
        completeAll(s.entries.get(0));
        WorkoutSummary summary = t.service.complete(s);

        assertEquals(1, summary.newRecords.size());
        assertEquals(Catalog.BENCH, summary.newRecords.get(0).exercise);
        assertEquals(190, t.service.personalRecord(Catalog.BENCH).get().weight, 0);
        assertEquals(TestClocks.NOW, t.service.personalRecord(Catalog.BENCH).get().achievedAt);
    }

    @Test
    public void zeroRepSetsDoNotCountTowardAPr() {
        WorkoutSession s = t.service.startSession(benchAndDb);
        for (SetEntry set : s.entries.get(0).work) set.record(0);
        assertTrue(t.service.complete(s).newRecords.isEmpty());
    }

    @Test
    public void untouchedExercisesAreNotSaved() {
        WorkoutSession s = t.service.startSession(benchAndDb);
        completeAll(s.entries.get(0));
        t.service.complete(s);
        assertEquals(50, t.workouts.lastSession(Catalog.INCLINE_DB).get(0).weight, 0);
        assertEquals(12, t.workouts.lastSession(Catalog.INCLINE_DB).get(0).reps);
    }

    @Test
    public void missesAccumulateAndHitsReset() {
        WorkoutSession s = t.service.startSession(benchAndDb);
        s.entries.get(0).work.get(0).record(3);
        t.service.complete(s);
        assertEquals(1, t.workouts.consecutiveFails(Catalog.BENCH));

        s = t.service.startSession(benchAndDb);
        s.entries.get(0).work.get(0).record(3);
        t.service.complete(s);
        assertEquals(2, t.workouts.consecutiveFails(Catalog.BENCH));

        s = t.service.startSession(benchAndDb);
        completeAll(s.entries.get(0));
        t.service.complete(s);
        assertEquals(0, t.workouts.consecutiveFails(Catalog.BENCH));
    }

    @Test
    public void aMissAfterADeloadStartsTheCountOver() {
        t.workouts
                .withFails(Catalog.BENCH, 3)
                .withSession(
                        Catalog.BENCH, new SetRecord(205, 5, 5), new SetRecord(205, 4, 5), new SetRecord(205, 3, 5));
        WorkoutSession s = t.service.startSession(benchAndDb);
        assertEquals(185, s.entries.get(0).suggestion.weight, 0);
        s.entries.get(0).work.get(0).record(2);
        t.service.complete(s);
        assertEquals(1, t.workouts.consecutiveFails(Catalog.BENCH));
    }

    @Test
    public void volumeIsAddedToToday() {
        WorkoutSession s = t.service.startSession(benchAndDb);
        completeAll(s.entries.get(0));
        t.service.complete(s);
        assertEquals(190 * 15, t.service.activity().last7DaysVolume()[6], 0);
        assertEquals(1, t.service.activity().workoutsThisWeek);
        assertEquals(
                LocalDate.of(2026, 11, 21),
                t.workouts.history(LocalDate.of(2026, 11, 21)).get(0).day);
    }

    @Test
    public void finishingWithNothingDoneSavesNothing() {
        WorkoutSummary summary = t.service.complete(t.service.startSession(benchAndDb));
        assertEquals(0, summary.sets);
        assertFalse(t.workouts.hasLoggedWorkouts());
    }
}
