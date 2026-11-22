package com.example.hello.ui.plan;

import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.model.Program;
import com.example.hello.domain.training.TrainingService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Content for the "Your plan" sheet: which program, and how many days a week. */
public final class PlanPresenter {

    public static final class Option {
        public final String id;
        public final String name;
        public final String description;
        public final String workouts;
        public final boolean selected;

        Option(String id, String name, String description, String workouts, boolean selected) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.workouts = workouts;
            this.selected = selected;
        }
    }

    public static final class State {
        public final List<Option> programs;
        public final List<Integer> goalChoices;
        public final int weeklyGoal;
        public final String goalHint;

        State(List<Option> programs, List<Integer> goalChoices, int weeklyGoal, String goalHint) {
            this.programs = Collections.unmodifiableList(programs);
            this.goalChoices = Collections.unmodifiableList(goalChoices);
            this.weeklyGoal = weeklyGoal;
            this.goalHint = goalHint;
        }
    }

    private final TrainingService training;

    public PlanPresenter(TrainingService training) {
        this.training = training;
    }

    public State present() {
        Program current = training.program();
        List<Option> options = new ArrayList<>();
        for (Program p : Catalog.PROGRAMS) {
            List<String> names = new ArrayList<>();
            p.rotation.forEach(w -> names.add(w.name));
            options.add(new Option(p.id, p.name, p.description, String.join(" · ", names), p.id.equals(current.id)));
        }
        List<Integer> goals = new ArrayList<>();
        for (int d = TrainingService.MIN_WEEKLY_GOAL; d <= TrainingService.MAX_WEEKLY_GOAL; d++) goals.add(d);
        int goal = training.weeklyGoal();
        return new State(options, goals, goal, goalHint(goal));
    }

    public State selectProgram(String id) {
        Catalog.programById(id).ifPresent(training::setProgram);
        return present();
    }

    public State selectWeeklyGoal(int days) {
        training.setWeeklyGoal(days);
        return present();
    }

    static String goalHint(int days) {
        if (days <= 2) return "A gentle start. Two full-body sessions still build strength.";
        if (days == 3) return "The classic: three sessions with a rest day between each.";
        if (days == 4) return "More volume. Make sure you're recovering between sessions.";
        return "Advanced. Watch for stalls; recovery matters as much as training.";
    }
}
