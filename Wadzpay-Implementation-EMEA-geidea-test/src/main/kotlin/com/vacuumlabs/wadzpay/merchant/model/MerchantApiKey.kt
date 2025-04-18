package com.vacuumlabs.wadzpay.merchant.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.repository.CrudRepository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
class MerchantApiKey(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    val merchant: Merchant,

    @Column(nullable = false)
    val apiKeySecretHash: String
) {
    @JsonIgnore
    var valid: Boolean = true

    fun apiKeyId(): String = "${merchant.name}_$id"
}

interface MerchantApiKeyRepository : CrudRepository<MerchantApiKey, Long>
