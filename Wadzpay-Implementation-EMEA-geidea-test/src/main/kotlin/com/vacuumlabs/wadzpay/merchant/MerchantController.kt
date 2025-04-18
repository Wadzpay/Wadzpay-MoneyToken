package com.vacuumlabs.wadzpay.merchant

import com.fasterxml.jackson.annotation.JsonFormat
import com.vacuumlabs.MERCHANT_API_VERSION
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.MERCHANT_ALREADY_EXISTS
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.WEBHOOK_PROVIDER_ERROR
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.LedgerService
import com.vacuumlabs.wadzpay.merchant.model.CountryCode
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.IndustryType
import com.vacuumlabs.wadzpay.merchant.model.Order
import com.vacuumlabs.wadzpay.merchant.model.OrderType
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.webhook.WebhookService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.security.Principal
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.Pattern
import javax.validation.constraints.Positive
import javax.validation.constraints.PositiveOrZero

@RestController
@RequestMapping("$MERCHANT_API_VERSION/merchant")
@Tag(name = "Merchant")
@Validated
class MerchantController(
    val orderService: OrderService,
    val merchantService: MerchantService,
    val webhookService: WebhookService,
    val ledgerService: LedgerService,
    val userAccountService: UserAccountService
) {

    @PostMapping("")
    @Operation(
        summary = "Create merchant",
        deprecated = true,
        description = "Endpoint will be removed once merchant can be created in merchant dashboard."
    )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Merchant created"),
        ApiResponse(
            responseCode = "409",
            description = MERCHANT_ALREADY_EXISTS,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun createMerchant(@RequestBody createMerchantRequest: CreateMerchantRequest): CreateMerchantResponse {
        val merchant = merchantService.createMerchant(createMerchantRequest)
        val merchantCredentials = merchantService.issueNewApiKey(merchant)
        return CreateMerchantResponse.fromCredentials(merchantCredentials)
    }

    @PostMapping("/order")
    @Operation(summary = "Create order by merchant")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Order created"),
        ApiResponse(
            responseCode = "400",
            description = "${ErrorCodes.INVALID_AMOUNT_TOO_MANY_DECIMAL_PLACES}, ${ErrorCodes.INVALID_EMAIL} , ${ErrorCodes.DUPLICATE_EXTERNAL_TRX_ID}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrder(@Valid @RequestBody request: CreateOrderRequest, principal: Principal): Order {

        LoggerFactory.getLogger(javaClass).info("CreateOrderRequest:: " + request.toString())

        val merchant = merchantService.getMerchantByName(principal.name)

        val order = if (request.isThirdParty) {
            orderService.createOrderThirdParty(
                cryptoType = request.currency,
                type = request.type,
                fiatAmount = request.fiatAmount,
                faitType = request.fiatCurrency,
                target = if (request.type == OrderType.ORDER) merchant else null,
                targetEmail = request.targetEmail,
                externalOrderId = request.externalOrderId,
                description = request.description,
                principal
            )
        } else {
            orderService.createOrder(

                currency = request.currency,
                type = request.type,
                fiatAmount = request.fiatAmount,
                fiatCurrency = request.fiatCurrency,
                source = if (request.type == OrderType.WITHDRAWAL) merchant else null,
                target = if (request.type == OrderType.ORDER) merchant else null,
                targetEmail = request.targetEmail,
                externalOrderId = request.externalOrderId,
                description = request.description
            )
        }

        orderService.triggerWebhookIfOrderExpires(order.uuid)
        return order
    }

    @PostMapping("/webhook")
    @Operation(summary = "Create order webhook for current merchant")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Webhook created"),
        ApiResponse(
            responseCode = "503",
            description = WEBHOOK_PROVIDER_ERROR,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrderWebhook(@RequestBody request: CreateOrderWebhookRequest, principal: Principal) {
        val merchant = merchantService.getMerchantByName(principal.name)
        webhookService.createOrderWebhook(merchant, request.targetUrl)
    }
}

data class FakeCommitRequestBody(val address: String, val amount: BigDecimal)
data class FakeEmailSMSRequestBody(val email: String, val mobile: String)

data class InvalidateApiKeyRequest(
    val apiKeyId: String
)

data class CreateOrderRequest(
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @field: Positive
    val amount: BigDecimal,
    val currency: CurrencyUnit,

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @field: PositiveOrZero
    /**
     * Equivalent of amount in FIAT promised to customer.
     *
     * Exchanges rates are up to merchants since we have no control over their systems.
     * */
    val fiatAmount: BigDecimal,
    val fiatCurrency: FiatCurrencyUnit,

    val externalOrderId: String,
    val description: String? = null,
    @field: Email(message = ErrorCodes.INVALID_EMAIL)
    val targetEmail: String? = null,

    val type: OrderType = OrderType.ORDER,

    val isThirdParty: Boolean = false
)

data class TestWebhookRequest(
    val orderId: UUID
)

data class CreateOrderWebhookRequest(
    val targetUrl: String
)

data class CreateMerchantRequest(
    val name: String,
    val countryOfRegistration: CountryCode,
    val registrationCode: String,
    val primaryContactFullName: String,
    @field: Email(message = ErrorCodes.INVALID_EMAIL)
    val primaryContactEmail: String,
    @field: Pattern(
        regexp = "^\\+[0-9]{7,15}\$",
        message = ErrorCodes.INVALID_PHONE_NUMBER
    )
    val primaryContactPhoneNumber: String,
    val companyType: String?,
    val industryType: IndustryType?,
    val merchantId: String?
)

data class CreateMerchantResponse(val username: String, val password: String, val basicKey: String) {
    companion object {
        fun fromCredentials(credentials: MerchantCredentials): CreateMerchantResponse {
            return CreateMerchantResponse(credentials.apiKeyId, credentials.apiKeySecret, credentials.encode())
        }
    }
}

data class MerchantTimeZoneRequest(
    val userName: String,
    val primaryContactEmail: String,
    val countryId: CountryCode,
    val timeZone: String
)
