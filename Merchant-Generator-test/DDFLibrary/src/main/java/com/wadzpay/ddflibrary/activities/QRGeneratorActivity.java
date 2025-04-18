package com.wadzpay.ddflibrary.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.wadzpay.ddflibrary.R;
import com.wadzpay.ddflibrary.api.ApiRetroFitPaymentInfo;
import com.wadzpay.ddflibrary.api.ConstantsApi;
import com.wadzpay.ddflibrary.callbacks.ActivityCallBack;
import com.wadzpay.ddflibrary.dialogs.ConstantsDialog;
import com.wadzpay.ddflibrary.dialogs.DialogCustomize;
import com.wadzpay.ddflibrary.dialogs.ToastCustomize;
import com.wadzpay.ddflibrary.library.QRGContents;
import com.wadzpay.ddflibrary.library.QRGEncoder;
import com.wadzpay.ddflibrary.logs.LoggerDDF;
import com.wadzpay.ddflibrary.utils.ConstantsActivity;

import java.math.BigDecimal;

public class QRGeneratorActivity extends AppCompatActivity implements ActivityCallBack {
    private String TAG = getClass().getSimpleName();

    private EditText etValue;
    private ImageView ivQrResult;
    private TextView tvAddress, tvCryptoAmount, tv_qr_format_req_pay,tv_qr_formats_req_pay;
    private Button btn_qr1_qr, btn_qr2_qr, btn_qr3_qr, btn_qr4_qr, btn_qr5_qr, btn_qr6_qr;
    private LinearLayout lyt_total_pay;
    private RelativeLayout rl_qr_format_req_pay;
    private String strInput;
    private Bitmap bitmapQR;
    private QRGEncoder qrgEncoder;
    int flagRadio = 0;
    DialogCustomize dialogCustomize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_generator);
        ConstantsActivity.flag_clear_fields = true;

        try {
            TextView tvTimerQR = findViewById(R.id.tv_timer_qr);
            reverseTimer(ConstantsActivity.INT_QR_TIMER, tvTimerQR);
            flagRadio = PaymentRequestDDFActivityK.Companion.getRadioFlag();
            initUI();
        } catch (Exception e) {
            LoggerDDF.e(TAG, e + "");
        }
    }

    ApiRetroFitPaymentInfo apiRetroFitPaymentInfo = new ApiRetroFitPaymentInfo();
    double cryptoAmount;

    private void initUI() {
        LoggerDDF.e(TAG, "initUI");
        ivQrResult = findViewById(R.id.iv_qr_result);
        tvAddress = findViewById(R.id.tv_address_qr);
        tvCryptoAmount = findViewById(R.id.tv_crypto_amount_qr);
        tv_qr_format_req_pay = findViewById(R.id.tv_qr_format_req_pay);
        tv_qr_formats_req_pay = findViewById(R.id.tv_qr_formats_req_pay);
        lyt_total_pay = findViewById(R.id.lyt_total_pay);
        etValue = findViewById(R.id.et_input_qr);

        tv_qr_format_req_pay.setText(ConstantsActivity.STR_FORMAT_ET);
        TextView tv_fiat_amount_qr = findViewById(R.id.tv_fiat_amount_qr);
//        tv_fiat_amount_qr.setText(ConstantsActivity.STR_CURRENCY_TOTAL_AMOUNT);

//        double cryptoAmount = Double.parseDouble(ConstantsActivity.STR_CURRENCY_AMOUNT);
//        double cryptoAmount = Double.parseDouble(ConstantsActivity.STR_CURRENCY_TOTAL_AMOUNT);
        cryptoAmount = Double.parseDouble(ConstantsActivity.STR_CURRENCY_AMOUNT);
//        tvCryptoAmount.setText(ConstantsActivity.STR_CURRENCY_AMOUNT + " " + ConstantsActivity.STR_CURRENCY_CODE);
//       commented
        /*if (flagRadio == 2) {
            tv_fiat_amount_qr.setText(" " + ConstantsActivity.STR_CURRENCY_FIAT_TYPE);
            tvCryptoAmount.setText(" " + ConstantsActivity.STR_CURRENCY_CODE);
//            lyt_total_pay.setVisibility(View.GONE);
        } else {
            tv_fiat_amount_qr.setText(ConstantsActivity.STR_CURRENCY_ET + " " + ConstantsActivity.STR_CURRENCY_FIAT_TYPE);
            tvCryptoAmount.setText(String.format("%.8f", cryptoAmount) + " " + ConstantsActivity.STR_CURRENCY_CODE);
            lyt_total_pay.setVisibility(View.VISIBLE);
        }*/
        tv_fiat_amount_qr.setText(ConstantsActivity.STR_CURRENCY_ET + " " + ConstantsActivity.STR_CURRENCY_FIAT_TYPE);
//        tvCryptoAmount.setText(String.format("%.8f", cryptoAmount) + " " + ConstantsActivity.STR_CURRENCY_CODE);
        BigDecimal bigDecimalAmount = BigDecimal.valueOf(cryptoAmount);
//        tvCryptoAmount.setText(cryptoAmount + " " + ConstantsActivity.STR_CURRENCY_CODE);

//        tvCryptoAmount.setText(bigDecimalAmount.toPlainString() + " " + ConstantsActivity.STR_CURRENCY_CODE);
        if (ConstantsActivity.STR_CURRENCY_CODE.equalsIgnoreCase("sart")) {
            tvCryptoAmount.setText(bigDecimalAmount.toPlainString() + " " + ConstantsActivity.STR_SAR_CODE);
        } else {
            tvCryptoAmount.setText(bigDecimalAmount.toPlainString() + " " + ConstantsActivity.STR_CURRENCY_CODE);
        }
//        tvCryptoAmount.setText(ConstantsActivity.STR_CURRENCY_AMOUNT);

        tvAddress.setText(ConstantsApi.strQRAddress);
        ImageView iv_back_common = findViewById(R.id.iv_back_common);
        iv_back_common.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTimer();
                finish();
            }
        });
        Button btn_cancel_qr = findViewById(R.id.btn_cancel_qr);
        btn_cancel_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayCancelAlertDialog();
            }
        });
        Button btn_generate_qr = findViewById(R.id.btn_generate_qr);
        btn_generate_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                apiRetroFitPaymentInfo.paymentInfoRetrofit(QRGeneratorActivity.this);
                apiRetroFitPaymentInfo.refreshPaymentInfoRetrofit(QRGeneratorActivity.this);
            }
        });

        tvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setClipboardEG(QRGeneratorActivity.this, tvAddress.getText().toString());
            }
        });

        btn_qr1_qr = findViewById(R.id.btn_qr1_qr);
        btn_qr2_qr = findViewById(R.id.btn_qr2_qr);
        btn_qr3_qr = findViewById(R.id.btn_qr3_qr);
        btn_qr4_qr = findViewById(R.id.btn_qr4_qr);
        btn_qr5_qr = findViewById(R.id.btn_qr5_qr);
        btn_qr6_qr = findViewById(R.id.btn_qr6_qr);
