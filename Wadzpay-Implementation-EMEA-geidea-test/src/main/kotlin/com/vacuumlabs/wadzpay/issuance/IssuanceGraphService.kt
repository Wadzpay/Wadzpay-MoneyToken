package com.vacuumlabs.wadzpay.issuance

import au.com.console.jpaspecificationdsl.and
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanksUserEntry
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanksUserEntryRepository
import com.vacuumlabs.wadzpay.issuance.models.SortableFields
import com.vacuumlabs.wadzpay.issuance.models.Status
import com.vacuumlabs.wadzpay.issuance.models.hasCreatedDateGreaterOrEqualTo
import com.vacuumlabs.wadzpay.issuance.models.hasCreatedDateLessOrEqualTo
import com.vacuumlabs.wadzpay.issuance.models.hasDateGreaterOrEqualTo
import com.vacuumlabs.wadzpay.issuance.models.hasDateLessOrEqualTo
import com.vacuumlabs.wadzpay.issuance.models.hasOwner
import com.vacuumlabs.wadzpay.issuance.models.hasStatus
import com.vacuumlabs.wadzpay.issuance.models.toViewModel
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.AccountOwner
import com.vacuumlabs.wadzpay.ledger.model.Transaction
import com.vacuumlabs.wadzpay.ledger.model.TransactionDirection
import com.vacuumlabs.wadzpay.ledger.model.TransactionMode
import com.vacuumlabs.wadzpay.ledger.model.TransactionRepository
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.ledger.service.GetTransactionListRequest
import com.vacuumlabs.wadzpay.ledger.service.TransactionService
import com.vacuumlabs.wadzpay.user.UserAccountService
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Calendar
import java.util.Date
import javax.validation.constraints.PositiveOrZero

