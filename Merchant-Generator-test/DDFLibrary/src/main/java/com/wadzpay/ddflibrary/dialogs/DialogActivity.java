package com.wadzpay.ddflibrary.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wadzpay.ddflibrary.R;

public class DialogActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Transparent);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        DialogCustomize dialogCustomize = DialogCustomize.getInstance();
//        dialogCustomize.displayProgressDialog(this, ConstantsDialog.FLAG_DIALOG_DISPLAY);
//        dialogCustomize.displayDialog(this, ConstantsDialog.FLAG_DIALOG_DISPLAY, "Title","Message");
        displayAlert(this);
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
