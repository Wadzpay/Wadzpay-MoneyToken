package com.vacuumlabs.wadzpay.issuance

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.issuance.models.ConversionRateAdjustmentViewModel
import com.vacuumlabs.wadzpay.issuance.models.ConversionRateViewModel
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanksUserEntry
import com.vacuumlabs.wadzpay.issuance.models.IssuanceWalletConfigViewModel
import com.vacuumlabs.wadzpay.issuance.models.Status
import com.vacuumlabs.wadzpay.issuance.models.TransactionLimitConfigViewModel
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.merchant.model.CountryCode
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.IndustryType
import com.vacuumlabs.wadzpay.user.TransactionDetailsRequest
import com.vacuumlabs.wadzpay.user.TransactionDetailsResponse
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.viewmodels.TransactionViewModel
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
import java.math.BigInteger
import java.security.Principal
import java.util.Date
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.Pattern
import kotlin.collections.List

@RestController
@RequestMapping("/issuance")
@Tag(name = "Issuance Dashboard")
@Validated
class IssuanceCommonController(
    val issuanceService: IssuanceService,
    val userAccountService: UserAccountService,
    val issuanceWalletService: IssuanceWalletService,
    val issuancePaymentService: IssuancePaymentService,
    val issuanceCommonService: IssuanceCommonService
) {

    @PostMapping("/admin/addSuperAdminIssuanceBank")
    @Operation(summary = "Add Issuance Admin Details")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Issuance Bank is created"),
        ApiResponse(
            responseCode = "400",
            description = "${ErrorCodes.INVALID_INPUT_FORMAT} (${ErrorCodes.INVALID_EMAIL}, ${ErrorCodes.INVALID_PHONE_NUMBER}, ${ErrorCodes.INVALID_PASSWORD}) - ValidationErrorResponse<br>${ErrorCodes.UNVERIFIED_PHONE_NUMBER} - ErrorResponse<br>" +
                "One of these error codes: https://github.com/firebase/firebase-admin-java/blob/104ab0dcefcbbff24b6b3fc747f3f363854185f2/src/main/java/com/google/firebase/auth/AuthErrorCode.java - ErrorResponse",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun createFakeUserAccount(@Valid @RequestBody createIssuanceBank: CreateIssuanceBank): IssuanceBanks? {
        createIssuanceBank.email = createIssuanceBank.email.toLowerCase()
        return issuanceService.createIssuanceBank(createIssuanceBank)
    }

    @PostMapping("/getInstitutionDetails")
    @Operation(summary = "Get All Institution Details")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Institution Details retrieved"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.ISSUANCE_BANK_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getInstitutionDetails(
        @RequestBody request: InstitutionRequest,
        principal: Authentication
    ): InstitutionResponse? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (userAccount.issuanceBanks != null) {
            return issuanceService.getInstitutionDetails(userAccount.issuanceBanks!!, request)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @PostMapping("/dashboard/updateIssuanceDetails")
    @Operation(summary = "Update current logged in issuance details.")
    @ApiResponse(responseCode = "200", description = "Successfully update issuance bank details")
    fun updateIssuanceDetails(
        principal: Authentication,
        @Valid @RequestBody updateIssuanceRequest: UpdateIssuanceRequest
    ): IssuanceBanks {
        val issuanceBank = issuanceService.getIssuanceBankAccountByEmail(principal.name)
        issuancePaymentService.saveIssuanceBanksAuditLogs(updateIssuanceRequest, issuanceBank)
        if (!updateIssuanceRequest.defaultTimeZone.isNullOrEmpty()) {
            issuanceBank.timeZone = updateIssuanceRequest.defaultTimeZone.toString()
        }
        if (updateIssuanceRequest.defaultCurrency != null) {
            issuanceBank.defaultCurrency = updateIssuanceRequest.defaultCurrency.toString()
        }
        if (updateIssuanceRequest.bankLogo != null) {
            issuanceBank.bankLogo = updateIssuanceRequest.bankLogo
        }
        if (updateIssuanceRequest.destinationFiatCurrency != null) {
            issuanceBank.destinationFiatCurrency = updateIssuanceRequest.destinationFiatCurrency
        }
        if (updateIssuanceRequest.fiatCurrency != null) {
            issuanceBank.fiatCurrency = updateIssuanceRequest.fiatCurrency
        }
        if (updateIssuanceRequest.isP2PEnabled != null) {
            issuanceBank.p2pTransfer = updateIssuanceRequest.isP2PEnabled
        }
        return issuanceService.updateIssuanceDetails(issuanceBank)
    }
    @GetMapping("/dashboard/issuanceDetails")
    @Operation(summary = "Get current logged in issuance details.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved issuance details")
    fun getV1MerchantDetails(principal: Authentication): IssuanceBanks {
        return issuanceService.getIssuanceBankAccountByEmail(principal.name) as IssuanceBanks
    }

    @PostMapping("/user/loadToken")
    @Operation(summary = "Load token Balance to wallet user")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Mapped successfully"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.BAD_REQUEST,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.INSUFFICIENT_FUNDS} , ${ErrorCodes.UNSUPPORTED_TOKEN} , ${ErrorCodes.USER_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND} , ${ErrorCodes.WALLET_DISABLED}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun loadWalletBalance(@RequestBody request: LoadTokenBalanceRequest, principal: Principal): TransactionViewModel? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val issuanceBanksUserEntry = issuanceWalletService.getIssuanceBankMapping(userAccount)
        if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.status == Status.DISABLED) {
            throw EntityNotFoundException(ErrorCodes.WALLET_DISABLED)
        }
        if (request.amount <= BigDecimal.ZERO) {
            throw EntityNotFoundException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (request.tokenAsset != CurrencyUnit.SART) {
            throw EntityNotFoundException(ErrorCodes.UNSUPPORTED_TOKEN)
        }
        return if (issuanceBanksUserEntry != null) {
            issuanceWalletService.loadWalletBalance(request, userAccount, issuanceBanksUserEntry.issuanceBanksId, true)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @PostMapping("/user/refundToken")
    @Operation(summary = "Refund token Balance to wallet user")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Mapped successfully"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.BAD_REQUEST,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.INSUFFICIENT_FUNDS} , ${ErrorCodes.UNSUPPORTED_TOKEN} , ${ErrorCodes.USER_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND} , ${ErrorCodes.WALLET_DISABLED}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun refundToken(@RequestBody request: RefundTokenBalanceRequest, principal: Principal): TransactionViewModel? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val issuanceBanksUserEntry = issuanceWalletService.getIssuanceBankMapping(userAccount)
        if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.status == Status.DISABLED) {
            throw EntityNotFoundException(ErrorCodes.WALLET_DISABLED)
        }
        if (request.amount <= BigDecimal.ZERO) {
            throw EntityNotFoundException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (request.tokenAsset != CurrencyUnit.SART) {
            throw EntityNotFoundException(ErrorCodes.UNSUPPORTED_TOKEN)
        }
        if (issuanceBanksUserEntry != null) {
            return issuanceWalletService.refundToken(request, userAccount, issuanceBanksUserEntry.issuanceBanksId, true)
        }
        throw BadRequestException(ErrorCodes.BAD_REQUEST)
    }

    @GetMapping("/user/issuanceBankByWalletUser")
    @Operation(summary = "Get Issuance Bank By Wallet User")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Issuance Bank"),
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
    fun issuanceBankByWalletUser(principal: Principal): IssuanceBanksUserEntry? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return issuanceWalletService.getIssuanceBankMapping(userAccount)
            ?: throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
    }

    @PostMapping("/user/getWalletUser")
    @Operation(summary = "Get wallet user details mapped with login issuance bank")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Get wallet users list"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_EMAIL,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getWalletUser(
        @RequestBody request: IssuanceGraphService.UserDetailsRequest,
        authentication: Authentication
    ): IssuanceWalletService.WalletDataResponse? {
        val userAccount = userAccountService.getUserAccountByEmail(authentication.name)
        if (userAccount.issuanceBanks != null) {
            return issuanceWalletService.fetchWalletUserToViewModels(request, userAccount)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @PostMapping("/user/enableDisableWalletUser")
    @Operation(summary = "Enable Disable Wallet user")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Done successfully"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.BAD_REQUEST,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun enableDisableWalletUser(@RequestBody request: EnableDisableWalletUserRequest, authentication: Authentication): Boolean {
        val issuanceBank = issuanceService.getIssuanceBankAccountByEmail(authentication.name)
        if (request.email != null) {
            val userAccount: UserAccount = if (!issuancePaymentService.checkEmail(request.email)) {
                userAccountService.getUserAccountByCustomerId(request.email, issuanceBank.institutionId)
            } else {
                userAccountService.getUserAccountByEmail(request.email)
            }
            return issuanceWalletService.enableDisableWalletUser(request, userAccount, issuanceBank)
        } else {
            throw BadRequestException(ErrorCodes.BAD_REQUEST)
        }
    }
    @PostMapping("/admin/saveInstitutionDetails")
    @Operation(summary = "Institution Registration Process")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Issuance Bank is created"),
        ApiResponse(
            responseCode = "400",
            description = "${ErrorCodes.INVALID_INPUT_FORMAT} (${ErrorCodes.INVALID_EMAIL}, ${ErrorCodes.INVALID_PHONE_NUMBER}, ${ErrorCodes.INVALID_PASSWORD}) - ValidationErrorResponse<br>${ErrorCodes.UNVERIFIED_PHONE_NUMBER} - ErrorResponse<br>" +
                "One of these error codes: https://github.com/firebase/firebase-admin-java/blob/104ab0dcefcbbff24b6b3fc747f3f363854185f2/src/main/java/com/google/firebase/auth/AuthErrorCode.java - ErrorResponse",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun issuanceSignup(@Valid @RequestBody institutionRegisterData: InstitutionRegisterData, authentication: Authentication): IssuanceBanks? {
        val userAccount = userAccountService.getUserAccountByEmail(authentication.name)
        if (userAccount.issuanceBanks != null) {
            return issuanceService.institutionDetailsRegistration(institutionRegisterData, userAccount.issuanceBanks!!)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }
    @PostMapping("/admin/issuanceSignup")
    @Operation(summary = "Issuance Signup Process")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Issuance Bank is created"),
        ApiResponse(
            responseCode = "400",
            description = "${ErrorCodes.INVALID_INPUT_FORMAT} (${ErrorCodes.INVALID_EMAIL}, ${ErrorCodes.INVALID_PHONE_NUMBER}, ${ErrorCodes.INVALID_PASSWORD}) - ValidationErrorResponse<br>${ErrorCodes.UNVERIFIED_PHONE_NUMBER} - ErrorResponse<br>" +
                "One of these error codes: https://github.com/firebase/firebase-admin-java/blob/104ab0dcefcbbff24b6b3fc747f3f363854185f2/src/main/java/com/google/firebase/auth/AuthErrorCode.java - ErrorResponse",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun issuanceSignup(@Valid @RequestBody createIssuanceBank: CreateIssuanceBank, authentication: Authentication): IssuanceBanks? {
        val userAccount = userAccountService.getUserAccountByEmail(authentication.name)
        if (userAccount.issuanceBanks != null) {
            createIssuanceBank.email = createIssuanceBank.email.toLowerCase()
            return issuanceService.issuanceSignup(createIssuanceBank, userAccount.issuanceBanks!!)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @GetMapping("/admin/getTransactionType")
    @Operation(summary = "Get transaction type on issuance portal.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Get conversion rate"),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.TRANSACTION_TYPE_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getTransactionType(principal: Authentication): List<TransactionTypeResponse>? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (userAccount.issuanceBanks != null) {
            return issuanceService.getTransactionType(userAccount.issuanceBanks!!)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @GetMapping("/admin/getWalletFeeType")
    @Operation(summary = "Get Wallet Fee type on issuance portal.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Get conversion rate"),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.WALLET_FEE_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getWalletFeeType(principal: Authentication): MutableIterable<WalletFeeTypeResponse> {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (userAccount.issuanceBanks != null) {
            return issuanceService.getWalletFeeType(userAccount.issuanceBanks!!)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @GetMapping("/admin/getWalletFeeConfig")
    @Operation(summary = "Get Wallet parameter on issuance portal.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Get Wallet parameter "),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.WALLET_FEE_CONFIG_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND} ,  ${ErrorCodes.WALLET_FEE_CONFIG_ALREADY_ADDED}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getWalletFeeConfig(principal: Authentication): List<IssuanceWalletConfigViewModel>? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (userAccount.issuanceBanks != null) {
            return issuanceService.getWalletFeeConfig(userAccount.issuanceBanks!!)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @PostMapping("/admin/saveWalletFeeConfig")
    @Operation(summary = "Add /Edit wallet parameter config on issuance portal.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successfully added Wallet parameter "),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.WALLET_FEE_CONFIG_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun saveWalletFeeConfig(@RequestBody addWalletFeeConfig: AddWalletFeeConfig, principal: Authentication): IssuanceWalletConfigViewModel? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (userAccount.issuanceBanks != null) {
            return issuanceService.saveWalletFeeConfig(userAccount.issuanceBanks!!, addWalletFeeConfig)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @GetMapping("/admin/getTransactionLimitConfig")
    @Operation(summary = "Get Transaction Limit Config on issuance portal.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Get Transaction Limit "),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.TRANSACTION_LIMIT_CONFIG_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND} ",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getTransactionLimitConfig(principal: Authentication): List<TransactionLimitConfigViewModel>? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (userAccount.issuanceBanks != null) {
            return issuanceService.getTransactionLimitConfig(userAccount.issuanceBanks!!)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @PostMapping("/admin/saveTransactionLimitConfig")
    @Operation(summary = "Add / Edit Transaction Limit config on issuance portal.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successfully added Transaction Limit "),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.TRANSACTION_LIMIT_CONFIG_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}  , ${ErrorCodes.TRANSACTION_LIMIT_ALREADY_ADDED}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun saveTransactionLimitConfig(@RequestBody addTransactionLimitConfig: AddTransactionLimitConfig, principal: Authentication): TransactionLimitConfigViewModel? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (addTransactionLimitConfig.transactionCount != null || addTransactionLimitConfig.minimum != null || addTransactionLimitConfig.maximum != null) {
            if (userAccount.issuanceBanks != null) {
                return issuanceService.saveTransactionLimitConfig(userAccount.issuanceBanks!!, addTransactionLimitConfig)
            }
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        } else {
            throw BadRequestException(ErrorCodes.NO_DATA_SENT)
        }
    }

    @PostMapping("/admin/saveTransactionLimitType")
    @Operation(summary = "Add / Edit Transaction Limit Type on issuance portal.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successfully added Transaction Limit Type"),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.TRANSACTION_LIMIT_CONFIG_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}  , ${ErrorCodes.TRANSACTION_LIMIT_ALREADY_ADDED}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun saveTransactionLimitType(@RequestBody addTransactionLimitType: AddTransactionLimitType, principal: Authentication): TransactionTypeResponse? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (userAccount.issuanceBanks != null) {
            return issuanceService.saveTransactionLimitType(userAccount.issuanceBanks!!, addTransactionLimitType)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }
    @PostMapping("/admin/saveWalletFeeType")
    @Operation(summary = "Add / Edit Wallet Fee Type on issuance portal.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successfully added Wallet Fee Type"),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.TRANSACTION_LIMIT_CONFIG_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}  , ${ErrorCodes.TRANSACTION_LIMIT_ALREADY_ADDED}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun saveWalletFeeType(@RequestBody addWalletFeeType: AddWalletFeeType, principal: Authentication): WalletFeeTypeResponse? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (userAccount.issuanceBanks != null) {
            return issuanceService.saveWalletFeeType(userAccount.issuanceBanks!!, addWalletFeeType)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }
    @GetMapping("/admin/conversionRates")
    @Operation(summary = "Get conversion rate on issuance portal.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Get conversion rate"),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.CONVERSION_RATE_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getConversionRates(principal: Authentication): List<ConversionRateViewModel> {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (userAccount.issuanceBanks != null) {
            return issuanceService.getConversionRates(userAccount.issuanceBanks!!)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }
    @PostMapping("/admin/addConversionRates")
    @Operation(summary = "Add/Edit conversion rate on issuance portal.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successfully added conversion rate"),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.CONVERSION_RATE_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}, ${ErrorCodes.CONVERSION_RATE_ALREADY_ADDED}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun addConversionRates(@RequestBody addConversionRate: AddConversionRate, principal: Authentication): ConversionRateViewModel? {
        System.out.println(" Principal Name : {}" + principal.name)
        System.out.println(" Principal Name : {}" + principal)
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (userAccount.issuanceBanks != null) {
            return issuanceService.addConversionRates(userAccount.issuanceBanks!!, addConversionRate)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @PostMapping("/admin/addConversionRatesAdjustment")
    @Operation(summary = "Add/Edit conversion rate adjustment on issuance portal.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successfully added conversion rate adjustment"),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.CONVERSION_RATE_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND} , ${ErrorCodes.CONVERSION_RATE_ADJUSTMENT_ALREADY_ADDED}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun addConversionRatesAdjustment(@RequestBody request: AddConversionRateAdjustment, principal: Authentication): ConversionRateAdjustmentViewModel? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (userAccount.issuanceBanks != null) {
            return issuanceService.addConversionRatesAdjustment(userAccount.issuanceBanks!!, request)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @PostMapping("/admin/getConversionRatesAdjustment")
    @Operation(summary = "Get conversion rate adjustment on issuance portal.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Get conversion rate"),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.CONVERSION_RATE_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getConversionRatesAdjustment(@RequestBody request: SearchRateAdjustment, principal: Authentication): List<ConversionRateAdjustmentViewModel> {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (userAccount.issuanceBanks != null) {
            return issuanceService.getConversionRatesAdjustment(userAccount.issuanceBanks!!, request)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @GetMapping("/getFiatCurrencyList")
    @Operation(summary = "Get fiat currency list.")
    @ApiResponse(responseCode = "200", description = "Fiat currency list")
    fun getFiatCurrencyList(principal: Authentication): Array<FiatCurrencyUnit> {
        return FiatCurrencyUnit.values()
    }

    @GetMapping("/getIndustryTypeList")
    @Operation(summary = "Get Industry Type list.")
    @ApiResponse(responseCode = "200", description = "Industry Type list")
    fun getIndustryTypeList(principal: Authentication): Array<IndustryType> {
        return IndustryType.values()
    }

    @GetMapping("/getTransactionMaster")
    @Operation(summary = "Get Transaction releted master data like transaction mode,transaction type")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Transaction master data"),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun getTransactionMaster(principal: Principal): Any? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return issuanceCommonService.getTransactionMaster(userAccount)
            ?: throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
    }

    @GetMapping("/getTransactionData")
    @Operation(summary = "Get transaction Details by login issuance")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Fetch Data"),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.ISSUANCE_BANK_NOT_FOUND},${ErrorCodes.TRANSACTION_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun getTransactionData(
        request: TransactionDetailsRequest,
        principal: Principal
    ): MutableList<TransactionDetailsResponse> {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (userAccount.issuanceBanks != null) {
            if (request.toDate != null && request.fromDate == null) {
                throw BadRequestException(ErrorCodes.FROM_DATE_REQUIRED)
            }
            return issuanceCommonService.getTransactionData(request, userAccount)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @GetMapping("/getDetails")
    @Operation(summary = "Get institution property data from file.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved institution property data")
    fun getInstitutionData(
        @RequestParam(required = true) institutionName: String?,
        @RequestParam(required = false) lang: String? // Optional language parameter
    ): Map<String, String> {
        return if (institutionName != null) {
            issuanceCommonService.getInstitutionProperties(institutionName, lang)
        } else {
            mapOf("error" to "Please provide institutionName")
        }
    }

    data class CreateIssuanceBank(
        val institutionName: String,
        val countryCode: CountryCode,
        val timeZone: String,
        val defaultCurrency: CurrencyUnit ? = null,
        @field: Email(message = ErrorCodes.INVALID_EMAIL)
        var email: String,
        @field: Pattern(
            regexp = "^\\+[0-9]{7,15}\$",
            message = ErrorCodes.INVALID_PHONE_NUMBER
        )
        val phoneNumber: String,
        val companyType: String?,
        /*
            Password format policy for the current regex:
            1. Its length must be between 8 - 128 characters.
            2. It must contain at least 1 alphabet characters, case in-sensitive.
            3. It must contain at least 1 numeric characters.
            4. It must contain at least 1 valid special symbols from ASCII table.
            */
        @field: Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[!@#&:;.,?/_*~%+=<>|`\\\\\"\$\\-()\\[\\]{}^'])[A-Za-z0-9!@#&:;.,?/_*~%+=<>|`\\\\\"\$\\-()\\[\\]{}^']{8,128}\$",
            message = ErrorCodes.INVALID_PASSWORD
        )
        val password: String,
        val isIssuanceAdmin: Boolean = false,
        val institutionLogo: String,
        val destinationFiatCurrency: FiatCurrencyUnit?,
        val fiatCurrency: FiatCurrencyUnit?,
        val p2pTransfer: Boolean = false
    )

    data class InstitutionRegisterData(
        var id: Long? = null,
        var institutionId: InstitutionRegisterObject? = null,
        var institutionName: InstitutionRegisterObject? = null,
        var institutionAbbreviation: InstitutionRegisterObject? = null,
        var institutionDescription: InstitutionRegisterObject? = null,
        var institutionLogo: InstitutionRegisterObject? = null,
        var institutionRegion: InstitutionRegisterObject? = null,
        var institutionTimeZone: InstitutionRegisterObject? = null,
        var defaultCurrency: InstitutionRegisterObject? = null,
        var destinationFiatCurrency: InstitutionRegisterObject? = null,
        var companyType: InstitutionRegisterObject? = null,
        var industryType: InstitutionRegisterObject? = null,
        var activationDate: InstitutionRegisterObject? = null,
        var addressLine1: InstitutionRegisterObject? = null,
        var addressLine2: InstitutionRegisterObject? = null,
        var addressLine3: InstitutionRegisterObject? = null,
        var city: InstitutionRegisterObject? = null,
        var provice: InstitutionRegisterObject? = null,
        var country: InstitutionRegisterObject? = null,
        var postalCode: InstitutionRegisterObject? = null,
        var primaryContactFirstName: InstitutionRegisterObject? = null,
        var primaryContactMiddleName: InstitutionRegisterObject? = null,
        var primaryContactLastName: InstitutionRegisterObject? = null,
        var primaryContactEmailId: InstitutionRegisterObject? = null,
        var primaryContactPhoneNumber: InstitutionRegisterObject? = null,
        var primaryContactDesignation: InstitutionRegisterObject? = null,
        var primaryContactDepartment: InstitutionRegisterObject? = null,
        var adminFirstName: InstitutionRegisterObject? = null,
        var adminMiddleName: InstitutionRegisterObject? = null,
        var adminLastName: InstitutionRegisterObject? = null,
        var adminEmailId: InstitutionRegisterObject? = null,
        var adminCountryCode: InstitutionRegisterObject? = null,
        var adminPhoneNumber: InstitutionRegisterObject? = null,
        var adminDepartment: InstitutionRegisterObject? = null,
        var customerOfflineTransaction: InstitutionRegisterObject? = null,
        var merchantOfflineTransaction: InstitutionRegisterObject? = null,
        var institutionStatus: InstitutionRegisterObject? = null,
        var approvalWorkFlow: InstitutionRegisterObject? = null,
        var p2pTransfer: InstitutionRegisterObject? = null,
        var isFinalSave: Boolean? = null
    )

    data class InstitutionRegisterObject(
        val value: String? = null,
        val isMandatoryField: Boolean ? = null,
        val isShow: Boolean? = null,
        val isEdit: Boolean? = null
    )
    data class UpdateIssuanceRequest(
        val bankName: String ? = null,
        val countryCode: CountryCode ? = null,
        val defaultCurrency: CurrencyUnit ? = null,
        val phoneNumber: String ? = null,
        val bankLogo: String? = null,
        val defaultTimeZone: String? = null,
        val destinationFiatCurrency: FiatCurrencyUnit? = null,
        val fiatCurrency: FiatCurrencyUnit ? = null,
        val isP2PEnabled: Boolean? = null
    )
    data class LoadTokenBalanceRequest(
        @JsonIgnore
        @field: Email(message = ErrorCodes.INVALID_EMAIL)
        val email: String? = null,
        val tokenAsset: CurrencyUnit = CurrencyUnit.SART,
        var amount: BigDecimal,
        var bankAccountNumber: String ? = null,
        var isFromWallet: Boolean ? = false,
        val fiatAsset: FiatCurrencyUnit ? = null,
        val fiatAmount: BigDecimal ? = BigDecimal.ZERO,
        val feeConfigData: List<FeeConfigDataDetails>? = null,
        val totalFeeApplied: BigDecimal ? = null,
        val totalRequestedAmount: BigDecimal? = null
    )

    data class EnableDisableWalletUserRequest(
        @field: Email(message = ErrorCodes.INVALID_EMAIL)
        val email: String ? = null,
        val isEnabled: Boolean
    )

    data class RefundTokenBalanceRequest(
        val tokenAsset: CurrencyUnit = CurrencyUnit.SART,
        var amount: BigDecimal,
        var bankAccountNumber: String ? = null,
        var isFromWallet: Boolean ? = false,
        val fiatAsset: FiatCurrencyUnit ? = null,
        val fiatAmount: BigDecimal ? = BigDecimal.ZERO,
        val feeConfigData: List<FeeConfigDataDetails>? = null,
        val totalFeeApplied: BigDecimal ? = null
    )

    data class AddConversionRate(
        val id: Long? = null,
        val isActive: Boolean? = null,
        val currencyFrom: FiatCurrencyUnit = FiatCurrencyUnit.MYR,
        val currencyTo: CurrencyUnit = CurrencyUnit.SART,
        val baseRate: BigDecimal,
        val validFrom: Date
    )

    data class AddConversionRateAdjustment(
        val id: Long? = null,
        val isActive: Boolean? = null,
        val currencyFrom: String,
        val currencyTo: String,
        val percentage: BigDecimal ? = null,
        val validFrom: Date,
        val markType: MarkType = MarkType.UP
    )

    data class SearchRateAdjustment(
        val markType: MarkType? = null
    )

    enum class MarkType {
        UP,
        DOWN
    }

    enum class Frequency(val frequencyId: String, val frequencyType: String) {
        PER_TRANSACTION("PER_TRANSACTION", "Per Transaction"),
        ONE_TIME("ONE_TIME", "One Time"),
        DAILY("DAILY", "Daily"),
        WEEKLY("WEEKLY", "Weekly"),
        MONTHLY("MONTHLY", "Monthly"),
        QUARTERLY("QUARTERLY", "Quarterly"),
        HALF_YEARLY("HALF_YEARLY", "Half Yearly"),
        YEARLY("YEARLY", "Yearly")
    }

    enum class TransactionLoadingType(val transactionId: String, val transactionType: String) {
        TTC_001("TTC_001", "Initial Loading"),
        TTC_002("TTC_002", "Subsequent Loading"),
        TTC_003("TTC_003", "Purchase"),
        TTC_004("TTC_004", "Merchant Offline"),
        TTC_005("TTC_005", "Customer Offline"),
        TTC_006("TTC_006", "Redeem Unspent"),
        TTC_007("TTC_007", "P2P Transfer"),
        TTC_008("TTC_008", "Wallet Balance"),
        TTC_009("TTC_009", "Buy"),
        TTC_010("TTC_010", "Sell"),
        TTC_011("TTC_011", "Withdrawal"),
        TTC_012("TTC_012", "P2P Transfer")
    }

    enum class WalletFeeType(val walletFeeId: String, val walletFeeType: String) {
        WF_001("WF_001", "Activation Fee"),
        WF_002("WF_002", "Topup Fee"),
        WF_003("WF_003", "Service Fee"),
        WF_004("WF_004", "Redeem Unspent Fee"),
        WF_005("WF_005", "Wallet Low Balance Fee"),
        WF_006("WF_006", "Initial Topup Fee"),
        WF_007("WF_007", "Custody Fee")
    }

    data class AddWalletFeeConfig(
        val id: Long? = null,
        val isActive: Boolean? = null,
        val walletConfigType: WalletFeeType = WalletFeeType.WF_001,
        val frequency: Frequency ? = null,
        val value: String ? = null,
        val minimum: BigDecimal ? = null,
        val maximum: BigDecimal ? = null,
        val feeType: String? = null,
        val digitalCurrency: String? = null,
        val fiatCurrency: String? = null
    )

    data class AddTransactionLimitConfig(
        val id: Long? = null,
        val isActive: Boolean? = null,
        val transactionType: TransactionLoadingType = TransactionLoadingType.TTC_001,
        val fiatCurrency: FiatCurrencyUnit? = null,
        val frequency: Frequency ? = null,
        val transactionCount: BigInteger? = null,
        val minimum: BigDecimal ? = null,
        val maximum: BigDecimal? = null,
        val digitalCurrency: String? = null,
        val incrementalQuantity: String? = null,
        val quantityUnit: String? = null
    )
    data class AddTransactionLimitType(
        val transactionType: TransactionLoadingType = TransactionLoadingType.TTC_001,
        val transactionName: String
    )

    data class AddWalletFeeType(
        val walletFeeType: WalletFeeType = WalletFeeType.WF_001,
        val walletFeeName: String
    )
    data class InstitutionRequest(
        val page: Long? = null,
        val id: String ? = null,
        val limit: Long = 10
    )

    data class InstitutionResponse(
        val totalCount: Int? = 0,
        val institutionData: MutableList<InstitutionRegisterData>? = null,
        val pagination: IssuanceWalletService.Pagination? = null
    )

    data class FeeConfigDataDetails(
        val feeId: Long? = null,
        val enteredAmount: BigDecimal? = null,
        val feeAmount: BigDecimal? = null,
        val feeCalculatedAmount: BigDecimal? = null,
        val feeName: String? = null,
        val feeType: String? = null,
        val currencyType: String? = null,
        val description: String? = null
    )

    data class ServiceFeeRequest(
        val tokenAsset: CurrencyUnit = CurrencyUnit.SART,
        var amount: BigDecimal,
        val feeConfigData: List<FeeConfigDataDetails>? = null,
        val totalFeeApplied: BigDecimal ? = null,
        val description: String? = null,
        val type: TransactionType
    )

    data class TransactionTypeResponse(
        val id: Long = 0,
        var transactionTypeId: String,
        var transactionType: String
    )

    data class WalletFeeTypeResponse(
        val id: Long = 0,
        var walletFeeId: String,
        var feeType: String
    )
}
