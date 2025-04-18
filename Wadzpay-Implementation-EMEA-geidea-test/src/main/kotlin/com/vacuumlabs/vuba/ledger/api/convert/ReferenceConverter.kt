package com.vacuumlabs.vuba.ledger.api.convert

import com.vacuumlabs.vuba.ledger.common.Reference
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class ReferenceConverter : Converter<String, Reference> {
    override fun convert(string: String) = Reference(string)
}
