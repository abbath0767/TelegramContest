package com.ng.telegramcontest.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.ng.telegramcontest.R;
import com.ng.telegramcontest.data.ChartData;

public class DateSelectorView extends RelativeLayout {

    public DateSelectorView(Context context) {
        this(context, null);
    }

    public DateSelectorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DateSelectorView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DateSelectorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflateView(context);
    }

    private SmallGraph mSmallGraph;
    private SelectWindowView mSelectWindowView;

    private void inflateView(Context context) {
        View.inflate(context, R.layout.view_date_selector, this);
        setBackgroundColor(context.getResources().getColor(R.color.white));
        mSmallGraph = (SmallGraph) findViewById(R.id.view_date_selector_small_graph);
        mSelectWindowView = (SelectWindowView) findViewById(R.id.view_date_selector_window);
    }

    public void initData(ChartData chartData, boolean[] selectedCharts) {
        mSmallGraph.initData(chartData, selectedCharts);
        mSelectWindowView.initData(chartData.getX());
    }

    public void changeSelect(boolean[] selectedCharts) {
        mSmallGraph.changeSelect(selectedCharts);
    }

    public void addOnBorderChangeListener(SelectWindowView.BorderChangeListener listener) {
        mSelectWindowView.addOnBorderChangeListener(listener);
    }

    public void setIsNightMode(boolean isNightTheme) {
        if (isNightTheme) {
            setBackgroundColor(getResources().getColor(R.color.colorPrimaryNight));
        } else {
            setBackgroundResource(0);
        }
        mSelectWindowView.setNightMode(isNightTheme);
    }
}
