package com.edlplan.beatmapservice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.edlplan.beatmapservice.util.SoftObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoverPool {

    private static HashMap<Integer, SoftObject<Bitmap>> bitmaps = new HashMap<>();
    private static final HashMap<Integer, Runnable> loadingBitmaps = new HashMap<>();
    private static final List<Integer> sidList = new ArrayList<>();


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
                    sidList.add(sid);
                    if (sidList.size() > 100) {
                        bitmaps.remove(sidList.get(0));
                        sidList.remove(0);
                    }
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
                    sidList.add(sid);
                    if (sidList.size() > 100) {
                        bitmaps.remove(sidList.get(0));
                        sidList.remove(0);
                    }
                }
                if (loadingBitmaps.containsKey(sid) && loadingBitmaps.get(sid) != null) {
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
