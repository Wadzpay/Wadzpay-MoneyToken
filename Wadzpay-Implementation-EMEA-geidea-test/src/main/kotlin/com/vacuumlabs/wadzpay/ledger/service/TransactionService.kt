package com.vacuumlabs.wadzpay.ledger.service

import au.com.console.jpaspecificationdsl.and
import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.wadzpay.bitgo.WalletService
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.emailSms.service.EmailSMSSenderService
import com.vacuumlabs.wadzpay.issuance.IssuanceCommonController
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanksUserEntry
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanksUserEntryRepository
import com.vacuumlabs.wadzpay.issuance.models.Status
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.AccountOwner
import com.vacuumlabs.wadzpay.ledger.model.MerchantConfig
import com.vacuumlabs.wadzpay.ledger.model.MerchantConfigRepository
import com.vacuumlabs.wadzpay.ledger.model.MerchantConfigRequest
import com.vacuumlabs.wadzpay.ledger.model.MerchantConfigResponse
import com.vacuumlabs.wadzpay.ledger.model.RefundAcceptRejectStatus
import com.vacuumlabs.wadzpay.ledger.model.RefundAcceptRejectType
import com.vacuumlabs.wadzpay.ledger.model.RefundFormFields
import com.vacuumlabs.wadzpay.ledger.model.RefundFormRepository
import com.vacuumlabs.wadzpay.ledger.model.RefundMode
import com.vacuumlabs.wadzpay.ledger.model.RefundOrigin
import com.vacuumlabs.wadzpay.ledger.model.RefundStatus
import com.vacuumlabs.wadzpay.ledger.model.RefundToken
import com.vacuumlabs.wadzpay.ledger.model.RefundTokenRepository
import com.vacuumlabs.wadzpay.ledger.model.RefundType
import com.vacuumlabs.wadzpay.ledger.model.SortableTransactionFields
import com.vacuumlabs.wadzpay.ledger.model.Transaction
import com.vacuumlabs.wadzpay.ledger.model.TransactionDirection
import com.vacuumlabs.wadzpay.ledger.model.TransactionMode
import com.vacuumlabs.wadzpay.ledger.model.TransactionRefundDetails
import com.vacuumlabs.wadzpay.ledger.model.TransactionRefundDetailsRepository
import com.vacuumlabs.wadzpay.ledger.model.TransactionRepository
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.ledger.model.TransactionWalletFeeDetailsRepository
import com.vacuumlabs.wadzpay.ledger.model.belongsTo
import com.vacuumlabs.wadzpay.ledger.model.belongsToAccount
import com.vacuumlabs.wadzpay.ledger.model.getDirection
import com.vacuumlabs.wadzpay.ledger.model.hasAmountEqualTo
import com.vacuumlabs.wadzpay.ledger.model.hasAmountGreaterOrEqualTo
import com.vacuumlabs.wadzpay.ledger.model.hasAmountLessOrEqualTo
import com.vacuumlabs.wadzpay.ledger.model.hasAsset
import com.vacuumlabs.wadzpay.ledger.model.hasDateGreaterOrEqualTo
import com.vacuumlabs.wadzpay.ledger.model.hasDateLessOrEqualTo
import com.vacuumlabs.wadzpay.ledger.model.hasDirection
import com.vacuumlabs.wadzpay.ledger.model.hasExtPosIdEqualTo
import com.vacuumlabs.wadzpay.ledger.model.hasExtPosSequenceNumberEqualTo
import com.vacuumlabs.wadzpay.ledger.model.hasFiatAmountEqualTo
import com.vacuumlabs.wadzpay.ledger.model.hasLogicalDateGreaterOrEqualTo
import com.vacuumlabs.wadzpay.ledger.model.hasLogicalDateLessOrEqualTo
import com.vacuumlabs.wadzpay.ledger.model.hasOwner
import com.vacuumlabs.wadzpay.ledger.model.hasStatus
import com.vacuumlabs.wadzpay.ledger.model.hasTotalDigitalCurrencyReceivedEqualTo
import com.vacuumlabs.wadzpay.ledger.model.hasTotalFiatReceivedEqualTo
import com.vacuumlabs.wadzpay.ledger.model.hasTransactionIdEqualTo
import com.vacuumlabs.wadzpay.ledger.model.hasTransactionMode
import com.vacuumlabs.wadzpay.ledger.model.hasType
import com.vacuumlabs.wadzpay.ledger.model.hasUUIDEqualTo
import com.vacuumlabs.wadzpay.ledger.model.ownerNamesContainPattern
import com.vacuumlabs.wadzpay.merchant.InitiateWebLinkRefund
import com.vacuumlabs.wadzpay.merchant.RefundInitiationRequest
import com.vacuumlabs.wadzpay.merchant.model.Merchant
import com.vacuumlabs.wadzpay.merchant.model.MerchantRepository
import com.vacuumlabs.wadzpay.notification.NotificationService
import com.vacuumlabs.wadzpay.pos.PosTransactionRepository
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.viewmodels.FeeConfigData
import com.vacuumlabs.wadzpay.viewmodels.RefundTransactionViewModel
import com.vacuumlabs.wadzpay.viewmodels.SettlementReport
import com.vacuumlabs.wadzpay.viewmodels.TransactionSettlement
import com.vacuumlabs.wadzpay.viewmodels.TransactionViewModel
import com.vacuumlabs.wadzpay.viewmodels.toRefundViewModel
import com.vacuumlabs.wadzpay.viewmodels.toTransactionSettlementViewModel
import com.vacuumlabs.wadzpay.viewmodels.toViewModel
import com.vacuumlabs.wadzpay.viewmodels.toViewModelSettlementReport
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.validation.constraints.PositiveOrZero
import kotlin.collections.ArrayList
import kotlin.streams.toList

