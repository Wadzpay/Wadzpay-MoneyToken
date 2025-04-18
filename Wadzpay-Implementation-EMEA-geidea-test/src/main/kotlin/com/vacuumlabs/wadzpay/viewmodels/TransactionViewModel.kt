package com.vacuumlabs.wadzpay.viewmodels

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvBindByPosition
import com.opencsv.bean.CsvDate
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.RefundMode
import com.vacuumlabs.wadzpay.ledger.model.RefundStatus
import com.vacuumlabs.wadzpay.ledger.model.RefundType
import com.vacuumlabs.wadzpay.ledger.model.Transaction
import com.vacuumlabs.wadzpay.ledger.model.TransactionDirection
import com.vacuumlabs.wadzpay.ledger.model.TransactionMode
import com.vacuumlabs.wadzpay.ledger.model.TransactionRefundDetails
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.Merchant
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Date
import java.sql.Time
import java.time.Instant
import java.util.UUID

fun Transaction.toViewModel(
    negateAmount: Boolean = false,
    direction: TransactionDirection = TransactionDirection.UNKNOWN
):
    TransactionViewModel {
        val posTrx = this.posTransaction
        var posId: String? = null
        var orderId: Long? = null
        var posTransactionIdTemp: String? = null
        var requestedDigitalAmount: BigDecimal = BigDecimal.ZERO
        if (!posTrx.isNullOrEmpty()) {
            posId = posTrx[0].merchantPos?.posId
            orderId = posTrx[0].orderId
            posTransactionIdTemp = posTrx[posTrx.size - 1].uuid
            requestedDigitalAmount =
                posTrx.find { it.status == TransactionStatus.SUCCESSFUL || it.status == TransactionStatus.UNDERPAID || it.status == TransactionStatus.OVERPAID || it.status == TransactionStatus.FAILED }?.amountcrypto
                ?: BigDecimal.ZERO
        }
        var createdAt = this.createdAt
        if (this.type == TransactionType.POS && this.paymentReceivedDate != null) {
            createdAt = this.paymentReceivedDate!!
        }
        return TransactionViewModel(
            this.uuid,
            this.order?.uuid,
            this.order?.externalOrderId,
            createdAt = createdAt,
            if (negateAmount) this.amount.negate() else this.amount,
            this.asset,
            if (negateAmount) this.fiatAmount?.negate() else this.fiatAmount,
            this.fiatAsset,
            this.status,
            this.type,
            getSender(this),
            getReceiver(this),
            direction,
            this.description,
            this.fee + this.amount,
            if (this.fee.signum() == 0) BigDecimal.ZERO else this.fee.divide(this.amount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)),
            this.fee,
            this.blockchainTxId,
            this.tx_id.toString(),
            if (negateAmount) this.totalDigitalCurrencyReceived?.negate() else this.totalDigitalCurrencyReceived,
            this.totalFiatReceived,
            posId,
            orderId,
            this.paymentReceivedDate,
            this.extPosSequenceNo,
            this.extPosTransactionId, extPosId = this.extPosId,
            sequenceNumber = this.sequenceNumber,
            extPosLogicalDate = this.extPosLogicalDate,
            extPosShift = this.extPosShift,
            extPosActualDate = this.extPosActualDate,
            extPosActualTime = this.extPosActualTime,
            requestedDigitalAmount = requestedDigitalAmount,
            txnMode = this.txMode,
            refundTransactionId = this.refundTransactionId,
            totalRequestedAmount = getRequestedAmount(this),
            totalRequestedAmountAsset = getRequestedAmountAsset(this),
            totalFeeApplied = this.totalFeeApplied,
            totalFeeAppliedAsset = this.totalFeeAppliedAsset,
            senderFirstName = this.sender.account.getOwnerFirstName(),
            senderLastName = this.sender.account.getOwnerLastName(),
            receiverFirstName = this.receiver.account.getOwnerFirstName(),
            receiverLastName = this.receiver.account.getOwnerLastName(),
            issuerName = getIssuanceBanks(this),
            senderDetails = this.sender.account.getOwnerName(),
            receiverDetails = this.receiver.account.getOwnerName()
        )
    }

fun getIssuanceBanks(transaction: Transaction): String? {
    if (transaction.issuanceBanks != null) {
        return transaction.issuanceBanks!!.bankName
    }
    return null
}

fun getRequestedAmountAsset(transaction: Transaction): String? {
    if (transaction.type == TransactionType.DEPOSIT) {
        return if (transaction.totalRequestedAmountAsset != null) {
            transaction.totalRequestedAmountAsset
        } else {
            transaction.fiatAsset.toString()
        }
    } else if (transaction.type == TransactionType.WITHDRAW) {
        return if (transaction.totalRequestedAmountAsset != null) {
            transaction.totalRequestedAmountAsset
        } else {
            transaction.asset.toString()
        }
    }
    return transaction.asset.toString()
}
fun getRequestedAmount(transaction: Transaction): BigDecimal? {
    if (transaction.type == TransactionType.DEPOSIT) {
        return if (transaction.totalRequestedAmount != null) {
            transaction.totalRequestedAmount
        } else {
            transaction.fiatAmount
        }
    } else if (transaction.type == TransactionType.WITHDRAW) {
        return if (transaction.totalRequestedAmount != null) {
            transaction.totalRequestedAmount
        } else {
            transaction.amount
        }
    }
    return transaction.amount
}
fun getReceiver(transaction: Transaction): String? {
    return when (transaction.type) {
        TransactionType.SELL -> {
            "Withdraw"
        }
        else -> {
            transaction.receiver.account.getOwnerName()
        }
    }
}

