package com.vacuumlabs.wadzpay.issuance.models

import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "issuance_wallet_fee_type")
data class IssuanceWalletFeeType(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var walletFeeId: String,

    var feeType: String,

    val createdAt: Instant = Instant.now(),

    val isActive: Boolean? = true,

    @ManyToOne
    @JoinColumn(name = "issuance_banks_id", nullable = false)
    var issuanceBanksId: IssuanceBanks? = null
)
@Repository
interface IssuanceWalletFeeTypeRepository :
    PagingAndSortingRepository<IssuanceWalletFeeType, Long>,
    JpaSpecificationExecutor<IssuanceWalletFeeType> {
    fun getByWalletFeeId(walletFeeId: String): IssuanceWalletFeeType ?
    fun getByIssuanceBanksId(issuanceBank: IssuanceBanks): List<IssuanceWalletFeeType>?
}