@Service
class IssuanceGraphService(
    val issuanceBanksUserEntryRepository: IssuanceBanksUserEntryRepository,
    val userAccountService: UserAccountService,
    val transactionRepository: TransactionRepository,
    val transactionService: TransactionService
) {
    fun fetchWalletUser(issuanceBank: IssuanceBanks, listRequest: UserDetailsRequest): List<IssuanceBanksUserEntry> {
        val specification = and(
            listRequest.toSpecification(issuanceBank)
        )
        return issuanceBanksUserEntryRepository.findAll(
            specification,
            Sort.by(listRequest.sortBy.value)
                .let { if (listRequest.sortDirection == Sort.Direction.DESC) it.descending() else it }
        )
    }
    fun UserDetailsRequest.toSpecification(issuanceBank: IssuanceBanks): Specification<IssuanceBanksUserEntry> = and(
        hasOwner(issuanceBank),
        fromDate?.let { hasDateGreaterOrEqualTo(fromDate.toInstant()) },
        toDate?.let { hasDateLessOrEqualTo(toDate.toInstant()) },
        type?.let { hasStatus(type!!) },
        createdFrom?.let { hasCreatedDateGreaterOrEqualTo(createdFrom!!.toInstant()) },
        createdTo?.let { hasCreatedDateLessOrEqualTo(createdTo!!.toInstant()) },
    )

    fun enabledWalletUserGraphData(
        issuanceBank: IssuanceBanks
    ): IssuanceWalletUserController.ResponseDateWise {
        return IssuanceWalletUserController.ResponseDateWise(
            weekly = enabledWalletUserGraphWeekly(issuanceBank),
            monthly = enabledWalletUserGraphMonthly(issuanceBank),
            yearly = enabledWalletUserGraphYearly(issuanceBank)
        )
    }

    fun enabledWalletUserGraphWeekly(issuanceBank: IssuanceBanks): WeeklyData {
        /* Weekly data */
        val todayDate = Instant.now()
        val weekOne = todayDate.minus(7, ChronoUnit.DAYS)
        val weekTwo = weekOne.minus(7, ChronoUnit.DAYS)
        val weekThree = weekTwo.minus(7, ChronoUnit.DAYS)
        val weekFour = weekThree.minus(7, ChronoUnit.DAYS)
        val weekly = Weekly(
            week4 = getIssuanceWalletBankData(issuanceBank, weekOne, todayDate, listOf(Status.ENABLE)),
            week3 = getIssuanceWalletBankData(issuanceBank, weekTwo, weekOne.minus(1, ChronoUnit.DAYS), listOf(Status.ENABLE)),
            week2 = getIssuanceWalletBankData(issuanceBank, weekThree, weekTwo.minus(1, ChronoUnit.DAYS), listOf(Status.ENABLE)),
            week1 = getIssuanceWalletBankData(issuanceBank, weekFour, weekThree.minus(1, ChronoUnit.DAYS), listOf(Status.ENABLE))
        )

        val weeklyLabel = WeeklyLabel(
            week4 = dateReturn(weekOne) + " " + dateReturn(todayDate),
            week3 = dateReturn(weekTwo) + " " + dateReturn(weekOne.minus(1, ChronoUnit.DAYS)),
            week2 = dateReturn(weekThree) + " " + dateReturn(weekTwo.minus(1, ChronoUnit.DAYS)),
            week1 = dateReturn(weekFour) + " " + dateReturn(weekThree.minus(1, ChronoUnit.DAYS))
        )

        return WeeklyData(
            data = weekly,
            labels = weeklyLabel
        )
    }

    fun enabledWalletUserGraphMonthly(issuanceBank: IssuanceBanks): MutableList<Monthly> {
        return calculate12MonthEnabledWalletUserGraphMonthly(issuanceBank)
    }

    fun enabledWalletUserGraphYearly(issuanceBank: IssuanceBanks): Yearly {
        return Yearly(
            Y2024 = getIssuanceWalletBankData(issuanceBank, getFirstDateOfTheYear(0), getLastDateOfTheYear(0), listOf(Status.ENABLE)),
            Y2023 = getIssuanceWalletBankData(issuanceBank, getFirstDateOfTheYear(1), getLastDateOfTheYear(1), listOf(Status.ENABLE)),
            Y2022 = getIssuanceWalletBankData(issuanceBank, getFirstDateOfTheYear(2), getLastDateOfTheYear(2), listOf(Status.ENABLE)),
            Y2021 = getIssuanceWalletBankData(issuanceBank, getFirstDateOfTheYear(3), getLastDateOfTheYear(3), listOf(Status.ENABLE)),
            Y2020 = getIssuanceWalletBankData(issuanceBank, getFirstDateOfTheYear(4), getLastDateOfTheYear(4), listOf(Status.ENABLE)),
        )
    }

    fun totalWalletCount(
        issuanceBank: IssuanceBanks
    ): IssuanceWalletUserController.ResponseDateWise {
        return IssuanceWalletUserController.ResponseDateWise(
            weekly = totalWalletGraphWeekly(issuanceBank),
            monthly = totalWalletGraphMonthly(issuanceBank),
            yearly = totalWalletGraphYearly(issuanceBank)
        )
    }

    fun totalWalletGraphWeekly(issuanceBank: IssuanceBanks): WeeklyData {
        /* Weekly data */
        val todayDate = Instant.now()
        val weekOne = todayDate.minus(7, ChronoUnit.DAYS)
        val weekTwo = weekOne.minus(7, ChronoUnit.DAYS)
        val weekThree = weekTwo.minus(7, ChronoUnit.DAYS)
        val weekFour = weekThree.minus(7, ChronoUnit.DAYS)
        val weekly = Weekly(
            week4 = getIssuanceWalletBankData(issuanceBank, weekOne, todayDate, null),
            week2 = getIssuanceWalletBankData(issuanceBank, weekTwo, weekOne.minus(1, ChronoUnit.DAYS), null),
            week3 = getIssuanceWalletBankData(issuanceBank, weekThree, weekTwo.minus(1, ChronoUnit.DAYS), null),
            week1 = getIssuanceWalletBankData(issuanceBank, weekFour, weekThree.minus(1, ChronoUnit.DAYS), null)
        )
        val weeklyLabel = WeeklyLabel(
            week4 = dateReturn(weekOne) + " " + dateReturn(todayDate),
            week3 = dateReturn(weekTwo) + " " + dateReturn(weekOne.minus(1, ChronoUnit.DAYS)),
            week2 = dateReturn(weekThree) + " " + dateReturn(weekTwo.minus(1, ChronoUnit.DAYS)),
            week1 = dateReturn(weekFour) + " " + dateReturn(weekThree.minus(1, ChronoUnit.DAYS))
        )
        return WeeklyData(
            data = weekly,
            labels = weeklyLabel
        )
    }
    fun totalWalletGraphMonthly(issuanceBank: IssuanceBanks): MutableList<Monthly> {
        /* Monthly data */
        return calculateTotalWalletGraphMonthly(issuanceBank)
    }

    fun totalWalletGraphYearly(issuanceBank: IssuanceBanks): Yearly {
        return Yearly(
            Y2024 = getIssuanceWalletBankData(issuanceBank, getFirstDateOfTheYear(0), getLastDateOfTheYear(0), null),
            Y2023 = getIssuanceWalletBankData(issuanceBank, getFirstDateOfTheYear(1), getLastDateOfTheYear(1), null),
            Y2022 = getIssuanceWalletBankData(issuanceBank, getFirstDateOfTheYear(2), getLastDateOfTheYear(2), null),
            Y2021 = getIssuanceWalletBankData(issuanceBank, getFirstDateOfTheYear(3), getLastDateOfTheYear(3), null),
            Y2020 = getIssuanceWalletBankData(issuanceBank, getFirstDateOfTheYear(4), getLastDateOfTheYear(4), null)
        )
    }

    fun calculate12MonthTotalTransactionData(issuanceBank: IssuanceBanks): MutableList<Monthly> {
        val monthDate = SimpleDateFormat("MMM-YY")
        val cal = Calendar.getInstance()
        val monthData = mutableListOf<Monthly>()
        cal.time = Date()
        for (i in 1..12) {
            val monthName = monthDate.format(cal.time)
            cal.add(Calendar.MONTH, -1)
            val monthValue = totalTransactionData(issuanceBank, null, getFirstDateOfTheMonth(i - 1), getLastDateOfTheMonth(i - 1))
            monthData.add(Monthly(monthName, monthValue, i))
        }
        monthData.sortByDescending { it.monthOrder }.toString()
        return monthData
    }

    fun calculate12MonthEnabledWalletUserGraphMonthly(issuanceBank: IssuanceBanks): MutableList<Monthly> {
        val monthDate = SimpleDateFormat("MMM-YY")
        val cal = Calendar.getInstance()
        val monthData = mutableListOf<Monthly>()
        cal.time = Date()
        for (i in 1..12) {
            val monthName = monthDate.format(cal.time)
            cal.add(Calendar.MONTH, -1)
            val monthValue = getIssuanceWalletBankData(issuanceBank, getFirstDateOfTheMonth(i - 1), getLastDateOfTheMonth(i - 1), listOf(Status.ENABLE))
            monthData.add(Monthly(monthName, monthValue, i))
        }
        monthData.sortByDescending { it.monthOrder }.toString()
        return monthData
    }

    fun calculateTotalDepositsMonthly(issuanceBank: IssuanceBanks): MutableList<Monthly> {
        val monthDate = SimpleDateFormat("MMM-YY")
        val cal = Calendar.getInstance()
        val monthData = mutableListOf<Monthly>()
        cal.time = Date()
        for (i in 1..12) {
            val monthName = monthDate.format(cal.time)
            cal.add(Calendar.MONTH, -1)
            val monthValue = totalTransactionData(issuanceBank, TransactionType.DEPOSIT, getFirstDateOfTheMonth(i - 1), getLastDateOfTheMonth(i - 1))
            monthData.add(Monthly(monthName, monthValue, i))
        }
        monthData.sortByDescending { it.monthOrder }.toString()
        return monthData
    }

    fun calculateTotalWalletGraphMonthly(issuanceBank: IssuanceBanks): MutableList<Monthly> {
        val monthDate = SimpleDateFormat("MMM-YY")
        val cal = Calendar.getInstance()
        val monthData = mutableListOf<Monthly>()
        cal.time = Date()
        for (i in 1..12) {
            val monthName = monthDate.format(cal.time)
            cal.add(Calendar.MONTH, -1)
            val monthValue = getIssuanceWalletBankData(issuanceBank, getFirstDateOfTheMonth(i - 1), getLastDateOfTheMonth(i - 1), null)
            monthData.add(Monthly(monthName, monthValue, i))
        }
        monthData.sortByDescending { it.monthOrder }.toString()
        return monthData
    }

    fun dateReturn(date: Instant): String {
        val monthDate = SimpleDateFormat("dd-MMM")
        val cal = Calendar.getInstance()
        cal.time = Date.from(date)
        return monthDate.format(cal.time)
    }

    fun getIssuanceWalletBankData(issuanceBank: IssuanceBanks, createdFrom: Instant?, createdTo: Instant?, status: List<Status>?): Int {
        val userDetailsRequest = UserDetailsRequest(page = null, type = status, createdFrom = Date.from(createdFrom), createdTo = Date.from(createdTo))
        val totalCount = fetchWalletUser(issuanceBank, userDetailsRequest).map {
            it.toViewModel()
        }
        return totalCount.size
    }

    fun totalDepositsGraphData(
        issuanceBank: IssuanceBanks
    ): IssuanceWalletUserController.ResponseDateWise {
        return IssuanceWalletUserController.ResponseDateWise(
            weekly = totalDepositsWeekly(issuanceBank),
            monthly = totalDepositsMonthly(issuanceBank),
            yearly = totalDepositsYearly(issuanceBank)
        )
    }

    fun totalDepositsWeekly(issuanceBank: IssuanceBanks): WeeklyData {
        /* Weekly data */
        val todayDate = Instant.now()
        val weekOne = todayDate.minus(7, ChronoUnit.DAYS)
        val weekTwo = weekOne.minus(7, ChronoUnit.DAYS)
        val weekThree = weekTwo.minus(7, ChronoUnit.DAYS)
        val weekFour = weekThree.minus(7, ChronoUnit.DAYS)
        val weekly = Weekly(
            week4 = totalTransactionData(issuanceBank, TransactionType.DEPOSIT, weekOne, todayDate),
            week3 = totalTransactionData(issuanceBank, TransactionType.DEPOSIT, weekTwo, weekOne.minus(1, ChronoUnit.DAYS)),
            week2 = totalTransactionData(issuanceBank, TransactionType.DEPOSIT, weekThree, weekTwo.minus(1, ChronoUnit.DAYS)),
            week1 = totalTransactionData(issuanceBank, TransactionType.DEPOSIT, weekFour, weekThree.minus(1, ChronoUnit.DAYS))
        )
        val weeklyLabel = WeeklyLabel(
            week4 = dateReturn(weekOne) + " " + dateReturn(todayDate),
            week3 = dateReturn(weekTwo) + " " + dateReturn(weekOne.minus(1, ChronoUnit.DAYS)),
            week2 = dateReturn(weekThree) + " " + dateReturn(weekTwo.minus(1, ChronoUnit.DAYS)),
            week1 = dateReturn(weekFour) + " " + dateReturn(weekThree.minus(1, ChronoUnit.DAYS))
        )
        return WeeklyData(
            data = weekly,
            labels = weeklyLabel
        )
    }

    fun totalDepositsMonthly(issuanceBank: IssuanceBanks): MutableList<Monthly> {
        /* Monthly data */
        return calculateTotalDepositsMonthly(issuanceBank)
    }

    fun totalDepositsYearly(issuanceBank: IssuanceBanks): Yearly {
        return Yearly(
            Y2024 = totalTransactionData(issuanceBank, TransactionType.DEPOSIT, getFirstDateOfTheYear(0), getLastDateOfTheYear(0)),
            Y2023 = totalTransactionData(issuanceBank, TransactionType.DEPOSIT, getFirstDateOfTheYear(1), getLastDateOfTheYear(1)),
            Y2022 = totalTransactionData(issuanceBank, TransactionType.DEPOSIT, getFirstDateOfTheYear(2), getLastDateOfTheYear(2)),
            Y2021 = totalTransactionData(issuanceBank, TransactionType.DEPOSIT, getFirstDateOfTheYear(3), getLastDateOfTheYear(3)),
            Y2020 = totalTransactionData(issuanceBank, TransactionType.DEPOSIT, getFirstDateOfTheYear(4), getLastDateOfTheYear(4)),
        )
    }

    fun totalTransactionData(issuanceBank: IssuanceBanks, type: TransactionType?, createdFrom: Instant?, createdTo: Instant?): Int? {
        if (createdFrom != null && createdTo != null) {
            var data = transactionRepository.getByIssuanceBanksDateRange(issuanceBank, createdFrom, createdTo)
            if (data != null) {
                if (type != null) {
                    data = data.filter { e -> e.type == type } as MutableList<Transaction>
                }
            }
            return data?.size
        } else {
            var data = transactionRepository.getByIssuanceBanks(issuanceBank)
            if (data != null) {
                if (type != null) {
                    data = data.filter { e -> e.type == type } as MutableList<Transaction>
                }
            }
            return data?.size
        }
    }

    fun totalTransactionDataList(issuanceBank: IssuanceBanks, type: TransactionType?, createdFrom: Instant?, createdTo: Instant?): MutableList<Transaction>? {
        if (createdFrom != null && createdTo != null) {
            var data = transactionRepository.getByIssuanceBanksDateRange(issuanceBank, createdFrom, createdTo)
            if (data != null) {
                if (type != null) {
                    data = data.filter { e -> e.type == type } as MutableList<Transaction>
                }
            }
            return data
        } else {
            var data = transactionRepository.getByIssuanceBanks(issuanceBank)
            if (data != null) {
                if (type != null) {
                    data = data.filter { e -> e.type == type } as MutableList<Transaction>
                }
            }
            return data
        }
    }

    fun totalTransactionAmount(issuanceBank: IssuanceBanks, type: TransactionType?, createdFrom: Instant?, createdTo: Instant?): BigDecimal {
        if (createdFrom != null && createdTo != null) {
            var dateTo = createdTo
            dateTo = dateTo.plus(1, ChronoUnit.DAYS)
            var data = transactionRepository.getByIssuanceBanksDateRange(issuanceBank, createdFrom, dateTo)
            if (data != null) {
                if (type != null) {
                    data = data.filter { e -> e.type == type } as MutableList<Transaction>
                    // data = data.filter { e -> CurrencyUnit.valueOf(e.asset) == CurrencyUnit.SART } as MutableList<Transaction>
                    return data.sumOf { it.amount }
                }
                return data.sumOf { it.amount }
            }
        } else {
            var data = transactionRepository.getByIssuanceBanks(issuanceBank)
            if (data != null) {
                if (type != null) {
                    data = data.filter { e -> e.type == type } as MutableList<Transaction>
                    // data = data.filter { e -> CurrencyUnit.valueOf(e.asset) == CurrencyUnit.SART } as MutableList<Transaction>
                    return data.sumOf { it.amount }
                }
                return data.sumOf { it.amount }
            }
        }
        return BigDecimal.ZERO
    }

    fun totalTransactionFiatAmount(
        issuanceBank: IssuanceBanks,
        type: TransactionType,
        startDate: Instant,
        endDate: Instant,
        accountOwner: AccountOwner
    ): BigDecimal {
        var dateTo = endDate
        dateTo = dateTo.plus(1, ChronoUnit.DAYS)
        var startDate = startDate
        startDate = startDate?.minus(1, ChronoUnit.DAYS)
        val request = GetTransactionListRequest(
            asset = mutableSetOf(CurrencyUnit.SART.toString()),
            type = mutableSetOf(type),
            status = mutableSetOf(
                TransactionStatus.SUCCESSFUL
            ),
            dateFrom = startDate,
            dateTo = dateTo
        )
        val data = transactionService.getTransactions(accountOwner, request)
        return data.sumOf { it.fiatAmount!! }
    }

    fun totalTransactionDigitalAmount(
        issuanceBank: IssuanceBanks,
        type: TransactionType,
        startDate: Instant,
        endDate: Instant,
        accountOwner: AccountOwner,
        transactionMode: TransactionMode?
    ): BigDecimal {
        var dateTo = endDate
        dateTo = dateTo.plus(1, ChronoUnit.DAYS)
        var startDate = startDate
        startDate = startDate.minus(1, ChronoUnit.DAYS)
        var transactionModeList: MutableSet<TransactionMode>? = null
        if (transactionMode != null) {
            transactionModeList = mutableSetOf(
                transactionMode
            )
        }
        val request = GetTransactionListRequest(
            asset = mutableSetOf(CurrencyUnit.SART.toString()),
            type = mutableSetOf(type),
            status = mutableSetOf(
                TransactionStatus.SUCCESSFUL
            ),
            dateFrom = startDate,
            dateTo = dateTo,
            transactionMode = transactionModeList,
            direction = if (type == TransactionType.PEER_TO_PEER) TransactionDirection.OUTGOING else TransactionDirection.UNKNOWN
        )
        val data = transactionService.getTransactions(accountOwner, request)
        return data.sumOf { it.amount }
    }

    fun totalInitialTransactionFiatAmount(
        issuanceBank: IssuanceBanks,
        type: TransactionType,
        startDate: Instant?,
        endDate: Instant?,
        accountOwner: AccountOwner,
        frequency: String?
    ): BigDecimal {
        var dateTo = endDate
        dateTo = dateTo?.plus(1, ChronoUnit.DAYS)
        val request = GetTransactionListRequest(
            asset = mutableSetOf(CurrencyUnit.SART.toString()),
            type = mutableSetOf(type),
            status = mutableSetOf(
                TransactionStatus.SUCCESSFUL
            ),
            dateFrom = null,
            dateTo = null
        )
        val data = transactionService.getTransactions(accountOwner, request)
        val orderbyasc = data.sortedBy { list -> list.createdAt }
        val createdDate = orderbyasc[0].createdAt
        return if (createdDate.isAfter(startDate) && createdDate.isBefore(dateTo)) {
            orderbyasc[0].fiatAmount ?: BigDecimal.ZERO
        } else {
            BigDecimal.ZERO
        }
    }

    fun getFirstDateOfTheMonth(month: Int): Instant {
        val monthBegin = LocalDate.now().minusMonths(month.toLong()).with(TemporalAdjusters.firstDayOfMonth())
        return monthBegin.atStartOfDay(ZoneId.systemDefault()).toInstant()
    }

    fun getLastDateOfTheMonth(month: Int): Instant {
        val monthEnd = LocalDate.now().minusMonths(month.toLong()).with(TemporalAdjusters.lastDayOfMonth())
        return monthEnd.atStartOfDay(ZoneId.systemDefault()).toInstant()
    }
    fun getFirstDateOfTheYear(month: Int): Instant {
        val yearBegin = LocalDate.now().minusYears(month.toLong()).with(TemporalAdjusters.firstDayOfYear())
        return yearBegin.atStartOfDay(ZoneId.systemDefault()).toInstant()
    }

    fun getLastDateOfTheYear(month: Int): Instant {
        val yearEnd = LocalDate.now().minusYears(month.toLong()).with(TemporalAdjusters.lastDayOfYear())
        return yearEnd.atStartOfDay(ZoneId.systemDefault()).toInstant()
    }

    fun totalTransactionDataForWalletUser(
        issuanceBank: IssuanceBanks,
        transactionType: TransactionType,
        startDate: Instant?,
        endDate: Instant?,
        accountOwner: AccountOwner,
        transactionMode: TransactionMode?
    ): Int {
        var transactionModeList: MutableSet<TransactionMode>? = null
        if (transactionMode != null) {
            transactionModeList = mutableSetOf(
                transactionMode
            )
        }
        val request = GetTransactionListRequest(
            asset = mutableSetOf(CurrencyUnit.SART.toString()),
            type = mutableSetOf(transactionType),
            status = mutableSetOf(
                TransactionStatus.SUCCESSFUL
            ),
            dateFrom = startDate,
            dateTo = endDate,
            transactionMode = transactionModeList,
            direction = if (transactionType == TransactionType.PEER_TO_PEER) TransactionDirection.OUTGOING else TransactionDirection.UNKNOWN
        )
        val data = transactionService.getTransactions(accountOwner, request)
        return data.size
    }

    fun totalTransactionDeposit(
        issuanceBank: IssuanceBanks,
        transactionType: TransactionType,
        startDate: Instant?,
        endDate: Instant?,
        accountOwner: AccountOwner,
        frequency: String?
    ): Int {
        var dateTo = endDate
        dateTo = dateTo?.plus(1, ChronoUnit.DAYS)
        var startDate = startDate
        startDate = startDate?.minus(1, ChronoUnit.DAYS)
        val request = GetTransactionListRequest(
            asset = mutableSetOf(CurrencyUnit.SART.toString()),
            type = mutableSetOf(transactionType),
            status = mutableSetOf(
                TransactionStatus.SUCCESSFUL
            ),
            dateFrom = null,
            dateTo = null
        )
        val data = transactionService.getTransactions(accountOwner, request)
        val orderbyasc = data.sortedBy { list -> list.createdAt }
        val createdDate = orderbyasc[0].createdAt
        return if (createdDate.isAfter(startDate) && createdDate.isBefore(dateTo)) {
            1
        } else {
            0
        }
    }

    fun calculateFeeDeductionAmount(balance: BigDecimal, fData: IssuanceWalletUserController.FeeConfigDetails): BigDecimal {
        var feeAmountDeduction = BigDecimal.ZERO
        if (fData.feeType == IssuanceWalletUserController.FeeType.Percentage) {
            val dividedValue: BigDecimal = BigDecimal.valueOf(100)
            if (fData.feeAmount != null) {
                feeAmountDeduction = (balance * fData.feeAmount!!) / dividedValue
            }
            if (fData.feeMaximumAmount != null && feeAmountDeduction > fData.feeMaximumAmount) {
                feeAmountDeduction = fData.feeMaximumAmount!!
            }
            if (fData.feeMinimumAmount != null && feeAmountDeduction < fData.feeMinimumAmount) {
                feeAmountDeduction = fData.feeMinimumAmount!!
            }
        }
        if (fData.feeType == IssuanceWalletUserController.FeeType.Fixed) {
            feeAmountDeduction = fData.feeAmount!!
        }
        return feeAmountDeduction
    }

    fun getTransactionData(
        startDate: Instant,
        endDate: Instant,
        accountOwner: AccountOwner
    ): List<Transaction> {
        var dateTo = endDate
        dateTo = dateTo.plus(1, ChronoUnit.DAYS)
        var startDate = startDate
        startDate = startDate.minus(1, ChronoUnit.DAYS)
        val request = GetTransactionListRequest(
            asset = mutableSetOf(CurrencyUnit.SART.toString()),
            status = mutableSetOf(
                TransactionStatus.SUCCESSFUL
            ),
            dateFrom = startDate,
            dateTo = dateTo,
            sortDirection = Sort.Direction.ASC
        )
        return transactionService.getTransactions(accountOwner, request)
    }

    data class Weekly(
        val week1: Int ? = null,
        val week2: Int ? = null,
        val week3: Int ? = null,
        val week4: Int ? = null
    )
    data class WeeklyLabel(
        val week1: String ? = null,
        val week2: String ? = null,
        val week3: String ? = null,
        val week4: String ? = null
    )
    data class WeeklyData(
        val data: Weekly ? = null,
        val labels: WeeklyLabel? = null
    )
    data class Monthly(
        val monthName: String ? = null,
        val monthValue: Int ? = 0,
        val monthOrder: Int ? = 0
    )

    data class Yearly(
        val Y2024: Int ? = 0,
        val Y2023: Int ? = 0,
        val Y2022: Int ? = 0,
        val Y2021: Int ? = 0,
        val Y2020: Int ? = 0
    )

    data class UserDetailsRequest(
        @PositiveOrZero
        val page: Long? = null,
        val email: String ? = null,
        val walletId: String ? = null,
        val firstName: String ? = null,
        val lastName: String ? = null,
        val mobileNumber: String ? = null,
        val fromDate: Date? = null,
        val toDate: Date? = null,
        var createdFrom: Date? = null,
        var createdTo: Date? = null,
        @PositiveOrZero
        val amountFrom: BigDecimal? = null,
        val amountTo: BigDecimal? = null,
        var type: Collection<Status>? = null,
        val sortBy: SortableFields = SortableFields.CRETAEDAT,
        val sortDirection: Sort.Direction = Sort.Direction.DESC,
        val limit: Long = 10
    )
}
