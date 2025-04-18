package com.vacuumlabs.wadzpay.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "twilio")
data class TwilioConfiguration(
    val accountSid: String,
    val authToken: String,
    val verifyServiceSid: String,
)
