package com.edlplan.audiov.core.visual;

import com.edlplan.audiov.core.audio.IAudioEntry;
import com.edlplan.audiov.core.graphics.ATexture;

/**
 * 抽象的可视化器
 */
public abstract class AbstractVisualizer {

    protected IAudioEntry entry;

    public void prepare() {
        onPrepare();
    }

    protected void onPrepare() {

    }

    /**
     * @param entry 更换音频
     */
    public final void changeAudio(IAudioEntry entry) {
        this.entry = entry;
        onChangeAudio(entry);
    }

    /**
     * 当音频发生切换的时候被调用
     *
     * @param entry 切换到的音频
     */
    protected void onChangeAudio(IAudioEntry entry) {

    }

    /**
     * 更新一些数据
     */
    public abstract void update();

    /**
     * 进行具体绘制
     */
    public abstract void draw();

    /**
     * @return 绘制的结果
     */
    public abstract ATexture getResult();
}
