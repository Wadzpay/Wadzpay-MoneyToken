package com.vacuumlabs.wadzpay.user

import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.services.CognitoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import javax.validation.constraints.Email

@RestController
@RequestMapping("/user")
@Tag(name = "User account test")
@Validated
@ConditionalOnProperty(
    prefix = "appconfig", name = ["production"], havingValue = "false"
)
class UserAccountTestController(
    val userAccountService: UserAccountService,
    val cognitoService: CognitoService
) {

    @DeleteMapping("")
    @Operation(summary = "Deletes currently signed in user")
    @Transactional
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "user is deleted"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun deleteUserAccount(principal: Principal) {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        // userAccountService.deleteUser(userAccount)
        cognitoService.disableUser(userAccount)
    }

    @PostMapping("/verifyEmailOnAWS")
    @Operation(summary = "Verify Email On AWS")
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
    fun verifyEmailOnAWS(
        @Email(message = ErrorCodes.INVALID_EMAIL)
        @RequestParam(value = "email", required = true) email: String,
        principal: Principal,
    ): Boolean {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return userAccountService.verifyEmailOnAWS(email)
    }
}
