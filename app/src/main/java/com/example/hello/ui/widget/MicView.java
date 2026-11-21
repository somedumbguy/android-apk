package com.example.hello.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import com.example.hello.ui.common.Palette;

/** A lime circle with a microphone glyph, drawn in code. */
public final class MicView extends View {
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();

    public MicView(Context c) {
        super(c);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeCap(Paint.Cap.ROUND);
        line.setColor(Palette.BG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float s = Math.min(getWidth(), getHeight());
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        fill.setColor(Palette.LIME);
        canvas.drawCircle(cx, cy, s / 2, fill);

        fill.setColor(Palette.BG);
        r.set(cx - s * 0.09f, cy - s * 0.26f, cx + s * 0.09f, cy + s * 0.06f);
        canvas.drawRoundRect(r, s * 0.09f, s * 0.09f, fill);

        line.setStrokeWidth(s * 0.05f);
        r.set(cx - s * 0.17f, cy - s * 0.22f, cx + s * 0.17f, cy + s * 0.12f);
        canvas.drawArc(r, 0, 180, false, line);
        canvas.drawLine(cx, cy + s * 0.12f, cx, cy + s * 0.23f, line);
        canvas.drawLine(cx - s * 0.09f, cy + s * 0.23f, cx + s * 0.09f, cy + s * 0.23f, line);
    }
}
