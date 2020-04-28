package com.edlplan.beatmapservice.download;

import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.edlplan.beatmapservice.Zip;
import com.edlplan.beatmapservice.site.GameModes;
import com.edlplan.beatmapservice.site.IBeatmapSetInfo;
import com.edlplan.beatmapservice.site.sayo.SayoServerSelector;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

public class DownloadCenter {

    public static void download(Context context, int id, File dir, Downloader.Callback callback, boolean unzip, IBeatmapSetInfo info) {
        new Thread(() -> {
            Downloader downloader = new Downloader();
            downloader.setTargetDirectory(dir);
            try {
                URL startURL;

                String server = SayoServerSelector.getInstance().getSelected().server;
                startURL = new URL("https://txy1.sayobot.cn/beatmaps/download/full/" + URLEncoder.encode("" + id, "UTF-8") + "?server=" + server);

                downloader.setFilenameOverride(String.format(Locale.getDefault(), "%d %s - %s.osz", id, info.getArtist(), info.getTitle()));
                downloader.setUrl(startURL);
                downloader.setCallback(callback);
                if (unzip) {
                    downloader.setHandleDownloadFile(file -> {
                        try {
                            Zip.unzip(file);
                            file.delete();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
                downloader.start();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void download(Context context, IBeatmapSetInfo info, Downloader.Callback callback) {
        download(context, info.getBeatmapSetID(), directoryToFile(findDirectory(context, info)), callback,
                PreferenceManager.getDefaultSharedPreferences(context).getBoolean("auto_unzip", false), info);
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

    public static File getDroidSongsDirectory(Context context) {
        return directoryToFile(getDownloadDirectory(context));
    }

}
