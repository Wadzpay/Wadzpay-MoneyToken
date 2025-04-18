package com.vacuumlabs.wadzpay.ledger.scheduler

import com.vacuumlabs.wadzpay.ledger.model.MerchantConfigRepository
import com.vacuumlabs.wadzpay.ledger.model.RefundAcceptRejectStatus
import com.vacuumlabs.wadzpay.ledger.model.RefundAcceptRejectType
import com.vacuumlabs.wadzpay.ledger.model.TransactionRefundDetailsRepository
import com.vacuumlabs.wadzpay.ledger.service.GetAcceptApproveRejectRequest
import com.vacuumlabs.wadzpay.ledger.service.TransactionService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RefundAutoApprovalProcessorScheduler(
    val transactionService: TransactionService,
    val transactionRefundDetailsRepository: TransactionRefundDetailsRepository,
    val merchantConfigRepository: MerchantConfigRepository
) {
    @Scheduled(initialDelayString = "\${scheduler.initialDelay}", fixedRateString = "\${scheduler.fixedRate}")
    fun processRefundAutoApproval() {
//        println("Auto Refund Scheduler Started At : " + Instant.now())
        val merchantConfigs = merchantConfigRepository.findAllByAutoRefundApprovalRequired(true)

        merchantConfigs.forEach {
            var dateTime = Instant.now()
            dateTime = dateTime.minusSeconds(it.autoRefundApproveSeconds)
            val refundTransactions = transactionRefundDetailsRepository.getRefundTransactionsForAutoApproval(dateTime, it.merchant!!.id)
//            println("refundTransactions  " + refundTransactions?.size + " For merchant " + it.merchant!!.id)
            refundTransactions?.forEach {
                val request = GetAcceptApproveRejectRequest(txn_uuid = it.transaction.uuid, status = RefundAcceptRejectStatus.ACCEPT, type = RefundAcceptRejectType.APPROVAL)
                val accountOwner = (it.transaction.sender.account.owner ?: it.transaction.receiver.account.owner)!!
                transactionService.updateOneLevelApproveRejectTransaction(request, accountOwner, null)
            }
//            println("Auto Refund Scheduler Completed At : " + Instant.now())
        }
    }
}
