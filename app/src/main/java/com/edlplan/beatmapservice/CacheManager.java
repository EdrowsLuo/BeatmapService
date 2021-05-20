package com.edlplan.beatmapservice;

import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;


import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CacheManager {
    public Map<String, Integer> downloadedSongs = new HashMap<>();
    private static final CacheManager cacheManager = new CacheManager();

    public static CacheManager get() {
        return cacheManager;
    }

    public void update(Context context, Integer sid, int lastUpdate) {
        downloadedSongs.put(String.valueOf(sid), lastUpdate);
        dumpCache(context);
    }

    public void loadCache(Context context
    ) {
        File file = new File(context.getApplicationContext().getExternalCacheDir(), "Cache.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
                updateCache(context);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                downloadedSongs = objectMapper.readValue(file, downloadedSongs.getClass());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateCache(Context context) {
        String path = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("default_download_path", "default");
        if(path=="default"){
            path= Environment.getExternalStorageDirectory()+ "osu!droid/Keyword.json";
        }
        File songDir = new File(path);
        File[] songList = songDir.listFiles();
        if (songList == null) {
            return;
        }
        for (File f : songList) {
            try {
                String id = f.getName().substring(0, f.getName().indexOf(' '));
                downloadedSongs.put(String.valueOf(Integer.parseInt(id)), (int) (f.lastModified() / 1000));
            } catch (Exception ignored) {
            }
        }
        dumpCache(context);
    }

    public void dumpCache(Context context) {
        File file = new File(context.getApplicationContext().getExternalCacheDir(), "Cache.json");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(file, downloadedSongs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
