package com.wadzpay.ddflibrary.activities


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import com.wadzpay.ddflibrary.R
import com.wadzpay.ddflibrary.api.ApiRetroFitPaymentInfo
import com.wadzpay.ddflibrary.api.ApiService
import com.wadzpay.ddflibrary.api.ConstantsApi
import com.wadzpay.ddflibrary.awsamplify.AwsAmplifyOperations
import com.wadzpay.ddflibrary.callbacks.ActivityCallBack
import com.wadzpay.ddflibrary.dialogs.ConstantsDialog
import com.wadzpay.ddflibrary.dialogs.DialogCustomize
import com.wadzpay.ddflibrary.dialogs.ToastCustomize
import com.wadzpay.ddflibrary.logs.LoggerDDF
import com.wadzpay.ddflibrary.sharedpreference.SharedPreferenceDDF
import com.wadzpay.ddflibrary.spinner.SpinnerCurrencyAdapter
import com.wadzpay.ddflibrary.spinner.SpinnerCurrencyItem
import com.wadzpay.ddflibrary.utils.ConstantsActivity
import com.wadzpay.ddflibrary.utils.CustomOperations
import org.json.JSONObject
import java.math.BigDecimal
import java.util.regex.Matcher
import java.util.regex.Pattern


class PaymentRequestDDFActivityK : AppCompatActivity(), ActivityCallBack {
    private val TAG = javaClass.simpleName

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_request_ddf_radio)
        Log.e("DDFActivityK", "PaymentRequestDDFActivityK");
        initUI()
//        getConversionRates()
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    private fun getConversionRates() {
//        ConstantsApi.FLAG_API_ACTIVITY_FAILED = "FiatTypeActivity"
        ConstantsApi.FLAG_API_NAME = ConstantsApi.FLAG_API_CONVERSION
        val serviceIntent = Intent(this, ApiService::class.java)
        startService(serviceIntent)
    }

    lateinit var etBtcReqPay: EditText
    lateinit var btnRadioAmountReqPay: Button
    lateinit var btnConversionsReqPay: Button
    lateinit var btnProceedReqPay: Button
    lateinit var tvSelectAmountReqPay: TextView
    lateinit var tvEtHintBtcReqPay: TextView
    lateinit var tv_fiat_two_req_pay: TextView
    lateinit var tvCurrencyAmountModeReqPay: TextView
    lateinit var tv_currency_code_select_req_pay: TextView
    lateinit var tvCurrencyCodeReqPay: TextView
    lateinit var tvCurrencyNameReqPay: TextView
    lateinit var tv_et_hint_btc_req_pay1: TextView
    lateinit var tv_et_hint_btc_req_pay2: TextView
    lateinit var tv_qr_format_req_pay: TextView
    lateinit var tv_profile_req_pay: TextView
    lateinit var iv_select_req_pay: ImageView
    lateinit var ivCurrencyCodeImgReqPay: ImageView
    lateinit var rlConversionsOptionsReqPay: RelativeLayout
    lateinit var rl_spinner_currency_select_req_pay: RelativeLayout
    lateinit var rlBottomReqPay: RelativeLayout
    lateinit var rl_qr_format_req_pay: RelativeLayout
    lateinit var rb_et_req_pay: RadioButton
    lateinit var rb_btn_req_pay: RadioButton
    lateinit var rg_req_pay: RadioGroup
    lateinit var et_radio_amount_req_pay: EditText
    lateinit var lyt_profile_req_pay: LinearLayout
    lateinit var iv_profile_req_pay: ImageView
    var bDisclaimer = false
    var dAmount = 0.0
    var dFeeAmount = 0.0
    var dTotalAmount = 0.0
    var apiRetroFitPaymentInfo = ApiRetroFitPaymentInfo()
    @SuppressLint("SuspiciousIndentation")
    private fun initUI() {
        paymentRequestDDFActivityK = this@PaymentRequestDDFActivityK
        rlConversionsOptionsReqPay =
            findViewById<RelativeLayout>(R.id.rl_conversions_options_req_pay)
        rlConversionsOptionsReqPay.visibility = View.GONE
        rlBottomReqPay = findViewById<RelativeLayout>(R.id.rl_bottom_req_pay)
        rl_qr_format_req_pay = findViewById<RelativeLayout>(R.id.rl_qr_format_req_pay)
//        rlBottomReqPay.visibility = View.GONE

        etBtcReqPay = findViewById(R.id.et_btc_req_pay)
        etBtcReqPay.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(10, 2))

        tvEtHintBtcReqPay = findViewById(R.id.tv_et_hint_btc_req_pay)
        tv_et_hint_btc_req_pay1 = findViewById(R.id.tv_et_hint_btc_req_pay1)
        tv_et_hint_btc_req_pay2 = findViewById(R.id.tv_et_hint_btc_req_pay2)
        if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT === "GEIDEA") {
            tv_et_hint_btc_req_pay2.text = "SAR"
        } else {

        }
        tv_qr_format_req_pay = findViewById(R.id.tv_qr_format_req_pay)
        tv_profile_req_pay = findViewById(R.id.tv_profile_req_pay)
        iv_select_req_pay = findViewById(R.id.iv_select_req_pay)
        ivCurrencyCodeImgReqPay = findViewById<ImageView>(R.id.iv_currency_code_img_req_pay)
        tvEtHintBtcReqPay.setOnClickListener { openFiatActivity() }
        tv_et_hint_btc_req_pay1.setOnClickListener { openFiatActivity() }
        tv_et_hint_btc_req_pay2.setOnClickListener { openFiatActivity() }

        tv_fiat_two_req_pay = findViewById(R.id.tv_fiat_two_req_pay)
        tv_fiat_two_req_pay.setOnClickListener {
            openFiatActivity()
//            openCurrencyActivity()
        }
        getMerchantDetails()

        //        tv_email_user_set.setText(ConstantsApi.strMDEmail);
//        tv_email_user_set.setText(Amplify.Auth.getCurrentUser().getUsername()+"");
//        tv_email_user_set.setText(ConstantsActivity.STR_USER_ID);
        val sharedPreferenceDDF = SharedPreferenceDDF.getInstance()
        ConstantsActivity.STR_USER_EMAIL =
            sharedPreferenceDDF.getStringValue(this, ConstantsActivity.STR_KEY_SP_USER_EMAIL)
        ConstantsActivity.STR_USER_PASSWORD =
            sharedPreferenceDDF.getStringValue(this, ConstantsActivity.STR_KEY_SP_USER_PASSWORD)

        val awsAmplifyOperations = AwsAmplifyOperations.getInstance(this);
