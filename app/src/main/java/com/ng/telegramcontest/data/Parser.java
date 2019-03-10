package com.ng.telegramcontest.data;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class Parser {

    private static final String PATH = "chart_data.json";
    private static final String UTF_8 = "UTF-8";
    private static final String COLUMN = "columns";

    Charts getCharts(Context context) {
        Charts charts = parseData(context);

        return charts;
    }

    private Charts parseData(Context context) {
        ChartData[] chartData = new ChartData[5];

        AssetManager am = context.getAssets();
        InputStream in;
        JsonReader reader = null;
        try {
            in = am.open(PATH);
            reader = new JsonReader(new InputStreamReader(in, UTF_8));

            int counter = 0;
            reader.beginArray();
            while (reader.hasNext()) {
                chartData[counter] = getChartData(reader);
                counter++;
            }
            reader.endArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new Charts(chartData);
    }

    private ChartData getChartData(JsonReader reader) throws IOException {
        ChartDataFactory factory = new ChartDataFactory();

        reader.beginObject();
        while (reader.hasNext()) {
            factory.newChartData();
            String name = reader.nextName();

            if (name.equals(COLUMN)) {
                reader.beginArray();

                while (reader.hasNext()) {
                    factory.newColumn();
                    boolean isName = true;
                    String columnName = "";
                    long value = 0l;
                    reader.beginArray();

                    while (reader.hasNext()) {
                        if (isName) {
                            isName = false;
                            columnName = reader.nextString();
                            factory.addColumnName(columnName);
                        } else {
                            value = reader.nextLong();
                            factory.addValue(value);
                        }
                    }

                    factory.pushValues();

                    reader.endArray();
                }

                reader.endArray();
            } else if (name.equals("types")) {
                reader.beginObject();

                while (reader.hasNext()) {
                    String nameType = reader.nextName();
                    String type = reader.nextString();
                    factory.pushType(nameType, type);
                }

                reader.endObject();
            } else if (name.equals("names")) {
                reader.beginObject();

                while (reader.hasNext()) {
                    String nameType = reader.nextName();
                    String strName = reader.nextString();
                    factory.pushName(nameType, strName);
                }

                reader.endObject();

            } else if (name.equals("colors")) {
                reader.beginObject();

                while (reader.hasNext()) {
                    String nameType = reader.nextName();
                    String color = reader.nextString();
                    factory.pushColor(nameType, color);
                }

                reader.endObject();
            }
        }
        reader.endObject();

        return factory.getChartData();
    }
}
