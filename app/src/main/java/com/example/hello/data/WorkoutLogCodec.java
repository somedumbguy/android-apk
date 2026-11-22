package com.example.hello.data;

import com.example.hello.domain.model.CompletedWorkout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Encodes the workout history as one "epochDay;plan;volume;sets" line per workout. */
final class WorkoutLogCodec {
    private WorkoutLogCodec() {}

    static String append(String log, CompletedWorkout w) {
        String line = w.day.toEpochDay() + ";" + w.plan.replaceAll("[;\\n]", " ") + ";" + w.volume + ";" + w.sets;
        return log == null || log.isEmpty() ? line : log + "\n" + line;
    }

    /** Skips malformed lines rather than losing the whole history. */
    static List<CompletedWorkout> decode(String log) {
        List<CompletedWorkout> out = new ArrayList<>();
        if (log == null || log.isEmpty()) return out;
        for (String line : log.split("\n")) {
            String[] f = line.split(";");
            if (f.length != 4) continue;
            try {
                out.add(new CompletedWorkout(
                        LocalDate.ofEpochDay(Long.parseLong(f[0])),
                        f[1],
                        Double.parseDouble(f[2]),
                        Integer.parseInt(f[3])));
            } catch (RuntimeException malformed) {
                // skip this line
            }
        }
        return out;
    }
}
