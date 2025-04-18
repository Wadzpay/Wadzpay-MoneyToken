package com.wadzpay.ddf;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.wadzpay.ddflibrary.activities.SplashScreenActivity;
import com.wadzpay.ddflibrary.activities.zTestActivity;
import com.wadzpay.ddflibrary.utils.ConstantsActivity;

public class DDFHostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConstantsActivity.FLAG_ENVIRONMENT = ConstantsActivity.FLAG_TEST;
        openLibraryApp();
    }

    private void openLibraryApp() {
        Intent in = new Intent(getApplicationContext(), SplashScreenActivity.class);
//        Intent in = new Intent(getApplicationContext(), zTestActivity.class);
        startActivity(in);
        finish();
    }
}