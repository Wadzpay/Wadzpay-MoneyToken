package com.vacuumlabs.wadzpay.filter

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.wadzpay.user.UserAccount
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table
data class TransactionFilter(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_account_id", nullable = false)
    val userAccount: UserAccount,
    @Column(nullable = true, length = 160)
    val requesterEmail: String? = null,
    val dateFrom: String? = null,
    val dateTo: String? = null,
    val directoinFilter: String? = null,
    val typeFilter: String? = null,
    val otherFilter: String? = null,
    val statusFilter: String? = null,
    val digitalCurrency: String? = null,
    val optionOne: String? = null,
    val optionTwo: String? = null,

)

data class TransactionFilterRequest(
    val requesterId: Long,
    val requesterEmail: String,
    val dateFrom: String,
    val dateTo: String,
    val directoinFilter: String,
    val typeFilter: String,
    val statusFilter: String,
    val digitalCurrency: String
)

@Repository
interface TransactionFilterRepository : CrudRepository<TransactionFilter, Long> {
    fun findByRequesterEmail(requesterEmail: String): List<TransactionFilter>?
}
