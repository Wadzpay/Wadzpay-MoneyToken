package com.wadzpay.ddflibrary.app;

import android.app.Application;
import android.content.Context;

import com.wadzpay.ddflibrary.logs.LoggerDDF;


public class DDFLibraryApp extends Application {
    private String TAG = getClass().getSimpleName();
    private static Context context;

    public void onCreate() {
        super.onCreate();
        LoggerDDF.e(TAG, "onCreate");
        DDFLibraryApp.context = getApplicationContext();
    }

    public static Context getAppContext() {
        LoggerDDF.e("DDFLibraryApp", "getAppContext");
        return DDFLibraryApp.context;
    }
}
