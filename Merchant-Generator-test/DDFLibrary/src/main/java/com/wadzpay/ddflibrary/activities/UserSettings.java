package com.wadzpay.ddflibrary.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.core.Amplify;
import com.wadzpay.ddflibrary.R;
import com.wadzpay.ddflibrary.api.ConstantsApi;
import com.wadzpay.ddflibrary.awsamplify.AwsAmplifyOperations;
import com.wadzpay.ddflibrary.callbacks.ActivityCallBack;
import com.wadzpay.ddflibrary.dialogs.ConstantsDialog;
import com.wadzpay.ddflibrary.dialogs.DialogCustomize;
import com.wadzpay.ddflibrary.sharedpreference.SharedPreferenceDDF;
import com.wadzpay.ddflibrary.utils.ConstantsActivity;

public class UserSettings extends AppCompatActivity {

    private String TAG = getClass().getSimpleName();
    AwsAmplifyOperations awsAmplifyOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setttings);

        awsAmplifyOperations = AwsAmplifyOperations.getInstance(this);

        ImageView iv_back_common = findViewById(R.id.iv_back_common);
        iv_back_common.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ImageView iv_sign_out_req_pay = findViewById(R.id.iv_sign_out_req_pay);
        iv_sign_out_req_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                awsAmplifyOperations.amplifySignOut();
            }
        });

        TextView tv_sign_out_user_set = findViewById(R.id.tv_sign_out_user_set);
        tv_sign_out_user_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                awsAmplifyOperations.amplifySignOut();
            }
        });

        TextView tv_email_user_set = findViewById(R.id.tv_email_user_set);
        TextView tv_phone_user_set = findViewById(R.id.tv_phone_user_set);
//        tv_email_user_set.setText(ConstantsApi.strMDEmail);
//        tv_email_user_set.setText(Amplify.Auth.getCurrentUser().getUsername()+"");
//        tv_email_user_set.setText(ConstantsActivity.STR_USER_ID);
        SharedPreferenceDDF sharedPreferenceDDF = SharedPreferenceDDF.getInstance();

        tv_email_user_set.setText(sharedPreferenceDDF.getStringValue(this,ConstantsActivity.STR_KEY_SP_USER_EMAIL));
        tv_phone_user_set.setText(ConstantsApi.strMDPhone);

        TextView tv_change_password = findViewById(R.id.tv_change_password);
        tv_change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConstantsActivity.STR_PASSWORD_CHANGES = getString(R.string.change_password);
                Intent i = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                startActivity(i);
            }
        });

        TextView tv_delete_account = findViewById(R.id.tv_delete_account);
        tv_delete_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button btn_static_qr = findViewById(R.id.btn_static_qr);
        if(ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT.equalsIgnoreCase("DDF")){
            btn_static_qr.setVisibility(View.GONE);
        }
        btn_static_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), QRGeneratorActivityStaticQR.class);
                startActivity(i);
                finish();
            }
        });
    }
}
