package com.example.hello;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hello.ai.Predictors;
import com.example.hello.ai.RuleBasedPredictor;
import com.example.hello.ai.Suggestion;
import com.example.hello.auth.User;
import com.example.hello.data.SampleData;
import com.example.hello.ui.BarChartView;
import com.example.hello.ui.MicView;
import com.example.hello.ui.RingView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** The home screen: greeting, AI coach card, stats, weekly chart, voice log and PRs. */
public final class HomeScreen {
    private HomeScreen() {}

    public interface Listener {
        void onAvatarTapped();
    }

    public static View create(final Activity a, User user, final Listener listener) {
        FrameLayout root = new FrameLayout(a);
        root.setBackgroundColor(Palette.BG);

        ScrollView scroll = new ScrollView(a);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout content = new LinearLayout(a);
        content.setOrientation(LinearLayout.VERTICAL);
        int side = dp(a, 20);
        content.setPadding(side, dp(a, 20), side, dp(a, 128));
        scroll.addView(content);
        root.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        Suggestion next = Predictors.create()
                .suggestNext(SampleData.NEXT_EXERCISE, SampleData.lastBenchSession());

        RingView ring = new RingView(a);
        ring.setData(SampleData.WORKOUTS_THIS_WEEK, SampleData.WEEKLY_GOAL, "workouts");
        BarChartView bars = new BarChartView(a);
        bars.setData(SampleData.LAST_7_DAYS_VOLUME, last7DayLabels(), 6);

        content.addView(header(a, user, listener));
        content.addView(heroCard(a, next), below(a, 24));
        content.addView(statsRow(a, ring), below(a, 12));
        content.addView(weeklyCard(a, bars), below(a, 12));
        content.addView(voiceCard(a), below(a, 12));
        content.addView(sectionTitle(a, "Personal records", "See all"), below(a, 32));
        content.addView(recordsStrip(a));

        // Fade the content out behind the floating button.
        View scrim = new View(a);
        scrim.setBackground(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[] {Palette.BG, Palette.BG & 0x00FFFFFF}));
        root.addView(scrim, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(a, 140), Gravity.BOTTOM));

        Button fab = Views.pillButton(a, "+   Log a set", Palette.LIME, Palette.BG, 0);
        fab.setTextSize(17);
        fab.setTypeface(Typeface.DEFAULT_BOLD);
        fab.setPadding(dp(a, 36), 0, dp(a, 36), 0);
        fab.setElevation(dp(a, 8));
        fab.setOnClickListener(soon(a, "Set logging"));
        FrameLayout.LayoutParams fabLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(a, 58),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        fabLp.bottomMargin = dp(a, 28);
        root.addView(fab, fabLp);

        animateEntrance(a, content, fab);
        ring.animateIn(500);
        bars.animateIn(600);
        return root;
    }

    // ---- Sections ----

    private static View header(Activity a, User user, final Listener listener) {
        LinearLayout row = new LinearLayout(a);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout col = new LinearLayout(a);
        col.setOrientation(LinearLayout.VERTICAL);
        String date = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
                .format(new Date()).toUpperCase(Locale.getDefault());
        TextView dateView = Views.text(a, date, 12, Palette.MUTED, Palette.MEDIUM);
        dateView.setLetterSpacing(0.12f);
        col.addView(dateView);
        TextView hi = Views.text(a, greeting() + ",\n" + user.getFirstName(), 30, Palette.TEXT, Palette.BLACK);
        hi.setPadding(0, dp(a, 6), 0, 0);
        col.addView(hi);
        row.addView(col, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView avatar = Views.avatar(a, user.getInitial(), 48);
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { listener.onAvatarTapped(); }
        });
        row.addView(avatar);
        return row;
    }

    private static View heroCard(Activity a, Suggestion s) {
        LinearLayout card = new LinearLayout(a);
        card.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(a, 22);
        card.setPadding(pad, pad, pad, pad);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR, Palette.HERO_GRADIENT);
        bg.setCornerRadius(dp(a, 28));
        card.setBackground(bg);

        LinearLayout top = new LinearLayout(a);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(Views.chip(a, "✦  AI COACH", Color.WHITE, 0x2EFFFFFF));
        top.addView(new View(a), new LinearLayout.LayoutParams(0, 1, 1));
        top.addView(Views.text(a, "Today · " + SampleData.TODAY_WORKOUT, 13, 0xCCFFFFFF, Palette.MEDIUM));
        card.addView(top);

        TextView exercise = Views.text(a, s.exercise, 20, Color.WHITE, Palette.MEDIUM);
        exercise.setPadding(0, dp(a, 20), 0, 0);
        card.addView(exercise);

        LinearLayout weight = numberWithUnit(a, RuleBasedPredictor.format(s.weight), 76, "lb", 22,
                Color.WHITE, 0xCCFFFFFF);
        card.addView(weight);

        card.addView(Views.text(a, s.sets + " sets × " + s.reps + " reps", 16, 0xE6FFFFFF));

        TextView delta = Views.chip(a, deltaText(s.delta), Palette.LIME, 0x40000000);
        LinearLayout.LayoutParams deltaLp = wrap();
        deltaLp.topMargin = dp(a, 14);
        card.addView(delta, deltaLp);

        TextView reason = Views.text(a, s.reason, 14, 0xD9FFFFFF);
        reason.setLineSpacing(0, 1.2f);
        reason.setPadding(0, dp(a, 14), 0, 0);
        card.addView(reason);

        Button start = Views.pillButton(a, "Start workout   →", Palette.LIME, Palette.BG, 0);
        start.setTextSize(16);
        start.setTypeface(Typeface.DEFAULT_BOLD);
        start.setOnClickListener(soon(a, "Workout mode"));
        LinearLayout.LayoutParams startLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(a, 56));
        startLp.topMargin = dp(a, 22);
        card.addView(start, startLp);
        return card;
    }

    private static View statsRow(Activity a, RingView ring) {
        LinearLayout row = new LinearLayout(a);
        row.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout streak = card(a);
        streak.addView(cardLabel(a, "Streak"));
        LinearLayout num = numberWithUnit(a, String.valueOf(SampleData.STREAK_DAYS), 44, "🔥", 26,
                Palette.TEXT, Palette.TEXT);
        num.setPadding(0, dp(a, 6), 0, 0);
        streak.addView(num);
        streak.addView(Views.text(a, "days in a row", 13, Palette.MUTED));
        LinearLayout.LayoutParams bestLp = wrap();
        bestLp.topMargin = dp(a, 16);
        streak.addView(Views.chip(a, "BEST · " + SampleData.BEST_STREAK, Palette.LIME, Palette.LIME_TINT), bestLp);

        LinearLayout goal = card(a);
        goal.addView(cardLabel(a, "Weekly goal"));
        LinearLayout.LayoutParams ringLp = new LinearLayout.LayoutParams(dp(a, 108), dp(a, 108));
        ringLp.gravity = Gravity.CENTER_HORIZONTAL;
        ringLp.topMargin = dp(a, 10);
        goal.addView(ring, ringLp);

        LinearLayout.LayoutParams left = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        left.rightMargin = dp(a, 6);
        LinearLayout.LayoutParams right = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        right.leftMargin = dp(a, 6);
        row.addView(streak, left);
        row.addView(goal, right);
        return row;
    }

    private static View weeklyCard(Activity a, BarChartView bars) {
        LinearLayout card = card(a);

        LinearLayout top = new LinearLayout(a);
        top.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout col = new LinearLayout(a);
        col.setOrientation(LinearLayout.VERTICAL);
        col.addView(cardLabel(a, "Last 7 days"));
        float total = 0;
        for (float v : SampleData.LAST_7_DAYS_VOLUME) total += v;
        col.addView(numberWithUnit(a, String.format(Locale.US, "%,d", (long) total), 32, "lb", 16,
                Palette.TEXT, Palette.MUTED));
        col.addView(Views.text(a, "Total volume lifted", 13, Palette.MUTED));
        top.addView(col, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        top.addView(Views.chip(a, "▲ " + SampleData.VOLUME_CHANGE_PCT + "%", Palette.LIME, Palette.LIME_TINT));
        card.addView(top);

        LinearLayout.LayoutParams barsLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(a, 150));
        barsLp.topMargin = dp(a, 20);
        card.addView(bars, barsLp);
        return card;
    }

    private static View voiceCard(Activity a) {
        LinearLayout card = new LinearLayout(a);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        int pad = dp(a, 18);
        card.setPadding(pad, pad, pad, pad);
        GradientDrawable bg = Views.rounded(a, Palette.SURFACE, 24);
        bg.setStroke(dp(a, 1), Palette.STROKE);
        card.setBackground(Views.ripple(bg, 0x22FFFFFF));
        card.setOnClickListener(soon(a, "Voice logging"));

        card.addView(new MicView(a), new LinearLayout.LayoutParams(dp(a, 52), dp(a, 52)));

        LinearLayout col = new LinearLayout(a);
        col.setOrientation(LinearLayout.VERTICAL);
        col.addView(Views.text(a, "Just say it", 17, Palette.TEXT, Palette.MEDIUM));
        col.addView(Views.text(a, "“Squat, 3 sets of 5 at 225”", 14, Palette.MUTED,
                Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)));
        TextView ai = Views.text(a, "AI logs it for you", 12, Palette.LIME, Palette.MEDIUM);
        ai.setPadding(0, dp(a, 4), 0, 0);
        col.addView(ai);
        LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        colLp.leftMargin = dp(a, 16);
        card.addView(col, colLp);

        card.addView(Views.text(a, "›", 30, Palette.MUTED));
        return card;
    }

    private static View sectionTitle(Activity a, String title, String action) {
        LinearLayout row = new LinearLayout(a);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(Views.text(a, title, 20, Palette.TEXT, Palette.BLACK),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView link = Views.text(a, action, 14, Palette.LIME, Palette.MEDIUM);
        link.setOnClickListener(soon(a, "Records history"));
        row.addView(link);
        return row;
    }

    private static View recordsStrip(Activity a) {
        HorizontalScrollView hsv = new HorizontalScrollView(a);
        hsv.setHorizontalScrollBarEnabled(false);
        hsv.setClipToPadding(false);
        int side = dp(a, 20);
        hsv.setPadding(side, 0, side, 0);

        LinearLayout strip = new LinearLayout(a);
        strip.setOrientation(LinearLayout.HORIZONTAL);
        List<SampleData.PersonalRecord> prs = SampleData.personalRecords();
        for (int i = 0; i < prs.size(); i++) {
            SampleData.PersonalRecord pr = prs.get(i);
            LinearLayout card = card(a);
            if (pr.isNew) ((GradientDrawable) card.getBackground()).setStroke(dp(a, 1.5f), Palette.LIME);

            TextView lift = Views.text(a, pr.lift.toUpperCase(Locale.US), 11, Palette.MUTED, Palette.MEDIUM);
            lift.setLetterSpacing(0.1f);
            card.addView(lift);
            LinearLayout w = numberWithUnit(a, RuleBasedPredictor.format(pr.weight), 34, "lb", 14,
                    Palette.TEXT, Palette.MUTED);
            w.setPadding(0, dp(a, 6), 0, 0);
            card.addView(w);
            card.addView(Views.text(a, pr.when, 12, Palette.MUTED));
            if (pr.isNew) {
                LinearLayout.LayoutParams chipLp = wrap();
                chipLp.topMargin = dp(a, 12);
                card.addView(Views.chip(a, "NEW PR", Palette.BG, Palette.LIME), chipLp);
            }

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    dp(a, 150), ViewGroup.LayoutParams.MATCH_PARENT);
            if (i < prs.size() - 1) lp.rightMargin = dp(a, 12);
            strip.addView(card, lp);
        }
        hsv.addView(strip);

        LinearLayout.LayoutParams bleed = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bleed.leftMargin = -side;
        bleed.rightMargin = -side;
        bleed.topMargin = dp(a, 14);
        hsv.setLayoutParams(bleed);
        return hsv;
    }

    // ---- Helpers ----

    private static LinearLayout card(Activity a) {
        LinearLayout card = new LinearLayout(a);
        card.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(a, 18);
        card.setPadding(pad, pad, pad, pad);
        GradientDrawable bg = Views.rounded(a, Palette.SURFACE, 24);
        bg.setStroke(dp(a, 1), Palette.STROKE);
        card.setBackground(bg);
        return card;
    }

    private static TextView cardLabel(Activity a, String s) {
        return Views.text(a, s, 13, Palette.MUTED, Palette.MEDIUM);
    }

    private static LinearLayout numberWithUnit(Activity a, String number, float sp, String unit,
                                               float unitSp, int color, int unitColor) {
        LinearLayout row = new LinearLayout(a);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBaselineAligned(true);
        TextView n = Views.text(a, number, sp, color, Palette.BLACK);
        n.setIncludeFontPadding(false);
        row.addView(n);
        TextView u = Views.text(a, unit, unitSp, unitColor, Palette.MEDIUM);
        u.setPadding(dp(a, 6), 0, 0, 0);
        row.addView(u);
        return row;
    }

    private static LinearLayout.LayoutParams below(Activity a, int topDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(a, topDp);
        return lp;
    }

    private static LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private static String deltaText(double delta) {
        if (delta > 0) return "▲ +" + RuleBasedPredictor.format(delta) + " LB VS LAST SESSION";
        if (delta < 0) return "▼ " + RuleBasedPredictor.format(delta) + " LB VS LAST SESSION";
        return "= SAME AS LAST SESSION";
    }

    private static String greeting() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (h < 5) return "Up late";
        if (h < 12) return "Good morning";
        if (h < 17) return "Good afternoon";
        return "Good evening";
    }

    private static String[] last7DayLabels() {
        String[] out = new String[7];
        SimpleDateFormat fmt = new SimpleDateFormat("EEEEE", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6);
        for (int i = 0; i < 7; i++) {
            out[i] = fmt.format(cal.getTime());
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return out;
    }

    private static void animateEntrance(Activity a, LinearLayout content, View fab) {
        DecelerateInterpolator ease = new DecelerateInterpolator(2f);
        for (int i = 0; i < content.getChildCount(); i++) {
            View v = content.getChildAt(i);
            v.setAlpha(0f);
            v.setTranslationY(dp(a, 28));
            v.animate().alpha(1f).translationY(0).setStartDelay(70L * i)
                    .setDuration(550).setInterpolator(ease).start();
        }
        fab.setAlpha(0f);
        fab.setScaleX(0.85f);
        fab.setScaleY(0.85f);
        fab.animate().alpha(1f).scaleX(1f).scaleY(1f).setStartDelay(550)
                .setDuration(450).setInterpolator(ease).start();
    }

    private static View.OnClickListener soon(final Activity a, final String feature) {
        return new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(a, feature + " is coming in round two", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private static int dp(Activity a, float v) {
        return Views.dp(a, v);
    }
}
