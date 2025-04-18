package com.vacuumlabs.wadzpay.control

import com.vacuumlabs.wadzpay.user.UserInitializerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Migration controller")
@Validated
@RequestMapping("/migration")
class MigrationController(
    val userInitializerService: UserInitializerService
) {

    @PostMapping("/createWadzpayMerchantAccounts")
    @Operation(summary = "Create merchant user that manages fees collection account and assign all WADZPAY_ADMIN users to it")
    @ApiResponse(responseCode = "200", description = "Merchant user was created and users were assigned to it")
    fun createWadzpayMerchantAccounts() {
        userInitializerService.createWadzpayMerchantAccounts()
    }
}
