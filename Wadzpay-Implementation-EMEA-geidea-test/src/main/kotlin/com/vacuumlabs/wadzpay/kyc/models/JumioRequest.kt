package com.vacuumlabs.wadzpay.kyc.models

data class JumioRequest(val customerInternalReference: String, val userReference: String, val callbackUrl: String, val successUrl: String)
