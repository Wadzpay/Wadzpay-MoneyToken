package com.vacuumlabs.wadzpay.pos

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "pos_salt")
data class PosSalt(
    val saltkey: String,
) {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @JsonIgnore
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "tx_id")
    var posTransaction: PosTransaction? = null
}
@Repository
interface PosSaltRepository : CrudRepository<PosSalt, Long> {
    //  fun getByTransactionIdPos(txId: Long):PosSalt
}
