package com.wadzpay.ddflibrary.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.provider.MediaStore;
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
import com.wadzpay.ddflibrary.dialogs.DialogCustomize;
import com.wadzpay.ddflibrary.dialogs.ToastCustomize;
import com.wadzpay.ddflibrary.library.QRGContents;
import com.wadzpay.ddflibrary.library.QRGEncoder;
import com.wadzpay.ddflibrary.logs.LoggerDDF;
import com.wadzpay.ddflibrary.utils.ConstantsActivity;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

public class QRGeneratorActivityStaticQR extends AppCompatActivity implements ActivityCallBack {
    private String TAG = getClass().getSimpleName();

    private EditText etValue;
    private ImageView ivQrResult;
    private TextView tvAddress, tvCryptoAmount, tv_qr_format_req_pay,tv_qr_formats_req_pay,tv_merchant_name;
    private Button btn_qr1_qr, btn_qr2_qr, btn_qr3_qr, btn_qr4_qr, btn_qr5_qr, btn_qr6_qr;
    private LinearLayout lyt_total_pay,lyt_parent,lyt_static_qr;
    private RelativeLayout rl_qr_format_req_pay;
    private String strInput;
    private Bitmap bitmapQR;
    private QRGEncoder qrgEncoder;
    int flagRadio = 0;
    DialogCustomize dialogCustomize;
    TextView tvTimerQR;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_static);
        LoggerDDF.e(TAG,ConstantsApi.strMDQRString +" @59");
        screenTimeOut();
        try {
            tvTimerQR = findViewById(R.id.tv_timer_qr);
//            reverseTimer(ConstantsActivity.INT_QR_TIMER, tvTimerQR);
//            flagRadio = PaymentRequestDDFActivityK.Companion.getRadioFlag();
            initUI();
        } catch (Exception e) {
            LoggerDDF.e(TAG, e + "@67");
        }
    }

    private void screenTimeOut() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");
        wakeLock.acquire();
    }

    ApiRetroFitPaymentInfo apiRetroFitPaymentInfo = new ApiRetroFitPaymentInfo();
    double cryptoAmount;

    private void initUI() {
        LoggerDDF.e(TAG, "initUI");
        ivQrResult = findViewById(R.id.iv_qr_result);
        tv_merchant_name = findViewById(R.id.tv_merchant_name);
        tv_merchant_name.setText(ConstantsApi.strPosName);
        tvAddress = findViewById(R.id.tv_address_qr);
        tvCryptoAmount = findViewById(R.id.tv_crypto_amount_qr);
        tv_qr_format_req_pay = findViewById(R.id.tv_qr_format_req_pay);
        tv_qr_formats_req_pay = findViewById(R.id.tv_qr_formats_req_pay);
        lyt_total_pay = findViewById(R.id.lyt_total_pay);
        etValue = findViewById(R.id.et_input_qr);

//        tv_qr_format_req_pay.setText(ConstantsActivity.STR_FORMAT_ET);
        TextView tv_fiat_amount_qr = findViewById(R.id.tv_fiat_amount_qr);
//        tv_fiat_amount_qr.setText(ConstantsActivity.STR_CURRENCY_TOTAL_AMOUNT);

//        double cryptoAmount = Double.parseDouble(ConstantsActivity.STR_CURRENCY_AMOUNT);
//        double cryptoAmount = Double.parseDouble(ConstantsActivity.STR_CURRENCY_TOTAL_AMOUNT);
//        cryptoAmount = Double.parseDouble(ConstantsActivity.STR_CURRENCY_AMOUNT);
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
//        tv_fiat_amount_qr.setText(ConstantsActivity.STR_CURRENCY_ET + " " + ConstantsActivity.STR_CURRENCY_FIAT_TYPE);
//        tvCryptoAmount.setText(String.format("%.8f", cryptoAmount) + " " + ConstantsActivity.STR_CURRENCY_CODE);
//        BigDecimal bigDecimalAmount = BigDecimal.valueOf(cryptoAmount);
//        tvCryptoAmount.setText(cryptoAmount + " " + ConstantsActivity.STR_CURRENCY_CODE);
//        tvCryptoAmount.setText(bigDecimalAmount.toPlainString() + " " + ConstantsActivity.STR_CURRENCY_CODE);

//        tvCryptoAmount.setText(ConstantsActivity.STR_CURRENCY_AMOUNT);

//        tvAddress.setText(ConstantsApi.strQRAddress);
        ImageView iv_back_common = findViewById(R.id.iv_back_common);
        iv_back_common.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTimer();
                finish();
            }
        });
        Button btn_check_qr = findViewById(R.id.btn_check_qr);
        btn_check_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTimer();
