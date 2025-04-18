package com.vacuumlabs.wadzpay.ledger.model

import au.com.console.jpaspecificationdsl.`in`
import au.com.console.jpaspecificationdsl.equal
import au.com.console.jpaspecificationdsl.get
import au.com.console.jpaspecificationdsl.greaterThanOrEqualTo
import au.com.console.jpaspecificationdsl.join
import au.com.console.jpaspecificationdsl.lessThanOrEqualTo
import au.com.console.jpaspecificationdsl.or
import au.com.console.jpaspecificationdsl.where
import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.vuba.ledger.api.convert.NullableReferenceAttributeConverter
import com.vacuumlabs.vuba.ledger.common.Reference
import com.vacuumlabs.wadzpay.common.BigDecimalAttributeConverter
import com.vacuumlabs.wadzpay.common.OptionalBigDecimalAttributeConverter
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.AmountValidation
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.Merchant
import com.vacuumlabs.wadzpay.merchant.model.Order
import com.vacuumlabs.wadzpay.pos.PosTransaction
import com.vacuumlabs.wadzpay.user.UserAccount
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
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Join
import javax.persistence.criteria.Root
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import kotlin.reflect.KProperty1

enum class TransactionDirection {
    INCOMING,
    OUTGOING,
    POS,
    UNKNOWN;

    fun isIncoming(): Boolean {
        return this == INCOMING
    }
}

enum class TransactionStatus {
    SUCCESSFUL,
    FAILED,
    IN_PROGRESS,
    NEW,
    UNDERPAID,
    OVERPAID,
    EXPIRED,
    STARTED,
    REFUNDED
}

enum class TransactionType {
    ON_RAMP,
    OFF_RAMP,
    MERCHANT,
    PEER_TO_PEER,
    EXTERNAL_SEND,
    EXTERNAL_RECEIVE,
    OTHER,
    POS,
    ORDER,
    REFUND,
    BUY,
    SELL,
    SWAP,
    DEPOSIT,
    WITHDRAW,
    FIAT_PEER_TO_PEER,
    PAY_PEER_TO_PEER,
    SYNC_BLOCKCHAIN,
    SERVICE_FEE,
    WALLET_FEE
}

enum class TransactionMode {
    MERCHANT_ONLINE,
    MERCHANT_OFFLINE,
    CUSTOMER_MERCHANT_ONLINE,
    CUSTOMER_OFFLINE
}

enum class SortableTransactionFields(val value: String) {
    CREATED_AT("createdAt"),
    AMOUNT("amount"),
    STATUS("status"),
    TYPE("type"),
    ASSET("asset"),
    EXTPOSTRANSACTIONID("extPosTransactionId"),
    TOTALDIGITALCURRENCYRECEIVED("totalDigitalCurrencyReceived"),
    TOTALFIATRECEIVED("totalFiatReceived"),
    REFUNDAMOUNTFIAT("refundAmountFiat"),
    REFUNDAMOUNTDIGITAL("refundAmountDigital"),
    PAYMENTRECEIVEDDATE("paymentReceivedDate");
}

@Entity
@AmountValidation
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true)
    val uuid: UUID = UUID.randomUUID(),

    @Column
    @Convert(converter = NullableReferenceAttributeConverter::class)
    /*can be null if transaction is in progress and there is no commit yet associated with it*/
    var reference: Reference? = null,

    @ManyToOne
    var sender: Subaccount,

    @ManyToOne
    val receiver: Subaccount,

    val asset: String,

    @Convert(converter = BigDecimalAttributeConverter::class)
    var amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    var fiatAsset: FiatCurrencyUnit? = null,

    @Convert(converter = OptionalBigDecimalAttributeConverter::class)
    var fiatAmount: BigDecimal? = null,

    @Enumerated(EnumType.STRING)
    var status: TransactionStatus,

    @Enumerated(EnumType.STRING)
    val type: TransactionType,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(nullable = true, unique = true)
    var blockchainTxId: String? = null,

    @Column(nullable = true, unique = true)
    val externalId: String? = null,

    @Column(nullable = true, length = 160)
    val description: String? = null,

    @Column(unique = true, length = 160)
    var tx_id: String? = null,

    @Convert(converter = BigDecimalAttributeConverter::class)
    var fee: BigDecimal = BigDecimal.ZERO,

    @OneToMany(mappedBy = "transaction")
    var posTransaction: MutableList<PosTransaction>? = null,

    @Convert(converter = OptionalBigDecimalAttributeConverter::class)
    var totalDigitalCurrencyReceived: BigDecimal? = null,

    @Convert(converter = OptionalBigDecimalAttributeConverter::class)
    var totalFiatReceived: BigDecimal? = null,

    var sourceWalletAddress: String? = null,
    var paymentReceivedDate: Instant? = null,
    var extPosId: String? = null,
    var extPosSequenceNo: String? = null,
    var extPosTransactionId: String? = null,
    var sequenceNumber: String? = null,
    var extPosLogicalDate: Instant? = null,
    var extPosShift: String? = null,
    var extPosActualDate: Date? = null,
    var extPosActualTime: Time? = null,
    var totalRefundedAmountFiat: BigDecimal = BigDecimal.ZERO,
    @Enumerated(EnumType.STRING)
    var txMode: TransactionMode? = null,

    @JsonIgnore
    @OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY)
    var refundTransactions: List<TransactionRefundDetails>? = listOf(),

    @JsonIgnore
    @ManyToOne
    var issuanceBanks: IssuanceBanks? = null,
    var refundTransactionId: String? = null,

    @JsonIgnore
    var passcodeHash: String? = null,

    var totalRequestedAmount: BigDecimal? = null,
    var totalRequestedAmountAsset: String? = null,
    var totalFeeApplied: BigDecimal? = null,
    var totalFeeAppliedAsset: String? = null,
) {
    @JsonIgnore
    @OneToOne(mappedBy = "transaction")
    var order: Order? = null
    fun setTxId(prefix: String = "WP", id: Long = 0): Transaction {
        tx_id = "$prefix-$id"
        return this
    }
}

