package com.wadzpay.ddflibrary.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.wadzpay.ddflibrary.R;
import com.wadzpay.ddflibrary.utils.ConstantsActivity;

public class RadioOptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio_options);
        initUI();
    }

    private RadioGroup radioGroupRG;
    private RadioButton radioButtonRG;
    private Button btnSubmitRG;

    private void initUI() {
        radioGroupRG = findViewById(R.id.radioGroup_rg);
        btnSubmitRG = findViewById(R.id.btn_submit_rg);

        btnSubmitRG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = radioGroupRG.getCheckedRadioButtonId();
                radioButtonRG = findViewById(selectedId);
                ConstantsActivity.STR_RADIO_OPTION = radioButtonRG.getText().toString();
                openNextActivity();
            }
        });
    }

    private void openNextActivity() {
        Intent i = new Intent(RadioOptionsActivity.this, PaymentRequestDDFActivityK.class);
        startActivity(i);
        finish();
    }
}
