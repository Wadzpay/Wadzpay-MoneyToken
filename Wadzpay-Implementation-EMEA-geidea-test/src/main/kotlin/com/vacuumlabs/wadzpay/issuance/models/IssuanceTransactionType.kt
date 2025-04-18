package com.vacuumlabs.wadzpay.issuance.models

import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "issuance_transaction_type")
data class IssuanceTransactionType(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var transactionTypeId: String,

    var transactionType: String,

    val createdAt: Instant = Instant.now(),

    val isActive: Boolean? = true,

    @ManyToOne
    @JoinColumn(name = "issuance_banks_id", nullable = false)
    var issuanceBanksId: IssuanceBanks? = null
)
@Repository
interface IssuanceTransactionTypeRepository :
    PagingAndSortingRepository<IssuanceTransactionType, Long>,
    JpaSpecificationExecutor<IssuanceTransactionType> {
    fun getByTransactionTypeId(transactionTypeId: String): IssuanceTransactionType ?

    fun getByIssuanceBanksId(issuanceBanksId: IssuanceBanks): List<IssuanceTransactionType>?
}
