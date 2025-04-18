package com.wadzpay.ddflibrary.utils;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.wadzpay.ddflibrary.api.ConstantsApi;
import com.wadzpay.ddflibrary.logs.LoggerDDF;

import java.math.BigDecimal;
import java.util.Locale;

public class TextToVoiceEG {
    private String TAG = getClass().getSimpleName();
    TextToSpeech textToSpeech;
    Context mContext;

    public void initSpeech(Context context) {
        mContext = context;
        textToSpeech = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                    LoggerDDF.e(TAG, "onInit");
//                    LoggerDDF.e(TAG+"@25", ConstantsApi.strTSReceived+" - "+ConstantsActivity.STR_CURRENCY_CODE);
                    LoggerDDF.e(TAG+"@25", ConstantsApi.strTSReceived+" - "+ConstantsApi.strTSDigitalCurrencyType);

                    double receivedAmount = Double.parseDouble(ConstantsApi.strTSReceived);
                    BigDecimal bigDecimalAmount = BigDecimal.valueOf(receivedAmount);
//                    speechNow(mContext,"Received Amount " + ConstantsApi.strTSReceived+ " of " + ConstantsActivity.STR_CURRENCY_CODE);
//                    String stringWithSpaceAfterEvery4thChar = ConstantsActivity.STR_CURRENCY_CODE.replace("....", "$0 ")
                    StringBuilder sb = new StringBuilder();
                    for (char c: ConstantsApi.strTSDigitalCurrencyType.toCharArray()) {
                        sb.append(c).append(" ");
                    }
                    String strCodeAlphabets = sb.toString().trim();
                    Log.e("strCodeAlphabets",strCodeAlphabets);
//                    speechNow(mContext,"Transaction "+ConstantsActivity.STR_TRANSACTION_STATUS+" And Received Amount " + bigDecimalAmount.toPlainString() + " of " + ConstantsActivity.STR_CURRENCY_CODE);
                    speechNow(mContext,"Transaction "+ConstantsActivity.STR_TRANSACTION_STATUS+" And Received Amount " + bigDecimalAmount.toPlainString() + " of " + strCodeAlphabets);
                }
            }
        });
    }

    public void speechNow(Context context, String strSpeech) {
        LoggerDDF.e(TAG, "speechNow");
        LoggerDDF.e(TAG, strSpeech+"");
        mContext = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(strSpeech,TextToSpeech.QUEUE_FLUSH,null,null);
        } else {
            textToSpeech.speak(strSpeech, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void setTextToSpeech(Context context) {
        LoggerDDF.e(TAG, "setTextToSpeech");
        mContext = context;
        textToSpeech = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                // if No error is found then only it will run
                textToSpeech.setLanguage(Locale.US);
                if (i != TextToSpeech.ERROR) {
                    // To Choose language of speech
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });
        textToSpeech.setLanguage(Locale.US);
        textToSpeech.speak("hello word", TextToSpeech.QUEUE_FLUSH, null, "hello world");
//        textToSpeech.speak("hello word",TextToSpeech.QUEUE_FLUSH,null,"hello world");
//        textToSpeech.speak("Welcome",TextToSpeech.QUEUE_FLUSH,null);

    }
}
