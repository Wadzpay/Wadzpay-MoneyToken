package com.vacuumlabs.wadzpay.issuance

import com.vacuumlabs.vuba.ledger.service.LedgerService
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.issuance.models.Status
import com.vacuumlabs.wadzpay.ledger.model.Transaction
import com.vacuumlabs.wadzpay.ledger.model.TransactionDirection
import com.vacuumlabs.wadzpay.ledger.model.TransactionMode
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.ledger.service.GetTransactionListRequest
import com.vacuumlabs.wadzpay.ledger.service.TransactionService
import com.vacuumlabs.wadzpay.services.CognitoService
import com.vacuumlabs.wadzpay.services.RedisService
import com.vacuumlabs.wadzpay.user.TransactionDetailsRequest
import com.vacuumlabs.wadzpay.user.TransactionDetailsResponse
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.viewmodels.toViewModel
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Properties
import kotlin.streams.toList

@Service
class IssuanceCommonService(
    val cognitoService: CognitoService,
    val redisService: RedisService,
    val appConfig: AppConfig,
    val issuanceGraphService: IssuanceGraphService,
    val userAccountService: UserAccountService,
    val ledgerService: LedgerService,
    val transactionService: TransactionService,
    val issuanceWalletService: IssuanceWalletService
) {
    fun getTransactionMaster(userAccount: UserAccount): Any? {
        val transactionModeList: MutableList<TransactionTypeData> = mutableListOf()
        val transactionTypeList: MutableList<TransactionTypeData> = mutableListOf()
        for (transactionMode in TransactionMode.values()) {
            if (setTransactionModeString(transactionMode) != null) {
                val transactionModeData = TransactionTypeData(
                    type = transactionMode,
                    value = setTransactionModeString(transactionMode)
                )
                transactionModeList.add(transactionModeData)
            }
        }
        for (transactionType in TransactionType.values()) {
            if (setTransactionTypeString(transactionType) != null) {
                val transactionModeData = TransactionTypeData(
                    type = transactionType,
                    value = setTransactionTypeString(transactionType)
                )
                transactionTypeList.add(transactionModeData)
            }
        }
        return TransactionMaster(
            transactionMode = transactionModeList,
            transactionType = transactionTypeList
        )
    }

    fun setTransactionModeString(transactionMode: TransactionMode): String? {
        return when (transactionMode) {
            TransactionMode.CUSTOMER_OFFLINE -> {
                "Customer Offline"
            }
            TransactionMode.CUSTOMER_MERCHANT_ONLINE -> {
                "Customer/Merchant Online"
            }
            TransactionMode.MERCHANT_OFFLINE -> {
                "Merchant Offline"
            }
            else -> {
                return null
            }
        }
    }

    fun setTransactionTypeString(transactionType: TransactionType): String? {
        return when (transactionType) {
            TransactionType.POS -> {
                "Payment"
            }

            TransactionType.PEER_TO_PEER -> {
                "Transfer"
            }

            TransactionType.REFUND -> {
                "Merchant Refund"
            }

            TransactionType.DEPOSIT -> {
                "Topup"
            }

            TransactionType.WITHDRAW -> {
                "Redeem unspent"
            }

            TransactionType.SERVICE_FEE -> {
                "Service Fee"
            }

            TransactionType.WALLET_FEE -> {
                "Low Balance fee"
            }

            else -> {
                return null
            }
        }
    }

    fun getTransactionData(
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
            val issuanceBanksUserEntry = issuanceWalletService.getIssuanceBankMapping(userAccount)
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
                    data = data.filter { e -> e.type == issuanceWalletService.getTransactionType(request.transactionType!!) } as MutableList<Transaction>
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
                        customerName = issuanceWalletService.getTransactionCustomerId(tData, true),
                        customerId = issuanceWalletService.getTransactionCustomerId(tData, false),
                        institutionName = issuerUserAccount.issuanceBanks?.bankName,
                        institutionId = issuerUserAccount.issuanceBanks?.institutionId,
                        transactionId = tData.uuid.toString(),
                        tokenName = tData.asset,
                        noOfTokens = tData.amount,
                        transactionDate = tData.createdAt,
                        transactionType = issuanceWalletService.getTransactionAPICALL(tData.transactionType).toString(),
                        ledgerType = issuanceWalletService.getTransactionType(tData.transactionType),
                        transactionStatus = tData.status.toString(),
                        receiverId = issuanceWalletService.getTransactionRecieverId(tData, false),
                        receiverName = issuanceWalletService.getTransactionRecieverId(tData, true)
                    )
                )
            }
        } else {
            val tRequest = GetTransactionListRequest(
                type = if (request.transactionType != null) mutableSetOf(issuanceWalletService.getTransactionType(request.transactionType!!)) else null,
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
                        customerName = issuanceWalletService.getTransactionCustomerId(tData, true),
                        customerId = issuanceWalletService.getTransactionCustomerId(tData, false),
                        institutionName = issuerUserAccount.issuanceBanks?.bankName,
                        institutionId = issuerUserAccount.issuanceBanks?.institutionId,
                        transactionId = tData.uuid.toString(),
                        tokenName = tData.asset,
                        noOfTokens = tData.amount,
                        transactionDate = tData.createdAt,
                        transactionType = issuanceWalletService.getTransactionAPICALL(tData.transactionType).toString(),
                        ledgerType = issuanceWalletService.getTransactionType(tData.transactionType),
                        transactionStatus = tData.status.toString(),
                        receiverId = issuanceWalletService.getTransactionRecieverId(tData, false),
                        receiverName = issuanceWalletService.getTransactionRecieverId(tData, true)
                    )
                )
            }
        }
        return transactionDetailsResponse
    }

    fun getInstitutionProperties(institutionName: String, lang: String?): Map<String, String> {
        val effectiveLang = lang ?: "English"
        val propertiesFileName = "${institutionName}_$effectiveLang.properties"
        val properties = loadProperties(propertiesFileName)

        return mapOf(
            "institutionName" to properties.getProperty("institution.name"),
            "institutionFullName" to properties.getProperty("institution.fullname"),
            "institutionLogo" to properties.getProperty("institution.logo"),
            "issuingCurrency" to properties.getProperty("issuing.currency"),
            "acquiringCurrency" to properties.getProperty("acquiring.currency")
        )
    }

    // Generic function to load properties from a file
    private fun loadProperties(fileName: String): Properties {
        val properties = Properties()
        try {
            val inputStream = this.javaClass.classLoader.getResourceAsStream(fileName)
                ?: throw IllegalArgumentException("Property file '$fileName' not found in classpath")

            inputStream.use {
                properties.load(it)
            }
        } catch (e: Exception) {
            e.printStackTrace() // Handle the exception if the file doesn't exist or can't be loaded
        }
        return properties
    }

    data class TransactionTypeData(
        var type: Any?,
        var value: String?
    )

    data class TransactionMaster(
        val transactionMode: List<TransactionTypeData>?,
        val transactionType: List<TransactionTypeData>?
    )
}
