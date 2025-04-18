package com.vacuumlabs.wadzpay.user

import com.vacuumlabs.SALT_KEY
import com.vacuumlabs.wadzpay.a.EncoderUtil
import com.vacuumlabs.wadzpay.algocutomtoken.models.WadzpayMinter
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.EMAIL_ALREADY_EXISTS
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.EMAIL_DOES_NOT_EXISTS
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.FRAUDULENT_USER
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.INCORRECT_CODE
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.INVALID_EMAIL
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.INVALID_INPUT_FORMAT
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.INVALID_PASSWORD
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.INVALID_PHONE_NUMBER
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.PHONE_NUMBER_ALREADY_EXISTS
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.PHONE_NUMBER_DOES_NOT_EXISTS
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.UNKNOWN_SEON_ERROR
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.UNKNOWN_TWILIO_ERROR
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.UNVERIFIED_PHONE_NUMBER
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.VERIFICATION_NOT_FOUND
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.common.ValidationErrorResponse
import com.vacuumlabs.wadzpay.control.NotificationRequest
import com.vacuumlabs.wadzpay.control.NotificationRequestPayment
import com.vacuumlabs.wadzpay.filter.TransactionFilter
import com.vacuumlabs.wadzpay.filter.TransactionFilterRequest
import com.vacuumlabs.wadzpay.filter.TransactionFilterService
import com.vacuumlabs.wadzpay.fledger.FledgerService
import com.vacuumlabs.wadzpay.issuance.IssuanceCommonController
import com.vacuumlabs.wadzpay.issuance.IssuancePaymentService
import com.vacuumlabs.wadzpay.issuance.IssuanceWalletService
import com.vacuumlabs.wadzpay.issuance.IssuanceWalletUserController
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import com.vacuumlabs.wadzpay.issuance.models.Status
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.LedgerService
import com.vacuumlabs.wadzpay.ledger.service.NotificationDataService
import com.vacuumlabs.wadzpay.merchant.OrderService
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.Merchant
import com.vacuumlabs.wadzpay.notification.NotificationData
import com.vacuumlabs.wadzpay.notification.NotificationService
import com.vacuumlabs.wadzpay.notification.notificationstatus.NotificationStatus
import com.vacuumlabs.wadzpay.notification.notificationstatus.NotificationStatusParams
import com.vacuumlabs.wadzpay.notification.notificationstatus.NotificationStatusService
import com.vacuumlabs.wadzpay.requeststatus.RequestStatus
import com.vacuumlabs.wadzpay.requeststatus.RequestStatusService
import com.vacuumlabs.wadzpay.requeststatus.UpdatePaymentRequest
import com.vacuumlabs.wadzpay.rolemanagement.model.VerifyUser
import com.vacuumlabs.wadzpay.utils.EncryptService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.security.Principal
import java.time.Instant
import java.util.UUID
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@RestController
@RequestMapping("/user")
@Tag(name = "User account")
@Validated
class UserAccountController(
    val registrationService: RegistrationService,
    val userAccountService: UserAccountService,
    val orderService: OrderService,
    val ledgerService: LedgerService,
    val notificationService: NotificationService,
    val notificationDataService: NotificationDataService,
    val transactionFilterService: TransactionFilterService,
    val requestStatusService: RequestStatusService,
    val notificationStatusService: NotificationStatusService,
    val contactService: ContactService,
    val fledgerService: FledgerService,
    val issuanceWalletService: IssuanceWalletService,

    val encryptService: EncryptService,
    val issuancePaymentService: IssuancePaymentService
) {
    //
    @GetMapping(
        value = [
            "/getPushNotificationData"
        ]
    )
    @Operation(summary = "Get list of Notifications")
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
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getPushNotificationData(
        principal: Principal
    ): List<NotificationData>? {

        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return notificationDataService.getPushNotificationEmailData(userAccount.email!!)
    }

    @PostMapping("/registration/phone")
    @Operation(summary = "Start registration flow for a phone number")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "OTP code is sent to the phone number"),
        ApiResponse(
            responseCode = "400",
            description = "$INVALID_INPUT_FORMAT ($INVALID_PHONE_NUMBER) - ValidationErrorResponse<br>$INVALID_PHONE_NUMBER - ErrorResponse",
            content = [Content(schema = Schema(implementation = ValidationErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "409",
            description = PHONE_NUMBER_ALREADY_EXISTS,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "503",
            description = UNKNOWN_TWILIO_ERROR,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun createPhone(@Valid @RequestBody request: CreatePhoneRequest) {
        registrationService.startRegistrationForPhoneNumber(request.phoneNumber)
    }

    @PostMapping("/registration/phone/verify")
    @Operation(summary = "Second step - verify OTP code sent to a phone number")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "code is correct"),
        ApiResponse(
            responseCode = "400",
            description = "$INVALID_INPUT_FORMAT ($INVALID_PHONE_NUMBER)",
            content = [Content(schema = Schema(implementation = ValidationErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = INCORRECT_CODE,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "$PHONE_NUMBER_DOES_NOT_EXISTS, $VERIFICATION_NOT_FOUND",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "409",
            description = PHONE_NUMBER_ALREADY_EXISTS,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "500",
            description = UNKNOWN_TWILIO_ERROR,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun verifyPhone(@Valid @RequestBody request: VerifyPhoneRequest) {
        registrationService.verifyPhoneNumber(request.phoneNumber, request.code)
    }

    @PostMapping("/registration/details")
    @Operation(summary = "Third step - Pairs email with already verified phone number, sends OTP to said email and saves the password")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "OTP code is sent to the email"),
        ApiResponse(
            responseCode = "400",
            description = "$INVALID_INPUT_FORMAT ($INVALID_EMAIL, $INVALID_PHONE_NUMBER, $INVALID_PASSWORD) - ValidationErrorResponse<br>$UNVERIFIED_PHONE_NUMBER - ErrorResponse<br>",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = UNVERIFIED_PHONE_NUMBER,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "409",
            description = EMAIL_ALREADY_EXISTS,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "422",
            description = FRAUDULENT_USER,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "503",
            description = UNKNOWN_SEON_ERROR,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun preRegister(@Valid @RequestBody request: PreRegisterRequest, httpRequest: HttpServletRequest) {
        registrationService.preRegister(request.email, request.phoneNumber, request.password, httpRequest.remoteAddr)
    }

    @PostMapping("/registration/verify-and-create")
    @Operation(summary = "Final step - Verifies given email and creates user")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "user is created"),
        ApiResponse(
            responseCode = "400",
            description = "$INVALID_INPUT_FORMAT ($INVALID_EMAIL, $INVALID_PHONE_NUMBER, $INVALID_PASSWORD) - ValidationErrorResponse<br>$UNVERIFIED_PHONE_NUMBER - ErrorResponse<br>" +
                "One of these error codes: https://github.com/firebase/firebase-admin-java/blob/104ab0dcefcbbff24b6b3fc747f3f363854185f2/src/main/java/com/google/firebase/auth/AuthErrorCode.java - ErrorResponse",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = UNVERIFIED_PHONE_NUMBER,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = INCORRECT_CODE,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "$EMAIL_DOES_NOT_EXISTS, $VERIFICATION_NOT_FOUND",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "409",
            description = EMAIL_ALREADY_EXISTS,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun createUserAccount(@Valid @RequestBody request: VerifyAndCreateRequest): UserAccount {
        return registrationService.finishRegistration(request.email, request.phoneNumber, request.code, request.password, request.isMerchantAdmin, request.institutionId)
    }
    @PostMapping("/registration/addFakeUser")
    @Operation(summary = "Final step - Verifies given email and creates user")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "user is created"),
        ApiResponse(
            responseCode = "400",
            description = "$INVALID_INPUT_FORMAT ($INVALID_EMAIL, $INVALID_PHONE_NUMBER, $INVALID_PASSWORD) - ValidationErrorResponse<br>$UNVERIFIED_PHONE_NUMBER - ErrorResponse<br>" +
                "One of these error codes: https://github.com/firebase/firebase-admin-java/blob/104ab0dcefcbbff24b6b3fc747f3f363854185f2/src/main/java/com/google/firebase/auth/AuthErrorCode.java - ErrorResponse",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun createFakeUserAccount(@Valid @RequestBody request: VerifyAndCreateRequest): UserAccount {
        return registrationService.finishFakeRegistration(request.email, request.phoneNumber, request.code, request.password, request.isMerchantAdmin)
    }
    @PostMapping("/account/create_transaction_from_order")
    @Operation(summary = "Commit order to a ledger")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "401",
            description = ErrorCodes.INCORRECT_TARGET_EMAIL,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND}, ${ErrorCodes.ORDER_NOT_FOUND}, ${ErrorCodes.SUBACCOUNT_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "410",
            description = "${ErrorCodes.EXPIRED_ORDER}, ${ErrorCodes.PROCESSED_ORDER}, ${ErrorCodes.CANCELLED_ORDER}, ${ErrorCodes.FAILED_ORDER}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "422",
            description = ErrorCodes.INSUFFICIENT_FUNDS,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun createTransactionFromOrder(@RequestBody request: CreateTransactionFromOrderRequest, principal: Principal) {
        val order = orderService.getOpenOrderByUuid(request.order_id)
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)

        orderService.createTransactionFromOrder(order, userAccount)
    }
    @PostMapping("/account/saveToken")
    @Operation(summary = "save token ")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "401",
            description = ErrorCodes.INCORRECT_TARGET_EMAIL,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND}, ${ErrorCodes.ORDER_NOT_FOUND}, ${ErrorCodes.SUBACCOUNT_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "410",
            description = "${ErrorCodes.EXPIRED_ORDER}, ${ErrorCodes.PROCESSED_ORDER}, ${ErrorCodes.CANCELLED_ORDER}, ${ErrorCodes.FAILED_ORDER}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "422",
            description = ErrorCodes.INSUFFICIENT_FUNDS,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun saveTokenToUser(request: HttpServletRequest, principal: Authentication) {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token: String = header.replace("Bearer ", "")
            userAccount.bearerToken = encryptService.getEncodedString(token)
            userAccountService.saveUserAccount(userAccount)
        }
    }

    @PostMapping("/account/verifyToken")
    @Operation(summary = "verify token ")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "401",
            description = ErrorCodes.INCORRECT_TARGET_EMAIL,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND}, ${ErrorCodes.ORDER_NOT_FOUND}, ${ErrorCodes.SUBACCOUNT_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "410",
            description = "${ErrorCodes.EXPIRED_ORDER}, ${ErrorCodes.PROCESSED_ORDER}, ${ErrorCodes.CANCELLED_ORDER}, ${ErrorCodes.FAILED_ORDER}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "422",
            description = ErrorCodes.INSUFFICIENT_FUNDS,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun verifyUserByToken(request: HttpServletRequest, principal: Authentication): VerifyUser {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        var isUserVerified = false
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token: String = header.replace("Bearer ", "")
            isUserVerified = encryptService.isDecodedStringMatched(token, userAccount.bearerToken!!)
        }
        return VerifyUser(isUserVerified, userAccount.email!!)
    }
    @PostMapping("/order/cancel")
    @Operation(summary = "Cancel an order")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND}, ${ErrorCodes.ORDER_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "410",
            description = "${ErrorCodes.EXPIRED_ORDER}, ${ErrorCodes.PROCESSED_ORDER}, ${ErrorCodes.CANCELLED_ORDER}, ${ErrorCodes.FAILED_ORDER}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun cancelOrder(@RequestBody request: CancelOrderRequest, principal: Principal) {
        val order = orderService.getOpenOrderByUuid(request.order_id)
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)

        orderService.cancelOrder(order, userAccount)
    }

    @PostMapping("/expoToken")
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
        request.expoToken?.let { notificationService.checkValidExpoPushToken(it) }
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        notificationService.saveExpoPushNotificationToken(request, userAccount)
    }

    @DeleteMapping("/expoToken")
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

    @GetMapping("")
    @Operation(summary = "Get user by email or phone number")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User found"),
        ApiResponse(
            responseCode = "400",
            description = "$INVALID_EMAIL, $INVALID_PHONE_NUMBER, ${ErrorCodes.PARAMETERS_NOT_SPECIFIED}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getUser(
        @RequestParam(value = "email", required = false) email: String?,
        @Pattern(
            regexp = "^\\+[0-9]{7,15}\$",
            message = INVALID_PHONE_NUMBER
        )
        @RequestParam(value = "phoneNumber", required = false) phoneNumber: String?,
        principal: Principal
    ): UserAccount {
        val loginUserAccount = userAccountService.getUserAccountByEmail(principal.name)
        var userAccount: UserAccount? = null
        if (email != null) {
            userAccount = if (issuancePaymentService.checkEmail(email)) {
                email.let { userAccountService.getUserAccountByEmail(it) }
            } else {
                val issuanceBanksUserEntry = issuanceWalletService.getIssuanceBankMapping(loginUserAccount)
                email.let {
                    userAccountService.getUserAccountByCustomerId(
                        it,
                        issuanceBanksUserEntry?.issuanceBanksId?.institutionId
                    )
                }
            }
        }
        phoneNumber?.let {
            userAccount = userAccountService.getUserAccountByPhoneNumber(phoneNumber)
        }
        if (userAccount != null) {
            fledgerService.initialFiatWallet(userAccount!!)
            if (userAccount!!.customerId != null) {
                userAccount!!.email = userAccount!!.customerId
            }
            return userAccount as UserAccount
        }
        throw BadRequestException(ErrorCodes.PARAMETERS_NOT_SPECIFIED)
    }

    @GetMapping("/getUserByEmailOrMobile")
    @Operation(summary = "Get user by email or phone number")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User found"),
        ApiResponse(
            responseCode = "400",
            description = "$INVALID_EMAIL, $INVALID_PHONE_NUMBER, ${ErrorCodes.PARAMETERS_NOT_SPECIFIED}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getUserByEmailOrMobile(
        @RequestParam(value = "email", required = false) email: String?,
        @RequestParam(value = "phoneNumber", required = false) phoneNumber: String?,
        principal: Principal
    ): ArrayList<UserDataResponse> {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val issuanceBanksUserEntry = issuanceWalletService.getIssuanceBankMapping(userAccount)
        if (issuanceBanksUserEntry != null) {
            if (issuanceBanksUserEntry.status == Status.DISABLED) {
                throw EntityNotFoundException(ErrorCodes.WALLET_DISABLED)
            } else {
                val userMappingData = issuanceWalletService.issuanceBanksUserEntryRepository.getByIssuanceBanksId(issuanceBanksUserEntry.issuanceBanksId)
                if (userMappingData != null) {
                    val userAccountList: MutableList<UserAccount> = mutableListOf()
                    userMappingData.forEach { tData ->
                        userAccountList.add(tData.userAccountId)
                    }
                    val userAccountDataList: MutableList<UserDataResponse> = mutableListOf()
                    userAccountList.forEach { tData ->
                        val userDataResponse = UserDataResponse(
                            cognitoUsername = tData.cognitoUsername,
                            email = tData.customerId ?: tData.email,
                            phoneNumber = tData.phoneNumber,
                            notificationStatus = tData.notificationStatus,
                            firstName = tData.firstName,
                            lastName = tData.lastName,
                            customerType = tData.customerType,
                            createdDate = tData.createdDate,
                            customerId = tData.customerId,
                            bearerToken = tData.bearerToken
                        )
                        userAccountDataList.add(userDataResponse)
                    }
                    if (userAccountDataList.isNotEmpty()) {
                        email?.let {
                            var userAccountListData = userAccountDataList.filter { e -> e.email != null && e.email!!.contains(email, true) } as ArrayList<UserDataResponse>
                            if (userAccountListData.isEmpty()) {
                                userAccountListData = userAccountDataList.filter { e -> e.customerId != null && e.customerId.contains(email, true) } as ArrayList<UserDataResponse>
                            }
                            return userAccountListData
                        }
                        phoneNumber?.let {
                            return userAccountDataList.filter { e ->
                                e.phoneNumber != null && e.phoneNumber.contains(
                                    phoneNumber
                                )
                            } as ArrayList<UserDataResponse>
                        }
                    }
                }
            }
        } else {
            var userAccountList: MutableList<UserAccount> = mutableListOf()
            email?.let {
                userAccountList = userAccountService.getUserAccountByEmailLike(email)
            }

            phoneNumber?.let {
                userAccountList = userAccountService.getUserAccountByPhoneNumberLike(phoneNumber)
            }
            val userAccountDataList: MutableList<UserDataResponse> = mutableListOf()
            userAccountList.forEach { tData ->
                val userDataResponse = UserDataResponse(
                    cognitoUsername = tData.cognitoUsername,
                    email = tData.customerId ?: tData.email,
                    phoneNumber = tData.phoneNumber,
                    notificationStatus = tData.notificationStatus,
                    firstName = tData.firstName,
                    lastName = tData.lastName,
                    customerType = tData.customerType,
                    createdDate = tData.createdDate,
                    customerId = tData.customerId,
                    bearerToken = tData.bearerToken
                )
                userAccountDataList.add(userDataResponse)
            }
            return userAccountDataList as ArrayList<UserDataResponse>
        }
        throw BadRequestException(ErrorCodes.PARAMETERS_NOT_SPECIFIED)
    }

    @GetMapping("/contacts")
    @Operation(summary = "Get contacts")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "OK")
    )
    fun getContacts(
        @RequestParam(value = "search", required = false) search: String?,
        principal: Principal
    ): List<ContactViewModel?> {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return contactService.getContacts(userAccount, search)
    }

    @PostMapping("/contact")
    @Operation(summary = "Add contact")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "OK"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "409",
            description = ErrorCodes.NICKNAME_ALREADY_USED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "409",
            description = ErrorCodes.CONTACT_ALREADY_EXISTS,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "422",
            description = ErrorCodes.OWN_ACCOUNT_CONTACT,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun addContact(@RequestBody request: AddOrUpdateContactRequest, principal: Principal) {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val contactUserAccount = userAccountService.getUserAccountByCognitoUsername(request.cognitoUsername)
        contactService.addContact(userAccount, contactUserAccount, request.nickname)
    }

    @PatchMapping("/contact")
    @Operation(summary = "Update contact")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.CONTACT_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "409",
            description = ErrorCodes.NICKNAME_ALREADY_USED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun updateContact(@RequestBody request: AddOrUpdateContactRequest, principal: Principal) {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val contactUserAccount = userAccountService.getUserAccountByCognitoUsername(request.cognitoUsername)
        contactService.updateContact(userAccount, contactUserAccount, request.nickname)
    }

    @DeleteMapping("/contact")
    @Operation(summary = "Delete contact")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.CONTACT_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun deleteContact(@RequestBody request: DeleteContactRequest, principal: Principal) {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val contactUserAccount = userAccountService.getUserAccountByCognitoUsername(request.cognitoUsername)
        contactService.deleteContact(userAccount, contactUserAccount, request.nickname)
    }

    @PostMapping("/sendPushNotification")
    @Operation(summary = "Sends push notification for testing purposes")
    @ApiResponse(responseCode = "200", description = "Notification sent")
    fun sendPushNotification(@RequestBody request: NotificationRequest, principal: Principal) {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        notificationService.sendPushNotifications(userAccount, request.title, request.body)
    }

    @PostMapping("/sendPushNotificationPayment")
    @Operation(summary = "Sends push notification for Payment purposes")
    @ApiResponse(responseCode = "200", description = "Notification sent")
    fun sendPushNotificationPayment(
        @RequestBody request: NotificationRequestPayment,
        principal: Principal,
    ): NotificationData {
        val requesterAccount = userAccountService.getUserAccountByEmail(principal.name)
        val senderAccount = userAccountService.getUserAccountByEmail(request.senderEmail)
        return notificationService.sendPushNotificationsPayment(
            requesterAccount, senderAccount,
            request,
            notificationDataService
        )
    }

    @PostMapping("/saveExternalNotificationRequest")
    @Operation(summary = "Save External Notification Request")
    @ApiResponse(responseCode = "200", description = "ok")
    fun saveExternalNotificationRequest(
        @RequestBody request: NotificationRequestPayment,
        principal: Principal,
    ): NotificationData {
        val requesterAccount = userAccountService.getUserAccountByEmail(principal.name)
        val senderAccount = userAccountService.getUserAccountByEmail(request.senderEmail)
        return notificationService.saveExternalNotificationRequest(
            requesterAccount, senderAccount,
            request,
            notificationDataService
        )
    }

    @PostMapping("/updatePaymentRequest") // updatePaymentRequest  sendNotifyRequestStatus
    @Operation(summary = "Sends Push Notification For Request Status")
    @ApiResponse(responseCode = "200", description = "Notification sent")
    fun sendNotifyRequestStatus(
        @RequestBody request: UpdatePaymentRequest,
        principal: Principal,
    ): NotificationData {
        val senderAccount = userAccountService.getUserAccountByEmail(principal.name)

        // status update

        val notificationData = notificationDataService.updatePaymentStatus(request)
        val requesterAccount = userAccountService.getUserAccountByEmail(notificationData.requesterEmail.toString())

        // Notify requester

        notificationService.notifyRequester(notificationData, requesterAccount)

        return notificationData
    }

    @GetMapping(
        value = [
            "/getRequestStatus"
        ]
    )
    @Operation(summary = "Get list of Request Status (NOT IN USE)")
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
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getRequestStatus(
        principal: Principal,
        @RequestParam(value = "email", required = true) emailParam: String?
    ): List<RequestStatus>? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return emailParam?.let { requestStatusService.getRequestStatus(it, userAccount) }
    }

    @GetMapping("/updateUserNotificationStatus")
    @Operation(summary = "Update User Notification Status")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User found"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun updateUserNotificationStatus(
        @RequestParam(value = "notificationStatus", required = false) notificationStatus: Boolean,
        principal: Principal
    ): UpdateUserNotificationStatusResponse {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return userAccountService.updateUserAccount(userAccount, notificationStatus)
    }

    @PostMapping("/saveTransactionFilter")
    @Operation(summary = "Save Transactions Filter Data")
    @ApiResponse(responseCode = "200", description = "OK")
    fun saveTransactionFilter(
        @RequestBody request: TransactionFilterRequest,
        principal: Principal
    ) {
        print(principal.name.toString() + " @pn")
        print("$principal @pn")
//        val userAccount = userAccountService.getUserAccountByEmail(emailParam.toString())
        val userAccount = userAccountService.getUserAccountByEmail(request.requesterEmail.toString())
        transactionFilterService.saveTransactionFilter(
            userAccount,
            request
        )
    }

    @GetMapping(
        value = [
            "/getTransactionFilter"
        ]
    )
    @Operation(summary = "Get list of Transactions Status")
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
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getTransactionFilter(
        principal: Principal,
        @RequestParam(value = "email", required = true) emailParam: String?
    ): List<TransactionFilter>? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return emailParam?.let { transactionFilterService.getTransactionFilter(it, userAccount) }
    }

    @PostMapping("/saveNotificationStatus")
    @Operation(summary = "Saves Notification Status")
    @ApiResponse(responseCode = "200", description = "OK")
    fun saveNotificationStatus(
        @RequestBody request: NotificationStatusParams,
        principal: Principal
    ) {
        val userAccount = userAccountService.getUserAccountByEmail(request.requesterEmail.toString())
        notificationStatusService.saveRequestStatus(
            userAccount,
            request
        )
    }

    @GetMapping(
        value = [
            "/getNotificationStatus"
        ]
    )
    @Operation(summary = "Get list of Notifications Status")
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
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getNotificationStatus(
        principal: Principal,
        @RequestParam(value = "email", required = true) emailParam: String?
    ): NotificationStatus? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return emailParam?.let { notificationStatusService.getNotificationStatus(it, userAccount) }
    }

    @PostMapping("/updateNotificationData")
    @Operation(summary = "Update Notification For Read Status")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Notification Updated successfully"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.NOTIFICATION_DATA_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun makeReadNotification(
        @RequestBody request: UpdateNotification,
        principal: Principal,
    ): UpdateNotification {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return notificationDataService.updateNotificationData(request)
    }

    @GetMapping("/getProfileDetails")
    @Operation(summary = "Get Wallet user profile Details")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun checkPasscodeSet(
        principal: Principal
    ): ProfileDetailResponse {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return userAccountService.checkPasscodeSet(userAccount)
    }

    @PostMapping("/savePasscode")
    @Operation(summary = "Save Wallet User Passcode ")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User Passcode saved successfully"),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.SALT_KEY_NOT_FOUND},${ErrorCodes.INVALID_ENCRYPTED_PASSCODE} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun savePasscode(
        @RequestBody request: Passcode,
        principal: Principal,
    ): SavePasscodeResponse {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return userAccountService.createPasscode(request, userAccount)
    }
    @PostMapping("/decryptPasscode")
    @Operation(summary = "Check Passcode Wallet User.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Check Passcode Wallet User."),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.INVALID_ENCRYPTED_PASSCODE} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun decryptPasscode(
        @RequestBody request: Passcode,
        principal: Principal,
    ): SavePasscodeResponse {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return userAccountService.decryptPasscode(request.passcodeHash, userAccount)
    }

    @PostMapping("/encryptPasscode")
    @Operation(summary = "Check Passcode Wallet User.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Check Passcode Wallet User."),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.INVALID_ENCRYPTED_PASSCODE} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun encryptPasscode(
        @RequestParam(required = true, name = "passCode") passCode: String,
        principal: Principal,
    ): String {
        return userAccountService.encryptPasscode(passCode)
    }

    @GetMapping("/getPasscodeTitle")
    @Operation(summary = "Get Wallet Passcode title")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getPasscodeTitle(
        principal: Principal
    ): WalletTitleResponse {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return userAccountService.getPasscodeTitle(userAccount)
    }

    @PostMapping("/uploadImage")
    @Operation(summary = "Upload Image to S3 bucket ")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun uploadImage(
        @RequestBody request: Passcode,
        principal: Principal,
    ): ImageResponse {
        userAccountService.getUserAccountByEmail(principal.name)
        return userAccountService.uploadImage(request)
    }

    @PostMapping("/common/sendOTP")
    @Operation(summary = "Send OTP To the given number and email")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "OTP code is sent to the email/Phone Number"),
        ApiResponse(
            responseCode = "400",
            description = "$INVALID_INPUT_FORMAT ($INVALID_EMAIL, $INVALID_PHONE_NUMBER, $INVALID_PASSWORD) - ValidationErrorResponse<br>$UNVERIFIED_PHONE_NUMBER - ErrorResponse<br>",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = UNVERIFIED_PHONE_NUMBER,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "422",
            description = FRAUDULENT_USER,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun sendOTP(@Valid @RequestBody request: OTPRequest) {
        if (request.email == null && request.phoneNumber == null) {
            throw BadRequestException(ErrorCodes.NO_DATA_SENT)
        }
        registrationService.sendOTP(request.email, request.phoneNumber)
    }

    @PostMapping("/common/verifyOTP")
    @Operation(summary = "Verifies given email and phone OTP")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "user is created"),
        ApiResponse(
            responseCode = "400",
            description = "$INVALID_INPUT_FORMAT ($INVALID_EMAIL, $INVALID_PHONE_NUMBER, $INVALID_PASSWORD) - ValidationErrorResponse<br>$UNVERIFIED_PHONE_NUMBER - ErrorResponse<br>" +
                "One of these error codes: https://github.com/firebase/firebase-admin-java/blob/104ab0dcefcbbff24b6b3fc747f3f363854185f2/src/main/java/com/google/firebase/auth/AuthErrorCode.java - ErrorResponse",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = UNVERIFIED_PHONE_NUMBER,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = INCORRECT_CODE,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun verifyOTP(@Valid @RequestBody request: VerifyOTPRequest) {
        registrationService.verifyOTP(request.email, request.emailCode, request.phoneNumber, request.phoneNumberCode)
    }

    @PostMapping("/saveWadzpayMinter")
    @Operation(summary = "Save Wadzpay Minter Wallet")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Saved successfully"),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun saveWadzpayMinter(
        @RequestBody request: WadzpayMinterRequest,
        principal: Principal,
    ): WadzpayMinter {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return userAccountService.saveWadzpayMinter(request)
    }

    @PostMapping("/registration")
    @Operation(summary = "Register Wadzpay Wallet User by login issuance")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User Added successfully"),
        ApiResponse(
            responseCode = "400",
            description = INVALID_EMAIL,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USERS_ALREADY_IN_DATABASE} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND} , ${ErrorCodes.ISSUANCE_WALLET_USER_ALREDY_MAPPED}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun walletUserRegistration(@RequestBody registrationRequest: RegistrationRequest, authentication: Authentication): RegistrationResponse {
        val userAccount = userAccountService.getUserAccountByEmail(authentication.name)
        if (registrationRequest.customerEmail.isNotEmpty()) {
            registrationRequest.customerEmail = registrationRequest.customerEmail.toLowerCase()
        }
        if (registrationRequest.customerId.length < 5 || registrationRequest.customerId.length > 20) {
            throw BadRequestException(ErrorCodes.INVALID_CUSTOMER_ID)
        }
        if (registrationRequest.customerMobile.isNotEmpty() && registrationRequest.customerMobile.length != 13) {
            throw BadRequestException(INVALID_PHONE_NUMBER)
        }
        if (userAccount.issuanceBanks != null) {
            return issuanceWalletService.walletUserRegistration(registrationRequest, userAccount)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }
    @GetMapping("/transactionDetails")
    @Operation(summary = "Get transaction Details by login issuance")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Fetch Data"),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.ISSUANCE_BANK_NOT_FOUND},${ErrorCodes.TRANSACTION_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun transactionDetails(
        request: TransactionDetailsRequest,
        principal: Principal
    ): MutableList<TransactionDetailsResponse> {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (userAccount.issuanceBanks != null) {
            if (request.toDate != null && request.fromDate == null) {
                throw BadRequestException(ErrorCodes.FROM_DATE_REQUIRED)
            }
            return issuanceWalletService.getTransactionDetails(request, userAccount)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }
//
    @GetMapping("/userData")
    @Operation(summary = "Get user by email or phone number")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User found"),
        ApiResponse(
            responseCode = "400",
            description = "$INVALID_EMAIL, $INVALID_PHONE_NUMBER, ${ErrorCodes.PARAMETERS_NOT_SPECIFIED}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getUserData(
        @Email(message = INVALID_EMAIL)
        @RequestParam(value = "email", required = false) email: String?,
        @Pattern(
            regexp = "^\\+[0-9]{7,15}\$",
            message = INVALID_PHONE_NUMBER
        )
        @RequestParam(value = "phoneNumber", required = false) phoneNumber: String?,
        principal: Principal
    ): UserAccount {
        val decryptedUserName = getDecryptedEmail(email.toString())
        var userAccount = decryptedUserName?.let { userAccountService.getUserAccountByEmail(it) }

        phoneNumber?.let {
            userAccount = userAccountService.getUserAccountByPhoneNumber(phoneNumber)
        }
        fledgerService.initialFiatWallet(userAccount)
        return userAccount as UserAccount
        throw BadRequestException(ErrorCodes.PARAMETERS_NOT_SPECIFIED)
    }

    fun getDecryptedEmail(
        data: String
    ): String {
        try {
            return EncoderUtil.getDecoded(SALT_KEY, data)
        } catch (e: Exception) {
            throw BadRequestException(ErrorCodes.INVALID_ID)
        }
    }
}

