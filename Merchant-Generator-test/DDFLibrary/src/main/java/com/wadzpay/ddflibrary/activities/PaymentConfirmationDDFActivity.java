package com.wadzpay.ddflibrary.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.wadzpay.ddflibrary.R;
import com.wadzpay.ddflibrary.api.ApiRetroFitPaymentInfo;
import com.wadzpay.ddflibrary.dialogs.ToastCustomize;
import com.wadzpay.ddflibrary.logs.LoggerDDF;
import com.wadzpay.ddflibrary.utils.ConstantsActivity;


public class PaymentConfirmationDDFActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_confirmation_ddf);
        try {
            initUI();
        } catch (Exception e) {
            LoggerDDF.e(TAG, e + "");
        }
    }

    double dAmount;
    double dFeeAmount;
    double dTotalAmount;
    boolean bDisclaimer = false;

    private void initUI() {
        ApiRetroFitPaymentInfo apiRetroFitPaymentInfo = new ApiRetroFitPaymentInfo();
        RelativeLayout rl_proceed_pay_conf = findViewById(R.id.rl_proceed_pay_conf);
        rl_proceed_pay_conf.setBackgroundResource(R.drawable.btn_corners_gray);
        rl_proceed_pay_conf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bDisclaimer) {
                    ConstantsActivity.STR_CURRENCY_TOTAL_AMOUNT = String.format("%.8f", dTotalAmount);
                    LoggerDDF.e(TAG + "@41", ConstantsActivity.STR_CURRENCY_TOTAL_AMOUNT);
                    apiRetroFitPaymentInfo.paymentInfoRetrofit(PaymentConfirmationDDFActivity.this);
                } else {
                    ToastCustomize toastCustomize = ToastCustomize.getInstance();
                    toastCustomize.displayToast(PaymentConfirmationDDFActivity.this,"Select Disclaimer");
                }
            }
        });

        TextView tv_currency_to_pay_conf = findViewById(R.id.tv_currency_to_pay_conf);
        tv_currency_to_pay_conf.setText("You are making payment of "+ ConstantsActivity.STR_CURRENCY_ET + " AED to DDF using digital currency.");
        TextView tv_currency_code_pay_conf = findViewById(R.id.tv_currency_code_pay_conf);
        TextView tv_currency_amount_pay_conf = findViewById(R.id.tv_currency_amount_pay_conf);
        TextView tv_fee_amount_pay_conf = findViewById(R.id.tv_fee_amount_pay_conf);
        TextView tv_total_amount_pay_conf = findViewById(R.id.tv_total_amount_pay_conf);
        tv_currency_code_pay_conf.setText(ConstantsActivity.STR_CURRENCY_CODE);
//        tv_currency_amount_pay_conf.setText(ConstantsActivity.STR_CURRENCY_AMOUNT);
//        tv_fee_amount_pay_conf.setText(ConstantsActivity.STR_CURRENCY_FEE_AMOUNT);
        dAmount = Double.parseDouble(ConstantsActivity.STR_CURRENCY_AMOUNT);
        dFeeAmount = Double.parseDouble(ConstantsActivity.STR_CURRENCY_FEE_AMOUNT);
        dTotalAmount = dAmount + dFeeAmount;
        tv_currency_amount_pay_conf.setText(String.format("%.8f", dAmount));
        tv_fee_amount_pay_conf.setText(String.format("%.8f", dFeeAmount));

//        LoggerDDF.e(TAG,new BigDecimal(dTotalAmount).toPlainString());
        LoggerDDF.e(TAG, dAmount + "");
        LoggerDDF.e(TAG, String.format("Value of a: %.8f", dAmount));
        LoggerDDF.e(TAG, dFeeAmount + "");
        LoggerDDF.e(TAG, String.format("Value of a: %.8f", dFeeAmount));
//        DecimalFormat df = new DecimalFormat("#.#");
//        df.setMaximumFractionDigits(15);
//        LoggerDDF.e(TAG, df.format(dTotalAmount));
//        String rounded = String.format("%.0f", dTotalAmount);
//                LoggerDDF.e(TAG, rounded);

        ConstantsActivity.STR_CURRENCY_TOTAL_AMOUNT = dTotalAmount + "";
//        tv_total_amount_pay_conf.setText(ConstantsActivity.STR_CURRENCY_TOTAL_AMOUNT);
        tv_total_amount_pay_conf.setText(String.format("%.8f", dTotalAmount));

        ImageView iv_back_common = findViewById(R.id.iv_back_common);
        iv_back_common.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        RelativeLayout rl_cancel_pay_conf = findViewById(R.id.rl_cancel_pay_conf);
        rl_cancel_pay_conf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        CheckBox cb_disclaimer_req_pay = findViewById(R.id.cb_disclaimer_req_pay);
        cb_disclaimer_req_pay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.e("onCheckedChanged", "onCheckedChanged");
                bDisclaimer = b;
                if (b) {
                    rl_proceed_pay_conf.setBackgroundResource(R.drawable.btn_corners_app_color);
                } else {
                    rl_proceed_pay_conf.setBackgroundResource(R.drawable.btn_corners_gray);
                }
            }
        });

    }
}
