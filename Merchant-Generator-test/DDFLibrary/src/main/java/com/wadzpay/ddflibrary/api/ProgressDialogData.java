package com.wadzpay.ddflibrary.api;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import com.wadzpay.ddflibrary.callbacks.ApiCallback;
import com.wadzpay.ddflibrary.logs.LoggerDDF;
import com.wadzpay.ddflibrary.utils.NetworkCheck;


public class ProgressDialogData extends ProgressDialog implements ApiCallback {
    private String TAG = getClass().getSimpleName();
    Context mContext;
    public ProgressDialogData(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        startApiCall();
    }

    public void startApiCall() {
        NetworkCheck networkCheck = NetworkCheck.getInstance();
        if (networkCheck.isNetworkConnected(mContext)) {
            ApiAsyncTask apiAsyncTask = null;
            if (apiAsyncTask != null) {
                apiAsyncTask.cancel(true);
            }
            ConstantsApi.API_CALL_URL = ConstantsApi.TEST_ONE_URL;
            apiAsyncTask = ApiAsyncTask.getInstance(mContext, ConstantsApi.API_CALL_URL);
//                    AsyncTaskApi asyncTaskApi = new AsyncTaskApi(this, BASE_URL, CONTACTS_URL);
            apiAsyncTask.execute();
        }
    }

    @Override
    public void apiSendResponse(String strResponse) {
        LoggerDDF.e(TAG, "sendResponse@41");
        LoggerDDF.e(TAG, strResponse);
    }

    @Override
    public void apiCallBackFailed(String strResponse) {

    }
}