//        awsAmplifyOperations.passContext(this)
        val ivBackCommon = findViewById<ImageView>(R.id.iv_back_common)
        ivBackCommon.setOnClickListener { finish() }
        val ivSignOutReqPay = findViewById<ImageView>(R.id.iv_sign_out_req_pay)
        ivSignOutReqPay.setOnClickListener {
            awsAmplifyOperations.amplifySignOut();
        }
        val lyt_profile_req_pay = findViewById<LinearLayout>(R.id.lyt_profile_req_pay)
        lyt_profile_req_pay.setOnClickListener {
//            val intent = Intent(this, zTestActivity::class.java)
            val intent = Intent(this, UserSettings::class.java)
            startActivity(intent)
        }
        val iv_profile_req_pay = findViewById<ImageView>(R.id.iv_profile_req_pay)
        iv_profile_req_pay.setOnClickListener {
//            val intent = Intent(this, zTestActivity::class.java)
            val intent = Intent(this, UserSettings::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent)
        }

        val rlCancelReqPay =
            findViewById<RelativeLayout>(R.id.rl_cancel_req_pay)
        rlCancelReqPay.setOnClickListener {
//            val intent = Intent(this, PaymentRequestDDFActivityK::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//            startActivity(intent)

            startActivity(getIntent());
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
//            recreate()
            ConstantsActivity.STR_CURRENCY_ET = ""
            if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT === "GEIDEA") {
                ConstantsActivity.STR_CURRENCY_FIAT_TYPE = "SAR"
            } else {
                ConstantsActivity.STR_CURRENCY_FIAT_TYPE = "FIAT"
            }
        }

        rl_qr_format_req_pay.setOnClickListener {
            dialogCustomize.displayArrayDialog(
                this@PaymentRequestDDFActivityK,
                ConstantsActivity.STR_FLAG_CHOICE_QR_FORMAT_DIALOG
            );
        }

        btnProceedReqPay = findViewById<Button>(R.id.btn_proceed_req_pay)
        btnProceedReqPay.setOnClickListener(View.OnClickListener {
            ConstantsActivity.STR_CURRENCY_ET = etBtcReqPay.getText().toString()
            if(ConstantsActivity.STR_CURRENCY_ET.isEmpty()){
                ToastCustomize.displayToast(
                    this@PaymentRequestDDFActivityK,
                    "Invalid Amount"
                )
                return@OnClickListener
            }
            val dNumber: BigDecimal = ConstantsActivity.STR_CURRENCY_ET.toBigDecimal()
            ConstantsActivity.STR_CURRENCY_ET = dNumber.toString()
//            ConstantsActivity.STR_CURRENCY_ET = removeTrailingZeros(dNumber)
                if ((tv_currency_code_select_req_pay.text.toString()).equals(getString(R.string.select_digital_asset))) {
                ToastCustomize.displayToast(
                    this@PaymentRequestDDFActivityK,
                    getString(R.string.invalid_currency)
                )
                return@OnClickListener
            }
            if (ConstantsActivity.STR_CURRENCY_ET == null || ConstantsActivity.STR_CURRENCY_ET.length == 0) {
                Toast.makeText(
                    applicationContext,
                    "Please Enter Valid Amount",
                    Toast.LENGTH_SHORT
                )
                    .show()

                return@OnClickListener
            }
//            ToastCustomize.displayToast(this@PaymentRequestDDFActivityK,rb_btn_req_pay.text.toString()+"")
            if ((rb_btn_req_pay.text.toString()).equals(getString(R.string.select_digital_asset))) {
                val toastCustomize = ToastCustomize.getInstance()
                ToastCustomize.displayToast(
                    this@PaymentRequestDDFActivityK,
                    getString(R.string.invalid_currency)
                )
                return@OnClickListener
            }
            CustomOperations.getCurrencyImageResource(ConstantsActivity.STR_CURRENCY_CODE)
            ConstantsActivity.STR_FORMAT_ET = "QR Format 5";
            apiRetroFitPaymentInfo.paymentInfoRetrofit(this@PaymentRequestDDFActivityK)

        })

        btnConversionsReqPay = findViewById<Button>(R.id.btn_conversions_req_pay)
        btnConversionsReqPay.setOnClickListener(View.OnClickListener {
            LoggerDDF.e(TAG, "btnConversionsReqPay")
            ConstantsActivity.flag_fiat_selected = false
            callConversionApi()
//            ConstantsActivity.STR_CURRENCY_ET = etBtcReqPay.getText().toString()
        })

        val rlProceedReqPay = findViewById<RelativeLayout>(R.id.rl_proceed_req_pay)
