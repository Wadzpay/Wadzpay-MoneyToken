package com.wadzpay.ddflibrary.api;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.wadzpay.ddflibrary.activities.CurrencyTypeActivity;
import com.wadzpay.ddflibrary.activities.FiatTypeActivity;
import com.wadzpay.ddflibrary.activities.PaymentRequestDDFActivityK;
import com.wadzpay.ddflibrary.awsamplify.AwsAmplifyOperations;
import com.wadzpay.ddflibrary.callbacks.ActivityCallBack;
import com.wadzpay.ddflibrary.callbacks.ApiCallback;
import com.wadzpay.ddflibrary.logs.LoggerDDF;
import com.wadzpay.ddflibrary.utils.ConstantsActivity;
import com.wadzpay.ddflibrary.utils.NetworkCheck;


public class ApiService extends Service implements ApiCallback {
    private String TAG = getClass().getSimpleName();
    Context mContext;

    //    Activity mActivity;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LoggerDDF.e(TAG, "IBinder");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LoggerDDF.e(TAG, "onStartCommand");
//        mActivity = (Activity) intent.getParcelableExtra("activity");
//        mActivity = (Activity) intent.getSerializableExtra("activity");
//        displayServiceDialog();
        startApiCall();
        return super.onStartCommand(intent, flags, startId);
    }

    AlertDialog alertDialog;

    private void displayServiceDialog() {
        LoggerDDF.e(TAG, "displayServiceDialog");
        alertDialog = new AlertDialog.Builder(ConstantsApi.mActivity)
                .setTitle("Title")
                .setMessage("Are you sure?")
                .create();

        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();

    }

    private void dismissServiceDialog() {
        LoggerDDF.e(TAG, "dismissServiceDialog");
        alertDialog.dismiss();
        if (alertDialog.isShowing()) {

        }
    }

    private void startApiCall() {
        LoggerDDF.e(TAG, "service startApiCall");
        NetworkCheck networkCheck = NetworkCheck.getInstance();
        if (networkCheck.isNetworkConnected(this)) {
            ApiAsyncTask apiAsyncTask = null;
            if (apiAsyncTask != null) {
                apiAsyncTask.cancel(true);
            }
//            ConstantsApi.API_CALL_URL = ConstantsApi.BASE_URL + ConstantsApi.DROP_DOWN_URL;
            LoggerDDF.e(TAG, "API = " + ConstantsApi.FLAG_API_NAME);
            ConstantsApi.API_GET_POST = "GET";
            if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_TEST)) {
                ConstantsApi.BASE_URL = "";
                ConstantsApi.SUB_URL = ConstantsApi.TEST_ONE_URL;
            } else if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_PAYMENT_INFO)) {
                ConstantsApi.API_GET_POST = "POST";
                ConstantsApi.SUB_URL = ConstantsApi.PAYMENT_INFO_URL;
            } else if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_CONVERSION)) {
                ConstantsApi.API_GET_POST = "POST";
                ConstantsApi.SUB_URL = ConstantsApi.CONVERSION_URL + "fiatType=USD";
            } else if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_CURRENCY_LIST)) {
                ConstantsApi.API_GET_POST = "GET";
                if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT.equalsIgnoreCase("GEIDEA")) {
                    ConstantsApi.SUB_URL = ConstantsApi.FIAT_LIST_URL_TWO;
                } else {
//                    ConstantsApi.SUB_URL = ConstantsApi.FIAT_LIST_URL;
                    ConstantsApi.SUB_URL = ConstantsApi.FIAT_LIST_URL_TWO;
                }
            } else if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_FIAT_LIST)) {
                ConstantsApi.API_GET_POST = "GET";
                if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT.equalsIgnoreCase("GEIDEA")) {
                    ConstantsApi.SUB_URL = ConstantsApi.FIAT_LIST_URL_TWO;
                } else {
//                    ConstantsApi.SUB_URL = ConstantsApi.FIAT_LIST_URL;
                    ConstantsApi.SUB_URL = ConstantsApi.FIAT_LIST_URL_TWO;
                }
            } else if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_MERCHANT_DETAILS)) {
                ConstantsApi.API_GET_POST = "GET";
                if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT.equalsIgnoreCase("GEIDEA")) {
                    ConstantsApi.SUB_URL = ConstantsApi.MERCHANT_DETAILS_URL_TWO;
                } else {
                    ConstantsApi.SUB_URL = ConstantsApi.MERCHANT_DETAILS_URL;
                }
            } else if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_ADD_POS_MERCHANT)) {
                ConstantsApi.API_GET_POST = "POST";
                ConstantsApi.SUB_URL = ConstantsApi.ADD_POS_MERCHANT_URL;
            } else {
                ConstantsApi.BASE_URL = "";
                ConstantsApi.SUB_URL = "https://api.first.org/data/v1/countries";
            }

            ConstantsApi.API_CALL_URL = ConstantsApi.BASE_URL + ConstantsApi.SUB_URL;
            ApiHttpCall apiHttpCall = ApiHttpCall.getInstance();
            apiHttpCall.httpUrlCallS(this);
            LoggerDDF.e(TAG, "end startApiCall");

        }
    }

    private ActivityCallBack activityCallback;

    public void callBackActivity() {
        LoggerDDF.e(TAG, " @callBackStart@apiservice@75");
        if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_TEST)) {
//            TestActivity testActivity = TestActivity.testActivity;
//            activityCallback = (ActivityCallBack) testActivity;
//            activityCallback.callActivity("1");
            /*for (int i = 0; i < ConstantsApi.alTestOne.get(0).size(); i++) {
                LoggerDDF.e(TAG, ConstantsApi.alTestOne.get(i).get(ConstantsApi.KEYS_TEST_ONE[1]) + " @35");
            }*/
        } else if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_CURRENCY_LIST)) {
            LoggerDDF.e(TAG, " @FLAG_API_FIAT_LIST@apiservice@75");
            CurrencyTypeActivity currencyTypeActivity = CurrencyTypeActivity.currencyTypeActivity;
            activityCallback = (ActivityCallBack) currencyTypeActivity;
            activityCallback.activityCall("CurrencyTypeActivity");
        } else if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_FIAT_LIST)) {
            LoggerDDF.e(TAG, " @FLAG_API_FIAT_LIST@apiservice@75");
            FiatTypeActivity fiatTypeActivity = FiatTypeActivity.fiatTypeActivity;
            activityCallback = (ActivityCallBack) fiatTypeActivity;
            activityCallback.activityCall("FiatTypeActivity");
        } else if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_MERCHANT_DETAILS)) {
            LoggerDDF.e(TAG, " @FLAG_API_MERCHANT_DETAILS@apiservice@75");
            PaymentRequestDDFActivityK paymentRequestDDFActivityK = PaymentRequestDDFActivityK.paymentRequestDDFActivityK;
            activityCallback = (ActivityCallBack) paymentRequestDDFActivityK;
            activityCallback.activityCall("MERCHANT_DETAILS");
        } else if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_CONVERSION)) {
            LoggerDDF.e(TAG, " @FLAG_API_FIAT_LIST@apiservice@75");
            CurrencyTypeActivity currencyTypeActivity = CurrencyTypeActivity.currencyTypeActivity;
            activityCallback = (ActivityCallBack) currencyTypeActivity;
            activityCallback.activityCall("CurrencyTypeActivity");
        }
    }

    @Override
    public void apiSendResponse(String strResponse) {
        try {
            LoggerDDF.e(TAG, "sendResponse@api@93*");
//            LoggerDDF.e(TAG, strResponse);
//            jsonOperation(strResponse.trim());
            ApiListOperations apiListOperations = ApiListOperations.getInstance();
            if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_TEST)) {
                apiListOperations.jsonOperationTestOne(strResponse.trim());
            } else if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_CURRENCY_LIST)) {
                apiListOperations.jsonOperationFiatList(strResponse.trim());
            } else if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_FIAT_LIST)) {
                apiListOperations.jsonOperationFiatList(strResponse.trim());
            } else if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_MERCHANT_DETAILS)) {
                apiListOperations.jsonOperationMerchantDetails(strResponse.trim());
            } else if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_CONVERSION)) {
                /*
                JSONObject jsonResponseDATA = new JSONObject(strResponse);
                ConstantsApi.alConversionsNew = apiListOperations.jsonHashMapArrayKeys(
                        jsonResponseDATA.getJSONArray("paymentModes"),
                        ConstantsApi.KEYS_CONVERSION_PARSE
                );
                LoggerDDF.e("alConversionsNew@163",ConstantsApi.alConversionsNew.size()+"");
                LoggerDDF.e("alConversionsNew@163",ConstantsApi.alConversionsNew.get(0).get(ConstantsApi.KEYS_CONVERSION_PARSE[0])+"");
               */
                apiListOperations.jsonOperationConversionsOldWay(strResponse.trim());
            }

            callBackActivity();
            stopService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void apiCallBackFailed(String strResponse) {
        LoggerDDF.e(TAG, ConstantsApi.FLAG_API_NAME + " apiCallBackFailed@184");
        if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_FIAT_LIST)) {
            LoggerDDF.e(TAG, " @FLAG_API_FIAT_LIST@apiservice@75");
            FiatTypeActivity fiatTypeActivity = FiatTypeActivity.fiatTypeActivity;
            activityCallback = (ActivityCallBack) fiatTypeActivity;
            activityCallback.activityCallFailed("FiatTypeActivity");
            amplifyOperations(fiatTypeActivity);
        } else if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_CURRENCY_LIST)) {
            LoggerDDF.e(TAG, " @FLAG_API_FIAT_LIST@apiservice@75");
            CurrencyTypeActivity currencyTypeActivity = CurrencyTypeActivity.currencyTypeActivity;
            activityCallback = (ActivityCallBack) currencyTypeActivity;
            activityCallback.activityCallFailed("CurrencyTypeActivity");
            amplifyOperations(currencyTypeActivity);
        } else if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_MERCHANT_DETAILS)) {
            PaymentRequestDDFActivityK paymentRequestDDFActivityK = PaymentRequestDDFActivityK.paymentRequestDDFActivityK;
            activityCallback = (ActivityCallBack) paymentRequestDDFActivityK;
            activityCallback.activityCall("MERCHANT_DETAILS");
        }
        stopService();
    }

    private void amplifyOperations(Activity activity) {
        ConstantsApi.API_TEMP_JWT = "";
        AwsAmplifyOperations awsAmplifyOperations = AwsAmplifyOperations.getInstance(activity);
        awsAmplifyOperations.passContext(activity);
        awsAmplifyOperations.amplifyInit();
    }

    private void callSummaryActivity() {

    }

    private void stopService() {
        LoggerDDF.e(TAG, "stopService");
        Intent serviceIntent = new Intent(this, ApiService.class);
        stopService(serviceIntent);
//        dismissServiceDialog();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
