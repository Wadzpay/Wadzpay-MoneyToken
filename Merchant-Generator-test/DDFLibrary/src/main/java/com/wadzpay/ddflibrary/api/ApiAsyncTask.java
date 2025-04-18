package com.wadzpay.ddflibrary.api;

import android.content.Context;
import android.os.AsyncTask;

import com.wadzpay.ddflibrary.callbacks.ApiCallback;
import com.wadzpay.ddflibrary.dialogs.ConstantsDialog;
import com.wadzpay.ddflibrary.dialogs.DialogCustomize;
import com.wadzpay.ddflibrary.logs.LoggerDDF;


public class ApiAsyncTask extends AsyncTask<String, String, String> {
    private String TAG = getClass().getSimpleName();
    static Context mContext;
    DialogCustomize dialogCustomize;
    private static ApiAsyncTask instance = null;
    private ApiAsyncTask() {}
    public static ApiAsyncTask getInstance(Context context, String url) {
        mContext = context;
        ConstantsApi.API_CALL_URL = url;
        instance = null;
        if(instance == null) {
            instance = new ApiAsyncTask();
        }
        return instance;
    }
    /*public AsyncTaskApi(Context context, String baseUrl, String apiUrl) {
        mContext = context;
        mActivity = (Activity) mContext;
        ConstantsApi.BASE_URL = baseUrl;
        ConstantsApi.CONTACTS_URL = apiUrl;
        ConstantsApi.API_CALL_URL = ConstantsApi.BASE_URL+ ConstantsApi.CONTACTS_URL;
    }*/
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialogCustomize = DialogCustomize.getInstance();
        dialogCustomize.displayProgressDialog(mContext, ConstantsDialog.FLAG_DIALOG_DISPLAY);
    }

    @Override
    protected String doInBackground(String... strings) {
        ApiHttpCall apiHttpCall = ApiHttpCall.getInstance();
//        apiHttpCall.httpUrlCalls();
        return null;
    }

    private ApiCallback apiCallback;
    private String strMessage = "";

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        LoggerDDF.e(TAG, "onPostExecute");
        dialogCustomize.displayProgressDialog(mContext, ConstantsDialog.FLAG_DIALOG_DISMISS);
        apiCallback = (ApiCallback) mContext;
        apiCallback.apiSendResponse(ConstantsApi.apiResponseData + "");
    }
}