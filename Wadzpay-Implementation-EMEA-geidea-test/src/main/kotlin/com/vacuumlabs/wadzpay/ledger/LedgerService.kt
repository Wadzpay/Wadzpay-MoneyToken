package com.vacuumlabs.wadzpay.ledger

import com.vacuumlabs.SOURCE_FORMAT_CA
import com.vacuumlabs.vuba.ledger.api.dsl.commit
import com.vacuumlabs.wadzpay.accountowner.AlgoBalancesRequest
import com.vacuumlabs.wadzpay.algocutomtoken.AlgoCustomTokenWallet
import com.vacuumlabs.wadzpay.asset.AssetService
import com.vacuumlabs.wadzpay.asset.models.AssetCreationRequest
import com.vacuumlabs.wadzpay.bitgo.BitGoTransfer
import com.vacuumlabs.wadzpay.bitgo.BitGoTransferEntry
import com.vacuumlabs.wadzpay.bitgo.normalize
import com.vacuumlabs.wadzpay.bitgo.toBitGoCoin
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ServiceUnavailableException
import com.vacuumlabs.wadzpay.common.UnprocessableEntityException
import com.vacuumlabs.wadzpay.comply.ComplyAdvantageService
import com.vacuumlabs.wadzpay.comply.models.ComplyAdvantageRequest
import com.vacuumlabs.wadzpay.comply.models.ComplyAdvantageRequestData
import com.vacuumlabs.wadzpay.comply.models.ComplyAdvantageResponse
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import com.vacuumlabs.wadzpay.ledger.model.Account
import com.vacuumlabs.wadzpay.ledger.model.AccountOwner
import com.vacuumlabs.wadzpay.ledger.model.AccountRepository
import com.vacuumlabs.wadzpay.ledger.model.AccountType
import com.vacuumlabs.wadzpay.ledger.model.CryptoAddress
import com.vacuumlabs.wadzpay.ledger.model.CryptoAddressRepository
import com.vacuumlabs.wadzpay.ledger.model.Subaccount
import com.vacuumlabs.wadzpay.ledger.model.SubaccountRepository
import com.vacuumlabs.wadzpay.ledger.model.Transaction
import com.vacuumlabs.wadzpay.ledger.model.TransactionMode
import com.vacuumlabs.wadzpay.ledger.model.TransactionRepository
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.ledger.service.TransactionService
import com.vacuumlabs.wadzpay.merchant.OrderService
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.OrderRepository
import com.vacuumlabs.wadzpay.pos.PosService
import com.vacuumlabs.wadzpay.pos.PosTransactionRepository
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.utils.ApisLog
import com.vacuumlabs.wadzpay.utils.ApisLoggerRepository
import com.vacuumlabs.wadzpay.utils.BlockConfirmationLogger
import com.vacuumlabs.wadzpay.utils.BlockConfirmationRepository
import com.vacuumlabs.wadzpay.webhook.toCurrencyUnit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.ResourceAccessException
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.UUID
import com.vacuumlabs.vuba.ledger.service.LedgerService as VubaLedgerService

