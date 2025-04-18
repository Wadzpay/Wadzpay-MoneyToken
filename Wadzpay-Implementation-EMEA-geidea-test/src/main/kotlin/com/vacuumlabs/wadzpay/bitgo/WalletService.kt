package com.vacuumlabs.wadzpay.bitgo

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vacuumlabs.block_confirmation
import com.vacuumlabs.wadzpay.algocutomtoken.AlgoCutomeTokenService
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ServiceUnavailableException
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.exchange.ExchangeService
import com.vacuumlabs.wadzpay.gap600.Gap600CoinType
import com.vacuumlabs.wadzpay.gap600.Gap600Service
import com.vacuumlabs.wadzpay.gap600.gap600source
import com.vacuumlabs.wadzpay.issuance.IssuanceWalletService
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import com.vacuumlabs.wadzpay.issuance.models.Status
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.LedgerService
import com.vacuumlabs.wadzpay.ledger.model.AccountOwner
import com.vacuumlabs.wadzpay.ledger.model.CryptoAddressRepository
import com.vacuumlabs.wadzpay.ledger.model.RefundStatus
import com.vacuumlabs.wadzpay.ledger.model.Transaction
import com.vacuumlabs.wadzpay.ledger.model.TransactionRefundDetails
import com.vacuumlabs.wadzpay.ledger.model.TransactionRefundDetailsRepository
import com.vacuumlabs.wadzpay.ledger.model.TransactionRepository
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.ledger.model.WadpayRefundTransactions
import com.vacuumlabs.wadzpay.ledger.model.WadpayRefundTransactionsRepository
import com.vacuumlabs.wadzpay.ledger.service.TransactionService
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.pos.PosWadzPayFeeService
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.utils.ApisLog
import com.vacuumlabs.wadzpay.utils.ApisLoggerRepository
import com.vacuumlabs.wadzpay.utils.BlockConfirmationLogger
import com.vacuumlabs.wadzpay.utils.BlockConfirmationRepository
import com.vacuumlabs.wadzpay.webhook.BitGoCoin
import com.vacuumlabs.wadzpay.webhook.toCurrencyUnit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.ResourceAccessException
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.util.UUID
import kotlin.Exception

