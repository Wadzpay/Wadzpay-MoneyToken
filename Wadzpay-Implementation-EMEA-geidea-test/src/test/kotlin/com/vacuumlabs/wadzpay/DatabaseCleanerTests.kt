package com.vacuumlabs.wadzpay

import com.ninjasquad.springmockk.SpykBean
import com.vacuumlabs.vuba.common.VubaBusinessException
import com.vacuumlabs.vuba.ledger.service.LedgerService
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.AccountRepository
import com.vacuumlabs.wadzpay.ledger.model.SubaccountRepository
import io.kotest.matchers.longs.shouldBeExactly
import io.mockk.clearAllMocks
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows

class DatabaseCleanerTests : IntegrationTests() {

    companion object {
        const val DEFAULT_ACCOUNTS_AMOUNT = 2
        val CURRENCIES_AMOUNT = CurrencyUnit.values().size
    }

    @SpykBean
    lateinit var ledgerService: LedgerService

    @SpykBean
    lateinit var subaccountRepository: SubaccountRepository

    @SpykBean
    lateinit var accountRepository: AccountRepository

    @BeforeEach
    fun clear() {
        clearAllMocks()
        databaseCleanupService.deleteData()
    }

    // @Test
    fun `can add initial data`() {
        databaseCleanupService.createInitData()

        accountRepository.count() shouldBeExactly DEFAULT_ACCOUNTS_AMOUNT.toLong()
        subaccountRepository.count() shouldBeExactly (DEFAULT_ACCOUNTS_AMOUNT * CURRENCIES_AMOUNT).toLong()

        verify(exactly = CURRENCIES_AMOUNT) { ledgerService.createAsset(any(), any()) }
        verify(exactly = 1) { ledgerService.createCommit(any()) }
        verify(exactly = CURRENCIES_AMOUNT * DEFAULT_ACCOUNTS_AMOUNT) { subaccountRepository.save(any()) }
        verify(exactly = DEFAULT_ACCOUNTS_AMOUNT) { accountRepository.save(any()) }
    }

    // @Test
    fun `can remove and add data`() {
        databaseCleanupService.deleteData()
        databaseCleanupService.createInitData()

        accountRepository.count() shouldBeExactly DEFAULT_ACCOUNTS_AMOUNT.toLong()
        subaccountRepository.count() shouldBeExactly (DEFAULT_ACCOUNTS_AMOUNT * CURRENCIES_AMOUNT).toLong()

        verify(exactly = CURRENCIES_AMOUNT) { ledgerService.createAsset(any(), any()) }
        verify(exactly = 1) { ledgerService.createCommit(any()) }
        verify(exactly = CURRENCIES_AMOUNT * DEFAULT_ACCOUNTS_AMOUNT) { subaccountRepository.save(any()) }
        verify(exactly = DEFAULT_ACCOUNTS_AMOUNT) { accountRepository.save(any()) }
    }

    // @Test
    fun `can add and remove data`() {
        databaseCleanupService.createInitData()
        databaseCleanupService.deleteData()

        accountRepository.count() shouldBeExactly 0
        subaccountRepository.count() shouldBeExactly 0

        verify(exactly = CURRENCIES_AMOUNT) { ledgerService.createAsset(any(), any()) }
        verify(exactly = 1) { ledgerService.createCommit(any()) }
        verify(exactly = CURRENCIES_AMOUNT * DEFAULT_ACCOUNTS_AMOUNT) { subaccountRepository.save(any()) }
        verify(exactly = DEFAULT_ACCOUNTS_AMOUNT) { accountRepository.save(any()) }
    }

    // @Test
    fun `can remove data`() {
        databaseCleanupService.deleteData()

        accountRepository.count() shouldBeExactly 0
        subaccountRepository.count() shouldBeExactly 0

        verify(exactly = 0) { ledgerService.createAsset(any(), any()) }
        verify(exactly = 0) { ledgerService.createCommit(any()) }
        verify(exactly = 0) { subaccountRepository.save(any()) }
        verify(exactly = 0) { accountRepository.save(any()) }
    }

    // @Test
    fun `can add, remove, add data`() {
        databaseCleanupService.createInitData()
        databaseCleanupService.deleteData()
        databaseCleanupService.createInitData()

        accountRepository.count() shouldBeExactly DEFAULT_ACCOUNTS_AMOUNT.toLong()
        subaccountRepository.count() shouldBeExactly (DEFAULT_ACCOUNTS_AMOUNT * CURRENCIES_AMOUNT).toLong()

        verify(exactly = CURRENCIES_AMOUNT * 2) { ledgerService.createAsset(any(), any()) }
        verify(exactly = 2) { ledgerService.createCommit(any()) }
        verify(exactly = CURRENCIES_AMOUNT * DEFAULT_ACCOUNTS_AMOUNT * 2) { subaccountRepository.save(any()) }
        verify(exactly = DEFAULT_ACCOUNTS_AMOUNT * 2) { accountRepository.save(any()) }
    }

    // @Test
    fun `can't add data two times`() {
        databaseCleanupService.createInitData()
        assertThrows<VubaBusinessException> {
            databaseCleanupService.createInitData()
        }
    }
}
