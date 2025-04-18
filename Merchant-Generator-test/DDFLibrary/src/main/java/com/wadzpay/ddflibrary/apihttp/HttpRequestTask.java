package com.wadzpay.ddflibrary.apihttp;

import android.app.Activity;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public class HttpRequestTask extends AsyncTask<Void, Void, HttpResponse> {

    private final HttpRequestConstants httpRequestConstants;
    private final HttpRequestConstants.Handler handler;
    private final WeakReference<Activity> activityRef;
    private final boolean activityRefSet;

    /**
     * start an async task for handling the http request and given a response handler
     * @param httpRequestConstants
     * @param handler
     */
    public HttpRequestTask(HttpRequestConstants httpRequestConstants, HttpRequestConstants.Handler handler) {
        this(httpRequestConstants, handler, null);
    }

    /**
     * start an async task for handling the http request, a response handler and the activity that is sending this request,
     * and it will check if the activity is not finished before calling the response handler
     * @param httpRequestConstants
     * @param handler
     * @param activity
     */
    public HttpRequestTask(HttpRequestConstants httpRequestConstants, HttpRequestConstants.Handler handler, Activity activity) {
        this.httpRequestConstants = httpRequestConstants;
        this.handler = handler;
        this.activityRef = new WeakReference<>(activity);
        this.activityRefSet = activity != null;
    }

    @Override
    protected HttpResponse doInBackground(Void... params) {
        return httpRequestConstants.request();
    }

    @Override
    protected void onPostExecute(final HttpResponse response) {
        handleResponse(response);
    }

    @Override
    protected void onCancelled(){
        handleResponse(new HttpResponse());
    }

    private void handleResponse(HttpResponse response) {

        if (handler == null)
            return;

        if (!activityRefSet)
            handler.response(response);
        else if (activityRef.get() != null && !activityRef.get().isFinishing())
            handler.response(response);
    }
}
