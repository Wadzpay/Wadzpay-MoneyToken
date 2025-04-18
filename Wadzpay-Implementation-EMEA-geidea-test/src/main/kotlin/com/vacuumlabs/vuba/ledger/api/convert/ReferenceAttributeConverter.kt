package com.vacuumlabs.vuba.ledger.api.convert

import com.vacuumlabs.vuba.ledger.common.Reference
import javax.persistence.AttributeConverter

class ReferenceAttributeConverter : AttributeConverter<Reference, String> {
    override fun convertToDatabaseColumn(ref: Reference) = ref.toString()
    override fun convertToEntityAttribute(string: String) = Reference(string)
}
