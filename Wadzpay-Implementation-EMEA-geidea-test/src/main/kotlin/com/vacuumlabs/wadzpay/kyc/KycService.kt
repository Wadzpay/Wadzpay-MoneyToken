package com.vacuumlabs.wadzpay.kyc

import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.kyc.models.JumioRequest
import com.vacuumlabs.wadzpay.kyc.models.JumioResponse
import com.vacuumlabs.wadzpay.user.UserAccount
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class KycService(
    @Qualifier("JUMIO") val restTemplate: RestTemplate,
    val jumioConfiguration: JUMIOConfiguration,
    val appConfig: AppConfig
) {
    fun createKycTransaction(userAccount: UserAccount): JumioResponse {
        val response = getKycUrl(userAccount)
        response.successUrl = jumioConfiguration.getSuccessURl(appConfig.environment)
        return response
    }

    fun getKycUrl(userAccount: UserAccount): JumioResponse {
        return try {

            return restTemplate.postForEntity(
                jumioConfiguration.url,
                JumioRequest(
                    userAccount.email!!,
                    userAccount.cognitoUsername,
                    jumioConfiguration.getCallBackURl(appConfig.environment),
                    jumioConfiguration.getSuccessURl(appConfig.environment)
                ),
                JumioResponse::class.java
            ).body ?: getKycUrl(userAccount)
        } catch (e: Exception) {
            LoggerFactory.getLogger(javaClass).info(e.message)
            getKycUrl(userAccount)
        }
    }
}
