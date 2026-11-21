package com.example.hello.domain.progression;

import com.example.hello.domain.model.Exercise;
import com.example.hello.domain.model.SetRecord;
import com.example.hello.domain.model.Suggestion;
import com.example.hello.domain.model.Weights;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Double progression for rep ranges (e.g. 3×8–12): aim for one more rep per set each session;
 * once every set reaches the top of the range, add weight and start back at the bottom.
 */
public final class DoubleProgression implements ProgressionStrategy {

    @Override
    public Suggestion next(Exercise e, List<SetRecord> last, Intensity intensity, int consecutiveFails) {
        double lastW = SetRecord.maxWeight(last);
        boolean allTop = last.size() >= e.sets;
        for (SetRecord s : last) if (s.reps < e.maxReps) allTop = false;

        if (allTop) {
            double inc = e.isBarbell() ? intensity.increment(e) : e.equipment.step;
            double next = lastW + inc;
            int[] reps = new int[e.sets];
            Arrays.fill(reps, e.minReps);
            String reason = String.format(Locale.US,
                    "You hit %d reps on every set. Move up to %s lb and start back at %d.",
                    e.maxReps, Weights.format(next), e.minReps);
            return new Suggestion(e, next, reps, inc, reason);
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
}
