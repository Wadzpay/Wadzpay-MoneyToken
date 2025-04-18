package com.vacuumlabs.wadzpay.accountowner

import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.BDOWithdrawal
import com.vacuumlabs.wadzpay.ledger.service.BDOWithdrawalService
import com.vacuumlabs.wadzpay.ledger.service.BDOWithdrawalViewModel
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.Instant

@RestController
@RequestMapping
@Tag(name = "BDO bank withdrawal methods")
@Validated
class BDOWithdrawalController(
    val bdoOWithdrawalService: BDOWithdrawalService,
    val accountOwnerService: AccountOwnerService,
) {

    @PostMapping("/bdo/withdraw/card")
    @Operation(summary = "BDO withdrawal by card process")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
    )
    fun bdoWithdrawByCardTransaction(
        principal: Authentication,
        @RequestBody request: BDOWithdrawByCardRequest,
    ): BDOWithdrawal? {
        println("Controller card process")
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)
        return bdoOWithdrawalService.bdoWithdrawByCardTransaction(request, accountOwner, principal)
    }

    @PostMapping("/bdo/withdraw/wallet")
    @Operation(summary = "BDO withdrawal by wallet process")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
    )
    fun bdoWithdrawByWalletTransaction(
        principal: Authentication,
        @RequestBody request: BDOWithdrawByWalletRequest,
    ): BDOWithdrawal? {
        println("Controller wallet process")
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)
        return bdoOWithdrawalService.bdoWithdrawByWalletTransaction(request, accountOwner, principal)
    }

    @PostMapping("/bdo/register/wallet")
    @Operation(summary = "BDO register new card/wallet address")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
    )
    fun bdoRegisterNewWallet(
        principal: Authentication,
        @RequestBody request: BDORegisterRequest,
    ): BDOWithdrawalViewModel? {
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)
        return bdoOWithdrawalService.bdoRegisterNewWallet(request, accountOwner, principal)
    }
}

data class BDOWithdrawByCardRequest(
    val cardNumber: String,
    val amount: BigDecimal,
    val asset: CurrencyUnit,
)

data class BDOWithdrawByWalletRequest(
    val walletAddress: String,
    val amount: BigDecimal,
    val asset: CurrencyUnit,
)

data class BDOWithdrawRequest(
    val cardNumber: String,
    val walletAddress: String,
    val amount: BigDecimal,
    val asset: CurrencyUnit,
    val created_at: Instant,
)

data class BDORegisterRequest(
    val cardNumber: String,
    val walletAddress: String,
    val totalAmount: BigDecimal,
    val asset: CurrencyUnit,
    val createdAt: Instant,
)
