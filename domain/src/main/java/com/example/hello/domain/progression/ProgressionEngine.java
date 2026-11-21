package com.example.hello.domain.progression;

import com.example.hello.domain.model.Exercise;
import com.example.hello.domain.model.Scheme;
import com.example.hello.domain.model.SetRecord;
import com.example.hello.domain.model.Suggestion;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Picks the strategy for an exercise's scheme. New schemes plug in without changing this class. */
public final class ProgressionEngine {
    static final double FIRST_SESSION_BARBELL = 45;

    private final Map<Scheme, ProgressionStrategy> strategies;

    public ProgressionEngine(Map<Scheme, ProgressionStrategy> strategies) {
        this.strategies = new EnumMap<>(strategies);
    }

    /** The standard set of strategies. */
    public static ProgressionEngine standard() {
        Map<Scheme, ProgressionStrategy> m = new EnumMap<>(Scheme.class);
        m.put(Scheme.LINEAR, new LinearProgression());
        m.put(Scheme.DOUBLE, new DoubleProgression());
        return new ProgressionEngine(m);
    }

    public Suggestion next(Exercise e, List<SetRecord> last, Intensity intensity, int consecutiveFails) {
        if (last == null || last.isEmpty()) {
            int[] reps = new int[e.sets];
            Arrays.fill(reps, e.minReps);
            return new Suggestion(
                    e,
                    e.isBarbell() ? FIRST_SESSION_BARBELL : 0,
                    reps,
                    0,
                    "First time? Start light and I'll take it from there.");
        }
        ProgressionStrategy s = strategies.get(e.scheme);
        if (s == null) throw new IllegalStateException("No strategy for " + e.scheme);
        return s.next(e, last, intensity, consecutiveFails);
    }
}
