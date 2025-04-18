package com.wadzpay.ddflibrary.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wadzpay.ddflibrary.R;
import com.wadzpay.ddflibrary.callbacks.ActivityCallBack;
import com.wadzpay.ddflibrary.callbacks.DialogCallBack;
import com.wadzpay.ddflibrary.logs.LoggerDDF;
import com.wadzpay.ddflibrary.utils.ConstantsActivity;


public class DialogCustomize {
    private String TAG = getClass().getSimpleName();

    private static DialogCustomize instance = null;

    private DialogCustomize() {
    }

    public static DialogCustomize getInstance() {
        if (instance == null) {
            instance = new DialogCustomize();
        }
        return instance;
    }

    private Dialog dialogDisplay;
    private Context mContext;
    private Activity mActivity;
    /*public DialogCustom(Context context) {
        mContext = context;
//        mActivity = (Activity) mContext;
    }*/

    /* progress dialog */
    public void displayProgressDialog(Context context, String flagDisplayDismiss) {
        if (flagDisplayDismiss.equals(ConstantsDialog.FLAG_DIALOG_DISPLAY)) {
            dialogDisplay = new Dialog(context);
//            dialogDisplay.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            if (!dialogDisplay.isShowing()) {
                dialogDisplay.setCancelable(false);
                dialogDisplay.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogDisplay.setContentView(R.layout.dialog_progress);
                dialogDisplay.show();
            }
        } else if (flagDisplayDismiss.equals(ConstantsDialog.FLAG_DIALOG_DISMISS)) {
            if (dialogDisplay!=null && dialogDisplay.isShowing()) {
                dialogDisplay.dismiss();
            }
        }

    }

    /* dialog display */
    public void displayDialog(Context context, String flagDisplayDismiss, String title, String msg) {
        activityCallback = (ActivityCallBack) context;
        if (flagDisplayDismiss.equals(ConstantsDialog.FLAG_DIALOG_DISPLAY)) {
            dialogDisplay = new Dialog(context);
            //            dialogDisplay = new Dialog(context, R.style.NewDialog);
//            dialogDisplay.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            if (!dialogDisplay.isShowing()) {
                dialogDisplay.setCancelable(false);
                dialogDisplay.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogDisplay.setContentView(R.layout.dialog_alert_info);
                TextView tv_d_title = dialogDisplay.findViewById(R.id.tv_d_title);
                TextView tv_d_msg = dialogDisplay.findViewById(R.id.tv_d_msg);
                tv_d_title.setText(title);
                tv_d_msg.setText(msg);
                Button btn_d_ok = dialogDisplay.findViewById(R.id.tv_d_ok);
                btn_d_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogDisplay.dismiss();
                        activityCallback.activityCall(ConstantsActivity.STR_FLAG_DIALOG);
                    }
                });
                Button btn_d_cancel = dialogDisplay.findViewById(R.id.tv_d_cancel);
                btn_d_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogDisplay.dismiss();
                    }
                });

                dialogDisplay.show();
            }
        } else if (flagDisplayDismiss.equals(ConstantsDialog.FLAG_DIALOG_DISMISS)) {
            if (dialogDisplay.isShowing()) {
                dialogDisplay.dismiss();
            }
        }
    }

    /* dialog validation */
    public void displayValidationDialog(Context context, String flagDisplayDismiss, String title, String msg) {
        mContext = context;
        dialogCallBack = (DialogCallBack) context;
        if (flagDisplayDismiss.equals(ConstantsDialog.FLAG_DIALOG_DISPLAY)) {
            dialogDisplay = new Dialog(context);
            if (!dialogDisplay.isShowing()) {
                dialogDisplay.setCancelable(false);
                dialogDisplay.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogDisplay.setContentView(R.layout.dialog_alert_validations);
                TextView tv_d_title = dialogDisplay.findViewById(R.id.tv_d_title);
                TextView tv_d_msg = dialogDisplay.findViewById(R.id.tv_d_msg);
                tv_d_title.setText(title);
                tv_d_msg.setText(msg);
                TextView tv_d_ok = dialogDisplay.findViewById(R.id.tv_d_ok);
                tv_d_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogDisplay.dismiss();
                        dialogCallBack.sendDialogFlag(ConstantsDialog.FLAG_DIALOG_SUBMIT_VEHICLE);
                    }
                });
                dialogDisplay.show();
            }
        } else if (flagDisplayDismiss.equals(ConstantsDialog.FLAG_DIALOG_DISMISS)) {
            if (dialogDisplay.isShowing()) {
                dialogDisplay.dismiss();
            }
        }
    }

    /* dialog with save or cancel */
    private DialogCallBack dialogCallBack;

    public void displayCancelDialog(Context context, String flagDisplayDismiss, String title, String msg) {
        mContext = context;
        dialogCallBack = (DialogCallBack) context;
        if (flagDisplayDismiss.equals(ConstantsDialog.FLAG_DIALOG_DISPLAY)) {
            dialogDisplay = new Dialog(context);
            if (!dialogDisplay.isShowing()) {
                dialogDisplay.setCancelable(true);
                dialogDisplay.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogDisplay.setContentView(R.layout.dialog_cancel);
                TextView tv_d_title = dialogDisplay.findViewById(R.id.tv_d_title);
//                TextView tv_d_msg = dialogDisplay.findViewById(R.id.tv_d_msg);
                tv_d_title.setText(title);
//                tv_d_msg.setText(msg);
                TextView tv_d_yes_discard = dialogDisplay.findViewById(R.id.tv_d_yes_discard);
                tv_d_yes_discard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogDisplay.dismiss();
//                        activityDiscard();
//                        dialogCallBack.sendDialogFlag(ConstantsDialog.FLAG_DIALOG_CANCEL_YES);
                    }
                });
                TextView tv_d_save_later = dialogDisplay.findViewById(R.id.tv_d_save_later);
                tv_d_save_later.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogDisplay.dismiss();
