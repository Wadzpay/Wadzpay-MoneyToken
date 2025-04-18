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
@Table(name = "institution_management_field_config")
data class InstitutionManagementFieldConfig(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "issuance_banks_id", nullable = false)
    var issuanceBanksId: IssuanceBanks,

    var fieldNameKey: String ? = null,

    var isAllowShow: Boolean = false,

    var isAllowEdit: Boolean = false,

    var isMandatory: Boolean = false,

    var isActive: Boolean = true,

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    val createdBy: IssuanceBanks,

    var createdDate: Instant? = null,

    var modifiedDate: Instant? = null,

    @ManyToOne
    @JoinColumn(name = "modified_by", nullable = false)
    var modifiedBy: IssuanceBanks? = null
)
@Repository
interface InstitutionManagementFieldConfigRepository :
    PagingAndSortingRepository<InstitutionManagementFieldConfig, Long>,
    JpaSpecificationExecutor<InstitutionManagementFieldConfig> {
    fun getByIssuanceBanksId(issuanceBanksId: IssuanceBanks): MutableList<InstitutionManagementFieldConfig> ?
}
