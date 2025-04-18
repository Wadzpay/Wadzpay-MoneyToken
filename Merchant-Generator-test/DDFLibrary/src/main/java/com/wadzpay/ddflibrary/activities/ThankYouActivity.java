package com.wadzpay.ddflibrary.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.print.PrintHelper;

import com.wadzpay.ddflibrary.R;
import com.wadzpay.ddflibrary.api.ApiRetroFitPaymentInfo;
import com.wadzpay.ddflibrary.api.ConstantsApi;
import com.wadzpay.ddflibrary.callbacks.ActivityCallBack;
import com.wadzpay.ddflibrary.dialogs.ToastCustomize;
import com.wadzpay.ddflibrary.library.QRGContents;
import com.wadzpay.ddflibrary.library.QRGEncoder;
import com.wadzpay.ddflibrary.logs.LoggerDDF;
import com.wadzpay.ddflibrary.utils.ConstantsActivity;
import com.wadzpay.ddflibrary.utils.CustomOperations;
import com.wadzpay.ddflibrary.utils.TextToVoiceEG;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ThankYouActivity extends AppCompatActivity implements ActivityCallBack {
    private String TAG = getClass().getSimpleName();
    TextToVoiceEG textToVoiceEG;
    LinearLayout lyt_parent_transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thank_you);
        initUI();
        lyt_parent_transaction = findViewById(R.id.lyt_parent_transaction);
//        ConstantsActivity.INT_CURRENT_IMAGE_SPINNER = 2131165300;
//        ConstantsActivity.STR_TRANSACTION_STATUS = "UNDERPAID";
//        ConstantsApi.strQRAddress = "2NBdkifimRaiLYWoiXt5xiMgqHKeS44zfc3";
//        ConstantsApi.strQRAddressFirst = "2NBdkifimRaiLYWoiXt5xiMgqHKeS44zfc3";
//        ConstantsApi.strUUID = "0ad3c9d1-b09f-4455-9f97-bd120a984e43";
        if (ConstantsActivity.STR_TRANSACTION_STATUS.equals("FAILED")) {
            updateUI();
        } else {
            ApiRetroFitPaymentInfo apiRetroFitPaymentInfo = new ApiRetroFitPaymentInfo();
            if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT.equalsIgnoreCase("GEIDEA")) {
                apiRetroFitPaymentInfo.getTransactionStatusAlgo(this);
            } else {
                apiRetroFitPaymentInfo.getTransactionStatus(this);
            }

        }
    }

    private void initUI() {
        LoggerDDF.e(TAG, "initUI");
        textToVoiceEG = new TextToVoiceEG();
    }

    @Override
    protected void onResume() {
        super.onResume();

        LoggerDDF.e(TAG, "onResume");

    }

    ImageView iv_status_transaction;
    TextView tv_created_date_thank_you;
    LinearLayout lyt_top_thank_you, lyt_bottom_thank_you;

    private void updateUI() {
        LoggerDDF.e(TAG, "updateUI");
        try {
//            ToastCustomize.displayToast(this, ConstantsActivity.STR_TRANSACTION_STATUS);
            lyt_parent_transaction.setVisibility(View.VISIBLE);
            ImageView iv_back_common = findViewById(R.id.iv_back_common);
            iv_back_common.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            ConstantsActivity.FLAG_STR_THANK_YOU = "FLAG_THANK_YOU";
            Button btn_done_thank_you = findViewById(R.id.btn_done_thank_you);
            btn_done_thank_you.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    finish();
                    lyt_top_thank_you.setVisibility(View.GONE);
                    lyt_bottom_thank_you.setVisibility(View.GONE);
                    btn_done_thank_you.setText(getString(R.string.print_receipt));
                    generateQR(ConstantsApi.strTransactionID);
                    double receivedAmount = Double.parseDouble(ConstantsApi.strTSReceived);
                    BigDecimal bigDecimalAmount = BigDecimal.valueOf(receivedAmount);
                    StringBuilder sb = new StringBuilder();
                    for (char c : ConstantsApi.strTSDigitalCurrencyType.toCharArray()) {
                        sb.append(c).append(" ");
                    }
                    String strCodeAlphabets = sb.toString().trim();
//                    textToVoiceEG.speechNow(ThankYouActivity.this, "Received Amount " + bigDecimalAmount.toPlainString() + " of " + ConstantsActivity.STR_CURRENCY_CODE);
                    textToVoiceEG.speechNow(ThankYouActivity.this, "Transaction " + ConstantsActivity.STR_TRANSACTION_STATUS + " And Received Amount " + bigDecimalAmount.toPlainString() + " of " + strCodeAlphabets);
                    doPhotoPrint();
                }
            });

            TextView tv_status_transaction = findViewById(R.id.tv_status_transaction);
            tv_status_transaction.setText(ConstantsActivity.STR_TRANSACTION_STATUS);
            iv_status_transaction = findViewById(R.id.iv_status_transaction);
            if (ConstantsActivity.STR_TRANSACTION_STATUS.equals("SUCCESSFUL")) {
                iv_status_transaction.setImageResource(R.drawable.ic_transaction_success);
            } else if (ConstantsActivity.STR_TRANSACTION_STATUS.equals("UNDERPAID")) {
                iv_status_transaction.setImageResource(R.drawable.ic_transaction_underpaid);
            } else if (ConstantsActivity.STR_TRANSACTION_STATUS.equals("OVERPAID")) {
                iv_status_transaction.setImageResource(R.drawable.ic_transaction_overpaid);
            } else if (ConstantsActivity.STR_TRANSACTION_STATUS.equals("FAILED")) {
                iv_status_transaction.setImageResource(R.drawable.ic_transaction_underpaid);
                return;
            }

        /*if(ConstantsApi.strTSStatus.equals("SUCCESSFUL")){
            iv_status_transaction.setImageResource(R.drawable.ic_transaction_success);
        } else if(ConstantsApi.strTSStatus.equals("UNDERPAID")){
            iv_status_transaction.setImageResource(R.drawable.ic_transaction_underpaid);
        } else if(ConstantsApi.strTSStatus.equals("OVERPAID")){
            iv_status_transaction.setImageResource(R.drawable.ic_transaction_overpaid);
        }*/

            TextView tv_fiat_thank_you = findViewById(R.id.tv_fiat_thank_you);
            TextView tv_crypto_thank_you = findViewById(R.id.tv_crypto_thank_you);
            TextView tv_received_thank_you = findViewById(R.id.tv_received_thank_you);
            TextView tv_uuid_thank_you = findViewById(R.id.tv_uuid_thank_you);
            TextView tv_pos_id_thank_you = findViewById(R.id.tv_pos_id_thank_you);
            TextView tv_pos_seq_thank_you = findViewById(R.id.tv_pos_seq_thank_you);
            TextView tv_pos_tx_id_thank_you = findViewById(R.id.tv_pos_tx_id_thank_you);
            TextView tv_under_paid_thank_you = findViewById(R.id.tv_under_paid_thank_you);
            tv_created_date_thank_you = findViewById(R.id.tv_created_date_thank_you);
            double cryptoAmount = Double.parseDouble(ConstantsApi.strTSCrypto);
            double receivedAmount = Double.parseDouble(ConstantsApi.strTSReceived);
            BigDecimal paidAmount = BigDecimal.valueOf(receivedAmount);
            BigDecimal underPaidAmount = paidAmount.subtract(ConstantsApi.bigDecimalTSCrypto);
            tv_under_paid_thank_you.setText(underPaidAmount.toPlainString());
