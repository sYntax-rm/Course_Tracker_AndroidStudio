package com.example.dcfg_studyfi_casestuy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


public class TimerProgressView extends View {

    private Paint trackPaint;
    private Paint progressPaint;
    private RectF oval;

    private float progress   = 0f;   // 0.0 → 1.0
    private int   strokeWidth = 12;  // dp, converted in init
    private int   progressColor = Color.parseColor("#6C8EFF");

    public TimerProgressView(Context context) {
        super(context);
        init();
    }

    public TimerProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimerProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        float stroke = strokeWidth * density;

        // Background track
        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(stroke);
        trackPaint.setColor(Color.parseColor("#2A2D3E"));
        trackPaint.setStrokeCap(Paint.Cap.ROUND);

        // Progress arc
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(stroke);
        progressPaint.setColor(progressColor);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        oval = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float density = getResources().getDisplayMetrics().density;
        float stroke = strokeWidth * density;
        float half = stroke / 2f;

        oval.set(half, half, getWidth() - half, getHeight() - half);

        // Draw full background track
        canvas.drawArc(oval, -90, 360, false, trackPaint);

        // Draw progress arc (clockwise from top)
        float sweepAngle = 360f * progress;
        if (sweepAngle > 0) {
            canvas.drawArc(oval, -90, sweepAngle, false, progressPaint);
        }
    }

    /**
     * Set current progress (0.0 = empty, 1.0 = full)
     */
    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
        invalidate();
    }

    /**
     * Change the accent color of the arc (call when switching modes)
     */
    public void setProgressColor(int color) {
        this.progressColor = color;
        progressPaint.setColor(color);
        invalidate();
    }
}

