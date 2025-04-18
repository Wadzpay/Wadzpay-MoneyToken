package com.vacuumlabs.wadzpay.gap600

import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.gap600.models.Gap600Message
import com.vacuumlabs.wadzpay.ledger.LedgerService
import com.vacuumlabs.wadzpay.utils.ApisLoggerRepository
import com.vacuumlabs.wadzpay.utils.BlockConfirmationRepository
import com.vacuumlabs.wadzpay.webhook.BitGoCoin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

// This service class holds the functionality to call Gap600 component.
// Any business logic around Gap600 can be implemented in this class.
@Service
class Gap600Service(
    val gap600Cmpnt: Gap600Cmpnt,
    val ledgerService: LedgerService,
    val appConfig: AppConfig,
    val blockConfirmationRepository: BlockConfirmationRepository,
    val apisLoggerRepository: ApisLoggerRepository
) {

    val logger: Logger = LoggerFactory.getLogger(javaClass)
    // getConfirmationStatus retrieves confirmation status from Gap600 component
    // input parameters : Cointype, TransactionHash & XOutputAddress
    // returns Gap600Message object
    fun getConfirmationStatus(coin: BitGoCoin, TransactionHash: String, xoutputaddress: String): Gap600Message ? {
        val gap600ConfirmationStatus = gap600Cmpnt.findGap600TransactionStatus(coin, TransactionHash, xoutputaddress)?.get()
        val gap600Status = if (gap600ConfirmationStatus != null) gap600ConfirmationStatus else null
        logger.info("after calling gap 600api")
        return gap600Status
    }
}