fun getSender(transaction: Transaction): String? {
    return when (transaction.type) {
        TransactionType.BUY -> {
            "Buy"
        }
        TransactionType.SWAP -> {
            "Swap"
        }
        TransactionType.DEPOSIT -> {
            "Deposit"
        }
        TransactionType.WITHDRAW -> {
            "Withdraw"
        }
        else -> {
            transaction.sender.account.getOwnerName()
        }
    }
}

fun Transaction.toRefundViewModel(direction: TransactionDirection = TransactionDirection.UNKNOWN, refundTransaction: TransactionRefundDetails?): RefundTransactionViewModel {

    val posTrx1 = this.posTransaction
    var posId1: String? = null
    if (!posTrx1.isNullOrEmpty()) {
        posId1 = posTrx1[0].merchantPos?.posId
    }
    return RefundTransactionViewModel(
        this.uuid,
        this.order?.uuid,
        this.order?.externalOrderId,
        createdAt = getUpdatedTimeForRefundAndInitiateTransaction(this, refundTransaction),
        this.amount,
        CurrencyUnit.valueOf(this.asset),
        this.fiatAmount,
        this.fiatAsset,
        this.status,
        transactionType = TransactionType.REFUND, // Always setting to Refund
        this.sender.account.getOwnerName(),
        this.receiver.account.getOwnerName(),
        direction,
        this.description,
        this.fee + this.amount,
        if (this.fee.signum() == 0) BigDecimal.ZERO else this.fee.divide(this.amount, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100)),
        this.fee,
        this.blockchainTxId,
        this.tx_id.toString(),
        this.totalDigitalCurrencyReceived,
        refundTransaction?.refundStatus,
        refundTransaction?.refundUserName,
        refundTransaction?.refundUserMobile,
        refundTransaction?.refundUserEmail,
        refundTransaction?.refundFiatType,
        refundTransaction?.refundWalletAddress,
        refundTransaction?.refundDigitalCurrencyType,
        refundTransaction?.refundReason,
        refundTransaction?.refundAcceptanceComment,
        refundTransaction?.refundApprovalComment,
        refundTransaction?.refundAmountFiat ?: BigDecimal.ZERO,
        refundTransaction?.refundMode ?: RefundMode.NA,
        this.totalFiatReceived,
        refundAmountDigital = if (refundTransaction?.refundTransactionFinance != null) {
            val feeByWadzpay = refundTransaction.refundTransactionFinance?.feeByWadzpay ?: BigDecimal.ZERO
            val amountCrypto = refundTransaction.refundTransactionFinance?.amountCrypto ?: BigDecimal.ZERO
            val feeByBlockchain = refundTransaction.refundTransactionFinance?.feeByBlockchain ?: BigDecimal.ZERO
            amountCrypto - feeByWadzpay - feeByBlockchain
        } else {
            BigDecimal.ZERO
        },
        sourceWalletAddress = if (refundTransaction != null) (refundTransaction.sourceWalletAddress ?: this.sourceWalletAddress) else this.sourceWalletAddress,
        this.extPosTransactionId,
        extPosLogicalDate = this.extPosLogicalDate,
        extPosShift = this.extPosShift,
        extPosActualDate = this.extPosActualDate,
        extPosActualTime = this.extPosActualTime,
        paymentReceivedDate = this.paymentReceivedDate,
        refundDateTime = refundTransaction?.refundDateTime,
        refundSettlementDate = refundTransaction?.refundSettlementDate,
        refundType = refundTransaction?.refundType ?: RefundType.NA,
        balanceAmountFiat = this.totalFiatReceived?.minus(this.totalRefundedAmountFiat) ?: BigDecimal.ZERO,
        numberOfRefunds = if (refundTransaction != null) refundTransaction.numberOfRefunds
        else if (this.refundTransactions != null && this.refundTransactions?.isEmpty() == false) (this.refundTransactions?.size?.plus(1)) else 1,
        walletAddressMatch = refundTransaction?.walletAddressMatch,
        extPosLogicalDateRefund = refundTransaction?.extPosLogicalDateRefund,
        extPosActualDateRefund = refundTransaction?.extPosActualDateRefund,
        extPosActualTimeRefund = refundTransaction?.extPosActualTimeRefund,
        extPosSequenceNoRefund = refundTransaction?.extPosSequenceNoRefund,
        extPosTransactionIdRefund = refundTransaction?.extPosTransactionIdRefund,
        refundTransactionID = refundTransaction?.uuid,
        extPosShiftRefund = refundTransaction?.extPosShiftRefund,
        refundOrigin = refundTransaction?.refundOrigin,
        refundableAmountFiat = if (refundTransaction != null) refundTransaction.refundableAmountFiat else this.totalFiatReceived?.minus(this.totalRefundedAmountFiat),
        isRefundReinitiate = refundTransaction?.isRefundReinitiate,
        txnMode = this.txMode,
        posId1
    )
}

