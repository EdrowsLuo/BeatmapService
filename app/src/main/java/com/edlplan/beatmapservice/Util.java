package com.edlplan.beatmapservice;

import android.app.Activity;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;

public class Util {

    private static final HashSet<String> runningTasks = new HashSet<>();

    public static File getCoverOutputDir() {
        return new File(Environment.getExternalStorageDirectory(), "beatmapservice/image");
    }

    public static InputStream openUrl(String url) throws IOException {
        return new URL(url).openConnection().getInputStream();
    }

    public static String timeToString(int s) {
        return (s / 60) + ":" + ((s % 60 < 10) ? ("0" + s % 60) : (s % 60));
    }

    public static void debug(String info) {
        if (BuildConfig.DEBUG) {
            System.out.println("DEBUG:: " + info);
        }
    }

    public static int round(double d) {
        return (int) Math.round(d);
    }

    public static double toDouble(Number number) {
        if (number == null) {
            return 0;
        } else if (number instanceof Double) {
            return (Double) number;
        } else if (number instanceof Integer) {
            return (Integer) number;
        } else if (number instanceof Long) {
            return (Long) number;
        } else if (number instanceof Float) {
            return (Float) number;
        } else if (number instanceof Short) {
            return (Short) number;
        } else if (number instanceof Byte) {
            return (Byte) number;
        } else {
            return 0;
        }
    }

    public static void flow(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[2048];
        int l;
        while ((l = in.read(buf)) != -1) {
            out.write(buf, 0, l);
        }
    }

    public static void flowAndClose(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[2048];
        int l;
        while ((l = in.read(buf)) != -1) {
            out.write(buf, 0, l);
        }
        in.close();
        out.close();
    }

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

    public static String readFullString(InputStream in,String charset) throws IOException {
        return new String(readFullByteArray(in), charset);
    }

    public static void toast(Activity activity, String txt) {
        activity.runOnUiThread(() -> Toast.makeText(activity, txt, Toast.LENGTH_SHORT).show());
    }

    public static void asyncCall(Runnable runnable) {
        (new Thread(runnable)).start();
    }

    public static boolean isTaskRunning(String key) {
        synchronized (runningTasks) {
            return runningTasks.contains(key);
        }
    }

    public static void asyncCall(String key, Runnable runnable) {
        synchronized (runningTasks) {
            if (runningTasks.contains(key)) {
                throw new RuntimeException(key + " is already running");
            }
            runningTasks.add(key);
        }
        (new Thread(()->{
            try {
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                synchronized (runningTasks) {
                    runningTasks.remove(key);
                }
            }
        })).start();
    }

    public static void asyncLoadString(String urls, RunnableWithParam<String> onLoad, @Nullable RunnableWithParam<Throwable> onErr) {
        asyncCall(() -> {
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

    public static boolean fileCopyTo(File src, File dst) throws IOException {
        if ((!src.exists()) || (dst.exists() && dst.isFile())) {
            return false;
        }
        if (!dst.exists()) {
            dst.mkdirs();
        }
        File out = new File(dst, src.getName());
        if (!out.exists()) out.createNewFile();
        flowAndClose(new FileInputStream(src), new FileOutputStream(out));
        return true;
    }

    public interface RunnableWithParam<T> {
        void run(T t);
    }
}
