package com.example.hello.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.view.View;
import com.example.hello.ui.common.Palette;
import com.example.hello.ui.common.Ui;

/** Animated progress ring with a label in the middle, e.g. "3/4 workouts". */
public final class RingView extends View {
    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arc = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint big = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint small = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();
    private final float stroke;
    private float progress;
    private float target;
    private String label = "";
    private String caption = "";

    public RingView(Context c) {
        super(c);
        float d = c.getResources().getDisplayMetrics().density;
        stroke = 10 * d;

        track.setStyle(Paint.Style.STROKE);
        track.setStrokeWidth(stroke);
        track.setColor(Palette.SURFACE_2);

        arc.setStyle(Paint.Style.STROKE);
        arc.setStrokeWidth(stroke);
        arc.setStrokeCap(Paint.Cap.ROUND);

        big.setColor(Palette.TEXT);
        big.setTypeface(Palette.BLACK);
        big.setTextAlign(Paint.Align.CENTER);
        big.setTextSize(Ui.sp(c, 24));

        small.setColor(Palette.MUTED);
        small.setTextAlign(Paint.Align.CENTER);
        small.setTextSize(Ui.sp(c, 11));
    }

    public void setData(int done, int goal, String caption) {
        target = goal <= 0 ? 0 : Math.min(1f, done / (float) goal);
        label = done + "/" + goal;
        this.caption = caption;
        invalidate();
    }

    public void animateIn(long delayMs) {
        Animations.easeIn(target, 1200, delayMs, v -> {
            progress = v;
            invalidate();
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float s = Math.min(w, h);
        float pad = stroke / 2 + 1;
        oval.set((w - s) / 2 + pad, (h - s) / 2 + pad, (w + s) / 2 - pad, (h + s) / 2 - pad);
        arc.setShader(new SweepGradient(w / 2f, h / 2f, new int[] {Palette.MINT, Palette.LIME, Palette.MINT}, null));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        canvas.drawArc(oval, 0, 360, false, track);
        if (progress > 0) {
            canvas.save();
            canvas.rotate(-90, cx, cy);
            canvas.drawArc(oval, 0, 360 * progress, false, arc);
            canvas.restore();
        }
        canvas.drawText(label, cx, cy + big.getTextSize() * 0.2f, big);
        canvas.drawText(caption, cx, cy + big.getTextSize() * 0.2f + small.getTextSize() * 1.5f, small);
    }
}
