package com.edlplan.beatmapservice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.system.Os;
import android.util.SparseArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class CoverPool {

    private static HashMap<Integer, Bitmap> bitmaps = new HashMap<>();

    public static Bitmap getCoverBitmap(int sid) {
        return bitmaps.get(sid);
    }

    public static void loadCoverBitmap(Context context, int sid, Runnable onloadDone) {
        (new Thread(() -> {
            try {
                File cache = new File(context.getCacheDir(), sid + "_cover.jpg");
                if (cache.exists()) {
                    bitmaps.put(sid, BitmapFactory.decodeFile(cache.getAbsolutePath()));
                    onloadDone.run();
                    return;
                }
                URL url = new URL(String.format("https://cdn.sayobot.cn:25225/beatmaps/%d/covers/cover.jpg?0", sid));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                if (connection.getResponseCode() == 200) {
                    byte[] bytes = Util.readFullByteArray(connection.getInputStream());
                    connection.disconnect();
                    cache.createNewFile();
                    OutputStream outputStream = new FileOutputStream(cache);
                    outputStream.write(bytes);
                    outputStream.close();
                    bitmaps.put(sid, BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                    onloadDone.run();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        })).start();
    }

}
