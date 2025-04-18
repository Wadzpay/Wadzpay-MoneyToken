package com.vacuumlabs.wadzpay.kyc

import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.kyc.models.JumioResponse
import com.vacuumlabs.wadzpay.kyc.models.KycLogRepository
import com.vacuumlabs.wadzpay.user.UserAccountService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/user")
@Tag(name = "User account")
@Validated
class KycController(val userAccountService: UserAccountService, val kycService: KycService, val kycLogRepository: KycLogRepository) {
    val logger = LoggerFactory.getLogger(javaClass)
    @GetMapping(
        value = [
            "/kyc"
        ]
    )
    @Operation(summary = "Create redirectUrl for KYC for a user.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - You need to login",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getKycIframe(principal: Principal): JumioResponse? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        /*  userAccount.kycVerified = VerificationStatus.IN_PROGRESS
          userAccountService.userAccountRepository.save(userAccount)*/
        return kycService.createKycTransaction(userAccount)
    }
}
