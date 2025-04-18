package com.vacuumlabs.wadzpay.comply

import com.vacuumlabs.wadzpay.comply.models.ComplyAdvantageRequest
import com.vacuumlabs.wadzpay.comply.models.ComplyAdvantageResponse
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.utils.ApisLog
import com.vacuumlabs.wadzpay.utils.ApisLoggerRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@Service
class ComplyAdvantageService(

    @Qualifier("COMPLYADVANTAGE") val restTemplate: RestTemplate,
    val complyAdvantageConfiguration: ComplyAdvantageConfiguration,
    val appConfig: AppConfig,
    val apisLoggerRepository: ApisLoggerRepository
) {

    fun complyAdvanceTransaction(complyAdvantageRequest: ComplyAdvantageRequest): ComplyAdvantageResponse? {

        try {
            val complyAdvantageResponse = restTemplate.postForEntity(
                complyAdvantageConfiguration.url,
                complyAdvantageRequest,
                ComplyAdvantageResponse::class.java
            ).body ?: complyAdvanceTransaction(complyAdvantageRequest)

            apisLoggerRepository.save(ApisLog("ComplyAdvantage", "$complyAdvantageRequest", complyAdvantageRequest.data.tx_id, complyAdvantageResponse.toString()))
            return complyAdvantageResponse
        } catch (e: RestClientResponseException) {
            apisLoggerRepository.save(ApisLog("ComplyAdvantageError", "$complyAdvantageRequest", complyAdvantageRequest.data.tx_id, e.message.toString()))
        } catch (exception: Exception) {
            apisLoggerRepository.save(ApisLog("ComplyAdvantageError", "$complyAdvantageRequest", complyAdvantageRequest.data.tx_id, exception.message.toString()))
        }
        return null
    }
}
