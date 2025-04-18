package com.wadzpay.ddflibrary.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.core.Amplify;
import com.wadzpay.ddflibrary.R;
import com.wadzpay.ddflibrary.logs.LoggerDDF;
import com.wadzpay.ddflibrary.utils.ConstantsActivity;

public class HomeActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initUI();
    }

    Button btnFiatHome;
    private void initUI() {
        Button btn_payment_home = findViewById(R.id.btn_payment_home);
        btnFiatHome = findViewById(R.id.btn_fiat_home);
        Button btn_sign_out_home = findViewById(R.id.btn_sign_out_home);

        btn_payment_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNextActivity();
            }
        });

        btnFiatHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFiatActivity();
            }
        });

        btn_sign_out_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amplifySignOut();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        btnFiatHome.setText("Fiat Type - "+ ConstantsActivity.STR_CURRENCY_FIAT_TYPE);
    }

    private void openNextActivity() {
        Intent i = new Intent(HomeActivity.this, PaymentRequestDDFActivityK.class);
        startActivity(i);
    }

    private void openFiatActivity() {
        Intent i = new Intent(HomeActivity.this, FiatTypeActivity.class);
        startActivity(i);
    }
    private void logOutActivity() {
        Intent i = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    //
    private void amplifySignOut() {
        LoggerDDF.e(TAG, "signOut");
        Amplify.Auth.signOut(
//                () -> Log.e("AuthQuickstart", "Signed out successfully"),
                () -> logOutActivity(),
                error -> Log.e("AuthQuickstart", error.toString())
        );
        /*
        Amplify.Auth.signOut(new Action() {
            @Override
            public void call() {
                LoggerDDF.e(TAG,"call");
            }
        }, new Consumer<AuthException>() {
            @Override
            public void accept(@NonNull AuthException value) {
                LoggerDDF.e(TAG,"accept");
            }
        });
        */
    }
}
