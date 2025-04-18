package com.vacuumlabs.vuba.ledger.api.convert

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.vacuumlabs.vuba.ledger.common.Reference

class ReferenceSerializer(t: Class<Reference>?) : StdSerializer<Reference>(t) {

    constructor() : this(null)

    override fun serialize(ref: Reference, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(ref.toString())
    }
}
