package com.vacuumlabs.wadzpay.ledger.scheduler

import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.emailSms.service.EmailSMSSenderService
import com.vacuumlabs.wadzpay.ledger.model.MerchantConfigRepository
import com.vacuumlabs.wadzpay.ledger.model.RefundStatus
import com.vacuumlabs.wadzpay.ledger.model.RefundToken
import com.vacuumlabs.wadzpay.ledger.model.RefundTokenRepository
import com.vacuumlabs.wadzpay.ledger.model.TransactionRefundDetails
import com.vacuumlabs.wadzpay.ledger.model.TransactionRefundDetailsRepository
import com.vacuumlabs.wadzpay.ledger.model.TransactionRepository
import com.vacuumlabs.wadzpay.merchant.model.Merchant
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date

@Service
class ResendWebLinkScheduler(
    var refundTokenRepository: RefundTokenRepository,
    var transactionRefundDetailsRepository: TransactionRefundDetailsRepository,
    var emailSMSSenderService: EmailSMSSenderService,
    val appConfig: AppConfig,
    val transactionRepository: TransactionRepository,
    val merchantConfigRepository: MerchantConfigRepository
) {

    @Scheduled(initialDelayString = "\${scheduler.initialDelay}", fixedRateString = "\${scheduler.fixedRate}")
    fun resendExpiredWebLink() {
//        println("Resend Weblink Scheduler Started At : " + Instant.now())
        val merchantConfigs = merchantConfigRepository.findAllByAutoSendWeblinkRequired(true)

        merchantConfigs.forEach {
            val expiredRefundToken = refundTokenRepository.getExpiredTokens(it.merchant!!.id)
            val merchantConfig = it
//            println("expiredRefundToken  :" + expiredRefundToken?.size)
            expiredRefundToken?.forEach {
                val refundTransaction = it.refundTransaction
                if (refundTransaction != null) {

                    if (refundTransaction.refundStatus == RefundStatus.REFUND_INITIATED) {
                        if ((it.count >= merchantConfig.resendExpiredWeblinkLimitCount + 1) ||
                            (refundTransaction.refundInitiateDate?.plusSeconds(merchantConfig.resendThresholdMaxSeconds)?.isBefore(Instant.now()) == true)
                        ) {
                            refundTransaction.refundStatus = RefundStatus.REFUND_EXPIRED
                            it.isExpired = true
                            transactionRefundDetailsRepository.save(refundTransaction)
                            refundTokenRepository.save(it)
                        } else if (it.count <= (merchantConfig.resendExpiredWeblinkLimitCount + 1)) {
                            val newRefundToken = refundTokenRepository.save(
                                RefundToken(
                                    validFor = Instant.now().plusSeconds((merchantConfig.resendExpiredWeblinkSeconds)),
                                    refundTransaction = refundTransaction,
                                    count = it.count + 1
                                )
                            ) // store the new token
                            it.isExpired = true // set expire to old
                            refundTokenRepository.save(it) // update the old token expired true
                            refundTransaction.refundToken = newRefundToken // map refund to new token
                            transactionRefundDetailsRepository.save(refundTransaction) // update refund
                            sendEmailAndSms(refundTransaction, newRefundToken) // send email
                        }
                    }
                }
            }
        }
//        println("Resend Weblink Scheduler Completed At : " + Instant.now())
    }

    fun sendEmailAndSms(refundTransaction: TransactionRefundDetails, refundToken: RefundToken) {
        if (refundTransaction.refundUserEmail != null) {
            val userAccount: Merchant = ((refundTransaction.transaction.sender.account.owner ?: refundTransaction.transaction.receiver.account.owner) as Merchant?)!!
            try {
                emailSMSSenderService.sendEmail(
                    "Refund Verification",
                    emailSMSSenderService.getMerchantEmailBody(
                        userAccount.primaryContactFullName + " (" + userAccount.name + ")",
                        appConfig.getRefundFormUrl() + refundToken.transactionRefundToken,
                        refundTransaction, refundTransaction.transaction
                    ),
                    refundTransaction.refundUserEmail!!,
                    "contact@wadzpay.com"
                )
            } catch (e: Exception) {
                throw BadRequestException(ErrorCodes.INVALID_EMAIL)
            }
        }

        if (refundTransaction.refundUserMobile != null) {
            val strRefundDate = refundDate() + ""
            val strSmsText: String = "Dear customer,\n" +
                "Please click below link to complete the refund request of ${refundTransaction.refundFiatType} ${refundTransaction.refundAmountFiat?.stripTrailingZeros()?.toPlainString()} for the transaction ID ${refundTransaction.transaction.uuid} " +
                "at Dubai Duty Free on $strRefundDate ." +
                "\n" +
                "URL:-" + appConfig.getRefundFormUrl() + refundToken.transactionRefundToken

            emailSMSSenderService.sendMobileSMS(
                refundTransaction.refundUserMobile!!,
                strSmsText
            )
        }
    }

    private fun refundDate(): String? {
        val date = Date()
        val dateFmt = SimpleDateFormat("dd-MM-yyyy")
        return dateFmt.format(date)
    }
}