//                        activitySaveLater();
//                        dialogCallBack.sendDialogFlag(ConstantsDialog.FLAG_DIALOG_CANCEL_NO);
                    }
                });
                dialogDisplay.show();
            }
        } else if (flagDisplayDismiss.equals(ConstantsDialog.FLAG_DIALOG_DISMISS)) {
            if (dialogDisplay.isShowing()) {
                dialogDisplay.dismiss();
            }
        }
    }

    /* dialog display */
/*
    public void displayDialogCamera(Context context, String flagDisplayDismiss, String title, String msg) {
        mContext = context;
        dialogCallBack = (DialogCallBack) context;
        if (flagDisplayDismiss.equals(ConstantsDialog.FLAG_DIALOG_DISPLAY)) {
            dialogDisplay = new Dialog(context);
            if (!dialogDisplay.isShowing()) {
                dialogDisplay.setCancelable(true);
                dialogDisplay.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogDisplay.setContentView(R.layout.dialog_camera);

                LinearLayout lyt_image_capture = dialogDisplay.findViewById(R.id.lyt_image_capture);
                lyt_image_capture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogCallBack.sendDialogFlag(ConstantsDialog.FLAG_DIALOG_IMAGE_CAPTURE);
                        dialogDisplay.dismiss();
                    }
                });

                LinearLayout lyt_video_capture = dialogDisplay.findViewById(R.id.lyt_video_capture);
                lyt_video_capture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogCallBack.sendDialogFlag(ConstantsDialog.FLAG_DIALOG_VIDEO_CAPTURE);
                        dialogDisplay.dismiss();
                    }
                });
                dialogDisplay.show();
            }
        } else if (flagDisplayDismiss.equals(ConstantsDialog.FLAG_DIALOG_DISMISS)) {
            if (dialogDisplay.isShowing()) {
                dialogDisplay.dismiss();
            }
        }
    }
*/

    private ActivityCallBack activityCallback;
    final int[] checkedItem = {-1};
    int selectedChoice = 0;

    public void displaySingleChoiceDialog(Context context, String flagDisplayDismiss) {
        activityCallback = (ActivityCallBack) context;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setIcon(R.drawable.ic_launcher);
        alertDialog.setTitle("Choose Option");
        final String[] listItems = new String[]{"Enter Amount", "Choose Amount"};
        alertDialog.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkedItem[0] = which;
                selectedChoice = which;
                dialog.dismiss();
                if (listItems[which].equalsIgnoreCase("Enter Amount")) {
                    displayEditTextDialog(context, "");
                } else {
                    displayArrayDialog(context, "");
                }

            }
        });

        /*alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (listItems[selectedChoice].equalsIgnoreCase("Enter Amount")) {
                    displayEditTextDialog(context, "");
                } else {
                    displayArrayDialog(context, "");
                }
                dialog.dismiss();
            }
        });*/

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog customAlertDialog = alertDialog.create();
        customAlertDialog.show();
    }

    public void displayEditTextDialog(Context context, String flagDisplayDismiss) {
        activityCallback = (ActivityCallBack) context;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
//        AlertDialog.Builder alertDialog =new AlertDialog.Builder(mContext, R.style.dia);
        alertDialog.setTitle("Enter Amount");
//        alertDialog.setMessage("0");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(10, 10, 10, 10);

        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.ic_launcher);

        alertDialog.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String etAmountValue = input.getText().toString();
                        LoggerDDF.e(TAG, etAmountValue + "");
                        ConstantsActivity.STR_CURRENCY_ET = etAmountValue;
                        activityCallback.activityCall("enter");
                    }
                });

        alertDialog.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    CharSequence[] itemsDialog;
    String arrayDialogTitle = "";
    public void displayArrayDialog(Context context, String flagDisplayDismiss) {
        activityCallback = (ActivityCallBack) context;
//        ArrayList<String> selectedItems = new ArrayList<String>();
        final CharSequence[] itemsAmounts = {
                "10","100", "200", "300", "400", "500",
                "600", "700", "800", "900", "1000"
        };
        final CharSequence[] itemsQrFormat = {
                "QR Format 1", "QR Format 2", "QR Format 3",
                "QR Format 4", "QR Format 5"
        };
        if(flagDisplayDismiss.equals(ConstantsActivity.STR_FLAG_CHOICE_AMOUNT_DIALOG)){
            itemsDialog = itemsAmounts;
            arrayDialogTitle = "Choose Amount";
        } else if(flagDisplayDismiss.equals(ConstantsActivity.STR_FLAG_CHOICE_QR_FORMAT_DIALOG)){
            itemsDialog = itemsQrFormat;
            arrayDialogTitle = "Choose Format";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(arrayDialogTitle);
        builder.setItems(itemsDialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                LoggerDDF.e("i", i + "");
                LoggerDDF.e("items", itemsDialog[i] + "");
                if(flagDisplayDismiss.equals(ConstantsActivity.STR_FLAG_CHOICE_AMOUNT_DIALOG)){
                    ConstantsActivity.STR_CURRENCY_ET = itemsDialog[i] + "";
                }  else if(flagDisplayDismiss.equals(ConstantsActivity.STR_FLAG_CHOICE_QR_FORMAT_DIALOG)){
                    ConstantsActivity.STR_FORMAT_ET = itemsDialog[i] + "";
                }
                activityCallback.activityCall(flagDisplayDismiss);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.show();
    }

}