data class DeleteContactRequest(
    @field: NotEmpty
    val nickname: String?,
    val cognitoUsername: String
)

data class AddOrUpdateContactRequest(
    @field: NotEmpty
    val nickname: String?,
    val cognitoUsername: String
)

data class CreateExpoTokenRequest(
    val expoToken: String ? = null,
    val fcmId: String ? = null,
    val deviceType: DeviceType ? = null
)

data class DeleteExpoTokenRequest(
    val expoToken: String ? = null,
    val fcmId: String? = null
)

data class CreatePhoneRequest(
    @field: Pattern(
        regexp = "^\\+[0-9]{7,15}\$",
        message = INVALID_PHONE_NUMBER
    )
    val phoneNumber: String
)

data class VerifyPhoneRequest(
    @field: Pattern(
        regexp = "^\\+[0-9]{7,15}\$",
        message = INVALID_PHONE_NUMBER
    )
    val phoneNumber: String,
    val code: String
)

data class PreRegisterRequest(
    @field: NotEmpty
    @field: Email(message = INVALID_EMAIL)
    val email: String,

    /*
    Phone number format policy for the current regex:
    *) The original format is from https://www.oreilly.com/library/view/regular-expressions-cookbook/9781449327453/ch04s03.html
    1. It must start with a plus sign.
    2. After plus sign, it must contain only numeric characters.
    3. In total the number of digits include country code must be between 7-15.
     */
    @field: Pattern(
        regexp = "^\\+[0-9]{7,15}\$",
        message = INVALID_PHONE_NUMBER
    )
    val phoneNumber: String,

    /*
    Password format policy for the current regex:
    1. Its length must be between 8 - 128 characters.
    2. It must contain at least 1 alphabet characters, case in-sensitive.
    3. It must contain at least 1 numeric characters.
    4. It must contain at least 1 valid special symbols from ASCII table.
    */
    @field: Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[!@#&:;.,?/_*~%+=<>|`\\\\\"\$\\-()\\[\\]{}^'])[A-Za-z0-9!@#&:;.,?/_*~%+=<>|`\\\\\"\$\\-()\\[\\]{}^']{8,128}\$",
        message = INVALID_PASSWORD
    )
    val password: String,
)

