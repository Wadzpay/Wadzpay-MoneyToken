package com.vacuumlabs.wadzpay.merchant

import com.vacuumlabs.wadzpay.accountowner.AccountOwnerService
import com.vacuumlabs.wadzpay.auth.Role
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.emailSms.service.EmailSMSSenderService
import com.vacuumlabs.wadzpay.ledger.service.TransactionService
import com.vacuumlabs.wadzpay.merchant.model.Merchant
import com.vacuumlabs.wadzpay.services.CognitoService
import com.vacuumlabs.wadzpay.services.InvitedMerchants
import com.vacuumlabs.wadzpay.services.RedisService
import com.vacuumlabs.wadzpay.user.UserAccountRepository
import com.vacuumlabs.wadzpay.user.UserAccountService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import javax.validation.Valid
import javax.validation.constraints.Email

@RestController
@RequestMapping("/merchantDashboard")
@Tag(name = "Merchant dashboard")

class MerchantDashboardController(
    val userAccountService: UserAccountService,
    val userAccountRepository: UserAccountRepository,
    val merchantService: MerchantService,
    val redisService: RedisService,
    val merchantDashboardService: MerchantDashboardService,
    val cognitoService: CognitoService,
    val accountOwnerService: AccountOwnerService,
    val transactionService: TransactionService,
    val emailSMSSenderService: EmailSMSSenderService
) {
    @PostMapping("/admin/merchant")
    @Operation(summary = "Create a merchant and assign logged in user as its MERCHANT_ADMIN")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Merchant created"),
        ApiResponse(
            responseCode = "409",
            description = ErrorCodes.MERCHANT_ALREADY_EXISTS,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun createMerchant(@RequestBody createMerchantRequest: CreateMerchantRequest, principal: Principal): Merchant {
        val merchant = merchantService.createMerchantTwo(createMerchantRequest)

        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        userAccount.merchant = merchant
        userAccountRepository.save(userAccount)

        return merchant
    }

    @PostMapping("/admin/invite")
    @Operation(
        summary = "Creates an invitation for a user to merchant dashboard with a specified role." +
            " Invitation is valid for a week." + " Roles can be MERCHANT_READER/MERCHANT_ADMIN/MERCHANT_MERCHANT/MERCHANT_SUPERVISOR/MERCHANT_POSOPERATOR"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User was invited successfully"),
        ApiResponse(
            responseCode = "400",
            description = "${ErrorCodes.INVALID_EMAIL},${ErrorCodes.INVALID_ROLE}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.MERCHANT_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun inviteUser(
        @Valid @RequestBody request: InviteUserRequest,
        authentication: Authentication
    ) {
        if (!request.role.isInvitable()) {
            throw BadRequestException(ErrorCodes.INVALID_ROLE)
        }

        val user = userAccountService.getUserAccountByEmail(authentication.name)

        redisService.inviteUser(
            request.email, request.role,
            user.merchant?.id
                ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)
        )
    }

    @GetMapping("/invite")
    @Operation(summary = "Checks if invitation for an email is present")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Status of the invite retrieved"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_EMAIL,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.MERCHANT_NOT_FOUND}, ${ErrorCodes.INVITATION_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )

    fun getInvitation(@Valid @Email(message = ErrorCodes.INVALID_EMAIL) email: String): Invitation {
        return merchantDashboardService.getInvitation(email)
            ?: throw EntityNotFoundException(ErrorCodes.INVITATION_NOT_FOUND)
    }
    @GetMapping("/getAllInvitedUsers")
    @Operation(summary = "Get All Invites For logged in merchant.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Status of the invite retrieved"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_EMAIL,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.MERCHANT_NOT_FOUND}, ${ErrorCodes.INVITATION_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun checkInvitation(principal: Authentication): ArrayList<InvitedMerchants> {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val merchant =
            userAccount.merchant ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)
        return merchantDashboardService.getInvitations(merchant.id)
    }

    @GetMapping("/getAllUsersOfMerchant")
    @Operation(summary = "Get All merchant For logged in merchant.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Status of the invite retrieved"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_EMAIL,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.MERCHANT_NOT_FOUND}, ${ErrorCodes.INVITATION_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getAllUsersOfMerchant(principal: Authentication): ArrayList<UserAccountService.DashboardMerchantList> {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val merchant =
            userAccount.merchant ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)
        return userAccountService.getUserAccountByMerchantId(merchant.id)
    }

    @PostMapping("/admin/disable")
    @Operation(summary = "Disables user by email")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User was disabled"),
        ApiResponse(
            responseCode = "400",
            description = "${ErrorCodes.INVALID_EMAIL}, ${ErrorCodes.CANNOT_DISABLE_OWN_ACCOUNT}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun disableUser(@Valid @RequestBody request: DisableUserRequest, authentication: Authentication) {
        val admin = userAccountService.getUserAccountByEmail(authentication.name)
        val userToDisable = userAccountService.getUserAccountByEmail(request.email)

        if (userToDisable.merchant != admin.merchant) {
            throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
        }

        if (request.email == authentication.name) {
            throw BadRequestException(ErrorCodes.CANNOT_DISABLE_OWN_ACCOUNT)
        }

        cognitoService.disableUser(userToDisable)
    }

    @PostMapping("/admin/enable")
    @Operation(summary = "Enables user by email")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User was enabled"),
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
    fun enableUser(@Valid @RequestBody request: EnableUserRequest, authentication: Authentication) {
        val admin = userAccountService.getUserAccountByEmail(authentication.name)
        val userToEnable = userAccountService.getUserAccountByEmail(request.email)

        if (userToEnable.merchant != admin.merchant) {
            throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
        }

        cognitoService.enableUser(userToEnable)
    }
}

data class InviteUserRequest(
    @field: Email(message = ErrorCodes.INVALID_EMAIL)
    val email: String,
    val role: Role
)

data class DisableUserRequest(@field: Email(message = ErrorCodes.INVALID_EMAIL) val email: String)

data class EnableUserRequest(@field: Email(message = ErrorCodes.INVALID_EMAIL) val email: String)
