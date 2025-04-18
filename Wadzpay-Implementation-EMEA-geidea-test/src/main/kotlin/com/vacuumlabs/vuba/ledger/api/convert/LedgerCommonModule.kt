package com.vacuumlabs.vuba.ledger.api.convert

import com.fasterxml.jackson.databind.module.SimpleModule
import com.vacuumlabs.vuba.ledger.common.DualReference
import com.vacuumlabs.vuba.ledger.common.Reference
import org.springframework.stereotype.Component

@Component
class LedgerCommonModule : SimpleModule("ledger-common") {
    init {
        addDeserializer(DualReference::class.java, DualReferenceDeserializer())
        addDeserializer(Reference::class.java, ReferenceDeserializer())
        addSerializer(DualReference::class.java, DualReferenceSerializer())
        addSerializer(Reference::class.java, ReferenceSerializer())
    }
}
