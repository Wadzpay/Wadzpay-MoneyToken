package com.vacuumlabs.wadzpay.pos

import com.fasterxml.jackson.annotation.JsonFormat
import com.vacuumlabs.MERCHANT_OFFLINE_POS_ID
import com.vacuumlabs.SALT_KEY
import com.vacuumlabs.wadzpay.a.EncoderUtil
import com.vacuumlabs.wadzpay.accountowner.AccountOwnerService
import com.vacuumlabs.wadzpay.accountowner.EncryptQRRequest
import com.vacuumlabs.wadzpay.algocutomtoken.AlgoCustomTokenWallet
import com.vacuumlabs.wadzpay.bitgo.BitGoTransfer
import com.vacuumlabs.wadzpay.bitgo.BitGoTransferEntry
import com.vacuumlabs.wadzpay.bitgo.BitGoWallet
import com.vacuumlabs.wadzpay.bitgo.normalize
import com.vacuumlabs.wadzpay.bitgo.toBitGoCoin
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.UnprocessableEntityException
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.exchange.ExchangeService
import com.vacuumlabs.wadzpay.issuance.IssuanceCommonController
import com.vacuumlabs.wadzpay.issuance.IssuanceService
import com.vacuumlabs.wadzpay.issuance.IssuanceWalletService
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.LedgerInitializerService
import com.vacuumlabs.wadzpay.ledger.LedgerService
import com.vacuumlabs.wadzpay.ledger.model.Account
import com.vacuumlabs.wadzpay.ledger.model.AccountOwner
import com.vacuumlabs.wadzpay.ledger.model.CryptoAddressRepository
import com.vacuumlabs.wadzpay.ledger.model.RefundMode
import com.vacuumlabs.wadzpay.ledger.model.RefundOrigin
import com.vacuumlabs.wadzpay.ledger.model.RefundStatus
import com.vacuumlabs.wadzpay.ledger.model.RefundToken
import com.vacuumlabs.wadzpay.ledger.model.Transaction
import com.vacuumlabs.wadzpay.ledger.model.TransactionMode
import com.vacuumlabs.wadzpay.ledger.model.TransactionRefundDetails
import com.vacuumlabs.wadzpay.ledger.model.TransactionRepository
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.ledger.service.TransactionService
import com.vacuumlabs.wadzpay.merchant.InitiateWebLinkRefund
import com.vacuumlabs.wadzpay.merchant.InitiateWebLinkRefundPosRequest
import com.vacuumlabs.wadzpay.merchant.MerchantService
import com.vacuumlabs.wadzpay.merchant.RefundInitiationRequest
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.Merchant
import com.vacuumlabs.wadzpay.pos.model.PaymentMode
import com.vacuumlabs.wadzpay.pos.model.PosFees
import com.vacuumlabs.wadzpay.pos.model.PosPaymentListResponse
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.utils.BlockConfirmationLogger
import com.vacuumlabs.wadzpay.utils.BlockConfirmationRepository
import com.vacuumlabs.wadzpay.webhook.BitGoWebhookController
import com.vacuumlabs.wadzpay.webhook.PrivateChainCoin
import com.vacuumlabs.wadzpay.webhook.PrivateChainEvent
import com.vacuumlabs.wadzpay.webhook.PrivateChainState
import com.vacuumlabs.wadzpay.webhook.PrivateChainType
import org.apache.http.conn.HttpHostConnectException
import org.slf4j.LoggerFactory
import org.springframework.boot.configurationprocessor.json.JSONObject
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.InetAddress
import java.security.Principal
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.validation.ConstraintViolationException
import kotlin.collections.ArrayList
@Service
class PosService(
    val bitGoWallet: BitGoWallet,
    val algoCustomTokenWallet: AlgoCustomTokenWallet,
    val appConfig: AppConfig,
    val accountOwnerService: AccountOwnerService,
    val exchangeService: ExchangeService,
    val userAccountService: UserAccountService,
    val posTransactionRepository: PosTransactionRepository,
    val posSaltRepository: PosSaltRepository,
    val posLogCommitsRepository: PosLogCommitsRepository,
    val blockConfirmRepository: BlockConfirmationRepository,
    val ledgerService: LedgerService,
    val transactionRepository: TransactionRepository,
    val merchantService: MerchantService,
    val merchantPosRepository: MerchantPosRepository,
    val transactionService: TransactionService,
    val webhookController: BitGoWebhookController,
    val ledgerInitializerService: LedgerInitializerService,
    val issuanceWalletService: IssuanceWalletService,
    val cryptoAddressRepository: CryptoAddressRepository,
    val issuanceService: IssuanceService
) {
    val logger = LoggerFactory.getLogger(javaClass)
    fun createPosTransaction(
        principal: Principal,
        cryptoType: CurrencyUnit,
        faitType: FiatCurrencyUnit,
        posTransactionRequest: PosTransactionRequest
    ): PosTransactionResponse? {
        logger.info("createPosTransaction flow")
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        // sartSubaccountOfSender = userAccount.account.getSubaccountByAsset(cryptoType)
        var posTransaction: PosTransaction?
        try {
            val merchantData =
                userAccount.merchant ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)
            val merchantPos = merchantPosRepository.findByPosIdAndMerchant(posTransactionRequest.posId, merchantData)
                ?: throw EntityNotFoundException(ErrorCodes.INVALID_POS_ID)
//            val merchantWalletAddress1 = merchantService.getMerchantByName(principal.name).account.getSubaccountByAsset(CurrencyUnit.SART).address?.address
//            print("@101 $merchantWalletAddress1")
//            val merchantWalletAddress2 = merchantData.account.getSubaccountByAsset(CurrencyUnit.SART).address?.address
            val merchantWalletAddress2 = merchantData.account.getSubaccountByAsset(cryptoType).address!!.address
            print("@103 $merchantWalletAddress2")
//            val walletAddress = account.account.subaccounts.map { it.address }
            val address = // bitGoWallet.createAddressNotSave(cryptoType)
                if (cryptoType == CurrencyUnit.SART) {
//                    algoCustomTokenWallet.createAddressNotSave(cryptoType)
                    merchantData.account.getSubaccountByAsset(cryptoType).address!!.address
                } else {
                    if (appConfig.environment.equals("dev", true)) {
                        if (appConfig.stubEnable) {
                            UUID.randomUUID().toString()
                        } else {
                            bitGoWallet.createAddressNotSave(cryptoType.toString())
                        }
                    } else {
                        bitGoWallet.createAddressNotSave(cryptoType.toString())
                    }
                }
            // logger.info("createPosTransaction flow step -1")
            val cryptoAmountObj = getCryptoAmount(posTransactionRequest.fiatAmount, faitType, cryptoType)
            var cryptoAmount = cryptoAmountObj.amount
            logger.info("crypto amount :$cryptoAmount")
            val wadzPayFee = getMdrPercentage(cryptoAmount, cryptoType, merchantData.mdrPercentage)
            val saltKey = EncoderUtil.getNewSaltKey()
            val posSalt = posSaltRepository.save(PosSalt(saltKey))
            if (cryptoAmount.stripTrailingZeros().scale() > 8) {
                val am =
                    cryptoAmount.setScale(8, RoundingMode.UP).stripTrailingZeros()
                cryptoAmount = am
            }
            // logger.info("createPosTransaction flow step 2")
            val tempPosTransaction = PosTransaction(
                merchantData,
                merchantPos,
                cryptoType,
                faitType,
                address,
                posTransactionRequest.fiatAmount,
                cryptoAmount,
                wadzPayFee,
                BigDecimal.ZERO,
                TransactionType.POS,
                userAccount.email!!,
                null,
                posTransactionRequest.description,
                TransactionStatus.IN_PROGRESS, BigDecimal.ZERO, cryptoAmountObj.exchangeRate, BigDecimal.ZERO
            )
            posTransaction = posTransactionRepository.save(tempPosTransaction)
            posSalt.posTransaction = posTransaction
            posTransaction.saltkey = posSalt
            posSaltRepository.save(posSalt)
            // logger.info("createPosTransaction flow step 3")
            // logger.info( "Is SART added : ${ledgerInitializerService.isSARaAdded()}")
            var senderSubaccount = ledgerService.getOmnibusAccount()
            try {
                senderSubaccount.getSubaccountByAsset(cryptoType)
            } catch (e: EntityNotFoundException) {
                logger.info("Error :${e.message}")
                try {
                    logger.info("calling createAccountsFor")
                    ledgerInitializerService.createAccountsFor(cryptoType)
                } catch (e: Exception) {
                    logger.info(" error while calling createAccountsFor : ${e.message} ")
                }
            }
            // logger.info("createPosTransaction flow step 4")

            // for( subac in senderSubaccount.subaccounts)
            // {
            // logger.info("omnibus sub acc : ${subac.asset.toString()} and ${subac.reference.toString()}")
            // }

            // val sender1 = senderSubaccount.getSubaccountByAsset(cryptoType)
            // logger.info("createPosTransaction flow step 5")
            // val receiver1 = merchantData.account.getSubaccountByAsset(cryptoType)
            // logger.info("createPosTransaction flow step 6")
            senderSubaccount = ledgerService.getOmnibusAccount()
            val transaction = Transaction(
                sender = senderSubaccount.getSubaccountByAsset(cryptoType),
                receiver = merchantData.account.getSubaccountByAsset(cryptoType),
                asset = cryptoType.name,
                amount = cryptoAmount,
                fiatAsset = faitType,
                fiatAmount = posTransaction.amountfiat,
                status = TransactionStatus.IN_PROGRESS,
                type = TransactionType.POS,
                description = posTransaction.description,
                fee = wadzPayFee,
                extPosId = posTransactionRequest.extPosId,
                extPosSequenceNo = posTransactionRequest.extPosSequenceNo,
                extPosTransactionId = posTransactionRequest.extPosTransactionId,
                extPosLogicalDate = posTransactionRequest.extPosLogicalDate,
                extPosShift = posTransactionRequest.extPosShift,
                extPosActualDate = posTransactionRequest.extPosActualDate,
                extPosActualTime = posTransactionRequest.extPosActualTime,
                sourceWalletAddress = merchantWalletAddress2,
                txMode = TransactionMode.CUSTOMER_MERCHANT_ONLINE
            )
            transaction.posTransaction?.add(posTransaction)
            val trx = transactionRepository.save(transaction)

            posTransaction.transaction = transactionRepository.save(trx.setTxId("POS", trx.id))
            posTransaction.assetcrypto = cryptoType
            posTransactionRepository.save(posTransaction)
            logger.info("transaction is complete with : ${trx.id}")
            // val total = posTransaction.feewadzpay ?: (BigDecimal.ZERO + posTransaction.amountcrypto!!)
            // val total = cryptoAmount !! + wadzPayFee!!
        } catch (e: Exception) {
            println("exception at createPosTransaction : ${e.message} $e")
            logger.info("exception at createPosTransaction : $e")
            throw BadRequestException("Error : ${e.message} $e")
        }
        println("@207 ${posTransaction.merchant.name}")
        val total = posTransaction.feewadzpay!! + posTransaction.amountcrypto!!
        val qrString = "transactionID:${posTransaction.uuid}|blockchainAddress:${posTransaction.address}|type:${posTransaction.assetcrypto}|transactionAmount:$total|merchantId:${posTransaction.merchant.id}|posId:${posTransactionRequest.posId}|merchantDisplayName:${posTransaction.merchant.name}"
        return posTransaction.amountfiat?.let {
            PosTransactionResponse(
                posTransaction.uuid,
                total,
                posTransaction.address,
                it,
                posTransaction.assetfiat,
                posTransaction.assetcrypto,
                posTransaction.status,
                posTransaction.transaction?.totalDigitalCurrencyReceived ?: BigDecimal.ZERO,
                posTransaction.transaction?.uuid.toString(),
                posTransaction.transaction?.blockchainTxId,
                posTransaction.merchant.name,
                qrString,
                EncoderUtil.getEncoded(SALT_KEY, qrString)
            )
        }
    }

    data class PosTransactionResponse(
        val uuid: String,
        val totalDigitalCurrency: BigDecimal,
        val address: String,
        val totalFiatReceived: BigDecimal,
        val fiatType: FiatCurrencyUnit,
        val digitalCurrencyType: CurrencyUnit,
        val transactionStatus: TransactionStatus,
        var digitalCurrencyReceived: BigDecimal?,
        val transactionId: String,
        val blockChainHash: String?,
        val merchantName: String?,
        val qrString: String? = null,
        val qrEncrypedString: String? = null
    )

    fun getSendFee(cryptoAmount: BigDecimal, asset: CurrencyUnit, address: String, mdrVal: BigDecimal): PosFees {

        logger.info(InetAddress.getLocalHost().hostName)
        println("@249 asset")
        println(asset)
        if (asset == CurrencyUnit.SART) {
            println("@252 inside if")
            logger.info("Posfee 0,0 for SART")
            return PosFees(BigDecimal(0), BigDecimal(0))
        } else {
            println("@256 inside else")
            val wadzpayFee = getMdrPercentage(cryptoAmount, asset, mdrVal)
            val externalEstimatedTransferFee = getEstimatedFee(asset, address, cryptoAmount)

            return PosFees(wadzpayFee, externalEstimatedTransferFee)
        }
    }

    private fun getEstimatedFee(asset: CurrencyUnit, address: String, cryptoAmount: BigDecimal): BigDecimal {
        logger.info(address)
        println(InetAddress.getLocalHost().hostName)
        return if (!InetAddress.getLocalHost().hostName.contains("MacBook")) {
            println("===   inside if")
            println(asset.toBitGoCoin(appConfig.production))
            println("===   inside")
            bitGoWallet.getFeeEstimate(asset.toBitGoCoin(appConfig.production), address, cryptoAmount)
        } else {
            BigDecimal.ZERO
        }
    }

    fun getMdrPercentage(
        cryptoAmount: BigDecimal,
        asset: CurrencyUnit,
        mdrVal: BigDecimal
    ): BigDecimal {
        var totalMdr = BigDecimal("0")
        if (mdrVal > BigDecimal("0") || mdrVal < BigDecimal("0")) {
            totalMdr = cryptoAmount * (BigDecimal(1) + (cryptoAmount / mdrVal))
        }
        println("totalMdr = $totalMdr")

        return totalMdr
    }

    fun getPosPaymentModes(
        principal: Authentication,
        faitAmount: BigDecimal,
        fait: FiatCurrencyUnit
    ): PosPaymentListResponse {
        println("@288 getPosPaymentModes")
        val paymentModes: ArrayList<PaymentMode> = ArrayList()
        val account = accountOwnerService.extractAccount(principal, useMerchant = true)
        println("@291")
        for (subAccounts in account.account.subaccounts) {
            println("@316")
            if (subAccounts.address != null) {
                println("@316")
                println("==================")
                println(subAccounts.asset)
                println("=================")
                println(fait)
                println("====================")
                val subAccountFromCurrencyUnit = CurrencyUnit.valueOf(subAccounts.asset)
                val cryptoAmount = getCryptoAmount(faitAmount, fait, subAccountFromCurrencyUnit)
                println("@316 $cryptoAmount")
                val address = subAccounts.address?.address.toString()
                println("@316 $address")
                val wadzpayFee =
                    getMdrPercentage(cryptoAmount.amount, subAccountFromCurrencyUnit, (account as Merchant).mdrPercentage)
                println("@316 $wadzpayFee")
                val dTotalAmount = cryptoAmount.amount + wadzpayFee
                println("@316 $dTotalAmount")
                paymentModes.add(
                    PaymentMode(
                        subAccounts.asset,
                        faitAmount,
                        cryptoAmount.amount,
                        getSendFee(
                            cryptoAmount.amount,
                            subAccountFromCurrencyUnit,
                            address,
                            (account as Merchant).mdrPercentage
                        ),
                        dTotalAmount
                    )
                )
            }
        }
        println("@316  $paymentModes ${paymentModes.size} ")
        return PosPaymentListResponse(paymentModes = paymentModes)
    }

    fun getCryptoAmount(
        fiatAmount: BigDecimal,
        fait: FiatCurrencyUnit,
        currencyUnit: CurrencyUnit
    ): CryptoAmount {
        println("@329 ****")
        println(currencyUnit)
        println(fait)
        println("@332 ****")
        if (FiatCurrencyUnit.SAR == fait) {
            logger.info("skipping exchange rates for SART")
            println("skipping exchange rates for SART")
            return CryptoAmount((fiatAmount * BigDecimal(1)), BigDecimal(0))
        } else {
            println("inside else 1")
            println(currencyUnit)
            println(fait)
            println("inside else 2")
            val exchangeRate = exchangeService.getExchangeRate(currencyUnit, fait)
            println(exchangeRate)
            val cryptoAmount = CryptoAmount((fiatAmount * exchangeRate), exchangeRate)
            println("****************")
            println(cryptoAmount)
            if (cryptoAmount.amount.stripTrailingZeros().scale() > 8) {
                val am =
                    cryptoAmount.amount.setScale(8, RoundingMode.UP)
                        .stripTrailingZeros()
                cryptoAmount.amount = am
            }
            println("***********((((((*****")
            return cryptoAmount
        }
    }

    data class CryptoAmount(var amount: BigDecimal, val exchangeRate: BigDecimal)

    fun fakePosTransaction(walletAddress: String, cryptoCommitted: BigDecimal, asset: CurrencyUnit): PosTransaction {
        val posTransaction =
            posTransactionRepository.getByAddressAndTypeAndAssetcrypto(walletAddress, TransactionType.POS, asset)
                ?: throw EntityNotFoundException("Transaction with BlockChain Address $walletAddress not found")
        logger.info(posTransaction.toString())
        val totalAmountRequired = posTransaction.feewadzpay?.let { posTransaction.amountcrypto?.plus(it) }
        val amountToCreditInMerchant =
            cryptoCommitted - (posTransaction.feewadzpay ?: BigDecimal.ZERO)
        val merchantAccount = merchantService.findById(posTransaction.merchant.id)
        val cryptoCommittedTotal =
            cryptoCommitted + (posTransaction.transaction?.totalDigitalCurrencyReceived ?: BigDecimal.ZERO)
        posTransaction.transaction?.totalDigitalCurrencyReceived = cryptoCommittedTotal
        posTransaction.digitalCurrencyReceived = cryptoCommitted + posTransaction.digitalCurrencyReceived

        posTransaction.transaction?.blockchainTxId = "Fake commit No Hash " + Instant.now().epochSecond

        logger.info(amountToCreditInMerchant.toString())
        posTransaction.transaction?.paymentReceivedDate = Instant.now()
        if (cryptoCommittedTotal == totalAmountRequired) {
            posTransaction.transaction?.totalFiatReceived = posTransaction.amountfiat
            posTransaction.totalFiatReceived = posTransaction.amountfiat ?: BigDecimal.ZERO

            return createTransaction(
                merchantAccount.account,
                asset,
                posTransaction.feewadzpay ?: BigDecimal.ZERO,
                amountToCreditInMerchant,
                posTransaction, TransactionStatus.SUCCESSFUL, "Completed"
            )
        } else if (cryptoCommittedTotal > totalAmountRequired) {
            posTransaction.transaction?.totalFiatReceived =
                (cryptoCommittedTotal / posTransaction.conversion_rate).setScale(2, RoundingMode.DOWN)
                    .stripTrailingZeros()
            posTransaction.totalFiatReceived =
                (cryptoCommittedTotal / posTransaction.conversion_rate).setScale(2, RoundingMode.DOWN)
                    .stripTrailingZeros()

            return createTransaction(
                merchantAccount.account,
                asset,
                posTransaction.feewadzpay ?: BigDecimal.ZERO,
                amountToCreditInMerchant,
                posTransaction,
                TransactionStatus.OVERPAID,
                "Transferred More Than Required Amount."
            )
        } else {
            posTransaction.transaction?.totalFiatReceived =
                (cryptoCommittedTotal / posTransaction.conversion_rate).setScale(2, RoundingMode.DOWN)
                    .stripTrailingZeros()
            posTransaction.totalFiatReceived =
                (cryptoCommittedTotal / posTransaction.conversion_rate).setScale(2, RoundingMode.DOWN)
                    .stripTrailingZeros()
            return createTransaction(
                merchantAccount.account,
                asset,
                posTransaction.feewadzpay ?: BigDecimal.ZERO,
                amountToCreditInMerchant,
                posTransaction,
                TransactionStatus.UNDERPAID,
                "Transferred Less Than Required Amount."
            )
        }
    }

    private fun createTransaction(
        receiver: Account,
        asset: CurrencyUnit,
        wadzpayFee: BigDecimal,
        amount: BigDecimal,
        posTransaction: PosTransaction,
        transactionStatus: TransactionStatus,
        comment: String
    ): PosTransaction {
        posTransaction.comments = comment
        posTransaction.transaction?.status = transactionStatus
        posTransaction.status = transactionStatus
        val sender = ledgerService.getOmnibusAccount()
        posTransaction.transaction?.fee = posTransaction.feewadzpay ?: BigDecimal.ZERO
        posTransaction.assetcrypto = asset
        val trx = posTransaction.transaction?.let {
            ledgerService.createTransaction(
                receiver, sender, true,
                it, asset, amount
            )
        }

        return posTransactionRepository.save(posTransaction)
    }

    fun posTransaction(
        bitGoTransfer: BitGoTransfer,
        bitGoTransferEntry: BitGoTransferEntry,
        asset: CurrencyUnit
    ): PosTransaction {

        val byTransferIdAndType =
            blockConfirmRepository.getByTransferIdAndType(bitGoTransfer.id, TransactionType.POS)
        if (byTransferIdAndType != null) {
            byTransferIdAndType.blockConfirmationCount++
            byTransferIdAndType.updatedAt = Instant.now()
            blockConfirmRepository.save(byTransferIdAndType)
            logger.info("For POS transaction, the transaction is already committed, hence not committing again")
            return posTransactionRepository.getByAddressAndTypeAndAssetcrypto(
                bitGoTransferEntry.address,
                TransactionType.POS,
                asset
            )
                ?: throw EntityNotFoundException("Transaction with BlockChain Address ${bitGoTransferEntry.address} not found")
        } else {
            blockConfirmRepository.save(
                BlockConfirmationLogger(
                    bitGoTransfer.txid,
                    bitGoTransfer.id,
                    bitGoTransferEntry.wallet,
                    TransactionType.POS,
                    1
                )
            )
        }
        /* if (!posLogCommitsRepository.getByBlockId(bitGoTransfer.txid).isNullOrEmpty()) {
             logger.info("For cryptoAddressForPOS, the transaction is already committed, hence not committing again")
             return posTransactionRepository.getByAddressAndType(bitGoTransferEntry.address, TransactionType.POS)
                 ?: throw EntityNotFoundException("Transaction with BlockChain Address ${bitGoTransferEntry.address} not found")
         }*/

        posLogCommitsRepository.save(
            PosLogCommits(
                bitGoTransferEntry.wallet.toString(),
                bitGoTransferEntry.address,
                bitGoTransferEntry.valueString.normalize(asset.toBitGoCoin(appConfig.production)).toString(),
                asset,
                TransactionType.POS,
                bitGoTransfer.txid,
                bitGoTransfer.entries.toString(),
                bitGoTransfer.id,
                bitGoTransfer.coin,
                bitGoTransfer.feeString.normalize(asset.toBitGoCoin(appConfig.production)).toString(),
                bitGoTransfer.type,
                bitGoTransfer.baseValueString.normalize(asset.toBitGoCoin(appConfig.production)).toString()
            )
        )

        val posTransaction =
            posTransactionRepository.getByAddressAndTypeAndAssetcrypto(
                bitGoTransferEntry.address,
                TransactionType.POS,
                asset
            )
                ?: throw EntityNotFoundException("Transaction with BlockChain Address ${bitGoTransferEntry.address} not found")
        val totalAmountRequired = posTransaction.feewadzpay?.let { posTransaction.amountcrypto?.plus(it) }
        val toolAmountGotByBlockChain =
            bitGoTransferEntry.valueString.normalize(asset.toBitGoCoin(appConfig.production))
        val merchantAccount = posTransaction.merchant
        val oldAmount = (posTransaction.transaction?.totalDigitalCurrencyReceived ?: BigDecimal.ZERO)
        val toolAmountGot = oldAmount + toolAmountGotByBlockChain
        posTransaction.digitalCurrencyReceived = posTransaction.digitalCurrencyReceived + toolAmountGotByBlockChain
        posTransaction.transaction?.totalDigitalCurrencyReceived = toolAmountGot

        posTransaction.transaction?.blockchainTxId = bitGoTransfer.txid
        posTransaction.blockchainid = bitGoTransfer.txid

        val amountToCreditInMerchant =
            toolAmountGotByBlockChain - (
                (posTransaction.feewadzpay ?: BigDecimal.ZERO) + (
                    posTransaction.feeexternal
                        ?: BigDecimal.ZERO
                    )
                )
        posTransaction.transaction?.paymentReceivedDate = Instant.now()
        posTransaction.transaction?.sourceWalletAddress =
            bitGoTransfer.entries.find { it.valueString < BigDecimal.ZERO }?.address
        if (toolAmountGot == totalAmountRequired) {
            posTransaction.transaction?.totalFiatReceived = posTransaction.amountfiat
            posTransaction.totalFiatReceived = posTransaction.amountfiat ?: BigDecimal.ZERO
            return createTransaction(
                merchantAccount.account,
                asset,
                posTransaction.feewadzpay ?: BigDecimal.ZERO,
                amountToCreditInMerchant,
                posTransaction, TransactionStatus.SUCCESSFUL, "Completed"
            )
        } else if (toolAmountGot > totalAmountRequired) {
            posTransaction.transaction?.totalFiatReceived =
                (toolAmountGot / posTransaction.conversion_rate).setScale(2, RoundingMode.DOWN)
                    .stripTrailingZeros()
            posTransaction.totalFiatReceived =
                (toolAmountGot / posTransaction.conversion_rate).setScale(2, RoundingMode.DOWN)
                    .stripTrailingZeros()
            return createTransaction(
                merchantAccount.account,
                asset,
                posTransaction.feewadzpay ?: BigDecimal.ZERO,
                amountToCreditInMerchant,
                posTransaction,
                TransactionStatus.OVERPAID,
                "Transferred More Than Required Amount."
            )
        } else {

            posTransaction.transaction?.totalFiatReceived =
                (toolAmountGot / posTransaction.conversion_rate).setScale(2, RoundingMode.DOWN)
                    .stripTrailingZeros()
            posTransaction.totalFiatReceived =
                (toolAmountGot / posTransaction.conversion_rate).setScale(2, RoundingMode.DOWN)
                    .stripTrailingZeros()

            return createTransaction(
                merchantAccount.account,
                asset,
                posTransaction.feewadzpay ?: BigDecimal.ZERO,
                amountToCreditInMerchant,
                posTransaction,
                TransactionStatus.UNDERPAID,
                "Transferred Less Than Required Amount."
            )
        }
    }

    fun refreshPosOrderWithBlockchainAddress(blockChainAddress: String): PosTransactionResponse? {
        val posTransaction = posTransactionRepository.getByAddressAndType(blockChainAddress, TransactionType.POS)
            ?: throw EntityNotFoundException("Transaction with BlockChain Address $blockChainAddress not found")
        val transaction = posTransaction.transaction
        val cryptoAmountObj = getCryptoAmount(
            posTransaction.amountfiat ?: BigDecimal.ZERO,
            posTransaction.assetfiat,
            posTransaction.assetcrypto
        )
        var cryptoAmount = cryptoAmountObj.amount
        if (cryptoAmount.stripTrailingZeros().scale() > 8) {
            val am = cryptoAmount.setScale(8, RoundingMode.UP)
                .stripTrailingZeros()
            cryptoAmount = am
        }

        val newBlockChainAddress = if (posTransaction.assetcrypto == CurrencyUnit.SART) {
            algoCustomTokenWallet.createAddressNotSave(posTransaction.assetcrypto.toString())
        } else {
            bitGoWallet.createAddressNotSave(posTransaction.assetcrypto.toString())
        }

        val newPosTransaction = PosTransaction(
            posTransaction.merchant,
            posTransaction.merchantPos,
            posTransaction.assetcrypto,
            posTransaction.assetfiat,
            newBlockChainAddress,
            posTransaction.amountfiat,
            cryptoAmount,
            getMdrPercentage(cryptoAmount, posTransaction.assetcrypto, posTransaction.merchant.mdrPercentage),
            BigDecimal.ZERO,
            posTransaction.type,
            posTransaction.senderid,
            "",
            posTransaction.description,
            posTransaction.transaction?.status ?: TransactionStatus.IN_PROGRESS,
            posTransaction.transaction?.totalDigitalCurrencyReceived ?: BigDecimal.ZERO,
            cryptoAmountObj.exchangeRate,
            posTransaction.transaction?.totalFiatReceived ?: BigDecimal.ZERO
        )
        newPosTransaction.transaction = posTransaction.transaction

        posTransactionRepository.save(newPosTransaction)

        transaction?.posTransaction?.add(newPosTransaction)

        if (transaction != null) {
            transactionRepository.save(transaction)
        }

        val total = newPosTransaction.amountcrypto!! + newPosTransaction.feewadzpay!!
        return newPosTransaction.amountfiat?.let {
            PosTransactionResponse(
                newPosTransaction.uuid,
                total,
                newPosTransaction.address,
                it,
                newPosTransaction.assetfiat,
                newPosTransaction.assetcrypto,
                newPosTransaction.transaction?.status ?: TransactionStatus.IN_PROGRESS,
                newPosTransaction.transaction?.totalDigitalCurrencyReceived,
                newPosTransaction.transaction?.uuid.toString(), newPosTransaction.transaction?.blockchainTxId,
                posTransaction.merchant.name
            )
        }
    }

    fun refreshOrder(blockChainAddress: String): PosTransactionResponse? {
        val posTransaction = posTransactionRepository.getByAddressAndType(blockChainAddress, TransactionType.POS)
            ?: throw EntityNotFoundException("Transaction with BlockChain Address $blockChainAddress not found")

        val transaction = posTransaction.transaction
        val cryptoAmountObj = getCryptoAmount(
            posTransaction.amountfiat ?: BigDecimal.ZERO,
            posTransaction.assetfiat,
            posTransaction.assetcrypto
        )
        var cryptoAmount = cryptoAmountObj.amount
//        val wadzPayFee = getWadzPayFee(cryptoAmount, posTransaction.assetcrypto, principal) // TODO: Diff merchant hav diff fee
        val wadzPayFee = BigDecimal.ZERO
        if (cryptoAmount.stripTrailingZeros().scale() > 8) {
            val am = cryptoAmount.setScale(8, RoundingMode.UP)
                .stripTrailingZeros()
            cryptoAmount = am
        }

        // save pos trx

        posTransaction.amountcrypto = cryptoAmount
        posTransaction.feewadzpay = wadzPayFee

        posTransactionRepository.save(posTransaction)

        // save trx

        transaction?.amount = cryptoAmount
        transaction?.fee = wadzPayFee
        if (transaction != null) {
            transactionRepository.save(transaction)
        }
        val total = posTransaction.amountcrypto!! + posTransaction.feewadzpay!!
        return posTransaction.amountfiat?.let {
            PosTransactionResponse(
                posTransaction.uuid,
                total,
                posTransaction.address,
                it,
                posTransaction.assetfiat,
                posTransaction.assetcrypto,
                posTransaction.status,
                posTransaction.digitalCurrencyReceived,
                posTransaction.transaction?.uuid.toString(),
                posTransaction.transaction?.blockchainTxId,
                ""
            )
        }
    }

    fun getTransactionDetails(address: String): PosTransaction {
        return posTransactionRepository.getByAddressAndType(address, TransactionType.POS)
            ?: throw EntityNotFoundException("Transaction with BlockChain Address $address not found")
    }

    fun addPosToMerchant(principal: Principal, createPosRequest: CreatePosRequest): MerchantPos {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val merchantData =
            userAccount.merchant ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)
        val merchantPos = merchantPosRepository.save(
            MerchantPos(
                createPosRequest.posNumber,
                createPosRequest.posName,
                merchantData
            )
        )
        merchantData.merchantPoses.add(merchantPos)
        merchantService.merchantRepository.save(merchantData)
        return merchantPos
    }

    fun initiateWebLinkRefundPos(
        accountOwner: AccountOwner,
        request: InitiateWebLinkRefundPosRequest,
        merchantData: Merchant
    ): RefundToken {
        val transaction = transactionService.getByIdAndOwner(request.transactionId, accountOwner)

        if (request.isReInitiateRefund == true) {
            val refundTransaction = transaction.refundTransactions?.find { it.uuid == request.refundTransactionID }
                ?: throw BadRequestException(ErrorCodes.NO_REFUND_REQUEST_FOUND_ERROR)

            if (refundTransaction.refundStatus == RefundStatus.REFUNDED) {
                throw BadRequestException(ErrorCodes.REFUND_ALREADY_EXISTS_ERROR)
            }
        }

        if (transactionService.isAnyRefundInQueue(transaction, request.isReInitiateRefund) == true) {
            throw BadRequestException(ErrorCodes.REFUND_ALREADY_EXISTS_ERROR)
        }

        if (request.refundAmountFiat > transaction.totalFiatReceived) {
            throw BadRequestException(ErrorCodes.CANNOT_REFUND_MORE_THAN_RECEIVED_FIAT)
        }

        val refundMode = if (request.refundAmountFiat <= merchantData.defaultRefundableFiatValue) {
            RefundMode.CASH
        } else {
            RefundMode.WALLET
        }

        if (refundMode == RefundMode.CASH && request.refundAmountFiat > merchantData.defaultRefundableFiatValue) {
            throw BadRequestException(ErrorCodes.TRANSACTION_REFUND_TYPE_IS_CASH_AMOUNT_SHOULD_UPTO_DEFAULT_REFUNDABLE_FIAT)
        }
        /*if (refundMode == RefundMode.WALLET && request.refundAmountFiat <= merchantData.defaultRefundableFiatValue) {
            throw BadRequestException(ErrorCodes.TRANSACTION_REFUND_TYPE_IS_WALLET_AMOUNT_SHOULD_MORE_THAN_DEFAULT_REFUNDABLE_FIAT)
        }*/

        val initiateWebLinkRefund = InitiateWebLinkRefund(
            request.refundUserMobile,
            request.refundUserEmail,
            request.refundUserName,
            if (appConfig.environment.equals("dev", true) || appConfig.environment.equals("test", true)) {
                CurrencyUnit.BTC
            } else {
                CurrencyUnit.USDT
            },
            transaction.fiatAsset!!,
            request.refundAmountFiat,
            getCryptoAmount(request.refundAmountFiat, transaction.fiatAsset!!, CurrencyUnit.USDT).amount,
            request.transactionId,
            refundMode,
            request.refundCustomerFormUrl ?: appConfig.getRefundFormUrl(),
            extPosIdRefund = request.extPosIdRefund,
            extPosActualDateRefund = request.extPosActualDateRefund,
            extPosTransactionIdRefund = request.extPosTransactionIdRefund,
            extPosLogicalDateRefund = request.extPosLogicalDateRefund,
            extPosShiftRefund = request.extPosShiftRefund,
            extPosActualTimeRefund = request.extPosActualTimeRefund,
            extPosSequenceNoRefund = request.extPosSequenceNoRefund,
            isReInitiateRefund = request.isReInitiateRefund,
            refundTransactionID = request.refundTransactionID
        )
        logger.info(initiateWebLinkRefund.toString())
        return transactionService.initiateWebLinkRefund(accountOwner, initiateWebLinkRefund, RefundOrigin.POS)
    }

    fun submitRefundFormAuto(userAccount: AccountOwner, request: RefundInitiationRequest, refundOrigin: String): TransactionRefundDetails {
        return transactionService.submitRefundFormAutoApprove(userAccount, request, refundOrigin)
    }

    fun submitRefundFormAutoQAR(userAccount: AccountOwner, request: RefundInitiationRequest, refundOrigin: String): TransactionRefundDetails {
        return transactionService.submitRefundFormAutoApproveQAR(userAccount, request, refundOrigin)
    }

