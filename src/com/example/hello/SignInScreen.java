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
import android.widget.LinearLayout;
import android.widget.TextView;

/** The welcome screen shown when signed out. */
public final class SignInScreen {
    private SignInScreen() {}

    public static View create(Activity a, boolean isMock, final Runnable onSignIn) {
        FrameLayout root = new FrameLayout(a);
        root.setBackgroundColor(Palette.BG);

        root.addView(glow(a, 0xA67C4DFF, 440, Gravity.TOP | Gravity.END, -140, -150));
        root.addView(glow(a, 0x40D4FF3A, 340, Gravity.CENTER_VERTICAL | Gravity.START, 0, -170));

        TextView mark = Views.text(a, "OVERLOAD", 14, Palette.LIME, Typeface.DEFAULT_BOLD);
        mark.setLetterSpacing(0.3f);
        FrameLayout.LayoutParams markLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.START);
        markLp.topMargin = Views.dp(a, 40);
        markLp.leftMargin = Views.dp(a, 28);
        root.addView(mark, markLp);

        LinearLayout col = new LinearLayout(a);
        col.setOrientation(LinearLayout.VERTICAL);
        int pad = Views.dp(a, 28);
        col.setPadding(pad, pad, pad, Views.dp(a, 40));

        LinearLayout.LayoutParams chipLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        col.addView(Views.chip(a, "✦  AI LIFTING LOG", Palette.LIME, Palette.LIME_TINT), chipLp);

        TextView headline = Views.text(a, "Lift more.\nLog less.", 52, Palette.TEXT, Palette.BLACK);
        headline.setLineSpacing(0, 0.92f);
        headline.setPadding(0, Views.dp(a, 18), 0, 0);
        col.addView(headline);

        TextView sub = Views.text(a,
                "Your AI spotter predicts your next weight and handles the data entry, so you can focus on the lift.",
                16, Palette.MUTED);
        sub.setLineSpacing(0, 1.25f);
        sub.setPadding(0, Views.dp(a, 16), 0, 0);
        col.addView(sub);

        Button signIn = Views.pillButton(a, "Sign in with Google", Color.WHITE, 0xFF1F1F1F, 0);
        signIn.setTextSize(16);
        signIn.setTypeface(Typeface.DEFAULT_BOLD);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { onSignIn.run(); }
        });
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Views.dp(a, 58));
        btnLp.topMargin = Views.dp(a, 36);
        col.addView(signIn, btnLp);

        if (isMock) {
            TextView note = Views.text(a, "Mock sign-in for testing", 12, Palette.MUTED);
            note.setGravity(Gravity.CENTER);
            note.setPadding(0, Views.dp(a, 14), 0, 0);
            col.addView(note);
        }

        root.addView(col, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));

        DecelerateInterpolator ease = new DecelerateInterpolator(2f);
        for (int i = 0; i < col.getChildCount(); i++) {
            View v = col.getChildAt(i);
            v.setAlpha(0f);
            v.setTranslationY(Views.dp(a, 24));
            v.animate().alpha(1f).translationY(0).setStartDelay(120L + 90L * i)
                    .setDuration(600).setInterpolator(ease).start();
        }
        return root;
    }

    /** A soft radial glow used as background decoration. */
    private static View glow(Activity a, int color, int sizeDp, int gravity, int topDp, int sideDp) {
        View v = new View(a);
        GradientDrawable g = new GradientDrawable();
        g.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        g.setColors(new int[] {color, color & 0x00FFFFFF});
        g.setGradientRadius(Views.dp(a, sizeDp) / 2f);
        v.setBackground(g);
        int size = Views.dp(a, sizeDp);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size, gravity);
        lp.topMargin = Views.dp(a, topDp);
        if ((gravity & Gravity.END) == Gravity.END) lp.rightMargin = Views.dp(a, sideDp);
        else lp.leftMargin = Views.dp(a, sideDp);
        return v;
    }
}
