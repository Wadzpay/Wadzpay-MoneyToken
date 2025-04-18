package com.vacuumlabs.wadzpay.issuance.models

import org.springframework.data.repository.CrudRepository
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
@Table(name = "issuance_banks_sub_bank_entry")
data class IssuanceBanksSubBankEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "issuance_banks_id", nullable = false)
    val issuanceBanksId: IssuanceBanks,

    @ManyToOne
    @JoinColumn(name = "parent_bank_id", nullable = false)
    val parentBankId: IssuanceBanks ? = null,

    val isAccessible: Boolean? = true,
    val isActive: Boolean? = true,
    val updatedAt: Instant? = null,
    val createdAt: Instant = Instant.now(),
)
@Repository
interface IssuanceBanksSubBankEntryRepository : CrudRepository<IssuanceBanksSubBankEntry, Long> {
    fun getByIssuanceBanksId(issuanceBanks: IssuanceBanks): IssuanceBanksSubBankEntry ?

    fun getByParentBankId(issuanceBanks: IssuanceBanks): MutableList<IssuanceBanksSubBankEntry> ?
}
