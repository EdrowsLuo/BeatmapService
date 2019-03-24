package com.edlplan.beatmapservice;


import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 辅助缓存数据的类
 */
public class CacheHelper {

    private static Context context;

    private static File cacheDir;

    public static void initial(Context context) {
        CacheHelper.context = context.getApplicationContext();
        cacheDir = CacheHelper.context.getCacheDir();
    }

    public static void clearCache(long since) {
        clearCache(cacheDir, since);
    }

    private static void clearCache(File f, long since) {
        if (f.isFile()) {
            if (f.lastModified() < since) {
                f.delete();
            }
        } else {
            clearCache(f, since);
        }
    }

    public static void setCache(String name, byte[] bytes) throws IOException {
        File cacheFile = getCacheFile(name);
        Util.checkFile(cacheFile);
        FileOutputStream outputStream = new FileOutputStream(cacheFile);
        outputStream.write(bytes);
        outputStream.close();
    }

    public static byte[] getCache(String name) {
        try {
            return Util.readFullByteArray(new FileInputStream(getCacheFile(name)));
        } catch (IOException e) {
            return null;
        }
    }

    public static File getCacheFile(String name) {
        return new File(cacheDir, name);
    }

}
