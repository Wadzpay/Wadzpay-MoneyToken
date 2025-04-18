package com.vacuumlabs.wadzpay.api

import com.algorand.algosdk.account.Account
import com.vacuumlabs.API_VERSION
import com.vacuumlabs.COMMON_PASSWORD
import com.vacuumlabs.EMAIL_FORMATE
import com.vacuumlabs.SALT_KEY
import com.vacuumlabs.wadzpay.a.EncoderUtil
import com.vacuumlabs.wadzpay.algocutomtoken.AlgoCutomeTokenService
import com.vacuumlabs.wadzpay.algocutomtoken.models.AssetsInfoOnAlgoNt
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.exchange.ExchangeService
import com.vacuumlabs.wadzpay.issuance.CommonService
import com.vacuumlabs.wadzpay.issuance.IssuanceService
import com.vacuumlabs.wadzpay.issuance.IssuanceWalletService
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import com.vacuumlabs.wadzpay.issuance.models.Status
import com.vacuumlabs.wadzpay.language.LanguageService
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.MerchantConfigRequest
import com.vacuumlabs.wadzpay.ledger.model.MerchantConfigResponse
import com.vacuumlabs.wadzpay.ledger.model.RefundFormFields
import com.vacuumlabs.wadzpay.ledger.model.RefundStatus
import com.vacuumlabs.wadzpay.ledger.model.RefundType
import com.vacuumlabs.wadzpay.ledger.model.TransactionRefundDetails
import com.vacuumlabs.wadzpay.ledger.service.TransactionService
import com.vacuumlabs.wadzpay.merchant.OrderService
import com.vacuumlabs.wadzpay.merchant.RefundBalance
import com.vacuumlabs.wadzpay.merchant.RefundInitiationRequest
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.Order
import com.vacuumlabs.wadzpay.services.CognitoService
import com.vacuumlabs.wadzpay.services.FirebaseMessagingService
import com.vacuumlabs.wadzpay.services.JWTData
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.usermanagement.dataclass.LoginConfigurationData
import com.vacuumlabs.wadzpay.usermanagement.dataclass.LoginRequest
import com.vacuumlabs.wadzpay.usermanagement.dataclass.LoginVerifyOTPRequest
import com.vacuumlabs.wadzpay.usermanagement.dataclass.SetPasswordRequest
import com.vacuumlabs.wadzpay.usermanagement.model.UserDetailsDataViewModel
import com.vacuumlabs.wadzpay.usermanagement.service.LoginService
import com.vacuumlabs.wadzpay.viewmodels.RefundTransactionViewModel
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import javax.servlet.http.HttpSession
import javax.validation.Valid
import javax.validation.constraints.Email

