package com.wadzpay.ddflibrary.api

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.wadzpay.ddflibrary.awsamplify.AwsAmplifyOperations
import com.wadzpay.ddflibrary.callbacks.ActivityCallBack
import com.wadzpay.ddflibrary.dialogs.ConstantsDialog
import com.wadzpay.ddflibrary.dialogs.DialogCustomize
import com.wadzpay.ddflibrary.logs.LoggerDDF
import com.wadzpay.ddflibrary.utils.ConstantsActivity
import com.wadzpay.ddflibrary.utils.NetworkCheck
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

class ApiRetroFitPaymentInfo {
    private val TAG = javaClass.simpleName
    lateinit var mContext: Context
    private lateinit var dialogCustomize: DialogCustomize
    private var activityCallback: ActivityCallBack? = null
    private lateinit var networkCheck: NetworkCheck

    //
    fun conversionListRetrofit(context: Context) {
        LoggerDDF.e(TAG, "conversionListRetrofit")
        if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT === "GEIDEA") {
            ConstantsActivity.STR_CURRENCY_FIAT_TYPE = "SAR"
            ConstantsActivity.STR_CURRENCY_CODE = "SART"
        } else {
//            ConstantsActivity.STR_CURRENCY_FIAT_TYPE = "FIAT"
        }
        LoggerDDF.e(TAG, ConstantsActivity.STR_CURRENCY_ET + "")
        LoggerDDF.e(TAG, ConstantsActivity.STR_CURRENCY_FIAT_TYPE + "")
        mContext = context
        networkCheck = NetworkCheck.getInstance()
        if (!networkCheck.isNetworkConnected(mContext)) {
            return
        }

        dialogCustomize = DialogCustomize.getInstance()
        dialogCustomize.displayProgressDialog(mContext, ConstantsDialog.FLAG_DIALOG_DISPLAY)

        val retrofit = Retrofit.Builder()
            .baseUrl(ConstantsApi.BASE_URL)
            .build()
        LoggerDDF.e(TAG, ConstantsApi.BASE_URL + "");
        // Create Service
        val service = retrofit.create(RetroFitParams::class.java)

        // Create JSON using JSONObject
        val jsonObject = JSONObject()
//        jsonObject.put("fiatAmount", "11")
        jsonObject.put("fiatAmount", ConstantsActivity.STR_CURRENCY_ET)

        // Convert JSONObject to String
        val jsonObjectString = jsonObject.toString()

        // Create RequestBody ( We're not using any converter, like GsonConverter, MoshiConverter e.t.c, that's why we use RequestBody )
        val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

        CoroutineScope(Dispatchers.IO).launch {
            // Do the POST request and get response
            LoggerDDF.e(TAG, ConstantsApi.alToken.get(0) + "")
            var response: Response<ResponseBody>? = null
            if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT === "GEIDEA") {
                response = service.postConversionListTwo(
                    ConstantsApi.alToken.get(0),
                    requestBody,
                    ConstantsActivity.STR_CURRENCY_FIAT_TYPE
                )
            } else {
                response = service.postConversionList(
                    ConstantsApi.alToken.get(0),
                    requestBody,
                    ConstantsActivity.STR_CURRENCY_FIAT_TYPE
                )
            }


