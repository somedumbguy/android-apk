package com.example.hello;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hello.data.Catalog;
import com.example.hello.data.SetRecord;
import com.example.hello.data.WorkoutStore;
import com.example.hello.engine.Exercise;
import com.example.hello.engine.PlateMath;
import com.example.hello.engine.Suggestion;
import com.example.hello.engine.Weights;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * One-tap workout logging. Every set is pre-filled from the progression engine:
 * tap ✓ if you did it as planned, or tap the reps to record what you actually did.
 */
public class WorkoutActivity extends Activity {
    private static final int ORANGE = 0xFFFFA53A;

    private static final class SetState {
        final double weight;
        final int target;
        final boolean warmup;
        int done = -1;
        TextView check;
        TextView reps;

        SetState(double weight, int target, boolean warmup) {
            this.weight = weight;
            this.target = target;
            this.warmup = warmup;
        }
    }

    private static final class ExState {
        final Exercise exercise;
        final Suggestion suggestion;
        final List<SetState> warmups = new ArrayList<>();
        final List<SetState> work = new ArrayList<>();

        ExState(Exercise e, Suggestion s) {
            exercise = e;
            suggestion = s;
        }
    }

    private WorkoutStore store;
    private final List<ExState> exercises = new ArrayList<>();
    private final Handler handler = new Handler();

    private FrameLayout restBar;
    private TextView restTime;
    private View restFill;
    private long restEndsAt;
    private long restTotalMs;

