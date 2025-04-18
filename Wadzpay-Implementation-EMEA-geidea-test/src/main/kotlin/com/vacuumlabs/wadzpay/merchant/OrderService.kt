package com.vacuumlabs.wadzpay.merchant

import com.vacuumlabs.wadzpay.a.EncoderUtil
import com.vacuumlabs.wadzpay.algocutomtoken.AlgoCustomTokenWallet
import com.vacuumlabs.wadzpay.bitgo.BitGoTransfer
import com.vacuumlabs.wadzpay.bitgo.BitGoTransferEntry
import com.vacuumlabs.wadzpay.bitgo.BitGoWallet
import com.vacuumlabs.wadzpay.bitgo.normalize
import com.vacuumlabs.wadzpay.bitgo.toBitGoCoin
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.UnauthorizedException
import com.vacuumlabs.wadzpay.common.UnprocessableEntityException
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.LedgerService
import com.vacuumlabs.wadzpay.ledger.model.AccountOwner
import com.vacuumlabs.wadzpay.ledger.model.Transaction
import com.vacuumlabs.wadzpay.ledger.model.TransactionRepository
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.ledger.service.TransactionService
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.Merchant
import com.vacuumlabs.wadzpay.merchant.model.Order
import com.vacuumlabs.wadzpay.merchant.model.OrderRepository
import com.vacuumlabs.wadzpay.merchant.model.OrderStatus
import com.vacuumlabs.wadzpay.merchant.model.OrderType
import com.vacuumlabs.wadzpay.pos.PosLogCommits
import com.vacuumlabs.wadzpay.pos.PosLogCommitsRepository
import com.vacuumlabs.wadzpay.pos.PosSalt
import com.vacuumlabs.wadzpay.pos.PosSaltRepository
import com.vacuumlabs.wadzpay.pos.PosService
import com.vacuumlabs.wadzpay.pos.PosTransaction
import com.vacuumlabs.wadzpay.pos.PosTransactionRepository
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.utils.BlockConfirmationLogger
import com.vacuumlabs.wadzpay.utils.BlockConfirmationRepository
import com.vacuumlabs.wadzpay.webhook.WebhookService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.security.Principal
import java.time.Instant
import java.util.UUID
import javax.validation.ConstraintViolationException

