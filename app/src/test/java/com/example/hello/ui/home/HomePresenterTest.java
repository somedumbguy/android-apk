package com.example.hello.ui.home;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.hello.auth.User;
import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.fakes.TestClocks;
import com.example.hello.domain.fakes.TestTraining;
import com.example.hello.domain.progression.Intensity;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

public class HomePresenterTest {
    private static final User USER = new User("u1", "Test User", "test@example.com");

    private TestTraining t;
    private HomePresenter presenter;

    @Before public void setUp() {
        t = new TestTraining();
        t.workouts.withCleanSession(Catalog.BENCH, 185)
                .withPr(Catalog.BENCH, 185, TestClocks.daysAgo(14))
                .withPr(Catalog.SQUAT, 315, TestClocks.daysAgo(9))
                .withPr(Catalog.DEADLIFT, 405, TestClocks.daysAgo(2))
                .withStreak(12, 21);
        presenter = new HomePresenter(t.service, t.clock, Locale.US);
    }

    @Test public void headerGreetsTheUserByTimeOfDay() {
        HomeState s = presenter.present(USER);
        assertEquals("SATURDAY, NOV 21", s.date);
        assertEquals("Good evening,\nTest", s.greeting);
        assertEquals("T", s.initial);
    }

    @Test public void recordsAreNewestFirstAndOnlyRecentOnesAreFresh() {
        HomeState s = presenter.present(USER);
        assertEquals(3, s.records.size());
        assertEquals("DEADLIFT", s.records.get(0).lift);
        assertEquals("SQUAT", s.records.get(1).lift);
        assertEquals("BENCH PRESS", s.records.get(2).lift);
        assertTrue(s.records.get(0).fresh);
        assertFalse(s.records.get(1).fresh);
        assertEquals("2 days ago", s.records.get(0).when);
    }

    @Test public void recordsForecastTheNextPlateMilestone() {
        HomeState.RecordCard bench = presenter.present(USER).records.get(2);
        assertEquals("185", bench.weight);
        assertEquals("NEXT · 225 LB", bench.next);
        assertEquals("~Jan 7", bench.nextDate);
    }

    @Test public void targetShowsAPrAttemptWhenTheWeightBeatsThePr() {
        HomeState.Target target = presenter.present(USER).target;
        assertEquals("190", target.weight);
        assertTrue(target.prAttempt);
        assertEquals("🏆  PR ATTEMPT", target.badge);
        assertEquals("BUILD", target.intensity);
    }

    @Test public void targetShowsTheChangeWhenNotAPrAttempt() {
        t.workouts.withPr(Catalog.BENCH, 250, TestClocks.NOW);
        HomeState.Target target = presenter.present(USER).target;
        assertFalse(target.prAttempt);
        assertEquals("▲ +5 LB VS LAST SESSION", target.badge);
    }

    @Test public void intensityChangesTheTarget() {
        t.service.setIntensity(Intensity.SEND_IT);
        HomeState.Target target = presenter.present(USER).target;
        assertEquals("192.5", target.weight);
        assertEquals("SEND IT", target.intensity);
    }

    @Test public void statsComeFromActivity() {
        HomeState.Stats s = presenter.present(USER).stats;
        assertEquals("12", s.streak);
        assertEquals("BEST · 21", s.best);
        assertEquals(7, s.volumes.length);
        assertEquals("0", s.totalVolume);
    }
}
