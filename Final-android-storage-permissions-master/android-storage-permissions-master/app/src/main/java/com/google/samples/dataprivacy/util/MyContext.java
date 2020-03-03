package com.google.samples.dataprivacy.util;

import android.app.Application;
import android.content.Context;

public class MyContext extends Application {
    private static MyContext instance;

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }

    public static Context getContext(){
        // or return instance;
        return instance.getApplicationContext();
    }
}

