package com.vacuumlabs.wadzpay.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "encryptionsaltkey")
class EncryptionKeyConfiguration {
    var version1: String = ""
    var version2: String = ""
    var version3: String = ""
}
