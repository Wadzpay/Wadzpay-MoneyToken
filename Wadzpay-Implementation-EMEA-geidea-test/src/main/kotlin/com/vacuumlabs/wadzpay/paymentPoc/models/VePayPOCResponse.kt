package com.vacuumlabs.wadzpay.paymentPoc.models

import java.math.BigDecimal

data class VePayPOCResponse(
    var status: Boolean,
    var data: String?,
    var transaction_id: String?,
    var message: String?,
    var status_code: Int,
    var ref_link: String?
)

data class VePayPaymentTransferResponse(
    var amount: BigDecimal?,
    var currency: String?,
    var transactionId: String?,
    var pay_id: String?,
    var purpose: String?,
    var reference_id: String?,
    var payment_details: PaymentDetails?,
    var status: String?,
    var message: String?
)

data class PaymentDetails(
    var type: String?,
    var account_number: String?,
    var ifsc_code: String?,
    var beneficiary_name: String?,
    var mode: String?
)

data class VePayPaymentStatusCheckResponse(
    var amount: BigDecimal?,
    var currency: String?,
    var transactionId: String?,
    var orderId: String?,
    var pay_id: String?,
    var purpose: String?,
    var reference_id: String?,
    var payment_details: Object?,
    var status: Object?,
    var message: String?
)

data class UPI(
    var vpa: String?
)

data class VePayPOCRefundResponse(
    var status: Boolean?,
    var message: String?
)

data class VePayPOCRefundRequest(
    var transaction_id: String?,
    var amount: BigDecimal?
)

enum class PayoutStatusCode {
    INPROGRESS,
    FAIL,
    CAPTURED,
    INVALID,
    PENDING,
    MRRF,
    DECLINED,
    CANCEL,
    DUPLICATE,
    REJECT,
    TIMEOUT,
    SUCCESS,
    CREATED,
    ACCEPTED,
    FAILED,
    FAILURE,
    IP_WHITELIST,
    INSUFFICIENT_BALANCE,
    UNAUTHORIZED,
    UNAVAILABLE
}
