package com.edlplan.audiov.core.visual;


import com.edlplan.audiov.core.audio.IAudioEntry;
import com.edlplan.audiov.core.option.IHasOption;
import com.edlplan.audiov.core.option.OptionEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * 对数据进行初始处理
 */
public abstract class BaseVisualizer extends AbstractVisualizer implements IHasOption {

    /**
     * 获取的fft数据数组
     */
    protected float[] fftData;

    protected int baseSize;

    public BaseVisualizer() {
        fftData = new float[512];
    }

    public float getBaseSize() {
        return baseSize;
    }

    public void setBaseSize(int baseSize) {
        if (Math.abs(baseSize - this.baseSize) > 0.5) {
            this.baseSize = baseSize;
            onBaseSizeChanged(baseSize);
        }
    }

    protected void onBaseSizeChanged(int newSize) {

    }

    @Override
    public void update() {
        if (entry == null || !entry.isPlaying()) {
            return;
        }
        entry.getFFT(fftData, IAudioEntry.FLAG_FFT1024);
        onUpdateFFT();
    }

    /**
     * 在这个方法里处理fft数据，具体的数据在成员变量fftData里
     */
    protected void onUpdateFFT() {

    }

    @Override
    public Map<String, OptionEntry<?>> dumpOptions() {
        return new HashMap<>();
    }

    @Override
    public void applyOptions(Map<String, OptionEntry<?>> options) {

    }
}
