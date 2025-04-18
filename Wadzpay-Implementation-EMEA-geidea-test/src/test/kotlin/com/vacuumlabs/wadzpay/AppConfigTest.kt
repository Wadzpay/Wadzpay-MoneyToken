package com.vacuumlabs.wadzpay

import com.fasterxml.jackson.databind.ObjectMapper
import com.vacuumlabs.wadzpay.configuration.ConfigResponse
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc

@AutoConfigureMockMvc
class AppConfigTest @Autowired constructor(
    var mapper: ObjectMapper,
    val mockMvc: MockMvc
) : IntegrationTests() {

    companion object {
        const val USER_EMAIL = "email@domain.com"
        private const val API_VERSION = "/v1"
        const val APP_CONFIG_URL = "$API_VERSION/config"
    }

    // @Test
    @WithMockUser(username = USER_EMAIL)
    fun `App config is valid`() {
        val config = mockMvc.get(mapper, APP_CONFIG_URL).response<ConfigResponse>(mapper)

        config.onRampApiKey shouldNotBe null
        config.digitalCurrencyList.size shouldBe 8
        config.digitalCurrencyList[0].code shouldBe "WTK"
        config.digitalCurrencyList[0].inwardAddress shouldNotBe null
        config.digitalCurrencyList[0].outwardAddress shouldNotBe null
        config.digitalCurrencyList[1].code shouldBe "BTC"
        config.digitalCurrencyList[1].inwardAddress shouldNotBe null
        config.digitalCurrencyList[1].outwardAddress shouldNotBe null
        config.digitalCurrencyList[2].code shouldBe "ETH"
        config.digitalCurrencyList[2].inwardAddress shouldNotBe null
        config.digitalCurrencyList[2].outwardAddress shouldNotBe null
        config.digitalCurrencyList[3].code shouldBe "USDC"
        config.digitalCurrencyList[3].inwardAddress shouldNotBe null
        config.digitalCurrencyList[3].outwardAddress shouldNotBe null
        config.digitalCurrencyList[4].code shouldBe "USDT"
        config.digitalCurrencyList[4].inwardAddress shouldNotBe null
        config.digitalCurrencyList[4].outwardAddress shouldNotBe null
        config.digitalCurrencyList[5].code shouldBe "ALGO"
        config.digitalCurrencyList[5].inwardAddress shouldNotBe null
        config.digitalCurrencyList[5].outwardAddress shouldNotBe null
        config.digitalCurrencyList[6].code shouldBe "USDCA"
        config.digitalCurrencyList[6].inwardAddress shouldNotBe null
        config.digitalCurrencyList[6].outwardAddress shouldNotBe null
        config.digitalCurrencyList[7].code shouldBe "SART"
        config.digitalCurrencyList[7].inwardAddress shouldNotBe null
        config.digitalCurrencyList[7].outwardAddress shouldNotBe null
    }
}
