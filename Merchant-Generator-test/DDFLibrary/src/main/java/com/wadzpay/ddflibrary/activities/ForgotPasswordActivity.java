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
import com.wadzpay.ddflibrary.utils.ConstantsActivity;
import com.wadzpay.ddflibrary.utils.EmailValidations;

public class ForgotPasswordActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        initUI();
        mContext = this;
    }

    EditText et_user_id_forgot;

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
        String strEmailPassword = et_user_id_forgot.getText().toString();
        boolean bValidate = EmailValidations.isValidEmailId(strEmailPassword);
        if (!bValidate) {
            ToastCustomize.displayToast(this, "Invalid Email");
            return;
        }
        /*if (strEmailPassword.length() < 1) {
            ToastCustomize.displayToast(getApplicationContext(), "Please Enter Valid Email");
            return;
        }*/
        ConstantsActivity.STR_EMAIL_ID = et_user_id_forgot.getText().toString();
        awsAmplifyOperations.resetPassword(ConstantsActivity.STR_EMAIL_ID);
    }
}
