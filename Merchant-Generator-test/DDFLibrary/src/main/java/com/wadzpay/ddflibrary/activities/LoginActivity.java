package com.wadzpay.ddflibrary.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.wadzpay.ddflibrary.R;
import com.wadzpay.ddflibrary.api.ApiService;
import com.wadzpay.ddflibrary.api.ConstantsApi;
import com.wadzpay.ddflibrary.awsamplify.AwsAmplifyOperations;
import com.wadzpay.ddflibrary.callbacks.ActivityCallBack;
import com.wadzpay.ddflibrary.callbacks.AmplifySendResult;
import com.wadzpay.ddflibrary.dialogs.DialogCustomize;
import com.wadzpay.ddflibrary.dialogs.ToastCustomize;
import com.wadzpay.ddflibrary.logs.LoggerDDF;
import com.wadzpay.ddflibrary.sharedpreference.SharedPreferenceDDF;
import com.wadzpay.ddflibrary.utils.ConstantsActivity;
import com.wadzpay.ddflibrary.utils.TextToVoiceEG;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity implements AmplifySendResult, ActivityCallBack {
    private String TAG = getClass().getSimpleName();
    Context mContext;
    DialogCustomize dialogCustomize;
    TextView tv_forgot_password_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initUI();
        mContext = this;
    }

    private void initUI() {
        AwsAmplifyOperations awsAmplifyOperations = AwsAmplifyOperations.getInstance(this);
        awsAmplifyOperations.passContext(this);
        dialogCustomize = DialogCustomize.getInstance();
        ImageView iv_back_common = findViewById(R.id.iv_back_common);
        iv_back_common.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextInputEditText etUserIdLogin = findViewById(R.id.et_user_id_login);
        TextInputEditText etPasswordLogin = findViewById(R.id.et_password_login);
//        etPasswordLogin.setLongClickable(false);
        etPasswordLogin.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //  Do Something or Don't
                return true;
            }
        });
        /*etPasswordLogin.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
                // TODO Auto-generated method stub

            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode,
                                               MenuItem item) {
                // TODO Auto-generated method stub
                return false;
            }
        });*/

        if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_DEV)) {
//            etUserIdLogin.setText("abhinav.maurya@wadzpay.com");
//            etPasswordLogin.setText("Abhinav@111");
            etUserIdLogin.setText("suresh.gandham@wadzpay.com");
            etPasswordLogin.setText("Wadzpay@123");
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_TEST)) {
//            etUserIdLogin.setText("matt2@awooble.com");
//            etPasswordLogin.setText("HYdyadM3AYDUwz2!");
//            etUserIdLogin.setText("matt555@awooble.com");
//            etPasswordLogin.setText("v6Y3MHXFiBc3vKn!");
//            etUserIdLogin.setText("abhinav.maurya@wadzpay.com");
//            etPasswordLogin.setText("Abhinav@111");
//            etUserIdLogin.setText("b.phanikumar@wadzpay.com");
//            etPasswordLogin.setText("Phanimech@08");
//            etUserIdLogin.setText("ranimurti@icloud.com");
            etUserIdLogin.setText("manoj.ray@wadzpay.com");
//            etUserIdLogin.setText("suresh.gandham@wadzpay.com");
//            etUserIdLogin.setText("saikiran.kondreddy@wadzpay.com");
//            etUserIdLogin.setText("sathya.prakash@wadzpay.com");
//            etUserIdLogin.setText("jayaram.jakkanaboina@wadzpay.com");
            etPasswordLogin.setText("Wadzpay@123");

        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_UAT)) {
            etUserIdLogin.setText("suresh.gandham@wadzpay.com");
            etPasswordLogin.setText("Wadzpay@123");
//            etUserIdLogin.setText("veronica.dang@wadzpay.com");
//            etPasswordLogin.setText("Tester@123");
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_PROD)) {
//            etUserIdLogin.setText("abhinav.maurya@wadzpay.com");
//            etPasswordLogin.setText("Abhinav@111");
            etUserIdLogin.setText("suresh.gandham@wadzpay.com");
            etPasswordLogin.setText("Wadzpay@123");
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_POC)) {
            etUserIdLogin.setText("ddf@gmail.com");
            etPasswordLogin.setText("DDF@wadzpay111");
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_DDF_UAT)) {
            etUserIdLogin.setText("suresh.gandham@wadzpay.com");
            etPasswordLogin.setText("Wadzpay@123");
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_DDF_PROD)) {
            etUserIdLogin.setText("suresh.gandham@wadzpay.com");
            etPasswordLogin.setText("Wadzpay@123");
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_GEIDEA_DEV)) {
            etUserIdLogin.setText("shubham.jain@wadzpay.com");
//            etUserIdLogin.setText("ayush.nirwan@wadzpay.com");
            etPasswordLogin.setText("Wadzpay@123");
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_GEIDEA_TEST)) {
            etUserIdLogin.setText("ayush.nirwan@wadzpay.com");
//            etUserIdLogin.setText("stephen.yeeda@wadzpay.com");
            etPasswordLogin.setText("Wadzpay@123");
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_GEIDEA_UAT)) {
            etUserIdLogin.setText("suresh.gandham@wadzpay.com");
            etPasswordLogin.setText("Wadzpay@123");
        }
        Button btnSubmitLogin = findViewById(R.id.btn_submit_login);
        btnSubmitLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LoggerDDF.e(TAG, "btnSubmitLogin");
                String strUserId = etUserIdLogin.getText().toString();
                String strPassword = etPasswordLogin.getText().toString();

                if (strUserId.length() < 4 || strUserId.length() < 4) {
//                    Toast.makeText(getApplicationContext(), "Please Enter Valid Amount", Toast.LENGTH_SHORT).show();
                    ToastCustomize.displayToast(getApplicationContext(), "Please Enter Valid Credentials");
                    return;
                }
                awsAmplifyOperations.amplifySignIn(strUserId, strPassword);
