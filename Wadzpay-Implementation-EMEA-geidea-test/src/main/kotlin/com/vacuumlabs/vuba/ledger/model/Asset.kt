package com.vacuumlabs.vuba.ledger.model

import com.vacuumlabs.vuba.ledger.api.convert.BigDecimalAttributeConverter
import java.math.BigDecimal
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Asset(
    @Id
    val identifier: String,

    @Convert(converter = BigDecimalAttributeConverter::class)
    val unit: BigDecimal
)
