package com.vacuumlabs.wadzpay.merchant
import com.fasterxml.jackson.annotation.JsonFormat
import com.vacuumlabs.MERCHANT_API_VERSION
import com.vacuumlabs.SALT_KEY
import com.vacuumlabs.wadzpay.a.EncoderUtil
import com.vacuumlabs.wadzpay.accountowner.AccountOwnerService
import com.vacuumlabs.wadzpay.auth.Role
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.emailSms.service.EmailSMSSenderService
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.RefundMode
import com.vacuumlabs.wadzpay.ledger.model.RefundOrigin
import com.vacuumlabs.wadzpay.ledger.model.RefundToken
import com.vacuumlabs.wadzpay.ledger.model.RefundType
import com.vacuumlabs.wadzpay.ledger.model.TransactionRefundDetails
import com.vacuumlabs.wadzpay.ledger.service.GetTransactionListRequest
import com.vacuumlabs.wadzpay.ledger.service.TransactionService
import com.vacuumlabs.wadzpay.merchant.export.TransactionCSVExportService
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.Merchant
import com.vacuumlabs.wadzpay.merchant.model.Order
import com.vacuumlabs.wadzpay.merchant.model.OrderType
import com.vacuumlabs.wadzpay.user.UserAccountService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayInputStream
import java.math.BigDecimal
import java.security.Principal
import java.sql.Date
import java.sql.Time
import java.time.Instant
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.Pattern
import javax.validation.constraints.PositiveOrZero

