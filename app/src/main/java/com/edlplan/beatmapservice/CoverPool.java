package com.edlplan.beatmapservice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.edlplan.beatmapservice.util.SoftObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class CoverPool {

    private static HashMap<Integer, SoftObject<Bitmap>> bitmaps = new HashMap<>();

    public static Bitmap getCoverBitmap(int sid) {
        if (!bitmaps.containsKey(sid)) {
            return null;
        }
        return bitmaps.get(sid).get();
    }

    public static void loadCoverBitmap(Context context, int sid, Runnable onloadDone) {
        (new Thread(() -> {
            try {
                File cache = new File(context.getCacheDir(), sid + "_cover.jpg");
                String cachePath = cache.getAbsolutePath();
                if (cache.exists()) {
                    bitmaps.put(sid, SoftObject.create(() -> BitmapFactory.decodeFile(cachePath)));
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
                    bitmaps.put(sid, SoftObject.create(() -> BitmapFactory.decodeFile(cachePath)));
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
