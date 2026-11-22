package com.example.hello.data;

import android.content.SharedPreferences;
import com.example.hello.domain.history.WorkoutRepository;
import com.example.hello.domain.model.CompletedWorkout;
import com.example.hello.domain.model.Exercise;
import com.example.hello.domain.model.PersonalRecord;
import com.example.hello.domain.model.SetRecord;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Stores training history on the phone, falling back to seed data for anything not yet logged. */
public final class SharedPrefsWorkoutRepository implements WorkoutRepository {
    private final SharedPreferences prefs;
    private final SeedData seed;
    private final Clock clock;

    public SharedPrefsWorkoutRepository(SharedPreferences prefs, SeedData seed, Clock clock) {
        this.prefs = prefs;
        this.seed = seed;
        this.clock = clock;
    }

    @Override
    public List<SetRecord> lastSession(Exercise e) {
        String s = prefs.getString(Keys.sets(e), null);
        if (s != null) return SetRecordCodec.decode(s);
        List<SetRecord> seeded = seed.sessions.get(e.id);
        return seeded == null ? new ArrayList<>() : new ArrayList<>(seeded);
    }

    @Override
    public int consecutiveFails(Exercise e) {
        return prefs.getInt(Keys.fails(e), 0);
    }

    @Override
    public Optional<PersonalRecord> personalRecord(Exercise e) {
        String w = prefs.getString(Keys.prWeight(e), null);
        if (w != null) {
            return Optional.of(new PersonalRecord(
                    e.id, Double.parseDouble(w), Instant.ofEpochMilli(prefs.getLong(Keys.prTime(e), 0))));
        }
        Double seeded = seed.prWeights.get(e.id);
        if (seeded == null) return Optional.empty();
        Integer daysAgo = seed.prDaysAgo.get(e.id);
        Instant at = clock.instant().minus(Duration.ofDays(daysAgo == null ? 0 : daysAgo));
        return Optional.of(new PersonalRecord(e.id, seeded, at));
    }

    @Override
    public void saveSession(Exercise e, List<SetRecord> sets, int fails) {
        prefs.edit()
                .putString(Keys.sets(e), SetRecordCodec.encode(sets))
                .putInt(Keys.fails(e), fails)
                .apply();
    }

    @Override
    public void savePersonalRecord(PersonalRecord r) {
        prefs.edit()
                .putString(Keys.PR_WEIGHT + r.exerciseId, String.valueOf(r.weight))
                .putLong(Keys.PR_TIME + r.exerciseId, r.achievedAt.toEpochMilli())
                .apply();
    }

    @Override
    public void recordWorkout(CompletedWorkout workout) {
        prefs.edit()
                .putString(Keys.LOG, WorkoutLogCodec.append(prefs.getString(Keys.LOG, null), workout))
                .apply();
    }

    @Override
    public List<CompletedWorkout> history(LocalDate today) {
        return hasLoggedWorkouts() ? WorkoutLogCodec.decode(prefs.getString(Keys.LOG, null)) : seed.demoHistory(today);
    }

    @Override
    public boolean hasLoggedWorkouts() {
        return prefs.contains(Keys.LOG);
    }

    /** Storage keys, keyed by stable exercise id. */
    private static final class Keys {
        static final String PR_WEIGHT = "pr:";
        static final String PR_TIME = "prTime:";
        static final String LOG = "log";

        static String sets(Exercise e) {
            return "sets:" + e.id;
        }

        static String fails(Exercise e) {
            return "fails:" + e.id;
        }

        static String prWeight(Exercise e) {
            return PR_WEIGHT + e.id;
        }

        static String prTime(Exercise e) {
            return PR_TIME + e.id;
        }
    }
}
