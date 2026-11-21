package com.example.hello.ui.intensity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.fakes.TestClocks;
import com.example.hello.domain.fakes.TestTraining;
import com.example.hello.domain.progression.Intensity;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

public class IntensityPresenterTest {
    private TestTraining t;
    private IntensityPresenter presenter;

    @Before public void setUp() {
        t = new TestTraining();
        t.workouts.withCleanSession(Catalog.BENCH, 185).withPr(Catalog.BENCH, 185, TestClocks.daysAgo(14));
        presenter = new IntensityPresenter(t.service, Locale.US);
    }

    @Test public void listsAllOptionsWithTheCurrentOneSelected() {
        IntensityPresenter.State s = presenter.present();
        assertEquals(3, s.options.size());
        assertEquals(Intensity.BUILD, s.selected);
        assertEquals(Intensity.BUILD.description, s.description);
    }

    @Test public void projectsEveryMainLift() {
        IntensityPresenter.State s = presenter.present();
        assertEquals(Catalog.MAIN_LIFTS.size(), s.projections.size());
        assertEquals("Bench Press", s.projections.get(0).lift);
        assertEquals("225 lb", s.projections.get(0).target);
        assertEquals("~Jan 7", s.projections.get(0).date);
    }

    @Test public void selectingSavesAndMovesTheDates() {
        String buildDate = presenter.present().projections.get(0).date;
        IntensityPresenter.State s = presenter.select(Intensity.SEND_IT);
        assertEquals(Intensity.SEND_IT, s.selected);
        assertEquals(Intensity.SEND_IT, t.settings.intensity());
        assertNotEquals(buildDate, s.projections.get(0).date);
    }
}
