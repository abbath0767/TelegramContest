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

    public long getMaxX() {
        return x.getMaxValue();
    }

    public long getMinX() {
        return x.getMinValue();
    }

    public long getMaxYFrom(boolean[] arraysOfNeedCharts) {
        long max = 0;

        for (int i = 0; i < arraysOfNeedCharts.length; i++) {
            long currentMax = dataSets[i].getMaxValue();
            if (arraysOfNeedCharts[i] && max < currentMax) {
                max = currentMax;
            }
        }

        return max;
    }

    public long getMaxYFrom(int[] arraysIndexes) {
        long maxY = -1;

        long max = dataSets[arraysIndexes[0]].getMaxValue();
        for (int i = 0; i < arraysIndexes.length; i++) {
            if (maxY <= max) {
                maxY = max;
            }
        }

        return maxY;
    }

    public long getMaxY() {
        int[] from = new int[dataSets.length];
        for (int i = 0; i < from.length; i++) {
            from[i] = i;
        }
        return getMaxYFrom(from);
    }

    public long getMinYFrom(boolean[] arraysOfNeedCharts) {
        long min = -1;

        for (int i = 0; i < arraysOfNeedCharts.length; i++) {
            long currentMin = dataSets[i].getMinValue();

            if (min == -1 && arraysOfNeedCharts[i]) {
                min = currentMin;
            } else if (arraysOfNeedCharts[i] && min > currentMin) {
                min = currentMin;
            }
        }

        return min;
    }

    public long getMinYFrom(int[] arraysIndexes) {
        long minY = -1;

        for (int i = 0; i < arraysIndexes.length; i++) {
            long min = dataSets[arraysIndexes[i]].getMinValue();
            if (i == 0) {
                minY = min;
            } else if (minY > min) {
                minY = min;
            }
        }

        return minY;
    }

    public long getMinY() {
        int[] from = new int[dataSets.length];
        for (int i = 0; i < from.length; i++) {
            from[i] = i;
        }
        return getMinYFrom(from);
    }

    public int size() {
        return x.getValues().length;
    }
}
