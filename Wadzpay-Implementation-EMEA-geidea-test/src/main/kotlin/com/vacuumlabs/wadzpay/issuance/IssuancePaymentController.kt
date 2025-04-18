package com.vacuumlabs.wadzpay.issuance

import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.utils.ApisLog
import com.vacuumlabs.wadzpay.utils.ApisLoggerRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/client")
@Tag(name = "Issuance Payment Controller")
@Validated
class IssuancePaymentController(
    val issuancePaymentService: IssuancePaymentService,
    val userAccountService: UserAccountService,
    val apisLoggerRepository: ApisLoggerRepository
) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)
    @PostMapping("/getTransactionTopUpStatus")
    @Operation(summary = "Confirm Top-up Payment Status")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "TopUp status updated successfully."),
        ApiResponse(responseCode = "403", description = ErrorCodes.UNAUTHORIZED),
        ApiResponse(
            responseCode = "400",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO},${ErrorCodes.WALLET_DISABLED}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getTransactionTopUpStatus(@RequestBody requestData: TransactionStatusRequest, principal: Authentication): TransactionStatusResponse? {
        val issuerInstitution = userAccountService.getUserAccountByEmail(principal.name)
        if (issuerInstitution.issuanceBanks != null) {
            print("Topup payment Request: $requestData")
            logger.info("Topup payment Request: $requestData")
            apisLoggerRepository.save(ApisLog("Topup payment Request", requestData.toString(), "", ""))
            if (requestData.tokenAmount <= BigDecimal.ZERO) {
                throw EntityNotFoundException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
            }
            if (requestData.userId.isNullOrEmpty()) {
                throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
            }
            return issuancePaymentService.getTransactionTopUpStatus(issuerInstitution, requestData)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @PostMapping("/getTransactionRedeemUnspentStatus")
    @Operation(summary = "Confirm Refund Payment Status")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Refund status updated successfully."),
        ApiResponse(responseCode = "403", description = ErrorCodes.UNAUTHORIZED),
        ApiResponse(
            responseCode = "400",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO},${ErrorCodes.WALLET_DISABLED},${ErrorCodes.SENDER_RECEIVER_WALLET_ADDRESS_CANT_BE_SAME},${ErrorCodes.INSUFFICIENT_FUNDS}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun refundToken(@RequestBody requestData: TransactionStatusRequest, principal: Authentication): TransactionStatusRedeemResponse? {
        val issuerInstitution = userAccountService.getUserAccountByEmail(principal.name)
        if (issuerInstitution.issuanceBanks != null) {
            print("Redeem payment Request: $requestData")
            logger.info("Redeem payment Request: $requestData")
            apisLoggerRepository.save(ApisLog("Redeem payment Request", requestData.toString(), "", ""))
            if (requestData.tokenAmount <= BigDecimal.ZERO) {
                throw EntityNotFoundException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
            }
            if (requestData.userId.isNullOrEmpty()) {
                throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
            }
            return issuancePaymentService.getTransactionRefundTokenStatus(issuerInstitution, requestData)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @PostMapping("/getTransactionPeerToPeerStatus")
    @Operation(summary = "Confirm Peer to Peer Payment Status")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Peer to Peer status updated successfully."),
        ApiResponse(responseCode = "403", description = ErrorCodes.UNAUTHORIZED),
        ApiResponse(
            responseCode = "400",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO},${ErrorCodes.WALLET_DISABLED},${ErrorCodes.INSUFFICIENT_FUNDS},${ErrorCodes.SENDER_RECEIVER_WALLET_ADDRESS_CANT_BE_SAME}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun p2pPayment(@RequestBody requestData: TransactionP2PStatusRequest, principal: Authentication): TransactionStatusP2PResponse? {
        val issuerInstitution = userAccountService.getUserAccountByEmail(principal.name)
        if (issuerInstitution.issuanceBanks != null) {
            print("Peer to Peer payment Request: $requestData")
            logger.info("Peer to Peer payment Request: $requestData")
            apisLoggerRepository.save(ApisLog("Peer to Peer payment Request", requestData.toString(), "", ""))
            if (requestData.tokenAmount <= BigDecimal.ZERO) {
                throw EntityNotFoundException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
            }
            if (requestData.userId.isNullOrEmpty()) {
                throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
            }
            return issuancePaymentService.p2pPayment(issuerInstitution, requestData)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }
    @PostMapping("/getTransactionQRPaymentStatus")
    @Operation(summary = "Confirm QR Payment Payment Status")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "QR Payment status updated successfully."),
        ApiResponse(responseCode = "404", description = ErrorCodes.USER_NOT_FOUND),
        ApiResponse(responseCode = "403", description = ErrorCodes.UNAUTHORIZED),
        ApiResponse(
            responseCode = "400",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO},${ErrorCodes.WALLET_DISABLED},${ErrorCodes.SENDER_RECEIVER_WALLET_ADDRESS_CANT_BE_SAME},${ErrorCodes.INSUFFICIENT_FUNDS}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun qrPayment(@RequestBody requestData: TransactionQRPaymentStatusRequest, principal: Authentication): TransactionStatusQRResponse? {
        val issuerInstitution = userAccountService.getUserAccountByEmail(principal.name)
        if (issuerInstitution.issuanceBanks != null) {
            print("QR Payment payment Request: $requestData")
            logger.info("QR Payment payment Request: $requestData")
            apisLoggerRepository.save(ApisLog("QR Payment payment Request", requestData.toString(), "", ""))
            if (requestData.tokenAmount <= BigDecimal.ZERO) {
                throw EntityNotFoundException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
            }
            if (requestData.userId.isNullOrEmpty()) {
                throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
            }
            return issuancePaymentService.qrPayment(issuerInstitution, requestData)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @PostMapping("/saveBankDetailsInCache")
    @Operation(summary = "Save Bank details in cache")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Bank Detail stored in Cache successfully."),
        ApiResponse(responseCode = "403", description = ErrorCodes.UNAUTHORIZED),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun saveBankDetailsInCache(@RequestBody transactionCacheRequest: TransactionCacheRequest, principal: Authentication): TransactionStatus {
        val issuerInstitution = userAccountService.getUserAccountByEmail(principal.name)
        if (transactionCacheRequest.userId.isNullOrEmpty()) {
            throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
        }
        return issuancePaymentService.saveBankDetailsInCache(issuerInstitution, transactionCacheRequest)
    }

    data class TransactionStatusRequest(
        /* Unique userId(Email) */
        val userId: String?,
        /* SAR Token*/
        var tokenAmount: BigDecimal,
        /* Transaction Status (true - Success/false - Failure)*/
        val transactionStatus: Boolean
    )

    data class TransactionStatusResponse(
        /* wadzpay unique id */
        val uuid: String,
        /* SAR Token amount of transaction */
        val totalTokenAmount: BigDecimal,
        /* Blockchain wallet address (acquirer’s blockchain address) */
        val address: String,
        /* Status of transaction where values can be SUCCESS/FAILED */
        val transactionStatus: String,
        /* SAR Token deposited in Pilgrim wallet */
        val depositedTokenAmount: BigDecimal,
        /* WadzPay Unique id for transaction */
        val transactionId: String? = null,
        /* Blockchain transaction hash for tracking on blockchain scan */
        val blockChainHash: String? = null
    )

    data class TransactionStatusRedeemResponse(
        /* wadzpay unique id */
        val uuid: String,
        /* SAR Token amount of transaction */
        val totalTokenAmount: BigDecimal,
        /* Blockchain wallet address (acquirer’s blockchain address) */
        val address: String,
        /* Status of transaction where values can be SUCCESS/FAILED */
        val transactionStatus: String,
        /* SAR Token deposited in Pilgrim wallet */
        val redeemTokenAmount: BigDecimal,
        /* WadzPay Unique id for transaction */
        val transactionId: String? = null,
        /* Blockchain transaction hash for tracking on blockchain scan */
        val blockChainHash: String? = null
    )

    data class TransactionStatusP2PResponse(
        /* wadzpay unique id */
        val uuid: String,
        /* SAR Token amount of transaction */
        val totalTokenAmount: BigDecimal,
        /* Blockchain wallet address (acquirer’s blockchain address) */
        val address: String,
        /* Status of transaction where values can be SUCCESS/FAILED */
        val transactionStatus: String,
        /* SAR Token deposited in Pilgrim wallet */
        val transferTokenAmount: BigDecimal,
        /* WadzPay Unique id for transaction */
        val transactionId: String? = null,
        /* Blockchain transaction hash for tracking on blockchain scan */
        val blockChainHash: String? = null
    )
    data class TransactionStatusQRResponse(
        /* wadzpay unique id */
        val uuid: String,
        /* SAR Token amount of transaction */
        val totalTokenAmount: BigDecimal,
        /* Blockchain wallet address (acquirer’s blockchain address) */
        val address: String,
        /* Status of transaction where values can be SUCCESS/FAILED */
        val transactionStatus: String,
        /* SAR Token deposited in Pilgrim wallet */
        val paidTokenAmount: BigDecimal,
        /* WadzPay Unique id for transaction */
        val transactionId: String? = null,
        /* Blockchain transaction hash for tracking on blockchain scan */
        val blockChainHash: String? = null
    )

    data class TransactionP2PStatusRequest(
        /* Unique userId(Email) */
        val userId: String?,
        /* SAR Token*/
        var tokenAmount: BigDecimal,
        /* Transaction Status (true - Success/false - Failure)*/
        val transactionStatus: Boolean,
        /* Unique receiver UserId(Email) */
        val receiverUserId: String
    )

    data class TransactionQRPaymentStatusRequest(
        /* Unique userId(Email) */
        val userId: String?,
        /* SAR Token*/
        var tokenAmount: BigDecimal,
        /* Transaction Status (true - Success/false - Failure)*/
        val transactionStatus: Boolean,
        /* POS Details (Required in POS Payment) */
        val posuuid: String ? = null,
        /* Unique Merchant Id (Email)*/
        val merchantId: String ? = null,
    )

    data class TransactionCacheRequest(
        /* Unique userId(Email/unique Id) */
        val userId: String?,
        /* Transaction Type (type - LOAD/REFUND)*/
        val transactionType: IssuanceWalletUserController.TransactionType,
        /* Bank account Details*/
        val bankAccount: String
    )
}