    private final Runnable tick = new Runnable() {
        @Override public void run() { updateRest(); }
    };
    private final Runnable hideRest = new Runnable() {
        @Override public void run() { restBar.setVisibility(View.GONE); }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Palette.BG);
        getWindow().setNavigationBarColor(Palette.BG);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        store = new WorkoutStore(this);
        for (Exercise e : Catalog.PUSH_DAY) {
            ExState x = new ExState(e, store.suggest(e));
            if (e.equipment == Exercise.Equipment.BARBELL) {
                for (double[] w : PlateMath.warmups(x.suggestion.weight)) {
                    x.warmups.add(new SetState(w[0], (int) w[1], true));
                }
            }
            for (int r : x.suggestion.reps) x.work.add(new SetState(x.suggestion.weight, r, false));
            exercises.add(x);
        }

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Palette.BG);

        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(12), dp(20), dp(200));
        scroll.addView(content);
        root.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        content.addView(topBar());
        for (ExState x : exercises) content.addView(exerciseCard(x), below(16));

        View scrim = new View(this);
        scrim.setBackground(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[] {Palette.BG, Palette.BG, Palette.BG & 0x00FFFFFF}));
        root.addView(scrim, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(170), Gravity.BOTTOM));

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.VERTICAL);
        bottom.setPadding(dp(20), 0, dp(20), dp(24));
        bottom.addView(buildRestBar());
        Button finish = Views.pillButton(this, "Finish workout", Palette.LIME, Palette.BG, 0);
        finish.setTextSize(17);
        finish.setTypeface(Typeface.DEFAULT_BOLD);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finishWorkout(); }
        });
        LinearLayout.LayoutParams finishLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(58));
        finishLp.topMargin = dp(12);
        bottom.addView(finish, finishLp);
        root.addView(bottom, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));

        setContentView(root);
    }

    // ---- Layout ----

    private View topBar() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView back = Views.text(this, "‹", 36, Palette.TEXT);
        back.setPadding(0, 0, dp(14), dp(4));
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { onBackPressed(); }
        });
        row.addView(back);

        int sets = 0;
        for (ExState x : exercises) sets += x.work.size();
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.addView(Views.text(this, Catalog.TODAY_WORKOUT, 26, Palette.TEXT, Palette.BLACK));
        col.addView(Views.text(this, exercises.size() + " exercises · " + sets + " sets · "
                + store.intensity().label, 13, Palette.MUTED));
        row.addView(col, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        Chronometer clock = new Chronometer(this);
        clock.setTextColor(Palette.LIME);
        clock.setTextSize(16);
        clock.setTypeface(Typeface.DEFAULT_BOLD);
        clock.setBase(SystemClock.elapsedRealtime());
        clock.start();
        row.addView(clock);
        return row;
    }

    private View exerciseCard(final ExState x) {
        Exercise e = x.exercise;
        Suggestion s = x.suggestion;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(10));
        GradientDrawable bg = Views.rounded(this, Palette.SURFACE, 24);
        bg.setStroke(dp(1), Palette.STROKE);
        card.setBackground(bg);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.addView(Views.text(this, e.name, 19, Palette.TEXT, Palette.MEDIUM),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        if (s.weight > store.pr(e)) head.addView(Views.chip(this, "🏆 PR ATTEMPT", Palette.BG, Palette.LIME));
        card.addView(head);

        TextView scheme = Views.text(this, e.schemeLabel(), 11, Palette.MUTED, Typeface.DEFAULT_BOLD);
        scheme.setLetterSpacing(0.1f);
        scheme.setPadding(0, dp(4), 0, 0);
        card.addView(scheme);

        TextView reason = Views.text(this, s.reason, 13, Palette.MUTED);
        reason.setLineSpacing(0, 1.2f);
        reason.setPadding(0, dp(10), 0, 0);
        card.addView(reason);

        if (e.equipment == Exercise.Equipment.BARBELL) {
            TextView plates = Views.text(this, "Per side:  " + PlateMath.perSide(s.weight), 13,
                    Palette.LIME, Palette.MEDIUM);
            plates.setPadding(0, dp(10), 0, dp(4));
            card.addView(plates);
        }

        for (SetState w : x.warmups) card.addView(setRow(w, "W"));
        for (int i = 0; i < x.work.size(); i++) card.addView(setRow(x.work.get(i), String.valueOf(i + 1)));
        return card;
    }

    private View setRow(final SetState set, String label) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(set.warmup ? 46 : 58));

        TextView idx = Views.text(this, label, 13, set.warmup ? Palette.MUTED : Palette.TEXT, Typeface.DEFAULT_BOLD);
        idx.setGravity(Gravity.CENTER);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(Palette.SURFACE_2);
        idx.setBackground(circle);
        row.addView(idx, new LinearLayout.LayoutParams(dp(30), dp(30)));

        TextView weight = Views.text(this, Weights.format(set.weight) + " lb", set.warmup ? 15 : 19,
                set.warmup ? Palette.MUTED : Palette.TEXT, set.warmup ? Palette.MEDIUM : Palette.BLACK);
        LinearLayout.LayoutParams wLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wLp.leftMargin = dp(14);
        row.addView(weight, wLp);

        set.reps = Views.text(this, "", set.warmup ? 15 : 19, Palette.MUTED, Palette.MEDIUM);
        set.reps.setPadding(dp(10), dp(8), dp(10), dp(8));
        set.reps.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { pickReps(set); }
        });
        row.addView(set.reps);

        row.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1));

        int size = dp(set.warmup ? 34 : 46);
        set.check = Views.text(this, "✓", set.warmup ? 15 : 20, Palette.MUTED, Typeface.DEFAULT_BOLD);
        set.check.setGravity(Gravity.CENTER);
        row.addView(set.check, new LinearLayout.LayoutParams(size, size));

        View.OnClickListener toggle = new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (set.done >= 0) {
                    set.done = -1;
                    render(set);
                } else {
                    mark(set, set.target);
                }
            }
        };
        set.check.setOnClickListener(toggle);
        row.setOnClickListener(toggle);
        render(set);
        return row;
    }

    private FrameLayout buildRestBar() {
        restBar = new FrameLayout(this);
        restBar.setBackground(Views.rounded(this, Palette.SURFACE_2, 20));
        restBar.setClipToOutline(true);
        restBar.setVisibility(View.GONE);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(18), dp(12), dp(12), dp(12));
        TextView label = Views.text(this, "REST", 12, Palette.MUTED, Typeface.DEFAULT_BOLD);
        label.setLetterSpacing(0.12f);
        row.addView(label);
        restTime = Views.text(this, "0:00", 26, Palette.TEXT, Palette.BLACK);
        restTime.setPadding(dp(12), 0, 0, 0);
        row.addView(restTime, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView plus = smallPill("+30s");
        plus.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                restEndsAt += 30000;
                restTotalMs += 30000;
            }
        });
        row.addView(plus);
        TextView skip = smallPill("Skip");
        skip.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                handler.removeCallbacks(tick);
                restBar.setVisibility(View.GONE);
            }
        });
        LinearLayout.LayoutParams skipLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        skipLp.leftMargin = dp(8);
        row.addView(skip, skipLp);
        restBar.addView(row);

        restFill = new View(this);
        restFill.setBackgroundColor(Palette.LIME);
        restFill.setPivotX(0);
        restBar.addView(restFill, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(3), Gravity.BOTTOM));
        return restBar;
    }

    private TextView smallPill(String s) {
        TextView t = Views.text(this, s, 14, Palette.TEXT, Typeface.DEFAULT_BOLD);
        t.setPadding(dp(14), dp(8), dp(14), dp(8));
        t.setBackground(Views.ripple(Views.rounded(this, Palette.SURFACE, 100), 0x33FFFFFF));
        return t;
    }

    // ---- Behaviour ----

    private void mark(SetState set, int reps) {
        set.done = reps;
        render(set);
        set.check.setScaleX(0.7f);
        set.check.setScaleY(0.7f);
        set.check.animate().scaleX(1f).scaleY(1f).setDuration(320)
                .setInterpolator(new OvershootInterpolator(3f)).start();
        if (!set.warmup) startRest(restFor(set));
    }

    private int restFor(SetState set) {
        for (ExState x : exercises) if (x.work.contains(set)) return x.exercise.restSeconds;
        return 90;
    }

    private void render(SetState set) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        if (set.done < 0) {
            bg.setColor(0x00000000);
            bg.setStroke(dp(2), 0xFF3A3A48);
            set.check.setText("✓");
            set.check.setTextColor(0xFF55556A);
            set.reps.setText("× " + set.target);
            set.reps.setTextColor(Palette.MUTED);
        } else if (set.done >= set.target) {
            bg.setColor(Palette.LIME);
            set.check.setText("✓");
            set.check.setTextColor(Palette.BG);
            set.reps.setText("× " + set.done);
            set.reps.setTextColor(Palette.TEXT);
        } else {
            bg.setColor(ORANGE);
            set.check.setText(String.valueOf(set.done));
            set.check.setTextColor(Palette.BG);
            set.reps.setText("× " + set.done + " / " + set.target);
            set.reps.setTextColor(ORANGE);
        }
        set.check.setBackground(bg);
    }

    private void pickReps(final SetState set) {
        final int max = set.target + 5;
        String[] items = new String[max + 1];
        for (int i = max; i >= 0; i--) {
            items[max - i] = i + (i == 1 ? " rep" : " reps") + (i == set.target ? "  (target)" : "");
        }
        new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle("How many reps?")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        mark(set, max - which);
                    }
                })
                .show();
    }

    private void startRest(int seconds) {
        handler.removeCallbacks(hideRest);
        handler.removeCallbacks(tick);
        restTotalMs = seconds * 1000L;
        restEndsAt = SystemClock.elapsedRealtime() + restTotalMs;
        restTime.setTextColor(Palette.TEXT);
        restBar.setVisibility(View.VISIBLE);
        handler.post(tick);
    }

    private void updateRest() {
        long left = restEndsAt - SystemClock.elapsedRealtime();
        if (left <= 0) {
            restTime.setText("Go!");
            restTime.setTextColor(Palette.LIME);
            restFill.setScaleX(0);
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (v != null) v.vibrate(new long[] {0, 220, 120, 220}, -1);
            handler.postDelayed(hideRest, 2500);
            return;
        }
        long secs = (left + 999) / 1000;
        restTime.setText(String.format(Locale.US, "%d:%02d", secs / 60, secs % 60));
        restFill.setScaleX(left / (float) restTotalMs);
        handler.postDelayed(tick, 200);
    }

    private boolean anyDone() {
        for (ExState x : exercises) for (SetState s : x.work) if (s.done >= 0) return true;
        return false;
    }

    private void finishWorkout() {
        List<WorkoutStore.ExerciseLog> logs = new ArrayList<>();
        for (ExState x : exercises) {
            List<SetRecord> sets = new ArrayList<>();
            for (SetState s : x.work) if (s.done >= 0) sets.add(new SetRecord(s.weight, s.done, s.target));
            if (!sets.isEmpty()) logs.add(new WorkoutStore.ExerciseLog(x.exercise, x.work.size(), sets));
        }
        if (logs.isEmpty()) {
            Toast.makeText(this, "Check off at least one set first", Toast.LENGTH_SHORT).show();
            return;
        }
        handler.removeCallbacks(tick);
        WorkoutStore.Result r = store.save(logs);

        StringBuilder msg = new StringBuilder();
        msg.append(String.format(Locale.US, "%,d lb lifted across %d sets.", (long) r.volume, r.sets));
        if (!r.newPrs.isEmpty()) {
            msg.append("\n\nNew personal records:");
            for (String pr : r.newPrs) msg.append("\n🏆  ").append(pr);
        }
        msg.append("\n\nYour next targets are ready on the home screen.");

        new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle(r.newPrs.isEmpty() ? "Workout complete 💪" : "New PR! 🏆")
                .setMessage(msg.toString())
                .setCancelable(false)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) { finish(); }
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        if (!anyDone()) {
            super.onBackPressed();
            return;
        }
        new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle("Discard this workout?")
                .setMessage("The sets you've checked off won't be saved.")
                .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) { finish(); }
                })
                .setNegativeButton("Keep going", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    // ---- Helpers ----

    private LinearLayout.LayoutParams below(int topDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(topDp);
        return lp;
    }

    private int dp(float v) {
        return Views.dp(this, v);
    }
}
