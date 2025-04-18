package com.vacuumlabs.wadzpay

import com.fasterxml.jackson.databind.ObjectMapper
import com.vacuumlabs.wadzpay.merchant.MerchantCredentials
import com.vacuumlabs.wadzpay.merchant.MerchantService
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.user.UserAccountService
import io.mockk.clearAllMocks
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import java.math.BigDecimal
import com.vacuumlabs.wadzpay.merchant.model.Order as OrderModel

@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation::class)
class OrderFlowTests @Autowired constructor(
    val mockMvc: MockMvc,
    var mapper: ObjectMapper,
    val merchantService: MerchantService,
    val userAccountService: UserAccountService
) : IntegrationTests() {

    companion object {
        val AMOUNT = BigDecimal("1.12")
        val CURRENCY = ONRAMP_CURRENCY

        lateinit var merchantCredentials: MerchantCredentials
        lateinit var userAccount: UserAccount
        lateinit var order: OrderModel
    }

    @BeforeAll
    fun clear() {
        clearAllMocks()
        databaseCleanupService.deleteData()
        databaseCleanupService.createInitData()

        val merchant = merchantService.createMerchant(CREATE_MERCHANT_REQUEST)
        merchantCredentials = merchantService.issueNewApiKey(merchant)
        userAccount = userAccountService.createUserAccount(
            COGNITO_USERNAME,
            EMAIL,
            PHONE_NUMBER
        )
    }

   /* @Test
    @Order(1)
    @WithMockUser(username = MERCHANT_NAME, roles = ["MERCHANT"])
    fun `Step 1 - create order by merchant`() {
        order = mockMvc.post(
            mapper, CREATE_ORDER_URL,
            CreateOrderRequest(
                AMOUNT, CURRENCY, BigDecimal("50.32"), FiatCurrencyUnit.EUR, "order123", "description"
            )
        ) { status { isCreated() } }.response(mapper)
    }*/
/*
    @Test
    @Order(2)
    @WithMockUser(username = EMAIL)
    fun `Step 2 - create transaction from order by user`() {
        mockMvc.post(mapper, CREATE_FAKE_TRANSACTION, CreateFakeTransactionRequest(ONRAMP_AMOUNT, ASSET))

        mockMvc.post(mapper, CREATE_TRANSACTION_FROM_ORDER_URL, CreateTransactionFromOrderRequest(order.uuid))
    }*/

/*    @Test
    @Order(3)
    @WithMockUser(username = EMAIL)
    fun `Step 3 - transaction is recorded and balance updated for user`() {
        val transactions = mockMvc.get(mapper, USER_TRANSACTIONS_URL).response<List<TransactionViewModel>>(mapper)
        transactions shouldHaveSize 2
        with(transactions[0]) {
            amount shouldBe AMOUNT
            asset shouldBe CURRENCY
            status shouldBe TransactionStatus.SUCCESSFUL
            direction shouldBe TransactionDirection.OUTGOING
        }

        val balance = mockMvc.get(mapper, USER_BALANCES_URL).response<Map<String, BigDecimal>>(mapper)
        balance shouldContain Pair(ONRAMP_CURRENCY.toString(), ONRAMP_AMOUNT.minus(AMOUNT).stripTrailingZeros())
    }*/

/*
    @Test
    @Order(4)
    @WithMockUser(username = MERCHANT_NAME, roles = ["MERCHANT"])
    fun `Step 4 - transaction is recorded and balance updated for merchant`() {
        val transactions = mockMvc.get(mapper, MERCHANT_TRANSACTIONS_URL).response<List<TransactionViewModel>>(mapper)
        transactions shouldHaveSize 1
        with(transactions.first()) {
            amount shouldBe AMOUNT
            asset shouldBe CURRENCY
            status shouldBe TransactionStatus.SUCCESSFUL
            direction shouldBe TransactionDirection.INCOMING
        }

        val balance = mockMvc.get(mapper, MERCHANT_BALANCES_URL).response<Map<String, BigDecimal>>(mapper)
        balance shouldContain Pair(ONRAMP_CURRENCY.toString(), AMOUNT)
    }
*/

  /*  @Test
    @Order(5)
    @WithMockUser(username = EMAIL)
    fun `Step 5 - transaction can't be cancelled once it's processed`() {
        val response = mockMvc.post(mapper, CANCEL_ORDER_URL, CancelOrderRequest(order_id = order.uuid)) {
            status { isUnprocessableEntity() }
        }.response<ErrorResponse>(mapper)
        response.message shouldBe ErrorCodes.PROCESSED_ORDER
        response.status shouldBe HttpStatus.UNPROCESSABLE_ENTITY.value()
    }*/
}