@Service
class LedgerService(
    val ledgerService: VubaLedgerService,
    val accountRepository: AccountRepository,
    val subaccountRepository: SubaccountRepository,
    val transactionRepository: TransactionRepository,
    val appConfig: AppConfig,
    val cryptoAddressRepository: CryptoAddressRepository,
    val subaccountService: SubaccountService,
    @org.springframework.context.annotation.Lazy
    @Autowired
    val transactionService: TransactionService,
    val posTransactionRepository: PosTransactionRepository,
    val orderRepository: OrderRepository,
    val blockConfirmRepository: BlockConfirmationRepository,
    val algoCustomTokenWallet: AlgoCustomTokenWallet,
    val apisLoggerRepository: ApisLoggerRepository,
    @org.springframework.context.annotation.Lazy
    @Autowired
    val posService: PosService,
    @org.springframework.context.annotation.Lazy
    @Autowired
    val orderService: OrderService,
    val complyAdvantageService: ComplyAdvantageService,
    @org.springframework.context.annotation.Lazy
    @Autowired
    val assetService: AssetService
) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun createAccount(accountOwner: AccountOwner, accountType: AccountType): Account {
        val assets = CurrencyUnit.values().map(CurrencyUnit::toString)
        val accountRef = ReferenceConventions.Account.account(accountOwner.id, accountType)
        val commitRef = ReferenceConventions.Account.createCommit(accountOwner.id, accountType)

        ledgerService.createAccount(accountRef)

        ledgerService.createCommit(
            commit {
                reference(commitRef)
                for (asset in assets) {
                    val subaccountRef = ReferenceConventions.Account.subaccount(accountOwner.id, asset, accountType)
                    subaccountDeclaration(subaccountRef, asset, zero = true)
                }
            }
        )

        val account = accountRepository.save(Account(owner = accountOwner, reference = accountRef, type = accountType))
        for (asset in assets) {
            val subaccountRef = ReferenceConventions.Account.subaccount(accountOwner.id, asset, accountType)
            val subAccount = subaccountService.createSubaccount(account, subaccountRef, asset, accountOwner as UserAccount)
            account.subaccounts.add(subAccount)
        }
        return account
    }

    fun createAccountTwo(accountOwner: AccountOwner, accountType: AccountType): Account {
        val assets = CurrencyUnit.values().map(CurrencyUnit::toString)
        val accountRef = ReferenceConventions.Account.account(accountOwner.id, accountType)
        val commitRef = ReferenceConventions.Account.createCommit(accountOwner.id, accountType)

        ledgerService.createAccount(accountRef)

        ledgerService.createCommit(
            commit {
                reference(commitRef)
                for (asset in assets) {
                    val subaccountRef = ReferenceConventions.Account.subaccount(accountOwner.id, asset, accountType)
                    subaccountDeclaration(subaccountRef, asset, zero = true)
                }
            }
        )

        val account = accountRepository.save(Account(owner = accountOwner, reference = accountRef, type = accountType))
        for (asset in assets) {
            val subaccountRef = ReferenceConventions.Account.subaccount(accountOwner.id, asset, accountType)
            val subAccount = subaccountService.createSubaccount(account, subaccountRef, CurrencyUnit.valueOf(asset))
            account.subaccounts.add(subAccount)
        }
        return account
    }

    fun createAccounts(accountOwner: AccountOwner): MutableList<Account> {
        accountOwner.accounts = mutableListOf(
            createAccount(accountOwner, AccountType.MAIN),
            createAccount(accountOwner, AccountType.RESERVATION)
        )
        return accountOwner.accounts
    }

    fun createAccountsTwo(accountOwner: AccountOwner): MutableList<Account> {
        accountOwner.accounts = mutableListOf(
            createAccountTwo(accountOwner, AccountType.MAIN),
            createAccountTwo(accountOwner, AccountType.RESERVATION)
        )
        return accountOwner.accounts
    }

    fun getBalances(accountOwner: AccountOwner): Map<String, BigDecimal> {
        val assets = CurrencyUnit.values().map(CurrencyUnit::toString)
        // print(assets[3])
        logger.info("current assets :$assets")
        var isNewWTKUser = false
        var isNewUSDCUser = false
        var isNewAlgoUser = false
        var isNewAlgoUSDCUser = false
        var isNewSARTUser = false
        // FOR SART
        for (subAcc in accountOwner.getAccount(AccountType.MAIN).subaccounts) {
            // println(subAcc.reference)
            if (subAcc.reference.toString().endsWith("SART")) {
                isNewSARTUser = true
                logger.info("User found for SART")
                break
            }
        }

        // FOR WTK
        for (subAcc in accountOwner.getAccount(AccountType.MAIN).subaccounts) {
            if (subAcc.reference.toString().endsWith("WTK")) {
                isNewWTKUser = true
                break
            }
        }

        // FOR USDC
        for (subAcc in accountOwner.getAccount(AccountType.MAIN).subaccounts) {

            if (subAcc.reference.toString().endsWith("USDC")) {
                isNewUSDCUser = true
                break
            }
        }

        for (subAcc in accountOwner.getAccount(AccountType.MAIN).subaccounts) {
            if (subAcc.reference.toString().endsWith("ALGO")) {
                isNewAlgoUser = true
                break
            }
        }

        for (subAcc in accountOwner.getAccount(AccountType.MAIN).subaccounts) {
            logger.info("sub account : ${subAcc.reference}")
            if (subAcc.reference.toString().endsWith("USDCA")) {
                isNewAlgoUSDCUser = true
                break
            }
        }

        if (!isNewWTKUser) {
            println(isNewWTKUser)
            addNewCryptoToUser(accountOwner, "WTK")
        }

        if (!isNewUSDCUser) {
            println(isNewUSDCUser)
            addNewCryptoToUser(accountOwner, "USDC")
        }
        // algorand Integration
        if (!isNewAlgoUser) {
            println("new algo User :" + isNewAlgoUser)
            addNewCryptoToUser(accountOwner, "ALGO")
        }

        if (!isNewAlgoUSDCUser) {
            println("new USDCA User :" + isNewAlgoUSDCUser)
            addNewCryptoToUser(accountOwner, "USDCA")
        }
        if (!isNewSARTUser) {
            println(isNewSARTUser)
            logger.info("creating SART User")
            addNewCryptoToUser(accountOwner, "SART")
        }
        return accountOwner.getAccount(AccountType.MAIN).subaccounts.associate {
            val vubaSubaccount = ledgerService.getSubaccount(it.reference)
            if (vubaSubaccount.isEmpty) throw IllegalArgumentException()
            vubaSubaccount.get().asset.identifier to vubaSubaccount.get().balance
        }
    }

    fun getAlgoBalances(accountOwner: AccountOwner, userAccount: UserAccount, request: AlgoBalancesRequest): Map<String, BigDecimal> {
        val assets = assetService.getAssetForInstitutionOfUser(userAccount, request.tokenName)
        val map = hashMapOf<String, BigDecimal>()
        if (assets != null) {
            for (asset in assets) {
                val subAccount = accountOwner.account.getSubAccountByAssetAlgoString(asset.assetName)
                val vubaSubaccount = subAccount?.let { ledgerService.getSubaccount(it.reference) }
                if (vubaSubaccount != null) {
                    if (vubaSubaccount.isPresent) {
                        map[asset.assetName] = vubaSubaccount.get().balance
                    }
                } else {
                    map[asset.assetName] = BigDecimal.ZERO
                }
            }
        }
        return map
    }

    private fun addNewCryptoToUser(accountOwner: AccountOwner, asset: String) {
        logger.info("adding new crypto user for $asset")
        val accountMain = accountOwner.getAccount(AccountType.MAIN)
        val accountREF = accountOwner.getAccount(AccountType.RESERVATION)
        val newList = accountOwner.accounts
        createSubCryptoAcc(accountOwner, accountMain, asset, AccountType.MAIN)?.let { newList.add(it) }
        createSubCryptoAcc(accountOwner, accountREF, asset, AccountType.RESERVATION)?.let { newList.add(it) }
        accountOwner.accounts = newList
    }

    private fun createSubCryptoAcc(
        accountOwner: AccountOwner,
        account: Account,
        asset: String,
        accType: AccountType
    ): Account? {
        try {
            val commitRef = ReferenceConventions.Account.createCommit(accountOwner.id, accType)

            val subaccountRef = ReferenceConventions.Account.subaccount(accountOwner.id, asset, accType)

            ledgerService.createCommit(
                commit {
                    reference(commitRef)
                    subaccountDeclaration(subaccountRef, asset, zero = true)
                },
                true
            )

            val subAccount =
                subaccountService.createSubaccount(account, subaccountRef, asset, accountOwner as UserAccount)
            account.subaccounts.add(subAccount)
        } catch (e: Exception) {
            if (e.message?.contains("already", true) == false) {
                createSubCryptoAcc(accountOwner, account, asset, accType)
            }
        }
        return account
    }

    fun getBalance(subaccount: Subaccount): BigDecimal = ledgerService.getSubaccount(subaccount.reference).get().balance

    //
    fun createTransaction(
        sender: Account,
        receiver: Account,
        amount: BigDecimal,
        asset: CurrencyUnit,
        fiatAmount: BigDecimal? = null,
        fiatAsset: FiatCurrencyUnit? = null,
        transactionType: TransactionType,
        description: String? = null,
        wadzpayFeeRate: BigDecimal = BigDecimal.ZERO,
        blockchainFee: BigDecimal = BigDecimal.ZERO,
        blockchainTxId: String? = null,
        skipBalanceCheck: Boolean = false,
        sourceWalletAddress: String = "",
        issuanceBanks: IssuanceBanks? = null,
        refundedTransactionId: String? = null
    ): Transaction {
        if (amount <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (amount.stripTrailingZeros().scale() > asset.maximumNumberOfDigits.toInt()) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_TOO_MANY_DECIMAL_PLACES)
        }

        val senderSubaccount = sender.getSubaccountByAsset(asset)
        val receiverSubaccount = receiver.getSubaccountByAsset(asset)
        val balance = getBalance(senderSubaccount)

        var wadzpayFee =
            amount.multiply(wadzpayFeeRate).setScale(asset.maximumNumberOfDigits.toInt(), RoundingMode.UP)
                .stripTrailingZeros()

        if (receiver.getOwnerName()?.contains("@")!!) {
            wadzpayFee = BigDecimal.ZERO
        }

        if (!skipBalanceCheck && balance < amount + wadzpayFee + blockchainFee) {
            throw UnprocessableEntityException(ErrorCodes.INSUFFICIENT_FUNDS)
        }

        val commitReference = ReferenceConventions.commit(UUID.randomUUID())
        ledgerService.createCommit(
            commit {
                reference(commitReference)
                if (!skipBalanceCheck) {
                    balanceGreaterEq(senderSubaccount.reference.toString(), BigDecimal.ZERO)
                }
                subaccountEntry(senderSubaccount.reference, (amount + wadzpayFee + blockchainFee).negate())
                if (wadzpayFee != BigDecimal.ZERO) {
                    subaccountEntry(ReferenceConventions.Wadzpay.feeCollection(asset.name), wadzpayFee)
                }
                /*In case of external send receiverSubaccount is omnibus.
                    so blockchain fee goes out of our system and this is why blockchainFee
                    should be added to omnibus balance
                 */
                subaccountEntry(receiverSubaccount.reference, amount + blockchainFee)
            }
        )

        val trx = transactionRepository.save(
            Transaction(
                reference = commitReference,
                sender = senderSubaccount,
                receiver = receiverSubaccount,
                asset = asset.name,
                amount = amount,
                fiatAsset = fiatAsset,
                fiatAmount = fiatAmount,
                status = TransactionStatus.SUCCESSFUL,
                type = transactionType,
                description = description,
                fee = wadzpayFee + blockchainFee,
                blockchainTxId = blockchainTxId,
                sourceWalletAddress = sourceWalletAddress,
                issuanceBanks = issuanceBanks,
                refundTransactionId = refundedTransactionId
            )
        )
        getComplyAdvantageResponse(trx)
        return transactionRepository.save(trx.setTxId(id = trx.id))
    }

    fun getComplyAdvantageResponse(trx: Transaction): ComplyAdvantageResponse? {

        var counterparty_bank_country: String? = null
        var customer_risk_category: String? = null

        if (trx.type == TransactionType.PEER_TO_PEER) {
            counterparty_bank_country = "wazpay"
            customer_risk_category = "Medium"
        } else {
            counterparty_bank_country = "bitgo"
            customer_risk_category = "High"
        }

        if (trx.fiatAmount == null)
            trx.fiatAmount = BigDecimal.ZERO

        val formatter = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        val createdAt = formatter.format(Date.from(trx.createdAt))

        val complyAdvantageRequest = ComplyAdvantageRequest(
            trx.uuid.toString(),
            SOURCE_FORMAT_CA,
            ComplyAdvantageRequestData(
                trx.uuid.toString(),
                trx.sender.account.getOwnerName()!!,
                trx.sender.account.getOwnerName()!!,
                customer_risk_category,
                trx.sender.id.toString(),
                trx.sender.account.getOwnerName()!!,
                trx.sender.address.toString(),
                counterparty_bank_country,
                trx.status.name,
                trx.fiatAsset.toString(),
                trx.amount,
                trx.asset,
                createdAt,
                customer_type = "",
                customer_account_balance = BigDecimal.ZERO,
                customer_account_currency = "",
                customer_base_account_balance = "",
                customer_account_number = "",
                counterparty_institution_id = "",
                tx_payment_channel = "",
                tx_mcc_code = "",
                tx_loan_funding_datetime = "",
                tx_loan_settlement_datetime = "",
                tx_loan_expected_repayment_date = "",
                tx_loan_monthly_expected_installment_amount = BigDecimal.ZERO,
                customer_account_type = "",
                customer_sort_code = "",
                customer_date_of_birth = "",
                customer_country = "",
                customer_state = "",
                customer_city = "",
                customer_address = "",
                customer_postcode = "",
                customer_income = "",
                customer_expected_amount = "",
                customer_bank_branch_id = "",
                customer_bank = "",
                customer_loan_id = "",
                customer_loan_amount = BigDecimal.ZERO,
                customer_loan_base_balance = BigDecimal.ZERO,
                customer_credit_limit = BigDecimal.ZERO,
                customer_expected_installment_amount = "",
                counterparty_type = "",
                counterparty_account_number = "",
                counterparty_sort_code = "",
                counterparty_date_of_birth = "",
                counterparty_state = "",
                counterparty_address = "",
                counterparty_city = "",
                counterparty_postcode = "",
                counterparty_reference = "",
                ll_counterparty_country = "",
                ll_counterparty_bank_country = "",
                ll_tx_currency = "",
                tx_type = "",
                tx_calendar_year = "",
                tx_calendar_month = "",
                tx_amount = BigDecimal.ZERO,
                tx_reference_text = "",
                tx_product = "",
                counterparty_institution_name = ""

            )
        )
        return complyAdvantageService.complyAdvanceTransaction(complyAdvantageRequest)
    }

    fun createTransaction(
        receiver: Account,
        sender: Account,
        skipBalanceCheck: Boolean = true,
        transaction: Transaction,
        asset: CurrencyUnit,
        amount: BigDecimal
    ): Transaction {

        if (transaction.amount <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (transaction.amount.stripTrailingZeros().scale() > asset.maximumNumberOfDigits.toInt()) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_TOO_MANY_DECIMAL_PLACES)
        }
        val receiverSubaccount = receiver.getSubaccountByAsset(asset)

        val commitReference = ReferenceConventions.commit(UUID.randomUUID())

        ledgerService.createCommit(
            commit {
                reference(commitReference)
                subaccountEntry(sender.getSubaccountByAsset(asset).reference, amount.negate())
                subaccountEntry(receiverSubaccount.reference, amount)
            },
            true
        )

        transaction.reference = commitReference
        return transactionRepository.save(transaction)
    }

    private fun createTransaction(
        sender: AccountOwner,
        receiver: AccountOwner,
        amount: BigDecimal,
        asset: CurrencyUnit,
        fiatAmount: BigDecimal? = null,
        fiatAsset: FiatCurrencyUnit? = null,
        transactionType: TransactionType,
        description: String? = null,
        feeRate: BigDecimal = BigDecimal.ZERO,
        issuanceBanks: IssuanceBanks? = null
    ): Transaction = createTransaction(
        sender.account, receiver.account,
        amount, asset, fiatAmount, fiatAsset, transactionType, description, feeRate, issuanceBanks = issuanceBanks
    )

    fun createExternalTransaction(
        sender: AccountOwner,
        amount: BigDecimal,
        blockchainFee: BigDecimal,
        asset: CurrencyUnit,
        blockchainTxId: String,
        description: String? = null,
        status: TransactionStatus,
        transactionType: TransactionType,
        issuanceBanks: IssuanceBanks? = null
    ): Transaction {
        if (status == TransactionStatus.SUCCESSFUL) {
            return createTransaction(
                sender.account, getOmnibusAccount(), amount, asset,
                transactionType = transactionType, description = description,
                blockchainTxId = blockchainTxId, blockchainFee = blockchainFee, issuanceBanks = issuanceBanks
            )
        } else {
            val trx = transactionRepository.save(
                Transaction(
                    sender = sender.account.getSubaccountByAsset(asset),
                    receiver = getOmnibusAccount().getSubaccountByAsset(asset),
                    amount = amount,
                    asset = asset.name,
                    status = status,
                    blockchainTxId = blockchainTxId,
                    type = transactionType,
                    description = description,
                    fee = blockchainFee
                )
            )
            try {
                getComplyAdvantageResponse(trx)
            } catch (e: Exception) {
                apisLoggerRepository.save(
                    ApisLog(
                        "ComplyError", e.message.toString(), trx.toString(), "Comply Log Failed",
                        Instant.now()
                    )
                )
            }
            return transactionRepository.save(trx.setTxId(id = trx.id))
        }
    }

    fun createMerchantTransaction(
        sender: AccountOwner,
        receiver: AccountOwner,
        amount: BigDecimal,
        asset: CurrencyUnit,
        fiatAmount: BigDecimal? = null,
        fiatAsset: FiatCurrencyUnit? = null,
        description: String? = null
    ): Transaction =
        createTransaction(sender, receiver, amount, asset, fiatAmount, fiatAsset, TransactionType.MERCHANT, description)

    fun createPeerToPeerTransaction(
        sender: AccountOwner,
        receiver: UserAccount,
        amount: BigDecimal,
        asset: CurrencyUnit,
        fiatAmount: BigDecimal? = null,
        fiatAsset: FiatCurrencyUnit? = null,
        description: String? = null,
        issuanceBanks: IssuanceBanks? = null
    ): Transaction =
        createTransaction(
            sender, receiver, amount, asset, fiatAmount, fiatAsset, TransactionType.PEER_TO_PEER, description,
            appConfig.feeRate, issuanceBanks = issuanceBanks
        )

    fun getOmnibusAccount() = accountRepository.getByReference(ReferenceConventions.Wadzpay.omnibus())

    fun transferMoney(
        from: Subaccount,
        to: Subaccount,
        asset: CurrencyUnit,
        amount: BigDecimal
    ) {
        val balance = getBalance(from)
        if (balance < amount) {
            throw UnprocessableEntityException(ErrorCodes.INSUFFICIENT_FUNDS)
        }
        val commitReference = ReferenceConventions.commit(UUID.randomUUID())
        ledgerService.createCommit(
            commit {
                reference(commitReference)
                balanceGreaterEq(from.reference.toString(), BigDecimal.ZERO)
                subaccountEntry(to.reference, amount)
                subaccountEntry(from.reference, amount.negate())
            }
        )
    }

    fun reserveMoney(
        account: AccountOwner,
        asset: CurrencyUnit,
        amount: BigDecimal
    ) = transferMoney(
        account.account.getSubaccountByAsset(asset),
        account.getAccount(AccountType.RESERVATION).getSubaccountByAsset(asset),
        asset,
        amount
    )

    fun returnMoney(
        account: AccountOwner,
        asset: CurrencyUnit,
        amount: BigDecimal
    ) = transferMoney(
        account.getAccount(AccountType.RESERVATION).getSubaccountByAsset(asset),
        account.account.getSubaccountByAsset(asset),
        asset,
        amount
    )

    fun receiveExternalMoney(bitGoTransfer: BitGoTransfer) {
        val asset = bitGoTransfer.coin.toCurrencyUnit()
        bitGoTransfer.entries.find {
            it.valueString > BigDecimal.ZERO // && cryptoAddressRepository.getByAddressAndAsset(it.address, asset) != null
        }?.let { receiveExternalMoney(asset, it, bitGoTransfer) }
    }

    fun receiveExternalMoney(
        asset: CurrencyUnit,
        bitGoTransferEntry: BitGoTransferEntry,
        bitGoTransfer: BitGoTransfer
    ) {

        /*apisLoggerRepository.save(
            ApisLog(
                "Entries",
                bitGoTransferEntry.toString(),
                bitGoTransferEntry.address,
                bitGoTransfer.toString()
            )
        )*/
        val cryptoAddressForUserReceive =
            cryptoAddressRepository.getByAddressAndAsset(bitGoTransferEntry.address, asset.toString())
        val cryptoAddressForPOS =
            posTransactionRepository.getByAddressAndTypeAndAssetcrypto(
                bitGoTransferEntry.address,
                TransactionType.POS,
                asset
            )
        val cryptoAddressForOrder =
            posTransactionRepository.getByAddressAndTypeAndAssetcrypto(
                bitGoTransferEntry.address,
                TransactionType.ORDER,
                asset
            )
        logger.info(cryptoAddressForUserReceive.toString())

        // Transfers
        if (cryptoAddressForUserReceive != null) {
            val byTransferIdAndType =
                blockConfirmRepository.getByTransferIdAndType(bitGoTransfer.id, TransactionType.EXTERNAL_RECEIVE)
            if (byTransferIdAndType != null) {
                byTransferIdAndType.blockConfirmationCount++
                byTransferIdAndType.updatedAt = Instant.now()
                blockConfirmRepository.save(byTransferIdAndType)
                logger.info("For EXTERNAL_RECEIVE transaction, the transaction is already committed, hence not committing again")
                return
            } else {
                blockConfirmRepository.save(
                    BlockConfirmationLogger(
                        bitGoTransfer.txid,
                        bitGoTransfer.id,
                        bitGoTransferEntry.wallet,
                        TransactionType.EXTERNAL_RECEIVE,
                        1
                    )
                )
            }

            val transaction = createTransaction(
                getOmnibusAccount(),
                cryptoAddressForUserReceive.owner.account,
                bitGoTransferEntry.valueString.normalize(asset.toBitGoCoin(appConfig.production)),
                asset,
                transactionType = TransactionType.EXTERNAL_RECEIVE,
                skipBalanceCheck = true,
                blockchainTxId = bitGoTransfer.txid,
                sourceWalletAddress = bitGoTransfer.entries.find { it.valueString < BigDecimal.ZERO }?.address ?: "",
                issuanceBanks = null
            )
            transactionService.showPushNotifications(transaction)
        } else if (cryptoAddressForPOS != null) {
            posService.posTransaction(bitGoTransfer, bitGoTransferEntry, asset)
            //  posTransaction.transaction?.let { transactionService.showPushNotificationsPOS(it) }
        } else if (cryptoAddressForOrder != null) {

            orderService.processThirdPartyOrder(bitGoTransfer, bitGoTransferEntry, asset)
        } else {
            apisLoggerRepository.save(
                ApisLog(
                    "receiveExternalMoney",
                    bitGoTransfer.toString(),
                    bitGoTransferEntry.address,
                    bitGoTransferEntry.toString()
                )
            )
        }
    }

    @Transactional
    fun confirmPendingExternalTransaction(transaction: Transaction) {

        transactionRepository.delete(transaction)
        transactionRepository.flush()
        createExternalTransaction(
            transaction.sender.account.owner!!,
            transaction.amount,
            transaction.fee,
            CurrencyUnit.valueOf(transaction.asset),
            transaction.blockchainTxId!!,
            status = TransactionStatus.SUCCESSFUL, transactionType = TransactionType.EXTERNAL_SEND
        )
    }
