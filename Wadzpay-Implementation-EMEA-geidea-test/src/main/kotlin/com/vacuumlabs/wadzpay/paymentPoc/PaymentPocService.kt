package com.vacuumlabs.wadzpay.paymentPoc

import com.vacuumlabs.DEV_URL
import com.vacuumlabs.FAILED_URL
import com.vacuumlabs.FAKE_COMMIT_BLOCKCHAIN
import com.vacuumlabs.PG_CANCEL_URL
import com.vacuumlabs.REFUND_DESCRIPTION
import com.vacuumlabs.ROUNDING_LIMIT
import com.vacuumlabs.SUCCESS_URL
import com.vacuumlabs.TEST_URL
import com.vacuumlabs.VEPAY_MERCHANT
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.UnprocessableEntityException
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.fledger.model.UserBankAccount
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.LedgerService
import com.vacuumlabs.wadzpay.ledger.model.AccountRepository
import com.vacuumlabs.wadzpay.ledger.model.Transaction
import com.vacuumlabs.wadzpay.ledger.model.TransactionRepository
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.ledger.service.GetTransactionListRequest
import com.vacuumlabs.wadzpay.ledger.service.TransactionService
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.FiatSubAccount
import com.vacuumlabs.wadzpay.merchant.model.FiatSubAccountRepository
import com.vacuumlabs.wadzpay.notification.NotificationService
import com.vacuumlabs.wadzpay.paymentPoc.models.BankDetails
import com.vacuumlabs.wadzpay.paymentPoc.models.PaymentMode
import com.vacuumlabs.wadzpay.paymentPoc.models.PaymentPOClogs
import com.vacuumlabs.wadzpay.paymentPoc.models.PaymentPOClogsRepository
import com.vacuumlabs.wadzpay.paymentPoc.models.PaymentType
import com.vacuumlabs.wadzpay.paymentPoc.models.PayoutStatusCode
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPOCRefundRequest
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPOCRefundResponse
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPOCRequest
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPOCResponse
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPaymentStatusCheckRequest
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPaymentStatusCheckResponse
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPaymentTransferRequest
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPaymentTransferResponse
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayPayoutStatusCheckRequest
import com.vacuumlabs.wadzpay.paymentPoc.models.VePayWebhookResponse
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.user.UserInitializerService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import javax.transaction.Transactional

