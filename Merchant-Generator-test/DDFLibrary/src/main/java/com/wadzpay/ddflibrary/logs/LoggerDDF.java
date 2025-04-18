package com.wadzpay.ddflibrary.logs;

import android.util.Log;


/* Custom Log Class with all operations */
public class LoggerDDF {

    private static LoggerDDF instance = null;

    private LoggerDDF() {
    }

    public static LoggerDDF getInstance() {
        if (instance == null) {
            instance = new LoggerDDF();
        }
        return instance;
    }

    public static void e(String tag, String data) {
        Log.e(tag, data);
    }

    public static void d(String tag, String data) {
        Log.d(tag, data);
    }

    public static void i(String tag, String data) {
        Log.i(tag, data);
    }

    public static void w(String tag, String data) {
        Log.e(tag, data);
    }

    public static void wtf(String tag, String data) {
        Log.wtf(tag, data);
    }

    /*public static void d(String tag, String data) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, data);
        }
    }

    public static void i(String tag, String data) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, data);
        }
    }

    public static void e(String tag, String data) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, data);
        }
    }

    public static void e(String tag, String data, Throwable t) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, data, t);
        }
    }

    public static void w(String tag, String data) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, data);
        }
    }

    public static void wtf(String tag, String data) {
        if (BuildConfig.DEBUG) {
            Log.wtf(tag, data);
        }
    }

    public static boolean isDebugEnabled(String tag) {
        return BuildConfig.DEBUG;
    }*/
}
