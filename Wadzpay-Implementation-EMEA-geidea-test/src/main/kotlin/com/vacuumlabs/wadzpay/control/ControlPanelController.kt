package com.vacuumlabs.wadzpay.control

import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.exchange.ExchangeService
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.TransactionDirection
import com.vacuumlabs.wadzpay.ledger.service.TransactionService
import com.vacuumlabs.wadzpay.merchant.RequestPaymentService
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.FiatSubAccount
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.user.UserInitializerService
import com.vacuumlabs.wadzpay.viewmodels.TransactionViewModel
import com.vacuumlabs.wadzpay.viewmodels.toViewModel
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.math.RoundingMode
import java.security.Principal
import kotlin.random.Random

/*This controller is only visible in development environment.
  It is used for making the manual testing process easier.
 */

@RestController
@ConditionalOnProperty(
    prefix = "appconfig", name = ["production"], havingValue = "false"
)
@Tag(name = "Control panel")
@Validated
class ControlPanelController(
    val userInitializerService: UserInitializerService,
    val userAccountService: UserAccountService,
    val transactionService: TransactionService,
    val exchangeService: ExchangeService,
    val requestPaymentService: RequestPaymentService
) {

    @PostMapping("/addFakeUserData")
    @Operation(
        summary = "Adds fake merchant, users and transactions to the database.\n" +
            "It also adds them to the Cognito if they are not present there.\n" +
            "Hint: Go to UserInitializerService to find out users credentials, which can be used later for generating JWT tokens."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Users were added"),
    )
    fun addFakeUsers() {
        userInitializerService.initializeUsers()
    }

    @PostMapping("/removeFakeUserData")
    @Operation(
        summary = "Removes fake merchant, users, transactions from the database.\n"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successfully removed fake users"),
    )
    fun removeFakeUsers() {
        userInitializerService.deleteAllUsers()
    }

    @PostMapping("/addFakeTransaction")
    @Operation(summary = "Adds a fake transaction from a fake merchant to the current user")
    @ApiResponse(responseCode = "200", description = "Transactions were added")
    fun addFakeTransaction(
        @RequestBody createFakeTransactionRequest: CreateFakeTransactionRequest?,
        principal: Principal
    ): TransactionViewModel {

        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val merchant = userInitializerService.getFakeMerchant()
        val request = createFakeTransactionRequest ?: CreateFakeTransactionRequest()
        val transaction = userInitializerService.createFakeTransaction(
            merchant.account.subaccounts.find { it.asset == request.asset.toString() }!!,
            userAccount.account.subaccounts.find { it.asset == request.asset.toString() }!!,
            request.asset,
            request.amount
        )
        transactionService.showPushNotifications(transaction)
        return transaction.toViewModel(direction = TransactionDirection.INCOMING)
    }

    @PostMapping("/buyFakeTransaction")
    @Operation(summary = "Adds a Buy Fake transaction from a fake merchant to the current user")
    @ApiResponse(responseCode = "200", description = "Transactions were added")
    fun buyFakeTransaction(
        fakeBuyTransactionRequest: CreateFakeBuyTransactionRequest,
        principal: Principal
    ): TransactionViewModel? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return if (fakeBuyTransactionRequest.fiatAmount != null && fakeBuyTransactionRequest.fiatAmount > BigDecimal.ZERO) {
            if (fakeBuyTransactionRequest.fiatAsset == null) {
                throw BadRequestException(ErrorCodes.INVALID_ASSET_TYPE)
            } else {
                requestPaymentService.buyFakeCryptoUsingFiat(fakeBuyTransactionRequest, userAccount, true)
            }
        } else {
            requestPaymentService.buyFakeCrypto(
                fakeBuyTransactionRequest.digitalAmount ?: BigDecimal.ONE,
                fakeBuyTransactionRequest.digitalAsset,
                userAccount,
                true,
                fakeBuyTransactionRequest.fiatAsset ?: FiatCurrencyUnit.USD,
                fakeBuyTransactionRequest.fiatAmount ?: BigDecimal.ONE,
                fakeBuyTransactionRequest.digitalAsset,
                fakeBuyTransactionRequest.digitalAmount ?: BigDecimal.ONE,
                fakeBuyTransactionRequest.isSibos
            )
        }
    }

    @GetMapping("/getFiatBalance")
    @Operation(summary = "GetFiat Balance of a user")
    @ApiResponse(responseCode = "200", description = "Transactions were added")
    fun getFiatBalance(
        principal: Principal
    ): MutableList<FiatSubAccount> {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val fiatList = userAccount.account.fiatSubAccount
        val iterate = fiatList.listIterator()
        while (iterate.hasNext()) {
            val oldValue = iterate.next()
            if (oldValue.balance.stripTrailingZeros().scale() > 2) {
                oldValue.balance = oldValue.balance.setScale(2, RoundingMode.UP)
            }
        }
        return fiatList
    }

    @PostMapping("/sellFakeTransaction")
    @Operation(summary = "Adds a Sell Fake transaction from a fake merchant to the current user")
    @ApiResponse(responseCode = "200", description = "Transactions were added")
    fun sellFakeTransaction(
        @RequestBody fakeSellTransactionRequest: CreateFakeSellTransactionRequest,
        principal: Principal
    ): TransactionViewModel? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return requestPaymentService.sellCrypto(fakeSellTransactionRequest, userAccount, true)
    }

    @PostMapping("/convertFakeTransaction")
    @Operation(summary = "Adds a Swap Fake transaction from a fake merchant to the current user")
    @ApiResponse(responseCode = "200", description = "Transactions were added")
    fun convertFakeTransaction(
        @RequestBody createFakeSwapTransactionRequest: CreateFakeSwapTransactionRequest,
        principal: Principal
    ): TransactionViewModel? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)

        return requestPaymentService.swapDigitalCurrency(createFakeSwapTransactionRequest, userAccount)
    }

    @PostMapping("/removeTransactions")
    @Operation(summary = "Removes fake transactions from the fake merchant to current user")
    @ApiResponse(responseCode = "200", description = "Transactions were removed")
    fun removeFakeTransaction(principal: Principal) {
        userInitializerService.deleteTransactions(
            userAccountService.getUserAccountByEmail(principal.name)
        )
    }
}

data class CreateFakeSwapTransactionRequest(
    val sellDigitalAmount: BigDecimal,
    val sellDigitalCurrencyUnit: CurrencyUnit,
    val buyDigitalCurrencyUnit: CurrencyUnit,
    val isSibos: Boolean = false
)

data class CreateFakeSellTransactionRequest(
    val digitalAmount: BigDecimal,
    val fiatAsset: FiatCurrencyUnit,
    val digitalAsset: CurrencyUnit = CurrencyUnit.USDT,
    val isSibos: Boolean
)

data class CreateFakeTransactionRequest(
    val amount: BigDecimal = Random.nextInt(1, 10).toBigDecimal(),

    val asset: CurrencyUnit = CurrencyUnit.BTC
)

data class CreateFakeBuyTransactionRequest(
    val digitalAmount: BigDecimal? = Random.nextInt(1, 10).toBigDecimal(),
    val fiatAmount: BigDecimal? = BigDecimal.ZERO,
    val fiatAsset: FiatCurrencyUnit?,
    val digitalAsset: CurrencyUnit = CurrencyUnit.BTC,
    val isSibos: Boolean = false
)

data class NotificationRequest(
    val title: String,
    val body: String
)

data class NotificationRequestPayment(
    val senderEmail: String,
    val digitalCurrency: String,
    val amount: String,
    val time: String
)
