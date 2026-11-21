package com.example.hello.ui.intensity;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.hello.domain.progression.Intensity;
import com.example.hello.ui.common.Palette;
import com.example.hello.ui.common.Ui;

/** Bottom sheet for picking how aggressive progression is, with live PR date projections. */
public final class IntensitySheet {
    private final Activity activity;
    private final IntensityPresenter presenter;
    private final LinearLayout segments;
    private final TextView description;
    private final LinearLayout rows;

    private IntensitySheet(
            Activity activity,
            IntensityPresenter presenter,
            LinearLayout segments,
            TextView description,
            LinearLayout rows) {
        this.activity = activity;
        this.presenter = presenter;
        this.segments = segments;
        this.description = description;
        this.rows = rows;
    }

    public static void show(Activity a, IntensityPresenter presenter, Runnable onClosed) {
        LinearLayout sheet = Ui.column(a);
        int pad = Ui.dp(a, 24);
        sheet.setPadding(pad, Ui.dp(a, 12), pad, pad);
        sheet.addView(Ui.sheetHandle(a, Palette.IDLE));
        sheet.addView(Ui.text(a, "How hard do you want to push?", 22, Palette.TEXT, Palette.BLACK));

        LinearLayout segments = new LinearLayout(a);
        segments.setOrientation(LinearLayout.HORIZONTAL);
        int inset = Ui.dp(a, 4);
        segments.setPadding(inset, inset, inset, inset);
        segments.setBackground(Ui.rounded(a, Palette.BG, 100));
        sheet.addView(segments, Ui.matchWidth(a, 0, 20));

        TextView description = Ui.text(a, "", 14, Palette.MUTED);
        description.setPadding(0, Ui.dp(a, 14), 0, 0);
        sheet.addView(description);

        TextView heading = Ui.caps(a, "PROJECTED NEXT MILESTONES", 11, Palette.MUTED);
        heading.setPadding(0, Ui.dp(a, 24), 0, Ui.dp(a, 6));
        sheet.addView(heading);

        LinearLayout rows = Ui.column(a);
        sheet.addView(rows);

        TextView foot = Ui.text(
                a, "Estimates from your current numbers, the jump size and expected misses.", 12, Palette.MUTED);
        foot.setPadding(0, Ui.dp(a, 12), 0, 0);
        sheet.addView(foot);

        Button done = Ui.primaryButton(a, "Done");
        sheet.addView(done, Ui.matchWidth(a, 54, 22));

        new IntensitySheet(a, presenter, segments, description, rows).render(presenter.present());

        Dialog d = Ui.showSheet(a, sheet, Palette.SURFACE);
        done.setOnClickListener(v -> d.dismiss());
        d.setOnDismissListener(di -> onClosed.run());
    }

    private void render(IntensityPresenter.State s) {
        segments.removeAllViews();
        for (Intensity option : s.options) {
            boolean on = option == s.selected;
            TextView pill = Ui.text(activity, option.label, 15, on ? Palette.BG : Palette.MUTED, Typeface.DEFAULT_BOLD);
            pill.setGravity(Gravity.CENTER);
            if (on) pill.setBackground(Ui.rounded(activity, Palette.LIME, 100));
            pill.setOnClickListener(v -> render(presenter.select(option)));
            segments.addView(pill, new LinearLayout.LayoutParams(0, Ui.dp(activity, 44), 1));
        }

        description.setText(s.description);

        rows.removeAllViews();
        for (IntensityPresenter.Projection p : s.projections) {
            LinearLayout row = Ui.row(activity);
            row.setPadding(0, Ui.dp(activity, 10), 0, Ui.dp(activity, 10));
            row.addView(Ui.text(activity, p.lift, 15, Palette.TEXT, Palette.MEDIUM), Ui.fill());
            row.addView(Ui.text(activity, p.target, 14, Palette.MUTED));
            TextView date = Ui.text(activity, p.date, 15, Palette.LIME, Typeface.DEFAULT_BOLD);
            date.setGravity(Gravity.END);
            date.setMinWidth(Ui.dp(activity, 84));
            row.addView(date);
            rows.addView(row);
        }
    }
}
