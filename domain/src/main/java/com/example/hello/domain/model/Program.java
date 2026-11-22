package com.example.hello.domain.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** A training program: workouts done in rotation, e.g. A, B, A, B. */
public final class Program {
    public final String id;
    public final String name;
    public final List<WorkoutPlan> rotation;

    public Program(String id, String name, List<WorkoutPlan> rotation) {
        if (rotation.isEmpty()) throw new IllegalArgumentException("a program needs at least one workout");
        this.id = id;
        this.name = name;
        this.rotation = Collections.unmodifiableList(rotation);
    }

    /**
     * The workout after {@code lastPlanName} in the rotation. With no history, or a last
     * workout from another program, it starts from the beginning.
     */
    public WorkoutPlan next(Optional<String> lastPlanName) {
        if (lastPlanName.isPresent()) {
            for (int i = 0; i < rotation.size(); i++) {
                if (rotation.get(i).name.equals(lastPlanName.get())) return rotation.get((i + 1) % rotation.size());
            }
        }
        return rotation.get(0);
    }
}
