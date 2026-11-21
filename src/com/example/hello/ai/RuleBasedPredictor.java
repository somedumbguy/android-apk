package com.example.hello.ai;

import com.example.hello.data.SetRecord;

import java.util.List;
import java.util.Locale;

/**
 * Placeholder predictor using classic progressive-overload rules:
 * hit every rep -> add weight; miss badly -> small deload; otherwise repeat.
 */
public final class RuleBasedPredictor implements WeightPredictor {

    @Override
    public Suggestion suggestNext(String exercise, List<SetRecord> lastSession) {
        if (lastSession == null || lastSession.isEmpty()) {
            return new Suggestion(exercise, 0, 3, 5, 0, "Log your first session and I'll start predicting.");
        }

        double last = 0;
        int totalReps = 0;
        boolean allHit = true;
        boolean bigMiss = false;
        for (SetRecord s : lastSession) {
            last = Math.max(last, s.weight);
            totalReps += s.reps;
            if (s.reps < s.targetReps) allHit = false;
            if (s.reps <= s.targetReps - 2) bigMiss = true;
        }
        int sets = lastSession.size();
        int reps = lastSession.get(0).targetReps;

        double next;
        String reason;
        if (allHit) {
            next = last + increment(exercise);
            reason = String.format(Locale.US,
                    "You hit all %d reps at %s lb last session. Time to add weight.",
                    totalReps, format(last));
        } else if (bigMiss) {
            next = Math.round(last * 0.95 / 5.0) * 5.0;
            reason = String.format(Locale.US,
                    "You missed reps at %s lb. A small deload will help you build back up.",
                    format(last));
        } else {
            next = last;
            reason = String.format(Locale.US,
                    "So close. Repeat %s lb and aim to hit every rep.", format(last));
        }
        return new Suggestion(exercise, next, sets, reps, next - last, reason);
    }

    private static double increment(String exercise) {
        String e = exercise.toLowerCase(Locale.US);
        boolean lowerBody = e.contains("squat") || e.contains("deadlift") || e.contains("leg");
        return lowerBody ? 10 : 5;
    }

    public static String format(double w) {
        return w == Math.floor(w) ? String.valueOf((long) w) : String.format(Locale.US, "%.1f", w);
    }
}