//    ge-idea demo test
    fun createTransactionSart(
        sender: Account,
        receiver: Account,
        receiverAddress: String,
        skipBalanceCheck: Boolean = true,
        transaction: Transaction,
        asset: CurrencyUnit,
        amount: BigDecimal,
        issuanceBank: IssuanceBanks?,
        passCodeHash: String?,
        transactionStatus: TransactionStatus
    ): Transaction {
        var bcTransId: String? = null
        if (transaction.amount <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (transaction.amount.stripTrailingZeros().scale() > asset.maximumNumberOfDigits.toInt()) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_TOO_MANY_DECIMAL_PLACES)
        }
        val receiverSubaccount = receiver.getSubaccountByAsset(asset)
        val senderAddress = sender.getSubaccountByAsset(CurrencyUnit.SART).address?.address
        if (senderAddress == null) {
            logger.info("Account not found")
            throw BadRequestException(ErrorCodes.ACCOUNT_NOT_FOUND)
        }
        apisLoggerRepository.save(
            ApisLog(
                "algoBCTransfer",
                amount.toString(),
                "Base amount: $amount $asset",
                receiverAddress
            )
        )
        try {
            logger.info("transferring SART...")
            val recAddress = algoCustomTokenWallet.accountInfoFromMnu(receiverAddress)!!.Address
            val balanceofsender = algoCustomTokenWallet.algoTokenBalance(senderAddress)
            logger.info("balance of Sendr : $balanceofsender")
            if (transactionStatus == TransactionStatus.SUCCESSFUL) {
                bcTransId = algoCustomTokenWallet.algoCutomTokenTransfer(senderAddress, recAddress, amount.toString())
            }
        } catch (ex: ResourceAccessException) {
            logger.info(ex.message)
            sender.owner?.let { returnMoney(it, asset, amount) }
            apisLoggerRepository.save(
                ApisLog(
                    "sendToExternalWalletWadzpayPrivateBlockChain",
                    ex.message.toString(),
                    sender.getOwnerName()!!,
                    receiverAddress
                )
            )
            throw ServiceUnavailableException(ErrorCodes.WALLET_NOT_AVAILABLE)
        }
        val commitReference = ReferenceConventions.commit(UUID.randomUUID())
        if (transactionStatus == TransactionStatus.SUCCESSFUL) {
            ledgerService.createCommit(
                commit {
                    reference(commitReference)
                    subaccountEntry(sender.getSubaccountByAsset(CurrencyUnit.SART).reference, amount.negate())
                    subaccountEntry(receiverSubaccount.reference, amount)
                },
                true
            )
        }
        transaction.reference = commitReference
        transaction.blockchainTxId = bcTransId
        transaction.issuanceBanks = issuanceBank
        transaction.sourceWalletAddress = senderAddress
        transaction.passcodeHash = passCodeHash
        transaction.status = transactionStatus
        return transactionRepository.save(transaction)
    }

    fun syncBlockChainBalance(accountOwner: AccountOwner) {
        val subaccount = accountOwner.account.getSubaccountByAsset(CurrencyUnit.SART)
        val blockChainBalance = subaccount.address?.address?.let { algoCustomTokenWallet.algoTokenBalance(it) }
        val ledgerBalance = getBalance(subaccount)
        val omnibusAccount = getOmnibusAccount()
        val to = accountOwner.account.subaccounts.find { it.asset == CurrencyUnit.SART.toString() }!!
        val from = omnibusAccount.subaccounts.find { it.asset == CurrencyUnit.SART.toString() }!!
        val commitReference = ReferenceConventions.commit(UUID.randomUUID())
        logger.info("CommitRef::")
        logger.info(commitReference.toString())
        if (blockChainBalance != null) {
            var actualBalance = BigDecimal.ZERO
            if (blockChainBalance.toBigDecimal().minus(ledgerBalance) > BigDecimal.ZERO) {
                actualBalance = blockChainBalance.toBigDecimal().minus(ledgerBalance)
                ledgerService.createCommit(
                    commit {
                        reference(commitReference)
                        subaccountEntry(to.reference, actualBalance)
                        subaccountEntry(from.reference, actualBalance.negate())
                    }
                )
                val trx = transactionRepository.save(
                    Transaction(
                        reference = commitReference,
                        sender = from,
                        receiver = to,
                        asset = CurrencyUnit.SART.toString(),
                        status = TransactionStatus.SUCCESSFUL,
                        createdAt = Instant.now(),
                        amount = actualBalance,
                        type = TransactionType.SYNC_BLOCKCHAIN
                    )
                )
                transactionRepository.save(trx.setTxId(id = trx.id))
            } else if (ledgerBalance.minus(blockChainBalance.toBigDecimal()) > BigDecimal.ZERO) {
                actualBalance = ledgerBalance.minus(blockChainBalance.toBigDecimal())
                ledgerService.createCommit(
                    commit {
                        reference(commitReference)
                        subaccountEntry(from.reference, actualBalance)
                        subaccountEntry(to.reference, actualBalance.negate())
                    }
                )
                val trx = transactionRepository.save(
                    Transaction(
                        reference = commitReference,
                        sender = from,
                        receiver = to,
                        asset = CurrencyUnit.SART.toString(),
                        status = TransactionStatus.SUCCESSFUL,
                        createdAt = Instant.now(),
                        amount = actualBalance,
                        type = TransactionType.SYNC_BLOCKCHAIN
                    )
                )
                transactionRepository.save(trx.setTxId(id = trx.id))
            } else {
                return
            }
        }
        return
    }

    fun createSARTTransaction(
        sender: AccountOwner,
        receiverAccount: AccountOwner,
        amount: BigDecimal,
        blockchainFee: BigDecimal,
        asset: CurrencyUnit,
        bcTransId: String ?,
        description: String?,
        status: TransactionStatus,
        transactionType: TransactionType,
        issuanceBanks: IssuanceBanks?,
        refundedTransactionId: String? = null
    ): Transaction {
        if (status == TransactionStatus.SUCCESSFUL) {
            return createTransaction(
                sender.account, receiverAccount.account, amount, asset,
                transactionType = transactionType, description = description,
                blockchainTxId = bcTransId, blockchainFee = blockchainFee, issuanceBanks = issuanceBanks
            )
        } else {
            val trx = transactionRepository.save(
                Transaction(
                    sender = sender.account.getSubaccountByAsset(asset),
                    receiver = receiverAccount.account.getSubaccountByAsset(asset),
                    amount = amount,
                    asset = asset.name,
                    status = status,
                    blockchainTxId = bcTransId,
                    type = transactionType,
                    description = description,
                    fee = blockchainFee,
                    issuanceBanks = issuanceBanks
                )
            )
            try {
                getComplyAdvantageResponse(trx)
            } catch (e: Exception) {
                apisLoggerRepository.save(
                    ApisLog(
                        "ComplyError", e.message.toString(), trx.toString(), "Comply Log Failed",
                        Instant.now()
                    )
                )
            }
            return transactionRepository.save(trx.setTxId(id = trx.id))
        }
    }

