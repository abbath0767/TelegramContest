package com.ng.telegramcontest;

import android.app.Application;

import com.ng.telegramcontest.data.DataStorage;

public class App extends Application {

    DataStorage dataStorage;

    @Override
    public void onCreate() {
        super.onCreate();

        dataStorage = new DataStorage();
        dataStorage.initialize(this);
    }

    public DataStorage getDataStorage() {
        return dataStorage;
    }
}
