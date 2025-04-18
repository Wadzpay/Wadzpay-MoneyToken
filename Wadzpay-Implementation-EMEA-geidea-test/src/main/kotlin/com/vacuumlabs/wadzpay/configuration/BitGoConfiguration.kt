package com.vacuumlabs.wadzpay.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "bitgo")
class BitGoConfiguration {
    var baseUrl: String = ""
    var token: String = ""
    var txWebhooks: List<String> = emptyList()
    var express: Express = Express()

    class Express {
        var baseUrl = ""
    }
}
