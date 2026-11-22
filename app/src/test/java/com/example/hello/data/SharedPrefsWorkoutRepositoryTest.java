package com.example.hello.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.fakes.TestClocks;
import com.example.hello.domain.history.ActivitySummary;
import com.example.hello.domain.model.PersonalRecord;
import com.example.hello.domain.model.SetRecord;
import com.example.hello.domain.progression.Intensity;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class SharedPrefsWorkoutRepositoryTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 21);

    private final InMemorySharedPreferences prefs = new InMemorySharedPreferences();
    private final SharedPrefsWorkoutRepository repo =
            new SharedPrefsWorkoutRepository(prefs, SeedData.demo(), TestClocks.fixed());

    @Test
    public void fallsBackToSeedDataUntilLogged() {
        assertEquals(185, repo.lastSession(Catalog.BENCH).get(0).weight, 0);
        PersonalRecord pr = repo.personalRecord(Catalog.DEADLIFT).get();
        assertEquals(405, pr.weight, 0);
        assertEquals(TestClocks.NOW.minus(Duration.ofDays(2)), pr.achievedAt);
    }

    @Test
    public void emptySeedHasNoHistory() {
        SharedPrefsWorkoutRepository empty =
                new SharedPrefsWorkoutRepository(new InMemorySharedPreferences(), SeedData.empty(), TestClocks.fixed());
        assertTrue(empty.lastSession(Catalog.BENCH).isEmpty());
        assertFalse(empty.personalRecord(Catalog.BENCH).isPresent());
    }

    @Test
    public void savedSessionsReplaceTheSeed() {
        List<SetRecord> sets = Arrays.asList(new SetRecord(190, 5, 5), new SetRecord(190, 4, 5));
        repo.saveSession(Catalog.BENCH, sets, 1);
        assertEquals(sets, repo.lastSession(Catalog.BENCH));
        assertEquals(1, repo.consecutiveFails(Catalog.BENCH));
    }

    @Test
    public void savedPrsReplaceTheSeed() {
        repo.savePersonalRecord(new PersonalRecord(Catalog.BENCH.id, 190, TestClocks.NOW));
        PersonalRecord pr = repo.personalRecord(Catalog.BENCH).get();
        assertEquals(190, pr.weight, 0);
        assertEquals(TestClocks.NOW, pr.achievedAt);
    }

    @Test
    public void todaysVolumeAccumulatesAndCountsTowardTheWeek() {
        ActivitySummary before = repo.activity(TODAY);
        repo.addVolume(TODAY, 1000);
        repo.addVolume(TODAY, 500);
        ActivitySummary after = repo.activity(TODAY);
        assertEquals(1500, after.last7DaysVolume()[6], 0);
        assertEquals(before.workoutsThisWeek + 1, after.workoutsThisWeek);
        assertEquals(before.streakDays + 1, after.streakDays);
    }

    @Test
    public void settingsRoundTripAndDefaultToBuild() {
        SharedPrefsSettingsRepository settings = new SharedPrefsSettingsRepository(new InMemorySharedPreferences());
        assertEquals(Intensity.BUILD, settings.intensity());
        settings.setIntensity(Intensity.SEND_IT);
        assertEquals(Intensity.SEND_IT, settings.intensity());
    }

    @Test
    public void corruptSettingsFallBackToBuild() {
        InMemorySharedPreferences p = new InMemorySharedPreferences();
        p.edit().putString("intensity", "WARP_SPEED").apply();
        assertEquals(Intensity.BUILD, new SharedPrefsSettingsRepository(p).intensity());
    }

    @Test
    public void activeWorkoutSnapshotSavesLoadsAndClears() {
        SharedPrefsActiveWorkoutStore store = new SharedPrefsActiveWorkoutStore(new InMemorySharedPreferences());
        assertFalse(store.load().isPresent());
        store.save("1|0|Push Day|");
        assertEquals("1|0|Push Day|", store.load().get());
        store.clear();
        assertFalse(store.load().isPresent());
    }
}
