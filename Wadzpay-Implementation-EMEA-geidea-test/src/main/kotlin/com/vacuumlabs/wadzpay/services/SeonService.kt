package com.vacuumlabs.wadzpay.services

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.FRAUDULENT_USER
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.UNKNOWN_SEON_ERROR
import com.vacuumlabs.wadzpay.common.ServiceUnavailableException
import com.vacuumlabs.wadzpay.common.UnprocessableEntityException
import com.vacuumlabs.wadzpay.configuration.SeonConfiguration
import com.vacuumlabs.wadzpay.utils.ApisLog
import com.vacuumlabs.wadzpay.utils.ApisLoggerRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

@Service
class SeonService(
    val seonConfiguration: SeonConfiguration,
    val apisLoggerRepository: ApisLoggerRepository,
    @Qualifier("Seon") val restTemplate: RestTemplate
) {

    val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Retryable(
        value = [Exception::class],
        maxAttemptsExpression = "\${retry-policy.retryCount}",
        backoff = Backoff(delayExpression = "\${retry-policy.retryInterval}")
    )
    private fun querySeon(requestBody: SeonRequestBody): SeonStatus {
        val restTemplate = RestTemplate()
        val headers = HttpHeaders()
        headers.set("X-Api-Key", seonConfiguration.apiKey)
        headers.set("Content-Type", "application/json")

        val request: HttpEntity<SeonRequestBody> = HttpEntity(requestBody, headers)
        val result: ResponseEntity<SeonResponse> = restTemplate.postForEntity<SeonResponse>(seonConfiguration.uri, request, SeonResponse::class.java)
        return SeonStatus.valueOf(result.body?.data?.state.toString())
    }

    fun checkStatus(
        phoneNumber: String,
        email: String,
        ipAddress: String
    ) {
        if (!seonConfiguration.enabled) return
        val status: SeonStatus
        val requestBodyTemp = SeonRequestBody(phoneNumber, email, ipAddress)
        try {
            val requestBody = SeonRequestBody(phoneNumber, email, ipAddress)
            status = querySeon(requestBody)
            logger.info(status.toString())
        } catch (e: Exception) {
            logger.error(e.message, e)
            apisLoggerRepository.save(
                ApisLog(
                    "SEON", e.message.toString(),
                    "$email $phoneNumber $ipAddress", requestBodyTemp.toString()
                )
            )
            throw ServiceUnavailableException(UNKNOWN_SEON_ERROR)
        }
        if (status === SeonStatus.DECLINE) {
            apisLoggerRepository.save(ApisLog("SEON", status.toString(), email, phoneNumber))
            throw UnprocessableEntityException(FRAUDULENT_USER)
        }
    }
}

// we get a lot of data in response that we don't need so we can just ignore it
@JsonIgnoreProperties(ignoreUnknown = true)
data class SeonResponse(
    val data: Data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Data(val state: String)
}

data class SeonRequestBody(
    @JsonProperty("phone_number")
    val phoneNumber: String,
    val email: String,
    @JsonProperty("ip")
    val ipAddress: String,
    @JsonProperty("config")
    val config: Config = Config(),
) {
    data class Config(
        @JsonProperty("phone_api")
        val phone_api: Boolean = true,
        @JsonProperty("email_api")
        val email_api: Boolean = true,
        @JsonProperty("ip_api")
        val ip_api: Boolean = true,
        @JsonProperty("device_fingerprinting")
        val device_fingerprinting: Boolean = true,
        @JsonProperty("ip")
        val ip: SeonIp = SeonIp(),
        @JsonProperty("email")
        val email: SeonEmail = SeonEmail(),
        @JsonProperty("phone")
        val phone: SeonPhone = SeonPhone()
    ) {
        data class SeonIp(
            @JsonProperty("version")
            val version: String = "v1.1",
            @JsonProperty("include")
            val include: String = "flags,history,id",
            @JsonProperty("flags_timeframe_days")
            val flags_timeframe_days: Int = 365
        )

        data class SeonEmail(
            @JsonProperty("version")
            val version: String = "v2.2",
            @JsonProperty("include")
            val include: String = "flags,history,id",
            @JsonProperty("flags_timeframe_days")
            val flags_timeframe_days: Int = 365
        )

        data class SeonPhone(
            @JsonProperty("version")
            val version: String = "v1.3",
            @JsonProperty("include")
            val include: String = "flags,history,id",
            @JsonProperty("flags_timeframe_days")
            val flags_timeframe_days: Int = 365
        )
    }
}

enum class SeonStatus {
    APPROVE, // Non-fraudulent
    REVIEW, // Suspicious score but not enough to be considered as fraudulent
    DECLINE, // Fraudulent
    NULL
}