data class VerifyAndCreateRequest(
    @field: Email(message = INVALID_EMAIL)
    val email: String,

    @field: Pattern(
        regexp = "^\\+[0-9]{7,15}\$",
        message = INVALID_PHONE_NUMBER
    )
    val phoneNumber: String,
    val code: String,

    /*
    Password format policy for the current regex:
    1. Its length must be between 8 - 128 characters.
    2. It must contain at least 1 alphabet characters, case in-sensitive.
    3. It must contain at least 1 numeric characters.
    4. It must contain at least 1 valid special symbols from ASCII table.
    */
    @field: Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[!@#&:;.,?/_*~%+=<>|`\\\\\"\$\\-()\\[\\]{}^'])[A-Za-z0-9!@#&:;.,?/_*~%+=<>|`\\\\\"\$\\-()\\[\\]{}^']{8,128}\$",
        message = INVALID_PASSWORD
    )
    val password: String,

    val isMerchantAdmin: Boolean = false,
    val institutionId: String?
)

data class CreateTransactionFromOrderRequest(val order_id: UUID)
data class LoadCustomTokenRequest(
    val amount: BigDecimal,
    val asset: CurrencyUnit
)
data class BcTransactionIdRequest(
    val bcTransId: String
)
data class CreatePeerToPeerTransactionRequest(
    val amount: BigDecimal,
    val asset: CurrencyUnit,
    val receiverUsername: String? = null,
    val receiverEmail: String? = null,
    @field: Pattern(
        regexp = "^\\+[0-9]{7,15}\$",
        message = INVALID_PHONE_NUMBER
    )
    val receiverPhone: String? = null,

    @field: Size(max = 160)
    val description: String? = null,
    val feeConfigData: List<IssuanceCommonController.FeeConfigDataDetails>? = null
)

