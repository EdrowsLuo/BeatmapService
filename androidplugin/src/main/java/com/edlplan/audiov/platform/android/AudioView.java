package com.edlplan.audiov.platform.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.edlplan.audiov.core.audio.IAudioEntry;
import com.edlplan.audiov.core.visual.BaseVisualizer;
import com.edlplan.audiov.core.visual.EdlAudioVisualizer;

public class AudioView extends View {

    BaseVisualizer visualizer;

    IAudioEntry audioEntry;

    public AudioView(Context context) {
        super(context);
    }

    public AudioView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public BaseVisualizer getVisualizer() {
        return visualizer;
    }

    public void setAudioEntry(IAudioEntry audioEntry) {
        this.audioEntry = audioEntry;
        if (visualizer != null) {
            visualizer.changeAudio(audioEntry);
        }
    }

    public IAudioEntry getAudioEntry() {
        return audioEntry;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (visualizer == null) {
            visualizer = new EdlAudioVisualizer();
            visualizer.setBaseSize(Math.min(getWidth(), getHeight()));
            visualizer.prepare();
            visualizer.changeAudio(audioEntry);
        }

        visualizer.update();
        visualizer.draw();

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Bitmap result = ((AndroidTexture) visualizer.getResult()).getBitmap();
        int hsize = Math.min(canvas.getWidth(), canvas.getHeight()) / 2;
        float cx = canvas.getWidth() / 2;
        float cy = canvas.getHeight() / 2;
        canvas.drawBitmap(
                result,
                new Rect(0, 0, result.getWidth(), result.getHeight()),
                new RectF(cx - hsize, cy - hsize, cx + hsize, cy + hsize),
                paint
        );
        invalidate();
    }
}
