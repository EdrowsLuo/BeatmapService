package com.edlplan.beatmapservice.site.sayo;

import com.edlplan.beatmapservice.Util;
import com.edlplan.beatmapservice.site.BeatmapInfo;
import com.edlplan.beatmapservice.site.IBeatmapDetailSite;
import com.edlplan.beatmapservice.site.IBeatmapSetInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SayoBeatmapDetailSite implements IBeatmapDetailSite {

    @Override
    public List<BeatmapInfo> getBeatmapInfo(IBeatmapSetInfo setInfo) {
        try {
            URL url = new URL("https://api.sayobot.cn/v2/beatmapinfo?0=" + setInfo.getBeatmapSetID());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            JSONObject obj = new JSONObject(Util.readFullString(connection.getInputStream()));
            if (obj.getInt("status") != 0) {
                return null;
            }
            //System.out.println(obj.toString(2));
            obj = obj.getJSONObject("data");
            JSONArray array = obj.getJSONArray("bid_data");
            List<BeatmapInfo> list = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject bd = array.getJSONObject(i);
                BeatmapInfo info = new BeatmapInfo();
                info.setBid(bd.getInt("bid"));
                info.setMode(bd.getInt("mode"));
                info.setVersion(bd.getString("version"));
                info.setLength(bd.getInt("length"));
                info.setCircleSize(bd.getDouble("CS"));
                info.setApproachRate(bd.getDouble("AR"));
                info.setOverallDifficulty(bd.getDouble("OD"));
                info.setHP(bd.getDouble("HP"));
                info.setStar(bd.getDouble("star"));
                info.setAim(bd.getDouble("aim"));
                info.setSpeed(bd.getDouble("speed"));
                info.setPP(bd.getDouble("pp"));
                info.setCircleCount(bd.getInt("circles"));
                info.setSliderCount(bd.getInt("sliders"));
                info.setSpinnerCount(bd.getInt("spinners"));
                info.setMaxCombo(bd.getInt("maxcombo"));
                info.setPlaycount(bd.getInt("playcount"));
                info.setPasscount(bd.getInt("passcount"));
                info.setImgMD5(bd.getString("img"));
                info.setStrainAim(divideToStrain(bd.getString("strain_aim")));
                info.setStrainSpeed(divideToStrain(bd.getString("strain_speed")));
                list.add(info);
            }
            return list;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int[] divideToStrain(CharSequence s) {
        int[] l = new int[s.length()];
        for (int i = 0; i < l.length; i++) {
            l[i] = s.charAt(i) - '0';
        }
        return l;
    }

}
