package com.vacuumlabs.wadzpay

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.SpykBean
import com.vacuumlabs.wadzpay.accountowner.CreateExternalTransactionRequest
import com.vacuumlabs.wadzpay.bitgo.BitGoExternalTransaction
import com.vacuumlabs.wadzpay.bitgo.BitGoTransfer
import com.vacuumlabs.wadzpay.bitgo.BitGoTransferEntry
import com.vacuumlabs.wadzpay.bitgo.BitGoTransferType
import com.vacuumlabs.wadzpay.bitgo.BitGoWallet
import com.vacuumlabs.wadzpay.bitgo.WalletService
import com.vacuumlabs.wadzpay.bitgo.toBitGoCoin
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.control.CreateFakeTransactionRequest
import com.vacuumlabs.wadzpay.ledger.LedgerService
import com.vacuumlabs.wadzpay.ledger.model.TransactionDirection
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.viewmodels.TransactionViewModel
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import org.junit.Ignore
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import java.math.BigDecimal

@AutoConfigureMockMvc
// @TestMethodOrder(OrderAnnotation::class)
class SendExternalFlowTests @Autowired constructor(
    val mockMvc: MockMvc,
    var mapper: ObjectMapper,
    val userAccountService: UserAccountService,
    val appConfig: AppConfig
) : IntegrationTests() {

    @SpykBean
    lateinit var ledgerService: LedgerService

    @SpykBean
    lateinit var bitGoWallet: BitGoWallet

    @SpykBean
    lateinit var walletService: WalletService

    lateinit var userAccount: UserAccount

    companion object {
        val AMOUNT = OFFRAMP_AMOUNT
        val ASSET = OFFRAMP_CURRENCY
        val FEE = OFFRAMP_FEE
        const val RECEIVER_ADDRESS = OFFRAMP_RECEIVER_ADDRESS
        const val DESCRIPTION = OFFRAMP_DESCRIPTION
    }

    @BeforeAll
    fun clear() {
        databaseCleanupService.deleteData()
        databaseCleanupService.createInitData()

        userAccount = userAccountService.createUserAccount(COGNITO_USERNAME, EMAIL, PHONE_NUMBER)
    }

    // @Test
    @WithMockUser(username = EMAIL)
    @Order(1)
    fun `Step 1 - send crypto to an external wallet`() {
        // prefund account
        mockMvc.post(mapper, CREATE_FAKE_TRANSACTION, CreateFakeTransactionRequest(ONRAMP_AMOUNT, ASSET)).response<TransactionViewModel>(mapper)

        val amountScaled = AMOUNT.multiply(ASSET.scalingFactor())
        val feeScaled = FEE.multiply(ASSET.scalingFactor())
        val bitGoTransfer = BitGoTransfer(
            BIT_GO_TRANSFER_ID,
            BLOCKCHAIN_TX_ID,
            BitGoTransferType.send,
            BIT_GO_COIN,
            amountScaled,
            feeScaled,
            listOf(
                BitGoTransferEntry(
                    "ADDRESS",
                    appConfig.walletIds.btc,
                    amountScaled
                )
            )
        )

        val bitGoExternalTransaction = BitGoExternalTransaction(bitGoTransfer, "TX", TX_ID, "signed")

        every { bitGoWallet.getFeeEstimate(ASSET.toBitGoCoin(appConfig.production), RECEIVER_ADDRESS, AMOUNT) } returns FEE
        every { bitGoWallet.sendToExternalWallet(AMOUNT, ASSET, RECEIVER_ADDRESS) } returns bitGoExternalTransaction

        mockMvc.post(mapper, USER_SEND_EXTERNAL, CreateExternalTransactionRequest(null, AMOUNT, ASSET, RECEIVER_ADDRESS, DESCRIPTION))

        verify { ledgerService.createExternalTransaction(userAccount, AMOUNT, FEE, ASSET, BLOCKCHAIN_TX_ID, DESCRIPTION, TransactionStatus.SUCCESSFUL, TransactionType.EXTERNAL_SEND) }
    }

    @Ignore
    // @Test
    @Order(2)
    @WithMockUser(username = EMAIL)
    fun `Step 2 - transaction is recorded and balance updated`() {
        val transactions = mockMvc.get(mapper, USER_TRANSACTIONS_URL).response<List<TransactionViewModel>>(mapper)
        transactions shouldHaveSize 2
        with(transactions.first()) {
            amount shouldBe AMOUNT
            feeAmount shouldBe FEE
            totalAmount shouldBe AMOUNT + FEE
            asset shouldBe ASSET
            status shouldBe TransactionStatus.SUCCESSFUL
            direction shouldBe TransactionDirection.OUTGOING
        }

        val balance = mockMvc.get(mapper, USER_BALANCES_URL).response<Map<String, BigDecimal>>(mapper)
        balance shouldContain Pair(ASSET.toString(), ONRAMP_AMOUNT - AMOUNT - FEE)
    }
}