//        ConstantsActivity.STR_FORMAT_ET = "QR Format 1";

        btn_qr1_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seletedQR(btn_qr1_qr);
                ConstantsActivity.STR_FORMAT_ET = "QR Format 1";
//                apiRetroFitPaymentInfo.refreshPaymentInfoRetrofit(QRGeneratorActivity.this);
                generateQRCall();
            }
        });

        btn_qr2_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seletedQR(btn_qr2_qr);
                ConstantsActivity.STR_FORMAT_ET = "QR Format 2";
//                apiRetroFitPaymentInfo.refreshPaymentInfoRetrofit(QRGeneratorActivity.this);
                generateQRCall();
            }
        });

        btn_qr3_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seletedQR(btn_qr3_qr);
                ConstantsActivity.STR_FORMAT_ET = "QR Format 3";
//                apiRetroFitPaymentInfo.refreshPaymentInfoRetrofit(QRGeneratorActivity.this);
                generateQRCall();
            }
        });

        btn_qr4_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seletedQR(btn_qr4_qr);
                ConstantsActivity.STR_FORMAT_ET = "QR Format 4";
//                apiRetroFitPaymentInfo.refreshPaymentInfoRetrofit(QRGeneratorActivity.this);
                generateQRCall();
            }
        });

        btn_qr5_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seletedQR(btn_qr5_qr);
                ConstantsActivity.STR_FORMAT_ET = "QR Format 5";
//                apiRetroFitPaymentInfo.refreshPaymentInfoRetrofit(QRGeneratorActivity.this);
                ConstantsActivity.STR_FORMAT_TITLE = "QR Address";
                generateQRCall();
            }
        });

        btn_qr6_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seletedQR(btn_qr6_qr);
                ConstantsActivity.STR_FORMAT_ET = "QR Format 6";
