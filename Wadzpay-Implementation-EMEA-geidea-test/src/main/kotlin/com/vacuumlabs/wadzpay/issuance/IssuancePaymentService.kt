package com.vacuumlabs.wadzpay.issuance
import com.vacuumlabs.ROUNDING_LIMIT
import com.vacuumlabs.vuba.ledger.service.LedgerService
import com.vacuumlabs.wadzpay.algocutomtoken.AlgoCutomeTokenService
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanksAuditLogs
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanksAuditLogsRepository
import com.vacuumlabs.wadzpay.issuance.models.Status
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.Transaction
import com.vacuumlabs.wadzpay.ledger.model.TransactionMode
import com.vacuumlabs.wadzpay.ledger.model.TransactionRepository
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.ledger.service.TransactionService
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.pos.PosService
import com.vacuumlabs.wadzpay.services.CognitoService
import com.vacuumlabs.wadzpay.services.RedisService
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.user.UserAccountService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.util.UUID
/**
 * This service provides methods for Transaction like LOAD/REDEEM/P2P/QR payment.
 */
@Service
class IssuancePaymentService(
    val cognitoService: CognitoService,
    val userAccountService: UserAccountService,
    val transactionRepository: TransactionRepository,
    val ledgerService: LedgerService,
    val algoCustomTokenService: AlgoCutomeTokenService,
    val transactionService: TransactionService,
    val issuanceWalletService: IssuanceWalletService,
    val issuanceService: IssuanceService,
    val issuanceConfigurationService: IssuanceConfigurationService,
    @org.springframework.context.annotation.Lazy
    @Autowired
    val posService: PosService,
    val redisService: RedisService,
    val issuanceBanksAuditLogsRepository: IssuanceBanksAuditLogsRepository
) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)
    @Transactional
    fun getTransactionTopUpStatus(issuerInstitutionUserAccount: UserAccount, transactionStatusRequest: IssuancePaymentController.TransactionStatusRequest): IssuancePaymentController.TransactionStatusResponse? {
        transactionStatusRequest.tokenAmount = transactionStatusRequest.tokenAmount.setScale(ROUNDING_LIMIT, RoundingMode.UP)
        val userAccount: UserAccount = if (!checkEmail(transactionStatusRequest.userId!!)) {
            userAccountService.getUserAccountByCustomerId(
                transactionStatusRequest.userId,
                issuerInstitutionUserAccount.issuanceBanks?.institutionId
            )
        } else {
            userAccountService.getUserAccountByEmail(transactionStatusRequest.userId)
        }
        if (userAccount.merchant != null) {
            throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
        }
        val fiatAsset = if (issuerInstitutionUserAccount.issuanceBanks != null) issuerInstitutionUserAccount.issuanceBanks!!.fiatCurrency else FiatCurrencyUnit.IDR
        val bankAccount = redisService.getBankDetailsInCache(transactionStatusRequest.userId, IssuanceWalletUserController.TransactionType.LOAD.toString())
        val loadTokenRequest = IssuanceCommonController.LoadTokenBalanceRequest(
            email = transactionStatusRequest.userId,
            tokenAsset = CurrencyUnit.SART,
            amount = transactionStatusRequest.tokenAmount,
            bankAccountNumber = bankAccount,
            fiatAsset = fiatAsset,
            fiatAmount = fiatAsset?.let {
                calculateFiatAmount(
                    transactionStatusRequest.tokenAmount,
                    it,
                    userAccount,
                    IssuanceWalletUserController.TransactionType.LOAD
                )
            },
            feeConfigData = null,
            totalFeeApplied = BigDecimal.ZERO,
            totalRequestedAmount = fiatAsset?.let {
                calculateFiatAmount(
                    transactionStatusRequest.tokenAmount,
                    it,
                    userAccount,
                    IssuanceWalletUserController.TransactionType.LOAD
                )
            }
        )
        val loadTokenResponse = issuerInstitutionUserAccount.issuanceBanks?.let {
            issuanceWalletService.loadWalletBalance(
                loadTokenRequest, userAccount,
                it,
                transactionStatusRequest.transactionStatus
            )
        }
        if (loadTokenResponse != null) {
            redisService.deleteBankDetailsInCache(transactionStatusRequest.userId, IssuanceWalletUserController.TransactionType.LOAD.toString())
            val subacc = userAccount.account.getSubaccountByAsset(CurrencyUnit.SART)
            val userAddress = subacc.address!!.address
            return IssuancePaymentController.TransactionStatusResponse(
                uuid = loadTokenResponse.uuid.toString(),
                totalTokenAmount = getWalletBalance(userAccount, CurrencyUnit.SART),
                address = userAddress,
                transactionStatus = loadTokenResponse.status.toString(),
                depositedTokenAmount = loadTokenResponse.amount,
                transactionId = loadTokenResponse.trxId,
                blockChainHash = loadTokenResponse.blockchainTxId
            )
        }
        throw EntityNotFoundException(ErrorCodes.BAD_REQUEST)
    }

    private fun calculateFiatAmount(
        digitalTokenAmount: BigDecimal,
        fiatAsset: FiatCurrencyUnit,
        userAccount: UserAccount,
        transactionType: IssuanceWalletUserController.TransactionType
    ): BigDecimal {
        println("fiatAsset ==> $fiatAsset")
        val baseRateData =
            issuanceService.fiatExchangeRatesNew(
                fiatAsset,
                transactionType,
                userAccount = userAccount
            )
        println("baseRate Data==> $baseRateData")
        val baseRate = baseRateData?.get(FiatCurrencyUnit.SAR)
        println("baseRate ==> $baseRate")
        return digitalTokenAmount.div(baseRate!!)
    }

    fun getTransactionRefundTokenStatus(issuerInstitutionUserAccount: UserAccount, transactionStatusRequest: IssuancePaymentController.TransactionStatusRequest): IssuancePaymentController.TransactionStatusRedeemResponse? {
        transactionStatusRequest.tokenAmount = transactionStatusRequest.tokenAmount.setScale(ROUNDING_LIMIT, RoundingMode.UP)
        val userAccount: UserAccount = if (!checkEmail(transactionStatusRequest.userId!!)) {
            userAccountService.getUserAccountByCustomerId(
                transactionStatusRequest.userId,
                issuerInstitutionUserAccount.issuanceBanks?.institutionId
            )
        } else {
            userAccountService.getUserAccountByEmail(transactionStatusRequest.userId)
        }
        if (userAccount.merchant != null) {
            throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
        }
        val subAccount = userAccount.account.getSubAccountByAssetAlgoString(CurrencyUnit.SART.toString())
        val vubaSubaccount = subAccount?.let { ledgerService.getSubaccount(it.reference) }
        if (vubaSubaccount != null) {
            if (vubaSubaccount.isPresent) {
                if (vubaSubaccount.get().balance < transactionStatusRequest.tokenAmount) {
                    throw EntityNotFoundException(ErrorCodes.INSUFFICIENT_FUNDS)
                }
            }
        } else {
            throw EntityNotFoundException(ErrorCodes.INSUFFICIENT_FUNDS)
        }
        val fiatAsset = if (issuerInstitutionUserAccount.issuanceBanks != null) issuerInstitutionUserAccount.issuanceBanks!!.fiatCurrency else FiatCurrencyUnit.IDR
        val bankAccount = redisService.getBankDetailsInCache(transactionStatusRequest.userId, IssuanceWalletUserController.TransactionType.REFUND.toString())
        val refundTokenRequest = IssuanceCommonController.RefundTokenBalanceRequest(
            tokenAsset = CurrencyUnit.SART,
            amount = transactionStatusRequest.tokenAmount,
            bankAccountNumber = bankAccount,
            fiatAsset = fiatAsset,
            fiatAmount = fiatAsset?.let { calculateFiatAmount(transactionStatusRequest.tokenAmount, it, userAccount, IssuanceWalletUserController.TransactionType.REFUND) },
            feeConfigData = null,
            totalFeeApplied = BigDecimal.ZERO
        )
        val refundTokenResponse = issuerInstitutionUserAccount.issuanceBanks?.let {
            issuanceWalletService.refundToken(
                refundTokenRequest,
                userAccount,
                it,
                transactionStatusRequest.transactionStatus
            )
        }
        if (refundTokenResponse != null) {
            redisService.deleteBankDetailsInCache(transactionStatusRequest.userId, IssuanceWalletUserController.TransactionType.REFUND.toString())
            val subacc = userAccount.account.getSubaccountByAsset(CurrencyUnit.SART)
            val userAddress = subacc.address!!.address
            return IssuancePaymentController.TransactionStatusRedeemResponse(
                uuid = refundTokenResponse.uuid.toString(),
                totalTokenAmount = getWalletBalance(userAccount, CurrencyUnit.SART),
                address = userAddress,
                transactionStatus = refundTokenResponse.status.toString(),
                redeemTokenAmount = refundTokenResponse.amount,
                transactionId = refundTokenResponse.trxId,
                blockChainHash = refundTokenResponse.blockchainTxId
            )
        }
        throw EntityNotFoundException(ErrorCodes.BAD_REQUEST)
    }

    fun p2pPayment(issuerInstitutionUserAccount: UserAccount, transactionStatusRequest: IssuancePaymentController.TransactionP2PStatusRequest): IssuancePaymentController.TransactionStatusP2PResponse? {
        transactionStatusRequest.tokenAmount = transactionStatusRequest.tokenAmount.setScale(ROUNDING_LIMIT, RoundingMode.UP)
        val senderAccount: UserAccount = if (!checkEmail(transactionStatusRequest.userId!!)) {
            userAccountService.getUserAccountByCustomerId(
                transactionStatusRequest.userId,
                issuerInstitutionUserAccount.issuanceBanks?.institutionId
            )
        } else {
            userAccountService.getUserAccountByEmail(transactionStatusRequest.userId)
        }
        if (senderAccount.merchant != null) {
            throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
        }
        val receiverAccount: UserAccount = if (!checkEmail(transactionStatusRequest.receiverUserId)) {
            userAccountService.getUserAccountByCustomerId(
                transactionStatusRequest.receiverUserId,
                issuerInstitutionUserAccount.issuanceBanks?.institutionId
            )
        } else {
            userAccountService.getUserAccountByEmail(transactionStatusRequest.receiverUserId)
        }
        if (receiverAccount.merchant != null) {
            throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
        }
        if (senderAccount == receiverAccount) {
            throw EntityNotFoundException(ErrorCodes.SENDER_RECEIVER_WALLET_ADDRESS_CANT_BE_SAME)
        }
        val subAccount = senderAccount.account.getSubAccountByAssetAlgoString(CurrencyUnit.SART.toString())
        val vubaSubaccount = subAccount?.let { ledgerService.getSubaccount(it.reference) }
        if (vubaSubaccount != null) {
            if (vubaSubaccount.isPresent) {
                if (vubaSubaccount.get().balance < transactionStatusRequest.tokenAmount) {
                    throw EntityNotFoundException(ErrorCodes.INSUFFICIENT_FUNDS)
                }
            }
        } else {
            throw EntityNotFoundException(ErrorCodes.INSUFFICIENT_FUNDS)
        }
        var issuanceBank: IssuanceBanks? = null
        val issuanceBanksUserEntry = issuanceWalletService.getIssuanceBankMapping(receiverAccount)
        if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.status == Status.DISABLED) {
            throw EntityNotFoundException(ErrorCodes.WALLET_DISABLED_RECIPIENT)
        }
        val senderIBank = issuanceWalletService.getIssuanceBankMapping(senderAccount)
        if (senderIBank != null) {
            if (senderIBank.status == Status.DISABLED) {
                throw EntityNotFoundException(ErrorCodes.WALLET_DISABLED)
            }
            if (senderIBank.issuanceBanksId.p2pTransfer != null && senderIBank.issuanceBanksId.p2pTransfer == false) {
                throw EntityNotFoundException(ErrorCodes.P2P_TRANSFER_DISABLED)
            }
            issuanceBank = senderIBank.issuanceBanksId
        }
        if (issuanceBanksUserEntry != null) {
            if (issuanceBanksUserEntry.issuanceBanksId != issuanceBank) {
                throw EntityNotFoundException(ErrorCodes.OTHER_BANK_TRANSFER_NOT_ALLOWED)
            }
        } else {
            throw EntityNotFoundException(ErrorCodes.OTHER_BANK_TRANSFER_NOT_ALLOWED)
        }
        val isActivationFeeDeducted = issuanceConfigurationService.checkActivationFeeAlreadyDeducted(
            IssuanceCommonController.WalletFeeType.WF_001.walletFeeId, receiverAccount
        )
        if (!isActivationFeeDeducted) {
            throw EntityNotFoundException(ErrorCodes.RECIPIENT_WALLET_NOT_ACTIVATED)
        }
        logger.info("algo transfer for p2p transaction")
        val transaction = algoCustomTokenService.p2pTransferByWadzpayPrivateBlockChain(
            senderAccount,
            receiverAccount,
            transactionStatusRequest.tokenAmount,
            CurrencyUnit.SART,
            receiverAccount.account.getSubaccountByAsset(CurrencyUnit.SART).address!!.address,
            TransactionType.PEER_TO_PEER,
            issuanceBanks = issuanceBank,
            status = if (!transactionStatusRequest.transactionStatus) TransactionStatus.FAILED else TransactionStatus.SUCCESSFUL
        )
        transactionService.showPushNotifications(transaction)
        val subacc = senderAccount.account.getSubaccountByAsset(CurrencyUnit.SART)
        val userAddress = subacc.address!!.address
        return IssuancePaymentController.TransactionStatusP2PResponse(
            uuid = transaction.uuid.toString(),
            totalTokenAmount = getWalletBalance(senderAccount, CurrencyUnit.SART),
            address = userAddress,
            transactionStatus = transaction.status.toString(),
            transferTokenAmount = transaction.amount,
            transactionId = transaction.tx_id,
            blockChainHash = transaction.blockchainTxId
        )
    }

    fun qrPayment(issuerInstitutionUserAccount: UserAccount, transactionStatusRequest: IssuancePaymentController.TransactionQRPaymentStatusRequest): IssuancePaymentController.TransactionStatusQRResponse? {
        transactionStatusRequest.tokenAmount = transactionStatusRequest.tokenAmount.setScale(ROUNDING_LIMIT, RoundingMode.UP)
        val senderAccount: UserAccount = if (!checkEmail(transactionStatusRequest.userId!!)) {
            userAccountService.getUserAccountByCustomerId(
                transactionStatusRequest.userId,
                issuerInstitutionUserAccount.issuanceBanks?.institutionId
            )
        } else {
            userAccountService.getUserAccountByEmail(transactionStatusRequest.userId)
        }
        if (senderAccount.merchant != null) {
            throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
        }
        val subAccount = senderAccount.account.getSubAccountByAssetAlgoString(CurrencyUnit.SART.toString())
        val vubaSubaccount = subAccount?.let { ledgerService.getSubaccount(it.reference) }
        if (vubaSubaccount != null) {
            if (vubaSubaccount.isPresent) {
                if (vubaSubaccount.get().balance < transactionStatusRequest.tokenAmount) {
                    throw EntityNotFoundException(ErrorCodes.INSUFFICIENT_FUNDS)
                }
            }
        } else {
            throw EntityNotFoundException(ErrorCodes.INSUFFICIENT_FUNDS)
        }
        val issuanceBanksUserEntry = issuanceWalletService.getIssuanceBankMapping(senderAccount)
        if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.status == Status.DISABLED) {
            throw EntityNotFoundException(ErrorCodes.WALLET_DISABLED)
        }
        var transaction: Transaction? = null
        if (transactionStatusRequest.posuuid != null) {
            transaction = posService.posTransactionSart(
                senderAccount,
                null,
                transactionStatusRequest.tokenAmount,
                CurrencyUnit.SART,
                UUID.fromString(transactionStatusRequest.posuuid),
                issuerInstitutionUserAccount.issuanceBanks,
                TransactionMode.CUSTOMER_MERCHANT_ONLINE,
                null,
                if (!transactionStatusRequest.transactionStatus) TransactionStatus.FAILED else TransactionStatus.SUCCESSFUL
            )
        } else {
            if (transactionStatusRequest.merchantId != null) {
                val merchantAccount: UserAccount = if (!isNumeric(transactionStatusRequest.merchantId)) {
                    userAccountService.getUserAccountByEmail(transactionStatusRequest.merchantId)
                } else {
                    val userAccount = userAccountService.getUserAccountByMerchantId(transactionStatusRequest.merchantId.toLong())
                    if (userAccount.size == 0) {
                        throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)
                    }
                    userAccount[0].userAccount
                }
                if (senderAccount == merchantAccount) {
                    throw EntityNotFoundException(ErrorCodes.SENDER_RECEIVER_WALLET_ADDRESS_CANT_BE_SAME)
                }
                val merchantSubacc = merchantAccount.merchant?.account?.getSubaccountByAsset(CurrencyUnit.SART)
                val merchantAddress = merchantSubacc?.address!!.address
                val posId = posService.getPosId(merchantAccount.merchant!!)
                posService.merchantOfflinePosTransactionSart(
                    senderAccount,
                    merchantAccount,
                    merchantAddress,
                    transactionStatusRequest.tokenAmount,
                    CurrencyUnit.SART,
                    merchantAccount.merchant!!.id,
                    posId,
                    issuerInstitutionUserAccount.issuanceBanks,
                    if (!transactionStatusRequest.transactionStatus) TransactionStatus.FAILED else TransactionStatus.SUCCESSFUL
                ).also { transaction = it }
            }
        }
        if (transaction != null) {
            val subacc = senderAccount.account.getSubaccountByAsset(CurrencyUnit.SART)
            val userAddress = subacc.address!!.address
            return IssuancePaymentController.TransactionStatusQRResponse(
                uuid = transaction!!.uuid.toString(),
                totalTokenAmount = getWalletBalance(senderAccount, CurrencyUnit.SART),
                address = userAddress,
                transactionStatus = transaction!!.status.toString(),
                paidTokenAmount = transaction!!.amount,
                transactionId = transaction!!.tx_id,
                blockChainHash = transaction!!.blockchainTxId
            )
        }
        throw EntityNotFoundException(ErrorCodes.BAD_REQUEST)
    }

    fun getWalletBalance(userAccount: UserAccount, currencyUnit: CurrencyUnit): BigDecimal {
        val subAccount = userAccount.account.getSubAccountByAssetAlgoString(CurrencyUnit.SART.toString())
        val vubaSubaccount = subAccount?.let { ledgerService.getSubaccount(it.reference) }
        var balance = BigDecimal.ZERO
        if (vubaSubaccount != null) {
            if (vubaSubaccount.isPresent) {
                balance = vubaSubaccount.get().balance.setScale(ROUNDING_LIMIT, RoundingMode.UP)
            }
        }
        return balance
    }
    fun checkEmail(username: String): Boolean {
        println("checkEmail ==> $username")
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        return username.matches(emailRegex.toRegex())
    }

    fun isNumeric(strNum: String?): Boolean {
        if (strNum == null) {
            return false
        }
        try {
            val d = strNum.toDouble()
        } catch (nfe: NumberFormatException) {
            return false
        }
        return true
    }

    fun saveBankDetailsInCache(
        issuerInstitution: UserAccount,
        transactionCacheRequest: IssuancePaymentController.TransactionCacheRequest
    ): TransactionStatus {
        /* Checking userId format */
        val senderAccount: UserAccount = if (!checkEmail(transactionCacheRequest.userId!!)) {
            userAccountService.getUserAccountByCustomerId(
                transactionCacheRequest.userId,
                issuerInstitution.issuanceBanks?.institutionId
            )
        } else {
            userAccountService.getUserAccountByEmail(transactionCacheRequest.userId)
        }
        if (senderAccount.merchant != null) {
            throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
        }
        /* Redis for store Bank details in cache*/
        redisService.setBankDetailsInCache(transactionCacheRequest.userId, transactionCacheRequest.transactionType.toString(), transactionCacheRequest.bankAccount)
        return TransactionStatus.SUCCESSFUL
    }

    fun saveIssuanceBanksAuditLogs(
        updateIssuanceRequest: IssuanceCommonController.UpdateIssuanceRequest,
        issuanceBanks: IssuanceBanks
    ): Boolean {
        val issuanceBankData = issuanceBanks.email?.let { issuanceService.getIssuanceBankAccountByEmail(it) }
        val auditLog = IssuanceBanksAuditLogs(
            issuanceBanksId = issuanceBanks,
            modifiedDate = Instant.now(),
            isActive = true,
            modifiedBy = issuanceBanks
        )
        if (updateIssuanceRequest.isP2PEnabled != null) {
            auditLog.columnName = "p2p_transfer"
            auditLog.oldValue = issuanceBankData?.p2pTransfer.toString()
            auditLog.newValue = updateIssuanceRequest.isP2PEnabled.toString()
        }
        issuanceBanksAuditLogsRepository.save(auditLog)
        return true
    }
}
