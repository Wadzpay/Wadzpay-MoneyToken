package com.vacuumlabs.wadzpay.webhook

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.vacuumlabs.wadzpay.bitgo.WalletService
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.issuance.IssuancePaymentService
import com.vacuumlabs.wadzpay.kyc.models.KycLogRepository
import com.vacuumlabs.wadzpay.kyc.models.KycLogs
import com.vacuumlabs.wadzpay.kyc.models.VerificationStatus
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.notification.NotificationService
import com.vacuumlabs.wadzpay.paymentPoc.PaymentPocService
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPaymentTransferResponse
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayWebhookRes
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayWebhookResponse
import com.vacuumlabs.wadzpay.services.CognitoService
import com.vacuumlabs.wadzpay.user.UserAccount
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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val String?.toVerificationStatus: VerificationStatus
    get() {
        return if (this == null) {
            VerificationStatus.NULL
        } else {
            try {
                VerificationStatus.valueOf(this)
            } catch (e: Exception) {
                VerificationStatus.UNKNOWN
            }
        }
    }

@RestController
@RequestMapping("/webhook")
@Tag(name = "Webhooks")
class BitGoWebhookController(
    val walletService: WalletService,
    val apisLoggerRepository: ApisLoggerRepository,
    val kycLogRepository: KycLogRepository,
    val userAccountService: UserAccountService,
    val notificationService: NotificationService,
    val cognitoService: CognitoService,
    val paymentPocService: PaymentPocService,
    val appConfig: AppConfig,
    val issuancePaymentService: IssuancePaymentService
) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/wallet")
    @Operation(summary = "Receiver for Omni wallet transfer webhook")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.TRANSFER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "422",
            description = "${ErrorCodes.INVALID_TRANSACTION_TYPE}, " +
                "${ErrorCodes.TRANSACTION_ALREADY_FINALIZED}, " +
                ErrorCodes.WRONG_AMOUNT,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getTransferNotification(@RequestBody notification: BitGoEvent) {
        logger.info("Received transfer notification: $notification")
        apisLoggerRepository.save(ApisLog("BitGoNotification", notification.toString(), "", ""))
        if (notification.type != BitGoType.transfer) {
            logger.info("Ignoring notification for transfer ${notification.transfer} because of unsupported type: ${notification.type}")
            return
        }
        if (notification.state != BitGoState.confirmed) {
            logger.info("Ignoring notification for transfer ${notification.transfer} because of unsupported state: ${notification.state}")
            /** To do list:
             *  1. If coinCode == BTC --> Call gap600 rest api
             *  */
            val gap600RunMode = appConfig.gap600RunMode
            val stubEnable = appConfig.stubEnable
            if (gap600RunMode && !stubEnable) {
                logger.info("Gap600RunMode is ON.")
                // Below gap600_zero_confimation_call funcation calls service for Gap600 confirmation service
                // This function will send CoinType, Wallet and Transfer details
                walletService.gap600_zero_confimation_call(
                    notification.coin,
                    notification.wallet,
                    notification.transfer
                )
                return
            }
        }

        logger.info("Confirming transfer ${notification.transfer}.")
        walletService.confirmTx(notification.coin, notification.wallet, notification.transfer)
    }

    @GetMapping("/jumio/success")
    @Operation(summary = "Receiver for JUMIO success")
    @ApiResponses(
        ApiResponse(responseCode = "200")
    )
    fun logJumioSuccess(
        @RequestParam(value = "transactionStatus", required = false) transactionStatus: String?,
        @RequestParam(value = "customerInternalReference", required = false) customerInternalReference: String?,
        @RequestParam(value = "transactionReference", required = false) transactionReference: String?
    ): UserAccount {
        logger.info("$transactionReference $customerInternalReference $transactionStatus")
        val userAccount = userAccountService.getUserAccountByEmail(customerInternalReference.toString())
        userAccount.kycVerified = VerificationStatus.IN_PROGRESS
        return userAccountService.userAccountRepository.save(userAccount)
    }

    @PostMapping("/jumio", consumes = ["application/x-www-form-urlencoded;charset=UTF-8"])
    @Operation(summary = "Receiver for JUMIO")
    @ApiResponses(
        ApiResponse(responseCode = "200")
    )
    fun logJumio(@RequestParam map: HashMap<String?, String?>): UserAccount {
        val kycLogs = KycLogs(
            0,
            map["callBackType"],
            map["callbackDate"],
            map["clientIp"],
            map["customerId"],
            map["firstAttemptDate"],
            map["idFirstName"],
            map["idDob"],
            map["idCountry"],
            map["idLastName"],
            map["idNumber"],
            map["idScanImage"],
            map["idScanImageBackside"],
            map["idScanImageFace"],
            map["idScanSource"],
            map["idScanStatus"],
            map["idSubtype"],
            map["identityVerification"],
            map["jumioIdScanReference"],
            map["merchantIdScanReference"],
            map["personalNumber"],
            map["transactionDate"],
            map["verificationStatus"].toVerificationStatus
        )

        val userAccount = userAccountService.getUserAccountByEmail(kycLogs.merchant_id_scan_reference.toString())

        // User Already Verified. No need to update.
        if (userAccount.kycVerified == VerificationStatus.APPROVED_VERIFIED) {
            return userAccount
        }

        if (map["identityVerification"]?.contains("\"NO_MATCH\"") == true) {
            userAccount.kycVerified = VerificationStatus.DENIED_UNSUPPORTED_ID_TYPE
        } else {
            userAccount.kycVerified = kycLogs.verificationStatus
        }

        userAccountService.userAccountRepository.save(userAccount)
        kycLogs.userAccount = userAccount
        kycLogRepository.save(kycLogs)
        val status = if (userAccount.kycVerified == VerificationStatus.APPROVED_VERIFIED) {
            "Approved"
        } else {
            "Failed"
        }
        val mesg = if (userAccount.kycVerified == VerificationStatus.APPROVED_VERIFIED) {
            "Welcome To WadzPay App."
        } else {
            "You Can Re-Apply"
        }
        println(mesg)
        notificationService.sendPushNotifications(userAccount, "Your KYC verification is: $status", mesg)

        return userAccount
    }

    @PostMapping("/vePayPayment")
    @Operation(summary = "vePay Payment webhook response")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
    )
    fun vePayPayment(@RequestBody vePayWebhookResponse: VePayWebhookResponse): VePayWebhookRes? {
        logger.info("Received vePayPayment: $vePayWebhookResponse")
        apisLoggerRepository.save(ApisLog("VePayWebhookResponse", vePayWebhookResponse.toString(), "", ""))
        paymentPocService.updateDepositFiatTxn(vePayWebhookResponse)
        return VePayWebhookRes(status = true)
    }

    @PostMapping("/vePayPayout")
    @Operation(summary = "vePay Payout webhook response")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
    )
    fun vePayPayout(@RequestBody vePayPaymentTransferResponse: VePayPaymentTransferResponse): VePayWebhookRes? {
        logger.info("Received vePayPayout: $vePayPaymentTransferResponse")
        apisLoggerRepository.save(ApisLog("VePayoutWebhookResponse", vePayPaymentTransferResponse.toString(), "", ""))
        paymentPocService.updateWithdrawFiatTxn(vePayPaymentTransferResponse)
        return VePayWebhookRes(status = true)
    }

    //
    @PostMapping("/privatechainService")
    @Operation(summary = "Receiver for Omni webhook")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.TRANSFER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "422",
            description = "${ErrorCodes.INVALID_TRANSACTION_TYPE}, " +
                "${ErrorCodes.TRANSACTION_ALREADY_FINALIZED}, " +
                ErrorCodes.WRONG_AMOUNT,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getPrivateChainService(@RequestBody notification: PrivateChainEvent) {
        print("Received privatechain notification: $notification")
        logger.info("Received privatechain notification: $notification")
        apisLoggerRepository.save(ApisLog("privatechain Notification", notification.toString(), "", ""))
        logger.info("Confirming privatechain transfer ${notification.transfer}.")
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class BitGoEvent(
    /** type of webhook we are reacting to for now only "transfer" */
    val type: BitGoType,
    /** wallet id */
    val wallet: String,
    /** transaction hash */
    val hash: String,
    /** crypto coin code - e.g. btc, tbtc, eth */
    val coin: BitGoCoin,
    /** transfer id */
    val transfer: String,
    /** state of the tx, ideally "processed" */
    val state: BitGoState
)

enum class BitGoType { transfer, transaction, pendingapproval, address_confirmation }

// enum class BitGoCoin { btc, tbtc, eth, gteth, usdt, tusdt, wtk, twtk, usdc, tusdc, algo, talgo }
enum class BitGoCoin(val coinCode: String) { btc("btc"), tbtc("tbtc"), eth("eth"), gteth("gteth"), usdt("usdt"), tusdt("tusdt"), wtk("wtk"), twtk("twtk"), usdc("usdc"), tusdc("tusdc"), algo("algo"), talgo("talgo"), usdca("algo:USDC-31566704"), tusdca("talgo:USDC-10458941"), sart("sart") }

fun BitGoCoin.toCurrencyUnit(): CurrencyUnit {
    return when (BitGoCoin.valueOf(name)) {
        BitGoCoin.btc -> CurrencyUnit.BTC
        BitGoCoin.tbtc -> CurrencyUnit.BTC
        BitGoCoin.eth -> CurrencyUnit.ETH
        BitGoCoin.gteth -> CurrencyUnit.ETH
        BitGoCoin.usdt -> CurrencyUnit.USDT
        BitGoCoin.tusdt -> CurrencyUnit.USDT
        BitGoCoin.twtk -> CurrencyUnit.WTK
        BitGoCoin.wtk -> CurrencyUnit.WTK
        BitGoCoin.tusdc -> CurrencyUnit.USDC
        BitGoCoin.usdc -> CurrencyUnit.USDC
        BitGoCoin.algo -> CurrencyUnit.ALGO
        BitGoCoin.talgo -> CurrencyUnit.ALGO
        BitGoCoin.usdca -> CurrencyUnit.USDCA
        BitGoCoin.tusdca -> CurrencyUnit.USDCA
        BitGoCoin.sart -> CurrencyUnit.SART
    }
}

enum class BitGoState { unconfirmed, new, pending, confirmed, failed }
@JsonIgnoreProperties(ignoreUnknown = true)
data class PrivateChainEvent(
    /** type of webhook we are reacting to for now only "transfer" */
    val type: PrivateChainType,
    /** wallet id */
    val wallet: String,
    /** transaction hash */
    val hash: String,
    /** crypto coin code - e.g. btc, tbtc, eth */
    val coin: PrivateChainCoin,
    /** transfer id */
    val transfer: String,
    /** state of the tx, ideally "processed" */
    val state: PrivateChainState
)
enum class PrivateChainType { transfer, transaction, pendingapproval, address_confirmation }
enum class PrivateChainCoin(val coinCode: String) { btc("btc"), tbtc("tbtc"), eth("eth"), gteth("gteth"), usdt("usdt"), tusdt("tusdt"), wtk("wtk"), twtk("twtk"), usdc("usdc"), tusdc("tusdc"), algo("algo"), talgo("talgo"), usdca("algo:USDC-31566704"), tusdca("talgo:USDC-10458941"), sart("sart") }
enum class PrivateChainState { unconfirmed, new, pending, confirmed, failed }
