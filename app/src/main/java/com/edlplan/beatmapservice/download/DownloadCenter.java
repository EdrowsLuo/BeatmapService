package com.edlplan.beatmapservice.download;

import android.os.Environment;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.edlplan.beatmapservice.MainActivity;
import com.edlplan.beatmapservice.R;
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

    public static void download(int id, Downloader.Callback callback) {
        new Thread(()->{
            Downloader downloader = new Downloader();
            downloader.setTargetDirectory(new File(Environment.getExternalStorageDirectory(), "osu!droid/Songs"));
            try {
                URL startURL;
                //if (((RadioGroup) findViewById(R.id.downloadSite)).getCheckedRadioButtonId() == R.id.radioButtonSayo) {
                    startURL = new URL("https://txy1.sayobot.cn/download/osz/" + URLEncoder.encode("" + id, "UTF-8"));
                //} else {
                //    startURL = new URL("https://bloodcat.com/osu/_data/beatmaps/" + Integer.parseInt(id) + ".osz");
                //}

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

}
