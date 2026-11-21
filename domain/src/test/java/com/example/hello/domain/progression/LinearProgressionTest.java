package com.example.hello.domain.progression;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.model.SetRecord;
import com.example.hello.domain.model.Suggestion;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class LinearProgressionTest {
    private final LinearProgression strategy = new LinearProgression();

    private static List<SetRecord> sets(double w, int... reps) {
        SetRecord[] out = new SetRecord[reps.length];
        for (int i = 0; i < reps.length; i++) out[i] = new SetRecord(w, reps[i], 5);
        return Arrays.asList(out);
    }

    @Test
    public void hittingEveryRepAddsTheIntensityJump() {
        assertEquals(187.5, strategy.next(Catalog.BENCH, sets(185, 5, 5, 5), Intensity.CRUISE, 0).weight, 0);
        assertEquals(190, strategy.next(Catalog.BENCH, sets(185, 5, 5, 5), Intensity.BUILD, 0).weight, 0);
        assertEquals(192.5, strategy.next(Catalog.BENCH, sets(185, 5, 5, 5), Intensity.SEND_IT, 0).weight, 0);
    }

    @Test
    public void deadliftJumpsAreDoubled() {
        assertEquals(375, strategy.next(Catalog.DEADLIFT, sets(365, 5), Intensity.BUILD, 0).weight, 0);
    }

    @Test
    public void missingRepsRepeatsTheWeight() {
        Suggestion s = strategy.next(Catalog.BENCH, sets(205, 5, 4, 3), Intensity.BUILD, 1);
        assertEquals(205, s.weight, 0);
        assertEquals(0, s.delta, 0);
    }

    @Test
    public void skippedSetsCountAsAMiss() {
        assertEquals(205, strategy.next(Catalog.BENCH, sets(205, 5, 5), Intensity.BUILD, 0).weight, 0);
    }

    @Test
    public void threeStallsInARowDeloadTenPercentRoundedToFive() {
        Suggestion s = strategy.next(Catalog.BENCH, sets(205, 5, 4, 3), Intensity.BUILD, LinearProgression.STALL_LIMIT);
        assertEquals(185, s.weight, 0);
        assertEquals(-20, s.delta, 0);
        assertTrue(s.reason.contains("Drop 10%"));
    }

    @Test
    public void deloadNeverGoesBelowTheBar() {
        assertEquals(45, strategy.next(Catalog.BENCH, sets(45, 2, 2, 2), Intensity.BUILD, 3).weight, 0);
    }

    @Test
    public void prescribesTheExercisesSetsAndReps() {
        assertArrayEquals(
                new int[] {5, 5, 5},
                strategy.next(Catalog.BENCH, sets(185, 5, 5, 5), Intensity.BUILD, 0)
                        .reps());
        assertArrayEquals(
                new int[] {5},
                strategy.next(Catalog.DEADLIFT, sets(365, 5), Intensity.BUILD, 0)
                        .reps());
    }
}
