package com.wadzpay.ddflibrary.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.wadzpay.ddflibrary.R;
import com.wadzpay.ddflibrary.awsamplify.AwsAmplifyOperations;
import com.wadzpay.ddflibrary.dialogs.ToastCustomize;
import com.wadzpay.ddflibrary.utils.ConstantsActivity;
import com.wadzpay.ddflibrary.utils.EmailValidations;

public class EmailVerification extends AppCompatActivity {
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);
        initUI();
        mContext = this;
    }

    private void initUI() {

        et_code_verify = findViewById(R.id.et_code_verify);
        et_new_password_forgot = findViewById(R.id.et_new_password_forgot);
        et_confirm_password_forgot = findViewById(R.id.et_confirm_password_forgot);
        et_new_password_forgot.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //  Do Something or Don't
                return true;
            }
        });
        et_confirm_password_forgot.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //  Do Something or Don't
                return true;
            }
        });
        TextView tv_check_code_email = findViewById(R.id.tv_check_code_email);
        tv_check_code_email.setText("Code Sent To " + ConstantsActivity.STR_EMAIL_ID);
        Button btn_submit_verify = findViewById(R.id.btn_submit_verify);
        btn_submit_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyEmailCode();
            }
        });
        TextView tv_resend_email = findViewById(R.id.tv_resend_email);
        tv_resend_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AwsAmplifyOperations awsAmplifyOperations = AwsAmplifyOperations.getInstance(EmailVerification.this);
                awsAmplifyOperations.resendPassword(ConstantsActivity.STR_EMAIL_ID);
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

        et_code_verify.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 5) {
                    enableFields();
                } else if (s.length() < 6) {
                    disableFields();
                }
            }
        });
        disableFields();
    }

    EditText et_code_verify;
    TextInputEditText et_new_password_forgot, et_confirm_password_forgot;

    private void verifyEmailCode() {


        AwsAmplifyOperations awsAmplifyOperations = AwsAmplifyOperations.getInstance(this);
        String strEmailCode = et_code_verify.getText().toString();
        String strNewPassword = et_new_password_forgot.getText().toString();
        String strConfirmPassword = et_confirm_password_forgot.getText().toString();

        if (strEmailCode.length() < 1) {
            ToastCustomize.displayToast(getApplicationContext(), "Please Enter Valid Email Code");
            return;
        }

        if (strNewPassword.length() < 9 || strNewPassword.length() < 9 || strNewPassword.length() > 15 || strNewPassword.length() > 15) {
            ToastCustomize.displayToast(getApplicationContext(), "Please Enter Minimum 8 Characters And Maximum 16 Characters");
            return;
        }
        if (!strNewPassword.equals(strConfirmPassword)) {
            ToastCustomize.displayToast(getApplicationContext(), "New Password And Confirm Password Should Be Same");
            return;
        }
        boolean bValidate = EmailValidations.isValidPassword(strNewPassword);
        if (!bValidate) {
            ToastCustomize.displayToast(this,  "Invalid Password");
            return;
        }
        ConstantsActivity.STR_NEW_PASSWORD = et_confirm_password_forgot.getText().toString();

        ConstantsActivity.STR_EMAIL_CODE = strEmailCode;
        awsAmplifyOperations.confirmPassword(ConstantsActivity.STR_NEW_PASSWORD, strEmailCode);
    }

    private void enableFields() {

        et_new_password_forgot.setEnabled(true);
        et_confirm_password_forgot.setEnabled(true);
        et_new_password_forgot.setBackgroundColor(Color.WHITE);
        et_confirm_password_forgot.setBackgroundColor(Color.WHITE);

    }

    private void disableFields() {

        et_new_password_forgot.setEnabled(false);
        et_confirm_password_forgot.setEnabled(false);
        et_new_password_forgot.setBackgroundColor(Color.LTGRAY);
        et_confirm_password_forgot.setBackgroundColor(Color.LTGRAY);
    }
}
