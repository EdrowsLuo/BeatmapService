package com.edlplan.beatmapservice.site.sayo;

import android.annotation.SuppressLint;
import android.util.Log;

import com.edlplan.beatmapservice.Util;
import com.edlplan.beatmapservice.site.BeatmapFilterInfo;
import com.edlplan.beatmapservice.site.BeatmapSetInfo;
import com.edlplan.beatmapservice.site.IBeatmapInfoSite;
import com.edlplan.beatmapservice.site.IBeatmapSetInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class SayoBeatmapInfoSite implements IBeatmapInfoSite {

    ArrayList<BeatmapSetInfo> loadedSets = new ArrayList<>();

    private int pageSize =  20;

    private int page = -1;

    private boolean hasEnd = false;

    @SuppressLint("DefaultLocale")
    private String makeupLoadURL() {
        return String.format("https://api.sayobot.cn/beatmaplist?0=%d&1=%d&2=2", pageSize, page * pageSize);
    }

    @Override
    public void reset() {
        loadedSets.clear();
        hasEnd = false;
        page = -1;
    }

    @Override
    public boolean hasMoreBeatmapSet() {
        return !hasEnd;
    }

    @Override
    public void tryToLoadMoreBeatmapSet() {
        if (!hasMoreBeatmapSet()) {
            return;
        }
        page++;
        try {
            URL url = new URL(makeupLoadURL());
            Log.i("load beatmap", "load url = " + url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (connection.getResponseCode() != 200) {
                Log.w("load beatmap", "http response not 200! " + url + " [" + connection.getResponseCode() + "]");
                return;
            }
            JSONObject body = new JSONObject(Util.readFullString(connection.getInputStream()));
            Log.i("load beatmap", "body = " + body.toString(4));
            if (body.getInt("status") != 0) {
                Log.w("load beatmap", "err status " + body.getInt("status"));
                return;
            }
            int endid = body.getInt("endid");
            if (endid < pageSize * page) {
                //超额
                //hasEnd = true;
                return;
            } else if (endid < (pageSize + 1) * page) {
                //请求的结果数不到一页，表示已经结束
                //hasEnd = true;
            }
            JSONArray data = body.getJSONArray("data");
            for (int i = 0; i < data.length();i++) {
                JSONObject obj = data.getJSONObject(i);
                BeatmapSetInfo setInfo = new BeatmapSetInfo();
                setInfo.setTitle(obj.getString("title"));
                setInfo.setArtist(obj.getString("artist"));
                setInfo.setCreator(obj.getString("creator"));
                setInfo.setBeatmapSetID(obj.getInt("sid"));
                setInfo.setModes(obj.getInt("modes"));
                loadedSets.add(setInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            page--;
        }
    }

    @Override
    public int getLoadedBeatmapSetCount() {
        return loadedSets.size();
    }

    @Override
    public IBeatmapSetInfo getInfoAt(int i) {
        return loadedSets.get(i);
    }

    @Override
    public void applyFilterInfo(BeatmapFilterInfo filterInfo) {

    }
}
