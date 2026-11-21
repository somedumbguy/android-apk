package com.example.hello.domain.model;

import java.util.Locale;

/** Weight formatting and rounding. */
public final class Weights {
    private Weights() {}

    /** 190 -> "190", 192.5 -> "192.5", 1.25 -> "1.25". */
    public static String format(double w) {
        if (w == Math.floor(w)) return String.valueOf((long) w);
        String s = String.format(Locale.US, "%.2f", w);
        return s.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    public static double roundTo(double w, double step) {
        return Math.round(w / step) * step;
    }
}
