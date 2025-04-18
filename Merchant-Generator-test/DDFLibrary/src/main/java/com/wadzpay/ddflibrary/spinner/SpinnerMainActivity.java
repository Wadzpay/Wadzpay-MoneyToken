package com.wadzpay.ddflibrary.spinner;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wadzpay.ddflibrary.R;

import java.util.ArrayList;

public class SpinnerMainActivity extends AppCompatActivity {
    private ArrayList<SpinnerCurrencyItem> mCurrencyList;
    private SpinnerCurrencyAdapter mCurrencyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spinner_main);

//        initList();

        Spinner spinnerCurrencies = findViewById(R.id.spinner_currencies);

        mCurrencyAdapter = new SpinnerCurrencyAdapter(this, mCurrencyList);
        spinnerCurrencies.setAdapter(mCurrencyAdapter);

        spinnerCurrencies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerCurrencyItem clickedItem = (SpinnerCurrencyItem) parent.getItemAtPosition(position);
                String clickedCurrencyName = clickedItem.getCurrencyCode();
                Toast.makeText(SpinnerMainActivity.this, clickedCurrencyName + " selected", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button btn_spinner = findViewById(R.id.btn_spinner);
        btn_spinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinnerCurrencies.performClick();
            }
        });
    }

   /* private void initList() {
        mCurrencyList = new ArrayList<>();
        mCurrencyList.add(new SpinnerCurrencyItem("BTC", "Bitcoin", "0.00001", R.drawable.));
        mCurrencyList.add(new SpinnerCurrencyItem("ETH", "Ethereum", "0.00001", R.drawable.));
        mCurrencyList.add(new SpinnerCurrencyItem("BNB", "BNB", "0.00001", R.drawable.));
        mCurrencyList.add(new SpinnerCurrencyItem("XRP", "Ripple", "0.00001", R.drawable.));
        mCurrencyList.add(new SpinnerCurrencyItem("USD", "US Dollar", "0.00001", R.drawable.));
        mCurrencyList.add(new SpinnerCurrencyItem("USDT", "US Dollar", "0.00001", R.drawable.));
    }*/
}