//        rlProceedReqPay.setBackgroundResource(R.drawable.btn_corners_gray)
        rlProceedReqPay.setOnClickListener(View.OnClickListener {
            ConstantsActivity.STR_CURRENCY_ET = etBtcReqPay.getText().toString()
            if(ConstantsActivity.STR_CURRENCY_ET.isEmpty()){
                ToastCustomize.displayToast(
                    this@PaymentRequestDDFActivityK,
                    "Invalid Amount"
                )
                return@OnClickListener
            }

            if (ConstantsApi.strPosID.isEmpty()) {
                ToastCustomize.displayToast(
                    this@PaymentRequestDDFActivityK,
                    "Invalid POS"
                )
                ConstantsApi.FLAG_API_NAME = ConstantsApi.FLAG_API_ADD_POS_MERCHANT
//                apiRetroFitPaymentInfo.addPosToMerchant(this@PaymentRequestDDFActivityK)
            }

//            val dNumber: Double = ConstantsActivity.STR_CURRENCY_ET.toDouble()
            val dNumber: BigDecimal = ConstantsActivity.STR_CURRENCY_ET.toBigDecimal()
            ConstantsActivity.STR_CURRENCY_ET = dNumber.toString()
//            ConstantsActivity.STR_CURRENCY_ET = removeTrailingZeros(dNumber)
            if (ConstantsActivity.STR_CURRENCY_ET == null || ConstantsActivity.STR_CURRENCY_ET.length == 0) {
                Toast.makeText(
                    applicationContext,
                    "Please Enter Valid Amount",
                    Toast.LENGTH_SHORT
                )
                    .show()

                return@OnClickListener
            }

            if ((tv_currency_code_select_req_pay.text.toString()).equals(getString(R.string.select_digital_asset))) {
                ToastCustomize.displayToast(
                    this@PaymentRequestDDFActivityK,
                    getString(R.string.select_digital_asset)
                )
                return@OnClickListener
            }

            if ((tv_et_hint_btc_req_pay2.text.toString()).equals(getString(R.string.fiat))) {
                ToastCustomize.displayToast(
                    this@PaymentRequestDDFActivityK,
                    getString(R.string.invalid_fiat)
                )
                return@OnClickListener
            }
            ConstantsActivity.STR_FORMAT_ET = "QR Format 5";
            apiRetroFitPaymentInfo.paymentInfoRetrofit(this@PaymentRequestDDFActivityK)
            /*if (bDisclaimer) {
                } else {
                    val toastCustomize = ToastCustomize.getInstance()
                    ToastCustomize.displayToast(
                        this@PaymentRequestDDFActivityK,
                        "Select Disclaimer"
                    )
                }*/

        })

        val cbDisclaimerPayReq = findViewById<CheckBox>(R.id.cb_disclaimer_pay_req)
        cbDisclaimerPayReq.setOnCheckedChangeListener { compoundButton, b ->
            Log.e("onCheckedChanged", "onCheckedChanged")
            bDisclaimer = b
            if (b) {
                rlProceedReqPay.setBackgroundResource(R.drawable.btn_corners_app_color)
            } else {
                rlProceedReqPay.setBackgroundResource(R.drawable.btn_corners_gray)
            }
        }

        val ivAmountRadioReqPay = findViewById<ImageView>(R.id.iv_amount_radio_req_pay)
        ivAmountRadioReqPay.setOnClickListener {
//            displayAmountDialog()
//            etOperations()
            dialogCustomize.displayArrayDialog(
                this@PaymentRequestDDFActivityK,
                ConstantsActivity.STR_FLAG_CHOICE_AMOUNT_DIALOG
            )
        }
//        val tvSelectAmountReqPay = findViewById(R.id.tv_select_amount_req_pay) as TextView
        tvSelectAmountReqPay = findViewById<TextView>(R.id.tv_select_amount_req_pay)
        btnRadioAmountReqPay = findViewById<Button>(R.id.btn_radio_amount_req_pay)
        btnRadioAmountReqPay.setOnClickListener { displaySingleChoiceDialog() }
        tvSelectAmountReqPay.setOnClickListener { displayAmountDialog() }
        /*if (ConstantsActivity.STR_RADIO_OPTION.equals(getString(R.string.enter_amount))) {
            etBtcReqPay.visibility = View.VISIBLE
            tvSelectAmountReqPay.visibility = View.GONE
        } else if (ConstantsActivity.STR_RADIO_OPTION.equals(getString(R.string.select_amount))) {
            etBtcReqPay.visibility = View.GONE
            tvSelectAmountReqPay.visibility = View.VISIBLE
        }*/

        rb_et_req_pay = findViewById(R.id.rb_et_req_pay)
        rb_btn_req_pay = findViewById(R.id.rb_btn_req_pay)
        rg_req_pay = findViewById(R.id.rg_req_pay)
        et_radio_amount_req_pay = findViewById(R.id.et_radio_amount_req_pay)
//        rg_req_pay.get(0).sele
//        rb_et_req_pay
//        rb_et_req_pay.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener())
        /*rg_req_pay.setOnCheckedChangeListener { radioGroup, i ->
//            Log.e(TAG,i.toString());
            radioButtonOperation(i)

        }*/
        ConstantsActivity.STR_CURRENCY_CODE = ""
        rb_btn_req_pay.setText(getString(R.string.select_currency))

        tv_currency_code_select_req_pay = findViewById(R.id.tv_currency_code_select_req_pay)
        if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT === "GEIDEA") {
            tv_currency_code_select_req_pay.text = ConstantsActivity.STR_SAR_CODE
        } else {
            tv_currency_code_select_req_pay.text = getString(R.string.select_digital_asset)
        }
        tvCurrencyCodeReqPay = findViewById<TextView>(R.id.tv_currency_code_req_pay)
        tvCurrencyNameReqPay = findViewById<TextView>(R.id.tv_currency_name_req_pay)
        rl_spinner_currency_select_req_pay =
            findViewById(R.id.rl_spinner_currency_select_req_pay)
        rl_spinner_currency_select_req_pay.setOnClickListener { btnOperations() }
        radioButtonsEvents()
        etBtcReqPay.visibility = View.VISIBLE

        /*etBtcReqPay.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    LoggerDDF.e(TAG, "ACTION_DOWN")
//                    etOperations()
                }
                MotionEvent.ACTION_UP -> {
                    LoggerDDF.e(TAG, "ACTION_UP")
                }
            }
            return@OnTouchListener true
        })*/

        tvSelectAmountReqPay.visibility = View.GONE
        btnRadioAmountReqPay.visibility = View.GONE

