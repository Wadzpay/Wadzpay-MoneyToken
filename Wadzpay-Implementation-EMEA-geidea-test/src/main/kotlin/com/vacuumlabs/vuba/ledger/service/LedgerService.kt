package com.vacuumlabs.vuba.ledger.service

import com.vacuumlabs.vuba.common.VubaBusinessException
import com.vacuumlabs.vuba.ledger.api.CreateCommitRequest
import com.vacuumlabs.vuba.ledger.common.DualReference
import com.vacuumlabs.vuba.ledger.common.Reference
import com.vacuumlabs.vuba.ledger.data.LedgerRepositories
import com.vacuumlabs.vuba.ledger.model.Account
import com.vacuumlabs.vuba.ledger.model.Asset
import com.vacuumlabs.vuba.ledger.model.Status
import com.vacuumlabs.vuba.ledger.model.StatusType
import com.vacuumlabs.vuba.ledger.model.Subaccount
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.Optional
import javax.transaction.Transactional

@Service
class LedgerService(
    private val repo: LedgerRepositories
) {
    /** Create a new entity in the database, or throw a [VubaBusinessException.AlreadyExists] if it already exists.
     *  Check for existence before saving to fail-early, and avoid relying on [DataIntegrityViolationException].
     */
    private fun <T : Any, R : Any> createInternal(subject: String, field: String, refValue: R, fetch: () -> Optional<T>, save: () -> T): T {
        val logger = LoggerFactory.getLogger(javaClass)
        logger.info("INCOMMIT")
        logger.info(field)
        logger.info(refValue.toString())
        logger.info(fetch().toString())
        if (fetch().isPresent) {
            throw VubaBusinessException.AlreadyExists(subject, field, refValue.toString())
        } else {
            return try {
                save()
            } catch (e: DataIntegrityViolationException) {
                throw VubaBusinessException.AlreadyExists(subject, field, refValue.toString())
            }
        }
    }
    private fun <T : Any, R : Any> createInternal(subject: String, field: String, refValue: R, fetch: () -> Optional<T>, save: () -> T, oldUser: Boolean): T {
        val logger = LoggerFactory.getLogger(javaClass)
        logger.info("INCOMMIT2")
        logger.info(field)
        logger.info(refValue.toString())
        logger.info(fetch().toString())
        if (fetch().isPresent) {
            logger.info(fetch().get().toString())
        }
        return try {
            save()
        } catch (e: DataIntegrityViolationException) {
            throw VubaBusinessException.AlreadyExists(subject, field, refValue.toString())
        }
    }
    fun getAccount(ref: Reference) = repo.forAccount.findById(ref.toString())

    @Transactional
    fun createAccount(ref: Reference) = createInternal(
        "Account", "reference", ref.toString(),
        fetch = { getAccount(ref) },
        save = { repo.forAccount.save(Account(ref.toString())) }
    )

    fun getAsset(id: String) = repo.forAsset.findById(id)

    @Transactional
    fun createAsset(id: String, unit: BigDecimal) = createInternal(
        "Asset", "id", id.toString(),
        fetch = { getAsset(id) },
        save = { repo.forAsset.save(Asset(id.toString(), unit)) }
    )

    fun getStatusType(ref: Reference) = repo.forStatusType.findById(ref.toString())

    @Transactional
    fun createStatusType(ref: Reference, values: Iterable<String>) = createInternal(
        "StatusType", "reference", ref.toString(),
        fetch = { getStatusType(ref) },
        save = { repo.forStatusType.save(StatusType(ref.toString()).apply { this.values.addAll(values) }) }
    )

    @Transactional
    fun addValuesToStatusType(ref: Reference, values: Iterable<String>) {
        val statusType = getStatusType(ref).orElseThrow {
            VubaBusinessException.NotFound("StatusType", "reference", ref.toString())
        }
        statusType.values.addAll(values)
    }

    fun getStatus(ref: DualReference) = repo.forStatus.findById(ref.toString())

    @Transactional
    fun getStatusesForAccount(accountRef: Reference, statusTypeRef: Reference? = null): List<Status> {
        getAccount(accountRef).orElseThrow {
            VubaBusinessException.NotFound("Account", "reference", accountRef.toString())
        }
        return statusTypeRef?.let { ref ->
            getStatusType(ref).orElseThrow {
                VubaBusinessException.NotFound("StatusType", "reference", ref.toString())
            }
            repo.forStatus.findAllByAccountReferenceAndTypeReference(accountRef.toString(), ref.toString())
        } ?: repo.forStatus.findAllByAccountReference(accountRef.toString())
    }

    fun getSubaccount(ref: DualReference) = repo.forSubaccount.findById(ref.toString())

    @Transactional
    fun getSubaccountsForAccount(accountRef: Reference, assetId: String? = null): List<Subaccount> {
        getAccount(accountRef).orElseThrow {
            VubaBusinessException.NotFound("Account", "reference", accountRef.toString())
        }
        return assetId?.let { id ->
            getAsset(id).orElseThrow {
                VubaBusinessException.NotFound("Asset", "id", id.toString())
            }
            repo.forSubaccount.findAllByAccountReferenceAndAssetIdentifier(accountRef.toString(), id.toString())
        } ?: repo.forSubaccount.findAllByAccountReference(accountRef.toString())
    }

    fun getCommit(ref: Reference) = repo.forCommit.findById(ref.toString())

    @Transactional
    fun createCommit(request: CreateCommitRequest) = createInternal(
        "Commit", "reference", request.reference,
        fetch = { getCommit(request.reference) },
        save = { request.run { CommitContext(reference, repo).eval(declarations, entries, conditions) } }
    )
    @Transactional
    fun createCommit(request: CreateCommitRequest, oldUser: Boolean) = createInternal(
        "Commit", "reference", request.reference,
        fetch = { getCommit(request.reference) },
        save = { request.run { CommitContext(reference, repo).eval(declarations, entries, conditions) } }, oldUser
    )
}