            withContext(Dispatchers.Main) {

                if (response.isSuccessful) {
                    try {
                        // Convert raw JSON to pretty JSON using GSON library
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        val conversionJson = gson.toJson(
                            JsonParser.parseString(
                                response.body()
                                    ?.string()
                            )
                        )
                        Log.e("Conversion Json: ", conversionJson)
                        //            String strData = jsonResponse.getString(ConstantsApi.KEYS_DROP_DOWN_PARSE[2]);
//            String strData = jsonResponse.getString("data");
//            JSONObject jsonResponseDATA = new JSONObject(strData);
                        val jsonResponseDATA = JSONObject(conversionJson)
                        val apiJsonParsing = ApiJsonParsing.getInstance()
//                    val jsonArray: JSONArray = jsonResponseDATA.optJSONArray("paymentModes")
//                    Log.e("fees@239", jsonArray.toString() + "");

                        ConstantsApi.alConversions.clear();
                        ConstantsApi.alConversions = apiJsonParsing.jsonHashMapArrayKeys(
                            jsonResponseDATA.getJSONArray("paymentModes"),
                            ConstantsApi.KEYS_CONVERSION_PARSE
                        );
                        Log.e("alConversions@239", ConstantsApi.alConversions.size.toString() + "");
                        Log.e(
                            "alConversions@240",
                            ConstantsApi.alConversions.get(0)
                                .get(ConstantsApi.KEYS_CONVERSION_PARSE[2]) + ""
                        );

//                    spinnerOperation()
//                    rlConversionsOptionsReqPay.visibility = View.VISIBLE
//                    rlBottomReqPay.visibility = View.VISIBLE

                        dialogCustomize.displayProgressDialog(
                            mContext,
                            ConstantsDialog.FLAG_DIALOG_DISMISS
                        )

                        activityCallback = mContext as ActivityCallBack
                        activityCallback!!.activityCall(ConstantsApi.FLAG_API_CONVERSION)
                    } catch (e: Exception) {
                        dialogCustomize.displayProgressDialog(
                            mContext,
                            ConstantsDialog.FLAG_DIALOG_DISMISS
                        )
                        LoggerDDF.e(TAG, e.toString() + "")
                        dialogCustomize.displayDialog(
                            mContext,
                            ConstantsDialog.FLAG_DIALOG_DISPLAY,
                            "SERVER ERROR",
                            e.toString()
                        )

                    }
                } else {
//                    dialogProgress.dismiss()
                    dialogCustomize.displayProgressDialog(
                        mContext,
                        ConstantsDialog.FLAG_DIALOG_DISMISS
                    )
                    Log.e("RETROFIT_ERROR@133", response.code().toString())
                    Log.e("RETROFIT_ERROR@134", response.toString())
                    dialogCustomize.displayDialog(
                        mContext,
                        ConstantsDialog.FLAG_DIALOG_DISPLAY,
                        "SERVER ERROR",
                        response.code().toString() + "\n" + response.message()
                    )
                }
            }
        }
    }

    private fun displayLogicalDate(): String? {
        val myDate = Date()
        val dateFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("UTC")
        calendar.time = myDate
        return dateFmt.format(myDate)
    }

    private fun getCurrentUTC(): String? {
        val time = Calendar.getInstance().time
        val outputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        outputFmt.timeZone = TimeZone.getTimeZone("UTC")
        return outputFmt.format(time)
    }

    private fun displayActualDate(): String? {
        val date = Date()
        val dateFmt = SimpleDateFormat("yyyy-MM-dd")
        return dateFmt.format(date)
    }

    private fun displayActualTime(): String? {
        val date = Date()
        val dateFmt = SimpleDateFormat("HH:mm:ss")
        return dateFmt.format(date)
    }

    //
    @SuppressLint("NewApi")
    fun paymentInfoRetrofit(context: Context) {
        Log.e(TAG, "paymentInfoRetrofit@180")
        if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT === "GEIDEA") {
            ConstantsActivity.STR_CURRENCY_FIAT_TYPE = "SAR"
            ConstantsActivity.STR_CURRENCY_CODE = "SART"
        } else {
//            ConstantsActivity.STR_CURRENCY_FIAT_TYPE = "FIAT"
        }

        ConstantsApi.API_TEMP_JWT = "0"
        ConstantsActivity.STR_TRANSACTION_STATUS = ""
        ConstantsApi.strQRAddressFirst = ""
        LoggerDDF.e(TAG, "paymentInfoRetrofit")
        ConstantsActivity.STR_EXT_POS_ID = "pos_app_id-" + ConstantsActivity.STR_CURRENCY_ET
        ConstantsActivity.STR_EXT_POS_SEQUENCE_NUMBER =
            "pos_app_seq-" + ConstantsActivity.STR_CURRENCY_ET
        ConstantsActivity.STR_EXT_POS_TRANSACTION_ID =
            "pos_app_tx_id-" + ConstantsActivity.STR_CURRENCY_ET
        mContext = context
        networkCheck = NetworkCheck.getInstance()
        if (!networkCheck.isNetworkConnected(mContext)) {
            return
        }

        dialogCustomize = DialogCustomize.getInstance()
        dialogCustomize.displayProgressDialog(mContext, ConstantsDialog.FLAG_DIALOG_DISPLAY)

        // Create Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(ConstantsApi.BASE_URL)
            .build()

        // Create Service
        val service = retrofit.create(RetroFitParams::class.java)

        // Create JSON using JSONObject
        val jsonObject = JSONObject()
//        jsonObject.put("fiatAmount", "1")
        Log.e("Total-Amount", ConstantsActivity.STR_CURRENCY_TOTAL_AMOUNT + "");
        Log.e("fiatAmount", ConstantsActivity.STR_CURRENCY_ET + "");
        jsonObject.put("fiatAmount", ConstantsActivity.STR_CURRENCY_ET)