//        spinnerOperation()
        var iv_currency_select_req_pay: ImageView = findViewById(R.id.iv_currency_select_req_pay)
        var iv_currency_options_req_pay: ImageView = findViewById(R.id.iv_currency_options_req_pay)
        if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT === "GEIDEA") {
            iv_currency_select_req_pay.visibility = View.GONE
            iv_currency_options_req_pay.visibility = View.GONE
        } else {

        }
    }

    private var mCurrencyList: ArrayList<SpinnerCurrencyItem>? = null
    private var mCurrencyAdapter: SpinnerCurrencyAdapter? = null
    private fun spinnerOperation() {
        LoggerDDF.e(TAG, "spinnerOperation@339")
//        initList()
        initList2()

        val tvAmountReqPay = findViewById<TextView>(R.id.tv_amount_req_pay)
        val tvCurrencyCodeModeReqPay =
            findViewById<TextView>(R.id.tv_currency_code_mode_req_pay)
        tvCurrencyAmountModeReqPay =
            findViewById<TextView>(R.id.tv_currency_amount_mode_req_pay)
        val spinnerCurrencies = findViewById<Spinner>(R.id.spinner_currencies)
        mCurrencyAdapter = SpinnerCurrencyAdapter(this, mCurrencyList)
        spinnerCurrencies.adapter = mCurrencyAdapter
        spinnerCurrencies.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                try {
                    LoggerDDF.e(TAG, "@364")
                    if (flagCurrencySelection == 0) {
                        selectedPositionK = position
                    } else {
                        selectedPositionK = selectedCurrencyPosition
                        flagCurrencySelection = 0
                    }

//                selectedPositionK = 2
//                selectedPositionK = selectedCurrencyPosition
                    LoggerDDF.e("selectedPositionK@368", "$selectedPositionK")
                    if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT === "GEIDEA") {
                        selectedPositionK = 0
                    }
                    parseFee(selectedPositionK)
//                val clickedItem = parent.getItemAtPosition(position) as SpinnerCurrencyItem
                    val clickedItem =
                        parent.getItemAtPosition(selectedPositionK) as SpinnerCurrencyItem
                    val clickedCurrencyCode = clickedItem.currencyCode
                    val clickedCurrencyName = clickedItem.currencyName
                    val clickedCurrencyAmount = clickedItem.currencyAmount
                    val clickedCurrencyImage = clickedItem.flagImage
                    //                Toast.makeText(PaymentRequest.this, clickedCurrencyCode + " selected", Toast.LENGTH_SHORT).show();
                    ConstantsActivity.STR_CURRENCY_CODE = clickedCurrencyCode
                    ConstantsActivity.STR_CURRENCY_NAME = clickedCurrencyName
                    ConstantsActivity.STR_CURRENCY_AMOUNT = clickedCurrencyAmount
                    /*ConstantsActivity.STR_CURRENCY_FEE_AMOUNT = ConstantsApi.alConversions.get(
                    selectedPositionK).get("amountCrypto")*/
                    ConstantsActivity.STR_CURRENCY_FEE_AMOUNT = ConstantsApi.strFeeOnWadzPay
                    Log.e("fee-amount", ConstantsActivity.STR_CURRENCY_FEE_AMOUNT + "")
//                intCurrencyImage = clickedCurrencyImage
//                intCurrencyImage = ConstantsActivity.hmCryptoImages[ConstantsActivity.STR_CURRENCY_CODE]!!
//                intCurrencyImage = CustomOperations.getCurrencyImageResource("bbb")
                    ConstantsActivity.INT_CURRENT_IMAGE_SPINNER =
                        CustomOperations.getCurrencyImageResource(ConstantsActivity.STR_CURRENCY_CODE)

                    if (ConstantsActivity.STR_CURRENCY_CODE.equals("sart", ignoreCase = true)) {
                        tvCurrencyCodeReqPay.text = ConstantsActivity.STR_SAR_CODE
                        btnConversionsReqPay.text = "Convert to " + ConstantsActivity.STR_SAR_CODE
                        tv_currency_code_select_req_pay.text = ConstantsActivity.STR_SAR_CODE
                    } else {
                        tvCurrencyCodeReqPay.text = ConstantsActivity.STR_CURRENCY_CODE
                        btnConversionsReqPay.text =
                            "Convert to " + ConstantsActivity.STR_CURRENCY_CODE
                        tv_currency_code_select_req_pay.text = ConstantsActivity.STR_CURRENCY_CODE
                    }

                    tvCurrencyNameReqPay.text = ConstantsActivity.STR_CURRENCY_NAME
                    ConstantsActivity.INT_CURRENT_IMAGE_SPINNER =
                        CustomOperations.getCurrencyImageResource(ConstantsActivity.STR_CURRENCY_CODE)
                    iv_select_req_pay.setImageResource(ConstantsActivity.INT_CURRENT_IMAGE_SPINNER)

                    //        tv_currency_amount_pay_conf.setText(ConstantsActivity.STR_CURRENCY_AMOUNT);
//        tv_fee_amount_pay_conf.setText(ConstantsActivity.STR_CURRENCY_FEE_AMOUNT);
                    val dAmount = ConstantsActivity.STR_CURRENCY_AMOUNT.toDouble()
                    Log.e("$TAG @410", dAmount.toString() + "");
                    Log.e("$TAG @410", ConstantsActivity.STR_CURRENCY_AMOUNT + "");
//                tvAmountReqPay.text = ConstantsActivity.STR_CURRENCY_AMOUNT
//                BigDecimal bd = (ConstantsActivity.STR_CURRENCY_AMOUNT)
//                tvAmountReqPay.text = String.format("%.8f", dAmount)
                    val bigDecimalCurrencyAmount: BigDecimal =
                        ConstantsActivity.STR_CURRENCY_AMOUNT.toBigDecimal()
                    tvAmountReqPay.text = bigDecimalCurrencyAmount.toPlainString()
//                tvAmountReqPay.text = ConstantsActivity.STR_CURRENCY_AMOUNT
//                ivCurrencyCodeImgReqPay.setImageResource(intCurrencyImage)
                    ivCurrencyCodeImgReqPay.setImageResource(ConstantsActivity.INT_CURRENT_IMAGE_SPINNER)
                    tvCurrencyCodeModeReqPay.text = ConstantsActivity.STR_CURRENCY_CODE

//                tvCurrencyAmountModeReqPay.text = ConstantsActivity.STR_CURRENCY_AMOUNT
//                val dAmount = ConstantsActivity.STR_CURRENCY_AMOUNT.toDouble()
//                tvCurrencyAmountModeReqPay.text = String.format("%.8f", dAmount)
                    amountValidation()
//                ConstantsActivity.STR_CURRENCY_CRYPTO_TYPE = ConstantsActivity.STR_CURRENCY_CODE
                } catch (e: Exception) {
                    ConstantsActivity.flag_clear_fields = true
                    onRestart()
                    ToastCustomize.displayToast(
                        this@PaymentRequestDDFActivityK,
                        "Conversion Not available for Selected Digital Asset "
                    )

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        val rlSpinnerCurrencyReqPay =
            findViewById<RelativeLayout>(R.id.rl_spinner_currency_req_pay)
        rlSpinnerCurrencyReqPay.setOnClickListener {
            LoggerDDF.e(TAG, "rlSpinnerCurrencyReqPay")
            if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT === "GEIDEA") {
            } else {
                spinnerCurrencies.performClick()
            }
        }
    }

    private fun initList2() {
        mCurrencyList = ArrayList()
        for (i in ConstantsApi.alConversions.indices) {
            Log.e("amountCrypto", ConstantsApi.alConversions.get(i).get("amountCrypto") + "")
            Log.e("totalAmount", ConstantsApi.alConversions.get(i).get("totalAmount") + "")
/*if (ConstantsApi.alConversions.get(i).get("asset")
                    .equals("USDT") || ConstantsApi.alConversions.get(i).get("asset").equals("WTK")
            ) {
                continue
            }*/
            if (ConstantsActivity.FLAG_ENVIRONMENT.equals(
                    ConstantsActivity.FLAG_PROD,
                    ignoreCase = true
                )
            ) {
                if (ConstantsApi.alConversions.get(i).get("asset")
                        .equals("BTC") || ConstantsApi.alConversions.get(i).get("asset")
                        .equals("WTK")
                ) {
                    continue
                }
            }

            mCurrencyList!!.add(
                SpinnerCurrencyItem(
                    ConstantsApi.alConversions.get(i).get("asset"),
                    "Bitcoin",
                    ConstantsApi.alConversions.get(i).get("totalAmount"),
                    R.drawable.ic_currency_btc
                )
            )
        }

    }

    /*private fun initList() {
        mCurrencyList = ArrayList()
        mCurrencyList!!.add(
            SpinnerCurrencyItem(
                "BTC",
                "Bitcoin",
                "0.001",
                R.drawable.
            )
        )
        mCurrencyList!!.add(
            SpinnerCurrencyItem(
                "ETH",
                "Ethereum",
                "0.002",
                R.drawable.
            )
        )
        mCurrencyList!!.add(SpinnerCurrencyItem("BNB", "BNB", "0.003", R.drawable.))
        mCurrencyList!!.add(
            SpinnerCurrencyItem(
                "XRP",
                "Ripple",
                "0.004",
                R.drawable.ic_currency_xrp
            )
        )
        mCurrencyList!!.add(
            SpinnerCurrencyItem(
                "USD",
                "US Dollar",
                "0.005",
                R.drawable.
            )
        )
        mCurrencyList!!.add(
            SpinnerCurrencyItem(
                "USDT",
                "US Dollar",
                "0.006",
                R.drawable.
            )
        )
    }*/

    private var dialogCustomize = DialogCustomize.getInstance()
    private fun displaySingleChoiceDialog() {
        dialogCustomize.displaySingleChoiceDialog(this, ConstantsDialog.FLAG_DIALOG_DISPLAY)
    }

    private fun displayAmountDialog() {
        val amountDialog = AlertDialog.Builder(this)
        amountDialog.setTitle("Choose Amount")

// add a list
        val arrayAmount = arrayOf(
            "10", "100", "200", "300", "400", "500",
            "600", "700", "800", "900", "1000"
        )
        amountDialog.setItems(arrayAmount) { dialog, which ->
            etBtcReqPay!!.setText(arrayAmount[which])
//            etBtcReqPay!!.text = arrayAmount[which]
            etBtcReqPay!!.setSelection(etBtcReqPay!!.text.length)
            tvSelectAmountReqPay.text = arrayAmount[which]
        }

// create and show the alert dialog
        val dialog = amountDialog.create()
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        LoggerDDF.e(TAG,"onResume @622")
        try {
            val tvAmountReqPay = findViewById<TextView>(R.id.tv_amount_req_pay)
            tvEtHintBtcReqPay.text = ConstantsActivity.STR_CURRENCY_FIAT_TYPE
            tv_et_hint_btc_req_pay1.text = ConstantsActivity.STR_CURRENCY_FIAT_TYPE
            if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT === "GEIDEA") {
                tv_et_hint_btc_req_pay2.text = "SAR"
            } else {
                tv_et_hint_btc_req_pay2.text = ConstantsActivity.STR_CURRENCY_FIAT_TYPE
            }
//        tv_et_hint_btc_req_pay2.text = "AED"
//        ConstantsActivity.STR_CURRENCY_FIAT_TYPE = "AED"
            tv_fiat_two_req_pay.text = ConstantsActivity.STR_CURRENCY_FIAT_TYPE
            if (ConstantsActivity.STR_CURRENCY_CODE.isNotEmpty()) {
                rb_btn_req_pay.text = ConstantsActivity.STR_CURRENCY_CODE
                if (ConstantsActivity.STR_CURRENCY_CODE.equals("sart", ignoreCase = true)) {
//                    currencyStr = "SAR*"
                    tv_currency_code_select_req_pay.text = ConstantsActivity.STR_SAR_CODE
                    btnConversionsReqPay.text = "Convert to " + ConstantsActivity.STR_SAR_CODE
                    tvCurrencyCodeReqPay.text = ConstantsActivity.STR_SAR_CODE
                } else {
                    tv_currency_code_select_req_pay.text = ConstantsActivity.STR_CURRENCY_CODE
                    btnConversionsReqPay.text = "Convert to " + ConstantsActivity.STR_CURRENCY_CODE
                    tvCurrencyCodeReqPay.text = ConstantsActivity.STR_CURRENCY_CODE
                }

                tvCurrencyNameReqPay.text = ConstantsActivity.STR_CURRENCY_NAME
                if (ConstantsActivity.STR_CURRENCY_AMOUNT.isNotEmpty()) {
                    val bigDecimalCurrencyAmount: BigDecimal =
                        ConstantsActivity.STR_CURRENCY_AMOUNT.toBigDecimal()
                    tvAmountReqPay.text = bigDecimalCurrencyAmount.toPlainString()
                }

            }
            ConstantsActivity.INT_CURRENT_IMAGE_SPINNER =
                CustomOperations.getCurrencyImageResource(ConstantsActivity.STR_CURRENCY_CODE)
            iv_select_req_pay.setImageResource(ConstantsActivity.INT_CURRENT_IMAGE_SPINNER)

            ivCurrencyCodeImgReqPay.setImageResource(ConstantsActivity.INT_CURRENT_IMAGE_SPINNER)

            /*if (ConstantsActivity.flag_fiat_selected) {
                if(ConstantsActivity.FLAG_STR_THANK_YOU.equals("FLAG_THANK_YOU")){
                    ConstantsActivity.FLAG_STR_THANK_YOU = ""
                    restartActivity()
                }else{
                    callConversionApi()
                }
            }*/
            if (ConstantsActivity.flag_fiat_selected) {
                callConversionApi()
            }

//        val textToVoiceEG =  TextToVoiceEG()
//        textToVoiceEG.setTextToSpeech(this@PaymentRequestDDFActivityK)
        } catch (e: Exception) {
            LoggerDDF.e(TAG, e.toString() + "")
        }
        editTextListener()
    }

    override fun onRestart() {
        super.onRestart()
        Log.e("onRestart", "onRestart")
        if (ConstantsActivity.flag_clear_fields) {
            Log.e("flag_clear_fields", ConstantsActivity.flag_clear_fields.toString() + "")
            ConstantsActivity.flag_clear_fields = false
            startActivity(getIntent());
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
//            recreate()
            ConstantsActivity.STR_CURRENCY_ET = ""
            if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT === "GEIDEA") {
                ConstantsActivity.STR_CURRENCY_FIAT_TYPE = "SAR"
            } else {
                ConstantsActivity.STR_CURRENCY_FIAT_TYPE = "FIAT"
            }
        }
    }

    private fun getMerchantDetails() {
//        ConstantsApi.FLAG_API_ACTIVITY_FAILED = "FiatTypeActivity"
        ConstantsApi.FLAG_API_NAME = ConstantsApi.FLAG_API_MERCHANT_DETAILS
        val serviceIntent = Intent(this, ApiService::class.java)
        startService(serviceIntent)
    }

    private fun restartActivity() {
        startActivity(getIntent());
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
//            recreate()
        ConstantsActivity.STR_CURRENCY_ET = ""
        if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT === "GEIDEA") {
            ConstantsActivity.STR_CURRENCY_FIAT_TYPE = "SAR"
        } else {
            ConstantsActivity.STR_CURRENCY_FIAT_TYPE = "FIAT"
        }
    }

    companion object {
        var selectedPositionK = 0
        var radioFlag: Int = 0
        var selectedCurrencyPosition: Int = 0
        var flagCurrencySelection: Int = 0
        lateinit var paymentRequestDDFActivityK: PaymentRequestDDFActivityK
    }

    //

    private fun parseFee(indexIn: Int) {
        try {
            val strFee: String =
                ConstantsApi.alConversions.get(indexIn)
                    .get(ConstantsApi.KEYS_CONVERSION_PARSE[3])
                    .toString()
            val jsonResponseFee = JSONObject(strFee)
            ConstantsApi.strFeeOnWadzPay = jsonResponseFee.getString("feeOnWadzpay")
            Log.e("strFeeOnWadzPay@369", ConstantsApi.strFeeOnWadzPay + "");
//            ConstantsApi.strFeeOnExternal = jsonResponseFee.getString("feeOnExternal")
//            Log.e("strFeeOnExternal@370", ConstantsApi.strFeeOnExternal + "");

        } catch (e: Exception) {
            Log.e("Exception@372", e.toString() + "");
        }
    }

    override fun activityCall(strResponse: String?) {
        LoggerDDF.e(TAG, "$strResponse @600");
        LoggerDDF.e(TAG, "activityCall @602");
//        ToastCustomize.displayToast(this,strResponse)
        /*if (ConstantsActivity.FLAG_ENVIRONMENT.equals(
                ConstantsActivity.FLAG_PROD,
                ignoreCase = true
            )
        ) {

            ConstantsApi.alConversions.removeAt(1)
            ConstantsApi.alConversions.removeAt(0)
        }*/
        LoggerDDF.e(TAG, " @612 " + ConstantsApi.API_STATUS_CODE);
        if (ConstantsApi.API_STATUS_CODE == 403 || ConstantsApi.API_STATUS_CODE == 503 || ConstantsApi.API_STATUS_CODE == 500) {
//        if (ConstantsApi.API_STATUS_CODE != 200 || ConstantsApi.API_STATUS_CODE != 201)
            LoggerDDF.e(TAG, "activityCall @614");
            amplifyOperations()

//            continue
        } else {
            LoggerDDF.e(TAG, "activityCall @617");
            if (strResponse.equals(ConstantsApi.FLAG_API_CONVERSION)) {
                flagCurrencySelection = 1
//            selectedPositionK = selectedCurrencyPosition
                spinnerOperation()
                rlConversionsOptionsReqPay.visibility = View.VISIBLE
                rlBottomReqPay.visibility = View.VISIBLE
            } else if (strResponse.equals(ConstantsApi.FLAG_API_PAYMENT_INFO)) {
                if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT === "GEIDEA") {
                    val intent = Intent(this, QRGeneratorActivityGeidea::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, QRGeneratorActivity::class.java)
                    startActivity(intent)
                }

            } else if (strResponse.equals(ConstantsActivity.STR_FLAG_CHOICE_AMOUNT_DIALOG)) {
                btnRadioAmountReqPay.text = ConstantsActivity.STR_CURRENCY_ET
                etBtcReqPay!!.setText(ConstantsActivity.STR_CURRENCY_ET)
                rb_btn_req_pay.setText(ConstantsActivity.STR_CURRENCY_ET)
                etBtcReqPay!!.setSelection(etBtcReqPay!!.text.length)
            } else if (strResponse.equals(ConstantsActivity.STR_FLAG_CHOICE_QR_FORMAT_DIALOG)) {
                tv_qr_format_req_pay.text = ConstantsActivity.STR_FORMAT_ET
            } else if (strResponse.equals("select")) {
                btnRadioAmountReqPay.text = ConstantsActivity.STR_CURRENCY_ET
                etBtcReqPay!!.setText(ConstantsActivity.STR_CURRENCY_ET)
                rb_btn_req_pay.setText(ConstantsActivity.STR_CURRENCY_ET)
                etBtcReqPay!!.setSelection(etBtcReqPay!!.text.length)
            } else if (strResponse.equals("enter")) {
                btnRadioAmountReqPay.text = ConstantsActivity.STR_CURRENCY_ET
                etBtcReqPay!!.setText(ConstantsActivity.STR_CURRENCY_ET)
            } else if (strResponse.equals(ConstantsApi.FLAG_API_MERCHANT_DETAILS)) {
//            LoggerDDF.e(TAG, ConstantsApi.alMerchantDetails.get(0).get("posName") + " @76##");
                if (ConstantsApi.alMerchantDetails.size == 0) {
                    ConstantsApi.FLAG_API_NAME = ConstantsApi.FLAG_API_ADD_POS_MERCHANT
//                val serviceIntent = Intent(this, ApiService::class.java)
//                startService(serviceIntent)
                    ConstantsApi.FLAG_API_NAME = ConstantsApi.FLAG_API_ADD_POS_MERCHANT
//                val serviceIntent = Intent(this, ApiService::class.java)
//                startService(serviceIntent)
                    apiRetroFitPaymentInfo.addPosToMerchant(this@PaymentRequestDDFActivityK)

                } else if (ConstantsApi.alMerchantDetails.size > 0) {
                    ConstantsApi.strPosID = ConstantsApi.alMerchantDetails.get(0)
                        .get(ConstantsApi.KEYS_MERCHANT_DETAILS_PARSE[0])
//                ConstantsApi.strPosID = ConstantsApi.alMerchantDetails.get(ConstantsApi.alMerchantDetails.size-1)
//                    .get(ConstantsApi.KEYS_MERCHANT_DETAILS_PARSE[0])
                    Log.e("strPosID@709", ConstantsApi.strPosID + "")
//                apiRetroFitPaymentInfo.addPosToMerchant(this@PaymentRequestDDFActivityK)
//                val strNameIndex = ConstantsApi.strPosName.substring(0)
                    val strNameIndex = ConstantsApi.strPosName.subSequence(0, 1)
                    tv_profile_req_pay.text = strNameIndex
                }
            } else if (strResponse.equals(ConstantsActivity.STR_FLAG_DIALOG)) {
                ToastCustomize.displayToast(this@PaymentRequestDDFActivityK, "NA")
            }
        }
    }

    private fun amplifyOperations() {
        ConstantsApi.API_TEMP_JWT = ""
        val awsAmplifyOperations = AwsAmplifyOperations.getInstance(this)
        awsAmplifyOperations.amplifySignOut();

    }

    override fun activityCallFailed(strResponse: String?) {
        LoggerDDF.e(TAG, "$strResponse @664");

    }

    private fun amountValidation() {
        dAmount = ConstantsActivity.STR_CURRENCY_AMOUNT.toDouble()
        dFeeAmount = ConstantsActivity.STR_CURRENCY_FEE_AMOUNT.toDouble()
        dTotalAmount = dAmount + dFeeAmount
        ConstantsActivity.STR_CURRENCY_TOTAL_AMOUNT = String.format("%.8f", dTotalAmount)
//        tvCurrencyAmountModeReqPay.text = String.format("%.8f", dTotalAmount)
        tvCurrencyAmountModeReqPay.text = String.format("%.8f", dAmount)

        LoggerDDF.e("$TAG@41", ConstantsActivity.STR_CURRENCY_TOTAL_AMOUNT)
    }

    private fun hideKeyPadMethod() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(
            btnConversionsReqPay.windowToken,
            InputMethodManager.RESULT_UNCHANGED_SHOWN
        )
    }

    private fun displayKeypadMethod() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(etBtcReqPay, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun radioButtonOperation(i: Int) {
        if (rb_et_req_pay.id == i) {

        } else {
            Log.e(TAG, "2");
        }
//        rb_btn_req_pay.setOnTouchListener(View.OnTouchListener())

    }

    //
    @SuppressLint("ClickableViewAccessibility")
    private fun radioButtonsEvents() {
//
        /* rb_et_req_pay.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
             when (motionEvent.action) {
                 MotionEvent.ACTION_DOWN -> {
                     LoggerDDF.e(TAG, "ACTION_DOWN")
 //                    etOperations()
                 }
                 MotionEvent.ACTION_UP -> {
                     LoggerDDF.e(TAG, "ACTION_UP")
                 }
             }
             return@OnTouchListener true
         })*/
//
        rb_btn_req_pay.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    LoggerDDF.e(TAG, "ACTION_DOWN")
                    btnOperations()
                }
                MotionEvent.ACTION_UP -> {
                    LoggerDDF.e(TAG, "ACTION_UP")
                }
            }
            return@OnTouchListener true
        })
    }

    private fun etOperations() {
        LoggerDDF.e(TAG, "etOperations")
        ConstantsActivity.STR_CURRENCY_ET = ""
//                    rg_req_pay.checkedRadioButtonId
        rg_req_pay.check(rb_et_req_pay.id)
        Log.e(TAG, "1");
        radioFlag = 1
//                Log.e(TAG, radioGroup.getChildAt(0).toString());
//        et_radio_amount_req_pay.visibility = View.VISIBLE
        rb_et_req_pay.text = ""
//                et_radio_amount_req_pay.setText(getString(R.string.enter_amount))
        et_radio_amount_req_pay.setHint(R.string.enter_amount)
//        et_radio_amount_req_pay.requestFocus()
        etBtcReqPay.requestFocus()
        rb_btn_req_pay.text = getString(R.string.select_currency)
        if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT === "GEIDEA") {
            tv_currency_code_select_req_pay.text = ConstantsActivity.STR_SAR_CODE
        } else {
            tv_currency_code_select_req_pay.text = getString(R.string.select_digital_asset)
        }
        displayKeypadMethod();

