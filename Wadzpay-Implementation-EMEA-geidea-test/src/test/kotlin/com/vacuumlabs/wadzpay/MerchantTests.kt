package com.vacuumlabs.wadzpay

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.SpykBean
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.control.CreateFakeTransactionRequest
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.TransactionRepository
import com.vacuumlabs.wadzpay.merchant.CreateMerchantResponse
import com.vacuumlabs.wadzpay.merchant.CreateOrderRequest
import com.vacuumlabs.wadzpay.merchant.InvalidateApiKeyRequest
import com.vacuumlabs.wadzpay.merchant.MerchantCredentials
import com.vacuumlabs.wadzpay.merchant.MerchantService
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.Merchant
import com.vacuumlabs.wadzpay.merchant.model.Order
import com.vacuumlabs.wadzpay.merchant.model.OrderRepository
import com.vacuumlabs.wadzpay.merchant.model.OrderStatus
import com.vacuumlabs.wadzpay.merchant.model.OrderType
import com.vacuumlabs.wadzpay.user.CancelOrderRequest
import com.vacuumlabs.wadzpay.user.CreateTransactionFromOrderRequest
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.user.UserAccountService
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.util.UUID

@AutoConfigureMockMvc
class MerchantTests @Autowired constructor(
    val mockMvc: MockMvc,
    var mapper: ObjectMapper,
    var userAccountService: UserAccountService,
    var merchantService: MerchantService,
    var orderRepository: OrderRepository
) : IntegrationTests() {
    @SpykBean
    lateinit var transactionRepository: TransactionRepository

    lateinit var userAccount: UserAccount
    lateinit var merchant: Merchant
    lateinit var order: Order
    lateinit var expiredOrder: Order

    companion object {
        const val MERCHANT_NAME_2 = "MERCHANT_NAME_2"
        const val INVALID_USER = "INVALID_USER"
        val ASSET = CurrencyUnit.BTC
        val AMOUNT = BigDecimal.valueOf(2.5213123)
        val EXTERNAL_ORDER_ID = "test_order_id"
        val DESCRIPTION = "test description"
    }

    @BeforeEach
    fun clear() {
        clearAllMocks()
        databaseCleanupService.deleteData()
        databaseCleanupService.createInitData()

        userAccount = userAccountService.createUserAccount(COGNITO_USERNAME, EMAIL, PHONE_NUMBER)
        merchantService.createMerchant(CREATE_MERCHANT_REQUEST)
        merchant = merchantService.getMerchantByName(MERCHANT_NAME)

        order = orderRepository.save(
            Order(
                uuid = UUID.randomUUID(),
                amount = AMOUNT,
                currency = ASSET,
                type = OrderType.ORDER,
                fiatAmount = ONRAMP_FIAT_AMOUNT,
                fiatCurrency = ONRAMP_FIAT_CURRENCY,
                target = merchant,
                externalOrderId = EXTERNAL_ORDER_ID,
                description = DESCRIPTION
            )
        )
        order.status shouldBe OrderStatus.OPEN

        expiredOrder = orderRepository.save(
            Order(
                uuid = UUID.randomUUID(),
                amount = AMOUNT,
                currency = CurrencyUnit.BTC,
                type = OrderType.ORDER,
                source = userAccount,
                target = merchant,
                createdAt = (Instant.now() - Duration.ofMinutes(10))
            )
        )
        expiredOrder.status shouldBe OrderStatus.EXPIRED
    }

    // @Test
    @WithMockUser(username = MERCHANT_NAME, roles = ["MERCHANT"])
    fun `Get order with invalid order id returns 404`() {
        val orderResponse = mockMvc.get(mapper, "$GET_ORDER_URL/invalid_order_id") { isBadRequest() }.response<ErrorResponse>(mapper)
        orderResponse.message shouldBe ErrorCodes.BAD_REQUEST
        orderResponse.status shouldBe HttpStatus.BAD_REQUEST.value()
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can't make a merchant transaction if funds are insufficient`() {
        val response =
            mockMvc.post(mapper, CREATE_TRANSACTION_FROM_ORDER_URL, CreateTransactionFromOrderRequest(order.uuid)) {
                status { isUnprocessableEntity() }
            }.response<ErrorResponse>(mapper)
        order.status shouldBe OrderStatus.OPEN
        response.message shouldBe ErrorCodes.INSUFFICIENT_FUNDS
        response.status shouldBe HttpStatus.UNPROCESSABLE_ENTITY.value()

        verify(exactly = 0) { transactionRepository.save(any()) }
    }

    // @Test
    fun `Can't make a merchant transaction if user is not authorized`() {
        mockMvc.post(mapper, CREATE_TRANSACTION_FROM_ORDER_URL, CreateTransactionFromOrderRequest(UUID.randomUUID())) {
            status { isForbidden() }
        }
    }

    // @Test
    @WithMockUser(username = MERCHANT_NAME)
    fun `Can't make a merchant transaction if order id is random`() {
        val response = mockMvc.post(
            mapper,
            CREATE_TRANSACTION_FROM_ORDER_URL,
            CreateTransactionFromOrderRequest(UUID.randomUUID())
        ) {
            status { isNotFound() }
        }.response<ErrorResponse>(mapper)
        response.message shouldBe ErrorCodes.ORDER_NOT_FOUND
        response.status shouldBe HttpStatus.NOT_FOUND.value()
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can't make transaction if order is expired`() {
        val response = mockMvc.post(
            mapper,
            CREATE_TRANSACTION_FROM_ORDER_URL,
            CreateTransactionFromOrderRequest(expiredOrder.uuid)
        ) {
            status { isUnprocessableEntity() }
        }.response<ErrorResponse>(mapper)
        response.message shouldBe ErrorCodes.EXPIRED_ORDER
        response.status shouldBe HttpStatus.UNPROCESSABLE_ENTITY.value()
        expiredOrder.status shouldBe OrderStatus.EXPIRED

        verify(exactly = 0) { transactionRepository.save(any()) }
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can't make transaction if order is cancelled`() {
        mockMvc.post(mapper, CANCEL_ORDER_URL, CancelOrderRequest(order.uuid))

        val response = mockMvc.post(
            mapper,
            CREATE_TRANSACTION_FROM_ORDER_URL,
            CreateTransactionFromOrderRequest(order.uuid)
        ) {
            status { isUnprocessableEntity() }
        }.response<ErrorResponse>(mapper)
        response.message shouldBe ErrorCodes.CANCELLED_ORDER
        response.status shouldBe HttpStatus.UNPROCESSABLE_ENTITY.value()

        verify(exactly = 0) { transactionRepository.save(any()) }
    }

  /*  //@Test
    @WithMockUser(username = EMAIL)
    fun `Can make a merchant transaction if enough funds`() {

        val prefundTransaction =
            mockMvc.post(
                mapper,
                CREATE_FAKE_TRANSACTION,
                CreateFakeTransactionRequest(AMOUNT, ASSET)
            ).response<TransactionViewModel>(mapper)

        mockMvc.post(mapper, CREATE_TRANSACTION_FROM_ORDER_URL, CreateTransactionFromOrderRequest(order.uuid))

        order = orderRepository.getByUuid(order.uuid)!!
        val transaction = order.transaction!!
        transaction.status shouldBe TransactionStatus.SUCCESSFUL
        transaction.type shouldBe TransactionType.MERCHANT
        order.amount shouldBe transaction.amount
        order.currency shouldBe transaction.asset
        order.source shouldBe transaction.sender.account.owner
        order.target shouldBe transaction.receiver.account.owner
        order.createdAt.shouldBeLessThan(transaction.createdAt)
        order.status shouldBe OrderStatus.PROCESSED

        mockMvc.get(
            mapper, USER_TRANSACTIONS_URL,
            listOf(
                TransactionViewModel(
                    transaction.uuid,
                    order.uuid,
                    order.externalOrderId,
                    transaction.createdAt,
                    transaction.amount,
                    transaction.asset,
                    transaction.fiatAmount,
                    transaction.fiatAsset,
                    transaction.status,
                    transaction.type,
                    transaction.sender.account.getOwnerName(),
                    transaction.receiver.account.getOwnerName(),
                    TransactionDirection.OUTGOING,
                    transaction.description,
                    transaction.amount
                ),
                prefundTransaction
            )
        )

        verify(exactly = 2) { transactionRepository.save(any()) }
    }*/

    // @Test
    fun `Can't create a merchant when it exists`() {
        assertThrows<EntityNotFoundException> { merchantService.getMerchantByName(MERCHANT_NAME_2) }

        mockMvc.post(mapper, CREATE_MERCHANT_URL, CREATE_MERCHANT_REQUEST.copy(name = MERCHANT_NAME)) { status { isConflict() } }

        assertThrows<EntityNotFoundException> { merchantService.getMerchantByName(MERCHANT_NAME_2) }
    }

    // @Test
    fun `Can create merchant`() {
        assertThrows<EntityNotFoundException> { merchantService.getMerchantByName(MERCHANT_NAME_2) }

        mockMvc.post(mapper, CREATE_MERCHANT_URL, CREATE_MERCHANT_REQUEST.copy(name = MERCHANT_NAME_2)) { status { isCreated() } }

        merchantService.getMerchantByName(MERCHANT_NAME_2)
    }

    // @Test
    fun `Can't create order for unauthorized merchant`() {
        mockMvc.post(
            mapper,
            CREATE_ORDER_URL,
            CreateOrderRequest(
                amount = order.amount,
                currency = order.currency,
                type = OrderType.ORDER,
                externalOrderId = order.externalOrderId.toString(),
                description = order.description,
                fiatAmount = BigDecimal.ZERO,
                fiatCurrency = FiatCurrencyUnit.AED
            )
        ) {
            status { isForbidden() }
        }
    }

   /* //@Test
    @WithMockUser(username = MERCHANT_NAME, roles = ["MERCHANT"])
    fun `Can create merchant order`() {
        val order = mockMvc.post(
            mapper,
            CREATE_ORDER_URL,
            order.externalOrderId?.let { CreateOrderRequest(amount = order.amount, currency = order.currency, externalOrderId = it, description = order.description, fiatAmount = BigDecimal.ZERO, fiatCurrency = FiatCurrencyUnit.AED) }
        ) {
            status { isCreated() }
        }.response<Order>(mapper)

        order.status shouldBe OrderStatus.OPEN

        mockMvc.get(mapper, "$GET_ORDER_URL/${order.uuid}", order)
    }*/

    // @Test
    fun `Can't get order for unathorized merchant`() {
        mockMvc.get(mapper, "$GET_ORDER_URL/${UUID.randomUUID()}") { isForbidden() }
    }

    // @Test
    @WithMockUser(username = MERCHANT_NAME, roles = ["MERCHANT"])
    fun `Can't get not existing order`() {
        mockMvc.get(mapper, "$GET_ORDER_URL/${UUID.randomUUID()}") { isNotFound() }
    }

    // @Test
    @WithMockUser(username = MERCHANT_NAME, roles = ["MERCHANT"])
    fun `Can't create non-positive order`() {
        mockMvc.post(
            mapper,
            CREATE_ORDER_URL,
            CreateOrderRequest(amount = BigDecimal.ZERO, currency = order.currency, externalOrderId = order.externalOrderId.toString(), description = order.description, fiatAmount = BigDecimal.ZERO, fiatCurrency = FiatCurrencyUnit.AED)
        ) {
            status { isBadRequest() }
        }

        mockMvc.post(
            mapper,
            CREATE_ORDER_URL,
            CreateOrderRequest(amount = AMOUNT.negate(), currency = order.currency, externalOrderId = order.externalOrderId.toString(), description = order.description, fiatAmount = BigDecimal.ZERO, fiatCurrency = FiatCurrencyUnit.AED)
        ) {
            status { isBadRequest() }
        }
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can't create order if you are not merchant`() {
        mockMvc.post(
            mapper,
            CREATE_ORDER_URL,
            CreateOrderRequest(amount = AMOUNT, currency = order.currency, externalOrderId = order.externalOrderId.toString(), description = order.description, fiatAmount = BigDecimal.ZERO, fiatCurrency = FiatCurrencyUnit.AED)
        ) {
            status { isForbidden() }
        }
    }

    // @Test
    @WithMockUser(username = INVALID_USER)
    fun `Can't create transaction if user account doesn't exist`() {
        mockMvc.post(mapper, CREATE_TRANSACTION_FROM_ORDER_URL, CreateTransactionFromOrderRequest(order.uuid)) {
            status { isNotFound() }
        }
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can't process order two times`() {
        mockMvc.post(mapper, CREATE_FAKE_TRANSACTION, CreateFakeTransactionRequest(AMOUNT, ASSET))

        mockMvc.post(mapper, CREATE_TRANSACTION_FROM_ORDER_URL, CreateTransactionFromOrderRequest(order.uuid))
        mockMvc.post(mapper, CREATE_TRANSACTION_FROM_ORDER_URL, CreateTransactionFromOrderRequest(order.uuid)) {
            status { isUnprocessableEntity() }
        }
    }

    // @Test
    fun `Can decode token`() {
        val token = "ZGVtbyBtZXJjaGFudF8yOmY5ODhkMTUwLTcyMGMtNDQwYi1hMmNmLTM0ZTc2M2Q5YzY3Ng=="
        val credentials = MerchantCredentials.fromToken(token)
        credentials.apiKeyId shouldBe "demo merchant_2"
        credentials.apiKeySecret shouldBe "f988d150-720c-440b-a2cf-34e763d9c676"
        credentials.encode() shouldBe "Basic $token"
        credentials.getMerchantName() shouldBe "demo merchant"
    }

    // @Test
    fun `Can authenticate as a merchant`() {
        val credentials = mockMvc.post(mapper, CREATE_MERCHANT_URL, CREATE_MERCHANT_REQUEST.copy(name = MERCHANT_NAME_2)) { status { isCreated() } }.response<CreateMerchantResponse>(mapper)

        mockMvc.post(
            mapper,
            CREATE_ORDER_URL,
            CreateOrderRequest(order.amount, order.currency, BigDecimal.ZERO, FiatCurrencyUnit.AED, order.externalOrderId.toString(), order.description),
            listOf(Pair("Authorization", credentials.basicKey))
        ) {
            status { isCreated() }
        }
    }

    // @Test
    fun `Can't authenticate as a merchant - invalid key`() {
        val credentials = mockMvc.post(mapper, CREATE_MERCHANT_URL, CREATE_MERCHANT_REQUEST.copy(name = MERCHANT_NAME_2)) { status { isCreated() } }.response<CreateMerchantResponse>(mapper)

        mockMvc.post(
            mapper,
            CREATE_ORDER_URL,
            CreateOrderRequest(order.amount, order.currency, BigDecimal.ZERO, FiatCurrencyUnit.AED, order.externalOrderId.toString(), order.description),
            listOf(Pair("Authorization", credentials.basicKey + "invalid"))
        ) {
            status { isForbidden() }
        }
    }

/*    //@Test
    @WithMockUser(username = MERCHANT_NAME, roles = ["MERCHANT"])
    fun `Can't create order with a lot of decimal places`() {
        mockMvc.post(
            mapper,
            CREATE_ORDER_URL,
            CreateOrderRequest(BigDecimal("0.123456789"), CurrencyUnit.BTC, BigDecimal.ZERO, FiatCurrencyUnit.AED, "")
        ) {
            status { isBadRequest() }
        }

        mockMvc.post(
            mapper,
            CREATE_ORDER_URL,
            CreateOrderRequest(BigDecimal("0.1234567890123456789"), CurrencyUnit.ETH, BigDecimal.ZERO, FiatCurrencyUnit.AED, "")
        ) {
            status { isBadRequest() }
        }
    }*/

  /*  //@Test
    @WithMockUser(username = MERCHANT_NAME, roles = ["MERCHANT"])
    fun `Can create order if not too much decimal places`() {
        mockMvc.post(
            mapper,
            CREATE_ORDER_URL,
            CreateOrderRequest(BigDecimal("0.1234567800000000000000000000000"), CurrencyUnit.BTC, BigDecimal.ZERO, FiatCurrencyUnit.AED, "")
        ) {
            status { isCreated() }
        }

        mockMvc.post(
            mapper,
            CREATE_ORDER_URL,
            CreateOrderRequest(BigDecimal("0.123456789012345678000000000000000000"), CurrencyUnit.ETH, BigDecimal.ZERO, FiatCurrencyUnit.AED, "")
        ) {
            status { isCreated() }
        }
    }*/

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can't cancel a nonexistent order`() {
        val response = mockMvc.post(mapper, CANCEL_ORDER_URL, CancelOrderRequest(UUID.randomUUID())) { status { isNotFound() } }.response<ErrorResponse>(mapper)
        response.message shouldBe ErrorCodes.ORDER_NOT_FOUND
        response.status shouldBe HttpStatus.NOT_FOUND.value()
    }
