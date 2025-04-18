package com.vacuumlabs.vuba.ledger.api.convert

import com.vacuumlabs.vuba.ledger.common.Reference
import javax.persistence.AttributeConverter

class NullableReferenceAttributeConverter : AttributeConverter<Reference?, String?> {
    override fun convertToDatabaseColumn(ref: Reference?) = ref?.toString()
    override fun convertToEntityAttribute(string: String?) = if (string == null) null else Reference(string)
}
