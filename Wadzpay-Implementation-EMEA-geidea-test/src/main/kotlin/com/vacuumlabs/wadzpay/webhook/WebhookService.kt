package com.vacuumlabs.wadzpay.webhook

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.vacuumlabs.wadzpay.common.TriggerRetryException
import com.vacuumlabs.wadzpay.merchant.MerchantService
import com.vacuumlabs.wadzpay.merchant.model.Merchant
import com.vacuumlabs.wadzpay.merchant.model.Order
import com.vacuumlabs.wadzpay.merchant.model.OrderStatus
import com.vacuumlabs.wadzpay.merchant.model.OrderType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import java.util.UUID

@Service
class WebhookService(
    val restTemplate: RestTemplate,
    val orderWebhookRepository: OrderWebhookRepository,
    val orderWebhookLogRepository: OrderWebhookLogRepository,
    val merchantService: MerchantService
) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun createOrderWebhook(webhookOwner: Merchant, targetUrl: String): OrderWebhook {
        val webhook = orderWebhookRepository.findByWebhookOwner(webhookOwner)
        webhook?.webhookOwner = null

        return orderWebhookRepository.save(
            OrderWebhook(
                null,
                UUID.randomUUID(),
                webhookOwner,
                targetUrl
            )
        )
    }

    @Transactional
    fun triggerWebhook(order: Order) {
        try {
            retryTriggeringWebhook(order)
        } catch (e: TriggerRetryException) {
            logger.warn("Webhook could not be triggered! Reason: ${e.message}")
        }
    }

    @Retryable(
        value = [TriggerRetryException::class],
        maxAttemptsExpression = "\${retry-policy.retryCount}",
        backoff = Backoff(delayExpression = "\${retry-policy.retryInterval}")
    )
    fun retryTriggeringWebhook(order: Order) {
        val merchant = when (order.type) {
            OrderType.ORDER -> merchantService.findById(order.target!!.id)
            OrderType.WITHDRAWAL -> merchantService.findById(order.source!!.id)
        }
        val webhook = orderWebhookRepository.findByWebhookOwner(merchant)
        if (webhook != null) {
            var statusCode: HttpStatus? = null
            var responseBody: String? = null
            try {
                val entity = restTemplate.postForEntity(
                    webhook.targetUrl,
                    WebhookOrderEvent(
                        order.uuid,
                        order.externalOrderId,
                        order.status,
                        merchant.apiKey.find { it.valid }!!.apiKeySecretHash
                    ),
                    String::class.java
                )
                statusCode = entity.statusCode
                responseBody = entity.body
            } catch (clientException: HttpStatusCodeException) {
                statusCode = clientException.statusCode
                responseBody = clientException.responseBodyAsString
                throw TriggerRetryException(clientException.message)
            } catch (ex: Exception) {
                throw TriggerRetryException(ex.message)
            } finally {
                val count = orderWebhookLogRepository.countByOrder(order)

                orderWebhookLogRepository.save(
                    OrderWebhookLog(
                        webhook = webhook,
                        order = order,
                        responseStatus = statusCode,
                        responseBody = responseBody,
                        retryCount = count
                    )
                )
            }
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class WebhookOrderEvent(
    val orderId: UUID,
    val externalId: String?,
    val status: OrderStatus,
    val apiKeyHash: String
)
