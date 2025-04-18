package com.wadzpay.ddflibrary.api;

import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.wadzpay.ddflibrary.activities.LoginActivity;
import com.wadzpay.ddflibrary.awsamplify.AwsAmplifyOperations;
import com.wadzpay.ddflibrary.callbacks.ApiCallback;
import com.wadzpay.ddflibrary.logs.LoggerDDF;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


public class ApiHttpCall {
    private Context mContext;
    private String TAG = getClass().getSimpleName();
    private static ApiHttpCall instance = null;
    AwsAmplifyOperations awsAmplifyOperations;

    private ApiHttpCall() {
    }

    public static ApiHttpCall getInstance() {
        if (instance == null) {
            instance = new ApiHttpCall();
        }
        return instance;
    }

    /*ApiServiceCall(Context context) {
        mContext = context;
    }*/

    //
    /*public void httpUrlCalls() {
        LoggerDDF.e(TAG, "httpUrlCalls");
        try {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
//            String strUrl = ConstantsApi.BASE_URL + ConstantsApi.CONTACTS_URL;
            String strUrl = ConstantsApi.API_CALL_URL;
            LoggerDDF.e(TAG, strUrl + " @50");
            URL url = new URL(strUrl);
//            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestMethod("GET");   //POST or GET
            urlConnection.connect();
            urlConnection.setConnectTimeout(ConstantsApi.API_CONNECTION_TIME_OUT);
            urlConnection.setReadTimeout(ConstantsApi.API_CONNECTION_TIME_OUT);
            // Check the connection status.
            LoggerDDF.e(TAG, "5");
            int statusCode = urlConnection.getResponseCode();
            LoggerDDF.e(TAG, statusCode + " @63");
            if (statusCode == 200) {
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String responseBits;
                while ((responseBits = bufferedReader.readLine()) != null) {
                    stringBuilder.append(responseBits);
                }
                String responseData = stringBuilder.toString();
//                LoggerDDF.e(TAG,responseData+"@73");
//                return responseData;
                ConstantsApi.apiResponseData = responseData;
//                LoggerDDF.e(TAG, ConstantsApi.apiResponseData+"");
                LoggerDDF.e(TAG, "apiResponseData");

            } else {
//                Toast.makeText(mContext, "Error Code : " + statusCode, Toast.LENGTH_SHORT).show();
                LoggerDDF.e(TAG, "Error Code : " + statusCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LoggerDDF.e(TAG, e.getMessage() + " @85");
            LoggerDDF.e(TAG, e + " @86");
        }
    }*/

    //
    public void httpUrlCallS(Context context) {
        mContext = context;
        LoggerDDF.e(TAG, "HTTP httpUrlCalls");
        LoggerDDF.e(TAG, ConstantsApi.API_GET_POST);
        try {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
//            String strUrl = ConstantsApi.BASE_URL + ConstantsApi.CONTACTS_URL;
            String strUrl = ConstantsApi.API_CALL_URL;
//            ConstantsApi.API_GET_POST = "GET";
//            strUrl = "https://api.test.wadzpay.com/pos/merchantDashboard/getConversionRateList?fiatType=USD";
//            ConstantsApi.API_GET_POST = "POST";
//            strUrl = "https://api.dev.wadzpay.com/api/v1/getJwtToken?username=suresh.gandham@wadzpay.com&password=Wadzpay@123";
            LoggerDDF.e(TAG, strUrl + " ##**");
            URL url = new URL(strUrl);

            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod(ConstantsApi.API_GET_POST);   //POST or GET
            LoggerDDF.e(TAG, ConstantsApi.API_GET_POST + " ##**");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
//            urlConnection.setRequestProperty("Authorization", "Bearer");
            urlConnection.setRequestProperty("Authorization", ConstantsApi.alToken.get(0) + "");
//            urlConnection.setDoOutput(true);
//            urlConnection.setDoInput(true);
            if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_CONVERSION)) {
                LoggerDDF.e(TAG, ConstantsApi.FLAG_API_CONVERSION);
//                strUrl = "https://api.test.wadzpay.com/pos/merchantDashboard/getConversionRateList?fiatType=USD";
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("fiatAmount", 4);
                String strJsonObject = jsonObject.toString().replaceAll("\\\\", "").trim();
                urlConnection.setRequestProperty("Content-Length", "" + strJsonObject.getBytes().length);
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(strJsonObject);
                writer.flush();
                writer.close();
                os.close();
                Log.e("strJsonObject", strJsonObject + "");
            } else if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_PAYMENT_INFO)) {
                LoggerDDF.e(TAG, ConstantsApi.FLAG_API_PAYMENT_INFO);
//                strUrl = "https://api.test.wadzpay.com/pos/merchantDashboard/getConversionRateList?fiatType=USD";
                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("posName", ConstantsApi.strPosName);
                jsonObject.put("fiatAmount", 10);
                String strJsonObject = jsonObject.toString().replaceAll("\\\\", "").trim();
                LoggerDDF.e(TAG, strJsonObject + " @123");
// Write Request to output stream to server.
                OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
//                out.write(jsonArray.toString().replaceAll("\\\\", "").trim());
                out.write(strJsonObject);
                out.close();
            } else if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_MEDIA)) {
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < ConstantsApi.alImageNameExtension.size(); i++) {
                    String strUrlPath = ConstantsApi.TEST_BB_URL + ConstantsApi.alImageNameExtension.get(i);
//                    strUrlPath = strUrlPath.replaceAll("\\\\", "").trim();
                    LoggerDDF.e(TAG, "url - " + strUrlPath);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("url", strUrlPath);
                    jsonObject.put("description", "DDF");
                    jsonArray.put(jsonObject);
                }

                LoggerDDF.e(TAG, jsonArray + " @144");
                String strJsonArray = jsonArray.toString().replaceAll("\\\\", "").trim();
                LoggerDDF.e(TAG, strJsonArray + " @123");
