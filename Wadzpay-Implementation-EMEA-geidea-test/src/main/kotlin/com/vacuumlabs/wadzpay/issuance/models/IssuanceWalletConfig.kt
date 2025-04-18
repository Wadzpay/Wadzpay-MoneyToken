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
@Table(name = "issuance_wallet_config")
data class IssuanceWalletConfig(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "issuance_banks_id", nullable = false)
    var issuanceBanksId: IssuanceBanks,

    var fiatCurrency: String? = null,

    var walletFeeId: String,

    var frequency: String ? = null,

    var feeValue: String ? = null,

    var minValue: BigDecimal ? = null,

    var maxValue: BigDecimal ? = null,

    var userCategory: String ? = null,

    val createdDate: Instant = Instant.now(),

    var isActive: Boolean = true,

    var feeType: String ? = null,

    var digitalCurrency: String? = null,

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    val createdBy: IssuanceBanks,

    var modifiedDate: Instant? = null,

    @ManyToOne
    @JoinColumn(name = "modified_by", nullable = false)
    var modifiedBy: IssuanceBanks? = null
)
fun IssuanceWalletConfig.toViewModel(): IssuanceWalletConfigViewModel {
    return IssuanceWalletConfigViewModel(
        id,
        fiatCurrency,
        walletFeeId,
        "",
        frequency,
        "",
        feeValue,
        if (minValue != null) minValue!!.setScale(ROUNDING_LIMIT, RoundingMode.UP).toString() else null,
        if (maxValue != null) maxValue!!.setScale(ROUNDING_LIMIT, RoundingMode.UP).toString() else null,
        userCategory,
        createdDate,
        modifiedDate,
        isActive,
        feeType,
        digitalCurrency
    )
}

data class IssuanceWalletConfigViewModel(
    val id: Long,
    val fiatCurrency: String?,
    val walletFeeId: String,
    var walletFeeType: String,
    var frequency: String?,
    var frequencyStr: String?,
    val value: String ?,
    val minValue: String ? = null,
    val maxValue: String ? = null,
    val userCategory: String?,
    val createdAt: Instant,
    var modifiedDate: Instant?,
    val isActive: Boolean,
    val feeType: String?,
    val digitalCurrency: String?
)
@Repository
interface IssuanceWalletConfigRepository :
    PagingAndSortingRepository<IssuanceWalletConfig, Long>,
    JpaSpecificationExecutor<IssuanceWalletConfig> {

    fun getByIssuanceBanksId(issuanceBanksId: IssuanceBanks): List<IssuanceWalletConfig> ?
}
