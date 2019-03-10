package com.ng.telegramcontest.data;

import android.content.Context;

public class DataStorage {
    private final Parser mParser = new Parser();
    private Charts mCharts;

    public void initialize(Context context) {
        mCharts = mParser.getCharts(context);
    }

    public Charts getCharts() {
        return mCharts;
    }
}
