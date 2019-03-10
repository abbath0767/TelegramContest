package com.ng.telegramcontest.ui.recycler;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.ng.telegramcontest.R;
import com.ng.telegramcontest.data.DataSet;

public class ChartNameViewHolder extends RecyclerView.ViewHolder {

    public ChartNameViewHolder(Context context, ViewGroup viewGroup, ChartNamesAdapter.SelectChartListener selectChartListener) {
        super(LayoutInflater.from(context).inflate(R.layout.item_chart_name, viewGroup, false));
        name = itemView.findViewById(R.id.item_chart_name);
        checkBox = itemView.findViewById(R.id.item_chart_checkbox);
        separator = itemView.findViewById(R.id.item_chatr_separator);
        mSelectChartListener = selectChartListener;
    }

    private ChartNamesAdapter.SelectChartListener mSelectChartListener;
    private TextView name;
    private CheckBox checkBox;
    private View separator;

    public void bind(DataSet dataSet, boolean isSelect, boolean isLast) {
        this.name.setText(dataSet.getEntityName());
        checkBox.setButtonTintList(ColorStateList.valueOf(Color.parseColor(dataSet.getColor())));
        checkBox.setChecked(isSelect);
        if (isLast) separator.setVisibility(View.GONE);
        else separator.setVisibility(View.VISIBLE);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int pos = getAdapterPosition();
                if (pos == -1)
                    return;
                mSelectChartListener.onChartSelect(pos, isChecked);
            }
        });
    }
}
