package com.ng.telegramcontest.util;

import com.ng.telegramcontest.data.DataSet;

public class CalculateHelper {
    public static long[] getPrepared(int count, DataSet dataSet, long minimum) {
        long[] result = new long[count];
        long[] values = dataSet.getValues();
        for (int i = 0; i < count; i++) {
            result[i] = values[i] - minimum;
        }
        return result;
    }

    public static long[] getCordValues(int count, long[] prepared, long preparedMax, long param) {
        long[] result = new long[count];
        for (int i = 0; i < count; i++) {
            result[i] = (param * prepared[i]) / preparedMax;
        }

        return result;
    }
}
