package com.edlplan.audiov.core.graphics;

import com.edlplan.audiov.core.AudioVCore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 抽象绘制的目标材质，EdGameFramework对应GLTexture，android对应Bitmap
 */
public abstract class ATexture {


    public static AFactory getFactory() {
        return AudioVCore.getInstance().graphics().getTextureFactory();
    }

    /**
     * @return 材质的宽度
     */
    public abstract int getWidth();

    /**
     * @return 材质的高度
     */
    public abstract int getHeight();

    /**
     * 创建Atexture的工厂
     */
    public static abstract class AFactory {
        /**
         * 创建一个空材质，在反射中使用，子类必须继承
         *
         * @param w 创建的材质的宽
         * @param h 创建的材质的高
         */
        public abstract ATexture create(int w, int h);

        /**
         * 通过解析一组数据来创建材质
         *
         * @param data   源数据，可能是读取自文件
         * @param offset 开始位置偏移
         * @param length 数据长度
         */
        public abstract ATexture create(byte[] data, int offset, int length);


        /**
         * 从文件创建材质
         *
         * @param f 文件
         * @return 创建的材质
         * @throws IOException 读取文件出错时抛出
         */
        public ATexture createFromFile(File f) throws IOException {
            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
            FileInputStream in = new FileInputStream(f);
            byte[] buffer = new byte[1024];
            int l;
            while ((l = in.read(buffer)) != -1) {
                byteOutput.write(buffer, 0, l);
            }
            byte[] out = byteOutput.toByteArray();
            return create(out, 0, out.length);
        }

        public abstract ATexture createFromAssets(String path) throws IOException;

    }
}
