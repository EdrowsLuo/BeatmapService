package com.edlplan.downloader;

import android.webkit.URLUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class Downloader {

    private URL url;

    private File targetDirectory;

    private Callback callback;

    private int autoRetryCount = 0;

    private int autoRetryMax = 5;

    public Downloader() {

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
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            System.out.println(url);
            System.out.println(connection.getResponseCode());
            System.out.println(connection.getHeaderFields());

            if (connection.getResponseCode() == 302) {
                System.out.println("redirect to " + connection.getHeaderField("location"));
                downloadURL(new URL(connection.getHeaderField("location")));
                return;
            }

            int size = Integer.parseInt(connection.getHeaderField("Content-Length"));
            String name = URLUtil.guessFileName(url.toString(), connection.getHeaderField("Content-Disposition"), null);
            name = URLDecoder.decode(name, "UTF-8");
            System.out.println("guess file name " + name);
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
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            while ((l = inputStream.read(buffer)) != -1) {
                callbackRate++;
                down += l;
                if (callbackRate % 20 == 0) {
                    callback.onProgress(down, size);
                }
                outputStream.write(buffer, 0, l);
            }
            if (!targetDirectory.exists()) {
                targetDirectory.mkdirs();
            }
            File target = new File(targetDirectory, name);
            if (!target.exists()) {
                target.createNewFile();
            }
            FileOutputStream output = new FileOutputStream(target);
            output.write(outputStream.toByteArray());
            output.close();
            connection.disconnect();
            callback.onComplete();
        } catch (Exception e) {
            e.printStackTrace();
            if (autoRetryCount < autoRetryMax) {
                autoRetryCount++;
                downloadURL(url);
            } else {
                callback.onError(e);
            }

        }
    }

    public void start() {
        (new Thread() {
            @Override
            public void run() {
                super.run();
                synchronized (url) {
                    downloadURL(url);
                }
            }
        }).start();
    }



    public interface Callback {

        void onProgress(int down, int total);

        void onError(Throwable e);

        void onComplete();

    }

    public static class CallbackContainer implements Callback {

        private Callback callback;

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
            this.callback = callback;
        }

        public Callback getCallback() {
            return callback;
        }

        @Override
        public void onProgress(int down, int total) {
            downloadByte = down;
            totalByte = total;
            if (callback!=null) callback.onProgress(down, total);
        }

        @Override
        public void onError(Throwable e) {
            err = true;
            if (callback!=null) callback.onError(e);
        }

        @Override
        public void onComplete() {
            completed = true;
            if (callback!=null) callback.onComplete();
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
