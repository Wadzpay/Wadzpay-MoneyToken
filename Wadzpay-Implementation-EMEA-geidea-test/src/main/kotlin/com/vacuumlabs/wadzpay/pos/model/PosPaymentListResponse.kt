package com.vacuumlabs.wadzpay.pos.model

import com.fasterxml.jackson.annotation.JsonIgnore
import java.math.BigDecimal

data class PosPaymentListResponse(val provider: String = "WadzPay", val paymentModes: ArrayList<PaymentMode>)
data class PosEncryptedTransactionDetailsResponse(val data: String, val saltKey: String)

data class PaymentMode(val asset: String, val amountFiat: BigDecimal, val amountCrypto: BigDecimal, val fees: PosFees, val totalAmount: BigDecimal,)
data class PosFees(
    val feeOnWadzpay: BigDecimal,
    @JsonIgnore
    val feeOnExternal: BigDecimal
)
