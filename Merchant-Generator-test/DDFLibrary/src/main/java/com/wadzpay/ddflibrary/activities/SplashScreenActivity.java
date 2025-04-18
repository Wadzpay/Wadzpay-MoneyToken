package com.wadzpay.ddflibrary.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.wadzpay.ddflibrary.R;
import com.wadzpay.ddflibrary.api.ConstantsApi;
import com.wadzpay.ddflibrary.awsamplify.AwsAmplifyOperations;
import com.wadzpay.ddflibrary.callbacks.AmplifySendResult;
import com.wadzpay.ddflibrary.logs.LoggerDDF;
import com.wadzpay.ddflibrary.utils.ConstantsActivity;
import com.wadzpay.ddflibrary.utils.NetworkCheck;


public class SplashScreenActivity extends AppCompatActivity implements AmplifySendResult {
    private String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        imagesHashMap();
        NetworkCheck networkCheck = NetworkCheck.getInstance();
        if (!networkCheck.isNetworkConnected(this)) {
            finish();
        }
        if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_DEV)) {
            ConstantsApi.BASE_URL = ConstantsApi.BASE_URL_DEV;
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_TEST)) {
            ConstantsApi.BASE_URL = ConstantsApi.BASE_URL_TEST;
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_UAT)) {
            ConstantsApi.BASE_URL = ConstantsApi.BASE_URL_UAT;
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_PROD)) {
            ConstantsApi.BASE_URL = ConstantsApi.BASE_URL_PROD;
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_POC)) {
            ConstantsApi.BASE_URL = ConstantsApi.BASE_URL_POC;
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_DDF_UAT)) {
            ConstantsApi.BASE_URL = ConstantsApi.BASE_URL_DDF_UAT;
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_DDF_PROD)) {
            ConstantsApi.BASE_URL = ConstantsApi.BASE_URL_DDF_PROD;
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_GEIDEA_DEV)) {
            ConstantsApi.BASE_URL = ConstantsApi.BASE_URL_GEIDEA_DEV;
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_GEIDEA_TEST)) {
            ConstantsApi.BASE_URL = ConstantsApi.BASE_URL_GEIDEA_TEST;
        } else if (ConstantsActivity.FLAG_ENVIRONMENT.equalsIgnoreCase(ConstantsActivity.FLAG_GEIDEA_UAT)) {
            ConstantsApi.BASE_URL = ConstantsApi.BASE_URL_GEIDEA_UAT;
        }
        LoggerDDF.e(TAG,ConstantsActivity.FLAG_ENVIRONMENT+" @38");
        LoggerDDF.e(TAG,ConstantsApi.BASE_URL+"  @38");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // This method will be executed once the timer is over
                amplifyOperations();
            }
        }, 1000);
    }

    private void amplifyOperations() {
        AwsAmplifyOperations awsAmplifyOperations = AwsAmplifyOperations.getInstance(this);
        awsAmplifyOperations.passContext(this);
        awsAmplifyOperations.amplifyInit();
    }

    //
    int imgCurrency;

    private void imagesHashMap() {
        try {
            ConstantsActivity.hmCryptoImages.clear();
            ConstantsActivity.hmCryptoImages.put("BNB", R.drawable.ic_currency_bnb);
            ConstantsActivity.hmCryptoImages.put("BTC", R.drawable.ic_btc);
            ConstantsActivity.hmCryptoImages.put("ETH", R.drawable.ic_eth);
            ConstantsActivity.hmCryptoImages.put("USD", R.drawable.ic_currency_usd);
            ConstantsActivity.hmCryptoImages.put("USDT", R.drawable.ic_usdt);
            ConstantsActivity.hmCryptoImages.put("USDC", R.drawable.ic_usdc);
            ConstantsActivity.hmCryptoImages.put("XRP", R.drawable.ic_currency_xrp);
            ConstantsActivity.hmCryptoImages.put("WTK", R.drawable.ic_wtk);
            ConstantsActivity.hmCryptoImages.put("SART", R.drawable.ic_currency_sart);
            ConstantsActivity.hmCryptoImages.put("USDCA", R.drawable.ic_usdc);
            ConstantsActivity.hmCryptoImages.put("TRX", R.drawable.ic_trx);
            ConstantsActivity.hmCryptoImages.put("TRXUSDC", R.drawable.ic_trx);
            ConstantsActivity.hmCryptoImages.put("TRXUSDT", R.drawable.ic_trx);
            ConstantsActivity.hmCryptoImages.put("NA", R.drawable.ic_image_not_found);

            imgCurrency = ConstantsActivity.hmCryptoImages.get("bbb");
            LoggerDDF.e(TAG, imgCurrency + "");

        } catch (Exception e) {
            imgCurrency = ConstantsActivity.hmCryptoImages.get("NA");
            LoggerDDF.e(TAG, e + " @88");
        } finally {
//            ImageView iv_logo_splash = findViewById(R.id.iv_logo_splash);
//            iv_logo_splash.setImageResource(imgCurrency);

        }

    }

    @Override
    public void amplifySend(String strResult) {
        LoggerDDF.e(TAG, strResult);
        if (strResult.equalsIgnoreCase(ConstantsActivity.FLAG_AMPLIFY_PAY)) {
            LoggerDDF.e(TAG, "PaymentRequestDDFActivityK");
//            openNextActivity(zKotlinActivity.class);
//            openNextActivity(zTestActivity.class);
            openNextActivity(PaymentRequestDDFActivityK.class);
//            openNextActivity(ThankYouActivity.class);
        } else {
            LoggerDDF.e(TAG, "LoginActivity");
            openNextActivity(WelcomeActivity.class);
        }
    }

    private void openNextActivity(Class intentScreen) {
        Intent i = new Intent(SplashScreenActivity.this, intentScreen);
        startActivity(i);
        finish();
    }
}
