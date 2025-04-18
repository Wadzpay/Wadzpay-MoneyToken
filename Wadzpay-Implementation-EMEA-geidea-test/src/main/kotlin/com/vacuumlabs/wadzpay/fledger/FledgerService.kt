package com.vacuumlabs.wadzpay.fledger

import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvBindByPosition
import com.vacuumlabs.MAX_CBD_VALUE
import com.vacuumlabs.MIN_CBD_VALUE
import com.vacuumlabs.vuba.ledger.service.LedgerService
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.UnprocessableEntityException
import com.vacuumlabs.wadzpay.exchange.ExchangeService
import com.vacuumlabs.wadzpay.fledger.model.UserBankAccount
import com.vacuumlabs.wadzpay.fledger.model.UserBankAccountRepository
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanksUserEntryRepository
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.AccountOwner
import com.vacuumlabs.wadzpay.ledger.model.AccountRepository
import com.vacuumlabs.wadzpay.ledger.model.Subaccount
import com.vacuumlabs.wadzpay.ledger.model.Transaction
import com.vacuumlabs.wadzpay.ledger.model.TransactionDirection
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
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
class FledgerService(
    val accountRepository: AccountRepository,
    val userInitializerService: UserInitializerService,
    val exchangeService: ExchangeService,
    val ledgerService: LedgerService,
    val fiatSubAccountRepository: FiatSubAccountRepository,
    val userBankAccountRepository: UserBankAccountRepository,
    val issuanceBanksUserEntryRepository: IssuanceBanksUserEntryRepository
) {
    fun sellCrypto(
        sellTransactionRequest: FledgerController.SellTransactionRequest,
        userAccount: UserAccount,
        createTransactionLog: Boolean
    ): TransactionViewModel? {
        return sellCryptoCurrency(
            sellTransactionRequest.digitalAmount,
            sellTransactionRequest.digitalAsset,
            sellTransactionRequest.fiatAsset,
            sellTransactionRequest.exchangeRate,
            sellTransactionRequest.finalFiatAmount,
            userAccount, createTransactionLog
        )
    }

    private fun sellCryptoCurrency(
        digitalAmount: BigDecimal,
        digitalAsset: CurrencyUnit,
        fiatType: FiatCurrencyUnit = FiatCurrencyUnit.AED,
        exchangeRate: BigDecimal?,
        finalFiatAmount: BigDecimal?,
        userAccount: UserAccount,
        createTransactionLog: Boolean
    ): TransactionViewModel? {
        val balance =
            getCryptoBalance(userAccount.account.getSubaccountByAsset(digitalAsset))
        if (balance < digitalAmount) {
            throw UnprocessableEntityException(ErrorCodes.INSUFFICIENT_FUNDS)
        }
        val merchant = userInitializerService.getFakeMerchant()
        val fiatValueAfterSell = if (finalFiatAmount != null && finalFiatAmount > BigDecimal.ZERO) {
            finalFiatAmount
        } else if (exchangeRate != null && exchangeRate!! > BigDecimal.ZERO) {
            digitalAmount / exchangeRate
        } else {
            val currentExchangeRate = exchangeService.getExchangeRate(
                digitalAsset,
                fiatType
            )
            digitalAmount / currentExchangeRate
        }
        // var fiatValueAfterSell = digitalAmount / exchangeRate
        if (fiatType == FiatCurrencyUnit.AED && fiatValueAfterSell < BigDecimal(MIN_CBD_VALUE)) {
            throw UnprocessableEntityException(ErrorCodes.CANNOT_ALLOW_LESS_THEN_MIN_FIAT)
        }
        if (fiatType == FiatCurrencyUnit.AED && fiatValueAfterSell > BigDecimal(MAX_CBD_VALUE)) {
            throw UnprocessableEntityException(ErrorCodes.CANNOT_ALLOW_LESS_THEN_MAX_FIAT)
        }
        val fiatSubAccount =
            userAccount.account.fiatSubAccount.find { it.fiatasset == fiatType }
        if (fiatSubAccount == null) {
            createFiatSubAccount(userAccount, fiatType, fiatValueAfterSell)
        }
        var digitalAmounttoSave = digitalAmount
        if (digitalAmounttoSave.stripTrailingZeros().scale() > 8) {
            digitalAmounttoSave = digitalAmounttoSave.setScale(8, RoundingMode.FLOOR)
        }
        val transaction = userInitializerService.createSellTransaction(
            userAccount.account.subaccounts.find { it.asset == digitalAsset.toString() }!!,
            merchant.account.subaccounts.find { it.asset == digitalAsset.toString() }!!,
            digitalAsset,
            digitalAmounttoSave,
            createTransactionLog, fiatType, fiatValueAfterSell
        )
        if (createTransactionLog) {
            // Not a swap. Fiat needs to be transferred to user.
            creditFiatToUser(fiatType, userAccount, fiatValueAfterSell)
        }
        return transaction?.toViewModel(direction = TransactionDirection.OUTGOING)
    }

    fun getCryptoBalance(subaccount: Subaccount): BigDecimal =
        ledgerService.getSubaccount(subaccount.reference).get().balance

    private fun creditFiatToUser(
        fiatType: FiatCurrencyUnit,
        userAccount: UserAccount,
        fiatValueAfterSell: BigDecimal
    ) {
        val fiatSubAccount =
            userAccount.account.fiatSubAccount.find { it.fiatasset == fiatType }
        if (fiatSubAccount != null) {
            val fiatBalance = fiatSubAccount.balance

            var fiatValueToCredit = fiatBalance + fiatValueAfterSell
            if (fiatValueToCredit.stripTrailingZeros().scale() > 2) {
                fiatValueToCredit = fiatValueToCredit.setScale(2, RoundingMode.UP)
            }
            fiatSubAccount.balance = fiatValueToCredit
            fiatSubAccountRepository.save(fiatSubAccount)
        }
    }

    private fun createFiatSubAccount(
        userAccount: UserAccount,
        fiatCurrencyUnit: FiatCurrencyUnit,
        initalBalance: BigDecimal
    ): Boolean {
        val fiatSubAccount = FiatSubAccount(userAccount.account.reference, userAccount.account, fiatCurrencyUnit)
        fiatSubAccount.balance = initalBalance
        userAccount.account.fiatSubAccount.add(fiatSubAccount)
        accountRepository.save(userAccount.account)
        fiatSubAccountRepository.save(fiatSubAccount)
        return true
    }

    fun buyCrypto(
        cryptoAmount: BigDecimal,
        digitalAsset: CurrencyUnit,
        userAccount: UserAccount,
        logTransactionEntry: Boolean,
        fiatAsset: FiatCurrencyUnit,
        fiatAmount: BigDecimal,
        toDigitalAsset: CurrencyUnit,
        toDigitalAssetAmount: BigDecimal,
    ): TransactionViewModel? {
        var digitalAmounttoSave = cryptoAmount
        if (digitalAmounttoSave.stripTrailingZeros().scale() > 8) {
            digitalAmounttoSave = digitalAmounttoSave.setScale(8, RoundingMode.UP)
        }
        val merchant = userInitializerService.getFakeMerchant()
        val transaction = userInitializerService.createFakeBuyTransaction(
            merchant.account.subaccounts.find { it.asset == digitalAsset.toString() }!!,
            userAccount.account.subaccounts.find { it.asset == digitalAsset.toString() }!!,
            digitalAsset,
            digitalAmounttoSave,
            logTransactionEntry, fiatAsset, fiatAmount, toDigitalAsset, toDigitalAssetAmount, false
        )
        return transaction?.toViewModel(direction = TransactionDirection.INCOMING)
    }

    fun buyCryptoUsingFiat(
        buyTransactionRequest: FledgerController.CreateBuyTransactionRequest,
        userAccount: UserAccount,
        logTransactionEntry: Boolean
    ): TransactionViewModel? {
        val fiatAmount = buyTransactionRequest.fiatAmount ?: BigDecimal.TEN
        val fiatSubAccount =
            userAccount.account.fiatSubAccount.find { it.fiatasset == buyTransactionRequest.fiatAsset }
        return if (fiatSubAccount == null) {
            return if (createFiatSubAccount(
                    userAccount, buyTransactionRequest.fiatAsset ?: FiatCurrencyUnit.AED,
                    BigDecimal.ZERO
                )
            ) {
                throw BadRequestException(ErrorCodes.INSUFFICIENT_FUNDS)
            } else {
                throw BadRequestException(ErrorCodes.INSUFFICIENT_FUNDS)
            }
        } else {
            val balance = fiatSubAccount.balance
            if (balance < fiatAmount) {
                throw UnprocessableEntityException(ErrorCodes.INSUFFICIENT_FUNDS)
            }
            if (buyTransactionRequest.fiatAsset == FiatCurrencyUnit.AED && fiatAmount < BigDecimal(MIN_CBD_VALUE)) {
                throw UnprocessableEntityException(ErrorCodes.CANNOT_ALLOW_LESS_THEN_MIN_FIAT)
            }
            if (buyTransactionRequest.fiatAsset == FiatCurrencyUnit.AED && fiatAmount > BigDecimal(MAX_CBD_VALUE)) {
                throw UnprocessableEntityException(ErrorCodes.CANNOT_ALLOW_LESS_THEN_MAX_FIAT)
            }
            var saveBalance = (balance - fiatAmount)
            if (saveBalance.stripTrailingZeros().scale() > 2) {
                saveBalance = saveBalance.setScale(2, RoundingMode.UP)
            }
            fiatSubAccount.balance = saveBalance

            var cryptoAmount =
                if (buyTransactionRequest.finalDigitalAmount != null && buyTransactionRequest.finalDigitalAmount > BigDecimal.ZERO) {
                    buyTransactionRequest.finalDigitalAmount
                } else if (buyTransactionRequest.exchangeRate != null && buyTransactionRequest.exchangeRate > BigDecimal.ZERO) {
                    (fiatAmount * buyTransactionRequest.exchangeRate)
                } else {
                    val currentExchangeRate = exchangeService.getExchangeRate(
                        buyTransactionRequest.digitalAsset,
                        buyTransactionRequest.fiatAsset!!
                    )
                    (fiatAmount * currentExchangeRate)
                }
            val transactionViewModel = buyCrypto(
                cryptoAmount,
                buyTransactionRequest.digitalAsset,
                userAccount,
                logTransactionEntry,
                buyTransactionRequest.fiatAsset!!,
                buyTransactionRequest.fiatAmount ?: BigDecimal.ZERO,
                buyTransactionRequest.digitalAsset,
                buyTransactionRequest.digitalAmount ?: BigDecimal.ZERO
            )
            fiatSubAccountRepository.save(fiatSubAccount)
            return transactionViewModel
        }
    }

    fun createDepositFiat(
        depositRequest: FledgerController.DepositRequest,
        userAccount: UserAccount,
        createTransactionLog: Boolean
    ): TransactionViewModel? {
        return createDepositFiatInToUserFiatWallet(
            depositRequest.fiatType,
            depositRequest.fiatAmount,
            depositRequest.bankAccountNumber,
            userAccount,
            createTransactionLog
        )
    }

    private fun createDepositFiatInToUserFiatWallet(
        fiatType: FiatCurrencyUnit,
        fiatAmount: BigDecimal,
        bankAccountNumber: String?,
        userAccount: UserAccount,
        createTransactionLog: Boolean
    ): TransactionViewModel? {
        val userBankAccount = userAccount.userBankAccount.find { it.bankAccountNumber == bankAccountNumber }
        if (userBankAccount == null) {
            throw BadRequestException(ErrorCodes.USER_BANK_ACCOUNT_NOT_FOUND)
        }
        val fiatSubAccount = userAccount.account.fiatSubAccount.find { it.fiatasset == fiatType }
        if (fiatSubAccount == null) {
            createFiatSubAccount(userAccount, fiatType, fiatAmount)
        }
        if (fiatType == FiatCurrencyUnit.AED && fiatAmount < BigDecimal(MIN_CBD_VALUE)) {
            throw UnprocessableEntityException(ErrorCodes.CANNOT_ALLOW_LESS_THEN_MIN_FIAT)
        }
        if (fiatType == FiatCurrencyUnit.AED && fiatAmount > BigDecimal(MAX_CBD_VALUE)) {
            throw UnprocessableEntityException(ErrorCodes.CANNOT_ALLOW_LESS_THEN_MAX_FIAT)
        }
        val digitalAsset: CurrencyUnit = CurrencyUnit.USDT
        val digitalAmount = 0.000001.toBigDecimal()
        val merchant = userInitializerService.getFakeMerchant()
        val transaction = userInitializerService.createFiatDepositTransaction(
            merchant.account.subaccounts.find { it.asset == digitalAsset.toString() }!!,
            userAccount.account.subaccounts.find { it.asset == digitalAsset.toString() }!!,
            digitalAsset,
            digitalAmount,
            createTransactionLog,
            fiatType,
            fiatAmount,
            bankAccountNumber,
            true
        )
        if (createTransactionLog) {
            creditFiatToUserDeposit(fiatType, userAccount, fiatAmount)
        }
        return transaction?.toViewModel(direction = TransactionDirection.INCOMING)
    }

    private fun creditFiatToUserDeposit(
        fiatType: FiatCurrencyUnit,
        userAccount: UserAccount,
        fiatValueAfterSell: BigDecimal
    ) {
        val fiatSubAccount =
            userAccount.account.fiatSubAccount.find { it.fiatasset == fiatType }
        if (fiatSubAccount != null) {
            val fiatBalance = fiatSubAccount.balance

            var fiatValueToCredit = fiatBalance + fiatValueAfterSell
            if (fiatValueToCredit.stripTrailingZeros().scale() > 2) {
                fiatValueToCredit = fiatValueToCredit.setScale(2, RoundingMode.UP)
            }
            fiatSubAccount.balance = fiatValueToCredit
            fiatSubAccountRepository.save(fiatSubAccount)
        }
    }

    fun createWithdrawFiat(
        withdrawRequest: FledgerController.WithdrawRequest,
        userAccount: UserAccount,
        logTransactionEntry: Boolean
    ): TransactionViewModel? {
        val fiatAmount = withdrawRequest.fiatAmount ?: BigDecimal.TEN
        val userBankAccount =
            userAccount.userBankAccount.find { it.bankAccountNumber == withdrawRequest.bankAccountNumber }
        if (userBankAccount == null) {
            throw BadRequestException(ErrorCodes.USER_BANK_ACCOUNT_NOT_FOUND)
        }
        val fiatSubAccount = userAccount.account.fiatSubAccount.find { it.fiatasset == withdrawRequest.fiatType }
        return if (fiatSubAccount == null) {
            return if (createFiatSubAccount(
                    userAccount, withdrawRequest.fiatType ?: FiatCurrencyUnit.AED,
                    BigDecimal.ZERO
                )
            ) {
                throw UnprocessableEntityException(ErrorCodes.INSUFFICIENT_FUNDS)
            } else {
                throw UnprocessableEntityException(ErrorCodes.INSUFFICIENT_FUNDS)
            }
        } else {
            if (withdrawRequest.fiatType == FiatCurrencyUnit.AED && fiatAmount < BigDecimal(MIN_CBD_VALUE)) {
                throw UnprocessableEntityException(ErrorCodes.CANNOT_ALLOW_LESS_THEN_MIN_FIAT)
            }
            if (withdrawRequest.fiatType == FiatCurrencyUnit.AED && fiatAmount > BigDecimal(MAX_CBD_VALUE)) {
                throw UnprocessableEntityException(ErrorCodes.CANNOT_ALLOW_LESS_THEN_MAX_FIAT)
            }
            val balance = fiatSubAccount.balance
            if (balance < fiatAmount) {
                throw UnprocessableEntityException(ErrorCodes.INSUFFICIENT_FUNDS)
            }
            var saveBalance = (balance - fiatAmount)
            if (saveBalance.stripTrailingZeros().scale() > 2) {
                saveBalance = saveBalance.setScale(2, RoundingMode.UP)
            }
            fiatSubAccount.balance = saveBalance
            val fiatBalance = fiatSubAccount.balance

            val digitalAsset: CurrencyUnit = CurrencyUnit.USDT
            val digitalAmount = 0.000001.toBigDecimal()
            val merchant = userInitializerService.getFakeMerchant()
            val transaction = userInitializerService.createFiatWithDrawTransaction(
                userAccount.account.subaccounts.find { it.asset == digitalAsset.toString() }!!,
                merchant.account.subaccounts.find { it.asset == digitalAsset.toString() }!!,
                digitalAsset,
                digitalAmount,
                logTransactionEntry,
                withdrawRequest.fiatType,
                fiatAmount,
                withdrawRequest.bankAccountNumber,
                true
            )
            if (logTransactionEntry) {
                fiatSubAccountRepository.save(fiatSubAccount)
            }
            return transaction?.toViewModel(direction = TransactionDirection.OUTGOING)
        }
    }

    fun addUserBankAccount(
        addUserBankAccount: FledgerController.AddUserBankAccountRequest,
        countryCode: FledgerController.CountryCode,
        userAccount: UserAccount
    ): UserBankAccountModel? {
        val isBankAccountFind =
            addUserBankAccount.bankAccountNumber?.let { userBankAccountRepository.getByBankAccountNumber(it) }
        if (isBankAccountFind != null && isBankAccountFind.size > 0) {
            throw BadRequestException(ErrorCodes.DUPLICATE_BANK_ACCOUNT)
        }
        if (countryCode.toString() == "AE" && !validateAEBankAccount(addUserBankAccount)) {
            throw BadRequestException(ErrorCodes.INVALID_BANK_ACCOUNT)
        }
        val userBankAccount =
            userAccount.userBankAccount.find { it.bankAccountNumber == addUserBankAccount.bankAccountNumber }
        if (userBankAccount != null) {
            throw BadRequestException(ErrorCodes.BANK_ACCOUNT_ALREADY_ADDED)
        }
        var fiatCurrencyUnit: FiatCurrencyUnit? = null
        val issuanceBanksUserEntry = issuanceBanksUserEntryRepository.getByUserAccountId(userAccount)
        if (issuanceBanksUserEntry != null) {
            fiatCurrencyUnit = issuanceBanksUserEntry.issuanceBanksId.fiatCurrency
        }
        var savedUserBankAccount = UserBankAccount(
            userAccount = userAccount,
            bankAccountNumber = addUserBankAccount.bankAccountNumber,
            accountHolderName = addUserBankAccount.accountHolderName,
            ifscCode = addUserBankAccount.ifscCode,
            branchName = addUserBankAccount.branchName,
            countryCode = countryCode.toString(),
            fiatCurrency = fiatCurrencyUnit?.toString()
        )
        savedUserBankAccount = userBankAccountRepository.save(savedUserBankAccount)
        return UserBankAccountModel(
            bankAccountNumber = savedUserBankAccount.bankAccountNumber,
            countryCode = savedUserBankAccount.countryCode
        )
    }

    private fun validateAEBankAccount(
        addUserBankAccount: FledgerController.AddUserBankAccountRequest
    ): Boolean {
        if (addUserBankAccount.bankAccountNumber!!.length != 21) {
            return false
        }
        val startIndex = 2
        val endIndex = 4
        val substringBankAccountNo = addUserBankAccount.bankAccountNumber.subSequence(startIndex, endIndex)
        if (substringBankAccountNo != "23") {
            return false
        }
        return true
    }

    fun createFiatTransaction(senderAccount: AccountOwner, receiverAccount: UserAccount, amount: BigDecimal?, asset: FiatCurrencyUnit?, description: String?, type: TransactionType): Transaction? {
        println("senderAccount ==>" + senderAccount)
        asset?.let {
            if (amount != null) {
                withdrawFiat(fiatType = it, fiatAmount = amount, userAccount = senderAccount as UserAccount)
            }
        }
        asset?.let {
            if (amount != null) {
                depositFiatInToUserFiatWallet(fiatType = asset, userAccount = receiverAccount, fiatAmount = amount)
            }
        }
        val digitalAsset: CurrencyUnit = CurrencyUnit.USDT
        val digitalAmount = 0.000001.toBigDecimal()
        val transaction = asset?.let {
            userInitializerService.createFiatTransaction(
                senderAccount.account.subaccounts.find { it.asset == digitalAsset.toString() }!!,
                receiverAccount.account.subaccounts.find { it.asset == digitalAsset.toString() }!!,
                digitalAsset,
                digitalAmount,
                it,
                amount,
                description,
                type
            )
        }
        return transaction
    }

    private fun depositFiatInToUserFiatWallet(
        fiatType: FiatCurrencyUnit,
        fiatAmount: BigDecimal,
        userAccount: UserAccount,
    ): TransactionViewModel? {
        val fiatSubAccount = userAccount.account.fiatSubAccount.find { it.fiatasset == fiatType }
        println("fiatSubAccount ==>" + fiatSubAccount)
        if (fiatSubAccount == null) {
            createFiatSubAccount(userAccount, fiatType, fiatAmount)
        }
        creditFiatToUserDeposit(fiatType, userAccount, fiatAmount)
        return null
    }

    fun withdrawFiat(
        fiatType: FiatCurrencyUnit,
        fiatAmount: BigDecimal,
        userAccount: UserAccount
    ): TransactionViewModel? {
        val fiatSubAccount = userAccount.account.fiatSubAccount.find { it.fiatasset == fiatType }
        return if (fiatSubAccount == null) {
            return if (createFiatSubAccount(
                    userAccount, fiatType,
                    BigDecimal.ZERO
                )
            ) {
                throw UnprocessableEntityException(ErrorCodes.INSUFFICIENT_FUNDS)
            } else {
                throw UnprocessableEntityException(ErrorCodes.INSUFFICIENT_FUNDS)
            }
        } else {
            val balance = fiatSubAccount.balance
            if (balance < fiatAmount) {
                throw UnprocessableEntityException(ErrorCodes.INSUFFICIENT_FUNDS)
            }
            var saveBalance = (balance - fiatAmount)
            if (saveBalance.stripTrailingZeros().scale() > 2) {
                saveBalance = saveBalance.setScale(2, RoundingMode.UP)
            }
            fiatSubAccount.balance = saveBalance
            fiatSubAccountRepository.save(fiatSubAccount)
            return null
        }
    }

    fun initialFiatWallet(userAccount: UserAccount) {
        var fiatCurrencyList = mutableListOf<FiatCurrencyUnit>()
        fiatCurrencyList.add(FiatCurrencyUnit.AED)
        fiatCurrencyList.add(FiatCurrencyUnit.PHP)
        fiatCurrencyList.add(FiatCurrencyUnit.THB)
        fiatCurrencyList.add(FiatCurrencyUnit.INR)
        for (item in fiatCurrencyList) {
            val fiatSubAccount = userAccount.account.fiatSubAccount.find { it.fiatasset == item }
            if (fiatSubAccount == null) {
                createFiatSubAccount(userAccount, item, BigDecimal.ZERO)
            }
        }
    }

    data class UserBankAccountModel(
        @CsvBindByPosition(position = 0)
        @CsvBindByName(column = "bankAccountNumber")
        val bankAccountNumber: String? = null,

        @CsvBindByPosition(position = 1)
        @CsvBindByName(column = "countryCode")
        val countryCode: String? = null,
    )
}
