package com.vacuumlabs.vuba.ledger.api

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.vacuumlabs.vuba.ledger.common.DualReference
import com.vacuumlabs.vuba.ledger.common.Reference
import com.vacuumlabs.vuba.ledger.common.ValidIdentifier
import java.math.BigDecimal
import javax.validation.Valid

data class CreateCommitRequest(
    @field:Valid
    val reference: Reference,
    @field:Valid
    val entries: List<Entry> = emptyList(),
    @field:Valid
    val declarations: List<Declaration> = emptyList(),
    @field:Valid
    val conditions: List<Condition> = emptyList()
) {
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes(
        JsonSubTypes.Type(Entry.Subaccount::class, name = "subaccount_entry"),
        JsonSubTypes.Type(Entry.Status::class, name = "status_entry"),
    )
    sealed class Entry {
        data class Subaccount(
            @field:Valid
            val subaccount: DualReference,
            val amount: BigDecimal
        ) : Entry()

        data class Status(
            @field:Valid
            val status: DualReference,
            @field:ValidIdentifier
            val value: String
        ) : Entry()
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes(
        JsonSubTypes.Type(Declaration.Subaccount::class, name = "subaccount_declaration"),
        JsonSubTypes.Type(Declaration.Status::class, name = "status_declaration"),
    )
    sealed class Declaration {
        data class Subaccount(
            @field:Valid
            val reference: DualReference,
            @field:ValidIdentifier
            val asset: String
        ) : Declaration()

        data class Status(
            @field:Valid
            val reference: DualReference,
            @field:Valid
            val statusType: Reference
        ) : Declaration()
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes(
        JsonSubTypes.Type(Condition.SubaccountValue::class, name = "subaccount_value_condition"),
        JsonSubTypes.Type(Condition.StatusValue::class, name = "status_value_condition"),
        JsonSubTypes.Type(Condition.SubaccountEntry::class, name = "subaccount_entry_condition"),
        JsonSubTypes.Type(Condition.StatusEntry::class, name = "status_entry_condition")
    )
    sealed class Condition {
        enum class Relation(val notation: String) {
            EQ("="),
            LT("<"),
            LE("<="),
            GT(">"),
            GE(">=");

            fun eval(left: BigDecimal, right: BigDecimal): Boolean {
                val compareRes = left.compareTo(right)
                return when (this) {
                    EQ -> compareRes == 0
                    LT -> compareRes < 0
                    LE -> compareRes <= 0
                    GT -> compareRes > 0
                    GE -> compareRes >= 0
                }
            }
        }

        data class SubaccountValue(
            val relation: Relation,
            @field:Valid
            val subaccount: DualReference,
            val value: BigDecimal
        ) : Condition()

        data class StatusValue(
            @field:Valid
            val status: DualReference,
            @field:ValidIdentifier
            val value: String
        ) : Condition()

        data class SubaccountEntry(
            @field:Valid
            val subaccount: DualReference,
            @field:Valid
            val commit: Reference?
        ) : Condition()

        data class StatusEntry(
            @field:Valid
            val status: DualReference,
            @field:Valid
            val commit: Reference?
        ) : Condition()
    }
}
