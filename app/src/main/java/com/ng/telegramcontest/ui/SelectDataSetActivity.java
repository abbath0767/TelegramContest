package com.ng.telegramcontest.ui;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import com.ng.telegramcontest.App;
import com.ng.telegramcontest.R;
import com.ng.telegramcontest.data.ChartData;
import com.ng.telegramcontest.data.DataStorage;

public class SelectDataSetActivity extends AppCompatActivity {

    private LinearLayout root;
    private DataStorage mDataStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_data_set_activity);
        root = findViewById(R.id.content_view);
        mDataStorage = ((App) getApplicationContext()).getDataStorage();
    }

    private void setActionBar(boolean isNightTheme) {
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        setTitle(R.string.select_diagram);

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
        findViewById(R.id.content_view).setBackgroundColor(getResources().getColor(colorActivityBackground));
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isNightTheme = mDataStorage.isNightTheme();
        setActionBar(isNightTheme);
        setUpChartsButtons(mDataStorage.getCharts().getChartsData(), isNightTheme);
    }

    private void setUpChartsButtons(final ChartData[] chartsData, boolean isNightTheme) {
        for (int i = 0; i < chartsData.length; i++) {
            final int num = i;
            AppCompatButton button = new AppCompatButton(this, null, R.style.Selector);
            button.setText(getResources().getString(R.string.data_set_describe, i + 1, chartsData[i].getX().getValues().length));
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            button.setBackgroundResource(outValue.resourceId);
            button.setPadding((int) getResources().getDimension(R.dimen.common_16), (int) getResources().getDimension(R.dimen.common_8), (int) getResources().getDimension(R.dimen.common_16), 0);
            button.setTextColor(getResources().getColor(isNightTheme ? R.color.white : R.color.black));

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDiagramWithDataSetNumber(num);
                }
            });

            root.addView(button, layoutParams);

            if (i != chartsData.length - 1) {
                View separator = new View(this);
                separator.setBackgroundColor(getResources().getColor(R.color.colorSeparatorDay));
                layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.common_1));
                layoutParams.leftMargin = (int) getResources().getDimension(R.dimen.common_16);
                layoutParams.rightMargin = (int) getResources().getDimension(R.dimen.common_16);
                layoutParams.topMargin = (int) getResources().getDimension(R.dimen.common_8);

                root.addView(separator, layoutParams);
            }
        }
    }

    private void openDiagramWithDataSetNumber(int num) {
        DiagramActivity.startActivity(this, num);
    }
}
