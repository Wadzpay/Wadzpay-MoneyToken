package com.vacuumlabs.wadzpay.pos

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.wadzpay.bitgo.BitGoTransferType
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.webhook.BitGoCoin
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import kotlin.collections.ArrayList

@Entity
@Table(name = "pos_inwards_log")
data class PosLogCommits(
    var wallet: String,
    var address: String,
    val value: String,
    @Enumerated(EnumType.STRING)
    val assetCrypto: CurrencyUnit,
    @Enumerated(EnumType.STRING)
    val type: TransactionType,
    val blockId: String,
    val entries: String,
    val bitgoId: String,
    @Enumerated(EnumType.STRING)
    val bitgoCoin: BitGoCoin,
    val bitgoFee: String,
    @Enumerated(EnumType.STRING)
    val bitgoType: BitGoTransferType,
    val bitgoBaseValue: String
) {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    val createdAt: Instant = Instant.now()
}
@Repository
interface PosLogCommitsRepository : CrudRepository<PosLogCommits, Long> {
    fun getAllById(id: Long): ArrayList<PosLogCommits>
    fun getByBlockId(blockId: String): ArrayList<PosLogCommits?>?
}