fun getUpdatedTimeForRefundAndInitiateTransaction(transaction: Transaction, refundTransaction: TransactionRefundDetails?): Instant? {
    return if (refundTransaction != null) {
        refundTransaction.lastUpdatedOn
    } else {
        val refundT = transaction.refundTransactions?.filter { it.refundStatus == RefundStatus.REFUNDED }?.sortedByDescending { it.numberOfRefunds }
        if (refundT?.isEmpty() == false) {
            refundT.get(0).lastUpdatedOn
        } else {
            transaction.paymentReceivedDate
        }
    }
}

data class TransactionViewModel(
    @CsvBindByPosition(position = 0)
    @CsvBindByName(column = "UUID")
    val uuid: UUID,

    @CsvBindByPosition(position = 1)
    @CsvBindByName(column = "Order_UUID")
    val order_uuid: UUID?,

    @CsvBindByPosition(position = 2)
    @CsvBindByName(column = "External_order_id")
    val external_order_id: String?,

    @CsvBindByPosition(position = 3)
    @CsvBindByName(column = "Created_at")
    @CsvDate(value = "yyyy-MM-dd-HH:mm:ss")
    val createdAt: Instant,

    @CsvBindByPosition(position = 4)
    @CsvBindByName(column = "Transferred_amount")
    val amount: BigDecimal,

    @CsvBindByPosition(position = 5)
    @CsvBindByName(column = "Currency_unit")
    val asset: String,

    @CsvBindByPosition(position = 6)
    @CsvBindByName(column = "Transferred_fiat_amount")
    val fiatAmount: BigDecimal?,

    @CsvBindByPosition(position = 7)
    @CsvBindByName(column = "Fiat_currency_unit")
    val fiatAsset: FiatCurrencyUnit?,

    @CsvBindByPosition(position = 8)
    @CsvBindByName(column = "Status")
    val status: TransactionStatus,

    @CsvBindByPosition(position = 9)
    @CsvBindByName(column = "Type")
    val transactionType: TransactionType,

    @CsvBindByPosition(position = 10)
    @CsvBindByName(column = "Sender")
    val senderName: String?,

    @CsvBindByPosition(position = 11)
    @CsvBindByName(column = "Receiver")
    val receiverName: String?,

    @CsvBindByPosition(position = 12)
    @CsvBindByName(column = "Direction")
    val direction: TransactionDirection = TransactionDirection.UNKNOWN,

    @CsvBindByPosition(position = 13)
    @CsvBindByName(column = "Description")
    val description: String? = null,

    @CsvBindByPosition(position = 14)
    @CsvBindByName(column = "Total_Amount")
    val totalAmount: BigDecimal,

    @CsvBindByPosition(position = 15)
    @CsvBindByName(column = "Fee_Percentage")
    val feePercentage: BigDecimal = BigDecimal.ZERO,

    @CsvBindByPosition(position = 16)
    @CsvBindByName(column = "Fee_Amount")
    val feeAmount: BigDecimal = BigDecimal.ZERO,

    @CsvBindByPosition(position = 17)
    @CsvBindByName(column = "Blockchain_transaction_id")
    val blockchainTxId: String? = null,
    @CsvBindByPosition(position = 18)
    @CsvBindByName(column = "Trx_ID")
    val trxId: String? = null,
    @CsvBindByPosition(position = 19)
    @CsvBindByName(column = "total_digital_currency_received")
    val totalDigitalCurrencyReceived: BigDecimal? = null,
    @CsvBindByPosition(position = 20)
    val totalFiatReceived: BigDecimal? = BigDecimal.ZERO,
    @CsvBindByPosition(position = 21)
    var posId: String? = null,
    @CsvBindByPosition(position = 22)
    //  @JsonProperty("PosTransactionId")
    var orderId: Long? = null,
    @CsvBindByPosition(position = 23)
    var paymentReceivedDate: Instant? = null,
    @CsvBindByPosition(position = 24)
    val extPosSequenceNo: String? = null,
    @CsvBindByPosition(position = 25)
    val extPosTransactionId: String? = null,
    @CsvBindByPosition(position = 26)
    val conversionRate: BigDecimal? = conversionRateCalculate(amount, fiatAmount),
    @CsvBindByPosition(position = 27)
    val transactionId: String? = uuid.toString(),
    @CsvBindByPosition(position = 28)
    val extPosId: String? = null,
    @CsvBindByPosition(position = 29)
    val sequenceNumber: String? = null,
    @CsvBindByPosition(position = 30)
    val extPosLogicalDate: Instant? = null,
    @CsvBindByPosition(position = 31)
    val extPosShift: String? = null,
    @CsvBindByPosition(position = 32)
    val extPosActualDate: Date? = null,
    @CsvBindByPosition(position = 33)
    val extPosActualTime: Time? = null,
    @CsvBindByPosition(position = 34)
    val refundDateTime: Instant? = null,
    @CsvBindByPosition(position = 35)
    val refundSettlementDate: Instant? = null,
    @CsvBindByPosition(position = 36)
    val requestedDigitalAmount: BigDecimal = BigDecimal.ZERO,
    @CsvBindByPosition(position = 37)
    val txnMode: TransactionMode? = null,
    @CsvBindByPosition(position = 38)
    val refundTransactionId: String? = null,
    @CsvBindByPosition(position = 39)
    var feeConfigData: MutableList<FeeConfigData>? = null,
    @CsvBindByPosition(position = 40)
    val totalRequestedAmount: BigDecimal? = null,
    @CsvBindByPosition(position = 41)
    val totalRequestedAmountAsset: String? = null,
    @CsvBindByPosition(position = 42)
    val totalFeeApplied: BigDecimal? = null,
    @CsvBindByPosition(position = 43)
    val totalFeeAppliedAsset: String? = null,
    val senderFirstName: String? = null,
    val senderLastName: String ? = null,
    val receiverFirstName: String ? = null,
    val receiverLastName: String? = null,
    val issuerName: String? = null,
    val senderDetails: String? = null,
    val receiverDetails: String? = null,
)

