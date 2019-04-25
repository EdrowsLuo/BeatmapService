package com.edlplan.beatmapservice.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.Arrays;

public class ValueListView extends View {

    private int size = 51;

    private int maxValue = 5;

    private double[] displayValue = new double[size];

    private int[] value = new int[size];

    private Animation[] animation = new Animation[size];

    private Paint backgroundPaint = new Paint();

    private Paint barPaint = new Paint();

    private Paint gridPaint = new Paint();

    public ValueListView(Context context) {
        super(context);
        initialPaint();

    }

    public ValueListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialPaint();
    }

    public ValueListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        barPaint.setStrokeWidth(4);
        barPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(1);
        gridPaint.setColor(0xFFFFA700);
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

    public void setValue(int[] rvalue) {
        if (this.value != rvalue) {
            this.value = Arrays.copyOf(rvalue, size);
            for (int i = 0; i < size; i++) {
                final int ii = i;
                animation[i] = new Animation();
                double endDisplayValue = value[i] > maxValue ? maxValue : (value[i] < 0) ? 0 : value[i];
                animation[i].setValue(displayValue[i], endDisplayValue, 200, Easing.OutQuad);
                animation[i].setSetter(v -> displayValue[ii] = v);
                animation[i].setOnUpdateListener(iii -> invalidate());
                animation[i].start();
            }

            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (animation != null) {
            for (Animation a : animation) {
                if (a != null) {
                    a.update();
                }
            }
        }

        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
        float unitY = canvas.getHeight() / maxValue;
        for (int i = 0; i < maxValue; i++) {
            canvas.drawLine(0, unitY * i, canvas.getWidth(), unitY * i, gridPaint);
        }
        double deltaX = canvas.getWidth() / (double) (size - 1);

        Path path = new Path();
        path.moveTo(0, canvas.getHeight());
        for (int i = 0; i < size; i++) {
            double p = Math.min(1, displayValue[i] / maxValue);
            path.lineTo((float) deltaX * i, (float) (canvas.getHeight() * (1 - p)));
        }
        path.lineTo(canvas.getWidth(), canvas.getHeight());
        path.close();
        //path.setFillType(Path.FillType.EVEN_ODD);

        barPaint.setAlpha(100);
        barPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, barPaint);
        barPaint.setAlpha(255);
        barPaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, barPaint);
        /*{
            Path path2 = new Path();
            path2.moveTo(0, canvas.getHeight());
            path2.moveTo(0, 0);
            path2.moveTo(canvas.getWidth(), canvas.getHeight());
            path2.close();
            path2.setFillType(Path.FillType.EVEN_ODD);
            canvas.drawPath(path2, barPaint);
        }*/
        //canvas.drawRect(0, (float) (canvas.getHeight() * (1 - p)), canvas.getWidth(), canvas.getHeight(), barPaint);
    }
}
