package com.wadzpay.ddflibrary.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wadzpay.ddflibrary.R;
import com.wadzpay.ddflibrary.api.ApiService;
import com.wadzpay.ddflibrary.api.ConstantsApi;
import com.wadzpay.ddflibrary.apihttp.HttpRequestConstants;
import com.wadzpay.ddflibrary.apihttp.HttpRequestTask;
import com.wadzpay.ddflibrary.apihttp.HttpResponse;
import com.wadzpay.ddflibrary.logs.LoggerDDF;
import com.wadzpay.ddflibrary.spinner.SpinnerCurrencyAdapter;
import com.wadzpay.ddflibrary.spinner.SpinnerCurrencyItem;
import com.wadzpay.ddflibrary.utils.ConstantsActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class zReqJavaActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_request_ddf);
        initUI();
    }

    EditText etBtcReqPay;

    private void initUI() {

        RelativeLayout rlConversionsOptionsReqPay = findViewById(R.id.rl_conversions_options_req_pay);
        RelativeLayout rlBottomReqPay = findViewById(R.id.rl_bottom_req_pay);
        etBtcReqPay = findViewById(R.id.et_btc_req_pay);
        ImageView iv_back_common = findViewById(R.id.iv_back_common);
        iv_back_common.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        Button btnConversionsReqPay = findViewById(R.id.btn_conversions_req_pay);
        btnConversionsReqPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConstantsActivity.STR_CURRENCY_ET = etBtcReqPay.getText().toString();
                Log.e(TAG, ConstantsActivity.STR_CURRENCY_ET + "");
                Log.e(TAG, ConstantsActivity.STR_CURRENCY_ET.length() + "");
                if (ConstantsActivity.STR_CURRENCY_ET == null || ConstantsActivity.STR_CURRENCY_ET.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Please Enter Valid Amount", Toast.LENGTH_SHORT).show();
                    return;
                }
                double dAmount = Double.parseDouble(ConstantsActivity.STR_CURRENCY_ET);
                if (dAmount == 0) {
                    Toast.makeText(getApplicationContext(), "Please Enter Valid Amount", Toast.LENGTH_SHORT).show();
                    return;
                }
                rlConversionsOptionsReqPay.setVisibility(View.VISIBLE);
                rlBottomReqPay.setVisibility(View.VISIBLE);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(btnConversionsReqPay.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
//                startCurrencyApi();
//                callMyApi();
//                Thread t1 = new Thread(new RunnableImpl());
//                t1.start();
                new AsyncTaskExample().execute();
//                sampleHttp();
//                callCurrencyHttp();
//                postRequest1();
                /*try {
                    httpCalls2();
                } catch (IOException e) {
                    Log.e(TAG, e + "");
                    e.printStackTrace();
                }*/
            }
        });

        RelativeLayout rlProceedReqPay = findViewById(R.id.rl_proceed_req_pay);
//        rlProceedReqPay.setBackgroundResource(R.drawable.btn_corners_gray);
        rlProceedReqPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(zReqJavaActivity.this, PaymentConfirmationDDFActivity.class);
                startActivity(i);
            }
        });

        spinnerOperation();
    }

    public static int selectedPosition = 0;
    private ArrayList<SpinnerCurrencyItem> mCurrencyList;
    private SpinnerCurrencyAdapter mCurrencyAdapter;
    //    private String strCurrencyCode;
