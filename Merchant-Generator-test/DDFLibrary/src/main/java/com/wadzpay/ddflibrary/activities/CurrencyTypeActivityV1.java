package com.wadzpay.ddflibrary.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.wadzpay.ddflibrary.R;
import com.wadzpay.ddflibrary.api.ApiService;
import com.wadzpay.ddflibrary.api.ConstantsApi;
import com.wadzpay.ddflibrary.awsamplify.AwsAmplifyOperations;
import com.wadzpay.ddflibrary.callbacks.ActivityCallBack;
import com.wadzpay.ddflibrary.dialogs.ConstantsDialog;
import com.wadzpay.ddflibrary.dialogs.DialogCustomize;
import com.wadzpay.ddflibrary.dialogs.ToastCustomize;
import com.wadzpay.ddflibrary.logs.LoggerDDF;
import com.wadzpay.ddflibrary.utils.ConstantsActivity;

import java.util.Collections;

public class CurrencyTypeActivityV1 extends AppCompatActivity implements View.OnClickListener, ActivityCallBack {
    private String TAG = getClass().getSimpleName();
    public static CurrencyTypeActivityV1 currencyTypeActivity;
    AwsAmplifyOperations awsAmplifyOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fiat_type);
//        ConstantsApi.API_TEMP_JWT = "";
        if (ConstantsApi.alDigitalCurrencies.size() > 0) {
            initUI();
        } else {
            fiatApiCall();
        }
    }

    DialogCustomize dialogCustomize;

    private void fiatApiCall() {
        LoggerDDF.e(TAG, "startService");
        ConstantsApi.mActivity = this;
        currencyTypeActivity = this;
        ConstantsApi.alDigitalCurrencies.clear();
        ConstantsActivity.STR_CURRENCY_CODE = "";
        dialogCustomize = DialogCustomize.getInstance();
        dialogCustomize.displayProgressDialog(this, ConstantsDialog.FLAG_DIALOG_DISPLAY);
        ConstantsApi.FLAG_API_ACTIVITY_FAILED = "CurrencyTypeActivity";
        ConstantsApi.FLAG_API_NAME = ConstantsApi.FLAG_API_CURRENCY_LIST;
        Intent serviceIntent = new Intent(this, ApiService.class);
//        serviceIntent.putExtra("activity", (Parcelable) this);
//        serviceIntent.putExtra("activity", (Serializable) this);
        startService(serviceIntent);
    }

    RadioGroup radioGroupFiat;
    LinearLayout lytFiatSettings;

    private void initUI() {
        if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_PROD)) {
            if (ConstantsApi.alDigitalCurrencies.size() >= 4) {
                ConstantsApi.alDigitalCurrencies.remove(1);
                ConstantsApi.alDigitalCurrencies.remove(0);
            }
//            ConstantsApi.alDigitalCurrencies.remove("BTC");
//            ConstantsApi.alDigitalCurrencies.remove("WTK");
        }

        Log.e(TAG, "initUI");
        lytFiatSettings = findViewById(R.id.lyt_fiat_settings);
        ImageView ivDoneFiat = findViewById(R.id.iv_done_fiat);
        ivDoneFiat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ImageView iv_back_common = findViewById(R.id.iv_back_common);
        iv_back_common.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        RadioGroup.LayoutParams layoutParams =
                new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

//        radioGroupFiat = new RadioGroup(this);
//        radioGroupFiat.setLayoutParams(layoutParams);
        radioGroupFiat = findViewById(R.id.rg_fiat_type);
        /*for (int i = 0; i < ConstantsApi.alDigitalCurrencies.size(); i++) {
            if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_PROD)) {

                if (ConstantsApi.alDigitalCurrencies.get(i).get(ConstantsApi.KEYS_FIAT_PARSE[0]).equals("BTC")) {
                    ConstantsApi.alDigitalCurrencies.remove(i);
                }

                if (ConstantsApi.alDigitalCurrencies.get(i).get(ConstantsApi.KEYS_FIAT_PARSE[0]).equals("WTK")) {
                    ConstantsApi.alDigitalCurrencies.remove(i);
                }

            }
        }*/
        addRadioButtons(ConstantsApi.alDigitalCurrencies.size());
        currencyTypeActivity = this;
        if (dialogCustomize != null) {
            dialogCustomize.displayProgressDialog(this, ConstantsDialog.FLAG_DIALOG_DISMISS);
        }

    }

    public void addRadioButtons(int number) {
        LinearLayout.LayoutParams paramsRadio = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        paramsRadio.setMargins(0, 20, 0, 20);

        LinearLayout.LayoutParams paramsView =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        2, 1.0f);

        radioGroupFiat.setOrientation(LinearLayout.VERTICAL);
        //
        for (int i = 0; i < number; i++) {
            RadioButton radioButtonFiats = new RadioButton(this);
            View viewLine = new View(this);
            viewLine.setLayoutParams(paramsView);
            viewLine.setPadding(0, 2, 0, 2);
            viewLine.setBackgroundColor(Color.GRAY);

            radioButtonFiats.setLayoutParams(paramsRadio);
//            radioButtonFiats.setId(View.generateViewId());
            radioButtonFiats.setId(i);
//            radioButtonFiats.setText("    " + "Radio " + radioButtonFiats.getId());
//            radioButtonFiats.setText("    " + ConstantsApi..get(radioButtonFiats.getId()).get(ConstantsApi.KEYS_FIAT_PARSE[0]));
            if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT == "GEIDEA") {
                if (!ConstantsApi.alDigitalCurrencies.get(i).get(ConstantsApi.KEYS_FIAT_PARSE[0]).equals("SART")) {
                    continue;
                }
            } else{
                if (ConstantsApi.alDigitalCurrencies.get(i).get(ConstantsApi.KEYS_FIAT_PARSE[0]).equals("SART")) {
                    continue;
                }
            }

            String currencyStr = ConstantsApi.alDigitalCurrencies.get(i).get(ConstantsApi.KEYS_FIAT_PARSE[0]);
            if (currencyStr.equalsIgnoreCase("sart")) {
                currencyStr = ConstantsActivity.STR_SAR_CODE;
            }
            radioButtonFiats.setText("    " + currencyStr);
            radioButtonFiats.setTextAppearance(this, android.R.style.TextAppearance_Large);
