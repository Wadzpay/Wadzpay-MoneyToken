package com.vacuumlabs.wadzpay.utils

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
@Entity
@Table(name = "block_confirmation_log")
data class BlockConfirmationLogger(
    @Column(unique = true)
    val hash: String,
    @Column(unique = true)
    val transferId: String,
    @Column(unique = true)
    val wallet: String?,
    @JsonIgnore
    @Enumerated(EnumType.STRING)
    val type: TransactionType,
    @Column(unique = true)
    var blockConfirmationCount: Int,
    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    val id: Long = 0
    @Column(nullable = true)
    var confirmationStatus: String? = null
    @Column(nullable = true)
    var confirmationSource: String? = null
}
@Repository
interface BlockConfirmationRepository : CrudRepository<BlockConfirmationLogger, Long> {
    fun getByTransferIdAndType(transferId: String, type: TransactionType): BlockConfirmationLogger?
}
