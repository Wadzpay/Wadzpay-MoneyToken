package com.vacuumlabs.wadzpay.user

import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import javax.validation.Valid

@RestController
@RequestMapping("/retail")
@Tag(name = "Retail User Controller")
@Validated
class RetailUserController(
    val userAccountService: UserAccountService,
    val retailUserService: RetailUserService

) {
    @PostMapping("/user/registration")
    @Operation(summary = "Register Wadzpay Wallet User by login issuance")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User Added successfully"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.WRONG_INPUT,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.CUSTOMER_ID_ALREADY_EXISTS,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun retailUserRegistration(@Valid @RequestBody registrationRequest: RetailUserRegistrationRequest, authentication: Authentication): RegistrationResponse? {
        if (registrationRequest.customerId != null) {
            var customerId = registrationRequest.customerId.toString()
            val userAccount = userAccountService.getUserAccountByEmail(authentication.name)
            if (customerId.isEmpty()) {
                throw EntityNotFoundException(ErrorCodes.NO_DATA_SENT)
            }
            if (customerId.isNotEmpty() && customerId.length > 100) {
                throw EntityNotFoundException(ErrorCodes.WRONG_INPUT)
            }
            println("registrationRequest.customerId ==> " + customerId)
            customerId = customerId.replace("\n", " ")
            customerId = customerId.replace("'\'", " ")
            println("registrationRequest.customerId ==> " + customerId)
            if (customerId.isNotEmpty() && (
                customerId.contains(" ") || customerId.contains("/") ||
                    customerId.contains("\\") || customerId.contains("-")
                )
            ) {
                throw EntityNotFoundException(ErrorCodes.WRONG_INPUT)
            }
            if (userAccount.issuanceBanks != null) {
                return retailUserService.retailUserRegistration(customerId, userAccount)
            } else {
                throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
            }
        }
        throw EntityNotFoundException(ErrorCodes.WRONG_INPUT)
    }
    fun checkSpecialChar(username: String): Boolean {
        val regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        return username.matches(regex.toRegex())
    }

    data class RetailUserRegistrationRequest(
        /*Unique customer Id */
        var customerId: Any?
    )

    data class RegistrationResponse(
        val createdDate: Instant? = null,
        val status: String? = null,
        val walletId: String? = null
    )
}
