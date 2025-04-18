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
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.control.CreateFakeTransactionRequest
import com.vacuumlabs.wadzpay.ledger.LedgerService
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.viewmodels.TransactionViewModel
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import org.junit.Ignore
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import java.math.BigDecimal

@AutoConfigureMockMvc
class SendExternalTests @Autowired constructor(val mockMvc: MockMvc, var mapper: ObjectMapper, val userAccountService: UserAccountService, val appConfig: AppConfig) : IntegrationTests() {

    @SpykBean
    lateinit var ledgerService: LedgerService

    @SpykBean
    lateinit var bitGoWallet: BitGoWallet

    @SpykBean
    lateinit var walletService: WalletService

    lateinit var userAccount: UserAccount

    lateinit var bitGoExternalTransaction: BitGoExternalTransaction

    companion object {
        val AMOUNT = OFFRAMP_AMOUNT
        val ASSET = OFFRAMP_CURRENCY
        val FEE = OFFRAMP_FEE
        const val RECEIVER_ADDRESS = OFFRAMP_RECEIVER_ADDRESS
        const val DESCRIPTION = OFFRAMP_DESCRIPTION
    }

    private fun getBitGoExternalTransaction(amount: BigDecimal, fee: BigDecimal): BitGoExternalTransaction {
        val amountScaled = amount.multiply(ASSET.scalingFactor())
        val feeScaled = fee.multiply(ASSET.scalingFactor())
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

        return BitGoExternalTransaction(bitGoTransfer, "TX", TX_ID, "signed")
    }

    @BeforeEach
    fun clear() {
        databaseCleanupService.deleteData()
        databaseCleanupService.createInitData()

        userAccount = userAccountService.createUserAccount(COGNITO_USERNAME, EMAIL, PHONE_NUMBER)
        bitGoExternalTransaction = getBitGoExternalTransaction(AMOUNT, FEE)
    }

    @Ignore
    @WithMockUser(username = EMAIL)
    fun `Can't send money to an external wallet if not enough funds`() {
        every { bitGoWallet.getFeeEstimate(ASSET.toBitGoCoin(appConfig.production), RECEIVER_ADDRESS, AMOUNT) } returns FEE
        every { bitGoWallet.sendToExternalWallet(AMOUNT, ASSET, RECEIVER_ADDRESS) } returns bitGoExternalTransaction

        val response = mockMvc.post(
            mapper,
            USER_SEND_EXTERNAL, CreateExternalTransactionRequest(null, AMOUNT, ASSET, RECEIVER_ADDRESS, DESCRIPTION)
        ) { status { isUnprocessableEntity() } }.response<ErrorResponse>(mapper)
        response.message shouldBe ErrorCodes.INSUFFICIENT_FUNDS
        response.status shouldBe HttpStatus.UNPROCESSABLE_ENTITY.value()
    }

    /*    @Test
        @WithMockUser(username = EMAIL)
        fun `Can't send money to an external wallet if amount has too many decimal places`() {
            mockMvc.post(mapper, CREATE_FAKE_TRANSACTION, CreateFakeTransactionRequest(ONRAMP_AMOUNT, ASSET)).response<TransactionViewModel>(mapper)

            val amount = BigDecimal("1.00000000000000000000000000000000000000001")

            val bitGoExternalTransaction = getBitGoExternalTransaction(amount, FEE)

            every { bitGoWallet.getFeeEstimate(ASSET.toBitGoCoin(appConfig.production), RECEIVER_ADDRESS, amount) } returns FEE
            every { bitGoWallet.sendToExternalWallet(amount, ASSET, RECEIVER_ADDRESS) } returns bitGoExternalTransaction

            val response = mockMvc.post(
                mapper,
                USER_SEND_EXTERNAL, CreateExternalTransactionRequest(amount, ASSET, RECEIVER_ADDRESS, DESCRIPTION)
            ) { status { isBadRequest() } }.response<ErrorResponse>(mapper)
            response.message shouldBe ErrorCodes.INVALID_AMOUNT_TOO_MANY_DECIMAL_PLACES
            response.status shouldBe HttpStatus.BAD_REQUEST.value()
        }*/

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can't send money to an external wallet if amount is negative`() {
        mockMvc.post(mapper, CREATE_FAKE_TRANSACTION, CreateFakeTransactionRequest(ONRAMP_AMOUNT, ASSET)).response<TransactionViewModel>(mapper)

        val amount = BigDecimal("-1")

        val bitGoExternalTransaction = getBitGoExternalTransaction(amount, FEE)

        every { bitGoWallet.getFeeEstimate(ASSET.toBitGoCoin(appConfig.production), RECEIVER_ADDRESS, amount) } returns FEE
        every { bitGoWallet.sendToExternalWallet(amount, ASSET, RECEIVER_ADDRESS) } returns bitGoExternalTransaction

        val response = mockMvc.post(
            mapper,
            USER_SEND_EXTERNAL, CreateExternalTransactionRequest(null, amount, ASSET, RECEIVER_ADDRESS, DESCRIPTION)
        ) { status { isBadRequest() } }.response<ErrorResponse>(mapper)
        response.message shouldBe ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO
        response.status shouldBe HttpStatus.BAD_REQUEST.value()
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can send money to an external wallet if enough funds`() {
        mockMvc.post(mapper, CREATE_FAKE_TRANSACTION, CreateFakeTransactionRequest(ONRAMP_AMOUNT, ASSET)).response<TransactionViewModel>(mapper)

        every { bitGoWallet.getFeeEstimate(ASSET.toBitGoCoin(appConfig.production), RECEIVER_ADDRESS, AMOUNT) } returns FEE
        every { bitGoWallet.sendToExternalWallet(AMOUNT, ASSET, RECEIVER_ADDRESS) } returns bitGoExternalTransaction
//        every { walletService.sendToExternalWalletReverse(AMOUNT, ASSET, RECEIVER_ADDRESS) } returns bitGoExternalTransaction

        mockMvc.post(
            mapper,
            USER_SEND_EXTERNAL, CreateExternalTransactionRequest(null, AMOUNT, ASSET, RECEIVER_ADDRESS, DESCRIPTION)
        )

        verify { ledgerService.createExternalTransaction(userAccount, AMOUNT, FEE, ASSET, BLOCKCHAIN_TX_ID, DESCRIPTION, TransactionStatus.SUCCESSFUL, TransactionType.EXTERNAL_SEND) }
    }
}
