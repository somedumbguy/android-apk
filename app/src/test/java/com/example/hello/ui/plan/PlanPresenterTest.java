package com.example.hello.ui.plan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.fakes.TestTraining;
import java.util.Arrays;
import org.junit.Test;

public class PlanPresenterTest {
    private final TestTraining t = new TestTraining();
    private final PlanPresenter presenter = new PlanPresenter(t.service);

    @Test
    public void listsProgramsWithTheCurrentOneSelected() {
        PlanPresenter.State s = presenter.present();
        assertEquals(Catalog.PROGRAMS.size(), s.programs.size());
        assertEquals("Full body A/B", s.programs.get(0).name);
        assertEquals("Workout A · Workout B", s.programs.get(0).workouts);
        assertTrue(s.programs.get(0).selected);
        assertFalse(s.programs.get(1).selected);
    }

    @Test
    public void selectingAProgramSavesItAndChangesTheNextWorkout() {
        PlanPresenter.State s = presenter.selectProgram("push");
        assertTrue(s.programs.get(1).selected);
        assertEquals(Catalog.PUSH_DAY, t.service.nextPlan());
    }

    @Test
    public void unknownProgramIdsAreIgnored() {
        presenter.selectProgram("nope");
        assertEquals(Catalog.DEFAULT_PROGRAM, t.service.program());
    }

    @Test
    public void weeklyGoalChoicesAndSelection() {
        PlanPresenter.State s = presenter.present();
        assertEquals(Arrays.asList(2, 3, 4, 5, 6), s.goalChoices);
        assertEquals(3, s.weeklyGoal);
        s = presenter.selectWeeklyGoal(4);
        assertEquals(4, s.weeklyGoal);
        assertEquals(4, t.service.activity().weeklyGoal);
    }

    @Test
    public void weeklyGoalIsClampedToTheChoices() {
        assertEquals(6, presenter.selectWeeklyGoal(9).weeklyGoal);
        assertEquals(2, presenter.selectWeeklyGoal(0).weeklyGoal);
    }

    @Test
    public void everyGoalHasAHint() {
        for (int d = 2; d <= 6; d++) assertFalse(PlanPresenter.goalHint(d).isEmpty());
    }
}
