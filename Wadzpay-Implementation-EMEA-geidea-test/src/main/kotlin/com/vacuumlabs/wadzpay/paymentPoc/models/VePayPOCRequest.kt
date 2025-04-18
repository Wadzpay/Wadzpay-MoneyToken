package com.vacuumlabs.wadzpay.paymentPoc.models

import java.math.BigDecimal

data class VePayPOCRequest(
    var curr_code: String?,
    var amount: BigDecimal?,
    var desc: String?,
    var merchant_order_token: String?,
    var customer_email: String?,
    var customer_mobile: String?,
    var customer_first_name: String?,
    var customer_last_name: String?,
    var ord_title: String?,
    var success_url: String?,
    var fail_url: String?,
    var pg_cancel_url: String?,
    var api_key: String?
)

data class VePayPaymentTransferRequest(
    var amount: BigDecimal?,
    var curr_code: String?,
    var purpose: String?,
    var queue_if_low_balance: Boolean?,
    var reference_id: String?,
    var narration: String?,
    var phonenumber: String?,
    var transfer_type: BankDetails,
)

data class BankDetails(
    var type: PaymentType,
    var account_number: String?,
    var ifsc_code: String?,
    var beneficiary_name: String?,
    var mode: PaymentMode
)

enum class PaymentMode {
    NEFT,
    RTGS,
    IMPS
}

enum class PaymentType {
    bank,
    upi,
    wallet
}
data class VePayPaymentStatusCheckRequest(
    var identifier: String?
)

data class VePayPayoutStatusCheckRequest(
    var transactionId: String?
)
