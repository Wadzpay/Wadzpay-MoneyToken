package com.vacuumlabs.wadzpay.paymentPoc

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "vepay")
class VePayConfiguration {
    var base_url: String = ""
    var merchant_ID: String = ""
    var merchant_key: String = ""
    var api_key: String = ""
}