fun conversionRateCalculate(amount: BigDecimal, fiatAmount: BigDecimal?): BigDecimal {
    try {
        return amount / fiatAmount!!
    } catch (e: Exception) {
        return BigDecimal("0.0")
    }
}

fun Transaction.toViewModelSettlementReport(merchant: Merchant):
    SettlementReport {
        val posTrx = this.posTransaction
        var posTd: String? = null

        if (!posTrx.isNullOrEmpty()) {
            posTd = posTrx[0].merchantPos?.posId
        }

        return SettlementReport(
            this.externalId ?: this.tx_id, posTd ?: "",
            this.uuid,
            this.createdAt,
            CurrencyUnit.valueOf(this.asset),
            this.amount,
            this.totalDigitalCurrencyReceived ?: BigDecimal.ZERO,
            getCommission(this.totalFiatReceived, merchant.settlementCommissionPercent, 2),
            getCommissionDeducted(this.totalDigitalCurrencyReceived, merchant.settlementCommissionPercent, 8),
            this.fiatAsset, getCommissionDeducted(this.totalFiatReceived, merchant.settlementCommissionPercent, 2),
            this.status,
            this.extPosLogicalDate,
            this.extPosShift,
            this.extPosActualDate,
            this.extPosActualTime,
            this.fiatAmount,
            this.totalFiatReceived
        )
    }

fun getCommissionDeducted(
    fiatOrCryptoAmount: BigDecimal?,
    commissionPercent: BigDecimal,
    roundOffTo: Int
): BigDecimal {
    var amount = (fiatOrCryptoAmount ?: BigDecimal.ZERO) - (
        (
            fiatOrCryptoAmount
                ?: BigDecimal.ZERO
            ) * commissionPercent
        )

    if (amount.stripTrailingZeros().scale() > roundOffTo) {
        val am =
            amount.setScale(roundOffTo, RoundingMode.UP).stripTrailingZeros()
        amount = am
    }

    return amount
}

fun getCommission(
    fiatAmount: BigDecimal?,
    commissionPercent: BigDecimal,
    roundOffTo: Int
): BigDecimal {
    var amount = (
        (
            fiatAmount
                ?: BigDecimal.ZERO
            ) * commissionPercent
        )
    if (amount.stripTrailingZeros().scale() > roundOffTo) {
        val am =
            amount.setScale(roundOffTo, RoundingMode.UP).stripTrailingZeros()
        amount = am
    }

    return amount
}

data class SettlementReport(
    @CsvBindByPosition(position = 0)
    @CsvBindByName(column = "Customer TXN ID")
    @JsonProperty("Customer TXN ID")
    val external_order_id: String?,
    @CsvBindByPosition(position = 1)
    @CsvBindByName(column = "POS ID")
    @JsonProperty("POS ID")
    val posId: String?,
    @CsvBindByPosition(position = 2)
    @CsvBindByName(column = "UUID")
    @JsonProperty("UUID")
    val uuid: UUID,
    @CsvBindByPosition(position = 3)
    @CsvBindByName(column = "Date")
    @CsvDate(value = "yyyy-MM-dd-HH:mm:ss")
    @JsonProperty("Date")
    val createdAt: Instant,
    @CsvBindByPosition(position = 4)
    @CsvBindByName(column = "Digital Currency")
    @JsonProperty("Digital Currency")
    val asset: CurrencyUnit,
    @CsvBindByPosition(position = 5)
    @CsvBindByName(column = "Digital Amount Requested")
    @JsonProperty("Digital Amount Requested")
    val totalAmount: BigDecimal,
    @CsvBindByPosition(position = 6)
    @CsvBindByName(column = "Digital Amount Received")
    @JsonProperty("Digital Amount Received")
    val totalDigitalCurrencyReceived: BigDecimal,
    @CsvBindByPosition(position = 7)
    @CsvBindByName(column = "Commission")
    @JsonProperty("Commission")
    val feeAmount: BigDecimal,
    @CsvBindByPosition(position = 8)
    @CsvBindByName(column = "Payable digital amount to merchant")
    @JsonProperty("Payable digital amount to merchant")
    val amount: BigDecimal,
    @CsvBindByPosition(position = 9)
    @CsvBindByName(column = "Fiat Currency Name")
    @JsonProperty("Fiat Currency Name")
    val fiatAsset: FiatCurrencyUnit?,
    @CsvBindByPosition(position = 10)
    @CsvBindByName(column = "Fiat Currency equivalent of payable digital amount")
    @JsonProperty("Fiat Currency equivalent of payable digital amount")
    val fiatAmount: BigDecimal?,
    @CsvBindByPosition(position = 11)
    @CsvBindByName(column = "Status")
    @JsonProperty("Status")
    val status: TransactionStatus,
    @CsvBindByPosition(position = 12)
    val extPosLogicalDate: Instant? = null,
    @CsvBindByPosition(position = 13)
    val extPosShift: String? = null,
    @CsvBindByPosition(position = 14)
    val extPosActualDate: Date? = null,
    @CsvBindByPosition(position = 15)
    val extPosActualTime: Time? = null,
    @CsvBindByPosition(position = 16)
    val fiatAmountRequested: BigDecimal? = null,
    @CsvBindByPosition(position = 17)
    val totalFiatAmountReceived: BigDecimal? = null
)

