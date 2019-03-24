package com.edlplan.audiov.platform.android;

import android.content.Context;

import com.edlplan.audiov.core.AudioVCore;
import com.edlplan.audiov.core.graphics.ACanvas;
import com.edlplan.audiov.core.graphics.ATexture;

public class AndroidPlugin implements AudioVCore.PlatformGraphics {

    public static final AndroidPlugin INSTANCE = new AndroidPlugin();

    private static Context context;

    public static void initial(Context context) {
        AndroidPlugin.context = context;
    }

    public static Context getContext() {
        return context;
    }

    @Override
    public ATexture.AFactory getTextureFactory() {
        return AndroidTexture.FACTORY;
    }

    @Override
    public ACanvas.AFactory getCanvasFactory() {
        return AndroidCanvas.FACTORY;
    }
}
