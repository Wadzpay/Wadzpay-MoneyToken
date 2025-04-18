package com.vacuumlabs.wadzpay.pos

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table
data class ConversionMarkup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(unique = true, nullable = false)
    var digitalCurrency: String? = null,
    val markUp: Int = 0,
    val markUpPercentage: BigDecimal = BigDecimal.ZERO,
    val fromDate: Instant,
    val toDate: Instant
)
data class ResponseConversionMarkup(
    val totalDigitalCurrencies: Int,
    val markupList: MutableList<ConversionMarkup>?
)
@Repository
interface ConversionMarkupRepository : CrudRepository<ConversionMarkup, Long> {
    fun getByDigitalCurrency(digitalCurrency: String?): ConversionMarkup
}
