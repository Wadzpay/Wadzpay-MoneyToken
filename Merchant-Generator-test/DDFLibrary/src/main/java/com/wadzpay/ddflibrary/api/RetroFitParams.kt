package com.wadzpay.ddflibrary.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query


interface RetroFitParams {

    @POST(ConstantsApi.CONVERSION_URL)
    suspend fun postConversionList(
        @Header("Authorization") auth: String = "",
        @Body requestBody: RequestBody,
        @Query("fiatType") fiatType: String = ""
    ): Response<ResponseBody>

    @POST(ConstantsApi.CONVERSION_URL_TWO)
    suspend fun postConversionListTwo(
        @Header("Authorization") auth: String = "",
        @Body requestBody: RequestBody,
        @Query("fiatType") fiatType: String = ""
    ): Response<ResponseBody>

    @POST(
        ConstantsApi.PAYMENT_INFO_URL_TWO
    )
    suspend fun postPaymentInfoTwo(
        @Header("Authorization") auth: String = "",
        @Body requestBody: RequestBody,
//        @Query("cryptoType") cryptType: String = "",
        @Query("digitalCurrencyType") digitalCurrencyType: String = "",
        @Query("fiatType") fiatType: String = ""
    ): Response<ResponseBody>

    @POST(
        ConstantsApi.PAYMENT_INFO_URL_THREE
    )
    suspend fun postPaymentInfoThree(
        @Header("Authorization") auth: String = "",
        @Body requestBody: RequestBody,
//        @Query("cryptoType") cryptType: String = "",
        @Query("digitalCurrencyType") digitalCurrencyType: String = "",
        @Query("fiatType") fiatType: String = ""
    ): Response<ResponseBody>

    @POST(
        ConstantsApi.REFRESH_PAYMENT_INFO_URL
    )
    suspend fun postRefreshPaymentInfo(
        @Header("Authorization") auth: String = "",
        @Query("blockChainAddress") digitalCurrencyType: String = "",
    ): Response<ResponseBody>

    @POST(
        ConstantsApi.TRANSACTION_STATUS_URL
    )
    suspend fun getTransactionStatus(
        @Header("Authorization") auth: String = "",
        @Query("blockChainAddress") digitalCurrencyType: String = "",
    ): Response<ResponseBody>

    @POST(
        ConstantsApi.TRANSACTION_STATUS_URL_TWO
    )
    suspend fun getTransactionStatusAlgo(
        @Header("Authorization") auth: String = "",
        @Query("uuid") digitalCurrencyType: String = "",
    ): Response<ResponseBody>

    @POST(ConstantsApi.ADD_POS_MERCHANT_URL)
    suspend fun addPosToMerchant(
        @Header("Authorization") auth: String = "",
        @Body requestBody: RequestBody
    ): Response<ResponseBody>
}