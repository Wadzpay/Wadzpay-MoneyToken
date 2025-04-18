package com.vacuumlabs.wadzpay.ledger.model

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "refund_form_fields_config")
class RefundFormFields {
    @Id
    val formName: String = ""

    @Column(nullable = true, unique = true)
    var txnReference: Boolean? = null

    @Column(nullable = true, unique = true)
    var customerName: Boolean? = null

    @Column(nullable = true, unique = true)
    var mobile: Boolean? = null

    @Column(nullable = true, unique = true)
    var email: Boolean? = null

    @Column(nullable = true, unique = true)
    var digitalAmt: Boolean? = null

    @Column(nullable = true, unique = true)
    var digitalName: Boolean? = null

    @Column(nullable = true, unique = true)
    var refundAmountInFiat: Boolean? = null

    @Column(nullable = true, unique = true)
    var refundAmountInCrypto: Boolean? = null

    @Column(nullable = true, unique = true)
    var reason: Boolean? = null

    @Column(nullable = true, unique = true)
    var srcWalletAddr: Boolean? = null

    @Column(nullable = true, unique = true)
    var walletAddr: Boolean? = null

    @Column(nullable = true, unique = true)
    var confirmWalletAddr: Boolean? = null
}

@Repository
interface RefundFormRepository : CrudRepository<RefundFormFields, Long> {

    fun findByFormName(formName: String): RefundFormFields
}
