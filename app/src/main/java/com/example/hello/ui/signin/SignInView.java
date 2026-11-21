package com.example.hello.ui.signin;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hello.ui.common.Palette;
import com.example.hello.ui.common.Screens;
import com.example.hello.ui.common.Ui;

/** The welcome screen shown when signed out. */
public final class SignInView {
    private SignInView() {}

    public static View create(Context c, boolean isMock, Runnable onSignIn) {
        FrameLayout root = new FrameLayout(c);
        root.setBackgroundColor(Palette.BG);

        root.addView(glow(c, 0xA67C4DFF, 440, Gravity.TOP | Gravity.END, -140, -150));
        root.addView(glow(c, 0x40D4FF3A, 340, Gravity.CENTER_VERTICAL | Gravity.START, 0, -170));

        // Content sits inside its own padded layer, so the glows can still reach the screen edges.
        FrameLayout safe = new FrameLayout(c);
        Screens.padForSystemBars(safe);
        root.addView(safe);

        TextView mark = Ui.text(c, "OVERLOAD", 14, Palette.LIME, Typeface.DEFAULT_BOLD);
        mark.setLetterSpacing(0.3f);
        FrameLayout.LayoutParams markLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.START);
        markLp.topMargin = Ui.dp(c, 24);
        markLp.leftMargin = Ui.dp(c, 28);
        safe.addView(mark, markLp);

        LinearLayout col = Ui.column(c);
        int pad = Ui.dp(c, 28);
        col.setPadding(pad, pad, pad, Ui.dp(c, 28));
        col.addView(Ui.chip(c, "✦  AI LIFTING LOG", Palette.LIME, Palette.LIME_TINT), Ui.wrap(c, 0));

        TextView headline = Ui.text(c, "Lift more.\nLog less.", 52, Palette.TEXT, Palette.BLACK);
        headline.setLineSpacing(0, 0.92f);
        headline.setPadding(0, Ui.dp(c, 18), 0, 0);
        col.addView(headline);

        TextView sub = Ui.text(c,
                "Your AI spotter predicts your next weight and handles the data entry, so you can focus on the lift.",
                16, Palette.MUTED);
        sub.setLineSpacing(0, 1.25f);
        sub.setPadding(0, Ui.dp(c, 16), 0, 0);
        col.addView(sub);

        Button signIn = Ui.pillButton(c, "Sign in with Google", Color.WHITE, 0xFF1F1F1F, 0);
        signIn.setTextSize(16);
        signIn.setTypeface(Typeface.DEFAULT_BOLD);
        signIn.setOnClickListener(v -> onSignIn.run());
        col.addView(signIn, Ui.matchWidth(c, 58, 36));

        if (isMock) {
            TextView note = Ui.text(c, "Mock sign-in for testing", 12, Palette.MUTED);
            note.setGravity(Gravity.CENTER);
            note.setPadding(0, Ui.dp(c, 14), 0, 0);
            col.addView(note);
        }

        safe.addView(col, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
        Screens.staggerIn(col, 120, 90);
        return root;
    }

    /** A soft radial glow used as background decoration. */
    private static View glow(Context c, int color, int sizeDp, int gravity, int topDp, int sideDp) {
        View v = new View(c);
        GradientDrawable g = new GradientDrawable();
        g.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        g.setColors(new int[] {color, color & 0x00FFFFFF});
        g.setGradientRadius(Ui.dp(c, sizeDp) / 2f);
        v.setBackground(g);
        int size = Ui.dp(c, sizeDp);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size, gravity);
        lp.topMargin = Ui.dp(c, topDp);
        if ((gravity & Gravity.END) == Gravity.END) lp.rightMargin = Ui.dp(c, sideDp);
        else lp.leftMargin = Ui.dp(c, sideDp);
        v.setLayoutParams(lp);
        return v;
    }
}
