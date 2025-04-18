package com.vacuumlabs.wadzpay

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.merchant.CreateMerchantRequest
import com.vacuumlabs.wadzpay.merchant.model.CountryCode
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.IndustryType
import com.vacuumlabs.wadzpay.utils.CleanerService
import com.vacuumlabs.wadzpay.webhook.BitGoCoin
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.result.StatusResultMatchersDsl
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import java.math.BigDecimal
import java.util.UUID

fun MockMvc.post(
    objectMapper: ObjectMapper,
    url: String,
    request: Any? = null,
    headers: List<Pair<String, String>>? = null,
    expect: MockMvcResultMatchersDsl.() -> Unit = { status { isOk() } }
): MockHttpServletResponse {
    return post(url) {
        if (request != null) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
        headers?.forEach { header(it.first, it.second) }
        accept = MediaType.APPLICATION_JSON
    }.andExpect(expect).andReturn().response
}

fun MockMvc.put(
    objectMapper: ObjectMapper,
    url: String,
    request: Any? = null,
    headers: List<Pair<String, String>>? = null,
    expect: MockMvcResultMatchersDsl.() -> Unit = { status { isOk() } }
): MockHttpServletResponse {
    return put(url) {
        if (request != null) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
        headers?.forEach { header(it.first, it.second) }
        accept = MediaType.APPLICATION_JSON
    }.andExpect(expect).andReturn().response
}

fun MockMvc.patch(
    objectMapper: ObjectMapper,
    url: String,
    request: Any? = null,
    headers: List<Pair<String, String>>? = null,
    expect: MockMvcResultMatchersDsl.() -> Unit = { status { isOk() } }
): MockHttpServletResponse {
    return patch(url) {
        if (request != null) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
        headers?.forEach { header(it.first, it.second) }
        accept = MediaType.APPLICATION_JSON
    }.andExpect(expect).andReturn().response
}

fun MockMvc.get(
    objectMapper: ObjectMapper,
    url: String,
    expectedBody: Any? = null,
    headers: List<Pair<String, String>>? = null,
    expectedStatus: StatusResultMatchersDsl.() -> Unit = { isOk() }
): MockHttpServletResponse {
    return get(url) {
        headers?.forEach { header(it.first, it.second) }
        accept = MediaType.APPLICATION_JSON
    }.andExpect {
        status(expectedStatus)
        if (expectedBody != null) {
            content { json(objectMapper.writeValueAsString(expectedBody)) }
        }
    }.andReturn().response
}

fun MockMvc.delete(
    objectMapper: ObjectMapper,
    url: String,
    request: Any? = null,
    headers: List<Pair<String, String> >? = null,
    expect: MockMvcResultMatchersDsl.() -> Unit = { status { isOk() } }
): MockHttpServletResponse {
    return delete(url) {
        if (request != null) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
        headers?.forEach { header(it.first, it.second) }
        accept = MediaType.APPLICATION_JSON
    }.andExpect(expect).andReturn().response
}

inline fun <reified T> MockHttpServletResponse.response(mapper: ObjectMapper): T {
    return mapper.readValue(contentAsString)
}

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = [IntegrationTests.Initializer::class])
abstract class IntegrationTests {
    @field:Autowired
    protected lateinit var databaseCleanupService: CleanerService

    companion object {
        const val REGISTER_PHONE_URL = "/user/registration/phone"
        const val VERIFY_PHONE_URL = "/user/registration/phone/verify"
        const val DETAILS_URL = "/user/registration/details"
        const val VERIFY_AND_CREATE_URL = "/user/registration/verify-and-create"
        const val USER_BALANCES_URL = "/user/balances"
        const val USER_TRANSACTIONS_URL = "/user/transactions"
        const val CANCEL_ORDER_URL = "/user/order/cancel"
        const val BLOCKCHAIN_TX_ID = "0xa1075db55d416d3ca199f55b6084e2115b9345e16c5cf302fc80e9d5fbf5d48d"
        val BLOCKCHAIN_TX_ID_NORMALIZED = BLOCKCHAIN_TX_ID.removePrefix("0x")
        val TX_ID = UUID.randomUUID().toString()
        val ONRAMP_FIAT_AMOUNT = BigDecimal("23.45")
        val ONRAMP_FIAT_CURRENCY = FiatCurrencyUnit.EUR

        const val CREATE_TRANSACTION_FROM_ORDER_URL = "/user/account/create_transaction_from_order"
        const val CREATE_FAKE_TRANSACTION = "/addFakeTransaction"
        const val API_VERSION = "/v1"
        const val TRIGGER_WEBHOOK_URL = "$API_VERSION/merchant/test-webhook"
        const val CREATE_MERCHANT_URL = "$API_VERSION/merchant"
        const val CREATE_ORDER_URL = "$API_VERSION/merchant/order"
        const val GET_ORDER_URL = "$API_VERSION/merchant/order"
        const val MERCHANT_BALANCES_URL = "$API_VERSION/merchant/balances"
        const val MERCHANT_TRANSACTIONS_URL = "$API_VERSION/merchant/transactions"
        const val ISSUE_NEW_CREDENTIALS_URL = "$API_VERSION/merchant/api-key"
        const val INVALIDATE_CREDENTIALS_URL = "$API_VERSION/merchant/api-key"
        const val MERCHANT_NAME = "MERCHANT_NAME"

        const val COGNITO_USERNAME = "123456789"
        const val EMAIL = "email@domain.com"
        const val PHONE_NUMBER = "+421944112233"

        const val IP_ADDRESS = "127.0.0.1"
        const val PASSWORD = "password123@"
        const val CORRECT_CODE = "123456"
        const val VERIFICATION_SID = "VEXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"

        const val BIT_GO_TRANSFER_ID = "12345"
        val BIT_GO_COIN = BitGoCoin.btc
        val ONRAMP_AMOUNT = BigDecimal("1.2345")
        val ONRAMP_CURRENCY = CurrencyUnit.BTC

        const val USER_SEND_EXTERNAL = "/user/account/sendDigitalCurrencyToExternalWallet"
        val OFFRAMP_AMOUNT = BigDecimal.ONE
        val OFFRAMP_CURRENCY = CurrencyUnit.BTC
        val OFFRAMP_FEE = BigDecimal("0.01")
        const val OFFRAMP_RECEIVER_ADDRESS = "0xa11234555d416d3ca199f55b6084e2115b9345e16c5cf302fc80e9d5fbf5d48d"
        const val OFFRAMP_DESCRIPTION = "description"

        val CREATE_MERCHANT_REQUEST = CreateMerchantRequest(MERCHANT_NAME, CountryCode.IN, "12345", "John Doe", "name@email.com", "12345", null, IndustryType.OTHER_INDUSTRY, null)

        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:13").apply {
            withDatabaseName("postgres")
            withUsername("postgres")
            withPassword("password")
        }

        val redisContainer = GenericContainer<Nothing>("redis:6")
            .apply { withExposedPorts(6379) }
    }

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            postgresContainer.start()
            redisContainer.start()

            TestPropertyValues.of(
                "spring.datasource.url=${postgresContainer.jdbcUrl}",
                "spring.datasource.password=${postgresContainer.password}",
                "spring.datasource.username=${postgresContainer.username}",
                "spring.redis.hostName=${redisContainer.containerIpAddress}",
                "spring.redis.port=${redisContainer.firstMappedPort}"
            ).applyTo(configurableApplicationContext.environment)
        }
    }
}
