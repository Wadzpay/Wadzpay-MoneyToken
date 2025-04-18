package com.vacuumlabs.vuba.ledger.model

import com.vacuumlabs.vuba.ledger.api.convert.BigDecimalAttributeConverter
import java.math.BigDecimal
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
data class SubaccountEntry(
    @ManyToOne
    val commit: Commit,
    @ManyToOne
    val subaccount: Subaccount,
    @Convert(converter = BigDecimalAttributeConverter::class)
    val amount: BigDecimal,
    @Convert(converter = BigDecimalAttributeConverter::class)
    val balance: BigDecimal
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id = 0L
}
