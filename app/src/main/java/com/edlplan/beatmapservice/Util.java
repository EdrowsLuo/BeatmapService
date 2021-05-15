package com.edlplan.beatmapservice;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import android.provider.DocumentsContract;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Locale;

public class Util {

    private static final HashSet<String> runningTasks = new HashSet<>();

    public static HttpURLConnection modifyUserAgent(HttpURLConnection connection) {
        connection.setRequestProperty("User-Agent", System.getProperty("http.agent")
                + String.format(Locale.getDefault(), " BeatmapService/%s", BuildConfig.VERSION_NAME));
        return connection;
    }

    public static File getCoverOutputDir() {
        return new File(Environment.getExternalStorageDirectory(), "beatmapservice/image");
    }

    public static InputStream openUrl(String url) throws IOException {
        return modifyUserAgent((HttpURLConnection) new URL(url).openConnection()).getInputStream();
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

    public static byte[] readFullByteArrayWithRetry(String url, int maxRetryCount, int waitOnRetry) throws IOException {
        int m = maxRetryCount;
        while (maxRetryCount > 0) {
            try {
                maxRetryCount--;
                URL u = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) u.openConnection();
                modifyUserAgent(connection);
                byte[] ary = readFullByteArray(connection.getInputStream());
                connection.disconnect();
                return ary;
            } catch (IOException e) {
                if (maxRetryCount <= 0) {
                    throw e;
                }
                try {
                    Thread.sleep(waitOnRetry);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                Log.w("Requests", String.format("%s %d %s", url, maxRetryCount, e.getMessage()));
            }
        }
        throw new IOException(String.format("%s %d", url, m));
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

    public static String readFullString(InputStream in, String charset) throws IOException {
        return new String(readFullByteArray(in), charset);
    }

    public static void toast(Activity activity, String txt) {
        activity.runOnUiThread(() -> Toast.makeText(activity, txt, Toast.LENGTH_SHORT).show());
    }

    public static void toast(Activity activity, int txt) {
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
        (new Thread(() -> {
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
                onLoad.run(readFullString(modifyUserAgent((HttpURLConnection) url.openConnection()).getInputStream()));
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

    public static String httpGet(String url) throws IOException {
        try {
            URL u = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            modifyUserAgent(connection);
            String r = readFullString(connection.getInputStream());
            connection.disconnect();
            return r;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
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

    public static String md5(String s) {
        MessageDigest m;
        try {
            byte[] all = s.getBytes();
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

    public static String getNameFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(0, dotPosition);
        }
        return "";
    }


    private static String getTypeForFile(DocumentFile file) {
        if (file.isDirectory()) {
            return DocumentsContract.Document.MIME_TYPE_DIR;
        } else {
            return getTypeForName(file.getName());
        }
    }

    public static String getTypeForName(String name) {
        final int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = name.substring(lastDot + 1).toLowerCase();
            final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
        }

        return "application/octet-stream";
    }


    /**
     * @return A string suitable for display in bytes, kilobytes or megabytes
     * depending on its size.
     */


    public static boolean moveDocument(Context context, DocumentFile fileFrom, DocumentFile fileTo) {

        if (fileTo.isDirectory() /*&& fileTo.canWrite()*/) {
            if (fileFrom.isFile()) {
                return copyDocument(context, fileFrom, fileTo);
            } else if (fileFrom.isDirectory()) {
                DocumentFile[] filesInDir = fileFrom.listFiles();
                DocumentFile filesToDir = fileTo.findFile(fileFrom.getName());
                if (filesToDir == null) {
                    filesToDir = fileTo.createDirectory(fileFrom.getName());
                    if (!filesToDir.exists()) {
                        return false;
                    }
                }
                for (int i = 0; i < filesInDir.length; i++) {
                    moveDocument(context, filesInDir[i], filesToDir);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean copyDocument(Context context, DocumentFile file, DocumentFile dest) {
        if (!file.exists() || file.isDirectory()) {
            Log.v("FileUtils", "copyDocument: file not exist or is directory, " + file);
            return false;
        }
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        byte[] data = new byte[2048];
        int read = 0;
        try {
            if (!dest.exists()) {
                dest = dest.getParentFile().createDirectory(dest.getName());
                if (!dest.exists()) {
                    return false;
                }
            }

            String mimeType = getTypeForFile(file);
            String displayName = getNameFromFilename(file.getName());
            DocumentFile destFile = dest.createFile(mimeType, displayName);

            int n = 0;
            while (destFile == null && n++ < 32) {
                String destName = displayName + " (" + n + ")";
                destFile = dest.createFile(mimeType, destName);
            }

            if (destFile == null) {
                return false;
            }

            bos = new BufferedOutputStream(getOutputStream(context, destFile));
            bis = new BufferedInputStream(getInputStream(context, file));
            while ((read = bis.read(data, 0, 2048)) != -1) {
                bos.write(data, 0, read);
            }
            return true;
        } catch (FileNotFoundException e) {
            Log.e("FileUtils", "copyDocument: file not found, " + file);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("FileUtils", "copyDocument: " + e.toString());
        }

        return false;
    }


    public static OutputStream getOutputStream(Context context, DocumentFile documentFile) throws FileNotFoundException {
        return context.getContentResolver().openOutputStream(documentFile.getUri());
    }

    public static InputStream getInputStream(Context context, DocumentFile documentFile) throws FileNotFoundException {
        return context.getContentResolver().openInputStream(documentFile.getUri());
    }
}

