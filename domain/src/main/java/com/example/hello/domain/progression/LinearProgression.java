package com.example.hello.domain.progression;

import com.example.hello.domain.model.Exercise;
import com.example.hello.domain.model.SetRecord;
import com.example.hello.domain.model.Suggestion;
import com.example.hello.domain.model.Weights;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * StrongLifts-style linear progression: hit every rep -> add the intensity's jump;
 * miss -> repeat the weight; miss {@link #STALL_LIMIT} sessions in a row -> drop 10%.
 */
public final class LinearProgression implements ProgressionStrategy {
    public static final int STALL_LIMIT = 3;
    static final double DELOAD = 0.9;
    static final double MIN_BAR = 45;

    @Override
    public Suggestion next(Exercise e, List<SetRecord> last, Intensity intensity, int consecutiveFails) {
        double lastW = SetRecord.maxWeight(last);
        boolean allHit = last.size() >= e.sets;
        int total = 0;
        for (SetRecord s : last) {
            total += s.reps;
            if (!s.hitTarget()) allHit = false;
        }

        double next;
        String reason;
        if (allHit) {
            double inc = intensity.increment(e);
            next = lastW + inc;
            reason = String.format(Locale.US, "You hit all %d reps at %s lb. Add %s lb today.",
                    total, Weights.format(lastW), Weights.format(inc));
        } else if (consecutiveFails >= STALL_LIMIT) {
            next = Math.max(MIN_BAR, Weights.roundTo(lastW * DELOAD, 5));
            reason = String.format(Locale.US,
                    "Stalled at %s lb %d sessions in a row. Drop 10%% to %s lb and build momentum back.",
                    Weights.format(lastW), consecutiveFails, Weights.format(next));
        } else {
            next = lastW;
            reason = String.format(Locale.US,
                    "Missed a few reps at %s lb. Same weight today. You've got this.", Weights.format(lastW));
        }
        int[] reps = new int[e.sets];
        Arrays.fill(reps, e.minReps);
        return new Suggestion(e, next, reps, next - lastW, reason);
    }
}
