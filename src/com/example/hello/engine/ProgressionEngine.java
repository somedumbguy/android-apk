package com.example.hello.engine;

import com.example.hello.data.SetRecord;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Deterministic progressive-overload rules. No AI: the same history always gives
 * the same prescription, so results are predictable and safe.
 *
 * Linear (fixed reps, e.g. 3×5): hit every rep -> add the intensity's jump; miss -> repeat;
 * three misses in a row -> drop 10% (StrongLifts-style deload).
 *
 * Double progression (rep range, e.g. 3×8–12): add a rep per set each session until every
 * set reaches the top of the range, then add weight and go back to the bottom.
 */
public final class ProgressionEngine {
    private ProgressionEngine() {}

    public static final int STALL_LIMIT = 3;

    public static Suggestion suggestNext(Exercise e, List<SetRecord> last, Intensity intensity, int fails) {
        if (last == null || last.isEmpty()) {
            double start = e.equipment == Exercise.Equipment.BARBELL ? 45 : 0;
            return new Suggestion(e, start, fill(e.sets, e.minReps), 0,
                    "First time? Start light and I'll take it from there.");
        }
        double lastW = 0;
        for (SetRecord s : last) lastW = Math.max(lastW, s.weight);
        return e.isLinear()
                ? linear(e, last, lastW, intensity, fails)
                : doubleProgression(e, last, lastW, intensity);
    }

    private static Suggestion linear(Exercise e, List<SetRecord> last, double lastW,
                                     Intensity intensity, int fails) {
        boolean allHit = last.size() >= e.sets;
        int total = 0;
        for (SetRecord s : last) {
            total += s.reps;
            if (s.reps < s.targetReps) allHit = false;
        }

        double next;
        String reason;
        if (allHit) {
            double inc = intensity.increment(e);
            next = lastW + inc;
            reason = String.format(Locale.US, "You hit all %d reps at %s lb. Add %s lb today.",
                    total, Weights.format(lastW), Weights.format(inc));
        } else if (fails >= STALL_LIMIT) {
            next = Math.max(45, Weights.roundTo(lastW * 0.9, 5));
            reason = String.format(Locale.US,
                    "Stalled at %s lb %d sessions in a row. Drop 10%% to %s lb and build momentum back.",
                    Weights.format(lastW), fails, Weights.format(next));
        } else {
            next = lastW;
            reason = String.format(Locale.US,
                    "Missed a few reps at %s lb. Same weight today. You've got this.", Weights.format(lastW));
        }
        return new Suggestion(e, next, fill(e.sets, e.minReps), next - lastW, reason);
    }

    private static Suggestion doubleProgression(Exercise e, List<SetRecord> last, double lastW,
                                                Intensity intensity) {
        boolean allTop = last.size() >= e.sets;
        for (SetRecord s : last) if (s.reps < e.maxReps) allTop = false;

        if (allTop) {
            double inc = e.equipment == Exercise.Equipment.BARBELL ? intensity.increment(e) : e.step();
            double next = lastW + inc;
            String reason = String.format(Locale.US,
                    "You hit %d reps on every set. Move up to %s lb and start back at %d.",
                    e.maxReps, Weights.format(next), e.minReps);
            return new Suggestion(e, next, fill(e.sets, e.minReps), inc, reason);
        }

        int[] targets = new int[e.sets];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < e.sets; i++) {
            int prev = i < last.size() ? last.get(i).reps : e.minReps - 1;
            targets[i] = Math.max(e.minReps, Math.min(e.maxReps, prev + 1));
            if (i > 0) sb.append("/");
            sb.append(targets[i]);
        }
        String reason = String.format(Locale.US,
                "One more rep per set than last time: %s at %s lb. Hit %d on every set to move up.",
                sb, Weights.format(lastW), e.maxReps);
        return new Suggestion(e, lastW, targets, 0, reason);
    }

    private static int[] fill(int n, int v) {
        int[] a = new int[n];
        Arrays.fill(a, v);
        return a;
    }
}
