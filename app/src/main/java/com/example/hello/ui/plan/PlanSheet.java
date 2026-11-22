package com.example.hello.ui.plan;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.hello.ui.common.Palette;
import com.example.hello.ui.common.SheetScaffold;
import com.example.hello.ui.common.Ui;

/** Bottom sheet for choosing a program and a weekly goal. */
public final class PlanSheet {
    private final Activity activity;
    private final PlanPresenter presenter;
    private final LinearLayout programs;
    private final LinearLayout goals;
    private final TextView hint;

    private PlanSheet(
            Activity activity, PlanPresenter presenter, LinearLayout programs, LinearLayout goals, TextView hint) {
        this.activity = activity;
        this.presenter = presenter;
        this.programs = programs;
        this.goals = goals;
        this.hint = hint;
    }

    public static void show(Activity a, PlanPresenter presenter, Runnable onClosed) {
        LinearLayout sheet = SheetScaffold.start(a, "Your plan");

        TextView programHeading = Ui.caps(a, "PROGRAM", 11, Palette.MUTED);
        programHeading.setPadding(0, Ui.dp(a, 20), 0, Ui.dp(a, 8));
        sheet.addView(programHeading);
        LinearLayout programs = Ui.column(a);
        sheet.addView(programs);

        TextView goalHeading = Ui.caps(a, "WORKOUT DAYS PER WEEK", 11, Palette.MUTED);
        goalHeading.setPadding(0, Ui.dp(a, 22), 0, Ui.dp(a, 8));
        sheet.addView(goalHeading);
        LinearLayout goals = new LinearLayout(a);
        goals.setOrientation(LinearLayout.HORIZONTAL);
        sheet.addView(goals, Ui.matchWidth(a, 0, 0));

        TextView hint = Ui.text(a, "", 13, Palette.MUTED);
        hint.setPadding(0, Ui.dp(a, 12), 0, 0);
        sheet.addView(hint);

        new PlanSheet(a, presenter, programs, goals, hint).render(presenter.present());
        SheetScaffold.finish(a, sheet, onClosed);
    }

    private void render(PlanPresenter.State s) {
        programs.removeAllViews();
        for (PlanPresenter.Option o : s.programs) programs.addView(programCard(o), Ui.matchWidth(activity, 0, 8));
        SheetScaffold.segments(
                activity,
                goals,
                s.goalChoices,
                s.weeklyGoal,
                d -> render(presenter.selectWeeklyGoal(d)),
                String::valueOf);
        hint.setText(s.goalHint);
    }

    private LinearLayout programCard(PlanPresenter.Option o) {
        LinearLayout card = Ui.column(activity);
        int pad = Ui.dp(activity, 16);
        card.setPadding(pad, pad, pad, pad);
        GradientDrawable bg = Ui.rounded(activity, Palette.BG, 18);
        bg.setStroke(Ui.dp(activity, o.selected ? 2 : 1), o.selected ? Palette.LIME : Palette.STROKE);
        card.setBackground(Ui.ripple(bg, 0x22FFFFFF));
        card.setOnClickListener(v -> render(presenter.selectProgram(o.id)));

        LinearLayout top = Ui.row(activity);
        top.addView(Ui.text(activity, o.name, 17, Palette.TEXT, Palette.MEDIUM), Ui.fill());
        if (o.selected) top.addView(Ui.chip(activity, "SELECTED", Palette.BG, Palette.LIME));
        card.addView(top);
        TextView workouts = Ui.caps(activity, o.workouts, 11, Palette.LIME);
        workouts.setPadding(0, Ui.dp(activity, 4), 0, 0);
        card.addView(workouts);
        TextView desc = Ui.text(activity, o.description, 13, Palette.MUTED);
        desc.setPadding(0, Ui.dp(activity, 6), 0, 0);
        card.addView(desc);
        return card;
    }
}
