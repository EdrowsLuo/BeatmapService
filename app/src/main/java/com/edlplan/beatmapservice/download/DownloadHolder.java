package com.edlplan.beatmapservice.download;

import com.edlplan.downloader.Downloader;

import java.util.HashMap;

public class DownloadHolder {

    public static DownloadHolder instance = new DownloadHolder();

    public static DownloadHolder get() {
        return instance;
    }

    private static HashMap<Integer, Downloader.CallbackContainer> downloadCallbacks = new HashMap<>();

    public void initialCallback(int sid, Downloader.CallbackContainer callback) {
        downloadCallbacks.put(sid, callback);
    }

    public Downloader.CallbackContainer getCallbackContainer(int sid) {
        return downloadCallbacks.get(sid);
    }
}
