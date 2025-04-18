package com.vacuumlabs.wadzpay.issuance

import com.vacuumlabs.WARNING_PERCENTAGE
import com.vacuumlabs.vuba.ledger.data.SubaccountEntryRepository
import com.vacuumlabs.vuba.ledger.model.Subaccount
import com.vacuumlabs.vuba.ledger.service.LedgerService
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanksRepository
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanksUserEntryRepository
import com.vacuumlabs.wadzpay.issuance.models.IssuanceTransactionLimitConfigRepository
import com.vacuumlabs.wadzpay.issuance.models.IssuanceWalletConfig
import com.vacuumlabs.wadzpay.issuance.models.IssuanceWalletConfigRepository
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.TransactionRepository
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.ledger.model.TransactionWalletFeeDetails
import com.vacuumlabs.wadzpay.ledger.model.TransactionWalletFeeDetailsRepository
import com.vacuumlabs.wadzpay.ledger.service.GetTransactionListRequest
import com.vacuumlabs.wadzpay.ledger.service.TransactionService
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.viewmodels.TransactionViewModel
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date

@Service
class IssuanceConfigurationService(
    val issuanceBanksUserEntryRepository: IssuanceBanksUserEntryRepository,
    val transactionRepository: TransactionRepository,
    val transactionService: TransactionService,
    val issuanceWalletConfigRepository: IssuanceWalletConfigRepository,
    val transactionWalletFeeDetailsRepository: TransactionWalletFeeDetailsRepository,
    val issuanceTransactionLimitConfigRepository: IssuanceTransactionLimitConfigRepository,
    val ledgerService: LedgerService,
    val issuanceBanksRepository: IssuanceBanksRepository,
    @Lazy val issuanceWalletService: IssuanceWalletService,
    @Lazy val issuanceGraphService: IssuanceGraphService,
    @Lazy val subaccountEntryRepository: SubaccountEntryRepository,
    val commonService: CommonService
) {
    fun getFeeConfigDetails(
        userAccount: UserAccount,
        walletFeeId: String,
        frequency: String?
    ): IssuanceWalletUserController.FeeConfigDetails? {
        val issuanceBanksUserEntry = issuanceBanksUserEntryRepository.getByUserAccountId(userAccount)
        val feeConfigData =
            issuanceBanksUserEntry?.let { issuanceWalletConfigRepository.getByIssuanceBanksId(issuanceBanksId = it.issuanceBanksId) }
        val feeConfigDataList =
            feeConfigData?.filter { e -> e.isActive && e.frequency == frequency && e.walletFeeId == walletFeeId && e.feeValue != BigDecimal.ZERO.toString() }
        if (!feeConfigDataList.isNullOrEmpty()) {
            feeConfigDataList.forEach { tData ->
                return setFeeConfigDetails(userAccount, tData)
            }
        }
        return null
    }

    fun setFeeConfigDetails(
        userAccount: UserAccount,
        tData: IssuanceWalletConfig
    ): IssuanceWalletUserController.FeeConfigDetails? {
        var feeName: String? = null
        IssuanceCommonController.WalletFeeType.values().forEach { data ->
            if (tData.walletFeeId == data.walletFeeId) {
                feeName = data.walletFeeType
            }
        }
        var feeDeductedData = mutableListOf<TransactionWalletFeeDetails>()
        if (tData.walletFeeId != IssuanceCommonController.WalletFeeType.WF_004.toString() && tData.frequency == IssuanceCommonController.Frequency.ONE_TIME.toString()) {
            feeDeductedData =
                tData.frequency?.let {
                    transactionWalletFeeDetailsRepository.getByFeeNameAndFeeFrequencyAndUserAccount(
                        feeName,
                        tData.frequency,
                        userAccount
                    )
                }!!
        }
        if (feeDeductedData.isEmpty()) {
            return IssuanceWalletUserController.FeeConfigDetails(
                feeId = tData.id,
                currencyType = checkCurrencyUnit(tData.fiatCurrency, tData.walletFeeId, tData.fiatCurrency),
                feeNameId = tData.walletFeeId,
                feeName = feeName,
                feeType = checkFixedOrPer(tData.feeValue),
                feeAmount = findValue(tData.feeValue, tData.fiatCurrency, tData.walletFeeId),
                feeFrequency = tData.frequency,
                feeMinimumAmount = convertedAmount(tData.minValue, tData.walletFeeId, tData.fiatCurrency),
                feeMaximumAmount = convertedAmount(tData.maxValue, tData.walletFeeId, tData.fiatCurrency),
                createdDate = tData.createdDate
            )
        }
        return null
    }

    fun convertedAmount(amount: BigDecimal?, walletFeeId: String, currency: String?): BigDecimal? {
        if (amount != null) {
            return if (walletFeeId == IssuanceCommonController.WalletFeeType.WF_001.toString() || walletFeeId == IssuanceCommonController.WalletFeeType.WF_002.toString()) {
                if (currency != FiatCurrencyUnit.SAR.toString()) {
                    amount
                } else {
                    amount
                }
            } else {
                if (currency != FiatCurrencyUnit.MYR.toString()) {
                    amount
                } else {
                    amount
                }
            }
        }
        return null
    }

    fun returnFeeConfig(tData: IssuanceWalletConfig): IssuanceWalletUserController.FeeConfigDetails {
        var feeName: String? = null
        IssuanceCommonController.WalletFeeType.values().forEach { data ->
            if (tData.walletFeeId == data.walletFeeId) {
                feeName = data.walletFeeType
            }
        }
        return IssuanceWalletUserController.FeeConfigDetails(
            feeId = tData.id,
            currencyType = checkCurrencyUnit(tData.fiatCurrency, tData.walletFeeId, tData.fiatCurrency),
            feeNameId = tData.walletFeeId,
            feeName = feeName,
            feeType = checkFixedOrPer(tData.feeValue),
            feeAmount = findValue(tData.feeValue, tData.fiatCurrency, tData.walletFeeId),
            feeFrequency = tData.frequency,
            feeMinimumAmount = convertedAmount(tData.minValue, tData.walletFeeId, tData.fiatCurrency),
            feeMaximumAmount = convertedAmount(tData.maxValue, tData.walletFeeId, tData.fiatCurrency)
        )
    }

    fun checkLoadingFee(
        userAccount: UserAccount,
        walletFeeId: String,
        frequency: String?
    ): IssuanceWalletUserController.FeeConfigDetails? {
        val issuanceBanksUserEntry = issuanceBanksUserEntryRepository.getByUserAccountId(userAccount)
        val feeConfigData =
            issuanceBanksUserEntry?.let { issuanceWalletConfigRepository.getByIssuanceBanksId(issuanceBanksId = it.issuanceBanksId) }
        val loadingFee =
            feeConfigData?.filter { e -> e.isActive && e.walletFeeId == walletFeeId && e.frequency == frequency && e.feeValue != BigDecimal.ZERO.toString() }
        val formatterDate = SimpleDateFormat("yyyy-MM-dd")
        val dateNow = Instant.now()
        val dateNowFormat = formatterDate.format(Date.from(dateNow))
        var feeName: String? = null
        IssuanceCommonController.WalletFeeType.values().forEach { data ->
            if (walletFeeId == data.walletFeeId) {
                feeName = data.walletFeeType
            }
        }
        if (!loadingFee.isNullOrEmpty()) {
            loadingFee.forEach { tData ->
                if (frequency == IssuanceCommonController.Frequency.DAILY.toString()) {
                    val startDate = issuanceBanksUserEntry.createdAt
                    val formatterTime = SimpleDateFormat("HH:mm:ss")
                    val startTime = formatterTime.format(Date.from(startDate))
                    val time: LocalTime = LocalTime.parse(startTime)
                    val todayDate = formatterDate.format(Date.from(Instant.now()))
                    val date: LocalDate = LocalDate.parse(todayDate)
                    var datetime: LocalDateTime = time.atDate(date)
                    var endDate = datetime.minus(24, ChronoUnit.HOURS)
                    if (endDate.atZone(ZoneId.systemDefault()).toInstant() < Instant.now()) {
                        endDate = datetime
                        datetime = endDate.plus(24, ChronoUnit.HOURS)
                    }
                    val isAlreadyPaid = checkIsFeeAlreadyDeducted(
                        endDate.atZone(ZoneId.systemDefault()).toInstant(),
                        datetime.atZone(ZoneId.systemDefault()).toInstant(),
                        tData.id,
                        userAccount,
                        feeName,
                        frequency
                    )
                    if (isAlreadyPaid != null && !isAlreadyPaid) {
                        return returnFeeConfig(tData)
                    }
                    return null
                }
                if (frequency == IssuanceCommonController.Frequency.WEEKLY.toString()) {
                    var startDate = issuanceBanksUserEntry.createdAt
                    var endDate = startDate.plus(7, ChronoUnit.DAYS)
                    for (i in 1..105) {
                        if (endDate < Instant.now()) {
                            startDate = endDate
                            endDate = endDate.plus(7, ChronoUnit.DAYS)
                        } else {
                            break
                        }
                    }
                    val endDateFormatted =
                        SimpleDateFormat("yyyy-MM-dd").format(Date.from(endDate.minus(1, ChronoUnit.DAYS)))
                    val isAlreadyPaid =
                        checkIsFeeAlreadyDeducted(startDate, endDate, tData.id, userAccount, feeName, frequency)
                    if (isAlreadyPaid != null && !isAlreadyPaid && dateNowFormat == endDateFormatted) {
                        return returnFeeConfig(tData)
                    }
                    return null
                }
                if (frequency == IssuanceCommonController.Frequency.MONTHLY.toString()) {
                    var startDate = issuanceBanksUserEntry.createdAt
                    var endDate = startDate.plus(30, ChronoUnit.DAYS)
                    for (i in 1..24) {
                        if (endDate < Instant.now()) {
                            startDate = endDate
                            endDate = endDate.plus(30, ChronoUnit.DAYS)
                        } else {
                            break
                        }
                    }
                    val endDateFormatted =
                        SimpleDateFormat("yyyy-MM-dd").format(Date.from(endDate.minus(1, ChronoUnit.DAYS)))
                    val isAlreadyPaid =
                        checkIsFeeAlreadyDeducted(startDate, endDate, tData.id, userAccount, feeName, frequency)
                    if (isAlreadyPaid != null && !isAlreadyPaid && dateNowFormat == endDateFormatted) {
                        return returnFeeConfig(tData)
                    }
                    return null
                }
                if (frequency == IssuanceCommonController.Frequency.QUARTERLY.toString()) {
                    var startDate = issuanceBanksUserEntry.createdAt
                    var endDate = startDate.plus(92, ChronoUnit.DAYS)
                    for (i in 1..8) {
                        if (endDate < Instant.now()) {
                            startDate = endDate
                            endDate = endDate.plus(92, ChronoUnit.DAYS)
                        } else {
                            break
                        }
                    }
                    val endDateFormatted =
                        SimpleDateFormat("yyyy-MM-dd").format(Date.from(endDate.minus(1, ChronoUnit.DAYS)))
                    val isAlreadyPaid =
                        checkIsFeeAlreadyDeducted(startDate, endDate, tData.id, userAccount, feeName, frequency)
                    if (isAlreadyPaid != null && isAlreadyPaid && dateNowFormat == endDateFormatted) {
                        return returnFeeConfig(tData)
                    }
                    return null
                }
                if (frequency == IssuanceCommonController.Frequency.HALF_YEARLY.toString()) {
                    var startDate = issuanceBanksUserEntry.createdAt
                    var endDate = startDate.plus(183, ChronoUnit.DAYS)
                    for (i in 1..4) {
                        if (endDate < Instant.now()) {
                            startDate = endDate
                            endDate = endDate.plus(183, ChronoUnit.DAYS)
                        } else {
                            break
                        }
                    }
                    val endDateFormatted =
                        SimpleDateFormat("yyyy-MM-dd").format(Date.from(endDate.minus(1, ChronoUnit.DAYS)))
                    val isAlreadyPaid =
                        checkIsFeeAlreadyDeducted(startDate, endDate, tData.id, userAccount, feeName, frequency)
                    if (isAlreadyPaid != null && !isAlreadyPaid && dateNowFormat == endDateFormatted) {
                        return returnFeeConfig(tData)
                    }
                    return null
                }
                if (frequency == IssuanceCommonController.Frequency.YEARLY.toString()) {
                    var startDate = issuanceBanksUserEntry.createdAt
                    var endDate = startDate.plus(365, ChronoUnit.DAYS)
                    for (i in 1..2) {
                        if (endDate < Instant.now()) {
                            startDate = endDate
                            endDate = endDate.plus(365, ChronoUnit.DAYS)
                        } else {
                            break
                        }
                    }
                    val endDateFormatted =
                        SimpleDateFormat("yyyy-MM-dd").format(Date.from(endDate.minus(1, ChronoUnit.DAYS)))
                    val isAlreadyPaid =
                        checkIsFeeAlreadyDeducted(startDate, endDate, tData.id, userAccount, feeName, frequency)
                    if (isAlreadyPaid != null && !isAlreadyPaid && dateNowFormat == endDateFormatted) {
                        return returnFeeConfig(tData)
                    }
                    return null
                }
            }
        }
        return null
    }

    fun checkFixedOrPer(value: String?): IssuanceWalletUserController.FeeType? {
        if (value != null) {
            return if (!value.contains("%")) {
                IssuanceWalletUserController.FeeType.Fixed
            } else {
                IssuanceWalletUserController.FeeType.Percentage
            }
        }
        return null
    }

    fun checkCurrencyUnit(value: String?, feeType: String, fiatCurrency: String?): String {
        if (feeType != IssuanceCommonController.WalletFeeType.WF_001.toString() && feeType != IssuanceCommonController.WalletFeeType.WF_002.toString() && feeType != IssuanceCommonController.WalletFeeType.WF_006.toString()) {
            return if (value != FiatCurrencyUnit.SAR.toString()) {
                "SAR*"
            } else {
                fiatCurrency ?: "MYR"
            }
        }
        return fiatCurrency ?: "MYR"
    }

    fun findValue(value: String?, currency: String?, walletFeeId: String): BigDecimal? {
        if (value != null) {
            return if (!value.contains("%")) {
                value.toBigDecimal()
            } else {
                val afterReplace = value.replace("%", "")
                afterReplace.toBigDecimal()
            }
        }
        return null
    }

    fun checkIsFeeAlreadyDeducted(
        startDate: Instant,
        endDate: Instant,
        id: Long,
        userAccount: UserAccount,
        feeName: String?,
        frequency: String?
    ): Boolean? {
        val feeDeductedData = transactionWalletFeeDetailsRepository.getByFeeNameAndFeeFrequencyAndUserAccount(
            feeName,
            frequency,
            userAccount
        )
        val endDateFormatted = SimpleDateFormat("yyyy-MM-dd").format(Date.from(endDate.minus(1, ChronoUnit.DAYS)))
        var isFeeDeducted = false
        if (feeDeductedData != null && feeDeductedData.size > 0) {
            feeDeductedData.forEach { fData ->
                val feeAddedDate = SimpleDateFormat("yyyy-MM-dd").format(Date.from(fData.createdAt))
                if (endDateFormatted == feeAddedDate && !isFeeDeducted) {
                    isFeeDeducted = true
                }
            }
        }
        return isFeeDeducted
    }

    fun checkActivationFeeAlreadyDeducted(feeName: String, userAccount: UserAccount): Boolean {
        var isFeeDeducted = true
        val issuanceBanksUserEntry = issuanceBanksUserEntryRepository.getByUserAccountId(userAccount)
        val feeConfigData =
            issuanceBanksUserEntry?.let { issuanceWalletConfigRepository.getByIssuanceBanksId(issuanceBanksId = it.issuanceBanksId) }
        val activationFee =
            feeConfigData?.filter { e -> e.isActive && e.walletFeeId == feeName && e.frequency == IssuanceCommonController.Frequency.ONE_TIME.frequencyId }
        if (!activationFee.isNullOrEmpty()) {
            var transactionWalletFee = mutableListOf<TransactionWalletFeeDetails>()
            activationFee.forEach { tData ->
                var feeName: String? = null
                IssuanceCommonController.WalletFeeType.values().forEach { data ->
                    if (tData.walletFeeId == data.walletFeeId) {
                        feeName = data.walletFeeType
                    }
                }
                val request = GetTransactionListRequest(
                    asset = mutableSetOf(CurrencyUnit.SART.toString()),
                    type = mutableSetOf(TransactionType.DEPOSIT)
                )
                val transaction = userAccount.account.owner?.let { transactionService.getTransactions(it, request) }
                if (tData.createdDate >= issuanceBanksUserEntry.createdAt && !transaction.isNullOrEmpty()) {
                    return true
                } else {
                    transactionWalletFee =
                        transactionWalletFeeDetailsRepository.getByFeeNameAndUserAccount(feeName, userAccount)!!
                }
            }
            isFeeDeducted = transactionWalletFee.isNotEmpty()
        }
        return isFeeDeducted
    }

    fun getTransactionDescriptionByFrequency(feeFrequency: String?): String {
        if (feeFrequency == IssuanceCommonController.Frequency.DAILY.frequencyId) {
            return IssuanceCommonController.Frequency.DAILY.frequencyType + " Service Fee"
        } else if (feeFrequency == IssuanceCommonController.Frequency.WEEKLY.frequencyId) {
            return IssuanceCommonController.Frequency.WEEKLY.frequencyType + " Service Fee"
        } else if (feeFrequency == IssuanceCommonController.Frequency.MONTHLY.frequencyId) {
            return IssuanceCommonController.Frequency.MONTHLY.frequencyType + " Service Fee"
        } else if (feeFrequency == IssuanceCommonController.Frequency.HALF_YEARLY.frequencyId) {
            return IssuanceCommonController.Frequency.HALF_YEARLY.frequencyType + " Service Fee"
        } else if (feeFrequency == IssuanceCommonController.Frequency.QUARTERLY.frequencyId) {
            return IssuanceCommonController.Frequency.QUARTERLY.frequencyType + " Service Fee"
        } else if (feeFrequency == IssuanceCommonController.Frequency.YEARLY.frequencyId) {
            return IssuanceCommonController.Frequency.YEARLY.frequencyType + " Service Fee"
        } else if (feeFrequency == IssuanceCommonController.Frequency.ONE_TIME.frequencyId) {
            return IssuanceCommonController.Frequency.ONE_TIME.frequencyType + " Service Fee"
        } else {
            return "Service Fee"
        }
    }

    fun getFeeTransactionDescriptionByFrequency(startDate: String?, endDate: String, feeFrequency: String): String {
        if (feeFrequency == IssuanceCommonController.Frequency.DAILY.frequencyId) {
            return IssuanceCommonController.Frequency.DAILY.frequencyType + " (" + endDate + ")"
        } else if (feeFrequency == IssuanceCommonController.Frequency.WEEKLY.frequencyId) {
            return IssuanceCommonController.Frequency.WEEKLY.frequencyType + " (" + startDate + " - " + endDate + ")"
        } else if (feeFrequency == IssuanceCommonController.Frequency.MONTHLY.frequencyId) {
            return IssuanceCommonController.Frequency.MONTHLY.frequencyType + " (" + startDate + " - " + endDate + ")"
        } else if (feeFrequency == IssuanceCommonController.Frequency.HALF_YEARLY.frequencyId) {
            return IssuanceCommonController.Frequency.HALF_YEARLY.frequencyType + " (" + startDate + " - " + endDate + ")"
        } else if (feeFrequency == IssuanceCommonController.Frequency.QUARTERLY.frequencyId) {
            return IssuanceCommonController.Frequency.QUARTERLY.frequencyType + " (" + startDate + " - " + endDate + ")"
        } else if (feeFrequency == IssuanceCommonController.Frequency.YEARLY.frequencyId) {
            return "1 year ($startDate - $endDate)"
        } else if (feeFrequency == IssuanceCommonController.Frequency.ONE_TIME.frequencyId) {
            return IssuanceCommonController.Frequency.ONE_TIME.frequencyType + " (" + startDate + " - " + endDate + ")"
        } else {
            return IssuanceCommonController.Frequency.ONE_TIME.frequencyType + " (" + startDate + " - " + endDate + ")"
        }
    }

    fun checkWalletBalanceRunningLow(feeName: String, userAccount: UserAccount): Boolean {
        var isBalanceRunningLow = false
        val subAccount = userAccount.account.getSubAccountByAssetAlgoString(CurrencyUnit.SART.toString())
        val vubaSubaccount = subAccount?.let { ledgerService.getSubaccount(it.reference) }
        var balance = BigDecimal.ZERO
        if (vubaSubaccount != null) {
            if (vubaSubaccount.isPresent) {
                balance = vubaSubaccount.get().balance
            }
        }
        println("balance ==> " + balance)
        // val balance = ledgerService.getSubaccount(userAccount.account.getSubaccountByAsset(CurrencyUnit.SART).reference).get().balance
        val issuanceBanksUserEntry = issuanceBanksUserEntryRepository.getByUserAccountId(userAccount)
        val minimumBalance: BigDecimal? =
            issuanceBanksUserEntry?.let { getMinimumWalletBalance(it.issuanceBanksId, true) }
        println("minimumBalance ==> " + minimumBalance)
        if (minimumBalance != null && minimumBalance >= balance) {
            isBalanceRunningLow = true
        }
        return isBalanceRunningLow
    }

    fun getMinimumWalletBalance(issuanceBanksId: IssuanceBanks, isWithPercentage: Boolean): BigDecimal? {
        val transactionLimit = issuanceTransactionLimitConfigRepository.getByTransactionTypeIdAndIssuanceBanksId(
            transactionType = IssuanceCommonController.TransactionLoadingType.TTC_008.transactionId,
            issuanceBanks = issuanceBanksId
        )
        var minimumBalance: BigDecimal? = null
        if (transactionLimit != null) {
            val afterFilterTransactionLimit =
                transactionLimit.filter { e -> e.isActive && e.frequency == null && e.count == null }
            if (afterFilterTransactionLimit.isNotEmpty() && afterFilterTransactionLimit[0].minValue != null) {
                minimumBalance = if (isWithPercentage) {
                    afterFilterTransactionLimit[0].minValue?.plus(
                        (
                            (
                                afterFilterTransactionLimit[0].minValue?.times(
                                    WARNING_PERCENTAGE.toBigDecimal()
                                ) ?: BigDecimal.ZERO
                                ) / 100.toBigDecimal()
                            )
                    )
                } else {
                    afterFilterTransactionLimit[0].minValue
                }
            }
        }
        return minimumBalance
    }

    fun deductWalletFee(userAccount: UserAccount): Boolean {
        val issuanceBanksUserEntry = issuanceBanksUserEntryRepository.getByUserAccountId(userAccount)
        val feeConfigData =
            issuanceBanksUserEntry?.let { issuanceWalletConfigRepository.getByIssuanceBanksId(issuanceBanksId = it.issuanceBanksId) }
        val walletFee =
            feeConfigData?.filter { e -> e.isActive && e.walletFeeId == IssuanceCommonController.WalletFeeType.WF_005.walletFeeId && e.frequency == null }
        val minimumBalance = issuanceBanksUserEntry?.let { getMinimumWalletBalance(it.issuanceBanksId, false) }
        val isActivationFeeDeducted = checkActivationFeeAlreadyDeducted(
            IssuanceCommonController.WalletFeeType.WF_001.walletFeeId, userAccount
        )
        if (minimumBalance != null && !walletFee.isNullOrEmpty()) {
            var balance = BigDecimal.ZERO
            val subAccount = userAccount.account.getSubAccountByAssetAlgoString(CurrencyUnit.SART.toString())
            val vubaSubaccount = subAccount?.let { ledgerService.getSubaccount(it.reference) }
            if (vubaSubaccount != null) {
                if (vubaSubaccount.isPresent) {
                    balance = vubaSubaccount.get().balance
                }
            }
            println("isActivationFeeDeducted ==> $isActivationFeeDeducted")
            println("minimumBalance ==> $minimumBalance")
            println("balance ==> $balance")
            println("balance ==> $ " + (balance < minimumBalance))
            if (balance < minimumBalance && isActivationFeeDeducted) {
                walletFee.forEach { tData ->
                    val feeDataResponse: MutableList<IssuanceWalletUserController.FeeConfigDetails> = mutableListOf()
                    if (tData.walletFeeId == IssuanceCommonController.WalletFeeType.WF_005.toString() && tData.frequency == null) {
                        val isEligibleForFeeDeduction =
                            checkEligibleForWalletDeduction(tData.id, userAccount, minimumBalance)
                        println("isEligibleForFeeDeduction ==> " + isEligibleForFeeDeduction)
                        if (isEligibleForFeeDeduction) {
                            var feeName: String? = null
                            IssuanceCommonController.WalletFeeType.values().forEach { data ->
                                if (tData.walletFeeId == data.walletFeeId) {
                                    feeName = data.walletFeeType
                                }
                            }
                            val feeConfigData = IssuanceWalletUserController.FeeConfigDetails(
                                feeId = tData.id,
                                currencyType = checkCurrencyUnit(
                                    tData.fiatCurrency,
                                    tData.walletFeeId,
                                    tData.fiatCurrency
                                ),
                                feeNameId = tData.walletFeeId,
                                feeName = feeName,
                                feeType = checkFixedOrPer(tData.feeValue),
                                feeAmount = findValue(tData.feeValue, tData.fiatCurrency, tData.walletFeeId),
                                feeFrequency = tData.frequency,
                                feeMinimumAmount = convertedAmount(
                                    tData.minValue,
                                    tData.walletFeeId,
                                    tData.fiatCurrency
                                ),
                                feeMaximumAmount = convertedAmount(
                                    tData.maxValue,
                                    tData.walletFeeId,
                                    tData.fiatCurrency
                                ),
                                feeDescription = "Low Balance Fee"
                            )
                            feeDataResponse.add(feeConfigData)
                            calculateWalletFee(userAccount, feeDataResponse, issuanceBanksUserEntry.issuanceBanksId)
                        }
                    }
                }
            }
        }
        return true
    }

    fun calculateWalletFee(
        userAccount: UserAccount,
        feeConfigData: MutableList<IssuanceWalletUserController.FeeConfigDetails>,
        issuanceBanksId: IssuanceBanks
    ): TransactionViewModel? {
        feeConfigData.forEach { fData ->
            val subAccount = userAccount.account.getSubAccountByAssetAlgoString(CurrencyUnit.SART.toString())
            val vubaSubaccount = subAccount?.let { ledgerService.getSubaccount(it.reference) }
            var balance = BigDecimal.ZERO
            if (vubaSubaccount != null) {
                if (vubaSubaccount.isPresent) {
                    balance = vubaSubaccount.get().balance
                }
            }
            val feeAmountDeduction = issuanceGraphService.calculateFeeDeductionAmount(balance, fData)
            println("feeAmountDeduction ==> " + feeAmountDeduction)
            if (feeAmountDeduction > BigDecimal.ZERO && balance > feeAmountDeduction) {
                val feeConfigData = IssuanceCommonController.FeeConfigDataDetails(
                    feeId = fData.feeId,
                    enteredAmount = feeAmountDeduction,
                    feeAmount = fData.feeAmount,
                    feeCalculatedAmount = feeAmountDeduction,
                    feeName = fData.feeName,
                    feeType = fData.feeType.toString(),
                    currencyType = fData.currencyType,
                    description = fData.feeDescription
                )
                val feeData = mutableListOf<IssuanceCommonController.FeeConfigDataDetails>()
                feeData.add(feeConfigData)
                val serviceFeeRequest =
                    IssuanceCommonController.ServiceFeeRequest(
                        tokenAsset = CurrencyUnit.SART,
                        amount = feeAmountDeduction,
                        totalFeeApplied = feeAmountDeduction,
                        description = "Wallet Fee",
                        feeConfigData = feeData,
                        type = TransactionType.WALLET_FEE
                    )
                return issuanceWalletService.serviceFeeDeduction(
                    serviceFeeRequest,
                    userAccount = userAccount,
                    issuanceBanksId = issuanceBanksId
                )
            }
        }
        return null
    }

    fun checkEligibleForWalletDeduction(id: Long, userAccount: UserAccount, minimumBalance: BigDecimal): Boolean {
        var isEligible = false
        val dateNow = Instant.now()
        val formatterDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val feeDeductionData = transactionWalletFeeDetailsRepository.getByFeeNameAndFeeFrequencyAndUserAccount(
            IssuanceCommonController.WalletFeeType.WF_005.walletFeeType,
            null,
            userAccount
        )
        println("feeDeductionData 513 $feeDeductionData")
        if (!feeDeductionData.isNullOrEmpty()) {
            val transactionComparator: Comparator<TransactionWalletFeeDetails> = Comparator
                .comparing(TransactionWalletFeeDetails::createdAt)

            val latestWalletFee: TransactionWalletFeeDetails = feeDeductionData.stream()
                .max(transactionComparator)
                .get()
            if (latestWalletFee.createdAt != null && userAccount.account.owner != null) {
                val feeDeductedDate = formatterDate.format(Date.from(latestWalletFee.createdAt))
                val transactionList = issuanceGraphService.getTransactionData(
                    latestWalletFee.createdAt!!, dateNow, userAccount.account.owner!!
                )
                if (transactionList.isNotEmpty()) {
                    var balanceHigherThenMinimumBalance = false
                    transactionList.forEach { transData ->
                        val transactionDate = formatterDate.format(Date.from(transData.createdAt))
                        if (feeDeductedDate < transactionDate) {
                            if (transData.reference != null) {
                                var reference: Subaccount? = null
                                val subAccount =
                                    userAccount.account.getSubAccountByAssetAlgoString(CurrencyUnit.SART.toString())
                                val vubaSubaccount = subAccount?.let { ledgerService.getSubaccount(it.reference) }
                                if (vubaSubaccount != null) {
                                    if (vubaSubaccount.isPresent) {
                                        reference = ledgerService.getSubaccount(
                                            userAccount.account.getSubaccountByAsset(CurrencyUnit.SART).reference
                                        ).get()
                                    }
                                }
                                if (reference != null) {
                                    val subAccountEntryData =
                                        subaccountEntryRepository.findByCommitReferenceAndSubaccount(
                                            transData.reference.toString(),
                                            reference
                                        )
                                    if (subAccountEntryData != null) {
                                        if (subAccountEntryData.balance < minimumBalance) {
                                            println("lower then minimum balance  ==>" + subAccountEntryData.balance)
                                            isEligible = false
                                        }
                                        if (subAccountEntryData.balance > minimumBalance) {
                                            println("higher then minimum balance  ==>" + subAccountEntryData.balance)
                                            balanceHigherThenMinimumBalance = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (balanceHigherThenMinimumBalance) {
                        isEligible = true
                    }
                } else {
                    isEligible = false
                }
            }
        } else {
            isEligible = true
        }
        return isEligible
    }

    fun getLowMaintainWalletBalance(userAccount: UserAccount): BigDecimal? {
        val issuanceBanksUserEntry = issuanceBanksUserEntryRepository.getByUserAccountId(userAccount)
        return issuanceBanksUserEntry?.let { getMinimumWalletBalance(it.issuanceBanksId, false) }
    }
    fun getP2PEnableDisableDetails(userAccount: UserAccount): Boolean {
        val issuanceBanksUserEntry = issuanceBanksUserEntryRepository.getByUserAccountId(userAccount)
        if (issuanceBanksUserEntry != null) {
            return if (issuanceBanksUserEntry.issuanceBanksId.p2pTransfer != null) issuanceBanksUserEntry.issuanceBanksId.p2pTransfer!! else true
        }
        return true
    }

    fun getIssuerInstitutionSupportedFiatCurrency(userAccount: UserAccount): FiatCurrencyUnit? {
        val issuanceBanksUserEntry = issuanceBanksUserEntryRepository.getByUserAccountId(userAccount)
        if (issuanceBanksUserEntry != null) {
            return issuanceBanksUserEntry.issuanceBanksId.fiatCurrency
        }
        return FiatCurrencyUnit.MYR
    }

    fun getUserAccountByIssuanceBank(customerId: String, institutionId: String): UserAccount {
        val issuanceBank = issuanceBanksRepository.getByInstitutionId(institutionId)
        if (issuanceBank != null) {
            val userMappingData = issuanceBanksUserEntryRepository.getByIssuanceBanksId(issuanceBank)
            if (userMappingData != null) {
                var userMappingDataRes = userMappingData.filter { e ->
                    !commonService.checkEmail(customerId) && e.userAccountId.customerId != null && e.userAccountId.customerId!!.contains(
                        customerId, true
                    )
                }
                if (userMappingDataRes.isEmpty()) {
                    userMappingDataRes = userMappingData.filter { e ->
                        e.userAccountId.email != null && e.userAccountId.email!!.contains(
                            customerId, true
                        )
                    }
                }
                if (userMappingDataRes.isNotEmpty()) {
                    return userMappingDataRes[0].userAccountId
                }
            }
            throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
        }
        throw EntityNotFoundException(ErrorCodes.INVALID_INSTITUTION_ID)
    }

    fun getUserAccountByIssuanceBankWithNull(customerId: String, institutionId: String): UserAccount? {
        val issuanceBank = issuanceBanksRepository.getByInstitutionId(institutionId)
        if (issuanceBank != null) {
            var userMappingData = issuanceBanksUserEntryRepository.getByIssuanceBanksId(issuanceBank)
            if (userMappingData != null) {
                userMappingData = userMappingData.filter { e ->
                    e.userAccountId.customerId != null && e.userAccountId.customerId!!.contains(
                        customerId, true
                    )
                }
                if (userMappingData.isNotEmpty()) {
                    return userMappingData[0].userAccountId
                }
                return null
            }
            return null
        }
        throw EntityNotFoundException(ErrorCodes.INVALID_INSTITUTION_ID)
    }
}