@Service
class OrderService(
    val orderRepository: OrderRepository,
    val ledgerService: LedgerService,
    val transactionService: TransactionService,
    val webhookService: WebhookService,
    val bitGoWallet: BitGoWallet,
    val algoCustomTokenWallet: AlgoCustomTokenWallet,
    val appConfig: AppConfig,
    val posService: PosService,
    val posSaltRepository: PosSaltRepository,
    val merchantService: MerchantService,
    val posTransactionRepository: PosTransactionRepository,
    val transactionRepository: TransactionRepository,
    val blockConfirmRepository: BlockConfirmationRepository,
    val posLogCommitsRepository: PosLogCommitsRepository
) {
    companion object {
        const val MILISECONDS_IN_MINUTE = 60 * 1000
    }

    val logger = LoggerFactory.getLogger(javaClass)
    fun createOrder(

        currency: CurrencyUnit,
        type: OrderType,
        fiatAmount: BigDecimal,
        fiatCurrency: FiatCurrencyUnit,
        source: AccountOwner?,
        target: AccountOwner?,
        targetEmail: String?,
        externalOrderId: String,
        description: String? = null
    ): Order {
        if (type == OrderType.WITHDRAWAL && targetEmail == null) {
            throw BadRequestException(ErrorCodes.INVALID_EMAIL)
        }

        val amount = posService.getCryptoAmount(fiatAmount, fiatCurrency, currency)

        try {
            val order = Order(
                amount = amount.amount,
                currency = currency,
                type = type,
                fiatAmount = fiatAmount,
                fiatCurrency = fiatCurrency,
                source = source,
                target = target,
                targetEmail = targetEmail,
                externalOrderId = externalOrderId,
                description = description
            )
            return orderRepository.save(order)
        } catch (ex: ConstraintViolationException) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_TOO_MANY_DECIMAL_PLACES)
        }
    }

    fun createOrderThirdParty(
        cryptoType: CurrencyUnit,
        type: OrderType,
        fiatAmount: BigDecimal,
        faitType: FiatCurrencyUnit,
        target: Merchant?,
        targetEmail: String?,
        externalOrderId: String?,
        description: String? = null,
        principal: Principal
    ): Order {

        if (type == OrderType.WITHDRAWAL && targetEmail == null) {
            throw BadRequestException(ErrorCodes.INVALID_EMAIL)
        }

        val amount = posService.getCryptoAmount(fiatAmount, faitType, cryptoType)

        val address = if (cryptoType == CurrencyUnit.SART) {
            algoCustomTokenWallet.createAddressNotSave(cryptoType.toString())
        } else {
            bitGoWallet.createAddressNotSave(cryptoType.toString())
        }

        try {
            val order = Order(
                amount = amount.amount,
                currency = cryptoType,
                type = type,
                fiatAmount = fiatAmount,
                fiatCurrency = faitType,
                source = null,
                target = target,
                targetEmail = targetEmail,
                externalOrderId = externalOrderId,
                description = description,
                isThirdParty = true,
                orderStatus = TransactionStatus.IN_PROGRESS,
                walletAddress = address
            )

            val amountObj = posService.getCryptoAmount(fiatAmount, faitType, cryptoType)
            var cryptoAmount = amountObj.amount
            val wadzPayFee =
                posService.getMdrPercentage(cryptoAmount, cryptoType, target?.mdrPercentage ?: BigDecimal.ZERO)
            val saltKey = EncoderUtil.getNewSaltKey()
            val posSalt = posSaltRepository.save(PosSalt(saltKey))
            if (cryptoAmount.stripTrailingZeros().scale() > 8) {
                val am =
                    cryptoAmount.setScale(8, RoundingMode.UP)
                        .stripTrailingZeros()
                cryptoAmount = am
            }

            val tempPosTransaction = PosTransaction(
                target as Merchant,
                null,
                cryptoType,
                faitType,
                address,
                fiatAmount,
                cryptoAmount,
                wadzPayFee,
                BigDecimal.ZERO,
                TransactionType.ORDER,
                targetEmail.toString(),
                null,
                description,
                TransactionStatus.IN_PROGRESS, BigDecimal.ZERO, amountObj.exchangeRate, BigDecimal.ZERO
            )

            val senderSubaccount = ledgerService.getOmnibusAccount()

            val transaction = Transaction(
                sender = senderSubaccount.getSubaccountByAsset(cryptoType),
                receiver = target.account.getSubaccountByAsset(cryptoType),
                asset = cryptoType.name,
                amount = cryptoAmount,
                fiatAsset = faitType,
                fiatAmount = tempPosTransaction.amountfiat,
                status = TransactionStatus.IN_PROGRESS,
                type = TransactionType.ORDER,
                description = tempPosTransaction.description,
                fee = wadzPayFee, externalId = order.externalOrderId
            )
            val trx: Transaction
            try {
                trx = transactionRepository.save(transaction)
            } catch (e: Exception) {
                logger.info(e.message.toString())
                throw BadRequestException(ErrorCodes.DUPLICATE_EXTERNAL_TRX_ID)
            }
            logger.info(transaction.toString())
            val posTransaction = posTransactionRepository.save(tempPosTransaction)

            posSalt.posTransaction = posTransaction
            posTransaction.saltkey = posSalt
            posSaltRepository.save(posSalt)

            transaction.posTransaction?.add(posTransaction)
            order.transaction = trx

            order.posTransaction?.add(posTransaction)
            val orderF = orderRepository.save(order)

            trx.order = orderF

            posTransaction.transaction = transactionRepository.save(trx.setTxId("WO", trx.id))

            posTransaction.order = orderF

            posTransactionRepository.save(posTransaction)

            return orderF
        } catch (ex: ConstraintViolationException) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_TOO_MANY_DECIMAL_PLACES)
        }
    }

    fun requestPayment(
        cryptoType: CurrencyUnit,
        type: OrderType,
        fiatAmount: BigDecimal,
        faitType: FiatCurrencyUnit,
        target: Merchant?,
        targetEmail: String?,
        externalOrderId: String?,
        description: String? = null,
        principal: Principal,
        requesterUserName: String? = null,
        requesterEmailAddress: String? = null,
        requesterMobileNumber: String? = null,
    ): Order {

        if (type == OrderType.WITHDRAWAL && targetEmail == null) {
            throw BadRequestException(ErrorCodes.INVALID_EMAIL)
        }

        val amount = posService.getCryptoAmount(fiatAmount, faitType, cryptoType)

        val address = if (cryptoType == CurrencyUnit.SART) {
            algoCustomTokenWallet.createAddressNotSave(cryptoType.toString())
        } else {
            bitGoWallet.createAddressNotSave(cryptoType.toString())
        }

        try {
            val order = Order(
                amount = amount.amount,
                currency = cryptoType,
                type = type,
                fiatAmount = fiatAmount,
                fiatCurrency = faitType,
                source = null,
                target = target,
                targetEmail = targetEmail,
                externalOrderId = externalOrderId,
                description = description,
                isThirdParty = true,
                orderStatus = TransactionStatus.IN_PROGRESS,
                walletAddress = address,
                requesterUserName = requesterUserName,
                requesterEmailAddress = requesterEmailAddress,
                requesterMobileNumber = requesterMobileNumber
            )

            val amountObj = posService.getCryptoAmount(fiatAmount, faitType, cryptoType)
            var cryptoAmount = amountObj.amount
            val wadzPayFee =
                posService.getMdrPercentage(cryptoAmount, cryptoType, target?.mdrPercentage ?: BigDecimal.ZERO)
            val saltKey = EncoderUtil.getNewSaltKey()
            val posSalt = posSaltRepository.save(PosSalt(saltKey))
            if (cryptoAmount.stripTrailingZeros().scale() > 8) {
                val am =
                    cryptoAmount.setScale(8, RoundingMode.UP)
                        .stripTrailingZeros()
                cryptoAmount = am
            }

            val tempPosTransaction = PosTransaction(
                target as Merchant,
                null,
                cryptoType,
                faitType,
                address,
                fiatAmount,
                cryptoAmount,
                wadzPayFee,
                BigDecimal.ZERO,
                TransactionType.ORDER,
                targetEmail.toString(),
                null,
                description,
                TransactionStatus.IN_PROGRESS, BigDecimal.ZERO, amountObj.exchangeRate, BigDecimal.ZERO
            )

            val senderSubaccount = ledgerService.getOmnibusAccount()

            val transaction = Transaction(
                sender = senderSubaccount.getSubaccountByAsset(cryptoType),
                receiver = target.account.getSubaccountByAsset(cryptoType),
                asset = cryptoType.name,
                amount = cryptoAmount,
                fiatAsset = faitType,
                fiatAmount = tempPosTransaction.amountfiat,
                status = TransactionStatus.IN_PROGRESS,
                type = TransactionType.ORDER,
                description = tempPosTransaction.description,
                fee = wadzPayFee, externalId = order.externalOrderId
            )
            val trx: Transaction
            try {
                trx = transactionRepository.save(transaction)
            } catch (e: Exception) {
                logger.info(e.message.toString())
                throw BadRequestException(ErrorCodes.DUPLICATE_EXTERNAL_TRX_ID)
            }
            logger.info(transaction.toString())
            val posTransaction = posTransactionRepository.save(tempPosTransaction)

            posSalt.posTransaction = posTransaction
            posTransaction.saltkey = posSalt
            posSaltRepository.save(posSalt)

            transaction.posTransaction?.add(posTransaction)
            order.transaction = trx

            order.posTransaction?.add(posTransaction)
            val orderF = orderRepository.save(order)

            trx.order = orderF

            posTransaction.transaction = transactionRepository.save(trx.setTxId("WO", trx.id))

            posTransaction.order = orderF

            posTransactionRepository.save(posTransaction)

            return orderF
        } catch (ex: ConstraintViolationException) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_TOO_MANY_DECIMAL_PLACES)
        }
    }

    fun createTransactionFromOrder(order: Order, userAccount: UserAccount) {
        if (order.type === OrderType.WITHDRAWAL && order.targetEmail !== userAccount.email) {
            throw UnauthorizedException(ErrorCodes.INCORRECT_TARGET_EMAIL)
        }

        when (order.type) {
            OrderType.ORDER -> order.source = userAccount
            OrderType.WITHDRAWAL -> order.target = userAccount
        }
        try {
            order.transaction = ledgerService.createMerchantTransaction(
                order.source!!,
                order.target!!,
                order.amount,
                order.currency,
                order.fiatAmount,
                order.fiatCurrency,
                order.description
            )
        } catch (ex: Exception) {
            order.isFailed = true
            throw ex
        } finally {
            orderRepository.save(order)
            webhookService.triggerWebhook(order)
        }
        transactionService.showPushNotifications(order.transaction!!)
    }

    fun cancelOrder(order: Order, userAccount: UserAccount) {
        if (order.type === OrderType.WITHDRAWAL && order.targetEmail !== userAccount.email) {
            throw UnauthorizedException(ErrorCodes.INCORRECT_TARGET_EMAIL)
        }

        order.isCancelled = true
        orderRepository.save(order)
        webhookService.triggerWebhook(order)
    }

    @Async
    fun triggerWebhookIfOrderExpires(orderUuid: UUID) {
        val order = orderRepository.getByUuid(orderUuid)
        val merchant = order?.target as Merchant

        Thread.sleep(merchant.orderExpTimeInMin * MILISECONDS_IN_MINUTE)
        webhookService.triggerWebhook(order)
    }

    fun getOrderByUuid(uuid: UUID): Order {
        return orderRepository.getByUuid(uuid) ?: throw EntityNotFoundException(ErrorCodes.ORDER_NOT_FOUND)
    }

    fun getOpenOrderByUuid(uuid: UUID): Order {
        val order = getOrderByUuid(uuid)
        return when (order.status) {
            OrderStatus.EXPIRED ->
                throw UnprocessableEntityException(ErrorCodes.EXPIRED_ORDER)

            OrderStatus.PROCESSED ->
                throw UnprocessableEntityException(ErrorCodes.PROCESSED_ORDER)

            OrderStatus.CANCELLED ->
                throw UnprocessableEntityException(ErrorCodes.CANCELLED_ORDER)

            OrderStatus.FAILED ->
                throw UnprocessableEntityException(ErrorCodes.FAILED_ORDER)

            OrderStatus.OPEN -> order

            else -> {
                order
            }
        }
    }

    fun processThirdPartyOrder(
        bitGoTransfer: BitGoTransfer,
        bitGoTransferEntry: BitGoTransferEntry,
        asset: CurrencyUnit
    ) {
        // Saving from multiple confirmation
        val byTransferIdAndType =
            blockConfirmRepository.getByTransferIdAndType(bitGoTransfer.id, TransactionType.ORDER)
        if (byTransferIdAndType != null) {
            byTransferIdAndType.blockConfirmationCount++
            byTransferIdAndType.updatedAt = Instant.now()
            blockConfirmRepository.save(byTransferIdAndType)
            logger.info("For ORDER transaction, the transaction is already committed, hence not committing again")
            return
        } else {
            blockConfirmRepository.save(
                BlockConfirmationLogger(
                    bitGoTransfer.txid,
                    bitGoTransfer.id,
                    bitGoTransferEntry.wallet,
                    TransactionType.ORDER,
                    1
                )
            )
        }

        /*if (!posLogCommitsRepository.getByBlockId(bitGoTransfer.txid).isNullOrEmpty()) {
            logger.info("For cryptoAddressForOrder, the transaction is already committed, hence not committing again")
            return
        }*/
        posLogCommitsRepository.save(
            PosLogCommits(
                bitGoTransferEntry.wallet.toString(),
                bitGoTransferEntry.address,
                bitGoTransferEntry.valueString.normalize(asset.toBitGoCoin(appConfig.production)).toString(),
                asset,
                TransactionType.ORDER,
                bitGoTransfer.txid,
                bitGoTransfer.entries.toString(),
                bitGoTransfer.id,
                bitGoTransfer.coin,
                bitGoTransfer.feeString.normalize(asset.toBitGoCoin(appConfig.production)).toString(),
                bitGoTransfer.type,
                bitGoTransfer.baseValueString.normalize(asset.toBitGoCoin(appConfig.production)).toString()
            )
        )
        // val order = orderRepository.getByWalletAddress(bitGoTransferEntry.address)
        val posTransaction =
            posTransactionRepository.getByAddressAndType(bitGoTransferEntry.address, TransactionType.ORDER)
        val order = posTransaction?.order
        val cryptoFromBitGo = bitGoTransfer.baseValueString.normalize(asset.toBitGoCoin(appConfig.production))
        // val trx = order?.transaction
        posTransaction?.transaction?.paymentReceivedDate = Instant.now()
        if (order != null) {
            if (cryptoFromBitGo == order.amount) {
                order.orderStatus = TransactionStatus.SUCCESSFUL
                posTransaction.status = TransactionStatus.SUCCESSFUL
                order.transaction?.status = TransactionStatus.SUCCESSFUL
            } else if (cryptoFromBitGo > order.amount) {
                order.orderStatus = TransactionStatus.OVERPAID
                posTransaction.status = TransactionStatus.OVERPAID
                order.transaction?.status = TransactionStatus.OVERPAID
            } else {
                order.orderStatus = TransactionStatus.UNDERPAID
                posTransaction.status = TransactionStatus.UNDERPAID
                order.transaction?.status = TransactionStatus.UNDERPAID
            }

            posTransaction.totalFiatReceived =
                (cryptoFromBitGo / posTransaction.conversion_rate).setScale(2, RoundingMode.UP)

            posTransactionRepository.save(posTransaction)

            try {
                logger.info(ledgerService.getOmnibusAccount().owner.toString())

                val sender = ledgerService.getOmnibusAccount()

                order.transaction = order.target?.account?.let {
                    order.transaction?.let { it1 ->
                        ledgerService.createTransaction(
                            it, sender, true,
                            it1, order.currency, cryptoFromBitGo
                        )
                    }
                }
            } catch (ex: Exception) {
                order.isFailed = true
                throw ex
            } finally {
                order.posTransaction?.forEach {
                    it.digitalCurrencyReceived = cryptoFromBitGo
                    posTransactionRepository.save(it)
                }
                val trx = order.transaction
                trx?.status = order.orderStatus!!
                trx?.totalDigitalCurrencyReceived = posTransaction.digitalCurrencyReceived
                trx?.totalFiatReceived = posTransaction.totalFiatReceived
                trx?.blockchainTxId = bitGoTransfer.txid
                trx?.sourceWalletAddress = bitGoTransfer.entries.find { it.valueString < BigDecimal.ZERO }?.address

                if (trx != null) {
                    transactionRepository.save(trx)
                }

                orderRepository.save(order)
                webhookService.triggerWebhook(order)
            }
            order.transaction?.let { transactionService.showPushNotifications(it) }
        }
    }

    fun processThirdPartyOrderFake(
        address: String,
        cryptoFromBitGo: BigDecimal
    ): Order? {
        // val order = orderRepository.getByWalletAddress(address)
        val posTransaction = posTransactionRepository.getByAddressAndType(address, TransactionType.ORDER)
        logger.info(posTransaction.toString())
        val order = posTransaction?.order
        logger.info(order.toString())

        order?.transaction?.totalDigitalCurrencyReceived = cryptoFromBitGo
        posTransaction?.transaction?.paymentReceivedDate = Instant.now()
        if (order != null) {
            if (cryptoFromBitGo == order.amount) {
                order.orderStatus = TransactionStatus.SUCCESSFUL
                posTransaction.status = TransactionStatus.SUCCESSFUL
                order.transaction?.status = TransactionStatus.SUCCESSFUL
            } else if (cryptoFromBitGo > order.amount) {
                order.orderStatus = TransactionStatus.OVERPAID
                posTransaction.status = TransactionStatus.OVERPAID
                order.transaction?.status = TransactionStatus.OVERPAID
            } else {
                order.orderStatus = TransactionStatus.UNDERPAID
                posTransaction.status = TransactionStatus.UNDERPAID
                order.transaction?.status = TransactionStatus.UNDERPAID
            }

            posTransaction.totalFiatReceived =
                (cryptoFromBitGo / posTransaction.conversion_rate).setScale(2, RoundingMode.UP)

            posTransactionRepository.save(posTransaction)

            try {
                logger.info(ledgerService.getOmnibusAccount().owner.toString())

                val sender = ledgerService.getOmnibusAccount()

                order.transaction = order.target?.account?.let {
                    order.transaction?.let { it1 ->
                        ledgerService.createTransaction(
                            it, sender, true,
                            it1, order.currency, cryptoFromBitGo
                        )
                    }
                }
            } catch (ex: Exception) {
                order.isFailed = true
                throw ex
            } finally {
                order.posTransaction?.forEach {
                    it.digitalCurrencyReceived = cryptoFromBitGo
                    posTransactionRepository.save(it)
                }
                val trx = order.transaction
                trx?.status = order.orderStatus!!
                trx?.totalFiatReceived = posTransaction.totalFiatReceived
                if (trx != null) {
                    transactionRepository.save(trx)
                }
                orderRepository.save(order)
                webhookService.triggerWebhook(order)
            }
            order.transaction?.let { transactionService.showPushNotifications(it) }
        }

        return order
    }

    fun getRefreshOrderByUuid(orderId: UUID): OrderRefreshResponse {
        val order = getOrderByUuid(orderId)

        val posTransaction =
            posTransactionRepository.getByAddressAndType(order.walletAddress.toString(), TransactionType.ORDER)
                ?: throw EntityNotFoundException("Transaction with BlockChain Address ${order.walletAddress} not found")

        val newBlockChainAddress = if (posTransaction.assetcrypto == CurrencyUnit.SART) {
            algoCustomTokenWallet.createAddressNotSave(posTransaction.assetcrypto.toString())
        } else {
            bitGoWallet.createAddressNotSave(posTransaction.assetcrypto.toString())
        }

        val amountObj = posService.getCryptoAmount(
            posTransaction.amountfiat ?: BigDecimal.ZERO,
            posTransaction.assetfiat,
            posTransaction.assetcrypto
        )

        val newPosTransaction = PosTransaction(
            posTransaction.merchant,
            posTransaction.merchantPos,
            posTransaction.assetcrypto,
            posTransaction.assetfiat,
            newBlockChainAddress,
            posTransaction.amountfiat,
            amountObj.amount,
            posService.getMdrPercentage(
                amountObj.amount,
                posTransaction.assetcrypto,
                posTransaction.merchant.mdrPercentage
            ),
            BigDecimal.ZERO,
            posTransaction.type,
            posTransaction.senderid,
            posTransaction.blockchainid,
            posTransaction.description,
            TransactionStatus.valueOf(order.orderStatus?.name.toString()),
            posTransaction.digitalCurrencyReceived,
            amountObj.exchangeRate, posTransaction.totalFiatReceived
        )

        order.walletAddress = newBlockChainAddress

        order.posTransaction?.add(newPosTransaction)
        order.transaction?.posTransaction?.add(newPosTransaction)

        val tx = order.transaction
        if (tx != null) {
            transactionRepository.save(tx)
        }
        orderRepository.save(order)
        newPosTransaction.order = order
        newPosTransaction.transaction = tx
        posTransactionRepository.save(newPosTransaction)

        return OrderRefreshResponse(
            order.uuid,
            amountObj.amount,
            order.currency,
            order.type,
            order.fiatAmount,
            order.fiatCurrency,
            order.targetEmail,
            order.createdAt,
            order.externalOrderId,
            order.description,
            order.isCancelled,
            order.isFailed,
            order.isThirdParty,
            order.walletAddress,
            order.orderStatus,
            order.status,
            tx?.totalDigitalCurrencyReceived, posTransaction.totalFiatReceived, posTransaction.conversion_rate
        )
    }

    data class OrderRefreshResponse(
        val uuid: UUID,
        val amount: BigDecimal,
        val currency: CurrencyUnit,
        val type: OrderType? = null,
        val fiatAmount: BigDecimal?,
        val fiatCurrency: FiatCurrencyUnit?,
        val targetEmail: String?,
        val createdAt: Instant,
        val externalOrderId: String?,
        val description: String?,
        val isCancelled: Boolean,
        val isFailed: Boolean,
        val isThirdParty: Boolean,
        val walletAddress: String?,
        val orderStatus: TransactionStatus?,
        val status: OrderStatus,
        val totalDigitalCurrencyReceived: BigDecimal?,
        val totalFiatReceived: BigDecimal,
        val conversionRate: BigDecimal
    )
}