data class FeeConfigData(
    var feeName: String? = null,
    var feeFrequency: String? = null,
    var feeAmount: BigDecimal? = null,
    var currencyUnit: String? = null,
    var description: String? = null
)

data class RefundTransactionViewModel(
    @CsvBindByPosition(position = 0)
    @CsvBindByName(column = "UUID")
    val uuid: UUID,

    @CsvBindByPosition(position = 1)
    @CsvBindByName(column = "Order_UUID")
    val order_uuid: UUID?,

    @CsvBindByPosition(position = 2)
    @CsvBindByName(column = "External_order_id")
    val external_order_id: String?,

    @CsvBindByPosition(position = 3)
    @CsvBindByName(column = "Created_at")
    @CsvDate(value = "yyyy-MM-dd-HH:mm:ss")
    var createdAt: Instant?,

    @CsvBindByPosition(position = 4)
    @CsvBindByName(column = "Transferred_amount")
    val amount: BigDecimal,

    @CsvBindByPosition(position = 5)
    @CsvBindByName(column = "Currency_unit")
    val asset: CurrencyUnit,

    @CsvBindByPosition(position = 6)
    @CsvBindByName(column = "Transferred_fiat_amount")
    val fiatAmount: BigDecimal?,

    @CsvBindByPosition(position = 7)
    @CsvBindByName(column = "Fiat_currency_unit")
    val fiatAsset: FiatCurrencyUnit?,

    @CsvBindByPosition(position = 8)
    @CsvBindByName(column = "Status")
    val status: TransactionStatus,

    @CsvBindByPosition(position = 9)
    @CsvBindByName(column = "Type")
    val transactionType: TransactionType,

    @CsvBindByPosition(position = 10)
    @CsvBindByName(column = "Sender")
    val senderName: String?,

    @CsvBindByPosition(position = 11)
    @CsvBindByName(column = "Receiver")
    val receiverName: String?,

    @CsvBindByPosition(position = 12)
    @CsvBindByName(column = "Direction")
    val direction: TransactionDirection = TransactionDirection.UNKNOWN,

    @CsvBindByPosition(position = 13)
    @CsvBindByName(column = "Description")
    val description: String? = null,

    @CsvBindByPosition(position = 14)
    @CsvBindByName(column = "Total_Amount")
    val totalAmount: BigDecimal,

    @CsvBindByPosition(position = 15)
    @CsvBindByName(column = "Fee_Percentage")
    val feePercentage: BigDecimal = BigDecimal.ZERO,

    @CsvBindByPosition(position = 16)
    @CsvBindByName(column = "Fee_Amount")
    val feeAmount: BigDecimal = BigDecimal.ZERO,

    @CsvBindByPosition(position = 17)
    @CsvBindByName(column = "Blockchain_transaction_id")
    val blockchainTxId: String? = null,
    @CsvBindByPosition(position = 18)
    @CsvBindByName(column = "Trx_ID")
    val trxId: String? = null,
    @CsvBindByPosition(position = 19)
    @CsvBindByName(column = "total_digital_currency_received")
    val totalDigitalCurrencyReceived: BigDecimal? = null,
    @CsvBindByPosition(position = 20)
    @CsvBindByName(column = "refund_status")
    val refundStatus: RefundStatus? = null,
    @CsvBindByPosition(position = 21)
    @CsvBindByName(column = "refundUserName")
    val refundUserName: String? = "",
    @CsvBindByPosition(position = 22)
    @CsvBindByName(column = "refundUserMobile")
    val refundUserMobile: String? = "",
    @CsvBindByPosition(position = 23)
    @CsvBindByName(column = "refundUserEmail")
    val refundUserEmail: String? = "",
    @CsvBindByPosition(position = 24)
    @CsvBindByName(column = "refund_fiat_type")
    val refundFiatType: FiatCurrencyUnit?,
    @CsvBindByPosition(position = 25)
    @CsvBindByName(column = "refund_wallet_address")
    val refundWalletAddress: String?,
    @CsvBindByPosition(position = 26)
    @CsvBindByName(column = "refund_digital_currency_type")
    val refundDigitalCurrencyType: CurrencyUnit?,
    val refundReason: String?,
    val refundAcceptanceComment: String?,
    val refundApprovalComment: String?,
    @CsvBindByPosition(position = 27)
    @CsvBindByName(column = "refund_fiat_amount")
    val refundFiatAmount: BigDecimal = BigDecimal.ZERO,
    @CsvBindByPosition(position = 28)
    @CsvBindByName(column = "refund_mode")
    val refundMode: RefundMode = RefundMode.NA,
    @CsvBindByPosition(position = 29)
    @CsvBindByName(column = "total_fiat_received")
    val totalFiatReceived: BigDecimal?,
    @CsvBindByPosition(position = 30)
    @CsvBindByName(column = "Refund_Amount_Digital")
    var refundAmountDigital: BigDecimal?,
    @CsvBindByPosition(position = 31)
    val sourceWalletAddress: String?,
    @CsvBindByPosition(position = 32)
    val extPosTransactionId: String? = null,
    @CsvBindByPosition(position = 33)
    val extPosLogicalDate: Instant? = null,
    @CsvBindByPosition(position = 34)
    val extPosShift: String? = null,
    @CsvBindByPosition(position = 35)
    val extPosActualDate: Date? = null,
    @CsvBindByPosition(position = 36)
    val extPosActualTime: Time? = null,
    @CsvBindByPosition(position = 37)
    val paymentReceivedDate: Instant? = null,
    @CsvBindByPosition(position = 38)
    val refundDateTime: Instant? = null,
    @CsvBindByPosition(position = 39)
    val refundSettlementDate: Instant? = null,
    @CsvBindByPosition(position = 40)
    @CsvBindByName(column = "refund_type")
    val refundType: RefundType = RefundType.NA,
    @CsvBindByPosition(position = 41)
    @CsvBindByName(column = "balance_amount_fiat")
    val balanceAmountFiat: BigDecimal = BigDecimal.ZERO,
    @CsvBindByPosition(position = 42)
    @CsvBindByName(column = "number_Of_Refunds")
    val numberOfRefunds: Int? = 0,
    var walletAddressMatch: Boolean? = null,
    var extPosLogicalDateRefund: Instant? = null,
    var extPosActualDateRefund: Date? = null,
    var extPosActualTimeRefund: Time? = null,
    var extPosSequenceNoRefund: String? = null,
    var extPosTransactionIdRefund: String? = null,
    @CsvBindByPosition(position = 43)
    @CsvBindByName(column = "UUID")
    var refundTransactionID: String? = null,
    var extPosShiftRefund: String? = null,
    var refundOrigin: String? = null,
    var refundableAmountFiat: BigDecimal? = BigDecimal.ZERO,
    var isRefundReinitiate: Boolean? = false,
    @CsvBindByPosition(position = 44)
    @CsvBindByName(column = "tx_mode")
    val txnMode: TransactionMode? = null,
    var posId: String? = null
)

