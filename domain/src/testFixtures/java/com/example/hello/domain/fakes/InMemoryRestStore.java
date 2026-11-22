package com.example.hello.domain.fakes;

import com.example.hello.domain.history.RestStore;
import java.util.Optional;

/** A RestStore held in memory, for tests. */
public final class InMemoryRestStore implements RestStore {
    private Rest rest;

    @Override
    public Optional<Rest> load() {
        return Optional.ofNullable(rest);
    }

    @Override
    public void save(Rest rest) {
        this.rest = rest;
    }

    @Override
    public void clear() {
        rest = null;
    }
}
