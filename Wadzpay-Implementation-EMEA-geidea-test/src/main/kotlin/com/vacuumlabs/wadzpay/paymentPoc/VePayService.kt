package com.vacuumlabs.wadzpay.paymentPoc

import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPOCRefundRequest
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPOCRefundResponse
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPOCRequest
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPOCResponse
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPaymentStatusCheckRequest
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPaymentStatusCheckResponse
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPaymentTransferRequest
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPaymentTransferResponse
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPayoutStatusCheckRequest
import com.vacuumlabs.wadzpay.utils.ApisLog
import com.vacuumlabs.wadzpay.utils.ApisLoggerRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@Service
class VePayService(
    @Qualifier("VEPAY") val restTemplate: RestTemplate,
    val vePayConfiguration: VePayConfiguration,
    val appConfig: AppConfig,
    val apisLoggerRepository: ApisLoggerRepository
) {

    fun vePayPaymentIntent(vePayPOCRequest: VePayPOCRequest): VePayPOCResponse? {
        // println("vePayPOCRequest==> ${vePayPOCRequest.toString()}")
        try {
            val vePayResponse = restTemplate.postForEntity(
                vePayConfiguration.base_url + "/api/payment/intents",
                vePayPOCRequest,
                VePayPOCResponse::class.java
            ).body ?: vePayPaymentIntent(vePayPOCRequest)
            apisLoggerRepository.save(
                ApisLog(
                    "VePayPaymentPOC",
                    "$vePayPOCRequest",
                    vePayPOCRequest.desc.toString(),
                    vePayResponse.toString()
                )
            )
            // println("vePayResponse success==> ${vePayResponse.toString()}")
            return vePayResponse
        } catch (e: RestClientResponseException) {
            println("VePayPaymentPOCError failed==> $e")
            apisLoggerRepository.save(
                ApisLog(
                    "VePayPaymentPOCError",
                    "$vePayPOCRequest",
                    vePayPOCRequest.desc.toString(),
                    e.message.toString()
                )
            )
        } catch (e: Exception) {
            println("VePayPaymentPOCError failed==> $e")
            apisLoggerRepository.save(
                ApisLog(
                    "VePayPaymentPOCError",
                    "$vePayPOCRequest",
                    vePayPOCRequest.desc.toString(),
                    e.message.toString()
                )
            )
        }
        return null
    }

    fun vePayPaymentTransfer(vePayPaymentTransferRequest: VePayPaymentTransferRequest): VePayPaymentTransferResponse? {
        try {
            val vePayPaymentTransferResponse = restTemplate.postForEntity(
                vePayConfiguration.base_url + "/api/pay/payout/transfer",
                vePayPaymentTransferRequest,
                VePayPaymentTransferResponse::class.java
            ).body ?: vePayPaymentTransfer(vePayPaymentTransferRequest)
            apisLoggerRepository.save(
                ApisLog(
                    "VePayPaymentTransfer",
                    "$vePayPaymentTransferRequest",
                    vePayPaymentTransferRequest.reference_id.toString(),
                    vePayPaymentTransferResponse.toString()
                )
            )
            return vePayPaymentTransferResponse
        } catch (e: RestClientResponseException) {
            apisLoggerRepository.save(
                ApisLog(
                    "VePayPaymentTransferError",
                    "$vePayPaymentTransferRequest",
                    vePayPaymentTransferRequest.reference_id.toString(),
                    e.message.toString()
                )
            )
        } catch (exception: Exception) {
            apisLoggerRepository.save(
                ApisLog(
                    "VePayPaymentTransferError",
                    "$vePayPaymentTransferRequest",
                    vePayPaymentTransferRequest.reference_id.toString(),
                    exception.message.toString()
                )
            )
        }
        return null
    }

    fun vePayPaymentStatusCheck(vePayPaymentStatusCheckRequest: VePayPaymentStatusCheckRequest): VePayPaymentStatusCheckResponse? {
        try {
            val vePayPaymentStatusCheckResponse = restTemplate.postForEntity(
                vePayConfiguration.base_url + "/api/intent/check/status",
                vePayPaymentStatusCheckRequest,
                VePayPaymentStatusCheckResponse::class.java
            ).body ?: vePayPaymentStatusCheck(vePayPaymentStatusCheckRequest)
            println(vePayPaymentStatusCheckResponse)
            apisLoggerRepository.save(
                ApisLog(
                    "VePayPaymentStatusCheck",
                    "$vePayPaymentStatusCheckRequest",
                    vePayPaymentStatusCheckRequest.identifier.toString(),
                    vePayPaymentStatusCheckResponse.toString()
                )
            )
            return vePayPaymentStatusCheckResponse
        } catch (e: RestClientResponseException) {
            apisLoggerRepository.save(
                ApisLog(
                    "VePayPaymentStatusCheckError",
                    "$vePayPaymentStatusCheckRequest",
                    vePayPaymentStatusCheckRequest.identifier.toString(),
                    e.message.toString()
                )
            )
        } catch (exception: Exception) {
            apisLoggerRepository.save(
                ApisLog(
                    "VePayPaymentStatusCheckError",
                    "$vePayPaymentStatusCheckRequest",
                    vePayPaymentStatusCheckRequest.identifier.toString(),
                    exception.message.toString()
                )
            )
        }
        return null
    }

    fun vePayPayoutStatusCheck(vePayPayoutStatusCheckRequest: VePayPayoutStatusCheckRequest): VePayPaymentTransferResponse? {
        try {
            val vePayPaymentTransferResponse = restTemplate.postForEntity(
                vePayConfiguration.base_url + "/api/pay/payout/status" + vePayPayoutStatusCheckRequest.transactionId,
                "",
                VePayPaymentTransferResponse::class.java
            ).body ?: vePayPayoutStatusCheck(vePayPayoutStatusCheckRequest)
            println(vePayPaymentTransferResponse)
            apisLoggerRepository.save(
                ApisLog(
                    "VePayPayoutStatusCheck",
                    "$vePayPayoutStatusCheckRequest",
                    vePayPayoutStatusCheckRequest.transactionId.toString(),
                    vePayPaymentTransferResponse.toString()
                )
            )
            return vePayPaymentTransferResponse
        } catch (e: RestClientResponseException) {
            apisLoggerRepository.save(
                ApisLog(
                    "VePayPayoutStatusCheckError",
                    "$vePayPayoutStatusCheckRequest",
                    vePayPayoutStatusCheckRequest.transactionId.toString(),
                    e.message.toString()
                )
            )
        } catch (exception: Exception) {
            apisLoggerRepository.save(
                ApisLog(
                    "VePayPayoutStatusCheckError",
                    "$vePayPayoutStatusCheckRequest",
                    vePayPayoutStatusCheckRequest.transactionId.toString(),
                    exception.message.toString()
                )
            )
        }
        return null
    }

    fun vePayRefundFiat(vePayPOCRefundRequest: VePayPOCRefundRequest): VePayPOCRefundResponse? {
        try {
            val vePayPOCRefundResponse = restTemplate.postForEntity(
                vePayConfiguration.base_url + "/api/intent/check/status",
                vePayPOCRefundRequest,
                VePayPOCRefundResponse::class.java
            ).body ?: vePayRefundFiat(vePayPOCRefundRequest)
            println(vePayPOCRefundResponse)
            apisLoggerRepository.save(
                ApisLog(
                    "vePayRefundFiat",
                    "$vePayPOCRefundRequest",
                    vePayPOCRefundResponse.toString(),
                    vePayPOCRefundResponse.toString()
                )
            )
            return vePayPOCRefundResponse
        } catch (e: RestClientResponseException) {
            apisLoggerRepository.save(
                ApisLog(
                    "vePayRefundFiatError",
                    "$vePayPOCRefundRequest",
                    vePayPOCRefundRequest.toString(),
                    e.message.toString()
                )
            )
        } catch (exception: Exception) {
            apisLoggerRepository.save(
                ApisLog(
                    "vePayRefundFiatError",
                    "$vePayPOCRefundRequest",
                    vePayPOCRefundRequest.toString(),
                    exception.message.toString()
                )
            )
        }
        return null
    }
}
