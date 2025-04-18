package com.wadzpay.ddflibrary.callbacks;

public interface ApiCallback {
//     void sendResult(boolean isSuccess);
     void apiSendResponse(String strResponse);
     void apiCallBackFailed(String strResponse);
}
