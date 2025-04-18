package com.wadzpay.ddflibrary.dialogs;


import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ToastCustomize {
    private static ToastCustomize instance = null;
    private ToastCustomize() {}

    public static ToastCustomize getInstance() {
        if(instance == null) {
            instance = new ToastCustomize();
        }
        return instance;
    }

    /* toast */
    public static void displayToast(Context context, String strToast){
        Toast toast = Toast.makeText(context, strToast, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
