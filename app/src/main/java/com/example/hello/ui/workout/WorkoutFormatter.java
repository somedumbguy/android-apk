package com.example.hello.ui.workout;

import com.example.hello.domain.model.Weights;
import com.example.hello.domain.plates.PlateMath;
import com.example.hello.domain.progression.Intensity;
import com.example.hello.domain.session.SetEntry;
import com.example.hello.domain.session.WorkoutSession;
import com.example.hello.domain.training.WorkoutSummary;
import com.example.hello.ui.format.Formats;

/** Text shown on the workout screen. Pure Java, so it's unit-tested. */
final class WorkoutFormatter {
    /** How far above the target the reps picker goes. */
    static final int EXTRA_REP_CHOICES = 5;

    private WorkoutFormatter() {}

    static String subtitle(WorkoutSession s, Intensity intensity) {
        return s.entries.size() + " exercises · " + s.totalWorkSets() + " sets · " + intensity.label;
    }

    static String plates(PlateMath plates, double weight) {
        return "Per side:  " + plates.describePerSide(weight);
    }

    static String reps(SetEntry s) {
        switch (s.status()) {
            case SHORT: return "× " + s.reps() + " / " + s.target;
            case DONE: return "× " + s.reps();
            default: return "× " + s.target;
        }
    }

    static String check(SetEntry s) {
        return s.status() == SetEntry.Status.SHORT ? String.valueOf(s.reps()) : "✓";
    }

    /** Reps choices for the picker, highest first. */
    static int[] repChoices(SetEntry s) {
        int max = s.target + EXTRA_REP_CHOICES;
        int[] out = new int[max + 1];
        for (int i = 0; i <= max; i++) out[i] = max - i;
        return out;
    }

    static String repChoiceLabel(int reps, int target) {
        return reps + (reps == 1 ? " rep" : " reps") + (reps == target ? "  (target)" : "");
    }

    static String summaryTitle(WorkoutSummary s) {
        return s.newRecords.isEmpty() ? "Workout complete 💪" : "New PR! 🏆";
    }

    static String summaryMessage(WorkoutSummary s) {
        StringBuilder msg = new StringBuilder();
        msg.append(Formats.thousands(s.volume)).append(" lb lifted across ").append(s.sets)
                .append(s.sets == 1 ? " set." : " sets.");
        if (!s.newRecords.isEmpty()) {
            msg.append("\n\nNew personal records:");
            for (WorkoutSummary.NewRecord r : s.newRecords) {
                msg.append("\n🏆  ").append(r.exercise.name).append(" · ").append(Weights.format(r.weight)).append(" lb");
            }
        }
        msg.append("\n\nYour next targets are ready on the home screen.");
        return msg.toString();
    }
}
