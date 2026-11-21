package com.example.hello.ui.home;

import com.example.hello.auth.User;
import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.forecast.Forecast;
import com.example.hello.domain.history.ActivitySummary;
import com.example.hello.domain.model.Exercise;
import com.example.hello.domain.model.PersonalRecord;
import com.example.hello.domain.model.Suggestion;
import com.example.hello.domain.model.Weights;
import com.example.hello.domain.progression.Intensity;
import com.example.hello.domain.training.TrainingService;
import com.example.hello.ui.format.Formats;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Turns training data into what the home screen shows. No Android code, so it's unit-tested. */
public final class HomePresenter {
    /** PRs newer than this get the "NEW" badge. */
    static final int FRESH_DAYS = 7;

    private final TrainingService training;
    private final Clock clock;
    private final Locale locale;

    public HomePresenter(TrainingService training, Clock clock, Locale locale) {
        this.training = training;
        this.clock = clock;
        this.locale = locale;
    }

    public HomeState present(User user) {
        LocalDate today = LocalDate.now(clock);
        return new HomeState(
                Formats.headerDate(today, locale),
                Formats.greeting(LocalTime.now(clock)) + ",\n" + user.getFirstName(),
                user.getInitial(),
                records(),
                target(),
                stats(today));
    }

    private List<HomeState.RecordCard> records() {
        Instant now = clock.instant();
        Intensity intensity = training.intensity();

        List<PersonalRecord> prs = new ArrayList<>();
        for (Exercise e : Catalog.MAIN_LIFTS) training.personalRecord(e).ifPresent(prs::add);
        prs.sort(Comparator.comparing((PersonalRecord r) -> r.achievedAt).reversed());

        List<HomeState.RecordCard> out = new ArrayList<>();
        for (PersonalRecord pr : prs) {
            Exercise e = Catalog.byId(pr.exerciseId).orElseThrow(IllegalStateException::new);
            Forecast f = training.forecastNextMilestone(e, intensity);
            out.add(new HomeState.RecordCard(
                    e.name.toUpperCase(Locale.US),
                    Weights.format(pr.weight),
                    Formats.relative(pr.achievedAt, now),
                    Formats.isWithinDays(pr.achievedAt, now, FRESH_DAYS),
                    "NEXT · " + Weights.format(f.target) + " LB",
                    "~" + Formats.shortDate(f.date, locale)));
        }
        return out;
    }

    private HomeState.Target target() {
        Exercise e = Catalog.FEATURED;
        Suggestion s = training.suggest(e);
        boolean prAttempt = training.isPrAttempt(s);
        return new HomeState.Target(
                training.intensity().label.toUpperCase(Locale.US),
                "Today · " + Catalog.PUSH_DAY.name + " · " + e.name,
                Weights.format(s.weight),
                s.repsLabel(),
                prAttempt,
                prAttempt ? "🏆  PR ATTEMPT" : Formats.delta(s.delta),
                s.reason);
    }

    private HomeState.Stats stats(LocalDate today) {
        ActivitySummary a = training.activity();
        return new HomeState.Stats(
                String.valueOf(a.streakDays),
                "BEST · " + a.bestStreak,
                a.workoutsThisWeek,
                a.weeklyGoal,
                a.last7DaysVolume(),
                Formats.last7DayLetters(today, locale),
                Formats.thousands(a.totalVolume()),
                (a.volumeChangePct >= 0 ? "▲ " : "▼ ") + Math.abs(a.volumeChangePct) + "%");
    }
}