//
    fun createSARTTransactionMerchant(
        sender: AccountOwner,
        receiverAccount: AccountOwner,
        amount: BigDecimal,
        blockchainFee: BigDecimal,
        asset: CurrencyUnit,
        bcTransId: String,
        description: String?,
        status: TransactionStatus,
        transactionType: TransactionType,
        issuanceBanks: IssuanceBanks?
    ): Transaction {
        if (status == TransactionStatus.SUCCESSFUL) {
            return createTransaction(
                sender.account, receiverAccount.account, amount, asset,
                transactionType = transactionType, description = description,
                blockchainTxId = bcTransId, blockchainFee = blockchainFee, issuanceBanks = issuanceBanks
            )
        } else {
            val trx = transactionRepository.save(
                Transaction(
                    sender = sender.account.getSubaccountByAsset(asset),
                    receiver = receiverAccount.account.getSubaccountByAsset(asset),
                    amount = amount,
                    asset = asset.name,
                    status = status,
                    blockchainTxId = bcTransId,
                    type = transactionType,
                    description = description,
                    fee = blockchainFee,
                    txMode = TransactionMode.CUSTOMER_OFFLINE
                )
            )
            try {
                getComplyAdvantageResponse(trx)
            } catch (e: Exception) {
                apisLoggerRepository.save(
                    ApisLog(
                        "ComplyError", e.message.toString(), trx.toString(), "Comply Log Failed",
                        Instant.now()
                    )
                )
            }
            return transactionRepository.save(trx.setTxId(id = trx.id))
        }
    }

    fun getTransactionByPasscodeHash(passCodeHash: String?): MutableList<Transaction>? {
        return transactionRepository.getByPasscodeHash(passCodeHash)
    }

    fun createAccountsForPrivateBC(accountOwner: AccountOwner, assets: MutableList<AssetCreationRequest>): MutableList<Account> {
        accountOwner.accounts = mutableListOf(
            createAccountOnPrivateBC(accountOwner, AccountType.MAIN, assets),
            createAccountOnPrivateBC(accountOwner, AccountType.RESERVATION, assets)
        )
        return accountOwner.accounts
    }
    fun createAccountOnPrivateBC(accountOwner: AccountOwner, accountType: AccountType, assets: MutableList<AssetCreationRequest>): Account {
        val accountRef = ReferenceConventions.Account.account(accountOwner.id, accountType)
        val commitRef = ReferenceConventions.Account.createCommit(accountOwner.id, accountType)

        ledgerService.createAccount(accountRef)

        val account = accountRepository.save(Account(owner = accountOwner, reference = accountRef, type = accountType))
        ledgerService.createCommit(
            commit {
                reference(commitRef)
                for (asset in assets) {
                    val subaccountRef = ReferenceConventions.Account.subaccount(accountOwner.id, asset.assetName, accountType)
                    subaccountDeclaration(subaccountRef, asset.assetName, zero = true)
                }
            }
        )
        for (asset in assets) {
            var cryptoAddressOfAsset: CryptoAddress? = null
            cryptoAddressOfAsset = account.subaccounts.find { it.asset == asset.assetName }?.address
            if (cryptoAddressOfAsset == null) {
                val subaccountRef = ReferenceConventions.Account.subaccount(accountOwner.id, asset.assetName, accountType)
                val subAccount = subaccountService.createAccountForPrivateBC(account, subaccountRef, asset.assetName, accountOwner as UserAccount)
                account.subaccounts.add(subAccount)
            }
        }
        return account
    }
    fun createAccountsForPrivateBC1(accountOwner: AccountOwner, assets: String): MutableList<Account> {
        accountOwner.accounts = mutableListOf(
            createAccountOnPrivateBC1(accountOwner, AccountType.MAIN, assets),
            createAccountOnPrivateBC1(accountOwner, AccountType.RESERVATION, assets)
        )
        return accountOwner.accounts
    }
    fun createAccountOnPrivateBC1(accountOwner: AccountOwner, accountType: AccountType, asset: String): Account {
        val account = accountOwner.account
        var cryptoAddressOfAsset: CryptoAddress? = null
        cryptoAddressOfAsset = account.subaccounts.find { it.asset == asset }?.address
        if (cryptoAddressOfAsset == null) {
            val commitRef = ReferenceConventions.Account.createCommit(accountOwner.id, accountType)
            val subaccountRef = ReferenceConventions.Account.subaccount(accountOwner.id, asset, accountType)
            ledgerService.createCommit(
                commit {
                    reference(commitRef)
                    subaccountDeclaration(subaccountRef, asset, zero = true)
                },
                true
            )
            val subAccount = subaccountService.createAccountForPrivateBC(account, subaccountRef, asset, accountOwner as UserAccount)
            account.subaccounts.add(subAccount)
        }
        return account
    }
}
