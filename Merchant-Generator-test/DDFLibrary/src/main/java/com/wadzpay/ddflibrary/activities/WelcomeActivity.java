package com.wadzpay.ddflibrary.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.wadzpay.ddflibrary.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        TextView tvSkipWelcome = findViewById(R.id.tv_skip_welcome);
        tvSkipWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNextActivity(LoginActivity.class);
            }
        });
        Button btnLoginWelcome = findViewById(R.id.btn_login_welcome);
        btnLoginWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNextActivity(LoginActivity.class);
            }
        });
    }

    private void openNextActivity(Class intentScreen) {
        Intent i = new Intent(WelcomeActivity.this, intentScreen);
        startActivity(i);
        finish();
    }
}
