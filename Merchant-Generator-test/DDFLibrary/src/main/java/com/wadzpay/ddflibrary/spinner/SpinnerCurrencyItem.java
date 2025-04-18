package com.wadzpay.ddflibrary.spinner;

public class SpinnerCurrencyItem {
    private String mCurrencyCode;
    private String mCurrencyName;
    private String mCurrencyAmount;
    private String mTotalAmount;
    private String mWadzPayFee;
    private int mCurrencyImage;

    public SpinnerCurrencyItem(String currencyCode, String currencyName, String mCurrencyAmount, int currencyImage) {
        mCurrencyCode = currencyCode;
        this.mCurrencyName = currencyName;
        this.mCurrencyAmount = mCurrencyAmount;
        mCurrencyImage = currencyImage;
    }

    public String getCurrencyCode() {
        return mCurrencyCode;
    }

    public int getFlagImage() {
        return mCurrencyImage;
    }

    public String getCurrencyName() {
        return mCurrencyName;
    }

    public String getCurrencyAmount() {
        return mCurrencyAmount;
    }
    public String getWadzPayFee() {
        return mCurrencyAmount;
    }

}
