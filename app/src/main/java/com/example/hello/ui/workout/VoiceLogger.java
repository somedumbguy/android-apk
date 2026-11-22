package com.example.hello.ui.workout;

import com.example.hello.domain.session.ExerciseEntry;
import com.example.hello.domain.session.SetEntry;
import com.example.hello.domain.session.WorkoutSession;
import com.example.hello.domain.voice.LogParser;
import com.example.hello.ui.format.Formats;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Applies a spoken log to today's workout. It never guesses at data: if what was said
 * doesn't fit the workout, nothing is logged and the message says why.
 */
public final class VoiceLogger {
    private VoiceLogger() {}

    public static final class Result {
        public final boolean logged;
        public final String message;
        /** The last set logged, for starting the rest timer; null if nothing was logged. */
        public final SetEntry lastSet;

        Result(boolean logged, String message, SetEntry lastSet) {
            this.logged = logged;
            this.message = message;
            this.lastSet = lastSet;
        }
    }

    static final String EXAMPLE = "Try “squat 3 sets of 5” or “did 4”.";

    public static Result apply(WorkoutSession session, LogParser.Parsed said) {
        if (said.isEmpty()) return nothing("Didn't catch that. " + EXAMPLE);

        Optional<ExerciseEntry> target = said.exercise.isPresent()
                ? session.entries.stream()
                        .filter(e -> e.exercise.equals(said.exercise.get()))
                        .findFirst()
                : session.nextPendingWorkSet().map(session::entryFor);
        if (!target.isPresent()) {
            return nothing(
                    said.exercise.isPresent()
                            ? said.exercise.get().name + " isn't in today's workout."
                            : "Every set is already logged.");
        }
        ExerciseEntry entry = target.get();

        List<SetEntry> pending = new ArrayList<>();
        for (SetEntry s : entry.work) if (!s.isDone()) pending.add(s);
        if (pending.isEmpty()) return nothing("All " + entry.exercise.name + " sets are already logged.");

        double planned = pending.get(0).weight;
        if (said.weight.isPresent() && said.weight.getAsDouble() != planned) {
            return nothing("Today's " + entry.exercise.name + " is " + Formats.lb(planned) + ", but I heard "
                    + Formats.lb(said.weight.getAsDouble()) + ". Logging a different weight isn't supported yet.");
        }

        int count = Math.min(said.sets.orElse(1), pending.size());
        SetEntry last = null;
        for (int i = 0; i < count; i++) {
            last = pending.get(i);
            last.record(said.reps.orElse(last.target));
        }
        String reps = said.reps.isPresent() ? String.valueOf(said.reps.getAsInt()) : String.valueOf(last.target);
        String message = count == 1
                ? "Logged " + entry.exercise.name + " set " + (entry.work.indexOf(last) + 1) + ": " + reps + " reps at "
                        + Formats.lb(planned)
                : "Logged " + entry.exercise.name + ": " + count + " × " + reps + " at " + Formats.lb(planned);
        return new Result(true, message, last);
    }

    private static Result nothing(String message) {
        return new Result(false, message, null);
    }
}
