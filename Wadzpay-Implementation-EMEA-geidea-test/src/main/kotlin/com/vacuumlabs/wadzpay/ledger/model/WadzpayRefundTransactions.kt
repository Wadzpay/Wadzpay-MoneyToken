package com.vacuumlabs.wadzpay.ledger.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "wadzpay_refund_transactions_finance")
data class WadpayRefundTransactions(
    @JsonIgnore
    @ManyToOne
    @JoinColumn
    var owner: AccountOwner? = null,
    @Column(unique = true)
    val uuid: String = UUID.randomUUID().toString(),

    @OneToOne
    var transaction: TransactionRefundDetails? = null,

    @Enumerated(EnumType.STRING)
    var assetCrypto: CurrencyUnit,

    var address: String? = null,

    @Enumerated(EnumType.STRING)
    val type: TransactionType,
    var blockchainHash: String? = null,
    var description: String? = null,
    var amountCrypto: BigDecimal = BigDecimal.ZERO,
    var feeByWadzpay: BigDecimal = BigDecimal.ZERO,
    var feeByBlockchain: BigDecimal = BigDecimal.ZERO
) {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
    val createdAt: Instant = Instant.now()
}

@Repository
interface WadpayRefundTransactionsRepository : CrudRepository<WadpayRefundTransactions, Long> {
    fun findByTransactionId(transactionId: Long): WadpayRefundTransactions?
}
