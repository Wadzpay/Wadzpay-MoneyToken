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
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.groups.Default

@Entity

data class MerchantAcquirer(
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = true, unique = true)
    @field:NotBlank(message = "Merchant Id must not be empty", groups = [FullDataValidation::class, Default::class])
    var merchantAcquirerId: String? = null,
    @Column
    var clientMerchantAcquirerId: String? = null,
    @Column(nullable = true, unique = true)
    @field:NotBlank(message = "Aggregator Id must not be empty", groups = [FullDataValidation::class, Default::class])
    var aggregatorPreferenceId: String? = null,
    @Column(nullable = true, unique = true)
    @field:NotBlank(message = "Institution Id must not be empty", groups = [FullDataValidation::class, Default::class])
    var insitutionPreferenceId: String? = null,
    @Column(nullable = true, unique = true)
    @field:NotBlank(message = "Merchant Group Id must not be empty", groups = [FullDataValidation::class, Default::class])
    var merchantGroupPreferenceId: String? = null,
    @Column(nullable = true, unique = true)
    @field:NotBlank(message = "Merchant Name must not be empty", groups = [FullDataValidation::class, Default::class])
    var merchantAcquirerName: String? = null,
    @Column(nullable = true, unique = true)
    var merchantAcquirerStatus: String? = null,
    @Column(nullable = false)
    var isParentBlocked: Boolean = false,
    @Column(nullable = false)
    var isParentDeActivated: Boolean = false,
    @Column(nullable = false)
    var isParentClosed: Boolean = false,
    @Column(nullable = false)
    var systemGenerated: Boolean = false,
    @Column(nullable = true, unique = true)
    var merchantAcquirerLogo: String? = null,
    @Column(nullable = true)
    var generatedFrom: String? = null,
    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @field:Valid
    var address: EntityAddress = EntityAddress(),
    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @field:Valid
    var adminDetails: EntityAdminDetails = EntityAdminDetails(),
    @field:Valid
    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    var bankDetails: EntityBankDetails = EntityBankDetails(),
    @field:Valid
    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    var contactDetails: EntityContactDetails = EntityContactDetails(),
    @field:Valid
    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    var info: EntityInfo = EntityInfo(),
    @field:Valid
    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    var others: EntityOthers = EntityOthers(),
    @JsonInclude()
    @Transient
    @CsvBindByName(column = "Errors")
    var error: String? = null,
    @JsonInclude()
    @Transient
    @CsvBindByName(column = "type")
    var type: String? = null

)

@Repository
interface MerchantAcquirerRepository : CrudRepository<MerchantAcquirer, Long> {
    fun findByMerchantGroupPreferenceId(merchantGroupPreferenceId: String): MutableList<MerchantAcquirer>?
    fun findByMerchantAcquirerId(merchantAcquirerId: String): MerchantAcquirer?
    @Query("FROM MerchantAcquirer WHERE aggregatorPreferenceId=:aggregatorPreferenceId and insitutionPreferenceId=:insitutionPreferenceId and merchantGroupPreferenceId=:merchantGroupPreferenceId order by  merchantAcquirerId")
    fun findByUniqueIdOrderByMerchantAcquirerId(aggregatorPreferenceId: String, insitutionPreferenceId: String, merchantGroupPreferenceId: String): MutableList<MerchantAcquirer>
    @Query(
        "SELECT NEW com.vacuumlabs.wadzpay.acquireradministration.model.MerchantAcquirerListing(ma,mg, i, a)  \n" +
            "FROM MerchantAcquirer ma \n" +
            "LEFT JOIN FETCH MerchantGroup mg ON ma.merchantGroupPreferenceId = mg .merchantGroupPreferenceId\n" +
            "LEFT JOIN FETCH Institution i ON ma.insitutionPreferenceId = i .institutionId\n" +
            "LEFT JOIN FETCH Aggregator a ON ma.aggregatorPreferenceId = a .aggregatorPreferenceId   WHERE  ma.aggregatorPreferenceId=:aggregatorPreferenceId and ma.insitutionPreferenceId=:insitutionPreferenceId  and ma.merchantGroupPreferenceId=:merchantGroupPreferenceId"
    )

    fun findByRecordsWithParent(aggregatorPreferenceId: String, insitutionPreferenceId: String, merchantGroupPreferenceId: String): MutableList<MerchantAcquirerListing>
    @Query("FROM MerchantAcquirer WHERE aggregatorPreferenceId=:aggregatorPreferenceId and insitutionPreferenceId=:insitutionPreferenceId and merchantGroupPreferenceId=:merchantGroupPreferenceId")
    fun findByUniqueId(aggregatorPreferenceId: String, insitutionPreferenceId: String, merchantGroupPreferenceId: String): MutableList<MerchantAcquirer>
    @Query("select m from MerchantAcquirer m where m.others.id=:id")
    fun getMerchantAcquirerByOthersId(id: Long): MerchantAcquirer?
}
