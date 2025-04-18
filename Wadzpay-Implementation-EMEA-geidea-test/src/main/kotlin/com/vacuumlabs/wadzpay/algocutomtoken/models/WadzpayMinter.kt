package com.vacuumlabs.wadzpay.algocutomtoken.models

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "wadzpay_minter")
data class WadzpayMinter(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val wadzpayMinterId: Long = 0,

    val assetName: String,

    val assetMintAmount: BigDecimal = BigDecimal.ZERO,

    val assetMintBaseUnit: String ? = null,

    val assetUrl: String ? = null,

    val wadzpayMinterAddress: String
)
@Repository
interface WadzpayMinterRepository : CrudRepository<WadzpayMinter, Long> {
    fun getByAssetName(assetName: String): WadzpayMinter?
}