//            tv_fiat_thank_you.setText(ConstantsApi.strTSFiat + " " + ConstantsActivity.STR_CURRENCY_FIAT_TYPE);
            tv_fiat_thank_you.setText(ConstantsApi.strTSFiat + " " + ConstantsActivity.STR_CURRENCY_FIAT_TYPE);
//            tv_crypto_thank_you.setText(String.format("%.8f", cryptoAmount) + " " + ConstantsActivity.STR_CURRENCY_CODE);
//            tv_crypto_thank_you.setText(ConstantsApi.bigDecimalTSCrypto.toPlainString() + " " + ConstantsActivity.STR_CURRENCY_CODE);
//            tv_crypto_thank_you.setText(ConstantsApi.bigDecimalTSCrypto.toPlainString() + " " + ConstantsActivity.STR_CURRENCY_CODE);
            BigDecimal bigDecimalTotalAmount = BigDecimal.valueOf(cryptoAmount);
//            tv_crypto_thank_you.setText(bigDecimalTotalAmount.toPlainString() + " " + ConstantsApi.strTSDigitalCurrencyType);
            if (ConstantsActivity.STR_CURRENCY_CODE.equalsIgnoreCase("sart")) {
                tv_crypto_thank_you.setText(bigDecimalTotalAmount.toPlainString() + " " + ConstantsActivity.STR_SAR_CODE);
            } else {
                tv_crypto_thank_you.setText(bigDecimalTotalAmount.toPlainString() + " " + ConstantsApi.strTSDigitalCurrencyType);
            }
//            tv_received_thank_you.setText(String.format("%.8f", receivedAmount) + " " + ConstantsActivity.STR_CURRENCY_CODE);
//            tv_received_thank_you.setText(ConstantsApi.strTSReceivedFinal + " " + ConstantsActivity.STR_CURRENCY_CODE);
//            tv_received_thank_you.setText(ConstantsApi.bigDecimalTSReceived + " " + ConstantsActivity.STR_CURRENCY_CODE);
//            tv_received_thank_you.setText(receivedAmount + " " + ConstantsActivity.STR_CURRENCY_CODE);
            BigDecimal bigDecimalAmount = BigDecimal.valueOf(receivedAmount);
            if (ConstantsActivity.STR_CURRENCY_CODE.equalsIgnoreCase("sart")) {
                tv_received_thank_you.setText(bigDecimalAmount.toPlainString() + " " + ConstantsActivity.STR_SAR_CODE);
            } else {
                tv_received_thank_you.setText(bigDecimalAmount.toPlainString() + " " + ConstantsApi.strTSDigitalCurrencyType);
            }
