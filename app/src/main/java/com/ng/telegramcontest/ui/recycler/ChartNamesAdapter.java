package com.ng.telegramcontest.ui.recycler;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.ng.telegramcontest.data.DataSet;

public class ChartNamesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final SelectChartListener mSelectChartListener;

    public ChartNamesAdapter(Context context, SelectChartListener listener) {
        this.mContext = context;
        this.mSelectChartListener = listener;
    }

    private DataSet[] mChartNames;
    private boolean[] selected;

    public void setNames(DataSet[] mDataSets, boolean[] selectedCharts) {
        this.mChartNames = mDataSets;
        selected = selectedCharts;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ChartNameViewHolder(mContext, viewGroup, mSelectChartListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ((ChartNameViewHolder) viewHolder).bind(mChartNames[i], selected[i], i == getItemCount() - 1);
    }

    @Override
    public int getItemCount() {
        return mChartNames.length;
    }

    public interface SelectChartListener {
        void onChartSelect(int chartIndex, boolean isSelect);
    }
}
