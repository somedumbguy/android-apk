package com.example.hello.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.example.hello.Palette;

/** Animated bar chart of daily volume. The highlighted bar (today) is drawn in lime. */
public final class BarChartView extends View {
    private final Paint bar = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float d;
    private float[] values = new float[0];
    private String[] labels = new String[0];
    private int highlight = -1;
    private float anim;

    public BarChartView(Context c) {
        super(c);
        d = c.getResources().getDisplayMetrics().density;
        label.setTextAlign(Paint.Align.CENTER);
        label.setTextSize(12 * c.getResources().getDisplayMetrics().scaledDensity);
        label.setTypeface(Palette.MEDIUM);
    }

    public void setData(float[] values, String[] labels, int highlight) {
        this.values = values;
        this.labels = labels;
        this.highlight = highlight;
        invalidate();
    }

    public void animateIn(long delayMs) {
        ValueAnimator va = ValueAnimator.ofFloat(0f, 1f);
        va.setDuration(900);
        va.setStartDelay(delayMs);
        va.setInterpolator(new DecelerateInterpolator(2f));
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator a) {
                anim = (Float) a.getAnimatedValue();
                invalidate();
            }
        });
        va.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int n = values.length;
        if (n == 0) return;
        float w = getWidth();
        float h = getHeight();
        float labelH = 24 * d;
        float chartH = h - labelH;
        float slot = w / n;
        float barW = Math.min(slot * 0.5f, 28 * d);
        float max = 1;
        for (float v : values) max = Math.max(max, v);

        for (int i = 0; i < n; i++) {
            float cx = slot * i + slot / 2;
            boolean hot = i == highlight;
            if (values[i] <= 0) {
                bar.setColor(hot ? Palette.LIME : 0xFF2E2E3A);
                canvas.drawCircle(cx, chartH - 4 * d, 4 * d, bar);
            } else {
                float bh = Math.max(8 * d, (chartH - 8 * d) * (values[i] / max) * anim);
                rect.set(cx - barW / 2, chartH - bh, cx + barW / 2, chartH);
                bar.setColor(hot ? Palette.LIME : 0xFF3A3A48);
                canvas.drawRoundRect(rect, 8 * d, 8 * d, bar);
            }
            if (i < labels.length) {
                label.setColor(hot ? Palette.TEXT : Palette.MUTED);
                canvas.drawText(labels[i], cx, h - 4 * d, label);
            }
        }
    }
}
