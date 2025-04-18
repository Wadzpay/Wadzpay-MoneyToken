package com.wadzpay.ddflibrary.activities;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.wadzpay.ddflibrary.R;
import com.wadzpay.ddflibrary.awsamplify.AwsAmplifyOperations;
import com.wadzpay.ddflibrary.dialogs.ToastCustomize;
import com.wadzpay.ddflibrary.logs.LoggerDDF;
import com.wadzpay.ddflibrary.sharedpreference.SharedPreferenceDDF;
import com.wadzpay.ddflibrary.utils.ConstantsActivity;

public class ChangePasswordActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        initUI();
        mContext = this;
    }

    EditText et_user_id_forgot;
    TextView tv_email_change_password;
    private void initUI() {
        try {

//        awsAmplifyOperations.passContext(this);
            et_user_id_forgot = findViewById(R.id.et_user_id_forgot);
//            et_user_id_forgot.setText("suresh.gandham@wadzpay.com");
//            et_user_id_forgot.setText("suresh.gandham@wadzpay.com");
            TextView tv_version = findViewById(R.id.tv_version);
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = pInfo.versionName;
//            String buildNo = pInfo.;
            tv_version.setText(ConstantsActivity.FLAG_ENVIRONMENT + " " + versionName);
//        et_user_id_forgot.setText("suresh.gandham@wadzpay.com");
//        et_new_password_forgot.setText("Naresh@123");
//        et_confirm_password_forgot.setText("Naresh@123");
            SharedPreferenceDDF sharedPreferenceDDF = SharedPreferenceDDF.getInstance();
            tv_email_change_password = findViewById(R.id.tv_email_change_password);
            tv_email_change_password.setText(sharedPreferenceDDF.getStringValue(this, ConstantsActivity.STR_KEY_SP_USER_EMAIL));

            Button btn_submit_forgot = findViewById(R.id.btn_submit_forgot);
            btn_submit_forgot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    emailUpdate();
                }
            });
//
            ImageView iv_back_common = findViewById(R.id.iv_back_common);
            iv_back_common.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

            TextView tv_title_forgot = findViewById(R.id.tv_title_forgot);
            tv_title_forgot.setText(ConstantsActivity.STR_PASSWORD_CHANGES);
        } catch (Exception e) {
            LoggerDDF.e(TAG, e + "");
        }
    }

    private void callUserApi() {

    }

    private void emailUpdate() {
        AwsAmplifyOperations awsAmplifyOperations = AwsAmplifyOperations.getInstance(this);
        String strEmailPassword = tv_email_change_password.getText().toString();
        if (strEmailPassword.length() < 1) {
            ToastCustomize.displayToast(getApplicationContext(), "Please Enter Valid Email");
            return;
        }
        ConstantsActivity.STR_EMAIL_ID = tv_email_change_password.getText().toString();
        awsAmplifyOperations.resetPassword(ConstantsActivity.STR_EMAIL_ID);
    }

}

