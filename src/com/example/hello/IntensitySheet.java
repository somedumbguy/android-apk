package com.example.hello;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hello.data.Catalog;
import com.example.hello.data.WorkoutStore;
import com.example.hello.engine.Exercise;
import com.example.hello.engine.Forecast;
import com.example.hello.engine.Intensity;
import com.example.hello.engine.Weights;

/** Bottom sheet for picking how aggressive progression is, with live PR date projections. */
public final class IntensitySheet {
    private IntensitySheet() {}

    public static void show(final Activity a, final WorkoutStore store, final Runnable onClosed) {
        LinearLayout sheet = new LinearLayout(a);
        sheet.setOrientation(LinearLayout.VERTICAL);
        int pad = Views.dp(a, 24);
        sheet.setPadding(pad, Views.dp(a, 12), pad, pad);
        sheet.addView(Views.sheetHandle(a, 0xFF3A3A48));

        sheet.addView(Views.text(a, "How hard do you want to push?", 22, Palette.TEXT, Palette.BLACK));

        final Intensity[] modes = Intensity.values();
        final TextView[] pills = new TextView[modes.length];
        LinearLayout seg = new LinearLayout(a);
        seg.setOrientation(LinearLayout.HORIZONTAL);
        seg.setPadding(Views.dp(a, 4), Views.dp(a, 4), Views.dp(a, 4), Views.dp(a, 4));
        seg.setBackground(Views.rounded(a, Palette.BG, 100));
        LinearLayout.LayoutParams segLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        segLp.topMargin = Views.dp(a, 20);
        sheet.addView(seg, segLp);

        final TextView desc = Views.text(a, "", 14, Palette.MUTED);
        desc.setPadding(0, Views.dp(a, 14), 0, 0);
        sheet.addView(desc);

        TextView heading = Views.text(a, "PROJECTED NEXT MILESTONES", 11, Palette.MUTED, Typeface.DEFAULT_BOLD);
        heading.setLetterSpacing(0.12f);
        heading.setPadding(0, Views.dp(a, 24), 0, Views.dp(a, 6));
        sheet.addView(heading);

        final LinearLayout rows = new LinearLayout(a);
        rows.setOrientation(LinearLayout.VERTICAL);
        sheet.addView(rows);

        TextView foot = Views.text(a,
                "Estimates from your current numbers, the jump size and expected misses.", 12, Palette.MUTED);
        foot.setPadding(0, Views.dp(a, 12), 0, 0);
        sheet.addView(foot);

        final Runnable refresh = new Runnable() {
            @Override public void run() {
                Intensity current = store.intensity();
                for (int i = 0; i < modes.length; i++) {
                    boolean on = modes[i] == current;
                    pills[i].setTextColor(on ? Palette.BG : Palette.MUTED);
                    pills[i].setBackground(on ? Views.rounded(a, Palette.LIME, 100) : null);
                }
                desc.setText(current.description);
                rows.removeAllViews();
                for (Exercise e : Catalog.MAIN_LIFTS) rows.addView(projectionRow(a, store, e, current));
            }
        };

        for (int i = 0; i < modes.length; i++) {
            final Intensity mode = modes[i];
            TextView pill = Views.text(a, mode.label, 15, Palette.MUTED, Typeface.DEFAULT_BOLD);
            pill.setGravity(Gravity.CENTER);
            pill.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    store.setIntensity(mode);
                    refresh.run();
                }
            });
            pills[i] = pill;
            seg.addView(pill, new LinearLayout.LayoutParams(0, Views.dp(a, 44), 1));
        }
        refresh.run();

        Button done = Views.pillButton(a, "Done", Palette.LIME, Palette.BG, 0);
        done.setTextSize(16);
        done.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams doneLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Views.dp(a, 54));
        doneLp.topMargin = Views.dp(a, 22);
        sheet.addView(done, doneLp);

        final Dialog d = Views.showSheet(a, sheet, Palette.SURFACE);
        done.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { d.dismiss(); }
        });
        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override public void onDismiss(DialogInterface di) { onClosed.run(); }
        });
    }

    private static View projectionRow(Activity a, WorkoutStore store, Exercise e, Intensity i) {
        Forecast f = store.forecast(e, i);
        LinearLayout row = new LinearLayout(a);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, Views.dp(a, 10), 0, Views.dp(a, 10));
        row.addView(Views.text(a, e.name, 15, Palette.TEXT, Palette.MEDIUM),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(Views.text(a, Weights.format(f.target) + " lb", 14, Palette.MUTED));
        TextView date = Views.text(a, "~" + f.dateLabel(), 15, Palette.LIME, Typeface.DEFAULT_BOLD);
        date.setGravity(Gravity.END);
        date.setMinWidth(Views.dp(a, 84));
        row.addView(date);
        return row;
    }
}
