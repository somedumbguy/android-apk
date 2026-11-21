package com.example.hello.domain.progression;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.model.SetRecord;
import com.example.hello.domain.model.Suggestion;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class DoubleProgressionTest {
    private final DoubleProgression strategy = new DoubleProgression();

    private static List<SetRecord> sets(double w, int... reps) {
        SetRecord[] out = new SetRecord[reps.length];
        for (int i = 0; i < reps.length; i++) out[i] = new SetRecord(w, reps[i], 8);
        return Arrays.asList(out);
    }

    @Test
    public void addsOneRepPerSetCappedAtTheTop() {
        Suggestion s = strategy.next(Catalog.INCLINE_DB, sets(50, 12, 11, 10), Intensity.BUILD, 0);
        assertEquals(50, s.weight, 0);
        assertArrayEquals(new int[] {12, 12, 11}, s.reps());
    }

    @Test
    public void neverPrescribesBelowTheBottomOfTheRange() {
        Suggestion s = strategy.next(Catalog.INCLINE_DB, sets(50, 5, 4, 3), Intensity.BUILD, 0);
        assertArrayEquals(new int[] {8, 8, 8}, s.reps());
    }

    @Test
    public void topOfRangeOnEverySetAddsTheEquipmentStepAndResetsReps() {
        Suggestion s = strategy.next(Catalog.INCLINE_DB, sets(50, 12, 12, 12), Intensity.CRUISE, 0);
        assertEquals(55, s.weight, 0);
        assertArrayEquals(new int[] {8, 8, 8}, s.reps());
    }

    @Test
    public void missingSetsMeansNotAllAtTheTop() {
        Suggestion s = strategy.next(Catalog.INCLINE_DB, sets(50, 12, 12), Intensity.BUILD, 0);
        assertEquals(50, s.weight, 0);
        assertArrayEquals(new int[] {12, 12, 8}, s.reps());
    }
}
