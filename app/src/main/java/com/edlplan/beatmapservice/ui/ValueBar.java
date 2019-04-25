package com.edlplan.beatmapservice.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class ValueBar extends View {

    private int maxValue = 100;

    private double displayValue = 0;

    private int value = 0;

    private Animation animation;

    private Paint backgroundPaint = new Paint();

    private Paint barPaint = new Paint();

    public ValueBar(Context context) {
        super(context);
        initialPaint();

    }

    public ValueBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialPaint();
    }

    public ValueBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialPaint();
    }

    public void setBackgroundColor(int color) {
        backgroundPaint.setColor(color);
        invalidate();
    }

    private void initialPaint() {
        backgroundPaint.setStyle(Paint.Style.FILL);
        barPaint.setStyle(Paint.Style.FILL);
        setBackgroundColor(Color.argb(255, 90, 90, 90));
        setBarColor(0xFFFF4081);
    }

    public void setBarColor(int color) {
        barPaint.setColor(color);
        invalidate();
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public void setValue(int value) {
        if (this.value != value) {
            this.value = value;
            animation = new Animation();
            double endDisplayValue = value > maxValue ? maxValue : (value < 0) ? 0 : value;
            animation.setValue(displayValue, endDisplayValue, 200, Easing.OutQuad);
            animation.setSetter(v -> displayValue = v);
            animation.setOnUpdateListener(i -> invalidate());
            animation.start();
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (animation != null) {
            animation.update();
        }
        double p = Math.min(1, displayValue / maxValue);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
        canvas.drawRect(0, (float) (canvas.getHeight() * (1 - p)), canvas.getWidth(), canvas.getHeight(), barPaint);
    }
}
