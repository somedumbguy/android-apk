package com.example.hello.domain.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.example.hello.domain.catalog.Catalog;

import org.junit.Test;

public class ModelTest {

    @Test public void formatsWeightsWithoutTrailingZeros() {
        assertEquals("190", Weights.format(190));
        assertEquals("192.5", Weights.format(192.5));
        assertEquals("1.25", Weights.format(1.25));
    }

    @Test public void schemeComesFromTheRepRange() {
        assertEquals(Scheme.LINEAR, Catalog.BENCH.scheme);
        assertEquals(Scheme.DOUBLE, Catalog.INCLINE_DB.scheme);
        assertEquals("3×5 · LINEAR PROGRESSION", Catalog.BENCH.schemeLabel());
        assertEquals("3×8–12 · DOUBLE PROGRESSION", Catalog.INCLINE_DB.schemeLabel());
    }

    @Test public void exercisesAreEqualByIdNotName() {
        Exercise renamed = Exercise.builder("bench_press", "Barbell Bench").build();
        assertEquals(Catalog.BENCH, renamed);
        assertNotEquals(Catalog.BENCH, Catalog.SQUAT);
    }

    @Test public void catalogLooksUpByIdAndIdsAreUnique() {
        assertEquals(Catalog.SQUAT, Catalog.byId("squat").get());
        assertEquals(false, Catalog.byId("nope").isPresent());
        assertEquals(Catalog.ALL.size(), Catalog.ALL.stream().map(e -> e.id).distinct().count());
    }

    @Test public void repsLabelDescribesEqualAndUnequalSets() {
        assertEquals("3 sets × 5 reps", new Suggestion(Catalog.BENCH, 190, new int[] {5, 5, 5}, 5, "").repsLabel());
        assertEquals("1 set × 5 reps", new Suggestion(Catalog.DEADLIFT, 375, new int[] {5}, 10, "").repsLabel());
        assertEquals("3 sets · 12 / 12 / 11 reps",
                new Suggestion(Catalog.INCLINE_DB, 50, new int[] {12, 12, 11}, 0, "").repsLabel());
    }
}
