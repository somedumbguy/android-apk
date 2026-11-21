package com.example.hello.ui.workout;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.VibrationEffect;
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
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import com.example.hello.app.AppContainer;
import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.plates.PlateMath;
import com.example.hello.domain.session.ExerciseEntry;
import com.example.hello.domain.session.SetEntry;
import com.example.hello.domain.session.WorkoutSession;
import com.example.hello.domain.training.TrainingService;
import com.example.hello.domain.training.WorkoutSummary;
import com.example.hello.ui.common.Cards;
import com.example.hello.ui.common.Palette;
import com.example.hello.ui.common.Screens;
import com.example.hello.ui.common.Ui;
import com.example.hello.ui.format.Formats;
import java.util.HashMap;
import java.util.Map;

/**
 * One-tap workout logging. Set state lives in {@link WorkoutSession}; this screen only draws
 * it and forwards taps. Tap a set to mark it done as planned, or tap its reps to record
 * what you actually did.
 */
public class WorkoutActivity extends ComponentActivity {
    private static final long TICK_MS = 200;
    private static final long HIDE_AFTER_REST_MS = 2500;
    private static final long[] REST_DONE_BUZZ = {0, 220, 120, 220};

    /** The views for one set row. */
    private static final class SetViews {
        final TextView check;
        final TextView reps;

        SetViews(TextView check, TextView reps) {
            this.check = check;
            this.reps = reps;
        }
    }

