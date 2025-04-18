package com.vacuumlabs.vuba.ledger.api.convert

import java.math.BigDecimal
import javax.persistence.AttributeConverter

class BigDecimalAttributeConverter : AttributeConverter<BigDecimal, String> {
    override fun convertToDatabaseColumn(decimal: BigDecimal) = decimal.toString()
    override fun convertToEntityAttribute(string: String) = BigDecimal(string)
}
