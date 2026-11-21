package com.example.hello.ui.workout;

import com.example.hello.ui.format.Formats;

import java.util.function.LongSupplier;

/** Countdown between sets. Time comes from an injected source, so tests control it. */
public final class RestTimer {
    private final LongSupplier nowMs;
    private long endsAt;
    private long totalMs;
    private boolean running;

    public RestTimer(LongSupplier nowMs) {
        this.nowMs = nowMs;
    }

    public void start(int seconds) {
        totalMs = seconds * 1000L;
        endsAt = nowMs.getAsLong() + totalMs;
        running = true;
    }

    public void addSeconds(int seconds) {
        if (!running) return;
        endsAt += seconds * 1000L;
        totalMs += seconds * 1000L;
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public long remainingMs() {
        return running ? Math.max(0, endsAt - nowMs.getAsLong()) : 0;
    }

    public boolean isFinished() {
        return running && remainingMs() == 0;
    }

    /** 1 at the start of the rest, 0 when it's over. */
    public float fractionRemaining() {
        return totalMs == 0 ? 0 : remainingMs() / (float) totalMs;
    }

    /** "2:05", rounding up so it never shows 0:00 while time remains. */
    public String label() {
        return Formats.minutesSeconds((remainingMs() + 999) / 1000);
    }
}
