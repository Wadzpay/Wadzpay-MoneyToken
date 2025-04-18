package com.vacuumlabs.wadzpay.gap600

import com.fasterxml.jackson.databind.ObjectMapper
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.configuration.HeaderModifierInterceptor
import com.vacuumlabs.wadzpay.gap600.models.Gap600ConfirmationResponse
import com.vacuumlabs.wadzpay.gap600.models.Gap600FailedResponse
import com.vacuumlabs.wadzpay.gap600.models.Gap600Message
import com.vacuumlabs.wadzpay.utils.ApisLog
import com.vacuumlabs.wadzpay.utils.ApisLoggerRepository
import com.vacuumlabs.wadzpay.utils.BlockConfirmationRepository
import com.vacuumlabs.wadzpay.webhook.BitGoCoin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.SocketException
import java.util.concurrent.CompletableFuture

// This is class will handle API request for Gap600 confirmation status
@Component
class Gap600Cmpnt(
    @Qualifier("Gap600") val restTemplate: RestTemplate,
    val gap600Configuration: Gap600Configuration,
    val appConfig: AppConfig,
    val blockConfirmationRepository: BlockConfirmationRepository,
    val apisLoggerRepository: ApisLoggerRepository,
    val objectMap: ObjectMapper

) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    // This method will call the Gap600 API
    // required parameters : CoinType, TransactionHash and OutputAddress of the transaction
    // returns : Gap600Message with confirmation status and confirmation source details
    @Retryable(
        value = [SocketException::class],
        maxAttemptsExpression = "\${retry-policy.retryCount}",
        backoff = Backoff(delayExpression = "\${retry-policy.retryInterval}")
    )
    @Async
    fun findGap600TransactionStatus(
        coin: BitGoCoin,
        transactionHash: String,
        xoutputaddress: String,
        retryEnabled: Boolean = true
    ): CompletableFuture<Gap600Message>? {

        val cp = CompletableFuture<Gap600Message>()
        var testpathsegment = ""

        if (appConfig.environment.equals("dev", true) || appConfig.environment.equals("test", true)) {
            testpathsegment = "test"
        }
        restTemplate.interceptors.clear()
        restTemplate.interceptors.add(HeaderModifierInterceptor("X-Api-Key", gap600Configuration.apikey))
        restTemplate.interceptors.add(HeaderModifierInterceptor("x-agent-id", gap600Configuration.agentid))
        restTemplate.interceptors.add(HeaderModifierInterceptor("x-output-address", xoutputaddress))
        restTemplate.requestFactory = HttpComponentsClientHttpRequestFactory()

        val start: Long = System.currentTimeMillis()
        val responseEntity: ResponseEntity<Any> = restTemplate.getForEntity<Any>(
            UriComponentsBuilder.fromHttpUrl(gap600Configuration.url)
                .path("/v2")
                .pathSegment("btc")
                .pathSegment("$testpathsegment")
                .pathSegment("tx")
                .pathSegment(transactionHash)
                .build()
                .toUri(),
            Any::class.java
        )
        val end: Long = System.currentTimeMillis()
        logger.info("gap600start : $start, gap600end : $end, responseTime : ${end - start}")
        apisLoggerRepository.save(
            ApisLog(
                "Gap600", responseEntity.toString(), "Transaction Hash:- $transactionHash",
                "ResponseTime:- ${end - start}"
            )
        )
        var json = ""
        if (responseEntity.statusCode == HttpStatus.OK) {
            json = ObjectMapper().writeValueAsString(responseEntity.body)
            logger.info("gap 600 output : $json")
            try {

                val gap600ConfirmationResponse = objectMap.readValue<Gap600ConfirmationResponse>(
                    json,
                    Gap600ConfirmationResponse::class.java
                )

                cp.complete(gap600ConfirmationResponse!!.message)
                return cp
            } catch (e: Exception) {
                logger.info("inside Exception case ")
                val gap600FailedResponse = objectMap.readValue<Gap600FailedResponse>(
                    json,
                    Gap600FailedResponse::class.java
                )
                logger.info("Gap600 Error Message :- " + gap600FailedResponse.toString())

                if (gap600FailedResponse.type.equals("OutputAddressNotFoundError")) {
                    // retry gap600 after 4 sec
                    if (retryEnabled) {
                        logger.info("Will Retry in 4 sec")
                        Thread.sleep(4000)
                        logger.info("Retrying after 4 sec")
                        findGap600TransactionStatus(coin, transactionHash, xoutputaddress, retryEnabled = false)
                    }
                }
                // logger.info("gap600 Response Conversion Error :" +e.message)

                apisLoggerRepository.save(
                    ApisLog(
                        "Gap600",
                        "Gap600 Error Message :- " + gap600FailedResponse.toString(),
                        "Transaction Hash:- $transactionHash",
                        gap600FailedResponse.toString()
                    )
                )

                logger.error("Error while Converting Response " + e.message, e)
                cp.complete(null)
                return cp
            }
        } else {
            logger.info("inside else case ")
            val gap600FailedResponse = objectMap.readValue<Gap600FailedResponse>(
                json,
                Gap600FailedResponse::class.java
            )
            logger.info("Exception case :" + gap600FailedResponse.toString())
            // logger.info("gap600 Response Conversion Error :" +e.message)

            apisLoggerRepository.save(
                ApisLog(
                    "Gap600",
                    "Gap600 Error FOR :-  $transactionHash",
                    "Gap600-Error:- " + gap600FailedResponse.toString(),
                    gap600FailedResponse.toString()
                )
            )
            cp.complete(null)
            return cp
        }
    }
}
