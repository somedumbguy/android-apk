package com.example.hello.domain.fakes;

import com.example.hello.domain.history.ActiveWorkoutStore;
import java.util.Optional;

/** An ActiveWorkoutStore held in memory, for tests. */
public final class InMemoryActiveWorkoutStore implements ActiveWorkoutStore {
    private String snapshot;

    @Override
    public Optional<String> load() {
        return Optional.ofNullable(snapshot);
    }

    @Override
    public void save(String snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public void clear() {
        snapshot = null;
    }
}
