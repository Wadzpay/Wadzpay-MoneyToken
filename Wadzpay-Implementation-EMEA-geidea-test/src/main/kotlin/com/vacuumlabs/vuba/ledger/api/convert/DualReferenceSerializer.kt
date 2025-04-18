package com.vacuumlabs.vuba.ledger.api.convert

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.vacuumlabs.vuba.ledger.common.DualReference

class DualReferenceSerializer(t: Class<DualReference>?) : StdSerializer<DualReference>(t) {

    constructor() : this(null)

    override fun serialize(ref: DualReference, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(ref.toString())
    }
}
