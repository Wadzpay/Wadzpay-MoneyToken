package com.vacuumlabs.wadzpay.pos

import com.fasterxml.jackson.annotation.JsonFormat
import com.vacuumlabs.wadzpay.a.EncoderUtil
import com.vacuumlabs.wadzpay.accountowner.AccountOwnerService
import com.vacuumlabs.wadzpay.accountowner.EncryptQRRequest
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.BigDecimalAttributeConverter
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.exchange.ExchangeService
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.RefundToken
import com.vacuumlabs.wadzpay.ledger.model.TransactionRefundDetails
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.service.GetTransactionListRequest
import com.vacuumlabs.wadzpay.ledger.service.TransactionService
import com.vacuumlabs.wadzpay.merchant.InitiateWebLinkRefundPosRequest
import com.vacuumlabs.wadzpay.merchant.MerchantService
import com.vacuumlabs.wadzpay.merchant.RefundInitiationRequest
import com.vacuumlabs.wadzpay.merchant.model.AmountValidation
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.notification.NotificationService
import com.vacuumlabs.wadzpay.pos.model.PosPaymentListResponse
import com.vacuumlabs.wadzpay.user.CreateExpoTokenRequest
import com.vacuumlabs.wadzpay.user.DeleteExpoTokenRequest
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.viewmodels.RefundTransactionViewModel
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.security.Principal
import java.sql.Date
import java.sql.Time
import java.time.Instant
import javax.persistence.Convert
import javax.validation.Valid

