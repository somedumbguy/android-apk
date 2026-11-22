package com.example.hello.domain.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.hello.domain.model.CompletedWorkout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ActivityCalculatorTest {
    /** Saturday 21 Nov 2026; its week starts Monday 16 Nov. */
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 21);

    private final List<CompletedWorkout> history = new ArrayList<>();

    private void workout(int month, int day, double volume) {
        history.add(new CompletedWorkout(LocalDate.of(2026, month, day), "Test", volume, 3));
    }

    private ActivitySummary summary(int goal) {
        return ActivityCalculator.summarize(history, TODAY, goal, false);
    }

    @Test
    public void weeksStartOnMonday() {
        assertEquals(LocalDate.of(2026, 11, 16), ActivityCalculator.weekOf(TODAY));
        assertEquals(LocalDate.of(2026, 11, 16), ActivityCalculator.weekOf(LocalDate.of(2026, 11, 16)));
        assertEquals(LocalDate.of(2026, 11, 16), ActivityCalculator.weekOf(LocalDate.of(2026, 11, 22)));
    }

    @Test
    public void emptyHistoryHasNoStreakAndNothingToCompare() {
        ActivitySummary s = summary(3);
        assertEquals(0, s.streakWeeks);
        assertEquals(0, s.bestStreakWeeks);
        assertEquals(0, s.workoutsThisWeek);
        assertFalse(s.volumeChangePct.isPresent());
        assertEquals(0, s.totalVolume(), 0);
    }

    @Test
    public void countsWorkoutDaysThisWeekNotSessions() {
        workout(11, 16, 1000);
        workout(11, 16, 500); // two sessions, one day
        workout(11, 18, 1000);
        workout(11, 15, 1000); // last week
        assertEquals(2, summary(3).workoutsThisWeek);
    }

    @Test
    public void anUnfinishedWeekDoesNotBreakTheStreak() {
        for (int d : new int[] {2, 4, 6, 9, 11, 13}) workout(11, d, 1000); // two full weeks
        workout(11, 16, 1000); // this week: 1 of 3 so far
        assertEquals(2, summary(3).streakWeeks);
    }

    @Test
    public void theCurrentWeekCountsOnceTheGoalIsHit() {
        for (int d : new int[] {9, 11, 13, 16, 18, 20}) workout(11, d, 1000);
        assertEquals(2, summary(3).streakWeeks);
    }

    @Test
    public void aMissedWeekEndsTheStreakButKeepsTheBest() {
        for (int d : new int[] {19, 21, 23, 26, 28, 30}) workout(10, d, 1000); // 2 good weeks in Oct
        workout(11, 2, 1000); // the week of 2 Nov misses the goal
        for (int d : new int[] {9, 11, 13}) workout(11, d, 1000);
        ActivitySummary s = summary(3);
        assertEquals(1, s.streakWeeks);
        assertEquals(2, s.bestStreakWeeks);
    }

    @Test
    public void volumeCoversTheLastSevenDaysAndComparesWithTheSevenBefore() {
        workout(11, 21, 1200); // today
        workout(11, 15, 800); // 6 days ago
        workout(11, 14, 1000); // 7 days ago: previous window
        ActivitySummary s = summary(3);
        double[] v = s.last7DaysVolume();
        assertEquals(800, v[0], 0);
        assertEquals(1200, v[6], 0);
        assertEquals(2000, s.totalVolume(), 0);
        assertEquals(100, s.volumeChangePct.getAsInt());
    }

    @Test
    public void passesTheDemoFlagAndGoalThrough() {
        ActivitySummary s = ActivityCalculator.summarize(history, TODAY, 4, true);
        assertTrue(s.demo);
        assertEquals(4, s.weeklyGoal);
    }
}
