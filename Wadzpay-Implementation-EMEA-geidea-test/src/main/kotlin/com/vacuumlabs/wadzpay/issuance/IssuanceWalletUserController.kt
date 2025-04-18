package com.vacuumlabs.wadzpay.issuance

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.vacuumlabs.wadzpay.accountowner.AccountOwnerService
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.issuance.models.UserAccountViewModel
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.user.UserAccountService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.math.RoundingMode
import java.security.Principal
import java.time.Instant
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.Pattern

@RestController
@RequestMapping("/issuanceWallet")
@Tag(name = "Issuance Wallet")
@Validated
class IssuanceWalletUserController(
    val issuanceWalletService: IssuanceWalletService,
    val userAccountService: UserAccountService,
    val issuanceService: IssuanceService,
    val accountOwnerService: AccountOwnerService
) {
    @PostMapping("/user/activateWalletUser")
    @Operation(summary = "Activate Wadzpay Wallet User by login issuance bank")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User Added successfully"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_EMAIL,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USERS_ALREADY_IN_DATABASE} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND} , ${ErrorCodes.ISSUANCE_WALLET_USER_ALREDY_MAPPED}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun activateWalletUser(@Valid @RequestBody activateWalletUserRequest: ActivateWalletUserRequest, authentication: Authentication): UserAccountViewModel? {
        val userAccount = userAccountService.getUserAccountByEmail(authentication.name)
        activateWalletUserRequest.email = activateWalletUserRequest.email.toLowerCase()
        if (activateWalletUserRequest.phoneNumber.length != 13) {
            throw BadRequestException(ErrorCodes.INVALID_PHONE_NUMBER)
        }
        if (userAccount.issuanceBanks != null) {
            return issuanceWalletService.inviteWalletUser(activateWalletUserRequest, userAccount)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @PostMapping("/supplyTokenToIssuer")
    @Operation(summary = "Add Token in to issuer wallet")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Token Added successfully"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_EMAIL,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.ISSUANCE_BANK_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun addToken(@RequestBody addToken: SupplyTokenToIssuerRequest, authentication: Authentication): SupplyTokenToIssuerResponse? {
        val userAccount = userAccountService.getUserAccountByEmail(authentication.name)
        if (addToken.noOfTokens <= BigDecimal.ZERO) {
            throw EntityNotFoundException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (userAccount.issuanceBanks != null) {
            return issuanceWalletService.addToken(addToken, userAccount)
        } else {
            throw BadRequestException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @GetMapping("/admin/walletBalance")
    @Operation(summary = "Wallet Balance Count on issuance dashboard")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Wallet Balance"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_EMAIL,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun walletBalance(authentication: Authentication): WalletBalanceResponse {
        val userAccount = userAccountService.getUserAccountByEmail(authentication.name)
        if (userAccount.issuanceBanks != null) {
            return issuanceWalletService.walletBalance(userAccount)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @GetMapping("/admin/walletSummary")
    @Operation(summary = "Wallet Summary Count on issuance dashboard")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Wallet Summary"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_EMAIL,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun walletSummary(authentication: Authentication): WalletSummaryResponse {
        val userAccount = userAccountService.getUserAccountByEmail(authentication.name)
        if (userAccount.issuanceBanks != null) {
            return issuanceWalletService.walletSummary(userAccount.issuanceBanks!!)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @GetMapping("/admin/walletRefund")
    @Operation(summary = "Wallet Refund Count on issuance dashboard")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Wallet Refund"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_EMAIL,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun walletRefund(authentication: Authentication): WalletRefundResponse {
        val userAccount = userAccountService.getUserAccountByEmail(authentication.name)
        if (userAccount.issuanceBanks != null) {
            return issuanceWalletService.walletRefund(userAccount.issuanceBanks!!)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }
    @GetMapping("/admin/walletSummaryGraph")
    @Operation(summary = "Wallet Summary Graph on issuance dashboard")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Wallet Summary Graph"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_EMAIL,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun walletSummaryGraph(authentication: Authentication): WalletSummaryGraphResponse? {
        val userAccount = userAccountService.getUserAccountByEmail(authentication.name)
        if (userAccount.issuanceBanks != null) {
            return issuanceWalletService.walletSummaryGraph(userAccount.issuanceBanks!!)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @GetMapping("/admin/transactionGraph")
    @Operation(summary = "Transaction Graph on issuance dashboard")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Transaction Graph Data"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_EMAIL,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun transactionGraph(authentication: Authentication): TransactionGraphResponse? {
        val userAccount = userAccountService.getUserAccountByEmail(authentication.name)
        if (userAccount.issuanceBanks != null) {
            return issuanceWalletService.transactionGraph(userAccount.issuanceBanks!!)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @GetMapping("/admin/fiatExchangeRate")
    @Operation(summary = "Retrieves exchange rates for Fiat Currency to SAR")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Success")
    )
    fun fiatExchangeRate(
        principal: Principal,
        @RequestParam(required = true, name = "from") from: FiatCurrencyUnit
    ): MutableMap<FiatCurrencyUnit, BigDecimal>? {
        issuanceService.getIssuanceBankAccountByEmail(principal.name)
        return issuanceService.fiatExchangeRates(from)
    }

    @GetMapping("/fiatExchangeRates")
    @Operation(summary = "Retrieves exchange rates for Fiat Currency to SAR")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Success")
    )
    fun fiatExchangeRates(
        principal: Principal,
        @RequestParam(required = true, name = "from") from: FiatCurrencyUnit,
        @RequestParam(required = false, name = "transactionType") transactionType: TransactionType
    ): MutableMap<FiatCurrencyUnit, BigDecimal>? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        // return issuanceService.fiatExchangeRates(from)
        return issuanceService.fiatExchangeRatesNew(from, transactionType, userAccount)
    }

    @GetMapping("/getTransactionValidation")
    @Operation(summary = "checkTransactionValidation")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Success")
    )
    fun checkTransactionValidation(
        principal: Authentication,
        @RequestParam(required = true, name = "type") type: IssuanceCommonController.TransactionLoadingType
    ): TransactionLimitResponse? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)
        return issuanceService.checkTransactionLimitConfig(type, userAccount, accountOwner)
    }

    @GetMapping("/runManuallyScheduler")
    @Operation(summary = "runManuallyScheduler")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Success")
    )
    fun runManuallyScheduler(
        principal: Authentication
    ) {
        return issuanceService.runManuallyScheduledToAllUserAccount()
    }

    @PostMapping("/institution/transferTokens")
    @Operation(summary = "Transfer Token from Address1 to Address 2")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Token Added successfully"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_EMAIL,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.ISSUANCE_BANK_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun transferTokens(
        @RequestBody request: TransferTokenRequest,
        @RequestParam(required = true, name = "typeOfTransaction") type: TransactionTypeList,
        authentication: Authentication
    ): SupplyTokenToIssuerResponse? {
        val issuerUserAccount = userAccountService.getUserAccountByEmail(authentication.name)
        if (request.noOfTokens <= BigDecimal.ZERO) {
            throw EntityNotFoundException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        } else {
            request.noOfTokens = request.noOfTokens.setScale(4, RoundingMode.HALF_UP)
        }
        when (type) {
            TransactionTypeList.SELL -> {
                request.receiverCustomerId = null
                if (request.customerId.isNullOrEmpty()) {
                    throw EntityNotFoundException(ErrorCodes.CUSTOMER_WALLET_REQUIRED)
                }
            }
            TransactionTypeList.TRANSFER -> {
                if (request.receiverCustomerId.isNullOrEmpty()) {
                    throw EntityNotFoundException(ErrorCodes.RECIPIENT_WALLET_REQUIRED)
                } else if (request.customerId.isNullOrEmpty()) {
                    throw EntityNotFoundException(ErrorCodes.CUSTOMER_WALLET_REQUIRED)
                }
            }
            TransactionTypeList.WITHDRAW -> {
                request.receiverCustomerId = null
                if (request.customerId.isNullOrEmpty()) {
                    throw EntityNotFoundException(ErrorCodes.CUSTOMER_WALLET_REQUIRED)
                }
            }
            TransactionTypeList.LOAD -> {
                request.customerId = null
                request.receiverCustomerId = null
            }
            else -> {
                request.receiverCustomerId = null
                if (request.customerId.isNullOrEmpty()) {
                    throw EntityNotFoundException(ErrorCodes.CUSTOMER_WALLET_REQUIRED)
                }
            }
        }
        return issuanceWalletService.transferTokens(request, issuerUserAccount, type)
    }

    @GetMapping("/walletName")
    @Operation(summary = "Get bank name from email")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved bank name")
    fun getBankNameFromEmail(
        @RequestParam(required = true) email: String
    ): Map<String, String?> {
        return issuanceWalletService.getBankName(email)
    }

    data class ActivateWalletUserRequest(
        @field: Pattern(
            regexp = "^\\+[0-9]{7,15}\$",
            message = ErrorCodes.INVALID_PHONE_NUMBER
        )
        val phoneNumber: String,
        @field: Email(message = ErrorCodes.INVALID_EMAIL)
        var email: String,
        val firstName: String ? = null,
        val lastName: String ? = null
    )

    data class WalletSummaryResponse(
        var totalWallets: Int ? = 0,
        var enableWallets: Int ? = 0,
        var disabledWallets: Int ? = 0,
        var totalDeposits: Int ? = 0,
        var totalWalletsInLastThirtyDays: Int ? = 0,
        var enableWalletsInLastThirtyDays: Int ? = 0,
        var totalDepositsInLastThirtyDays: Int ? = 0,
    )

    data class WalletBalanceResponse(
        var totalWalletBalance: String? = BigDecimal.ZERO.toString(),
        var enableWalletBalance: String? = BigDecimal.ZERO.toString(),
        var totalDepositBalance: String? = BigDecimal.ZERO.toString(),
        var refundedBalance: String? = BigDecimal.ZERO.toString()
    )

    data class WalletRefundResponse(
        var totalRefundRequest: Int ? = 0,
        var totalRefunded: String? = BigDecimal.ZERO.toString(),
        var totalRefundRequestInLastThirtyDays: Int ? = 0,
        var totalRefundedInLastThirtyDays: String? = BigDecimal.ZERO.toString()
    )

    data class TransactionGraphResponse(
        var weeklyTransaction: IssuanceGraphService.WeeklyData? = null,
        var monthlyTransaction: MutableList<IssuanceGraphService.Monthly>? = null,
        var yearlyTransaction: IssuanceGraphService.Yearly? = null
    )

    data class WalletSummaryGraphResponse(
        var totalWalletsBalances: ResponseDateWise? = null,
        var enabledWalletsBalances: ResponseDateWise? = null,
        var totalDepositsBalances: ResponseDateWise? = null
    )
    data class ResponseDateWise(
        var weekly: IssuanceGraphService.WeeklyData? = null,
        var monthly: MutableList<IssuanceGraphService.Monthly>? = null,
        var yearly: IssuanceGraphService.Yearly? = null
    )

    enum class TransactionType {
        LOAD,
        REFUND
    }

    data class TransactionLimitResponse(
        var initialLoading: Boolean = false,
        var ONE_TIME: TransactionLimitValue? = null,
        var PER_TRANSACTION: TransactionLimitValue? = null,
        var DAILY: TransactionLimitValue? = null,
        var WEEKLY: TransactionLimitValue? = null,
        var MONTHLY: TransactionLimitValue? = null,
        var QUARTERLY: TransactionLimitValue? = null,
        var HALF_YEARLY: TransactionLimitValue? = null,
        var YEARLY: TransactionLimitValue? = null,
        var feeConfig: List<FeeConfigDetails>? = null
    )

    data class TransactionLimitValue(
        var count: Int? = null,
        var availableCount: Int? = null,
        var transactionCount: Int? = null,
        var minimumBalance: BigDecimal? = null,
        var maximumBalance: BigDecimal? = null,
        var transactionBalance: BigDecimal? = null,
        var remainingMaximumBalance: BigDecimal? = null
    )
    data class FeeConfigDetails(
        var feeId: Long? = null,
        var currencyType: String? = null,
        var feeNameId: String,
        var feeName: String? = null,
        var feeType: FeeType? = null,
        var feeAmount: BigDecimal? = null,
        var feeFrequency: String? = null,
        var feeMinimumAmount: BigDecimal? = null,
        var feeMaximumAmount: BigDecimal? = null,
        var feeDescription: String? = null,
        var createdDate: Instant? = null
    )
    enum class FeeType {
        Percentage,
        Fixed
    }

    data class TransferTokenRequest(
        var customerId: String? = null,
        val tokenName: String,
        var noOfTokens: BigDecimal,
        var receiverCustomerId: String? = null
    )

    enum class TransactionTypeList {
        LOAD,
        SELL,
        BUY,
        TRANSFER,
        WITHDRAW
    }

    data class SupplyTokenToIssuerRequest(
        val customerName: String ? = null,
        val customerId: String? = null,
        val tokenName: String,
        var noOfTokens: BigDecimal
    )
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class SupplyTokenToIssuerResponse(
        val customerName: String ? = null,
        val customerId: String? = null,
        val customerUpdatedBalance: BigDecimal? = null,
        val institutionName: String? = null,
        val institutionId: String? = null,
        val institutionUpdatedBalance: BigDecimal? = null,
        val typeOfTransaction: String? = null,
        val tokenName: String,
        val noOfTokens: BigDecimal,
        val receiverCustomerId: String? = null,
        val transactionId: String,
        val receiverName: String ? = null,
        val status: String,
        val createdDate: Instant
    )
}
