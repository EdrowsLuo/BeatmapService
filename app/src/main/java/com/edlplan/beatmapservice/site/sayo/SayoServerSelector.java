package com.edlplan.beatmapservice.site.sayo;

import android.app.Activity;
import android.util.Log;

import com.edlplan.beatmapservice.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SayoServerSelector {

    private static SayoServerSelector instance;

    public static SayoServerSelector getInstance() {
        if (instance == null) {
            instance = new SayoServerSelector();
        }
        return instance;
    }

    private List<ServerInfo> serverInfoList;

    private int selectedInfo = 0;

    private void initial() throws Exception {
        serverInfoList = new ArrayList<>();
        JSONObject servers = new JSONObject(Util.httpGet("https://api.sayobot.cn/static/servers"));
        for (int i = 0; i < servers.getJSONArray("data").length(); i++) {
            JSONObject raw = servers.getJSONArray("data").getJSONObject(i);
            serverInfoList.add(new ServerInfo() {{
                server = raw.getString("server");
                server_name = raw.getString("server_name");
                server_nameU = raw.getString("server_nameU");
            }});
        }
    }

    public void asyncInitial(Util.RunnableWithParam<Boolean> onInitial) {
        Util.asyncCall(() -> {
            try {
                initial();
                onInitial.run(true);
            } catch (Exception e) {
                e.printStackTrace();
                serverInfoList = new ArrayList<>();
                serverInfoList.add(new ServerInfo() {{
                    server = "0";
                    server_name = "auto";
                    server_nameU = "自动";
                }});
                onInitial.run(false);
            }
        });
    }

    public void switchInfo(int idx) {
        if (idx < 0 || idx >= serverInfoList.size()) {
            idx = 0;
            Log.w("SayoServerSelector", "Select a invalid server");
        }
        selectedInfo = idx;
    }

    public ServerInfo getSelected() {
        if (serverInfoList.isEmpty()) {
            return null;
        } else {
            return serverInfoList.get(selectedInfo);
        }
    }

    public List<ServerInfo> getServerInfoList() {
        return serverInfoList;
    }

    public static class ServerInfo {
        public String server;
        public String server_name;
        public String server_nameU;

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ServerInfo) {
                ServerInfo o = (ServerInfo) obj;
                return o.server.equals(server) && o.server_name.equals(server_name)
                        && o.server_nameU.equals(server_nameU);
            } else {
                return false;
            }
        }
    }

}
