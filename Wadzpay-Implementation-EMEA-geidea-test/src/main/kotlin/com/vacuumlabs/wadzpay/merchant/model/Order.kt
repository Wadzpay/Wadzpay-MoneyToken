package com.vacuumlabs.wadzpay.merchant.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.wadzpay.common.BigDecimalAttributeConverter
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.OptionalBigDecimalAttributeConverter
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.AccountOwner
import com.vacuumlabs.wadzpay.ledger.model.Transaction
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.model.TransactionValidator
import com.vacuumlabs.wadzpay.pos.PosTransaction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Table
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

// const val ORDER_EXPIRATION_MINS = 5L

enum class OrderStatus {
    OPEN,
    PROCESSED,
    EXPIRED,
    CANCELLED,
    FAILED,
    SUCCESSFUL,
    IN_PROGRESS,
    NEW,
    UNDERPAID,
    OVERPAID
}

enum class FiatCurrencyUnit(val sign: String, val fullName: String) {
    // TODO sufficient for pilot but using some complete list would be better in the future
    USD("$", "United States dollar (USD)"),
    EUR("€", "Euro (EUR)"),
    INR("₹", "Indian rupee (INR)"),
    IRT("₹t", "Indian rupee token (IRT)"),
    IDR("Rp", "Indonesian rupiah (IDR)"),
    GBP("£", "Pound sterling (GBP)"),
    SGD("S$", "Singapore dollar (SGD)"),
    PKR("₨", "Pakistani rupee (PKR)"),
    PHP("₱", "Philippine peso (PHP)"),
    AED("AED", "UAE dirham (AED)"),
    SAR("SR", "Saudi Riyal (SAR)"),
    THB("฿", "Thai baht (THB)"),
    VND("₫", "Vietnamese Dong (VND)"),
    BHD(".د.ب", "Bahrain Dinar (BHD)"),
    MYR("RM", "Malaysia (MYR)"),
    QAR("QR", "Qatari Riyal (QAR)")
}

enum class OrderType { ORDER, WITHDRAWAL }

@Entity
@Table(name = "wadzpay_order")
@AmountValidation
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    val id: Long = 0,

    @Column(unique = true)
    val uuid: UUID = UUID.randomUUID(),

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Convert(converter = BigDecimalAttributeConverter::class)
    // TODO add positive check
    val amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    val currency: CurrencyUnit,

    @Enumerated(EnumType.STRING)
    val type: OrderType,

    /**
     * Equivalent of amount in FIAT promised to customer.
     *
     * Exchanges rates are up to merchants since we have no control over their systems.
     * */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Convert(converter = OptionalBigDecimalAttributeConverter::class)
    val fiatAmount: BigDecimal? = null,

    @Enumerated(EnumType.STRING)
    val fiatCurrency: FiatCurrencyUnit? = null,

    @JsonIgnore
    @ManyToOne
    var source: AccountOwner? = null,

    @ManyToOne
    @JsonIgnore
    var target: AccountOwner? = null,

    val targetEmail: String? = null,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),

    val externalOrderId: String? = null,

    @Column(length = 160)
    val description: String? = null,

    var isCancelled: Boolean = false,
    var isFailed: Boolean = false,

    var isThirdParty: Boolean = false,

    var walletAddress: String? = null,

    @Enumerated(EnumType.STRING)
    var orderStatus: TransactionStatus? = null,
    val requesterUserName: String? = null,
    val requesterEmailAddress: String? = null,
    val requesterMobileNumber: String? = null
) {
    @JsonIgnore
    @OneToOne
    var transaction: Transaction? = null

    @JsonIgnore
    @OneToMany(mappedBy = "order")
    var posTransaction: MutableList<PosTransaction>? = null

    /**
     * Determines Order status.
     *
     * We don't store status explicitly since expiration is dependant on time.
     */
    val status: OrderStatus
        get() {
            val diff: Duration = Duration.between(this.createdAt, Instant.now())
            if (diff > Duration.ofMinutes((this.target as Merchant).orderExpTimeInMin) && !(this.transaction?.status != TransactionStatus.OVERPAID || this.transaction?.status != TransactionStatus.UNDERPAID || this.transaction?.status != TransactionStatus.SUCCESSFUL)) {
                this.transaction?.status = TransactionStatus.EXPIRED
                return OrderStatus.EXPIRED
            }
            if (this.transaction != null) {
                return OrderStatus.valueOf(this.transaction?.status?.name.toString())
            }
            if (this.isCancelled) {
                return OrderStatus.CANCELLED
            }
            if (this.isFailed) {
                return OrderStatus.FAILED
            }
            if (diff > Duration.ofMinutes((this.target as Merchant).orderExpTimeInMin)) {
                return OrderStatus.EXPIRED
            }
            return OrderStatus.OPEN
        }
}

class OrderValidator : ConstraintValidator<AmountValidation, Order> {
    override fun isValid(value: Order?, context: ConstraintValidatorContext?): Boolean {
        if (value != null) {
            return value.currency.validAmount(value.amount)
        }
        return true
    }
}

@Constraint(validatedBy = [OrderValidator::class, TransactionValidator::class])
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AmountValidation(
    val message: String = ErrorCodes.INVALID_AMOUNT_TOO_MANY_DECIMAL_PLACES,
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Any>> = []
)

@Repository
interface OrderRepository : CrudRepository<Order, Long> {
    fun getByUuid(uuid: UUID): Order?
    fun getByWalletAddress(address: String): Order?
}