//            radioButtonFiats.setPadding(10, 0, 0, 10);
            radioButtonFiats.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.text_view_tablet));
            LoggerDDF.e(TAG + "@127", ConstantsApi.alDigitalCurrencies.get(i).get(ConstantsApi.KEYS_FIAT_PARSE[0] + ""));
            radioButtonFiats.setOnClickListener((View.OnClickListener) this);
            radioGroupFiat.addView(radioButtonFiats);
            radioGroupFiat.addView(viewLine);

            /*if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_PROD)) {

                if (ConstantsApi.alDigitalCurrencies.get(i).get(ConstantsApi.KEYS_FIAT_PARSE[0]).equals("BTC")) {
                    radioButtonFiats.setVisibility(View.GONE);
                    viewLine.setVisibility(View.GONE);
                }

                if (ConstantsApi.alDigitalCurrencies.get(i).get(ConstantsApi.KEYS_FIAT_PARSE[0]).equals("WTK")) {
                    radioButtonFiats.setVisibility(View.GONE);
                    viewLine.setVisibility(View.GONE);
                }

            }*/

        }
//        lytFiatSettings.addView(radioGroupFiat);
    }

    @Override
    public void onClick(View v) {
        Log.e(TAG, "" + ((RadioButton) v).getText() + " Id is " + v.getId());
        @SuppressLint("ResourceType") int positionRadio = v.getId();
        ConstantsActivity.STR_CURRENCY_CODE = ConstantsApi.alDigitalCurrencies.get(positionRadio).get(ConstantsApi.KEYS_FIAT_PARSE[0]) + "";
        PaymentRequestDDFActivityK.Companion.setSelectedCurrencyPosition(positionRadio);
//        LoggerDDF.e(TAG,ConstantsApi.alConversions.get(0).size()+"");
        try {
            if (ConstantsApi.alConversions.size() > 0) {
                ConstantsActivity.STR_CURRENCY_AMOUNT = ConstantsApi.alConversions.get(positionRadio).get("totalAmount");
            } else {
                ConstantsActivity.STR_CURRENCY_AMOUNT = "";
            }
        } catch (Exception e) {
            LoggerDDF.e(TAG, e + "");
        }

        finish();
//        ToastCustomize toastCustomize = ToastCustomize.getInstance();
//        toastCustomize.displayToast(this, ConstantsActivity.STR_CURRENCY_FAIT_TYPE);
    }

    @Override
    public void activityCall(String strResponse) {
        Log.e(TAG, "callActivity@currencyactivity@132");
        /*if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_DDF_UAT) || ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_DDF_PROD)) {
        }*/
        Collections.swap(ConstantsApi.alDigitalCurrencies, 4, 3);
        initUI();
    }

    @Override
    public void activityCallFailed(String strResponse) {
        Log.e(TAG, "callBackFailedActivity@currencyactivity@193");
//        ToastCustomize toastCustomize = ToastCustomize.getInstance();
//        toastCustomize.displayToast(this,ConstantsApi.FLAG_API_MESSAGE);
        initUI();
        Log.e(TAG, ConstantsApi.API_STATUS_CODE + "@193");
        if (ConstantsApi.API_STATUS_CODE != 200 || ConstantsApi.API_STATUS_CODE != 201) {
            awsAmplifyOperations = AwsAmplifyOperations.getInstance(this);
            awsAmplifyOperations.amplifySignOut();
            ToastCustomize toastCustomize = ToastCustomize.getInstance();
            toastCustomize.displayToast(this, "Server Error Login Again " + ConstantsApi.API_STATUS_CODE);
        }
        /*dialogCustomize.displayDialog(
                this,
                ConstantsDialog.FLAG_DIALOG_DISPLAY,
                "SERVER ERROR",
                ConstantsApi.API_STATUS_CODE + "\n" + ConstantsApi.FLAG_API_MESSAGE
        );*/

    }
}