//                apiRetroFitPaymentInfo.refreshPaymentInfoRetrofit(QRGeneratorActivity.this);
                generateQRCall();
            }
        });

        dialogCustomize = DialogCustomize.getInstance();
        rl_qr_format_req_pay = findViewById(R.id.rl_qr_format_req_pay);
        rl_qr_format_req_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogCustomize.displayArrayDialog(QRGeneratorActivity.this, ConstantsActivity.STR_FLAG_CHOICE_QR_FORMAT_DIALOG);
            }
        });

        generateQRCall();
    }

    private void generateQRCall() {
        String str_generate_qr_format = "";
        /*if (ConstantsActivity.STR_CURRENCY_CODE.equals("WTK")) {
            generateQR(ConstantsApi.strQRAddress + "@1?value=" + String.format("%.8f", cryptoAmount));
        } else {
            generateQR(ConstantsApi.strQRAddress);
        }*/
//        generateQR(ConstantsApi.strQRAddress + "?value=" + String.format("%.8f", cryptoAmount));
        /*ConstantsActivity.STR_QR_FORMAT = getString(R.string.qr_format);
        if(ConstantsActivity.STR_QR_FORMAT.equals(getString(R.string.qr_with_address))){
            generateQR(ConstantsApi.strQRAddress);
        } else if(ConstantsActivity.STR_QR_FORMAT.equals(getString(R.string.qr_with_amount))){
            generateQR(ConstantsApi.strQRAddress + "?value=" + String.format("%.8f", cryptoAmount));
        } else if(ConstantsActivity.STR_QR_FORMAT.equals(getString(R.string.qr_with_currency))){
            generateQR(ConstantsActivity.STR_CURRENCY_CODE+ ":"+ConstantsApi.strQRAddress + "?value=" + String.format("%.8f", cryptoAmount));
        }*/

        /*if (ConstantsActivity.STR_FORMAT_ET.equals("QR Format 1")) {
            str_generate_qr_format = ConstantsApi.strQRAddress;
            seletedQR(btn_qr1_qr);
        } else if (ConstantsActivity.STR_FORMAT_ET.equals("QR Format 2")) {
            str_generate_qr_format = ConstantsApi.strQRAddress + "?amount=" + String.format("%.8f", cryptoAmount);
            seletedQR(btn_qr2_qr);
        } else if (ConstantsActivity.STR_FORMAT_ET.equals("QR Format 3")) {
            str_generate_qr_format = ConstantsApi.strQRAddress + "?value=" + String.format("%.8f", cryptoAmount);
            seletedQR(btn_qr3_qr);
        } else if (ConstantsActivity.STR_FORMAT_ET.equals("QR Format 4")) {
            str_generate_qr_format = ConstantsActivity.STR_CURRENCY_CODE + ":" + ConstantsApi.strQRAddress + "?amount=" + String.format("%.8f", cryptoAmount);
            seletedQR(btn_qr4_qr);
        } else if (ConstantsActivity.STR_FORMAT_ET.equals("QR Format 5")) {
            str_generate_qr_format = ConstantsActivity.STR_CURRENCY_CODE + ":" + ConstantsApi.strQRAddress + "?value=" + String.format("%.8f", cryptoAmount);
            seletedQR(btn_qr5_qr);
        } else if (ConstantsActivity.STR_FORMAT_ET.equals("QR Format 6")) {
            str_generate_qr_format = ConstantsActivity.STR_CURRENCY_CODE + ":" + ConstantsApi.strQRAddress;
            seletedQR(btn_qr6_qr);
        }*/
        Log.e(TAG + "@256", ConstantsActivity.STR_FORMAT_ET + "");
        if (ConstantsActivity.STR_FORMAT_ET.equals("QR Format 1")) {
            ConstantsActivity.STR_FORMAT_TITLE = "QR With Currency, Address And Amount";
            str_generate_qr_format = ConstantsActivity.STR_CURRENCY_CODE + ":" + ConstantsApi.strQRAddress + "?amount=" + String.format("%.8f", cryptoAmount);
            seletedQR(btn_qr1_qr);
        } else if (ConstantsActivity.STR_FORMAT_ET.equals("QR Format 2")) {
            str_generate_qr_format = ConstantsActivity.STR_CURRENCY_CODE + ":" + ConstantsApi.strQRAddress + "?value=" + String.format("%.8f", cryptoAmount);
            ConstantsActivity.STR_FORMAT_TITLE = "QR With Currency, Address And Value";
            seletedQR(btn_qr2_qr);
        } else if (ConstantsActivity.STR_FORMAT_ET.equals("QR Format 3")) {
            str_generate_qr_format = ConstantsApi.strQRAddress + "?amount=" + String.format("%.8f", cryptoAmount);
            ConstantsActivity.STR_FORMAT_TITLE = "QR With Address And Amount";
            seletedQR(btn_qr3_qr);
        } else if (ConstantsActivity.STR_FORMAT_ET.equals("QR Format 4")) {
            str_generate_qr_format = ConstantsApi.strQRAddress + "?value=" + String.format("%.8f", cryptoAmount);
            ConstantsActivity.STR_FORMAT_TITLE = "QR With Address And Value";
            seletedQR(btn_qr4_qr);
        } else if (ConstantsActivity.STR_FORMAT_ET.equals("QR Format 5")) {
            str_generate_qr_format = ConstantsApi.strQRAddress;
            ConstantsActivity.STR_FORMAT_TITLE = "QR With Address";
            seletedQR(btn_qr5_qr);
        } else if (ConstantsActivity.STR_FORMAT_ET.equals("QR Format 6")) {
            str_generate_qr_format = ConstantsActivity.STR_CURRENCY_CODE + ":" + ConstantsApi.strQRAddress;
            ConstantsActivity.STR_FORMAT_TITLE = "QR With Currency And Address";
            seletedQR(btn_qr6_qr);
        }

        generateQR(str_generate_qr_format);
    }

    private void seletedQR(Button btnChange) {
        btn_qr1_qr.setBackgroundResource(R.drawable.btn_corners_gray);
        btn_qr2_qr.setBackgroundResource(R.drawable.btn_corners_gray);
        btn_qr3_qr.setBackgroundResource(R.drawable.btn_corners_gray);
        btn_qr4_qr.setBackgroundResource(R.drawable.btn_corners_gray);
        btn_qr5_qr.setBackgroundResource(R.drawable.btn_corners_gray);
        btn_qr6_qr.setBackgroundResource(R.drawable.btn_corners_gray);

        btnChange.setBackgroundResource(R.drawable.btn_corners_app_color);
        tv_qr_formats_req_pay.setText(ConstantsActivity.STR_FORMAT_TITLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        Drawable drawableImage = getResources().getDrawable(R.drawable.);
        Drawable drawableImage = getResources().getDrawable(ConstantsActivity.INT_CURRENT_IMAGE_SPINNER);
//        Bitmap bitmap = ((BitmapDrawable) drawableImage).getBitmap();
//        Bitmap bitmap = ((BitmapDrawable) drawableImage).getBitmap();
        Bitmap bitmap = setBitMapVector(drawableImage);
        drawableImage.setBounds(0, 0, 0, 0);
        Drawable drawableBitmap = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 40, 40, true));
