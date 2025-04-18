package com.wadzpay.ddflibrary.api

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
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
import org.json.JSONObject
import retrofit2.Retrofit

class ApiRetroFitPaymentInfoTwo {
    private val TAG = javaClass.simpleName
    lateinit var mContext: Context
    private lateinit var dialogCustomize: DialogCustomize
    private var activityCallback: ActivityCallBack? = null
    private lateinit var networkCheck: NetworkCheck

    //
    fun conversionListRetrofit(context: Context) {
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
            val response = service.postConversionList(
                ConstantsApi.alToken.get(0),
                requestBody,
                ConstantsActivity.STR_CURRENCY_FIAT_TYPE
            )

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {

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
                            .get(ConstantsApi.KEYS_CONVERSION_PARSE[3]) + ""
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

                } else {
//                    dialogProgress.dismiss()
                    dialogCustomize.displayProgressDialog(
                        mContext,
                        ConstantsDialog.FLAG_DIALOG_DISMISS
                    )
                    Log.e("RETROFIT_ERROR", response.code().toString())

                }
            }
        }
    }
}