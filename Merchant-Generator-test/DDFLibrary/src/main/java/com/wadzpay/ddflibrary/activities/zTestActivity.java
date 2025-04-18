package com.wadzpay.ddflibrary.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wadzpay.ddflibrary.R;
import com.wadzpay.ddflibrary.api.ApiService;
import com.wadzpay.ddflibrary.api.ConstantsApi;
import com.wadzpay.ddflibrary.dialogs.ToastCustomize;
import com.wadzpay.ddflibrary.logs.LoggerDDF;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;


public class zTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ImageView iv_logo_splash = findViewById(R.id.iv_logo_splash);
        iv_logo_splash.setVisibility(View.VISIBLE);
        iv_logo_splash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                boolean pswd = isValidPassword("Wadzpay@123");
//                ToastCustomize.displayToast(zTestActivity.this,pswd+"");
//                new AsyncTaskExample().execute();
                startConversion();
            }
        });

    }

    private void startConversion() {
        LoggerDDF.e(TAG, "startCurrencyApi");
        ConstantsApi.FLAG_API_NAME = ConstantsApi.FLAG_API_CONVERSION;
        Intent serviceIntent = new Intent(this, ApiService.class);
        startService(serviceIntent);
    }

    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

    @Override
    protected void onResume() {
        super.onResume();
//        new AsyncTaskExample().execute();
    }

    private class AsyncTaskExample extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                startApi6();
            } catch (IOException e) {
                e.printStackTrace();
                LoggerDDF.e(TAG, e + " @58");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

        }
    }

    private String TAG = getClass().getSimpleName();

    public void startApi6() throws IOException {
        StrictMode.ThreadPolicy policy =
                new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ConstantsApi.API_GET_POST = "POST";
        String strUrl = "https://api.dev.wadzpay.com/pos/merchantDashboard/getConversionRateList?fiatType=USD";
        URL url = new URL(strUrl);

        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.setRequestMethod(ConstantsApi.API_GET_POST);   //POST or GET
        LoggerDDF.e(TAG, ConstantsApi.API_GET_POST + " ##**");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestProperty("Content-Language", "en-US");
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Authorization", "Bearer eyJraWQiOiJRZzhXaSsySTVwOUFPMG9MOVNieE9VZEsweHB4K2hVWnBQenN4MUVOOUxjPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJhN2ZlMTRiNC00ZDIwLTQyMTctOGNkNC1mMzNiMzE3NzdkOWEiLCJjb2duaXRvOmdyb3VwcyI6WyJNRVJDSEFOVF9BRE1JTiJdLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLmV1LWNlbnRyYWwtMS5hbWF6b25hd3MuY29tXC9ldS1jZW50cmFsLTFfWUhHQzBBZFN3IiwiY29nbml0bzp1c2VybmFtZSI6ImE3ZmUxNGI0LTRkMjAtNDIxNy04Y2Q0LWYzM2IzMTc3N2Q5YSIsIm9yaWdpbl9qdGkiOiJlZDgxYmEzZC05MTE0LTQ4ZjEtYmNmYS00MGI5YzYxYzcxNDciLCJhdWQiOiIycGdlNm5odWk4ZWRqY2tyNWZ0czVvMGExdSIsImV2ZW50X2lkIjoiMGMwNTg0NDMtN2NiMy00MjA4LThhNzktMGNiOGFmNDJmYTY1IiwidG9rZW5fdXNlIjoiaWQiLCJhdXRoX3RpbWUiOjE2NDgzMDc0NjksInBob25lX251bWJlciI6Iis5MTkzMTkwMTc4MTMiLCJleHAiOjE2NDgzMTEwNjksImlhdCI6MTY0ODMwNzQ2OSwianRpIjoiMDQwMTdkY2EtZDBhNy00YjRkLTliYjUtNmM0NzljYmIxZDAxIiwiZW1haWwiOiJhYmhpbmF2Lm1hdXJ5YUB3YWR6cGF5LmNvbSJ9.COmyeO85O4hJgpqu62L70Dw32aMDGNyqUrWYmfM9utQmluumFsjYzKGIh6w3pc6tD8VqeUITsQwioXnrz7OYHdJV4PfhijN7hUD4muoFvWbYca8v3F5ipAjKWXk6yn8ulgbJAjgyiyPX_Kv4I3MyhD5rJBguDTmh7Ec_lkcuhHbNzJ6Vewf_4cXxFTGp1ZX1uYQCnsd_Ed71k895PJT428cM6m6mYJR0Z-a2dPCtL5I8zw-f9Ia75ZICef8qPbxDZmTJE5CequPN4YxRSLqJhUcNLbIyIpysANgpq6RCc1xbIJ-oIfzoliO3dKMZqFAWfYFlAW28ruQNAfB_AlbnQg");

//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("fiatAmount", "11");
//        String strJsonArray = jsonObject.toString().replaceAll("\\\\", "").trim();
//        String strJsonArray = jsonObject.toString();
        String strJsonArray = "{\"fiatAmount\":\"11\"}";

        OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
//                out.write(jsonArray.toString().replaceAll("\\\\", "").trim());
        out.write(strJsonArray);
        out.close();

        urlConnection.connect();
        urlConnection.setConnectTimeout(ConstantsApi.API_CONNECTION_TIME_OUT);
        urlConnection.setReadTimeout(ConstantsApi.API_CONNECTION_TIME_OUT);

        // Check the connection status.
        int statusCode = urlConnection.getResponseCode();
        LoggerDDF.e(TAG, statusCode + " @100");
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
//                LoggerDDF.e(TAG, responseData + " @****");
            LoggerDDF.e(TAG, responseData + " @****");
        }
    }


    private void testApi() {
        LoggerDDF.e(TAG, "startCurrencyApi");
        ConstantsApi.API_GET_POST = "POST";
//        ConstantsApi.FLAG_API_NAME = ConstantsApi.FLAG_API_CONVERSION;
        Intent serviceIntent = new Intent(this, ApiService.class);
        startService(serviceIntent);
    }

    public void startApi5() throws IOException {
        ConstantsApi.API_GET_POST = "GET";
        StrictMode.ThreadPolicy policy =
                new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String strUrl = "https://api.dev.wadzpay.com/v1/config";
        URL url = new URL(strUrl);

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod(ConstantsApi.API_GET_POST);   //POST or GET
        LoggerDDF.e(TAG, ConstantsApi.API_GET_POST + " ##**");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Authorization", "Bearer eyJraWQiOiJRZzhXaSsySTVwOUFPMG9MOVNieE9VZEsweHB4K2hVWnBQenN4MUVOOUxjPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJhN2ZlMTRiNC00ZDIwLTQyMTctOGNkNC1mMzNiMzE3NzdkOWEiLCJjb2duaXRvOmdyb3VwcyI6WyJNRVJDSEFOVF9BRE1JTiJdLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLmV1LWNlbnRyYWwtMS5hbWF6b25hd3MuY29tXC9ldS1jZW50cmFsLTFfWUhHQzBBZFN3IiwiY29nbml0bzp1c2VybmFtZSI6ImE3ZmUxNGI0LTRkMjAtNDIxNy04Y2Q0LWYzM2IzMTc3N2Q5YSIsIm9yaWdpbl9qdGkiOiJlZDgxYmEzZC05MTE0LTQ4ZjEtYmNmYS00MGI5YzYxYzcxNDciLCJhdWQiOiIycGdlNm5odWk4ZWRqY2tyNWZ0czVvMGExdSIsImV2ZW50X2lkIjoiMGMwNTg0NDMtN2NiMy00MjA4LThhNzktMGNiOGFmNDJmYTY1IiwidG9rZW5fdXNlIjoiaWQiLCJhdXRoX3RpbWUiOjE2NDgzMDc0NjksInBob25lX251bWJlciI6Iis5MTkzMTkwMTc4MTMiLCJleHAiOjE2NDgzMTEwNjksImlhdCI6MTY0ODMwNzQ2OSwianRpIjoiMDQwMTdkY2EtZDBhNy00YjRkLTliYjUtNmM0NzljYmIxZDAxIiwiZW1haWwiOiJhYmhpbmF2Lm1hdXJ5YUB3YWR6cGF5LmNvbSJ9.COmyeO85O4hJgpqu62L70Dw32aMDGNyqUrWYmfM9utQmluumFsjYzKGIh6w3pc6tD8VqeUITsQwioXnrz7OYHdJV4PfhijN7hUD4muoFvWbYca8v3F5ipAjKWXk6yn8ulgbJAjgyiyPX_Kv4I3MyhD5rJBguDTmh7Ec_lkcuhHbNzJ6Vewf_4cXxFTGp1ZX1uYQCnsd_Ed71k895PJT428cM6m6mYJR0Z-a2dPCtL5I8zw-f9Ia75ZICef8qPbxDZmTJE5CequPN4YxRSLqJhUcNLbIyIpysANgpq6RCc1xbIJ-oIfzoliO3dKMZqFAWfYFlAW28ruQNAfB_AlbnQg");

        urlConnection.connect();
        urlConnection.setConnectTimeout(ConstantsApi.API_CONNECTION_TIME_OUT);
        urlConnection.setReadTimeout(ConstantsApi.API_CONNECTION_TIME_OUT);
        // Check the connection status.
        int statusCode = urlConnection.getResponseCode();
        LoggerDDF.e(TAG, statusCode + " @136");
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
//                LoggerDDF.e(TAG, responseData + " @****");
            LoggerDDF.e(TAG, responseData + " @****");
        }
    }

    public void startApi4() throws IOException {
        String urlStr = "https://api.dev.wadzpay.com/pos/merchantDashboard/getConversionRateList?fiatType=AED";
        String jsonBodyStr = "{\"fiatAmount\":\"11\"}";
        URL url = new URL(urlStr);
        HttpsURLConnection httpURLConnection = (HttpsURLConnection) url.openConnection();
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);

        httpURLConnection.setConnectTimeout(10000);
        httpURLConnection.setReadTimeout(10000);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("Authorization", "Bearer eyJraWQiOiJRZzhXaSsySTVwOUFPMG9MOVNieE9VZEsweHB4K2hVWnBQenN4MUVOOUxjPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJhN2ZlMTRiNC00ZDIwLTQyMTctOGNkNC1mMzNiMzE3NzdkOWEiLCJjb2duaXRvOmdyb3VwcyI6WyJNRVJDSEFOVF9BRE1JTiJdLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLmV1LWNlbnRyYWwtMS5hbWF6b25hd3MuY29tXC9ldS1jZW50cmFsLTFfWUhHQzBBZFN3IiwiY29nbml0bzp1c2VybmFtZSI6ImE3ZmUxNGI0LTRkMjAtNDIxNy04Y2Q0LWYzM2IzMTc3N2Q5YSIsIm9yaWdpbl9qdGkiOiJjMGJlYzkzMS1lNjM0LTQyODMtYTAyZS1hMGQ2ZTI0NjE2MjIiLCJhdWQiOiIycGdlNm5odWk4ZWRqY2tyNWZ0czVvMGExdSIsImV2ZW50X2lkIjoiMWEwMzIxZTAtYjVhZS00N2IzLWE2NzEtZjM4ZGQ3ZGFkZTZmIiwidG9rZW5fdXNlIjoiaWQiLCJhdXRoX3RpbWUiOjE2NDgzMDMzNTgsInBob25lX251bWJlciI6Iis5MTkzMTkwMTc4MTMiLCJleHAiOjE2NDgzMDY5NTgsImlhdCI6MTY0ODMwMzM1OCwianRpIjoiOWY0NGVkMTctZmRhNC00YzI5LTlkZmQtOGY0NmViOWE2NGVhIiwiZW1haWwiOiJhYmhpbmF2Lm1hdXJ5YUB3YWR6cGF5LmNvbSJ9.ZuPs9Jx1qaVjG8gqpvDxkQyoJFA_oGUOZk6rFqt8aXLFuZE9pK1QD1w5u-gnqxThppKx_hphZc_QhkkmosfECqWQkOymkIj4aAAZiivLtwSBT0oJH444sZGJPeuGn2ByouDRY3JfmVdd1Jw8kRstZ5ldU9s_BlDjr95O5TOqYwFLbaL_VCQ6x2kRPZRQodOtLOkiomSvXLQLhgfz6EzoLg0BjH6pcJGXBuZ7j3BpG-88Knj44gXFoORwt1HRA6RJIHbIvZ3FhuSQejPunOF4wwEuHaBj3Aqyy01rieejgjuf06uZzwuE8jHmVoWNFo8Nc8_dq7HaPKl4IqM5kxiIfQ");

        /*try (OutputStream outputStream = httpURLConnection.getOutputStream()) {
            outputStream.write(jsonBodyStr.getBytes());
            outputStream.flush();
        }*/
        DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
