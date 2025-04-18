package com.wadzpay.ddflibrary.api;


import android.util.Log;

import com.wadzpay.ddflibrary.logs.LoggerDDF;
import com.wadzpay.ddflibrary.utils.ConstantsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ApiListOperations {
    private String TAG = getClass().getSimpleName();
    private static ApiListOperations instance = null;

    private ApiListOperations() {
    }

    public static ApiListOperations getInstance() {
        if (instance == null) {
            instance = new ApiListOperations();
        }
        return instance;
    }

    public void jsonOperationFiatList(String strResponse) {
        try {
            LoggerDDF.e(TAG, "jsonOperationFiatList");
            JSONObject jsonResponse = new JSONObject(strResponse);
//            String strData = jsonResponse.getString("fiats");
            JSONObject jsonResponseDATA = new JSONObject(strResponse);
//            JSONObject jsonResponseDATA = new JSONObject(strResponse);
            ApiJsonParsing apiJsonParsing = ApiJsonParsing.getInstance();
            ConstantsApi.alFiats = apiJsonParsing.jsonHashMapArrayKeys(
                    jsonResponseDATA.getJSONArray("fiats"),
                    ConstantsApi.KEYS_FIAT_PARSE
            );
            ConstantsApi.alDigitalCurrencies = apiJsonParsing.jsonHashMapArrayKeys(
                    jsonResponseDATA.getJSONArray("digitalCurrencyList"),
                    ConstantsApi.KEYS_CURRENCY_PARSE
            );
//            ConstantsApi.alFiats.get(1).get("code");
//            LoggerDDF.e(TAG, ConstantsApi.alFiats.get(0).get(ConstantsApi.KEYS_FIAT_PARSE[0]) + " @33");
            LoggerDDF.e(TAG, ConstantsApi.alFiats.size() + " @33##");
//            LoggerDDF.e(TAG, ConstantsApi.alFiats.get(0).get(ConstantsApi.KEYS_FIAT_PARSE[0]) + " @33##");

            /*for (int i = 0; i < ConstantsApi.alFiats.size(); i++) {
                LoggerDDF.e(TAG, ConstantsApi.alFiats.get(i).get(ConstantsApi.KEYS_FIAT_PARSE[0]) + " @33##");
                LoggerDDF.e(TAG, ConstantsApi.alFiats.get(i).get(ConstantsApi.KEYS_FIAT_PARSE[1]) + " @33##");
            }*/

        } catch (JSONException e) {
            e.printStackTrace();
            LoggerDDF.e(TAG, e + "");
        }
    }


    public void jsonOperationMerchantDetails(String strResponse) {
        try {
            LoggerDDF.e(TAG, "jsonOperationMerchantDetails @64");
            JSONObject jsonResponse = new JSONObject(strResponse);
            String strDataMerchant = jsonResponse.getString("merchant");
            LoggerDDF.e(TAG + "@62", strDataMerchant);
            LoggerDDF.e(TAG, ConstantsApi.alMerchantDetails.size() + " @71##");
            JSONObject jsonResponseDATA2 = new JSONObject(strDataMerchant);
            LoggerDDF.e(TAG, jsonResponseDATA2.getString("name") + " @65##");
            ConstantsApi.strPosName = jsonResponseDATA2.getString("name");
            ConstantsApi.strMDEmail = jsonResponseDATA2.getString("primaryContactEmail");
            ConstantsApi.strMDPhone = jsonResponseDATA2.getString("primaryContactPhoneNumber");
//            ConstantsApi.strMDQRString = jsonResponseDATA2.getString("qrStringEncrypted");
            if(ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT.equalsIgnoreCase("GEIDEA")) {
                ConstantsApi.strMDQRString = jsonResponse.getString("qrStringEncrypted");
            }
            JSONArray jsonArray = jsonResponseDATA2.getJSONArray("merchantPoses");
            LoggerDDF.e(TAG + "@65", jsonArray.length() + "");

            ApiJsonParsing apiJsonParsing = ApiJsonParsing.getInstance();
            ConstantsApi.alMerchantDetails = apiJsonParsing.jsonHashMapArrayKeys(
                    jsonResponseDATA2.getJSONArray("merchantPoses"),
                    ConstantsApi.KEYS_MERCHANT_DETAILS_PARSE
            );
            LoggerDDF.e(TAG, ConstantsApi.alMerchantDetails.size() + " @71##");
//            LoggerDDF.e(TAG, ConstantsApi.alMerchantDetails.get(0).get("posName") + " @76##");
        } catch (JSONException e) {
            e.printStackTrace();
            LoggerDDF.e(TAG + "@80", e + "");
        }
    }

    public void jsonOperationTestOne(String strResponse) {
        try {
            LoggerDDF.e(TAG, "jsonOperationTestOne");
            JSONObject jsonResponseDATA = new JSONObject(strResponse);
//            String strData = jsonResponse.getString(ConstantsApi.KEYS_DROP_DOWN_PARSE[2]);
//            String strData = jsonResponse.getString("data");
//            JSONObject jsonResponseDATA = new JSONObject(strData);

            ApiJsonParsing apiJsonParsing = ApiJsonParsing.getInstance();
            ConstantsApi.alTestOne = apiJsonParsing.jsonHashMapArrayKeys(jsonResponseDATA.getJSONArray("data"), ConstantsApi.KEYS_TEST_PARSE);
            LoggerDDF.e(TAG, ConstantsApi.alTestOne.get(0).get(ConstantsApi.KEYS_TEST_PARSE[1]) + " @35");
            LoggerDDF.e(TAG, ConstantsApi.alTestOne.get(0).size() + " @35");
            /*for (int i = 0; i < ConstantsApi.alTestOne.get(0).size(); i++) {
                LoggerDDF.e(TAG, ConstantsApi.alTestOne.get(i).get(ConstantsApi.KEYS_TEST_ONE[1]) + " @35");
            }*/
        } catch (JSONException e) {
            e.printStackTrace();
            LoggerDDF.e(TAG, e + "");
        }
    }

    public ArrayList<HashMap<String, String>> jsonHashMapArrayKeys(JSONArray jsonArray, String[] keys) {
        ArrayList<HashMap<String, String>> contactList = new ArrayList<>();
        try {
            if (jsonArray != null) {
                JSONObject jsonObject;
                Log.e("jsonArray", jsonArray.length() + "");
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMapValues = new HashMap<>();
                    ;
                    for (int j = 0; j < keys.length; j++) {
                        String valueResponse = jsonObject.getString(keys[j]);
                        Log.e("valueResponse", valueResponse + "");
                        hashMapValues.put(keys[j], valueResponse);
                    }
                    contactList.add(hashMapValues);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("contactList", contactList.size() + "");
        return contactList;
    }

    //    old way
    public void jsonOperationConversionsOldWay(String strResponse) {
        try {
            LoggerDDF.e(TAG, "jsonOperationConversionsOldWay");
            JSONObject jsonResponseDATA = new JSONObject(strResponse);
            ApiJsonParsing apiJsonParsing = ApiJsonParsing.getInstance();
            ConstantsApi.alDigitalCurrencies = apiJsonParsing.jsonHashMapArrayKeys(
                    jsonResponseDATA.getJSONArray("paymentModes"),
                    ConstantsApi.KEYS_CONVERSION_PARSE
            );

            LoggerDDF.e(TAG, ConstantsApi.alDigitalCurrencies.size() + " @33##");
            LoggerDDF.e(TAG, ConstantsApi.alDigitalCurrencies.get(0).get(ConstantsApi.KEYS_CONVERSION_PARSE[0]) + " @33##");
        } catch (JSONException e) {
            e.printStackTrace();
            LoggerDDF.e(TAG, e + "");
        }
    }
}