data class CancelOrderRequest(val order_id: UUID)

data class UpdateNotification(
    val id: Long,
    val isRead: Boolean = false
)
data class UpdateUserNotificationStatusResponse(
    val notificationStatus: Boolean?
)
data class Passcode(
    val passcodeHash: String
)

data class ImageResponse(
    val imageURL: String
)
data class OTPRequest(
    val email: String ? = null,
    val phoneNumber: String ? = null
)
data class VerifyOTPRequest(
    val email: String ? = null,
    val phoneNumber: String ? = null,
    val emailCode: String ? = null,
    val phoneNumberCode: String ? = null
)

data class ProfileDetailResponse(
    val isPasscodeSet: Boolean = false,
    val isActivationFeeCharged: Boolean = false,
    val isBalanceRunningLow: Boolean = false,
    val lowMaintainWalletBalance: BigDecimal? = null,
    val isP2PEnabled: Boolean = false,
    val fiatCurrency: FiatCurrencyUnit?
)

data class WalletTitleResponse(
    val passcodeScreen: PasscodeScreenTextResponse
)
data class PasscodeScreenTextResponse(
    val createPasscode: TitleSubTitleResponse,
    val confirmPasscode: TitleSubTitleResponse,
)
data class TitleSubTitleResponse(
    val title: String,
    val subtitle: String,
)
data class SavePasscodeResponse(
    var message: String? = null
)

