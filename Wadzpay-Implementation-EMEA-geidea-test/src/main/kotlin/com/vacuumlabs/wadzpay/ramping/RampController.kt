package com.vacuumlabs.wadzpay.ramping

import com.fasterxml.jackson.databind.ObjectMapper
import com.vacuumlabs.wadzpay.auth.HmacHelper
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.common.UnauthorizedException
import com.vacuumlabs.wadzpay.ledger.LedgerService
import com.vacuumlabs.wadzpay.user.UserAccountService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ramp")
@Tag(name = "Ramp")
class RampController(
    val userAccountService: UserAccountService,
    val ledgerService: LedgerService,
    val hmacHelper: HmacHelper,
    val objectMapper: ObjectMapper
) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/onrampOrder")
    @Operation(
        summary = "This endpoint is called via Onramper WebHook, when customer finishes buying crypto with Onramper widget. " +
            "Transaction with SUCCESSFUL status are created once money arrives to our BitGo wallet.",
        deprecated = true
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(
            responseCode = "401",
            description = ErrorCodes.INVALID_SIGNATURE,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun createOnrampOrder(@RequestBody onrampOrderString: String, @RequestHeader("X-Onramper-Webhook-Signature") signature: String) {
        logger.info("Received Onramper webhook: $onrampOrderString")

        if (!hmacHelper.verifySignature(onrampOrderString, signature)) {
            logger.info("Invalid webhook signature!")
            throw UnauthorizedException(ErrorCodes.INVALID_SIGNATURE)
        }
    }
}
