package com.edlplan.beatmapservice.site.sayo;

import android.annotation.SuppressLint;
import android.util.Log;
import android.webkit.URLUtil;

import com.edlplan.beatmapservice.CacheManager;
import com.edlplan.beatmapservice.Util;
import com.edlplan.beatmapservice.site.BeatmapFilterInfo;
import com.edlplan.beatmapservice.site.BeatmapListType;
import com.edlplan.beatmapservice.site.BeatmapSetInfo;
import com.edlplan.beatmapservice.site.IBeatmapListSite;
import com.edlplan.beatmapservice.site.IBeatmapSetInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class SayoBeatmapListSite implements IBeatmapListSite {

    private final Object lock = new Object();

    ArrayList<BeatmapSetInfo> loadedSets = new ArrayList<>();
    ArrayList<Integer> newLoadedSetsIDs = new ArrayList<>();
    private BeatmapFilterInfo filterInfo;

    private int pageSize = 50;

    private int preIndex = 0;

    private boolean hasEnd = false;

    private int updateDeltatime = 1000;

    private long latestUpdateTime = -1;

    @SuppressLint("DefaultLocale")
    private String makeupLoadURL() {
        if (filterInfo == null) {
            return String.format("https://api.sayobot.cn/beatmaplist?0=%d&1=%d&2=2", pageSize, preIndex);
        } else {
            if (filterInfo.getBeatmapListType() != BeatmapListType.SEARCH) {
                return String.format("https://api.sayobot.cn/beatmaplist?0=%d&1=%d&2=%d", pageSize, preIndex, filterInfo.getBeatmapListType());
            } else {
                try {
                    String url = String.format(
                            "https://api.sayobot.cn/beatmaplist?0=%d&1=%d&2=%d&3=%s&5=%d&6=%d",
                            pageSize, preIndex,
                            filterInfo.getBeatmapListType(),
                            filterInfo.getKeyWords() != null ? URLEncoder.encode(filterInfo.getKeyWords(), "UTF-8").replace("+", "%20") : "",
                            filterInfo.getModes(),
                            filterInfo.getRankedState());
                    if (filterInfo.getValueLimit() != null) {
                        url = url + "&9=\"" + filterInfo.getValueLimit().toSayoString() + ",end\"";
                    }
                    return url;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
    }

    @Override
    public void reset() {
        synchronized (lock) {
            loadedSets.clear();
            hasEnd = false;
            preIndex = 0;
        }
    }

    @Override
    public boolean hasMoreBeatmapSet() {
        return !hasEnd;
    }

    @Override
    public void tryToLoadMoreBeatmapSet() {
        synchronized (lock) {
            if (!hasMoreBeatmapSet()) {
                return;
            }
            try {
                if (latestUpdateTime == -1) {
                    latestUpdateTime = System.currentTimeMillis();
                }
                if (System.currentTimeMillis() - latestUpdateTime < updateDeltatime) {
                    Thread.sleep(latestUpdateTime + updateDeltatime - System.currentTimeMillis());
                }
                latestUpdateTime = System.currentTimeMillis();
                System.out.println("load page offset " + preIndex);
                URL url = new URL(makeupLoadURL());
                Log.i("load beatmap", "load url = " + url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                Util.modifyUserAgent(connection);
                if (connection.getResponseCode() != 200) {
                    Log.w("load beatmap", "http response not 200! " + url + " [" + connection.getResponseCode() + "]");
                    return;
                }
                String s;
                if (connection.getHeaderField("Content-Type") != null
                        && connection.getHeaderField("Content-Type").contains("charset=GB2312")) {
                    s = Util.readFullString(connection.getInputStream(), "GB2312");
                } else {
                    s = Util.readFullString(connection.getInputStream());
                }

                JSONObject body = new JSONObject(s);
                if (body.getInt("status") != 0) {
                    if (body.getInt("status") == -1) {
                        Log.w("load beatmap", "status " + body.getInt("status"));
                        //没有搜索到铺面
                        hasEnd = true;
                        return;
                    }
                    Log.w("load beatmap", "err status " + body.getInt("status"));
                    return;
                }
                int endid = body.getInt("endid");
                if (endid == 0) {
                    hasEnd = true;
                }
                JSONArray data = body.getJSONArray("data");
                newLoadedSetsIDs.clear();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject obj = data.getJSONObject(i);
                    BeatmapSetInfo setInfo = new BeatmapSetInfo();
                    setInfo.setTitle(obj.getString("title"));
                    setInfo.setArtist(obj.getString("artist"));
                    setInfo.setCreator(obj.getString("creator"));
                    setInfo.setBeatmapSetID(obj.getInt("sid"));
                    setInfo.setModes(obj.getInt("modes"));
                    setInfo.setRankedState(obj.getInt("approved"));
                    setInfo.setFavCount(obj.getInt("favourite_count"));
                    setInfo.setLastUpdate(obj.getInt("lastupdate"));
                    if (!
                            (CacheManager.get().ignoreDownloaded && //已勾选 可下载
                                    (setInfo.getFavCount() == 0
                                            ||
                                            (CacheManager.get().downloadedSongs.containsKey(String.valueOf(setInfo.getBeatmapSetID()))  //已下载
                                                    && CacheManager.get().downloadedSongs.get(String.valueOf(setInfo.getBeatmapSetID())) > setInfo.getLastUpdate()) //不可更新
                                    )
                            )
                    ) {
                        newLoadedSetsIDs.add(setInfo.getBeatmapSetID());
                        loadedSets.add(setInfo);
                    }
                }
                preIndex = endid;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public int getLoadedBeatmapSetCount() {
        return loadedSets.size();
    }

    @Override
    public ArrayList<Integer> getNewLoadedSetsIDs() {
        return newLoadedSetsIDs;
    }

    @Override
    public IBeatmapSetInfo getInfoAt(int i) {
        return loadedSets.get(i);
    }

    @Override
    public void applyFilterInfo(BeatmapFilterInfo filterInfo) {
        reset();
        this.filterInfo = filterInfo;
    }
}
