package com.edlplan.beatmapservice;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {

    public static byte[] readFullByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        byte[] buf = new byte[256];
        int l;
        while ((l = in.read(buf)) != -1) {
            o.write(buf, 0, l);
        }
        return o.toByteArray();
    }

    public static String readFullString(InputStream in) throws IOException {
        return new String(readFullByteArray(in), "UTF-8");
    }

    public static void toast(Activity activity, String txt) {
        activity.runOnUiThread(() -> Toast.makeText(activity, txt, Toast.LENGTH_SHORT).show());
    }

    public static void asynCall(Runnable runnable) {
        (new Thread(runnable)).start();
    }

    public static void asynLoadString(String urls, RunnableWithParam<String> onLoad, @Nullable RunnableWithParam<Throwable> onErr) {
        asynCall(() -> {
            try {
                URL url = new URL(urls);
                onLoad.run(readFullString(url.openConnection().getInputStream()));
            } catch (Exception e) {
                if (onErr != null) {
                    onErr.run(e);
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void checkFile(File file) throws IOException {
        File p = file.getParentFile();
        if (!p.exists()) {
            p.mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    public static String md5(File file) throws IOException {
        MessageDigest m;
        try {
            byte[] all = readFullByteArray(new FileInputStream(file));
            m = MessageDigest.getInstance("MD5");
            m.update(all, 0, all.length);
            String md5 = new BigInteger(1, m.digest()).toString(16);
            m.reset();
            return md5;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public interface RunnableWithParam<T> {
        void run(T t);
    }
}