// TODO: once it is possible to manage merchants from merchant dashboard only, remove this controller and /v1/merchant API
@RestController
@RequestMapping("")
@Tag(name = "Merchant common WadzPay")
@Validated
class MerchantCommonController(
    val userAccountService: UserAccountService,
    val merchantService: MerchantService,
    val transactionCSVExportService: TransactionCSVExportService,
    val transactionService: TransactionService,
    val accountOwnerService: AccountOwnerService,
    val orderService: OrderService,
    val emailSMSSenderService: EmailSMSSenderService,
    val appConfig: AppConfig

) {
    @GetMapping(
        value = [
            "$MERCHANT_API_VERSION/merchant/transactionReports",
            "merchantDashboard/transactionReports"
        ],
        produces = ["text/csv"]
    )
    @Operation(summary = "Returns csv file with transaction reports")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved transaction reports")
    fun writeTransactionReports(@Valid request: GetTransactionListRequest, principal: Authentication):
        ResponseEntity<Resource> {
            val merchant = accountOwnerService.extractAccount(principal, useMerchant = true)
            val fileInputStream = InputStreamResource(
                ByteArrayInputStream(
                    transactionCSVExportService.exportTransactions(
                        transactionService.getTransactionViewModels(merchant, request)
                    )
                )
            )
            val csvFileName = "transactionsReport.csv"

            val headers = HttpHeaders()
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$csvFileName")
            headers.set(HttpHeaders.CONTENT_TYPE, "text/csv")

            return ResponseEntity(fileInputStream, headers, HttpStatus.OK)
        }

    @GetMapping(
        value = [
            "merchantDashboard/merchantDetails"
        ]
    )
    @Operation(summary = "Get current logged in merchant details.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved merchant details")
    fun getMerchantDetails(principal: Authentication): MerchantResponse {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val merchantData =
            userAccount.merchant ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)
        val merchant = merchantService.findById(merchantData.id)
        //   val merchant = accountOwnerService.extractAccount(principal,true) as Merchant
        return MerchantResponse(
            merchant,
            if (principal.authorities.contains(SimpleGrantedAuthority(Role.MERCHANT_ADMIN.toAuthority()))) {
                Role.MERCHANT_ADMIN
            } else if (principal.authorities.contains(SimpleGrantedAuthority(Role.MERCHANT_MERCHANT.toAuthority()))) {
                Role.MERCHANT_MERCHANT
            } else if (principal.authorities.contains(SimpleGrantedAuthority(Role.MERCHANT_SUPERVISOR.toAuthority()))) {
                Role.MERCHANT_SUPERVISOR
            } else if (principal.authorities.contains(SimpleGrantedAuthority(Role.MERCHANT_POSOPERATOR.toAuthority()))) {
                Role.MERCHANT_POSOPERATOR
            } else {
                Role.MERCHANT_READER
            }
        )
    }

    @PostMapping(
        value = [
            "merchantDashboard/updateMerchantDetails"
        ]
    )
    @Operation(summary = "Update current logged in merchant details.")
    @ApiResponse(responseCode = "200", description = "Successfully update merchant details")
    fun updateMerchantDetails(
        principal: Authentication,
        @Valid @RequestBody updateMerchantRequest: UpdateMerchantRequest
    ): Merchant {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val merchantData =
            userAccount.merchant ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)
        val merchant = merchantService.findById(merchantData.id)
        merchant.primaryContactEmail = updateMerchantRequest.primaryContactEmail
        merchant.primaryContactFullName = updateMerchantRequest.primaryContactFullName
        merchant.primaryContactPhoneNumber = updateMerchantRequest.primaryContactPhoneNumber
        merchant.defaultRefundableFiatValue = updateMerchantRequest.defaultRefundableFiatValue
        merchant.defaultTimeZone = updateMerchantRequest.defaultTimeZone
        merchant.mdrPercentage = updateMerchantRequest.mdrPercentage
        return merchantService.merchantRepository.save(merchant)
    }

    @GetMapping(
        value = [
            "$MERCHANT_API_VERSION/merchant/merchantDetails"
        ]
    )
    @Operation(summary = "Get current logged in merchant details.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved merchant details")
    fun getV1MerchantDetails(principal: Authentication): Merchant {
        return accountOwnerService.extractAccount(principal, true) as Merchant
    }

    @PostMapping(
        value = [
            "merchantDashboard/initiateWebLinkRefund"
        ]
    )
    @Operation(summary = "Initiate refund. Send Url to email and mobile.")
    @ApiResponse(responseCode = "200", description = "Successfully initiated refund details")
    fun initiateRefundDetails(
        @Valid @RequestBody request: InitiateWebLinkRefund,
        principal: Authentication
    ): RefundToken {
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val merchantData = userAccount.merchant ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)

        if (request.refundAmountFiat <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (request.refundAmountDigital <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }

        if (request.refundMode == RefundMode.WALLET && request.refundUserMobile == null && request.refundUserEmail == null) {
            throw BadRequestException(ErrorCodes.MOBILE_OR_EMAIL_ANY_ONE_REQUIRED)
        }
        if (request.refundAmountFiat > merchantData.defaultRefundableFiatValue && request.refundMode == RefundMode.CASH) {
            throw BadRequestException(ErrorCodes.TRANSACTION_REFUND_TYPE_IS_CASH_AMOUNT_SHOULD_UPTO_DEFAULT_REFUNDABLE_FIAT)
        }
        /*if (request.refundAmountFiat <= merchantData.defaultRefundableFiatValue && request.refundMode == RefundMode.WALLET) {
            throw BadRequestException(ErrorCodes.TRANSACTION_REFUND_TYPE_IS_WALLET_AMOUNT_SHOULD_MORE_THAN_DEFAULT_REFUNDABLE_FIAT)
        }*/

        return transactionService.initiateWebLinkRefund(accountOwner, request, RefundOrigin.MERCHANT_DASHBOARD)
    }

    @PostMapping(
        value = [
            "merchantDashboard/submitRefundForm"
        ]
    )
    @Operation(summary = "Initiate refund. Send Url to email and mobile.")
    @ApiResponse(responseCode = "200", description = "Successfully initiated refund details")
    fun submitRefundForm(@Valid @RequestBody request: RefundInitiationRequest, principal: Authentication): TransactionRefundDetails {
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
        if (request.refundAmountFiat > merchantData.defaultRefundableFiatValue && request.refundMode == RefundMode.CASH) {
            throw BadRequestException(ErrorCodes.TRANSACTION_REFUND_TYPE_IS_CASH_AMOUNT_SHOULD_UPTO_DEFAULT_REFUNDABLE_FIAT)
        }
        if (request.reasonForRefund.isNullOrEmpty()) {
            throw BadRequestException(ErrorCodes.REASON_FOR_REFUND_IS_MANDATORY)
        }
        if (request.refundUserMobile == null && request.refundUserEmail == null) {
            throw BadRequestException(ErrorCodes.MOBILE_OR_EMAIL_ANY_ONE_REQUIRED)
        }
        if (appConfig.environment.equals("uat", true)) {
            if (!request.refundWalletAddress.startsWith("0x") || request.refundWalletAddress.length != 42) {
                throw BadRequestException(ErrorCodes.UNSUPPORTED_ETH_OR_USDT_WALLET_ADDRESS)
            }
        }
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)

        return transactionService.submitRefundForm(accountOwner, request)
    }

    @GetMapping(
        value = [
            "merchantDashboard/merchantDetailsStatic"
        ]
    )
    @Operation(summary = "Get current logged in merchant details.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved merchant details")
    fun getMerchantDetailsStatic(principal: Authentication): MerchantResponseStatic {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val merchantData =
            userAccount.merchant ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)
        val merchant = merchantService.findById(merchantData.id)
        //   val merchant = accountOwnerService.extractAccount(principal,true) as Merchant
        val address = merchantData.account.getSubaccountByAsset(CurrencyUnit.SART).address!!.address
        val assetSart = CurrencyUnit.SART
