package com.edlplan.beatmapservice.download;

import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.edlplan.beatmapservice.site.GameModes;
import com.edlplan.beatmapservice.site.IBeatmapSetInfo;
import com.edlplan.downloader.Downloader;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class DownloadCenter {

    public static DownloadEntryInfo requestNewDownloadEntry() {
        return new DownloadEntryInfo();
    }

    public static void download(int id, File dir, Downloader.Callback callback) {
        new Thread(()->{
            Downloader downloader = new Downloader();
            downloader.setTargetDirectory(dir);
            try {
                URL startURL;
                startURL = new URL("https://txy1.sayobot.cn/download/osz/" + URLEncoder.encode("" + id, "UTF-8"));
                downloader.setUrl(startURL);
                downloader.setCallback(callback);
                downloader.start();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void download(Context context, int sid, Downloader.Callback callback) {
        download(sid, directoryToFile(findDirectory(context, null)), callback);
    }

    public static void download(Context context, IBeatmapSetInfo info, Downloader.Callback callback) {
        download(info.getBeatmapSetID(), directoryToFile(findDirectory(context, info)), callback);
    }

    private static File directoryToFile(String d) {
        return d.equals("default") ? new File(Environment.getExternalStorageDirectory(), "osu!droid/Songs") : new File(d);
    }

    private static String findDirectory(Context context, @Nullable IBeatmapSetInfo info) {
        if (info != null && (info.getModes() & GameModes.STD) == 0) {
            //没有std模式的谱面
            if (!getOtherModeDirectory(context).equals("default")) {
                //设置了其余模式分流路径
                return getOtherModeDirectory(context);
            }
        }
        return getDownloadDirectory(context);
    }

    private static String getOtherModeDirectory(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("default_download_path_mania", "default");
    }

    private static String getDownloadDirectory(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("default_download_path", "default");
    }

}
