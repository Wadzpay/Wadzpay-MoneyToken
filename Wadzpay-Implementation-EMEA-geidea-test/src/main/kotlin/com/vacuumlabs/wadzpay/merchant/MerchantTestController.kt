package com.vacuumlabs.wadzpay.merchant

import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.merchant.model.Order
import com.vacuumlabs.wadzpay.webhook.WebhookService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("v1/merchant")
@Tag(name = "merchantTest")
@Validated
@ConditionalOnProperty(
    prefix = "appconfig", name = ["production"], havingValue = "false"
)
class MerchantTestController(
    val orderService: OrderService,
    val webhookService: WebhookService
) {
    @PostMapping("/test-webhook")
    @Operation(summary = "Triggers a webhook on provided order id")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Webhook triggered"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.ORDER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun testWebhook(@RequestBody request: TestWebhookRequest) {
        val order = orderService.getOrderByUuid(request.orderId)
        webhookService.triggerWebhook(order)
    }

    @PostMapping("/FakeCommitOrder")
    @Operation(summary = "Fake Commit Orders")
    fun fakeCommitOrder(@RequestBody fakeCommitRequestBody: FakeCommitRequestBody): Order? {
        return orderService.processThirdPartyOrderFake(fakeCommitRequestBody.address, fakeCommitRequestBody.amount)
    }
}
