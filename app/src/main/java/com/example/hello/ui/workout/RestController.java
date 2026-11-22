package com.example.hello.ui.workout;

import com.example.hello.domain.history.RestStore;
import java.util.function.LongSupplier;

/**
 * The one rest timer. The workout screen, the notification's buttons and the end-of-rest
 * alarm all go through here, and its state is saved, so they always agree. No Android code:
 * the notification and alarm sit behind {@link Notifier}.
 */
public final class RestController {

    /** Shows the rest outside the app and alerts when it ends. */
    public interface Notifier {
        /** Show a live countdown to {@code endsAtMillis} and schedule the end-of-rest alert. */
        void showRest(long endsAtMillis, String next);

        /** Remove the countdown and cancel the alert. */
        void cancel();
    }

    private final RestTimer timer;
    private final RestStore store;
    private final Notifier notifier;
    private String next = "";

    public RestController(LongSupplier nowMillis, RestStore store, Notifier notifier) {
        this.timer = new RestTimer(nowMillis);
        this.store = store;
        this.notifier = notifier;
        store.load().ifPresent(r -> {
            timer.restore(r.endsAtMillis, r.totalMs);
            next = r.next;
        });
    }

    /** Starts a rest; {@code next} describes the upcoming set, e.g. "Bench Press · set 2". */
    public void start(int seconds, String next) {
        timer.start(seconds);
        this.next = next;
        publish();
    }

    public void addSeconds(int seconds) {
        if (!timer.isRunning()) return;
        timer.addSeconds(seconds);
        publish();
    }

    /** Ends the rest early, or clears it once it's over. */
    public void stop() {
        timer.stop();
        store.clear();
        notifier.cancel();
    }

    public boolean isRunning() {
        return timer.isRunning();
    }

    public boolean isFinished() {
        return timer.isFinished();
    }

    public float fractionRemaining() {
        return timer.fractionRemaining();
    }

    public String label() {
        return timer.label();
    }

    public String next() {
        return next;
    }

    private void publish() {
        store.save(new RestStore.Rest(timer.endsAt(), timer.totalMs(), next));
        notifier.showRest(timer.endsAt(), next);
    }
}
