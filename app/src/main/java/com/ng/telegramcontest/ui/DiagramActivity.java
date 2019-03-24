package com.ng.telegramcontest.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.ng.telegramcontest.App;
import com.ng.telegramcontest.R;
import com.ng.telegramcontest.data.ChartData;
import com.ng.telegramcontest.data.DataStorage;
import com.ng.telegramcontest.ui.recycler.ChartNamesAdapter;
import com.ng.telegramcontest.ui.view.BigGraph;
import com.ng.telegramcontest.ui.view.DateSelectorView;
import com.ng.telegramcontest.ui.view.SelectWindowView;

public class DiagramActivity extends AppCompatActivity implements ChartNamesAdapter.SelectChartListener, SelectWindowView.BorderChangeListener {

    private final static String CHART_NUMBER = "CHART_NUMBER";

    private DataStorage mDataStorage;
    private ChartData mChartData;
    private int chartNumber = 0;
    private boolean[] selectedCharts;

    private DateSelectorView mDateSelectorView;
    private BigGraph mBigGraph;
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

        chartNumber = getIntent().getExtras().getInt(CHART_NUMBER);

        mDataStorage = ((App) getApplicationContext()).getDataStorage();
        mChartData = mDataStorage.getCharts().getChartsData()[chartNumber];
        selectedCharts = new boolean[mChartData.getDataSets().length];
        for (int i = 0; i < selectedCharts.length; i++) {
            selectedCharts[i] = true;
        }

        boolean isNightTheme = mDataStorage.isNightTheme();

        setActionBar(isNightTheme);
        setViews(isNightTheme);

        initViewData();
    }

    private void setViews(boolean isNightTheme) {
        mRecyclerView = (RecyclerView) findViewById(R.id.chars_name_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mChartNamesAdapter = new ChartNamesAdapter(this, this, isNightTheme);
        mRecyclerView.setAdapter(mChartNamesAdapter);

        mDateSelectorView = (DateSelectorView) findViewById(R.id.date_selector_view);
        mDateSelectorView.setIsNightMode(isNightTheme);

        mBigGraph = (BigGraph) findViewById(R.id.big_graph);
        mBigGraph.setIsNightMode(isNightTheme);

        if (isNightTheme)
            findViewById(R.id.big_graph_holder).setBackgroundColor(getResources().getColor(R.color.colorPrimaryNight));
        else
            findViewById(R.id.big_graph_holder).setBackgroundResource(0);
    }

    private void setViewsColor(boolean isNightTheme) {
        mChartNamesAdapter.changeTheme(isNightTheme);
        mDateSelectorView.setIsNightMode(isNightTheme);
        mBigGraph.setIsNightMode(isNightTheme);
        if (isNightTheme)
            findViewById(R.id.big_graph_holder).setBackgroundColor(getResources().getColor(R.color.colorPrimaryNight));
        else
            findViewById(R.id.big_graph_holder).setBackgroundResource(0);
    }

    private void initViewData() {
        mChartNamesAdapter.setNames(mChartData.getDataSets(), selectedCharts);
        mDateSelectorView.initData(mChartData, selectedCharts);
        mBigGraph.initData(mChartData, selectedCharts);
    }

    private void setActionBar(boolean isNightTheme) {
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        setTitle(R.string.statistics);

        setActionBarColor(isNightTheme);
    }

    private void setActionBarColor(boolean isNightTheme) {
        int colorResBar;
        int colorResStatusBar;
        int colorActivityBackground;
        if (isNightTheme) {
            colorResBar = R.color.colorPrimaryNight;
            colorResStatusBar = R.color.colorPrimaryDarkNight;
            colorActivityBackground = R.color.colorPrimaryDarkNight;
        } else {
            colorResBar = R.color.colorPrimary;
            colorResStatusBar = R.color.colorPrimaryDark;
            colorActivityBackground = R.color.defaultBackColor;
        }

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(colorResBar)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(colorResStatusBar));
        }
        findViewById(R.id.parent).setBackgroundColor(getResources().getColor(colorActivityBackground));
    }

    @Override
    public void onChartSelect(int chartIndex, boolean isSelect) {
        selectedCharts[chartIndex] = isSelect;
        changeSelect();
    }

    private void changeSelect() {
        mDateSelectorView.changeSelect(selectedCharts);
        mBigGraph.changeSelect(selectedCharts);
    }

    @Override
    public void onBorderChange(float fromX, float toX, int type) {
        mBigGraph.pushBorderChange(fromX, toX, type);
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
            changeTheme();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeTheme() {
        boolean nowIsNight = !mDataStorage.isNightTheme();
        mDataStorage.setNightMode(nowIsNight);
        setNightTheme(nowIsNight);
    }

    private void setNightTheme(boolean nowIsNight) {
        setActionBarColor(nowIsNight);
        setViewsColor(nowIsNight);
    }

    @Override
    protected void onPause() {
        mDateSelectorView.addOnBorderChangeListener(null);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDateSelectorView.addOnBorderChangeListener(this);
    }
}
