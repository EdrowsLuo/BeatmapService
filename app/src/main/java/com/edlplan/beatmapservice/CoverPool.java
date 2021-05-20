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
    private static final HashMap<Integer, Runnable> loadingBitmaps = new HashMap<>();

    public static Bitmap getCoverBitmap(int sid) {
        if (!bitmaps.containsKey(sid)) {
            return null;
        }
        return bitmaps.get(sid).get();
    }

    public static void preloadCoverBitmap(Context context, int sid) {
        if (loadingBitmaps.containsKey(sid) || bitmaps.containsKey(sid)) {
            return;
        }
        (new Thread(() -> {
            try {
                loadingBitmaps.put(sid, null);
                File cache = new File(context.getCacheDir(), sid + "_cover.jpg");
                String cachePath = cache.getAbsolutePath();
                if (cache.exists()) {
                    bitmaps.put(sid, SoftObject.create(() -> BitmapFactory.decodeFile(cachePath)));
                    return;
                }
                URL url = new URL(String.format("https://a.sayobot.cn/beatmaps/%d/covers/cover.webp?0", sid));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                Util.modifyUserAgent(connection);
                if (connection.getResponseCode() == 200) {
                    byte[] bytes = Util.readFullByteArray(connection.getInputStream());
                    connection.disconnect();
                    cache.createNewFile();
                    OutputStream outputStream = new FileOutputStream(cache);
                    outputStream.write(bytes);
                    outputStream.close();
                    bitmaps.put(sid, SoftObject.create(() -> BitmapFactory.decodeFile(cachePath)));
                }
                if(loadingBitmaps.containsKey(sid)&&loadingBitmaps.get(sid)!=null){
                    loadingBitmaps.get(sid).run();
                }
                loadingBitmaps.remove(sid);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        })).start();
    }

    public static void loadCoverBitmap(Context context, int sid, Runnable onloadDone) {
        if (loadingBitmaps.containsKey(sid)) {
            loadingBitmaps.put(sid, onloadDone);
            return;
        }
        (new Thread(() -> {
            try {
                File cache = new File(context.getCacheDir(), sid + "_cover.jpg");
                String cachePath = cache.getAbsolutePath();
                if (cache.exists()) {
                    bitmaps.put(sid, SoftObject.create(() -> BitmapFactory.decodeFile(cachePath)));
                    onloadDone.run();
                    return;
                }
                URL url = new URL(String.format("https://a.sayobot.cn/beatmaps/%d/covers/cover.webp?0", sid));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                Util.modifyUserAgent(connection);
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
