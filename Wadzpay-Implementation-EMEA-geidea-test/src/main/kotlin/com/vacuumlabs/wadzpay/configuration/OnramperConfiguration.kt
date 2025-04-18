package com.vacuumlabs.wadzpay.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "onramper")
class OnramperConfiguration {
    var secret: String = ""
    var hashingAlgorithm: String = ""
}