//    private String strCurrencyName;
//    private String strCurrencyAmount;
    private int intCurrencyImage;

    private void spinnerOperation() {
//        initList();
        ImageView ivCurrencyCodeImgReqPay = findViewById(R.id.iv_currency_code_img_req_pay);
        TextView tvCurrencyCodeReqPay = findViewById(R.id.tv_currency_code_req_pay);
        TextView tvCurrencyNameReqPay = findViewById(R.id.tv_currency_name_req_pay);
        TextView tvAmountReqPay = findViewById(R.id.tv_amount_req_pay);
        TextView tvCurrencyCodeModeReqPay = findViewById(R.id.tv_currency_code_mode_req_pay);
        TextView tvCurrencyAmountModeReqPay = findViewById(R.id.tv_currency_amount_mode_req_pay);

        Spinner spinnerCurrencies = findViewById(R.id.spinner_currencies);
        mCurrencyAdapter = new SpinnerCurrencyAdapter(this, mCurrencyList);
        spinnerCurrencies.setAdapter(mCurrencyAdapter);
        spinnerCurrencies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition = position;
                SpinnerCurrencyItem clickedItem = (SpinnerCurrencyItem) parent.getItemAtPosition(position);
                String clickedCurrencyCode = clickedItem.getCurrencyCode();
                String clickedCurrencyName = clickedItem.getCurrencyName();
                String clickedCurrencyAmount = clickedItem.getCurrencyAmount();
                int clickedCurrencyImage = clickedItem.getFlagImage();
//                Toast.makeText(PaymentRequest.this, clickedCurrencyCode + " selected", Toast.LENGTH_SHORT).show();
                ConstantsActivity.STR_CURRENCY_CODE = clickedCurrencyCode;
                ConstantsActivity.STR_CURRENCY_NAME = clickedCurrencyName;
                ConstantsActivity.STR_CURRENCY_AMOUNT = clickedCurrencyAmount;
                intCurrencyImage = clickedCurrencyImage;
                tvCurrencyCodeReqPay.setText(ConstantsActivity.STR_CURRENCY_CODE);
                tvCurrencyNameReqPay.setText(ConstantsActivity.STR_CURRENCY_NAME);
                tvAmountReqPay.setText(ConstantsActivity.STR_CURRENCY_AMOUNT);
                ivCurrencyCodeImgReqPay.setImageResource(intCurrencyImage);

                tvCurrencyCodeModeReqPay.setText(ConstantsActivity.STR_CURRENCY_CODE);
                tvCurrencyAmountModeReqPay.setText(ConstantsActivity.STR_CURRENCY_AMOUNT);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        RelativeLayout rlSpinnerCurrencyReqPay = findViewById(R.id.rl_spinner_currency_req_pay);
        rlSpinnerCurrencyReqPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerCurrencies.performClick();
            }
        });


        ImageView iv_amount_radio_req_pay = findViewById(R.id.iv_amount_radio_req_pay);
        iv_amount_radio_req_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayAmountDialog();
            }
        });
    }

    /*private void initList() {
        mCurrencyList = new ArrayList<>();
        mCurrencyList.add(new SpinnerCurrencyItem("BTC", "Bitcoin", "0.001", R.drawable.));
        mCurrencyList.add(new SpinnerCurrencyItem("ETH", "Ethereum", "0.002", R.drawable.));
        mCurrencyList.add(new SpinnerCurrencyItem("BNB", "BNB", "0.003", R.drawable.));
        mCurrencyList.add(new SpinnerCurrencyItem("XRP", "Ripple", "0.004", R.drawable.));
        mCurrencyList.add(new SpinnerCurrencyItem("USD", "US Dollar", "0.005", R.drawable.));
        mCurrencyList.add(new SpinnerCurrencyItem("USDT", "US Dollar", "0.006", R.drawable.));
    }*/

    private void displayAmountDialog() {
        AlertDialog.Builder amountDialog = new AlertDialog.Builder(this);
        amountDialog.setTitle("Choose Amount");

// add a list
        String[] arrayAmount = {"100", "200", "300", "400", "500"};
        amountDialog.setItems(arrayAmount, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                etBtcReqPay.setText(arrayAmount[which]);
                etBtcReqPay.setSelection(etBtcReqPay.getText().length());
            }
        });