//    ge-idea demo test
    fun posTransactionSart(
        senderAccount: AccountOwner,
        walletAddress: String?,
        amount: BigDecimal,
        asset: CurrencyUnit,
        tranId: UUID?,
        issuanceBank: IssuanceBanks?,
        transactionMode: TransactionMode,
        passCodeHash: String?,
        transactionStatus: TransactionStatus
    ): Transaction? {
        val posTransaction =
            asset?.let { tranId?.let { it1 -> posTransactionRepository.getByUuid(it1.toString()) } }
                ?: throw EntityNotFoundException("Transaction with BlockChain Address $walletAddress not found")
        logger.info(posTransaction.toString())

        if (posTransaction.status == TransactionStatus.IN_PROGRESS) {
            if (transactionMode == TransactionMode.CUSTOMER_OFFLINE) {
                // TODO: This code are  for transaction limit
                val senderAccountsUserAccount = senderAccount.account.owner
                senderAccount.account.owner?.let {
                    issuanceService.validateCustomerOfflinePayment(
                        IssuanceCommonController.TransactionLoadingType.TTC_005, amount,
                        senderAccountsUserAccount as UserAccount,
                        it
                    )
                }
            }
            if (amount.compareTo(posTransaction.amountfiat) != 0) {
                throw EntityNotFoundException(ErrorCodes.INVALID_AMOUNT)
            }
            val subAccount = senderAccount.account.getSubaccountByAsset(asset)
            senderAccount.account.owner?.let { ledgerService.syncBlockChainBalance(it) }
            val balance = issuanceWalletService.getCryptoBalance(subAccount)
            if (balance < amount) {
                throw UnprocessableEntityException(ErrorCodes.INSUFFICIENT_FUNDS)
            }

            val merchantAccount = merchantService.findById(posTransaction.merchant.id)
            if (transactionStatus == TransactionStatus.SUCCESSFUL) {
                posTransaction.transaction?.totalDigitalCurrencyReceived = amount
                posTransaction.digitalCurrencyReceived = amount
                posTransaction.transaction?.totalFiatReceived = posTransaction.amountfiat
                posTransaction.totalFiatReceived = posTransaction.amountfiat ?: BigDecimal.ZERO
                posTransaction.transaction?.totalFiatReceived = posTransaction.amountfiat
                posTransaction.transaction?.paymentReceivedDate = Instant.now()
            }
            posTransaction.transaction?.sender = subAccount
            return createTransactionSart(
                senderAccount.account,
                merchantAccount.account,
                walletAddress ?: posTransaction.address,
                asset,
                posTransaction.feewadzpay ?: BigDecimal.ZERO,
                amount,
                posTransaction,
                transactionStatus ?: TransactionStatus.SUCCESSFUL,
                if (transactionStatus != null) if (transactionStatus == TransactionStatus.SUCCESSFUL) "Completed" else "Failed" else "Completed",
                transactionMode,
                issuanceBank,
                passCodeHash
            )
        }
        throw EntityNotFoundException(ErrorCodes.TRANSACTION_IS_ALREADY_PAID)
    }

    private fun createTransactionSart(
        senderAccount: Account,
        receiver: Account,
        reciverAddress: String,
        asset: CurrencyUnit,
        wadzpayFee: BigDecimal,
        amount: BigDecimal,
        posTransaction: PosTransaction,
        transactionStatus: TransactionStatus,
        comment: String,
        transactionMode: TransactionMode,
        issuanceBank: IssuanceBanks?,
        passCodeHash: String?
    ): Transaction? {
        posTransaction.comments = comment
        posTransaction.transaction?.fee = posTransaction.feewadzpay ?: BigDecimal.ZERO
        posTransaction.assetcrypto = asset
        posTransaction.transaction?.txMode = transactionMode
        val trx = posTransaction.transaction?.let {
            ledgerService.createTransactionSart(
                senderAccount, receiver, reciverAddress, true,
                it, asset, amount, issuanceBank, passCodeHash, transactionStatus
            )
        }
        if (trx != null) {
            posTransaction.blockchainid = trx.blockchainTxId
            if (transactionStatus == TransactionStatus.SUCCESSFUL) {
                posTransaction.status = transactionStatus
                posTransaction.transaction?.totalDigitalCurrencyReceived = posTransaction.digitalCurrencyReceived
            }
        }
        posTransactionRepository.save(posTransaction)
        return trx
    }
    fun createPosTransactionSart(
        principal: Principal,
        cryptoType: CurrencyUnit,
        faitType: FiatCurrencyUnit,
        posTransactionRequest: PosTransactionRequest
    ): PosTransactionResponse? {
        println("createPosTransactionSart : " + Instant.now())
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        var posTransaction: PosTransaction? = null
        try {
            val merchantData =
                userAccount.merchant ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)
            val merchantPos = merchantPosRepository.findByPosIdAndMerchant(posTransactionRequest.posId, merchantData)
                ?: throw EntityNotFoundException(ErrorCodes.INVALID_POS_ID)
//            val address = UUID.randomUUID().toString()
            val address = getLocalAddress(35)
//            val cryptoAmountObj = getCryptoAmount(posTransactionRequest.fiatAmount, faitType, cryptoType)
            val cryptoAmountObj = CryptoAmount((posTransactionRequest.fiatAmount * BigDecimal(1)), BigDecimal(1))
            var cryptoAmount = cryptoAmountObj.amount
            val wadzPayFee = getMdrPercentage(cryptoAmount, cryptoType, merchantData.mdrPercentage)
            val saltKey = EncoderUtil.getNewSaltKey()
            val posSalt = posSaltRepository.save(PosSalt(saltKey))
            if (cryptoAmount.stripTrailingZeros().scale() > 8) {
                val am =
                    cryptoAmount.setScale(8, RoundingMode.UP).stripTrailingZeros()
                cryptoAmount = am
            }

            val tempPosTransaction = PosTransaction(
                merchantData,
                merchantPos,
                cryptoType,
                faitType,
                address,
                posTransactionRequest.fiatAmount,
                cryptoAmount,
                wadzPayFee,
                BigDecimal.ZERO,
                TransactionType.POS,
                userAccount.email!!,
                null,
                posTransactionRequest.description,
                TransactionStatus.IN_PROGRESS, BigDecimal.ZERO, cryptoAmountObj.exchangeRate, BigDecimal.ZERO
            )
            posTransaction = posTransactionRepository.save(tempPosTransaction)
            posSalt.posTransaction = posTransaction
            posTransaction.saltkey = posSalt
            posSaltRepository.save(posSalt)
            println("posSaltRepository : " + Instant.now())

            val senderSubaccount = ledgerService.getOmnibusAccount()
            println("senderSubaccount : " + Instant.now())

            val transaction = Transaction(
                sender = senderSubaccount.getSubaccountByAsset(CurrencyUnit.BTC),
                receiver = merchantData.account.getSubaccountByAsset(CurrencyUnit.BTC),
                asset = cryptoType.name,
                amount = cryptoAmount,
                fiatAsset = faitType,
                fiatAmount = posTransaction.amountfiat,
                status = TransactionStatus.IN_PROGRESS,
                type = TransactionType.POS,
                description = posTransaction.description,
                fee = wadzPayFee,
                extPosId = posTransactionRequest.extPosId,
                extPosSequenceNo = posTransactionRequest.extPosSequenceNo,
                extPosTransactionId = posTransactionRequest.extPosTransactionId,
                extPosLogicalDate = posTransactionRequest.extPosLogicalDate,
                extPosShift = posTransactionRequest.extPosShift,
                extPosActualDate = posTransactionRequest.extPosActualDate,
                extPosActualTime = posTransactionRequest.extPosActualTime,
                sourceWalletAddress = address,
            )
            println("transaction : " + Instant.now())
            println(transaction)
            transaction.posTransaction?.add(posTransaction)
            val trx = transactionRepository.save(transaction)
            println("transactionRepository : " + Instant.now())

            posTransaction.transaction = transactionRepository.save(trx.setTxId("POS", trx.id))
            posTransaction.assetcrypto = cryptoType
            posTransactionRepository.save(posTransaction)
            println("posTransactionRepository : " + Instant.now())
            // val total = posTransaction.feewadzpay ?: (BigDecimal.ZERO + posTransaction.amountcrypto!!)
            // val total = cryptoAmount !! + wadzPayFee!!
            val privateChainEvent = PrivateChainEvent(
                PrivateChainType.transaction, posTransaction.address, posTransaction.blockchainid.toString(),
                PrivateChainCoin.btc, "Transfer", PrivateChainState.new
            )
            webhookController.getPrivateChainService(privateChainEvent)
        } catch (e: HttpHostConnectException) {
            throw BadRequestException("HttpHostConnectException")
        } catch (e: EntityNotFoundException) {
            throw BadRequestException("EntityNotFoundException")
        } catch (e: ConstraintViolationException) {
            throw BadRequestException("ConstraintViolationException")
        } catch (e: Exception) {
            println("@249 \n " + e.toString())
            println("@249 \n " + e.message.toString())
//            createPosTransactionSart(principal, CurrencyUnit.BTC, faitType, posTransactionRequest)
//            throw BadRequestException(ErrorCodes.BITGO_SERVER_ERROR)
            throw BadRequestException("Error $e ${e.message} ")
        }
        val total = posTransaction!!.feewadzpay!! + posTransaction.amountcrypto!!
        return posTransaction.amountfiat?.let {
            PosTransactionResponse(
                posTransaction.uuid,
                total,
                posTransaction.address,
                it,
                posTransaction.assetfiat,
                posTransaction.assetcrypto,
                posTransaction.status,
                posTransaction.transaction?.totalDigitalCurrencyReceived ?: BigDecimal.ZERO,
                posTransaction.transaction?.uuid.toString(), posTransaction.transaction?.blockchainTxId,
                posTransaction.merchant.name
            )
        }
    }

    fun getLocalAddress(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
//
    fun getPosPaymentModesAlgo(
        principal: Authentication,
        faitAmount: BigDecimal,
        fait: FiatCurrencyUnit
    ): PosPaymentListResponse {
        println("@1030 getPosPaymentModesAlgo")
        val paymentModes: ArrayList<PaymentMode> = ArrayList()
        val account = accountOwnerService.extractAccount(principal, useMerchant = true)
        println("@1033")
        if (fait != FiatCurrencyUnit.SAR) {
            throw BadRequestException("Invalid Fiat Currency")
        }
        println("@1037  $paymentModes ${paymentModes.size} ")
        paymentModes.add(
            PaymentMode(
                CurrencyUnit.SART.name,
                faitAmount,
                faitAmount,
                PosFees(BigDecimal.ZERO, BigDecimal.ZERO),
                faitAmount
            )
        )
        println("@1047")
        return PosPaymentListResponse(paymentModes = paymentModes)
    }

    fun getCryptoAmountAlgo(
        fiatAmount: BigDecimal,
        fait: FiatCurrencyUnit,
        currencyUnit: CurrencyUnit
    ): CryptoAmount {
        println("@1071 getCryptoAmountAlgo")
        return CryptoAmount((fiatAmount * BigDecimal(1)), BigDecimal(0))
    }

    fun getMdrPercentageAlgo(
        cryptoAmount: BigDecimal,
        asset: CurrencyUnit,
        mdrVal: BigDecimal
    ): BigDecimal {
        println("@1089 getMdrPercentageAlgo $asset ")
        var totalMdr = BigDecimal("0")
        if (mdrVal > BigDecimal("0") || mdrVal < BigDecimal("0")) {
            totalMdr = cryptoAmount * (BigDecimal(1) + (cryptoAmount / mdrVal))
        }
        println("totalMdr = $totalMdr")

        return totalMdr
    }
//
    fun getTransactionDetailsAlgo(uuid: String): PosTransaction {
        return posTransactionRepository.getByUuid(uuid)
            ?: throw EntityNotFoundException("Transaction with BlockChain Address $uuid not found")
    }

    fun startScanVerify(request: EncryptQRRequest): String {
        var scanData: String?
        var responseData: String?
        try {
            scanData = EncoderUtil.getDecoded(SALT_KEY, request.data)
            println("@1135 scanData = $scanData")
            val jsonDataConvert = convertScanDataToJson(scanData)
            println("@1136 jsonConvert =  $jsonDataConvert")
            val jsonObjectScanData = JSONObject(jsonDataConvert)
            println("@1139 jsonObject =  $jsonObjectScanData")
            val address = jsonObjectScanData.get("blockchainAddress").toString()
            println("@1142 address =  $address")
            val addressCode = countByAddress(address)
            if (addressCode > 0) {
                responseData = jsonDataConvert
            } else {
                responseData = ErrorCodes.INVALID_QR
            }
        } catch (e: Exception) {
            throw BadRequestException(ErrorCodes.INVALID_QR)
        }
        return responseData
    }
    fun convertScanDataToJson(inputData: String): String {
        val jsonObject = JSONObject()
        val keyValuePairs = inputData.split("|")
        for (pair in keyValuePairs) {
            val key = pair.substringBefore(":")
            val value = pair.substringAfter(":")
            jsonObject.put(key, value)
        }
        println("@1161 $jsonObject ")
        return jsonObject.toString()
    }
    fun countByAddress(address: String): Long {
        val cryptoAddressForUserReceive =
            cryptoAddressRepository.countByAddress(address)
        return if (cryptoAddressForUserReceive > 0) { 1 } else { 0 }
    }

    fun merchantOfflinePosTransactionSart(
        senderAccount: AccountOwner,
        receiverAccount: AccountOwner,
        receiverAddress: String,
        amount: BigDecimal,
        asset: CurrencyUnit,
        merchantId: Long,
        posId: String,
        issuanceBank: IssuanceBanks?,
        transactionStatus: TransactionStatus
    ): Transaction? {
        val subAccount = senderAccount.account.getSubaccountByAsset(asset)
        val balance = issuanceWalletService.getCryptoBalance(subAccount)
        if (balance < amount) {
            throw UnprocessableEntityException(ErrorCodes.INSUFFICIENT_FUNDS)
        }
        subAccount.account.owner?.let { ledgerService.syncBlockChainBalance(it) }
        val userAccount = userAccountService.getUserAccountByMerchantId(merchantId)
        if (userAccount.size == 0) {
            throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)
        }
        var posTransaction: PosTransaction? = null
        val merchantData =
            userAccount[0].userAccount.merchant ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)
        val merchantPos = merchantPosRepository.findByPosIdAndMerchant(posId, merchantData)
            ?: throw EntityNotFoundException(ErrorCodes.INVALID_POS_ID)
        try {
            val wadzPayFee = getMdrPercentage(BigDecimal.ZERO, asset, merchantData.mdrPercentage)
            val saltKey = EncoderUtil.getNewSaltKey()
            val posSalt = posSaltRepository.save(PosSalt(saltKey))
            val tempPosTransaction = PosTransaction(
                merchantData,
                merchantPos,
                asset,
                FiatCurrencyUnit.SAR,
                receiverAddress,
                amount,
                BigDecimal.ZERO,
                wadzPayFee,
                BigDecimal.ZERO,
                TransactionType.OTHER,
                userAccount[0].userAccount.email!!,
                null,
                "Merchant Offline Payment",
                TransactionStatus.IN_PROGRESS,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
            )
            posTransaction = posTransactionRepository.save(tempPosTransaction)
            posSalt.posTransaction = posTransaction
            posTransaction.saltkey = posSalt
            posSaltRepository.save(posSalt)
            posTransaction.amountcrypto = posTransaction.amountfiat
            val senderSubaccount = ledgerService.getOmnibusAccount()
            val transaction = Transaction(
                sender = senderSubaccount.getSubaccountByAsset(asset),
                receiver = merchantData.account.getSubaccountByAsset(asset),
                asset = asset.name,
                amount = amount,
                fiatAsset = FiatCurrencyUnit.SAR,
                fiatAmount = posTransaction.amountfiat,
                status = TransactionStatus.IN_PROGRESS,
                type = TransactionType.OTHER,
                description = posTransaction.description,
                fee = wadzPayFee,
                sourceWalletAddress = receiverAddress,
            )
            transaction.posTransaction?.add(posTransaction)
            val utilDate = Date()
            val sqlDate = java.sql.Date(utilDate.time)
            val sqlTime = java.sql.Time(utilDate.time)
            transaction.extPosActualDate = sqlDate
            transaction.extPosActualTime = sqlTime
            val trx = transactionRepository.save(transaction)
            posTransaction.transaction = transactionRepository.save(trx.setTxId("POS", trx.id))
            posTransaction.assetcrypto = asset
            posTransactionRepository.save(posTransaction)
            posTransactionSart(
                senderAccount,
                receiverAddress,
                amount,
                asset,
                UUID.fromString(posTransaction.uuid),
                issuanceBank,
                TransactionMode.MERCHANT_OFFLINE,
                null,
                transactionStatus
            )
            return transaction
        } catch (e: Exception) {
            logger.info("exception at createPosTransaction : $e")
            throw BadRequestException("Error : ${e.message} $e")
        }
    }

    fun getPosId(merchant: Merchant): String {
        val merchantPos = merchantPosRepository.findByMerchant(merchant)
        return if (merchantPos != null && merchantPos.size > 0) {
            MERCHANT_OFFLINE_POS_ID
        } else {
            BigDecimal.ZERO.toString()
        }
    }
}

data class CreatePosRequest(
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val posName: String,
    val posNumber: String
)
