package com.example.hello.domain.history;

import com.example.hello.domain.model.CompletedWorkout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Turns the workout history into streaks, weekly progress and volume. Weeks run Monday to
 * Sunday. A streak counts weeks in a row that hit the weekly goal; the current week counts
 * once it's hit, and doesn't break the streak while it's still in progress.
 */
public final class ActivityCalculator {
    private ActivityCalculator() {}

    public static ActivitySummary summarize(
            List<CompletedWorkout> history, LocalDate today, int weeklyGoal, boolean demo) {
        Map<LocalDate, Double> volumeByDay = new HashMap<>();
        Map<LocalDate, Set<LocalDate>> daysByWeek = new HashMap<>();
        for (CompletedWorkout w : history) {
            volumeByDay.merge(w.day, w.volume, Double::sum);
            daysByWeek.computeIfAbsent(weekOf(w.day), k -> new HashSet<>()).add(w.day);
        }

        double[] last7 = new double[7];
        double previous7 = 0;
        for (int i = 0; i < 7; i++) {
            last7[i] = volumeByDay.getOrDefault(today.minusDays(6 - i), 0.0);
            previous7 += volumeByDay.getOrDefault(today.minusDays(13 - i), 0.0);
        }
        double recent7 = 0;
        for (double v : last7) recent7 += v;
        OptionalInt change = previous7 > 0
                ? OptionalInt.of((int) Math.round((recent7 - previous7) / previous7 * 100))
                : OptionalInt.empty();

        LocalDate thisWeek = weekOf(today);
        int thisWeekCount = daysByWeek.getOrDefault(thisWeek, new HashSet<>()).size();

        return new ActivitySummary(
                currentStreak(daysByWeek, thisWeek, weeklyGoal),
                bestStreak(daysByWeek, weeklyGoal),
                thisWeekCount,
                weeklyGoal,
                last7,
                change,
                demo);
    }

    /** The Monday that starts the week containing {@code day}. */
    static LocalDate weekOf(LocalDate day) {
        return day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private static boolean hitGoal(Map<LocalDate, Set<LocalDate>> days, LocalDate week, int goal) {
        Set<LocalDate> d = days.get(week);
        return d != null && d.size() >= goal;
    }

    private static int currentStreak(Map<LocalDate, Set<LocalDate>> days, LocalDate thisWeek, int goal) {
        int streak = hitGoal(days, thisWeek, goal) ? 1 : 0;
        for (LocalDate w = thisWeek.minusWeeks(1); hitGoal(days, w, goal); w = w.minusWeeks(1)) streak++;
        return streak;
    }

    private static int bestStreak(Map<LocalDate, Set<LocalDate>> days, int goal) {
        int best = 0;
        for (LocalDate week : days.keySet()) {
            if (!hitGoal(days, week, goal) || hitGoal(days, week.minusWeeks(1), goal)) continue;
            int run = 0;
            for (LocalDate w = week; hitGoal(days, w, goal); w = w.plusWeeks(1)) run++;
            best = Math.max(best, run);
        }
        return best;
    }
}
