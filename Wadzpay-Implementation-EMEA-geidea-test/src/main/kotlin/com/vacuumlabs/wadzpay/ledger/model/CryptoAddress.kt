package com.vacuumlabs.wadzpay.ledger.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToOne

@Entity
data class CryptoAddress(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    val id: Long = 0,

    val asset: String,

    var address: String,
) {
    @JsonIgnore
    @OneToOne(mappedBy = "address")
    lateinit var owner: Subaccount
}

@Repository
interface CryptoAddressRepository : CrudRepository<CryptoAddress, Long> {
    fun getByAddressAndAsset(address: String, asset: String): CryptoAddress?
    fun countByAddress(address: String): Long
}
