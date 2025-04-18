package com.vacuumlabs.vuba.ledger.api.dsl

import com.vacuumlabs.vuba.ledger.api.CreateCommitRequest
import com.vacuumlabs.vuba.ledger.api.CreateCommitRequest.Condition.Relation
import com.vacuumlabs.vuba.ledger.common.DualReference
import com.vacuumlabs.vuba.ledger.common.Reference
import java.math.BigDecimal

@LedgerApiDsl
class CommitBuilder {
    private var reference: Reference? = null

    private val declarations = mutableListOf<CreateCommitRequest.Declaration>()
    private val entries = mutableListOf<CreateCommitRequest.Entry>()
    private val conditions = mutableListOf<CreateCommitRequest.Condition>()

    fun reference(value: Reference) {
        this.reference = value
    }

    fun reference(value: String) {
        this.reference = Reference(value)
    }

    // Declarations
    fun statusDeclaration(body: StatusDeclarationBuilder.() -> Unit) =
        StatusDeclarationBuilder().apply(body).build().also { declarations.add(it) }

    fun statusDeclaration(reference: DualReference, statusType: Reference) = statusDeclaration {
        this.reference = reference
        this.statusType = statusType
    }

    fun statusDeclaration(reference: String, statusType: String) = statusDeclaration {
        this.reference = DualReference(reference)
        this.statusType = Reference(statusType)
    }

    fun subaccountDeclaration(zero: Boolean = false, body: SubaccountDeclarationBuilder.() -> Unit) =
        SubaccountDeclarationBuilder().apply(body).build()
            .also { declarations.add(it) }
            .also { if (zero) subaccountEntry(it.reference, BigDecimal.ZERO) }

    fun subaccountDeclaration(reference: DualReference, asset: String, zero: Boolean = false) = subaccountDeclaration(zero) {
        this.reference = reference
        this.asset = asset
    }

    fun subaccountDeclaration(reference: String, asset: String, zero: Boolean = false) = subaccountDeclaration(zero) {
        this.reference = DualReference(reference)
        this.asset = asset
    }

    // Change entries
    fun statusEntry(body: StatusEntryBuilder.() -> Unit) =
        StatusEntryBuilder().apply(body).build().also { entries.add(it) }

    fun statusEntry(subaccount: DualReference, value: String) = statusEntry {
        this.subaccount = subaccount
        this.value = value
    }

    fun statusEntry(subaccount: String, value: String) = statusEntry {
        this.subaccount = DualReference(subaccount)
        this.value = value
    }

    fun subaccountEntry(body: SubaccountEntryBuilder.() -> Unit) =
        SubaccountEntryBuilder().apply(body).build().also { entries.add(it) }

    fun subaccountEntry(subaccount: String, amount: String) = subaccountEntry {
        this.subaccount = DualReference(subaccount)
        this.amount = BigDecimal(amount)
    }

    fun subaccountEntry(subaccount: DualReference, amount: BigDecimal) = subaccountEntry {
        this.subaccount = subaccount
        this.amount = amount
    }

    // Conditions
    fun subaccountBalanceCondition(subaccount: DualReference, op: Relation, value: BigDecimal) =
        CreateCommitRequest.Condition.SubaccountValue(op, subaccount, value).also { conditions.add(it) }

    fun subaccountBalanceCondition(subaccount: String, op: Relation, value: BigDecimal) =
        subaccountBalanceCondition(DualReference(subaccount), op, value)

    fun statusValueCondition(status: DualReference, value: String) =
        CreateCommitRequest.Condition.StatusValue(status, value).also { conditions.add(it) }

    fun statusValueCondition(status: String, value: String) =
        statusValueCondition(DualReference(status), value)

    fun subaccountCommitCondition(subaccount: DualReference, commit: Reference? = null) =
        CreateCommitRequest.Condition.SubaccountEntry(subaccount, commit).also { conditions.add(it) }

    fun subaccountCommitCondition(subaccount: String, commit: String) =
        subaccountCommitCondition(DualReference(subaccount), Reference(commit))

    fun statusCommitCondition(status: DualReference, commit: Reference? = null) =
        CreateCommitRequest.Condition.StatusEntry(status, commit).also { conditions.add(it) }

    fun statusCommitCondition(status: String, commit: String) =
        statusCommitCondition(DualReference(status), Reference(commit))

    // Easy-read shorthands
    fun dualSubaccountEntry(from: String, to: String, amount: String) {
        dualSubaccountEntry(
            from = DualReference(from),
            to = DualReference(to),
            amount = BigDecimal(amount)
        )
    }
    fun dualSubaccountEntry(from: DualReference, to: DualReference, amount: BigDecimal) {
        subaccountEntry(from, amount.negate())
        subaccountEntry(to, amount)
    }

    fun balanceEquals(subaccount: String, value: BigDecimal) = subaccountBalanceCondition(subaccount, Relation.EQ, value)
    fun balanceZero(subaccount: String) = balanceEquals(subaccount, 0.toBigDecimal())
    fun balanceGreater(subaccount: String, value: BigDecimal) = subaccountBalanceCondition(subaccount, Relation.GT, value)
    fun balanceGreaterEq(subaccount: String, value: BigDecimal) = subaccountBalanceCondition(subaccount, Relation.GE, value)
    fun balanceLess(subaccount: String, value: BigDecimal) = subaccountBalanceCondition(subaccount, Relation.LT, value)
    fun balanceLessEq(subaccount: String, value: BigDecimal) = subaccountBalanceCondition(subaccount, Relation.LE, value)
    fun statusEquals(status: String, value: String) = statusValueCondition(status, value)

    fun subaccountLastCommit(subaccount: String, commit: String) = subaccountCommitCondition(subaccount, commit)
    fun statusLastCommit(status: String, commit: String) = statusCommitCondition(status, commit)

    fun build() = CreateCommitRequest(
        reference ?: throw IllegalArgumentException(),
        entries.toList(),
        declarations.toList(),
        conditions.toList()
    )
}

@LedgerApiDsl
class SubaccountEntryBuilder {
    var subaccount: DualReference? = null
    var amount: BigDecimal? = null

    fun build() = CreateCommitRequest.Entry.Subaccount(
        subaccount ?: throw IllegalArgumentException(),
        amount ?: throw IllegalArgumentException()
    )
}

@LedgerApiDsl
class StatusEntryBuilder {
    var subaccount: DualReference? = null
    var value: String? = null

    fun build() = CreateCommitRequest.Entry.Status(
        subaccount ?: throw IllegalArgumentException(),
        value ?: throw IllegalArgumentException()
    )
}

@LedgerApiDsl
class SubaccountDeclarationBuilder {
    var reference: DualReference? = null
    var asset: String? = null

    fun build() = CreateCommitRequest.Declaration.Subaccount(
        reference ?: throw IllegalArgumentException(),
        asset ?: throw IllegalArgumentException()
    )
}

@LedgerApiDsl
class StatusDeclarationBuilder {
    var reference: DualReference? = null
    var statusType: Reference? = null

    fun build() = CreateCommitRequest.Declaration.Status(
        reference ?: throw IllegalArgumentException(),
        statusType ?: throw IllegalArgumentException()
    )
}

fun commit(body: CommitBuilder.() -> Unit) = CommitBuilder().apply(body).build()
