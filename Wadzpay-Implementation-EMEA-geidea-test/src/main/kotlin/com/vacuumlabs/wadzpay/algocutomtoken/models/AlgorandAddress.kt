package com.vacuumlabs.wadzpay.ledger.model

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity(name = "wadzpayAlgoAddress")
@Table(name = "wadzpay_algo_addresses")
data class AlgorandAddress(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "algoaddress")
    val algoAddress: String,
    @Column(name = "algomnu")
    val algoMnu: String
) {
    // fun getById(id: Long): AlgorandAddress?
    // fun getByAlgoAddress(algoAdrs: String): AlgorandAddress?
}

@Repository
interface AlgorandAddressRepository : CrudRepository<AlgorandAddress, Long> {
    fun getByAlgoAddress(adrs: String): AlgorandAddress?
}