//        btnConversionsReqPay.text = getString(R.string.conversion_rates)
//
        ConstantsActivity.STR_CURRENCY_CODE = ""
        ConstantsActivity.INT_CURRENT_IMAGE_SPINNER =
            CustomOperations.getCurrencyImageResource(ConstantsActivity.STR_CURRENCY_CODE)
        iv_select_req_pay.setImageResource(ConstantsActivity.INT_CURRENT_IMAGE_SPINNER)
    }

    private fun btnOperations() {
        if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT === "GEIDEA") {

        } else {
            LoggerDDF.e(TAG, "btnOperations")
//                    rg_req_pay.checkedRadioButtonId
//        etBtcReqPay.setText("")
//        ConstantsActivity.STR_CURRENCY_ET = ""
            rg_req_pay.check(rb_btn_req_pay.id)
            Log.e(TAG, "2");
            radioFlag = 2
            et_radio_amount_req_pay.setText("")
            et_radio_amount_req_pay.visibility = View.GONE
            rb_et_req_pay.text = getString(R.string.enter_amount)
            hideKeyPadMethod()
//                    dialogCustomize.displayArrayDialog(this@PaymentRequestDDFActivityK, "")

//        btnConversionsReqPay.text = getString(R.string.proceed)
            openCurrencyActivity()
        }
    }

    private fun openFiatActivity() {
        if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT === "GEIDEA") {

        } else {
            val i = Intent(this, FiatTypeActivity::class.java)
            startActivity(i)
        }
    }


    private fun openCurrencyActivity() {
        val i = Intent(this, CurrencyTypeActivity::class.java)
        startActivity(i)
    }

    private fun callConversionApi() {
        LoggerDDF.e(TAG, "start callConversionApi")
        if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT === "GEIDEA") {

        } else {

        }
        if ((tv_et_hint_btc_req_pay2.text.toString()).equals(getString(R.string.fiat))) {
            ToastCustomize.displayToast(
                this@PaymentRequestDDFActivityK,
                getString(R.string.invalid_fiat)
            )
            return
        }
