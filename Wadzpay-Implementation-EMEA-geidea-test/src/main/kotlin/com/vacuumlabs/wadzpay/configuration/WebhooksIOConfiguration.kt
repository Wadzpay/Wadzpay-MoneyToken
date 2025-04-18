package com.vacuumlabs.wadzpay.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "webhooks-io")
class WebhooksIOConfiguration(
    var baseUrl: String = "",
    var apiToken: String = "",
    var accountId: String = "",
    var bucketId: String = ""
)
