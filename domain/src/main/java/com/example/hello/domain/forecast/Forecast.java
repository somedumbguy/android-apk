package com.example.hello.domain.forecast;

import java.time.LocalDate;

/** When a lift is projected to reach a target weight. */
public final class Forecast {
    public final double target;
    public final int sessions;
    public final LocalDate date;

    public Forecast(double target, int sessions, LocalDate date) {
        this.target = target;
        this.sessions = sessions;
        this.date = date;
    }
}