fun TransactionViewModel.toPushNotificationTitle(): String? {
    if (transactionType == TransactionType.ON_RAMP) {
        return when (status) {
            TransactionStatus.NEW -> "Deposit order was recorded. Waiting for the confirmation."
            TransactionStatus.IN_PROGRESS -> "Deposit transaction was created. Waiting for the confirmation."
            TransactionStatus.SUCCESSFUL -> "Deposit was confirmed."
            TransactionStatus.FAILED -> "Deposit failed. Please, contact the support."
            TransactionStatus.UNDERPAID -> "Deposited less than Order Amount."
            TransactionStatus.OVERPAID -> "Deposited more than Order Amount."
            TransactionStatus.EXPIRED -> "Timeout/Expired."
            TransactionStatus.STARTED -> "Transaction Initiated"
            TransactionStatus.REFUNDED -> "Transaction Refunded"
        }
    }
    if (transactionType == TransactionType.EXTERNAL_RECEIVE) {
        return "External wallet"
    }
    if (transactionType == TransactionType.EXTERNAL_SEND) {
        return "External wallet"
    }
    if (transactionType == TransactionType.POS) {
        return when (status) {
            TransactionStatus.OVERPAID -> {
                "Deposited more than Order Amount"
            }

            TransactionStatus.SUCCESSFUL -> {
                "Deposited the Order Amount"
            }

            else -> {
                "Deposited less than Order Amount"
            }
        }
    }
    if (transactionType == TransactionType.WITHDRAW) {
        return "Withdraw"
    }
    if (transactionType == TransactionType.DEPOSIT) {
        return "Deposit"
    }
    if (transactionType == TransactionType.SELL) {
        return "Sell"
    }
    if (transactionType == TransactionType.FIAT_PEER_TO_PEER || transactionType == TransactionType.PAY_PEER_TO_PEER) {
        return if (direction.isIncoming()) "FiatReceive" else "FiatTransfer"
    }
    return if (direction.isIncoming()) senderName else receiverName
}

