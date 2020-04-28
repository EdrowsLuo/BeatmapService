package com.edlplan.beatmapservice.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class TriangleEffectView extends View {

    private TriangleDrawable triangleDrawable;

    public TriangleEffectView(Context context) {
        super(context);
        setBackground(triangleDrawable = new TriangleDrawable());
    }

    public TriangleEffectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackground(triangleDrawable = new TriangleDrawable());
    }

    public TriangleEffectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackground(triangleDrawable = new TriangleDrawable());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TriangleEffectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public TriangleDrawable getTriangleDrawable() {
        return triangleDrawable;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        invalidate();
    }
}
