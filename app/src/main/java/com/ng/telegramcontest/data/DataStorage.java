package com.ng.telegramcontest.data;

import android.content.Context;

public class DataStorage {
    private final Parser mParser = new Parser();
    private Prefs mPrefs;
    private Charts mCharts;

    public void initialize(Context context) {
        mCharts = mParser.getCharts(context);
        mPrefs = new Prefs(context);
    }

    public Charts getCharts() {
        return mCharts;
    }

    public boolean isNightTheme() {
        return mPrefs.isNightTheme();
    }

    public void setNightMode(boolean value) {
        mPrefs.setNightMode(value);
    }
}
