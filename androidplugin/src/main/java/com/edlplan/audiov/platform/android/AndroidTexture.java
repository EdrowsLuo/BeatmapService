package com.edlplan.audiov.platform.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.edlplan.audiov.core.graphics.ATexture;

import java.io.IOException;

public class AndroidTexture extends ATexture {

    public static AndroidTextureFactory FACTORY = new AndroidTextureFactory();

    private Bitmap bitmap;

    public AndroidTexture(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    @Override
    public int getWidth() {
        return bitmap.getWidth();
    }

    @Override
    public int getHeight() {
        return bitmap.getHeight();
    }

    public static class AndroidTextureFactory extends AFactory {

        @Override
        public ATexture createFromAssets(String path) throws IOException {
            Bitmap bmp = BitmapFactory.decodeStream(AndroidPlugin.getContext().getAssets().open(path));
            return new AndroidTexture(bmp);
        }

        @Override
        public ATexture create(int w, int h) {
            return new AndroidTexture(Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888));
        }

        @Override
        public ATexture create(byte[] data, int offset, int length) {
            return new AndroidTexture(BitmapFactory.decodeByteArray(data, offset, length));
        }
    }
}
