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
@Table(name = "institution_user_management")
data class InstitutionUserManagement(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "issuance_banks_id", nullable = false)
    var issuanceBanksId: IssuanceBanks,

    var firstName: String ? = null,

    var middleName: String ? = null,

    var lastName: String ? = null,

    @Column(unique = true)
    var phoneNumber: String? = null,

    @Column(unique = true)
    var email: String? = null,

    @Column(unique = true)
    var cognitoUsername: String? = null,

    var department: String ? = null,

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    var roleId: InstitutionRoleMaster? = null,

    var isActive: Boolean = true,

    val createdDate: Instant = Instant.now(),

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    val createdBy: IssuanceBanks ? = null,

    var modifiedDate: Instant? = null,

    @ManyToOne
    @JoinColumn(name = "modified_by", nullable = false)
    var modifiedBy: IssuanceBanks? = null
)
@Repository
interface InstitutionUserManagementRepository :
    PagingAndSortingRepository<InstitutionUserManagement, Long>,
    JpaSpecificationExecutor<InstitutionUserManagement> {

    fun getByEmail(email: String?): InstitutionUserManagement?
    fun getById(id: Long): InstitutionUserManagement ?
}
