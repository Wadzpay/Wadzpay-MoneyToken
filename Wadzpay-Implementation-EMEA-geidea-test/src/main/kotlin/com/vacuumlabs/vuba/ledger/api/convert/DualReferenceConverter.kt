package com.vacuumlabs.vuba.ledger.api.convert

import com.vacuumlabs.vuba.ledger.common.DualReference
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class DualReferenceConverter : Converter<String, DualReference> {
    override fun convert(string: String) = DualReference(string)
}
