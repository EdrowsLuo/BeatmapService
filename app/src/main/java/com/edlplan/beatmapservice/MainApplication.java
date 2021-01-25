package com.edlplan.beatmapservice;

import android.app.Application;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        UMConfigure.init(this,
                "600e372d6a2a470e8f889a74",
                String.format("%s[%s]", BuildConfig.FLAVOR, BuildConfig.BUILD_TYPE),
                UMConfigure.DEVICE_TYPE_PHONE, "");
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
    }
}