//            tv_received_thank_you.setText(ConstantsApi.strTSReceived + " " + ConstantsActivity.STR_CURRENCY_CODE);
//            tv_created_date_thank_you.setText(ConstantsApi.strTSCreated);
//            tv_uuid_thank_you.setText(ConstantsApi.strUUID);
            tv_uuid_thank_you.setText(ConstantsApi.strTransactionID);
            tv_pos_id_thank_you.setText(ConstantsActivity.STR_EXT_POS_ID);
            tv_pos_seq_thank_you.setText(ConstantsActivity.STR_EXT_POS_SEQUENCE_NUMBER);
            tv_pos_tx_id_thank_you.setText(ConstantsActivity.STR_EXT_POS_TRANSACTION_ID);
            setTextDrawableLeft(tv_crypto_thank_you);
            setTextDrawableLeft(tv_received_thank_you);
//            String strDate = parseDateFormat(ConstantsApi.strTSCreated);
//            tv_created_date_thank_you.setText(strDate);
            textToVoiceEG.initSpeech(this);
            parseDateFormat(ConstantsApi.strTSCreated);
            lyt_top_thank_you = findViewById(R.id.lyt_top_thank_you);
            lyt_bottom_thank_you = findViewById(R.id.lyt_bottom_thank_you);
        } catch (Exception e) {
            Log.e("Exception -", e + "");
        }

//        textToVoiceEG.speechNow(ThankYouActivity.this, "Received Amount " + ConstantsApi.strTSReceived);
    }

    private void doPhotoPrint() {
        PrintHelper photoPrinter = new PrintHelper(this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher);
        Bitmap bitMapLayout = getBitmapFromView(lyt_parent_transaction);
        photoPrinter.printBitmap("receipt_print", bitMapLayout);
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
    @Override
    public void activityCall(String strResponse) {
        updateUI();
    }

    @Override
    public void activityCallFailed(String strResponse) {

    }

    //
    private String strInput;
    private Bitmap bitmapQR;
    private QRGEncoder qrgEncoder;

    private void generateQR(String strInputText) {
//        ToastCustomize toastCustomize = ToastCustomize.getInstance();
//        toastCustomize.displayToast(this,strInputText);
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
//            smallerDimension = smallerDimension * 2;
            LoggerDDF.e(TAG, smallerDimension + "");
            qrgEncoder = new QRGEncoder(
                    this.strInput, null,
                    QRGContents.Type.TEXT,
                    smallerDimension);
            qrgEncoder.setColorBlack(Color.BLACK);
            qrgEncoder.setColorWhite(Color.WHITE);
            try {
                bitmapQR = qrgEncoder.getBitmap();
//                bitmapQR =   Bitmap.createScaledBitmap(bitmapQR, 290, 290, false);
                iv_status_transaction.setImageBitmap(bitmapQR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
//            etValue.setError(getResources().getString(R.string.value_required));
        }
    }

    private void setTextDrawableLeft(TextView tvLeftDrawable) {
        ConstantsActivity.INT_CURRENT_IMAGE_SPINNER =
                CustomOperations.getCurrencyImageResource(ConstantsApi.strTSDigitalCurrencyType);
        LoggerDDF.e(TAG, ConstantsActivity.INT_CURRENT_IMAGE_SPINNER + "");
        Drawable drawableImage = getResources().getDrawable(ConstantsActivity.INT_CURRENT_IMAGE_SPINNER);
        Bitmap bitmap = setBitMapVector(drawableImage);
        drawableImage.setBounds(0, 0, 0, 0);
        Drawable drawableBitmap = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 40, 40, true));
        tvLeftDrawable.setCompoundDrawablesWithIntrinsicBounds(drawableBitmap, null, null, null);
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

    String strDateTime = "2022-06-22T09:35:28.427889Z";
    String strDateResult = "";

    private String parseDateFormat(String receivedDate) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            SimpleDateFormat output = new SimpleDateFormat("MMMM dd, yyyy hh:mm a");
            Date d = null;
            d = input.parse(receivedDate);
            String strDateResult = output.format(d);
            Log.e("strDateResult", "" + strDateResult);
//            ToastCustomize.displayToast(this,strDateResult);
            tv_created_date_thank_you.setText(strDateResult + "");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return strDateResult;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConstantsApi.strQRAddressFirst = "";
    }
}
