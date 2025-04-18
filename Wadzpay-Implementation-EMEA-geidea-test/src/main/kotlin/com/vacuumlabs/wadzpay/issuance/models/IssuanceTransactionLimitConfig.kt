package com.vacuumlabs.wadzpay.issuance.models

import com.vacuumlabs.ROUNDING_LIMIT
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "issuance_transaction_limit_config")
data class IssuanceTransactionLimitConfig(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "issuance_banks_id", nullable = false)
    var issuanceBanksId: IssuanceBanks,

    var fiatCurrency: String? = null,

    var transactionTypeId: String,

    var frequency: String? = null,

    var count: String ? = null,

    var minValue: BigDecimal ? = null,

    var maxValue: BigDecimal ? = null,

    var userCategory: String ? = null,

    val createdDate: Instant = Instant.now(),

    var isActive: Boolean = true,

    var digitalCurrency: String? = null,

    var incrementalQuantity: String? = null,

    var quantityUnit: String? = null,

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    val createdBy: IssuanceBanks,

    var modifiedDate: Instant? = null,

    @ManyToOne
    @JoinColumn(name = "modified_by", nullable = false)
    var modifiedBy: IssuanceBanks? = null

)

fun IssuanceTransactionLimitConfig.toViewModel(): TransactionLimitConfigViewModel {
    return TransactionLimitConfigViewModel(
        id,
        fiatCurrency,
        transactionTypeId,
        "",
        frequency,
        "",
        count?.toInt(),
        if (minValue != null) minValue!!.setScale(ROUNDING_LIMIT, RoundingMode.UP).toString() else null,
        if (maxValue != null) maxValue!!.setScale(ROUNDING_LIMIT, RoundingMode.UP).toString() else null,
        userCategory,
        createdDate,
        modifiedDate,
        isActive,
        digitalCurrency,
        incrementalQuantity,
        quantityUnit
    )
}

data class TransactionLimitConfigViewModel(
    val id: Long,
    val currency: String?,
    val transactionTypeId: String,
    var transactionType: String,
    var frequency: String?,
    var frequencyStr: String?,
    val transactionCount: Int ?,
    val minValue: String ? = null,
    val maxValue: String ? = null,
    val userCategory: String?,
    val createdAt: Instant,
    var modifiedDate: Instant?,
    val isActive: Boolean,
    var digitalCurrency: String?,
    var incrementalQuantity: String?,
    var quantityUnit: String?
)
@Repository
interface IssuanceTransactionLimitConfigRepository :
    PagingAndSortingRepository<IssuanceTransactionLimitConfig, Long>,
    JpaSpecificationExecutor<IssuanceTransactionLimitConfig> {
    fun getByIssuanceBanksId(issuanceBanksId: IssuanceBanks): List<IssuanceTransactionLimitConfig> ?
    fun getByTransactionTypeIdAndIssuanceBanksId(transactionType: String, issuanceBanks: IssuanceBanks): List<IssuanceTransactionLimitConfig>?

    fun getByTransactionTypeIdAndIssuanceBanksIdAndFiatCurrency(transactionType: String, issuanceBanks: IssuanceBanks, fiatCurrency: String): List<IssuanceTransactionLimitConfig>?
}