//        jsonObject.put("posId", 0)
//        ConstantsApi.strPosID = "1"
//        jsonObject.put("fiatAmount", ConstantsActivity.STR_CURRENCY_ET)
        val strLogicalDate = displayLogicalDate() + "Z"
        val strLogicalDateUTC = getCurrentUTC() + "Z"
//        val instant:Instant = Instant.now()
        LoggerDDF.e(TAG, "strLogicalDateUTC  - $strLogicalDateUTC")
        LoggerDDF.e(TAG, "strLogicalDate  - $strLogicalDate")
        val strActualDate = displayActualDate() + ""
        LoggerDDF.e(TAG, "strActualDate  - $strActualDate")
        val strActualTime = displayActualTime() + ""
        LoggerDDF.e(TAG, "strActualTime  - $strActualTime")
        var validFor: Instant = Instant.now().minusSeconds(ConstantsActivity.flag_seconds)
//        var validFor: Instant = Instant.now().minus(1, ChronoUnit.MINUTES)
        LoggerDDF.e(TAG, "validFor  - $validFor")

        jsonObject.put("posId", ConstantsApi.strPosID)
        jsonObject.put("extPosId", ConstantsActivity.STR_EXT_POS_ID)
        jsonObject.put("extPosSequenceNo", ConstantsActivity.STR_EXT_POS_SEQUENCE_NUMBER)
        jsonObject.put("extPosTransactionId", ConstantsActivity.STR_EXT_POS_TRANSACTION_ID)
        jsonObject.put("description", "POS-APP")
