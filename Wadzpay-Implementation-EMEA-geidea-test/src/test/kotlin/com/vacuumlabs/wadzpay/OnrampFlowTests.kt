package com.vacuumlabs.wadzpay

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.SpykBean
import com.vacuumlabs.wadzpay.bitgo.BitGoWallet
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.ledger.LedgerService
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.user.UserAccountService
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc

@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation::class)
class OnrampFlowTests @Autowired constructor(
    val mockMvc: MockMvc,
    var mapper: ObjectMapper,
    val userAccountService: UserAccountService,
    val appConfig: AppConfig
) : IntegrationTests() {

    @SpykBean
    lateinit var ledgerService: LedgerService

    @SpykBean
    lateinit var bitGoWallet: BitGoWallet

    companion object {
        const val BITGO_WEBHOOK_URL = "/webhook/wallet"
        const val WALLET_ID = "17VZNX1SN5NtKa8UQFxwQbFeFc3iqRYhem"

        lateinit var userAccount: UserAccount
    }

    @BeforeAll
    fun clear() {
        databaseCleanupService.deleteData()
        databaseCleanupService.createInitData()

        userAccount = userAccountService.createUserAccount(COGNITO_USERNAME, EMAIL, PHONE_NUMBER)
    }

    /*@Test
    @Order(1)
    fun `Step 1 - receive webhook notification from Bitgo`() {
        val bitgoNotification = BitGoEvent(BitGoType.transfer, WALLET_ID, BLOCKCHAIN_TX_ID, BIT_GO_COIN, BIT_GO_TRANSFER_ID, BitGoState.confirmed)
        val onrampAmountScaled = ONRAMP_AMOUNT.multiply(CurrencyUnit.BTC.scalingFactor())
        val bitGoTransfer = BitGoTransfer(
            BIT_GO_TRANSFER_ID,
            BLOCKCHAIN_TX_ID,
            BitGoTransferType.receive,
            BIT_GO_COIN,
            onrampAmountScaled,
            BigDecimal.ZERO,
            listOf(
                BitGoTransferEntry(
                    userAccount.account.getSubaccountByAsset(ONRAMP_CURRENCY).address!!.address,
                    appConfig.walletIds.btc,
                    onrampAmountScaled
                )
            )
        )

        every { bitGoWallet.getTransfer(BIT_GO_COIN, WALLET_ID, BIT_GO_TRANSFER_ID) } returns bitGoTransfer

        mockMvc.post(mapper, BITGO_WEBHOOK_URL, bitgoNotification)

        verify { ledgerService.receiveExternalMoney(bitGoTransfer) }
    }

    @Test
    @Order(2)
    @WithMockUser(username = EMAIL)
    fun `Step 2 - transaction is recorded and balance updated`() {
        val transactions = mockMvc.get(mapper, USER_TRANSACTIONS_URL).response<List<TransactionViewModel>>(mapper)
        transactions shouldHaveSize 1
        with(transactions.first()) {
            amount shouldBe ONRAMP_AMOUNT
            asset shouldBe ONRAMP_CURRENCY
            status shouldBe TransactionStatus.SUCCESSFUL
            direction shouldBe TransactionDirection.INCOMING
        }

        val balance = mockMvc.get(mapper, USER_BALANCES_URL).response<Map<String, BigDecimal>>(mapper)
        balance shouldContain Pair(ONRAMP_CURRENCY.toString(), ONRAMP_AMOUNT)
    }*/
}
