package com.edlplan.beatmapservice.download;

import android.content.Context;
import android.webkit.URLUtil;

import androidx.documentfile.provider.DocumentFile;

import com.edlplan.beatmapservice.BuildConfig;
import com.edlplan.beatmapservice.Util;
import com.edlplan.beatmapservice.Zip;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLHandshakeException;

public class Downloader {
    public static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);


    public static final String[] ESCAPE_CHARACTER_LIST = {"\\", "/", ":", "*", "?", "\"", "<", ">", "|"};
    public DocumentFile pickedDir;
    public Context context;

    private URL url;

    private File targetDirectory;

    private Callback callback;

    private Util.RunnableWithParam<File> handleDownloadFile;

    private int autoRetryCount = 0;

    private int autoRetryMax = 3;

    private int autoRetryInterval = 5000;

    private String filenameOverride = null;

    public Downloader() {

    }

    public void setFilenameOverride(String filenameOverride) {
        this.filenameOverride = filenameOverride;
    }

    public void setHandleDownloadFile(Util.RunnableWithParam<File> handleDownloadFile) {
        this.handleDownloadFile = handleDownloadFile;
    }

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private void downloadURL(URL url) {
        File target = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            Util.modifyUserAgent(connection);
            if (BuildConfig.DEBUG) {
                System.out.println(url);
                System.out.println(System.getProperty("http.agent"));
                System.out.println(connection.getResponseCode());
                System.out.println(connection.getHeaderFields());
            }

            if (connection.getResponseCode() == 302) {
                System.out.println("redirect to " + connection.getHeaderField("location"));
                downloadURL(new URL(connection.getHeaderField("location")));
                return;
            }

            int size = Integer.parseInt(connection.getHeaderField("Content-Length"));
            String name = filenameOverride == null ? URLDecoder.decode(
                    URLUtil.guessFileName(url.toString(), connection.getHeaderField("Content-Disposition"), null),
                    "UTF-8"
            ) : filenameOverride;

            System.out.println("guess file name " + name);
            name = escapeFilename(name);
            System.out.println("new file name " + name);

            /*if (connection.getHeaderField("Content-Disposition") != null) {
                String[] spl = connection.getHeaderField("Content-Disposition").split(";");
                name = getPair(spl[0].trim())[1];
                System.out.println("use header file name " + name);
            } else {
                name = url.getFile();
                name = name.substring(name.lastIndexOf('/') + 1);
                name = URLUtil.guessFileName(name, "UTF-8");
                System.out.println("follow url file name " + name);
            }*/

            int down = 0;
            byte[] buffer = new byte[512];
            int l;
            int callbackRate = 0;
            InputStream inputStream = connection.getInputStream();
            if (this.pickedDir == null) {

                if (!targetDirectory.exists()) {
                    targetDirectory.mkdirs();
                }
                target = new File(targetDirectory, name + ".tmp");
                if (!target.exists()) {
                    target.createNewFile();
                }
                FileOutputStream outputStream = new FileOutputStream(target);

                while ((l = inputStream.read(buffer)) != -1) {
                    callbackRate++;
                    down += l;
                    if (callbackRate % 20 == 0) {
                        callback.onProgress(down, size);
                    }
                    outputStream.write(buffer, 0, l);
                }

                outputStream.close();
                connection.disconnect();
                callback.onComplete();

                File end = new File(targetDirectory, name);
                target.renameTo(end);
                target = end;

                if (handleDownloadFile != null) {
                    handleDownloadFile.run(target);
                }

            } else {
                List<String> path = new ArrayList<>();
                File tempDir = targetDirectory;
                String dirName;
                while (tempDir != null) {
                    dirName = tempDir.getName();
                    path.add(dirName);
                    tempDir = tempDir.getParentFile();
                }
                DocumentFile dir = pickedDir;

                for (int i = path.size() - 3; i >= 0; i -= 1) {
                    dir = pickedDir.findFile(path.get(i));
                }
                assert dir != null;

                File cacheDir = context.getApplicationContext().getExternalCacheDir();

                target = new File(cacheDir, name);
                if (!target.exists()) {
                    target.createNewFile();
                }


                FileOutputStream outputStream = new FileOutputStream(target);
                while ((l = inputStream.read(buffer)) != -1) {
                    callbackRate++;
                    down += l;
                    if (callbackRate % 20 == 0) {
                        callback.onProgress(down, size);
                    }
                    outputStream.write(buffer, 0, l);
                }

                outputStream.close();
                connection.disconnect();
                callback.onComplete();
                DocumentFile finalDir = dir;
                File finalTarget = target;
                Zip.fixedThreadPool.execute(() -> {
                    try {
                        Zip.unzipDocumentFile(finalDir, finalTarget, context, targetDirectory);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                // use param1 and param2 here

            }
        } catch (SSLHandshakeException e) {
            e.printStackTrace();
            if (target != null) {
                target.delete();
            }
            callback.onError(e);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (target != null) {
                target.delete();
            }
            callback.onError(e);
        } catch (Exception e) {
            e.printStackTrace();
            if (autoRetryCount < autoRetryMax & !e.getMessage().contains("Permission")) {
                autoRetryCount++;
                try {
                    Thread.sleep(autoRetryInterval);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                downloadURL(url);
            } else {
                if (target != null) {
                    target.delete();
                }
                callback.onError(e);
            }

        }
    }


    public String escapeFilename(String old) {
        for (String c : ESCAPE_CHARACTER_LIST) {
            if (old.contains(c)) {
                old = old.replace(c, "_");
            }
        }
        return old;
    }


    public void start() {
        fixedThreadPool.execute(
                () -> downloadURL(url));
    }


    public interface Callback {

        void onProgress(int down, int total);

        void onError(Throwable e);

        void onComplete();

    }

    public static class CallbackContainer implements Callback {

        private final List<Callback> callbacks = new ArrayList<>();

        private int downloadByte;

        private int totalByte;

        private boolean completed = false;

        private boolean err = false;

        public int getDownloadByte() {
            return downloadByte;
        }

        public int getTotalByte() {
            return totalByte;
        }

        public double getProgress() {
            return totalByte == 0 ? 0 : (downloadByte / (double) totalByte);
        }

        public boolean isCompleted() {
            return completed;
        }

        public boolean isErr() {
            return err;
        }

        public void setCallback(Callback callback) {
            synchronized (callbacks) {
                if (callback == null) {
                    return;
                }
                if (!callbacks.contains(callback)) {
                    callbacks.add(callback);
                }
            }
        }

        public void deleteCallback(Callback callback) {
            if (callback == null) {
                return;
            }
            synchronized (callbacks) {
                callbacks.remove(callback);
            }
        }

        @Override
        public void onProgress(int down, int total) {
            downloadByte = down;
            totalByte = total;
            synchronized (callbacks) {
                for (Callback callback : callbacks) {
                    callback.onProgress(down, total);
                }
            }
        }

        @Override
        public void onError(Throwable e) {
            err = true;
            synchronized (callbacks) {
                for (Callback callback : callbacks) {
                    callback.onError(e);
                }
            }
        }

        @Override
        public void onComplete() {
            completed = true;
            synchronized (callbacks) {
                for (Callback callback : callbacks) {
                    callback.onComplete();
                }
            }
        }
    }

    public static String[] getPair(String res) throws UnsupportedEncodingException {
        String[] sp = res.split("=");
        if (sp[1].startsWith("\"")) {
            sp[1] = URLDecoder.decode(sp[1], "UTF-8");
            sp[1] = sp[1].substring(1, sp[1].length() - 1);
        }
        return sp;
    }

}
