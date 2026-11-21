package com.example.hello.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hello.ui.common.Cards;
import com.example.hello.ui.common.Palette;
import com.example.hello.ui.common.Screens;
import com.example.hello.ui.common.Ui;
import com.example.hello.ui.widget.BarChartView;
import com.example.hello.ui.widget.MicView;
import com.example.hello.ui.widget.RingView;

/** Draws a {@link HomeState}. Contains no logic: every value comes from the presenter. */
public final class HomeView {
    private HomeView() {}

    public interface Listener {
        void onAvatarTapped();
        void onStartWorkout();
        void onIntensityTapped();
    }

    private static final int SIDE_DP = 20;

    public static View create(Context c, HomeState state, Listener listener) {
        FrameLayout root = new FrameLayout(c);
        root.setBackgroundColor(Palette.BG);

        LinearLayout content = Ui.column(c);
        int side = Ui.dp(c, SIDE_DP);
        content.setPadding(side, Ui.dp(c, 12), side, Ui.dp(c, 120));
        root.addView(Screens.scroller(c, content));

        RingView ring = new RingView(c);
        ring.setData(state.stats.workoutsDone, state.stats.weeklyGoal, "workouts");
        BarChartView bars = new BarChartView(c);
        bars.setData(state.stats.volumes, state.stats.dayLetters, state.stats.volumes.length - 1);

        content.addView(header(c, state, listener));
        content.addView(Cards.sectionTitle(c, "Personal records", "See all", Ui.comingSoon(c, "Records history")),
                Ui.matchWidth(c, 0, 28));
        content.addView(records(c, state));
        content.addView(target(c, state.target, listener), Ui.matchWidth(c, 0, 20));
        content.addView(stats(c, state.stats, ring), Ui.matchWidth(c, 0, 12));
        content.addView(weekly(c, state.stats, bars), Ui.matchWidth(c, 0, 12));
        content.addView(voice(c), Ui.matchWidth(c, 0, 12));

        root.addView(Cards.bottomScrim(c, 140));
        Button fab = Ui.primaryButton(c, "+   Log a set");
        fab.setTextSize(17);
        fab.setPadding(Ui.dp(c, 36), 0, Ui.dp(c, 36), 0);
        fab.setElevation(Ui.dp(c, 8));
        fab.setOnClickListener(v -> listener.onStartWorkout());
        FrameLayout.LayoutParams fabLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                Ui.dp(c, 58), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        fabLp.bottomMargin = Ui.dp(c, 20);
        root.addView(fab, fabLp);

        Screens.staggerIn(content, 0, 70);
        Screens.popIn(fab, 550);
        ring.animateIn(500);
        bars.animateIn(600);
        return root;
    }

    private static View header(Context c, HomeState s, Listener listener) {
        LinearLayout row = Ui.row(c);
        LinearLayout col = Ui.column(c);
        TextView date = Ui.text(c, s.date, 12, Palette.MUTED, Palette.MEDIUM);
        date.setLetterSpacing(0.12f);
        col.addView(date);
        TextView hi = Ui.text(c, s.greeting, 30, Palette.TEXT, Palette.BLACK);
        hi.setPadding(0, Ui.dp(c, 6), 0, 0);
        col.addView(hi);
        row.addView(col, Ui.fill());

        TextView avatar = Ui.avatar(c, s.initial, 48);
        avatar.setOnClickListener(v -> listener.onAvatarTapped());
        row.addView(avatar);
        return row;
    }

