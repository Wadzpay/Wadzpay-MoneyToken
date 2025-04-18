package com.vacuumlabs.wadzpay.gap600

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

// This class has Gap600 Configuration settings
@Configuration
@ConfigurationProperties(prefix = "gap600")
class Gap600Configuration {
    var apikey: String = ""
    var agentid: String = ""
    var url: String = ""
}
