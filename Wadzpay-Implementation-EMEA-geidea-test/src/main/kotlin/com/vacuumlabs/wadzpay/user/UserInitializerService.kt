package com.vacuumlabs.wadzpay.user

import com.vacuumlabs.vuba.ledger.api.dsl.commit
import com.vacuumlabs.vuba.ledger.common.Reference
import com.vacuumlabs.wadzpay.auth.Role
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.issuance.IssuanceCommonController
import com.vacuumlabs.wadzpay.issuance.IssuanceService
import com.vacuumlabs.wadzpay.issuance.IssuanceWalletUserController
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import com.vacuumlabs.wadzpay.issuance.models.IssuanceWalletConfig
import com.vacuumlabs.wadzpay.issuance.models.IssuanceWalletConfigRepository
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.LedgerService
import com.vacuumlabs.wadzpay.ledger.ReferenceConventions
import com.vacuumlabs.wadzpay.ledger.SubaccountService
import com.vacuumlabs.wadzpay.ledger.model.Account
import com.vacuumlabs.wadzpay.ledger.model.AccountOwnerRepository
import com.vacuumlabs.wadzpay.ledger.model.AccountRepository
import com.vacuumlabs.wadzpay.ledger.model.AccountType
import com.vacuumlabs.wadzpay.ledger.model.Subaccount
import com.vacuumlabs.wadzpay.ledger.model.SubaccountRepository
import com.vacuumlabs.wadzpay.ledger.model.Transaction
import com.vacuumlabs.wadzpay.ledger.model.TransactionRepository
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.ledger.model.TransactionWalletFeeDetails
import com.vacuumlabs.wadzpay.ledger.model.TransactionWalletFeeDetailsRepository
import com.vacuumlabs.wadzpay.ledger.service.TransactionService
import com.vacuumlabs.wadzpay.merchant.CreateMerchantRequest
import com.vacuumlabs.wadzpay.merchant.MerchantService
import com.vacuumlabs.wadzpay.merchant.model.CountryCode
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.IndustryType
import com.vacuumlabs.wadzpay.merchant.model.Merchant
import com.vacuumlabs.wadzpay.merchant.model.MerchantRepository
import com.vacuumlabs.wadzpay.services.CognitoService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.util.UUID
import com.vacuumlabs.vuba.ledger.service.LedgerService as VubaLedgerService

