package com.vacuumlabs.wadzpay.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "ssl")
class SslConfiguration {
    var keystoreType = ""
    var trustStorePath: String = ""
    var trustStorePassword: CharArray = charArrayOf()
    var protocol: String = "TLSv1.2"
}
