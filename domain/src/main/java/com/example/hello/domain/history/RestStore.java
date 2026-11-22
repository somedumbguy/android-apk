package com.example.hello.domain.history;

import java.util.Optional;

/** Keeps the running rest timer, so the screen, the notification and the alarm share one timer. */
public interface RestStore {

    /** A rest in progress: when it ends (wall-clock millis), its total length and what's next. */
    final class Rest {
        public final long endsAtMillis;
        public final long totalMs;
        public final String next;

        public Rest(long endsAtMillis, long totalMs, String next) {
            this.endsAtMillis = endsAtMillis;
            this.totalMs = totalMs;
            this.next = next;
        }
    }

    Optional<Rest> load();

    void save(Rest rest);

    void clear();
}
