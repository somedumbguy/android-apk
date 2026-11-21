package com.example.hello.ui.common;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Small, reusable building blocks for UI written in code. */
public final class Ui {
    private Ui() {}

    // ---- Units ----

    public static int dp(Context c, float dp) {
        return Math.round(dp * c.getResources().getDisplayMetrics().density);
    }

    /** Text size in pixels, honouring the user's font scale. */
    public static float sp(Context c, float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, c.getResources().getDisplayMetrics());
    }

    // ---- Text ----

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

    /** Small uppercase label with letter spacing, e.g. "PROJECTED MILESTONES". */
    public static TextView caps(Context c, String s, float sp, int color) {
        TextView tv = text(c, s, sp, color, Typeface.DEFAULT_BOLD);
        tv.setLetterSpacing(0.1f);
        return tv;
    }

    /** A small rounded label, e.g. "COACH" or "NEW PR". */
    public static TextView chip(Context c, String s, int textColor, int bgColor) {
        TextView tv = caps(c, s, 11, textColor);
        tv.setPadding(dp(c, 10), dp(c, 5), dp(c, 10), dp(c, 5));
        tv.setBackground(rounded(c, bgColor, 100));
        return tv;
    }

    // ---- Shapes ----

    public static GradientDrawable rounded(Context c, int color, float radiusDp) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(c, radiusDp));
        return bg;
    }

    public static GradientDrawable circle(int color) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(color);
        return bg;
    }

    public static Drawable ripple(Drawable content, int rippleColor) {
        return new RippleDrawable(ColorStateList.valueOf(rippleColor), content, null);
    }

    // ---- Controls ----

    /** A colored circle with the user's initial. */
    public static TextView avatar(Context c, String initial, int sizeDp) {
        TextView tv = text(c, initial, sizeDp * 0.42f, Color.WHITE, Typeface.DEFAULT_BOLD);
        tv.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR, Palette.AVATAR_GRADIENT);
        bg.setShape(GradientDrawable.OVAL);
        tv.setBackground(bg);
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

    /** The app's main call-to-action button: lime, bold, full width. */
    public static Button primaryButton(Context c, String label) {
        Button b = pillButton(c, label, Palette.LIME, Palette.BG, 0);
        b.setTextSize(16);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        return b;
    }

    public static void toast(Context c, String message) {
        Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
    }

    public static View.OnClickListener comingSoon(Context c, String feature) {
        return v -> toast(c, feature + " is coming soon");
    }

    // ---- Layout params ----

    public static LinearLayout.LayoutParams matchWidth(Context c, int heightDp, int topMarginDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                heightDp <= 0 ? ViewGroup.LayoutParams.WRAP_CONTENT : dp(c, heightDp));
        lp.topMargin = dp(c, topMarginDp);
        return lp;
    }

    public static LinearLayout.LayoutParams wrap(Context c, int topMarginDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(c, topMarginDp);
        return lp;
    }

    /** Fills the remaining width in a horizontal LinearLayout. */
    public static LinearLayout.LayoutParams fill() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
    }

    public static LinearLayout row(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    public static LinearLayout column(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    // ---- Bottom sheets ----

    /** Shows content as a bottom sheet that slides up, with rounded top corners. */
    public static Dialog showSheet(Activity a, View content, int bgColor) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(bgColor);
        float r = dp(a, 28);
        bg.setCornerRadii(new float[] {r, r, r, r, 0, 0, 0, 0});
        content.setBackground(bg);

        // Keep content above the navigation bar when the window draws behind it.
        int baseBottom = content.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), baseBottom + nav.bottom);
            return insets;
        });

        Dialog d = new Dialog(a);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(content);
        Window w = d.getWindow();
        w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        w.setGravity(Gravity.BOTTOM);
        w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        w.setWindowAnimations(android.R.style.Animation_InputMethod);
        d.show();
        return d;
    }

    /** The small bar at the top of a bottom sheet. */
    public static View sheetHandle(Context c, int color) {
        View handle = new View(c);
        handle.setBackground(rounded(c, color, 2));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(c, 32), dp(c, 4));
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        lp.bottomMargin = dp(c, 18);
        handle.setLayoutParams(lp);
        return handle;
    }
}
