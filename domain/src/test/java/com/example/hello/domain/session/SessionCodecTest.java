package com.example.hello.domain.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.fakes.TestTraining;
import com.example.hello.domain.model.SetRecord;
import com.example.hello.domain.model.WorkoutPlan;
import java.util.Arrays;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

public class SessionCodecTest {
    private TestTraining t;
    private final WorkoutPlan plan = new WorkoutPlan("Test", Arrays.asList(Catalog.BENCH, Catalog.INCLINE_DB));

    @Before
    public void setUp() {
        t = new TestTraining();
        t.workouts
                .withCleanSession(Catalog.BENCH, 185)
                .withSession(
                        Catalog.INCLINE_DB,
                        new SetRecord(50, 12, 8),
                        new SetRecord(50, 11, 8),
                        new SetRecord(50, 10, 8));
    }

    @Test
    public void roundTripsDoneShortAndPendingSetsAndTheStartTime() {
        WorkoutSession s = t.service.startSession(plan);
        s.entries.get(0).warmups.get(0).complete();
        s.entries.get(0).work.get(0).complete();
        s.entries.get(0).work.get(1).record(3);

        Optional<WorkoutSession> r = SessionCodec.restore(SessionCodec.encode(s), t.service.startSession(plan));

        assertTrue(r.isPresent());
        ExerciseEntry bench = r.get().entries.get(0);
        assertTrue(bench.warmups.get(0).isDone());
        assertEquals(5, bench.work.get(0).reps());
        assertEquals(SetEntry.Status.SHORT, bench.work.get(1).status());
        assertFalse(bench.work.get(2).isDone());
        assertFalse(r.get().entries.get(1).work.get(0).isDone());
        assertEquals(s.startedAt, r.get().startedAt);
    }

    @Test
    public void aChangedPrescriptionIsNotRestored() {
        WorkoutSession s = t.service.startSession(plan);
        s.entries.get(0).work.get(0).complete();
        String saved = SessionCodec.encode(s);

        t.workouts.withCleanSession(Catalog.BENCH, 200); // the weights no longer match
        assertFalse(SessionCodec.restore(saved, t.service.startSession(plan)).isPresent());
    }

    @Test
    public void aDifferentPlanIsNotRestored() {
        WorkoutSession s = t.service.startSession(plan);
        WorkoutPlan other = new WorkoutPlan("Other", plan.exercises);
        assertFalse(SessionCodec.restore(SessionCodec.encode(s), t.service.startSession(other))
                .isPresent());
    }

    @Test
    public void differentShapesAreNotRestored() {
        WorkoutSession s = t.service.startSession(plan);
        WorkoutPlan fewer = new WorkoutPlan("Test", Arrays.asList(Catalog.BENCH));
        assertFalse(SessionCodec.restore(SessionCodec.encode(s), t.service.startSession(fewer))
                .isPresent());
    }

    @Test
    public void garbageAndOtherVersionsAreIgnored() {
        WorkoutSession fresh = t.service.startSession(plan);
        assertFalse(SessionCodec.restore("", fresh).isPresent());
        assertFalse(SessionCodec.restore("nonsense", fresh).isPresent());
        assertFalse(SessionCodec.restore("2|0|Test|", fresh).isPresent());
        assertFalse(SessionCodec.restore("1|x|Test|1:2:3/4:5:6", fresh).isPresent());
    }
}
