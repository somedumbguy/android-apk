package com.example.hello.ui.workout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.hello.domain.fakes.InMemoryRestStore;
import org.junit.Test;

public class RestControllerTest {
    /** Records what the controller asked the notification to do. */
    static final class FakeNotifier implements RestController.Notifier {
        long shownUntil = -1;
        String shownNext;
        int cancels;

        @Override
        public void showRest(long endsAtMillis, String next) {
            shownUntil = endsAtMillis;
            shownNext = next;
        }

        @Override
        public void cancel() {
            cancels++;
            shownUntil = -1;
        }
    }

    private long now = 1_000_000;
    private final InMemoryRestStore store = new InMemoryRestStore();
    private final FakeNotifier notifier = new FakeNotifier();

    private RestController controller() {
        return new RestController(() -> now, store, notifier);
    }

    @Test
    public void startingShowsTheCountdownAndSavesIt() {
        controller().start(90, "Bench Press · set 2 of 3");
        assertEquals(now + 90_000, notifier.shownUntil);
        assertEquals("Bench Press · set 2 of 3", notifier.shownNext);
        assertEquals(now + 90_000, store.load().get().endsAtMillis);
    }

    @Test
    public void anotherInstanceSeesTheSameRest() {
        controller().start(90, "next");
        now += 30_000;
        RestController other = controller();
        assertTrue(other.isRunning());
        assertEquals("1:00", other.label());
        assertEquals("next", other.next());
    }

    @Test
    public void addingTimeMovesTheCountdownAndTheAlarm() {
        RestController c = controller();
        c.start(60, "next");
        c.addSeconds(30);
        assertEquals(now + 90_000, notifier.shownUntil);
        assertEquals(now + 90_000, store.load().get().endsAtMillis);
    }

    @Test
    public void addingTimeDoesNothingWhenNotResting() {
        controller().addSeconds(30);
        assertEquals(-1, notifier.shownUntil);
        assertFalse(store.load().isPresent());
    }

    @Test
    public void stoppingClearsEverything() {
        RestController c = controller();
        c.start(60, "next");
        c.stop();
        assertFalse(c.isRunning());
        assertFalse(store.load().isPresent());
        assertEquals(1, notifier.cancels);
        assertFalse(controller().isRunning());
    }

    @Test
    public void finishesWhenTimeRunsOut() {
        RestController c = controller();
        c.start(60, "next");
        now += 60_000;
        assertTrue(c.isFinished());
        assertEquals(0f, c.fractionRemaining(), 0.001);
    }
}