//        dos.writeChars("param1=value1&param2=value2");
        dos.writeChars(jsonBodyStr);
        dos.flush();
        dos.close();

        Log.e("test", httpURLConnection.getResponseCode() + " - " + httpURLConnection.getResponseMessage());

        if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    // ... do something with line
                }
            }
        } else {
            // ... do something with unsuccessful response
        }
    }

    private void startApi3() throws Exception {
        StrictMode.ThreadPolicy policy =
                new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        URL url = new URL("https://api.dev.wadzpay.com/pos/merchantDashboard/getConversionRateList?fiatType=AED");
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setConnectTimeout(10000);
        http.setReadTimeout(10000);
//        http.setDoInput(true);
        http.setRequestProperty("Accept", "application/json");
        http.setRequestProperty("Content-Type", "application/json");
        http.setRequestProperty("Content-Language", "en-US");
        http.setRequestProperty("Authorization", "Bearer eyJraWQiOiJRZzhXaSsySTVwOUFPMG9MOVNieE9VZEsweHB4K2hVWnBQenN4MUVOOUxjPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJhN2ZlMTRiNC00ZDIwLTQyMTctOGNkNC1mMzNiMzE3NzdkOWEiLCJjb2duaXRvOmdyb3VwcyI6WyJNRVJDSEFOVF9BRE1JTiJdLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLmV1LWNlbnRyYWwtMS5hbWF6b25hd3MuY29tXC9ldS1jZW50cmFsLTFfWUhHQzBBZFN3IiwiY29nbml0bzp1c2VybmFtZSI6ImE3ZmUxNGI0LTRkMjAtNDIxNy04Y2Q0LWYzM2IzMTc3N2Q5YSIsIm9yaWdpbl9qdGkiOiJlZDgxYmEzZC05MTE0LTQ4ZjEtYmNmYS00MGI5YzYxYzcxNDciLCJhdWQiOiIycGdlNm5odWk4ZWRqY2tyNWZ0czVvMGExdSIsImV2ZW50X2lkIjoiMGMwNTg0NDMtN2NiMy00MjA4LThhNzktMGNiOGFmNDJmYTY1IiwidG9rZW5fdXNlIjoiaWQiLCJhdXRoX3RpbWUiOjE2NDgzMDc0NjksInBob25lX251bWJlciI6Iis5MTkzMTkwMTc4MTMiLCJleHAiOjE2NDgzMTEwNjksImlhdCI6MTY0ODMwNzQ2OSwianRpIjoiMDQwMTdkY2EtZDBhNy00YjRkLTliYjUtNmM0NzljYmIxZDAxIiwiZW1haWwiOiJhYmhpbmF2Lm1hdXJ5YUB3YWR6cGF5LmNvbSJ9.COmyeO85O4hJgpqu62L70Dw32aMDGNyqUrWYmfM9utQmluumFsjYzKGIh6w3pc6tD8VqeUITsQwioXnrz7OYHdJV4PfhijN7hUD4muoFvWbYca8v3F5ipAjKWXk6yn8ulgbJAjgyiyPX_Kv4I3MyhD5rJBguDTmh7Ec_lkcuhHbNzJ6Vewf_4cXxFTGp1ZX1uYQCnsd_Ed71k895PJT428cM6m6mYJR0Z-a2dPCtL5I8zw-f9Ia75ZICef8qPbxDZmTJE5CequPN4YxRSLqJhUcNLbIyIpysANgpq6RCc1xbIJ-oIfzoliO3dKMZqFAWfYFlAW28ruQNAfB_AlbnQg");
        http.setUseCaches(false);

//        http.setInstanceFollowRedirects(false);
//        http.setAllowUserInteraction(false);
//        http.setRequestProperty("Content-Length","20");

        String data = "{\"fiatAmount\":\"11\"}";


//        byte[] out = data.getBytes(StandardCharsets.UTF_8);

//        OutputStream stream = http.getOutputStream();
//        stream.write(out);

        DataOutputStream dos = new DataOutputStream(http.getOutputStream());
//        dos.writeChars("param1=value1&param2=value2");
        dos.writeChars(data);
        dos.flush();
        dos.close();

        /*BufferedReader in = new BufferedReader(
                new InputStreamReader(http.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();*/

//        OutputStreamWriter out1 = new OutputStreamWriter(http.getOutputStream());
//        out1.write(out);
//        out1.close();

        Log.e("test", http.getResponseCode() + " - " + http.getResponseMessage());

        if (http.getResponseCode() == 200) {
            InputStream inputStream = new BufferedInputStream(http.getInputStream());
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String responseBits;
            while ((responseBits = bufferedReader.readLine()) != null) {
                stringBuilder.append(responseBits);
            }
            String responseData = stringBuilder.toString();
//                LoggerDDF.e(TAG, responseData + " @****");
            LoggerDDF.e("response", responseData + " @****");
        }
        http.disconnect();
    }

    private void startApi2() throws Exception {
        URL url = new URL("https://reqbin.com/echo/post/json");
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setRequestProperty("Accept", "application/json");
        http.setRequestProperty("Authorization", "Bearer {token}");
        http.setRequestProperty("Content-Type", "application/json");
        /*HttpConnection connnection;
        connnection.requestProperties.put("Content-type", contentType);
        connnection.setRequestBody(body);*/

        String data = "{\n	\"employee\":{ \"name\":\"Emma\", \"age\":28, \"city\":\"Boston\" }\n} ";

//        RequestBody requestBody = RequestBody.create(data) ;
        byte[] out = data.getBytes(StandardCharsets.UTF_8);

        OutputStream stream = http.getOutputStream();
        stream.write(out);

        Log.e("test", http.getResponseCode() + " - " + http.getResponseMessage());

        if (http.getResponseCode() == 200) {
            InputStream inputStream = new BufferedInputStream(http.getInputStream());
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String responseBits;
            while ((responseBits = bufferedReader.readLine()) != null) {
                stringBuilder.append(responseBits);
            }
            String responseData = stringBuilder.toString();
//                LoggerDDF.e(TAG, responseData + " @****");
            LoggerDDF.e("response", responseData + " @****");
        }
        http.disconnect();
    }


    private void startApi() throws Exception {
        URL url = new URL("http://example-url");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestProperty("Authorization", "Bearer " + " Actual bearer token issued by provider.");
        //e.g. bearer token= eyJhbGciOiXXXzUxMiJ9.eyJzdWIiOiPyc2hhcm1hQHBsdW1zbGljZS5jb206OjE6OjkwIiwiZXhwIjoxNTM3MzQyNTIxLCJpYXQiOjE1MzY3Mzc3MjF9.O33zP2l_0eDNfcqSQz29jUGJC-_THYsXllrmkFnk85dNRbAw66dyEKBP5dVcFUuNTA8zhA83kk3Y41_qZYx43T

        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestMethod("POST");


        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String output;

        StringBuffer response = new StringBuffer();
        while ((output = in.readLine()) != null) {
            response.append(output);
        }

        in.close();
        // printing result from response
        System.out.println("Response:-" + response.toString());
    }
}
