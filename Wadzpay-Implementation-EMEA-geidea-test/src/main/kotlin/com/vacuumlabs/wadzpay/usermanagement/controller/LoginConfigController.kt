package com.vacuumlabs.wadzpay.usermanagement.controller

import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.usermanagement.dataclass.CreateLoginConfigurationRequest
import com.vacuumlabs.wadzpay.usermanagement.dataclass.LoginConfigurationData
import com.vacuumlabs.wadzpay.usermanagement.dataclass.UpdateLoginConfigurationRequest
import com.vacuumlabs.wadzpay.usermanagement.service.LoginConfigurationService
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

@RestController
@Tag(name = "Login Controller")
@RequestMapping("/loginConfig")
@Validated
data class LoginConfigController(
    val userDetailsService: UserDetailsService,
    val userAccountService: UserAccountService,
    val loginConfigurationService: LoginConfigurationService
) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/getConfigDataByAggregatorId")
    @Operation(summary = "Get Login configuration by aggregatorId")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getConfigDataByAggregatorId(
        principal: Authentication,
        @RequestParam(required = true, name = "aggregatorId") aggregatorId: Long
    ): MutableIterable<LoginConfigurationData> {
        return loginConfigurationService.getConfigDataByAggregatorId(aggregatorId)
    }
    @PostMapping("/createLoginConfiguration")
    @Operation(summary = "create Login configuration")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful created"),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
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
    fun createLoginConfiguration(principal: Authentication, @RequestBody createLoginConfigurationRequest: CreateLoginConfigurationRequest): LoginConfigurationData {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return loginConfigurationService.createLoginConfiguration(userAccount, createLoginConfigurationRequest)
    }

    @PostMapping("/updateLoginConfiguration")
    @Operation(summary = "update Login Configuration")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
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
    fun updateLoginConfiguration(principal: Authentication, @RequestBody updateLoginConfigurationRequest: UpdateLoginConfigurationRequest): LoginConfigurationData {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return loginConfigurationService.updateLoginConfiguration(userAccount, updateLoginConfigurationRequest)
    }
}