/*
    //@Test
    @WithMockUser(username = EMAIL)
    fun `Can't cancel processed order`() {
        mockMvc.post(mapper, CREATE_FAKE_TRANSACTION, CreateFakeTransactionRequest(AMOUNT, ASSET))

        mockMvc.post(mapper, CREATE_TRANSACTION_FROM_ORDER_URL, CreateTransactionFromOrderRequest(order.uuid))
        val response = mockMvc.post(mapper, CANCEL_ORDER_URL, CancelOrderRequest(order.uuid)) { status { isUnprocessableEntity() } }.response<ErrorResponse>(mapper)
        response.message shouldBe ErrorCodes.PROCESSED_ORDER
        response.status shouldBe HttpStatus.UNPROCESSABLE_ENTITY.value()
    }*/

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can't cancel expired order`() {
        val response = mockMvc.post(mapper, CANCEL_ORDER_URL, CancelOrderRequest(expiredOrder.uuid)) { status { isUnprocessableEntity() } }.response<ErrorResponse>(mapper)
        response.message shouldBe ErrorCodes.EXPIRED_ORDER
        response.status shouldBe HttpStatus.UNPROCESSABLE_ENTITY.value()
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can't cancel cancelled order`() {
        mockMvc.post(mapper, CANCEL_ORDER_URL, CancelOrderRequest(order.uuid))

        val response = mockMvc.post(mapper, CANCEL_ORDER_URL, CancelOrderRequest(order.uuid)) { status { isUnprocessableEntity() } }.response<ErrorResponse>(mapper)
        response.message shouldBe ErrorCodes.CANCELLED_ORDER
        response.status shouldBe HttpStatus.UNPROCESSABLE_ENTITY.value()
    }

    // @Test
    @WithMockUser(username = EMAIL)
    fun `Can cancel open order`() {
        mockMvc.post(mapper, CANCEL_ORDER_URL, CancelOrderRequest(order.uuid))
    }

   /* //@Test
    fun `Can use both credentials to login`() {
        val credentials = mockMvc.post(mapper, CREATE_MERCHANT_URL, CREATE_MERCHANT_REQUEST.copy(name = MERCHANT_NAME_2)) { status { isCreated() } }.response<CreateMerchantResponse>(mapper)

        val credentials2 = mockMvc.post(mapper, ISSUE_NEW_CREDENTIALS_URL, null, listOf(Pair("Authorization", credentials.basicKey))) { status { isCreated() } }.response<CreateMerchantResponse>(mapper)

        mockMvc.post(
            mapper,
            CREATE_ORDER_URL,
            CreateOrderRequest(order.amount, order.currency, BigDecimal.ZERO, FiatCurrencyUnit.AED, ""), listOf(Pair("Authorization", credentials.basicKey))
        ) {
            status { isCreated() }
        }

        mockMvc.post(
            mapper,
            CREATE_ORDER_URL,
            CreateOrderRequest(order.amount, order.currency, BigDecimal.ZERO, FiatCurrencyUnit.AED, ""),
            listOf(Pair("Authorization", credentials2.basicKey))
        ) {
            status { isCreated() }
        }
    }
*/
    // @Test
    fun `Can invalidate credentials`() {
        val credentials = mockMvc.post(mapper, CREATE_MERCHANT_URL, CREATE_MERCHANT_REQUEST.copy(name = MERCHANT_NAME_2)) { status { isCreated() } }.response<CreateMerchantResponse>(mapper)

        mockMvc.delete(
            mapper,
            INVALIDATE_CREDENTIALS_URL,
            InvalidateApiKeyRequest(credentials.username),
            listOf(Pair("Authorization", credentials.basicKey))
        ) {
            status { isOk() }
        }

        mockMvc.post(
            mapper,
            CREATE_ORDER_URL,
            CreateOrderRequest(order.amount, order.currency, BigDecimal.ZERO, FiatCurrencyUnit.AED, ""),
            listOf(Pair("Authorization", credentials.basicKey))
        ) {
            status { isForbidden() }
        }
    }

    // @Test
    @WithMockUser(username = MERCHANT_NAME, roles = ["MERCHANT"])
    fun `Can't invalidate non-existing credentials`() {
        mockMvc.delete(mapper, INVALIDATE_CREDENTIALS_URL, InvalidateApiKeyRequest("invalid")) {
            status { isNotFound() }
        }
    }
}