@Service
class WalletService(
    val bitGoWallet: BitGoWallet,
    val ledgerService: LedgerService,
    val appConfig: AppConfig,
    val cryptoAddressRepository: CryptoAddressRepository,
    val transactionRepository: TransactionRepository,
    val transactionService: TransactionService,
    val apisLoggerRepository: ApisLoggerRepository,
    val exchangeService: ExchangeService,
    val posWadzPayFeeService: PosWadzPayFeeService,
    val wadpayRefundTransactionsRepository: WadpayRefundTransactionsRepository,
    val gap600Service: Gap600Service,
    val blockConfirmRepository: BlockConfirmationRepository,
    val transactionRefundDetailsRepository: TransactionRefundDetailsRepository,
    val algoCutomeTokenService: AlgoCutomeTokenService,
    val userAccountService: UserAccountService,
    val issuanceWalletService: IssuanceWalletService
) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    data class BitgoError(

        var name: String? = null,
        var error: String? = null,
        var status: Int? = null,
        var requestId: String? = null,
        var message: String? = null,
        var bitgoJsVersion: String? = null,
        var bitgoExpressVersion: String? = null

    )
    fun confirmTx(coin: BitGoCoin, walletId: String, transferId: String) {
        val bitGoTransfer = bitGoWallet.getTransfer(coin, walletId, transferId) ?: throw EntityNotFoundException(
            ErrorCodes.TRANSFER_NOT_FOUND
        )
        apisLoggerRepository.save(
            ApisLog(
                "bitGoTransfer", bitGoTransfer.toString(), "WalletId:- $walletId",
                "TransferredId:- $transferId"
            )
        )
        logger.info("BitGo transfer info: $bitGoTransfer")

        if (bitGoTransfer.type == BitGoTransferType.send) {
            confirmSend(bitGoTransfer, walletId)
        } else {
            logger.info("Verifying external receive!")
            try {
                val allBitGoEntry =
                    bitGoTransfer.entries.filter { it.valueString > BigDecimal.ZERO && it.wallet == walletId }

                for (entry in allBitGoEntry) {
                    ledgerService.receiveExternalMoney(coin.toCurrencyUnit(), entry, bitGoTransfer)
                }
                /*
                val bitGoEntry =
                    bitGoTransfer.entries.find { it.valueString > BigDecimal.ZERO && it.wallet == walletId }
                bitGoEntry?.let { ledgerService.receiveExternalMoney(coin.toCurrencyUnit(), it, bitGoTransfer) }
                */
            } catch (ex: Exception) {
                logger.error(ex.message)
                apisLoggerRepository.save(
                    ApisLog(
                        "bitGoTransferEx", bitGoTransfer.toString(), "WalletId:- $walletId $ex",
                        ex.message.toString()
                    )
                )
                // we don't rethrow exceptions here because it would trigger a BitGo retry policy
            }
        }
    }

    // Below gap600_zero_confimation_call funcation calls service for Gap600 confirmation service
    // This function will send CoinType, Wallet and Transfer details
    fun gap600_zero_confimation_call(
        coin: BitGoCoin,
        walletId: String,
        transferId: String
    ) {
        //
        val bitGoTransfer = bitGoWallet.getTransfer(coin, walletId, transferId) ?: throw EntityNotFoundException(
            ErrorCodes.TRANSFER_NOT_FOUND
        )

        logger.info("Checking for zero confirmation to call Gap600 API...")
        apisLoggerRepository.save(
            ApisLog(
                "bitGoTransfer", bitGoTransfer.toString(), "WalletId:- $walletId",
                "TransferredId:- $transferId"
            )
        )
        logger.info("BitGo transfer info: $bitGoTransfer")
        // Below if condition checks if Currency Unit is 'BTC'
        // Gap600 Confirmation will be called only if currncy unit is 'BTC'
        var coinType = coin.toCurrencyUnit().toString()
        if (coinType == Gap600CoinType.valueOf(coinType).toString()) {
            logger.info("calling gap600 with transaction-hash:${bitGoTransfer.txid}")
            // below code is checking for valueString < 0 in "entries" to filter and
            // get the sender walletId
            val gapBitGoEntry =
                bitGoTransfer.entries.filter { it.valueString > BigDecimal.ZERO && it.wallet == walletId }

            // iterating the entry details to map the sender details to Gap600 rest api request body
            for (entry in gapBitGoEntry) {
                logger.info("Transaction Id-->${bitGoTransfer.txid}")
                logger.info("calling gap600 with wallet id:${entry.wallet} and output-address:${entry.address} and valueString:${entry.valueString}")
                val gap600ResponseMessage =
                    gap600Service.getConfirmationStatus(coin, bitGoTransfer.txid, entry.address)
                /**  2. If gap600 returns successfull response
                 *      Then Get status, txValueBTC from gap600 response
                 */
                if (gap600ResponseMessage == null || gap600ResponseMessage.status.isEmpty()) {
                    return
                }
                val gap600RespMsgConfrStatus = gap600ResponseMessage.status
                val gap600RespMsgTxValueBTC = gap600ResponseMessage.txValueBTC
                logger.info("gap600RespMsgConfrStatus:$gap600RespMsgConfrStatus and gap600RespMsgTxValueBTC:$gap600RespMsgTxValueBTC")

                logger.info("Adding new block confirmation log record for Gap600")
                // getting data from block_confirmation_log table for the transactid
                var gap600blockconfirmentry = BlockConfirmationLogger(
                    bitGoTransfer.txid,
                    bitGoTransfer.id,
                    entry.wallet,
                    TransactionType.OTHER,
                    block_confirmation
                )
                gap600blockconfirmentry.confirmationStatus = gap600RespMsgConfrStatus
                gap600blockconfirmentry.confirmationSource = gap600source
                // saving confirmationStatus and confirmationSource into block_confirmation_log
                blockConfirmRepository.save(
                    gap600blockconfirmentry
                )
                if (gap600ResponseMessage.status.equals("confirmed", true)) {
                    val allPositiveBitGoEntry =
                        bitGoTransfer.entries.filter { it.valueString > BigDecimal.ZERO && it.wallet == walletId }
                    for (e in allPositiveBitGoEntry) {
                        println(e)
                        ledgerService.receiveExternalMoney(coin.toCurrencyUnit(), e, bitGoTransfer)
                    }
                }
            }
        }
    }

    fun mapBitGoTransferTypeToTransferType(bitGoTransfer: BitGoTransfer): TransactionType {
        if (bitGoTransfer.type == BitGoTransferType.send)
            return TransactionType.EXTERNAL_SEND
        else
            return TransactionType.EXTERNAL_RECEIVE
    }

    @Transactional
    fun sendRefundToExternalWallet(
        refundedBy: AccountOwner,
        refundAmountDigital: BigDecimal,
        assetDigital: CurrencyUnit,
        refundedToAddress: String,
        description: String? = null,
        transactionType: TransactionType,
        refundFiatType: FiatCurrencyUnit? = null,
        refundAmountFiat: BigDecimal? = BigDecimal.ZERO,
        blockchainTxId: String? = null,
        refundTransaction: TransactionRefundDetails
    ): TransactionRefundDetails {

        var amountToRefundDigital =
            posWadzPayFeeService.getRefundDigitalAmountAfterFee(refundAmountDigital, assetDigital)
        val feeByWadzPay = refundAmountDigital - amountToRefundDigital
        if (amountToRefundDigital <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }

        if (amountToRefundDigital.stripTrailingZeros().scale() > assetDigital.maximumNumberOfDigits.toInt()) {
            val am =
                amountToRefundDigital.setScale(assetDigital.maximumNumberOfDigits.toInt(), RoundingMode.UP)
                    .stripTrailingZeros()
            amountToRefundDigital = am
        }
        println("refundAmountDigitalNew:- $amountToRefundDigital")

        var estimatedFee = bitGoWallet.getFeeEstimate(
            assetDigital.toBitGoCoin(true),
            refundedToAddress,
            amountToRefundDigital
        )

        println("estimatedFee:- $estimatedFee")

        if (assetDigital.onEthBlockchain()) {
            // Refund Digital type is Not on ETH
            // 1 USDT = X ETH
            val exchangeRate = exchangeService.getExchangeRate(assetDigital, CurrencyUnit.ETH)
            println("exchangeRate:- $exchangeRate")
            val estimatedFeeInRefundAsset = estimatedFee * exchangeRate[CurrencyUnit.ETH]!!
            println("estimatedFeeInRefundAsset:- $estimatedFeeInRefundAsset")
            amountToRefundDigital -= estimatedFeeInRefundAsset
            estimatedFee = estimatedFeeInRefundAsset
        } else {
            // BTC FOUND
            amountToRefundDigital -= estimatedFee
        }

        if (amountToRefundDigital.stripTrailingZeros().scale() > assetDigital.maximumNumberOfDigits.toInt()) {
            val am =
                amountToRefundDigital.setScale(assetDigital.maximumNumberOfDigits.toInt(), RoundingMode.DOWN)
                    .stripTrailingZeros()
            amountToRefundDigital = am
        }
        var refundAmountDigitalNew = refundAmountDigital
        if (refundAmountDigitalNew.stripTrailingZeros().scale() > assetDigital.maximumNumberOfDigits.toInt()) {
            val am =
                refundAmountDigitalNew.setScale(assetDigital.maximumNumberOfDigits.toInt(), RoundingMode.DOWN)
                    .stripTrailingZeros()
            refundAmountDigitalNew = am
        }

        if (amountToRefundDigital <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.AFTER_DEDUCTING_FEE_FINAL_AMOUNT_IS_ZERO_OR_NEGATIVE)
        }

        println("RefundAmount:- $amountToRefundDigital")

        val bitGoExternal = try {
            // bitGoWallet.sendToExternalWallet(amountToRefundDigital, assetDigital, refundedToAddress)
            if (appConfig.environment.equals("dev", true)) {
                if (appConfig.stubEnable) {
                    sendToExternalWalletReverse(amountToRefundDigital, assetDigital, refundedToAddress)
                } else {
                    bitGoWallet.sendToExternalWallet(amountToRefundDigital, assetDigital, refundedToAddress)
                }
            } else {
                bitGoWallet.sendToExternalWallet(amountToRefundDigital, assetDigital, refundedToAddress)
            }
        } catch (ex: Exception) {
            apisLoggerRepository.save(
                ApisLog(
                    "refundError", ex.message.toString(), "amountToRefundDigital",
                    "${assetDigital.toBitGoCoin(appConfig.production)} $refundedToAddress"
                )
            )

            try {
                val json = ex.message?.split(":", limit = 3)?.get(2) ?: ""
                val typeToken = object : TypeToken<List<BitgoError>>() {}.type
                val errors = Gson().fromJson<List<BitgoError>>(json, typeToken)
                throw errors[0].error?.let { BadRequestException(it) }!!
            } catch (e: Exception) {
                throw ServiceUnavailableException("ERROR: " + ex.message.toString())
            }
        }
        val wadzpayAccount = ledgerService.getOmnibusAccount()

        refundTransaction.numberOfRefunds = transactionService.countOfSuccessRefunds(refundTransaction.transaction) + 1
        refundTransaction.refundStatus = RefundStatus.REFUNDED
        refundTransaction.refundSettlementDate = Instant.now()
        refundTransaction.refundFiatType = refundFiatType
        refundTransaction.refundAmountFiat = refundAmountFiat
        refundTransaction.refundBlockchainHash = bitGoExternal.txid
        transactionRefundDetailsRepository.save(refundTransaction)

        val transaction = refundTransaction.transaction
        transaction.totalRefundedAmountFiat = transaction.totalRefundedAmountFiat.add(refundTransaction.refundAmountFiat)
        transactionRepository.save(transaction)

        wadpayRefundTransactionsRepository.save(
            WadpayRefundTransactions(
                owner = refundedBy,
                transaction = refundTransaction,
                address = refundedToAddress,
                assetCrypto = assetDigital,
                type = TransactionType.REFUND,
                blockchainHash = blockchainTxId,
                description = "Refunded",
                amountCrypto = refundAmountDigital,
                feeByWadzpay = feeByWadzPay,
                feeByBlockchain = estimatedFee
            )
        )
        return refundTransaction
    }

    fun sendToExternalWalletReverse(
        amount: BigDecimal,
        asset: CurrencyUnit,
        address: String
    ): BitGoExternalTransaction {
        var blockChainAddress = "Fake commit No Hash " + Instant.now().epochSecond
        var bitGoTransfer = BitGoTransfer(
            id = "",
            txid = UUID.randomUUID().toString(),
            type = BitGoTransferType.send,
            coin = BitGoCoin.btc,
            baseValueString = amount,
            feeString = BigDecimal.ZERO
        )
        var bitGoExternalTransaction = BitGoExternalTransaction(
            transfer = bitGoTransfer,
            tx = "",
            txid = UUID.randomUUID().toString(),
            status = "success"
        )
        return bitGoExternalTransaction
    }

    @Transactional
    fun sendToExternalWallet(
        sender: AccountOwner,
        amountx: BigDecimal,
        asset: CurrencyUnit,
        address: String,
        description: String? = null,
        transactionType: TransactionType,
        refundFiatType: FiatCurrencyUnit? = null,
        refundAmountFiat: BigDecimal? = BigDecimal.ZERO
    ): Transaction {
        // if (cryptoAddressRepository.countByAddress(address) != 0L) {
        //   throw UnprocessableEntityException(ErrorCodes.EXTERNAL_SEND_INSIDE_THE_SYSTEM) TODO: Do P2P trx
        // }

        var amount = amountx

        if (amount <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (amount.stripTrailingZeros().scale() > asset.maximumNumberOfDigits.toInt()) {
            val am =
                amount.setScale(asset.maximumNumberOfDigits.toInt(), RoundingMode.UP).stripTrailingZeros()
            amount = am
            //  throw BadRequestException(ErrorCodes.INVALID_AMOUNT_TOO_MANY_DECIMAL_PLACES)
        }

        val estimatedFee = bitGoWallet.getFeeEstimate(asset.toBitGoCoin(appConfig.production), address, amount)
        val estimatedAmount = amount
        apisLoggerRepository.save(
            ApisLog(
                "BitGoExtmatedFee",
                estimatedAmount.toString(),
                "Base amount: $amountx $asset",
                address
            )
        )
        ledgerService.reserveMoney(sender, asset, estimatedAmount)

        val bitGoExternalTransaction = try {
            bitGoWallet.sendToExternalWallet(amount, asset, address)
        } catch (ex: ResourceAccessException) {
            logger.info(ex.message)
            ledgerService.returnMoney(sender, asset, estimatedAmount)
            apisLoggerRepository.save(
                ApisLog(
                    "sendToExternalWallet 1", ex.message.toString(), sender.account.getOwnerName().toString(),
                    "${asset.toBitGoCoin(appConfig.production)} $address"
                )
            )
            throw ServiceUnavailableException(ErrorCodes.WALLET_NOT_AVAILABLE)
        } catch (ex: BadRequestException) {
            ledgerService.returnMoney(sender, asset, estimatedAmount)
            apisLoggerRepository.save(
                ApisLog(
                    "sendToExternalWallet 2",
                    ex.message.toString(),
                    sender.account.getOwnerName().toString() + "   " + ex.localizedMessage,
                    "${asset.toBitGoCoin(appConfig.production)} $address"
                )
            )
            if (ex.message.toString().contains("InsufficientBalance"))
                throw ServiceUnavailableException(ErrorCodes.INSUFFICIENT_FUND)
            if (ex.message.toString().contains("InvalidAmount"))
                throw ServiceUnavailableException(ErrorCodes.INVALID_AMOUNT)
            else
                throw ServiceUnavailableException(ErrorCodes.INVALID_WALLET_ADDRESS)
        } catch (ex: Exception) {
            ledgerService.returnMoney(sender, asset, estimatedAmount)
            logger.info(ex.message)
            apisLoggerRepository.save(
                ApisLog(
                    "sendToExternalWallet 3", ex.message.toString(), sender.account.getOwnerName().toString(),
                    "${asset.toBitGoCoin(appConfig.production)} $address"
                )
            )
            if (ex.message == null)
                throw ServiceUnavailableException(ErrorCodes.UNKNOWN_WALLET_ERROR)
            if (ex.message.toString().contains("InsufficientBalance"))
                throw ServiceUnavailableException(ErrorCodes.INSUFFICIENT_FUND)
            if (ex.message.toString().contains("InvalidAmount"))
                throw ServiceUnavailableException(ErrorCodes.INVALID_AMOUNT)
            else
                throw ServiceUnavailableException(ErrorCodes.INVALID_WALLET_ADDRESS)
        }

        logger.info("BitGo external transaction: $bitGoExternalTransaction")
        apisLoggerRepository.save(
            ApisLog(
                "ExternalSendResponse",
                bitGoExternalTransaction.toString(),
                "$address $asset",
                amountx.toString()
            )
        )

        ledgerService.returnMoney(sender, asset, estimatedAmount)
        val fee = bitGoExternalTransaction.transfer.normalizedFee()

        val transaction = ledgerService.createExternalTransaction(
            sender,
            amount,
            fee,
            asset,
            bitGoExternalTransaction.transfer.txid,
            description,
            status = TransactionStatus.SUCCESSFUL,
            transactionType
        )
        transaction.fiatAmount = refundAmountFiat
        transaction.fiatAsset = refundFiatType
        if (refundFiatType != null) {
            transaction.status = TransactionStatus.REFUNDED
        }
        transactionService.showPushNotifications(transaction)

        return transaction
    }

    private fun containsOnramperAddress(bitGoTransfer: BitGoTransfer): Boolean {
        val asset = bitGoTransfer.coin.toCurrencyUnit()
        return bitGoTransfer.entries.any {
            it.address == asset.toAddress(appConfig)
        }
    }

    @Transactional
    fun confirmSend(bitGoTransfer: BitGoTransfer, walletId: String) {
        apisLoggerRepository.save(
            ApisLog(
                "WalletSendConfirm",
                bitGoTransfer.txid,
                bitGoTransfer.id,
                bitGoTransfer.toString()
            )
        )

        val allBitGoEntry =
            bitGoTransfer.entries.filter { it.valueString > BigDecimal.ZERO && it.wallet == walletId }

        for (entry in allBitGoEntry) {
            ledgerService.receiveExternalMoney(bitGoTransfer.coin.toCurrencyUnit(), entry, bitGoTransfer)
        }

        val fee = bitGoTransfer.feeString.normalize(bitGoTransfer.coin)

        // val pendingTransaction = transactionRepository.getByBlockchainTxId(bitGoTransfer.txid)!!

        /*  if (fee != pendingTransaction.fee) {
              apisLoggerRepository.save(
                  ApisLog(
                      "FeeChangedForTrx",
                      pendingTransaction.toString(),
                      "Fee charged = " + pendingTransaction.fee + " Fee Applied:- " + fee,
                      bitGoTransfer.toString()
                  )
              )
          }*/

        /* val refundableAmount = (pendingTransaction.amount + pendingTransaction.fee) - fee
         ledgerService.returnMoney(
             pendingTransaction.sender.account.owner!!,
             pendingTransaction.asset,
             refundableAmount
         )
         pendingTransaction.fee = fee
         pendingTransaction.status = TransactionStatus.SUCCESSFUL
         transactionRepository.save(pendingTransaction)*/

        /*    if (bitGoTransfer.coin.toCurrencyUnit().onEthBlockchain()) {

                val fee = bitGoTransfer.feeString.normalize(bitGoTransfer.coin)

                val pendingTransaction = transactionRepository.getByBlockchainTxId(bitGoTransfer.txid)!!

                ledgerService.returnMoney(
                    pendingTransaction.sender.account.owner!!,
                    pendingTransaction.asset,
                    pendingTransaction.amount + pendingTransaction.fee
                )

                pendingTransaction.fee = fee

               // ledgerService.confirmPendingExternalTransaction(pendingTransaction)
            }*/
    }

    @Transactional
    fun sendToExternalWalletPrivateBlockChain(
        sender: AccountOwner,
        amountx: BigDecimal,
        asset: CurrencyUnit,
        address: String,
        description: String? = null,
        transactionType: TransactionType,
        refundFiatType: FiatCurrencyUnit? = null,
        refundAmountFiat: BigDecimal? = BigDecimal.ZERO,
        issuanceBanks: IssuanceBanks? = null
    ): Transaction {
        var amount = amountx

        if (amount <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }
        if (amount.stripTrailingZeros().scale() > asset.maximumNumberOfDigits.toInt()) {
            val am =
                amount.setScale(asset.maximumNumberOfDigits.toInt(), RoundingMode.UP).stripTrailingZeros()
            amount = am
            //  throw BadRequestException(ErrorCodes.INVALID_AMOUNT_TOO_MANY_DECIMAL_PLACES)
        }
        val transaction = ledgerService.createExternalTransaction(
            sender,
            amount,
            BigDecimal.ZERO,
            asset,
            UUID.randomUUID().toString(),
            description,
            status = TransactionStatus.SUCCESSFUL,
            transactionType,
            issuanceBanks
        )
        transactionService.showPushNotifications(transaction)
        return transaction
    }
//
    @Transactional
    fun sendRefundToExternalWalletDev(
        refundedBy: AccountOwner,
        refundAmountDigital: BigDecimal,
        assetDigital: CurrencyUnit,
        refundedToAddress: String,
        description: String? = null,
        transactionType: TransactionType,
        refundFiatType: FiatCurrencyUnit? = null,
        refundAmountFiat: BigDecimal? = BigDecimal.ZERO,
        blockchainTxId: String? = null,
        refundTransaction: TransactionRefundDetails
    ): TransactionRefundDetails {

        var amountToRefundDigital =
            posWadzPayFeeService.getRefundDigitalAmountAfterFee(refundAmountDigital, assetDigital)
        val feeByWadzPay = refundAmountDigital - amountToRefundDigital
        if (amountToRefundDigital <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }

        if (amountToRefundDigital.stripTrailingZeros().scale() > assetDigital.maximumNumberOfDigits.toInt()) {
            val am =
                amountToRefundDigital.setScale(assetDigital.maximumNumberOfDigits.toInt(), RoundingMode.UP)
                    .stripTrailingZeros()
            amountToRefundDigital = am
        }
        println("refundAmountDigitalNew:- $amountToRefundDigital")

    /* var estimatedFee = bitGoWallet.getFeeEstimate(
         assetDigital.toBitGoCoin(true),
         refundedToAddress,
         amountToRefundDigital
     )*/
        var estimatedFee = BigDecimal(0.000001)
        println("estimatedFee:- $estimatedFee")

        if (assetDigital.onEthBlockchain()) {
            // Refund Digital type is Not on ETH
            // 1 USDT = X ETH
            val exchangeRate = exchangeService.getExchangeRate(assetDigital, CurrencyUnit.ETH)
            println("exchangeRate:- $exchangeRate")
            val estimatedFeeInRefundAsset = estimatedFee * exchangeRate[CurrencyUnit.ETH]!!
            println("estimatedFeeInRefundAsset:- $estimatedFeeInRefundAsset")
            amountToRefundDigital -= estimatedFeeInRefundAsset
            estimatedFee = estimatedFeeInRefundAsset
        } else {
            // BTC FOUND
            amountToRefundDigital -= estimatedFee
        }

        if (amountToRefundDigital.stripTrailingZeros().scale() > assetDigital.maximumNumberOfDigits.toInt()) {
            val am =
                amountToRefundDigital.setScale(assetDigital.maximumNumberOfDigits.toInt(), RoundingMode.DOWN)
                    .stripTrailingZeros()
            amountToRefundDigital = am
        }
        var refundAmountDigitalNew = refundAmountDigital
        if (refundAmountDigitalNew.stripTrailingZeros().scale() > assetDigital.maximumNumberOfDigits.toInt()) {
            val am =
                refundAmountDigitalNew.setScale(assetDigital.maximumNumberOfDigits.toInt(), RoundingMode.DOWN)
                    .stripTrailingZeros()
            refundAmountDigitalNew = am
        }

        if (amountToRefundDigital <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.AFTER_DEDUCTING_FEE_FINAL_AMOUNT_IS_ZERO_OR_NEGATIVE)
        }

        println("RefundAmount:- $amountToRefundDigital")

    /*val bitGoExternal = try {
        // bitGoWallet.sendToExternalWallet(amountToRefundDigital, assetDigital, refundedToAddress)
        if (appConfig.environment.equals("dev", true)) {
            if (appConfig.stubEnable) {
                sendToExternalWalletReverse(amountToRefundDigital, assetDigital, refundedToAddress)
            } else {
                bitGoWallet.sendToExternalWallet(amountToRefundDigital, assetDigital, refundedToAddress)
            }
        } else {
            bitGoWallet.sendToExternalWallet(amountToRefundDigital, assetDigital, refundedToAddress)
        }
    } catch (ex: Exception) {
        apisLoggerRepository.save(
            ApisLog(
                "refundError", ex.message.toString(), "amountToRefundDigital",
                "${assetDigital.toBitGoCoin(appConfig.production)} $refundedToAddress"
            )
        )

        try {
            val json = ex.message?.split(":", limit = 3)?.get(2) ?: ""
            val typeToken = object : TypeToken<List<BitgoError>>() {}.type
            val errors = Gson().fromJson<List<BitgoError>>(json, typeToken)
            throw errors[0].error?.let { BadRequestException(it) }!!
        } catch (e: Exception) {
            throw ServiceUnavailableException("ERROR: " + ex.message.toString())
        }
    }
    val wadzpayAccount = ledgerService.getOmnibusAccount()*/

        refundTransaction.numberOfRefunds = transactionService.countOfSuccessRefunds(refundTransaction.transaction) + 1
        refundTransaction.refundStatus = RefundStatus.REFUNDED
        refundTransaction.refundSettlementDate = Instant.now()
        refundTransaction.refundFiatType = refundFiatType
        refundTransaction.refundAmountFiat = refundAmountFiat
        // refundTransaction.refundBlockchainHash = bitGoExternal.txid
        transactionRefundDetailsRepository.save(refundTransaction)
        val transaction = refundTransaction.transaction
        transaction.totalRefundedAmountFiat = transaction.totalRefundedAmountFiat.add(refundTransaction.refundAmountFiat)
        transactionRepository.save(transaction)

        wadpayRefundTransactionsRepository.save(
            WadpayRefundTransactions(
                owner = refundedBy,
                transaction = refundTransaction,
                address = refundedToAddress,
                assetCrypto = assetDigital,
                type = TransactionType.REFUND,
                blockchainHash = blockchainTxId,
                description = "Refunded",
                amountCrypto = refundAmountDigital,
                feeByWadzpay = feeByWadzPay,
                feeByBlockchain = estimatedFee
            )
        )
        return refundTransaction
    }

    @Transactional
    fun sendRefundToExternalWalletAlgo(
        refundedBy: AccountOwner,
        refundAmountDigital: BigDecimal,
        assetDigital: CurrencyUnit,
        refundedToAddress: String,
        description: String? = null,
        transactionType: TransactionType,
        refundFiatType: FiatCurrencyUnit? = null,
        refundAmountFiat: BigDecimal? = BigDecimal.ZERO,
        blockchainTxId: String? = null,
        refundTransaction: TransactionRefundDetails,
        principal: Authentication?,
        transaction: Transaction
    ): TransactionRefundDetails {

        println("@729 sendRefundToExternalWalletAlgo")
        var amountToRefundDigital =
            posWadzPayFeeService.getRefundDigitalAmountAfterFee(refundAmountDigital, assetDigital)
        val feeByWadzPay = refundAmountDigital - amountToRefundDigital
        if (amountToRefundDigital <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        }

        if (amountToRefundDigital.stripTrailingZeros().scale() > assetDigital.maximumNumberOfDigits.toInt()) {
            val am =
                amountToRefundDigital.setScale(assetDigital.maximumNumberOfDigits.toInt(), RoundingMode.UP)
                    .stripTrailingZeros()
            amountToRefundDigital = am
        }
        println("refundAmountDigitalNew:- $amountToRefundDigital")

        var estimatedFee = BigDecimal.ZERO

        println("estimatedFee:- $estimatedFee")

        if (assetDigital.onEthBlockchain()) {
            // Refund Digital type is Not on ETH
            // 1 USDT = X ETH
            val exchangeRate = exchangeService.getExchangeRate(assetDigital, CurrencyUnit.ETH)
            println("exchangeRate:- $exchangeRate")
            val estimatedFeeInRefundAsset = estimatedFee * exchangeRate[CurrencyUnit.ETH]!!
            println("estimatedFeeInRefundAsset:- $estimatedFeeInRefundAsset")
            amountToRefundDigital -= estimatedFeeInRefundAsset
            estimatedFee = estimatedFeeInRefundAsset
        } else {
            // BTC FOUND
            amountToRefundDigital -= estimatedFee
            println("@761 amountToRefundDigital $amountToRefundDigital")
        }

        if (amountToRefundDigital.stripTrailingZeros().scale() > assetDigital.maximumNumberOfDigits.toInt()) {
            val am =
                amountToRefundDigital.setScale(assetDigital.maximumNumberOfDigits.toInt(), RoundingMode.DOWN)
                    .stripTrailingZeros()
            amountToRefundDigital = am
        }
        var refundAmountDigitalNew = refundAmountDigital
        if (refundAmountDigitalNew.stripTrailingZeros().scale() > assetDigital.maximumNumberOfDigits.toInt()) {
            val am =
                refundAmountDigitalNew.setScale(assetDigital.maximumNumberOfDigits.toInt(), RoundingMode.DOWN)
                    .stripTrailingZeros()
            refundAmountDigitalNew = am
        }

        if (amountToRefundDigital <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.AFTER_DEDUCTING_FEE_FINAL_AMOUNT_IS_ZERO_OR_NEGATIVE)
        }

        println("RefundAmount:- $amountToRefundDigital")

        /*val bitGoExternal = try {
            // bitGoWallet.sendToExternalWallet(amountToRefundDigital, assetDigital, refundedToAddress)
            if (appConfig.environment.equals("dev", true)) {
                    bitGoWallet.sendToExternalWalletAlgo(amountToRefundDigital, assetDigital, refundedToAddress)
            } else {
                bitGoWallet.sendToExternalWalletAlgo(amountToRefundDigital, assetDigital, refundedToAddress)
            }
        } catch (ex: Exception) {
            apisLoggerRepository.save(
                ApisLog(
                    "refundError", ex.message.toString(), "amountToRefundDigital",
                    "${assetDigital.toBitGoCoin(appConfig.production)} $refundedToAddress"
                )
            )

            try {
                val json = ex.message?.split(":", limit = 3)?.get(2) ?: ""
                val typeToken = object : TypeToken<List<BitgoError>>() {}.type
                val errors = Gson().fromJson<List<BitgoError>>(json, typeToken)
                throw errors[0].error?.let { BadRequestException(it) }!!
            } catch (e: Exception) {
                throw ServiceUnavailableException("ERROR: " + ex.message.toString())
            }
        }*/
        println("@820 algoCutomeTokenService")
        println("@824 refundedBy = $refundedBy")
