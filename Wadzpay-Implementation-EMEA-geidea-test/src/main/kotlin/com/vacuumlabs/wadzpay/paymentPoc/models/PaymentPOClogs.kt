package com.vacuumlabs.wadzpay.paymentPoc.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.wadzpay.user.UserAccount
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "payment_poc_logs")
data class PaymentPOClogs(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_account_id", nullable = false)
    var userAccount: UserAccount,
    var userEmail: String? = null,
    val transactionType: String? = null,
    val currCode: String? = null,
    var amount: BigDecimal,
    val merchantOrderToken: String? = null,
    val ordTitle: String? = null,
    val successUrl: String? = null,
    val failUrl: String? = null,
    val pgCancelUrl: String? = null,
    val queueIfLowBalance: String? = null,
    var status: String? = null,
    val responseData: String? = null,
    val transactionId: String? = null,
    val payId: String? = null,
    val referenceId: String? = null,
    var message: String? = null,
    val statusCode: String? = null,
    val refLink: String? = null,
    var orderId: String? = null,
    var paymentType: String? = null,
    var upi: String? = null,
    val accountNumber: String? = null,
    val ifscCode: String? = null,
    val beneficiaryName: String? = null,
    var paymentMode: String? = null,
    val updatedAt: Instant = Instant.now(),
    val createdAt: Instant = Instant.now(),
    var wadzpayTrxId: String? = null
)

@Repository
interface PaymentPOClogsRepository : CrudRepository<PaymentPOClogs, Long> {
    fun getByTransactionId(transactionId: String): PaymentPOClogs?

    fun getByWadzpayTrxId(wadzpayTrxId: String): PaymentPOClogs?
}
