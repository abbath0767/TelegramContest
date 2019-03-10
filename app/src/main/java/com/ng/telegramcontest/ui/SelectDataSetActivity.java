package com.ng.telegramcontest.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
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
        setTitle(R.string.select_diagram);

        root = findViewById(R.id.content_view);

        mDataStorage = ((App) getApplicationContext()).getDataStorage();

        setUpChartsButtons(mDataStorage.getCharts().getChartsData());
    }

    private void setUpChartsButtons(final ChartData[] chartsData) {
        for (int i = 0; i < chartsData.length; i++) {
            final int num = i;
            AppCompatButton button = new AppCompatButton(this, null, R.style.Selector);
            button.setText(getResources().getString(R.string.data_set_describe, i + 1, chartsData[i].getX().getValues().length));
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            button.setBackgroundResource(outValue.resourceId);
            button.setPadding((int) getResources().getDimension(R.dimen.common_16), (int) getResources().getDimension(R.dimen.common_8), (int) getResources().getDimension(R.dimen.common_16), 0);

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
