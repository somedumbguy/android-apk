package com.example.hello.domain.progression;

import static org.junit.Assert.assertEquals;

import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.model.Scheme;
import com.example.hello.domain.model.SetRecord;
import com.example.hello.domain.model.Suggestion;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class ProgressionEngineTest {

    @Test public void firstSessionStartsAtTheBarForBarbellLifts() {
        Suggestion s = ProgressionEngine.standard().next(Catalog.BENCH, Collections.emptyList(), Intensity.BUILD, 0);
        assertEquals(45, s.weight, 0);
    }

    @Test public void firstSessionStartsAtZeroForOtherEquipment() {
        Suggestion s = ProgressionEngine.standard().next(Catalog.INCLINE_DB, null, Intensity.BUILD, 0);
        assertEquals(0, s.weight, 0);
    }

    @Test public void delegatesToTheStrategyForTheExercisesScheme() {
        ProgressionStrategy fixed = (e, last, i, fails) -> new Suggestion(e, 999, new int[] {1}, 0, "stub");
        Map<Scheme, ProgressionStrategy> m = new EnumMap<>(Scheme.class);
        m.put(Scheme.LINEAR, fixed);
        ProgressionEngine engine = new ProgressionEngine(m);
        Suggestion s = engine.next(Catalog.BENCH, Collections.singletonList(new SetRecord(100, 5, 5)), Intensity.BUILD, 0);
        assertEquals(999, s.weight, 0);
    }

    @Test(expected = IllegalStateException.class)
    public void missingStrategyFailsLoudly() {
        new ProgressionEngine(new EnumMap<>(Scheme.class))
                .next(Catalog.BENCH, Collections.singletonList(new SetRecord(100, 5, 5)), Intensity.BUILD, 0);
    }
}
