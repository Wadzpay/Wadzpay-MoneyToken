package com.wadzpay.ddflibrary.broadcastreceiver;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.wadzpay.ddflibrary.activities.LoginActivity;
import com.wadzpay.ddflibrary.activities.SplashScreenActivity;
import com.wadzpay.ddflibrary.dialogs.ConstantsDialog;
import com.wadzpay.ddflibrary.dialogs.DialogActivity;
import com.wadzpay.ddflibrary.dialogs.DialogCustomize;
import com.wadzpay.ddflibrary.dialogs.ToastCustomize;
import com.wadzpay.ddflibrary.logs.LoggerDDF;

public class BroadCastReceiverCustom extends BroadcastReceiver {
    private String TAG = getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        LoggerDDF.e(TAG, "onReceive");
        /*Runnable runnable = new Runnable() {
            @Override
            public void run() {
                DialogCustomize dialogCustomize = DialogCustomize.getInstance();
                dialogCustomize.displayProgressDialog(context, ConstantsDialog.FLAG_DIALOG_DISPLAY);

            }
        };
        runnable.run();*/
        ToastCustomize toastCustomize = ToastCustomize.getInstance();
        toastCustomize.displayToast(context, "BroadCastReceiverCustom");
//        displayAlert(context);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                displayAlert(context);
                Intent i = new Intent(context, DialogActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }, 400);
    }

    private void displayAlert(Context context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("I am Title");
        dialog.setMessage("I am testing the message content");
        dialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "Click to confirm", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "click to cancel", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