@RestController
@RequestMapping("/pos")
@Tag(name = "POS")
@Validated
class PosController(
    val transactionService: TransactionService,
    val accountOwnerService: AccountOwnerService,
    val posService: PosService,
    val userAccountService: UserAccountService,
    val merchantService: MerchantService,
    val appConfig: AppConfig,
    val notificationService: NotificationService,
    val exchangeService: ExchangeService,
) {
    val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping(
        value = [
            "merchantDashboard/addPosToMerchant"
        ]
    )
    @Operation(summary = "Adds POS to loged in merchant.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "POS Added")
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun addPOS(
        principal: Principal,
        @RequestBody createPosRequest: CreatePosRequest
    ): MerchantPos {
        return posService.addPosToMerchant(principal, createPosRequest)
    }

    @PostMapping(
        value = [
            "/merchant/payment"
        ]
    )
    @Operation(summary = "Get Payment info in Encrypted From for POS")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "POS order created"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.DATE_SHOULD_NOT_BE_FUTURE_DATE,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "500",
            description = ErrorCodes.SERVER_ERROR,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "503",
            description = ErrorCodes.SERVER_ERROR,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun payDigitalCurrencyToPos(
        principal: Principal,
        @RequestParam(required = true, name = "digitalCurrencyType") digitalCurrencyType: CurrencyUnit,
        @RequestParam(required = true, name = "fiatType") faitType: FiatCurrencyUnit,
        @RequestBody posTransactionRequest: PosTransactionRequest
    ): PosService.PosTransactionResponse? {
        if (posTransactionRequest.fiatAmount <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (posTransactionRequest.description.isNullOrBlank()) {
            throw BadRequestException(ErrorCodes.DESCRIPTION_IS_MANDATORY_FIELD)
        }

//        return tempCreateTransaction(principal,digitalCurrencyType,faitType,posTransactionRequest)
        return posService.createPosTransaction(principal, digitalCurrencyType, faitType, posTransactionRequest)

        // to enable encryption
        //  val saltKey = data.saltkey?.saltkey
        // return saltKey?.let { EncoderUtil.getEncoded(it, data.toString()) }?.let { EncryptRequest(it, saltKey) }
    }

    @GetMapping("/merchant/refund")
    @Operation(summary = "Get list of transactions for Refund merchantDashboard")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
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
    fun refundTransactionReport(
        principal: Authentication,
        @Valid request: GetTransactionListRequest
    ): List<RefundTransactionViewModel> {

        if (request.status == null) {
            request.status =
                mutableListOf(TransactionStatus.OVERPAID, TransactionStatus.UNDERPAID, TransactionStatus.SUCCESSFUL)
        }

        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)
        return transactionService.getRefundTransactionViewModels(accountOwner, request)
    }

    private fun tempCreateTransaction(
        principal: Principal,
        digitalCurrencyType: CurrencyUnit,
        faitType: FiatCurrencyUnit,
        posTransactionRequest: PosTransactionRequest
    ): PosService.PosTransactionResponse? {
        try {
            return posService.createPosTransaction(principal, digitalCurrencyType, faitType, posTransactionRequest)
        } catch (e: Exception) {
            return tempCreateTransaction(principal, digitalCurrencyType, faitType, posTransactionRequest)
        }
    }

    @PostMapping(
        value = [
            "merchantDashboard/refreshPaymentInfo"
        ]
    )
    @Operation(summary = "Get all Payment info in Encrypted From for POS")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "POS order created"),
        ApiResponse(
            responseCode = "404",
            description = "Transaction with BlockChain Address xxx not found",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun refreshPosOrder(
        principal: Principal,
        @RequestParam(required = true, name = "blockChainAddress") blockChainAddress: String
    ): PosService.PosTransactionResponse? {
        return posService.refreshPosOrderWithBlockchainAddress(blockChainAddress)
    }

    @PostMapping(
        value = [
            "merchantDashboard/getTransactionStatus"
        ]
    )
    @Operation(summary = "Get Transection Status for POS")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Transaction Status"),
        ApiResponse(
            responseCode = "404",
            description = "Transaction with BlockChain Address xxx not found",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun getTransactionStatus(
        principal: Principal,
        @RequestParam(required = true, name = "blockChainAddress") blockChainAddress: String
    ): PosService.PosTransactionResponse {
        val posTransaction = posService.getTransactionDetails(blockChainAddress)

        return PosService.PosTransactionResponse(
            posTransaction.uuid,
            posTransaction.transaction?.posTransaction?.find { it.status == TransactionStatus.SUCCESSFUL || it.status == TransactionStatus.UNDERPAID || it.status == TransactionStatus.OVERPAID }?.amountcrypto
                ?: BigDecimal.ZERO,
            posTransaction.address,
            posTransaction.transaction?.totalFiatReceived ?: BigDecimal.ZERO,
            posTransaction.assetfiat,
            posTransaction.assetcrypto,
            posTransaction.transaction?.status ?: TransactionStatus.IN_PROGRESS,
            posTransaction.transaction?.totalDigitalCurrencyReceived ?: BigDecimal.ZERO,
            posTransaction.transaction?.uuid.toString(), posTransaction.transaction?.blockchainTxId,
            posTransaction.merchant.name
        )
    }

    @PostMapping(
        value = [
            "merchantDashboard/initiateWebLinkRefund"
        ]
    )
    @Operation(summary = "Initiate refund. Send Url to email and mobile.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successfully initiated refund details"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.REFUND_ALREADY_EXISTS_ERROR + " , " + ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO + " , " + ErrorCodes.CANNOT_REFUND_MORE_THAN_RECEIVED_FIAT,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )

    fun initiateRefundDetails(
        @Valid @RequestBody request: InitiateWebLinkRefundPosRequest,
        principal: Authentication
    ): RefundToken {
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)

        if (request.refundUserMobile == null && request.refundUserEmail == null) {
            throw BadRequestException(ErrorCodes.MOBILE_OR_EMAIL_ANY_ONE_REQUIRED)
        }
        if (request.refundAmountFiat <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }

        val merchantData = userAccount.merchant ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)

        return posService.initiateWebLinkRefundPos(accountOwner, request, merchantData)
    }

    @PostMapping(
        value = [
            "merchantDashboard/submitRefundFormAutoApprove"
        ]
    )
    @Operation(summary = "Initiate refund. Send Url to email and mobile.")
    @ApiResponse(responseCode = "200", description = "Successfully initiated refund details")
    fun submitRefundFormAutoApprove(@Valid @RequestBody request: RefundInitiationRequest, principal: Authentication): TransactionRefundDetails {
        println("@339 submitRefundFormAlgo")
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val merchantData = userAccount.merchant ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)

        if (request.refundAmountFiat <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (request.refundAmountDigital <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        /*if (request.refundAmountFiat <= merchantData.defaultRefundableFiatValue && request.refundMode == RefundMode.WALLET) {
            throw BadRequestException(ErrorCodes.TRANSACTION_REFUND_TYPE_IS_WALLET_AMOUNT_SHOULD_MORE_THAN_DEFAULT_REFUNDABLE_FIAT)
        }*/

        if (request.reasonForRefund.isNullOrEmpty()) {
            throw BadRequestException(ErrorCodes.REASON_FOR_REFUND_IS_MANDATORY)
        }

        if (appConfig.environment.equals("uat", true)) {
            if (!request.refundWalletAddress.startsWith("0x") || request.refundWalletAddress.length != 42) {
                throw BadRequestException(ErrorCodes.UNSUPPORTED_ETH_OR_USDT_WALLET_ADDRESS)
            }
        }
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)

        return posService.submitRefundFormAuto(accountOwner, request, "POS")
    }

    @PostMapping(
        value = [
            "merchantDashboard/submitRefundFormAutoApproveQAR"
        ]
    )
    @Operation(summary = "Initiate refund. Send Url to email and mobile.")
    @ApiResponse(responseCode = "200", description = "Successfully initiated refund details")
    fun submitRefundFormAutoApproveQAR(@Valid @RequestBody request: RefundInitiationRequest, principal: Authentication): TransactionRefundDetails {
        println("@339 submitRefundFormAlgo")
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val merchantData = userAccount.merchant ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)

        if (request.refundAmountFiat <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (request.refundAmountDigital <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        /*if (request.refundAmountFiat <= merchantData.defaultRefundableFiatValue && request.refundMode == RefundMode.WALLET) {
            throw BadRequestException(ErrorCodes.TRANSACTION_REFUND_TYPE_IS_WALLET_AMOUNT_SHOULD_MORE_THAN_DEFAULT_REFUNDABLE_FIAT)
        }*/

        if (request.reasonForRefund.isNullOrEmpty()) {
            throw BadRequestException(ErrorCodes.REASON_FOR_REFUND_IS_MANDATORY)
        }

        if (appConfig.environment.equals("uat", true)) {
            if (!request.refundWalletAddress.startsWith("0x") || request.refundWalletAddress.length != 42) {
                throw BadRequestException(ErrorCodes.UNSUPPORTED_ETH_OR_USDT_WALLET_ADDRESS)
            }
        }
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)

        return posService.submitRefundFormAutoQAR(accountOwner, request, "POS")
    }

    @PostMapping(
        value = [
            "merchantDashboard/getConversionRateList"
        ]
    )
    @Operation(summary = "Returns List of Digital Currency Names, Digital Currency Amount, Conversion Rate and Gas Fee. For input Amount and Fiat")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "List of Digital Currency Names, Digital Currency Amount, Conversion Rate and Gas Fee."
        ),
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
    fun getConversionRate(
        principal: Authentication,
        @RequestParam(required = true, name = "fiatType") from: FiatCurrencyUnit,
        @RequestBody amountRequest: AmountRequest
    ): PosPaymentListResponse {
        println("@320 getConversionRate")
        if (amountRequest.fiatAmount <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        println("@324 ")
        println(from)
        println("@326 ")
        return posService.getPosPaymentModes(principal, amountRequest.fiatAmount, from)
    }

    /*@GetMapping(
        value = [
            "merchantDashboard/getMarkupList"
        ]
    )
    @Operation(summary = "Returns List of Digital Currency Names, Digital Currency Amount, Conversion Rate and Gas Fee. For input Amount and Fiat")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "List of Digital Currency Names, Digital Currency Amount, Conversion Rate and Gas Fee."
        ),
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
    fun getMarkupList(
        principal: Authentication,
    ): ResponseConversionMarkup {
        return ResponseConversionMarkup(exchangeService.getConversionMarkupList()!!.size, exchangeService.getConversionMarkupList())
    }*/

    @PostMapping(
        value = [
            "merchantDashboard/getEncrypted"
        ]
    )
    @Operation(summary = "Encrypt Using SaltKey")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Encrypted Data"
        ),
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
    fun getEncrypted(
        principal: Authentication,
        @RequestParam(required = true, name = "fiatType") from: FiatCurrencyUnit,
        @RequestBody encryptRequest: EncryptRequest
    ): String {
        return EncoderUtil.getEncoded(encryptRequest.saltKey, encryptRequest.data)
    }

    @PostMapping(
        value = [
            "merchantDashboard/getDecrypted"
        ]
    )
    @Operation(summary = "Decrypt Using Salt Key")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Decrypted Data"
        ),
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
    fun getDecrypted(
        principal: Authentication,
        @RequestBody request: EncryptRequest
    ): String {
        return EncoderUtil.getDecoded(request.saltKey, request.data)
    }

    @PostMapping("/merchantDashboard/expoToken")
    @Operation(summary = "Add new expo token for the current user")
    @ApiResponses(
        ApiResponse(responseCode = "201"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "422",
            description = ErrorCodes.INVALID_EXPO_TOKEN,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun addExpoToken(@RequestBody request: CreateExpoTokenRequest, principal: Principal) {
        notificationService.checkValidExpoPushToken(request.expoToken)
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        notificationService.saveExpoPushNotificationToken(request, userAccount)
    }

    @DeleteMapping("/merchantDashboard/expoToken")
    @Operation(summary = "Delete expo token for the current user")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Token was deleted"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "422",
            description = ErrorCodes.INVALID_EXPO_TOKEN,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun deleteExpoToken(@RequestBody request: DeleteExpoTokenRequest, principal: Principal) {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val expoToken = notificationService.getExpoPushNotificationTokenByNameAndOwner(request, userAccount)
        if (expoToken != null) {
            notificationService.deleteExpoPushNotificationToken(expoToken)
        }
    }

    //    ge-idea demo test
    @PostMapping(
        value = [
            "/merchant/paymentSart"
        ]
    )
    @Operation(summary = "Get Payment info in Encrypted From for POS")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "POS order created"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.DATE_SHOULD_NOT_BE_FUTURE_DATE,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "500",
            description = ErrorCodes.SERVER_ERROR,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "503",
            description = ErrorCodes.SERVER_ERROR,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun payDigitalCurrencyToPosSart(
        principal: Principal,
        @RequestParam(required = true, name = "digitalCurrencyType") digitalCurrencyType: CurrencyUnit,
        @RequestParam(required = true, name = "fiatType") faitType: FiatCurrencyUnit,
        @RequestBody posTransactionRequest: PosTransactionRequest
    ): PosService.PosTransactionResponse? {
        println("payDigitalCurrencyToPosSart : " + Instant.now())
        if (posTransactionRequest.fiatAmount <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (posTransactionRequest.description.isNullOrBlank()) {
            throw BadRequestException(ErrorCodes.DESCRIPTION_IS_MANDATORY_FIELD)
        }

//        return tempCreateTransaction(principal,digitalCurrencyType,faitType,posTransactionRequest)
        return posService.createPosTransactionSart(principal, CurrencyUnit.BTC, faitType, posTransactionRequest)

        // to enable encryption
        //  val saltKey = data.saltkey?.saltkey
        // return saltKey?.let { EncoderUtil.getEncoded(it, data.toString()) }?.let { EncryptRequest(it, saltKey) }
    }

    //
    @PostMapping(
        value = [
            "/merchant/paymentAlgo"
        ]
    )
    @Operation(summary = "Get Payment info in Encrypted From for POS - 8")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "POS order created"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.DATE_SHOULD_NOT_BE_FUTURE_DATE,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "500",
            description = ErrorCodes.SERVER_ERROR,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "503",
            description = ErrorCodes.SERVER_ERROR,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun payDigitalCurrencyToPosAlgo(
        principal: Principal,
        @RequestParam(required = true, name = "digitalCurrencyType") digitalCurrencyType: CurrencyUnit,
        @RequestParam(required = true, name = "fiatType") faitType: FiatCurrencyUnit,
        @RequestBody posTransactionRequest: PosTransactionRequest
    ): PosService.PosTransactionResponse? {
        if (posTransactionRequest.fiatAmount <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (posTransactionRequest.description.isNullOrBlank()) {
            throw BadRequestException(ErrorCodes.DESCRIPTION_IS_MANDATORY_FIELD)
        }
        if (digitalCurrencyType != CurrencyUnit.SART) {
            throw BadRequestException(ErrorCodes.UNSUPPORTED_TOKEN)
        }
        if (faitType != FiatCurrencyUnit.SAR) {
            throw BadRequestException(ErrorCodes.UNSUPPORTED_TOKEN)
        }
//        return tempCreateTransaction(principal,digitalCurrencyType,faitType,posTransactionRequest)
        return posService.createPosTransaction(principal, digitalCurrencyType, faitType, posTransactionRequest)

        // to enable encryption
        //  val saltKey = data.saltkey?.saltkey
        // return saltKey?.let { EncoderUtil.getEncoded(it, data.toString()) }?.let { EncryptRequest(it, saltKey) }
    }

    @PostMapping(
        value = [
            "merchantDashboard/initiateWebLinkRefundAlgo"
        ]
    )
    @Operation(summary = "Initiate refund. Send Url to email and mobile.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successfully initiated refund details"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.REFUND_ALREADY_EXISTS_ERROR + " , " + ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO + " , " + ErrorCodes.CANNOT_REFUND_MORE_THAN_RECEIVED_FIAT,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )

    fun initiateRefundDetailsAlgo(
        @Valid @RequestBody request: InitiateWebLinkRefundPosRequest,
        principal: Authentication
    ): RefundToken {
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)

        if (request.refundUserMobile == null && request.refundUserEmail == null) {
            throw BadRequestException(ErrorCodes.MOBILE_OR_EMAIL_ANY_ONE_REQUIRED)
        }
        if (request.refundAmountFiat <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }

        val merchantData = userAccount.merchant ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)

        return posService.initiateWebLinkRefundPos(accountOwner, request, merchantData)
    }

    @PostMapping(
        value = [
            "merchantDashboard/getConversionRateListAlgo"
        ]
    )
    @Operation(summary = "Returns List of Digital Currency Names, Digital Currency Amount, Conversion Rate and Gas Fee. For input Amount and Fiat")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "List of Digital Currency Names, Digital Currency Amount, Conversion Rate and Gas Fee."
        ),
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
    fun getConversionRateAlgo(
        principal: Authentication,
        @RequestParam(required = true, name = "fiatType") from: FiatCurrencyUnit,
        @RequestBody amountRequest: AmountRequest
    ): PosPaymentListResponse {
        println("@639 getConversionRateAlgo")
        if (amountRequest.fiatAmount <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        return posService.getPosPaymentModesAlgo(principal, amountRequest.fiatAmount, from)
    }

    //
    @PostMapping(
        value = [
            "merchantDashboard/getTransactionStatusAlgo"
        ]
    )
    @Operation(summary = "Get Transection Status for POS")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Transaction Status"),
        ApiResponse(
            responseCode = "404",
            description = "Transaction with BlockChain Address xxx not found",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun getTransactionStatusAlgo(
        principal: Principal,
        @RequestParam(required = true, name = "uuid") uuid: String
    ): PosService.PosTransactionResponse {
        val posTransaction = posService.getTransactionDetailsAlgo(uuid)

        return PosService.PosTransactionResponse(
            posTransaction.uuid,
            posTransaction.transaction?.posTransaction?.find { it.status == TransactionStatus.SUCCESSFUL || it.status == TransactionStatus.UNDERPAID || it.status == TransactionStatus.OVERPAID }?.amountcrypto
                ?: BigDecimal.ZERO,
            posTransaction.address,
            posTransaction.transaction?.totalFiatReceived ?: BigDecimal.ZERO,
            posTransaction.assetfiat,
            posTransaction.assetcrypto,
            posTransaction.transaction?.status ?: TransactionStatus.IN_PROGRESS,
            posTransaction.transaction?.totalDigitalCurrencyReceived ?: BigDecimal.ZERO,
            posTransaction.transaction?.uuid.toString(), posTransaction.transaction?.blockchainTxId,
            posTransaction.merchant.name
        )
    }

    @PostMapping(
        value = [
            "/merchant/scanVerify"
        ]
    )
    @Operation(summary = "Decrypt Using Salt Key")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Decrypted Data"
        ),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_QR,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getMerchantScanVerify(
        principal: Principal,
        @RequestBody request: EncryptQRRequest
    ): String {
        return posService.startScanVerify(request)
    }
}

@AmountValidation
data class AmountRequest(
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Convert(converter = BigDecimalAttributeConverter::class)
    var fiatAmount: BigDecimal
)

@AmountValidation
data class PosTransactionRequest(
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Convert(converter = BigDecimalAttributeConverter::class)
    var fiatAmount: BigDecimal,
    var posId: String,
    var extPosId: String? = null,
    var extPosSequenceNo: String? = null,
    var extPosTransactionId: String? = null,
    var description: String,
    var extPosLogicalDate: Instant? = null,
    var extPosShift: String? = null,
    var extPosActualDate: Date,
    var extPosActualTime: Time,
//    var sourceWalletAddress: String? = null
)

data class EncryptRequest(val data: String, val saltKey: String)

data class VerifyPasscodeRequest(
    val userWalletAddress: String,
    val passcode: String
)
