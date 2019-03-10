package com.ng.telegramcontest.data;

import java.util.ArrayList;
import java.util.List;

public class ChartDataFactory {
    private long[] tmpValues = new long[10000];
    private int tmpIndex = 0;
    private List<DataSet> mDataSets = new ArrayList<DataSet>();

    public void newChartData() {
    }

    public void newColumn() {
    }

    public void addColumnName(String columnName) {
        DataSet.Type type;
        if (columnName.equals("x")) type = DataSet.Type.X;
        else type = DataSet.Type.Y;

        DataSet dataSet = new DataSet(null, type, columnName, null);
        mDataSets.add(dataSet);
    }

    public ChartData getChartData() {
        DataSet[] y = new DataSet[mDataSets.size() - 1];
        int index = 0;
        DataSet x = null;

        for (DataSet set: mDataSets) {
            if (set.getType().equals(DataSet.Type.X)) {
                x = set;
            } else {
                y[index] = set;
                index++;
            }
        }

        return new ChartData(y, x);
    }

    public void addValue(long value) {
        tmpValues[tmpIndex] = value;
        tmpIndex++;
    }

    public void pushValues() {
        DataSet dataSet = mDataSets.get(mDataSets.size() - 1);
        long[] values = new long[tmpIndex];
        System.arraycopy(tmpValues, 0, values, 0, tmpIndex);
        tmpIndex = 0;
        dataSet.setValues(values);
    }

    //useless?
    public void pushType(String nameType, String type) {
        for (int i = 0; i < mDataSets.size(); i++) {
            DataSet dataSet = mDataSets.get(i);
            if (dataSet.getName().equals(nameType)) {
                dataSet.setTypeName(type);
            }
        }
    }

    public void pushName(String nameType, String strName) {
        for (int i = 0; i < mDataSets.size(); i++) {
            DataSet dataSet = mDataSets.get(i);
            if (dataSet.getName().equals(nameType)) {
                dataSet.setEntityName(strName);
            }
        }
    }

    public void pushColor(String nameType, String color) {
        for (int i = 0; i < mDataSets.size(); i++) {
            DataSet dataSet = mDataSets.get(i);
            if (dataSet.getName().equals(nameType)) {
                dataSet.setColor(color);
            }
        }
    }
}
