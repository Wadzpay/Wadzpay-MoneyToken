package com.wadzpay.ddflibrary.api;

import android.app.Application;
import android.content.Context;

import com.wadzpay.ddflibrary.app.DDFLibraryApp;
import com.wadzpay.ddflibrary.callbacks.ApiCallback;
import com.wadzpay.ddflibrary.logs.LoggerDDF;
import com.wadzpay.ddflibrary.utils.NetworkCheck;


public class ApiOperations implements ApiCallback {
    private String TAG = getClass().getSimpleName();
    private static ApiOperations instance = null;

    private ApiOperations() {
    }

    public static ApiOperations getInstance() {
        if (instance == null) {
            instance = new ApiOperations();
        }
        return instance;
    }

    Application applicationContext;
    Context mContext;
    public void startApiCall() {
        mContext = DDFLibraryApp.getAppContext();
//        applicationContext = (Application) context.getApplicationContext();
        NetworkCheck networkCheck = NetworkCheck.getInstance();
        if (networkCheck.isNetworkConnected(mContext)) {
            ApiAsyncTask apiAsyncTask = null;
            if (apiAsyncTask != null) {
                apiAsyncTask.cancel(true);
            }
            apiAsyncTask = ApiAsyncTask.getInstance(mContext, ConstantsApi.API_CALL_URL);
//                    AsyncTaskApi asyncTaskApi = new AsyncTaskApi(this, BASE_URL, CONTACTS_URL);
            apiAsyncTask.execute();
        }
    }

    public void vehicleInfo() {

    }

    @Override
    public void apiSendResponse(String strResponse) {
        LoggerDDF.e(TAG, "sendResponse@44");
        LoggerDDF.e(TAG, strResponse);
    }

    @Override
    public void apiCallBackFailed(String strResponse) {

    }
}
