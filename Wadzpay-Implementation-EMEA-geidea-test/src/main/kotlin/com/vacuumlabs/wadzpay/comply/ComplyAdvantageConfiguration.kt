package com.vacuumlabs.wadzpay.comply

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "complyadvantage")
class ComplyAdvantageConfiguration {
    var api_key: String = ""
    var url: String = ""

    fun getUrlByEnvironment(env: String): String {
        return if (env.equals("prod", true)) {
            "https://wadzpay-qa.api.tm.complyadvantage.com/external/v2/transactions"
        } else {
            "https://$env.api.complyadvantage.com/users"
        }
    }
}
