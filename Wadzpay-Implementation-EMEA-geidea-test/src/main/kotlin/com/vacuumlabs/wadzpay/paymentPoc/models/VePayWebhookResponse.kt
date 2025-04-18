package com.vacuumlabs.wadzpay.paymentPoc.models

import java.math.BigDecimal

data class VePayWebhookResponse(
    var amount: BigDecimal?,
    var currency: String?,
    var transactionId: String?,
    var customer_details: Object?,
    var orderId: String?,
    var payment_details: Object?,
    var status: String?,
    var message: String?
)

data class CustomerDetails(
    var mobile: String?,
    var email: String?
)

data class VePayWebhookRes(
    var status: Boolean?
)
