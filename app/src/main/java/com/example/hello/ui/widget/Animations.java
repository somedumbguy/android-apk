package com.example.hello.ui.widget;

import android.animation.ValueAnimator;
import android.view.animation.DecelerateInterpolator;
import java.util.function.Consumer;

/** Shared animation helper for the custom chart views. */
final class Animations {
    private Animations() {}

    /** Animates a float from 0 to {@code to}, calling {@code onFrame} each frame. */
    static void easeIn(float to, long durationMs, long delayMs, Consumer<Float> onFrame) {
        ValueAnimator va = ValueAnimator.ofFloat(0f, to);
        va.setDuration(durationMs);
        va.setStartDelay(delayMs);
        va.setInterpolator(new DecelerateInterpolator(2f));
        va.addUpdateListener(a -> onFrame.accept((Float) a.getAnimatedValue()));
        va.start();
    }
}
