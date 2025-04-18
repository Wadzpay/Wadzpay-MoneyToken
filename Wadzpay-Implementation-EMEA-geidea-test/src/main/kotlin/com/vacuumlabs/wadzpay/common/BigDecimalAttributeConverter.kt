package com.vacuumlabs.wadzpay.common

import java.math.BigDecimal
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class BigDecimalAttributeConverter : AttributeConverter<BigDecimal, BigDecimal> {
    override fun convertToDatabaseColumn(decimal: BigDecimal) = decimal.stripTrailingZeros()
    override fun convertToEntityAttribute(decimal: BigDecimal) = decimal.stripTrailingZeros()
}