enum class RefundMode {
    WALLET, CASH, NA
}

enum class RefundStatus {
    REFUND_INITIATED, REFUND_FAILED, REFUND_CANCELED, REFUND_EXPIRED, REFUND_ACCEPTED, REFUNDED, REFUND_APPROVED, NULL, REFUND_HOLD
}

enum class RefundAcceptRejectStatus {
    ACCEPT, REJECT, HOLD
}

enum class RefundType {
    FULL, PARTIAL, NA
}

enum class RefundAcceptRejectType {
    ACCEPTANCE, APPROVAL, HOLD
}

enum class RefundOrigin(val value: String) {
    POS("POS"), MERCHANT_DASHBOARD("Merchant Dashboard")
}

class TransactionValidator : ConstraintValidator<AmountValidation, Transaction> {
    override fun isValid(value: Transaction?, context: ConstraintValidatorContext?): Boolean {
        try {
            if (value != null) {
                return CurrencyUnit.valueOf(value.asset).validAmount(value.amount) && value.amount > BigDecimal.ZERO
            }
        } catch (ex: IllegalArgumentException) {
            return true
        }
        return true
    }
}

fun Transaction.belongsTo(owner: AccountOwner): Boolean {
    return this.sender.account.owner == owner || this.receiver.account.owner == owner
}

fun Transaction.getDirection(owner: AccountOwner): TransactionDirection {
    return if (owner == this.sender.account.owner) TransactionDirection.OUTGOING else
        TransactionDirection.INCOMING
}

fun ownerNamesContainPattern(pattern: String): Specification<Transaction> =
    or(
        accountOwnerFieldContainsPattern(pattern, Merchant::name, Merchant::class.java),
        accountOwnerFieldContainsPattern(pattern, UserAccount::email, UserAccount::class.java)
    )

fun <T : AccountOwner> accountOwnerFieldContainsPattern(
    pattern: String,
    property: KProperty1<T, String?>,
    className: Class<T>
): Specification<Transaction> =
    where {
        val sender = joinToAccountOwner(root = it, criteriaBuilder = this, className, Transaction::sender)
        val receiver = joinToAccountOwner(root = it, criteriaBuilder = this, className, Transaction::receiver)
        or(
            like(sender.get(property), "$pattern%"),
            like(receiver.get(property), "$pattern%"),
        )
    }

fun <T : AccountOwner> joinToAccountOwner(
    root: Root<Transaction>,
    criteriaBuilder: CriteriaBuilder,
    className: Class<T>,
    attribute: KProperty1<Transaction, Subaccount>
): Join<Account, T> {
    val accountOwner = root.join(attribute).join(Subaccount::account).join(Account::owner)
    return criteriaBuilder.treat(accountOwner, className) // this is necessary to downcast to the inherited entity
}

fun hasStatus(statuses: Collection<TransactionStatus>): Specification<Transaction> =
    Transaction::status.`in`(statuses)

fun hasType(types: Collection<TransactionType>): Specification<Transaction> =
    Transaction::type.`in`(types)

fun hasAsset(assets: Collection<String>): Specification<Transaction> =
    Transaction::asset.`in`(assets)
fun hasOwner(owner: AccountOwner): Specification<Transaction> =
    or(Transaction::sender.`in`(owner.account.subaccounts), Transaction::receiver.`in`(owner.account.subaccounts))

fun hasTransactionMode(transactionMode: Collection<TransactionMode>): Specification<Transaction> =
    Transaction::txMode.`in`(transactionMode)

fun hasDirection(direction: TransactionDirection, owner: AccountOwner): Specification<Transaction> =
    when (direction) {
        TransactionDirection.INCOMING -> Transaction::receiver.`in`(owner.account.subaccounts)
        TransactionDirection.OUTGOING -> Transaction::sender.`in`(owner.account.subaccounts)
        TransactionDirection.POS -> Transaction::receiver.`in`(owner.account.subaccounts)
        TransactionDirection.UNKNOWN -> Specification.where(null)
    }

