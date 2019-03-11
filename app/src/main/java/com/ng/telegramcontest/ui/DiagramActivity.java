package com.ng.telegramcontest.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.ng.telegramcontest.App;
import com.ng.telegramcontest.R;
import com.ng.telegramcontest.data.ChartData;
import com.ng.telegramcontest.data.DataStorage;
import com.ng.telegramcontest.ui.recycler.ChartNamesAdapter;

public class DiagramActivity extends AppCompatActivity implements ChartNamesAdapter.SelectChartListener {

    private final static String CHART_NUMBER = "CHART_NUMBER";

    private DataStorage mDataStorage;
    private ChartData mChartData;
    private int chartNumber = 0;
    private boolean[] selectedCharts;

    private DateSelectorView mDateSelectorView;
    private RecyclerView mRecyclerView;
    private ChartNamesAdapter mChartNamesAdapter;

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
        setContentView(R.layout.activity_diagram);
        setActionBar();
        setViews();

        chartNumber = getIntent().getExtras().getInt(CHART_NUMBER);

        mDataStorage = ((App) getApplicationContext()).getDataStorage();
        mChartData = mDataStorage.getCharts().getChartsData()[chartNumber];
        selectedCharts = new boolean[mChartData.getDataSets().length];
        for (int i = 0; i < selectedCharts.length; i++) {
            selectedCharts[i] = true;
        }

        initViewData();
    }

    private void setViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.chars_name_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mChartNamesAdapter = new ChartNamesAdapter(this, this);
        mRecyclerView.setAdapter(mChartNamesAdapter);

        mDateSelectorView = (DateSelectorView) findViewById(R.id.date_selector_view);
    }

    private void initViewData() {
        mChartNamesAdapter.setNames(mChartData.getDataSets(), selectedCharts);
        mDateSelectorView.initData(mChartData, selectedCharts);
    }

    private void setActionBar() {
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        setTitle(R.string.statistics);
    }

    @Override
    public void onChartSelect(int chartIndex, boolean isSelect) {
        selectedCharts[chartIndex] = isSelect;
        changeSelect();
    }

    private void changeSelect() {
        mDateSelectorView.changeSelect(selectedCharts);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.statistics_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_change_theme) {
            Log.d("TAG", "Click on change theme");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