    private static View records(Context c, HomeState s) {
        HorizontalScrollView hsv = new HorizontalScrollView(c);
        hsv.setHorizontalScrollBarEnabled(false);
        hsv.setClipToPadding(false);
        int side = Ui.dp(c, SIDE_DP);
        hsv.setPadding(side, 0, side, 0);

        LinearLayout strip = new LinearLayout(c);
        strip.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < s.records.size(); i++) {
            HomeState.RecordCard r = s.records.get(i);
            LinearLayout card = Cards.card(c);
            if (r.fresh) Cards.outline(card, Palette.LIME);

            LinearLayout top = Ui.row(c);
            TextView lift = Ui.caps(c, r.lift, 11, Palette.MUTED);
            lift.setSingleLine(true);
            top.addView(lift, Ui.fill());
            if (r.fresh) top.addView(Ui.chip(c, "NEW", Palette.BG, Palette.LIME));
            card.addView(top);

            LinearLayout w = Cards.numberWithUnit(c, r.weight, 34, "lb", 14, Palette.TEXT, Palette.MUTED);
            w.setPadding(0, Ui.dp(c, 6), 0, 0);
            card.addView(w);
            card.addView(Ui.text(c, r.when, 12, r.fresh ? Palette.LIME : Palette.MUTED));

            View divider = new View(c);
            divider.setBackgroundColor(Palette.STROKE);
            LinearLayout.LayoutParams divLp = Ui.matchWidth(c, 1, 12);
            divLp.bottomMargin = Ui.dp(c, 10);
            card.addView(divider, divLp);

            card.addView(Ui.caps(c, r.next, 11, Palette.MUTED));
            card.addView(Ui.text(c, r.nextDate, 18, Palette.LIME, Palette.BLACK));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.dp(c, 156),
                    ViewGroup.LayoutParams.MATCH_PARENT);
            if (i < s.records.size() - 1) lp.rightMargin = Ui.dp(c, 12);
            strip.addView(card, lp);
        }
        hsv.addView(strip);

        // Bleed to the screen edges so cards scroll under the side padding.
        LinearLayout.LayoutParams bleed = Ui.matchWidth(c, 0, 14);
        bleed.leftMargin = -side;
        bleed.rightMargin = -side;
        hsv.setLayoutParams(bleed);
        return hsv;
    }

    private static View target(Context c, HomeState.Target t, Listener listener) {
        LinearLayout card = Ui.column(c);
        int pad = Ui.dp(c, 22);
        card.setPadding(pad, pad, pad, pad);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR, Palette.HERO_GRADIENT);
        bg.setCornerRadius(Ui.dp(c, 28));
        card.setBackground(bg);

        LinearLayout top = Ui.row(c);
        top.addView(Ui.chip(c, "✦  COACH", Color.WHITE, 0x2EFFFFFF));
        top.addView(new View(c), Ui.fill());
        TextView mode = Ui.chip(c, t.intensity + "  ▾", Color.WHITE, 0x40000000);
        mode.setPadding(Ui.dp(c, 14), Ui.dp(c, 8), Ui.dp(c, 14), Ui.dp(c, 8));
        mode.setOnClickListener(v -> listener.onIntensityTapped());
        top.addView(mode);
        card.addView(top);

        TextView context = Ui.text(c, t.context, 15, 0xE6FFFFFF, Palette.MEDIUM);
        context.setPadding(0, Ui.dp(c, 20), 0, 0);
        card.addView(context);
        card.addView(Cards.numberWithUnit(c, t.weight, 76, "lb", 22, Color.WHITE, 0xCCFFFFFF));
        card.addView(Ui.text(c, t.reps, 16, 0xE6FFFFFF));

        TextView badge = t.prAttempt
                ? Ui.chip(c, t.badge, Palette.BG, Palette.LIME)
                : Ui.chip(c, t.badge, Palette.LIME, 0x40000000);
        card.addView(badge, Ui.wrap(c, 14));

        TextView reason = Ui.text(c, t.reason, 14, 0xD9FFFFFF);
        reason.setLineSpacing(0, 1.2f);
        reason.setPadding(0, Ui.dp(c, 14), 0, 0);
        card.addView(reason);

        Button start = Ui.primaryButton(c, "Start workout   →");
        start.setOnClickListener(v -> listener.onStartWorkout());
        card.addView(start, Ui.matchWidth(c, 56, 22));
        return card;
    }

    private static View stats(Context c, HomeState.Stats s, RingView ring) {
        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout streak = Cards.card(c);
        streak.addView(Cards.label(c, "Streak"));
        LinearLayout num = Cards.numberWithUnit(c, s.streak, 44, "🔥", 26, Palette.TEXT, Palette.TEXT);
        num.setPadding(0, Ui.dp(c, 6), 0, 0);
        streak.addView(num);
        streak.addView(Ui.text(c, "days in a row", 13, Palette.MUTED));
        streak.addView(Ui.chip(c, s.best, Palette.LIME, Palette.LIME_TINT), Ui.wrap(c, 16));

        LinearLayout goal = Cards.card(c);
        goal.addView(Cards.label(c, "Weekly goal"));
        LinearLayout.LayoutParams ringLp = new LinearLayout.LayoutParams(Ui.dp(c, 108), Ui.dp(c, 108));
        ringLp.gravity = Gravity.CENTER_HORIZONTAL;
        ringLp.topMargin = Ui.dp(c, 10);
        goal.addView(ring, ringLp);

        LinearLayout.LayoutParams left = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        left.rightMargin = Ui.dp(c, 6);
        LinearLayout.LayoutParams right = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        right.leftMargin = Ui.dp(c, 6);
        row.addView(streak, left);
        row.addView(goal, right);
        return row;
    }

    private static View weekly(Context c, HomeState.Stats s, BarChartView bars) {
        LinearLayout card = Cards.card(c);
        LinearLayout top = new LinearLayout(c);
        top.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout col = Ui.column(c);
        col.addView(Cards.label(c, "Last 7 days"));
        col.addView(Cards.numberWithUnit(c, s.totalVolume, 32, "lb", 16, Palette.TEXT, Palette.MUTED));
        col.addView(Ui.text(c, "Total volume lifted", 13, Palette.MUTED));
        top.addView(col, Ui.fill());
        top.addView(Ui.chip(c, s.change, Palette.LIME, Palette.LIME_TINT));
        card.addView(top);
        card.addView(bars, Ui.matchWidth(c, 150, 20));
        return card;
    }

    private static View voice(Context c) {
        LinearLayout card = Ui.row(c);
        int pad = Ui.dp(c, 18);
        card.setPadding(pad, pad, pad, pad);
        card.setBackground(Ui.ripple(Cards.cardBackground(c), 0x22FFFFFF));
        card.setOnClickListener(Ui.comingSoon(c, "Voice logging"));

        card.addView(new MicView(c), new LinearLayout.LayoutParams(Ui.dp(c, 52), Ui.dp(c, 52)));
        LinearLayout col = Ui.column(c);
        col.addView(Ui.text(c, "Just say it", 17, Palette.TEXT, Palette.MEDIUM));
        col.addView(Ui.text(c, "“Squat, 3 sets of 5 at 225”", 14, Palette.MUTED,
                Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)));
        TextView ai = Ui.text(c, "AI logs it for you", 12, Palette.LIME, Palette.MEDIUM);
        ai.setPadding(0, Ui.dp(c, 4), 0, 0);
        col.addView(ai);
        LinearLayout.LayoutParams colLp = Ui.fill();
        colLp.leftMargin = Ui.dp(c, 16);
        card.addView(col, colLp);
        card.addView(Ui.text(c, "›", 30, Palette.MUTED));
        return card;
    }
}
