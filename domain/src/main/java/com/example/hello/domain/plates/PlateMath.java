package com.example.hello.domain.plates;

import com.example.hello.domain.model.Weights;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Plates per side and warm-up sets for a given bar and plate set. */
public final class PlateMath {
    private static final double EPSILON = 0.001;

    public final double bar;
    private final double[] plates;

    /** @param plates available plate weights, heaviest first */
    public PlateMath(double bar, double[] plates) {
        this.bar = bar;
        this.plates = plates.clone();
    }

    /** A 45 lb bar with standard pound plates. */
    public static PlateMath standard() {
        return new PlateMath(45, new double[] {45, 35, 25, 10, 5, 2.5, 1.25});
    }

    /** Plates for one side, heaviest first, e.g. [45, 25, 2.5] for 190 lb. */
    public List<Double> perSide(double total) {
        if (total <= bar) return Collections.emptyList();
        List<Double> out = new ArrayList<>();
        double side = (total - bar) / 2;
        for (double p : plates) {
            while (side >= p - EPSILON) {
                out.add(p);
                side -= p;
            }
        }
        return out;
    }

    /** e.g. "45 · 25 · 2.5", "Empty bar" or "Lighter than the bar". */
    public String describePerSide(double total) {
        if (total < bar) return "Lighter than the bar";
        if (total == bar) return "Empty bar";
        StringBuilder sb = new StringBuilder();
        for (double p : perSide(total)) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(Weights.format(p));
        }
        return sb.toString();
    }

    /** One warm-up set. */
    public static final class Warmup {
        public final double weight;
        public final int reps;

        Warmup(double weight, int reps) {
            this.weight = weight;
            this.reps = reps;
        }
    }

    /** Warm-ups leading to a work weight: bar×10, then about 50%×5, 70%×3, 85%×2. */
    public List<Warmup> warmups(double work) {
        List<Warmup> out = new ArrayList<>();
        if (work <= bar + 20) return out;
        out.add(new Warmup(bar, 10));
        double[][] steps = {{0.5, 5}, {0.7, 3}, {0.85, 2}};
        double prev = bar;
        for (double[] s : steps) {
            double w = Weights.roundTo(work * s[0], 5);
            if (w > prev && w < work) {
                out.add(new Warmup(w, (int) s[1]));
                prev = w;
            }
        }
        return out;
    }
}
