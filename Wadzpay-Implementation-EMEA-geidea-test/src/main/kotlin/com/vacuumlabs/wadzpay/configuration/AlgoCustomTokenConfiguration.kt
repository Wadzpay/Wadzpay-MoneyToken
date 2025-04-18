package com.vacuumlabs.wadzpay.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "algocustomtoken")
class AlgoCustomTokenConfiguration {
    var baseUrl: String = ""
    var token: String = ""
    var sartokenid: String = ""
    var bcIndexerUrl: String = ""
    var algoApiVersion: String = ""
    var creatorAddress: String = ""
    var useJAVAAlgoSDK: Boolean = false
}
