package com.vacuumlabs.vuba.ledger.api.convert

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.vacuumlabs.vuba.ledger.common.DualReference

class DualReferenceDeserializer(vc: Class<*>?) : StdDeserializer<DualReference>(vc) {

    constructor() : this(null)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DualReference {
        val string = p.readValueAsTree<JsonNode>()?.textValue()
            ?: throw JsonParseException(p, "Cannot parse node as text")
        return DualReference(string)
    }
}