//        val receiverAccount = userAccountService.getUserAccountByEmail("swati.srivastav@wadzpay.com")
        var receiverAccount1: AccountOwner? = transaction.sender.account.owner
        println("@824 receiverAccount1= $receiverAccount1")
//        val refundedToAddress1 = "54IVZ7SXYFIJTQ6Q4QZLKHBRTTRU3HG5QRZ6MC2LRCQMIWUHQFEY7GL7KI"
        val refundedToAddress1 = transaction.sender.account.getSubaccountByAsset(assetDigital).address?.address.toString()
        println("@824 refundedToAddress1= $refundedToAddress1")
        var issuanceBank: IssuanceBanks? = null
        if (assetDigital == CurrencyUnit.SART) {
            val receiverUserAccount = transaction.sender.userAccountId
            val issuanceBanksUserEntry = receiverUserAccount?.let { issuanceWalletService.getIssuanceBankMapping(it) }
            if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.status == Status.DISABLED) {
                throw EntityNotFoundException(ErrorCodes.WALLET_DISABLED)
            }
            if (issuanceBanksUserEntry != null) {
                issuanceBank = issuanceBanksUserEntry.issuanceBanksId
            }
        }
        val sartExternal = algoCutomeTokenService.sendToExternalWalletWadzpayPrivateBlockChain(
            refundedBy,
            receiverAccount1!!,
            amountToRefundDigital,
            assetDigital,
            refundedToAddress1,
            "description",
            TransactionType.REFUND,
            refundFiatType,
            refundAmountFiat,
            issuanceBank,
            refundTransaction.uuid
        )

        val wadzpayAccount = ledgerService.getOmnibusAccount()
        println("@834 wadzpayAccount")
        refundTransaction.numberOfRefunds = transactionService.countOfSuccessRefunds(refundTransaction.transaction) + 1
        refundTransaction.refundStatus = RefundStatus.REFUNDED
        refundTransaction.refundSettlementDate = Instant.now()
        refundTransaction.refundFiatType = refundFiatType
        refundTransaction.refundAmountFiat = refundAmountFiat
        refundTransaction.refundBlockchainHash = sartExternal.blockchainTxId
        val refundTransactionAfterSave = transactionRefundDetailsRepository.save(refundTransaction)
        println("@842 transactionRefundDetailsRepository")
        val transaction = refundTransaction.transaction
        transaction.totalRefundedAmountFiat = transaction.totalRefundedAmountFiat.add(refundTransaction.refundAmountFiat)
        println(transaction.uuid)
        transactionRepository.save(transaction)
        /* Update transaction refund details in transaction column * - Anita Prajapati start*/
        val transactionUUID = sartExternal.uuid
        val rTransaction = transactionRepository.getByUuid(transactionUUID)
        if (rTransaction != null) {
            rTransaction.refundTransactionId = refundTransactionAfterSave.uuid
            transactionRepository.save(rTransaction)
        }
        /* Update transaction refund details in transaction column * - Anita Prajapati end*/
        wadpayRefundTransactionsRepository.save(
            WadpayRefundTransactions(
                owner = refundedBy,
                transaction = refundTransaction,
                address = refundedToAddress,
                assetCrypto = assetDigital,
                type = TransactionType.REFUND,
                blockchainHash = blockchainTxId,
                description = "Refunded",
                amountCrypto = refundAmountDigital,
                feeByWadzpay = feeByWadzPay,
                feeByBlockchain = estimatedFee
            )
        )
        println("@861 wadpayRefundTransactionsRepository")
        return refundTransaction
    }
}
