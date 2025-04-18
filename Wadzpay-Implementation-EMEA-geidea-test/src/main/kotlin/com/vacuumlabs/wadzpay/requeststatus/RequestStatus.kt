package com.vacuumlabs.wadzpay.requeststatus

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.user.UserAccount
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
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
import javax.persistence.Table

@Entity
@Table
data class RequestStatus(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_account_id", nullable = false)
    val userAccount: UserAccount,
    @Column(nullable = true, length = 160)
    val requesterName: String? = null,
    @Column(nullable = true, length = 160)
    val requesterEmail: String? = null,
    @Column(nullable = true, length = 160)
    val requesterPhone: String? = null,
    @Column(nullable = true, length = 160)
    val receiverName: String? = null,
    @Column(unique = true, nullable = false)
    val receiverEmail: String? = null,
    @Column(nullable = true, length = 160)
    val receiverPhone: String? = null,
    @Column(nullable = true, length = 160)
    val digitalCurrency: String? = null,
    @Column(nullable = true, length = 160)
    val amount: String? = null,
    @Column(nullable = true, length = 160)
    val fee: String? = null,
    @Column(nullable = true, length = 160)
    val walletAddress: String? = null,
    @Column(nullable = true, length = 160)
    val timeNotification: String? = null,
    @Column(nullable = true, length = 160)
    val title: String? = null,
    @Column(nullable = true, length = 160)
    val body: String? = null,
    @Column(nullable = true, length = 160)
    val uuid: String = UUID.randomUUID().toString(),
    @Enumerated(EnumType.STRING)
    var status: TransactionStatus,
    @Column(nullable = true, length = 160)
    val requestStatusId: String? = null,
)
data class UpdatePaymentRequest(
    val status: TransactionStatus,
    val id: Long
)

@Repository
interface RequestStatusRepository : CrudRepository<RequestStatus, Long> {
    fun findByRequesterEmail(requesterEmail: String?): List<RequestStatus>?
    fun findByReceiverEmail(receiverEmail: String?): List<RequestStatus>?
}