//        val qrString = "transactionID:0000|blockchainAddress:$address|type:$assetSart|transactionAmount:0|merchantId:${merchant.id}|posId:${merchant.merchantPoses[0].posId}|merchantDisplayName:${merchant.name}"
        val qrString = "transactionID:00000|blockchainAddress:$address|type:$assetSart|transactionAmount:0.00|merchantId:${merchant.id}|posId:00000|merchantDisplayName:${merchant.name}"
        val qrStringEncrypted = EncoderUtil.getEncoded(SALT_KEY, qrString)

        return MerchantResponseStatic(
            merchant,
            if (principal.authorities.contains(SimpleGrantedAuthority(Role.MERCHANT_ADMIN.toAuthority()))) {
                Role.MERCHANT_ADMIN
            } else if (principal.authorities.contains(SimpleGrantedAuthority(Role.MERCHANT_MERCHANT.toAuthority()))) {
                Role.MERCHANT_MERCHANT
            } else if (principal.authorities.contains(SimpleGrantedAuthority(Role.MERCHANT_SUPERVISOR.toAuthority()))) {
                Role.MERCHANT_SUPERVISOR
            } else if (principal.authorities.contains(SimpleGrantedAuthority(Role.MERCHANT_POSOPERATOR.toAuthority()))) {
                Role.MERCHANT_POSOPERATOR
            } else {
                Role.MERCHANT_READER
            },
            qrStringEncrypted
        )
    }

    @GetMapping(
        value = [
            "merchantDashboard/staticCodeDisplay"
        ]
    )
    @Operation(summary = "Get Static Code Display")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved merchant details")
    fun getStaticQR(principal: Authentication): StaticCode {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val merchantData =
            userAccount.merchant ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)
        val merchant = merchantService.findById(merchantData.id)
        //   val merchant = accountOwnerService.extractAccount(principal,true) as Merchant
        val merchantResponse = MerchantResponse(
            merchant,
            if (principal.authorities.contains(SimpleGrantedAuthority(Role.MERCHANT_ADMIN.toAuthority()))) {
                Role.MERCHANT_ADMIN
            } else if (principal.authorities.contains(SimpleGrantedAuthority(Role.MERCHANT_MERCHANT.toAuthority()))) {
                Role.MERCHANT_MERCHANT
            } else if (principal.authorities.contains(SimpleGrantedAuthority(Role.MERCHANT_SUPERVISOR.toAuthority()))) {
                Role.MERCHANT_SUPERVISOR
            } else if (principal.authorities.contains(SimpleGrantedAuthority(Role.MERCHANT_POSOPERATOR.toAuthority()))) {
                Role.MERCHANT_POSOPERATOR
            } else {
                Role.MERCHANT_READER
            }
        )

        return StaticCode(
            merchantResponse.merchant.name,
            merchantResponse.merchant.merchantId
        )
    }

    data class StaticCode(
        val merchantName: String,
        val merchantId: String?
    )

    data class MerchantResponse(
        var merchant: Merchant,
        var role: Role
    )

    data class MerchantResponseStatic(
        var merchant: Merchant,
        var role: Role,
        var qrStringEncrypted: String
    )

    @PostMapping(
        value = [
            "$MERCHANT_API_VERSION/merchant/api-key"
        ]
    )
    @Operation(summary = "Issues new API Key for the authorized merchant")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "API key was issued")
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun issueApiKey(principal: Principal): CreateMerchantResponse {
        return CreateMerchantResponse.fromCredentials(
            merchantService.issueNewApiKey(merchantService.getMerchantByName(principal.name))
        )
    }

    @PostMapping(
        value = [
            "merchantDashboard/admin/api-key"
        ]
    )
    @Operation(summary = "Issues new API Key for the authorized merchant for dashboard")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "API key was issued")
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun issueApiKey(principal: Authentication): CreateMerchantResponse {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val merchant =
            userAccount.merchant ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)

        return CreateMerchantResponse.fromCredentials(
            merchantService.issueNewApiKey(merchantService.findById(merchant.id))
        )
    }

    @PostMapping("/merchant/setting/setTimeZone")
    @Operation(summary = "Update the time zone for current merchant")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "time zone updated")
    )
    fun setTimeZone(@RequestBody request: MerchantTimeZoneRequest, principal: Authentication): Merchant {
        val merchant = merchantService.findByNameAndPrimaryContactEmail(request.userName, request.primaryContactEmail)
        if (request.countryId.toString().isNotEmpty() && request.timeZone.isNotEmpty()) {
            return merchantService.setMerchantTimeZone(request, merchant)
        } else {
            throw BadRequestException(ErrorCodes.COUNTRY_CODE_OR_TIME_ZONE_MISSING)
        }
    }

    @DeleteMapping(
        value = [
            "$MERCHANT_API_VERSION/merchant/api-key",
        ]
    )
    @Operation(summary = "Invalidates API key for the merchant")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "API key was invalidated"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.API_KEY_DOES_NOT_EXIST,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "409",
            description = ErrorCodes.API_KEY_ALREADY_INVALID,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun invalidateApiKey(principal: Principal, @RequestBody request: InvalidateApiKeyRequest) {
        val merchant = merchantService.getMerchantByName(principal.name)
        merchantService.invalidateApiKey(request.apiKeyId, merchant)
    }

    @DeleteMapping(
        value = [
            "merchantDashboard/admin/api-key"
        ]
    )
    @Operation(summary = "Invalidates API key for the merchant Dashboard")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "API key was invalidated"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.API_KEY_DOES_NOT_EXIST,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "409",
            description = ErrorCodes.API_KEY_ALREADY_INVALID,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun invalidateApiKey(principal: Authentication, @RequestBody request: InvalidateApiKeyRequest) {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val merchant =
            userAccount.merchant ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)
        merchantService.invalidateApiKey(request.apiKeyId, merchant)
    }

    //    start amber demo - update git 1
    @PostMapping("merchantDashboard/requestPayment")
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
    fun requestPayment(@Valid @RequestBody request: CreateReceivePayment, principal: Principal): Order {

        LoggerFactory.getLogger(javaClass).info("CreateReceivePayment:: " + request.toString())
        if (request.requesterEmailAddress == null && request.requesterMobileNumber == null) {
            throw BadRequestException(ErrorCodes.INVALID_EMAIL)
        }
        if (request.requesterUserName == null) {
            request.requesterUserName = "Customer"
        }
        if (request.fiatAmount <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }

        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val merchant =
            userAccount.merchant ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)

        val order = orderService.requestPayment(
            cryptoType = request.digitalAsset,
            type = OrderType.ORDER, // TODO CHANGE ORDERTYPE TO PAYMENT REQUEST
            fiatAmount = request.fiatAmount,
            faitType = request.fiatAsset,
            target = merchant,
            targetEmail = request.requesterEmailAddress,
            externalOrderId = request.externalOrderId,
            description = null,
            principal,
            requesterUserName = request.requesterUserName,
            requesterEmailAddress = request.requesterEmailAddress,
            requesterMobileNumber = request.requesterMobileNumber,
        )

        orderService.triggerWebhookIfOrderExpires(order.uuid)
        if (request.requesterEmailAddress != null) {
            emailSMSSenderService.sendEmail(
                "Receive Payment Link",
                emailSMSSenderService.getReceivePaymentEmailBody(
                    request.requesterUserName.toString(),
                    "https://login.${appConfig.environment}.wadzpay.com?order_id=" + order.uuid + "&redirect="
                ),
                request.requesterEmailAddress!!,
                "contact@wadzpay.com"
            )
        }

        if (request.requesterMobileNumber != null) {
            println(request.requesterMobileNumber)
            emailSMSSenderService.sendMobileSMS(
                request.requesterMobileNumber!!,
                "Dear Customer,\n Please pay to Amber Lounge using the below link : \n" +
                    "https://login.${appConfig.environment}.wadzpay.com?order_id=" + order.uuid + "&redirect=",
            )
        }
        return order
    }
    data class CreateReceivePayment(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        val digitalAsset: CurrencyUnit,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @field: PositiveOrZero
        val fiatAmount: BigDecimal,
        val fiatAsset: FiatCurrencyUnit,
        val externalOrderId: String,
        var requesterUserName: String? = null,
        @field: Email(message = ErrorCodes.INVALID_EMAIL)
        val requesterEmailAddress: String? = null,
        val requesterMobileNumber: String? = null,
    )
    //    end amber demo

    @PostMapping(
        value = [
            "merchantDashboard/submitRefundFormAlgo"
        ]
    )
    @Operation(summary = "Initiate refund. Send Url to email and mobile.")
    @ApiResponse(responseCode = "200", description = "Successfully initiated refund details")
    fun submitRefundFormAlgo(@Valid @RequestBody request: RefundInitiationRequest, principal: Authentication): TransactionRefundDetails {
        println("@477 submitRefundFormAlgo")
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
        if (request.refundAmountFiat > merchantData.defaultRefundableFiatValue && request.refundMode == RefundMode.CASH) {
            throw BadRequestException(ErrorCodes.TRANSACTION_REFUND_TYPE_IS_CASH_AMOUNT_SHOULD_UPTO_DEFAULT_REFUNDABLE_FIAT)
        }
        if (request.reasonForRefund.isNullOrEmpty()) {
            throw BadRequestException(ErrorCodes.REASON_FOR_REFUND_IS_MANDATORY)
        }
        if (request.refundUserMobile == null && request.refundUserEmail == null) {
            throw BadRequestException(ErrorCodes.MOBILE_OR_EMAIL_ANY_ONE_REQUIRED)
        }
        if (appConfig.environment.equals("uat", true)) {
            if (!request.refundWalletAddress.startsWith("0x") || request.refundWalletAddress.length != 42) {
                throw BadRequestException(ErrorCodes.UNSUPPORTED_ETH_OR_USDT_WALLET_ADDRESS)
            }
        }
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)

        return transactionService.submitRefundFormAlgo(accountOwner, request)
    }

    @PostMapping(
        value = [
            "merchantDashboard/submitRefundFormAutoApprove"
        ]
    )
    @Operation(summary = "Initiate refund. Send Url to email and mobile.")
    @ApiResponse(responseCode = "200", description = "Successfully initiated refund details")
    fun submitRefundFormAutoApprove(@Valid @RequestBody request: RefundInitiationRequest, principal: Authentication): TransactionRefundDetails {
        println("@561 submitRefundFormAlgo")
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

        return transactionService.submitRefundFormAutoApprove(accountOwner, request, "Merchant Dashboard")
    }

    @PostMapping(
        value = [
            "merchantDashboard/submitRefundFormAutoApproveQAR"
        ]
    )
    @Operation(summary = "Initiate refund. Send Url to email and mobile.")
    @ApiResponse(responseCode = "200", description = "Successfully initiated refund details")
    fun submitRefundFormAutoApproveQAR(@Valid @RequestBody request: RefundInitiationRequest, principal: Authentication): TransactionRefundDetails {
        println("@597 submitRefundFormAlgo")
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

        return transactionService.submitRefundFormAutoApproveQAR(accountOwner, request, "Merchant Dashboard")
    }

    @PostMapping(
        value = [
            "merchantDashboard/initiateWebLinkRefundAlgo"
        ]
    )
    @Operation(summary = "Initiate refund. Send Url to email and mobile.")
    @ApiResponse(responseCode = "200", description = "Successfully initiated refund details")
    fun initiateRefundDetailsAlgo(
        @Valid @RequestBody request: InitiateWebLinkRefund,
        principal: Authentication
    ): RefundToken {
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val merchantData = userAccount.merchant ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)

        if (request.refundAmountFiat <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (request.refundAmountDigital <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }

        if (request.refundMode == RefundMode.WALLET && request.refundUserMobile == null && request.refundUserEmail == null) {
            throw BadRequestException(ErrorCodes.MOBILE_OR_EMAIL_ANY_ONE_REQUIRED)
        }
        if (request.refundAmountFiat > merchantData.defaultRefundableFiatValue && request.refundMode == RefundMode.CASH) {
            throw BadRequestException(ErrorCodes.TRANSACTION_REFUND_TYPE_IS_CASH_AMOUNT_SHOULD_UPTO_DEFAULT_REFUNDABLE_FIAT)
        }
        /*if (request.refundAmountFiat <= merchantData.defaultRefundableFiatValue && request.refundMode == RefundMode.WALLET) {
            throw BadRequestException(ErrorCodes.TRANSACTION_REFUND_TYPE_IS_WALLET_AMOUNT_SHOULD_MORE_THAN_DEFAULT_REFUNDABLE_FIAT)
        }*/

        return transactionService.initiateWebLinkRefund(accountOwner, request, RefundOrigin.MERCHANT_DASHBOARD)
    }

    @GetMapping("/merchant/getDetails")
    @Operation(summary = "Get merchant property data.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved merchant property data")
    fun getMerchantData(
        @RequestParam(required = true) merchantName: String?,
        @RequestParam(required = false) lang: String? // Optional language parameter
    ): Map<String, String> {
        return if (merchantName != null) {
            merchantService.getMerchantProperties(merchantName, lang)
        } else {
            mapOf("error" to "Please provide merchantName")
        }
    }
}