@Service
class TransactionService(
    val notificationService: NotificationService,
    val transactionRepository: TransactionRepository,
    @Lazy val userAccountService: UserAccountService,
    @Lazy val walletService: WalletService,
    val posTransactionRepository: PosTransactionRepository,
    val refundTokenRepository: RefundTokenRepository,
    val emailSMSSenderService: EmailSMSSenderService,
    val appConfig: AppConfig,
    val refundFormRepository: RefundFormRepository,
    val refundTransactionsRepository: TransactionRefundDetailsRepository,
    val merchantConfigRepository: MerchantConfigRepository,
    val merchantRepository: MerchantRepository,
    val transactionWalletFeeDetailsRepository: TransactionWalletFeeDetailsRepository,
    val issuanceBanksUserEntryRepository: IssuanceBanksUserEntryRepository
) {
    fun showPushNotifications(transaction: Transaction) {
        val sender = transaction.sender.account.owner
        val receiver = transaction.receiver.account.owner
        if (sender is UserAccount) {
            notificationService.sendTransactionNotification(
                sender,
                transaction.toViewModel(direction = TransactionDirection.OUTGOING)
            )
        }
        if (receiver is UserAccount) {
            notificationService.sendTransactionNotification(
                receiver,
                transaction.toViewModel(direction = TransactionDirection.INCOMING)
            )
        }
    }

    fun showPushNotificationsPOS(transaction: Transaction) {
        val sender = transaction.sender.account.owner
        val receiver = transaction.receiver.account.owner
        if (sender is UserAccount) {
            notificationService.sendTransactionNotification(
                sender,
                transaction.toViewModel(direction = TransactionDirection.OUTGOING)
            )
        }
        if (receiver is UserAccount) {
            notificationService.sendTransactionNotification(
                receiver,
                transaction.toViewModel(direction = TransactionDirection.POS)
            )
        }
    }

    fun getByIdAndOwner(id: UUID, owner: AccountOwner): Transaction {

        println("143")
        val transaction = transactionRepository.getByUuid(id)
            ?: throw EntityNotFoundException("Transaction with id $id not found")

        if (!transaction.belongsTo(owner)) {
            throw EntityNotFoundException("Transaction with id $id not found")
        }
        return transaction
    }

    fun updateAcceptApproveRejectDetails(
        request: GetAcceptApproveRejectRequest,
        accountOwner: AccountOwner,
        principal: Authentication
    ): RefundTransactionViewModel? {
        val transaction = getByIdAndOwner(request.txn_uuid, accountOwner)
        val refundTransaction = getRefundTransactionWaitingForApproval(transaction) ?: throw BadRequestException(ErrorCodes.NO_REFUND_TRANSACTION_FOR_APPROVAL)
        return when (request.type) {
            RefundAcceptRejectType.ACCEPTANCE -> {
                // Accepted by supervisor
                refundTransaction.refundStatus = if (request.status == RefundAcceptRejectStatus.ACCEPT) {
                    RefundStatus.REFUND_APPROVED
                } else {
                    RefundStatus.REFUND_CANCELED
                }
                processAcceptance(principal, accountOwner, request, transaction)
            }

            RefundAcceptRejectType.APPROVAL -> {
                // Approved. Need to transfer refund amount
                processApproval(accountOwner, request, transaction, refundTransaction)
            }

            else -> {
                null
            }
        }
    }

    fun updateAcceptApproveRejectDetailsQAR(
        request: GetAcceptApproveRejectRequest,
        accountOwner: AccountOwner,
        principal: Authentication
    ): RefundTransactionViewModel? {
        val transaction = getByIdAndOwner(request.txn_uuid, accountOwner)
        val refundTransaction = getRefundTransactionWaitingForApproval(transaction) ?: throw BadRequestException(ErrorCodes.NO_REFUND_TRANSACTION_FOR_APPROVAL)
        return when (request.type) {
            RefundAcceptRejectType.ACCEPTANCE -> {
                // Accepted by supervisor
                refundTransaction.refundStatus = if (request.status == RefundAcceptRejectStatus.ACCEPT) {
                    RefundStatus.REFUND_APPROVED
                } else {
                    RefundStatus.REFUND_CANCELED
                }
                processAcceptance(principal, accountOwner, request, transaction)
            }

            RefundAcceptRejectType.APPROVAL -> {
                // Approved. Need to transfer refund amount
                processApprovalQAR(accountOwner, request, transaction, refundTransaction)
            }

            else -> {
                null
            }
        }
    }

    @Transactional(rollbackForClassName = ["ServiceUnavailableException"])
    fun updateOneLevelApproveRejectTransaction(
        request: GetAcceptApproveRejectRequest,
        accountOwner: AccountOwner,
        principal: Authentication?
    ): RefundTransactionViewModel? {
        val transaction = getByIdAndOwner(request.txn_uuid, accountOwner)
        val refundTransaction: TransactionRefundDetails
        if (isFullyRefunded(transaction)) {
            throw BadRequestException(ErrorCodes.TRANSACTION_IS_ALREADY_SETTLED_NOT_REFUND_AGAIN)
        }

        return when (request.type) {
            RefundAcceptRejectType.APPROVAL -> {
                refundTransaction = getRefundTransactionWaitingForApproval(transaction) ?: throw BadRequestException(ErrorCodes.NO_REFUND_TRANSACTION_FOR_APPROVAL)
                // Approved by supervisor
                refundTransaction.refundStatus = if (request.status == RefundAcceptRejectStatus.ACCEPT) {
                    RefundStatus.REFUND_APPROVED
                } else {
                    RefundStatus.REFUND_CANCELED
                }
                processApproval(accountOwner, request, transaction, refundTransaction)
            }

            RefundAcceptRejectType.HOLD -> {
                refundTransaction = getRefundTransactionWaitingForHoldUnHold(transaction) ?: throw BadRequestException(ErrorCodes.NO_REFUND_TRANSACTION_FOR_HOLD)
                processHold(refundTransaction)
            }

            else -> {
                null
            }
        }
    }

    private fun processHold(refundTransaction: TransactionRefundDetails): RefundTransactionViewModel {
        if (refundTransaction.refundStatus == RefundStatus.REFUND_HOLD) {
            refundTransaction.refundStatus = RefundStatus.REFUND_ACCEPTED
        } else if (refundTransaction.refundStatus == RefundStatus.REFUND_ACCEPTED) {
            refundTransaction.refundStatus = RefundStatus.REFUND_HOLD
        }
        refundTransaction.lastUpdatedOn = Instant.now()
        refundTransactionsRepository.save(refundTransaction)
        return refundTransaction.transaction.toRefundViewModel(refundTransaction = refundTransaction)
    }

    private fun processApproval(
        accountOwner: AccountOwner,
        request: GetAcceptApproveRejectRequest,
        transaction: Transaction,
        _refundTransaction: TransactionRefundDetails
    ): RefundTransactionViewModel {
        var refundTransaction = _refundTransaction
        refundTransaction.refundAcceptanceComment = request.rejectReason
        refundTransaction.refundSettlementDate = Instant.now()
        refundTransaction.refundDateTime = Instant.now()
        refundTransaction.lastUpdatedOn = Instant.now()
        if (request.status == RefundAcceptRejectStatus.REJECT) {
            refundTransaction.refundStatus = RefundStatus.REFUND_CANCELED
            refundTransactionsRepository.save(refundTransaction)
            sendRefundRejectedEmailAndSms(refundTransaction, transaction, request)
            return transaction.toRefundViewModel(refundTransaction = refundTransaction)
        }

        // if refund asset is USDT
        try {
            if (appConfig.environment.equals("dev", true) || appConfig.environment.equals("geidea-dev", true)) {
                println(" if - @248 - ${appConfig.environment}")
                refundTransaction = walletService.sendRefundToExternalWalletDev(
                    accountOwner,
                    refundTransaction.refundAmountDigital
                        ?: throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO),
                    refundTransaction.refundDigitalCurrencyType ?: throw BadRequestException(ErrorCodes.INVALID_ASSET_TYPE),
                    refundTransaction.refundWalletAddress ?: throw BadRequestException(ErrorCodes.INVALID_WALLET_ADDRESS),
                    RefundStatus.REFUNDED.name,
                    TransactionType.REFUND,
                    refundTransaction.refundFiatType,
                    refundTransaction.refundAmountFiat,
                    transaction.blockchainTxId,
                    refundTransaction
                )
            } else {
                println(" if - @264 - ${appConfig.environment}")
                refundTransaction = walletService.sendRefundToExternalWallet(
                    accountOwner,
                    refundTransaction.refundAmountDigital
                        ?: throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO),
                    refundTransaction.refundDigitalCurrencyType ?: throw BadRequestException(ErrorCodes.INVALID_ASSET_TYPE),
                    refundTransaction.refundWalletAddress ?: throw BadRequestException(ErrorCodes.INVALID_WALLET_ADDRESS),
                    RefundStatus.REFUNDED.name,
                    TransactionType.REFUND,
                    refundTransaction.refundFiatType,
                    refundTransaction.refundAmountFiat,
                    transaction.blockchainTxId,
                    refundTransaction
                )
            }
        } catch (e: Exception) {
            // any exception, Refund Failed
            refundTransaction.refundStatus = RefundStatus.REFUND_FAILED
            refundTransactionsRepository.save(refundTransaction)
        }

        if (refundTransaction.refundStatus == RefundStatus.REFUNDED) {
            sendRefundSuccessEmailAndSms(refundTransaction, transaction, request)
        } else if (refundTransaction.refundStatus == RefundStatus.REFUND_FAILED) {
            sendRefundFailedEmailAndSms(refundTransaction, transaction, request)
        }

        return transaction.toRefundViewModel(refundTransaction = refundTransaction)
    }

    private fun processApprovalQAR(
        accountOwner: AccountOwner,
        request: GetAcceptApproveRejectRequest,
        transaction: Transaction,
        _refundTransaction: TransactionRefundDetails
    ): RefundTransactionViewModel {
        var refundTransaction = _refundTransaction
        refundTransaction.refundAcceptanceComment = request.rejectReason
        refundTransaction.refundSettlementDate = Instant.now()
        refundTransaction.refundDateTime = Instant.now()
        refundTransaction.lastUpdatedOn = Instant.now()
        if (request.status == RefundAcceptRejectStatus.REJECT) {
            refundTransaction.refundStatus = RefundStatus.REFUND_CANCELED
            refundTransactionsRepository.save(refundTransaction)
            sendRefundRejectedEmailAndSms(refundTransaction, transaction, request)
            return transaction.toRefundViewModel(refundTransaction = refundTransaction)
        }

        // if refund asset is USDT
        try {
            if (appConfig.environment.equals("dev", true) || appConfig.environment.equals("geidea-dev", true)) {
                println(" if - @248 - ${appConfig.environment}")
                refundTransaction = walletService.sendRefundToExternalWalletDev(
                    accountOwner,
                    refundTransaction.refundAmountDigital
                        ?: throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO),
                    refundTransaction.refundDigitalCurrencyType ?: throw BadRequestException(ErrorCodes.INVALID_ASSET_TYPE),
                    refundTransaction.refundWalletAddress ?: throw BadRequestException(ErrorCodes.INVALID_WALLET_ADDRESS),
                    RefundStatus.REFUNDED.name,
                    TransactionType.REFUND,
                    refundTransaction.refundFiatType,
                    refundTransaction.refundAmountFiat,
                    transaction.blockchainTxId,
                    refundTransaction
                )
            } else {
                println(" if - @264 - ${appConfig.environment}")
                refundTransaction = walletService.sendRefundToExternalWallet(
                    accountOwner,
                    refundTransaction.refundAmountDigital
                        ?: throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO),
                    refundTransaction.refundDigitalCurrencyType ?: throw BadRequestException(ErrorCodes.INVALID_ASSET_TYPE),
                    refundTransaction.refundWalletAddress ?: throw BadRequestException(ErrorCodes.INVALID_WALLET_ADDRESS),
                    RefundStatus.REFUNDED.name,
                    TransactionType.REFUND,
                    refundTransaction.refundFiatType,
                    refundTransaction.refundAmountFiat,
                    transaction.blockchainTxId,
                    refundTransaction
                )
            }
        } catch (e: Exception) {
            // any exception, Refund Failed
            refundTransaction.refundStatus = RefundStatus.REFUND_FAILED
            refundTransactionsRepository.save(refundTransaction)
        }

        if (refundTransaction.refundStatus == RefundStatus.REFUNDED) {
            sendRefundSuccessEmailAndSmsQAR(refundTransaction, transaction, request)
        } else if (refundTransaction.refundStatus == RefundStatus.REFUND_FAILED) {
            sendRefundFailedEmailAndSms(refundTransaction, transaction, request)
        }

        return transaction.toRefundViewModel(refundTransaction = refundTransaction)
    }

    private fun processAcceptance(
        principal: Authentication,
        accountOwner: AccountOwner,
        request: GetAcceptApproveRejectRequest,
        transaction: Transaction
    ): RefundTransactionViewModel {
        val refundTransaction = transaction.refundTransactions!!.get(0)
        refundTransaction.refundAcceptanceComment = request.rejectReason
        refundTransaction.refundSettlementDate = Instant.now()

        if (refundTransaction.refundStatus == RefundStatus.REFUND_APPROVED && refundTransaction.refundUserEmail != null) {
            /*emailSMSSenderService.sendEmail(
                "Refund Acceptance Acknowledgment Slip",
                emailSMSSenderService.getRefundAcceptanceSlipEmailBody(
                    transaction.tx_id.toString(),
                    transaction.uuid.toString(),
                    transaction.refundAmountFiat,
                    transaction.refundFiatType,
                    transaction.refundAmountDigital,
                    transaction.refundDigitalCurrencyType,
                    transaction.refundSettlementDate ?: Instant.now(),
                    (transaction.receiver.account.owner as Merchant).defaultTimeZone.toString(),
                    transaction.refundWalletAddress.toString()
                ),
                transaction.refundUserEmail.toString(),
                "conatct@wadzpay.com"
            )

            if (transaction.refundUserMobile != null) {
                emailSMSSenderService.sendMobileSMS(
                    transaction.refundUserMobile.toString(),
                    "Refund Acceptance Acknowledgment: Your refund is accepted by Acceptance."
                )
            }*/
        } else if (refundTransaction.refundStatus == RefundStatus.REFUND_CANCELED) {
            if (refundTransaction.refundUserEmail != null) {
                emailSMSSenderService.sendEmail(
                    "Refund Rejected",
                    emailSMSSenderService.getRefundRejectEmailBody(
                        refundTransaction.refundUserName.toString(),
                        request.rejectReason,
                        transaction.tx_id.toString(),
                        transaction.uuid,
                        refundTransaction.refundInitiateDate,
                        refundTransaction.refundAmountFiat
                    ),
                    refundTransaction.refundUserEmail.toString(),
                    "contact@wadzpay.com"
                )
            }

            if (refundTransaction.refundUserMobile != null) {
                emailSMSSenderService.sendMobileSMS(
                    refundTransaction.refundUserMobile.toString(),
                    "Refund Rejected: " + request.rejectReason
                )
            }
        }
        return transactionRepository.save(transaction).toRefundViewModel(refundTransaction = refundTransaction)
    }

    fun getTransactionViewModels(
        accountOwner: AccountOwner,
        listRequest: GetTransactionListRequest
    ): List<TransactionViewModel> {
        val listResponse = getTransactions(accountOwner, listRequest).map {
            it.toViewModel(
                direction = it.getDirection(accountOwner)
            )
        }
        listResponse.forEach { data ->
            data.feeConfigData = getFeeData(data.uuid)
        }
        return listResponse.filter { e -> e.transactionType != TransactionType.SYNC_BLOCKCHAIN }.sortedByDescending { e -> e.createdAt }
    }

    fun getFeeData(transactionUUID: UUID): MutableList<FeeConfigData>? {
        val feeData = transactionWalletFeeDetailsRepository.getByTransactionUuid(transactionUUID.toString())
        val feeConfig: MutableList<FeeConfigData> = mutableListOf()
        if (feeData != null) {
            val activationFee = feeData.filter { e -> e.feeName == IssuanceCommonController.WalletFeeType.WF_001.walletFeeType }
            val loadingFee = feeData.filter { e -> e.feeName == IssuanceCommonController.WalletFeeType.WF_002.walletFeeType }
            val refundUnspentFee = feeData.filter { e -> e.feeName == IssuanceCommonController.WalletFeeType.WF_004.walletFeeType }
            val serviceFee = feeData.filter { e -> e.feeName == IssuanceCommonController.WalletFeeType.WF_003.walletFeeType }
            val walletFee = feeData.filter { e -> e.feeName == IssuanceCommonController.WalletFeeType.WF_005.walletFeeType }
            val initialLoadingFee = feeData.filter { e -> e.feeName == IssuanceCommonController.WalletFeeType.WF_006.walletFeeType }
            val activationFeeTotal = activationFee.sumOf { it.feeAmount!! }
            if (activationFeeTotal > BigDecimal.ZERO && activationFee.isNotEmpty()) {
                feeConfig.add(
                    FeeConfigData(
                        feeAmount = activationFeeTotal.setScale(2, RoundingMode.FLOOR),
                        feeName = IssuanceCommonController.WalletFeeType.WF_001.walletFeeType,
                        currencyUnit = activationFee[0].feeAsset,
                        description = activationFee[0].feeDescription
                    )
                )
            }
            val loadingFeeTotal = loadingFee.sumOf { it.feeAmount!! }
            if (loadingFeeTotal > BigDecimal.ZERO && loadingFee.isNotEmpty()) {
                feeConfig.add(
                    FeeConfigData(
                        feeAmount = loadingFeeTotal.setScale(2, RoundingMode.FLOOR),
                        feeName = IssuanceCommonController.WalletFeeType.WF_002.walletFeeType,
                        currencyUnit = loadingFee[0].feeAsset,
                        description = loadingFee[0].feeDescription
                    )
                )
            }
            val initialLoadingFeeTotal = initialLoadingFee.sumOf { it.feeAmount!! }
            if (initialLoadingFeeTotal > BigDecimal.ZERO && initialLoadingFee.isNotEmpty()) {
                feeConfig.add(
                    FeeConfigData(
                        feeAmount = initialLoadingFeeTotal.setScale(2, RoundingMode.FLOOR),
                        feeName = IssuanceCommonController.WalletFeeType.WF_006.walletFeeType,
                        currencyUnit = initialLoadingFee[0].feeAsset,
                        description = initialLoadingFee[0].feeDescription
                    )
                )
            }
            val refundUnspentFeeTotal = refundUnspentFee.sumOf { it.feeAmount!! }
            if (refundUnspentFeeTotal > BigDecimal.ZERO && refundUnspentFee.isNotEmpty()) {
                feeConfig.add(
                    FeeConfigData(
                        feeAmount = refundUnspentFeeTotal.setScale(2, RoundingMode.FLOOR),
                        feeName = IssuanceCommonController.WalletFeeType.WF_004.walletFeeType,
                        currencyUnit = refundUnspentFee[0].feeAsset,
                        description = refundUnspentFee[0].feeDescription
                    )
                )
            }
            val serviceFeeTotal = serviceFee.sumOf { it.feeAmount!! }
            if (serviceFeeTotal > BigDecimal.ZERO && serviceFee.isNotEmpty()) {
                feeConfig.add(
                    FeeConfigData(
                        feeAmount = serviceFeeTotal.setScale(2, RoundingMode.FLOOR),
                        feeName = IssuanceCommonController.WalletFeeType.WF_003.walletFeeType,
                        currencyUnit = serviceFee[0].feeAsset,
                        description = serviceFee[0].feeDescription
                    )
                )
            }
            val walletFeeTotal = walletFee.sumOf { it.feeAmount!! }
            if (walletFeeTotal > BigDecimal.ZERO && walletFee.isNotEmpty()) {
                feeConfig.add(
                    FeeConfigData(
                        feeAmount = walletFeeTotal.setScale(2, RoundingMode.FLOOR),
                        feeName = IssuanceCommonController.WalletFeeType.WF_005.walletFeeType,
                        currencyUnit = walletFee[0].feeAsset,
                        description = walletFee[0].feeDescription
                    )
                )
            }
            if (feeConfig.isNotEmpty()) {
                return feeConfig
            }
        }
        return null
    }

    fun getTransactions(accountOwner: AccountOwner, listRequest: GetTransactionListRequest): List<Transaction> {
        val specification = and(
            listRequest.toSpecification(accountOwner),
            listRequest.cognitoUsername?.let {
                belongsToAccount(userAccountService.getUserAccountByCognitoUsername(it))
            }
        )
        var result =
            transactionRepository.findAll(
                specification,
                Sort.by(listRequest.sortBy.value)
                    .let { if (listRequest.sortDirection == Sort.Direction.DESC) it.descending() else it }
            )

        if (listRequest.uuidSearch != null) {
            result = result.filter { r -> r.uuid.toString().contains(listRequest.uuidSearch.toString().trim()) }
        }
        if (listRequest.extPosTransactionId != null) {
            result = result.filter { r ->
                r.extPosTransactionId.toString().contains(listRequest.extPosTransactionId.toString().trim())
            }
        }
        if (listRequest.issuanceBanksId != null) {
            result = result.filter { r -> r.issuanceBanks.toString().contains(listRequest.issuanceBanksId.toString().trim()) }
        }

        if (listRequest.appSearch == false && listRequest.fiatAmount != null) {
            var resultFiatAmount: MutableList<Transaction> = mutableListOf()
            resultFiatAmount = transactionRepository.getByFiatAmount(listRequest.fiatAmount)!!
            return resultFiatAmount
        } else if (listRequest.tx_id != null) {
            val resultTransactionId: MutableList<Transaction> = mutableListOf()
            resultTransactionId.clear()
            for (i in result.indices) {
                if (result[i].tx_id != null) {
                    if (listRequest.tx_id.equals(result[i].tx_id)) {
                        resultTransactionId.add(result[i])
                    }
                }
            }
            return resultTransactionId
        }
        if (listRequest.page != null) {
            val pageNo = listRequest.page
            result = result.stream().skip(pageNo * 10L).limit(10).toList() as ArrayList<Transaction>
        }
        return result
    }

    fun getSettlementReport(
        accountOwner: AccountOwner,
        from: Instant,
        to: Instant,
        posId: String?
    ): List<SettlementReport> {
        val logger = LoggerFactory.getLogger(javaClass)
        var trx: List<Transaction>
        if (posId.isNullOrBlank()) {
            trx = getTransactions(
                accountOwner,
                GetTransactionListRequest(
                    direction = TransactionDirection.INCOMING,
                    type = mutableSetOf(TransactionType.POS, TransactionType.ORDER),
                    status = mutableSetOf(
                        TransactionStatus.OVERPAID,
                        TransactionStatus.UNDERPAID,
                        TransactionStatus.SUCCESSFUL
                    ),
                    dateFrom = from,
                    dateTo = to
                )
            )
        } else {
            trx = getTransactions(
                accountOwner,
                GetTransactionListRequest(
                    direction = TransactionDirection.INCOMING,
                    type = mutableSetOf(TransactionType.POS),
                    status = mutableSetOf(
                        TransactionStatus.OVERPAID,
                        TransactionStatus.UNDERPAID,
                        TransactionStatus.SUCCESSFUL
                    ),
                    dateFrom = from,
                    dateTo = to
                )
            )

            val trxPOS: ArrayList<Transaction> = ArrayList()
            trx.map {
                val posTransactionList = posTransactionRepository.getByTransaction(it)
                if (!posTransactionList.isNullOrEmpty()) {
                    if (posTransactionList[0].merchantPos?.posId?.equals(posId, true) == true) {
                        trxPOS.add(it)
                    }
                }
            }
            trx = trxPOS
        }

        return trx.toSettlementReport((accountOwner as Merchant))
    }

    fun getRefundSettlementReport(
        accountOwner: AccountOwner,
        from: Instant,
        to: Instant,
        posId: String?
    ): List<SettlementReport> {
        val logger = LoggerFactory.getLogger(javaClass)
        var trx: List<Transaction>
        if (posId.isNullOrBlank()) {
            trx = getTransactions(
                accountOwner,
                GetTransactionListRequest(
                    type = mutableSetOf(TransactionType.POS, TransactionType.ORDER, TransactionType.REFUND),
                    status = mutableSetOf(
                        TransactionStatus.OVERPAID,
                        TransactionStatus.UNDERPAID,
                        TransactionStatus.SUCCESSFUL,
                        TransactionStatus.REFUNDED
                    ),
                    dateFrom = from,
                    dateTo = to
                )
            )
        } else {
            trx = getTransactions(
                accountOwner,
                GetTransactionListRequest(
                    type = mutableSetOf(TransactionType.POS, TransactionType.REFUND),
                    status = mutableSetOf(
                        TransactionStatus.OVERPAID,
                        TransactionStatus.UNDERPAID,
                        TransactionStatus.SUCCESSFUL,
                        TransactionStatus.REFUNDED
                    ),
                    dateFrom = from,
                    dateTo = to
                )
            )

            val trxPOS: ArrayList<Transaction> = ArrayList()
            trx.map {
                val posTransactionList = posTransactionRepository.getByTransaction(it)
                if (!posTransactionList.isNullOrEmpty()) {
                    if (posTransactionList[0].merchantPos?.posId?.equals(posId, true) == true) {
                        trxPOS.add(it)
                    }
                }
            }
            trx = trxPOS
        }

        return trx.toSettlementReport((accountOwner as Merchant))
    }

    fun getRefundTransactionViewModels(
        accountOwner: AccountOwner,
        request: GetTransactionListRequest
    ): List<RefundTransactionViewModel> {

        // sorting refund table with refund amount
        var sortBy: SortableTransactionFields? = null
        if (request.sortBy == SortableTransactionFields.REFUNDAMOUNTFIAT) {
            request.sortBy = SortableTransactionFields.CREATED_AT
            sortBy = SortableTransactionFields.REFUNDAMOUNTFIAT
        } else if (request.sortBy == SortableTransactionFields.REFUNDAMOUNTDIGITAL) {
            request.sortBy = SortableTransactionFields.CREATED_AT
            sortBy = SortableTransactionFields.REFUNDAMOUNTDIGITAL
        }

        // filtering logical date with refund data, not transaction data
        val refundLogicalDateFrom = request.logicalDateFrom
        val refundLogicalDateTo = request.logicalDateTo
        request.logicalDateFrom = null
        request.logicalDateTo = null

        val transactions = getTransactions(accountOwner, request)
        var result: ArrayList<RefundTransactionViewModel> = ArrayList()

        transactions.forEach {
            // Shows when no filter or Not started in filter and if any refund in-progress... don't show initiate
            if (request.refundType == null && request.refundStatus == null && request.refundMode == null &&
                request.refundTransactionID == null && request.refundPosTransactionID == null &&
                refundLogicalDateFrom == null && refundLogicalDateTo == null
            ) {
                if ((request.refundStatus.isNullOrEmpty() || request.refundStatus?.contains(RefundStatus.NULL) == true) &&
                    (it.refundTransactions?.any { it.refundStatus != RefundStatus.REFUNDED } == false) &&
                    (it.totalFiatReceived?.compareTo(it.totalRefundedAmountFiat) != 0) &&
                    (it.totalDigitalCurrencyReceived != null) &&
                    request.isAcceptancePage == false
                ) {
                    result.add(it.toRefundViewModel(direction = it.getDirection(accountOwner), refundTransaction = null))
                }
            }

            if ((request.refundStatus?.contains(RefundStatus.NULL) == true) &&
                (it.refundTransactions?.any { it.refundStatus != RefundStatus.REFUNDED } == false) &&
                (it.totalFiatReceived?.compareTo(it.totalRefundedAmountFiat) != 0) &&
                (it.totalDigitalCurrencyReceived != null) &&
                request.isAcceptancePage == false
            ) {
                result.add(it.toRefundViewModel(direction = it.getDirection(accountOwner), refundTransaction = null))
            }

            val filteredRefunds: List<TransactionRefundDetails>? = it.refundTransactions?.filter {
                ((request.refundStatus == null) || (request.refundStatus?.contains(it.refundStatus) == true)) &&
                    ((request.refundMode == null) || (request.refundMode?.contains(it.refundMode) == true)) &&
                    ((request.refundType == null) || (request.refundType?.contains(it.refundType) == true)) &&
                    ((request.refundTransactionID == null) || (it.uuid.contains(request.refundTransactionID))) &&
                    ((request.refundPosTransactionID == null) || (it.extPosTransactionIdRefund?.contains(request.refundPosTransactionID) == true)) &&
                    ((refundLogicalDateFrom == null) || (it.extPosLogicalDateRefund?.isAfter(refundLogicalDateFrom)) == true) &&
                    ((refundLogicalDateTo == null) || (it.extPosLogicalDateRefund?.isBefore(refundLogicalDateTo)) == true)
            }
            filteredRefunds?.stream()?.sorted(Comparator.comparing(TransactionRefundDetails::numberOfRefunds))?.forEach {
                result.add(it.transaction.toRefundViewModel(direction = it.transaction.getDirection(accountOwner), refundTransaction = it))
            }
        }

        if (sortBy != null) {
            if (sortBy == SortableTransactionFields.REFUNDAMOUNTFIAT) {
                if (request.sortDirection == Sort.Direction.DESC) {
                    result.sortByDescending { it.refundFiatAmount }
                } else {
                    result.sortBy { it.refundFiatAmount }
                }
            }
            if (sortBy == SortableTransactionFields.REFUNDAMOUNTDIGITAL) {
                if (request.sortDirection == Sort.Direction.DESC) {
                    result.sortByDescending { it.refundAmountDigital }
                } else {
                    result.sortBy { it.refundAmountDigital }
                }
            }
        }

        if (request.page != null) {
            val pageNo = request.page
            result = result.stream().skip(pageNo * 10L).limit(10).toList() as ArrayList<RefundTransactionViewModel>
        }

        var result1: ArrayList<RefundTransactionViewModel> = ArrayList()
        if (request.posId != null) {
            result.forEach {
                if (it.posId == request.posId) {
                    result1.add(it)
                }
            }
        } else {
            result.forEach {
                result1.add(it)
            }
        }

        return result1
    }

    @Transactional(rollbackForClassName = ["BadRequestException"])
    fun initiateWebLinkRefund(userAccount: AccountOwner, request: InitiateWebLinkRefund, refundOrigin: RefundOrigin): RefundToken {
        val transaction = getByIdAndOwner(request.transactionId, userAccount)

        if (isAnyRefundInQueue(transaction, request.isReInitiateRefund) == true) {
            throw BadRequestException(ErrorCodes.REFUND_ALREADY_EXISTS_ERROR)
        }

        if (isFullyRefunded(transaction)) {
            throw BadRequestException(ErrorCodes.ALREADY_FULLY_REFUNDED)
        }

        if (request.refundFiatType != transaction.fiatAsset) {
            throw BadRequestException(ErrorCodes.REFUND_FIAT_TYPE_AND_TRANSACTION_FIAT_TYPE_SHOULD_BE_SAME)
        }

        if (request.refundAmountFiat.compareTo(transaction.totalFiatReceived) == 1) {
            throw BadRequestException(ErrorCodes.REFUND_AMOUNT_CANT_BE_GREATER_THAN_RECEIVED_AMOUNT)
        }

        if (request.refundAmountFiat.compareTo(balanceRefundedAmountFiat(transaction)) == 1) {
            throw BadRequestException(ErrorCodes.REFUND_AMOUNT_CANT_BE_GREATER_THAN_BALANCE_AMOUNT)
        }
        // blocking usdt refund in dev and test env
        // commenting as we can intiate refund for any digital currency
        /*
        if (appConfig.environment.equals("dev", true) || appConfig.environment.equals("test", true)) {
            if (request.refundDigitalType != CurrencyUnit.BTC) {
                throw BadRequestException(ErrorCodes.REFUND_IN_TEST_AND_DEV_SHOULD_BE_BTC)
            }
        } else {
            if (request.refundDigitalType != CurrencyUnit.USDT) {
                throw BadRequestException(ErrorCodes.REFUND_DIGITAL_CURRENCY_TYPE_SHOULD_BE_USDT)
            }
        }*/

        if (request.refundAmountDigital.stripTrailingZeros().scale() > 8) {
            val am =
                request.refundAmountDigital.setScale(8, RoundingMode.UP)
                    .stripTrailingZeros()
            request.refundAmountDigital = am
        }

        val refundTransaction: TransactionRefundDetails

        if (request.isReInitiateRefund == true) {
            refundTransaction = refundTransactionsRepository.findByUuid(request.refundTransactionID) ?: throw BadRequestException(ErrorCodes.REFUND_NOT_FOUND)
            refundTransaction.refundStatus = RefundStatus.REFUND_INITIATED
            refundTransaction.refundInitiatedFrom = (userAccount as Merchant).id
            refundTransaction.refundUserMobile = request.refundUserMobile ?: refundTransaction.refundUserMobile
            refundTransaction.refundUserEmail = request.refundUserEmail ?: refundTransaction.refundUserEmail
            refundTransaction.lastUpdatedOn = Instant.now()
            refundTransaction.refundInitiateDate = Instant.now()
            refundTransaction.isRefundReinitiate = true
            refundTransaction.refundOrigin = refundOrigin.toString()
        } else {
            val count = getNoOfRefunds(transaction) ?: 1
            refundTransaction = refundTransactionsRepository.save(
                TransactionRefundDetails(
                    transaction = transaction,
                    type = TransactionType.REFUND,
                    refundUserMobile = request.refundUserMobile,
                    refundUserEmail = request.refundUserEmail,
                    refundUserName = request.refundUserName,
                    refundStatus = if (request.refundMode == RefundMode.CASH) RefundStatus.REFUNDED else RefundStatus.REFUND_INITIATED,
                    uuid = if (request.refundMode == RefundMode.CASH) (transaction.uuid.toString() + "-" + count) else UUID.randomUUID().toString(),
                    refundSettlementDate = if (request.refundMode == RefundMode.CASH) Instant.now() else null,
                    refundInitiateDate = Instant.now(),
                    refundAmountFiat = request.refundAmountFiat,
                    refundFiatType = request.refundFiatType,
                    refundInitiatedFrom = (userAccount as Merchant).id,
                    refundMode = request.refundMode,
                    refundAmountDigital = request.refundAmountDigital,
                    refundDateTime = Instant.now(),
                    extPosIdRefund = request.extPosIdRefund,
                    extPosTransactionIdRefund = request.extPosTransactionIdRefund,
                    extPosLogicalDateRefund = request.extPosLogicalDateRefund,
                    extPosShiftRefund = request.extPosShiftRefund,
                    extPosActualDateRefund = request.extPosActualDateRefund,
                    extPosSequenceNoRefund = request.extPosSequenceNoRefund,
                    extPosActualTimeRefund = request.extPosActualTimeRefund,
                    refundDigitalCurrencyType = if (appConfig.environment.equals("dev", true) || appConfig.environment.equals("test", true)) {
                        CurrencyUnit.BTC
                    } else {
                        CurrencyUnit.USDT
                    },
                    refundType = if (request.refundAmountFiat >= transaction.totalFiatReceived) RefundType.FULL else RefundType.PARTIAL,
                    createdAt = Instant.now(),
                    lastUpdatedOn = Instant.now(),
                    numberOfRefunds = count,
                    refundOrigin = refundOrigin.value,
                    refundableAmountFiat = transaction.totalFiatReceived?.minus(transaction.totalRefundedAmountFiat)
                )
            )
        }

        val refundTx = refundTransactionsRepository.save(refundTransaction)

        if (request.refundMode == RefundMode.CASH) {
            transaction.totalRefundedAmountFiat = transaction.totalRefundedAmountFiat.add(refundTransaction.refundAmountFiat)
            transactionRepository.save(transaction)
            return RefundToken()
        }

        if (request.isReInitiateRefund == true) {
            refundTokenRepository.updateTokenToExpire(refundTransaction.id)
        }
        val webLinkConfig = merchantConfigRepository.findByMerchantId(refundTransaction.refundInitiatedFrom!!)

        var refundToken = RefundToken(
            validFor = Instant.now().plusSeconds(webLinkConfig?.resendExpiredWeblinkSeconds!!),
            refundTransaction = refundTx
        )
        refundToken = refundTokenRepository.save(refundToken)
        refundTx.refundToken = refundToken
        refundTransactionsRepository.save(refundTx)

        if (request.refundUserEmail != null) {
            try {
                emailSMSSenderService.sendEmail(
                    "Refund Verification",
                    emailSMSSenderService.getMerchantEmailBody(
                        userAccount.primaryContactFullName + " (" + userAccount.name + ")",
                        request.refundCustomerFormUrl + refundToken.transactionRefundToken,
                        refundTransaction, transaction
                    ),
                    request.refundUserEmail!!,
                    "contact@wadzpay.com"
                )
            } catch (e: Exception) {
                println("refund email - @705 - ${e.message} $e ")
                throw BadRequestException(ErrorCodes.INVALID_EMAIL)
            }
        }

        if (request.refundUserMobile != null) {
            try {
                val strRefundDate = refundDate() + ""

                val strSmsText: String = "Dear customer,\n" +
                    "Please click below link to complete the refund request of ${request.refundFiatType} ${request.refundAmountFiat.stripTrailingZeros().toPlainString()} for the transaction ID ${request.transactionId} " +
                    "at Dubai Duty Free on $strRefundDate ." +
                    "\n" +
                    "URL:-" + request.refundCustomerFormUrl + refundToken.transactionRefundToken

                emailSMSSenderService.sendMobileSMS(
                    request.refundUserMobile!!,
                    strSmsText
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return refundToken
    }

    private fun refundDate(): String? {
        val date = Date()
        val dateFmt = SimpleDateFormat("dd-MM-yyyy")
        return dateFmt.format(date)
    }

    fun submitRefundForm(userAccount: AccountOwner, request: RefundInitiationRequest): TransactionRefundDetails {
        val transaction = getByIdAndOwner(request.transactionId, userAccount)
        if (request.refundAmountFiat <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (request.refundAmountDigital <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (isAnyRefundInQueue(transaction, request.isReInitiateRefund) == true) {
            throw BadRequestException(ErrorCodes.REFUND_ALREADY_EXISTS_ERROR)
        }
        return createOrUpdateRefundTransaction(transaction, request, null)
    }

    private fun createOrUpdateRefundTransaction(transaction: Transaction, request: RefundInitiationRequest, _refundTransaction: TransactionRefundDetails?): TransactionRefundDetails {
        var refundTransaction = _refundTransaction
        if (appConfig.environment.equals("uat", true) ||
            appConfig.environment.equals("ddf_uat", true) ||
            appConfig.environment.equals("ddf_prod", true)
        ) {
            if (!request.refundWalletAddress.startsWith("0x") || request.refundWalletAddress.length != 42) {
                throw BadRequestException(ErrorCodes.UNSUPPORTED_ETH_OR_USDT_WALLET_ADDRESS)
            }
        }

        if (request.refundAmountFiat.compareTo(transaction.totalFiatReceived) == 1) {
            throw BadRequestException(ErrorCodes.REFUND_AMOUNT_CANT_BE_GREATER_THAN_RECEIVED_AMOUNT)
        }

        if (request.refundAmountFiat.compareTo(balanceRefundedAmountFiat(transaction)) == 1) {
            throw BadRequestException(ErrorCodes.REFUND_AMOUNT_CANT_BE_GREATER_THAN_BALANCE_AMOUNT)
        }

        if (request.refundAmountFiat > transaction.totalFiatReceived) {
            throw BadRequestException(ErrorCodes.CANNOT_REFUND_MORE_THAN_RECEIVED_FIAT)
        }

        if (request.refundAmountDigital.stripTrailingZeros().scale() > 8) {
            val am =
                request.refundAmountDigital.setScale(8, RoundingMode.UP)
                    .stripTrailingZeros()
            request.refundAmountDigital = am
        }

        if (refundTransaction == null) { // SubmitRefundForm
            if (request.isReInitiateRefund == true) {
                refundTransaction = refundTransactionsRepository.findByUuid(request.refundTransactionID) ?: throw BadRequestException(ErrorCodes.REFUND_NOT_FOUND)
                refundTransaction.refundStatus = RefundStatus.REFUND_ACCEPTED
                refundTransaction.walletAddressMatch = transaction.sourceWalletAddress.equals(request.sourceWalletAddress, false)
                refundTransaction.refundUserMobile = request.refundUserMobile ?: refundTransaction.refundUserMobile
                refundTransaction.refundUserEmail = request.refundUserEmail ?: refundTransaction.refundUserEmail
                refundTransaction.lastUpdatedOn = Instant.now()
                refundTransaction.refundInitiateDate = Instant.now()
            } else {
                refundTransaction = TransactionRefundDetails(
                    transaction = transaction,
                    type = TransactionType.REFUND,
                    refundStatus = RefundStatus.REFUND_ACCEPTED,
                    walletAddressMatch = transaction.sourceWalletAddress.equals(request.sourceWalletAddress, false),
                    numberOfRefunds = getNoOfRefunds(transaction) ?: 1,
                    refundOrigin = RefundOrigin.MERCHANT_DASHBOARD.value,
                    refundableAmountFiat = transaction.totalFiatReceived?.minus(transaction.totalRefundedAmountFiat),
                    refundType = if (request.refundAmountFiat >= transaction.totalFiatReceived) RefundType.FULL else RefundType.PARTIAL,
                    refundInitiateDate = Instant.now(),
                    createdAt = Instant.now(),
                    lastUpdatedOn = Instant.now()
                )
            }
        } else { // RefundForm, initiate->RefundForm
            refundTransaction.refundStatus = RefundStatus.REFUND_ACCEPTED
            refundTransaction.walletAddressMatch = transaction.sourceWalletAddress.equals(request.sourceWalletAddress, false)
            refundTransaction.lastUpdatedOn = Instant.now()
        }
        refundTransaction.refundUserMobile = request.refundUserMobile ?: refundTransaction.refundUserMobile
        refundTransaction.refundUserEmail = request.refundUserEmail ?: refundTransaction.refundUserEmail
        refundTransaction.refundAmountFiat = request.refundAmountFiat
        refundTransaction.refundFiatType = request.refundFiatType
        refundTransaction.refundReason = request.reasonForRefund
        refundTransaction.refundAmountDigital = request.refundAmountDigital
        refundTransaction.refundWalletAddress = request.refundWalletAddress
        refundTransaction.sourceWalletAddress = request.sourceWalletAddress
        refundTransaction.refundUserName = request.refundUserName
        refundTransaction.refundMode = request.refundMode
        refundTransaction.extPosLogicalDateRefund = request.extPosLogicalDate ?: refundTransaction.extPosLogicalDateRefund
        refundTransaction.extPosShiftRefund = request.extPosShift ?: refundTransaction.extPosShiftRefund
        refundTransaction.extPosActualDateRefund = request.extPosActualDate ?: refundTransaction.extPosActualDateRefund
        refundTransaction.extPosActualTimeRefund = request.extPosActualTime ?: refundTransaction.extPosActualTimeRefund
        refundTransaction.refundDateTime = Instant.now()
        refundTransaction.lastUpdatedOn = Instant.now()
        refundTransaction.refundDigitalCurrencyType = if (appConfig.environment.equals("dev", true) ||
            appConfig.environment.equals("test", true)
        ) {
            CurrencyUnit.BTC
        } else {
            CurrencyUnit.USDT
        }

        refundTransaction = refundTransactionsRepository.save(refundTransaction)

        // Send EMAIL refund-ack-slip
        if (refundTransaction.refundUserEmail != null) {
            emailSMSSenderService.sendEmail(
                "Refund Acknowledgment Slip",
                emailSMSSenderService.getRefundAckSlipEmailBody(

                    transaction.tx_id.toString(),
                    transaction.uuid.toString(),
                    refundTransaction.refundAmountFiat,
                    refundTransaction.refundFiatType,
                    refundTransaction.refundAmountDigital,
                    refundTransaction.refundDigitalCurrencyType,
                    refundTransaction.refundDateTime ?: Instant.now(),
                    (transaction.receiver.account.owner as Merchant).defaultTimeZone.toString()
                ),
                refundTransaction.refundUserEmail.toString(), "contact@wadzpay.com"
            )
        }

        if (request.refundUserMobile != null) {
            val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            formatter.timeZone =
                TimeZone.getTimeZone("GMT" + (transaction.receiver.account.owner as Merchant).defaultTimeZone.toString())
            val strRefundDate = formatter.format(Date.from(Instant.now()))

            val strSmsText: String = "Dear Customer,\n" +
                "We acknowledge the refund request initiated for transaction ID  ${request.transactionId} on $strRefundDate for refund amount of ${request.refundFiatType} ${request.refundAmountFiat.stripTrailingZeros().toPlainString()} " +
                "Once approved, the refund amount will be credited in your designated ${request.refundDigitalType} wallet."
            emailSMSSenderService.sendMobileSMS(
                request.refundUserMobile!!,
                strSmsText
            )
        }

        return refundTransaction
    }

    fun submitRefundForm(request: RefundInitiationRequest): TransactionRefundDetails {
        val transaction = transactionRepository.getByUuid(request.transactionId) ?: throw EntityNotFoundException(
            ErrorCodes.TRANSACTION_NOT_FOUND
        )

        println("refund refundToken  : " + request.refundToken)

        val refundTrx = refundTokenRepository.findByTransactionRefundToken(UUID.fromString(request.refundToken))
            ?: throw BadRequestException("Refund Transaction is Not Found!")

        if (refundTrx.isExpired == true) {
            throw BadRequestException(ErrorCodes.TOKEN_EXPIRED)
        }

        val refundTransaction = transaction.refundTransactions?.find { it.refundStatus == RefundStatus.REFUND_INITIATED }
            ?: throw BadRequestException(ErrorCodes.NO_REFUND_REQUEST_FOUND_ERROR)

        return createOrUpdateRefundTransaction(transaction, request, refundTransaction)
    }

    fun getRefundTransactionDetailsFromToken(refundToken: UUID): RefundTransactionViewModel? {
        val refundTrx = refundTokenRepository.findByTransactionRefundToken(refundToken)
            ?: throw BadRequestException("Refund Transaction With $refundToken Not Found!")

        if (refundTrx.validFor.isAfter(Instant.now()) && refundTrx.refundTransaction?.refundStatus == RefundStatus.REFUND_INITIATED) {
            return refundTrx.refundTransaction?.transaction?.toRefundViewModel(refundTransaction = refundTrx.refundTransaction!!)
        } else {
            throw BadRequestException(ErrorCodes.TOKEN_EXPIRED)
        }
    }

    fun getRefundFormFields(formName: String): RefundFormFields? {
        return refundFormRepository.findByFormName(formName)
    }

    fun submitRefundFormFields(request: RefundFormFields) {
        refundFormRepository.save(request)
    }

    fun sendRefundSuccessEmailAndSms(refundTransaction: TransactionRefundDetails, transaction: Transaction, request: GetAcceptApproveRejectRequest) {
        if (refundTransaction.refundUserName == null || refundTransaction.refundUserName!!.trim() == "") {
            refundTransaction.refundUserName = "Customer"
        }
        if (refundTransaction.refundUserEmail != null && refundTransaction.refundUserEmail?.trim() != "") {
            emailSMSSenderService.sendEmail(
                "Refunded Successfully",
                emailSMSSenderService.getRefundSuccessEmailBody(
                    refundTransaction.refundUserName.toString(),
                    refundTransaction.refundWalletAddress.toString(),
                    (transaction.receiver.account.owner as Merchant).primaryContactPhoneNumber.toString(),
                    (transaction.receiver.account.owner as Merchant).primaryContactEmail.toString(),
                    request,
                    transaction,
                    (transaction.receiver.account.owner as Merchant).defaultTimeZone.toString(),
                    refundTransaction
                ),
                refundTransaction.refundUserEmail.toString(),
                "contact@wadzpay.com"
            )
        }
        if (refundTransaction.refundUserMobile != null && refundTransaction.refundUserMobile?.trim() != "") {
            val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            formatter.timeZone =
                TimeZone.getTimeZone("GMT" + (transaction.receiver.account.owner as Merchant).defaultTimeZone.toString())
            val strRefundDate = formatter.format(Date.from(Instant.now()))
            if (refundTransaction.refundUserName == null || refundTransaction.refundUserName!!.trim() == "") {
                refundTransaction.refundUserName = "Customer"
            }

            val strSmsText: String = "Dear " + refundTransaction.refundUserName + ",\n" +
                "We are happy to inform that your refund request for Transaction ID ${request.txn_uuid}  on $strRefundDate for ${transaction.fiatAsset} ${refundTransaction.refundAmountFiat?.stripTrailingZeros()?.toPlainString()} has been successfully processed.\n" +
                "\n" +
                "Your refund amount is transferred to your Wallet ID ${refundTransaction.refundWalletAddress}"

            emailSMSSenderService.sendMobileSMS(
                refundTransaction.refundUserMobile.toString(),
                strSmsText
            )
        }
    }

    fun sendRefundSuccessEmailAndSmsQAR(refundTransaction: TransactionRefundDetails, transaction: Transaction, request: GetAcceptApproveRejectRequest) {
        if (refundTransaction.refundUserName == null || refundTransaction.refundUserName!!.trim() == "") {
            refundTransaction.refundUserName = "Customer"
        }
        if (refundTransaction.refundUserEmail != null && refundTransaction.refundUserEmail?.trim() != "") {
            emailSMSSenderService.sendEmail(
                "Refunded Successfully",
                emailSMSSenderService.getRefundSuccessEmailBodyQAR(
                    refundTransaction.refundUserName.toString(),
                    refundTransaction.refundWalletAddress.toString(),
                    (transaction.receiver.account.owner as Merchant).primaryContactPhoneNumber.toString(),
                    (transaction.receiver.account.owner as Merchant).primaryContactEmail.toString(),
                    request,
                    transaction,
                    (transaction.receiver.account.owner as Merchant).defaultTimeZone.toString(),
                    refundTransaction
                ),
                refundTransaction.refundUserEmail.toString(),
                "contact@wadzpay.com"
            )
        }
        if (refundTransaction.refundUserMobile != null && refundTransaction.refundUserMobile?.trim() != "") {
            val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            formatter.timeZone =
                TimeZone.getTimeZone("GMT" + (transaction.receiver.account.owner as Merchant).defaultTimeZone.toString())
            val strRefundDate = formatter.format(Date.from(Instant.now()))
            if (refundTransaction.refundUserName == null || refundTransaction.refundUserName!!.trim() == "") {
                refundTransaction.refundUserName = "Customer"
            }

            val strSmsText: String = "Dear " + refundTransaction.refundUserName + ",\n" +
                "We are happy to inform that your refund request for Transaction ID ${request.txn_uuid}  on $strRefundDate for QAR ${refundTransaction.refundAmountFiat?.stripTrailingZeros()?.toPlainString()} has been successfully processed.\n" +
                "\n" +
                "Your refund amount is transferred to your Wallet ID ${refundTransaction.refundWalletAddress}"

            emailSMSSenderService.sendMobileSMS(
                refundTransaction.refundUserMobile.toString(),
                strSmsText
            )
        }
    }
    fun sendRefundFailedEmailAndSms(refundTransaction: TransactionRefundDetails, transaction: Transaction, request: GetAcceptApproveRejectRequest) {
        if (refundTransaction.refundUserEmail != null) {
            emailSMSSenderService.sendEmail(
                "Refund Failed",
                emailSMSSenderService.getRefundFailedEmailBody(
                    refundTransaction.refundUserName.toString(),
                    request.rejectReason,
                    transaction.tx_id.toString(),
                    transaction.uuid,
                    refundTransaction.refundInitiateDate,
                    refundTransaction.refundAmountFiat
                ),
                refundTransaction.refundUserEmail.toString(),
                "contact@wadzpay.com"
            )
        }
        if (refundTransaction.refundUserMobile != null) {
            emailSMSSenderService.sendMobileSMS(
                refundTransaction.refundUserMobile.toString(),
                "Refund Failed: Transaction failed from Blockchain. Kindly contact us."
            )
        }
    }

    fun sendRefundRejectedEmailAndSms(refundTransaction: TransactionRefundDetails, transaction: Transaction, request: GetAcceptApproveRejectRequest) {
        if (refundTransaction.refundUserEmail != null) {
            emailSMSSenderService.sendEmail(
                "Refund Rejected",
                emailSMSSenderService.getRefundRejectEmailBody(
                    refundTransaction.refundUserName.toString(),
                    request.rejectReason,
                    transaction.tx_id.toString(),
                    transaction.uuid,
                    refundTransaction.refundInitiateDate,
                    refundTransaction.refundAmountFiat
                ),
                refundTransaction.refundUserEmail.toString(),
                "contact@wadzpay.com"
            )
        }
        if (refundTransaction.refundUserMobile != null) {
            emailSMSSenderService.sendMobileSMS(
                refundTransaction.refundUserMobile.toString(),
                "Refund Rejected: " + request.rejectReason
            )
        }
    }

    fun getTransaction(uuid: UUID): Transaction {
        return transactionRepository.getByUuid(uuid)
            ?: throw BadRequestException("Refund Transaction With $uuid Not Found!")
    }

    fun isAnyRefundInQueue(transaction: Transaction, reInitiateRefund: Boolean?): Boolean? {
        var isAnyRefundInQueue: Boolean? = false
        isAnyRefundInQueue = if (reInitiateRefund == null || reInitiateRefund == false) {
            transaction.refundTransactions?.any {
                it.refundStatus != RefundStatus.REFUNDED
            }
        } else {
            transaction.refundTransactions?.any {
                it.refundStatus != RefundStatus.REFUNDED &&
                    !(
                        it.refundStatus == RefundStatus.REFUND_FAILED ||
                            it.refundStatus == RefundStatus.REFUND_EXPIRED ||
                            it.refundStatus == RefundStatus.REFUND_CANCELED
                        )
            }
        }
        return isAnyRefundInQueue
    }

    fun isFullyRefunded(transaction: Transaction): Boolean {
        return transaction.totalFiatReceived == transaction.totalRefundedAmountFiat
    }
    fun balanceRefundedAmountFiat(transaction: Transaction): BigDecimal {
        return transaction.totalFiatReceived?.minus(transaction.totalRefundedAmountFiat) ?: BigDecimal.ZERO
    }

    fun getRefundTransactionWaitingForApproval(transaction: Transaction): TransactionRefundDetails? {
        return transaction.refundTransactions?.find {
            it.refundStatus == RefundStatus.REFUND_ACCEPTED || it.refundStatus == RefundStatus.REFUND_HOLD || it.refundStatus == RefundStatus.REFUND_INITIATED
        }
    }

    fun getRefundTransactionWaitingForHoldUnHold(transaction: Transaction): TransactionRefundDetails? {
        return transaction.refundTransactions?.find {
            (it.refundStatus == RefundStatus.REFUND_ACCEPTED || it.refundStatus == RefundStatus.REFUND_HOLD)
        }
    }

    fun countOfSuccessRefunds(transaction: Transaction): Int {
        return transaction.refundTransactions?.filter { it.refundStatus == RefundStatus.REFUNDED }?.size ?: 0
    }

    fun getNoOfRefunds(transaction: Transaction): Int? {
        return if (transaction.refundTransactions != null && transaction.refundTransactions?.isEmpty() == false)
            (transaction.refundTransactions?.size?.plus(1)) else 1
    }

    fun getMerchantConfigs(): ArrayList<MerchantConfigResponse> {
        val configs = merchantConfigRepository.findAll()
        val response = ArrayList<MerchantConfigResponse>()
        configs.forEach {
            response.add(
                MerchantConfigResponse(
                    id = it.id!!,
                    merchantId = it.merchant!!.id,
                    autoRefundApproveSeconds = it.autoRefundApproveSeconds,
                    resendExpiredWeblinkSeconds = it.resendExpiredWeblinkSeconds,
                    resendExpiredWeblinkLimitCount = it.resendExpiredWeblinkLimitCount,
                    autoRefundApprovalRequired = it.autoRefundApprovalRequired,
                    autoSendWeblinkRequired = it.autoSendWeblinkRequired,
                    description = it.description,
                    resendThresholdMaxSeconds = it.resendThresholdMaxSeconds,
                    createdBy = it.createdBy
                )
            )
        }
        return response
    }

    fun createOrUpdateDDFRefundConfig(request: MerchantConfigRequest): MerchantConfigResponse {
        val merchantOpt = merchantRepository.findById(request.merchantId)

        val merchant = merchantOpt.get()

        var merchantConfig: MerchantConfig?
        merchantConfig = merchantConfigRepository.findByMerchantId(request.merchantId)
        if (merchantConfig != null) {
            merchantConfig.autoRefundApproveSeconds = request.autoRefundApproveSeconds
            merchantConfig.resendExpiredWeblinkSeconds = request.resendExpiredWeblinkSeconds
            merchantConfig.resendExpiredWeblinkLimitCount = request.resendExpiredWeblinkLimitCount
            merchantConfig.autoRefundApprovalRequired = request.autoRefundApprovalRequired
            merchantConfig.autoSendWeblinkRequired = request.autoSendWeblinkRequired
            merchantConfig.description = request.description
            merchantConfig.resendThresholdMaxSeconds = request.resendThresholdMaxSeconds
            merchantConfig = merchantConfigRepository.save(merchantConfig)
        } else {
            merchantConfig = merchantConfigRepository.save(
                MerchantConfig(
                    merchant = merchant,
                    autoRefundApproveSeconds = request.autoRefundApproveSeconds,
                    resendExpiredWeblinkSeconds = request.resendExpiredWeblinkSeconds,
                    resendExpiredWeblinkLimitCount = request.resendExpiredWeblinkLimitCount,
                    autoRefundApprovalRequired = request.autoRefundApprovalRequired,
                    autoSendWeblinkRequired = request.autoSendWeblinkRequired,
                    description = request.description,
                    resendThresholdMaxSeconds = request.resendThresholdMaxSeconds,
                    createdBy = request.createdBy
                )
            )
        }
        return MerchantConfigResponse(
            id = merchantConfig.id!!,
            merchantId = merchantConfig.merchant!!.id,
            autoRefundApproveSeconds = merchantConfig.autoRefundApproveSeconds,
            resendExpiredWeblinkSeconds = merchantConfig.resendExpiredWeblinkSeconds,
            resendExpiredWeblinkLimitCount = merchantConfig.resendExpiredWeblinkLimitCount,
            autoRefundApprovalRequired = merchantConfig.autoRefundApprovalRequired,
            autoSendWeblinkRequired = merchantConfig.autoSendWeblinkRequired,
            description = merchantConfig.description,
            resendThresholdMaxSeconds = request.resendThresholdMaxSeconds,
            createdBy = merchantConfig.createdBy
        )
    }

    fun updateOneLevelApproveRejectTransactionAlgo(
        request: GetAcceptApproveRejectRequest,
        accountOwner: AccountOwner,
        principal: Authentication?
    ): RefundTransactionViewModel? {
        println("@1140 updateOneLevelApproveRejectTransactionAlgo")

        val transaction = getByIdAndOwner(request.txn_uuid, accountOwner)
        val refundTransaction: TransactionRefundDetails
        if (isFullyRefunded(transaction)) {
            throw BadRequestException(ErrorCodes.TRANSACTION_IS_ALREADY_SETTLED_NOT_REFUND_AGAIN)
        }

        return when (request.type) {
            RefundAcceptRejectType.APPROVAL -> {

                refundTransaction = getRefundTransactionWaitingForApproval(transaction) ?: throw BadRequestException(
                    ErrorCodes.NO_REFUND_TRANSACTION_FOR_APPROVAL
                )
                // Approved by supervisor
                refundTransaction.refundStatus = if (request.status == RefundAcceptRejectStatus.ACCEPT) {
                    RefundStatus.REFUND_APPROVED
                } else {
                    RefundStatus.REFUND_CANCELED
                }
                processApprovalAlgo(accountOwner, request, transaction, refundTransaction, principal)
            }

            RefundAcceptRejectType.HOLD -> {
                refundTransaction = getRefundTransactionWaitingForHoldUnHold(transaction) ?: throw BadRequestException(
                    ErrorCodes.NO_REFUND_TRANSACTION_FOR_HOLD
                )
                processHold(refundTransaction)
            }

            else -> {
                null
            }
        }
    }

    fun updateOneLevelApproveRejectTransactionAlgoQAR(
        request: GetAcceptApproveRejectRequest,
        accountOwner: AccountOwner,
        principal: Authentication?
    ): RefundTransactionViewModel? {
        println("@1140 updateOneLevelApproveRejectTransactionAlgo")

        val transaction = getByIdAndOwner(request.txn_uuid, accountOwner)
        val refundTransaction: TransactionRefundDetails
        if (isFullyRefunded(transaction)) {
            throw BadRequestException(ErrorCodes.TRANSACTION_IS_ALREADY_SETTLED_NOT_REFUND_AGAIN)
        }

        return when (request.type) {
            RefundAcceptRejectType.APPROVAL -> {

                refundTransaction = getRefundTransactionWaitingForApproval(transaction) ?: throw BadRequestException(
                    ErrorCodes.NO_REFUND_TRANSACTION_FOR_APPROVAL
                )
                // Approved by supervisor
                refundTransaction.refundStatus = if (request.status == RefundAcceptRejectStatus.ACCEPT) {
                    RefundStatus.REFUND_APPROVED
                } else {
                    RefundStatus.REFUND_CANCELED
                }
                processApprovalAlgoQAR(accountOwner, request, transaction, refundTransaction, principal)
            }

            RefundAcceptRejectType.HOLD -> {
                refundTransaction = getRefundTransactionWaitingForHoldUnHold(transaction) ?: throw BadRequestException(
                    ErrorCodes.NO_REFUND_TRANSACTION_FOR_HOLD
                )
                processHold(refundTransaction)
            }

            else -> {
                null
            }
        }
    }

    private fun processApprovalAlgo(
        accountOwner: AccountOwner,
        request: GetAcceptApproveRejectRequest,
        transaction: Transaction,
        _refundTransaction: TransactionRefundDetails,
        principal: Authentication?
    ): RefundTransactionViewModel {
        println("@1181 processApprovalAlgo")
        var refundTransaction = _refundTransaction
        refundTransaction.refundAcceptanceComment = request.rejectReason
        refundTransaction.refundSettlementDate = Instant.now()
        refundTransaction.refundDateTime = Instant.now()
        refundTransaction.lastUpdatedOn = Instant.now()
        if (request.status == RefundAcceptRejectStatus.REJECT) {
            refundTransaction.refundStatus = RefundStatus.REFUND_CANCELED
            refundTransactionsRepository.save(refundTransaction)
            sendRefundRejectedEmailAndSms(refundTransaction, transaction, request)
            return transaction.toRefundViewModel(refundTransaction = refundTransaction)
        }

        // if refund asset is USDT
        try {
            refundTransaction = walletService.sendRefundToExternalWalletAlgo(
                accountOwner,
                refundTransaction.refundAmountDigital
                    ?: throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO),
                refundTransaction.refundDigitalCurrencyType
                    ?: throw BadRequestException(ErrorCodes.INVALID_ASSET_TYPE),
                refundTransaction.refundWalletAddress
                    ?: throw BadRequestException(ErrorCodes.INVALID_WALLET_ADDRESS),
                RefundStatus.REFUNDED.name,
                TransactionType.REFUND,
                refundTransaction.refundFiatType,
                refundTransaction.refundAmountFiat,
                transaction.blockchainTxId,
                refundTransaction,
                principal,
                transaction
            )
        } catch (e: Exception) {
            // any exception, Refund Failed
            println("@1214 $e")
            throw BadRequestException("Error: $e.toString()")
            refundTransaction.refundStatus = RefundStatus.REFUND_FAILED
            refundTransactionsRepository.save(refundTransaction)
        }

        if (refundTransaction.refundStatus == RefundStatus.REFUNDED) {
            sendRefundSuccessEmailAndSms(refundTransaction, transaction, request)
        } else if (refundTransaction.refundStatus == RefundStatus.REFUND_FAILED) {
            sendRefundFailedEmailAndSms(refundTransaction, transaction, request)
        }

        return transaction.toRefundViewModel(refundTransaction = refundTransaction)
    }

    private fun processApprovalAlgoQAR(
        accountOwner: AccountOwner,
        request: GetAcceptApproveRejectRequest,
        transaction: Transaction,
        _refundTransaction: TransactionRefundDetails,
        principal: Authentication?
    ): RefundTransactionViewModel {
        println("@1181 processApprovalAlgo")
        var refundTransaction = _refundTransaction
        refundTransaction.refundAcceptanceComment = request.rejectReason
        refundTransaction.refundSettlementDate = Instant.now()
        refundTransaction.refundDateTime = Instant.now()
        refundTransaction.lastUpdatedOn = Instant.now()
        if (request.status == RefundAcceptRejectStatus.REJECT) {
            refundTransaction.refundStatus = RefundStatus.REFUND_CANCELED
            refundTransactionsRepository.save(refundTransaction)
            sendRefundRejectedEmailAndSms(refundTransaction, transaction, request)
            return transaction.toRefundViewModel(refundTransaction = refundTransaction)
        }

        // if refund asset is USDT
        try {
            refundTransaction = walletService.sendRefundToExternalWalletAlgo(
                accountOwner,
                refundTransaction.refundAmountDigital
                    ?: throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO),
                refundTransaction.refundDigitalCurrencyType
                    ?: throw BadRequestException(ErrorCodes.INVALID_ASSET_TYPE),
                refundTransaction.refundWalletAddress
                    ?: throw BadRequestException(ErrorCodes.INVALID_WALLET_ADDRESS),
                RefundStatus.REFUNDED.name,
                TransactionType.REFUND,
                refundTransaction.refundFiatType,
                refundTransaction.refundAmountFiat,
                transaction.blockchainTxId,
                refundTransaction,
                principal,
                transaction
            )
        } catch (e: Exception) {
            // any exception, Refund Failed
            println("@1214 $e")
            throw BadRequestException("Error: $e.toString()")
            refundTransaction.refundStatus = RefundStatus.REFUND_FAILED
            refundTransactionsRepository.save(refundTransaction)
        }

        if (refundTransaction.refundStatus == RefundStatus.REFUNDED) {
            sendRefundSuccessEmailAndSmsQAR(refundTransaction, transaction, request)
        } else if (refundTransaction.refundStatus == RefundStatus.REFUND_FAILED) {
            sendRefundFailedEmailAndSms(refundTransaction, transaction, request)
        }

        return transaction.toRefundViewModel(refundTransaction = refundTransaction)
    }
//
    fun submitRefundFormAlgo(userAccount: AccountOwner, request: RefundInitiationRequest): TransactionRefundDetails {
        println("@1226 submitRefundFormAlgo")
        val transaction = getByIdAndOwner(request.transactionId, userAccount)
        if (request.refundAmountFiat <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (request.refundAmountDigital <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        /*if (isAnyRefundInQueue(transaction, request.isReInitiateRefund) == true) {
            throw BadRequestException(ErrorCodes.REFUND_ALREADY_EXISTS_ERROR)
        }*/
        return createOrUpdateRefundTransactionAlgo(transaction, request, null)
    }

    private fun createOrUpdateRefundTransactionAlgo(transaction: Transaction, request: RefundInitiationRequest, _refundTransaction: TransactionRefundDetails?): TransactionRefundDetails {
        println("@1241 createOrUpdateRefundTransactionAlgo")
        var refundTransaction = _refundTransaction
        if (appConfig.environment.equals("uat", true) ||
            appConfig.environment.equals("ddf_uat", true) ||
            appConfig.environment.equals("ddf_prod", true)
        ) {
            if (!request.refundWalletAddress.startsWith("0x") || request.refundWalletAddress.length != 42) {
                throw BadRequestException(ErrorCodes.UNSUPPORTED_ETH_OR_USDT_WALLET_ADDRESS)
            }
        }

        if (request.refundAmountFiat.compareTo(transaction.totalFiatReceived) == 1) {
            throw BadRequestException(ErrorCodes.REFUND_AMOUNT_CANT_BE_GREATER_THAN_RECEIVED_AMOUNT)
        }

        if (request.refundAmountFiat.compareTo(balanceRefundedAmountFiat(transaction)) == 1) {
            throw BadRequestException(ErrorCodes.REFUND_AMOUNT_CANT_BE_GREATER_THAN_BALANCE_AMOUNT)
        }

        if (request.refundAmountFiat > transaction.totalFiatReceived) {
            throw BadRequestException(ErrorCodes.CANNOT_REFUND_MORE_THAN_RECEIVED_FIAT)
        }

        if (request.refundAmountDigital.stripTrailingZeros().scale() > 8) {
            val am =
                request.refundAmountDigital.setScale(8, RoundingMode.UP)
                    .stripTrailingZeros()
            request.refundAmountDigital = am
        }

        if (refundTransaction == null) { // SubmitRefundForm
            println("@1272 refundTransaction")
            if (request.isReInitiateRefund == true) {
                println("@1272 isReInitiateRefund")
                refundTransaction = refundTransactionsRepository.findByUuid(request.refundTransactionID) ?: throw BadRequestException(ErrorCodes.REFUND_NOT_FOUND)
                refundTransaction.refundStatus = RefundStatus.REFUND_ACCEPTED
                refundTransaction.walletAddressMatch = transaction.sourceWalletAddress.equals(request.sourceWalletAddress, false)
                refundTransaction.refundUserMobile = request.refundUserMobile ?: refundTransaction.refundUserMobile
                refundTransaction.refundUserEmail = request.refundUserEmail ?: refundTransaction.refundUserEmail
                refundTransaction.lastUpdatedOn = Instant.now()
                refundTransaction.refundInitiateDate = Instant.now()
            } else {
                println("@1272 else")
                refundTransaction = TransactionRefundDetails(
                    transaction = transaction,
                    type = TransactionType.REFUND,
                    refundStatus = RefundStatus.REFUND_ACCEPTED,
                    walletAddressMatch = transaction.sourceWalletAddress.equals(request.sourceWalletAddress, false),
                    numberOfRefunds = getNoOfRefunds(transaction) ?: 1,
                    refundOrigin = RefundOrigin.MERCHANT_DASHBOARD.value,
                    refundableAmountFiat = transaction.totalFiatReceived?.minus(transaction.totalRefundedAmountFiat),
                    refundType = if (request.refundAmountFiat >= transaction.totalFiatReceived) RefundType.FULL else RefundType.PARTIAL,
                    refundInitiateDate = Instant.now(),
                    createdAt = Instant.now(),
                    lastUpdatedOn = Instant.now()
                )
            }
        } else { // RefundForm, initiate->RefundForm
            println("@1299 else")
            refundTransaction.refundStatus = RefundStatus.REFUND_ACCEPTED
            refundTransaction.walletAddressMatch = transaction.sourceWalletAddress.equals(request.sourceWalletAddress, false)
            refundTransaction.lastUpdatedOn = Instant.now()
        }
        refundTransaction.refundUserMobile = request.refundUserMobile ?: refundTransaction.refundUserMobile
        refundTransaction.refundUserEmail = request.refundUserEmail ?: refundTransaction.refundUserEmail
        refundTransaction.refundAmountFiat = request.refundAmountFiat
        refundTransaction.refundFiatType = request.refundFiatType
        refundTransaction.refundReason = request.reasonForRefund
        refundTransaction.refundAmountDigital = request.refundAmountDigital
        refundTransaction.refundWalletAddress = request.refundWalletAddress
        refundTransaction.sourceWalletAddress = request.sourceWalletAddress
        refundTransaction.refundUserName = request.refundUserName
        refundTransaction.refundMode = request.refundMode
        refundTransaction.extPosLogicalDateRefund = request.extPosLogicalDate ?: refundTransaction.extPosLogicalDateRefund
        refundTransaction.extPosShiftRefund = request.extPosShift ?: refundTransaction.extPosShiftRefund
        refundTransaction.extPosActualDateRefund = request.extPosActualDate ?: refundTransaction.extPosActualDateRefund
        refundTransaction.extPosActualTimeRefund = request.extPosActualTime ?: refundTransaction.extPosActualTimeRefund
        refundTransaction.refundDateTime = Instant.now()
        refundTransaction.lastUpdatedOn = Instant.now()
        refundTransaction.refundDigitalCurrencyType = if (appConfig.environment.equals("dev", true) ||
            appConfig.environment.equals("test", true)
        ) {
            CurrencyUnit.BTC
        } else {
            CurrencyUnit.SART
        }

        refundTransaction = refundTransactionsRepository.save(refundTransaction)

        // Send EMAIL refund-ack-slip
        if (refundTransaction.refundUserEmail != null) {
            emailSMSSenderService.sendEmail(
                "Refund Acknowledgment Slip",
                emailSMSSenderService.getRefundAckSlipEmailBody(

                    transaction.tx_id.toString(),
                    transaction.uuid.toString(),
                    refundTransaction.refundAmountFiat,
                    refundTransaction.refundFiatType,
                    refundTransaction.refundAmountDigital,
                    refundTransaction.refundDigitalCurrencyType,
                    refundTransaction.refundDateTime ?: Instant.now(),
                    (transaction.receiver.account.owner as Merchant).defaultTimeZone.toString()
                ),
                refundTransaction.refundUserEmail.toString(), "contact@wadzpay.com"
            )
        }

        if (request.refundUserMobile != null) {
            val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            formatter.timeZone =
                TimeZone.getTimeZone("GMT" + (transaction.receiver.account.owner as Merchant).defaultTimeZone.toString())
            val strRefundDate = formatter.format(Date.from(Instant.now()))

            val strSmsText: String = "Dear Customer,\n" +
                "We acknowledge the refund request initiated for transaction ID  ${request.transactionId} on $strRefundDate for refund amount of ${request.refundFiatType} ${request.refundAmountFiat.stripTrailingZeros().toPlainString()} " +
                "Once approved, the refund amount will be credited in your designated ${request.refundDigitalType} wallet."
            emailSMSSenderService.sendMobileSMS(
                request.refundUserMobile!!,
                strSmsText
            )
        }
        println("@1363 refundTransaction")
        return refundTransaction
    }

    fun submitRefundFormAutoApprove(userAccount: AccountOwner, request: RefundInitiationRequest, refundOrigin: String): TransactionRefundDetails {
        println("@1226 submitRefundFormTwoAlgo")
        val transaction = getByIdAndOwner(request.transactionId, userAccount)

        if (request.refundAmountFiat <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (request.refundAmountDigital <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }

        if (request.sourceWalletAddress != request.refundWalletAddress) {
            throw BadRequestException("Looks like a non-genuine transaction, please enter valid wallet address")
        }

        /*if (isAnyRefundInQueue(transaction, request.isReInitiateRefund) == true) {
            throw BadRequestException(ErrorCodes.REFUND_ALREADY_EXISTS_ERROR)
        }*/

        return createOrUpdateRefundTransactionAlgoAutoApprove(transaction, request, null, refundOrigin)!!
    }

    fun submitRefundFormAutoApproveQAR(userAccount: AccountOwner, request: RefundInitiationRequest, refundOrigin: String): TransactionRefundDetails {
        println("@1226 submitRefundFormTwoAlgo")
        val transaction = getByIdAndOwner(request.transactionId, userAccount)

        if (request.refundAmountFiat <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (request.refundAmountDigital <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }

        if (request.sourceWalletAddress != request.refundWalletAddress) {
            throw BadRequestException("Looks like a non-genuine transaction, please enter valid wallet address")
        }

        /*if (isAnyRefundInQueue(transaction, request.isReInitiateRefund) == true) {
            throw BadRequestException(ErrorCodes.REFUND_ALREADY_EXISTS_ERROR)
        }*/

        return createOrUpdateRefundTransactionAlgoAutoApproveQAR(transaction, request, null, refundOrigin)!!
    }

    fun createOrUpdateRefundTransactionAlgoAutoApprove(transaction: Transaction, request: RefundInitiationRequest, _refundTransaction: TransactionRefundDetails?, refundOrigin: String): TransactionRefundDetails? {
        println("@1241 createOrUpdateRefundTransactionAlgoTwo")
        var refundTransaction = _refundTransaction
        if (appConfig.environment.equals("uat", true) ||
            appConfig.environment.equals("ddf_uat", true) ||
            appConfig.environment.equals("ddf_prod", true)
        ) {
            if (!request.refundWalletAddress.startsWith("0x") || request.refundWalletAddress.length != 42) {
                throw BadRequestException(ErrorCodes.UNSUPPORTED_ETH_OR_USDT_WALLET_ADDRESS)
            }
        }

        if (request.refundAmountFiat.compareTo(transaction.totalFiatReceived) == 1) {
            throw BadRequestException(ErrorCodes.REFUND_AMOUNT_CANT_BE_GREATER_THAN_RECEIVED_AMOUNT)
        }

        if (request.refundAmountFiat.compareTo(balanceRefundedAmountFiat(transaction)) == 1) {
            throw BadRequestException(ErrorCodes.REFUND_AMOUNT_CANT_BE_GREATER_THAN_BALANCE_AMOUNT)
        }

        if (request.refundAmountFiat > transaction.totalFiatReceived) {
            throw BadRequestException(ErrorCodes.CANNOT_REFUND_MORE_THAN_RECEIVED_FIAT)
        }

        val userAccountByAddress: UserAccount = transaction.sender.userAccountId!!
        val issuanceBanksUserEntry = getIssuanceBankMappingCheck(userAccountByAddress)
        if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.status == Status.DISABLED) {
            throw EntityNotFoundException(ErrorCodes.WALLET_DISABLED)
        }

        if (request.refundAmountDigital.stripTrailingZeros().scale() > 8) {
            val am =
                request.refundAmountDigital.setScale(8, RoundingMode.UP)
                    .stripTrailingZeros()
            request.refundAmountDigital = am
        }

        if (refundTransaction == null) { // SubmitRefundForm
            println("@1272 refundTransaction")
            if (request.isReInitiateRefund == true) {
                println("@1272 isReInitiateRefund")
                refundTransaction = refundTransactionsRepository.findByUuid(request.refundTransactionID) ?: throw BadRequestException(ErrorCodes.REFUND_NOT_FOUND)
                refundTransaction.refundStatus = RefundStatus.REFUND_APPROVED
                refundTransaction.walletAddressMatch = transaction.sourceWalletAddress.equals(request.sourceWalletAddress, false)
                refundTransaction.refundUserMobile = request.refundUserMobile ?: refundTransaction.refundUserMobile
                refundTransaction.refundUserEmail = request.refundUserEmail ?: refundTransaction.refundUserEmail
                refundTransaction.lastUpdatedOn = Instant.now()
                refundTransaction.refundInitiateDate = Instant.now()
            } else {
                refundTransaction = TransactionRefundDetails(
                    transaction = transaction,
                    type = TransactionType.REFUND,
                    refundStatus = RefundStatus.REFUND_APPROVED,
                    walletAddressMatch = transaction.sourceWalletAddress.equals(request.sourceWalletAddress, false),
                    numberOfRefunds = getNoOfRefunds(transaction) ?: 1,
                    refundOrigin = RefundOrigin.MERCHANT_DASHBOARD.value,
                    refundableAmountFiat = transaction.totalFiatReceived?.minus(transaction.totalRefundedAmountFiat),
                    refundType = if (request.refundAmountFiat >= transaction.totalFiatReceived) RefundType.FULL else RefundType.PARTIAL,
                    refundInitiateDate = Instant.now(),
                    createdAt = Instant.now(),
                    lastUpdatedOn = Instant.now()
                )
            }
        } else { // RefundForm, initiate->RefundForm
            refundTransaction.refundStatus = RefundStatus.REFUND_APPROVED
            refundTransaction.walletAddressMatch = transaction.sourceWalletAddress.equals(request.sourceWalletAddress, false)
            refundTransaction.lastUpdatedOn = Instant.now()
        }
        refundTransaction.refundUserMobile = request.refundUserMobile ?: refundTransaction.refundUserMobile
        refundTransaction.refundUserEmail = request.refundUserEmail ?: refundTransaction.refundUserEmail
        refundTransaction.refundAmountFiat = request.refundAmountFiat
        refundTransaction.refundFiatType = request.refundFiatType
        refundTransaction.refundReason = request.reasonForRefund
        refundTransaction.refundAmountDigital = request.refundAmountDigital
        refundTransaction.refundWalletAddress = request.refundWalletAddress
        refundTransaction.sourceWalletAddress = request.sourceWalletAddress
        refundTransaction.refundUserName = request.refundUserName
        refundTransaction.refundMode = request.refundMode
        refundTransaction.extPosLogicalDateRefund = request.extPosLogicalDate ?: refundTransaction.extPosLogicalDateRefund
        refundTransaction.extPosShiftRefund = request.extPosShift ?: refundTransaction.extPosShiftRefund
        refundTransaction.extPosActualDateRefund = request.extPosActualDate ?: refundTransaction.extPosActualDateRefund
        refundTransaction.extPosActualTimeRefund = request.extPosActualTime ?: refundTransaction.extPosActualTimeRefund
        refundTransaction.refundDateTime = Instant.now()
        refundTransaction.lastUpdatedOn = Instant.now()
        refundTransaction.refundStatus = RefundStatus.REFUND_APPROVED
        refundTransaction.refundOrigin = refundOrigin
        refundTransaction.refundDigitalCurrencyType = if (appConfig.environment.equals("dev", true) ||
            appConfig.environment.equals("test", true)
        ) {
            CurrencyUnit.BTC
        } else {
            CurrencyUnit.SART
        }

        refundTransaction = refundTransactionsRepository.save(refundTransaction)
        println(refundTransaction.refundStatus)

        val request = GetAcceptApproveRejectRequest(txn_uuid = refundTransaction.transaction.uuid, status = RefundAcceptRejectStatus.ACCEPT, type = RefundAcceptRejectType.APPROVAL)
        val accountOwner = (refundTransaction.transaction.receiver.account.owner ?: refundTransaction.transaction.receiver.account.owner)!!
        println(accountOwner)
        try {
            refundTransaction = walletService.sendRefundToExternalWalletAlgo(
                accountOwner,
                refundTransaction.refundAmountDigital
                    ?: throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO),
                refundTransaction.refundDigitalCurrencyType
                    ?: throw BadRequestException(ErrorCodes.INVALID_ASSET_TYPE),
                refundTransaction.refundWalletAddress
                    ?: throw BadRequestException(ErrorCodes.INVALID_WALLET_ADDRESS),
                RefundStatus.REFUNDED.name,
                TransactionType.REFUND,
                refundTransaction.refundFiatType,
                refundTransaction.refundAmountFiat,
                transaction.blockchainTxId,
                refundTransaction,
                null,
                transaction
            )
        } catch (e: Exception) {
            // any exception, Refund Failed
            println("@1214 $e")
            throw BadRequestException("Error: $e")
            refundTransaction.refundStatus = RefundStatus.REFUND_FAILED
            refundTransactionsRepository.save(refundTransaction)
        }

        if (refundTransaction != null) {
            if (refundTransaction.refundStatus == RefundStatus.REFUNDED) {
                sendRefundSuccessEmailAndSms(refundTransaction, transaction, request)
            } else if (refundTransaction.refundStatus == RefundStatus.REFUND_FAILED) {
                sendRefundFailedEmailAndSms(refundTransaction, transaction, request)
            }
        }

        transaction.toRefundViewModel(refundTransaction = refundTransaction)
        return refundTransaction
    }

    fun createOrUpdateRefundTransactionAlgoAutoApproveQAR(transaction: Transaction, request: RefundInitiationRequest, _refundTransaction: TransactionRefundDetails?, refundOrigin: String): TransactionRefundDetails? {
        println("@1241 createOrUpdateRefundTransactionAlgoTwo")
        var refundTransaction = _refundTransaction
        if (appConfig.environment.equals("uat", true) ||
            appConfig.environment.equals("ddf_uat", true) ||
            appConfig.environment.equals("ddf_prod", true)
        ) {
            if (!request.refundWalletAddress.startsWith("0x") || request.refundWalletAddress.length != 42) {
                throw BadRequestException(ErrorCodes.UNSUPPORTED_ETH_OR_USDT_WALLET_ADDRESS)
            }
        }

        if (request.refundAmountFiat.compareTo(transaction.totalFiatReceived) == 1) {
            throw BadRequestException(ErrorCodes.REFUND_AMOUNT_CANT_BE_GREATER_THAN_RECEIVED_AMOUNT)
        }

        if (request.refundAmountFiat.compareTo(balanceRefundedAmountFiat(transaction)) == 1) {
            throw BadRequestException(ErrorCodes.REFUND_AMOUNT_CANT_BE_GREATER_THAN_BALANCE_AMOUNT)
        }

        if (request.refundAmountFiat > transaction.totalFiatReceived) {
            throw BadRequestException(ErrorCodes.CANNOT_REFUND_MORE_THAN_RECEIVED_FIAT)
        }

        val userAccountByAddress: UserAccount = transaction.sender.userAccountId!!
        val issuanceBanksUserEntry = getIssuanceBankMappingCheck(userAccountByAddress)
        if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.status == Status.DISABLED) {
            throw EntityNotFoundException(ErrorCodes.WALLET_DISABLED)
        }

        if (request.refundAmountDigital.stripTrailingZeros().scale() > 8) {
            val am =
                request.refundAmountDigital.setScale(8, RoundingMode.UP)
                    .stripTrailingZeros()
            request.refundAmountDigital = am
        }

        if (refundTransaction == null) { // SubmitRefundForm
            println("@1272 refundTransaction")
            if (request.isReInitiateRefund == true) {
                println("@1272 isReInitiateRefund")
                refundTransaction = refundTransactionsRepository.findByUuid(request.refundTransactionID) ?: throw BadRequestException(ErrorCodes.REFUND_NOT_FOUND)
                refundTransaction.refundStatus = RefundStatus.REFUND_APPROVED
                refundTransaction.walletAddressMatch = transaction.sourceWalletAddress.equals(request.sourceWalletAddress, false)
                refundTransaction.refundUserMobile = request.refundUserMobile ?: refundTransaction.refundUserMobile
                refundTransaction.refundUserEmail = request.refundUserEmail ?: refundTransaction.refundUserEmail
                refundTransaction.lastUpdatedOn = Instant.now()
                refundTransaction.refundInitiateDate = Instant.now()
            } else {
                refundTransaction = TransactionRefundDetails(
                    transaction = transaction,
                    type = TransactionType.REFUND,
                    refundStatus = RefundStatus.REFUND_APPROVED,
                    walletAddressMatch = transaction.sourceWalletAddress.equals(request.sourceWalletAddress, false),
                    numberOfRefunds = getNoOfRefunds(transaction) ?: 1,
                    refundOrigin = RefundOrigin.MERCHANT_DASHBOARD.value,
                    refundableAmountFiat = transaction.totalFiatReceived?.minus(transaction.totalRefundedAmountFiat),
                    refundType = if (request.refundAmountFiat >= transaction.totalFiatReceived) RefundType.FULL else RefundType.PARTIAL,
                    refundInitiateDate = Instant.now(),
                    createdAt = Instant.now(),
                    lastUpdatedOn = Instant.now()
                )
            }
        } else { // RefundForm, initiate->RefundForm
            refundTransaction.refundStatus = RefundStatus.REFUND_APPROVED
            refundTransaction.walletAddressMatch = transaction.sourceWalletAddress.equals(request.sourceWalletAddress, false)
            refundTransaction.lastUpdatedOn = Instant.now()
        }
        refundTransaction.refundUserMobile = request.refundUserMobile ?: refundTransaction.refundUserMobile
        refundTransaction.refundUserEmail = request.refundUserEmail ?: refundTransaction.refundUserEmail
        refundTransaction.refundAmountFiat = request.refundAmountFiat
        refundTransaction.refundFiatType = request.refundFiatType
        refundTransaction.refundReason = request.reasonForRefund
        refundTransaction.refundAmountDigital = request.refundAmountDigital
        refundTransaction.refundWalletAddress = request.refundWalletAddress
        refundTransaction.sourceWalletAddress = request.sourceWalletAddress
        refundTransaction.refundUserName = request.refundUserName
        refundTransaction.refundMode = request.refundMode
        refundTransaction.extPosLogicalDateRefund = request.extPosLogicalDate ?: refundTransaction.extPosLogicalDateRefund
        refundTransaction.extPosShiftRefund = request.extPosShift ?: refundTransaction.extPosShiftRefund
        refundTransaction.extPosActualDateRefund = request.extPosActualDate ?: refundTransaction.extPosActualDateRefund
        refundTransaction.extPosActualTimeRefund = request.extPosActualTime ?: refundTransaction.extPosActualTimeRefund
        refundTransaction.refundDateTime = Instant.now()
        refundTransaction.lastUpdatedOn = Instant.now()
        refundTransaction.refundStatus = RefundStatus.REFUND_APPROVED
        refundTransaction.refundOrigin = refundOrigin
        refundTransaction.refundDigitalCurrencyType = if (appConfig.environment.equals("dev", true) ||
            appConfig.environment.equals("test", true)
        ) {
            CurrencyUnit.BTC
        } else {
            CurrencyUnit.SART
        }

        refundTransaction = refundTransactionsRepository.save(refundTransaction)
        println(refundTransaction.refundStatus)

        val request = GetAcceptApproveRejectRequest(txn_uuid = refundTransaction.transaction.uuid, status = RefundAcceptRejectStatus.ACCEPT, type = RefundAcceptRejectType.APPROVAL)
        val accountOwner = (refundTransaction.transaction.receiver.account.owner ?: refundTransaction.transaction.receiver.account.owner)!!
        println(accountOwner)
        try {
            refundTransaction = walletService.sendRefundToExternalWalletAlgo(
                accountOwner,
                refundTransaction.refundAmountDigital
                    ?: throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO),
                refundTransaction.refundDigitalCurrencyType
                    ?: throw BadRequestException(ErrorCodes.INVALID_ASSET_TYPE),
                refundTransaction.refundWalletAddress
                    ?: throw BadRequestException(ErrorCodes.INVALID_WALLET_ADDRESS),
                RefundStatus.REFUNDED.name,
                TransactionType.REFUND,
                refundTransaction.refundFiatType,
                refundTransaction.refundAmountFiat,
                transaction.blockchainTxId,
                refundTransaction,
                null,
                transaction
            )
        } catch (e: Exception) {
            // any exception, Refund Failed
            println("@1214 $e")
            throw BadRequestException("Error: $e")
            refundTransaction.refundStatus = RefundStatus.REFUND_FAILED
            refundTransactionsRepository.save(refundTransaction)
        }

        if (refundTransaction != null) {
            if (refundTransaction.refundStatus == RefundStatus.REFUNDED) {
                sendRefundSuccessEmailAndSmsQAR(refundTransaction, transaction, request)
            } else if (refundTransaction.refundStatus == RefundStatus.REFUND_FAILED) {
                sendRefundFailedEmailAndSms(refundTransaction, transaction, request)
            }
        }

        transaction.toRefundViewModel(refundTransaction = refundTransaction)
        return refundTransaction
    }

    fun getIssuanceBankMappingCheck(userAccount: UserAccount): IssuanceBanksUserEntry? {
        val issuanceBanksUserEntry = issuanceBanksUserEntryRepository.getByUserAccountId(userAccount)
        println("issunace bank Id 320 ==? @1680 " + issuanceBanksUserEntry?.issuanceBanksId)
        if (issuanceBanksUserEntry != null) {
            return issuanceBanksUserEntry
        }
        return null
        // throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
    }
    fun getTransactionSettlementViewModels(
        accountOwner: AccountOwner,
        listRequest: GetTransactionSettlementListRequest
    ): List<TransactionSettlement> {
        val listResponse = getTransactionsSettlement(accountOwner, listRequest).map {
            it.toTransactionSettlementViewModel(
                direction = it.getDirection(accountOwner)
            )
        }
        listResponse.forEach { data ->
            data.feeConfigData = getFeeData(data.uuid)
        }
        return listResponse.filter { e -> e.transactionType != TransactionType.SYNC_BLOCKCHAIN }
    }

    fun getTransactionsSettlement(accountOwner: AccountOwner, listRequest: GetTransactionSettlementListRequest): List<Transaction> {
        val specification = and(
            listRequest.toSpecification(accountOwner)
        )
        val result =
            transactionRepository.findAll(
                specification,
            )

        /*if (listRequest.issuanceBanksId != null) {
            result = result.filter { r -> r.issuanceBanks.toString().contains(listRequest.issuanceBanksId.toString().trim()) }
        }*/
        return result
    }
    fun getTransactionByUUID(uuid: UUID): Transaction {
        return transactionRepository.getByUuid(uuid)
            ?: throw BadRequestException(ErrorCodes.TRANSACTION_NOT_FOUND)
    }

    fun getTransactionBySubAccount(subAccount: UserAccount): Transaction {
        return transactionRepository.getByUuid(UUID.randomUUID())
            ?: throw BadRequestException(ErrorCodes.TRANSACTION_NOT_FOUND)
    }
}

private fun List<Transaction>.toSettlementReport(merchant: Merchant): List<SettlementReport> {
    return this.map {
        it.toViewModelSettlementReport(merchant)
    }
}

data class Pagination(
    var page: Int? = null,
    val pageSize: Int = 10
)

fun Pagination.toPageable(sortBy: SortableTransactionFields, direction: Sort.Direction): Pageable {
    if (page == null) {
        page = 0
    }
    try {
        return PageRequest.of(page!!, pageSize, direction, sortBy.value)
    } catch (ex: Exception) {
        throw BadRequestException(ErrorCodes.INVALID_PAGINATION)
    }
}
/**/
data class GetTransactionListRequest(
    @PositiveOrZero
    val page: Int? = null,
    val cognitoUsername: String? = null,
    val direction: TransactionDirection = TransactionDirection.UNKNOWN,
    var type: Collection<TransactionType>? = null,
    var status: Collection<TransactionStatus>? = null,
    val asset: Collection<String>? = null,
    @PositiveOrZero
    val amountFrom: BigDecimal? = null,
    @PositiveOrZero
    val amountTo: BigDecimal? = null,
    val dateFrom: Instant? = null,
    val dateTo: Instant? = null,
    var search: String? = null,
    var sortBy: SortableTransactionFields = SortableTransactionFields.CREATED_AT,
    val sortDirection: Sort.Direction = Sort.Direction.DESC,
    var fiatAmount: BigDecimal? = null,
    var amount: BigDecimal? = null,
    var uuid: UUID? = null,
    val uuidSearch: String? = null,
    val tx_id: String? = null,
    var refundStatus: Collection<RefundStatus>? = null,
    var refundMode: Collection<RefundMode>? = null,
    val extPosId: String? = null,
    val extPosSequenceNumber: String? = null,
    val extPosTransactionId: String? = null,
    var logicalDateFrom: Instant? = null,
    var logicalDateTo: Instant? = null,
    var appSearch: Boolean? = null,
    val totalDigitalCurrencyReceived: BigDecimal? = null,
    val totalFiatReceived: BigDecimal? = null,
    val refundFiatAmount: BigDecimal? = null,
    val refundAmountDigital: BigDecimal? = null,
    var refundType: Collection<RefundType>? = null,
    val isAcceptancePage: Boolean? = false,
    val refundTransactionID: String? = null,
    val refundPosTransactionID: String? = null,
    val issuanceBanksId: String ? = null,
    val transactionMode: Collection<TransactionMode> ? = null,
    val posId: String? = null,
)

data class GetAcceptApproveRejectRequest(
    val txn_uuid: UUID,
    // for accept/reject
    var status: RefundAcceptRejectStatus,
    // for acceptance/approval
    var type: RefundAcceptRejectType,
    var rejectReason: String? = null
)

fun GetTransactionListRequest.toSpecification(owner: AccountOwner): Specification<Transaction> = and(
    hasOwner(owner),
    hasDirection(direction, owner),
    type?.let { hasType(it) },
    status?.let { hasStatus(it) },
    asset?.let { hasAsset(asset) },
    amountFrom?.let { hasAmountGreaterOrEqualTo(amountFrom) },
    amountTo?.let { hasAmountLessOrEqualTo(amountTo) },
    fiatAmount?.let { hasFiatAmountEqualTo(it) },
    amount?.let { hasAmountEqualTo(it) },
    uuid?.let { hasUUIDEqualTo(uuid!!) },
    tx_id?.let { hasTransactionIdEqualTo(tx_id) },
    dateFrom?.let { hasDateGreaterOrEqualTo(dateFrom) },
    dateTo?.let { hasDateLessOrEqualTo(dateTo) },
    search?.let { ownerNamesContainPattern(it) },
    extPosId?.let { hasExtPosIdEqualTo(it) },
    extPosSequenceNumber?.let { hasExtPosSequenceNumberEqualTo(it) },
    logicalDateFrom?.let { hasLogicalDateGreaterOrEqualTo(logicalDateFrom!!) },
    logicalDateTo?.let { hasLogicalDateLessOrEqualTo(logicalDateTo!!) },
    totalDigitalCurrencyReceived?.let { hasTotalDigitalCurrencyReceivedEqualTo(it) },
    totalFiatReceived?.let { hasTotalFiatReceivedEqualTo(it) },
    transactionMode?.let { hasTransactionMode(transactionMode) }
)

data class GetTransactionSettlementListRequest(
    @JsonIgnore
    var status: Collection<TransactionStatus>? = mutableListOf(
        TransactionStatus.OVERPAID,
        TransactionStatus.UNDERPAID,
        TransactionStatus.SUCCESSFUL
    ),
    var dateFrom: Instant? = null,
    var dateTo: Instant? = null,
)

fun GetTransactionSettlementListRequest.toSpecification(owner: AccountOwner): Specification<Transaction> = and(
    hasOwner(owner),
    status?.let { hasStatus(it) },
    dateFrom?.let { hasDateGreaterOrEqualTo(dateFrom!!) },
    dateTo?.let { hasDateLessOrEqualTo(dateTo!!) }
)
