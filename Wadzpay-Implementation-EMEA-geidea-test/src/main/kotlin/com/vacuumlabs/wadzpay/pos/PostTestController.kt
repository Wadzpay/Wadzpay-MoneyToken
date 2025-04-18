package com.vacuumlabs.wadzpay.pos

import com.vacuumlabs.wadzpay.bitgo.WalletService
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.services.TwilioService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.security.Principal

@RestController
@RequestMapping("pos/")
@Tag(name = "posTest")
@Validated
@ConditionalOnProperty(
    prefix = "appconfig", name = ["production"], havingValue = "false"
)
class PostTestController(val posService: PosService, val walletService: WalletService, val twilioService: TwilioService) {

    @PostMapping(
        value = [
            "merchantDashboard/createFakeCommit"
        ]
    )
    @Operation(summary = "Create all Fake Transaction Commit")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Fake Commit created"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun createFakeCommit(
        principal: Principal,
        @RequestParam(required = true, name = "address") address: String,
        @RequestParam(required = true, name = "digitalCurrency") digitalCurrency: BigDecimal,
        @RequestParam(required = true, name = "digitalCurrencyType") digitalCurrencyType: CurrencyUnit,
    ): PosTransaction {
        if (digitalCurrency <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        return posService.fakePosTransaction(address, digitalCurrency, digitalCurrencyType)
    }

// Uncomment if you want to test APIs (Development purpose)
/*

@PostMapping(
        value = [
            "refundTest"
        ]
    )
    @Operation(summary = "Create all Fake Transaction Commit")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Fake Commit created"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun refundTest(
        @RequestParam(required = true, name = "address") address: String,
        @RequestParam(required = true, name = "digitalCurrency") digitalCurrency: BigDecimal,
        @RequestParam(required = true, name = "digitalCurrencyType") digitalCurrencyType: CurrencyUnit,
        @RequestParam(required = true, name = "refundFiatType") refundFiatType: FiatCurrencyUnit,
    ) {
         walletService.sendRefundToExternalWallet(
            refundAmountDigital = digitalCurrency,
            assetDigital = digitalCurrencyType,
            refundedToAddress = address,
            transactionType = TransactionType.REFUND, refundFiatType = refundFiatType
        )
    }


    @PostMapping(
        value = [
            "otpTest"
        ]
    )
    @Operation(summary = "Create all Fake Transaction Commit")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Fake Commit created"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun otpTest(
        @RequestParam(required = true, name = "mobileNo") mobileNo: String
    ) {
        twilioService.sendPhoneOTPCode(mobileNo)
    }

    @PostMapping(
        value = [
            "smsTest"
        ]
    )
    @Operation(summary = "Create all Fake Transaction Commit")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Fake Commit created"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun smsTest(
        @RequestParam(required = true, name = "mobileNo") mobileNo: String
    ) {
        twilioService.sendSms(mobileNo)
    }


*/

    @PostMapping(
        value = [
            "merchantDashboard/createFakeCommitSart"
        ]
    )
    @Operation(summary = "Create all Fake Transaction Commit")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Fake Commit created"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun createFakeCommitSart(
        principal: Principal,
        @RequestParam(required = true, name = "address") address: String,
        @RequestParam(required = true, name = "digitalCurrency") digitalCurrency: BigDecimal,
        @RequestParam(required = true, name = "digitalCurrencyType") digitalCurrencyType: CurrencyUnit,
    ): PosTransaction? {
        if (digitalCurrency <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        return null
        // posService.fakePosTransactionSart(null, address, digitalCurrency, digitalCurrencyType, null)
    }
}
