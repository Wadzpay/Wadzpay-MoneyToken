package com.wadzpay.ddflibrary.spinner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wadzpay.ddflibrary.R;
import com.wadzpay.ddflibrary.utils.ConstantsActivity;
import com.wadzpay.ddflibrary.utils.CustomOperations;

import java.math.BigDecimal;
import java.util.ArrayList;

public class SpinnerCurrencyAdapter extends ArrayAdapter<SpinnerCurrencyItem> {
    private String TAG = getClass().getSimpleName();

    public SpinnerCurrencyAdapter(Context context, ArrayList<SpinnerCurrencyItem> currencyList) {
        super(context, 0, currencyList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        Log.e(TAG,position+"");
        /*View viewSpinner = null;
        viewSpinner = super.getDropDownView(position, null, parent);
        if (position == PaymentRequestDDFActivity.selectedPosition) {
            viewSpinner.setBackgroundColor(Color.BLUE);
        } else{
            viewSpinner.setBackgroundColor(Color.WHITE);
        }*/
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        SpinnerCurrencyItem currentItem = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.item_spinner_currency_row, parent, false
            );
        }

        ImageView ivCurrencyCode = convertView.findViewById(R.id.iv_currency_code);
        TextView tvCurrencyCode = convertView.findViewById(R.id.tv_currency_code);
        TextView tvCurrencyName = convertView.findViewById(R.id.tv_currency_name);
        TextView tvAmountSpinnerCurrency = convertView.findViewById(R.id.tv_amount_spinner_currency);

        if (currentItem != null) {
            double dAmount = Double.parseDouble(currentItem.getCurrencyAmount());
            String strAmount = String.format("%.8f", dAmount);

//            ivCurrencyCode.setImageResource(currentItem.getFlagImage());
//            ivCurrencyCode.setImageResource(CustomOperations.getCurrencyImageResource("bbb"));
            ivCurrencyCode.setImageResource(CustomOperations.getCurrencyImageResource(currentItem.getCurrencyCode()));
            if(currentItem.getCurrencyCode().equalsIgnoreCase("sart")){
                tvCurrencyCode.setText(ConstantsActivity.STR_SAR_CODE+"");
            } else{
                tvCurrencyCode.setText(currentItem.getCurrencyCode()+"");
            }
            tvCurrencyName.setText(currentItem.getCurrencyName()+"");
            tvCurrencyName.setVisibility(View.GONE);
//            tvAmountSpinnerCurrency.setText(currentItem.getCurrencyAmount()+"");
//            tvAmountSpinnerCurrency.setText(strAmount+"");

            BigDecimal bigDecimalAmount = BigDecimal.valueOf(dAmount);
//            tvAmountSpinnerCurrency.setText(strAmount+"");
            tvAmountSpinnerCurrency.setText(bigDecimalAmount.toPlainString()+"");
        }
        /*if(position == PaymentRequestDDFActivityK){
            convertView.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.app_color));
            tvCurrencyCode.setTextColor(Color.WHITE);
            tvCurrencyName.setTextColor(Color.WHITE);
            tvAmountSpinnerCurrency.setTextColor(Color.WHITE);
        } else {
            convertView.setBackgroundColor(Color.WHITE);
            tvCurrencyCode.setTextColor(Color.BLACK);
            tvCurrencyName.setTextColor(Color.BLACK);
            tvAmountSpinnerCurrency.setTextColor(Color.BLACK);
        }*/

        return convertView;
    }
}