fun TransactionViewModel.toPushNotificationBody(): String {
    if (transactionType == TransactionType.EXTERNAL_RECEIVE) {
        return if (asset != CurrencyUnit.SART.toString()) {
            "Incoming transaction: + $amount $asset"
        } else {
            "Incoming transaction: + $amount SARt"
        }
    }
    if (transactionType == TransactionType.EXTERNAL_SEND) {
        return if (asset != CurrencyUnit.SART.toString()) {
            "Outgoing transaction: - $amount $asset"
        } else {
            "Outgoing transaction: - $amount SARt"
        }
    }
    if (transactionType == TransactionType.DEPOSIT) {
        return if (asset != CurrencyUnit.SART.toString()) {
            "+ $fiatAmount $fiatAsset Deposited"
        } else {
            "+ $amount SARt Deposited"
        }
    }
    if (transactionType == TransactionType.WITHDRAW) {
        return if (asset != CurrencyUnit.SART.toString()) {
            "- $fiatAmount $fiatAsset Withdrawn"
        } else {
            "- $amount SARt Withdrawn"
        }
    }
    if (transactionType == TransactionType.SELL) {
        return "Sold: - $amount $asset"
    }
    if (transactionType == TransactionType.FIAT_PEER_TO_PEER || transactionType == TransactionType.PAY_PEER_TO_PEER) {
        return if (direction.isIncoming()) "Incoming transaction: + $fiatAmount $fiatAsset"
        else "Outgoing transaction: - $fiatAmount $fiatAsset"
    }
    return "${
    if (direction.isIncoming()) "Incoming transaction: + $amount"
    else "Outgoing transaction: - $amount"
    } ${
    if (asset != CurrencyUnit.SART.toString()) asset
    else "SARt"
    }"
}
//
fun Transaction.toTransactionSettlementViewModel(
    negateAmount: Boolean = false,
    direction: TransactionDirection = TransactionDirection.UNKNOWN
):
    TransactionSettlement {
        val posTrx = this.posTransaction
        var posId: String? = null
        var orderId: Long? = null
        var merchantId: String? = null
        var posTransactionIdTemp: String? = null
        var requestedDigitalAmount: BigDecimal = BigDecimal.ZERO
        if (!posTrx.isNullOrEmpty()) {
            posId = posTrx[0].merchantPos?.posId
            orderId = posTrx[0].orderId
            posTransactionIdTemp = posTrx[posTrx.size - 1].uuid
            requestedDigitalAmount =
                posTrx.find { it.status == TransactionStatus.SUCCESSFUL || it.status == TransactionStatus.UNDERPAID || it.status == TransactionStatus.OVERPAID }?.amountcrypto
                ?: BigDecimal.ZERO
            merchantId = posTrx[0].merchant.id.toString()
        }
        return TransactionSettlement(
            this.uuid,
            this.order?.uuid,
            this.order?.externalOrderId,
            this.createdAt,
            if (negateAmount) this.amount.negate() else this.amount,
            this.asset,
            if (negateAmount) this.fiatAmount?.negate() else this.fiatAmount,
            this.fiatAsset,
            this.status,
            this.type,
            getSender(this),
            getReceiver(this),
            direction,
            this.description,
            this.fee + this.amount,
            if (this.fee.signum() == 0) BigDecimal.ZERO else this.fee.divide(this.amount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)),
            this.fee,
            this.blockchainTxId,
            this.tx_id.toString(),
            if (negateAmount) this.totalDigitalCurrencyReceived?.negate() else this.totalDigitalCurrencyReceived,
            this.totalFiatReceived,
            "$merchantId / $posId",
            orderId,
            this.paymentReceivedDate,
            this.extPosSequenceNo,
            this.extPosTransactionId, extPosId = this.extPosId,
            sequenceNumber = this.sequenceNumber,
            extPosLogicalDate = this.extPosLogicalDate,
            extPosShift = this.extPosShift,
            extPosActualDate = this.extPosActualDate,
            extPosActualTime = this.extPosActualTime,
            requestedDigitalAmount = requestedDigitalAmount,
            txnMode = this.txMode,
            refundTransactionId = this.refundTransactionId,
            totalRequestedAmount = getRequestedAmount(this),
            totalRequestedAmountAsset = getRequestedAmountAsset(this),
            totalFeeApplied = this.totalFeeApplied,
            totalFeeAppliedAsset = this.totalFeeAppliedAsset,
            senderFirstName = this.sender.account.getOwnerFirstName(),
            senderLastName = this.sender.account.getOwnerLastName(),
            receiverFirstName = this.receiver.account.getOwnerFirstName(),
            receiverLastName = this.receiver.account.getOwnerLastName(),
            issuerID = getIssuanceBanksID(this),
            senderDetails = this.sender.account.getOwnerName(),
            receiverDetails = this.receiver.account.getOwnerName()
        )
    }