data class UpdateMerchantRequest(
    val primaryContactFullName: String,
    @field: Email(message = ErrorCodes.INVALID_EMAIL)
    val primaryContactEmail: String,
    @field: Pattern(
        regexp = "^\\+[0-9]{7,15}\$",
        message = ErrorCodes.INVALID_PHONE_NUMBER
    )
    val primaryContactPhoneNumber: String,
    val defaultRefundableFiatValue: BigDecimal,
    val defaultTimeZone: String,
    val mdrPercentage: BigDecimal
)

data class RefundInitiationRequest(
    val transactionId: UUID,
    val refundUserName: String? = "NA",
    @field: Pattern(
        regexp = "^\\+[0-9]{7,15}$|^$",
        message = ErrorCodes.INVALID_PHONE_NUMBER
    )
    var refundUserMobile: String? = null,
    @field: Email(message = ErrorCodes.INVALID_EMAIL)
    var refundUserEmail: String?,
    val refundWalletAddress: String,
    var refundDigitalType: CurrencyUnit,
    var refundFiatType: FiatCurrencyUnit,
    var refundAmountFiat: BigDecimal,
    var refundAmountDigital: BigDecimal,
    val reasonForRefund: String? = "NA",
    var refundMode: RefundMode,
    var extPosLogicalDate: Instant? = null,
    var extPosShift: String? = null,
    var extPosActualDate: Date? = null,
    var extPosActualTime: Time? = null,
    var sourceWalletAddress: String,
    var isReInitiateRefund: Boolean? = false,
    var refundTransactionID: String? = null,
    var refundToken: String? = null,
)