//        jsonObject.put("extPosLogicalDate", "2022-11-07T11:47:54.608Z")
//        jsonObject.put("extPosLogicalDate", strLogicalDateUTC)
        jsonObject.put("extPosLogicalDate", validFor)
        jsonObject.put("extPosShift", "1")
        jsonObject.put("extPosActualDate", strActualDate)
        jsonObject.put("extPosActualTime", strActualTime + "")

        // Convert JSONObject to String
        val jsonObjectString = jsonObject.toString()

        // Create RequestBody ( We're not using any converter, like GsonConverter, MoshiConverter e.t.c, that's why we use RequestBody )
        val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

        CoroutineScope(Dispatchers.IO).launch {
            // Do the POST request and get response
            /*val response = service.postPaymentInfo(
                ConstantsApi.alToken.get(0), requestBody
            )*/
            LoggerDDF.e("jwt", ConstantsApi.alToken[0] + "");
            LoggerDDF.e("CRYPTO_TYPE", ConstantsActivity.STR_CURRENCY_CODE + " @279");
            LoggerDDF.e("fiatType", ConstantsActivity.STR_CURRENCY_FIAT_TYPE + " @279");
            var response: Response<ResponseBody>? = null
            if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT === "GEIDEA") {
                response = service.postPaymentInfoThree(
                    ConstantsApi.alToken[0],
                    requestBody,
                    ConstantsActivity.STR_CURRENCY_CODE,
                    ConstantsActivity.STR_CURRENCY_FIAT_TYPE
                )
            } else {
                response = service.postPaymentInfoTwo(
                    ConstantsApi.alToken[0],
                    requestBody,
                    ConstantsActivity.STR_CURRENCY_CODE,
                    ConstantsActivity.STR_CURRENCY_FIAT_TYPE
                )
            }


            ConstantsApi.API_STATUS_CODE = response.code()
            if (ConstantsApi.API_STATUS_CODE == 403 || ConstantsApi.API_STATUS_CODE == 503 || ConstantsApi.API_STATUS_CODE == 500) {
                LoggerDDF.e("fiatType", ConstantsActivity.STR_CURRENCY_FIAT_TYPE + "");
                amplifyOperationsSignOut(context)
            }
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    try {
                        // Convert raw JSON to pretty JSON using GSON library
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        val paymentInfoJson = gson.toJson(
                            JsonParser.parseString(
                                response.body()
                                    ?.string()
                            )
                        )
                        Log.e("paymentInfoJson JSON: ", paymentInfoJson)
                        val jsonResponseDATA = JSONObject(paymentInfoJson)
                        if (ConstantsActivity.FLAG_DDF_GEIDEA_ENVIRONMENT === "GEIDEA") {
                            ConstantsApi.strQRString = jsonResponseDATA.getString("qrString")
                            ConstantsApi.strQREncryptedString =
                                jsonResponseDATA.getString("qrEncrypedString")
                        }
                        ConstantsApi.strQRAddress = jsonResponseDATA.getString("address")
                        ConstantsApi.strUUID = jsonResponseDATA.getString("uuid")
                        if (ConstantsApi.strQRAddressFirst.equals("")) {
                            ConstantsApi.strQRAddressFirst = jsonResponseDATA.getString("address")
                        }
                        ConstantsActivity.STR_CURRENCY_AMOUNT =
                            jsonResponseDATA.getString("totalDigitalCurrency")
//                    val intent = Intent(this@RetrofitActivity, DetailsActivity::class.java)
//                    intent.putExtra("json_results", prettyJson)
//                    this@RetrofitActivity.startActivity(intent)
                        dialogCustomize.displayProgressDialog(
                            mContext,
                            ConstantsDialog.FLAG_DIALOG_DISMISS
                        )
                        generateQR()
                    } catch (e: Exception) {
                        dialogCustomize.displayProgressDialog(
                            mContext,
                            ConstantsDialog.FLAG_DIALOG_DISMISS
                        )
                        LoggerDDF.e(TAG, e.toString() + "")
                        dialogCustomize.displayDialog(
                            mContext,
                            ConstantsDialog.FLAG_DIALOG_DISPLAY,
                            "SERVER ERROR",
                            e.toString()
                        )

                    }
                } else {
                    dialogCustomize.displayProgressDialog(
                        mContext,
                        ConstantsDialog.FLAG_DIALOG_DISMISS
                    )

                    Log.e("RETROFIT_ERROR@240", response.code().toString())
                    Log.e("RETROFIT_ERROR@240", response.message().toString())
                    Log.e("RETROFIT_ERROR@240", response.toString())
                    Log.e("RETROFIT_ERROR@240", response.body().toString())
                    Log.e("RETROFIT_ERROR@240", response.errorBody().toString())
                    Log.e("RETROFIT_ERROR@240", response.raw().toString())
//                    Log.e("RETROFIT_ERROR@240", response.getString("message"))
                    dialogCustomize.displayDialog(
                        mContext,
                        ConstantsDialog.FLAG_DIALOG_DISPLAY,
                        "SERVER ERROR",
                        response.code().toString() + "\n" + response.message()
                    )
                }
            }
        }
    }

    private fun amplifyOperationsSignOut(context: Context) {
        ConstantsApi.API_TEMP_JWT = ""
        val awsAmplifyOperations = AwsAmplifyOperations.getInstance(context)
        awsAmplifyOperations.amplifySignOut()
    }

    //
    fun refreshPaymentInfoRetrofit(context: Context) {
        try {
            LoggerDDF.e(TAG, "refreshPaymentInfoRetrofit")
            mContext = context
            networkCheck = NetworkCheck.getInstance()
            if (!networkCheck.isNetworkConnected(mContext)) {
                return
            }

            dialogCustomize = DialogCustomize.getInstance()
//            dialogCustomize.displayProgressDialog(mContext, ConstantsDialog.FLAG_DIALOG_DISPLAY)

            // Create Retrofit
            val retrofit = Retrofit.Builder()
                .baseUrl(ConstantsApi.BASE_URL)
                .build()

            // Create Service
            val service = retrofit.create(RetroFitParams::class.java)

            CoroutineScope(Dispatchers.IO).launch {
                // Do the POST request and get response

                LoggerDDF.e("jwt", ConstantsApi.alToken[0] + "");
                LoggerDDF.e("address", ConstantsApi.strQRAddress + "");
                val response = service.postRefreshPaymentInfo(
                    ConstantsApi.alToken[0] + "" + ConstantsApi.API_TEMP_JWT,
//                    ConstantsApi.alToken[0] + "",
                    ConstantsApi.strQRAddress
                )
                LoggerDDF.e("response@295", response.code().toString() + " @295");
                LoggerDDF.e("response@295", response.toString() + " @295");
                if (response.code() == 403 || response.code() == 500 || response.code() == 503) {
//                    response.code().toString() == "403"
                    LoggerDDF.e("response@403", response.code().toString() + " @403");
                    /*dialogCustomize.displayProgressDialog(
                        mContext,
                        ConstantsDialog.FLAG_DIALOG_DISMISS
                    )*/
                    amplifyOperations(context)
                    generateQR()
                    return@launch
                }
                LoggerDDF.e("response@302", response.code().toString() + " @302");
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        try {
// Convert raw JSON to pretty JSON using GSON library
                            val gson = GsonBuilder().setPrettyPrinting().create()
                            val paymentInfoJson = gson.toJson(
                                JsonParser.parseString(
                                    response.body()
                                        ?.string()
                                )
                            )

                            Log.e("Refresh Pay JSON: ", paymentInfoJson)
                            val jsonResponseDATA = JSONObject(paymentInfoJson)
//                    ConstantsApi.strQRAddress = jsonResponseDATA.getString("address")
                            ConstantsApi.strQRAddress = jsonResponseDATA.getString("address")
                            ConstantsActivity.STR_CURRENCY_AMOUNT =
                                jsonResponseDATA.getString("totalDigitalCurrency")
                            ConstantsActivity.STR_TRANSACTION_STATUS =
                                jsonResponseDATA.getString("transactionStatus")
//                    ConstantsApi.strTSReceived = jsonResponseDATA.getString("digitalCurrencyReceived")
//                    ConstantsApi.bigDecimalTSReceived = ConstantsApi.strTSReceived.toBigDecimal()
                            /*dialogCustomize.displayProgressDialog(
                                mContext,
                                ConstantsDialog.FLAG_DIALOG_DISMISS
                            )*/
                            generateQR()
                        } catch (e: Exception) {
                            /*dialogCustomize.displayProgressDialog(
                                mContext,
                                ConstantsDialog.FLAG_DIALOG_DISMISS
                            )*/
                            LoggerDDF.e(TAG, e.toString() + "")
                            dialogCustomize.displayDialog(
                                mContext,
                                ConstantsDialog.FLAG_DIALOG_DISPLAY,
                                "SERVER ERROR",
                                e.toString()
                            )

                        }

                    } else {
                        /*dialogCustomize.displayProgressDialog(
                            mContext,
                            ConstantsDialog.FLAG_DIALOG_DISMISS
                        )*/

                        Log.e("RETROFIT_ERROR@339", response.code().toString())
                        Log.e("RETROFIT_ERROR@339", response.toString())
                        dialogCustomize.displayDialog(
                            mContext,
                            ConstantsDialog.FLAG_DIALOG_DISPLAY,
                            "SERVER ERROR",
                            response.code().toString() + "\n" + response.message()
                        )
                    }
                }
            }
        } catch (e: Exception) {
            dialogCustomize.displayProgressDialog(
                mContext,
                ConstantsDialog.FLAG_DIALOG_DISMISS
            )
            LoggerDDF.e(TAG, e.toString() + "")
            dialogCustomize.displayDialog(
                mContext,
                ConstantsDialog.FLAG_DIALOG_DISPLAY,
                "SERVER ERROR",
                e.toString()
            )
        }

    }

    private fun amplifyOperations(context: Context) {
        LoggerDDF.e(TAG, "amplifyOperations@462")
        ConstantsApi.API_TEMP_JWT = ""
        val awsAmplifyOperations = AwsAmplifyOperations.getInstance(context)
//        awsAmplifyOperations.amplifyReSignIn("suresh.gandham@wadzpay.com", "Wadzpay@123")
        awsAmplifyOperations.amplifyReSignIn(
            ConstantsActivity.STR_USER_EMAIL,
            ConstantsActivity.STR_USER_PASSWORD
        )
    }

    //
    fun getTransactionStatus(context: Context) {
        LoggerDDF.e(TAG, "getTransactionStatusInfoRetrofit")
        mContext = context
        networkCheck = NetworkCheck.getInstance()
        if (!networkCheck.isNetworkConnected(mContext)) {
            return
        }

        dialogCustomize = DialogCustomize.getInstance()
        dialogCustomize.displayProgressDialog(mContext, ConstantsDialog.FLAG_DIALOG_DISPLAY)

        // Create Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(ConstantsApi.BASE_URL)
            .build()

        // Create Service
        val service = retrofit.create(RetroFitParams::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            // Do the POST request and get response

            LoggerDDF.e("jwt", ConstantsApi.alToken[0] + "");
            LoggerDDF.e("address", ConstantsApi.strQRAddress + "");
            LoggerDDF.e("strQRAddressFirst", ConstantsApi.strQRAddressFirst + "");
//            ConstantsApi.strQRAddress = "2NBZtkpo26w9NozFm2SFcNNNQqvWVvePJA7"
            val response = service.getTransactionStatus(
                ConstantsApi.alToken[0],
                ConstantsApi.strQRAddressFirst
            )

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    try {
                        // Convert raw JSON to pretty JSON using GSON library
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        val paymentInfoJson = gson.toJson(
                            JsonParser.parseString(
                                response.body()
                                    ?.string()
                            )
                        )
                        Log.e("Transaction JSON: ", paymentInfoJson)
                        val jsonResponseDATA = JSONObject(paymentInfoJson)
//                    ConstantsApi.strQRAddress = jsonResponseDATA.getString("address")
//                    ConstantsActivity.STR_CURRENCY_AMOUNT = jsonResponseDATA.getString("totalDigitalCurrency")
//                    ConstantsActivity.STR_TRANSACTION_STATUS = jsonResponseDATA.getString("transactionStatus")
//                    ConstantsApi.strTSStatus = jsonResponseDATA.getString("status")
                        ConstantsApi.strTSStatus =
                            jsonResponseDATA.getString("transactionStatus")
//                    ConstantsApi.strTSFiat = jsonResponseDATA.getString("amountfiat")
                        ConstantsApi.strTSFiat = jsonResponseDATA.getString("totalFiatReceived")
//                    ConstantsApi.strTSCrypto = jsonResponseDATA.getString("amountcrypto")
                        ConstantsApi.strTSCrypto =
                            jsonResponseDATA.getString("totalDigitalCurrency")
                        ConstantsApi.strTSDigitalCurrencyType =
                            jsonResponseDATA.getString("digitalCurrencyType")
                        LoggerDDF.e(TAG + "@424", ConstantsApi.strTSDigitalCurrencyType + "")
                        ConstantsApi.strTSReceived =
                            jsonResponseDATA.getString("digitalCurrencyReceived")
//                    ConstantsApi.strTSCreated = jsonResponseDATA.getString("createdAt")
                        ConstantsApi.strUUID = jsonResponseDATA.getString("uuid")
                        ConstantsApi.strTransactionID =
                            jsonResponseDATA.getString("transactionId")
                        ConstantsApi.bigDecimalTSCrypto =
                            ConstantsApi.strTSCrypto.toBigDecimal()
//                    ConstantsApi.bigDecimalTSReceived = ConstantsApi.strTSReceived.toBigDecimal()
                        Log.e(TAG + "@333", ConstantsApi.bigDecimalTSCrypto.toString())
                        Log.e(TAG + "@333", ConstantsApi.strTSCrypto.toString())

                        generateQR()
                        dialogCustomize.displayProgressDialog(
                            mContext,
                            ConstantsDialog.FLAG_DIALOG_DISMISS
                        )
                    } catch (e: Exception) {
                        dialogCustomize.displayProgressDialog(
                            mContext,
                            ConstantsDialog.FLAG_DIALOG_DISMISS
                        )
                        LoggerDDF.e(TAG, e.toString() + "")
                        dialogCustomize.displayDialog(
                            mContext,
                            ConstantsDialog.FLAG_DIALOG_DISPLAY,
                            "SERVER ERROR",
                            e.toString()
                        )

                    }
                } else {
                    dialogCustomize.displayProgressDialog(
                        mContext,
                        ConstantsDialog.FLAG_DIALOG_DISMISS
                    )

                    Log.e("RETROFIT_ERROR@457", response.code().toString())
                    Log.e("RETROFIT_ERROR@457", response.toString())
                    dialogCustomize.displayDialog(
                        mContext,
                        ConstantsDialog.FLAG_DIALOG_DISPLAY,
                        "SERVER ERROR",
                        response.code().toString() + "\n" + response.message()
                    )
                }
            }
        }
    }

    //
    fun getTransactionStatusAlgo(context: Context) {
        LoggerDDF.e(TAG, "getTransactionStatusAlgo@578")
        mContext = context
        networkCheck = NetworkCheck.getInstance()
        if (!networkCheck.isNetworkConnected(mContext)) {
            return
        }

//        dialogCustomize = DialogCustomize.getInstance()
//        dialogCustomize.displayProgressDialog(mContext, ConstantsDialog.FLAG_DIALOG_DISPLAY)

        // Create Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(ConstantsApi.BASE_URL)
            .build()

        // Create Service
        val service = retrofit.create(RetroFitParams::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            // Do the POST request and get response

            LoggerDDF.e("jwt", ConstantsApi.alToken[0] + "");
            LoggerDDF.e("address", ConstantsApi.strQRAddress + "");
            LoggerDDF.e("strQRAddressFirst", ConstantsApi.strQRAddressFirst + "");
            LoggerDDF.e("strUUID", ConstantsApi.strUUID + "");
//            ConstantsApi.strQRAddress = "2NBZtkpo26w9NozFm2SFcNNNQqvWVvePJA7"
            val response = service.getTransactionStatusAlgo(
                ConstantsApi.alToken[0] + "" + ConstantsApi.API_TEMP_JWT,
                ConstantsApi.strUUID
            )
            if (response.code() == 403 || response.code() == 500 || response.code() == 503) {
//                    response.code().toString() == "403"
                LoggerDDF.e("response@403", response.code().toString() + " @403");
                /*dialogCustomize.displayProgressDialog(
                    mContext,
                    ConstantsDialog.FLAG_DIALOG_DISMISS
                )*/
                amplifyOperations(context)
                generateQR()
                return@launch
            }
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    try {
                        // Convert raw JSON to pretty JSON using GSON library
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        val paymentInfoJson = gson.toJson(
                            JsonParser.parseString(
                                response.body()
                                    ?.string()
                            )
                        )
                        Log.e("Transaction JSON: ", paymentInfoJson)
                        val jsonResponseDATA = JSONObject(paymentInfoJson)
//                    ConstantsApi.strQRAddress = jsonResponseDATA.getString("address")
//                    ConstantsActivity.STR_CURRENCY_AMOUNT = jsonResponseDATA.getString("totalDigitalCurrency")
//                    ConstantsActivity.STR_TRANSACTION_STATUS = jsonResponseDATA.getString("transactionStatus")
//                    ConstantsApi.strTSStatus = jsonResponseDATA.getString("status")
                        ConstantsApi.strTSStatus =
                            jsonResponseDATA.getString("transactionStatus")
                        ConstantsApi.strTSFiat = jsonResponseDATA.getString("totalFiatReceived")
                        ConstantsActivity.STR_CURRENCY_FIAT_TYPE =
                            jsonResponseDATA.getString("fiatType")
                        ConstantsActivity.STR_TRANSACTION_STATUS = ConstantsApi.strTSStatus
//                    ConstantsApi.strTSFiat = jsonResponseDATA.getString("amountfiat")
//                        ConstantsApi.strTSFiat = jsonResponseDATA.getString("fiatAmount")
//                    ConstantsApi.strTSCrypto = jsonResponseDATA.getString("amountcrypto")
                        ConstantsApi.strTSCrypto =
                            jsonResponseDATA.getString("totalDigitalCurrency")
                        ConstantsApi.strTSDigitalCurrencyType =
                            jsonResponseDATA.getString("digitalCurrencyType")
                        LoggerDDF.e(TAG + "@424", ConstantsApi.strTSDigitalCurrencyType + "")
                        ConstantsApi.strTSReceived =
                            jsonResponseDATA.getString("digitalCurrencyReceived")
//                    ConstantsApi.strTSCreated = jsonResponseDATA.getString("createdAt")
                        ConstantsApi.strUUID = jsonResponseDATA.getString("uuid")
                        ConstantsApi.strTransactionID =
                            jsonResponseDATA.getString("transactionId")
                        ConstantsApi.bigDecimalTSCrypto =
                            ConstantsApi.strTSCrypto.toBigDecimal()
//                    ConstantsApi.bigDecimalTSReceived = ConstantsApi.strTSReceived.toBigDecimal()
                        Log.e(TAG + "@333", ConstantsApi.bigDecimalTSCrypto.toString())
                        Log.e(TAG + "@333", ConstantsApi.strTSCrypto.toString())

                        generateQR()
                        /*dialogCustomize.displayProgressDialog(
                            mContext,
                            ConstantsDialog.FLAG_DIALOG_DISMISS
                        )*/
                    } catch (e: Exception) {
                        /* dialogCustomize.displayProgressDialog(
                             mContext,
                             ConstantsDialog.FLAG_DIALOG_DISMISS
                         )*/
                        LoggerDDF.e(TAG, e.toString() + " @658")
                        dialogCustomize.displayDialog(
                            mContext,
                            ConstantsDialog.FLAG_DIALOG_DISPLAY,
                            "SERVER ERROR",
                            e.toString()
                        )

                    }
                } else {
                    /*dialogCustomize.displayProgressDialog(
                        mContext,
                        ConstantsDialog.FLAG_DIALOG_DISMISS
                    )*/

                    Log.e("RETROFIT_ERROR@457", response.code().toString())
                    Log.e("RETROFIT_ERROR@457", response.toString())
                    dialogCustomize.displayDialog(
                        mContext,
                        ConstantsDialog.FLAG_DIALOG_DISPLAY,
                        "SERVER ERROR",
                        response.code().toString() + "\n" + response.message()
                    )
                }
            }
        }
    }

    //
    fun addPosToMerchant(context: Context) {
        LoggerDDF.e(TAG, "addPosToMerchant")
        mContext = context
        networkCheck = NetworkCheck.getInstance()
        if (!networkCheck.isNetworkConnected(mContext)) {
            return
        }

        dialogCustomize = DialogCustomize.getInstance()
        dialogCustomize.displayProgressDialog(mContext, ConstantsDialog.FLAG_DIALOG_DISPLAY)

        val retrofit = Retrofit.Builder()
            .baseUrl(ConstantsApi.BASE_URL)
            .build()
        LoggerDDF.e(TAG, ConstantsApi.BASE_URL + "");
        // Create Service
        val service = retrofit.create(RetroFitParams::class.java)

        // Create JSON using JSONObject
        val jsonObject = JSONObject()
//        jsonObject.put("posName", "POS-NEW")
        LoggerDDF.e(TAG, ConstantsApi.strPosName + " @483");
        jsonObject.put("posName", "0")
        jsonObject.put("posNumber", "0")

        // Convert JSONObject to String
        val jsonObjectString = jsonObject.toString()
        LoggerDDF.e(TAG,jsonObjectString)
        // Create RequestBody ( We're not using any converter, like GsonConverter, MoshiConverter e.t.c, that's why we use RequestBody )
        val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

        CoroutineScope(Dispatchers.IO).launch {
            // Do the POST request and get response
            LoggerDDF.e(TAG, ConstantsApi.alToken.get(0) + "")
            val response = service.addPosToMerchant(
                ConstantsApi.alToken.get(0),
                requestBody
            )

            withContext(Dispatchers.Main) {
                LoggerDDF.e(TAG + " @520", TAG + " @520");
                LoggerDDF.e(TAG + " @520", response.code().toString() + "");
                if (response.isSuccessful) {
                    try {
                        // Convert raw JSON to pretty JSON using GSON library
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        val addPosJson = gson.toJson(
                            JsonParser.parseString(
                                response.body()
                                    ?.string()
                            )
                        )
                        Log.e("AddPOS Json: ", addPosJson)
                        val jsonResponseDATA = JSONObject(addPosJson)

//                         ConstantsApi.strTransactionID =
//                            jsonResponseDATA.getString("transactionId")
                        ConstantsApi.strPosID = jsonResponseDATA.getString("posId")

//                    val jsonResponseDATA = JSONObject(addPosJson)
//                    val apiJsonParsing = ApiJsonParsing.getInstance()

                        /*ConstantsApi.alConversions = apiJsonParsing.jsonHashMapArrayKeys(
                            jsonResponseDATA.getJSONArray("paymentModes"),
                            ConstantsApi.KEYS_CONVERSION_PARSE
                        );*/

                        dialogCustomize.displayProgressDialog(
                            mContext,
                            ConstantsDialog.FLAG_DIALOG_DISMISS
                        )

                        activityCallback = mContext as ActivityCallBack
                        activityCallback!!.activityCall(ConstantsApi.FLAG_API_CONVERSION)
                    } catch (e: Exception) {
                        dialogCustomize.displayProgressDialog(
                            mContext,
                            ConstantsDialog.FLAG_DIALOG_DISMISS
                        )
                        LoggerDDF.e(TAG, e.toString() + "")
                        dialogCustomize.displayDialog(
                            mContext,
                            ConstantsDialog.FLAG_DIALOG_DISPLAY,
                            "SERVER ERROR",
                            e.toString()
                        )

                    }
                } else {
//                    dialogProgress.dismiss()
                    dialogCustomize.displayProgressDialog(
                        mContext,
                        ConstantsDialog.FLAG_DIALOG_DISMISS
                    )
                    Log.e("RETROFIT_ERROR@561", response.code().toString() + "@400")
                    Log.e("RETROFIT_BODY", response.errorBody().toString() + "@400")
                    Log.e("RETROFIT_RAW", response.raw().toString() + "@400")
                    Log.e("RETROFIT_MESSAGE", response.message().toString() + "@400")
                    Log.e("RETROFIT_ERROR@565", "$response @400")
                    dialogCustomize.displayDialog(
                        mContext,
                        ConstantsDialog.FLAG_DIALOG_DISPLAY,
                        "SERVER ERROR",
                        response.code().toString() + "\n" + response.message()
                    )
                }
            }
        }
    }

    //
    private fun generateQR() {
        LoggerDDF.e(TAG, "generateQR@813")
        activityCallback = mContext as ActivityCallBack
        activityCallback!!.activityCall(ConstantsApi.FLAG_API_PAYMENT_INFO)

//        val intent = Intent(mContext, QRGeneratorActivity::class.java)
//        mContext.startActivity(intent)
    }
}