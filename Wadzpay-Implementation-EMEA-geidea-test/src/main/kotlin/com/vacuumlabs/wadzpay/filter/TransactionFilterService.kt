package com.vacuumlabs.wadzpay.filter

import com.vacuumlabs.wadzpay.user.UserAccount
import org.springframework.stereotype.Service

@Service
class TransactionFilterService(val transactionFilterRepository: TransactionFilterRepository) {

    fun saveTransactionFilter(
        userAccount: UserAccount,
        request: TransactionFilterRequest
    ): TransactionFilter {
        println("@16  saveTransactionFilter")
        val transactionFilterVal = TransactionFilter(
            userAccount = userAccount,
            requesterEmail = request.requesterEmail,
            dateFrom = request.dateFrom,
            dateTo = request.dateTo,
            directoinFilter = request.directoinFilter,
            typeFilter = request.typeFilter,
            statusFilter = request.statusFilter,
            digitalCurrency = request.digitalCurrency,
        )

        transactionFilterRepository.save(transactionFilterVal)
        return transactionFilterVal
    }

    fun getTransactionFilter(emailParam: String, userAccount: UserAccount): List<TransactionFilter>? {
        println("@47 $emailParam")
        /*val transactionFilterData1 = transactionFilterRepository.findAll().toString()
        println("@53 $transactionFilterData1")
        var transactionFilterVal: MutableList<TransactionFilter> = mutableListOf()
        transactionFilterVal = transactionFilterRepository.findAll() as MutableList<TransactionFilter>
        println("@55 $transactionFilterVal")*/

        val transactionFilterVal2 = transactionFilterRepository.findByRequesterEmail(emailParam)
        println("@47 $transactionFilterVal2")
        return transactionFilterVal2
    }
}
