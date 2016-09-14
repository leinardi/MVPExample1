package com.leinardi.mvp;

import android.app.Application;

/**
 * Created by leinardi on 18/07/16.
 */

public class MvpExample1App extends Application {

    private static MvpExample1App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static MvpExample1App getInstance() {
        return sInstance;
    }

}