//        tvCryptoAmount.setCompoundDrawablesWithIntrinsicBounds(drawableImage, null, null, null);
        tvCryptoAmount.setCompoundDrawablesWithIntrinsicBounds(drawableBitmap, null, null, null);
    }

    private Bitmap setBitMapVector(Drawable drawable) {
        try {
            Bitmap bitmap;

            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            // Handle the error
            return null;
        }
    }

    private void generateQR(String strInputText) {
//        ToastCustomize toastCustomize = ToastCustomize.getInstance();
//        toastCustomize.displayToast(this, strInputText);
        strInput = strInputText;
        if (strInput.length() > 0) {
            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            int height = point.y;
            int smallerDimension = width < height ? width : height;
            smallerDimension = smallerDimension * 3 / 4;

            qrgEncoder = new QRGEncoder(
                    this.strInput, null,
                    QRGContents.Type.TEXT,
                    smallerDimension);
            qrgEncoder.setColorBlack(Color.BLACK);
            qrgEncoder.setColorWhite(Color.WHITE);
            try {
                bitmapQR = qrgEncoder.getBitmap();
                ivQrResult.setImageBitmap(bitmapQR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            etValue.setError(getResources().getString(R.string.value_required));
        }
    }

    private void setClipboardEG(Context context, String text) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
        ToastCustomize toastCustomize = ToastCustomize.getInstance();
        toastCustomize.displayToast(QRGeneratorActivity.this, "Copied To Clipboard");
    }

    //


    @Override
    protected void onStop() {
        super.onStop();
//        finish();
    }

    @Override
    public void activityCall(String strResponse) {
        try {
            LoggerDDF.e(TAG, strResponse + " @151");
//            cancel action
            if(ConstantsActivity.STR_FLAG_DIALOG.equalsIgnoreCase(TAG)){
                stopTimer();
                finish();
            }
            if (strResponse.equals(ConstantsActivity.STR_FLAG_CHOICE_QR_FORMAT_DIALOG)) {
//                ToastCustomize.displayToast(QRGeneratorActivity.this,ConstantsActivity.STR_FLAG_CHOICE_QR_FORMAT_DIALOG);
                tv_qr_format_req_pay.setText(ConstantsActivity.STR_FORMAT_ET);
                apiRetroFitPaymentInfo.refreshPaymentInfoRetrofit(QRGeneratorActivity.this);
                return;
            }
            Log.e(TAG, ConstantsActivity.STR_TRANSACTION_STATUS + "@230");
            if (ConstantsActivity.STR_TRANSACTION_STATUS.equalsIgnoreCase("SUCCESSFUL")
                            || ConstantsActivity.STR_TRANSACTION_STATUS.equalsIgnoreCase("UNDERPAID")
                            || ConstantsActivity.STR_TRANSACTION_STATUS.equalsIgnoreCase("OVERPAID")
            ) {
                Intent intent = new Intent(getApplicationContext(), ThankYouActivity.class);
                startActivity(intent);
                finish();
                return;
            }
            openReopenActivity();
//            Log.e(TAG, ConstantsApi.strTSReceived + "@230");
//            ConstantsApi.strTSReceivedFinal = ConstantsApi.strTSReceived;
            Log.e(TAG, ConstantsApi.strTSReceivedFinal + "@371");
//            double dAmountReceived = Double.parseDouble(ConstantsApi.strTSReceived);
//            if (ConstantsActivity.STR_TRANSACTION_STATUS.equalsIgnoreCase("IN_PROGRESS")) {
            /*if (dAmountReceived == 0.0) {
                openReopenActivity();
            } else if (dAmountReceived > 0.0) {
                Intent intent = new Intent(getApplicationContext(), ThankYouActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(getApplicationContext(), ThankYouActivity.class);
                startActivity(intent);
                finish();
            }*/


        } catch (Exception e) {
            Log.e("Exception@222", e + "");
        }
    }

    @Override
    public void activityCallFailed(String strResponse) {
        Log.e("activityCallFailed@432", strResponse + "@432");
    }

    private void openReopenActivity() {
        /*Intent intent = new Intent(QRGeneratorActivity.this, QRGeneratorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();*/

        /*startActivity(getIntent());
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);*/

//        stopTimer();
//        TextView tvTimerQR = findViewById(R.id.tv_timer_qr);
//        reverseTimer(ConstantsActivity.INT_QR_TIMER, tvTimerQR);

    }

    CountDownTimer countDownTimer;

    public void reverseTimer(int Seconds, final TextView tv_timer) {

        countDownTimer = new CountDownTimer(Seconds * 1000 + 1000, 1000) {

            public void onTick(long millisUntilFinished) {
//                Log.e("millisUntilFinished ", millisUntilFinished + "");
                int secondsTick = (int) (millisUntilFinished / 1000);
                int minutes = secondsTick / 60;
                secondsTick = secondsTick % 60;
//                tv.setText("TIME : " + String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
                tv_timer.setText(String.format("%02d", secondsTick));
                ConstantsActivity.INT_QR_TRANSACTION_TIMER = ConstantsActivity.INT_QR_TRANSACTION_TIMER - 1;
//                Log.e("TRANSACTION_TIMER ", ConstantsActivity.INT_QR_TRANSACTION_TIMER + "");
            }

            public void onFinish() {
//                openReopenActivity();
//                apiRetroFitPaymentInfo.paymentInfoRetrofit(QRGeneratorActivity.this);
                tv_timer.setText("00");
                if (ConstantsActivity.INT_QR_TRANSACTION_TIMER < 1) {
                    ConstantsActivity.STR_TRANSACTION_STATUS = "FAILED";
                    Intent intent = new Intent(getApplicationContext(), ThankYouActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    apiRetroFitPaymentInfo.refreshPaymentInfoRetrofit(QRGeneratorActivity.this);
                    TextView tvTimerQR = findViewById(R.id.tv_timer_qr);
                    reverseTimer(ConstantsActivity.INT_QR_TIMER, tvTimerQR);
                }
            }
        };
        countDownTimer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoggerDDF.e(TAG, "onDestroy");
        stopTimer();
    }

    private void stopTimer() {
        countDownTimer.cancel();
//        countDownTimer.onFinish();
    }

    private void displayCancelDialog(){
        ConstantsActivity.STR_FLAG_DIALOG = TAG;
        dialogCustomize.displayDialog(
                this,
                ConstantsDialog.FLAG_DIALOG_DISPLAY,
                "Alert",
                "Are you sure?"
        );
    }

    private void displayCancelAlertDialog() {
        LoggerDDF.e(TAG, "displayServiceDialog");
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setTitle("Cancel");
        builder1.setMessage("Are Your Sure?");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();

    }
}
