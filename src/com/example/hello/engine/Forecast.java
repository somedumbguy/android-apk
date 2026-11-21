package com.example.hello.engine;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Projects when a lift reaches a target weight, from the next session's weight, the
 * intensity's jump size and its expected miss rate. Deterministic and deliberately simple.
 */
public final class Forecast {
    /** Plate milestones: 1 plate per side (135) up to 6 plates (585). */
    private static final double[] MILESTONES = {135, 225, 315, 405, 495, 585};

    public final double target;
    public final int sessions;
    public final Date date;

    private Forecast(double target, int sessions, Date date) {
        this.target = target;
        this.sessions = sessions;
        this.date = date;
    }

    public static double nextMilestone(double pr) {
        for (double m : MILESTONES) if (m > pr) return m;
        return Math.ceil((pr + 1) / 50.0) * 50;
    }

    public static Forecast project(Exercise e, double nextSessionWeight, double target, Intensity intensity) {
        double inc = intensity.increment(e);
        int jumps = nextSessionWeight >= target ? 0 : (int) Math.ceil((target - nextSessionWeight) / inc);
        int sessions = 1 + (int) Math.ceil(jumps * (1 + intensity.missRate));
        int days = (int) Math.round(sessions / e.sessionsPerWeek * 7);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, days);
        return new Forecast(target, sessions, cal.getTime());
    }

    public String dateLabel() {
        return new SimpleDateFormat("MMM d", Locale.getDefault()).format(date);
    }
}