enum class DeviceType {
    ANDROID,
    IOS
}
/* This class is using for define the customer type in user account*/
enum class CustomerType {
    ISSUER,
    MERCHANT,
    WALLET_USER
}

data class WadzpayMinterRequest(
    val assetName: CurrencyUnit,
    val assetMintAmount: BigDecimal,
    val assetMintBaseUnit: String ? = null,
    val assetUrl: String ? = null,
    val wadzpayMinterAddress: String
)

data class RegistrationRequest(
    val customerName: String,
    val customerId: String,
    @field: Pattern(
        regexp = "^\\+[0-9]{7,15}\$",
        message = INVALID_PHONE_NUMBER
    )
    val customerMobile: String,
    @field: Email(message = INVALID_EMAIL)
    var customerEmail: String,
    val partnerInstitutionId: String ? = null
)

data class RegistrationResponse(
    val customerId: String? = null,
    val customerName: String,
    val customerRegistrationNumber: Long,
    val customerMobile: String? = null,
    var customerEmail: String? = null,
    val institutionName: String ? = null,
    val institutionId: String ? = null,
    val customerWalletId: String ? = null,
    val customerType: String ? = null,
    val createdDate: Instant ? = null,
    val partnerInstitutionId: String ? = null,
    val status: String? = null
)
data class TransactionDetailsRequest(
    val customerId: String ? = null,
    // val customerEmail: String? = null,
    val fromDate: Instant ? = null,
    val toDate: Instant ? = null,
    var transactionType: IssuanceWalletUserController.TransactionTypeList? = null,
    val tokenName: String ? = null
)
data class TransactionDetailsResponse(
    val customerId: String ? = null,
    val customerName: String ? = null,
    val institutionName: String ? = null,
    val institutionId: String ? = null,
    val transactionId: String ? = null,
    val tokenName: String ? = null,
    var noOfTokens: BigDecimal ? = null,
    val transactionDate: Instant ? = null,
    val transactionType: String? = null,
    val ledgerType: String? = null,
    val transactionStatus: String? = null,
    val receiverName: String? = null,
    val receiverId: String? = null
)

data class UserDataResponse(
    val cognitoUsername: String,
    var email: String?,
    val phoneNumber: String?,
    var notificationStatus: Boolean? = true,
    var merchant: Merchant? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var issuanceBanks: IssuanceBanks? = null,
    var customerType: String ? = null,
    var createdDate: Instant? = null,
    val customerId: String? = null,
    var bearerToken: String? = null
)
