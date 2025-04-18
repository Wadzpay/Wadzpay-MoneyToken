package com.vacuumlabs.vuba.ledger.service

import com.vacuumlabs.vuba.common.VubaBusinessException
import com.vacuumlabs.vuba.common.api.ValidationError
import com.vacuumlabs.vuba.ledger.api.CreateCommitRequest.Condition
import com.vacuumlabs.vuba.ledger.api.CreateCommitRequest.Declaration
import com.vacuumlabs.vuba.ledger.api.CreateCommitRequest.Entry
import com.vacuumlabs.vuba.ledger.common.Reference
import com.vacuumlabs.vuba.ledger.common.splitReference
import com.vacuumlabs.vuba.ledger.data.LedgerRepositories
import com.vacuumlabs.vuba.ledger.model.Commit
import com.vacuumlabs.vuba.ledger.model.Status
import com.vacuumlabs.vuba.ledger.model.StatusEntry
import com.vacuumlabs.vuba.ledger.model.Subaccount
import com.vacuumlabs.vuba.ledger.model.SubaccountEntry
import org.springframework.dao.DataIntegrityViolationException
import java.math.BigDecimal

class CommitContext(
    ref: Reference,
    private val repo: LedgerRepositories
) {
    private val commit = Commit(ref.toString())

    private val subaccountEnv = mutableMapOf<String, SubaccountValue>()
    private val statusEnv = mutableMapOf<String, StatusValue>()
    private val subaccountEntries = mutableListOf<SubaccountEntry>()
    private val statusEntries = mutableListOf<StatusEntry>()
    private val validationErrors = mutableListOf<ValidationError>()

    private enum class ValueStatus { CREATED, FETCHED, UPDATED }
    private data class SubaccountValue(val subaccount: Subaccount, val valueStatus: ValueStatus)
    private data class StatusValue(val status: Status, val valueStatus: ValueStatus)

    // Placeholder value for newly declared Status instances
    private val undefined = "_undefined_"

    private var evaluated = false
    fun eval(
        declarations: Iterable<Declaration>,
        entries: Iterable<Entry>,
        conditions: Iterable<Condition>
    ) = if (evaluated) {
        throw IllegalStateException("Commit context has already been evaluated")
    } else {
        evaluated = true
        declarations.forEach(this::declare)
        entries.forEach(this::record)
        conditions.forEach(this::check)
        sanityCheck()

        if (validationErrors.isNotEmpty())
            throw VubaBusinessException.BusinessValidation(validationErrors, "There were validation errors in commit ${commit.reference}")
        else
            commit()
    }

    private fun collectErrors(body: () -> Unit) {
        try {
            body()
        } catch (e: CommitValidationException) {
            validationErrors.add(e.error)
        }
    }

    private fun declare(declaration: Declaration) = when (declaration) {
        is Declaration.Subaccount -> declareSubaccount(declaration)
        is Declaration.Status -> declareStatus(declaration)
    }

    private fun declareSubaccount(declaration: Declaration.Subaccount) {
        val account = fetchAccount(declaration.reference.value.splitReference().first)
        val asset = fetchAsset(declaration.asset.toString())
        try {
            val subaccount = repo.forSubaccount.save(
                Subaccount(
                    declaration.reference.toString(),
                    account,
                    asset,
                    BigDecimal.ZERO
                )
            )
            subaccountEnv[declaration.reference.toString()] = SubaccountValue(subaccount, ValueStatus.CREATED)
        } catch (e: DataIntegrityViolationException) {
            throw VubaBusinessException.AlreadyExists("Subaccount", "reference", declaration.reference.toString())
        }
    }

    private fun declareStatus(declaration: Declaration.Status) {
        val account = fetchAccount(declaration.reference.value.splitReference().first)
        val statusType = fetchStatusType(declaration.statusType.toString())
        try {
            val status = repo.forStatus.save(
                Status(
                    declaration.reference.toString(),
                    account,
                    statusType,
                    undefined
                )
            )
            statusEnv[declaration.reference.toString()] = StatusValue(status, ValueStatus.CREATED)
        } catch (e: DataIntegrityViolationException) {
            throw VubaBusinessException.AlreadyExists("Status", "reference", declaration.reference.toString())
        }
    }

    private fun record(entry: Entry) = collectErrors {
        when (entry) {
            is Entry.Subaccount -> recordSubaccountEntry(entry)
            is Entry.Status -> recordStatusEntry(entry)
        }
    }

    private fun recordSubaccountEntry(entry: Entry.Subaccount) {
        val subaccountValue = loadSubaccountValue(entry.subaccount.toString())
        val subaccount = subaccountValue.subaccount

        if (subaccountValue.valueStatus == ValueStatus.UPDATED) {
            validationError(
                "Subaccount", "reference", subaccount.reference.toString(),
                "Multiple entries for subaccount ${subaccount.reference}"
            )
        }

        val amount = entry.amount
        val unit = subaccount.asset.unit
        val quotient: BigDecimal = amount.divideToIntegralValue(unit)
        if (amount.compareTo(quotient * unit) != 0) {
            validationError(
                "Asset", "id", subaccount.asset.toString(),
                "Invalid entry for subaccount ${subaccount.reference}: amount $amount is " +
                    "not a multiple of unit ${subaccount.asset.unit} of asset ${subaccount.asset.identifier}"
            )
        }

        val balance = subaccount.balance + amount
        subaccount.balance = balance
        subaccountEntries.add(SubaccountEntry(commit, subaccount, amount, balance))
        subaccountEnv[subaccount.reference.toString()] = SubaccountValue(subaccount, ValueStatus.UPDATED)
    }

    private fun recordStatusEntry(entry: Entry.Status) {
        val statusValue = loadStatusValue(entry.status.toString())
        val status = statusValue.status

        if (statusValue.valueStatus == ValueStatus.UPDATED) {
            validationError(
                "Status", "reference", status.reference.toString(),
                "Multiple entries for status ${status.reference}"
            )
        }

        val value = entry.value
        if (value !in status.type.values) {
            validationError(
                "Status", "reference", status.reference.toString(),
                "Invalid value for ${status.reference}: $value is not an element of ${status.type.reference}"
            )
        }

        status.value = value
        statusEntries.add(StatusEntry(commit, status, value))
        statusEnv[status.reference] = StatusValue(status, ValueStatus.UPDATED)
    }

    private fun check(condition: Condition) = collectErrors {
        when (condition) {
            is Condition.SubaccountValue -> checkSubaccountValue(condition)
            is Condition.StatusValue -> checkStatusValue(condition)
            is Condition.SubaccountEntry -> checkSubaccountEntry(condition)
            is Condition.StatusEntry -> checkStatusEntry(condition)
        }
    }

    private fun checkSubaccountValue(condition: Condition.SubaccountValue) {
        val subaccount = loadSubaccountValue(condition.subaccount.toString()).subaccount
        if (!condition.relation.eval(subaccount.balance, condition.value)) {
            validationError(
                "Subaccount", "reference", subaccount.reference.toString(),
                "Value condition on subaccount ${subaccount.reference} failed: " +
                    "expected ${condition.relation.notation} ${condition.value}, found ${subaccount.balance}"
            )
        }
    }

    private fun checkStatusValue(condition: Condition.StatusValue) {
        val statusValue = loadStatusValue(condition.status.toString())
        val status = loadStatusValue(condition.status.toString()).status

        if (statusValue.valueStatus == ValueStatus.CREATED) {
            validationError(
                "Status", "reference", status.reference,
                "Declared status ${status.reference} has not been initialized"
            )
        }
        if (condition.value !in status.type.values) {
            validationError(
                "Status", "reference", status.reference,
                "Invalid value for ${status.reference}: ${condition.value} is not an element of ${status.type.reference}"
            )
        }
        if (status.value != condition.value) {
            validationError(
                "Status", "reference", status.reference,
                "Value condition on ${status.reference} failed: expected ${condition.value}, found ${status.value}"
            )
        }
    }

    private fun checkSubaccountEntry(condition: Condition.SubaccountEntry) {
        val subaccount = loadSubaccountValue(condition.subaccount.toString()).subaccount
        val optEntry = repo.forSubaccountEntry.findFirstBySubaccountReferenceOrderByIdDesc(subaccount.reference)

        val expected = condition.commit
        val actual = optEntry?.commit?.reference
        if (expected?.toString() != actual) {
            validationError(
                "Subaccount", "reference", subaccount.reference.toString(),
                "Value condition on ${subaccount.reference} failed: expected $expected, found $actual"
            )
        }
    }

    private fun checkStatusEntry(condition: Condition.StatusEntry) {
        val status = loadStatusValue(condition.status.toString()).status
        val optEntry = repo.forStatusEntry.findFirstByStatusReferenceOrderByIdDesc(status.reference)

        val expected = condition.commit
        val actual = optEntry?.commit?.reference
        if (expected?.toString() != actual) {
            validationError(
                "Status", "reference", status.reference.toString(),
                "Value condition on ${status.reference} failed: expected $expected, found $actual"
            )
        }
    }

    private fun sanityCheck() = collectErrors {
        // Check if all declared subaccounts have been assigned an initial balance
        val badSubaccounts = subaccountEnv.filter { (_, v) -> v.valueStatus == ValueStatus.CREATED }.keys
        if (badSubaccounts.isNotEmpty()) {
            val ref = badSubaccounts.first()
            validationError(
                "Subaccount", "reference", ref.toString(),
                "Declared subaccount $ref has not been initialized"
            )
        }

        // Check if all declared statuses have been assigned an initial value
        val badStatuses = statusEnv.filter { (_, v) -> v.valueStatus == ValueStatus.CREATED }.keys
        if (badStatuses.isNotEmpty()) {
            val ref = badStatuses.first()
            validationError(
                "Status", "reference", ref.toString(),
                "Declared status $ref has not been initialized"
            )
        }

        // Check if amounts for entries of a given asset sum up to zero
        val badAssets = subaccountEntries
            .groupBy { it.subaccount.asset }
            .mapValues { (_, v) -> v.map { it.amount } }
            .mapValues { (_, v) -> v.reduce { a, b -> a.add(b) } }
            .filter { (_, v) -> v.compareTo(BigDecimal.ZERO) != 0 }
        if (badAssets.isNotEmpty()) {
            val (asset, sum) = badAssets.entries.first()
            validationError(
                "Asset", "id", asset.identifier.toString(),
                "Subaccount entries for asset ${asset.identifier} sum up to nonzero value $sum"
            )
        }
    }

    private fun commit(): Commit {
        val result = repo.forCommit.save(commit)
        subaccountEntries.forEach { repo.forSubaccountEntry.save(it) }
        statusEntries.forEach { repo.forStatusEntry.save(it) }
        return result
    }

    private fun fetchAccount(ref: Reference) = repo.forAccount.findById(ref.toString()).orElseThrow {
        VubaBusinessException.NotFound("Account", "reference", ref.toString())
    }

    private fun fetchAsset(id: String) = repo.forAsset.findById(id).orElseThrow {
        VubaBusinessException.NotFound("Asset", "id", id)
    }

    private fun fetchStatus(ref: String) = repo.forStatus.findById(ref).orElseThrow {
        VubaBusinessException.NotFound("Status", "reference", ref)
    }

    private fun fetchStatusType(ref: String) = repo.forStatusType.findById(ref).orElseThrow {
        VubaBusinessException.NotFound("StatusType", "reference", ref)
    }

    private fun fetchSubaccount(ref: String) = repo.forSubaccount.findById(ref).orElseThrow {
        VubaBusinessException.NotFound("Subaccount", "reference", ref)
    }

    private fun loadSubaccountValue(ref: String) = subaccountEnv.computeIfAbsent(ref) {
        SubaccountValue(fetchSubaccount(it), ValueStatus.FETCHED)
    }

    private fun loadStatusValue(ref: String) = statusEnv.computeIfAbsent(ref) {
        StatusValue(fetchStatus(it), ValueStatus.FETCHED)
    }

    class CommitValidationException(val error: ValidationError) : RuntimeException(error.message)

    private fun validationError(subject: String, field: String, value: String, message: String) {
        throw CommitValidationException(ValidationError(subject, field, value, message))
    }
}
