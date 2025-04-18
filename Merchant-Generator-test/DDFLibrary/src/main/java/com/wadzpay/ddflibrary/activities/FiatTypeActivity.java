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

public class FiatTypeActivity extends AppCompatActivity implements View.OnClickListener, ActivityCallBack {
    private String TAG = getClass().getSimpleName();
    public static FiatTypeActivity fiatTypeActivity;
    AwsAmplifyOperations awsAmplifyOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fiat_type);
        ConstantsActivity.flag_fiat_selected = true;
        if (ConstantsApi.alFiats.size() > 0) {
            initUI();
        } else {
            fiatApiCall();
        }
    }

    DialogCustomize dialogCustomize;

    private void fiatApiCall() {
//        FiatTypeActivity fiatTypeActivity = new FiatTypeActivity();
        LoggerDDF.e(TAG, "startService");
        ConstantsApi.mActivity = this;
        fiatTypeActivity = this;
        ConstantsApi.alFiats.clear();
        dialogCustomize = DialogCustomize.getInstance();
        dialogCustomize.displayProgressDialog(this, ConstantsDialog.FLAG_DIALOG_DISPLAY);
        ConstantsApi.FLAG_API_ACTIVITY_FAILED = "FiatTypeActivity";
        ConstantsApi.FLAG_API_NAME = ConstantsApi.FLAG_API_FIAT_LIST;
        Intent serviceIntent = new Intent(this, ApiService.class);
//        serviceIntent.putExtra("activity", (Parcelable) this);
//        serviceIntent.putExtra("activity", (Serializable) this);
        startService(serviceIntent);
    }

    RadioGroup radioGroupFiat;
    LinearLayout lytFiatSettings;

    private void initUI() {
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
        addRadioButtons(ConstantsApi.alFiats.size());
        fiatTypeActivity = this;
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
            /*if (ConstantsApi.alFiats.get(i).get(ConstantsApi.KEYS_FIAT_PARSE[0]).equals("PHP")) {
                continue;
            }*/
            if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT == "GEIDEA") {
                if (!ConstantsApi.alFiats.get(i).get(ConstantsApi.KEYS_FIAT_PARSE[0]).equals("SAR")) {
                    continue;
                }
            } else{
                if (ConstantsApi.alFiats.get(i).get(ConstantsApi.KEYS_FIAT_PARSE[0]).equals("SAR")) {
                    continue;
                }
            }

            radioButtonFiats.setText("    " + ConstantsApi.alFiats.get(i).get(ConstantsApi.KEYS_FIAT_PARSE[0]));
            radioButtonFiats.setTextAppearance(this, android.R.style.TextAppearance_Large);
//            radioButtonFiats.setPadding(10, 0, 0, 10);
            radioButtonFiats.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.text_view_tablet));
            radioButtonFiats.setOnClickListener((View.OnClickListener) this);
            radioGroupFiat.addView(radioButtonFiats);
            radioGroupFiat.addView(viewLine);

        }
//        lytFiatSettings.addView(radioGroupFiat);
    }

    @Override
    public void onClick(View v) {
        Log.e(TAG, "" + ((RadioButton) v).getText() + " Id is " + v.getId());
        @SuppressLint("ResourceType") int positionRadio = v.getId();
        ConstantsActivity.STR_CURRENCY_FIAT_TYPE = ConstantsApi.alFiats.get(positionRadio).get(ConstantsApi.KEYS_FIAT_PARSE[0]) + "";
        finish();
//        ToastCustomize toastCustomize = ToastCustomize.getInstance();
//        toastCustomize.displayToast(this, ConstantsActivity.STR_CURRENCY_FAIT_TYPE);
    }

    @Override
    public void activityCall(String strResponse) {
        Log.e(TAG, "callActivity@fiatactivity@132");
        initUI();
    }

    @Override
    public void activityCallFailed(String strResponse) {
        Log.e(TAG, "callBackFailedActivity@fiatactivity@138");
//        ToastCustomize toastCustomize = ToastCustomize.getInstance();
//        toastCustomize.displayToast(this,ConstantsApi.FLAG_API_MESSAGE);
        Log.e(TAG, ConstantsApi.API_STATUS_CODE + "@193");
        initUI();
        Log.e(TAG, ConstantsApi.API_STATUS_CODE + "@193");
        if (ConstantsApi.API_STATUS_CODE == 403 || ConstantsApi.API_STATUS_CODE == 503 || ConstantsApi.API_STATUS_CODE == 500) {
            awsAmplifyOperations = AwsAmplifyOperations.getInstance(this);
            awsAmplifyOperations.amplifySignOut();
            ToastCustomize toastCustomize = ToastCustomize.getInstance();
//            toastCustomize.displayToast(this, "Session Expired");
            toastCustomize.displayToast(this, "Server Error Login Again " + ConstantsApi.API_STATUS_CODE);
        }
    }
}