// Write Request to output stream to server.
                OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
//                out.write(jsonArray.toString().replaceAll("\\\\", "").trim());
                out.write(strJsonArray);
                out.close();
            } else if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_FIAT_LIST)) {

            } else if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_MERCHANT_DETAILS)) {

            } else if (ConstantsApi.FLAG_API_NAME.equalsIgnoreCase(ConstantsApi.FLAG_API_ADD_POS_MERCHANT)) {
                LoggerDDF.e(TAG, ConstantsApi.FLAG_API_ADD_POS_MERCHANT);

                /*String jsonInputString = "{\"posName\": \"pos-187\"}";
                urlConnection.setRequestProperty("Content-Length", "" + jsonInputString.getBytes().length);
                Log.e("strJsonObject@199", jsonInputString + "");
                try(OutputStream os = urlConnection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                try(BufferedReader br = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    Log.e("response@200",response.toString());
                }*/

                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("posName", ConstantsApi.strPosName);
                jsonObject.put("posName", "0");
                jsonObject.put("posNumber", "0");
                String strJsonObject = jsonObject.toString().replaceAll("\\\\", "").trim();
                LoggerDDF.e(TAG, strJsonObject + " @123");
// Write Request to output stream to server.
                OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
//                out.write(jsonArray.toString().replaceAll("\\\\", "").trim());
                out.write(strJsonObject);
                out.close();

            }

            urlConnection.connect();
            urlConnection.setConnectTimeout(ConstantsApi.API_CONNECTION_TIME_OUT);
            urlConnection.setReadTimeout(ConstantsApi.API_CONNECTION_TIME_OUT);
            // Check the connection status.
            int statusCode = urlConnection.getResponseCode();
            ConstantsApi.API_STATUS_CODE = statusCode;
            LoggerDDF.e(TAG, statusCode + " @136");
            if (statusCode == 200 || statusCode == 201) {
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String responseBits;
                while ((responseBits = bufferedReader.readLine()) != null) {
                    stringBuilder.append(responseBits);
                }
                String responseData = stringBuilder.toString();
                LoggerDDF.e(TAG, responseData + " @****");
//                LoggerDDF.e(TAG, responseData + " @****");
//                return responseData;
                ConstantsApi.apiResponseData = responseData;
//                LoggerDDF.e(TAG, ConstantsApi.apiResponseData+"");
                LoggerDDF.e(TAG, "apiResponseData");
                LoggerDDF.e(TAG + "@206", "" + ConstantsApi.apiResponseData);
                callBackStart();
            } else if (statusCode == 403) {
                LoggerDDF.e(TAG, "403");
                LoggerDDF.e(TAG + "@403", "" + ConstantsApi.apiResponseData);
                Toast.makeText(mContext,"@403 " + ConstantsApi.FLAG_API_MESSAGE, Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(mContext, LoginActivity.class);
//                mContext.startActivity(intent);
//                awsAmplifyOperations = AwsAmplifyOperations.getInstance(mContext);
//                awsAmplifyOperations.amplifySignOut();
//                amplifyOperations();
                callBackFailed("FiatTypeActivity-failed");
            } else {
                String errorMsg = urlConnection.getErrorStream().toString();
                ConstantsApi.FLAG_API_MESSAGE = statusCode + "" + "\n" + errorMsg;
                Toast.makeText(mContext,"@253 " + ConstantsApi.FLAG_API_MESSAGE, Toast.LENGTH_SHORT).show();
                LoggerDDF.e(TAG, "@253 " + errorMsg);
//                awsAmplifyOperations = AwsAmplifyOperations.getInstance(mContext);
//                awsAmplifyOperations.amplifySignOut();
                callBackFailed("FiatTypeActivity-failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LoggerDDF.e(TAG, e.getMessage() + " @185");
            LoggerDDF.e(TAG, e + " @186");
//            Toast.makeText(mContext,"@254 " + ConstantsApi.FLAG_API_MESSAGE, Toast.LENGTH_SHORT).show();
            Toast.makeText(mContext,"@264 " + e.getMessage() +" - "+e.toString(), Toast.LENGTH_SHORT).show();
//            awsAmplifyOperations = AwsAmplifyOperations.getInstance(mContext);
//            awsAmplifyOperations.amplifySignOut();
        }
    }
    private void amplifyOperations() {
        ConstantsApi.API_TEMP_JWT = "";
        AwsAmplifyOperations awsAmplifyOperations = AwsAmplifyOperations.getInstance(mContext);
        awsAmplifyOperations.passContext(mContext);
        awsAmplifyOperations.amplifyInit();
    }
    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private ApiCallback apiCallback;

    public void callBackStart() {
        LoggerDDF.e(TAG, " @callBackStart");
        apiCallback = (ApiCallback) mContext;
        apiCallback.apiSendResponse(ConstantsApi.apiResponseData + "");
    }

    public void callBackFailed(String FailedText) {
        LoggerDDF.e(TAG, " @callBackFailed@293");
        apiCallback = (ApiCallback) mContext;
        apiCallback.apiCallBackFailed(FailedText);
    }
}
