package com.vacuumlabs.wadzpay.ledger.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "refund_token")
data class RefundToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var validFor: Instant = Instant.now(),
    var transactionRefundToken: UUID = UUID.randomUUID(),
    var count: Int = 1,
    var isExpired: Boolean? = false,
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_transaction_id")
    var refundTransaction: TransactionRefundDetails? = null
) {

    var created_at: Instant = Instant.now()
}

@Repository
interface RefundTokenRepository : CrudRepository<RefundToken, Long> {
    fun findByTransactionRefundToken(transactionRefundToken: UUID): RefundToken?

    // @Query("FROM RefundToken WHERE (isExpired is null or isExpired = false) and validFor < now()")
    @Query("FROM RefundToken WHERE (isExpired is null or isExpired = false) and validFor < now() and refundTransaction.refundInitiatedFrom = :merchantId")
    fun getExpiredTokens(merchantId: Long): List<RefundToken>?

    @Modifying
    @Query("update refund_token set is_expired = true where is_expired = false and refund_transaction_id = :refundId", nativeQuery = true)
    fun updateTokenToExpire(refundId: Long)
}
