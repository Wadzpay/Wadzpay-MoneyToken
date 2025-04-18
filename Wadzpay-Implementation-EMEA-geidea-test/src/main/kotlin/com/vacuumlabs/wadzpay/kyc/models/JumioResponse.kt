package com.vacuumlabs.wadzpay.kyc.models

data class JumioResponse(
    val timestamp: String,
    val transactionReference: String,
    val redirectUrl: String,
    var successUrl: String = ""
)
