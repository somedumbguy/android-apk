package com.example.hello.ui.common;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ScrollView;
import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Screen-level setup shared by every activity. */
public final class Screens {
    private Screens() {}

    /** Draws behind transparent system bars with light icons, on every Android version. */
    public static void setUp(ComponentActivity a) {
        EdgeToEdge.enable(a, SystemBarStyle.dark(Color.TRANSPARENT), SystemBarStyle.dark(Color.TRANSPARENT));
    }

    /** Pads a screen's root view so content stays clear of the status bar, nav bar and cutouts. */
    public static void padForSystemBars(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    public static ScrollView scroller(android.content.Context c, View content) {
        ScrollView scroll = new ScrollView(c);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.addView(content);
        return scroll;
    }

    /** Fades and slides each child of a container in, one after another. */
    public static void staggerIn(ViewGroup container, long startDelayMs, long stepMs) {
        DecelerateInterpolator ease = new DecelerateInterpolator(2f);
        float offset = Ui.dp(container.getContext(), 26);
        for (int i = 0; i < container.getChildCount(); i++) {
            View v = container.getChildAt(i);
            v.setAlpha(0f);
            v.setTranslationY(offset);
            v.animate()
                    .alpha(1f)
                    .translationY(0)
                    .setStartDelay(startDelayMs + stepMs * i)
                    .setDuration(550)
                    .setInterpolator(ease)
                    .start();
        }
    }

    /** Pops a view in with a slight scale. */
    public static void popIn(View v, long delayMs) {
        v.setAlpha(0f);
        v.setScaleX(0.85f);
        v.setScaleY(0.85f);
        v.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(delayMs)
                .setDuration(450)
                .setInterpolator(new DecelerateInterpolator(2f))
                .start();
    }
}