@Service
class UserInitializerService(
    val ledgerService: LedgerService,
    val vubaLedgerService: VubaLedgerService,
    val userAccountService: UserAccountService,
    val userAccountRepository: UserAccountRepository,
    val merchantService: MerchantService,
    val merchantRepository: MerchantRepository,
    val transactionRepository: TransactionRepository,
    val cognitoService: CognitoService,
    val accountOwnerRepository: AccountOwnerRepository,
    val accountRepository: AccountRepository,
    val subaccountRepository: SubaccountRepository,
    val subaccountService: SubaccountService,
    val transactionService: TransactionService,
    val issuanceWalletConfigRepository: IssuanceWalletConfigRepository,
    val transactionWalletFeeDetailsRepository: TransactionWalletFeeDetailsRepository,
    val issuanceService: IssuanceService
) {
    val usersCount = 10

    val merchantName = "Fake Crypto"
    val wadzpayMerchantName = "Demo Wadzpay"

    fun getFakeMerchant(): Merchant {
        val merchant = merchantRepository.getByName(merchantName)
        if (merchant == null) {
            merchantService.createMerchant(
                CreateMerchantRequest(
                    merchantName,
                    CountryCode.IN,
                    "123456",
                    "John Wick",
                    "name1@email.com",
                    "123456",
                    "MONEY",
                    IndustryType.OTHER_INDUSTRY,
                    null
                )
            )
            return merchantService.getMerchantByName(merchantName)
        }
        return merchant
    }

    fun initializeUsers() {
        repeat(usersCount) {
            registerFakeUser(it)
        }
    }

    fun deleteTransactions(userAccount: UserAccount) {
        userAccount.account.subaccounts.flatMap { it.incomingTransactions + it.outgoingTransactions }.forEach {
            deleteFakeTransaction(it)
        }
    }

    fun deleteAllUsers() {
        repeat(usersCount) {
            deleteFakeUser(it)
        }
    }

    fun deleteFakeTransaction(transaction: Transaction) {
        val commitReference = ReferenceConventions.commit(UUID.randomUUID())
        vubaLedgerService.createCommit(
            commit {
                reference(commitReference)
                subaccountEntry(transaction.receiver.reference, transaction.amount.negate())
                subaccountEntry(transaction.sender.reference, transaction.amount)
            }
        )
        transactionRepository.delete(transaction)
    }

    @Transactional
    fun createFakeTransaction(from: Account, to: Account, asset: CurrencyUnit, amount: BigDecimal) =
        createFakeTransaction(from.getSubaccountByAsset(asset), to.getSubaccountByAsset(asset), asset, amount)

    @Transactional
    fun createFakeTransaction(from: Subaccount, to: Subaccount, asset: CurrencyUnit, amount: BigDecimal): Transaction {
        val logger = LoggerFactory.getLogger(javaClass)
        val commitReference = ReferenceConventions.commit(UUID.randomUUID())
        logger.info("CommitRef::")
        logger.info(commitReference.toString())
        vubaLedgerService.createCommit(
            commit {
                reference(commitReference)
                subaccountEntry(from.reference, amount.negate())
                subaccountEntry(to.reference, amount)
            }
        )
        val trx = transactionRepository.save(
            Transaction(
                reference = commitReference, sender = from, receiver = to, asset = asset.name,
                status = TransactionStatus.SUCCESSFUL, createdAt = Instant.now(), amount = amount,
                type = TransactionType.OTHER
            )
        )
        return transactionRepository.save(trx.setTxId(id = trx.id))
    }

    @Transactional
    fun createFakeBuyTransaction(
        from: Subaccount,
        to: Subaccount,
        asset: CurrencyUnit,
        amount: BigDecimal,
        createTransactionEntry: Boolean = true,
        fiatAsset: FiatCurrencyUnit,
        fiatAmount: BigDecimal,
        toDigitalAsset: CurrencyUnit,
        toDigitalAmount: BigDecimal,
        isSibos: Boolean
    ): Transaction? {
        val logger = LoggerFactory.getLogger(javaClass)
        val commitReference = ReferenceConventions.commit(UUID.randomUUID())
        logger.info("CommitRef::")
        logger.info(commitReference.toString())
        vubaLedgerService.createCommit(
            commit {
                reference(commitReference)
                subaccountEntry(from.reference, amount.negate())
                subaccountEntry(to.reference, amount)
            }
        )
        var fiatAmountDes = if (fiatAmount.stripTrailingZeros().scale() > 2) {
            fiatAmount.setScale(2, RoundingMode.UP).stripTrailingZeros().toString()
        } else {
            fiatAmount.stripTrailingZeros().toPlainString()
        }
        if (createTransactionEntry) {
            val trx = transactionRepository.save(
                Transaction(
                    reference = commitReference,
                    sender = from,
                    receiver = to,
                    asset = asset.name,
                    status = TransactionStatus.SUCCESSFUL,
                    createdAt = Instant.now(),
                    amount = amount,
                    type = TransactionType.BUY,
                    description = amount.toString() + " " + asset.name + " for " + fiatAmountDes + " " + fiatAsset
                )
            )
            transactionService.showPushNotifications(trx)
            return transactionRepository.save(trx.setTxId(id = trx.id))
        } else {
            val trx = transactionRepository.save(
                Transaction(
                    reference = commitReference,
                    sender = from,
                    receiver = to,
                    asset = asset.name,
                    status = TransactionStatus.SUCCESSFUL,
                    createdAt = Instant.now(),
                    amount = amount,
                    type = TransactionType.SWAP,
                    description = toDigitalAmount.toString() + " " + if (!isSibos && toDigitalAsset == CurrencyUnit.BTC) {
                        "XSGD"
                    } else {
                        toDigitalAsset
                    } + " to " + amount.toString() + " " + if (!isSibos && asset == CurrencyUnit.BTC) {
                        "XSGD"
                    } else {
                        asset.name
                    }
                )
            )
            transactionService.showPushNotifications(trx)
            return transactionRepository.save(trx.setTxId(id = trx.id))
        }
    }

    @Transactional
    fun createFakeSellTransaction(
        from: Subaccount,
        to: Subaccount,
        asset: CurrencyUnit,
        amount: BigDecimal,
        createTransactionEntry: Boolean = true,
        fiatCurrencyUnit: FiatCurrencyUnit,
        fiatAmount: BigDecimal
    ): Transaction? {
        val logger = LoggerFactory.getLogger(javaClass)
        val commitReference = ReferenceConventions.commit(UUID.randomUUID())
        logger.info("CommitRef::")
        logger.info(commitReference.toString())
        vubaLedgerService.createCommit(
            commit {
                reference(commitReference)
                subaccountEntry(to.reference, amount)
                subaccountEntry(from.reference, amount.negate())
            }
        )
        if (createTransactionEntry) {
            val trx = transactionRepository.save(
                Transaction(
                    reference = commitReference,
                    sender = from,
                    receiver = to,
                    asset = asset.name,
                    status = TransactionStatus.SUCCESSFUL,
                    createdAt = Instant.now(),
                    amount = amount,
                    type = TransactionType.SELL,
                    description = amount.toString() + " " + asset.name + " Sold For " + fiatAmount + " " + fiatCurrencyUnit.name
                )
            )
            transactionService.showPushNotifications(trx)
            return transactionRepository.save(trx.setTxId(id = trx.id))
        } else {
            return null
        }
    }

    fun registerFakeUser(userId: Int) {
        val phoneNumber = "+42194411223$userId"
        val email = "user$userId@example.com"
        val password = "Password123@$userId"

        var cognitoUid: String? = cognitoService.getCognitoUsernameByEmail(email)

        if (cognitoUid == null) {
            cognitoUid = cognitoService.register(email, phoneNumber, password, false)
        }

        if (userAccountService.userAccountRepository.getByEmail(email) == null) {
            userAccountService.createUserAccount(cognitoUid, email, phoneNumber)
        }
    }

    fun deleteFakeUser(userId: Int) {
        val email = "user$userId@example.com"
        if (userAccountService.userAccountRepository.getByEmail(email) != null) {
            userAccountService.userAccountRepository.delete(userAccountService.getUserAccountByEmail(email))
        }
    }

    fun migrateSubaccounts() {
        accountOwnerRepository.findAll().forEach {
            if (accountRepository.getByOwnerAndType(it, AccountType.RESERVATION) == null) {
                ledgerService.createAccount(it, AccountType.RESERVATION)
                accountOwnerRepository.save(it)
            }
        }
    }

    fun migrateAddresses() {
        subaccountRepository.findAll().forEach {
            it.address = subaccountService.createAddress(it)
            subaccountRepository.save(it)
        }
    }

    fun createWadzpayMerchantAccounts() {
        // create merchant if it doesn't already exist
        val merchant = try {
            merchantService.getMerchantByName(wadzpayMerchantName)
        } catch (e: EntityNotFoundException) {
            val merchant = Merchant(wadzpayMerchantName, CountryCode.UK, "", "", "", "", null, null, null)
            merchantRepository.save(merchant)

            val feesCollectionAccount = accountRepository.getByReference(ReferenceConventions.Wadzpay.feeCollection())
            feesCollectionAccount.owner = merchant
            accountRepository.save(feesCollectionAccount)

            merchant
        }

        // get list of users with WADZPAY_ADMIN role from cognito
        // and create user accounts for them if they don't exist yet
        val wadzpayAdminUsers = cognitoService.getCognitoUsersByGroup(Role.WADZPAY_ADMIN)
        wadzpayAdminUsers.forEach {
            val userAccount = try {
                userAccountService.getUserAccountByCognitoUsername(it.username)
            } catch (e: EntityNotFoundException) {
                userAccountRepository.save(UserAccount(it.username, it.email, it.phoneNumber))
            }

            cognitoService.addToGroup(userAccount, Role.MERCHANT_ADMIN)

            // assign them to the merchant
            userAccount.merchant = merchant
            userAccountRepository.save(userAccount)
        }
    }

    @Transactional
    fun createFiatDepositTransaction(
        from: Subaccount,
        to: Subaccount,
        digitalAsset: CurrencyUnit,
        digitalAmount: BigDecimal,
        createTransactionEntry: Boolean = true,
        fiatCurrencyUnit: FiatCurrencyUnit,
        fiatAmount: BigDecimal,
        bankAccountNumber: String?,
        isDeposit: Boolean
    ): Transaction? {
        val logger = LoggerFactory.getLogger(javaClass)
        val commitReference = ReferenceConventions.commit(UUID.randomUUID())
        logger.info("CommitRef::")
        logger.info(commitReference.toString())
        /*vubaLedgerService.createCommit(
            commit {
                reference(commitReference)
                subaccountEntry(to.reference, digitalAmount)
                subaccountEntry(from.reference, digitalAmount.negate())
            }
        )*/
        var fiatAmountSave = fiatAmount
        var fiatAmountDes = ""
        if (fiatAmount.stripTrailingZeros().scale() > 2) {
            fiatAmountDes = fiatAmount.setScale(2, RoundingMode.UP).stripTrailingZeros().toString()
            fiatAmountSave = fiatAmount.setScale(2, RoundingMode.UP).stripTrailingZeros()
        } else {
            fiatAmountDes = fiatAmount.stripTrailingZeros().toPlainString()
        }
        if (createTransactionEntry) {
            val trx = transactionRepository.save(
                Transaction(
                    reference = commitReference,
                    sender = from,
                    receiver = to,
                    asset = digitalAsset.name,
                    status = TransactionStatus.SUCCESSFUL,
                    createdAt = Instant.now(),
                    amount = digitalAmount,
                    type = TransactionType.DEPOSIT,
                    fiatAsset = fiatCurrencyUnit,
                    fiatAmount = fiatAmountSave,
                    description = fiatAmountDes + " " + fiatCurrencyUnit.name + " " + "Deposit Form" + " " + bankAccountNumber
                )
            )
            transactionService.showPushNotifications(trx)
            return transactionRepository.save(trx.setTxId(id = trx.id))
        } else {
            return null
        }
    }

    @Transactional
    fun createFiatWithDrawTransaction(
        from: Subaccount,
        to: Subaccount,
        digitalAsset: CurrencyUnit,
        digitalAmount: BigDecimal,
        createTransactionEntry: Boolean = true,
        fiatCurrencyUnit: FiatCurrencyUnit,
        fiatAmount: BigDecimal,
        bankAccountNumber: String?,
        isDeposit: Boolean
    ): Transaction? {
        val logger = LoggerFactory.getLogger(javaClass)
        val commitReference = ReferenceConventions.commit(UUID.randomUUID())
        logger.info("CommitRef::")
        logger.info(commitReference.toString())
        /* vubaLedgerService.createCommit(
             commit {
                 reference(commitReference)
                 subaccountEntry(to.reference, digitalAmount)
                 subaccountEntry(from.reference, digitalAmount.negate())
             }
         )*/
        var fiatAmountSave = fiatAmount
        var fiatAmountDes = ""
        if (fiatAmount.stripTrailingZeros().scale() > 2) {
            fiatAmountDes = fiatAmount.setScale(2, RoundingMode.UP).stripTrailingZeros().toString()
            fiatAmountSave = fiatAmount.setScale(2, RoundingMode.UP).stripTrailingZeros()
        } else {
            fiatAmountDes = fiatAmount.stripTrailingZeros().toPlainString()
        }
        if (createTransactionEntry) {
            val trx = transactionRepository.save(
                Transaction(
                    reference = commitReference,
                    sender = from,
                    receiver = to,
                    asset = digitalAsset.name,
                    status = TransactionStatus.SUCCESSFUL,
                    createdAt = Instant.now(),
                    amount = digitalAmount,
                    type = TransactionType.WITHDRAW,
                    fiatAsset = fiatCurrencyUnit,
                    fiatAmount = fiatAmountSave,
                    description = fiatAmountDes + " " + fiatCurrencyUnit.name + " " + "Withdrawn To" + " " + bankAccountNumber
                )
            )
            transactionService.showPushNotifications(trx)
            return transactionRepository.save(trx.setTxId(id = trx.id))
        } else {
            return null
        }
    }

    @Transactional
    fun createSellTransaction(
        from: Subaccount,
        to: Subaccount,
        asset: CurrencyUnit,
        amount: BigDecimal,
        createTransactionEntry: Boolean = true,
        fiatCurrencyUnit: FiatCurrencyUnit,
        fiatAmount: BigDecimal
    ): Transaction? {
        val logger = LoggerFactory.getLogger(javaClass)
        val commitReference = ReferenceConventions.commit(UUID.randomUUID())
        logger.info("CommitRef::")
        logger.info(commitReference.toString())
        vubaLedgerService.createCommit(
            commit {
                reference(commitReference)
                subaccountEntry(to.reference, amount)
                subaccountEntry(from.reference, amount.negate())
            }
        )
        var fiatAmountDes = if (fiatAmount.stripTrailingZeros().scale() > 2) {
            fiatAmount.setScale(2, RoundingMode.UP).stripTrailingZeros().toString()
        } else {
            fiatAmount.stripTrailingZeros().toPlainString()
        }
        if (createTransactionEntry) {
            val trx = transactionRepository.save(
                Transaction(
                    reference = commitReference,
                    sender = from,
                    receiver = to,
                    asset = asset.name,
                    status = TransactionStatus.SUCCESSFUL,
                    createdAt = Instant.now(),
                    amount = amount,
                    type = TransactionType.SELL,
                    description = amount.toString() + " " + asset.name + " Sold For " + fiatAmountDes + " " + fiatCurrencyUnit.name
                )
            )
            transactionService.showPushNotifications(trx)
            return transactionRepository.save(trx.setTxId(id = trx.id))
        } else {
            return null
        }
    }

    fun createFiatPendingTransaction(
        from: Subaccount,
        to: Subaccount,
        asset: CurrencyUnit,
        amount: BigDecimal,
        fiatCurrencyUnit: FiatCurrencyUnit,
        fiatAmount: BigDecimal
    ): Transaction? {
        val logger = LoggerFactory.getLogger(javaClass)
        val commitReference = ReferenceConventions.commit(UUID.randomUUID())
        logger.info("CommitRef::")
        logger.info(commitReference.toString())
        var fiatAmountSave = fiatAmount
        var fiatAmountDes = ""
        if (fiatAmount.stripTrailingZeros().scale() > 2) {
            fiatAmountDes = fiatAmount.setScale(2, RoundingMode.UP).stripTrailingZeros().toString()
            fiatAmountSave = fiatAmount.setScale(2, RoundingMode.UP).stripTrailingZeros()
        } else {
            fiatAmountDes = fiatAmount.stripTrailingZeros().toPlainString()
        }
        val trx = transactionRepository.save(
            Transaction(
                reference = commitReference,
                sender = from,
                receiver = to,
                asset = asset.name,
                status = TransactionStatus.IN_PROGRESS,
                createdAt = Instant.now(),
                amount = amount,
                type = TransactionType.DEPOSIT,
                fiatAsset = fiatCurrencyUnit,
                fiatAmount = fiatAmountSave,
                description = fiatAmountDes + " " + fiatCurrencyUnit.name + " " + "Deposit"
            )
        )
        return transactionRepository.save(trx.setTxId(id = trx.id))
    }

    fun createFiatWithDrawPendingTransaction(
        from: Subaccount,
        to: Subaccount,
        digitalAsset: CurrencyUnit,
        digitalAmount: BigDecimal,
        fiatType: FiatCurrencyUnit,
        fiatAmount: BigDecimal?,
        bankAccountNumber: String?
    ): Transaction? {
        val logger = LoggerFactory.getLogger(javaClass)
        val commitReference = ReferenceConventions.commit(UUID.randomUUID())
        logger.info("CommitRef::")
        logger.info(commitReference.toString())
        /* vubaLedgerService.createCommit(
             commit {
                 reference(commitReference)
                 subaccountEntry(to.reference, digitalAmount)
                 subaccountEntry(from.reference, digitalAmount.negate())
             }
         )*/
        var fiatAmountSave = fiatAmount
        var fiatAmountDes = ""
        if (fiatAmount != null) {
            if (fiatAmount.stripTrailingZeros().scale() > 2) {
                fiatAmountDes = fiatAmount.setScale(2, RoundingMode.UP).stripTrailingZeros().toString()
                fiatAmountSave = fiatAmount.setScale(2, RoundingMode.UP).stripTrailingZeros()
            } else {
                fiatAmountDes = fiatAmount.stripTrailingZeros().toPlainString()
            }
        }
        val trx = transactionRepository.save(
            Transaction(
                reference = commitReference,
                sender = from,
                receiver = to,
                asset = digitalAsset.name,
                status = TransactionStatus.IN_PROGRESS,
                createdAt = Instant.now(),
                amount = digitalAmount,
                type = TransactionType.WITHDRAW,
                fiatAsset = fiatType,
                fiatAmount = fiatAmountSave,
                description = fiatAmountDes + " " + fiatType.name + " " + "Withdrawn To" + " " + bankAccountNumber
            )
        )
        transactionService.showPushNotifications(trx)
        return transactionRepository.save(trx.setTxId(id = trx.id))
    }

    fun createTokenDepositTransaction(
        from: Subaccount,
        to: Subaccount,
        digitalAsset: CurrencyUnit,
        digitalAmount: BigDecimal,
        issuanceBanks: IssuanceBanks,
        bankAccountNumber: String?,
        fiatAmount: BigDecimal?,
        fiatAsset: FiatCurrencyUnit?,
        blockchainTransId: String?,
        totalFeeApplied: BigDecimal?,
        enteredAmount: List<IssuanceCommonController.FeeConfigDataDetails>?,
        totalRequestedAmount: BigDecimal?,
        status: TransactionStatus
    ): Transaction? {
        val logger = LoggerFactory.getLogger(javaClass)
        var commitReference: Reference? = null
        if (status == TransactionStatus.SUCCESSFUL) {
            commitReference = ReferenceConventions.commit(UUID.randomUUID())
            logger.info("CommitRef::")
            logger.info(commitReference.toString())
            vubaLedgerService.createCommit(
                commit {
                    reference(commitReference)
                    subaccountEntry(to.reference, digitalAmount)
                    subaccountEntry(from.reference, digitalAmount.negate())
                }
            )
        }
        var feeAmount = BigDecimal.ZERO
        if (totalFeeApplied != null) {
            feeAmount = totalFeeApplied
        }
        val totalRequestedAmountAsset = fiatAsset.toString()
        val trx = transactionRepository.save(
            Transaction(
                reference = commitReference,
                sender = from,
                receiver = to,
                asset = digitalAsset.name,
                status = status,
                createdAt = Instant.now(),
                blockchainTxId = blockchainTransId,
                amount = digitalAmount,
                type = TransactionType.DEPOSIT,
                description = digitalAmount.toString() + " " + digitalAsset.name + " " + if (bankAccountNumber != null) "Deposit From $bankAccountNumber" else "deposited",
                issuanceBanks = issuanceBanks,
                fiatAsset = fiatAsset,
                fiatAmount = fiatAmount,
                totalFeeApplied = feeAmount,
                totalFeeAppliedAsset = if (feeAmount > BigDecimal.ZERO) totalRequestedAmountAsset else null,
                totalRequestedAmount = totalRequestedAmount,
                totalRequestedAmountAsset = totalRequestedAmountAsset
            )
        )
        transactionService.showPushNotifications(trx)
        return transactionRepository.save(trx.setTxId(id = trx.id))
    }

    @Transactional
    fun createRefundTransaction(
        from: Subaccount,
        to: Subaccount,
        digitalAsset: CurrencyUnit,
        digitalAmount: BigDecimal,
        issuanceBanks: IssuanceBanks,
        bankAccountNumber: String?,
        fiatAsset: FiatCurrencyUnit?,
        fiatAmount: BigDecimal?,
        blockchainTransId: String?,
        totalFeeApplied: BigDecimal?,
        feeConfigData: List<IssuanceCommonController.FeeConfigDataDetails>?,
        status: TransactionStatus
    ): Transaction? {
        val logger = LoggerFactory.getLogger(javaClass)
        var commitReference: Reference? = null
        if (status == TransactionStatus.SUCCESSFUL) {
            commitReference = ReferenceConventions.commit(UUID.randomUUID())
            logger.info("CommitRef::")
            logger.info(commitReference.toString())
            vubaLedgerService.createCommit(
                commit {
                    reference(commitReference)
                    subaccountEntry(to.reference, digitalAmount)
                    subaccountEntry(from.reference, digitalAmount.negate())
                }
            )
        }
        var feeAmount = BigDecimal.ZERO
        if (totalFeeApplied != null) {
            feeAmount = totalFeeApplied
        }
        var totalRequestedAmount = digitalAmount
        val totalRequestedAmountAsset = digitalAsset.toString()
        if (!feeConfigData.isNullOrEmpty()) {
            totalRequestedAmount = feeConfigData[0].enteredAmount!!
        }
        val trx = transactionRepository.save(
            Transaction(
                reference = commitReference,
                sender = from,
                receiver = to,
                asset = digitalAsset.name,
                status = status,
                createdAt = Instant.now(),
                blockchainTxId = blockchainTransId,
                amount = digitalAmount,
                type = TransactionType.WITHDRAW,
                description = digitalAmount.toString() + " " + digitalAsset.name + " " + "Withdrawn To" + " " + (
                    bankAccountNumber
                        ?: "Bank Wallet"
                    ),
                issuanceBanks = issuanceBanks,
                fiatAmount = fiatAmount,
                fiatAsset = fiatAsset,
                totalFeeApplied = feeAmount,
                totalFeeAppliedAsset = totalRequestedAmountAsset,
                totalRequestedAmount = totalRequestedAmount,
                totalRequestedAmountAsset = totalRequestedAmountAsset
            )
        )
        transactionService.showPushNotifications(trx)
        return transactionRepository.save(trx.setTxId(id = trx.id))
    }

    fun createFiatTransaction(
        from: Subaccount,
        to: Subaccount,
        digitalAsset: CurrencyUnit,
        digitalAmount: BigDecimal,
        fiatType: FiatCurrencyUnit,
        fiatAmount: BigDecimal?,
        description: String?,
        type: TransactionType
    ): Transaction? {
        val logger = LoggerFactory.getLogger(javaClass)
        val commitReference = ReferenceConventions.commit(UUID.randomUUID())
        logger.info("CommitRef::")
        logger.info(commitReference.toString())
        /* vubaLedgerService.createCommit(
             commit {
                 reference(commitReference)
                 subaccountEntry(to.reference, digitalAmount)
                 subaccountEntry(from.reference, digitalAmount.negate())
             }
         )*/
        var fiatAmountSave = fiatAmount
        if (fiatAmount != null) {
            if (fiatAmount.stripTrailingZeros().scale() > 2) {
                fiatAmountSave = fiatAmount.setScale(2, RoundingMode.UP).stripTrailingZeros()
            }
        }
        val trx = transactionRepository.save(
            Transaction(
                reference = commitReference,
                sender = from,
                receiver = to,
                asset = digitalAsset.name,
                status = TransactionStatus.SUCCESSFUL,
                createdAt = Instant.now(),
                amount = digitalAmount,
                type = type,
                fiatAsset = fiatType,
                fiatAmount = fiatAmountSave,
                description = description
            )
        )
        return transactionRepository.save(trx.setTxId(id = trx.id))
    }

    fun createFaildTransaction(
        from: Subaccount,
        to: Subaccount,
        digitalAsset: CurrencyUnit,
        digitalAmount: BigDecimal,
        issuanceBanks: IssuanceBanks,
        bankAccountNumber: String?,
        fiatAmount: BigDecimal?,
        fiatAsset: FiatCurrencyUnit?,
        transactionType: TransactionType,
        reason: String
    ): Transaction? {
        val trx = transactionRepository.save(
            Transaction(
                sender = from,
                receiver = to,
                asset = digitalAsset.name,
                status = TransactionStatus.FAILED,
                createdAt = Instant.now(),
                amount = digitalAmount,
                type = transactionType,
                description = reason,
                issuanceBanks = issuanceBanks,
                fiatAsset = fiatAsset,
                fiatAmount = fiatAmount
            )
        )
        return transactionRepository.save(trx.setTxId(id = trx.id))
    }

    fun saveFeeDetails(
        transaction: Transaction?,
        request: List<IssuanceCommonController.FeeConfigDataDetails>,
        issuanceBank: IssuanceBanks,
        userAccount: UserAccount
    ) {
        request.forEach { feeData ->
            transaction?.let {
                val walletFeeId = feeData.feeId?.let { it1 -> getWalletFeeDetails(issuanceBank, it1) }
                if (walletFeeId != null) {
                    val feeDataSave = TransactionWalletFeeDetails(
                        transactionUuid = it.uuid.toString(),
                        walletFeeId = walletFeeId.id,
                        userAccount = userAccount,
                        baseAmount = feeData.enteredAmount,
                        feeAmount = feeData.feeCalculatedAmount,
                        feeName = feeData.feeName,
                        feeType = feeData.feeType,
                        feeValue = feeData.feeAmount,
                        createdAt = Instant.now(),
                        feeFrequency = walletFeeId.frequency,
                        feeAsset = feeData.currencyType,
                        feeDescription = feeData.description
                    )
                    transactionWalletFeeDetailsRepository.save(feeDataSave)
                }
            }
        }
    }

    @Transactional
    fun createServiceFeeTransaction(
        from: Subaccount,
        to: Subaccount,
        digitalAsset: CurrencyUnit,
        digitalAmount: BigDecimal,
        issuanceBanks: IssuanceBanks,
        blockchainTransId: String?,
        totalFeeApplied: BigDecimal?,
        feeConfigData: List<IssuanceCommonController.FeeConfigDataDetails>?,
        description: String?,
        type: TransactionType
    ): Transaction? {
        val logger = LoggerFactory.getLogger(javaClass)
        val commitReference = ReferenceConventions.commit(UUID.randomUUID())
        logger.info("CommitRef::")
        logger.info(commitReference.toString())
        vubaLedgerService.createCommit(
            commit {
                reference(commitReference)
                subaccountEntry(to.reference, digitalAmount)
                subaccountEntry(from.reference, digitalAmount.negate())
            }
        )
        var feeAmount = BigDecimal.ZERO
        if (totalFeeApplied != null) {
            feeAmount = totalFeeApplied
        }
        var totalRequestedAmount = digitalAmount
        var totalRequestedAmountAsset = digitalAsset.toString()
        if (feeConfigData != null && feeConfigData.isNotEmpty()) {
            totalRequestedAmount = feeConfigData[0].enteredAmount!!
        }
        val trx = transactionRepository.save(
            Transaction(
                reference = commitReference,
                sender = from,
                receiver = to,
                asset = digitalAsset.name,
                status = TransactionStatus.SUCCESSFUL,
                createdAt = Instant.now(),
                blockchainTxId = blockchainTransId,
                amount = digitalAmount,
                type = type,
                description = description,
                issuanceBanks = issuanceBanks,
                totalFeeApplied = feeAmount,
                totalFeeAppliedAsset = totalRequestedAmountAsset,
                totalRequestedAmount = totalRequestedAmount,
                totalRequestedAmountAsset = totalRequestedAmountAsset
            )
        )
        transactionService.showPushNotifications(trx)
        return transactionRepository.save(trx.setTxId(id = trx.id))
    }

    fun getWalletFeeDetails(issuanceBanks: IssuanceBanks, feeIdRequest: Long): IssuanceWalletConfig {
        issuanceWalletConfigRepository.getByIssuanceBanksId(issuanceBanksId = issuanceBanks)?.forEach { feeData ->
            if (feeData.id == feeIdRequest) {
                return feeData
            }
        }
        throw EntityNotFoundException(ErrorCodes.WALLET_FEE_CONFIG_NOT_FOUND)
    }

    fun saveMarkUpDownDetails(transactionType: IssuanceWalletUserController.TransactionType, transaction: Transaction, userAccount: UserAccount) {
        val markUpData = transaction.fiatAsset?.let {
            issuanceService.getMarkUpDownDetails(
                it,
                transactionType,
                userAccount = userAccount
            )
        }!!
        println("markUpData ==> " + markUpData)
        val feeName: String = if (transactionType == IssuanceWalletUserController.TransactionType.LOAD) {
            "MarkUp Fee"
        } else {
            "MarkDown Fee"
        }
        if (markUpData > BigDecimal.ZERO) {
            val totalCreditedAmount = transaction.amount
            val totalFiatAmount = transaction.fiatAmount
            val baseRate = transaction.fiatAsset?.let {
                issuanceService.getBaseRate(
                    it,
                    transactionType,
                    userAccount = userAccount
                )
            }!!
            println("baseRate ==> $baseRate")
            println("markUpData ==>" + markUpData)
            val actualPaymentDigitalCurrency = if (transactionType == IssuanceWalletUserController.TransactionType.LOAD) {
                totalFiatAmount?.times(baseRate)
            } else {
                totalFiatAmount?.div(baseRate)
            }
            println("actualPaymentDigitalCurrency ==> $actualPaymentDigitalCurrency")
            println("totalCreditedAmount ==> " + totalCreditedAmount)
            val diffrence = actualPaymentDigitalCurrency?.minus(totalCreditedAmount)
            println("difference ==> $diffrence")
            val feeDataSave = TransactionWalletFeeDetails(
                transactionUuid = transaction.uuid.toString(),
                walletFeeId = 0,
                userAccount = userAccount,
                baseAmount = transaction.amount,
                feeAmount = diffrence,
                feeName = feeName,
                feeType = "Percentage",
                feeValue = markUpData,
                createdAt = Instant.now(),
                feeFrequency = null,
                feeAsset = transaction.asset.toString(),
                feeDescription = "$markUpData Percentage $feeName"
            )
            transactionWalletFeeDetailsRepository.save(feeDataSave)
        } else {
            if (transactionType == IssuanceWalletUserController.TransactionType.REFUND) {
            }
        }
    }

    fun createTokenDepositTransactionByMinter(
        from: Subaccount,
        to: Subaccount,
        digitalAsset: String,
        digitalAmount: BigDecimal,
        issuanceBanks: IssuanceBanks?,
        fiatAmount: BigDecimal?,
        fiatAsset: FiatCurrencyUnit?,
        blockchainTransId: String?,
        status: TransactionStatus,
        type: TransactionType,
        description: String
    ): Transaction? {
        val logger = LoggerFactory.getLogger(javaClass)
        var commitReference: Reference? = null
        if (status == TransactionStatus.SUCCESSFUL) {
            commitReference = ReferenceConventions.commit(UUID.randomUUID())
            logger.info("CommitRef::")
            logger.info(commitReference.toString())
            vubaLedgerService.createCommit(
                commit {
                    reference(commitReference)
                    subaccountEntry(to.reference, digitalAmount)
                    subaccountEntry(from.reference, digitalAmount.negate())
                }
            )
        }
        val trx = transactionRepository.save(
            Transaction(
                reference = commitReference,
                sender = from,
                receiver = to,
                asset = digitalAsset,
                status = status,
                createdAt = Instant.now(),
                blockchainTxId = blockchainTransId,
                amount = digitalAmount,
                type = type,
                description = description,
                issuanceBanks = issuanceBanks,
                fiatAsset = fiatAsset,
                fiatAmount = fiatAmount
            )
        )
        transactionService.showPushNotifications(trx)
        return transactionRepository.save(trx.setTxId(id = trx.id))
    }

    @Transactional
    fun createRefundFeeTransaction(
        from: Subaccount,
        to: Subaccount,
        digitalAsset: CurrencyUnit,
        digitalAmount: BigDecimal,
        totalFeeApplied: BigDecimal?,
    ): Boolean {
        val logger = LoggerFactory.getLogger(javaClass)
        if (totalFeeApplied != null) {
            val commitReference = ReferenceConventions.commit(UUID.randomUUID())
            logger.info("CommitRef::")
            logger.info(commitReference.toString())
            vubaLedgerService.createCommit(
                commit {
                    reference(commitReference)
                    subaccountEntry(to.reference, totalFeeApplied)
                    subaccountEntry(from.reference, totalFeeApplied.negate())
                }
            )
        }
        return true
    }

    fun transactionCommitReference(
        from: Subaccount,
        to: Subaccount,
        digitalAsset: CurrencyUnit,
        digitalAmount: BigDecimal
    ): Reference {
        val logger = LoggerFactory.getLogger(javaClass)
        val commitReference = ReferenceConventions.commit(UUID.randomUUID())
        logger.info("CommitRef::")
        logger.info(commitReference.toString())
        vubaLedgerService.createCommit(
            commit {
                reference(commitReference)
                subaccountEntry(to.reference, digitalAmount)
                subaccountEntry(from.reference, digitalAmount.negate())
            }
        )
        return commitReference
    }
}
