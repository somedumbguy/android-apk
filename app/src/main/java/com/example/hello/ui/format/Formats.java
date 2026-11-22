package com.example.hello.ui.format;

import com.example.hello.domain.model.Weights;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Text formatting shared by screens. Pure Java, so it's unit-testable. */
public final class Formats {
    private Formats() {}

    /** "Today", "Yesterday", "3 days ago", "Last week", "3 weeks ago". */
    public static String relative(Instant then, Instant now) {
        long days = Duration.between(then, now).toDays();
        if (days <= 0) return "Today";
        if (days == 1) return "Yesterday";
        if (days < 7) return days + " days ago";
        if (days < 14) return "Last week";
        return (days / 7) + " weeks ago";
    }

    public static boolean isWithinDays(Instant then, Instant now, int days) {
        return Duration.between(then, now).toDays() < days;
    }

    /** "Jan 7". */
    public static String shortDate(LocalDate d, Locale locale) {
        return DateTimeFormatter.ofPattern("MMM d", locale).format(d);
    }

    /** "SATURDAY, NOV 21". */
    public static String headerDate(LocalDate d, Locale locale) {
        return DateTimeFormatter.ofPattern("EEEE, MMM d", locale).format(d).toUpperCase(locale);
    }

    /** One-letter day names for the 7 days ending on {@code today}. */
    public static String[] last7DayLetters(LocalDate today, Locale locale) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("EEEEE", locale);
        String[] out = new String[7];
        for (int i = 0; i < 7; i++) out[i] = f.format(today.minusDays(6 - i));
        return out;
    }

    public static String greeting(LocalTime t) {
        int h = t.getHour();
        if (h < 5) return "Up late";
        if (h < 12) return "Good morning";
        if (h < 17) return "Good afternoon";
        return "Good evening";
    }

    /** "18,420". */
    public static String thousands(double v) {
        return String.format(Locale.US, "%,d", Math.round(v));
    }

    public static String lb(double w) {
        return Weights.format(w) + " lb";
    }

    /** "▲ +5 LB VS LAST SESSION", "▼ -20 LB · DELOAD" or "= SAME AS LAST SESSION". */
    public static String delta(double delta) {
        if (delta > 0) return "▲ +" + Weights.format(delta) + " LB VS LAST SESSION";
        if (delta < 0) return "▼ " + Weights.format(delta) + " LB · DELOAD";
        return "= SAME AS LAST SESSION";
    }

    /** "2:05". */
    public static String minutesSeconds(long totalSeconds) {
        return String.format(Locale.US, "%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    /** "▲ 12%" or "▼ 5%". */
    public static String percentChange(int pct) {
        return (pct >= 0 ? "▲ " : "▼ ") + Math.abs(pct) + "%";
    }
}
