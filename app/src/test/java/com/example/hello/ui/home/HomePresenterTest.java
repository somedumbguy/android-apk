package com.example.hello.ui.home;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.hello.auth.User;
import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.fakes.TestClocks;
import com.example.hello.domain.fakes.TestTraining;
import com.example.hello.domain.progression.Intensity;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;

public class HomePresenterTest {
    private static final User USER = new User("u1", "Test User", "test@example.com");

    private TestTraining t;
    private HomePresenter presenter;

    @Before
    public void setUp() {
        t = new TestTraining();
        t.workouts
                .withCleanSession(Catalog.BENCH, 185)
                .withPr(Catalog.BENCH, 185, TestClocks.daysAgo(14))
                .withPr(Catalog.SQUAT, 315, TestClocks.daysAgo(9))
                .withPr(Catalog.DEADLIFT, 405, TestClocks.daysAgo(2));
        presenter = new HomePresenter(t.service, t.clock, Locale.US);
    }

    @Test
    public void headerGreetsTheUserByTimeOfDay() {
        HomeState s = presenter.present(USER);
        assertEquals("SATURDAY, NOV 21", s.date);
        assertEquals("Good evening,\nTest", s.greeting);
        assertEquals("T", s.initial);
    }

    @Test
    public void recordsAreNewestFirstAndOnlyRecentOnesAreFresh() {
        HomeState s = presenter.present(USER);
        assertEquals(3, s.records.size());
        assertEquals("DEADLIFT", s.records.get(0).lift);
        assertEquals("SQUAT", s.records.get(1).lift);
        assertEquals("BENCH PRESS", s.records.get(2).lift);
        assertTrue(s.records.get(0).fresh);
        assertFalse(s.records.get(1).fresh);
        assertEquals("2 days ago", s.records.get(0).when);
    }

    @Test
    public void recordsForecastTheNextPlateMilestone() {
        HomeState.RecordCard bench = presenter.present(USER).records.get(2);
        assertEquals("185", bench.weight);
        assertEquals("NEXT · 225 LB", bench.next);
        assertEquals("~Jan 7", bench.nextDate);
    }

    @Test
    public void targetShowsAPrAttemptWhenTheWeightBeatsThePr() {
        HomeState.Target target = presenter.present(USER).target;
        assertEquals("190", target.weight);
        assertTrue(target.prAttempt);
        assertEquals("🏆  PR ATTEMPT", target.badge);
        assertEquals("BUILD", target.intensity);
    }

    @Test
    public void targetShowsTheChangeWhenNotAPrAttempt() {
        t.workouts.withPr(Catalog.BENCH, 250, TestClocks.NOW);
        HomeState.Target target = presenter.present(USER).target;
        assertFalse(target.prAttempt);
        assertEquals("▲ +5 LB VS LAST SESSION", target.badge);
    }

    @Test
    public void intensityChangesTheTarget() {
        t.service.setIntensity(Intensity.SEND_IT);
        HomeState.Target target = presenter.present(USER).target;
        assertEquals("192.5", target.weight);
        assertEquals("SEND IT", target.intensity);
    }

    @Test
    public void statsComeFromTheWorkoutHistory() {
        // Today is Saturday 21 Nov; this week started Monday 16 Nov.
        for (int day : new int[] {9, 11, 13}) t.workouts.withWorkout(java.time.LocalDate.of(2026, 11, day), 900);
        for (int day : new int[] {16, 18, 20}) t.workouts.withWorkout(java.time.LocalDate.of(2026, 11, day), 1000);

        HomeState.Stats s = presenter.present(USER).stats;
        assertEquals("2", s.streak);
        assertEquals("weeks in a row", s.streakUnit);
        assertEquals("BEST · 2", s.best);
        assertEquals(3, s.workoutsDone);
        assertEquals("3,000", s.totalVolume);
        assertEquals("▲ 11%", s.change);
        assertTrue(s.changeUp);
        assertFalse(s.demo);
    }

    @Test
    public void withNoHistoryThereIsNothingToCompareAndItsLabelledDemo() {
        HomeState.Stats s = presenter.present(USER).stats;
        assertEquals("0", s.streak);
        assertEquals(null, s.change);
        assertTrue(s.demo);
    }

    @Test
    public void startButtonOffersToResumeAWorkoutInProgress() {
        assertEquals("Start workout   →", presenter.present(USER).target.action);
        com.example.hello.domain.session.WorkoutSession s = t.service.resumeOrStart(Catalog.PUSH_DAY);
        s.entries.get(0).work.get(0).complete();
        t.service.saveProgress(s);
        assertEquals("Resume workout   →", presenter.present(USER).target.action);
    }

    @Test
    public void recordCardsShowTheEstimatedMaxWhenKnown() {
        t.workouts.withE1rm(Catalog.BENCH, 215.83);
        HomeState s = presenter.present(USER);
        assertEquals("EST. 1RM · 216 LB", s.records.get(2).e1rm);
        assertEquals(null, s.records.get(0).e1rm); // deadlift has no estimate in this test
    }
}
