package com.vacuumlabs.wadzpay.usermanagement.controller

import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.usermanagement.dataclass.DepartmentData
import com.vacuumlabs.wadzpay.usermanagement.dataclass.DepartmentRequest
import com.vacuumlabs.wadzpay.usermanagement.service.DepartmentService
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
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/department")
@Tag(name = "Department Master Controller")
@Validated
class DepartmentController(
    val userAccountService: UserAccountService,
    val departmentService: DepartmentService
) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)
    @GetMapping("/fetchAll")
    @Operation(summary = "Get list of Department")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun fetchDepartment(
        principal: Authentication
    ): MutableIterable<DepartmentData> {
        return departmentService.fetchDepartment()
    }
    @PostMapping("/create")
    @Operation(summary = "create Department")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful created"),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
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
    fun createDepartment(principal: Authentication, @RequestBody departmentRequest: DepartmentRequest): DepartmentData {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (departmentRequest.departmentName.isEmpty()) {
            throw BadRequestException(ErrorCodes.BAD_REQUEST)
        }
        return departmentService.createDepartment(userAccount, departmentRequest)
    }

    @PostMapping("/update")
    @Operation(summary = "update Department")
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
    fun updateDepartment(principal: Authentication, @RequestBody departmentUpdateRequest: DepartmentData): DepartmentData {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return departmentService.updateDepartment(userAccount, departmentUpdateRequest)
    }
}
