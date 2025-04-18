package com.vacuumlabs.wadzpay.issuance

import com.vacuumlabs.COMMON_PASSWORD
import com.vacuumlabs.ROUNDING_LIMIT
import com.vacuumlabs.vuba.ledger.service.LedgerService
import com.vacuumlabs.wadzpay.algocutomtoken.AlgoCutomeTokenService
import com.vacuumlabs.wadzpay.asset.AssetService
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.DuplicateEntityException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.UnprocessableEntityException
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanksRepository
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanksUserEntry
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanksUserEntryRepository
import com.vacuumlabs.wadzpay.issuance.models.Status
import com.vacuumlabs.wadzpay.issuance.models.UserAccountViewModel
import com.vacuumlabs.wadzpay.issuance.models.toViewModel
import com.vacuumlabs.wadzpay.kyc.models.VerificationStatus
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.Subaccount
import com.vacuumlabs.wadzpay.ledger.model.Transaction
import com.vacuumlabs.wadzpay.ledger.model.TransactionDirection
import com.vacuumlabs.wadzpay.ledger.model.TransactionRepository
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.ledger.service.GetTransactionListRequest
import com.vacuumlabs.wadzpay.ledger.service.TransactionService
import com.vacuumlabs.wadzpay.services.CognitoService
import com.vacuumlabs.wadzpay.services.RedisService
import com.vacuumlabs.wadzpay.user.CustomerType
import com.vacuumlabs.wadzpay.user.RegistrationRequest
import com.vacuumlabs.wadzpay.user.RegistrationResponse
import com.vacuumlabs.wadzpay.user.TransactionDetailsRequest
import com.vacuumlabs.wadzpay.user.TransactionDetailsResponse
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.user.UserAccountRepository
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.user.UserInitializerService
import com.vacuumlabs.wadzpay.viewmodels.TransactionViewModel
import com.vacuumlabs.wadzpay.viewmodels.toViewModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlin.math.ceil
import kotlin.streams.toList

