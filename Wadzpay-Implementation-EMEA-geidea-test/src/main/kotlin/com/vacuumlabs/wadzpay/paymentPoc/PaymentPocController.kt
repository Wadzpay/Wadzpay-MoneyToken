package com.vacuumlabs.wadzpay.paymentPoc

import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPOCRefundResponse
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPaymentStatusCheckResponse
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPaymentTransferResponse
import com.vacuumlabs.wadzpay.user.UserAccountService
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
import java.math.BigDecimal
import java.security.Principal

@RestController
@RequestMapping
@ConditionalOnProperty(
    prefix = "appconfig", name = ["production"], havingValue = "false"
)
@Tag(name = "Payment POC")
@Validated
class PaymentPocController(
    val userAccountService: UserAccountService,
    val paymentPocService: PaymentPocService
) {
    @PostMapping("user/depositFiat")
    @Operation(summary = "Deposit Fiat in to user wallet from Bank Account")
    @ApiResponse(responseCode = "200", description = "Transactions were added")
    fun depositFiat(
        @RequestBody depositRequest: PaymentPocController.DepositRequestPayment,
        principal: Principal
    ): DepositResponse? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (depositRequest.fiatType == null) {
            throw BadRequestException(ErrorCodes.INVALID_ASSET_TYPE)
        } else if (depositRequest.fiatAmount == null || depositRequest.fiatAmount == BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        } else {
            return paymentPocService.createDepositFiatLink(depositRequest, userAccount)
        }
    }

    @PostMapping("user/paymentStatusCheck")
    @Operation(summary = "Deposit Fiat status check")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Transactions status"),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.TRANSACTION_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun paymentStatusCheck(
        @RequestBody statusCheckRequest: PaymentStatusCheckRequest,
        principal: Principal
    ): VePayPaymentStatusCheckResponse? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (statusCheckRequest.txnId == null) {
            throw BadRequestException(ErrorCodes.BAD_REQUEST)
        } else {
            return paymentPocService.paymentStatusCheck(statusCheckRequest, userAccount)
        }
    }

    @PostMapping("user/withdrawFiat")
    @Operation(summary = "Withdraw Fiat from user wallet to Bank Account")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Transactions were added"),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.TRANSACTION_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun withdrawFiat(
        @RequestBody withdrawRequest: PaymentPocController.WithdrawRequestPayment,
        principal: Principal
    ): VePayPaymentTransferResponse? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (withdrawRequest.fiatType == null) {
            throw BadRequestException(ErrorCodes.INVALID_ASSET_TYPE)
        } else if (withdrawRequest.fiatAmount == null || withdrawRequest.fiatAmount == BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        } else {
            return paymentPocService.createWithdrawFiat(withdrawRequest, userAccount)
        }
    }

    @PostMapping("user/payoutStatusCheck")
    @Operation(summary = "Withdraw Fiat status check")
    @ApiResponse(responseCode = "200", description = "Transactions were added")
    fun payoutStatusCheck(
        @RequestBody statusCheckRequest: PaymentStatusCheckRequest,
        principal: Principal
    ): VePayPaymentTransferResponse? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (statusCheckRequest.txnId == null) {
            throw BadRequestException(ErrorCodes.BAD_REQUEST)
        } else {
            return paymentPocService.payoutStatusCheck(statusCheckRequest, userAccount)
        }
    }

    @PostMapping("user/refundFiat")
    @Operation(summary = "Refund Fiat from user wallet to Bank Account")
    @ApiResponse(responseCode = "200", description = "Transactions were added")
    fun refundFiat(
        @RequestBody refundRequest: PaymentPocController.RefundRequest,
        principal: Principal
    ): VePayPOCRefundResponse? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (refundRequest.txnId == null) {
            throw BadRequestException(ErrorCodes.BAD_REQUEST)
        } else if (refundRequest.reason == null) {
            throw BadRequestException(ErrorCodes.REASON_FOR_REFUND_IS_MANDATORY)
        } else {
            return paymentPocService.refundFiat(refundRequest, userAccount)
        }
    }

    data class DepositRequestPayment(
        val fiatType: FiatCurrencyUnit,
        val fiatAmount: BigDecimal,
        val firstName: String,
        val lastName: String
    )

    data class WithdrawRequestPayment(
        val fiatType: FiatCurrencyUnit,
        val fiatAmount: BigDecimal,
        val bankAccountNumber: String? = null
    )

    data class DepositResponse(
        val refLink: String
    )

    data class PaymentStatusCheckRequest(
        val txnId: String
    )

    data class RefundRequest(
        val txnId: String,
        val reason: String
    )
}
