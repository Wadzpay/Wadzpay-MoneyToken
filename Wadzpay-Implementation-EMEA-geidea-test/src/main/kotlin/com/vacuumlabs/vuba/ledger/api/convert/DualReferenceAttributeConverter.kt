package com.vacuumlabs.vuba.ledger.api.convert

import com.vacuumlabs.vuba.ledger.common.DualReference
import javax.persistence.AttributeConverter

class DualReferenceAttributeConverter : AttributeConverter<DualReference, String> {
    override fun convertToDatabaseColumn(ref: DualReference) = ref.toString()
    override fun convertToEntityAttribute(string: String) = DualReference(string)
}