@RestController
@RequestMapping("/api/$API_VERSION")
@Tag(name = "Open APIs")
class OpenApiController(
    val cognitoService: CognitoService,
    val orderService: OrderService,
    val transactionService: TransactionService,
    val exchangeService: ExchangeService,
    val userAccountService: UserAccountService,
    val appConfig: AppConfig,
    val issuanceService: IssuanceService,
    val issuanceWalletService: IssuanceWalletService,
    val algoCutomeTokenService: AlgoCutomeTokenService,
    val firebaseService: FirebaseMessagingService,
    val httpSession: HttpSession,
    val languageService: LanguageService,
    val commonService: CommonService,
    val loginService: LoginService
) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)
    @PostMapping("/getJwtToken")
    fun loginNoJwt(username: String, password: String?): JWTData {
        val password = password ?: COMMON_PASSWORD
        val jwtData = cognitoService.login(username.toLowerCase(), password)
        val userEncoder = BCryptPasswordEncoder(16)
        val encodedUser: String = userEncoder.encode(username)
        httpSession.setAttribute("currentUser", encodedUser)
        return jwtData
    }

    @PostMapping("/getJwtTokenForWebViewApp")
    fun getJwtTokenForWebViewApp(institutionId: String, username: String, password: String?): JWTData {
        val userData = userAccountService.getUserAccountByCustomerId(username, institutionId)
        val username1 = userData.email.toString()
        val password1 = password ?: COMMON_PASSWORD
        val jwtData = cognitoService.login(username1.toLowerCase(), password1)
        val userEncoder = BCryptPasswordEncoder(16)
        val encodedUser: String = userEncoder.encode(password1)
        httpSession.setAttribute("currentUser", encodedUser)
        return jwtData
    }

    @GetMapping(
        value = [
            "/merchant/order/{orderId}",
            "merchantDashboard/order/{orderId}"
        ]
    )
    @Operation(summary = "Get order by it's id")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.BAD_REQUEST,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.ORDER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getOrder(@PathVariable orderId: UUID): Order {
        return orderService.getOrderByUuid(orderId)
    }

    @GetMapping(
        value = [
            "/merchant/orderRefresh/{orderId}",
            "merchantDashboard/orderRefresh/{orderId}"
        ]
    )
    @Operation(summary = "Get order by it's id")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.BAD_REQUEST,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.ORDER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun orderRefresh(@PathVariable orderId: UUID): OrderService.OrderRefreshResponse {
        return orderService.getRefreshOrderByUuid(orderId)
    }

    @PostMapping(
        value = [
            "merchantDashboard/getRefundDetailsFromToken"
        ]
    )
    @Operation(summary = "Get refund trx details from token")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.BAD_REQUEST,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.ORDER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getRefundDetailsFromToken(@Valid @RequestBody request: RefundDetailsFromTokenRequest): RefundTransactionViewModel? {

        return transactionService.getRefundTransactionDetailsFromToken(request.refundUUID)
    }

    @PostMapping(
        value = [
            "pos/refundForm"
        ]
    )
    @Operation(summary = "Initiate refund.")
    @ApiResponse(responseCode = "200", description = "Successfully initiated refund details")
    fun submitRefundForm(@Valid @RequestBody request: RefundInitiationRequest): TransactionRefundDetails {
        if (request.refundAmountFiat <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (request.refundAmountDigital <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (request.refundWalletAddress.isNullOrBlank()) {
            throw BadRequestException(ErrorCodes.REFUND_WALLET_ADDRESS_IS_EMPTY)
        }
        if (request.refundUserMobile == null && request.refundUserEmail == null) {
            throw BadRequestException(ErrorCodes.MOBILE_OR_EMAIL_ANY_ONE_REQUIRED)
        }
        if (request.reasonForRefund.isNullOrBlank()) {
            throw BadRequestException(ErrorCodes.REASON_FOR_REFUND_IS_MANDATORY)
        }
        if (appConfig.environment.equals("uat", true)) {
            if (!request.refundWalletAddress.startsWith("0x") || request.refundWalletAddress.length != 42) {
                throw BadRequestException(ErrorCodes.UNSUPPORTED_ETH_OR_USDT_WALLET_ADDRESS)
            }
        }
        return transactionService.submitRefundForm(request)
    }

    @GetMapping(
        value = [
            "merchantDashboard/exchangeRates"
        ]
    )
    @Operation(summary = "Retrieves exchange rates for single Digital Currency and all fiat currencies from CryptoCompare.com")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Success")
    )
    fun getExchangeRate(
        @RequestParam(required = true, name = "from") from: FiatCurrencyUnit
    ): Map<CurrencyUnit, BigDecimal> {

        return exchangeService.getExchangeRates(from)
    }

    @GetMapping(
        value = [
            "merchantDashboard/refundFormFields"
        ]
    )
    @Operation(summary = "Retrieves refundFormFields from DataBase")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Success")
    )
    fun getRefundFormFields(formName: String): RefundFormFields? {
        return transactionService.getRefundFormFields(formName)
    }

    @PostMapping(
        value = [
            "pos/insertOrUpdateFormFieldsConfig"
        ]
    )
    @Operation(summary = "insert or update refund form fields")
    @ApiResponse(responseCode = "200", description = "Successfully initiated refund details")
    fun insertOrUpdateFormFieldsConfig(@Valid @RequestBody request: RefundFormFields) {
        return transactionService.submitRefundFormFields(request)
    }
    @GetMapping(
        value = [
            "pos/successUrl",
        ],
        produces = [MediaType.TEXT_HTML_VALUE]
    )
    @Operation(summary = "Success Url for ve payment POC")
    @ApiResponse(responseCode = "200", description = "Successfully initiated refund details")
    fun successUrl(): String {
        return "<html>\n" + "<header><title>Wadzpay</title></header>\n" +
            "<body>\n" + "Success Page \n" + "</body>\n" + "</html>"
    }

    @GetMapping(
        value = [
            "pos/failedUrl",
        ],
        produces = [MediaType.TEXT_HTML_VALUE]
    )
    @Operation(summary = "Failed Url for ve payment POC")
    @ApiResponse(responseCode = "200", description = "Successfully initiated refund details")
    fun failedUrl(): String {
        return "<html>\n" + "<header><title>Wadzpay</title></header>\n" +
            "<body>\n" + "Failed Page \n" + "</body>\n" + "</html>"
    }

    @GetMapping(
        value = [
            "pos/pgCancelUrl",
        ],
        produces = [MediaType.TEXT_HTML_VALUE]
    )
    @Operation(summary = "Payment Cancel Url for ve payment POC")
    @ApiResponse(responseCode = "200", description = "Successfully initiated refund details")
    fun pgCancelUrl(): String {
        return "<html>\n" + "<header><title>Wadzpay</title></header>\n" +
            "<body>\n" + "Payment Cancel Page \n" + "</body>\n" + "</html>"
    }

    @GetMapping(
        value = [
            "merchantDashboard/getRefundBalance"
        ]
    )
    @Operation(summary = "get the refund balance detail for tx id")
    @ApiResponse(responseCode = "200", description = "Successfully get refund balance details")
    fun getRefundBalance(@RequestParam uuid: UUID): RefundBalance {
        val transaction = transactionService.getTransaction(uuid)
        val successRefunds = transaction.refundTransactions?.filter { it.refundStatus == RefundStatus.REFUNDED }
        val noOfRefunds = successRefunds?.size
        val refundAmountDigital = successRefunds?.map { it.refundAmountDigital }?.fold(BigDecimal.ZERO, BigDecimal::add) ?: BigDecimal.ZERO
        return RefundBalance(
            uuid = transaction.uuid,
            fiatAmount = transaction.totalFiatReceived,
            refundAmountDigital = refundAmountDigital,
            refundAmountFiat = transaction.totalRefundedAmountFiat,
            balanceAmountFiat = transaction.totalFiatReceived?.minus(transaction.totalRefundedAmountFiat),
            noOfTimesRefundDone = noOfRefunds,
            refundType = if (noOfRefunds == 0) RefundType.NA else if (transaction.totalRefundedAmountFiat >= transaction.totalFiatReceived) RefundType.FULL else if (transaction.totalRefundedAmountFiat < transaction.totalFiatReceived) RefundType.PARTIAL else RefundType.NA
        )
    }

    @GetMapping("/checkEmailExists")
    @Operation(summary = "Check Email Exists or Not")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User found"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun checkEmailExists(
        @Email(message = ErrorCodes.INVALID_EMAIL)
        @RequestParam(value = "email", required = true) email: String,
        @RequestParam(value = "institutionId", required = false) institutionId: String?
    ): CheckEmailExistResponse {
        val userAccount = userAccountService.getUserAccountByEmail(email)
        if (userAccount.issuanceBanks != null) {
            throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
        }
        val issuanceBanksUserEntry = issuanceWalletService.getIssuanceBankMapping(userAccount)
        if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.status == Status.DISABLED) {
            throw EntityNotFoundException(ErrorCodes.WALLET_DISABLED)
        } else if (institutionId != null && !issuanceBanksUserEntry?.issuanceBanksId?.institutionId.equals(institutionId)) {
            throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
        }
        return CheckEmailExistResponse(cognitoUser = userAccount.cognitoUsername)
    }

    @GetMapping("/merchantConfigs")
    @Operation(summary = "Get configs from merchant_config table")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "configs"),
    )
    fun getDDFRefundConfigs(): ArrayList<MerchantConfigResponse> {
        return transactionService.getMerchantConfigs()
    }

    @PostMapping("/createOrUpdateMerchantConfig")
    @Operation(summary = "update/create merchant_config table")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "configs"),
    )
    fun updateDDFRefundConfig(
        @RequestBody request: MerchantConfigRequest
    ): MerchantConfigResponse {
        return transactionService.createOrUpdateDDFRefundConfig(request)
    }

    @GetMapping("/issuanceBankVerify")
    @Operation(summary = "Get Issuance by email")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User found"),
        ApiResponse(
            responseCode = "400",
            description = "${ErrorCodes.INVALID_EMAIL}, ${ErrorCodes.INVALID_PHONE_NUMBER}, ${ErrorCodes.PARAMETERS_NOT_SPECIFIED}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "500",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun issuanceBankVerify(
        @Email(message = ErrorCodes.INVALID_EMAIL)
        @RequestParam(value = "email", required = false) email: String?
    ): IssuanceBanks {
        email?.let {
            return issuanceService.getIssuanceBankAccountByEmail(email)
        }
        throw BadRequestException(ErrorCodes.PARAMETERS_NOT_SPECIFIED)
    }

    /*   @GetMapping("/sendMobileSMS")
       @Operation(summary = "Send sms to phone number")
       @ApiResponses(
           ApiResponse(responseCode = "200", description = "SMS sent"),
           ApiResponse(
               responseCode = "400",
               description = "${ErrorCodes.INVALID_EMAIL}, ${ErrorCodes.INVALID_PHONE_NUMBER}, ${ErrorCodes.PARAMETERS_NOT_SPECIFIED}",
               content = [Content(schema = Schema(implementation = ErrorResponse::class))]
           )
       )
       fun sendMobileSMS(
           @Pattern(
               regexp = "^\\+[0-9]{7,15}\$",
               message = ErrorCodes.INVALID_PHONE_NUMBER
           )
           @RequestParam(value = "phoneNumber", required = false) phoneNumber: String?,
           @RequestParam(value = "smsBody", required = false) smsBody: String?
       ): String {
           return emailSMSSenderService.sendMobileSMS(phoneNumber!!, smsBody!!)
       }*/
    @GetMapping("/checkAccountDetails")
    @Operation(summary = "getAppDetails")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User found"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getAppDetails(
        @Email(message = ErrorCodes.INVALID_EMAIL)
        @RequestParam(value = "email", required = true) email: String
    ): String {

        return "${userAccountService.getUserAccountByEmail(email)} ${appConfig.environment}"
    }
    @PostMapping("/savealgoaccount")
    @Operation(summary = "save algorand account details")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "configs")
    )
    fun saveAlgoAccount(
        @RequestBody request: AlgoAccountDetailsRequestCumResp
    ): String? {
        try {
            val successId = algoCutomeTokenService.saveAlgorandAddress(request.algoaddress, request.algomnu).toString()
            return successId
        } catch (e: Exception) {
            return e.message
        }
    }

    @PostMapping("/findalgoaccount")
    @Operation(summary = "get algorand account details")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "algorand account details"),
        ApiResponse(responseCode = "404", description = "Algorand address not found")
    )
    fun findAlgoAccount(
        @RequestBody request: AlgoAccountDetailsRequestCumResp
    ): AlgoAccountDetailsRequestCumResp? {
        try {
            val algorandAddress = algoCutomeTokenService.findAlgorandAddress(request.algoaddress)!!
            val algoAccountDetailsResponse =
                AlgoAccountDetailsRequestCumResp(algorandAddress.algoAddress, algorandAddress.algoMnu)
            return algoAccountDetailsResponse
        } catch (e: Exception) {
            throw EntityNotFoundException("algorand Address not found")
        }
    }
    @GetMapping("/checkApi")
    @Operation(summary = "getCheckAPI")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User found"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getCheckAPI(): String {
        println("Environment :  " + appConfig.environment)
        return "check api"
    }

    @GetMapping("/getAssetsOnAlgoNt")
    @Operation(summary = "getAssetsOnAlgoNt")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "assets found"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.BAD_REQUEST,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getAssetsOnAlgoNt(): ArrayList<AssetsInfoOnAlgoNt> {
        return algoCutomeTokenService.getAssetsOnAlgoNt()
    }
    @GetMapping("/getAssetsOnAlgoNtFromCache")
    @Operation(summary = "getAssetsOnAlgoNtFromCache")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "assets found"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.BAD_REQUEST,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getAssetsOnAlgoNtFromCache(): ArrayList<AssetsInfoOnAlgoNt> {
        return algoCutomeTokenService.getAssetsOnAlgoNtFromCache()
    }

    @PostMapping("/send-notification")
    @ResponseBody
    fun sendNotification(
        @RequestBody note: Note?,
        @RequestParam token: String?
    ): String? {
        return note?.let { firebaseService.sendNotification(it, token) }
    }
    @PostMapping("/testingDecimal")
    @ResponseBody
    fun testingDecimal(
        @RequestParam string: RoundingMode,
        @RequestParam number: BigDecimal
    ): BigDecimal? {
        return number.setScale(2, string)
    }

    @PostMapping("/testingAlgoJAVASDK")
    @ResponseBody
    fun testingDecimal(): String? {
        val acc = Account()
        println("acc.toMnemonic() ==> " + acc.toMnemonic())
        val address = acc.address
        println("acc.address() ==> " + acc.address)
        return address.toString()
    }

    @PostMapping("/getWebViewJwtToken")
    fun loginWebViewJwt(username: String, password: String?): JWTData {
        val decryptedUserName = getDecryptedUserName(username)
        var username = decryptedUserName
        if (!commonService.checkEmail(username)) {
            username += EMAIL_FORMATE
        }
        val password = password ?: COMMON_PASSWORD
        return cognitoService.login(username.toLowerCase(), password)
    }

    fun getDecryptedUserName(
        data: String
    ): String {
        try {
            return EncoderUtil.getDecoded(SALT_KEY, data)
        } catch (e: Exception) {
            throw BadRequestException(ErrorCodes.INVALID_ID)
        }
    }

    @GetMapping(
        value = [
            "user/getDecryptedEmail"
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
    fun getDecryptedEmailID(
        email: String
    ): decryptedData {
        try {
            return decryptedData(
                EncoderUtil.getDecoded(SALT_KEY, email)
            )
        } catch (e: Exception) {
            throw BadRequestException(ErrorCodes.INVALID_ID)
        }
    }
    @GetMapping(
        value = [
            "user/getEncryptedEmail"
        ]
    )
    @Operation(summary = "Encrypt Using SaltKey")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Encrypted Data"
        ),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getEncryptedEmail(
        email: String
    ): decryptedData {
        return decryptedData(
            EncoderUtil.getEncoded(SALT_KEY, email)
        )
    }
    @GetMapping("/userVerifyReset")
    @Operation(summary = "Check Email Exists or Not")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User found"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun userVerifyReset(
        @Email(message = ErrorCodes.INVALID_EMAIL)
        @RequestParam(value = "email", required = true) email: String
    ): UserVerifyReset {
        userAccountService.getUserAccountByEmail(email)
        return UserVerifyReset(response = "Success")
    }
    @GetMapping("/userVerifyResetIssuanceDash")
    @Operation(summary = "Check Email Exists or Not")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User found"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun userVerifyResetIssuanceDash(
        @Email(message = ErrorCodes.INVALID_EMAIL)
        @RequestParam(value = "email", required = true) email: String
    ): UserVerifyReset {
        issuanceService.getIssuanceBankAccountByEmail(email)
        return UserVerifyReset(response = "Success")
    }

    @GetMapping("/getLanguages")
    @Operation(summary = "Get Languages with issuer institution.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Get Mapped Languages"),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getLanguages(@RequestParam(value = "institutionId", required = true) institutionId: String): List<Any> {
        return languageService.getLanguages(institutionId)
    }

    @GetMapping("/getUserByUUID")
    @Operation(summary = "Get User Details by UUID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.INVALID_ID,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getUserByUUID(
        @RequestParam(required = true, name = "userUUID") userUUID: String
    ): UserDetailsDataViewModel {
        return loginService.getUserByUUID(userUUID)
    }

    @GetMapping("/getUserConfigByEmail")
    @Operation(summary = "Get User Login Configuration by emailId")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getUserConfigByEmail(
        @RequestParam(required = true, name = "email") email: String
    ): LoginConfigurationData {
        return loginService.getUserConfigByEmail(email)
    }
    @PostMapping("/setPassword")
    @Operation(summary = "Set Password")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful created"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "409",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun setPassword(@RequestBody setPasswordRequest: SetPasswordRequest): Boolean {
        return loginService.setPassword(setPasswordRequest)
    }

    @GetMapping("/resetPasswordLinkByEmail")
    @Operation(summary = "Send User password link by emailId")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun resetPasswordLinkByEmail(
        @RequestParam(required = true, name = "email") email: String
    ): Boolean {
        return loginService.resetPasswordLinkByEmail(email)
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful created"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "409",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun login(@RequestBody loginRequest: LoginRequest): Any {
        return loginService.login(loginRequest)
    }

    @GetMapping("/resendOTPByEmail")
    @Operation(summary = "Re-send OTP")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun resendOTPByEmail(
        @RequestParam(required = true, name = "email") email: String
    ): Boolean {
        return loginService.resendOTPByEmail(email)
    }

    @PostMapping("/verifyOTPByEmail")
    @Operation(summary = "Verify OTP")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun verifyOTPByEmail(@RequestBody verifyOTPRequest: LoginVerifyOTPRequest): Any {
        return loginService.verifyOTPByEmail(verifyOTPRequest)
    }
}

data class RefundDetailsFromTokenRequest(val refundUUID: UUID)

data class CheckEmailExistResponse(val cognitoUser: String)
data class UserVerifyReset(val response: String)
data class AlgoAccountDetailsRequestCumResp(
    val algoaddress: String,
    val algomnu: String
)

data class Note(
    val subject: String? = null,
    val content: String? = null,
    val data: Map<String, String>? = null,
    val image: String? = null
)
data class decryptedData(
    val email: String = ""
)
