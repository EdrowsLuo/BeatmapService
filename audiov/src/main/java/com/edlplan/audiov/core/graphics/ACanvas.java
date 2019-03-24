package com.edlplan.audiov.core.graphics;

import com.edlplan.audiov.core.AudioVCore;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 对Canvas的抽象，定义了一些最基础的操作
 */
public abstract class ACanvas {

    private ATexture target;

    public ACanvas(ATexture target) {
        this.target = target;
    }

    /**
     * 通过一个材质创建画板，需要设置了全局工厂
     *
     * @param texture
     * @return
     */
    public static ACanvas of(ATexture texture) {
        return AudioVCore.getInstance().graphics().getCanvasFactory().create(texture);
    }

    public ATexture getTarget() {
        return target;
    }

    /**
     * 开始绘制
     */
    public abstract void start();

    /**
     * 结束绘制
     */
    public abstract void end();

    /**
     * 用某种颜色替换整个画板
     *
     * @param r,g,b,a 替换的颜色
     */
    public abstract void clear(float r, float g, float b, float a);

    /**
     * 在画板指定位置绘制一个材质
     *
     * @param texture 要绘制的材质
     * @param ox      选取区域的材质x坐标
     * @param oy      选取区域的材质y坐标
     * @param ow      选取的材质宽度
     * @param oh      选取的材质高度
     * @param cx      绘制起始位置的x坐标
     * @param cy      绘制起始位置的y坐标
     * @param cw      绘制区域的宽度
     * @param ch      绘制区域的高度
     * @param alpha   透明度
     */
    public abstract void drawTexture(
            ATexture texture,
            int ox, int oy, int ow, int oh,
            float cx, float cy, float cw, float ch,
            float alpha);

    /**
     * 简化参数的绘制
     *
     * @param texture 要绘制的材质
     * @param cx      在画板的起始x位置
     * @param cy      在画板的起始y位置
     * @param alpha   透明度
     */
    public void drawTexture(ATexture texture, int cx, int cy, float alpha) {
        drawTexture(
                texture,
                0, 0, texture.getWidth(), texture.getHeight(),
                cx, cy, texture.getWidth(), texture.getHeight(),
                alpha
        );
    }

    /**
     * 绘制一些线
     *
     * @param lineData  绘制的线的起始点和结束点的集合，2个数据一组
     * @param lineWidth 绘制的线的宽度
     * @param r,g,b,a   线的颜色
     */
    public abstract void drawLines(
            float[] lineData,
            float lineWidth, float r, float g, float b, float a);

    /**
     * 创建ACanvas的工厂
     */
    public static abstract class AFactory {
        public abstract ACanvas create(ATexture texture);
    }

}
