package com.example.hello.domain.fakes;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/** Fixed clocks so tests give the same result every run. */
public final class TestClocks {
    private TestClocks() {}

    /** Saturday 2026-11-21 18:00 UTC. */
    public static final Instant NOW = Instant.parse("2026-11-21T18:00:00Z");

    public static Clock fixed() {
        return at(NOW);
    }

    public static Clock at(Instant instant) {
        return Clock.fixed(instant, ZoneOffset.UTC);
    }

    public static Instant daysAgo(int days) {
        return NOW.minusSeconds(days * 86_400L);
    }
}
