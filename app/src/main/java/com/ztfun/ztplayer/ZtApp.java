package com.ztfun.ztplayer;

import android.app.Application;

import com.ztfun.util.Log;

public class ZtApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // setup log
        Log.isDebug = BuildConfig.DEBUG;
    }
}
