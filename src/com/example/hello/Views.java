package com.example.hello;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Small helpers for building the UI in code. */
public final class Views {
    private Views() {}

    public static int dp(Context c, float dp) {
        return Math.round(dp * c.getResources().getDisplayMetrics().density);
    }

    public static TextView text(Context c, String s, float sp, int color) {
        TextView tv = new TextView(c);
        tv.setText(s);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        return tv;
    }

    public static TextView text(Context c, String s, float sp, int color, Typeface font) {
        TextView tv = text(c, s, sp, color);
        tv.setTypeface(font);
        return tv;
    }

    public static GradientDrawable rounded(Context c, int color, float radiusDp) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(c, radiusDp));
        return bg;
    }

    public static Drawable ripple(Drawable content, int rippleColor) {
        return new RippleDrawable(ColorStateList.valueOf(rippleColor), content, null);
    }

    /** A small rounded label, e.g. "AI COACH" or "NEW PR". */
    public static TextView chip(Context c, String s, int textColor, int bgColor) {
        TextView tv = text(c, s, 11, textColor, Typeface.DEFAULT_BOLD);
        tv.setLetterSpacing(0.1f);
        tv.setPadding(dp(c, 10), dp(c, 5), dp(c, 10), dp(c, 5));
        tv.setBackground(rounded(c, bgColor, 100));
        return tv;
    }

    /** A colored circle with the user's initial. */
    public static TextView avatar(Context c, String initial, int sizeDp) {
        TextView tv = new TextView(c);
        tv.setText(initial);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(sizeDp * 0.42f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setGravity(Gravity.CENTER);
        GradientDrawable circle = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, new int[] {0xFF8E3DFF, 0xFFE0489A});
        circle.setShape(GradientDrawable.OVAL);
        tv.setBackground(circle);
        int size = dp(c, sizeDp);
        tv.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        return tv;
    }

    /** A rounded button with a ripple. Pass a non-zero strokeColor for an outlined style. */
    public static Button pillButton(Context c, String label, int fill, int textColor, int strokeColor) {
        Button b = new Button(c);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setTextColor(textColor);
        b.setStateListAnimator(null);
        GradientDrawable bg = rounded(c, fill, 100);
        if (strokeColor != 0) bg.setStroke(dp(c, 1), strokeColor);
        b.setBackground(ripple(bg, 0x33808080));
        b.setPadding(dp(c, 24), 0, dp(c, 24), 0);
        return b;
    }
}
