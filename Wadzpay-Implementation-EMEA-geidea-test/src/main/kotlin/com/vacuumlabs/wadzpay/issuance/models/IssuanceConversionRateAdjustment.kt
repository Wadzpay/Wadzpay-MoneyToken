package com.vacuumlabs.wadzpay.issuance.models

import com.vacuumlabs.wadzpay.issuance.IssuanceCommonController
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "issuance_conversion_rate_adjustment")
data class IssuanceConversionRateAdjustment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "issuance_banks_id", nullable = false)
    val issuanceBanksId: IssuanceBanks,

    var currencyFrom: String,

    var currencyTo: String,

    @Enumerated(EnumType.STRING)
    var type: IssuanceCommonController.MarkType,

    var percentage: BigDecimal? = null,

    var validFrom: Instant,
    var validTo: Instant? = null,
    val createdAt: Instant,
    var isActive: Boolean = true,

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    val createdBy: IssuanceBanks
)
fun IssuanceConversionRateAdjustment.toViewModel(): ConversionRateAdjustmentViewModel {
    return ConversionRateAdjustmentViewModel(
        id,
        currencyFrom,
        currencyTo,
        percentage,
        validFrom,
        validTo,
        type,
        createdAt,
        isActive,
        false
    )
}

data class ConversionRateAdjustmentViewModel(
    val id: Long,
    val currencyFrom: String,
    val currencyTo: String,
    val percentage: BigDecimal?,
    val validFrom: Instant,
    val validTo: Instant?,
    val type: IssuanceCommonController.MarkType,
    val createdAt: Instant,
    val isActive: Boolean,
    var currentActive: Boolean
)
@Repository
interface IssuanceConversionRateAdjustmentRepository : CrudRepository<IssuanceConversionRateAdjustment, Long> {
    fun getByIssuanceBanksId(issuanceBanksId: IssuanceBanks): List<IssuanceConversionRateAdjustment>?
}
