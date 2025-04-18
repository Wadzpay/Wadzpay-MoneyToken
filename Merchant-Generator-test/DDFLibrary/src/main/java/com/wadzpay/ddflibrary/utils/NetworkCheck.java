package com.wadzpay.ddflibrary.utils;

import android.content.Context;
import android.net.ConnectivityManager;

import com.wadzpay.ddflibrary.R;
import com.wadzpay.ddflibrary.dialogs.ToastCustomize;


/* Custom Network Class with internet check  */
public class NetworkCheck {
    private Context mNetworkContext;
    private static NetworkCheck instance = null;

    private NetworkCheck() {
    }

    public static NetworkCheck getInstance() {
        if (instance == null) {
            instance = new NetworkCheck();
        }
        return instance;
    }
//    public NetworkCheck(Context context){
//        mNetworkContext = context;
//    }

    /* Network Availability Check */
    public boolean isNetworkConnected(Context context) {
        mNetworkContext = context;
        ConnectivityManager cm = (ConnectivityManager) mNetworkContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isNetworkConnected = cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
        if (!isNetworkConnected) {
            ToastCustomize.displayToast(mNetworkContext,mNetworkContext.getString(R.string.network_unavailable));
        }
        return isNetworkConnected;
    }
}
