package com.example.hello.engine;

import java.util.Locale;

/** Weight formatting and rounding. */
public final class Weights {
    private Weights() {}

    public static String format(double w) {
        if (w == Math.floor(w)) return String.valueOf((long) w);
        String s = String.format(Locale.US, "%.2f", w);
        return s.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    public static double roundTo(double w, double step) {
        return Math.round(w / step) * step;
    }
}
