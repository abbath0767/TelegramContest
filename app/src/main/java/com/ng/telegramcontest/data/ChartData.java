package com.ng.telegramcontest.data;

import java.util.Arrays;

public class ChartData {
    private final DataSet[] dataSets;
    private final DataSet x;

    public ChartData(DataSet[] dataSets, DataSet x) {
        this.dataSets = dataSets;
        this.x = x;
    }

    @Override
    public String toString() {
        return "ChartData{" +
                "dataSets=" + Arrays.toString(dataSets) +
                ", x=" + x +
                '}';
    }

    public DataSet[] getDataSets() {
        return dataSets;
    }

    public DataSet getX() {
        return x;
    }
}
