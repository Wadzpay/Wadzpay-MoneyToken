package com.vacuumlabs.wadzpay

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.SpykBean
import com.vacuumlabs.wadzpay.bitgo.BitGoTransfer
import com.vacuumlabs.wadzpay.bitgo.BitGoTransferType
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.control.CreateFakeTransactionRequest
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.user.CreatePeerToPeerTransactionRequest
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.viewmodels.TransactionViewModel
import com.vacuumlabs.wadzpay.webhook.BitGoCoin
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import org.junit.Ignore
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import java.math.BigDecimal

@AutoConfigureMockMvc
@Disabled
@Ignore
class PeerToPeerTransactionTests @Autowired constructor(
    val mockMvc: MockMvc,
    val mapper: ObjectMapper,
    val userAccountService: UserAccountService,
    val appConfig: AppConfig
) : IntegrationTests() {
    @SpykBean
    lateinit var senderUserAccount: UserAccount
    lateinit var receiverUserAccount: UserAccount

    companion object {
        const val CREATE_PEER_TO_PEER_TRANSACTION = "/user/account/p2p_transaction"

        const val RECEIVER_COGNITO_USERNAME = "123456799"
        const val RECEIVER_EMAIL = "email2@domain.com"
        const val RECEIVER_PHONE_NUMBER = "+421944112234"
        const val NON_EXISTENT_RECEIVER_EMAIL = "wrong_email@domain.com"

        val ASSET = CurrencyUnit.BTC
        val ASSET_WITHOUT_FUNDS = CurrencyUnit.ETH
        val AMOUNT = BigDecimal.valueOf(2.52)
        val ONRAMP_AMOUNT = AMOUNT.multiply(BigDecimal.valueOf(1.005))
        val BITGO_TRANSFER = BitGoTransfer("", "", BitGoTransferType.receive, BitGoCoin.btc, AMOUNT.multiply(CurrencyUnit.BTC.scalingFactor()), BigDecimal.ZERO)
    }

    @BeforeEach
    fun clear() {
        clearAllMocks()
        databaseCleanupService.deleteData()
        databaseCleanupService.createInitData()

        senderUserAccount = userAccountService.createUserAccount(COGNITO_USERNAME, EMAIL, PHONE_NUMBER)
        receiverUserAccount = userAccountService.createUserAccount(RECEIVER_COGNITO_USERNAME, RECEIVER_EMAIL, RECEIVER_PHONE_NUMBER)
    }

    @Test
    fun `Can't make a peer-to-peer transaction if user is not authorized`() {
        mockMvc.post(mapper, CREATE_PEER_TO_PEER_TRANSACTION, CreatePeerToPeerTransactionRequest(AMOUNT, ASSET, RECEIVER_COGNITO_USERNAME)) {
            status { isForbidden() }
        }
    }

    @Ignore
    @WithMockUser(username = EMAIL)
    fun `Can't make a peer-to-peer transaction if funds are insufficient`() {
        val response = mockMvc.post(mapper, CREATE_PEER_TO_PEER_TRANSACTION, CreatePeerToPeerTransactionRequest(AMOUNT, ASSET_WITHOUT_FUNDS, RECEIVER_COGNITO_USERNAME)) {
            status { isUnprocessableEntity() }
        }.response<ErrorResponse>(mapper)
        response.message shouldBe ErrorCodes.INSUFFICIENT_FUNDS
        response.status shouldBe HttpStatus.UNPROCESSABLE_ENTITY.value()
    }

    @Test
    @WithMockUser(username = EMAIL)
    fun `Can't make a peer-to-peer transaction if receiver doesn't exist`() {
        val response = mockMvc.post(mapper, CREATE_PEER_TO_PEER_TRANSACTION, CreatePeerToPeerTransactionRequest(AMOUNT, ASSET_WITHOUT_FUNDS, NON_EXISTENT_RECEIVER_EMAIL)) {
            status { isNotFound() }
        }.response<ErrorResponse>(mapper)
        response.message shouldBe ErrorCodes.USER_NOT_FOUND
    }
    var feeAmountGlobal: BigDecimal? = null
    @Ignore
    @Test
    @WithMockUser(username = EMAIL)
    fun `Can't make a peer-to-peer transaction if not enough funds for fee`() {
        mockMvc.post(mapper, CREATE_FAKE_TRANSACTION, CreateFakeTransactionRequest(AMOUNT, ASSET))
        if (feeAmountGlobal?.stripTrailingZeros() != BigDecimal.ZERO) {
            mockMvc.post(mapper, CREATE_PEER_TO_PEER_TRANSACTION, CreatePeerToPeerTransactionRequest(AMOUNT, ASSET, RECEIVER_COGNITO_USERNAME)) {
                status { isUnprocessableEntity() }
            }
        }
    }

    @Ignore
    @Test
    @WithMockUser(username = EMAIL)
    fun `Can make a peer-to-peer transaction if enough funds`() {
        mockMvc.post(mapper, CREATE_FAKE_TRANSACTION, CreateFakeTransactionRequest(ONRAMP_AMOUNT, ASSET))

        mockMvc.post(mapper, CREATE_PEER_TO_PEER_TRANSACTION, CreatePeerToPeerTransactionRequest(AMOUNT, ASSET, RECEIVER_COGNITO_USERNAME))

        val transactions = mockMvc.get(mapper, USER_TRANSACTIONS_URL).response<List<TransactionViewModel>>(mapper)

        transactions shouldHaveSize 2
        with(transactions[0]) {
            status shouldBe TransactionStatus.SUCCESSFUL
            transactionType shouldBe TransactionType.PEER_TO_PEER
            feeAmountGlobal = feeAmount
            if (feeAmount.stripTrailingZeros() != BigDecimal.ZERO) {
                feeAmount.stripTrailingZeros() shouldBe (ONRAMP_AMOUNT - AMOUNT).stripTrailingZeros()
                feePercentage.stripTrailingZeros() shouldBe appConfig.feeRate.multiply(BigDecimal.valueOf(100))
                    .stripTrailingZeros()
                totalAmount.stripTrailingZeros() shouldBe ONRAMP_AMOUNT.stripTrailingZeros()
            }
            amount shouldBe AMOUNT
        }
    }

    @Test
    @WithMockUser(username = EMAIL)
    fun `Can't make a peer-to-peer transaction with description that is too long`() {
        mockMvc.post(mapper, CREATE_FAKE_TRANSACTION, CreateFakeTransactionRequest(AMOUNT, ASSET))

        mockMvc.post(mapper, CREATE_PEER_TO_PEER_TRANSACTION, CreatePeerToPeerTransactionRequest(AMOUNT, ASSET, RECEIVER_EMAIL, description = getRandomString(200))) {
            status { isBadRequest() }
        }
    }

    fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}