    private TrainingService training;
    private PlateMath plates;
    private WorkoutSession session;
    private final Map<SetEntry, SetViews> setViews = new HashMap<>();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final RestTimer rest = new RestTimer(SystemClock::elapsedRealtime);
    private FrameLayout restBar;
    private TextView restTime;
    private View restFill;
    private final Runnable tick = this::updateRest;
    private final Runnable hideRest = () -> restBar.setVisibility(View.GONE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Screens.setUp(this);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        AppContainer container = AppContainer.from(this);
        training = container.training;
        plates = container.plates;
        session = training.startSession(Catalog.PUSH_DAY);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmLeave();
            }
        });

        setContentView(buildScreen());
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    // ---- Layout ----

    private View buildScreen() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Palette.BG);
        Screens.padForSystemBars(root);

        LinearLayout content = Ui.column(this);
        content.setPadding(dp(20), dp(8), dp(20), dp(200));
        root.addView(Screens.scroller(this, content));

        content.addView(topBar());
        for (ExerciseEntry entry : session.entries) content.addView(exerciseCard(entry), Ui.matchWidth(this, 0, 16));

        root.addView(Cards.bottomScrim(this, 170));

        LinearLayout bottom = Ui.column(this);
        bottom.setPadding(dp(20), 0, dp(20), dp(16));
        bottom.addView(buildRestBar());
        Button finish = Ui.primaryButton(this, "Finish workout");
        finish.setTextSize(17);
        finish.setOnClickListener(v -> finishWorkout());
        bottom.addView(finish, Ui.matchWidth(this, 58, 12));
        root.addView(
                bottom,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
        return root;
    }

    private View topBar() {
        LinearLayout row = Ui.row(this);

        TextView back = Ui.text(this, "‹", 36, Palette.TEXT);
        back.setPadding(0, 0, dp(14), dp(4));
        back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        row.addView(back);

        LinearLayout col = Ui.column(this);
        col.addView(Ui.text(this, session.plan.name, 26, Palette.TEXT, Palette.BLACK));
        col.addView(Ui.text(this, WorkoutFormatter.subtitle(session, training.intensity()), 13, Palette.MUTED));
        row.addView(col, Ui.fill());

        Chronometer clock = new Chronometer(this);
        clock.setTextColor(Palette.LIME);
        clock.setTextSize(16);
        clock.setTypeface(Typeface.DEFAULT_BOLD);
        clock.setBase(SystemClock.elapsedRealtime());
        clock.start();
        row.addView(clock);
        return row;
    }

    private View exerciseCard(ExerciseEntry entry) {
        LinearLayout card = Cards.card(this);
        card.setPadding(dp(18), dp(18), dp(18), dp(10));

        LinearLayout head = Ui.row(this);
        head.addView(Ui.text(this, entry.exercise.name, 19, Palette.TEXT, Palette.MEDIUM), Ui.fill());
        if (entry.prAttempt) head.addView(Ui.chip(this, "🏆 PR ATTEMPT", Palette.BG, Palette.LIME));
        card.addView(head);

        TextView scheme = Ui.caps(this, entry.exercise.schemeLabel(), 11, Palette.MUTED);
        scheme.setPadding(0, dp(4), 0, 0);
        card.addView(scheme);

        TextView reason = Ui.text(this, entry.suggestion.reason, 13, Palette.MUTED);
        reason.setLineSpacing(0, 1.2f);
        reason.setPadding(0, dp(10), 0, 0);
        card.addView(reason);

        if (entry.exercise.isBarbell()) {
            TextView perSide = Ui.text(
                    this, WorkoutFormatter.plates(plates, entry.suggestion.weight), 13, Palette.LIME, Palette.MEDIUM);
            perSide.setPadding(0, dp(10), 0, dp(4));
            card.addView(perSide);
        }

        for (SetEntry w : entry.warmups) card.addView(setRow(w, "W"));
        for (int i = 0; i < entry.work.size(); i++) card.addView(setRow(entry.work.get(i), String.valueOf(i + 1)));
        return card;
    }

    private View setRow(SetEntry set, String label) {
        boolean warm = set.warmup;
        LinearLayout row = Ui.row(this);
        row.setMinimumHeight(dp(warm ? 46 : 58));

        TextView idx = Ui.text(this, label, 13, warm ? Palette.MUTED : Palette.TEXT, Typeface.DEFAULT_BOLD);
        idx.setGravity(Gravity.CENTER);
        idx.setBackground(Ui.circle(Palette.SURFACE_2));
        row.addView(idx, new LinearLayout.LayoutParams(dp(30), dp(30)));

        TextView weight = Ui.text(
                this,
                Formats.lb(set.weight),
                warm ? 15 : 19,
                warm ? Palette.MUTED : Palette.TEXT,
                warm ? Palette.MEDIUM : Palette.BLACK);
        weight.setPadding(dp(14), 0, 0, 0);
        row.addView(weight);

        TextView reps = Ui.text(this, "", warm ? 15 : 19, Palette.MUTED, Palette.MEDIUM);
        reps.setPadding(dp(10), dp(8), dp(10), dp(8));
        reps.setOnClickListener(v -> pickReps(set));
        row.addView(reps);

        row.addView(new View(this), Ui.fill());

        int size = dp(warm ? 34 : 46);
        TextView check = Ui.text(this, "✓", warm ? 15 : 20, Palette.MUTED, Typeface.DEFAULT_BOLD);
        check.setGravity(Gravity.CENTER);
        row.addView(check, new LinearLayout.LayoutParams(size, size));

        setViews.put(set, new SetViews(check, reps));
        View.OnClickListener toggle = v -> {
            if (set.toggle()) onSetDone(set);
            else render(set);
        };
        check.setOnClickListener(toggle);
        row.setOnClickListener(toggle);
        render(set);
        return row;
    }

    private FrameLayout buildRestBar() {
        restBar = new FrameLayout(this);
        restBar.setBackground(Ui.rounded(this, Palette.SURFACE_2, 20));
        restBar.setClipToOutline(true);
        restBar.setVisibility(View.GONE);

        LinearLayout row = Ui.row(this);
        row.setPadding(dp(18), dp(12), dp(12), dp(12));
        row.addView(Ui.caps(this, "REST", 12, Palette.MUTED));
        restTime = Ui.text(this, "0:00", 26, Palette.TEXT, Palette.BLACK);
        restTime.setPadding(dp(12), 0, 0, 0);
        row.addView(restTime, Ui.fill());

        TextView plus = smallPill("+30s");
        plus.setOnClickListener(v -> rest.addSeconds(30));
        row.addView(plus);
        TextView skip = smallPill("Skip");
        skip.setOnClickListener(v -> stopRest());
        LinearLayout.LayoutParams skipLp = Ui.wrap(this, 0);
        skipLp.leftMargin = dp(8);
        row.addView(skip, skipLp);
        restBar.addView(row);

        restFill = new View(this);
        restFill.setBackgroundColor(Palette.LIME);
        restFill.setPivotX(0);
        restBar.addView(
                restFill, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(3), Gravity.BOTTOM));
        return restBar;
    }

    private TextView smallPill(String s) {
        TextView t = Ui.text(this, s, 14, Palette.TEXT, Typeface.DEFAULT_BOLD);
        t.setPadding(dp(14), dp(8), dp(14), dp(8));
        t.setBackground(Ui.ripple(Ui.rounded(this, Palette.SURFACE, 100), 0x33FFFFFF));
        return t;
    }

    // ---- Rendering ----

    private void render(SetEntry set) {
        SetViews v = setViews.get(set);
        GradientDrawable bg = Ui.circle(0x00000000);
        int checkColor = Palette.BG;
        int repsColor = Palette.TEXT;
        switch (set.status()) {
            case PENDING:
                bg.setStroke(dp(2), Palette.IDLE);
                checkColor = Palette.IDLE_TEXT;
                repsColor = Palette.MUTED;
                break;
            case DONE:
                bg.setColor(Palette.LIME);
                break;
            case SHORT:
                bg.setColor(Palette.ORANGE);
                repsColor = Palette.ORANGE;
                break;
        }
        v.check.setBackground(bg);
        v.check.setText(WorkoutFormatter.check(set));
        v.check.setTextColor(checkColor);
        v.reps.setText(WorkoutFormatter.reps(set));
        v.reps.setTextColor(repsColor);
    }

    // ---- Behaviour ----

    private void onSetDone(SetEntry set) {
        render(set);
        TextView check = setViews.get(set).check;
        check.setScaleX(0.7f);
        check.setScaleY(0.7f);
        check.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(320)
                .setInterpolator(new OvershootInterpolator(3f))
                .start();
        if (!set.warmup) {
            ExerciseEntry entry = session.entryFor(set);
            startRest(entry == null ? 90 : entry.exercise.restSeconds);
        }
    }

    private void pickReps(SetEntry set) {
        int[] choices = WorkoutFormatter.repChoices(set);
        String[] labels = new String[choices.length];
        for (int i = 0; i < choices.length; i++) labels[i] = WorkoutFormatter.repChoiceLabel(choices[i], set.target);
        new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle("How many reps?")
                .setItems(labels, (d, which) -> {
                    set.record(choices[which]);
                    onSetDone(set);
                })
                .show();
    }

    private void startRest(int seconds) {
        handler.removeCallbacks(hideRest);
        handler.removeCallbacks(tick);
        rest.start(seconds);
        restTime.setTextColor(Palette.TEXT);
        restBar.setVisibility(View.VISIBLE);
        handler.post(tick);
    }

    private void stopRest() {
        rest.stop();
        handler.removeCallbacks(tick);
        restBar.setVisibility(View.GONE);
    }

    private void updateRest() {
        if (rest.isFinished()) {
            rest.stop();
            restTime.setText("Go!");
            restTime.setTextColor(Palette.LIME);
            restFill.setScaleX(0);
            buzz();
            handler.postDelayed(hideRest, HIDE_AFTER_REST_MS);
            return;
        }
        restTime.setText(rest.label());
        restFill.setScaleX(rest.fractionRemaining());
        handler.postDelayed(tick, TICK_MS);
    }

    private void buzz() {
        Vibrator v = getSystemService(Vibrator.class);
        if (v != null) v.vibrate(VibrationEffect.createWaveform(REST_DONE_BUZZ, -1));
    }

    private void finishWorkout() {
        if (!session.hasProgress()) {
            Ui.toast(this, "Check off at least one set first");
            return;
        }
        stopRest();
        WorkoutSummary summary = training.complete(session);
        new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle(WorkoutFormatter.summaryTitle(summary))
                .setMessage(WorkoutFormatter.summaryMessage(summary))
                .setCancelable(false)
                .setPositiveButton("Done", (d, w) -> finish())
                .show();
    }

    private void confirmLeave() {
        if (!session.hasProgress()) {
            finish();
            return;
        }
        new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle("Discard this workout?")
                .setMessage("The sets you've checked off won't be saved.")
                .setPositiveButton("Discard", (d, w) -> finish())
                .setNegativeButton("Keep going", null)
                .show();
    }

    private int dp(float v) {
        return Ui.dp(this, v);
    }
}