// create and show the alert dialog
        AlertDialog dialog = amountDialog.create();
        dialog.show();
    }

    private void startCurrencyApi() {
        LoggerDDF.e(TAG, "startCurrencyApi");
        ConstantsApi.FLAG_API_NAME = ConstantsApi.FLAG_API_CONVERSION;
        Intent serviceIntent = new Intent(this, ApiService.class);
        startService(serviceIntent);
    }

    private void getPaymentInfoApi() {
        LoggerDDF.e(TAG, "getConversionsApi");
        ConstantsApi.FLAG_API_NAME = ConstantsApi.FLAG_API_PAYMENT_INFO;
        Intent serviceIntent = new Intent(this, ApiService.class);
        startService(serviceIntent);
    }


    private void callMyApi() {
        LoggerDDF.e(TAG, "callMyApi");
        try {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            URL url = new URL("https://api.dev.wadzpay.com/pos/merchantDashboard/getConversionRateList?fiatType=USD");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
//            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer ");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("fiatAmount", "11");

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
            os.flush();
            os.close();

            Log.e("STATUS", String.valueOf(conn.getResponseCode()));
            Log.e("MSG", conn.getResponseMessage());

            conn.disconnect();
        } catch (Exception e) {

        }
    }

    //
    private void sampleHttp() {
        final String[] text = {""};

        Map requestProperties = new HashMap<>();
        requestProperties.put("Accept-Language", "en-US");

        new HttpRequestTask(
                new HttpRequestConstants("http://httpbin.org/post", HttpRequestConstants.POST, "{ \"post\": \"some-data-æøå\" }", null, requestProperties),
                new HttpRequestConstants.Handler() {
                    @Override
                    public void response(HttpResponse response) {
                        if (response.code == 200) {
                            text[0] += "" + response.body + "\n\n";
//                            textView.setText(text[0]);
                            Log.e("@60", text[0] + "");
                        } else {
                            Log.e("@286", response + "");

                        }
                    }
                }, this).execute();
        Log.e("@64", "@64");
    }

    //
    private void callCurrencyHttp() {
        final String[] text = {""};

        Map requestProperties = new HashMap<>();
        requestProperties.put("Content-Type", "application/json");
        requestProperties.put("Authorization", "Bearer ");

        new HttpRequestTask(
                new HttpRequestConstants("https://api.dev.wadzpay.com/pos/merchantDashboard/getConversionRateList?fiatType=USD", HttpRequestConstants.POST, "{ \"fiatAmount\": \"11\" }", "Bearer ", requestProperties),
                new HttpRequestConstants.Handler() {
                    @Override
                    public void response(HttpResponse response) {
                        if (response.code == 200) {
                            text[0] += "" + response.body + "";
                            Log.e("@60", text[0] + "");
                        } else {
                            Log.e("@286", response + "");

                        }
                    }
                }, this).execute();
        Log.e("@64", "@64");
    }

    public JSONObject postApi2(String REQUEST_URL, Map<String, Object> params) {
        JSONObject jsonObject = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(REQUEST_URL);
            StringBuilder postData = new StringBuilder();

            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//                connection.setRequestProperty("auth1", GPLUS_EmailID);
            connection.setConnectTimeout(8000);
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(8000);
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.getOutputStream().write(postDataBytes);
            connection.connect();
            StringBuilder sb;
            int statusCode = connection.getResponseCode();
            if (statusCode == 200) {
                sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                jsonObject = new JSONObject(sb.toString());
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonObject;
    }

    private class AsyncTaskExample extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @Override
        protected Bitmap doInBackground(String... strings) {
//            postRequest1();
            try {
                httpCalls2();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

        }
    }


    public void postRequest1() {
        try {

            URL url = new URL("https://api.dev.wadzpay.com/pos/merchantDashboard/getConversionRateList?fiatType=USD");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
//            conn.setRequestProperty("","");
            conn.setRequestProperty("Content-Type", "application/json");
//            urlConnection.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer");

            conn.setDoInput(true);
            conn.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("fiatAmount", "11");
            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            conn.connect();
            int statusCode = conn.getResponseCode();

            Log.e("statusCode",statusCode+"");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("e",e+"");
        }
    }

    public static void httpCalls2() throws IOException {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n  \"fiatAmount\": 1.11\n}");
        Request request = new Request.Builder()
                .url("https://api.test.wadzpay.com/pos/merchantDashboard/getConversionRateList?fiatType=USD")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer eyJraWQiOiJQenV2T2Z0NEZpRnZieGJqbFVHZDJQM0dranU4djNHd2dIVHdJaTdCS0ZzPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiI5NmRlMTgyYi0yODc2LTQ1MzctYTMyNC0zYjhhOWNjNWNhYTgiLCJjb2duaXRvOmdyb3VwcyI6WyJNRVJDSEFOVF9BRE1JTiJdLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLmFwLXNvdXRoZWFzdC0xLmFtYXpvbmF3cy5jb21cL2FwLXNvdXRoZWFzdC0xX2ppdHlRSjl5ZSIsImNvZ25pdG86dXNlcm5hbWUiOiI5NmRlMTgyYi0yODc2LTQ1MzctYTMyNC0zYjhhOWNjNWNhYTgiLCJvcmlnaW5fanRpIjoiMTQyY2VhYWQtNDllMC00ZDhhLWIyNDQtMTAxMDFkOWRhNTkwIiwiYXVkIjoicWNha2ttNXZuN3R1cW81dW1kYTRvdHVmMyIsImV2ZW50X2lkIjoiOGExNDJhYzgtM2I0MS00YTBmLTljM2YtMzA5OGFkYWIyMmIwIiwidG9rZW5fdXNlIjoiaWQiLCJhdXRoX3RpbWUiOjE2NDgwMTU3MzUsInBob25lX251bWJlciI6Iis2MTA0MTA5MTAxMjgiLCJleHAiOjE2NDgwMTkzMzUsImlhdCI6MTY0ODAxNTczNSwianRpIjoiNTY5MDk2MTAtZDhiNS00YThjLWE2NTMtZjU3OTI1YTI2NTBiIiwiZW1haWwiOiJtYXR0MkBhd29vYmxlLmNvbSJ9.NUimM1DPMvR8IeevjY9MggCjz-A2-C8xSRTHR1EFBw8BXpNO3WsjX7BM55Vtos5wcAuRHi76fp4o7VmFe8o1Mc0wa31AODms1ePevh4r2fJALXSPskHpIU3vWiBRTYHOWQ-Ou5DLjDZg8_gkmFjnAlz5MSWazMP4XK_8RHo6mEtmd8Yjwj8dYZSf8tO7j0ASUH52ePWcUOQoNDkrpBLYzhAOx8ikUtJrGGefjnHZbTnVwFd7oDo5mZvUM2zMg_yff2NNUwGiWEsFOVTeg8frM6XxnZ0f4nh7yMYmxCIHvW4B53g3hg75WvfwaKRP3G88oJZkTedM-ao91bt3ewkMiA")
                .addHeader("cache-control", "no-cache")
                .addHeader("postman-token", "725f1996-bee6-7b4c-abe4-ed8b153bee71")
                .build();

        Response response = client.newCall(request).execute();

        Log.e("",response+"");
    }
}

