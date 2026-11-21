package com.example.hello;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Button;
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

    /** A colored circle with the user's initial. */
    public static TextView avatar(Context c, String initial, int sizeDp) {
        TextView tv = new TextView(c);
        tv.setText(initial);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(sizeDp * 0.42f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setGravity(Gravity.CENTER);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(0xFF7B5CC4);
        tv.setBackground(circle);
        int size = dp(c, sizeDp);
        tv.setLayoutParams(new android.widget.LinearLayout.LayoutParams(size, size));
        return tv;
    }

    /** A rounded button. Pass a non-zero strokeColor for an outlined style. */
    public static Button pillButton(Context c, String label, int fill, int textColor, int strokeColor) {
        Button b = new Button(c);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setTextColor(textColor);
        b.setStateListAnimator(null);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(fill);
        bg.setCornerRadius(dp(c, 24));
        if (strokeColor != 0) bg.setStroke(dp(c, 1), strokeColor);
        b.setBackground(bg);
        b.setPadding(dp(c, 24), 0, dp(c, 24), 0);
        return b;
    }
}
