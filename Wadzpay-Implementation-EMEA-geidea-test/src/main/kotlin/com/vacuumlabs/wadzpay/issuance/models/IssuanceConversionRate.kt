package com.vacuumlabs.wadzpay.issuance.models

import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
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
@Table(name = "issuance_conversion_rate")
data class IssuanceConversionRate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "issuance_banks_id", nullable = false)
    var issuanceBanksId: IssuanceBanks,

    @Enumerated(EnumType.STRING)
    var currencyFrom: FiatCurrencyUnit,

    @Enumerated(EnumType.STRING)
    var currencyTo: CurrencyUnit,

    var baseRate: BigDecimal = BigDecimal.ZERO,
    var validFrom: Instant,
    var validTo: Instant? = null,
    val createdAt: Instant,
    var isActive: Boolean = true,

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    var createdBy: IssuanceBanks
)
fun IssuanceConversionRate.toViewModel(): ConversionRateViewModel {
    return ConversionRateViewModel(
        id,
        currencyFrom,
        currencyTo,
        baseRate,
        validFrom,
        validTo,
        createdAt,
        isActive,
        false
    )
}

data class ConversionRateViewModel(
    val id: Long,
    val currencyFrom: FiatCurrencyUnit,
    val currencyTo: CurrencyUnit,
    val baserRate: BigDecimal,
    val validFrom: Instant,
    val validTo: Instant?,
    val createdAt: Instant,
    val isActive: Boolean,
    var currentActive: Boolean
)

@Repository
interface IssuanceConversionRateRepository : CrudRepository<IssuanceConversionRate, Long> {
    fun getByIssuanceBanksId(issuanceId: IssuanceBanks): List<IssuanceConversionRate> ?
}