fun hasAmountGreaterOrEqualTo(amount: BigDecimal): Specification<Transaction> =
    Transaction::amount.greaterThanOrEqualTo(amount)

fun hasAmountLessOrEqualTo(amount: BigDecimal): Specification<Transaction> =
    Transaction::amount.lessThanOrEqualTo(amount)

fun hasAmountEqualTo(amount: BigDecimal): Specification<Transaction> =
    Transaction::amount.equal(amount)

fun hasFiatAmountEqualTo(fiatAmount: BigDecimal): Specification<Transaction> =
    Transaction::fiatAmount.equal(fiatAmount)

fun hasTransactionIdEqualTo(tx_id: String): Specification<Transaction> =
    Transaction::tx_id.equal(tx_id)

fun hasUUIDEqualTo(uuid: UUID): Specification<Transaction> =
    Transaction::uuid.equal(uuid)

fun hasDateGreaterOrEqualTo(date: Instant): Specification<Transaction> =
    Transaction::createdAt.greaterThanOrEqualTo(date)

fun hasDateLessOrEqualTo(date: Instant): Specification<Transaction> =
    Transaction::createdAt.lessThanOrEqualTo(date)

fun belongsToAccount(account: AccountOwner): Specification<Transaction> =
    or(Transaction::sender.`in`(account.account.subaccounts), Transaction::receiver.`in`(account.account.subaccounts))

fun hasTotalDigitalCurrencyReceivedEqualTo(totalDigitalCurrencyReceived: BigDecimal): Specification<Transaction> =
    Transaction::totalDigitalCurrencyReceived.equal(totalDigitalCurrencyReceived)

fun hasTotalFiatReceivedEqualTo(totalFiatReceived: BigDecimal): Specification<Transaction> =
    Transaction::totalFiatReceived.equal(totalFiatReceived)

fun hasExtPosIdEqualTo(extPosId: String): Specification<Transaction> =
    Transaction::extPosId.equal(extPosId)

fun hasExtPosSequenceNumberEqualTo(extPosSequenceNo: String): Specification<Transaction> =
    Transaction::extPosSequenceNo.equal(extPosSequenceNo)

fun hasExtPosTransactionIdEqualTo(extPosTransactionId: String): Specification<Transaction> =
    Transaction::extPosTransactionId.equal(extPosTransactionId)

fun hasLogicalDateGreaterOrEqualTo(date: Instant): Specification<Transaction> =
    Transaction::extPosLogicalDate.greaterThanOrEqualTo(date)

fun hasLogicalDateLessOrEqualTo(date: Instant): Specification<Transaction> =
    Transaction::extPosLogicalDate.lessThanOrEqualTo(date)

/*fun hasRefundStatus(refundStatus: Collection<RefundStatus>): Specification<Transaction> {
    return Specification<Transaction> {
        transaction, _cq, cb: CriteriaBuilder -> cb.`in`(transaction
            .join<Transaction, TransactionRefundDetails>("refundTransactions")
            .get <Any>("refundStatus")).value(refundStatus)
    }
}*/

@Repository
interface TransactionRepository : PagingAndSortingRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    fun getByBlockchainTxId(blockchainTxId: String): Transaction?
    fun getByExternalId(externalId: String?): Transaction?
    fun getByUuid(uuid: UUID): Transaction?
    fun flush()
    fun getByFiatAmount(fiatAmount: BigDecimal?): MutableList<Transaction>?
    fun getByAmount(amount: BigDecimal?): MutableList<Transaction>?

    fun getByExtPosId(extPosId: String?): MutableList<Transaction>?

    fun getByExtPosSequenceNo(extPosSequenceNo: String?): MutableList<Transaction>?

    @Query("SELECT * FROM Transaction WHERE cast(uuid as VARCHAR) LIKE %:uuid%", nativeQuery = true)
    fun findByUUID(uuid: String): MutableList<Transaction>?
    fun getById(id: Long): Transaction?

    @Query("FROM Transaction WHERE extPosTransactionId LIKE %:extPosTransactionId%")
    fun getByExtPosTransactionId(extPosTransactionId: String?): MutableList<Transaction>?

    fun getByIssuanceBanks(issuanceBanks: IssuanceBanks?): MutableList<Transaction>?
    @Query("FROM Transaction WHERE issuanceBanks  = :issuanceBanks AND createdAt between cast(:dateFrom AS date ) AND cast(:dateTo AS date)")
    fun getByIssuanceBanksDateRange(issuanceBanks: IssuanceBanks, dateFrom: Instant, dateTo: Instant): MutableList<Transaction>?

    fun getByPasscodeHash(passcodeHash: String?): MutableList<Transaction>?

    fun getBySenderAndStatusAndType(subaccount: Subaccount, status: TransactionStatus, type: TransactionType): MutableList<Transaction>
}
