package com.example.hello.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.hello.domain.model.CompletedWorkout;
import java.time.LocalDate;
import java.util.List;
import org.junit.Test;

public class WorkoutLogCodecTest {
    private static final LocalDate DAY = LocalDate.of(2026, 11, 21);

    @Test
    public void appendsAndDecodes() {
        String log = WorkoutLogCodec.append(null, new CompletedWorkout(DAY, "Push Day", 2850, 3));
        log = WorkoutLogCodec.append(log, new CompletedWorkout(DAY.plusDays(2), "Pull; Day", 4100.5, 5));
        List<CompletedWorkout> w = WorkoutLogCodec.decode(log);
        assertEquals(2, w.size());
        assertEquals(DAY, w.get(0).day);
        assertEquals("Pull  Day", w.get(1).plan);
        assertEquals(4100.5, w.get(1).volume, 0);
        assertEquals(5, w.get(1).sets);
    }

    @Test
    public void skipsMalformedLines() {
        String log = "garbage\n" + DAY.toEpochDay() + ";Push Day;100.0;3\nx;y;z;w";
        List<CompletedWorkout> w = WorkoutLogCodec.decode(log);
        assertEquals(1, w.size());
        assertTrue(WorkoutLogCodec.decode(null).isEmpty());
        assertTrue(WorkoutLogCodec.decode("").isEmpty());
    }
}
