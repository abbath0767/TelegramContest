package com.ng.telegramcontest.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.ng.telegramcontest.App;
import com.ng.telegramcontest.data.ChartData;
import com.ng.telegramcontest.data.DataStorage;

public class DiagramActivity extends AppCompatActivity {

    private final static String CHART_NUMBER = "CHART_NUMBER";

    private DataStorage mDataStorage;
    private ChartData mChartData;
    private int chartNumber = 0;

    public static void startActivity(Context context, int chartNumber) {
        Intent intent = new Intent(context, DiagramActivity.class);
        Bundle data = new Bundle();
        data.putInt(CHART_NUMBER, chartNumber);
        intent.putExtras(data);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView();

        chartNumber = getIntent().getExtras().getInt(CHART_NUMBER);

        mDataStorage = ((App)getApplicationContext()).getDataStorage();
        mChartData = mDataStorage.getCharts().getChartsData()[chartNumber];
    }
}
