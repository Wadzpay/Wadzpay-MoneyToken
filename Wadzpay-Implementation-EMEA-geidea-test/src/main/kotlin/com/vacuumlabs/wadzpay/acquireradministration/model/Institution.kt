package com.vacuumlabs.wadzpay.acquireradministration.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvRecurse
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToOne
import javax.persistence.Table
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.groups.Default

@Entity
@Table(name = "institution")
data class Institution(
    @JsonIgnore
    @Id
    @Column(updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) {
    @Column(unique = true)
    @CsvBindByName
    @field:NotBlank(message = "Instituion Id must not be empty", groups = [FullDataValidation::class, Default::class])
    var institutionId: String? = null

    @Column(unique = true)
    @CsvBindByName
    @field:NotBlank(message = "Aggregator Id must not be empty", groups = [FullDataValidation::class, Default::class])
    var aggregatorPreferenceId: String? = null

    @Column(nullable = true, unique = true)
    @CsvBindByName
/*
    @field:NotBlank(message = "Client Institution Id must not be empty", groups = [FullDataValidation::class, Default::class])
*/
    var insitutionPreferenceId: String? = null
    @Column(unique = true)
    @CsvBindByName
    @field:NotBlank(message = "Institution Name must not be empty", groups = [FullDataValidation::class, Default::class])
    var insitutionName: String? = null
    @Column(nullable = false)
    @CsvBindByName
    var insitutionStatus: String = "active"
    @Column(nullable = false)
    var isParentBlocked: Boolean = false
    @Column(nullable = false)
    var isParentDeActivated: Boolean = false
    @Column(nullable = false)
    var isParentClosed: Boolean = false
    @Column(nullable = true, unique = true)
    @CsvBindByName
    var institutionLogo: String? = null
    @Column(nullable = false)
    var systemGenerated: Boolean = false
    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @CsvRecurse
    @field:Valid
    var address: EntityAddress = EntityAddress()

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @CsvRecurse
    @field:Valid
    var adminDetails: EntityAdminDetails = EntityAdminDetails()

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @CsvRecurse
    @field:Valid
    var bankDetails: EntityBankDetails = EntityBankDetails()
    @field:Valid
    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    var contactDetails: EntityContactDetails = EntityContactDetails()

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @CsvRecurse
    @field:Valid
    var info: EntityInfo = EntityInfo()

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @CsvRecurse
    @field:Valid
    var others: EntityOthers = EntityOthers()
    @JsonInclude()
    @Transient
    @CsvBindByName(column = "Errors")
    var error: String? = null
    @JsonInclude()
    @Transient
    @CsvBindByName(column = "type")
    var type: String? = null
}
@Repository
interface InstitutionRepository : CrudRepository<Institution, Long> {
    fun findByAggregatorPreferenceId(aggregatorPreferenceId: String): MutableList<Institution>?
    fun getInstitutionById(id: Long): Institution
    @Query("select i from Institution i where i.others.id=:id")
    fun getInstitutionByOthersId(id: Long): Institution?
    fun findByAggregatorPreferenceIdOrderByInstitutionId(aggregatorPreferenceId: String): MutableList<Institution>
    fun findByInstitutionId(institutionId: String): Institution?
    fun getByInstitutionId(institutionId: String): Institution
    fun getInstitutionByAggregatorPreferenceId(aggregatorPreferenceId: String): MutableList<Institution>
}
