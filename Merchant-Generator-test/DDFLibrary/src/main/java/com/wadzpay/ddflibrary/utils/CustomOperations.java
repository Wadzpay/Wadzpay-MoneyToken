package com.wadzpay.ddflibrary.utils;

import com.wadzpay.ddflibrary.logs.LoggerDDF;

public class CustomOperations {
    public static String TAG = CustomOperations.class.getSimpleName();

    public static int getCurrencyImageResource(String strCurrencyCode){
        int imageRes;
        try {
            imageRes = ConstantsActivity.hmCryptoImages.get(strCurrencyCode);
        } catch (Exception e){
            LoggerDDF.e(TAG, strCurrencyCode + " @13");
            LoggerDDF.e(TAG, e + " @13");
            imageRes = ConstantsActivity.hmCryptoImages.get("NA");
        } finally {

        }
        return imageRes;
    }
}
