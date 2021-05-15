package com.edlplan.beatmapservice.download;

import java.util.HashMap;

public class DownloadHolder {

    public static DownloadHolder instance = new DownloadHolder();

    public static DownloadHolder get() {
        return instance;
    }

    private static HashMap<Integer, Downloader.CallbackContainer> downloadCallbacks = new HashMap<>();

    public void initialCallback(int sid, Downloader.CallbackContainer callback) {
        if (callback == null) {
            downloadCallbacks.remove(sid);
        }
        downloadCallbacks.put(sid, callback);
    }

    public Downloader.CallbackContainer getCallbackContainer(int sid) {
        return downloadCallbacks.get(sid);
    }

    public boolean isRunning(int sid) {
        Downloader.CallbackContainer callback = downloadCallbacks.get(sid);
        return callback != null && !callback.isCompleted() && !callback.isErr();
    }

    public int currentRunningNum() {
        int count = 0;
        for (Integer sid : downloadCallbacks.keySet()) {
            if (isRunning(sid)) {
                count += 1;
            }
        }
        return count;
    }
}
