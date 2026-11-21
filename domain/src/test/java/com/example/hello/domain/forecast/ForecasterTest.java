package com.example.hello.domain.forecast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.fakes.TestClocks;
import com.example.hello.domain.progression.Intensity;
import java.time.LocalDate;
import org.junit.Test;

public class ForecasterTest {
    private final Forecaster forecaster = new Forecaster(TestClocks.fixed());

    @Test
    public void nextMilestoneIsTheNextPlateCount() {
        assertEquals(135, Forecaster.nextMilestone(0), 0);
        assertEquals(225, Forecaster.nextMilestone(185), 0);
        assertEquals(315, Forecaster.nextMilestone(225), 0);
        assertEquals(650, Forecaster.nextMilestone(600), 0);
    }

    @Test
    public void projectsSessionsAndDateFromJumpSizeAndMissRate() {
        // 190 -> 225 at +5: 7 jumps, ×1.15 misses -> 9 more sessions, 10 total.
        // At 1.5 sessions a week that's 47 days after 2026-11-21.
        Forecast f = forecaster.project(Catalog.BENCH, 190, 225, Intensity.BUILD);
        assertEquals(10, f.sessions);
        assertEquals(LocalDate.of(2027, 1, 7), f.date);
    }

    @Test
    public void moreAggressiveIntensityArrivesSooner() {
        LocalDate cruise = forecaster.project(Catalog.BENCH, 190, 225, Intensity.CRUISE).date;
        LocalDate build = forecaster.project(Catalog.BENCH, 190, 225, Intensity.BUILD).date;
        LocalDate send = forecaster.project(Catalog.BENCH, 190, 225, Intensity.SEND_IT).date;
        assertTrue(send.isBefore(build));
        assertTrue(build.isBefore(cruise));
    }

    @Test
    public void alreadyAtTargetIsOneSessionAway() {
        assertEquals(1, forecaster.project(Catalog.BENCH, 225, 225, Intensity.BUILD).sessions);
    }
}
