package com.vacuumlabs.wadzpay.fledger

import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.viewmodels.TransactionViewModel
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.security.Principal

@RestController
@RequestMapping
@ConditionalOnProperty(
    prefix = "appconfig", name = ["production"], havingValue = "false"
)
@Tag(name = "CBD Fake Ledger")
@Validated
class FledgerController(
    val fledgerService: FledgerService,
    val userAccountService: UserAccountService
) {
    @PostMapping("/sellDigitalCurrency")
    @Operation(summary = "Sell Digtal Currency get Fiat Currency")
    @ApiResponse(responseCode = "200", description = "Transactions were added")
    fun sellDigitalCurrency(
        @RequestBody sellTransactionRequest: SellTransactionRequest,
        principal: Principal
    ): TransactionViewModel? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return fledgerService.sellCrypto(sellTransactionRequest, userAccount, true)
    }

    fun sellDigitalCurrencyFix(
        principal: Principal,
        sellTransactionRequest: SellTransactionRequest
    ) {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        fledgerService.sellCrypto(sellTransactionRequest, userAccount, true)
    }

    @PostMapping("/buyDigitalCurrency")
    @Operation(summary = "Buy Fiat to Digital Currency ")
    @ApiResponse(responseCode = "200", description = "Transactions were added")
    fun buyDigitalCurrency(
        @RequestBody buyTransactionRequest: CreateBuyTransactionRequest,
        principal: Principal
    ): TransactionViewModel? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return if (buyTransactionRequest.fiatAmount != null && buyTransactionRequest.fiatAmount > BigDecimal.ZERO) {
            if (buyTransactionRequest.fiatAsset == null) {
                throw BadRequestException(ErrorCodes.INVALID_ASSET_TYPE)
            } else {
                fledgerService.buyCryptoUsingFiat(buyTransactionRequest, userAccount, true)
            }
        } else {
            fledgerService.buyCrypto(
                buyTransactionRequest.digitalAmount ?: BigDecimal.ONE,
                buyTransactionRequest.digitalAsset,
                userAccount,
                true,
                buyTransactionRequest.fiatAsset ?: FiatCurrencyUnit.USD,
                buyTransactionRequest.fiatAmount ?: BigDecimal.ONE,
                buyTransactionRequest.digitalAsset,
                buyTransactionRequest.digitalAmount ?: BigDecimal.ONE
            )
        }
    }

    @PostMapping("/depositFiat")
    @Operation(summary = "Deposit Fiat in to user wallet from Bank Account")
    @ApiResponse(responseCode = "200", description = "Transactions were added")
    fun depositFiat(
        @RequestParam(required = true, name = "countryCode") countryCode: CountryCode,
        @RequestBody depositRequest: DepositRequest,
        principal: Principal
    ): TransactionViewModel? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return fledgerService.createDepositFiat(depositRequest, userAccount, true)
    }

    @PostMapping("/withdrawFiat")
    @Operation(summary = "Withdraw Fiat from user wallet to Bank Account")
    @ApiResponse(responseCode = "200", description = "Transactions were added")
    fun withdrawFiat(
        @RequestParam(required = true, name = "countryCode") countryCode: CountryCode,
        @RequestBody withdrawRequest: WithdrawRequest,
        principal: Principal
    ): TransactionViewModel? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (withdrawRequest.fiatType == null && withdrawRequest.fiatAmount == null) {
            throw BadRequestException(ErrorCodes.INVALID_ASSET_TYPE)
        }
        return fledgerService.createWithdrawFiat(withdrawRequest, userAccount, true)
    }

    @PostMapping("/addUserBankAccount")
    @Operation(summary = "Add user Bank Account")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Bank Account were added")
    )
    fun addUserBankAccount(
        principal: Principal,
        @RequestParam(required = true, name = "countryCode") countryCode: CountryCode,
        @RequestBody userBankAccount: AddUserBankAccountRequest
    ): FledgerService.UserBankAccountModel? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return fledgerService.addUserBankAccount(userBankAccount, countryCode, userAccount)
    }

    data class CreateSwapTransactionRequest(
        val sellDigitalAmount: BigDecimal,
        val sellDigitalCurrencyUnit: CurrencyUnit,
        val buyDigitalCurrencyUnit: CurrencyUnit,
    )

    data class SellTransactionRequest(
        val digitalAmount: BigDecimal,
        val fiatAsset: FiatCurrencyUnit,
        val digitalAsset: CurrencyUnit = CurrencyUnit.USDT,
        val exchangeRate: BigDecimal?,
        val finalFiatAmount: BigDecimal?
    )

    data class CreateBuyTransactionRequest(
        val digitalAmount: BigDecimal?,
        val fiatAmount: BigDecimal?,
        val fiatAsset: FiatCurrencyUnit?,
        val digitalAsset: CurrencyUnit = CurrencyUnit.BTC,
        val exchangeRate: BigDecimal?,
        val finalDigitalAmount: BigDecimal?
    )

    data class DepositRequest(
        val fiatType: FiatCurrencyUnit,
        val fiatAmount: BigDecimal,
        val bankAccountNumber: String? = null
    )

    data class WithdrawRequest(
        val fiatType: FiatCurrencyUnit,
        val fiatAmount: BigDecimal,
        val bankAccountNumber: String? = null
    )

    data class AddUserBankAccountRequest(
        val bankAccountNumber: String? = null,
        val accountHolderName: String? = null,
        val ifscCode: String? = null,
        val branchName: String? = null
    )

    enum class CountryCode {
        AE,
        IN,
        MY,
        IE
    }
}
