package com.edlplan.beatmapservice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.preference.PreferenceManager;

public enum UserPrefer {
    ForOsuDroid,
    ForMalody,
    None;

    public static UserPrefer getUserDefault(Context context) {
        return valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString("user_prefer", "None"));
    }

    @SuppressLint("ApplySharedPref")
    public static void setUserDefault(Context context, UserPrefer prefer) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("user_prefer", prefer.name()).commit();
    }
}