//            this
        radioFlag = 1
        if (radioFlag == 1) {
            LoggerDDF.e(TAG, "radioFlag" + radioFlag.toString() + "")
//                ConstantsActivity.STR_CURRENCY_ET = et_radio_amount_req_pay.getText().toString()
            ConstantsActivity.STR_CURRENCY_ET = etBtcReqPay.getText().toString()
            if(ConstantsActivity.STR_CURRENCY_ET.isEmpty()){
                ToastCustomize.displayToast(
                    this@PaymentRequestDDFActivityK,
                    "Invalid Amount"
                )
                return
            }
//            val dNumber: Double = ConstantsActivity.STR_CURRENCY_ET.toDouble()
            val dNumber: BigDecimal = ConstantsActivity.STR_CURRENCY_ET.toBigDecimal()
            ConstantsActivity.STR_CURRENCY_ET = dNumber.toString()
//            ConstantsActivity.STR_CURRENCY_ET = removeTrailingZeros(dNumber)
            Log.e(TAG, ConstantsActivity.STR_CURRENCY_ET + "")
            Log.e(TAG, ConstantsActivity.STR_CURRENCY_ET.length.toString() + "")
            if (ConstantsActivity.STR_CURRENCY_ET == null || ConstantsActivity.STR_CURRENCY_ET.length == 0
                || ConstantsActivity.STR_CURRENCY_ET.isEmpty()
            ) {
                if (ConstantsActivity.flag_fiat_selected) {
                    return
                }
                Toast.makeText(
                    applicationContext,
                    "Please Enter Valid Amount",
                    Toast.LENGTH_SHORT
                )
                    .show()
                return
            }

//                val dAmount = ConstantsActivity.STR_CURRENCY_ET.toDouble()
            val dAmount = ConstantsActivity.STR_CURRENCY_ET.toDoubleOrNull()
            if (dAmount != null) {
                LoggerDDF.e(TAG, "Number is double @200")
            } else {
                if (ConstantsActivity.flag_fiat_selected) {
                    return
                }
                LoggerDDF.e(TAG, "Double Error @200")
                ToastCustomize.displayToast(
                    this@PaymentRequestDDFActivityK,
                    "Invalid Amount"
                )
                return
            }

            if (dAmount == 0.0) {
                if (ConstantsActivity.flag_fiat_selected) {
                    return
                }
                Toast.makeText(
                    applicationContext,
                    "Please Enter Valid Amount",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            hideKeyPadMethod()
            apiRetroFitPaymentInfo.conversionListRetrofit(this@PaymentRequestDDFActivityK)

        } else if (radioFlag == 2) {
            LoggerDDF.e(TAG, "radioFlag" + radioFlag.toString() + "")
            if (rb_btn_req_pay.text.equals(getString(R.string.select_currency))) {
                val toastCustomize = ToastCustomize.getInstance()
                ToastCustomize.displayToast(
                    this@PaymentRequestDDFActivityK,
                    getString(R.string.invalid_details)
                )
            } else {
                ConstantsActivity.STR_CURRENCY_ET = "0.1"
                ConstantsActivity.INT_CURRENT_IMAGE_SPINNER =
                    CustomOperations.getCurrencyImageResource(ConstantsActivity.STR_CURRENCY_CODE)
                apiRetroFitPaymentInfo.paymentInfoRetrofit(this@PaymentRequestDDFActivityK)
            }

        } else if (radioFlag == 0) {
            LoggerDDF.e(TAG, "radioFlag" + radioFlag.toString() + "")
            ToastCustomize.displayToast(
                this@PaymentRequestDDFActivityK,
                getString(R.string.invalid_details)
            )
        }
        ConstantsActivity.flag_fiat_selected = false
        LoggerDDF.e(TAG, "end callConversionApi")
    }

    //
    class DecimalDigitsInputFilter(digitsBeforeZero: Int, digitsAfterZero: Int) :
        InputFilter {
        var mPattern: Pattern

        init {
            mPattern =
                Pattern.compile("[0-9]{0," + (digitsBeforeZero - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)||(\\.)?")
        }

        override fun filter(
            source: CharSequence?,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            val matcher: Matcher = mPattern.matcher(dest)
            return if (!matcher.matches()) "" else null
        }
    }

    private fun editTextListener() {
        etBtcReqPay.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                if (s.length ==1 && s.toString().startsWith(".")) {
                    etBtcReqPay.setText("0.")
                    etBtcReqPay.setSelection(etBtcReqPay.length())
                }
                if (s.length > 1 && s.toString().startsWith("00")) {
                    etBtcReqPay.setText("0")
                    etBtcReqPay.setSelection(etBtcReqPay.length())
                } else if (s.length > 1 && s.toString().startsWith("01")) {
                    etBtcReqPay.setText("1")
                    etBtcReqPay.setSelection(etBtcReqPay.length())
                } else if (s.length > 1 && s.toString().startsWith("02")) {
                    etBtcReqPay.setText("2")
                    etBtcReqPay.setSelection(etBtcReqPay.length())
                } else if (s.length > 1 && s.toString().startsWith("03")) {
                    etBtcReqPay.setText("3")
                    etBtcReqPay.setSelection(etBtcReqPay.length())
                } else if (s.length > 1 && s.toString().startsWith("04")) {
                    etBtcReqPay.setText("4")
                    etBtcReqPay.setSelection(etBtcReqPay.length())
                } else if (s.length > 1 && s.toString().startsWith("05")) {
                    etBtcReqPay.setText("5")
                    etBtcReqPay.setSelection(etBtcReqPay.length())
                } else if (s.length > 1 && s.toString().startsWith("06")) {
                    etBtcReqPay.setText("6")
                    etBtcReqPay.setSelection(etBtcReqPay.length())
                } else if (s.length > 1 && s.toString().startsWith("07")) {
                    etBtcReqPay.setText("7")
                    etBtcReqPay.setSelection(etBtcReqPay.length())
                } else if (s.length > 1 && s.toString().startsWith("08")) {
                    etBtcReqPay.setText("8")
                    etBtcReqPay.setSelection(etBtcReqPay.length())
                } else if (s.length > 1 && s.toString().startsWith("09")) {
                    etBtcReqPay.setText("9")
                    etBtcReqPay.setSelection(etBtcReqPay.length())
                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                LoggerDDF.e(TAG, "onTextChanged")
                rlConversionsOptionsReqPay.visibility = View.GONE
            }
        })
    }

    fun removeTrailingZeros(number: Double): String? {
        var formatted = number.toString()
        if (formatted.contains(".")) {
            formatted = formatted.replace("0*$".toRegex(), "")
            formatted = formatted.replace(
                "\\.$".toRegex(),
                ""
            ) // Remove trailing dot if only zeros are left
        }
        return formatted
    }
}
