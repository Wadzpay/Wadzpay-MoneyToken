package com.vacuumlabs.wadzpay.merchant.export

import CSVHeaderMappingHelper
import com.opencsv.CSVWriter
import com.opencsv.bean.StatefulBeanToCsv
import com.opencsv.bean.StatefulBeanToCsvBuilder
import com.vacuumlabs.wadzpay.ledger.model.hasDateGreaterOrEqualTo
import com.vacuumlabs.wadzpay.ledger.service.GetTransactionListRequest
import com.vacuumlabs.wadzpay.ledger.service.TransactionService
import com.vacuumlabs.wadzpay.merchant.model.MerchantRepository
import com.vacuumlabs.wadzpay.services.S3Service
import com.vacuumlabs.wadzpay.viewmodels.TransactionViewModel
import com.vacuumlabs.wadzpay.viewmodels.toViewModel
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date

@Service
class TransactionCSVExportService(
    val merchantRepository: MerchantRepository,
    val transactionService: TransactionService,
    val s3Service: S3Service
) {

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    fun exportTransactionsForAllMerchants() {

        val yesterdayInstant = Instant.now().minusSeconds((24 * 60 * 60).toLong())

        val dateToday = "-${SimpleDateFormat("yyyy-MM-dd").format(Date.from(Instant.now()))}.csv"

        val transactionsAll = transactionService.transactionRepository.findAll(hasDateGreaterOrEqualTo(yesterdayInstant))
            .map { it.toViewModel() }

        s3Service.writeTransactions("all-transactions$dateToday", exportTransactions(transactionsAll))

        merchantRepository.findAll().forEach {

            val transactions = transactionService.getTransactionViewModels(
                it,
                GetTransactionListRequest(dateFrom = yesterdayInstant)
            )

            val transactionsArray = exportTransactions(transactions)

            s3Service.writeTransactions(
                "transactions-${it.name}$dateToday",
                transactionsArray
            )
        }
    }

    fun exportTransactions(
        transactions: List<TransactionViewModel>
    ): ByteArray {

        /*Check why the next two lines are required in CSVHeaderMappingHelper source file*/
        val mappingStrategy = CSVHeaderMappingHelper<TransactionViewModel>()
        mappingStrategy.type = TransactionViewModel::class.java

        val os = ByteArrayOutputStream()
        val writer = OutputStreamWriter(os)

        val beanToCsv: StatefulBeanToCsv<TransactionViewModel> = StatefulBeanToCsvBuilder<TransactionViewModel>(writer)
            .withMappingStrategy(mappingStrategy)
            .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
            .build()

        beanToCsv.write(transactions)
        writer.flush()

        return os.toByteArray()
    }
}
