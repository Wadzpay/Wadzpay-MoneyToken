package com.vacuumlabs.wadzpay.acquireradministration.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.opencsv.bean.CsvBindByName
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
@Table(name = "merchant_group")
data class MerchantGroup(
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) {
    @field:NotBlank(message = "Aggregator Id must not be empty", groups = [FullDataValidation::class, Default::class])
    @Column(nullable = true, unique = true)
    var aggregatorPreferenceId: String? = null
    @field:NotBlank(message = "Institution Id must not be empty", groups = [FullDataValidation::class, Default::class])
    @Column(nullable = true, unique = true)
    var insitutionPreferenceId: String? = null
    @field:NotBlank(message = "Merchant Group Id must not be empty", groups = [FullDataValidation::class, Default::class])
    @Column(nullable = true, unique = true)
    var merchantGroupPreferenceId: String? = null
    @Column(nullable = true)
    var clientMerchantGroupId: String? = null
    @field:NotBlank(message = "Merchant group name must not be empty", groups = [FullDataValidation::class, Default::class])
    @Column(nullable = true, unique = true)
    var merchantGroupName: String? = null
    @Column(nullable = true, unique = true)
    var merchantGroupStatus: String? = null
    @Column(nullable = false)
    var isParentBlocked: Boolean = false
    @Column(nullable = false)
    var isParentDeActivated: Boolean = false
    @Column(nullable = false)
    var isParentClosed: Boolean = false
    @Column(nullable = false)

    var systemGenerated: Boolean = false
    @Column(nullable = true)
    var generatedFrom: String? = null
    @Column(nullable = true, unique = true)
    var merchantGroupLogo: String? = null

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @field:Valid
    var address: EntityAddress = EntityAddress()

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @field:Valid
    var adminDetails: EntityAdminDetails = EntityAdminDetails()

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @field:Valid
    var bankDetails: EntityBankDetails = EntityBankDetails()

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @field:Valid
    var contactDetails: EntityContactDetails = EntityContactDetails()

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @field:Valid
    var info: EntityInfo = EntityInfo()

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
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
interface MerchantGroupRepository : CrudRepository<MerchantGroup, Long> {
    fun findByAggregatorPreferenceId(aggregatorPreferenceId: String): MutableList<MerchantGroup>
    fun findByInsitutionPreferenceId(insitutionPreferenceId: String): MutableList<MerchantGroup>?
    @Query(
        "SELECT NEW com.vacuumlabs.wadzpay.acquireradministration.model.MerchantGroupListing(m, i, a)  \n" +
            "FROM MerchantGroup m \n" +
            "LEFT JOIN FETCH Institution i ON m.insitutionPreferenceId = i .institutionId\n" +
            "LEFT JOIN FETCH Aggregator a ON m.aggregatorPreferenceId = a .aggregatorPreferenceId   WHERE m.aggregatorPreferenceId=:aggregatorPreferenceId and m.insitutionPreferenceId=:insitutionPreferenceId "
    )

    fun findByRecordsWithParent(insitutionPreferenceId: String, aggregatorPreferenceId: String): MutableList<MerchantGroupListing>
    @Query("FROM MerchantGroup WHERE aggregatorPreferenceId=:aggregatorPreferenceId and insitutionPreferenceId=:insitutionPreferenceId")
    fun findByUniqueId(insitutionPreferenceId: String, aggregatorPreferenceId: String): MutableList<MerchantGroup>
    @Query("FROM MerchantGroup WHERE aggregatorPreferenceId=:aggregatorPreferenceId and insitutionPreferenceId=:insitutionPreferenceId order by merchantGroupPreferenceId")
    fun findByUniqueIdOrderByMerchantGroupPreferecneId(insitutionPreferenceId: String, aggregatorPreferenceId: String): MutableList<MerchantGroup>
    fun getMerchantGroupById(id: Long): MerchantGroup
    fun findByMerchantGroupPreferenceId(merchantGroupPreferecneId: String): MerchantGroup?
    @Query("select m from MerchantGroup m where m.others.id=:id")
    fun getMerchantGroupByOthersId(id: Long): MerchantGroup?
    fun getByMerchantGroupPreferenceId(merchantGroupPreferecneId: String): MerchantGroup?
}
