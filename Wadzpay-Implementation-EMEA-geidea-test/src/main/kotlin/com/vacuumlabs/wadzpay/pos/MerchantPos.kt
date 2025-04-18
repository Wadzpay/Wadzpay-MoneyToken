package com.vacuumlabs.wadzpay.pos

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.wadzpay.merchant.model.Merchant
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Table
import kotlin.collections.ArrayList

@Entity
@Table(name = "merchant_pos")
data class MerchantPos(
    val posId: String,
    val posName: String,
    @JsonIgnore
    @ManyToOne
    val merchant: Merchant

) {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}

@Repository
interface MerchantPosRepository : CrudRepository<MerchantPos, Long> {
    fun findByMerchant(merchant: Merchant): ArrayList<MerchantPos>?
    fun findByPosIdAndMerchant(id: String, merchant: Merchant): MerchantPos?
    //  fun findByIdAndMerchant(id:Long,merchant: Merchant):MerchantPos?
}