@Service
class IssuanceWalletService(
    val cognitoService: CognitoService,
    val userAccountService: UserAccountService,
    val issuanceBanksUserEntryRepository: IssuanceBanksUserEntryRepository,
    val userAccountRepository: UserAccountRepository,
    val redisService: RedisService,
    val userInitializerService: UserInitializerService,
    val transactionRepository: TransactionRepository,
    val transactionService: TransactionService,
    val issuanceGraphService: IssuanceGraphService,
    val ledgerService: LedgerService,
    val ledgerServiceWadzPay: com.vacuumlabs.wadzpay.ledger.LedgerService,
    val algoCutomeTokenService: AlgoCutomeTokenService,
    @org.springframework.context.annotation.Lazy
    @Autowired
    val assetService: AssetService,
    val issuanceBanksRepository: IssuanceBanksRepository
) {
    fun issuanceWalletRegistration(
        userAccount: UserAccount,
        issuanceBank: IssuanceBanks,
        partnerInstitutionID: String?
    ): IssuanceBanksUserEntry? {
        val walletId = generateWalletId(issuanceBank.id.toInt(), userAccount.id.toInt())
        return IssuanceBanksUserEntry(
            issuanceBanksId = issuanceBank, userAccountId = userAccount,
            status = Status.ENABLE,
            walletId = walletId,
            partnerInstitutionName = partnerInstitutionID
        ).let { saveIssuanceBankUserEntry(it) }
    }

    fun generateWalletId(issuanceBankId: Int, userAccountId: Int): String {
        // Define the desired length of the institution Id
        val institutionLength = 6
        // Define the desired length of the userId
        val userIdLength = 9

        // Convert the number to a string
        var issuanceBankIdStr = issuanceBankId.toString()
        var userAccountIdStr = userAccountId.toString()

        // Pad the number with zeros to ensure the desired length
        while (issuanceBankIdStr.length < institutionLength) {
            issuanceBankIdStr = "0$issuanceBankIdStr"
        }
        println("issuanceBankIdStr ==> $issuanceBankIdStr")
        while (userAccountIdStr.length < userIdLength) {
            userAccountIdStr = "0$userAccountIdStr"
        }
        println("userAccountIdStr==> $userAccountIdStr")
        // Concatenate the prefix and the padded number
        println("Final string ==> $issuanceBankIdStr-$userAccountIdStr")
        return "$issuanceBankIdStr-$userAccountIdStr"
    }

    fun fetchWalletUserToViewModels(
        userDetailsRequest: IssuanceGraphService.UserDetailsRequest,
        userAccount: UserAccount
    ): WalletDataResponse {
        if (userDetailsRequest.createdFrom != null && userDetailsRequest.createdTo != null) {
            val createdTo = userDetailsRequest.createdTo?.toInstant()
            val nextDay = createdTo?.plus(1, ChronoUnit.DAYS)
            userDetailsRequest.createdTo = Date.from(nextDay)
        }
        var walletData = issuanceGraphService.fetchWalletUser(userAccount.issuanceBanks!!, userDetailsRequest).map {
            it.toViewModel()
        }

        walletData.forEach { data ->
            val walletUserAccount = if (data.custonerId != null) {
                userAccountService.getUserAccountByCustomerId(data.custonerId, userAccount.issuanceBanks!!.institutionId)
            } else {
                data.email?.let { userAccountRepository.getByEmail(it) }!!
            }
            val subAccount = walletUserAccount.account.getSubAccountByAssetAlgoString(CurrencyUnit.SART.toString())
            val vubaSubaccount = subAccount?.let { ledgerService.getSubaccount(it.reference) }
            var balance = BigDecimal.ZERO.setScale(ROUNDING_LIMIT, RoundingMode.UP)
            if (vubaSubaccount != null) {
                if (vubaSubaccount.isPresent) {
                    println("Before Rounding Mode balance ==> " + vubaSubaccount.get().balance)
                    println("Rounding Mode ==> " + vubaSubaccount.get().balance.setScale(ROUNDING_LIMIT, RoundingMode.UP).toString())
                    balance = vubaSubaccount.get().balance.setScale(ROUNDING_LIMIT, RoundingMode.UP)
                    println("After Rounding Mode balance ==> " + balance.toString())
                }
            }
            data.tokenBalance = balance.toString()
        }
        walletData = filterByFirstName(walletData, userDetailsRequest)

        val pagination = Pagination(
            current_page = userDetailsRequest.page,
            total_records = walletData.size,
            total_pages = calculateTotalNoPages(walletData.size.toDouble(), userDetailsRequest.limit.toDouble())
        )
        if (userDetailsRequest.page != null && userDetailsRequest.page > 0) {
            val pageNo = userDetailsRequest.page - 1
            walletData =
                walletData.stream().skip(pageNo * userDetailsRequest.limit).limit(userDetailsRequest.limit).toList()
        }
        walletData = walletData.sortedByDescending { list -> list.createdAt }
        return WalletDataResponse(
            fetchAllWalletUserCount(userAccount.issuanceBanks!!),
            fetchAllEnabledDisabledWalletUserCount(userAccount.issuanceBanks!!, Status.ENABLE, null, null),
            fetchAllEnabledDisabledWalletUserCount(userAccount.issuanceBanks!!, Status.DISABLED, null, null),
            walletData,
            pagination
        )
    }

    fun filterByFirstName(
        walletDatas: List<UserAccountViewModel>,
        userDetailsRequest: IssuanceGraphService.UserDetailsRequest
    ): List<UserAccountViewModel> {
        var walletData = walletDatas
        if (userDetailsRequest.firstName != null) {
            walletData = walletData.filter { e ->
                e.firstName?.contains(userDetailsRequest.firstName, ignoreCase = true) ?: false
            }
        }
        if (userDetailsRequest.lastName != null) {
            walletData =
                walletData.filter { e -> e.lastName?.contains(userDetailsRequest.lastName, ignoreCase = true) ?: false }
        }
        if (userDetailsRequest.mobileNumber != null) {
            walletData = walletData.filter { e ->
                e.phoneNumber?.contains(userDetailsRequest.mobileNumber, ignoreCase = true) ?: false
            }
        }
        if (userDetailsRequest.email != null) {
            walletData =
                walletData.filter { e -> e.email?.contains(userDetailsRequest.email, ignoreCase = true) ?: false }
        }
        if (userDetailsRequest.walletId != null) {
            walletData =
                walletData.filter { e -> e.walletId.contains(userDetailsRequest.walletId, ignoreCase = true) }
        }
        if (userDetailsRequest.amountFrom != null && userDetailsRequest.amountTo != null) {
            walletData =
                walletData.filter { e -> e.tokenBalance.toString().toBigDecimal() >= userDetailsRequest.amountFrom && e.tokenBalance.toString().toBigDecimal() <= userDetailsRequest.amountTo }
        }
        if (userDetailsRequest.amountFrom != null) {
            walletData = walletData.filter { e -> e.tokenBalance.toString().toBigDecimal() >= userDetailsRequest.amountFrom }
        }
        if (userDetailsRequest.amountTo != null) {
            walletData = walletData.filter { e -> e.tokenBalance.toString().toBigDecimal() <= userDetailsRequest.amountTo }
        }
        return walletData
    }

    fun fetchAllWalletUserCount(
        issuanceBank: IssuanceBanks
    ): Int? {
        val userDetailsRequest = IssuanceGraphService.UserDetailsRequest(page = null)
        val totalCount = issuanceGraphService.fetchWalletUser(issuanceBank, userDetailsRequest).map {
            it.toViewModel()
        }.size
        return totalCount
    }

    fun fetchAllEnabledDisabledWalletUserCount(
        issuanceBank: IssuanceBanks,
        enable: Status,
        fromDate: Instant?,
        toDate: Instant?
    ): Int? {
        val temp = mutableListOf(
            enable
        )
        val userDetailsRequest = IssuanceGraphService.UserDetailsRequest(page = null)
        userDetailsRequest.type = temp
        if (fromDate != null) {
            userDetailsRequest.createdFrom = Date.from(fromDate)
        }
        if (toDate != null) {
            userDetailsRequest.createdTo = Date.from(toDate)
        }
        val totalCount = issuanceGraphService.fetchWalletUser(issuanceBank, userDetailsRequest).map {
            it.toViewModel()
        }.size
        return totalCount
    }

    fun saveIssuanceBankUserEntry(issuanceBanksUserEntry: IssuanceBanksUserEntry): IssuanceBanksUserEntry {
        return issuanceBanksUserEntryRepository.save(issuanceBanksUserEntry)
    }
    fun loadWalletBalance(
        request: IssuanceCommonController.LoadTokenBalanceRequest,
        userAccount: UserAccount,
        issuanceBank: IssuanceBanks,
        isPaymnentSuccess: Boolean?
    ): TransactionViewModel? {
        val issuanceBanksUserEntry = issuanceBanksUserEntryRepository.getByUserAccountId(userAccount)
        if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.status == Status.DISABLED) {
            throw EntityNotFoundException(ErrorCodes.WALLET_DISABLED)
        }
        ledgerServiceWadzPay.getBalances(userAccount)
        if (request.isFromWallet != null && request.isFromWallet == true) {
            request.bankAccountNumber = "Bank Wallet"
        } else {
            if (request.bankAccountNumber == null) {
                // throw EntityNotFoundException(ErrorCodes.USER_BANK_ACCOUNT_NOT_FOUND)
            }
        }
        val subacc = userAccount.account.getSubaccountByAsset(request.tokenAsset)
        val userAddress = subacc.address!!.address
        if (request.amount.stripTrailingZeros().scale() > request.tokenAsset.maximumNumberOfDigits.toInt()) {
            val am = request.amount.setScale(request.tokenAsset.maximumNumberOfDigits.toInt(), RoundingMode.UP)
                .stripTrailingZeros()
            request.amount = am
        }
        var bcTransId: String? = null
        if (isPaymnentSuccess == true) {
            bcTransId = algoCutomeTokenService.loadCustomTokensToAccount(userAddress, request.amount.toString())
        }
        val omnibusAccount = ledgerServiceWadzPay.getOmnibusAccount()
        val transaction = userInitializerService.createTokenDepositTransaction(
            omnibusAccount.subaccounts.find { it.asset == request.tokenAsset.toString() }!!,
            userAccount.account.subaccounts.find { it.asset == request.tokenAsset.toString() }!!,
            request.tokenAsset,
            request.amount,
            issuanceBank,
            request.bankAccountNumber,
            request.fiatAmount,
            request.fiatAsset,
            bcTransId,
            request.totalFeeApplied,
            request.feeConfigData,
            request.totalRequestedAmount,
            if (isPaymnentSuccess == false) TransactionStatus.FAILED else TransactionStatus.SUCCESSFUL
        )
        if (request.feeConfigData != null) {
            userInitializerService.saveFeeDetails(transaction, request.feeConfigData, issuanceBank, userAccount)
        }
        if (transaction != null) {
            userInitializerService.saveMarkUpDownDetails(IssuanceWalletUserController.TransactionType.LOAD, transaction, userAccount)
        }
        val transactionViewModel = transaction?.toViewModel(direction = TransactionDirection.INCOMING)
        if (transactionViewModel != null) {
            transactionViewModel.feeConfigData = transactionService.getFeeData(transactionViewModel.uuid)
        }
        return transactionViewModel
    }

    fun enableDisableWalletUser(
        request: IssuanceCommonController.EnableDisableWalletUserRequest,
        userAccount: UserAccount,
        issuanceBank: IssuanceBanks
    ): Boolean {
        val issuanceBanksUserEntry = issuanceBanksUserEntryRepository.getByUserAccountId(userAccount)
        if (issuanceBanksUserEntry != null) {
            issuanceBanksUserEntry.isActive = request.isEnabled
            if (request.isEnabled) {
                issuanceBanksUserEntry.status = Status.ENABLE
                cognitoService.enableUser(userAccount)
            } else {
                issuanceBanksUserEntry.status = Status.DISABLED
                cognitoService.disableUser(userAccount)
            }
            issuanceBanksUserEntry.updatedAt = Instant.now()
            issuanceBanksUserEntryRepository.save(issuanceBanksUserEntry)
            return true
        }
        throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
    }

    fun getIssuanceBankMapping(userAccount: UserAccount): IssuanceBanksUserEntry? {
        val issuanceBanksUserEntry = issuanceBanksUserEntryRepository.getByUserAccountId(userAccount)
        println("issunace bank Id 320 ==? " + issuanceBanksUserEntry?.issuanceBanksId)
        if (issuanceBanksUserEntry != null) {
            return issuanceBanksUserEntry
        }
        return null
        // throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
    }
    @Transactional
    fun inviteWalletUser(
        activateWalletUserRequest: IssuanceWalletUserController.ActivateWalletUserRequest,
        userAccountIssuer: UserAccount
    ): UserAccountViewModel? {
        var userAccount = userAccountRepository.getByEmail(activateWalletUserRequest.email)
        if (userAccount == null) {
            userAccount = walletUserRegistration(activateWalletUserRequest.email, activateWalletUserRequest.phoneNumber)
        } else {
            throw DuplicateEntityException(ErrorCodes.EMAIL_ALREADY_EXISTS)
        }
        var issuanceBanksUserEntry = userAccount.let { issuanceBanksUserEntryRepository.getByUserAccountId(it) }
        if (issuanceBanksUserEntry != null) {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_WALLET_USER_ALREDY_MAPPED)
        }
        if (userAccount.firstName == null) {
            userAccount.firstName = activateWalletUserRequest.firstName
        }
        if (userAccount.lastName == null) {
            userAccount.lastName = activateWalletUserRequest.lastName
        }
        userAccount.kycVerified = VerificationStatus.APPROVED_VERIFIED
        userAccount.customerType = CustomerType.WALLET_USER.toString()
        userAccount.createdDate = Instant.now()
        userAccount = userAccountRepository.save(userAccount)
        issuanceBanksUserEntry = userAccountIssuer.issuanceBanks?.let {
            issuanceWalletRegistration(
                userAccount,
                it,
                null
            )
        }
        ledgerServiceWadzPay.getBalances(userAccount)
        val balance = getCryptoBalance(userAccount.account.getSubaccountByAsset(CurrencyUnit.SART))
        val userAccountViewModel = issuanceBanksUserEntry?.toViewModel()
        if (userAccountViewModel != null) {
            userAccountViewModel.tokenBalance = balance.toString()
        }
        return userAccountViewModel
    }

    fun getCryptoBalance(subaccount: Subaccount): BigDecimal =
        ledgerService.getSubaccount(subaccount.reference).get().balance

    fun walletUserRegistration(email: String, phoneNumber: String): UserAccount {
        // check if there is no other account with this email
        /*if (!cognitoService.isEmailAvailable(email)) {
            throw DuplicateEntityException(ErrorCodes.EMAIL_ALREADY_EXISTS)
        }*/
        val userAccountByPhone = userAccountRepository.getByPhoneNumber(phoneNumber)
        if (userAccountByPhone != null) {
            throw DuplicateEntityException(ErrorCodes.PHONE_NUMBER_ALREADY_EXISTS)
        }
        val userAccountByEmail = userAccountRepository.getByEmail(email)
        println("userAccountByEmail ==> $userAccountByEmail")
        if (userAccountByEmail != null) {
            throw DuplicateEntityException(ErrorCodes.EMAIL_ALREADY_EXISTS)
        }
        val cognitoUsername = cognitoService.register(email, phoneNumber, COMMON_PASSWORD, true)

        redisService.deleteRegistrationEntity(email, phoneNumber)
        val userAccount = userAccountService.createUserAccount(cognitoUsername, email, phoneNumber)
        userAccount.customerType = CustomerType.WALLET_USER.toString()
        userAccount.createdDate = Instant.now()
        userAccountService.userAccountRepository.save(userAccount)
        return userAccount
    }
    fun walletUserRegistrationWithDynamicToken(email: String, phoneNumber: String, customerID: String?, userAccountIssuer: UserAccount): UserAccount {
        if (!customerID.isNullOrEmpty()) {
            val userAccountByCustomerID = userAccountRepository.getByCustomerIdIgnoreCase(customerID)
            if (userAccountByCustomerID != null) {
                throw DuplicateEntityException(ErrorCodes.CUSTOMER_ID_ALREADY_EXISTS)
            }
        }
        println("phoneNumber ==> " + phoneNumber)
        if (phoneNumber.isNotEmpty()) {
            val userAccountByPhone = userAccountRepository.getByPhoneNumber(phoneNumber)
            if (userAccountByPhone != null) {
                throw DuplicateEntityException(ErrorCodes.PHONE_NUMBER_ALREADY_EXISTS)
            }
        }
        if (email.isNotEmpty()) {
            val userAccountByEmail = userAccountRepository.getByEmail(email)
            println("userAccountByEmail ==> $userAccountByEmail")
            if (userAccountByEmail != null) {
                throw DuplicateEntityException(ErrorCodes.EMAIL_ALREADY_EXISTS)
            }
        }
        val cognitoUsername = cognitoService.register(email, phoneNumber, COMMON_PASSWORD, false)

        redisService.deleteRegistrationEntity(email, phoneNumber)
        val userAccount = userAccountService.createUserAccountForPrivateBC(cognitoUsername, email, phoneNumber, customerID, userAccountIssuer)
        userAccount.customerType = CustomerType.WALLET_USER.toString()
        userAccount.createdDate = Instant.now()
        userAccountService.userAccountRepository.save(userAccount)
        return userAccount
    }

    fun walletSummary(issuanceBank: IssuanceBanks): IssuanceWalletUserController.WalletSummaryResponse {

        val instant = Instant.now()
        val request = IssuanceGraphService.UserDetailsRequest(
            createdFrom = Date.from(instant.minus(30, ChronoUnit.DAYS))
        )
        val totalWalletData = issuanceGraphService.fetchWalletUser(issuanceBank, request)
        return IssuanceWalletUserController.WalletSummaryResponse(
            totalWallets = fetchAllWalletUserCount(issuanceBank),
            enableWallets = fetchAllEnabledDisabledWalletUserCount(issuanceBank, Status.ENABLE, null, null),
            disabledWallets = fetchAllEnabledDisabledWalletUserCount(issuanceBank, Status.DISABLED, null, null),
            totalDeposits = issuanceGraphService.totalTransactionData(
                issuanceBank,
                TransactionType.DEPOSIT,
                null,
                null
            ),
            totalWalletsInLastThirtyDays = totalWalletData.size,
            enableWalletsInLastThirtyDays = fetchAllEnabledDisabledWalletUserCount(
                issuanceBank,
                Status.ENABLE,
                instant.minus(30, ChronoUnit.DAYS),
                instant
            ),
            totalDepositsInLastThirtyDays = issuanceGraphService.totalTransactionData(
                issuanceBank,
                TransactionType.DEPOSIT,
                instant.minus(30, ChronoUnit.DAYS),
                instant
            )
        )
    }

    fun walletBalance(userAccount: UserAccount): IssuanceWalletUserController.WalletBalanceResponse {
        /*  To get total Wallet Balances */
        val request = IssuanceGraphService.UserDetailsRequest()
        val walletData = issuanceGraphService.fetchWalletUser(userAccount.issuanceBanks!!, request).map {
            it.toViewModel()
        }
        val totalWalletData: MutableList<UserAccountViewModel>
        totalWalletData = walletData as MutableList<UserAccountViewModel>
        totalWalletData.forEach { data ->
            val walletUserAccount = data.email?.let { userAccountRepository.getByEmail(it) }
            val subAccount = walletUserAccount?.account?.getSubAccountByAssetAlgoString(CurrencyUnit.SART.toString())
            val vubaSubaccount = subAccount?.let { ledgerService.getSubaccount(it.reference) }
            var balance = BigDecimal.ZERO
            if (vubaSubaccount != null) {
                if (vubaSubaccount.isPresent) {
                    balance = vubaSubaccount.get().balance
                }
            }
            data.tokenBalance = balance.toString()
        }

        /*  To get total Enabled Wallet Balances */
        val requestEnable = IssuanceGraphService.UserDetailsRequest(type = listOf(Status.ENABLE))
        val walletEnableData = issuanceGraphService.fetchWalletUser(userAccount.issuanceBanks!!, requestEnable).map {
            it.toViewModel()
        }
        val totalEnableWalletData: MutableList<UserAccountViewModel> = walletEnableData as MutableList<UserAccountViewModel>
        totalEnableWalletData.forEach { data ->
            val userAccount = data.email?.let { userAccountRepository.getByEmail(it) }
            val subAccount = userAccount?.account?.getSubAccountByAssetAlgoString(CurrencyUnit.SART.toString())
            val vubaSubaccount = subAccount?.let { ledgerService.getSubaccount(it.reference) }
            var balance = BigDecimal.ZERO
            if (vubaSubaccount != null) {
                if (vubaSubaccount.isPresent) {
                    balance = vubaSubaccount.get().balance
                }
            }
            data.tokenBalance = balance.toString()
        }
        val totalDepositBalance = issuanceGraphService.totalTransactionAmount(
            userAccount.issuanceBanks!!,
            TransactionType.DEPOSIT,
            null,
            null
        ).setScale(ROUNDING_LIMIT, RoundingMode.UP).toString()
        val refundedBalance = issuanceGraphService.totalTransactionAmount(
            userAccount.issuanceBanks!!,
            TransactionType.WITHDRAW,
            null,
            null
        ).setScale(ROUNDING_LIMIT, RoundingMode.UP).toString()
        return IssuanceWalletUserController.WalletBalanceResponse(
            totalWalletBalance = totalDepositBalance.toBigDecimal().add(refundedBalance.toBigDecimal()).toString(),
            enableWalletBalance = totalEnableWalletData.sumOf { it.tokenBalance.toString().toBigDecimal() }.setScale(ROUNDING_LIMIT, RoundingMode.UP).toString(),
            totalDepositBalance = totalDepositBalance,
            refundedBalance = refundedBalance
        )
    }

    fun calculateTotalNoPages(size: Double, limit: Double): Double {
        val totalNoPages = ceil((size / limit))
        return if (totalNoPages > 0) {
            totalNoPages
        } else {
            1.0
        }
    }

    fun transactionGraph(issuanceBank: IssuanceBanks): IssuanceWalletUserController.TransactionGraphResponse {
        return IssuanceWalletUserController.TransactionGraphResponse(
            weeklyTransaction = transactionGraphWeekly(issuanceBank),
            monthlyTransaction = transactionGraphMonthly(issuanceBank),
            yearlyTransaction = transactionGraphYearly(issuanceBank)
        )
    }

    fun transactionGraphWeekly(issuanceBank: IssuanceBanks): IssuanceGraphService.WeeklyData {
        /* Weekly data */
        val todayDate = Instant.now()
        val weekOne = todayDate.minus(7, ChronoUnit.DAYS)
        val weekTwo = weekOne.minus(7, ChronoUnit.DAYS)
        val weekThree = weekTwo.minus(7, ChronoUnit.DAYS)
        val weekFour = weekThree.minus(7, ChronoUnit.DAYS)
        val weekly = IssuanceGraphService.Weekly(
            week4 = issuanceGraphService.totalTransactionData(issuanceBank, null, weekOne, todayDate),
            week3 = issuanceGraphService.totalTransactionData(
                issuanceBank,
                null,
                weekTwo,
                weekOne.minus(1, ChronoUnit.DAYS)
            ),
            week2 = issuanceGraphService.totalTransactionData(
                issuanceBank,
                null,
                weekThree,
                weekTwo.minus(1, ChronoUnit.DAYS)
            ),
            week1 = issuanceGraphService.totalTransactionData(
                issuanceBank,
                null,
                weekFour,
                weekThree.minus(1, ChronoUnit.DAYS)
            )
        )
        val weeklyLabel = IssuanceGraphService.WeeklyLabel(
            week4 = issuanceGraphService.dateReturn(weekOne) + " " + issuanceGraphService.dateReturn(todayDate),
            week3 = issuanceGraphService.dateReturn(weekTwo) + " " + issuanceGraphService.dateReturn(
                weekOne.minus(
                    1,
                    ChronoUnit.DAYS
                )
            ),
            week2 = issuanceGraphService.dateReturn(weekThree) + " " + issuanceGraphService.dateReturn(
                weekTwo.minus(
                    1,
                    ChronoUnit.DAYS
                )
            ),
            week1 = issuanceGraphService.dateReturn(weekFour) + " " + issuanceGraphService.dateReturn(
                weekThree.minus(
                    1,
                    ChronoUnit.DAYS
                )
            )
        )
        return IssuanceGraphService.WeeklyData(
            data = weekly,
            labels = weeklyLabel
        )
    }

    fun transactionGraphMonthly(issuanceBank: IssuanceBanks): MutableList<IssuanceGraphService.Monthly> {
        return issuanceGraphService.calculate12MonthTotalTransactionData(issuanceBank)
    }

    fun transactionGraphYearly(issuanceBank: IssuanceBanks): IssuanceGraphService.Yearly {
        return IssuanceGraphService.Yearly(
            Y2024 = issuanceGraphService.totalTransactionData(
                issuanceBank,
                null,
                issuanceGraphService.getFirstDateOfTheYear(0),
                issuanceGraphService.getLastDateOfTheYear(0)
            ),
            Y2023 = issuanceGraphService.totalTransactionData(
                issuanceBank,
                null,
                issuanceGraphService.getFirstDateOfTheYear(1),
                issuanceGraphService.getLastDateOfTheYear(1)
            ),
            Y2022 = issuanceGraphService.totalTransactionData(
                issuanceBank,
                null,
                issuanceGraphService.getFirstDateOfTheYear(2),
                issuanceGraphService.getLastDateOfTheYear(2)
            ),
            Y2021 = issuanceGraphService.totalTransactionData(
                issuanceBank,
                null,
                issuanceGraphService.getFirstDateOfTheYear(3),
                issuanceGraphService.getLastDateOfTheYear(3)
            ),
            Y2020 = issuanceGraphService.totalTransactionData(
                issuanceBank,
                null,
                issuanceGraphService.getFirstDateOfTheYear(4),
                issuanceGraphService.getLastDateOfTheYear(4)
            )
        )
    }

    fun walletSummaryGraph(issuanceBank: IssuanceBanks): IssuanceWalletUserController.WalletSummaryGraphResponse? {
        return IssuanceWalletUserController.WalletSummaryGraphResponse(
            totalWalletsBalances = issuanceGraphService.totalWalletCount(issuanceBank),
            enabledWalletsBalances = issuanceGraphService.enabledWalletUserGraphData(issuanceBank),
            totalDepositsBalances = issuanceGraphService.totalDepositsGraphData(issuanceBank)
        )
    }

    fun refundToken(
        request: IssuanceCommonController.RefundTokenBalanceRequest,
        userAccount: UserAccount,
        issuanceBanksId: IssuanceBanks,
        isPaymnentSuccess: Boolean?
    ): TransactionViewModel? {

        if (request.isFromWallet != null && request.isFromWallet == true) {
            request.bankAccountNumber = "Bank Wallet"
        } else {
            if (request.bankAccountNumber == null) {
                // throw EntityNotFoundException(ErrorCodes.USER_BANK_ACCOUNT_NOT_FOUND)
            }
        }
        val subAccount = userAccount.account.getSubaccountByAsset(request.tokenAsset)
        val userAddress = subAccount.address!!.address
        userAccount.account.owner?.let { ledgerServiceWadzPay.syncBlockChainBalance(it) }
        val balance = getCryptoBalance(subAccount)
        // val balance = BigDecimal(algoCutomeTokenService.accountAssetBalance(userAddress))
        if (request.amount.stripTrailingZeros().scale() > request.tokenAsset.maximumNumberOfDigits.toInt()) {
            val am = request.amount.setScale(request.tokenAsset.maximumNumberOfDigits.toInt(), RoundingMode.UP)
                .stripTrailingZeros()
            request.amount = am
        }
        if (balance < request.amount) {
            throw UnprocessableEntityException(ErrorCodes.INSUFFICIENT_FUNDS)
        }
        var bcTransId: String ? = null
        if (isPaymnentSuccess == true) {
            bcTransId = algoCutomeTokenService.refundUnspentCustomTokens(userAddress, request.amount.toString())
        }
        val omnibusAccount = ledgerServiceWadzPay.getOmnibusAccount()
        val transaction = userInitializerService.createRefundTransaction(
            userAccount.account.subaccounts.find { it.asset == request.tokenAsset.toString() }!!,
            omnibusAccount.subaccounts.find { it.asset == request.tokenAsset.toString() }!!,
            request.tokenAsset,
            request.amount,
            issuanceBanksId,
            request.bankAccountNumber,
            request.fiatAsset,
            request.fiatAmount,
            bcTransId,
            request.totalFeeApplied,
            request.feeConfigData,
            if (isPaymnentSuccess == false) TransactionStatus.FAILED else TransactionStatus.SUCCESSFUL
        )
        if (request.feeConfigData != null) {
            userInitializerService.saveFeeDetails(transaction, request.feeConfigData, issuanceBanksId, userAccount)
            if (isPaymnentSuccess != false) {
                userInitializerService.createRefundFeeTransaction(
                    userAccount.account.subaccounts.find { it.asset == request.tokenAsset.toString() }!!,
                    omnibusAccount.subaccounts.find { it.asset == request.tokenAsset.toString() }!!,
                    request.tokenAsset,
                    request.amount,
                    request.totalFeeApplied
                )
            }
            if (request.totalFeeApplied != null && isPaymnentSuccess != false) {
                algoCutomeTokenService.refundUnspentCustomTokens(userAddress, request.totalFeeApplied.toString())
            }
        }
        if (transaction != null) {
            userInitializerService.saveMarkUpDownDetails(IssuanceWalletUserController.TransactionType.REFUND, transaction, userAccount)
        }
        val transactionViewModel = transaction?.toViewModel(direction = TransactionDirection.OUTGOING)
        if (transactionViewModel != null) {
            transactionViewModel.feeConfigData = transactionService.getFeeData(transactionViewModel.uuid)
        }
        userAccount.account.owner?.let { ledgerServiceWadzPay.syncBlockChainBalance(it) }
        return transactionViewModel
    }

    fun walletRefund(issuanceBank: IssuanceBanks): IssuanceWalletUserController.WalletRefundResponse {
        val instant = Instant.now()
        return IssuanceWalletUserController.WalletRefundResponse(
            totalRefundRequest = issuanceGraphService.totalTransactionData(
                issuanceBank,
                TransactionType.WITHDRAW,
                instant,
                null
            ),
            totalRefunded = issuanceGraphService.totalTransactionAmount(
                issuanceBank,
                TransactionType.WITHDRAW,
                null,
                null
            ).setScale(ROUNDING_LIMIT, RoundingMode.UP).toString(),
            totalRefundRequestInLastThirtyDays = issuanceGraphService.totalTransactionData(
                issuanceBank,
                TransactionType.WITHDRAW,
                instant.minus(30, ChronoUnit.DAYS),
                instant
            ),
            totalRefundedInLastThirtyDays = issuanceGraphService.totalTransactionAmount(
                issuanceBank,
                TransactionType.WITHDRAW,
                instant.minus(30, ChronoUnit.DAYS),
                instant
            ).setScale(ROUNDING_LIMIT, RoundingMode.UP).toString()
        )
    }

    fun addToken(
        request: IssuanceWalletUserController.SupplyTokenToIssuerRequest,
        userAccount: UserAccount
    ): IssuanceWalletUserController.SupplyTokenToIssuerResponse? {
        val subAcc = userAccount.account.getSubaccountByAssetString(request.tokenName)
        val userAddress = subAcc.address!!.address
        println("userAddress ==> " + userAddress)
        val bcTransId = algoCutomeTokenService.algoCustomTokenTransferFromMinter(userAddress, request.tokenName, request.noOfTokens)
        println("bcTransId ==> $bcTransId")
        var status = TransactionStatus.SUCCESSFUL
        if (bcTransId == ErrorCodes.BLOCKCHAIN_SERVICE_NOT_FOUND || bcTransId == ErrorCodes.WALLET_ADDRESS_NOT_EXIST) {
            status = TransactionStatus.FAILED
        }
        val description = request.noOfTokens.toString() + " " + request.tokenName + " " + "Deposited " + status
        val omnibusAccount = ledgerServiceWadzPay.getOmnibusAccount()
        val transaction = userInitializerService.createTokenDepositTransactionByMinter(
            from = omnibusAccount.subaccounts.find { it.asset == request.tokenName }!!,
            to = userAccount.account.subaccounts.find { it.asset == request.tokenName }!!,
            digitalAsset = request.tokenName,
            digitalAmount = request.noOfTokens,
            null,
            null,
            null,
            blockchainTransId = bcTransId,
            status,
            TransactionType.DEPOSIT,
            description
        )
        val balance = getCryptoBalance(userAccount.account.getSubaccountByAssetString(request.tokenName))
        println("balance ==> $balance")
        println("transaction ==> $transaction")
        if (transaction != null) {
            return IssuanceWalletUserController.SupplyTokenToIssuerResponse(
                customerName = "Why we need this data",
                customerId = "Why we need this data",
                institutionName = userAccount.issuanceBanks?.bankName,
                institutionId = userAccount.issuanceBanks?.institutionId,
                typeOfTransaction = "LOAD",
                transactionId = transaction.uuid.toString(),
                tokenName = request.tokenName,
                noOfTokens = request.noOfTokens,
                receiverName = "",
                status = transaction.status.toString(),
                createdDate = transaction.createdAt
            )
        }
        return null
    }

    fun serviceFeeDeduction(
        request: IssuanceCommonController.ServiceFeeRequest,
        userAccount: UserAccount,
        issuanceBanksId: IssuanceBanks
    ): TransactionViewModel? {
        val subAccount = userAccount.account.getSubaccountByAsset(request.tokenAsset)
        val userAddress = subAccount.address!!.address
        userAccount.account.owner?.let { ledgerServiceWadzPay.syncBlockChainBalance(it) }
        val balance = getCryptoBalance(subAccount)
        if (request.amount.stripTrailingZeros().scale() > request.tokenAsset.maximumNumberOfDigits.toInt()) {
            val am = request.amount.setScale(request.tokenAsset.maximumNumberOfDigits.toInt(), RoundingMode.UP)
                .stripTrailingZeros()
            request.amount = am
        }
        if (balance < request.amount) {
            throw UnprocessableEntityException(ErrorCodes.INSUFFICIENT_FUNDS)
        }
        val bcTransId = algoCutomeTokenService.refundUnspentCustomTokens(userAddress, request.amount.toString())
        val omnibusAccount = ledgerServiceWadzPay.getOmnibusAccount()
        val transaction = userInitializerService.createServiceFeeTransaction(
            userAccount.account.subaccounts.find { it.asset == request.tokenAsset.toString() }!!,
            omnibusAccount.subaccounts.find { it.asset == request.tokenAsset.toString() }!!,
            request.tokenAsset,
            request.amount,
            issuanceBanksId,
            bcTransId,
            request.totalFeeApplied,
            request.feeConfigData,
            request.description,
            request.type
        )
        if (request.feeConfigData != null) {
            userInitializerService.saveFeeDetails(transaction, request.feeConfigData, issuanceBanksId, userAccount)
        }
        val transactionViewModel = transaction?.toViewModel(direction = TransactionDirection.OUTGOING)
        if (transactionViewModel != null) {
            transactionViewModel.feeConfigData = transactionService.getFeeData(transactionViewModel.uuid)
        }
        return transactionViewModel
    }

    fun transferTokens(
        request: IssuanceWalletUserController.TransferTokenRequest,
        issuerUserAccount: UserAccount,
        type: IssuanceWalletUserController.TransactionTypeList
    ): IssuanceWalletUserController.SupplyTokenToIssuerResponse? {
        val validateAsset = assetService.validateAsset(request.tokenName, issuerUserAccount)
        if (!validateAsset) {
            throw EntityNotFoundException(ErrorCodes.TOKEN_NOT_FOUND)
        }
        if (type == IssuanceWalletUserController.TransactionTypeList.BUY) {
            val customerAccount = request.customerId?.let {
                userAccountService.getUserAccountByCustomerId(
                    it,
                    issuerUserAccount.issuanceBanks?.institutionId
                )
            }
            if (customerAccount != null) {
                assetService.createWalletAddressForUserAccount(request.tokenName, issuerUserAccount, customerAccount)
            }
        }
        // val validateTokenAmount = assetService.validateTokenAmount(request.noOfTokens, type, issuerUserAccount, request.tokenName)
        val validateTokenAmount = true
        if (!validateTokenAmount) {
            throw EntityNotFoundException(ErrorCodes.CANNOT_ALLOW_LESS_THEN_MIN_VALUE)
        }

        validateCustomerInstitution(request, issuerUserAccount)
        val to = getRecipientSubAccount(request, type, issuerUserAccount)
        println("to ==> " + to)
        val from = getSenderSubAccount(request, type, issuerUserAccount)
        println("from ==> " + from)
        val transactionType = getTransactionType(type)
        println("transactionType ==> " + transactionType)
        val description = getDescriptionByType(type, request)
        println("description ==> " + description)
        val senderWalletAddress = getSenderWalletAddress(request, type, issuerUserAccount)
        println("senderWalletAddress ==> $senderWalletAddress")
        val recipientWalletAddress = getRecipientWalletAddress(request, type, issuerUserAccount)
        println("recipientWalletAddress ==> $recipientWalletAddress")
        val bcTransId = algoCutomeTokenService.algoCustomTokenTransferByAssetId(
            senderWalletAddress,
            recipientWalletAddress,
            request.tokenName,
            request.noOfTokens
        )

        println("bcTransId ==> $bcTransId")
        var status = TransactionStatus.SUCCESSFUL
        if (bcTransId == ErrorCodes.BLOCKCHAIN_SERVICE_NOT_FOUND || bcTransId == ErrorCodes.WALLET_ADDRESS_NOT_EXIST) {
            status = TransactionStatus.FAILED
        }
        println("from ==> " + from)
        println("to ==> " + to)
        if (from != null && to != null) {
            val transaction = userInitializerService.createTokenDepositTransactionByMinter(
                from = from,
                to = to,
                digitalAsset = request.tokenName,
                digitalAmount = request.noOfTokens,
                issuerUserAccount.issuanceBanks,
                null,
                null,
                blockchainTransId = bcTransId,
                status,
                transactionType,
                description
            )
            var customerUserAccount: UserAccount? = null
            if (!request.customerId.isNullOrEmpty()) {
                customerUserAccount = userAccountService.getUserAccountByCustomerId(
                    request.customerId!!,
                    issuerUserAccount.issuanceBanks?.institutionId
                )
            }

            println("transaction ==> $transaction")
            if (transaction != null) {
                return IssuanceWalletUserController.SupplyTokenToIssuerResponse(
                    customerName = getSenderWalletId(request.customerId, true, issuerUserAccount),
                    customerId = getSenderWalletId(request.customerId, false, issuerUserAccount),
                    customerUpdatedBalance = customerUserAccount?.let { getUpdatedBalance(it, request.tokenName) },
                    institutionName = issuerUserAccount.issuanceBanks?.bankName,
                    institutionId = issuerUserAccount.issuanceBanks?.institutionId,
                    institutionUpdatedBalance = getUpdatedBalance(issuerUserAccount, request.tokenName),
                    typeOfTransaction = getTransactionAPICALL(transaction.type).toString(),
                    transactionId = transaction.uuid.toString(),
                    tokenName = request.tokenName,
                    noOfTokens = request.noOfTokens,
                    receiverCustomerId = getReceiverWalletId(request.receiverCustomerId, false, issuerUserAccount),
                    receiverName = getReceiverWalletId(request.receiverCustomerId, true, issuerUserAccount),
                    status = transaction.status.toString(),
                    createdDate = transaction.createdAt
                )
            }
        }
        return null
    }

    private fun getUpdatedBalance(userAccount: UserAccount, tokenName: String): BigDecimal {
        return getCryptoBalance(userAccount.account.getSubaccountByAssetString(tokenName))
    }

    private fun validateCustomerInstitution(request: IssuanceWalletUserController.TransferTokenRequest, issuerUserAccount: UserAccount) {
        var customerUserAccount: UserAccount? = null
        var receiverUserAccount: UserAccount? = null
        if (!request.customerId.isNullOrEmpty()) {
            customerUserAccount = userAccountService.getUserAccountByCustomerId(
                request.customerId!!,
                issuerUserAccount.issuanceBanks?.institutionId
            )
        }
        if (!request.receiverCustomerId.isNullOrEmpty()) {
            receiverUserAccount = userAccountService.getUserAccountByCustomerId(
                request.receiverCustomerId!!,
                issuerUserAccount.issuanceBanks?.institutionId
            )
        }
        if (customerUserAccount != null) {
            val issuanceBanksUserEntry = getIssuanceBankMapping(customerUserAccount)
            if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.status == Status.DISABLED) {
                throw EntityNotFoundException(ErrorCodes.WALLET_DISABLED)
            }
            println("issuanceBanksUserEntry ===> $issuanceBanksUserEntry")
            println("issuanceBanksUserEntry ===> " + issuerUserAccount.issuanceBanks)
            if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.issuanceBanksId != issuerUserAccount.issuanceBanks) {
                throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
            }
        }
        if (receiverUserAccount != null) {
            val issuanceBanksUserEntry = getIssuanceBankMapping(receiverUserAccount)
            if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.status == Status.DISABLED) {
                throw EntityNotFoundException(ErrorCodes.RECIPIENT_WALLET_NOT_ACTIVATED)
            }
            println("issuanceBanksUserEntry ===> $issuanceBanksUserEntry")
            println("issuanceBanksUserEntry ===> " + issuerUserAccount.issuanceBanks)
            if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.issuanceBanksId != issuerUserAccount.issuanceBanks) {
                throw EntityNotFoundException(ErrorCodes.RECIPIENT_WALLET_REQUIRED)
            }
        }
        if (customerUserAccount != null && receiverUserAccount != null) {
            val customerIDUserEntry = getIssuanceBankMapping(customerUserAccount)
            println("customerIDUserEntry ===> $customerIDUserEntry")

            val receiverUserUserEntry = getIssuanceBankMapping(receiverUserAccount)
            println("receiverUserUserEntry ===> $receiverUserUserEntry")

            if (receiverUserUserEntry != null && customerIDUserEntry != null && receiverUserUserEntry.issuanceBanksId != customerIDUserEntry.issuanceBanksId) {
                throw EntityNotFoundException(ErrorCodes.SENDER_RECEIVER_INSTITUTION_SHOULD_BE_SAME)
            }
            if (customerUserAccount == receiverUserAccount) {
                throw EntityNotFoundException(ErrorCodes.SENDER_RECEIVER_WALLET_ADDRESS_CANT_BE_SAME)
            }
        }
    }
    private fun getReceiverWalletId(
        receiverWalletId: String?,
        isName: Boolean,
        issuerUserAccount: UserAccount
    ): String? {
        /* TODO() Sender Wallet User Detail */
        var receiverUserAccount: UserAccount ? = null
        if (!receiverWalletId.isNullOrEmpty()) {
            receiverUserAccount = userAccountService.getUserAccountByCustomerId(
                receiverWalletId,
                issuerUserAccount.issuanceBanks?.institutionId
            )
        }
        if (receiverUserAccount != null) {
            return if (isName) {
                receiverUserAccount.firstName + " " + if (receiverUserAccount.lastName != null) receiverUserAccount.lastName else ""
            } else {
                receiverWalletId
            }
        }
        return null
    }

    private fun getSenderWalletId(
        customerId: String?,
        isName: Boolean,
        issuerUserAccount: UserAccount
    ): String? {
        /* TODO() Sender Wallet User Detail */
        var senderUserAccount: UserAccount ? = null
        if (!customerId.isNullOrEmpty()) {
            senderUserAccount = userAccountService.getUserAccountByCustomerId(
                customerId,
                issuerUserAccount.issuanceBanks?.institutionId
            )
        }
        if (senderUserAccount != null) {
            return if (isName) {
                senderUserAccount.firstName + " " + if (senderUserAccount.lastName != null) senderUserAccount.lastName else ""
            } else {
                customerId
            }
        }
        return null
    }

    private fun getDescriptionByType(
        type: IssuanceWalletUserController.TransactionTypeList,
        data: IssuanceWalletUserController.TransferTokenRequest
    ): String {
        return when (type) {
            IssuanceWalletUserController.TransactionTypeList.SELL -> {
                data.noOfTokens.toString() + " " + data.tokenName + " " + "Sold"
            }
            IssuanceWalletUserController.TransactionTypeList.TRANSFER -> {
                data.noOfTokens.toString() + " " + data.tokenName + " " + "transfer"
            }
            IssuanceWalletUserController.TransactionTypeList.WITHDRAW -> {
                data.noOfTokens.toString() + " " + data.tokenName + " " + "withdrawal"
            }
            IssuanceWalletUserController.TransactionTypeList.LOAD -> {
                data.noOfTokens.toString() + " " + data.tokenName + " " + "deposited"
            }
            else -> {
                data.noOfTokens.toString() + " " + data.tokenName + " " + "Buy"
            }
        }
    }

    fun getTransactionType(type: IssuanceWalletUserController.TransactionTypeList): TransactionType {
        return when (type) {
            IssuanceWalletUserController.TransactionTypeList.SELL -> {
                TransactionType.SELL
            }
            IssuanceWalletUserController.TransactionTypeList.TRANSFER -> {
                TransactionType.PEER_TO_PEER
            }
            IssuanceWalletUserController.TransactionTypeList.WITHDRAW -> {
                TransactionType.WITHDRAW
            }
            IssuanceWalletUserController.TransactionTypeList.LOAD -> {
                TransactionType.DEPOSIT
            }
            else -> {
                TransactionType.BUY
            }
        }
    }

    fun getTransactionAPICALL(type: TransactionType): IssuanceWalletUserController.TransactionTypeList {
        return when (type) {
            TransactionType.SELL -> {
                IssuanceWalletUserController.TransactionTypeList.SELL
            }
            TransactionType.PEER_TO_PEER -> {
                IssuanceWalletUserController.TransactionTypeList.TRANSFER
            }
            TransactionType.WITHDRAW -> {
                IssuanceWalletUserController.TransactionTypeList.WITHDRAW
            }
            TransactionType.DEPOSIT -> {
                IssuanceWalletUserController.TransactionTypeList.LOAD
            }
            else -> {
                IssuanceWalletUserController.TransactionTypeList.BUY
            }
        }
    }

    fun getTransactionType(type: TransactionType): String {
        return when (type) {
            TransactionType.SELL -> {
                "Debit"
            }
            TransactionType.PEER_TO_PEER -> {
                "Debit"
            }
            TransactionType.WITHDRAW -> {
                "Debit"
            }
            TransactionType.DEPOSIT -> {
                "Credit"
            }
            else -> {
                "Credit"
            }
        }
    }
    fun getTransactionCustomerId(type: TransactionViewModel, isName: Boolean): String? {
        when (type.transactionType) {
            TransactionType.SELL -> {
                return if (isName) {
                    type.senderFirstName + " " + (type.senderLastName ?: "")
                } else {
                    type.senderDetails
                }
            }
            TransactionType.PEER_TO_PEER -> {
                return if (isName) {
                    type.senderFirstName + " " + (type.senderLastName ?: "")
                } else {
                    type.senderDetails
                }
            }
            TransactionType.WITHDRAW -> {
                return if (isName) {
                    type.senderFirstName + " " + (type.senderLastName ?: "")
                } else {
                    type.senderDetails
                }
            }
            TransactionType.DEPOSIT -> {
                return null
            }
            else -> {
                return if (isName) {
                    type.receiverFirstName + " " + (type.receiverFirstName ?: "")
                } else {
                    type.receiverDetails
                }
            }
        }
    }

    fun getTransactionRecieverId(type: TransactionViewModel, isName: Boolean): String? {
        return when (type.transactionType) {
            TransactionType.PEER_TO_PEER -> {
                if (isName) {
                    type.receiverFirstName + " " + (type.receiverLastName ?: "")
                } else {
                    type.receiverDetails
                }
            }
            else -> {
                null
            }
        }
    }

    private fun getRecipientSubAccount(
        request: IssuanceWalletUserController.TransferTokenRequest,
        type: IssuanceWalletUserController.TransactionTypeList,
        issuerUserAccount: UserAccount
    ): Subaccount? {
        /* TODO() Issuer Wallet User Detail */
        val issuerSubAcc = issuerUserAccount.account.getSubaccountByAssetString(request.tokenName)

        /* TODO() Minter Wallet User Detail */
        val omnibusAccount = ledgerServiceWadzPay.getOmnibusAccount()
        val minterSubAcc = omnibusAccount.subaccounts.find { it.asset == request.tokenName }

        /* TODO() Sender Wallet User Detail */
        var customerUserAccount: UserAccount ? = null
        if (!request.customerId.isNullOrEmpty()) {
            println("request.customerId ==> " + request.customerId)
            customerUserAccount = userAccountService.getUserAccountByCustomerId(
                customerId = request.customerId!!,
                issuerUserAccount.issuanceBanks?.institutionId
            )
        }

        /* TODO() ReceiverCustomer Wallet User Detail */
        var receiverCustomerAccount: UserAccount? = null
        if (!request.receiverCustomerId.isNullOrEmpty()) {
            receiverCustomerAccount = userAccountService.getUserAccountByCustomerId(request.receiverCustomerId!!, issuerUserAccount.issuanceBanks?.institutionId)
        }
        return if (type == IssuanceWalletUserController.TransactionTypeList.SELL && customerUserAccount != null) {
            val subAccount = customerUserAccount.account.getSubAccountByAssetAlgoString(request.tokenName)
            val vubaSubaccount = subAccount?.let { ledgerService.getSubaccount(it.reference) }
            if (vubaSubaccount != null) {
                if (vubaSubaccount.isPresent) {
                    if (vubaSubaccount.get().balance < request.noOfTokens) {
                        throw BadRequestException(ErrorCodes.INSUFFICIENT_FUNDS)
                    }
                }
            } else {
                throw BadRequestException(ErrorCodes.INSUFFICIENT_FUNDS)
            }
            issuerSubAcc
        } else if (type == IssuanceWalletUserController.TransactionTypeList.TRANSFER && receiverCustomerAccount != null && customerUserAccount != null) {
            val subAccount = customerUserAccount.account.getSubAccountByAssetAlgoString(request.tokenName)
            println("subAccount ==> " + subAccount)
            val vubaSubaccount = subAccount?.let { ledgerService.getSubaccount(it.reference) }
            println("vubaSubaccount ==> " + vubaSubaccount)
            if (vubaSubaccount != null) {
                if (vubaSubaccount.isPresent) {
                    if (vubaSubaccount.get().balance < request.noOfTokens) {
                        throw BadRequestException(ErrorCodes.INSUFFICIENT_FUNDS)
                    }
                }
            } else {
                throw BadRequestException(ErrorCodes.INSUFFICIENT_FUNDS)
            }
            val receiveSubAccount = receiverCustomerAccount.account.getSubAccountByAssetAlgoString(request.tokenName)
            println("receiveSubAccount in case of transfer==> $receiveSubAccount")
            if (receiveSubAccount == null) {
                assetService.createWalletAddressForUserAccount(
                    request.tokenName,
                    issuerUserAccount,
                    receiverCustomerAccount
                )
            }
            receiverCustomerAccount.account.getSubaccountByAssetString(request.tokenName)
        } else if (type == IssuanceWalletUserController.TransactionTypeList.WITHDRAW && customerUserAccount != null) {
            val subAccount = customerUserAccount.account.getSubAccountByAssetAlgoString(request.tokenName)
            val vubaSubaccount = subAccount?.let { ledgerService.getSubaccount(it.reference) }
            if (vubaSubaccount != null) {
                if (vubaSubaccount.isPresent) {
                    if (vubaSubaccount.get().balance < request.noOfTokens) {
                        throw BadRequestException(ErrorCodes.INSUFFICIENT_FUNDS)
                    }
                }
            } else {
                throw BadRequestException(ErrorCodes.INSUFFICIENT_FUNDS)
            }
            minterSubAcc
        } else if (type == IssuanceWalletUserController.TransactionTypeList.BUY && customerUserAccount != null) {
            customerUserAccount.account.getSubaccountByAssetString(request.tokenName)
        } else {
            issuerSubAcc
        }
    }

    private fun getSenderSubAccount(
        data: IssuanceWalletUserController.TransferTokenRequest,
        type: IssuanceWalletUserController.TransactionTypeList,
        issuerUserAccount: UserAccount
    ): Subaccount? {
        /* TODO() Issuer Wallet User Detail */
        val issuerSubAcc = issuerUserAccount.account.getSubaccountByAssetString(data.tokenName)

        /* TODO() Minter Wallet User Detail */
        val omnibusAccount = ledgerServiceWadzPay.getOmnibusAccount()
        val minterSubAcc = omnibusAccount.subaccounts.find { it.asset == data.tokenName }

        /* TODO() Sender Wallet User Detail */
        var senderSubAcc: Subaccount? = null
        var senderUserAccount: UserAccount ? = null
        if (!data.customerId.isNullOrEmpty()) {
            senderUserAccount = userAccountService.getUserAccountByCustomerId(data.customerId!!, issuerUserAccount.issuanceBanks?.institutionId)
            senderSubAcc = senderUserAccount.account.getSubaccountByAssetString(data.tokenName)
        }
        /* TODO() ReceiverCustomer Wallet User Detail */
        var receiverCustomerSubAcc: Subaccount? = null
        if (!data.receiverCustomerId.isNullOrEmpty()) {
            val receiverCustomerAccount = userAccountService.getUserAccountByCustomerId(data.receiverCustomerId!!, issuerUserAccount.issuanceBanks?.institutionId)
            receiverCustomerSubAcc = receiverCustomerAccount.account.getSubaccountByAssetString(data.tokenName)
        }
        return if (type == IssuanceWalletUserController.TransactionTypeList.BUY && receiverCustomerSubAcc != null) {
            issuerSubAcc
        } else if (type == IssuanceWalletUserController.TransactionTypeList.SELL && senderSubAcc != null) {
            senderSubAcc
        } else if (type == IssuanceWalletUserController.TransactionTypeList.LOAD) {
            minterSubAcc
        } else if (type == IssuanceWalletUserController.TransactionTypeList.WITHDRAW && senderSubAcc != null) {
            senderSubAcc
        } else if (type == IssuanceWalletUserController.TransactionTypeList.TRANSFER && receiverCustomerSubAcc != null && senderUserAccount != null) {
            senderSubAcc
        } else {
            issuerSubAcc
        }
    }

    private fun getRecipientWalletAddress(
        request: IssuanceWalletUserController.TransferTokenRequest,
        type: IssuanceWalletUserController.TransactionTypeList,
        issuerUserAccount: UserAccount
    ): String {
        /* TODO() Issuer Wallet User Detail */
        val issuerSubAcc = issuerUserAccount.account.getSubaccountByAssetString(request.tokenName)
        val issuerAddress = issuerSubAcc.address!!.address

        /* TODO() Minter Wallet User Detail */
        val minterWalletAddress = algoCutomeTokenService.getMinterWalletAdress(request.tokenName)
        println("minterWalletAddress ==> $minterWalletAddress")

        /* TODO() Sender Wallet User Detail */
        val senderSubAcc: Subaccount?
        var senderUserAccount: UserAccount ?
        var senderAddress: String? = null
        if (!request.customerId.isNullOrEmpty()) {
            senderUserAccount = userAccountService.getUserAccountByCustomerId(request.customerId!!, issuerUserAccount.issuanceBanks?.institutionId)
            senderSubAcc = senderUserAccount.account.getSubaccountByAssetString(request.tokenName)
            senderAddress = senderSubAcc.address!!.address
        }

        /* TODO() ReceiverCustomer Wallet User Detail */
        var receiverCustomerAddress: String? = null
        if (!request.receiverCustomerId.isNullOrEmpty()) {
            val receiverCustomerAccount = userAccountService.getUserAccountByCustomerId(request.receiverCustomerId!!, issuerUserAccount.issuanceBanks?.institutionId)
            val receiverCustomerSubAcc = receiverCustomerAccount.account.getSubaccountByAssetString(request.tokenName)
            receiverCustomerAddress = receiverCustomerSubAcc.address!!.address
        }
        return if (type == IssuanceWalletUserController.TransactionTypeList.SELL || type == IssuanceWalletUserController.TransactionTypeList.LOAD) {
            issuerAddress
        } else if (type == IssuanceWalletUserController.TransactionTypeList.BUY && senderAddress != null) {
            senderAddress
        } else if (type == IssuanceWalletUserController.TransactionTypeList.TRANSFER && receiverCustomerAddress != null) {
            receiverCustomerAddress
        } else if (type == IssuanceWalletUserController.TransactionTypeList.WITHDRAW && minterWalletAddress != null) {
            minterWalletAddress
        } else {
            issuerAddress
        }
    }

    private fun getSenderWalletAddress(
        request: IssuanceWalletUserController.TransferTokenRequest,
        type: IssuanceWalletUserController.TransactionTypeList,
        issuerUserAccount: UserAccount
    ): String {
        /* TODO() Issuer Wallet User Detail */
        val issuerSubAcc = issuerUserAccount.account.getSubaccountByAssetString(request.tokenName)
        val issuerAddress = issuerSubAcc.address!!.address

        val minterWalletAddress = algoCutomeTokenService.getMinterWalletAdress(request.tokenName)
        println("minterWalletAddress ==> $minterWalletAddress")

        /* TODO() Sender Wallet User Detail */
        val senderSubAcc: Subaccount?
        var senderUserAccount: UserAccount ? = null
        var senderAddress: String? = null
        if (!request.customerId.isNullOrEmpty()) {
            senderUserAccount = userAccountService.getUserAccountByCustomerId(request.customerId!!, issuerUserAccount.issuanceBanks?.institutionId)
            senderSubAcc = senderUserAccount.account.getSubaccountByAssetString(request.tokenName)
            senderAddress = senderSubAcc.address!!.address
        }

        /* TODO() ReceiverCustomer Wallet User Detail */
        var receiverCustomerAddress: String? = null
        if (!request.receiverCustomerId.isNullOrEmpty()) {
            val receiverCustomerAccount = userAccountService.getUserAccountByCustomerId(request.receiverCustomerId!!, issuerUserAccount.issuanceBanks?.institutionId)
            val receiverCustomerSubAcc = receiverCustomerAccount.account.getSubaccountByAssetString(request.tokenName)
            receiverCustomerAddress = receiverCustomerSubAcc.address!!.address
        }
        return if (type == IssuanceWalletUserController.TransactionTypeList.SELL && senderUserAccount != null && senderAddress != null) {
            senderAddress
        } else if (type == IssuanceWalletUserController.TransactionTypeList.TRANSFER && receiverCustomerAddress != null && senderUserAccount != null && senderAddress != null) {
            senderAddress
        } else if (type == IssuanceWalletUserController.TransactionTypeList.BUY) {
            issuerAddress
        } else if (type == IssuanceWalletUserController.TransactionTypeList.WITHDRAW && senderUserAccount != null && senderAddress != null) {
            senderAddress
        } else if (type == IssuanceWalletUserController.TransactionTypeList.LOAD && minterWalletAddress != null) {
            minterWalletAddress
        } else {
            issuerAddress
        }
    }

    @Transactional
    fun walletUserRegistration(
        registrationRequest: RegistrationRequest,
        userAccountIssuer: UserAccount
    ): RegistrationResponse {
        val nameSplit = registrationRequest.customerName
        val parts = nameSplit.split("\\s+".toRegex())
        var userAccount = walletUserRegistrationWithDynamicToken(registrationRequest.customerEmail, registrationRequest.customerMobile, registrationRequest.customerId, userAccountIssuer)
        if (userAccount.firstName == null) {
            userAccount.firstName = parts[0]
        }
        if (parts.size > 1) {
            println(parts[1])
            if (userAccount.lastName == null) {
                userAccount.lastName = parts[1]
            }
        }
        userAccount.kycVerified = VerificationStatus.APPROVED_VERIFIED
        userAccount = userAccountRepository.save(userAccount)
        val issuanceBanksUserEntry =
            issuanceWalletRegistration(
                userAccount,
                userAccountIssuer.issuanceBanks!!,
                registrationRequest.partnerInstitutionId
            )
        return RegistrationResponse(
            customerId = registrationRequest.customerId,
            customerName = (if (userAccount.lastName != null) userAccount.firstName + " " + userAccount.lastName else userAccount.firstName)!!,
            customerRegistrationNumber = userAccount.id,
            customerMobile = userAccount.phoneNumber,
            customerEmail = userAccount.email,
            institutionName = issuanceBanksUserEntry?.issuanceBanksId?.bankName,
            institutionId = issuanceBanksUserEntry?.issuanceBanksId?.institutionId,
            customerWalletId = issuanceBanksUserEntry?.walletId,
            customerType = userAccount.customerType,
            createdDate = userAccount.createdDate,
            partnerInstitutionId = issuanceBanksUserEntry?.partnerInstitutionName,
            status = TransactionStatus.SUCCESSFUL.toString()
        )
    }

    fun getTransactionDetails(
        request: TransactionDetailsRequest,
        issuerUserAccount: UserAccount
    ): MutableList<TransactionDetailsResponse> {
        var userAccount = issuerUserAccount
        if (!request.customerId.isNullOrEmpty()) {
            userAccount = userAccountService.getUserAccountByCustomerId(request.customerId, issuerUserAccount.issuanceBanks?.institutionId)
        } /*else if (!request.customerEmail.isNullOrEmpty()) {
            userAccount = userAccountService.getUserAccountByEmail(request.customerEmail)
        }*/
        // if (!request.customerID.isNullOrEmpty() || !request.customerEmail.isNullOrEmpty()) {
        if (!request.customerId.isNullOrEmpty()) {
            val issuanceBanksUserEntry = getIssuanceBankMapping(userAccount)
            if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.status == Status.DISABLED) {
                throw EntityNotFoundException(ErrorCodes.WALLET_DISABLED)
            }
            if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.issuanceBanksId != issuerUserAccount.issuanceBanks) {
                throw EntityNotFoundException(ErrorCodes.CUSTOMER_NOT_FOUND)
            }
        }
        var startDate = request.fromDate
        var dateTo = request.toDate
        if (startDate != null) {
            startDate = startDate.minus(1, ChronoUnit.DAYS)
            if (dateTo == null) {
                dateTo = Instant.now()
            }
        }
        if (dateTo != null) {
            dateTo = dateTo.plus(1, ChronoUnit.DAYS)
        }
        val transactionDetailsResponse: MutableList<TransactionDetailsResponse> = mutableListOf()
        if (userAccount.issuanceBanks != null) {
            var data = issuanceGraphService.totalTransactionDataList(userAccount.issuanceBanks!!, null, startDate, dateTo)
            if (data != null) {
                if (request.transactionType != null) {
                    data = data.filter { e -> e.type == getTransactionType(request.transactionType!!) } as MutableList<Transaction>
                }
                if (request.tokenName != null) {
                    data = data.filter { e -> e.asset == request.tokenName } as MutableList<Transaction>
                }
            }
            var listResponse = data?.map {
                it.toViewModel(
                    direction = TransactionDirection.INCOMING
                )
            }
            listResponse = listResponse?.sortedByDescending { list -> list.createdAt }
            if (request.fromDate == null) {
                listResponse = listResponse?.stream()?.skip(0 * 10)?.limit(10)?.toList()
            }
            listResponse?.forEach { tData ->
                transactionDetailsResponse.add(
                    TransactionDetailsResponse(
                        customerName = getTransactionCustomerId(tData, true),
                        customerId = getTransactionCustomerId(tData, false),
                        institutionName = issuerUserAccount.issuanceBanks?.bankName,
                        institutionId = issuerUserAccount.issuanceBanks?.institutionId,
                        transactionId = tData.uuid.toString(),
                        tokenName = tData.asset,
                        noOfTokens = tData.amount,
                        transactionDate = tData.createdAt,
                        transactionType = getTransactionAPICALL(tData.transactionType).toString(),
                        ledgerType = getTransactionType(tData.transactionType),
                        transactionStatus = tData.status.toString(),
                        receiverId = getTransactionRecieverId(tData, false),
                        receiverName = getTransactionRecieverId(tData, true)
                    )
                )
            }
        } else {
            val tRequest = GetTransactionListRequest(
                type = if (request.transactionType != null) mutableSetOf(getTransactionType(request.transactionType!!)) else null,
                dateFrom = startDate,
                dateTo = dateTo,
                asset = if (request.tokenName != null) mutableSetOf(request.tokenName) else null
            )
            val data = userAccount.account.owner?.let { transactionService.getTransactions(it, tRequest) }
            var listResponse = data?.map {
                it.toViewModel(
                    direction = TransactionDirection.INCOMING
                )
            }
            listResponse = listResponse?.sortedByDescending { list -> list.createdAt }
            if (request.fromDate == null) {
                listResponse = listResponse?.stream()?.skip(0 * 10)?.limit(10)?.toList()
            }
            listResponse?.forEach { tData ->
                transactionDetailsResponse.add(
                    TransactionDetailsResponse(
                        customerName = getTransactionCustomerId(tData, true),
                        customerId = getTransactionCustomerId(tData, false),
                        institutionName = issuerUserAccount.issuanceBanks?.bankName,
                        institutionId = issuerUserAccount.issuanceBanks?.institutionId,
                        transactionId = tData.uuid.toString(),
                        tokenName = tData.asset,
                        noOfTokens = tData.amount,
                        transactionDate = tData.createdAt,
                        transactionType = getTransactionAPICALL(tData.transactionType).toString(),
                        ledgerType = getTransactionType(tData.transactionType),
                        transactionStatus = tData.status.toString(),
                        receiverId = getTransactionRecieverId(tData, false),
                        receiverName = getTransactionRecieverId(tData, true)
                    )
                )
            }
        }
        return transactionDetailsResponse
    }

    fun mappingWithInstitution(institutionId: String, userAccount: UserAccount): Boolean {
        val issuanceBank = issuanceBanksRepository.getByInstitutionId(institutionId)
        if (issuanceBank != null) {
            issuanceWalletRegistration(
                userAccount,
                issuanceBank,
                null
            )
        }
        return true
    }

    fun getBankName(email: String): Map<String, String?> {
        val userAccount = userAccountRepository.getByEmail(email)
            ?: throw IllegalArgumentException("User with email $email not found")

        val userEntry = issuanceBanksUserEntryRepository.getByUserAccountId(userAccount)
            ?: throw IllegalArgumentException("No issuance bank entry found for user ID ${userAccount.id}")

        return mapOf(
            "bankName" to userEntry.issuanceBanksId.bankName
        )
    }

    data class WalletDataResponse(
        val totalCount: Int? = 0,
        val totalEnabled: Int? = 0,
        val totalDisabled: Int? = 0,
        val walletList: List<UserAccountViewModel>? = null,
        val pagination: Pagination? = null
    )

    data class Pagination(
        val current_page: Long? = 0,
        val total_records: Int = 0,
        val total_pages: Double = 0.0
    )
}
