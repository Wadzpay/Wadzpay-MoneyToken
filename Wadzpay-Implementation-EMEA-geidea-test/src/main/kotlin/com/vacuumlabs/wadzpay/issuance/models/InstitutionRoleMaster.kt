package com.vacuumlabs.wadzpay.issuance.models

import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "institution_role_master")
data class InstitutionRoleMaster(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    var roleName: String,

    var isActive: Boolean = true,

    val createdDate: Instant = Instant.now(),

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    val createdBy: IssuanceBanks,

    var modifiedDate: Instant? = null,

    @ManyToOne
    @JoinColumn(name = "modified_by", nullable = false)
    var modifiedBy: IssuanceBanks? = null
)
@Repository
interface InstitutionRoleMasterRepository :
    PagingAndSortingRepository<InstitutionRoleMaster, Long>,
    JpaSpecificationExecutor<InstitutionRoleMaster> {
    fun getByRoleName(roleName: String): InstitutionRoleMaster ?
}
