package com.vacuumlabs.wadzpay.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "seon")
data class SeonConfiguration(
    var uri: String = "",
    var apiKey: String = "",
    var enabled: Boolean = true
)
