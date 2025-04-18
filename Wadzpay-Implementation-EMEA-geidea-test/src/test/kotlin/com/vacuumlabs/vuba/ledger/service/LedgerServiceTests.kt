package com.vacuumlabs.vuba.ledger.service

import com.vacuumlabs.vuba.common.VubaBusinessException
import com.vacuumlabs.vuba.ledger.api.CreateCommitRequest.Condition.Relation
import com.vacuumlabs.vuba.ledger.api.dsl.commit
import com.vacuumlabs.vuba.ledger.common.DualReference
import com.vacuumlabs.vuba.ledger.common.Reference
import com.vacuumlabs.vuba.ledger.data.AccountRepository
import com.vacuumlabs.vuba.ledger.data.AssetRepository
import com.vacuumlabs.vuba.ledger.data.CommitRepository
import com.vacuumlabs.vuba.ledger.data.LedgerRepositories
import com.vacuumlabs.vuba.ledger.data.StatusEntryRepository
import com.vacuumlabs.vuba.ledger.data.StatusRepository
import com.vacuumlabs.vuba.ledger.data.StatusTypeRepository
import com.vacuumlabs.vuba.ledger.data.SubaccountEntryRepository
import com.vacuumlabs.vuba.ledger.data.SubaccountRepository
import com.vacuumlabs.vuba.ledger.model.Account
import com.vacuumlabs.vuba.ledger.model.Asset
import com.vacuumlabs.vuba.ledger.model.Commit
import com.vacuumlabs.vuba.ledger.model.Status
import com.vacuumlabs.vuba.ledger.model.StatusEntry
import com.vacuumlabs.vuba.ledger.model.StatusType
import com.vacuumlabs.vuba.ledger.model.Subaccount
import com.vacuumlabs.vuba.ledger.model.SubaccountEntry
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.dao.DataIntegrityViolationException
import java.math.BigDecimal
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class LedgerServiceTests {
    private val accountRepository = Mockito.mock(AccountRepository::class.java)
    private val assetRepository = Mockito.mock(AssetRepository::class.java)
    private val commitRepository = Mockito.mock(CommitRepository::class.java)
    private val statusEntryRepository = Mockito.mock(StatusEntryRepository::class.java)
    private val statusRepository = Mockito.mock(StatusRepository::class.java)
    private val statusTypeRepository = Mockito.mock(StatusTypeRepository::class.java)
    private val subaccountEntryRepository = Mockito.mock(SubaccountEntryRepository::class.java)
    private val subaccountRepository = Mockito.mock(SubaccountRepository::class.java)

    private val ledgerRepositories = LedgerRepositories(
        accountRepository,
        assetRepository,
        commitRepository,
        statusEntryRepository,
        statusRepository,
        statusTypeRepository,
        subaccountEntryRepository,
        subaccountRepository
    )

    private val service = LedgerService(ledgerRepositories)

    private fun <T> any(): T {
        @Suppress("UNCHECKED_CAST")
        fun <T> uninitialized(): T = null as T
        Mockito.any<T>()
        return uninitialized()
    }

    private inner class MockBuilder {
        private val accounts = mutableMapOf<String, Account>()
        private val assets = mutableMapOf<String, Asset>()
        private val commits = mutableMapOf<String, Commit>()
        private val statuses = mutableMapOf<String, Status>()
        private val statusEntries = mutableListOf<StatusEntry>()
        private val statusTypes = mutableMapOf<String, StatusType>()
        private val subaccounts = mutableMapOf<String, Subaccount>()
        private val subaccountEntries = mutableListOf<SubaccountEntry>()

        fun exists(account: Account) {
            accounts.put(account.reference, account)?.let { throw IllegalStateException() }
        }

        fun exists(asset: Asset) {
            assets.put(asset.identifier, asset)?.let { throw IllegalStateException() }
        }

        fun exists(commit: Commit) {
            commits.put(commit.reference, commit)?.let { throw IllegalStateException() }
        }

        fun exists(status: Status) {
            statuses.put(status.reference, status)?.let { throw IllegalStateException() }
        }

        fun exists(statusEntry: StatusEntry) {
            statusEntries.add(statusEntry)
        }

        fun exists(statusType: StatusType) {
            statusTypes.put(statusType.reference, statusType)?.let { throw IllegalStateException() }
        }

        fun exists(subaccount: Subaccount) {
            subaccounts.put(subaccount.reference, subaccount)?.let { throw IllegalStateException() }
        }

        fun exists(subaccountEntry: SubaccountEntry) {
            subaccountEntries.add(subaccountEntry)
        }

        fun build() {
            Mockito.`when`(accountRepository.findById(any())).thenAnswer { invocation ->
                val ref = invocation.arguments[0] as String
                Optional.ofNullable(accounts[ref])
            }

            Mockito.`when`(accountRepository.save(any())).thenAnswer { invocation ->
                val account = invocation.arguments[0] as Account
                accounts[account.reference]?.let {
                    throw DataIntegrityViolationException("")
                } ?: account
            }

            Mockito.`when`(assetRepository.findById(any())).thenAnswer { invocation ->
                val id = invocation.arguments[0] as String
                Optional.ofNullable(assets[id])
            }

            Mockito.`when`(assetRepository.save(any())).thenAnswer { invocation ->
                val asset = invocation.arguments[0] as Asset
                assets[asset.identifier]?.let {
                    throw DataIntegrityViolationException("")
                } ?: asset
            }

            Mockito.`when`(commitRepository.findById(any())).thenAnswer { invocation ->
                val ref = invocation.arguments[0] as String
                Optional.ofNullable(commits[ref])
            }

            Mockito.`when`(commitRepository.save(any())).thenAnswer { invocation ->
                val commit = invocation.arguments[0] as Commit
                commits[commit.reference]?.let {
                    throw DataIntegrityViolationException("")
                } ?: commit
            }

            Mockito.`when`(statusRepository.findById(any())).thenAnswer { invocation ->
                val ref = invocation.arguments[0] as String
                Optional.ofNullable(statuses[ref])
            }

            Mockito.`when`(statusRepository.findAllByAccountReference(any())).thenAnswer { invocation ->
                val ref = invocation.arguments[0] as String
                statuses.values.filter { it.account.reference == ref }
            }

            Mockito.`when`(statusRepository.findAllByAccountReferenceAndTypeReference(any(), any())).thenAnswer { invocation ->
                val accountRef = invocation.arguments[0] as String
                val typeRef = invocation.arguments[1] as String
                statuses.values
                    .filter { it.account.reference == accountRef }
                    .filter { it.type.reference == typeRef }
            }

            Mockito.`when`(statusRepository.save(any())).thenAnswer { invocation ->
                val status = invocation.arguments[0] as Status
                statuses[status.reference]?.let {
                    throw DataIntegrityViolationException("")
                } ?: status
            }

            Mockito.`when`(statusEntryRepository.findFirstByStatusReferenceOrderByIdDesc(any())).thenAnswer { invocation ->
                val ref = invocation.arguments[0] as String
                statusEntries.lastOrNull { it.status.reference == ref }
            }

            Mockito.`when`(statusTypeRepository.findById(any())).thenAnswer { invocation ->
                val ref = invocation.arguments[0] as String
                Optional.ofNullable(statusTypes[ref])
            }

            Mockito.`when`(statusTypeRepository.save(any())).thenAnswer { invocation ->
                val statusType = invocation.arguments[0] as StatusType
                statusTypes[statusType.reference]?.let {
                    throw DataIntegrityViolationException("")
                } ?: statusType
            }

            Mockito.`when`(subaccountRepository.findById(any())).thenAnswer { invocation ->
                val ref = invocation.arguments[0] as String
                Optional.ofNullable(subaccounts[ref])
            }

            Mockito.`when`(subaccountRepository.findAllByAccountReference(any())).thenAnswer { invocation ->
                val ref = invocation.arguments[0] as String
                subaccounts.values.filter { it.account.reference == ref }
            }

            Mockito.`when`(subaccountRepository.findAllByAccountReferenceAndAssetIdentifier(any(), any())).thenAnswer { invocation ->
                val accountRef = invocation.arguments[0] as String
                val assetId = invocation.arguments[1] as String
                subaccounts.values
                    .filter { it.account.reference == accountRef }
                    .filter { it.asset.identifier == assetId }
            }

            Mockito.`when`(subaccountRepository.save(any())).thenAnswer { invocation ->
                val subaccount = invocation.arguments[0] as Subaccount
                subaccounts[subaccount.reference]?.let {
                    throw DataIntegrityViolationException("")
                } ?: subaccount
            }

            Mockito.`when`(subaccountEntryRepository.findFirstBySubaccountReferenceOrderByIdDesc(any())).thenAnswer { invocation ->
                val ref = invocation.arguments[0] as String
                subaccountEntries.lastOrNull { it.subaccount.reference == ref }
            }
        }
    }

    private fun assume(body: MockBuilder.() -> Unit) = MockBuilder().apply(body).build()

    private companion object {
        val ACCOUNT = Reference("example.account")

        val ASSET_ID = "EUR"
        val ASSET_UNIT = "0.01".toBigDecimal()
        val ASSET = Asset(ASSET_ID.toString(), ASSET_UNIT)

        val SUBACCOUNT = DualReference(ACCOUNT, Reference("example.subaccount"))
        val SUBACCOUNT_1 = DualReference(ACCOUNT, Reference("example.subaccount.1"))
        val SUBACCOUNT_2 = DualReference(ACCOUNT, Reference("example.subaccount.2"))

        val VALUE = "value"
        val VALUE_1 = "value1"
        val VALUE_2 = "value2"
        val STATUS_TYPE = Reference("example.status_type")

        val STATUS = DualReference(ACCOUNT, Reference("example.status"))

        val COMMIT = Reference("example.commit")
        val PREV_COMMIT = Reference("previous.commit")

        fun statusType(ref: Reference, vararg values: String) = StatusType(ref.toString()).apply { this.values.addAll(values) }
    }

    @Test
    fun `get account that does not exist`() {
        // Given
        assume {}
        // When
        val result = service.getAccount(ACCOUNT).orElse(null)
        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `get account that exists`() {
        // Given
        val account = Account(ACCOUNT.toString())

        assume {
            exists(account)
        }

        // When
        val result = service.getAccount(ACCOUNT).orElse(null)

        // Then
        Assertions.assertEquals(account, result)
    }

    @Test
    fun `create account that does not exist`() {
        // Given
        assume {}

        // When
        val result = service.createAccount(ACCOUNT)

        // Then
        val account = Account(ACCOUNT.toString())

        Mockito.verify(accountRepository).save(account)
        Assertions.assertEquals(account, result)
    }

    @Test
    fun `create account that exists`() {
        // Given
        val account = Account(ACCOUNT.toString())

        assume {
            exists(account)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.AlreadyExists::class.java) {
            // When
            service.createAccount(ACCOUNT)
        }
    }

    @Test
    fun `get asset that does not exist`() {
        // Given
        assume {
            exists(ASSET)
        }

        // When
        val result = service.getAsset(ASSET_ID).orElse(null)

        // Then
        Assertions.assertEquals(ASSET, result)
    }

    @Test
    fun `get asset that exists`() {
        // Given
        assume {}
        // When
        val result = service.getAsset(ASSET_ID).orElse(null)
        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `create asset that does not exist`() {
        // Given
        assume {}

        // When
        val result = service.createAsset(ASSET_ID, ASSET_UNIT)

        // Then
        Mockito.verify(assetRepository).save(ASSET)
        Assertions.assertEquals(ASSET, result)
    }

    @Test
    fun `create asset that exists`() {
        // Given
        assume {
            exists(ASSET)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.AlreadyExists::class.java) {
            // When
            service.createAsset(ASSET_ID, ASSET_UNIT)
        }
    }

    @Test
    fun `get status type that does not exist`() {
        // Given
        assume {}
        // When
        val result = service.getStatusType(STATUS_TYPE).orElse(null)
        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `get status type that exists`() {
        // Given
        val statusType = statusType(STATUS_TYPE, VALUE)

        assume {
            exists(statusType)
        }

        // When
        val result = service.getStatusType(STATUS_TYPE).orElse(null)

        // Then
        Assertions.assertEquals(statusType, result)
    }

    @Test
    fun `create status type that does not exist`() {
        // Given
        assume {}

        // When
        val result = service.createStatusType(STATUS_TYPE, listOf(VALUE))

        // Then
        val statusType = statusType(STATUS_TYPE, VALUE)

        Mockito.verify(statusTypeRepository).save(statusType)
        Assertions.assertEquals(statusType, result)
    }

    @Test
    fun `create status type that exists`() {
        // Given
        val statusType = StatusType(STATUS_TYPE.toString())

        assume {
            exists(statusType)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.AlreadyExists::class.java) {
            // When
            service.createStatusType(STATUS_TYPE, emptyList())
        }
    }

    @Test
    fun `add value to status type that exists`() {
        // Given
        val statusType = StatusType(STATUS_TYPE.toString())

        assume {
            exists(statusType)
        }

        // When
        service.addValuesToStatusType(STATUS_TYPE, listOf(VALUE))

        // Then
        Assertions.assertTrue(statusType.values.contains(VALUE))
    }

    @Test
    fun `add value to status type that does not exist`() {
        // Given
        assume {}
        // Then
        Assertions.assertThrows(VubaBusinessException.NotFound::class.java) {
            // When
            service.addValuesToStatusType(STATUS_TYPE, listOf(VALUE))
        }
    }

    @Test
    fun `get status that does not exist`() {
        // Given
        assume {}
        // When
        val result = service.getStatus(STATUS).orElse(null)
        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `get status that exists`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val statusType = statusType(STATUS_TYPE, VALUE)
        val status = Status(STATUS.toString(), account, statusType, VALUE)

        assume {
            exists(status)
        }

        // When
        val result = service.getStatus(STATUS).orElse(null)

        // Then
        Assertions.assertEquals(status, result)
    }

    @Test
    fun `get statuses for account that does not exist`() {
        // Given
        assume {}
        // Then
        Assertions.assertThrows(VubaBusinessException.NotFound::class.java) {
            // When
            service.getStatusesForAccount(ACCOUNT)
        }
    }

    @Test
    fun `get statuses for account that exists`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val statusType = statusType(STATUS_TYPE, VALUE)
        val status = Status(STATUS.toString(), account, statusType, VALUE)

        assume {
            exists(account)
            exists(status)
        }

        // When
        val result = service.getStatusesForAccount(ACCOUNT)

        // Then
        Assertions.assertEquals(listOf(status), result)
    }

    @Test
    fun `get statuses for account that exists and status type that does not exist`() {
        // Given
        val account = Account(ACCOUNT.toString())

        assume {
            exists(account)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.NotFound::class.java) {
            // When
            service.getStatusesForAccount(ACCOUNT, STATUS_TYPE)
        }
    }

    @Test
    fun `get statuses for account and status type that both exist`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val statusType = statusType(STATUS_TYPE, VALUE)
        val status = Status(STATUS.toString(), account, statusType, VALUE)

        assume {
            exists(account)
            exists(statusType)
            exists(status)
        }

        // When
        val result = service.getStatusesForAccount(ACCOUNT, STATUS_TYPE)

        // Then
        Assertions.assertEquals(listOf(status), result)
    }

    @Test
    fun `get subaccount that does not exist`() {
        // Given
        assume {}
        // When
        val result = service.getSubaccount(SUBACCOUNT).orElse(null)
        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `get subaccount that exists`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val subaccount = Subaccount(SUBACCOUNT.toString(), account, ASSET, BigDecimal.ZERO)

        assume {
            exists(subaccount)
        }

        // When
        val result = service.getSubaccount(SUBACCOUNT).orElse(null)

        // Then
        Assertions.assertEquals(subaccount, result)
    }

    @Test
    fun `get subaccoounts for accoount that does not exist`() {
        // Given
        assume {}
        // Then
        Assertions.assertThrows(VubaBusinessException.NotFound::class.java) {
            // When
            service.getSubaccountsForAccount(ACCOUNT)
        }
    }

    @Test
    fun `get subaccounts for account that exists`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val subaccount = Subaccount(SUBACCOUNT.toString(), account, ASSET, BigDecimal.ZERO)

        assume {
            exists(account)
            exists(subaccount)
        }

        // When
        val result = service.getSubaccountsForAccount(ACCOUNT)

        // Then
        Assertions.assertEquals(listOf(subaccount), result)
    }

    @Test
    fun `get subaccounts for account that exists and asset that does not exist`() {
        // Given
        val account = Account(ACCOUNT.toString())

        assume {
            exists(account)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.NotFound::class.java) {
            // When
            service.getSubaccountsForAccount(ACCOUNT, ASSET_ID)
        }
    }

    @Test
    fun `get subaccounts for account and asset that both exist`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val subaccount = Subaccount(SUBACCOUNT.toString(), account, ASSET, BigDecimal.ZERO)

        assume {
            exists(account)
            exists(ASSET)
            exists(subaccount)
        }

        // When
        val result = service.getSubaccountsForAccount(ACCOUNT, ASSET_ID)

        // Then
        Assertions.assertEquals(listOf(subaccount), result)
    }

    @Test
    fun `get commit that does not exist`() {
        // Given
        assume {}
        // When
        val result = service.getCommit(COMMIT).orElse(null)
        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `get commit that exists`() {
        // Given
        val commit = Commit(COMMIT.toString())

        assume {
            exists(commit)
        }

        // When
        val result = service.getCommit(COMMIT).orElse(null)

        // Then
        Assertions.assertEquals(commit, result)
    }

    @Test
    fun `create commit that exists`() {
        // Given
        val commit = Commit(COMMIT.toString())

        assume {
            exists(commit)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.AlreadyExists::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with transfer between subaccounts`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val subaccount1 = Subaccount(SUBACCOUNT_1.toString(), account, ASSET, "100".toBigDecimal())
        val subaccount2 = Subaccount(SUBACCOUNT_2.toString(), account, ASSET, "100".toBigDecimal())

        assume {
            exists(subaccount1)
            exists(subaccount2)
        }

        // When
        val request = commit {
            reference(COMMIT)
            subaccountEntry(SUBACCOUNT_1, "-0.5".toBigDecimal())
            subaccountEntry(SUBACCOUNT_2, "+0.5".toBigDecimal())
        }

        service.createCommit(request)

        // Then
        val commit = Commit(COMMIT.toString())
        val subaccountEntry1 = SubaccountEntry(commit, subaccount1, "-0.5".toBigDecimal(), "99.5".toBigDecimal())
        val subaccountEntry2 = SubaccountEntry(commit, subaccount2, "+0.5".toBigDecimal(), "100.5".toBigDecimal())

        Assertions.assertEquals(subaccount1.balance, "99.5".toBigDecimal())
        Assertions.assertEquals(subaccount2.balance, "100.5".toBigDecimal())
        Mockito.verify(subaccountEntryRepository).save(subaccountEntry1)
        Mockito.verify(subaccountEntryRepository).save(subaccountEntry2)
        Mockito.verify(commitRepository).save(commit)
    }

    @Test
    fun `create commit with subaccount declaration`() {
        // Given
        val account = Account(ACCOUNT.toString())

        assume {
            exists(account)
            exists(ASSET)
        }

        // When
        val request = commit {
            reference(COMMIT)
            subaccountDeclaration(SUBACCOUNT, ASSET_ID)
            subaccountEntry(SUBACCOUNT, BigDecimal.ZERO)
        }

        service.createCommit(request)

        // Then
        val subaccount = Subaccount(SUBACCOUNT.toString(), account, ASSET, BigDecimal.ZERO)
        val commit = Commit(COMMIT.toString())
        val subaccountEntry = SubaccountEntry(commit, subaccount, BigDecimal.ZERO, BigDecimal.ZERO)

        Mockito.verify(subaccountRepository).save(subaccount)
        Mockito.verify(subaccountEntryRepository).save(subaccountEntry)
        Mockito.verify(commitRepository).save(commit)
    }

    @Test
    fun `create commit with entry on subaccount that does not exist`() {
        // Given
        assume {}

        // Then
        Assertions.assertThrows(VubaBusinessException.NotFound::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                subaccountEntry(SUBACCOUNT, BigDecimal.ZERO)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with subaccount declaration on account that does not exist`() {
        // Given
        assume {}

        // Then
        Assertions.assertThrows(VubaBusinessException.NotFound::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                subaccountDeclaration(SUBACCOUNT, ASSET_ID)
                subaccountEntry(SUBACCOUNT, BigDecimal.ZERO)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with subaccount declaration with asset that does not exist`() {
        // Given
        val account = Account(ACCOUNT.toString())

        assume {
            exists(account)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.NotFound::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                subaccountDeclaration(SUBACCOUNT, ASSET_ID)
                subaccountEntry(SUBACCOUNT, BigDecimal.ZERO)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with declaration of subaccount that exists`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val subaccount = Subaccount(SUBACCOUNT.toString(), account, ASSET, BigDecimal.ZERO)

        assume {
            exists(account)
            exists(ASSET)
            exists(subaccount)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.AlreadyExists::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                subaccountDeclaration(SUBACCOUNT, ASSET_ID)
                subaccountEntry(SUBACCOUNT, BigDecimal.ZERO)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with multiple entries on subaccount`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val subaccount = Subaccount(SUBACCOUNT.toString(), account, ASSET, BigDecimal.ZERO)

        assume {
            exists(account)
            exists(ASSET)
            exists(subaccount)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.BusinessValidation::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                subaccountEntry(SUBACCOUNT, BigDecimal.ZERO)
                subaccountEntry(SUBACCOUNT, BigDecimal.ZERO)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with subaccount declaration without initialization`() {
        // Given
        val account = Account(ACCOUNT.toString())

        assume {
            exists(account)
            exists(ASSET)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.BusinessValidation::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                subaccountDeclaration(SUBACCOUNT, ASSET_ID)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with subaccount entries of the same asset with nonzero sum`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val subaccount1 = Subaccount(SUBACCOUNT_1.toString(), account, ASSET, "100".toBigDecimal())
        val subaccount2 = Subaccount(SUBACCOUNT_2.toString(), account, ASSET, "100".toBigDecimal())

        assume {
            exists(account)
            exists(ASSET)
            exists(subaccount1)
            exists(subaccount2)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.BusinessValidation::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                subaccountEntry(SUBACCOUNT_1, "+9.95".toBigDecimal())
                subaccountEntry(SUBACCOUNT_2, "-0.05".toBigDecimal())
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with subaccount entries with amount not a multiple of the asset unit`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val subaccount1 = Subaccount(SUBACCOUNT_1.toString(), account, ASSET, BigDecimal.ZERO)
        val subaccount2 = Subaccount(SUBACCOUNT_2.toString(), account, ASSET, BigDecimal.ZERO)

        assume {
            exists(account)
            exists(ASSET)
            exists(subaccount1)
            exists(subaccount2)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.BusinessValidation::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                subaccountEntry(SUBACCOUNT_1, "-0.005".toBigDecimal())
                subaccountEntry(SUBACCOUNT_2, "+0.005".toBigDecimal())
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with value condition on subaccount that does not exist`() {
        // Given
        assume {}

        // Then
        Assertions.assertThrows(VubaBusinessException.NotFound::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                subaccountBalanceCondition(SUBACCOUNT, Relation.EQ, BigDecimal.ZERO)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with subaccount value condition that fails`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val subaccount1 = Subaccount(SUBACCOUNT_1.toString(), account, ASSET, "100".toBigDecimal())
        val subaccount2 = Subaccount(SUBACCOUNT_2.toString(), account, ASSET, "100".toBigDecimal())

        assume {
            exists(account)
            exists(subaccount1)
            exists(subaccount2)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.BusinessValidation::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                subaccountEntry(SUBACCOUNT_1, "+10".toBigDecimal())
                subaccountEntry(SUBACCOUNT_2, "-10".toBigDecimal())
                subaccountBalanceCondition(SUBACCOUNT_1, Relation.LT, "110".toBigDecimal())
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with subaccount value condition that holds`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val subaccount1 = Subaccount(SUBACCOUNT_1.toString(), account, ASSET, "100".toBigDecimal())
        val subaccount2 = Subaccount(SUBACCOUNT_2.toString(), account, ASSET, "100".toBigDecimal())

        assume {
            exists(account)
            exists(subaccount1)
            exists(subaccount2)
        }

        // When
        val request = commit {
            reference(COMMIT)
            subaccountEntry(SUBACCOUNT_1, "+10".toBigDecimal())
            subaccountEntry(SUBACCOUNT_2, "-10".toBigDecimal())
            subaccountBalanceCondition(SUBACCOUNT_1, Relation.EQ, "110".toBigDecimal())
        }

        service.createCommit(request)

        // Then
        val commit = Commit(COMMIT.toString())
        val subaccountEntry1 = SubaccountEntry(commit, subaccount1, "+10".toBigDecimal(), "110".toBigDecimal())
        val subaccountEntry2 = SubaccountEntry(commit, subaccount2, "-10".toBigDecimal(), "90".toBigDecimal())

        Assertions.assertEquals(subaccount1.balance, "110".toBigDecimal())
        Assertions.assertEquals(subaccount2.balance, "90".toBigDecimal())
        Mockito.verify(subaccountEntryRepository).save(subaccountEntry1)
        Mockito.verify(subaccountEntryRepository).save(subaccountEntry2)
        Mockito.verify(commitRepository).save(commit)
    }

    @Test
    fun `create commit with entry condition on subaccount that does not exist`() {
        // Given
        assume {}

        // Then
        Assertions.assertThrows(VubaBusinessException.NotFound::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                subaccountCommitCondition(SUBACCOUNT, PREV_COMMIT)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with subaccount entry condition with nonnull commit that fails`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val subaccount = Subaccount(SUBACCOUNT.toString(), account, ASSET, BigDecimal.ZERO)

        assume {
            exists(subaccount)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.BusinessValidation::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                subaccountCommitCondition(SUBACCOUNT, PREV_COMMIT)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with subaccount entry condition with null commit that fails`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val subaccount = Subaccount(SUBACCOUNT.toString(), account, ASSET, BigDecimal.ZERO)
        val prevCommit = Commit(PREV_COMMIT.toString())
        val subaccountEntry = SubaccountEntry(prevCommit, subaccount, BigDecimal.ZERO, BigDecimal.ZERO)

        assume {
            exists(subaccount)
            exists(subaccountEntry)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.BusinessValidation::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                subaccountCommitCondition(SUBACCOUNT)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with subaccount entry condition with nonnull commit that holds`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val subaccount = Subaccount(SUBACCOUNT.toString(), account, ASSET, BigDecimal.ZERO)
        val prevCommit = Commit(PREV_COMMIT.toString())
        val subaccountEntry = SubaccountEntry(prevCommit, subaccount, BigDecimal.ZERO, BigDecimal.ZERO)

        assume {
            exists(subaccount)
            exists(subaccountEntry)
        }

        // When
        val request = commit {
            reference(COMMIT)
            subaccountCommitCondition(SUBACCOUNT, PREV_COMMIT)
        }

        service.createCommit(request)

        // Then
        val commit = Commit(COMMIT.toString())

        Mockito.verify(commitRepository).save(commit)
    }

    @Test
    fun `create commit with subaccount entry condition with null commit that holds`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val subaccount = Subaccount(SUBACCOUNT.toString(), account, ASSET, BigDecimal.ZERO)

        assume {
            exists(subaccount)
        }

        // When
        val request = commit {
            reference(COMMIT)
            subaccountCommitCondition(SUBACCOUNT)
        }

        service.createCommit(request)

        // Then
        val commit = Commit(COMMIT.toString())

        Mockito.verify(commitRepository).save(commit)
    }

    @Test
    fun `create commit with status value update`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val statusType = statusType(STATUS_TYPE, VALUE_1, VALUE_2)
        val status = Status(STATUS.toString(), account, statusType, VALUE_1)

        assume {
            exists(status)
        }

        // When
        val request = commit {
            reference(COMMIT)
            statusEntry(STATUS, VALUE_2)
        }

        service.createCommit(request)

        // Then
        val commit = Commit(COMMIT.toString())
        val statusEntry = StatusEntry(commit, status, VALUE_2)

        Assertions.assertEquals(VALUE_2, status.value)
        Mockito.verify(statusEntryRepository).save(statusEntry)
        Mockito.verify(commitRepository).save(commit)
    }

    @Test
    fun `create commit with status declaration`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val statusType = statusType(STATUS_TYPE, VALUE_1, VALUE_2)

        assume {
            exists(account)
            exists(statusType)
        }

        // When
        val request = commit {
            reference(COMMIT)
            statusDeclaration(STATUS, STATUS_TYPE)
            statusEntry(STATUS, VALUE_1)
        }

        service.createCommit(request)

        // Then
        val status = Status(STATUS.toString(), account, statusType, VALUE_1)
        val commit = Commit(COMMIT.toString())
        val statusEntry = StatusEntry(commit, status, VALUE_1)

        Mockito.verify(statusRepository).save(status)
        Mockito.verify(statusEntryRepository).save(statusEntry)
        Mockito.verify(commitRepository).save(commit)
    }

    @Test
    fun `create commit with entry on status that does not exist`() {
        // Given
        assume {}

        // Then
        Assertions.assertThrows(VubaBusinessException.NotFound::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                statusEntry(STATUS, VALUE)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with status declaration on account that does not exist`() {
        // Given
        assume {}

        // Then
        Assertions.assertThrows(VubaBusinessException.NotFound::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                statusDeclaration(STATUS, STATUS_TYPE)
                statusEntry(STATUS, VALUE)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with status declaration with status type that does not exist`() {
        // Given
        val account = Account(ACCOUNT.toString())

        assume {
            exists(account)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.NotFound::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                statusDeclaration(STATUS, STATUS_TYPE)
                statusEntry(STATUS, VALUE)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with declaration of status that exists`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val statusType = statusType(STATUS_TYPE, VALUE)
        val status = Status(STATUS.toString(), account, statusType, VALUE)

        assume {
            exists(account)
            exists(statusType)
            exists(status)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.AlreadyExists::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                statusDeclaration(STATUS, STATUS_TYPE)
                statusEntry(STATUS, VALUE)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with multiple entries on status`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val statusType = statusType(STATUS_TYPE, VALUE)
        val status = Status(STATUS.toString(), account, statusType, VALUE)

        assume {
            exists(account)
            exists(statusType)
            exists(status)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.BusinessValidation::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                statusEntry(STATUS, VALUE)
                statusEntry(STATUS, VALUE)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with status entry with value not in status type`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val statusType = statusType(STATUS_TYPE, VALUE_1)
        val status = Status(STATUS.toString(), account, statusType, VALUE_1)

        assume {
            exists(account)
            exists(statusType)
            exists(status)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.BusinessValidation::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                statusEntry(STATUS, VALUE_2)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with status declaration without initialization`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val statusType = statusType(STATUS_TYPE, VALUE)

        assume {
            exists(account)
            exists(statusType)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.BusinessValidation::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                statusDeclaration(STATUS, STATUS_TYPE)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with value condition on status that does not exist`() {
        // Given
        assume {}

        // Then
        Assertions.assertThrows(VubaBusinessException.NotFound::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                statusValueCondition(STATUS, VALUE)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with value condition on uninitialized status`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val statusType = statusType(STATUS_TYPE, VALUE)

        assume {
            exists(account)
            exists(statusType)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.BusinessValidation::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                statusDeclaration(STATUS, STATUS_TYPE)
                statusValueCondition(STATUS, VALUE)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with status value condition with value not in status type`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val statusType = statusType(STATUS_TYPE, VALUE_1)
        val status = Status(STATUS.toString(), account, statusType, VALUE_1)

        assume {
            exists(account)
            exists(statusType)
            exists(status)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.BusinessValidation::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                statusValueCondition(STATUS, VALUE_2)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with status value condition that fails`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val statusType = statusType(STATUS_TYPE, VALUE_1, VALUE_2)
        val status = Status(STATUS.toString(), account, statusType, VALUE_1)

        assume {
            exists(account)
            exists(statusType)
            exists(status)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.BusinessValidation::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                statusEntry(STATUS, VALUE_2)
                statusValueCondition(STATUS, VALUE_1)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with status value condition on status that holds`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val statusType = statusType(STATUS_TYPE, VALUE_1, VALUE_2)
        val status = Status(STATUS.toString(), account, statusType, VALUE_1)

        assume {
            exists(account)
            exists(statusType)
            exists(status)
        }

        // When
        val request = commit {
            reference(COMMIT)
            statusEntry(STATUS, VALUE_2)
            statusValueCondition(STATUS, VALUE_2)
        }

        service.createCommit(request)

        // Then
        val commit = Commit(COMMIT.toString())
        val statusEntry = StatusEntry(commit, status, VALUE_2)

        Assertions.assertEquals(VALUE_2, status.value)
        Mockito.verify(statusEntryRepository).save(statusEntry)
        Mockito.verify(commitRepository).save(commit)
    }

    @Test
    fun `create commit with entry condition on status that does not exist`() {
        // Given
        assume {}

        // Then
        Assertions.assertThrows(VubaBusinessException.NotFound::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                statusCommitCondition(STATUS, PREV_COMMIT)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with status entry condition with nonnull commit that fails`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val statusType = statusType(STATUS_TYPE, VALUE)
        val status = Status(STATUS.toString(), account, statusType, VALUE)

        assume {
            exists(status)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.BusinessValidation::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                statusCommitCondition(STATUS, PREV_COMMIT)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with status entry condition with null commit that fails`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val statusType = statusType(STATUS_TYPE, VALUE)
        val status = Status(STATUS.toString(), account, statusType, VALUE)
        val prevCommit = Commit(PREV_COMMIT.toString())
        val statusEntry = StatusEntry(prevCommit, status, VALUE)

        assume {
            exists(status)
            exists(statusEntry)
        }

        // Then
        Assertions.assertThrows(VubaBusinessException.BusinessValidation::class.java) {
            // When
            val request = commit {
                reference(COMMIT)
                statusCommitCondition(STATUS)
            }

            service.createCommit(request)
        }
    }

    @Test
    fun `create commit with status entry condition with nonnull commit that holds`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val statusType = statusType(STATUS_TYPE, VALUE)
        val status = Status(STATUS.toString(), account, statusType, VALUE)
        val prevCommit = Commit(PREV_COMMIT.toString())
        val statusEntry = StatusEntry(prevCommit, status, VALUE)
        val commit = Commit(COMMIT.toString())

        assume {
            exists(status)
            exists(statusEntry)
        }

        // When
        val request = commit {
            reference(COMMIT)
            statusCommitCondition(STATUS, PREV_COMMIT)
        }

        service.createCommit(request)

        // Then
        Mockito.verify(commitRepository).save(commit)
    }

    @Test
    fun `create commit with status entry condition with null commit that holds`() {
        // Given
        val account = Account(ACCOUNT.toString())
        val statusType = statusType(STATUS_TYPE, VALUE)
        val status = Status(STATUS.toString(), account, statusType, VALUE)
        val commit = Commit(COMMIT.toString())

        assume {
            exists(status)
        }

        // When
        val request = commit {
            reference(COMMIT)
            statusCommitCondition(STATUS)
        }

        service.createCommit(request)

        // Then
        Mockito.verify(commitRepository).save(commit)
    }
}