data class TransactionSettlement(
    @JsonIgnore
    @CsvBindByPosition(position = 0)
    @CsvBindByName(column = "UUID")
    val uuid: UUID,

    @JsonIgnore
    @CsvBindByPosition(position = 1)
    @CsvBindByName(column = "Order_UUID")
    val order_uuid: UUID?,

    @JsonIgnore
    @CsvBindByPosition(position = 2)
    @CsvBindByName(column = "External_order_id")
    val external_order_id: String?,

    @JsonProperty("Transaction Date")
    @CsvBindByPosition(position = 3)
    @CsvBindByName(column = "Created_at")
    @CsvDate(value = "yyyy-MM-dd-HH:mm:ss")
    val createdAt: Instant,

    @JsonProperty("Order Amount")
    @CsvBindByPosition(position = 4)
    @CsvBindByName(column = "Transferred_amount")
    val amount: BigDecimal,

    @JsonProperty("Order Currency")
    @CsvBindByPosition(position = 5)
    @CsvBindByName(column = "Currency_unit")
    val asset: String,

    @JsonIgnore
    @CsvBindByPosition(position = 6)
    @CsvBindByName(column = "Transferred_fiat_amount")
    val fiatAmount: BigDecimal?,

    @JsonIgnore
    @CsvBindByPosition(position = 7)
    @CsvBindByName(column = "Fiat_currency_unit")
    val fiatAsset: FiatCurrencyUnit?,

    @JsonIgnore
    @CsvBindByPosition(position = 8)
    @CsvBindByName(column = "Status")
    val status: TransactionStatus,

    @JsonProperty("Transaction Type")
    @CsvBindByPosition(position = 9)
    @CsvBindByName(column = "Type")
    val transactionType: TransactionType,

    @JsonIgnore
    @CsvBindByPosition(position = 10)
    @CsvBindByName(column = "Sender")
    val senderName: String?,

    @JsonIgnore
    @CsvBindByPosition(position = 11)
    @CsvBindByName(column = "Receiver")
    val receiverName: String?,

    @JsonIgnore
    @CsvBindByPosition(position = 12)
    @CsvBindByName(column = "Direction")
    val direction: TransactionDirection = TransactionDirection.UNKNOWN,

    @JsonIgnore
    @CsvBindByPosition(position = 13)
    @CsvBindByName(column = "Description")
    val description: String? = null,

    @JsonIgnore
    @CsvBindByPosition(position = 14)
    @CsvBindByName(column = "Total_Amount")
    val totalAmount: BigDecimal,

    @JsonIgnore
    @CsvBindByPosition(position = 15)
    @CsvBindByName(column = "Fee_Percentage")
    val feePercentage: BigDecimal = BigDecimal.ZERO,

    @JsonIgnore
    @CsvBindByPosition(position = 16)
    @CsvBindByName(column = "Fee_Amount")
    val feeAmount: BigDecimal = BigDecimal.ZERO,

    @JsonIgnore
    @CsvBindByPosition(position = 17)
    @CsvBindByName(column = "Blockchain_transaction_id")
    val blockchainTxId: String? = null,
    @JsonIgnore
    @CsvBindByPosition(position = 18)
    @CsvBindByName(column = "Trx_ID")
    val trxId: String? = null,
    @JsonProperty("Paid Amount")
    @CsvBindByPosition(position = 19)
    @CsvBindByName(column = "total_digital_currency_received")
    val totalDigitalCurrencyReceived: BigDecimal? = null,
    @JsonIgnore
    @CsvBindByPosition(position = 20)
    val totalFiatReceived: BigDecimal? = BigDecimal.ZERO,
    @JsonProperty("Merchant ID / POS ID")
    @CsvBindByPosition(position = 21)
    var posId: String? = null,
    @JsonIgnore
    @CsvBindByPosition(position = 22)
    //  @JsonProperty("PosTransactionId")
    var orderId: Long? = null,
    @JsonIgnore
    @CsvBindByPosition(position = 23)
    var paymentReceivedDate: Instant? = null,
    @JsonIgnore
    @CsvBindByPosition(position = 24)
    val extPosSequenceNo: String? = null,
    @JsonIgnore
    @CsvBindByPosition(position = 25)
    val extPosTransactionId: String? = null,
    @JsonIgnore
    @CsvBindByPosition(position = 26)
    val conversionRate: BigDecimal? = conversionRateCalculate(amount, fiatAmount),
    @JsonProperty("Transaction ID")
    @CsvBindByPosition(position = 27)
    val transactionId: String? = uuid.toString(),
    @JsonIgnore
    @CsvBindByPosition(position = 28)
    val extPosId: String? = null,
    @JsonIgnore
    @CsvBindByPosition(position = 29)
    val sequenceNumber: String? = null,
    @JsonIgnore
    @CsvBindByPosition(position = 30)
    val extPosLogicalDate: Instant? = null,
    @JsonIgnore
    @CsvBindByPosition(position = 31)
    val extPosShift: String? = null,
    @JsonIgnore
    @CsvBindByPosition(position = 32)
    val extPosActualDate: Date? = null,
    @JsonIgnore
    @CsvBindByPosition(position = 33)
    val extPosActualTime: Time? = null,
    @JsonIgnore
    @CsvBindByPosition(position = 34)
    val refundDateTime: Instant? = null,
    @JsonIgnore
    @CsvBindByPosition(position = 35)
    val refundSettlementDate: Instant? = null,
    @JsonIgnore
    @CsvBindByPosition(position = 36)
    val requestedDigitalAmount: BigDecimal = BigDecimal.ZERO,
    @JsonProperty("Transaction Mode")
    @CsvBindByPosition(position = 37)
    val txnMode: TransactionMode? = null,
    @JsonIgnore
    @CsvBindByPosition(position = 38)
    val refundTransactionId: String? = null,
    @JsonIgnore
    @CsvBindByPosition(position = 39)
    var feeConfigData: MutableList<FeeConfigData>? = null,
    @JsonIgnore
    @CsvBindByPosition(position = 40)
    val totalRequestedAmount: BigDecimal? = null,
    @JsonProperty("Paid Currency")
    @CsvBindByPosition(position = 41)
    val totalRequestedAmountAsset: String? = null,
    @JsonIgnore
    @CsvBindByPosition(position = 42)
    val totalFeeApplied: BigDecimal? = null,
    @JsonIgnore
    @CsvBindByPosition(position = 43)
    val totalFeeAppliedAsset: String? = null,
    @JsonIgnore
    val senderFirstName: String? = null,
    @JsonIgnore
    val senderLastName: String ? = null,
    @JsonIgnore
    val receiverFirstName: String ? = null,
    @JsonIgnore
    val receiverLastName: String? = null,
    @JsonProperty("Issuer ID")
    val issuerID: String? = null,
    @JsonIgnore
    val senderDetails: String? = null,
    @JsonIgnore
    val receiverDetails: String? = null,
)
fun getIssuanceBanksID(transaction: Transaction): String? {
    if (transaction.issuanceBanks != null) {
        return transaction.issuanceBanks!!.id.toString()
    }
    return null
}
