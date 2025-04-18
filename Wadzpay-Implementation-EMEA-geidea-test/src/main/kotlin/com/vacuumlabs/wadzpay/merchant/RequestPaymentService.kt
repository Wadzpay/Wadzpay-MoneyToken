package com.vacuumlabs.wadzpay.merchant

import com.vacuumlabs.ROUNDING_LIMIT
import com.vacuumlabs.vuba.ledger.service.LedgerService
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.UnprocessableEntityException
import com.vacuumlabs.wadzpay.control.CreateFakeBuyTransactionRequest
import com.vacuumlabs.wadzpay.control.CreateFakeSellTransactionRequest
import com.vacuumlabs.wadzpay.control.CreateFakeSwapTransactionRequest
import com.vacuumlabs.wadzpay.exchange.ExchangeService
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.AccountRepository
import com.vacuumlabs.wadzpay.ledger.model.Subaccount
import com.vacuumlabs.wadzpay.ledger.model.TransactionDirection
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.FiatSubAccount
import com.vacuumlabs.wadzpay.merchant.model.FiatSubAccountRepository
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.user.UserInitializerService
import com.vacuumlabs.wadzpay.viewmodels.TransactionViewModel
import com.vacuumlabs.wadzpay.viewmodels.toViewModel
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class RequestPaymentService(
    val accountRepository: AccountRepository,
    val fiatSubAccountRepository: FiatSubAccountRepository,
    val exchangeService: ExchangeService,
    val userInitializerService: UserInitializerService,
    val ledgerService: LedgerService
) {

    fun buyFakeCryptoUsingFiat(
        fakeBuyTransactionRequest: CreateFakeBuyTransactionRequest,
        userAccount: UserAccount,
        logTransactionEntry: Boolean
    ): TransactionViewModel? {
        val fiatAmount = fakeBuyTransactionRequest.fiatAmount ?: BigDecimal.TEN
        val fiatSubAccount =
            userAccount.account.fiatSubAccount.find { it.fiatasset == fakeBuyTransactionRequest.fiatAsset }
        return if (fiatSubAccount == null) {
            return if (createFiatSubAccount(
                    userAccount, fakeBuyTransactionRequest.fiatAsset ?: FiatCurrencyUnit.SGD,
                    BigDecimal.ZERO
                )
            ) {
                throw BadRequestException(ErrorCodes.SUBACCOUNT_NOT_FOUND)
            } else {
                throw BadRequestException(ErrorCodes.SUBACCOUNT_NOT_FOUND)
            }
        } else {
            val balance = fiatSubAccount.balance
            if (balance < fiatAmount) {
                throw UnprocessableEntityException(ErrorCodes.INSUFFICIENT_FUNDS)
            }
            fiatSubAccount.balance = (balance - fiatAmount)
            fiatSubAccountRepository.save(fiatSubAccount)

            var exchangeRate = exchangeService.getExchangeRateFake(
                fakeBuyTransactionRequest.digitalAsset,
                fakeBuyTransactionRequest.fiatAsset!!
            )
            if (fakeBuyTransactionRequest.isSibos) {
                exchangeRate = exchangeService.getExchangeRate(
                    fakeBuyTransactionRequest.digitalAsset,
                    fakeBuyTransactionRequest.fiatAsset!!
                )
            }
            val cryptoAmount = (fiatAmount * exchangeRate)
            buyFakeCrypto(
                cryptoAmount,
                fakeBuyTransactionRequest.digitalAsset,
                userAccount,
                logTransactionEntry,
                fakeBuyTransactionRequest.fiatAsset,
                fakeBuyTransactionRequest.fiatAmount ?: BigDecimal.ZERO,
                fakeBuyTransactionRequest.digitalAsset,
                fakeBuyTransactionRequest.digitalAmount ?: BigDecimal.ZERO,
                fakeBuyTransactionRequest.isSibos
            )
        }
    }

    private fun createFiatSubAccount(
        userAccount: UserAccount,
        fiatCurrencyUnit: FiatCurrencyUnit,
        initalBalace: BigDecimal
    ): Boolean {
        val fiatSubAccount = FiatSubAccount(userAccount.account.reference, userAccount.account, fiatCurrencyUnit)
        fiatSubAccount.balance = initalBalace
        userAccount.account.fiatSubAccount.add(fiatSubAccount)
        accountRepository.save(userAccount.account)
        fiatSubAccountRepository.save(fiatSubAccount)
        return true
    }

    fun buyFakeCrypto(
        cryptoAmount: BigDecimal,
        digitalAsset: CurrencyUnit,
        userAccount: UserAccount,
        logTransactionEntry: Boolean,
        fiatAsset: FiatCurrencyUnit,
        fiatAmount: BigDecimal,
        toDigitalAsset: CurrencyUnit,
        toDigitalAssetAmount: BigDecimal,
        isSibos: Boolean
    ): TransactionViewModel? {
        val merchant = userInitializerService.getFakeMerchant()
        val transaction = userInitializerService.createFakeBuyTransaction(
            merchant.account.subaccounts.find { it.asset == digitalAsset.toString() }!!,
            userAccount.account.subaccounts.find { it.asset == digitalAsset.toString() }!!,
            digitalAsset,
            cryptoAmount,
            logTransactionEntry, fiatAsset, fiatAmount, toDigitalAsset, toDigitalAssetAmount, isSibos
        )
        return transaction?.toViewModel(direction = TransactionDirection.INCOMING)
    }

    fun sellCrypto(
        fakeSellTransactionRequest: CreateFakeSellTransactionRequest,
        userAccount: UserAccount,
        createTransactionLog: Boolean
    ): TransactionViewModel? {

        return sellFakeCrypto(
            fakeSellTransactionRequest.digitalAmount,
            fakeSellTransactionRequest.digitalAsset,
            fakeSellTransactionRequest.isSibos,
            fakeSellTransactionRequest.fiatAsset,
            userAccount, createTransactionLog
        )
    }

    private fun sellFakeCrypto(
        digitalAmount: BigDecimal,
        digitalAsset: CurrencyUnit,
        isSibos: Boolean,
        fiatType: FiatCurrencyUnit = FiatCurrencyUnit.SGD,
        userAccount: UserAccount,
        createTransactionLog: Boolean
    ): TransactionViewModel? {
        val balance =
            getCryptoBalance(userAccount.account.getSubaccountByAsset(digitalAsset))
        if (balance < digitalAmount) {
            throw UnprocessableEntityException(ErrorCodes.INSUFFICIENT_FUNDS)
        }
        val merchant = userInitializerService.getFakeMerchant()
        var exchangeRate = exchangeService.getExchangeRateFake(
            digitalAsset,
            fiatType
        )
        if (isSibos) {
            exchangeRate = exchangeService.getExchangeRate(
                digitalAsset,
                fiatType
            )
        }
        val fiatValueAfterSell = digitalAmount / exchangeRate

        val fiatSubAccount =
            userAccount.account.fiatSubAccount.find { it.fiatasset == fiatType }
        if (fiatSubAccount == null) {
            createFiatSubAccount(userAccount, fiatType, fiatValueAfterSell)
        }

        // Round of fiatValue after sell digital Amount
        var digitalAmountSave = digitalAmount
        if (digitalAmount.stripTrailingZeros().scale() > digitalAsset.maximumNumberOfDigits.toInt()) {
            digitalAmountSave = digitalAmountSave.setScale(digitalAsset.maximumNumberOfDigits.toInt(), RoundingMode.UP).stripTrailingZeros()
        }

        var fiatAmountSave = fiatValueAfterSell
        if (fiatValueAfterSell.stripTrailingZeros().scale() > ROUNDING_LIMIT) {
            fiatAmountSave = fiatValueAfterSell.setScale(ROUNDING_LIMIT, RoundingMode.DOWN).stripTrailingZeros()
        }

        val transaction = userInitializerService.createFakeSellTransaction(
            userAccount.account.subaccounts.find { it.asset == digitalAsset.toString() }!!,
            merchant.account.subaccounts.find { it.asset == digitalAsset.toString() }!!,
            digitalAsset,
            digitalAmountSave,
            createTransactionLog, fiatType, fiatAmountSave
        )

        if (createTransactionLog) {
            // Not a swap. Fiat needs to be transferred to user.
            creditFiatToUser(digitalAmount, digitalAsset, fiatType, userAccount, fiatAmountSave)
        }

        return transaction?.toViewModel(direction = TransactionDirection.OUTGOING)
    }

    private fun creditFiatToUser(
        digitalAmount: BigDecimal,
        digitalAsset: CurrencyUnit,
        fiatType: FiatCurrencyUnit,
        userAccount: UserAccount,
        fiatValueAfterSell: BigDecimal
    ) {
        val fiatSubAccount =
            userAccount.account.fiatSubAccount.find { it.fiatasset == fiatType }
        if (fiatSubAccount != null) {
            val fiatBalance = fiatSubAccount.balance

            var fiatValueToCredit = fiatBalance + fiatValueAfterSell
            // Round of fiat value to credit upto 2 decimal place
            if (fiatValueToCredit.stripTrailingZeros().scale() > ROUNDING_LIMIT) {
                fiatValueToCredit = fiatValueAfterSell.setScale(ROUNDING_LIMIT, RoundingMode.DOWN).stripTrailingZeros()
            }
            fiatSubAccount.balance = fiatValueToCredit
            fiatSubAccountRepository.save(fiatSubAccount)
        }
    }

    fun getCryptoBalance(subaccount: Subaccount): BigDecimal =
        ledgerService.getSubaccount(subaccount.reference).get().balance

    fun swapDigitalCurrency(
        request: CreateFakeSwapTransactionRequest,
        userAccount: UserAccount
    ): TransactionViewModel? {
        sellFakeCrypto(
            request.sellDigitalAmount,
            request.sellDigitalCurrencyUnit,
            request.isSibos,
            userAccount = userAccount,
            createTransactionLog = false
        )

        var exchangeRateSell = exchangeService.getExchangeRateFake(
            request.sellDigitalCurrencyUnit,
            FiatCurrencyUnit.USD
        )
        if (request.isSibos) {
            exchangeRateSell = exchangeService.getExchangeRate(
                request.sellDigitalCurrencyUnit,
                FiatCurrencyUnit.USD
            )
        }
        var sellDigitalAmount = request.sellDigitalAmount
        if (request.sellDigitalAmount.stripTrailingZeros().scale() > request.sellDigitalCurrencyUnit.maximumNumberOfDigits.toInt()) {
            sellDigitalAmount = sellDigitalAmount.setScale(request.sellDigitalCurrencyUnit.maximumNumberOfDigits.toInt(), RoundingMode.UP).stripTrailingZeros()
        }
        var fiatSellValueUSD = sellDigitalAmount / exchangeRateSell
        if (fiatSellValueUSD <= BigDecimal.ZERO) {
            throw UnprocessableEntityException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        var exchangeRateBuy = exchangeService.getExchangeRateFake(
            request.buyDigitalCurrencyUnit,
            FiatCurrencyUnit.USD
        )
        if (request.isSibos) {
            exchangeRateBuy = exchangeService.getExchangeRate(
                request.buyDigitalCurrencyUnit,
                FiatCurrencyUnit.USD
            )
        }
        if (exchangeRateBuy.stripTrailingZeros().scale() > request.buyDigitalCurrencyUnit.maximumNumberOfDigits.toInt()) {
            exchangeRateBuy = exchangeRateBuy.setScale(request.buyDigitalCurrencyUnit.maximumNumberOfDigits.toInt(), RoundingMode.UP).stripTrailingZeros()
        }

        if (fiatSellValueUSD.stripTrailingZeros().scale() > ROUNDING_LIMIT) {
            fiatSellValueUSD = fiatSellValueUSD.setScale(ROUNDING_LIMIT, RoundingMode.UP).stripTrailingZeros()
        }
        var digitalAmountBuy = fiatSellValueUSD * exchangeRateBuy

        if (digitalAmountBuy.stripTrailingZeros().scale() > request.buyDigitalCurrencyUnit.maximumNumberOfDigits.toInt()) {
            digitalAmountBuy = digitalAmountBuy.setScale(request.buyDigitalCurrencyUnit.maximumNumberOfDigits.toInt(), RoundingMode.UP).stripTrailingZeros()
        }
        return buyFakeCrypto(
            digitalAmountBuy,
            request.buyDigitalCurrencyUnit,
            userAccount,
            false,
            FiatCurrencyUnit.USD,
            BigDecimal.ZERO,
            request.sellDigitalCurrencyUnit,
            sellDigitalAmount,
            request.isSibos
        )
    }
}
