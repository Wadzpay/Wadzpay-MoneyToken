package com.vacuumlabs.wadzpay.ledger.model

import au.com.console.jpaspecificationdsl.`in`
import au.com.console.jpaspecificationdsl.equal
import com.vacuumlabs.wadzpay.common.OptionalBigDecimalAttributeConverter
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.sql.Date
import java.sql.Time
import java.time.Instant
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OneToOne

@Entity
data class TransactionRefundDetails(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    var transaction: Transaction,

    @Column(unique = true)
    var uuid: String = UUID.randomUUID().toString(),

    @Column(nullable = true, unique = true)
    var refundBlockchainHash: String? = null,

    @Enumerated(EnumType.STRING)
    val type: TransactionType,

    @Enumerated(EnumType.STRING)
    var refundMode: RefundMode? = RefundMode.NA,
    var numberOfRefunds: Int = 0,
    var refundUserName: String? = null,
    var refundUserMobile: String? = null,
    @Enumerated(EnumType.STRING)
    var refundFiatType: FiatCurrencyUnit? = null,
    var refundWalletAddress: String? = null,
    var sourceWalletAddress: String? = null,
    var refundDateTime: Instant? = null,
    @Enumerated(EnumType.STRING)
    var refundStatus: RefundStatus? = RefundStatus.NULL,
    var refundInitiateDate: Instant? = null,
    var refundSettlementDate: Instant? = null,
    var refundInitiatedFrom: Long? = null,
    @Convert(converter = OptionalBigDecimalAttributeConverter::class)
    var refundAmountDigital: BigDecimal? = BigDecimal.ZERO,
    var refundAmountFiat: BigDecimal? = BigDecimal.ZERO,
    @Enumerated(EnumType.STRING)
    var refundDigitalCurrencyType: CurrencyUnit? = null,
    var refundReason: String? = null,
    var refundAcceptanceComment: String? = null,
    var refundApprovalComment: String? = null,
    @Enumerated(EnumType.STRING)
    var refundType: RefundType? = RefundType.NA,
    var refundUserEmail: String? = null,
    var extPosIdRefund: String? = null,
    var extPosTransactionIdRefund: String? = null,
    var extPosLogicalDateRefund: Instant? = null,
    var extPosShiftRefund: String? = null,
    var extPosActualDateRefund: Date? = null,
    var extPosSequenceNoRefund: String? = null,
    var extPosActualTimeRefund: Time? = null,
    var createdAt: Instant? = null,
    var lastUpdatedOn: Instant? = null,
    @OneToMany(mappedBy = "refundTransaction", fetch = FetchType.LAZY)
    val refundTokens: MutableList<RefundToken> = mutableListOf<RefundToken>(),
    var walletAddressMatch: Boolean? = null,
    var refundOrigin: String? = null,
    var refundableAmountFiat: BigDecimal? = BigDecimal.ZERO,
    var isRefundReinitiate: Boolean? = false
) {

    @OneToOne
    var refundToken: RefundToken? = null

    @OneToOne(mappedBy = "transaction")
    var refundTransactionFinance: WadpayRefundTransactions? = null
}

fun hasRefundStatus(refundStatus: Collection<RefundStatus>): Specification<TransactionRefundDetails> =
    TransactionRefundDetails::refundStatus.`in`(refundStatus)

fun hasRefundMode(refundMode: Collection<RefundMode>): Specification<TransactionRefundDetails> =
    TransactionRefundDetails::refundMode.`in`(refundMode)

fun hasRefundAmountFiatEqualTo(refundAmountFiat: BigDecimal): Specification<TransactionRefundDetails> =
    TransactionRefundDetails::refundAmountFiat.equal(refundAmountFiat)

fun hasRefundAmountDigitalEqualTo(refundAmountDigital: BigDecimal): Specification<TransactionRefundDetails> =
    TransactionRefundDetails::refundAmountDigital.equal(refundAmountDigital)

fun hasRefundType(refundType: Collection<RefundType>): Specification<TransactionRefundDetails> =
    TransactionRefundDetails::refundType.`in`(refundType)

@Repository
interface TransactionRefundDetailsRepository : PagingAndSortingRepository<TransactionRefundDetails, Long>, JpaSpecificationExecutor<TransactionRefundDetails> {
    @Query(
        "FROM TransactionRefundDetails trd WHERE trd.refundStatus in(com.vacuumlabs.wadzpay.ledger.model.RefundStatus.REFUND_ACCEPTED)" +
            " and trd.walletAddressMatch = true and trd.refundOrigin = 'POS' and trd.refundInitiatedFrom = :merchantId  and trd.refundInitiateDate <= :queueTime"
    )
    fun getRefundTransactionsForAutoApproval(queueTime: Instant, merchantId: Long): MutableList<TransactionRefundDetails>?

    fun findByUuid(uuid: String?): TransactionRefundDetails?
}
