package com.example.hello.domain.progression;

import com.example.hello.domain.model.Exercise;

/**
 * How aggressively linear lifts progress. The jump is added after each successful session
 * (scaled per exercise, e.g. doubled for deadlifts). missRate is the expected share of failed
 * sessions, used for forecasts.
 */
public enum Intensity {
    CRUISE("Cruise", "Small, steady jumps. You'll rarely miss a rep.", 2.5, 0.05),
    BUILD("Build", "Classic linear progression, StrongLifts 5×5 style: +5 lb a session, +10 on deadlifts.", 5, 0.15),
    SEND_IT("Send it", "Big jumps every session. Expect more misses and resets.", 7.5, 0.35);

    public final String label;
    public final String description;
    public final double baseIncrement;
    public final double missRate;

    Intensity(String label, String description, double baseIncrement, double missRate) {
        this.label = label;
        this.description = description;
        this.baseIncrement = baseIncrement;
        this.missRate = missRate;
    }

    public double increment(Exercise e) {
        return baseIncrement * e.incrementScale;
    }
}
