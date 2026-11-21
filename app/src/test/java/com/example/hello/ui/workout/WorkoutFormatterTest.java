package com.example.hello.ui.workout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.fakes.TestTraining;
import com.example.hello.domain.plates.PlateMath;
import com.example.hello.domain.progression.Intensity;
import com.example.hello.domain.session.SetEntry;
import com.example.hello.domain.training.WorkoutSummary;

import org.junit.Test;

import java.util.Collections;

public class WorkoutFormatterTest {

    @Test public void repsAndCheckReflectTheSetStatus() {
        SetEntry s = new SetEntry(190, 5, false);
        assertEquals("× 5", WorkoutFormatter.reps(s));
        assertEquals("✓", WorkoutFormatter.check(s));

        s.complete();
        assertEquals("× 5", WorkoutFormatter.reps(s));
        assertEquals("✓", WorkoutFormatter.check(s));

        s.record(3);
        assertEquals("× 3 / 5", WorkoutFormatter.reps(s));
        assertEquals("3", WorkoutFormatter.check(s));
    }

    @Test public void repChoicesRunFromAboveTargetDownToZero() {
        int[] choices = WorkoutFormatter.repChoices(new SetEntry(190, 5, false));
        assertEquals(5 + WorkoutFormatter.EXTRA_REP_CHOICES, choices[0]);
        assertEquals(0, choices[choices.length - 1]);
        assertEquals("5 reps  (target)", WorkoutFormatter.repChoiceLabel(5, 5));
        assertEquals("1 rep", WorkoutFormatter.repChoiceLabel(1, 5));
    }

    @Test public void platesLine() {
        assertEquals("Per side:  45 · 25 · 2.5", WorkoutFormatter.plates(PlateMath.standard(), 190));
    }

    @Test public void summaryCallsOutNewRecords() {
        WorkoutSummary none = new WorkoutSummary(2850, 3, Collections.emptyList());
        assertEquals("Workout complete 💪", WorkoutFormatter.summaryTitle(none));
        assertTrue(WorkoutFormatter.summaryMessage(none).startsWith("2,850 lb lifted across 3 sets."));

        WorkoutSummary pr = new WorkoutSummary(2850, 3,
                Collections.singletonList(new WorkoutSummary.NewRecord(Catalog.BENCH, 190)));
        assertEquals("New PR! 🏆", WorkoutFormatter.summaryTitle(pr));
        assertTrue(WorkoutFormatter.summaryMessage(pr).contains("Bench Press · 190 lb"));
    }

    @Test public void subtitleSummarizesTheSession() {
        TestTraining t = new TestTraining();
        assertEquals("4 exercises · 12 sets · Build",
                WorkoutFormatter.subtitle(t.service.startSession(Catalog.PUSH_DAY), Intensity.BUILD));
    }
}
