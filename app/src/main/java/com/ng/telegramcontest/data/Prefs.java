package com.ng.telegramcontest.data;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {

    private static String TELEGRAM_CONTEST_PREFS = "TELEGRAM_CONTEST_PREFS";
    private static String NIGHT_MODE_ENABLE = "NIGHT_MODE_ENABLE";
    private final SharedPreferences mShared;

    private Context mContext;

    public Prefs(Context context) {
        mContext = context;
        mShared = context.getSharedPreferences(TELEGRAM_CONTEST_PREFS, Context.MODE_PRIVATE);
    }

    public boolean isNightTheme() {
        return mShared.getBoolean(NIGHT_MODE_ENABLE, false);
    }

    public void setNightMode(boolean value) {
        mShared.edit().putBoolean(NIGHT_MODE_ENABLE, value).apply();
    }
}
