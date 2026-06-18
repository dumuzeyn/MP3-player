package com.dumuzeyn.mp3player;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class WaveformView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int seed;
    private boolean active;
    private long startedAt;

    public WaveformView(Context context, String key, int color, boolean active) {
        super(context);
        this.seed = Math.abs(key.hashCode());
        this.active = active;
        this.startedAt = System.currentTimeMillis();
        paint.setColor(color);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setActive(boolean active) {
        this.active = active;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        int bars = 24;
        float gap = width / (bars * 1.55f);
        float barWidth = Math.max(3f, gap * 0.34f);
        paint.setStrokeWidth(barWidth);
        float time = (System.currentTimeMillis() - startedAt) / (active ? 180f : 520f);

        for (int i = 0; i < bars; i++) {
            float x = gap + i * gap * 1.48f;
            float base = 0.24f + ((seed >> (i % 12)) & 15) / 22f;
            float pulse = active ? (float) Math.sin(time + i * 0.7f) * 0.22f : 0f;
            float bar = Math.max(0.18f, Math.min(0.92f, base + pulse));
            float center = height * 0.55f;
            float half = height * bar * 0.38f;
            canvas.drawLine(x, center - half, x, center + half, paint);
        }

        if (active) postInvalidateDelayed(48);
    }
}

