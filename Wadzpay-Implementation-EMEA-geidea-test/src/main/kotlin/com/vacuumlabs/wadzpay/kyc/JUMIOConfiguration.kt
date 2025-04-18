package com.vacuumlabs.wadzpay.kyc

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "jumio")
class JUMIOConfiguration {
    var apiToken: String = ""
    var secret: String = ""
    var url: String = ""

    fun getCallBackURl(env: String): String {
        return if (env.equals("prod", true)) {
            "https://api.wadzpay.com/webhook/jumio"
        } else {
            "https://api.$env.wadzpay.com/webhook/jumio"
        }
    }

    fun getSuccessURl(env: String): String {
        return if (env.equals("prod", true)) {
            "https://api.wadzpay.com/webhook/jumio/success"
        } else {
            "https://api.$env.wadzpay.com/webhook/jumio/success"
        }
        // return "https://webhook.site/fc4d48af-c4e0-49cb-93d1-9c83d77ec50e"
    }
}