@Service
class PaymentPocService(
    val notificationService: NotificationService,
    val accountRepository: AccountRepository,
    val userInitializerService: UserInitializerService,
    val fiatSubAccountRepository: FiatSubAccountRepository,
    val vePayService: VePayService,
    val vePayConfiguration: VePayConfiguration,
    val paymentPOClogsRepository: PaymentPOClogsRepository,
    val transactionRepository: TransactionRepository,
    val userAccountService: UserAccountService,
    val transactionService: TransactionService,
    val ledgerService: LedgerService,
    val appConfig: AppConfig
) {
    fun createDepositFiatLink(
        depositRequest: PaymentPocController.DepositRequestPayment,
        userAccount: UserAccount
    ): PaymentPocController.DepositResponse? {
        if (userAccount.firstName == null && depositRequest.firstName == null) {
            throw UnprocessableEntityException(ErrorCodes.BAD_REQUEST)
        }
        if (depositRequest.fiatType != null && depositRequest.fiatType != FiatCurrencyUnit.INR) {
            throw UnprocessableEntityException(ErrorCodes.CURRENTLY_NOT_SUPPORTED_THIS_FIAT)
        }
        val fiatSubAccount = userAccount.account.fiatSubAccount.find { it.fiatasset == depositRequest.fiatType }
        if (fiatSubAccount == null) {
            createFiatSubAccount(userAccount, depositRequest.fiatType, BigDecimal.ZERO)
        }
        val vePayPOCResponse = generateRefLinkUsingVePay(depositRequest, userAccount)
        // println("vePayPOCResponse ==>${vePayPOCResponse.toString()}")
        if (vePayPOCResponse != null && vePayPOCResponse.status && vePayPOCResponse.status_code == 200) {
            val digitalAsset: CurrencyUnit = CurrencyUnit.USDT
            val digitalAmount = 0.000001.toBigDecimal()
            val merchant = userInitializerService.getFakeMerchant()
            val transaction = userInitializerService.createFiatPendingTransaction(
                merchant.account.subaccounts.find { it.asset == digitalAsset.toString() }!!,
                userAccount.account.subaccounts.find { it.asset == digitalAsset.toString() }!!,
                digitalAsset,
                digitalAmount,
                depositRequest.fiatType,
                depositRequest.fiatAmount
            )
            // println("transaction ==> $transaction")
            if (transaction != null) {
                var paymentPOCLogs = vePayPOCResponse.transaction_id?.let {
                    paymentPOClogsRepository.getByTransactionId(
                        it
                    )
                }
                if (paymentPOCLogs != null) {
                    paymentPOCLogs.wadzpayTrxId = transaction.id.toString()
                    paymentPOClogsRepository.save(paymentPOCLogs)
                }
            }
            val depositResponse = vePayPOCResponse?.ref_link?.let {
                PaymentPocController.DepositResponse(
                    it
                )
            }
            return depositResponse
        } else {
            throw UnprocessableEntityException(ErrorCodes.BAD_REQUEST)
        }
    }

    private fun generateRefLinkUsingVePay(
        depositRequest: PaymentPocController.DepositRequestPayment,
        userAccount: UserAccount
    ): VePayPOCResponse? {
        val merchantOrderToken = VEPAY_MERCHANT + Instant.now().epochSecond
        var successUrl = TEST_URL + SUCCESS_URL
        var failUrl = TEST_URL + FAILED_URL
        var pgCancelUrl = TEST_URL + PG_CANCEL_URL
        if (appConfig.environment.equals("dev", true)) {
            successUrl = DEV_URL + SUCCESS_URL
            failUrl = DEV_URL + FAILED_URL
            pgCancelUrl = DEV_URL + PG_CANCEL_URL
        }
        val vePayPOCRequest = VePayPOCRequest(
            curr_code = depositRequest.fiatType.toString(),
            amount = generateAmount(depositRequest.fiatAmount),
            desc = "Deposit Fiat For user ${userAccount.phoneNumber}",
            merchant_order_token = merchantOrderToken,
            customer_email = userAccount.email,
            customer_mobile = userAccount.phoneNumber?.drop(3),
            customer_first_name = if (userAccount.firstName != null) userAccount.firstName else depositRequest.firstName,
            customer_last_name = if (userAccount.lastName != null) userAccount.lastName else depositRequest.lastName,
            ord_title = "Deposit Fiat ${depositRequest.fiatAmount}",
            success_url = successUrl,
            fail_url = failUrl,
            pg_cancel_url = pgCancelUrl,
            api_key = vePayConfiguration.api_key
        )
        val vePayPOCResponse = vePayService.vePayPaymentIntent(vePayPOCRequest)
        if (vePayPOCResponse != null && vePayPOCResponse.status && vePayPOCResponse.status_code == 200) {
            val paymentPOClogs = PaymentPOClogs(
                userAccount = userAccount,
                userEmail = userAccount.email,
                transactionType = TransactionType.DEPOSIT.toString(),
                currCode = depositRequest.fiatType.toString(),
                amount = depositRequest.fiatAmount,
                merchantOrderToken = merchantOrderToken,
                ordTitle = "Deposit Fiat ${depositRequest.fiatAmount}",
                successUrl = successUrl,
                failUrl = failUrl,
                pgCancelUrl = pgCancelUrl,
                status = vePayPOCResponse.status.toString(),
                responseData = vePayPOCResponse.data,
                transactionId = vePayPOCResponse.transaction_id,
                message = vePayPOCResponse.message,
                statusCode = vePayPOCResponse.status_code.toString(),
                refLink = vePayPOCResponse.ref_link
            )
            paymentPOClogsRepository.save(paymentPOClogs)
        }
        return vePayPOCResponse
    }

    @Transactional
    fun updateDepositFiatTxn(vePayWebhookResponse: VePayWebhookResponse) {
        if (vePayWebhookResponse.status != null) {
            val paymentPOClogs =
                vePayWebhookResponse.transactionId?.let { paymentPOClogsRepository.getByTransactionId(it) }
            if (paymentPOClogs != null) {
                val userAccount = paymentPOClogs.userEmail?.let { userAccountService.getUserAccountByEmail(it) }
                val transaction = paymentPOClogs.wadzpayTrxId?.let { transactionRepository.getById(it.toLong()) }
                if (transaction != null && transaction.status != TransactionStatus.SUCCESSFUL) {
                    paymentPOClogs.orderId = vePayWebhookResponse.orderId
                    paymentPOClogs.status = vePayWebhookResponse.status
                    paymentPOClogs.message = vePayWebhookResponse.message
                    paymentPOClogs.paymentType = vePayWebhookResponse.payment_details.toString()
                    if (vePayWebhookResponse.status == PayoutStatusCode.CAPTURED.toString()) {
                        transaction.status = TransactionStatus.SUCCESSFUL
                        if (userAccount != null) {
                            val fiatSubAccount = userAccount.account.fiatSubAccount.find { it.fiatasset == transaction.fiatAsset }
                            if (fiatSubAccount != null) {
                                transaction.fiatAsset?.let {
                                    transaction.fiatAmount?.let {
                                        it1 ->
                                        creditFiatToUserDeposit(
                                            it, userAccount,
                                            it1
                                        )
                                    }
                                }
                            }
                        }
                        var fiatAmountRoundOf = ""
                        fiatAmountRoundOf =
                            if (transaction.fiatAmount!!.stripTrailingZeros().scale() > ROUNDING_LIMIT) {
                                transaction.fiatAmount!!.setScale(ROUNDING_LIMIT, RoundingMode.UP).stripTrailingZeros()
                                    .toString()
                            } else {
                                transaction.fiatAmount!!.stripTrailingZeros()?.toPlainString().toString()
                            }
                        if (userAccount != null) {
                            notificationService.sendPushNotifications(
                                userAccount,
                                "Deposit",
                                "+ $fiatAmountRoundOf ${transaction.fiatAsset} Deposited"
                            )
                        }
                    } else {
                        transaction.status = TransactionStatus.FAILED
                        if (userAccount != null) {
                            var fiatAmountRoundOf = ""
                            fiatAmountRoundOf =
                                if (transaction.fiatAmount!!.stripTrailingZeros().scale() > ROUNDING_LIMIT) {
                                    transaction.fiatAmount!!.setScale(ROUNDING_LIMIT, RoundingMode.UP)
                                        .stripTrailingZeros()
                                        .toString()
                                } else {
                                    transaction.fiatAmount!!.stripTrailingZeros()?.toPlainString().toString()
                                }
                            notificationService.sendPushNotifications(
                                userAccount,
                                "Deposit Failed",
                                "Deposit Failed for $fiatAmountRoundOf ${transaction.fiatAsset}"
                            )
                        }
                    }
                    paymentPOClogsRepository.save(paymentPOClogs)
                    transactionRepository.save(transaction)
                } else {
                    if (transaction != null) {
                        println("transaction ID ==> " + transaction.tx_id)
                        println("transaction status ==> " + transaction.status)
                    }
                }
            }
        }
    }

    fun creditFiatToUserDeposit(
        fiatType: FiatCurrencyUnit,
        userAccount: UserAccount,
        fiatValueAfterSell: BigDecimal
    ) {
        println("FiatCurrencyUnit == >" + fiatType)
        val fiatSubAccount = userAccount.account.fiatSubAccount.find { it.fiatasset == fiatType }
        if (fiatSubAccount != null) {
            println("fiatSubAccount == >" + fiatSubAccount)
            val fiatBalance = fiatSubAccount.balance

            var fiatValueToCredit = fiatBalance + fiatValueAfterSell
            if (fiatValueToCredit.stripTrailingZeros().scale() > ROUNDING_LIMIT) {
                fiatValueToCredit = fiatValueToCredit.setScale(ROUNDING_LIMIT, RoundingMode.UP)
            }
            fiatSubAccount.balance = fiatValueToCredit
            fiatSubAccountRepository.save(fiatSubAccount)
        }
    }

    private fun createFiatSubAccount(
        userAccount: UserAccount,
        fiatCurrencyUnit: FiatCurrencyUnit,
        initialBalance: BigDecimal
    ): Boolean {
        val fiatSubAccount = FiatSubAccount(userAccount.account.reference, userAccount.account, fiatCurrencyUnit)
        fiatSubAccount.balance = initialBalance
        userAccount.account.fiatSubAccount.add(fiatSubAccount)
        accountRepository.save(userAccount.account)
        fiatSubAccountRepository.save(fiatSubAccount)
        return true
    }

    fun paymentStatusCheck(
        statusCheckRequest: PaymentPocController.PaymentStatusCheckRequest,
        userAccount: UserAccount
    ): VePayPaymentStatusCheckResponse? {
        var transactionList = GetTransactionListRequest(tx_id = statusCheckRequest.txnId)
        var transactionData = transactionService.getTransactionViewModels(userAccount, transactionList)
        if (transactionData != null && transactionData.isNotEmpty()) {
            var transaction = transactionRepository.getByUuid(transactionData[0].uuid)
            if (transaction != null) {
                var paymentPOCLogs = paymentPOClogsRepository.getByWadzpayTrxId(transaction.id.toString())
                var veStatusCheckRequest = VePayPaymentStatusCheckRequest(identifier = paymentPOCLogs?.transactionId)
                return vePayService.vePayPaymentStatusCheck(veStatusCheckRequest)
            }
        }
        throw EntityNotFoundException(ErrorCodes.TRANSACTION_NOT_FOUND)
    }

    fun createWithdrawFiat(
        withdrawRequest: PaymentPocController.WithdrawRequestPayment,
        userAccount: UserAccount
    ): VePayPaymentTransferResponse? {
        if (withdrawRequest.fiatType != null && withdrawRequest.fiatType != FiatCurrencyUnit.INR) {
            throw UnprocessableEntityException(ErrorCodes.CURRENTLY_NOT_SUPPORTED_THIS_FIAT)
        }
        val fiatSubAccount = userAccount.account.fiatSubAccount.find { it.fiatasset == withdrawRequest.fiatType }
        println("fiatSubAccount ==> $fiatSubAccount")
        if (fiatSubAccount == null) {
            createFiatSubAccount(userAccount, withdrawRequest.fiatType, BigDecimal.ZERO)
        } else if (fiatSubAccount.balance < withdrawRequest.fiatAmount) {
            throw UnprocessableEntityException(ErrorCodes.INSUFFICIENT_FUNDS)
        }
        val fiatAmount = withdrawRequest.fiatAmount ?: BigDecimal.TEN
        val userBankAccount =
            userAccount.userBankAccount.find { it.bankAccountNumber == withdrawRequest.bankAccountNumber }
                ?: throw BadRequestException(ErrorCodes.USER_BANK_ACCOUNT_NOT_FOUND)
        val vePayPaymentTransferResponse = transferToBankAccount(withdrawRequest, userAccount, userBankAccount)
        if (vePayPaymentTransferResponse != null) {
            if (vePayPaymentTransferResponse.status == PayoutStatusCode.CREATED.toString()) {
                val digitalAsset: CurrencyUnit = CurrencyUnit.USDT
                val digitalAmount = 0.000001.toBigDecimal()
                val merchant = userInitializerService.getFakeMerchant()
                val transaction = userInitializerService.createFiatWithDrawPendingTransaction(
                    userAccount.account.subaccounts.find { it.asset == digitalAsset.toString() }!!,
                    merchant.account.subaccounts.find { it.asset == digitalAsset.toString() }!!,
                    digitalAsset,
                    digitalAmount,
                    withdrawRequest.fiatType,
                    fiatAmount,
                    withdrawRequest.bankAccountNumber
                )
                if (transaction != null) {
                    var paymentPOCLogs = vePayPaymentTransferResponse.transactionId?.let {
                        paymentPOClogsRepository.getByTransactionId(
                            it
                        )
                    }
                    if (paymentPOCLogs != null) {
                        paymentPOCLogs.wadzpayTrxId = transaction.id.toString()
                        paymentPOClogsRepository.save(paymentPOCLogs)
                    }
                }
                if (fiatSubAccount != null) {
                    val balance = fiatSubAccount?.balance
                    var saveBalance = (balance?.minus(fiatAmount))
                    if (saveBalance != null) {
                        if (saveBalance.stripTrailingZeros().scale() > ROUNDING_LIMIT) {
                            saveBalance = saveBalance.setScale(ROUNDING_LIMIT, RoundingMode.UP)
                        }
                        if (fiatSubAccount != null) {
                            if (saveBalance != null) {
                                fiatSubAccount.balance = saveBalance
                            }
                        }
                    }
                    fiatSubAccountRepository.save(fiatSubAccount)
                }
                return vePayPaymentTransferResponse
            } else if (vePayPaymentTransferResponse.status == PayoutStatusCode.INSUFFICIENT_BALANCE.toString()) {
                throw UnprocessableEntityException(ErrorCodes.INSUFFICIENT_FUNDS_VEPAY)
            } else {
                throw vePayPaymentTransferResponse.status?.let { UnprocessableEntityException(it) }!!
            }
        }
        throw UnprocessableEntityException(ErrorCodes.BAD_REQUEST)
    }

    private fun transferToBankAccount(
        withdrawRequest: PaymentPocController.WithdrawRequestPayment,
        userAccount: UserAccount,
        userBankAccount: UserBankAccount
    ): VePayPaymentTransferResponse? {
        var merchantOrderToken = VEPAY_MERCHANT + Instant.now().epochSecond
        println("merchantOrderToken $merchantOrderToken")
        var bankDetails = BankDetails(
            type = PaymentType.bank,
            account_number = userBankAccount.bankAccountNumber,
            ifsc_code = userBankAccount.ifscCode,
            beneficiary_name = userBankAccount.accountHolderName,
            mode = PaymentMode.NEFT
        )
        var vePayPaymentTransferRequest = VePayPaymentTransferRequest(
            curr_code = withdrawRequest.fiatType.toString(),
            amount = generateAmount(withdrawRequest.fiatAmount),
            purpose = "Withdraw from wallet",
            queue_if_low_balance = false,
            reference_id = merchantOrderToken,
            narration = "Withdraw from wallet and deposit to User Bank Account",
            phonenumber = userAccount.phoneNumber?.drop(3),
            transfer_type = bankDetails
        )
        println("vePayPaymentTransferRequest ==> " + vePayPaymentTransferRequest)
        var vePayPaymentTransferResponse = vePayService.vePayPaymentTransfer(vePayPaymentTransferRequest)
        if (vePayPaymentTransferResponse != null) {
            var paymentPOClogs: PaymentPOClogs? = null
            if (vePayPaymentTransferResponse.status == PayoutStatusCode.CREATED.toString()) {
                paymentPOClogs = PaymentPOClogs(
                    userAccount = userAccount,
                    userEmail = userAccount.email,
                    transactionType = TransactionType.WITHDRAW.toString(),
                    currCode = withdrawRequest.fiatType.toString(),
                    amount = withdrawRequest.fiatAmount,
                    merchantOrderToken = merchantOrderToken,
                    ordTitle = "Withdraw Fiat ${withdrawRequest.fiatAmount}",
                    status = vePayPaymentTransferResponse.status.toString(),
                    transactionId = vePayPaymentTransferResponse.transactionId,
                    payId = vePayPaymentTransferResponse.pay_id,
                    referenceId = vePayPaymentTransferResponse.reference_id,
                    message = vePayPaymentTransferResponse.message,
                    paymentType = vePayPaymentTransferResponse.payment_details?.type,
                    accountNumber = vePayPaymentTransferResponse.payment_details?.account_number,
                    ifscCode = vePayPaymentTransferResponse.payment_details?.ifsc_code,
                    beneficiaryName = vePayPaymentTransferResponse.payment_details?.beneficiary_name,
                    paymentMode = vePayPaymentTransferResponse.payment_details?.mode

                )
            } else {
                paymentPOClogs = PaymentPOClogs(
                    userAccount = userAccount,
                    userEmail = userAccount.email,
                    transactionType = TransactionType.WITHDRAW.toString(),
                    currCode = withdrawRequest.fiatType.toString(),
                    amount = withdrawRequest.fiatAmount,
                    merchantOrderToken = merchantOrderToken,
                    ordTitle = "Withdraw Fiat ${withdrawRequest.fiatAmount}",
                    status = vePayPaymentTransferResponse.status.toString(),
                )
            }
            paymentPOClogsRepository.save(paymentPOClogs)
        }
        return vePayPaymentTransferResponse
    }

    fun payoutStatusCheck(
        statusCheckRequest: PaymentPocController.PaymentStatusCheckRequest,
        userAccount: UserAccount
    ): VePayPaymentTransferResponse? {
        var transactionList = GetTransactionListRequest(tx_id = statusCheckRequest.txnId)
        var transactionData = transactionService.getTransactionViewModels(userAccount, transactionList)
        if (transactionData.isNotEmpty()) {
            var transaction = transactionRepository.getByUuid(transactionData[0].uuid)
            if (transaction != null) {
                var paymentPOCLogs = paymentPOClogsRepository.getByWadzpayTrxId(transaction.id.toString())
                var veStatusCheckRequest = VePayPayoutStatusCheckRequest(transactionId = paymentPOCLogs?.transactionId)
                return vePayService.vePayPayoutStatusCheck(veStatusCheckRequest)
            }
        }
        throw EntityNotFoundException(ErrorCodes.TRANSACTION_NOT_FOUND)
    }

    fun updateWithdrawFiatTxn(vePayPayoutTransferResponse: VePayPaymentTransferResponse) {
        // println("vePayPaymentTransferResponse ${vePayPayoutTransferResponse.toString()}")
        if (vePayPayoutTransferResponse != null) {
            val paymentPOClogs =
                vePayPayoutTransferResponse.transactionId?.let { paymentPOClogsRepository.getByTransactionId(it) }
            if (paymentPOClogs != null) {
                // paymentPOClogs.orderId = vePayPayoutTransferResponse.orderId
                paymentPOClogs.status = vePayPayoutTransferResponse.status.toString()
                paymentPOClogs.message = vePayPayoutTransferResponse.message
                if (vePayPayoutTransferResponse.status!!.equals(PayoutStatusCode.SUCCESS)) {
                    // paymentPOClogs.orderId = vePayPayoutTransferResponse.orderId
                    // paymentPOClogs.paymentType = vePayPayoutTransferResponse.payment_details.type
                    // paymentPOClogs.paymentMode = vePayPayoutTransferResponse.payment_details.upi.vpa
                    // paymentPOClogs.status = vePayPayoutTransferResponse.status.toString()
                    // paymentPOClogs.message = vePayPayoutTransferResponse.message
                }
                paymentPOClogsRepository.save(paymentPOClogs)
                // println("paymentPOClogs.wadzpayTrxId ==>" + paymentPOClogs.wadzpayTrxId)
                val transaction = paymentPOClogs.wadzpayTrxId?.let { transactionRepository.getById(it.toLong()) }
                // println("transaction ${transaction.toString()}")
                if (transaction != null) {
                    transaction.status = TransactionStatus.SUCCESSFUL
                    transactionRepository.save(transaction)

                    val userAccount = paymentPOClogs.userEmail?.let { userAccountService.getUserAccountByEmail(it) }
                    transaction.fiatAsset?.let {
                        if (userAccount != null) {
                            // transaction.fiatAmount?.let { it1 -> createFiatSubAccount(userAccount, it, it1) }
                        }
                    }
                }
            }
        }
    }

    fun generateAmount(fiatAmount: BigDecimal): BigDecimal {
        return fiatAmount * 100.toBigDecimal()
    }

    fun refundFiat(
        refundRequest: PaymentPocController.RefundRequest,
        userAccount: UserAccount
    ): VePayPOCRefundResponse? {
        var transactionList = GetTransactionListRequest(tx_id = refundRequest.txnId)
        var transactionData = transactionService.getTransactionViewModels(userAccount, transactionList)
        println("transactionData ==> $transactionData")
        if (transactionData.isNotEmpty()) {
            var transaction = transactionRepository.getByUuid(transactionData[0].uuid)
            println("transaction ==> $transaction")
            if (transaction != null) {
                if (transaction.status != TransactionStatus.SUCCESSFUL) {
                    throw EntityNotFoundException(ErrorCodes.TRANSACTION_INPROGRESS)
                }
                if (transaction.status == TransactionStatus.REFUNDED) {
                    throw EntityNotFoundException(ErrorCodes.INVALID_REFUND_ERROR)
                }
                var paymentPOCLogs = paymentPOClogsRepository.getByWadzpayTrxId(transaction.id.toString())
                println("paymentPOCLogs ==> $paymentPOCLogs")
                if (paymentPOCLogs != null) {
                    var veSPayRefundRequest = VePayPOCRefundRequest(
                        transaction_id = paymentPOCLogs.transactionId,
                        amount = transactionData[0].fiatAmount?.let { generateAmount(it) }
                    )
                    var veRefundResponse = vePayService.vePayRefundFiat(veSPayRefundRequest)
                    if (veRefundResponse != null && veRefundResponse.status == true) {
                        var refundTransaction = transaction.refundTransactions?.get(0)
                        refundTransaction?.refundApprovalComment = refundRequest.reason
                        refundTransaction?.refundSettlementDate = Instant.now()
                        refundTransaction?.refundDateTime = Instant.now()
                        var transactionRefund = refundTransaction(transaction, userAccount)
                    }
                    return veRefundResponse
                } else {
                    throw EntityNotFoundException(ErrorCodes.TRANSACTION_NOT_FOUND)
                }
            } else {
                throw EntityNotFoundException(ErrorCodes.TRANSACTION_NOT_FOUND)
            }
        } else {
            throw EntityNotFoundException(ErrorCodes.TRANSACTION_NOT_FOUND)
        }
    }

    fun refundTransaction(
        refundedTransaction: Transaction,
        userAccount: UserAccount
    ): Transaction {
        val assetDigital: CurrencyUnit = CurrencyUnit.USDT
        val digitalAmount = 0.000001.toBigDecimal()
        val merchant = userInitializerService.getFakeMerchant()

        /* wadpayRefundTransactionsRepository.save(
             WadpayRefundTransactions(
                 owner = refundedBy,
                 transaction = transaction,
                 address = refundedToAddress,
                 assetCrypto = assetDigital,
                 type = TransactionType.REFUND,
                 blockchainHash = transaction.blockchainTxId,
                 description = "Refunded",
                 amountCrypto = refundAmountDigital,
                 feeByWadzpay = feeByWadzPay,
                 feeByBlockchain = estimatedFee
             )
         ) */
        var blockChainAddress = FAKE_COMMIT_BLOCKCHAIN + Instant.now().epochSecond
        return transactionRepository.save(
            Transaction(
                amount = digitalAmount,
                asset = assetDigital.name,
                receiver = merchant.account.subaccounts.find { it.asset == assetDigital.toString() }!!,
                sender = userAccount.account.getSubaccountByAsset(assetDigital),
                status = TransactionStatus.REFUNDED,
                type = TransactionType.REFUND,
                blockchainTxId = blockChainAddress,
                fiatAsset = refundedTransaction.fiatAsset,
                fiatAmount = refundedTransaction.fiatAmount,
                description = REFUND_DESCRIPTION,
                // refundSettlementDate = Instant.now()
            )
        )
    }
}