//                finish();
//                reverseTimer(ConstantsActivity.INT_QR_TIMER, tvTimerQR);
                apiRetroFitPaymentInfo.getTransactionStatusAlgo(QRGeneratorActivityStaticQR.this);
            }
        });
        Button btn_cancel_qr = findViewById(R.id.btn_cancel_qr);
        btn_cancel_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTimer();
                finish();
            }
        });

        Button btn_generate_qr = findViewById(R.id.btn_generate_qr);
        btn_generate_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                apiRetroFitPaymentInfo.paymentInfoRetrofit(QRGeneratorActivity.this);
                apiRetroFitPaymentInfo.refreshPaymentInfoRetrofit(QRGeneratorActivityStaticQR.this);
            }
        });

        tvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setClipboardEG(QRGeneratorActivityStaticQR.this, tvAddress.getText().toString());
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
                dialogCustomize.displayArrayDialog(QRGeneratorActivityStaticQR.this, ConstantsActivity.STR_FLAG_CHOICE_QR_FORMAT_DIALOG);
            }
        });

        lyt_parent = findViewById(R.id.lyt_parent);
        lyt_static_qr = findViewById(R.id.lyt_static_qr);
        Button btn_share_qr = findViewById(R.id.btn_share_qr);
        btn_share_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share_bitMap_to_Apps();
            }
        });

        generateQRCall();
    }

    public void share_bitMap_to_Apps() {
//        String shareBody = "Hi, Scan Above QR Code To Get Details";
        Intent i = new Intent(Intent.ACTION_SEND);
//        i.putExtra(Intent.EXTRA_EMAIL, new String[]{});
//        i.putExtra(Intent.EXTRA_SUBJECT, "Static QR Code");
//        i.putExtra(Intent.EXTRA_TEXT, "Hi,\nScan Above QR Code To Get Details");
//        i.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
//        i.setData(Uri.parse("mailto:"));
        i.setType("image/*");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
    /*compress(Bitmap.CompressFormat.PNG, 100, stream);
    byte[] bytes = stream.toByteArray();*/

        i.putExtra(Intent.EXTRA_STREAM, getImageUri(this, getBitmapFromView(lyt_static_qr)));
        try {
            startActivity(Intent.createChooser(i, "Static QR Code..."));
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    public Bitmap getBitmapFromView(View view) {
        // Define a bitmap with the same size as the view
        LoggerDDF.e(TAG,view.getWidth()+" - "+view.getWidth());
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        // Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        // Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            // has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            // does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        // return the bitmap
        return returnedBitmap;
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Static QR Code", null);
        return Uri.parse(path);
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
//            ConstantsActivity.STR_FORMAT_TITLE = "QR With Address";
            ConstantsActivity.STR_FORMAT_TITLE = "Static QR Code";
            seletedQR(btn_qr5_qr);
        } else if (ConstantsActivity.STR_FORMAT_ET.equals("QR Format 6")) {
            str_generate_qr_format = ConstantsActivity.STR_CURRENCY_CODE + ":" + ConstantsApi.strQRAddress;
            ConstantsActivity.STR_FORMAT_TITLE = "QR With Currency And Address";
            seletedQR(btn_qr6_qr);
        }
//        str_generate_qr_format = ConstantsApi.strQRString;
//        str_generate_qr_format = ConstantsApi.strQREncryptedString;
        str_generate_qr_format = ConstantsApi.strMDQRString;
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
        toastCustomize.displayToast(QRGeneratorActivityStaticQR.this, "Copied To Clipboard");
    }

    //


    @Override
    protected void onStop() {
        super.onStop();
//        finish();
    }

    @Override
    public void activityCall(String strResponse) {
       /* try {
            Log.e(TAG,  "activityCall@387");
            LoggerDDF.e(TAG, strResponse + " @151");

            if (strResponse.equals(ConstantsActivity.STR_FLAG_CHOICE_QR_FORMAT_DIALOG)) {
//                ToastCustomize.displayToast(QRGeneratorActivity.this,ConstantsActivity.STR_FLAG_CHOICE_QR_FORMAT_DIALOG);
                tv_qr_format_req_pay.setText(ConstantsActivity.STR_FORMAT_ET);
//                apiRetroFitPaymentInfo.refreshPaymentInfoRetrofit(QRGeneratorActivityStaticQR.this);
                apiRetroFitPaymentInfo.getTransactionStatusAlgo(QRGeneratorActivityStaticQR.this);
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
//            openReopenActivity();
            reverseTimer(ConstantsActivity.INT_QR_TIMER, tvTimerQR);

//            Log.e(TAG, ConstantsApi.strTSReceived + "@230");
//            ConstantsApi.strTSReceivedFinal = ConstantsApi.strTSReceived;
            Log.e(TAG, ConstantsApi.strTSReceivedFinal + "@371");
//            double dAmountReceived = Double.parseDouble(ConstantsApi.strTSReceived);
//            if (ConstantsActivity.STR_TRANSACTION_STATUS.equalsIgnoreCase("IN_PROGRESS")) {

        } catch (Exception e) {
            Log.e("Exception@222", e + "");
//            reverseTimer(ConstantsActivity.INT_QR_TIMER, tvTimerQR);
            openReopenActivity();
        }*/
    }

    @Override
    public void activityCallFailed(String strResponse) {
        Log.e("activityCallFailed@432", strResponse + "@432");
    }

    private void openReopenActivity() {
        Log.e("openReopenActivity@436",  "@436");
   /*Intent intent = new Intent(QRGeneratorActivity.this, QRGeneratorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();*/

        startActivity(getIntent());
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoggerDDF.e(TAG, "onDestroy");
        stopTimer();
    }

    private void stopTimer() {
        Log.e("stopTimer@494",  "@494");
    }
}
