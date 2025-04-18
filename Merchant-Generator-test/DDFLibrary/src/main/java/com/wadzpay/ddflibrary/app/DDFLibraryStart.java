package com.wadzpay.ddflibrary.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.wadzpay.ddflibrary.api.ApiService;
import com.wadzpay.ddflibrary.api.ConstantsApi;
import com.wadzpay.ddflibrary.logs.LoggerDDF;


public class DDFLibraryStart {
    private String TAG = getClass().getSimpleName();
    private static Context mContext;
    private static Activity mActivity;

    private DDFLibraryStart(Context context) {
        mContext = context;
    }

    public static void openQRCodeLibrary(Context context) {
        mContext = context;
        mActivity = (Activity) context;
        startFirstActivity();
    }

    public static void startFirstActivity() {
        String TAG = "DDFLibraryStart";
        LoggerDDF.e(TAG, "startFirstActivity");
        startService();
    }

    public static void startService(){
        String TAG = "DDFLibraryStart";
        LoggerDDF.e(TAG, "startService");
        ConstantsApi.FLAG_API_NAME = ConstantsApi.FLAG_API_TEST;
        Intent serviceIntent = new Intent(mActivity, ApiService.class);
        mActivity.startService(serviceIntent);
    }

    /* Common Intent  */
    private static void startFirstIntent(Class editScreen) {
        Intent intent = new Intent(mActivity, editScreen);
        mActivity.startActivity(intent);
        mActivity.finish();
    }
}
