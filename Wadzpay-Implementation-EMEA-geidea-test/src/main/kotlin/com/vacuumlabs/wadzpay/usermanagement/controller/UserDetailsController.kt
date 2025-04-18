package com.vacuumlabs.wadzpay.usermanagement.controller

import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleDataService
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleListingModule
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.usermanagement.dataclass.DeactivateUserDetailsRequest
import com.vacuumlabs.wadzpay.usermanagement.dataclass.EditUserDetailsRequest
import com.vacuumlabs.wadzpay.usermanagement.dataclass.GetUserDetailsListRequest
import com.vacuumlabs.wadzpay.usermanagement.dataclass.UserDetailsRequest
import com.vacuumlabs.wadzpay.usermanagement.service.DepartmentService
import com.vacuumlabs.wadzpay.usermanagement.service.UserDetailsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@Tag(name = "Level Users Details Controller")
@RequestMapping("/user")
@Validated
data class UserDetailsController(
    val userDetailsService: UserDetailsService,
    val userAccountService: UserAccountService,
    val departmentService: DepartmentService
) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/createUser")
    @Operation(summary = "create User")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun createUser(@Valid @RequestBody userDetailsRequest: UserDetailsRequest): Any {
        return userDetailsService.createUser(userDetailsRequest)
    }
    @PostMapping("/updateUser")
    @Operation(summary = "update User Details")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun updateUser(@Valid @RequestBody request: EditUserDetailsRequest): Any {
        return userDetailsService.updateUser(request)
    }
    @PostMapping("/userFiltersPagination")
    @Operation(summary = "Get list of Users Details")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun fetchUsersFiltersPagination(
        principal: Authentication,
        @RequestBody request: GetUserDetailsListRequest
    ): Any {
        if (request.currentLevel == null) {
            throw EntityNotFoundException(ErrorCodes.NO_DATA_SENT)
        }
        return userDetailsService.fetchUsersFiltersPagination(request)
    }

    @GetMapping("/role/getRolesByUsers")
    @Operation(summary = "Get list of Roles")
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
    fun getRolesByUsers(
        principal: Authentication,
        @RequestParam(value = "currentLevel", required = true) currentLevel: Int,
    ): MutableList<RoleListingModule> {
        return userDetailsService.getRolesByUsers(currentLevel)
    }
    @PostMapping("/role/getRolesByUsers")
    @Operation(summary = "Get list of Roles")
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
    fun getRolesByUsers(
        principal: Authentication,
        @RequestBody request: RoleDataService.RoleListRequest,
    ): MutableList<RoleListingModule> {
        return userDetailsService.getRolesByUsers(request.currentLevel)
    }

    @PostMapping("/deActivateUser")
    @Operation(summary = "Deactivate User")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun deActivateUser(
        principal: Authentication,
        @RequestBody request: DeactivateUserDetailsRequest
    ): Any {
        return userDetailsService.deActivateUser(request)
    }
}
