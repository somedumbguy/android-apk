package com.example.hello.ui.intensity;

import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.forecast.Forecast;
import com.example.hello.domain.model.Exercise;
import com.example.hello.domain.model.Weights;
import com.example.hello.domain.progression.Intensity;
import com.example.hello.domain.training.TrainingService;
import com.example.hello.ui.format.Formats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Builds the intensity picker's content and applies the user's choice. */
public final class IntensityPresenter {

    public static final class Projection {
        public final String lift;
        public final String target;
        public final String date;

        Projection(String lift, String target, String date) {
            this.lift = lift;
            this.target = target;
            this.date = date;
        }
    }

    public static final class State {
        public final List<Intensity> options;
        public final Intensity selected;
        public final String description;
        public final List<Projection> projections;

        State(List<Intensity> options, Intensity selected, String description, List<Projection> projections) {
            this.options = Collections.unmodifiableList(options);
            this.selected = selected;
            this.description = description;
            this.projections = Collections.unmodifiableList(projections);
        }
    }

    private final TrainingService training;
    private final Locale locale;

    public IntensityPresenter(TrainingService training, Locale locale) {
        this.training = training;
        this.locale = locale;
    }

    public State present() {
        Intensity current = training.intensity();
        List<Projection> rows = new ArrayList<>();
        for (Exercise e : Catalog.MAIN_LIFTS) {
            Forecast f = training.forecastNextMilestone(e, current);
            rows.add(new Projection(e.name, Weights.format(f.target) + " lb", "~" + Formats.shortDate(f.date, locale)));
        }
        List<Intensity> options = new ArrayList<>();
        Collections.addAll(options, Intensity.values());
        return new State(options, current, current.description, rows);
    }

    /** Saves the choice and returns the updated state. */
    public State select(Intensity intensity) {
        training.setIntensity(intensity);
        return present();
    }
}
