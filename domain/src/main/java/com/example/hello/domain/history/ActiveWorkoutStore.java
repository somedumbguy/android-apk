package com.example.hello.domain.history;

import java.util.Optional;

/** Keeps the in-progress workout, so it survives the app being closed or the screen rotating. */
public interface ActiveWorkoutStore {

    /** The saved snapshot, if any. */
    Optional<String> load();

    void save(String snapshot);

    void clear();
}
