package com.example.hello.engine;

import java.util.ArrayList;
import java.util.List;

/** Which plates to load, and auto-generated warm-up sets, for a 45 lb barbell. */
public final class PlateMath {
    private PlateMath() {}

    public static final double BAR = 45;
    private static final double[] PLATES = {45, 35, 25, 10, 5, 2.5, 1.25};

    /** e.g. "45 · 25 · 2.5" for 190 lb. */
    public static String perSide(double total) {
        if (total < BAR) return "Lighter than the bar";
        if (total == BAR) return "Empty bar";
        double side = (total - BAR) / 2;
        StringBuilder sb = new StringBuilder();
        for (double p : PLATES) {
            while (side >= p - 0.001) {
                if (sb.length() > 0) sb.append(" · ");
                sb.append(Weights.format(p));
                side -= p;
            }
        }
        return sb.toString();
    }

    /** Warm-up weights and reps leading up to a work weight: bar×10, then ~50%, 70%, 85%. */
    public static List<double[]> warmups(double work) {
        List<double[]> out = new ArrayList<>();
        if (work <= BAR + 20) return out;
        out.add(new double[] {BAR, 10});
        double[][] steps = {{0.5, 5}, {0.7, 3}, {0.85, 2}};
        double prev = BAR;
        for (double[] s : steps) {
            double w = Weights.roundTo(work * s[0], 5);
            if (w > prev && w < work) {
                out.add(new double[] {w, s[1]});
                prev = w;
            }
        }
        return out;
    }
}