data class InitiateWebLinkRefund(
    @field: Pattern(
        regexp = "^\\+[0-9]{7,15}\$",
        message = ErrorCodes.INVALID_PHONE_NUMBER
    )
    var refundUserMobile: String?,
    @field: Email(message = ErrorCodes.INVALID_EMAIL)
    var refundUserEmail: String?,
    var refundUserName: String?,
    val refundDigitalType: CurrencyUnit,
    val refundFiatType: FiatCurrencyUnit,
    val refundAmountFiat: BigDecimal,
    var refundAmountDigital: BigDecimal,
    val transactionId: UUID,
    val refundMode: RefundMode,
    val refundCustomerFormUrl: String,
    var extPosIdRefund: String? = null,
    var extPosTransactionIdRefund: String? = null,
    var extPosLogicalDateRefund: Instant? = null,
    var extPosShiftRefund: String? = null,
    var extPosSequenceNoRefund: String? = null,
    var extPosActualDateRefund: Date? = null,
    var extPosActualTimeRefund: Time? = null,
    var isReInitiateRefund: Boolean? = false,
    var refundTransactionID: String? = null
)

data class InitiateWebLinkRefundPosRequest(
    @field: Pattern(
        regexp = "^\\+[0-9]{7,15}\$",
        message = ErrorCodes.INVALID_PHONE_NUMBER
    )
    var refundUserMobile: String?,
    @field: Email(message = ErrorCodes.INVALID_EMAIL)
    var refundUserEmail: String?,
    var refundUserName: String?,
    val refundAmountFiat: BigDecimal,
    val transactionId: UUID,
    val refundCustomerFormUrl: String?,
    var extPosIdRefund: String? = null,
    var extPosTransactionIdRefund: String? = null,
    var extPosLogicalDateRefund: Instant? = null,
    var extPosShiftRefund: String? = null,
    var extPosSequenceNoRefund: String? = null,
    var extPosActualDateRefund: Date? = null,
    var extPosActualTimeRefund: Time? = null,
    var isReInitiateRefund: Boolean? = false,
    var refundTransactionID: String? = null
)

data class RefundBalance(
    val uuid: UUID,
    val fiatAmount: BigDecimal?,
    val refundAmountDigital: BigDecimal?,
    val refundAmountFiat: BigDecimal?,
    var balanceAmountFiat: BigDecimal?,
    val noOfTimesRefundDone: Int?,
    val refundType: RefundType
)