//                Intent intent = new Intent(mContext, BroadCastReceiverCustom.class);
//                sendBroadcast(intent);
                sharedPrefsSave(ConstantsActivity.STR_KEY_SP_USER_EMAIL, strUserId);
                sharedPrefsSave(ConstantsActivity.STR_KEY_SP_USER_PASSWORD, strPassword);
            }
        });

        ImageView iv_logo_login = findViewById(R.id.iv_logo_login);
        iv_logo_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                awsAmplifyOperations.amplifySignOut();
            }
        });

        tv_forgot_password_login = findViewById(R.id.tv_forgot_password_login);
        tv_forgot_password_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConstantsActivity.STR_PASSWORD_CHANGES = getString(R.string.reset_password);
                Intent i = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                startActivity(i);
            }
        });
    }

    private void sharedPrefsSave(String strKey, String strValue) {
        SharedPreferenceDDF sharedPreferenceDDF = SharedPreferenceDDF.getInstance();
//        sharedPreferenceDDF.putStringValue(this,ConstantsActivity.STR_KEY_SP_USER_EMAIL,strUserId);
        sharedPreferenceDDF.putStringValue(this, strKey, strValue);
    }

    private void startTestApi() {
        LoggerDDF.e(TAG, "startCurrencyApi");
        ConstantsApi.FLAG_API_NAME = ConstantsApi.FLAG_API_CONVERSION;
        Intent serviceIntent = new Intent(this, ApiService.class);
        startService(serviceIntent);
    }

    private void openNextActivity() {
        LoggerDDF.e(TAG, "openNextActivity");
        Intent i = new Intent(LoginActivity.this, PaymentRequestDDFActivityK.class);
//        Intent i = new Intent(LoginActivity.this, ThankYouActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void amplifySend(String strResult) {
        LoggerDDF.e(TAG, strResult);
        openNextActivity();
    }

    @Override
    public void activityCall(String strResponse) {

    }

    @Override
    public void activityCallFailed(String strResponse) {

    }
}
