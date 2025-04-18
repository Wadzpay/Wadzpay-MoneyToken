package com.wadzpay.ddflibrary.sharedpreference;


import android.content.Context;
import android.content.SharedPreferences;

import com.wadzpay.ddflibrary.R;

/* Shared Preference Class Operations */
public class SharedPreferenceDDF {
    private String TAG = getClass().getSimpleName();
    private static SharedPreferenceDDF instance = null;

    private SharedPreferenceDDF() {
    }

    public static SharedPreferenceDDF getInstance() {
        if (instance == null) {
            instance = new SharedPreferenceDDF();
        }
        return instance;
    }

    /* save value with key */
    public void putStringValue(Context context, String key, String value) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(ConstantsSP.SHARED_PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
        editor.commit();
    }

    /* retrieve string value with key */
    public String getStringValue(Context context, String key) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(ConstantsSP.SHARED_PREFERENCE_NAME, 0);
        String value = pref.getString(key, context.getString(R.string.app_name));
        return value;
    }

    /* save int value with key */
    public void putIntValue(Context context, String key, int value) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(ConstantsSP.SHARED_PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.apply();
        editor.commit();
    }

    /* retrieve int value with key */
    public int getIntValue(Context context, String key) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(ConstantsSP.SHARED_PREFERENCE_NAME, 0);
        int value = pref.getInt(key, 999);
        return value;
    }
}
